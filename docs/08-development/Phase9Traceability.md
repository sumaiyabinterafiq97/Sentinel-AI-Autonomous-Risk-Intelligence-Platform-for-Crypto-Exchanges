# Phase 9 Traceability Matrix

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Phase 9 Traceability |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 9 |
| Last Updated | 2026-09-11 |

---

## Purpose

Map FR → NFR → API → OpenAPI operation → event → schema → data → domain → UI → security → validation for MVP critical flows. Complements Phase 3–8 matrices; focuses on contract exit evidence.

**Legend:** COMPLETE · PARTIAL · BLOCKING · NON-BLOCKING · DEFERRED · FUTURE · PENDING HUMAN DECISION

---

## Critical Flow 1 — Transaction / Risk Analysis

| Layer | Reference | Status |
|-------|-----------|--------|
| FR | RISK-FR scoring / RiskCalculated publication | COMPLETE |
| NFR | NFR-PERF risk path; ADR-003 non-AI-mandatory scoring | COMPLETE |
| API | API-RISK-001–007 | COMPLETE |
| OpenAPI | risk tag ops (`x-api-id` API-RISK-*) | COMPLETE |
| Event | RiskCalculated, HighRiskDetected | COMPLETE |
| Schema | risk/*.v1.schema.json | COMPLETE |
| Data | RISK migration specs; assessments | COMPLETE (logical) |
| Domain | RISK owns scores | COMPLETE |
| UI | SCR risk widgets / workflows | PARTIAL |
| Security | tenant + authz risk read/write | COMPLETE (contract) |
| Validation | Contract tests planned (ContractValidationCI design) | PARTIAL |

---

## Critical Flow 2 — Alert Creation / Prioritization

| Layer | Reference | Status |
|-------|-----------|--------|
| FR | ALERT-FR-003–008 | COMPLETE |
| API | API-ALERT-001–007 | COMPLETE |
| OpenAPI | alerts ops | COMPLETE |
| Event | AlertCreated, AlertAssigned, AlertClosed | COMPLETE |
| Schema | alert/*.v1.schema.json | COMPLETE |
| Data | ALERT migration | COMPLETE (logical) |
| Domain | ALERT owns lifecycle + priority | COMPLETE |
| UI | Alert queue workflows | COMPLETE |
| AI | Must not own priority | COMPLETE |
| Validation | Idempotency on assign/close | PARTIAL (P9-G017) |

---

## Critical Flow 3 — Investigation / Case Lifecycle

| Layer | Reference | Status |
|-------|-----------|--------|
| FR | INVEST-FR case lifecycle | COMPLETE |
| API | API-INVEST-001–009 | COMPLETE |
| OpenAPI | cases + evidence ops | COMPLETE |
| Event | CaseCreated/Updated/Closed/Assigned, EvidenceAttached | COMPLETE |
| Schema | invest/*.v1.schema.json | COMPLETE |
| Data | INVEST migration | COMPLETE (logical) |
| Domain | INVEST owns cases | COMPLETE |
| UI | SCR investigation | COMPLETE |
| Security | restricted classification on case events | COMPLETE |
| Validation | Consumer SEC(V2) CaseCreated only | COMPLETE |

---

## Critical Flow 4 — Compliance Workflow

| Layer | Reference | Status |
|-------|-----------|--------|
| FR | COMP-FR-003–007 | COMPLETE |
| API | API-COMP-* MVP | COMPLETE |
| OpenAPI | compliance ops | COMPLETE |
| Event | ComplianceReviewed, TravelRuleValidated, SanctionsHitDetected, AuditPackagePrepared | COMPLETE |
| Schema | comp/*.v1.schema.json | COMPLETE |
| Domain | COMP owns outcomes | COMPLETE |
| AI | No compliance approve API | COMPLETE |
| UI | Compliance screens | PARTIAL |
| Validation | Human approval boundary | COMPLETE (contract) |

---

## Critical Flow 5 — AI Investigation Assistance

| Layer | Reference | Status |
|-------|-----------|--------|
| FR | AI-FR-001, 005–009 | COMPLETE |
| NFR | NFR-PERF-006, NFR-RES-003, NFR-SEC-010 | COMPLETE |
| API | API-AI-001, API-AI-004 | COMPLETE |
| OpenAPI | assistInvestigation, getRecommendation | COMPLETE |
| Event | AIRecommendationGenerated; consumes CaseUpdated, EvidenceAttached, AlertCreated | COMPLETE |
| Schema | ai/*.v1.schema.json | COMPLETE |
| Data | AI migration 010 | COMPLETE (logical) |
| Domain | AI assistive; INVEST owns case | COMPLETE |
| UI | SCR-05, SCR-15 | COMPLETE |
| Security | Tool allowlist; prompt injection model | COMPLETE (docs) |
| Validation | No case mutation | COMPLETE (boundary) |

---

## Critical Flow 6 — Dashboard Workspace

| Layer | Reference | Status |
|-------|-----------|--------|
| FR | DASH-FR workspace / DASH-FR-011 SSE | COMPLETE |
| API | API-DASH-001–005, API-DASH-007 | COMPLETE |
| OpenAPI | workspace + `subscribeWorkspaceChannel` | COMPLETE |
| Event | Consumer of alert/case/risk/AI (+ SEC V2) | COMPLETE |
| Data | DASH migration 012 presentation | COMPLETE (logical) |
| Domain | Presentation only | COMPLETE |
| UI | DashboardScreens SCR-01+ | COMPLETE |
| Transport | SSE MVP; WebSocket V2 | COMPLETE |

---

## Critical Flow 7 — Authentication / Authorization

| Layer | Reference | Status |
|-------|-----------|--------|
| FR | AUTH / AUTHZ FRs | COMPLETE |
| API | API-AUTH-*, API-AUTHZ-* | COMPLETE |
| OpenAPI | login (public), session, roles/permissions | COMPLETE |
| Event | UserLoggedIn, SessionExpired | COMPLETE |
| Schema | auth/*.v1.schema.json | COMPLETE |
| Domain | AUTH vs AUTHZ vs USER separation | COMPLETE |
| SEC consume | UserLoggedIn, SessionExpired only (among auth) | COMPLETE |
| Validation | bearerAuth global | COMPLETE |

---

## Critical Flow 8 — Administration

| Layer | Reference | Status |
|-------|-----------|--------|
| FR | ADMIN FRs | COMPLETE |
| API | API-ADMIN-001–005 | COMPLETE |
| OpenAPI | settings/integrations/actions expansions | COMPLETE |
| Event | AdminSettingUpdated, IntegrationConfigured, AdminActionPerformed | COMPLETE |
| Schema | admin/*.v1.schema.json | COMPLETE |
| Domain | ADMIN does not own USER/ORG lifecycle | COMPLETE |
| Validation | Audit ActionPerformed | COMPLETE |

---

## Incomplete Chains (Explicit)

| Gap | Classification |
|-----|----------------|
| Full NFR row for every MVP API | NON-BLOCKING / PARTIAL |
| Phase 10 accessibility acceptance evidence | DEFERRED to Phase 10 |
| Executable contract test suite | DEFERRED (design in ContractValidationCI) |
| Human PRD/Phase 9 approvals | PENDING HUMAN DECISION / BLOCKING for gate |
| Jurisdiction retention durations | PENDING HUMAN DECISION (compliance) |

---

## Related Documents

- [Phase8Traceability.md](Phase8Traceability.md)
- [Phase9ContractExitMatrix.md](../00-project/Phase9ContractExitMatrix.md)
- [Phase9GapRegister.md](../00-project/Phase9GapRegister.md)
