# Schema Registry Governance

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Schema Registry Governance |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 4 |
| Last Updated | 2026-09-03 |

---

## Purpose

Schema lifecycle governance for domain events and async payloads. **No registry deployment in Phase 4** — rules only.

---

## Schema Ownership

| Artifact | Owner |
|----------|-------|
| Event payload schema | Producer domain team |
| Event envelope schema | Platform architecture |
| OpenAPI request/response | API-owning domain |

---

## Schema Identity

Format: `{eventType}@{schemaVersion}`

Example: `RiskCalculated@1.0.0`

---

## Compatibility Policy

| Change | Compatibility | Action |
|--------|---------------|--------|
| Add optional field | BACKWARD compatible | Patch version bump |
| Add required field | BREAKING | Major version + new stream suffix |
| Remove field | BREAKING | Major version |
| Rename field | BREAKING | Major version |
| Enum value add | BACKWARD (with consumer tolerance) | Minor + documentation |

Default consumer rule: **ignore unknown optional fields**.

---

## Validation

| Stage | Validation |
|-------|------------|
| PR | Schema diff vs registry (future CI) |
| Publish | Producer validates payload against schema before emit |
| Consume | Consumer validates; reject/quarantine on failure |

---

## Deprecation

1. Mark schema deprecated in registry metadata
2. Minimum 2 release cycles before removal
3. Consumers must migrate to new major version

---

## Rollback

- Producers may dual-publish during migration (old + new major)
- Consumers roll back by reverting code, not deleting schemas
- Never delete major version with active consumers

---

## Event Evolution Examples (Frozen Names)

| Event | Current | Evolution allowed |
|-------|---------|-------------------|
| `RiskCalculated` | v1.0 | Add optional `behavioralFactors[]` |
| `AlertCreated` | v1.0 | Add optional `tags[]` |
| `RiskCalculated` | rename to `RiskScored` | **Forbidden** without FDS change control |

---

## Related Documents

- [EventContracts.md](EventContracts.md)
- [MessageBrokerArchitecture.md](MessageBrokerArchitecture.md)
- [ContractValidationCI.md](../08-development/ContractValidationCI.md)
