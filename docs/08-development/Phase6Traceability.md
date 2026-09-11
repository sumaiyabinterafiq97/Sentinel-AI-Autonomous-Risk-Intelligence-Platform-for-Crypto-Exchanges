# Phase 6 Traceability Matrix

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Phase 6 Traceability |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 6 |
| Last Updated | 2026-09-03 |

---

## Purpose

Maps operational MVP domains through UX workflows, APIs, data, events, NFRs, and validation. Focus: RISK, ALERT, INVEST, DASH, COMP, AI, ADMIN.

---

## RISK Domain Chain

| FR | UX Workflow | API | Data Entity | Event | NFR | Validation |
|----|-------------|-----|-------------|-------|-----|------------|
| RISK-FR-009 | Ingest (background) | API-RISK-001 | transaction_ingest_log | RiskCalculated | NFR-PERF-001 | Idempotency + latency sim |
| RISK-FR-003 | Assessment list in Risk screen | API-RISK-003,004 | risk_assessments | — | NFR-PERF-002 | Contract test |
| RISK-FR-001 | Alert detail risk panel | API-RISK-007,004 | risk_assessments, rule_hits | — | NFR-EXPL-001 | Explainability test |
| RISK-FR-010 | (event path) | — | risk_assessments | RiskCalculated, HighRiskDetected | NFR-PERF-009 | Schema + integration |
| RISK-FR-008 | Rule admin (admin role) | API-RISK-005,006 | risk_rules | RuleCreated/Updated | NFR-AUD-001 | AuthZ test |

**Migration:** 006_risk_init — ✅ Phase 6 spec  
**Boundary:** No alert.alerts writes — ✅ documented

---

## ALERT Domain Chain

| FR | UX Workflow | API | Data Entity | Event | NFR | Validation |
|----|-------------|-----|-------------|-------|-----|------------|
| ALERT-FR-003 | Alert queue | API-ALERT-001, DASH-003 | alerts | — | NFR-PERF-003 | Cursor pagination |
| ALERT-FR-004 | Alert detail | API-ALERT-002 | alerts, alert_risk_context | — | — | Contract |
| ALERT-FR-006 | Assign/close | API-ALERT-003,004 | alerts | AlertAssigned, AlertClosed | NFR-AUD-001 | Audit test |
| ALERT-FR-011 | Auto-create from RISK | — | alerts | consumes RiskCalculated | NFR-PERF-009 | Integration |
| ALERT-FR-008 | — | — | alerts | AlertCreated | — | Schema validation |

**Migration:** 007_alert_init — ✅ Phase 6 spec  
**Priority ownership:** ALERT — ✅

---

## INVEST Domain Chain

| FR | UX Workflow | API | Data Entity | Event | NFR | Validation |
|----|-------------|-----|-------------|-------|-----|------------|
| INVEST-FR-001 | Create case | API-INVEST-001 | investigation_cases | CaseCreated | NFR-AUD-001 | Idempotency |
| INVEST-FR-003 | Attach evidence | API-INVEST-005 | case_evidence | EvidenceAttached | NFR-SEC-006 | Classification |
| INVEST-FR-007 | Close case | API-INVEST-007 | investigation_cases | CaseClosed | NFR-AUD-002 | COMP consumer test |
| INVEST-FR-006 | Assign | API-INVEST-006 | investigation_cases | CaseAssigned | — | Schema Phase 6 |
| INVEST-FR-008 | Case links | API-INVEST-009 | case_links | — | — | Contract |

**Migration:** 008_invest_init — ✅ Phase 6 spec

---

## DASH Domain Chain

| FR | UX Workflow | API | Data Entity | Event | NFR | Validation |
|----|-------------|-----|-------------|-------|-----|------------|
| DASH-FR-001 | Overview | API-DASH-001 | workspace_preferences | consumes * | NFR-USAB-001 | BFF aggregation |
| DASH-FR-011 | SSE refresh | API-DASH-007 | projection_cache | consumes * | NFR-RES-003 | SSE contract |
| DASH-FR-003 | Queues | API-DASH-003 | — (read via ALERT/INVEST) | — | NFR-PERF-003 | Load test sim |

