# Phase 4 Traceability Matrix

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Phase 4 Traceability |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 4 |
| Last Updated | 2026-09-03 |

---

## Purpose

End-to-end traceability for **MVP API operations** from functional requirements through contracts, events, data ownership, NFRs, and future validation methods. Extends [Phase3Traceability.md](Phase3Traceability.md).

**Coverage target:** 100% of MVP inventory rows (`APIInventory.md` summary: 66 ops; 67 table rows including expanded multi-method entries).

---

## Traceability Legend

| Symbol | Meaning |
|--------|---------|
| ✅ | Complete traceability documented |
| ⚠️ | Partial — gap noted |
| — | Intentionally no HTTP API (event/internal only) |
| V2 | Out of MVP scope |

---

## MVP API → FR → Contract → Event → Data → NFR → Validation

### CORE

| API ID | FR(s) | OpenAPI | Request/Response | Event(s) | DB Owner | NFR(s) | Validation |
|--------|-------|---------|------------------|----------|----------|--------|------------|
| API-CORE-001 | CORE-FR-004 | ✅ | HealthResponse | — | `core` schema | NFR-AVAIL-001 | Contract + health probe test |
| API-CORE-002 | CORE-FR-024 | ✅ | StatusResponse | — | `core` | NFR-OBS-001 | Contract test |
| API-CORE-003 | CORE-FR-008 | ✅ | ConfigRead | — | `core.platform_config` | NFR-SEC-003 | AuthZ + contract |
| API-CORE-004 | CORE-FR-007 | ✅ | ConfigPatch | ConfigurationUpdated | `core.platform_config` | NFR-AUD-001 | Idempotency + event contract |
| API-CORE-005 | CORE-FR-010 | ✅ | FeatureFlagList | — | `core.feature_flags` | — | Contract test |
| API-CORE-006 | CORE-FR-009 | ✅ | FeatureFlagPatch | FeatureFlagChanged | `core.feature_flags` | NFR-AUD-001 | Event + audit test |

### AUTH

| API ID | FR(s) | OpenAPI | Request/Response | Event(s) | DB Owner | NFR(s) | Validation |
|--------|-------|---------|------------------|----------|----------|--------|------------|
| API-AUTH-001 | AUTH-FR-001 | ✅ | LoginRequest/TokenResponse | UserLoggedIn | `auth.sessions` | NFR-SEC-001 | Security + contract |
| API-AUTH-002 | AUTH-FR-003 | ✅ | — | SessionExpired | `auth.sessions` | NFR-SEC-001 | Contract |
| API-AUTH-003 | AUTH-FR-002 | ✅ | RefreshRequest | — | `auth.sessions` | NFR-SEC-002 | Token rotation test |
| API-AUTH-004 | AUTH-FR-004 | ✅ | SessionResponse | — | `auth.sessions` | NFR-SEC-003 | Contract |
| API-AUTH-005 | AUTH-FR-006 | ✅ | MfaVerifyRequest | — | `auth.mfa_enrollments` | NFR-SEC-004 | Contract |

### AUTHZ

| API ID | FR(s) | OpenAPI | Request/Response | Event(s) | DB Owner | NFR(s) | Validation |
|--------|-------|---------|------------------|----------|----------|--------|------------|
| API-AUTHZ-001 | AUTHZ-FR-001 | ✅ | EvaluateRequest/Decision | — | `authz` (roles, bindings) | NFR-SEC-005 | AuthZ unit + contract |
| API-AUTHZ-002 | AUTHZ-FR-003 | ✅ | RoleList | — | `authz.roles` | — | Contract |
| API-AUTHZ-003 | AUTHZ-FR-003 | ✅ | RoleCreate | RoleCreated | `authz.roles` | NFR-AUD-001 | Event contract |
| API-AUTHZ-004 | AUTHZ-FR-004 | ✅ | PermissionList | — | `authz.permissions` | — | Contract |

### USER

