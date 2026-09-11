# API Contract Governance

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | API Contract Governance |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 4 |
| Last Updated | 2026-09-03 |

---

## Purpose

Govern API contract lifecycle, compatibility, traceability, and violation handling. Implements [APIStandards.md](APIStandards.md) at process level.

**Authoritative contract:** [OpenAPI.yaml](OpenAPI.yaml) + [APIInventory.md](APIInventory.md)

---

## 1. API Versioning Policy

| Rule | Policy |
|------|--------|
| URL version | `/v1` mandatory prefix |
| Breaking change | Requires `/v2` and ADR |
| OpenAPI `info.version` | Semantic doc version; bump minor for additive MVP completion; major for breaking |

---

## 2. Backward Compatibility Rules

| Change type | Compatibility |
|-------------|---------------|
| Add optional response field | Compatible |
| Add optional request field | Compatible |
| Add new endpoint | Compatible |
| Add enum value | Compatible with client tolerance note |
| Remove field | **Breaking** |
| Rename field | **Breaking** |
| Change type | **Breaking** |
| Change auth requirements | **Breaking** |

---

## 3. Deprecation Policy

- Minimum **90 days** notice via `Deprecation` header and changelog
- Deprecated operations remain functional until removal in next major version
- OpenAPI `deprecated: true` required

---

## 4. Error Contract Governance

All operations MUST reference standard error responses per [ErrorHandling.md](ErrorHandling.md). Custom errors use `{DOMAIN}_{CATEGORY}_{NNN}` codes registered in inventory.

---

## 5. Authentication / Authorization Contract

| Requirement | Governance |
|-------------|------------|
| Public endpoints | Explicit `security: []` in OpenAPI |
| Protected endpoints | `bearerAuth` + AUTHZ evaluation documented via `x-permissions` |
| Service endpoints | Document service identity requirement |

---

## 6. Idempotency Requirements

Operations marked Idem in APIInventory MUST document `Idempotency-Key` parameter and idempotent behavior in OpenAPI description.

---

## 7. Pagination / Filtering / Sorting

List operations MUST document cursor pagination parameters per APIStandards. Undocumented filters are contract violations.

---

## 8. Tenant Isolation

All tenant-scoped operations MUST require `X-Organization-Id` unless explicitly platform-global (documented exception list in OpenAPI).

---

## 9. Correlation / Request IDs

All mutating operations SHOULD accept `X-Correlation-Id`; responses MUST echo IDs in `meta`.

---

## 10. Auditability

State-changing operations on sensitive resources MUST list audit implication in `x-audit: required`.

---

## 11. OpenAPI Ownership

| Role | Responsibility |
|------|----------------|
| Domain owner | Accurate paths/schemas for domain |
| Architecture | Cross-domain consistency, governance |
| QA | Contract test alignment |

---

## 12. Contract Review Process

1. Change proposed in PR with APIInventory + OpenAPI diff
2. Architecture review for cross-domain impact
3. CI contract validation (future — see ContractValidationCI.md)
4. Merge only if traceability updated

---

## 13. Release Tagging

Every operation MUST have `x-release: MVP | V2 | V3`. MVP gate uses MVP-tagged operations only.

---

## 14. FR Traceability Requirements

Every MVP operation MUST have `x-fr` array matching APIInventory. Missing traceability = contract gap.

---

## 15. Contract Violations

| Violation | Severity |
|-----------|----------|
| OpenAPI op missing from inventory | BLOCKING |
| Inventory MVP op missing from OpenAPI | BLOCKING |
| Domain ownership breach (e.g., RISK creates alert) | BLOCKING |
| Missing tenant header on scoped API | BLOCKING |
| Breaking change without version bump | BLOCKING |
| Missing error response on mutating API | NON-BLOCKING |
| Undocumented optional filter | INFORMATIONAL |

---

## Related Documents

- [ContractValidationCI.md](../08-development/ContractValidationCI.md)
- [Phase4Traceability.md](../08-development/Phase4Traceability.md)
