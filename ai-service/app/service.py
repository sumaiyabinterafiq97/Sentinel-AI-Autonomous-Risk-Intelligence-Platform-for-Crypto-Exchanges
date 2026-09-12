"""Assistive agents. Deterministic local generator — not a hosted LLM."""

from __future__ import annotations

import time
from typing import Any
from uuid import UUID

from app.auth import AccessPrincipal
from app.errors import not_found, validation
from app.events import AiEventPublisher, iso
from app.store import AgentRun, AiStore, Prompt, PromptVersion, Recommendation, new_id, utcnow
from app.tools import (
    TOOL_EMBED,
    TOOL_GET_ALERT,
    TOOL_GET_ASSESSMENT,
    TOOL_GET_CASE,
    TOOL_GET_RULE_HITS,
    TOOL_LIST_EVIDENCE,
    TOOL_SEARCH,
    ToolDenied,
    ToolRegistry,
)

MODEL_ID = "sentinel-deterministic-assist-simulation"


def infer_agent_type(name: str) -> str:
    lowered = name.lower()
    if "risk" in lowered:
        return "risk"
    if "retriev" in lowered:
        return "retrieval"
    return "investigation"


def format_content(layers: dict[str, str]) -> str:
    return (
        f"SYSTEM: {layers['system']}\n"
        f"AI: {layers['ai']}\n"
        f"EVIDENCE: {layers['evidence']}\n"
        f"HUMAN: {layers['human']}"
    )


def job_http_status(rec_status: str) -> str:
    if rec_status == "failed":
        return "failed"
    if rec_status == "pending":
        return "pending"
    return "completed"


