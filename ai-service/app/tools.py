"""Read-only tool registry. No domain lifecycle mutations."""

from __future__ import annotations

import hashlib
from typing import Any
from uuid import UUID

from app.auth import AccessPrincipal
from app.embeddings import cosine, embed, looks_like_injection
from app.store import AiStore, EmbeddingChunk, new_id, utcnow

TOOL_GET_CASE = "TOOL-INVEST-CASE-GET"
TOOL_LIST_EVIDENCE = "TOOL-INVEST-EVIDENCE-LIST"
TOOL_GET_ALERT = "TOOL-ALERT-GET"
TOOL_GET_ASSESSMENT = "TOOL-RISK-ASSESSMENT-GET"
TOOL_GET_RULE_HITS = "TOOL-RISK-RULE-HITS-GET"
TOOL_SEARCH = "TOOL-RETRIEVE-SEARCH"
TOOL_EMBED = "TOOL-RETRIEVE-EMBED"

FORBIDDEN_TOOLS = frozenset(
    {
        "CreateAlert",
        "CloseAlert",
        "SetAlertPriority",
        "CreateCase",
        "CloseCase",
        "AssignCase",
        "ApproveCompliance",
        "DisposeSanctions",
        "PublishRiskCalculated",
        "SetRiskScore",
        "RevokeSession",
        "ChangeRoles",
        "CrossTenantSearch",
    }
)

ALLOWLIST: dict[str, frozenset[str]] = {
    "investigation": frozenset(
        {TOOL_GET_CASE, TOOL_LIST_EVIDENCE, TOOL_GET_ALERT, TOOL_GET_ASSESSMENT, TOOL_SEARCH}
    ),
    "risk": frozenset({TOOL_GET_ASSESSMENT, TOOL_GET_RULE_HITS}),
    "retrieval": frozenset({TOOL_SEARCH, TOOL_EMBED}),
}

TOOL_PERMISSION: dict[str, str] = {
    TOOL_GET_CASE: "invest:case:read",
    TOOL_LIST_EVIDENCE: "invest:case:read",
    TOOL_GET_ALERT: "alert:alert:read",
    TOOL_GET_ASSESSMENT: "risk:assessment:read",
    TOOL_GET_RULE_HITS: "risk:assessment:read",
    TOOL_SEARCH: "ai:retrieve:execute",
    TOOL_EMBED: "ai:retrieve:execute",
}


class ToolDenied(Exception):
    def __init__(self, tool_id: str, reason: str) -> None:
        super().__init__(reason)
        self.tool_id = tool_id
        self.reason = reason


class ToolRegistry:
    def __init__(self, store: AiStore) -> None:
        self.store = store

    def invoke(
        self,
        *,
        agent_type: str,
        tool_id: str,
        principal: AccessPrincipal,
        organization_id: UUID,
        arguments: dict[str, Any],
        run_log: list[dict[str, Any]],
    ) -> dict[str, Any]:
        if tool_id in FORBIDDEN_TOOLS:
            run_log.append({"toolId": tool_id, "outcome": "denied", "reason": "forbidden"})
            raise ToolDenied(tool_id, "forbidden tool")
        allowed = ALLOWLIST.get(agent_type, frozenset())
        if tool_id not in allowed:
            run_log.append({"toolId": tool_id, "outcome": "denied", "reason": "not-allowlisted"})
            raise ToolDenied(tool_id, "tool not allowlisted for agent")
        required = TOOL_PERMISSION.get(tool_id)
        if required and not principal.has(required):
            run_log.append({"toolId": tool_id, "outcome": "denied", "reason": "authz"})
            raise ToolDenied(tool_id, "missing permission")
        if arguments.get("organizationId") and str(arguments["organizationId"]) != str(organization_id):
            run_log.append({"toolId": tool_id, "outcome": "denied", "reason": "cross-tenant"})
            raise ToolDenied(tool_id, "cross-tenant tool argument")

        result = self._execute(tool_id, organization_id, arguments)
        run_log.append({"toolId": tool_id, "outcome": "ok"})
        return result

    def _execute(self, tool_id: str, organization_id: UUID, arguments: dict[str, Any]) -> dict[str, Any]:
        if tool_id == TOOL_GET_CASE:
            case_id = str(arguments.get("caseId") or "")
            payload = self.store.get_context(organization_id, "case", case_id)
            return {"found": payload is not None, "data": payload, "classification": "restricted"}
        if tool_id == TOOL_LIST_EVIDENCE:
            case_id = str(arguments.get("caseId") or "")
            payload = self.store.get_context(organization_id, "case_evidence", case_id)
            items = [] if payload is None else payload.get("items") or []
            return {"found": payload is not None, "data": items, "classification": "restricted"}
        if tool_id == TOOL_GET_ALERT:
            alert_id = str(arguments.get("alertId") or "")
            payload = self.store.get_context(organization_id, "alert", alert_id)
            return {"found": payload is not None, "data": payload, "classification": "confidential"}
        if tool_id in {TOOL_GET_ASSESSMENT, TOOL_GET_RULE_HITS}:
            assessment_id = str(arguments.get("assessmentId") or "")
            payload = self.store.get_context(organization_id, "assessment", assessment_id)
            if payload is None:
                return {"found": False, "data": None, "classification": "confidential"}
            if tool_id == TOOL_GET_RULE_HITS:
                return {
                    "found": True,
                    "data": payload.get("ruleHits") or [],
                    "classification": "confidential",
                }
            return {"found": True, "data": payload, "classification": "confidential"}
        if tool_id in {TOOL_SEARCH, TOOL_EMBED}:
            return self._search(organization_id, str(arguments.get("query") or ""))
        raise ToolDenied(tool_id, "unknown tool")

    def _search(self, organization_id: UUID, query: str) -> dict[str, Any]:
        query_vec = embed(query)
        scored: list[tuple[float, EmbeddingChunk]] = []
        for chunk in self.store.embeddings.values():
            if chunk.organization_id != organization_id or chunk.deleted_at is not None:
                continue
            text = str(chunk.metadata.get("text") or "")
            if looks_like_injection(text):
                continue
            scored.append((cosine(query_vec, chunk.embedding), chunk))
        scored.sort(key=lambda item: item[0], reverse=True)
        hits = []
        for score, chunk in scored[:5]:
            if score <= 0:
                continue
            hits.append(
                {
                    "sourceType": chunk.source_type,
                    "sourceId": chunk.source_id,
                    "chunkIndex": chunk.chunk_index,
                    "score": round(score, 4),
                    "text": chunk.metadata.get("text"),
                    "classification": chunk.classification,
                }
            )
        return {"found": bool(hits), "data": hits, "classification": "internal"}

    def index_document(
        self,
        *,
        organization_id: UUID,
        source_type: str,
        source_id: str,
        text: str,
        classification: str = "internal",
    ) -> None:
        if looks_like_injection(text):
            return
        chunk = EmbeddingChunk(
            id=new_id(),
            organization_id=organization_id,
            source_type=source_type,
            source_id=source_id,
            chunk_index=0,
            content_hash=hashlib.sha256(text.encode("utf-8")).hexdigest(),
            embedding=embed(text),
            model_id="sentinel-hash-embed-simulation",
            model_version="1",
            classification=classification,
            metadata={"text": text},
            created_at=utcnow(),
        )
        self.store.embeddings[chunk.id] = chunk