| API ID | FR(s) | OpenAPI | Request/Response | Event(s) | DB Owner | NFR(s) | Validation |
|--------|-------|---------|------------------|----------|----------|--------|------------|
| API-USER-001 | USER-FR-003 | ✅ | UserList (cursor) | — | `user.users` | NFR-PRIV-001 | Pagination contract |
| API-USER-002 | USER-FR-001 | ✅ | UserCreate | UserCreated | `user.users` | NFR-AUD-001 | Idempotency + event |
| API-USER-003 | USER-FR-004 | ✅ | UserGet | — | `user.users` | NFR-PRIV-001 | Tenant isolation test |
| API-USER-004 | USER-FR-002 | ✅ | UserPatch | UserUpdated | `user.users` | NFR-AUD-001 | Event contract |

### ORG

| API ID | FR(s) | OpenAPI | Request/Response | Event(s) | DB Owner | NFR(s) | Validation |
|--------|-------|---------|------------------|----------|----------|--------|------------|
| API-ORG-001 | ORG-FR-003 | ✅ | OrgList | — | `org.organizations` | — | Contract |
| API-ORG-002 | ORG-FR-001 | ✅ | OrgCreate | OrganizationCreated | `org.organizations` | NFR-AUD-001 | Event contract |
| API-ORG-003 | ORG-FR-002 | ✅ | OrgPatch | OrganizationUpdated | `org.organizations` | NFR-AUD-001 | Event contract |

### ALERT

| API ID | FR(s) | OpenAPI | Request/Response | Event(s) | DB Owner | NFR(s) | Validation |
|--------|-------|---------|------------------|----------|----------|--------|------------|
| API-ALERT-001 | ALERT-FR-003 | ✅ | AlertList (cursor) | — | `alert.alerts` | NFR-PERF-003 | Cursor pagination test |
| API-ALERT-002 | ALERT-FR-004 | ✅ | AlertGet | — | `alert.alerts` | — | Contract |
| API-ALERT-003 | ALERT-FR-005 | ✅ | AlertAssign | AlertAssigned | `alert.alerts` | NFR-AUD-001 | Event + audit |
| API-ALERT-004 | ALERT-FR-006 | ✅ | AlertStatusPatch | AlertStatusChanged | `alert.alerts` | NFR-AUD-001 | ALERT owns priority |
| API-ALERT-005 | ALERT-FR-007 | ✅ | AlertComment | AlertCommentAdded | `alert.alert_comments` | — | Contract |
| API-ALERT-006 | ALERT-FR-009 | ✅ | AlertEscalate | AlertEscalated | `alert.alerts` | — | Event contract |

**Internal/event-only FRs:**

| FR | Reason | Validation |
|----|--------|------------|
| ALERT-FR-011 | Consumes RISK events — no HTTP | Integration + event contract test |
| ALERT-FR-008 | Publishes AlertCreated on create path | Event schema validation |

### RISK

| API ID | FR(s) | OpenAPI | Request/Response | Event(s) | DB Owner | NFR(s) | Validation |
|--------|-------|---------|------------------|----------|----------|--------|------------|
| API-RISK-001 | RISK-FR-009 | ✅ | TransactionIngest | RiskCalculated (async) | `risk` | NFR-PERF-001 | Async + idempotency |
| API-RISK-002 | RISK-FR-002 | ✅ | RuleList | — | `risk.risk_rules` | — | Contract |
| API-RISK-003 | RISK-FR-003 | ✅ | AssessmentList | — | `risk.risk_assessments` | NFR-PERF-003 | Contract |
| API-RISK-004 | RISK-FR-003 | ✅ | AssessmentGet | — | `risk.risk_assessments` | — | Contract |
| API-RISK-005 | RISK-FR-008 | ✅ | RuleCreate | RuleCreated | `risk.risk_rules` | NFR-AUD-001 | Event contract |
| API-RISK-006 | RISK-FR-008 | ✅ | RulePatch | RuleUpdated | `risk.risk_rules` | NFR-AUD-001 | Event contract |
| API-RISK-007 | RISK-FR-001 | ✅ | ScoreExplain | — | derived read | NFR-EXPL-001 | Explainability test |

| FR | Reason | Validation |
|----|--------|------------|
| RISK-FR-010 | Publishes HighRiskDetected | Event contract + schema compat CI |

### INVEST

