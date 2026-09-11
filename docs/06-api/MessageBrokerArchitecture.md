# Message Broker Architecture

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Message Broker Architecture |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 4 |
| Last Updated | 2026-09-03 |

---

## Purpose

Conceptual asynchronous messaging architecture for Sentinel AI. **Vendor-neutral** — see ADR-015 for pattern decision and implementation candidates.

---

## Architectural Pattern

```text
Domain Service → Outbox (PostgreSQL) → Relay → Durable Event Log → Consumer Groups → Domain Handlers
```

| Principle | Rule |
|-----------|------|
| Producer ownership | Only owning domain publishes domain events |
| Consumer idempotency | At-least-once delivery; consumers dedupe |
| Commit order | DB commit before event visible |
| No cross-domain DB writes via messages | Events notify; consumers write own schema |

---

## Topic / Stream Naming

Pattern: `sentinel.{domain}.{eventType}.v{major}`

Examples (logical names, not vendor-specific):

| Stream | Producer | Purpose |
|--------|----------|---------|
| `sentinel.risk.RiskCalculated.v1` | RISK | Risk assessment published |
| `sentinel.alert.AlertCreated.v1` | ALERT | Alert lifecycle |
| `sentinel.invest.CaseCreated.v1` | INVEST | Case lifecycle |

**Rules:**

- PascalCase event type matching FDS frozen names exactly
- Major version suffix for breaking schema changes
- No producer publishes to another domain's stream

---

## Delivery Semantics

| Aspect | Policy |
|--------|--------|
| Guarantee | At-least-once |
| Ordering | Per partition key (`organizationId` + aggregate id) |
| Retry | Exponential backoff; max attempts configurable |
| DLQ | `{stream}.dlq` after exhaustion |
| Poison message | Quarantine; alert OPS (V2) |

---

## Idempotency and Duplicates

Consumers maintain `processed_event_ids` (see `ai.processed_event_ids`, domain equivalents). Duplicate `eventId` → ack without side effect.

---

## Correlation and Metadata

Every message carries envelope from [EventContracts.md](EventContracts.md): `eventId`, `correlationId`, `causationId`, `organizationId`, `timestamp`, `metadata.classification`.

---

## Security

| Control | Requirement |
|---------|-------------|
| Transport encryption | TLS in non-local environments |
| Authentication | Service identity per producer/consumer |
| Authorization | Consumer groups scoped to subscribed streams only |
| PII | Minimize payload; classification drives redaction in logs |

---

## Observability

- Publish/consume metrics per stream
- Lag monitoring per consumer group
- Trace propagation via `correlationId`

---

## Replay Policy

| Scenario | Policy |
|----------|--------|
| Consumer bug fix | Replay from offset after idempotency guard |
| New consumer | Read from agreed offset or snapshot + events |
| Full rebuild | Replay from retention window; rebuild projections |

Retention: see ADR-018 and SchemaRegistryGovernance.md.

---

## Local Development

Abstract requirement: single-node broker compatible with production semantics (durability optional in dev). Implementation phase selects tool per ADR-015.

---

## Frozen Event Contracts

MVP/V2 publish/consume sets remain per FDS/FRS. SEC contract unchanged (ADR-004, EventContracts.md).

---

## Related Documents

- [SchemaRegistryGovernance.md](SchemaRegistryGovernance.md)
- [ArchitectureDecisionRecords.md](../03-architecture/ArchitectureDecisionRecords.md) — ADR-015
