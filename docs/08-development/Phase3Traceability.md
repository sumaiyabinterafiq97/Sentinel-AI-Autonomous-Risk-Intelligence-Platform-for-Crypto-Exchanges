# Phase 3 Traceability Matrix

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Phase 3 Traceability |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 3 |
| Last Updated | 2026-09-03 |

---

## FR → API (MVP Sample — full inventory in APIInventory.md)

| FR | API ID(s) | Notes |
|----|-----------|-------|
| CORE-FR-004 | API-CORE-001 | Health |
| CORE-FR-007,008 | API-CORE-003,004 | Config |
| AUTH-FR-001 | API-AUTH-001 | Login |
| AUTH-FR-004 | API-AUTH-004 | Session |
| AUTHZ-FR-001 | API-AUTHZ-001 | Evaluate |
| USER-FR-001,002 | API-USER-002,004 | User lifecycle |
| ORG-FR-001,002 | API-ORG-002,003 | Org lifecycle |
| ALERT-FR-003–006 | API-ALERT-001–006 | Alert ops |
| RISK-FR-001,003,008 | API-RISK-003–007 | Rules + assessments |
| RISK-FR-009 | API-RISK-001 | Ingest |
| INVEST-FR-001–006 | API-INVEST-001–009 | Cases |
| COMP-FR-001–005 | API-COMP-001–007 | Compliance |
| DASH-FR-001–003 | API-DASH-001–003 | Workspace |
| AI-FR-001–004 | API-AI-001–005 | Assistive |
| ADMIN-FR-001–004 | API-ADMIN-001–004 | Admin |

### FRs without external HTTP API (internal/event only)

| FR | Reason |
|----|--------|
| ALERT-FR-011 | Event consumer from RISK — integration test |
| RISK-FR-010 | Event publisher — verified via event tests |
| INVEST-FR-008 | Event consumer — integration test |
| CORE-FR-017 | Event contract enforcement — platform internal |
| DASH-FR-011 | Event-driven refresh — WebSocket/poll internal |

---

## FR → Data Entity

| FR | Primary tables |
|----|----------------|
| USER-FR-001 | `user.users` |
| ORG-FR-001 | `org.organizations` |
| RISK-FR-003 | `risk.risk_assessments`, `risk.risk_rule_hits` |
| ALERT-FR-001 | `alert.alerts` |
| INVEST-FR-001 | `invest.investigation_cases` |
| INVEST-FR-003 | `invest.case_evidence` |
| COMP-FR-001 | `comp.kyc_reviews` |
| AI-FR-001 | `ai.ai_recommendations` |
| ADMIN-FR-001 | `admin.admin_settings` |
| CORE-FR-012 | `core.audit_records` |
| SEC-FR-001 (V2) | `sec.api_activity_records` |

---

## FR → Event

| FR | Events produced/consumed |
|----|--------------------------|
| RISK-FR-010 | Publishes: RiskCalculated, HighRiskDetected |
| ALERT-FR-008,011 | Consumes: RiskCalculated, HighRiskDetected; Publishes: AlertCreated, etc. |
| INVEST-FR-007,008 | Publishes: Case*; Consumes: AlertCreated, RiskCalculated |
| COMP-FR-007,008 | Publishes: Compliance*; Consumes: CaseClosed, CaseUpdated, RiskCalculated, UserUpdated |
| AI-FR-006,007 | Publishes: AIRecommendationGenerated; Consumes: CaseUpdated, etc. |
| SEC-FR-006,007 (V2) | Locked publish/consume sets |

---

## Persona → API Workflows

| Persona | Primary APIs |
|---------|--------------|
| Risk Analyst | API-DASH-003, API-ALERT-*, API-RISK-003, API-INVEST-002, API-AI-001 |
| Compliance Officer | API-COMP-*, API-INVEST-003 (read) |
| Security Engineer (V2) | API-SEC-* |
| Platform Administrator | API-ADMIN-*, API-USER-*, API-ORG-* |

---

## Domain → Data Ownership

See [DataArchitecture.md](../04-database/DataArchitecture.md) registry.

---

## NFR → Verification

See [APIAndDataTestingStrategy.md](APIAndDataTestingStrategy.md).

---

## Traceability Gaps

| Gap | Impact | Resolution |
|-----|--------|------------|
| DASH-FR-011 real-time transport | WebSocket vs poll undefined | Phase 4 API detail |
| Exact retention periods | NFR-PRIV-002 | Compliance stakeholder |
| REPORT MVP dependency (BQ-4) | Gate policy | Human decision |

---

## Related Documents

- [APIInventory.md](../06-api/APIInventory.md)
- [PostgreSQL.md](../04-database/PostgreSQL.md)
- [EventContracts.md](../06-api/EventContracts.md)
