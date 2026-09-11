# Phase 7 Traceability Matrix

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Phase 7 Traceability |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 7 |
| Last Updated | 2026-09-11 |

---

## Purpose

Map FR → API → event → database → UI → NFR → validation for Phase 7 focus domains:

**COMP, AI, ADMIN, DASH**

Also record MVP migration coverage through migration 012.

---

## COMP Domain Chain

| FR | API | Event | Database | UI | NFR | Validation |
|----|-----|-------|----------|-----|-----|------------|
| COMP-FR-001 | API-COMP-001,002 | ComplianceReviewed | comp.kyc_reviews | SCR-08, SCR-09 | NFR-PRIV-001, NFR-AUD-001 | Human actor required on decision |
| COMP-FR-002 | API-COMP-003 | ComplianceReviewed | comp.aml_reviews | SCR-08, SCR-09 | NFR-AUD-001 | Workflow + audit |
| COMP-FR-003 | API-COMP-004 | TravelRuleValidated | travel_rule_validations | SCR-08, SCR-09 | — | Schema + API contract |
| COMP-FR-004 | API-COMP-005,006 | SanctionsHitDetected / ComplianceReviewed | sanctions_screenings | SCR-09 | NFR-AUD-001 | Disposition requires human |
| COMP-FR-005 | API-COMP-007 | AuditPackagePrepared | audit_packages | SCR-10 | NFR-AUD-002 | Async package lifecycle |
| COMP-FR-006 | API-COMP-* link/export | — | compliance_records | SCR-09, SCR-10 | NFR-PRIV-001 | Tenant isolation |

**Migration:** 009_comp_init — ✅ Phase 7  
**Chain status:** ✅ Complete (docs) — executable tests ⚠️ deferred

---

## AI Domain Chain

| FR | API | Event | Database | UI | NFR | Validation |
|----|-----|-------|----------|-----|-----|------------|
| AI-FR-001 | API-AI-001 | AIRecommendationGenerated | ai_recommendations, agent_runs | SCR-05, SCR-15 | NFR-PERF-006 | Timeout 10s / assistive only |
| AI-FR-002 | API-AI-002 | AIRecommendationGenerated | ai_recommendations | SCR-03, SCR-06, SCR-15 | NFR-EXPL-001, NFR-PERF-006 | No alert mutation |
| AI-FR-003 | API-AI-003 | — | recommendations + pgvector | SCR-05, SCR-15 | NFR-SEC-010 | Tool auth |
| AI-FR-004 | API-AI-005 | PromptUpdated | prompts, prompt_versions | Admin/AI config surface | NFR-AUD-001 | Version immutability |
| AI-FR-009 | API-AI-004 | — | ai_recommendations | SCR-15 | — | Read recommendation |
| AI-FR-006 | — | AIRecommendationGenerated | ai_recommendations | DASH display | — | Schema validation |

**Migration:** 010_ai_init — ✅ Phase 7  
**Boundary:** No ALERT/INVEST/COMP writes — ✅ documented  
**AgentRunFailed:** Deferred GD-002 — failure in `agent_runs`  
**Chain status:** ✅ Complete (docs)

---

## ADMIN Domain Chain

| FR | API | Event | Database | UI | NFR | Validation |
|----|-----|-------|----------|-----|-----|------------|
| ADMIN-FR-001 | API-ADMIN-001 | AdminSettingUpdated | admin_settings | SCR-13 | NFR-AUD-001 | Setting change audit |
| ADMIN-FR-002 | API-ADMIN-002 | IntegrationConfigured | integration_configs | SCR-13 | NFR-SEC-* | secret_ref only |
| ADMIN-FR-003 | API-ADMIN-005 | AdminActionPerformed | admin_action_log + core.audit_records | SCR-14 | NFR-AUD-002 | Audit query |
| ADMIN-FR-004 | API-ADMIN-003,004 | delegates USER/ORG | — (no admin.users/orgs) | SCR-12 | NFR-AUD-001 | No lifecycle duplication |

**Migration:** 011_admin_init — ✅ Phase 7  
**Chain status:** ✅ Complete (docs)

---

## DASH Domain Chain

| FR | API | Event | Database | UI | NFR | Validation |
|----|-----|-------|----------|-----|-----|------------|
| DASH-FR-001 | API-DASH-001 | consumes * | workspace_preferences | SCR-01 | NFR-USAB-001 | BFF entry |
| DASH-FR-002 | API-DASH-002 | consumes * | projection_cache (optional) | SCR-01 | NFR-PERF-003 | Dashboard load |
| DASH-FR-003/004 | API-DASH-003 | consumes Alert/Case | — (domain APIs) | SCR-02, SCR-04 | NFR-PERF-003 | Queue BFF |
| DASH-FR-006 | API-DASH-004 | — | projection_cache | SCR-01 | — | Widgets |
| DASH-FR-007 | API-DASH-005 | — | widget_interactions | SCR-01 | NFR-AUD-001 | Interaction log |
| DASH-FR-011 | API-DASH-007 | SSE | projection invalidation | SCR-01–05 | NFR-RES-003 | SSE + poll fallback |

**Migration:** 012_dash_init — ✅ Phase 7  
**Ownership:** Presentation only — ✅  
**Chain status:** ✅ Complete (docs)

---

## MVP Migration Coverage

| Order | Migration | Domain | Spec status |
|-------|-----------|--------|-------------|
| 001–005 | CORE–ORG | Identity foundation | ✅ Phases 4–5 |
| 006–008 | RISK–INVEST | Operational path | ✅ Phase 6 |
| 009–012 | COMP–DASH | Remaining MVP | ✅ Phase 7 |

**Executable SQL:** None (by design)

---

## Event Schema Governance (Phase 7)

| Item | Status |
|------|--------|
| MVP required schemas | ✅ Complete (coverage matrix) |
| GD-002 deferred events | ✅ Decision recorded — schemas not created |
| SEC lock | ✅ Preserved |

---

## Intentional Gaps / Deferred

| ID | Item | Classification |
|----|------|----------------|
| TR7-001 | Executable contract/UI tests | NON-BLOCKING |
| TR7-002 | NFR-OQ-002 jurisdiction durations | PENDING HUMAN / OPEN |
| TR7-003 | PRD formal signatures | PENDING HUMAN |
| TR7-004 | BQ-4 product-owner sign-off | PENDING HUMAN |
| TR7-005 | PlatformStarted/Unavailable/AgentRunFailed schemas | DEFERRED (GD-002) |
| TR7-006 | V2 SEC/REPORT/OPS UI | DEFERRED V2 |
| TR7-007 | Implementation plan (roadmap Phase 11) | BLOCKING for gate |

---

## Related Documents

- [InitialMigrationSpecifications.md](../04-database/InitialMigrationSpecifications.md)
- [DashboardScreens.md](../07-ui/DashboardScreens.md)
- [EventContractCoverageMatrix.md](../06-api/EventContractCoverageMatrix.md)
- [Phase6Traceability.md](Phase6Traceability.md)
- [Phase7Report.md](../00-project/Phase7Report.md)
