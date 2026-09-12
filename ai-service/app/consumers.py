"""AI-FR-007: index upstream context. Does not invoke agents."""

from __future__ import annotations

from typing import Any
from uuid import UUID

from app.store import AiStore
from app.tools import ToolRegistry

CONSUMER = "ai-context"
UPSTREAM = frozenset({"CaseUpdated", "RiskCalculated", "EvidenceAttached", "AlertCreated"})


class AiUpstreamConsumer:
    def __init__(self, store: AiStore, tools: ToolRegistry) -> None:
        self.store = store
        self.tools = tools

    def consume(self, envelope: dict[str, Any]) -> str:
        event_type = str(envelope.get("eventType") or "")
        if event_type not in UPSTREAM:
            return "ignored"
        event_id = UUID(str(envelope["eventId"]))
        if not self.store.mark_processed(event_id, CONSUMER):
            return "duplicate"
        org = UUID(str(envelope["organizationId"]))
        payload = envelope.get("payload") or {}
        if event_type == "CaseUpdated":
            case_id = str(payload.get("caseId") or "")
            self.store.put_context(org, "case", case_id, dict(payload))
        elif event_type == "AlertCreated":
            alert_id = str(payload.get("alertId") or "")
            self.store.put_context(org, "alert", alert_id, dict(payload))
        elif event_type == "RiskCalculated":
            assessment_id = str(payload.get("assessmentId") or "")
            self.store.put_context(org, "assessment", assessment_id, dict(payload))
        elif event_type == "EvidenceAttached":
            case_id = str(payload.get("caseId") or "")
            evidence_id = str(payload.get("evidenceId") or payload.get("evidenceRef") or "")
            existing = self.store.get_context(org, "case_evidence", case_id) or {"items": []}
            items = list(existing.get("items") or [])
            items.append(dict(payload))
            self.store.put_context(org, "case_evidence", case_id, {"items": items})
            text = str(payload.get("evidenceRef") or payload.get("summary") or evidence_id)
            self.tools.index_document(
                organization_id=org,
                source_type="case_evidence",
                source_id=evidence_id or case_id,
                text=text,
                classification="restricted",
            )
        return "processed"
