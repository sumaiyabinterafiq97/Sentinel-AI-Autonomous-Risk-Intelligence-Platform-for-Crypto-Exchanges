# Event JSON Schemas

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Event Schema Index |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 5 |
| Last Updated | 2026-09-03 |

---

## Purpose

Formal JSON Schema definitions for Sentinel AI domain events. Schemas complement [EventContracts.md](../EventContracts.md) and [SchemaRegistryGovernance.md](../SchemaRegistryGovernance.md).

**Governance:**

- Event names match FDS/FRS frozen contracts exactly
- Do not add events without change control
- `schemaVersion` in envelope uses semantic versioning (major.minor)
- Breaking payload changes increment major version

---

## Structure

| Path | Content |
|------|---------|
| `envelope.schema.json` | Common event envelope (all events) |
| `events/mvp/` | MVP domain event payloads |
| `events/v2/` | Version 2 domain event payloads |

---

## Envelope

All events MUST conform to `envelope.schema.json`. Domain-specific payload schemas are referenced by `eventType`.

---

## Compatibility

| Change type | Compatibility |
|-------------|---------------|
| Add optional payload field | Backward compatible (same major) |
| Remove required field | Breaking — major bump |
| Rename field | Breaking — major bump |
| Change field type | Breaking — major bump |

See SchemaRegistryGovernance.md for producer/consumer validation rules.

---

## SEC Contract Lock (V2)

**Publishes:** `ThreatDetected`, `SuspiciousSessionDetected`, `ApiAbuseDetected`

**Consumes:** `UserLoggedIn`, `SessionExpired`, `AlertCreated`, `CaseCreated`

**Excluded from SEC consumption:** `DeviceSignalReceived`, `EvidenceAttached`

---

## Related Documents

- [EventContracts.md](../EventContracts.md)
- [MessageBrokerArchitecture.md](../MessageBrokerArchitecture.md)
- [SchemaRegistryGovernance.md](../SchemaRegistryGovernance.md)
