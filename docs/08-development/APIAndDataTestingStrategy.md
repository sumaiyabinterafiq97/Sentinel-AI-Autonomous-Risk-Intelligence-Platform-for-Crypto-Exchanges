# API and Data Testing Strategy

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | API and Data Testing Strategy |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 3 |
| Last Updated | 2026-09-03 |

---

## Purpose

Testing foundation mapping FRs, NFRs, API contracts, events, and data constraints. **No test code in Phase 3.**

---

## Test Levels

| Level | Scope | Tools (implementation phase) |
|-------|-------|------------------------------|
| Unit | Domain logic, validators | Language test framework |
| Integration | API + DB + events | Contract tests, test containers |
| Contract | OpenAPI, event schemas | OpenAPI diff, schema registry |
| E2E | Persona workflows | Staging environment |
| Security | AUTHZ, tenant isolation | OWASP-oriented suites |
| Performance | NFR targets | Load generator |
| Resilience | Degradation, idempotency | Chaos/fault injection |
| AI boundary | Tool allowlist, no lifecycle mutation | AI-specific integration tests |

---

## FR → Test Mapping (MVP Critical Paths)

| FR | Test focus |
|----|------------|
| RISK-FR-003 | Transaction scoring produces assessment; publishes RiskCalculated |
| RISK-FR-010 | Event contract fields validated |
| ALERT-FR-001 | Alert created from risk event; ALERT does not score |
| ALERT-FR-003 | Priority owned by ALERT API |
| ALERT-FR-005 | Close requires human disposition API |
| INVEST-FR-001 | Case lifecycle; human close only |
| INVEST-FR-007 | Publishes CaseCreated/Updated/Closed |
| COMP-FR-004 | Sanctions workflow; human disposition |
| AI-FR-001 | Assist does not mutate case state |
| AI-FR-008 | Tool denied without permission |
| ADMIN-FR-004 | Orchestration delegates; no duplicate USER records |
| CORE-FR-012 | Audit record on sensitive API |

Full matrix: [Phase3Traceability.md](Phase3Traceability.md)

---

## NFR → Verification Mapping

| NFR | Verification |
|-----|--------------|
| NFR-PERF-001 | Load test: ingest → RiskCalculated P95 ≤ 2s |
| NFR-PERF-002 | API read P95 ≤ 300ms |
| NFR-PERF-006 | AI timeout does not block risk path |
| NFR-RES-001 | Chaos: AI down; alert triage works |
| NFR-RES-004 | Duplicate event delivery idempotency |
| NFR-SEC-002 | 403 on missing permission |
| NFR-AUD-001 | Audit record exists for alert close |
| NFR-INT-002 | Idempotency-Key replay returns same resource |
| NFR-AI-001 | MVP workflows pass with AI disabled |

---

## API Contract Tests

| Artifact | Validation |
|----------|------------|
| OpenAPI.yaml | Lint (spectral); breaking change detection |
| APIInventory.md | Every MVP op in OpenAPI or documented exception |
| Error responses | Match ErrorHandling.md for 4xx/5xx |

---

## Event Contract Tests

| Event set | Test |
|-----------|------|
| RISK MVP publish | Payload schema + after DB commit |
| ALERT MVP publish | AlertCreated matches alert row |
| SEC V2 (when enabled) | Only 3 publish, 4 consume events |
| Malformed event | Quarantine; no consumer state change |

---

## Database Tests

| Area | Test |
|------|------|
| Tenant isolation | Cross-org query returns empty/403 |
| Schema ownership | No cross-schema FK |
| Soft delete | Deleted users excluded from lists |
| Audit immutability | No UPDATE on audit_records |
| pgvector isolation | Retrieval filtered by organization_id |

---

## Security Tests

| Scenario | Expected |
|----------|----------|
| Missing token | 401 |
| Wrong org header | 403 or empty |
| AI tool escalation | Denied |
| Prompt injection sample | Sanitized/blocked |
| Rate limit exceeded | 429 |

---

## AI Boundary Tests

| Test | Pass criteria |
|------|---------------|
| POST assist without invest:case:read | 403 |
| AI assist investigation | No row change in investigation_cases |
| AI unavailable | 503; case API still 200 |
| Recommendation provenance | source refs present |

---

## Performance Test Scenarios

1. Sustained transaction ingest (NFR-SCAL-001 target)
2. 100 concurrent analyst alert list (NFR-SCAL-002)
3. AI assist under load (does not degrade RISK)

---

## Test Data Strategy

- Synthetic exchange-modeled data for staging
- No production PII in dev/test
- Tenant fixtures for isolation tests

---

## Related Documents

- [Phase3Traceability.md](Phase3Traceability.md)
- [NonFunctionalRequirements.md](../02-requirements/NonFunctionalRequirements.md)