class AiService:
    def __init__(self, store: AiStore, tools: ToolRegistry, publisher: AiEventPublisher) -> None:
        self.store = store
        self.tools = tools
        self.publisher = publisher

    def assist_investigation(
        self,
        *,
        principal: AccessPrincipal,
        organization_id: UUID,
        correlation_id: UUID | None,
        case_id: str,
        query: str | None,
        idempotency_key: str | None,
    ) -> dict[str, Any]:
        return self._assist(
            principal=principal,
            organization_id=organization_id,
            correlation_id=correlation_id,
            agent_type="investigation",
            target_type="case",
            target_id=case_id,
            query=query,
            idempotency_key=idempotency_key,
        )

    def assist_risk(
        self,
        *,
        principal: AccessPrincipal,
        organization_id: UUID,
        correlation_id: UUID | None,
        assessment_id: str,
        idempotency_key: str | None,
    ) -> dict[str, Any]:
        return self._assist(
            principal=principal,
            organization_id=organization_id,
            correlation_id=correlation_id,
            agent_type="risk",
            target_type="assessment",
            target_id=assessment_id,
            query=None,
            idempotency_key=idempotency_key,
        )

    def assist_retrieve(
        self,
        *,
        principal: AccessPrincipal,
        organization_id: UUID,
        correlation_id: UUID | None,
        query: str,
        case_id: str | None,
        idempotency_key: str | None,
    ) -> dict[str, Any]:
        target = case_id or "query"
        return self._assist(
            principal=principal,
            organization_id=organization_id,
            correlation_id=correlation_id,
            agent_type="retrieval",
            target_type="retrieval" if case_id is None else "case",
            target_id=target,
            query=query,
            idempotency_key=idempotency_key,
        )

    def get_recommendation(self, organization_id: UUID, recommendation_id: UUID) -> Recommendation:
        rec = self.store.recommendations.get(recommendation_id)
        if rec is None or rec.organization_id != organization_id:
            raise not_found()
        return rec

    def list_prompts(self, organization_id: UUID, cursor: str | None, limit: int) -> tuple[list[dict[str, Any]], bool]:
        rows = [p for p in self.store.prompts.values() if p.organization_id == organization_id]
        rows.sort(key=lambda p: p.created_at)
        start = 0
        if cursor:
            for i, prompt in enumerate(rows):
                if str(prompt.id) == cursor:
                    start = i + 1
                    break
        page = rows[start : start + limit]
        has_more = start + limit < len(rows)
        return [self._prompt_api(p) for p in page], has_more

    def create_prompt(
        self,
        *,
        principal: AccessPrincipal,
        organization_id: UUID,
        correlation_id: UUID | None,
        name: str,
        template: str,
    ) -> dict[str, Any]:
        if not name.strip() or not template.strip():
            raise validation("name", "name and template are required")
        agent_type = infer_agent_type(name)
        for existing in self.store.prompts.values():
            if (
                existing.organization_id == organization_id
                and existing.agent_type == agent_type
                and existing.name == name
            ):
                raise validation("name", "prompt name already exists for agent type")
        prompt = Prompt(
            id=new_id(),
            organization_id=organization_id,
            agent_type=agent_type,
            name=name,
            active_version_id=None,
            created_at=utcnow(),
            updated_at=utcnow(),
        )
        version = PromptVersion(
            id=new_id(),
            prompt_id=prompt.id,
            version=1,
            content=template,
            model_hint=MODEL_ID,
            created_by=principal.user_id,
            created_at=utcnow(),
        )
        prompt.active_version_id = version.id
        self.store.prompts[prompt.id] = prompt
        self.store.prompt_versions[version.id] = version
        self.publisher.publish(
            event_type="PromptUpdated",
            payload={
                "promptId": str(prompt.id),
                "agentType": agent_type,
                "version": 1,
                "updatedAt": iso(),
                "updatedBy": str(principal.user_id),
            },
            organization_id=organization_id,
            correlation_id=correlation_id,
        )
        self.store.relay_pending()
        return self._prompt_api(prompt)

    def patch_prompt(
        self,
        *,
        principal: AccessPrincipal,
        organization_id: UUID,
        name: str | None,
        template: str | None,
    ) -> dict[str, Any]:
        if not name:
            raise validation("name", "name is required to identify the prompt")
        prompt = next(
            (
                p
                for p in self.store.prompts.values()
                if p.organization_id == organization_id and p.name == name
            ),
            None,
        )
        if prompt is None:
            raise not_found()
        if template:
            versions = [v for v in self.store.prompt_versions.values() if v.prompt_id == prompt.id]
            next_version = max(v.version for v in versions) + 1
            version = PromptVersion(
                id=new_id(),
                prompt_id=prompt.id,
                version=next_version,
                content=template,
                model_hint=MODEL_ID,
                created_by=principal.user_id,
                created_at=utcnow(),
            )
            self.store.prompt_versions[version.id] = version
            prompt.active_version_id = version.id
            prompt.updated_at = utcnow()
            self.publisher.publish(
                event_type="PromptUpdated",
                payload={
                    "promptId": str(prompt.id),
                    "agentType": prompt.agent_type,
                    "version": next_version,
                    "updatedAt": iso(),
                    "updatedBy": str(principal.user_id),
                },
                organization_id=organization_id,
                correlation_id=None,
            )
            self.store.relay_pending()
        return self._prompt_api(prompt)

    def _prompt_api(self, prompt: Prompt) -> dict[str, Any]:
        template = ""
        if prompt.active_version_id:
            version = self.store.prompt_versions.get(prompt.active_version_id)
            if version:
                template = version.content
        return {"id": str(prompt.id), "name": prompt.name, "template": template}

    def _assist(
        self,
        *,
        principal: AccessPrincipal,
        organization_id: UUID,
        correlation_id: UUID | None,
        agent_type: str,
        target_type: str,
        target_id: str,
        query: str | None,
        idempotency_key: str | None,
    ) -> dict[str, Any]:
        if not target_id:
            raise validation("targetId", "target identifier is required")
        if idempotency_key:
            existing_id = self.store.idempotency.get((organization_id, idempotency_key))
            if existing_id:
                rec = self.store.recommendations[existing_id]
                run = next(r for r in self.store.runs.values() if r.recommendation_id == rec.id)
                return self._job(run, rec)

        started = time.perf_counter()
        run = AgentRun(
            id=new_id(),
            recommendation_id=None,
            organization_id=organization_id,
            status="running",
            latency_ms=None,
            token_usage={"model": MODEL_ID, "mode": "simulation"},
            correlation_id=correlation_id,
            error_code=None,
            started_at=utcnow(),
            completed_at=None,
        )
        self.store.runs[run.id] = run
        prompt_version_id = self._active_prompt_version(organization_id, agent_type)
        rec = Recommendation(
            id=new_id(),
            organization_id=organization_id,
            agent_type=agent_type,
            target_type=target_type,
            target_id=target_id,
            content={},
            provenance={},
            prompt_version_id=prompt_version_id,
            model_id=MODEL_ID,
            status="pending",
            created_at=utcnow(),
        )
        run.recommendation_id = rec.id
        self.store.recommendations[rec.id] = rec
        if idempotency_key:
            self.store.remember_idempotency(organization_id, idempotency_key, rec.id)

        layers, provenance, rec_status = self._generate(
            agent_type=agent_type,
            principal=principal,
            organization_id=organization_id,
            target_id=target_id,
            query=query,
            run_log=run.tool_log,
        )
        rec.content = {
            "text": format_content(layers),
            "layers": layers,
            "assistiveOnly": True,
        }
        rec.provenance = provenance
        rec.status = rec_status
        run.status = rec_status
        run.completed_at = utcnow()
        run.latency_ms = int((time.perf_counter() - started) * 1000)
        self.publisher.publish(
            event_type="AIRecommendationGenerated",
            payload={
                "recommendationId": str(rec.id),
                "agentType": agent_type,
                "targetType": target_type,
                "targetId": target_id,
                "status": rec_status if rec_status in {"completed", "partial", "failed"} else "failed",
                "generatedAt": iso(),
                "promptVersionId": None if prompt_version_id is None else str(prompt_version_id),
            },
            organization_id=organization_id,
            correlation_id=correlation_id,
        )
        self.store.relay_pending()
        return self._job(run, rec)

    def _active_prompt_version(self, organization_id: UUID, agent_type: str) -> UUID | None:
        for prompt in self.store.prompts.values():
            if prompt.organization_id == organization_id and prompt.agent_type == agent_type:
                return prompt.active_version_id
        return None

    def _generate(
        self,
        *,
        agent_type: str,
        principal: AccessPrincipal,
        organization_id: UUID,
        target_id: str,
        query: str | None,
        run_log: list[dict[str, Any]],
    ) -> tuple[dict[str, str], dict[str, Any], str]:
        sources: list[dict[str, Any]] = []
        facts: list[str] = []
        missing = False

        def call(tool_id: str, arguments: dict[str, Any]) -> dict[str, Any] | None:
            try:
                return self.tools.invoke(
                    agent_type=agent_type,
                    tool_id=tool_id,
                    principal=principal,
                    organization_id=organization_id,
                    arguments=arguments,
                    run_log=run_log,
                )
            except ToolDenied:
                return None

        if agent_type == "investigation":
            case = call(TOOL_GET_CASE, {"caseId": target_id})
            if not case or not case.get("found"):
                missing = True
            else:
                data = case["data"] or {}
                facts.append(f"caseId={target_id}")
                if data.get("status"):
                    facts.append(f"status={data.get('status')}")
                sources.append({"kind": "SYSTEM", "ref": f"case:{target_id}"})
            evidence = call(TOOL_LIST_EVIDENCE, {"caseId": target_id})
            if evidence and evidence.get("found"):
                sources.append({"kind": "EVIDENCE", "ref": f"case_evidence:{target_id}"})
                facts.append(f"evidenceCount={len(evidence.get('data') or [])}")
            alert_id = None
            if case and case.get("data"):
                alert_id = (case["data"] or {}).get("sourceAlertId") or (case["data"] or {}).get("alertId")
            if alert_id:
                alert = call(TOOL_GET_ALERT, {"alertId": str(alert_id)})
                if alert and alert.get("found"):
                    sources.append({"kind": "SYSTEM", "ref": f"alert:{alert_id}"})
            if principal.has("ai:retrieve:execute"):
                retrieved = call(TOOL_SEARCH, {"query": query or target_id})
                if retrieved and retrieved.get("data"):
                    for hit in retrieved["data"]:
                        sources.append(
                            {
                                "kind": "EVIDENCE",
                                "ref": f"{hit.get('sourceType')}:{hit.get('sourceId')}",
                            }
                        )
            human = "Review in INVEST; AI cannot close, assign, or attach evidence."
            if missing:
                layers = {
                    "system": "No authorized case context is available for this tenant.",
                    "ai": "Investigation assistance is incomplete. No case facts were invented.",
                    "evidence": "No sources returned — verify manually.",
                    "human": human,
                }
                return layers, {"sources": sources, "grounded": False}, "partial"
            layers = {
                "system": "; ".join(facts) if facts else f"caseId={target_id}",
                "ai": (
                    "Suggested follow-ups: confirm timeline, related alerts, and evidence completeness. "
                    "Assistive only."
                ),
                "evidence": "; ".join(s["ref"] for s in sources) or "No sources returned — verify manually.",
                "human": human,
            }
            return layers, {"sources": sources, "grounded": True}, "completed"

        if agent_type == "risk":
            assessment = call(TOOL_GET_ASSESSMENT, {"assessmentId": target_id})
            hits = call(TOOL_GET_RULE_HITS, {"assessmentId": target_id})
            if not assessment or not assessment.get("found"):
                layers = {
                    "system": "No authorized risk assessment is available for this tenant.",
                    "ai": "Risk explanation cannot be grounded. No score was invented.",
                    "evidence": "No sources returned — verify manually.",
                    "human": "Use RISK APIs for authoritative scores. AI cannot change scores or create alerts.",
                }
                return layers, {"sources": [], "grounded": False}, "partial"
            data = assessment["data"] or {}
            score = data.get("score")
            sources.append({"kind": "SYSTEM", "ref": f"assessment:{target_id}"})
            hit_list = [] if hits is None else hits.get("data") or []
            if hit_list:
                sources.append({"kind": "SYSTEM", "ref": f"ruleHits:{target_id}"})
            layers = {
                "system": f"assessmentId={target_id}; score={score}",
                "ai": (
                    f"The recorded system score is {score}. This narrative does not replace deterministic RISK scoring."
                ),
                "evidence": "; ".join(s["ref"] for s in sources),
                "human": "Use RISK/ALERT APIs for any score or alert action. AI cannot publish RiskCalculated.",
            }
            return layers, {"sources": sources, "grounded": True, "systemScore": score}, "completed"

        retrieved = call(TOOL_SEARCH, {"query": query or target_id})
        embedded = call(TOOL_EMBED, {"query": query or target_id})
        hits = []
        if retrieved and retrieved.get("data"):
            hits.extend(retrieved["data"])
        elif embedded and embedded.get("data"):
            hits.extend(embedded["data"])
        for hit in hits:
            sources.append({"kind": "EVIDENCE", "ref": f"{hit.get('sourceType')}:{hit.get('sourceId')}"})
        if not hits:
            layers = {
                "system": "Tenant retrieval index returned no permitted documents.",
                "ai": "No documents were fabricated.",
                "evidence": "No sources returned — verify manually.",
                "human": "Attach evidence only via INVEST APIs. Retrieval is not attach.",
            }
            return layers, {"sources": [], "grounded": False}, "partial"
        layers = {
            "system": f"query={query or target_id}",
            "ai": "Retrieved tenant-scoped candidates. Citations below are index metadata, not new evidence.",
            "evidence": "; ".join(s["ref"] for s in sources),
            "human": "Select and attach via INVEST if appropriate. AI cannot attach evidence.",
        }
        return layers, {"sources": sources, "grounded": True, "hits": hits}, "completed"

    def _job(self, run: AgentRun, rec: Recommendation) -> dict[str, Any]:
        return {
            "jobId": str(run.id),
            "recommendationId": str(rec.id),
            "status": job_http_status(rec.status),
        }