| API ID | FR(s) | OpenAPI | Request/Response | Event(s) | DB Owner | NFR(s) | Validation |
|--------|-------|---------|------------------|----------|----------|--------|------------|
| API-INVEST-001 | INVEST-FR-001 | ✅ | CaseCreate | CaseCreated | `invest.investigation_cases` | NFR-AUD-001 | Idempotency |
| API-INVEST-002 | INVEST-FR-002 | ✅ | CaseList | — | `invest` | NFR-PERF-003 | Cursor pagination |
| API-INVEST-003 | INVEST-FR-002 | ✅ | CaseGet | — | `invest` | — | Contract |
| API-INVEST-004 | INVEST-FR-004 | ✅ | CaseStatusPatch | CaseUpdated | `invest` | NFR-AUD-001 | Event contract |
| API-INVEST-005 | INVEST-FR-003 | ✅ | EvidenceAttach | EvidenceAttached | `invest.case_evidence` | NFR-SEC-006 | Classification test |
| API-INVEST-006 | INVEST-FR-005 | ✅ | CaseAssign | CaseAssigned | `invest` | — | Contract |
| API-INVEST-007 | INVEST-FR-006 | ✅ | CaseClose | CaseClosed | `invest` | NFR-AUD-001 | Event contract |
| API-INVEST-008 | INVEST-FR-007 | ✅ | CaseNote | — | `invest.case_notes` | — | Contract |
| API-INVEST-009 | INVEST-FR-008 | ✅ | CaseLink (GET/POST) | — | `invest.case_links` | — | Multi-method OpenAPI |

### COMP

| API ID | FR(s) | OpenAPI | Request/Response | Event(s) | DB Owner | NFR(s) | Validation |
|--------|-------|---------|------------------|----------|----------|--------|------------|
| API-COMP-001 | COMP-FR-001 | ✅ | KycReviewList | — | `comp.kyc_reviews` | NFR-PRIV-001 | Contract |
| API-COMP-002 | COMP-FR-001 | ✅ | KycReviewCreate | ComplianceReviewCreated | `comp` | NFR-AUD-001 | Event |
| API-COMP-003 | COMP-FR-002 | ✅ | KycDecision | ComplianceDecisionRecorded | `comp` | NFR-AUD-001 | Human decision audit |
| API-COMP-004 | COMP-FR-003 | ✅ | SanctionsCheck | — | `comp.sanctions_checks` | NFR-PERF-004 | Contract |
| API-COMP-005 | COMP-FR-004 | ✅ | AuditPackageRequest | ComplianceAuditPackageGenerated | `comp` | NFR-AUD-002 | Async 202 |
| API-COMP-006 | COMP-FR-005 | ✅ | RegulatoryExport | — | `comp` | NFR-PRIV-002 | Retention policy test |
| API-COMP-007 | COMP-FR-006 | ✅ | CompCaseLink | — | `comp` | — | Contract |

### DASH (BFF — ADR-017)

| API ID | FR(s) | OpenAPI | Request/Response | Event(s) | DB Owner | NFR(s) | Validation |
|--------|-------|---------|------------------|----------|----------|--------|------------|
| API-DASH-001 | DASH-FR-001 | ✅ | WorkspaceResponse | consumes domain events | read models / cache | NFR-USAB-001 | BFF aggregation test |
| API-DASH-002 | DASH-FR-002 | ✅ | DashboardResponse | — | derived | — | Contract |
| API-DASH-003 | DASH-FR-003,004 | ✅ | QueueResponse | — | derived | NFR-PERF-003 | Contract |
| API-DASH-004 | DASH-FR-006 | ✅ | WidgetList | — | derived | — | Contract |
| API-DASH-005 | DASH-FR-007 | ✅ | InteractionLog | — | `dash.widget_interactions` | NFR-AUD-001 | Contract |

| FR | Reason | Validation |
|----|--------|------------|
| DASH-FR-011 | SSE refresh (ADR-016) | SSE channel contract + fallback poll |

### AI (Assistive only)