**Presentation only:** No alert/case lifecycle ownership — ✅

---

## COMP Domain Chain

| FR | UX Workflow | API | Data Entity | Event | NFR | Validation |
|----|-------------|-----|-------------|-------|-----|------------|
| COMP-FR-001 | KYC queue | API-COMP-001,002 | kyc_reviews | ComplianceReviewed | NFR-PRIV-001 | Workflow test |
| COMP-FR-005 | Audit package | API-COMP-005 | audit_packages | AuditPackagePrepared | NFR-AUD-002 | Async test |
| COMP-FR-004 | Sanctions | API-COMP-004 | sanctions_screenings | SanctionsHitDetected | — | Schema Phase 6 |

**Migration:** Deferred Phase 7 — ⚠️ partial

---

## AI Domain Chain

| FR | UX Workflow | API | Data Entity | Event | NFR | Validation |
|----|-------------|-----|-------------|-------|-----|------------|
| AI-FR-001 | Investigation assist | API-AI-001 | ai_recommendations | AIRecommendationGenerated | NFR-PERF-006 | Timeout test |
| AI-FR-003 | Evidence retrieve | API-AI-003 | ai + pgvector | — | NFR-SEC-010 | Tool auth |
| AI-FR-004 | Prompt admin | API-AI-005 | prompts | PromptUpdated | NFR-AUD-001 | Schema Phase 6 |

**Boundary:** No case/alert create APIs — ✅

---

## ADMIN Domain Chain

| FR | UX Workflow | API | Data Entity | Event | NFR | Validation |
|----|-------------|-----|-------------|-------|-----|------------|
| ADMIN-FR-001 | Settings | API-ADMIN-001 | admin_settings | AdminSettingUpdated | NFR-AUD-001 | Schema Phase 6 |
| ADMIN-FR-004 | Provision user | API-ADMIN-003 | — (orchestrates USER) | — | NFR-AUD-001 | No USER dup |
| ADMIN-FR-003 | Audit query | API-ADMIN-005 | core.audit_records read | AdminActionPerformed | NFR-AUD-002 | Contract |

**Migration:** Deferred Phase 7 — ⚠️ partial

---

## Coverage Summary

| Domain | UX | API | Migration spec | Event schemas | Complete chain |
|--------|-----|-----|----------------|---------------|----------------|
| RISK | ✅ | ✅ | ✅ | ✅ | ✅ |
| ALERT | ✅ | ✅ | ✅ | ✅ | ✅ |
| INVEST | ✅ | ✅ | ✅ | ✅ | ✅ |
| DASH | ✅ | ✅ | — | N/A (consumer) | ✅ |
| COMP | ✅ | ✅ | ⚠️ | ✅ Phase 6 | ⚠️ partial |
| AI | ✅ | ✅ | ⚠️ | ✅ | ⚠️ partial |
| ADMIN | ✅ | ✅ | ⚠️ | ✅ Phase 6 | ⚠️ partial |

---

## Gaps

| ID | Gap | Classification |
|----|-----|----------------|
| TR6-001 | COMP/AI/ADMIN/DASH migration specs not written | NON-BLOCKING |
| TR6-002 | Executable validation tests | NON-BLOCKING |
| TR6-003 | PlatformStarted/Unavailable schemas deferred | INFORMATIONAL |
| TR6-004 | Frontend implementation | BLOCKING for gate (Phase 12) |

---

## Related Documents

- [MVPWorkflows.md](../07-ui/MVPWorkflows.md)
- [EventContractCoverageMatrix.md](../06-api/EventContractCoverageMatrix.md)
- [InitialMigrationSpecifications.md](../04-database/InitialMigrationSpecifications.md)
