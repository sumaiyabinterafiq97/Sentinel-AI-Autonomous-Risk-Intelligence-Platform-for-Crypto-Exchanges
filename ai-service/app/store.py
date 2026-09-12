"""In-memory AI schema simulation (migration 010). Not authoritative over domain tables."""

from __future__ import annotations

from dataclasses import dataclass, field
from datetime import UTC, datetime
from threading import Lock
from typing import Any
from uuid import UUID, uuid4


def utcnow() -> datetime:
    return datetime.now(UTC)


@dataclass
class Prompt:
    id: UUID
    organization_id: UUID
    agent_type: str
    name: str
    active_version_id: UUID | None
    created_at: datetime
    updated_at: datetime


@dataclass
class PromptVersion:
    id: UUID
    prompt_id: UUID
    version: int
    content: str
    model_hint: str | None
    created_by: UUID | None
    created_at: datetime


@dataclass
class Recommendation:
    id: UUID
    organization_id: UUID
    agent_type: str
    target_type: str
    target_id: str
    content: dict[str, Any]
    provenance: dict[str, Any]
    prompt_version_id: UUID | None
    model_id: str | None
    status: str
    created_at: datetime


@dataclass
class AgentRun:
    id: UUID
    recommendation_id: UUID | None
    organization_id: UUID
    status: str
    latency_ms: int | None
    token_usage: dict[str, Any] | None
    correlation_id: UUID | None
    error_code: str | None
    started_at: datetime
    completed_at: datetime | None
    tool_log: list[dict[str, Any]] = field(default_factory=list)


@dataclass
class OutboxEvent:
    event_id: UUID
    event_type: str
    schema_version: str
    payload: dict[str, Any]
    organization_id: UUID
    correlation_id: UUID | None
    stream: str
    status: str
    created_at: datetime
    envelope: dict[str, Any]


@dataclass
class EmbeddingChunk:
    id: UUID
    organization_id: UUID
    source_type: str
    source_id: str
    chunk_index: int
    content_hash: str
    embedding: list[float]
    model_id: str
    model_version: str
    classification: str
    metadata: dict[str, Any]
    created_at: datetime
    deleted_at: datetime | None = None


class AiStore:
    def __init__(self) -> None:
        self._lock = Lock()
        self.prompts: dict[UUID, Prompt] = {}
        self.prompt_versions: dict[UUID, PromptVersion] = {}
        self.recommendations: dict[UUID, Recommendation] = {}
        self.runs: dict[UUID, AgentRun] = {}
        self.processed: set[tuple[UUID, str]] = set()
        self.embeddings: dict[UUID, EmbeddingChunk] = {}
        self.context: dict[tuple[UUID, str, str], dict[str, Any]] = {}
        self.outbox: list[OutboxEvent] = []
        self.durable_log: list[dict[str, Any]] = []
        self.idempotency: dict[tuple[UUID, str], UUID] = {}

    def append_outbox(self, event: OutboxEvent) -> None:
        with self._lock:
            self.outbox.append(event)

    def relay_pending(self) -> list[OutboxEvent]:
        delivered: list[OutboxEvent] = []
        with self._lock:
            for event in self.outbox:
                if event.status != "pending":
                    continue
                event.status = "delivered"
                self.durable_log.append(event.envelope)
                delivered.append(event)
        return delivered

    def mark_processed(self, event_id: UUID, consumer: str) -> bool:
        key = (event_id, consumer)
        with self._lock:
            if key in self.processed:
                return False
            self.processed.add(key)
            return True

    def put_context(self, organization_id: UUID, record_type: str, record_id: str, payload: dict[str, Any]) -> None:
        with self._lock:
            self.context[(organization_id, record_type, record_id)] = {
                "payload": payload,
                "updated_at": utcnow().isoformat(),
            }

    def get_context(self, organization_id: UUID, record_type: str, record_id: str) -> dict[str, Any] | None:
        row = self.context.get((organization_id, record_type, record_id))
        return None if row is None else dict(row["payload"])

    def remember_idempotency(self, organization_id: UUID, key: str, recommendation_id: UUID) -> UUID | None:
        with self._lock:
            existing = self.idempotency.get((organization_id, key))
            if existing:
                return existing
            self.idempotency[(organization_id, key)] = recommendation_id
            return None


def new_id() -> UUID:
    return uuid4()
