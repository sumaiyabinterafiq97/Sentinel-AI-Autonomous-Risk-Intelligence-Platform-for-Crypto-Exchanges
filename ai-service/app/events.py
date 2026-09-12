from __future__ import annotations

from datetime import UTC, datetime
from typing import Any
from uuid import UUID

from app.store import AiStore, OutboxEvent, new_id, utcnow

PRODUCER = "AI"
SCHEMA_VERSION = "1.0"

AI_PUBLISHABLE = frozenset({"AIRecommendationGenerated", "PromptUpdated"})
FORBIDDEN_PUBLISH = frozenset(
    {
        "RiskCalculated",
        "HighRiskDetected",
        "AlertCreated",
        "AlertAssigned",
        "AlertClosed",
        "CaseCreated",
        "CaseUpdated",
        "CaseClosed",
        "CaseAssigned",
        "EvidenceAttached",
        "ComplianceReviewed",
        "TravelRuleValidated",
        "SanctionsHitDetected",
        "AuditPackagePrepared",
    }
)


def stream_name(event_type: str) -> str:
    return f"sentinel.ai.{event_type}.v1"


def iso(ts: datetime | None = None) -> str:
    value = ts or datetime.now(UTC)
    return value.strftime("%Y-%m-%dT%H:%M:%SZ")


class AiEventPublisher:
    def __init__(self, store: AiStore) -> None:
        self.store = store

    def publish(
        self,
        *,
        event_type: str,
        payload: dict[str, Any],
        organization_id: UUID,
        correlation_id: UUID | None,
    ) -> UUID:
        if event_type in FORBIDDEN_PUBLISH:
            raise RuntimeError(f"AI must not publish {event_type}")
        if event_type not in AI_PUBLISHABLE:
            raise RuntimeError(f"unapproved AI event {event_type}")
        event_id = new_id()
        envelope = {
            "eventId": str(event_id),
            "eventType": event_type,
            "schemaVersion": SCHEMA_VERSION,
            "timestamp": iso(),
            "producer": PRODUCER,
            "correlationId": None if correlation_id is None else str(correlation_id),
            "organizationId": str(organization_id),
            "payload": payload,
        }
        record = OutboxEvent(
            event_id=event_id,
            event_type=event_type,
            schema_version=SCHEMA_VERSION,
            payload=payload,
            organization_id=organization_id,
            correlation_id=correlation_id,
            stream=stream_name(event_type),
            status="pending",
            created_at=utcnow(),
            envelope=envelope,
        )
        self.store.append_outbox(record)
        return event_id