| API ID | FR(s) | OpenAPI | Request/Response | Event(s) | DB Owner | NFR(s) | Validation |
|--------|-------|---------|------------------|----------|----------|--------|------------|
| API-AI-001 | AI-FR-001 | ✅ | InvestAssistRequest/202 | AIRecommendationGenerated | `ai.ai_recommendations` | NFR-SEC-009, PERF-006 | AI eval harness |
| API-AI-002 | AI-FR-002 | ✅ | RiskExplainRequest/202 | AIRecommendationGenerated | `ai` | NFR-EXPL-001 | Explainability test |
| API-AI-003 | AI-FR-003 | ✅ | RetrieveRequest | — | `ai` + pgvector | NFR-SEC-010 | Tool auth test |
| API-AI-004 | AI-FR-009 | ✅ | RecommendationGet | — | `ai.ai_recommendations` | — | Contract |
| API-AI-005 | AI-FR-004 | ✅ | Prompt CRUD (GET/POST/PATCH) | — | `ai.prompts` | NFR-AUD-001 | Multi-method OpenAPI |

**Prohibited:** AI APIs must not map to ALERT/INVEST/COMP lifecycle FRs (verified — no violations).

### ADMIN

| API ID | FR(s) | OpenAPI | Request/Response | Event(s) | DB Owner | NFR(s) | Validation |
|--------|-------|---------|------------------|----------|----------|--------|------------|
| API-ADMIN-001 | ADMIN-FR-001 | ✅ | Settings GET/PATCH | AdminSettingUpdated | `admin.admin_settings` | NFR-AUD-001 | Multi-method |
| API-ADMIN-002 | ADMIN-FR-002 | ✅ | Integration CRUD | IntegrationConfigured | `admin.integrations` | — | Orchestration only |
| API-ADMIN-003 | ADMIN-FR-004 | ✅ | UserProvision | delegates USER | — | NFR-AUD-001 | No USER lifecycle dup |
| API-ADMIN-004 | ADMIN-FR-004 | ✅ | OrgProvision | delegates ORG | — | NFR-AUD-001 | No ORG lifecycle dup |
| API-ADMIN-005 | ADMIN-FR-003 | ✅ | AuditRecordList | — | `core.audit_records` (read) | NFR-AUD-002 | Cross-read allowed |

---

## Coverage Summary

| Area | MVP Target | Status | Notes |
|------|------------|--------|-------|
| MVP API inventory rows | 66–67 | ✅ Complete | All `x-api-id` present in OpenAPI |
| OpenAPI HTTP operations | 73 MVP | ✅ Complete | Multi-method expansions documented |
| MVP event contracts | FDS matrix | ⚠️ Partial | Envelope + key payloads in EventContracts.md; not all payload schemas JSON-Schema formalized |
| MVP DB ownership | DataArchitecture.md | ⚠️ Partial | CORE/AUTH migrations specified; other domains Phase 5+ |
| NFR mapping on APIs | Key NFRs | ⚠️ Partial | OpenAPI `x-nfr` on subset; full mapping in ContractValidationCI design |
| V2 APIs (WALLET, SEC, REPORT, OPS) | 23 | — Deferred | 2 V2 samples in OpenAPI only |

---

## Intentionally No HTTP API (MVP)

| FR | Domain | Reason |
|----|--------|--------|
| CORE-FR-017 | CORE | Event contract enforcement — platform internal |
| ALERT-FR-011 | ALERT | Event consumer from RISK |
| RISK-FR-010 | RISK | Event publisher only |
| INVEST-FR-008 (partial) | INVEST | Event-driven case linking notification |
| AUTH session events | AUTH | Published via domain logic post-login |
| DASH-FR-011 | DASH | SSE transport — ADR-016 |

---

## Validation Methods (Future CI — see ContractValidationCI.md)

| Stage | Validates |
|-------|-----------|
| OpenAPI lint | Syntax, `$ref`, unique operationIds |
| Inventory reconcile | Every MVP `API-*` in OpenAPI |
| FR traceability | Every MVP op has `x-fr` |
| Event schema | Envelope + registered event types |
| Schema compat | BACKWARD for event minor versions |
| Boundary | No prohibited cross-domain paths |
| Frozen domain | No unauthorized FR edits in PR |

---

## Related Documents

- [APIInventory.md](../06-api/APIInventory.md)
- [OpenAPI.yaml](../06-api/OpenAPI.yaml)
- [EventContracts.md](../06-api/EventContracts.md)
- [DataArchitecture.md](../04-database/DataArchitecture.md)
- [ContractValidationCI.md](ContractValidationCI.md)
- [Phase4ContractGapReport.md](../00-project/Phase4ContractGapReport.md)
