# API Inventory

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | API Inventory |
| Version | 1.2 (Draft) |
| Status | Draft — Phase 9 exit arithmetic reconciliation |
| Last Updated | 2026-09-11 |

---

## Purpose

Catalog of all Phase 3 API operations. Machine-readable contract in [OpenAPI.yaml](OpenAPI.yaml).

**OpenAPI coverage (Phase 9 exit audit):** All **68 MVP inventory rows** are represented in OpenAPI (**74** MVP HTTP operations where multi-method endpoints expand inventory rows). Two V2 sample operations (SEC, REPORT) included for contract pattern reference. API-DASH-007 (SSE) is MVP. Governance: [APIContractGovernance.md](APIContractGovernance.md). Phase 9: [Phase9APIContractExitReport.md](../00-project/Phase9APIContractExitReport.md).

**Legend:** Sync = synchronous HTTP; Async = returns 202 or event-driven side effect; Idem = idempotency key supported.

---

## Summary by Domain

| Domain | MVP ops | V2 ops | Total |
|--------|---------|--------|-------|
| CORE | 6 | 0 | 6 |
| AUTH | 5 | 0 | 5 |
| AUTHZ | 4 | 0 | 4 |
| USER | 4 | 0 | 4 |
| ORG | 3 | 1 | 4 |
| ALERT | 7 | 1 | 8 |
| RISK | 7 | 0 | 7 |
| INVEST | 9 | 0 | 9 |
| COMP | 7 | 0 | 7 |
| DASH | 6 | 1 | 7 |
| AI | 5 | 2 | 7 |
| ADMIN | 5 | 1 | 6 |
| WALLET | 0 | 4 | 4 |
| SEC | 0 | 5 | 5 |
| REPORT | 0 | 4 | 4 |
| OPS | 0 | 4 | 4 |
| **Total** | **68** | **23** | **91** |

---

## CORE

| API ID | Method | Path | Release | Purpose | Permissions | Related FRs | Related Events |
|--------|--------|------|---------|---------|-------------|-------------|----------------|
| API-CORE-001 | GET | `/v1/platform/health` | MVP | Liveness/readiness | public/internal | CORE-FR-004 | — |
| API-CORE-002 | GET | `/v1/platform/status` | MVP | Operational status | `platform:status:read` | CORE-FR-024 | — |
| API-CORE-003 | GET | `/v1/platform/config` | MVP | Read active config | `platform:config:read` | CORE-FR-008 | — |
| API-CORE-004 | PATCH | `/v1/platform/config` | MVP | Update config | `platform:config:write` | CORE-FR-007 | ConfigurationUpdated |
| API-CORE-005 | GET | `/v1/platform/feature-flags` | MVP | List flags | `platform:flags:read` | CORE-FR-010 | — |
| API-CORE-006 | PATCH | `/v1/platform/feature-flags/{flagKey}` | MVP | Update flag | `platform:flags:write` | CORE-FR-009 | FeatureFlagChanged |

---

## AUTH

| API ID | Method | Path | Release | Purpose | Permissions | Related FRs | Idem |
|--------|--------|------|---------|---------|-------------|-------------|------|
| API-AUTH-001 | POST | `/v1/auth/login` | MVP | Authenticate | public | AUTH-FR-001 | — |
| API-AUTH-002 | POST | `/v1/auth/logout` | MVP | End session | authenticated | AUTH-FR-003 | — |
| API-AUTH-003 | POST | `/v1/auth/refresh` | MVP | Refresh token | refresh token | AUTH-FR-002 | — |
| API-AUTH-004 | GET | `/v1/auth/session` | MVP | Current session | authenticated | AUTH-FR-004 | — |
| API-AUTH-005 | POST | `/v1/auth/mfa/verify` | MVP | MFA verification | authenticated | AUTH-FR-006 | — |

**Publishes (via domain logic, not HTTP):** `UserLoggedIn`, `SessionExpired`

---

## AUTHZ

| API ID | Method | Path | Release | Purpose | Permissions | Related FRs |
|--------|--------|------|---------|---------|-------------|-------------|
| API-AUTHZ-001 | POST | `/v1/authz/evaluate` | MVP | Evaluate permission | service/human | AUTHZ-FR-001 |
| API-AUTHZ-002 | GET | `/v1/authz/roles` | MVP | List roles | `authz:role:read` | AUTHZ-FR-003 |
| API-AUTHZ-003 | POST | `/v1/authz/roles` | MVP | Create role | `authz:role:write` | AUTHZ-FR-003 |
| API-AUTHZ-004 | POST | `/v1/authz/role-assignments` | MVP | Assign role | `authz:assignment:write` | AUTHZ-FR-005 |

---

## USER

| API ID | Method | Path | Release | Purpose | Permissions | Related FRs | Events |
|--------|--------|------|---------|---------|-------------|-------------|--------|
| API-USER-001 | GET | `/v1/users` | MVP | List users | `user:user:read` | USER-FR-006 | — |
| API-USER-002 | POST | `/v1/users` | MVP | Create user | `user:user:write` | USER-FR-001 | UserCreated |
| API-USER-003 | GET | `/v1/users/{userId}` | MVP | Get user | `user:user:read` | USER-FR-006 | — |
| API-USER-004 | PATCH | `/v1/users/{userId}` | MVP | Update/deactivate | `user:user:write` | USER-FR-002 | UserUpdated |

**Does NOT expose:** AUTH session tokens, AUTHZ policy internals.

---

## ORG

| API ID | Method | Path | Release | Purpose | Permissions | Related FRs |
|--------|--------|------|---------|---------|-------------|-------------|
| API-ORG-001 | GET | `/v1/organizations` | MVP | List orgs | `org:org:read` | ORG-FR-005 |
| API-ORG-002 | POST | `/v1/organizations` | MVP | Create org | `org:org:write` | ORG-FR-001 |
| API-ORG-003 | PATCH | `/v1/organizations/{orgId}` | MVP | Update org | `org:org:write` | ORG-FR-002 |
| API-ORG-004 | GET | `/v1/organizations/{orgId}/hierarchy` | V2 | Org hierarchy | `org:org:read` | ORG-FR (V2) |

---

## ALERT

| API ID | Method | Path | Release | Purpose | Permissions | Related FRs | Events | Idem |
|--------|--------|------|---------|---------|-------------|-------------|--------|------|
| API-ALERT-001 | GET | `/v1/alerts` | MVP | List/discover alerts | `alert:alert:read` | ALERT-FR-006 | — | — |
| API-ALERT-002 | GET | `/v1/alerts/{alertId}` | MVP | Get alert | `alert:alert:read` | ALERT-FR-006 | — | — |
| API-ALERT-003 | PATCH | `/v1/alerts/{alertId}` | MVP | Update lifecycle | `alert:alert:write` | ALERT-FR-005 | AlertCreated/Closed | — |
| API-ALERT-004 | POST | `/v1/alerts/{alertId}/assign` | MVP | Assign alert | `alert:alert:assign` | ALERT-FR-004 | AlertAssigned | Yes |
| API-ALERT-005 | POST | `/v1/alerts/{alertId}/close` | MVP | Close alert | `alert:alert:close` | ALERT-FR-005 | AlertClosed | Yes |
| API-ALERT-006 | PATCH | `/v1/alerts/{alertId}/priority` | MVP | Set queue priority | `alert:alert:priority` | ALERT-FR-003 | — | — |
| API-ALERT-007 | POST | `/v1/alerts/{alertId}/investigation-link` | MVP | Link investigation context | `alert:alert:write` | ALERT-FR-007 | — | — |
| API-ALERT-008 | POST | `/v1/alerts/ingest` | V2 | External alert ingest | service | ALERT-FR-002 | AlertCreated | Yes |

**Does NOT expose:** RISK scoring APIs. ALERT consumes risk events; does not calculate scores.

---

## RISK

| API ID | Method | Path | Release | Purpose | Permissions | Related FRs | Events | NFRs |
|--------|--------|------|---------|---------|-------------|-------------|--------|------|
| API-RISK-001 | POST | `/v1/risk/transactions/ingest` | MVP | Ingest transaction (service) | `risk:ingest:write` | RISK-FR-009 | TransactionReceived→RiskCalculated | PERF-001 |
| API-RISK-002 | GET | `/v1/risk/assessments` | MVP | Discover assessments | `risk:assessment:read` | RISK-FR-008 | — | PERF-002 |
| API-RISK-003 | GET | `/v1/risk/assessments/{assessmentId}` | MVP | Get assessment + explanation | `risk:assessment:read` | RISK-FR-006,008 | — | PERF-002 |
| API-RISK-004 | GET | `/v1/risk/rules` | MVP | List rules | `risk:rule:read` | RISK-FR-001 | — | — |
| API-RISK-005 | POST | `/v1/risk/rules` | MVP | Create rule | `risk:rule:write` | RISK-FR-001 | — | — |
| API-RISK-006 | PATCH | `/v1/risk/rules/{ruleId}` | MVP | Update rule | `risk:rule:write` | RISK-FR-001 | — | — |
| API-RISK-007 | POST | `/v1/risk/evaluate` | MVP | Trigger evaluation (admin/service) | `risk:evaluate:write` | RISK-FR-002,003 | RiskCalculated | PERF-001 |

**Does NOT expose:** Alert creation. RISK publishes events; ALERT owns alerts.

---

## INVEST

| API ID | Method | Path | Release | Purpose | Permissions | Related FRs | Events | Idem |
|--------|--------|------|---------|---------|-------------|-------------|--------|------|
| API-INVEST-001 | GET | `/v1/investigations/cases` | MVP | List cases | `invest:case:read` | INVEST-FR-006 | — | — |
| API-INVEST-002 | POST | `/v1/investigations/cases` | MVP | Create case | `invest:case:write` | INVEST-FR-001 | CaseCreated | Yes |
| API-INVEST-003 | GET | `/v1/investigations/cases/{caseId}` | MVP | Get case | `invest:case:read` | INVEST-FR-006 | — | — |
| API-INVEST-004 | PATCH | `/v1/investigations/cases/{caseId}` | MVP | Update case | `invest:case:write` | INVEST-FR-001 | CaseUpdated | — |
| API-INVEST-005 | POST | `/v1/investigations/cases/{caseId}/close` | MVP | Close case (human) | `invest:case:close` | INVEST-FR-001 | CaseClosed | Yes |
| API-INVEST-006 | POST | `/v1/investigations/cases/{caseId}/assign` | MVP | Assign case | `invest:case:assign` | INVEST-FR-002 | CaseAssigned | Yes |
| API-INVEST-007 | POST | `/v1/investigations/cases/{caseId}/evidence` | MVP | Attach evidence | `invest:evidence:write` | INVEST-FR-003 | EvidenceAttached | Yes |
| API-INVEST-008 | GET | `/v1/investigations/cases/{caseId}/timeline` | MVP | Get timeline | `invest:case:read` | INVEST-FR-005 | — | — |
| API-INVEST-009 | GET/POST | `/v1/investigations/cases/{caseId}/notes` | MVP | Notes | `invest:case:write` | INVEST-FR-004 | — | — |

**Does NOT expose:** AI case closure. Humans close cases (ADR-006).

---

## COMP

| API ID | Method | Path | Release | Purpose | Permissions | Related FRs | Events |
|--------|--------|------|---------|---------|-------------|-------------|--------|
| API-COMP-001 | POST | `/v1/compliance/kyc-reviews` | MVP | Start KYC review | `comp:kyc:write` | COMP-FR-001 | ComplianceReviewed |
| API-COMP-002 | PATCH | `/v1/compliance/kyc-reviews/{reviewId}` | MVP | Complete KYC | `comp:kyc:approve` | COMP-FR-001 | ComplianceReviewed |
| API-COMP-003 | POST | `/v1/compliance/aml-reviews` | MVP | AML review | `comp:aml:write` | COMP-FR-002 | ComplianceReviewed |
| API-COMP-004 | POST | `/v1/compliance/travel-rule/validations` | MVP | Travel Rule | `comp:travelrule:write` | COMP-FR-003 | TravelRuleValidated |
| API-COMP-005 | POST | `/v1/compliance/sanctions-screenings` | MVP | Sanctions screen | `comp:sanctions:write` | COMP-FR-004 | SanctionsHitDetected |
| API-COMP-006 | PATCH | `/v1/compliance/sanctions-screenings/{screeningId}` | MVP | Disposition match | `comp:sanctions:approve` | COMP-FR-004 | ComplianceReviewed |
| API-COMP-007 | POST | `/v1/compliance/audit-packages` | MVP | Prepare audit package | `comp:audit:write` | COMP-FR-005 | AuditPackagePrepared |

---

## DASH (Presentation / BFF)

| API ID | Method | Path | Release | Purpose | Permissions | Related FRs |
|--------|--------|------|---------|---------|-------------|-------------|
| API-DASH-001 | GET | `/v1/workspace` | MVP | Workspace entry | `dash:workspace:read` | DASH-FR-001 |
| API-DASH-002 | GET | `/v1/workspace/dashboard` | MVP | Role dashboard | `dash:workspace:read` | DASH-FR-002 |
| API-DASH-003 | GET | `/v1/workspace/queues/{queueType}` | MVP | Work queue view | `dash:queue:read` | DASH-FR-003,004 |
| API-DASH-004 | GET | `/v1/workspace/widgets` | MVP | Summary widgets | `dash:widget:read` | DASH-FR-006 |
| API-DASH-005 | POST | `/v1/workspace/widgets/{widgetId}/interactions` | MVP | Log interaction | `dash:workspace:read` | DASH-FR-007 |
| API-DASH-007 | GET | `/v1/workspace/subscriptions/{channel}` | MVP | SSE workspace refresh (ADR-016) | `dash:workspace:read` | DASH-FR-011 |
| API-DASH-006 | GET | `/v1/workspace/reports/{reportId}` | V2 | Navigate to REPORT | `report:report:read` | DASH (V2) |

**DASH aggregates via downstream domain APIs—does not own alert/case/compliance lifecycle.**

---

## AI (Assistive)

| API ID | Method | Path | Release | Purpose | Permissions | Related FRs | Sync/Async | NFRs |
|--------|--------|------|---------|---------|-------------|-------------|------------|------|
| API-AI-001 | POST | `/v1/ai/assist/investigation` | MVP | Investigation assist | `ai:investigation:assist` | AI-FR-001 | Async 202 | PERF-006 |
| API-AI-002 | POST | `/v1/ai/assist/risk-explanation` | MVP | Risk explanation | `ai:risk:assist` | AI-FR-002 | Async 202 | PERF-006 |
| API-AI-003 | POST | `/v1/ai/assist/retrieve` | MVP | Evidence retrieval | `ai:retrieve:execute` | AI-FR-003 | Sync/Async | — |
| API-AI-004 | GET | `/v1/ai/recommendations/{recommendationId}` | MVP | Get recommendation | `ai:recommendation:read` | AI-FR-009 | Sync | — |
| API-AI-005 | GET/POST/PATCH | `/v1/ai/prompts` | MVP | Prompt management | `ai:prompt:write` | AI-FR-004 | Sync | — |
| API-AI-006 | POST | `/v1/ai/assist/compliance` | V2 | Compliance assist | `ai:compliance:assist` | AI (V2) | Async | — |
| API-AI-007 | POST | `/v1/ai/evaluations` | V2 | Run evaluation | `ai:eval:write` | AI (V2) | Async | AI-003 |

**AI APIs do NOT:** create alerts, close cases, approve compliance, change alert priority.

---

## ADMIN

| API ID | Method | Path | Release | Purpose | Permissions | Related FRs | Events |
|--------|--------|------|---------|---------|-------------|-------------|--------|
| API-ADMIN-001 | GET/PATCH | `/v1/admin/settings` | MVP | Platform settings | `admin:settings:write` | ADMIN-FR-001 | AdminSettingUpdated |
| API-ADMIN-002 | GET/POST/PATCH | `/v1/admin/integrations` | MVP | Integrations | `admin:integration:write` | ADMIN-FR-002 | IntegrationConfigured |
| API-ADMIN-003 | POST | `/v1/admin/users/provision` | MVP | Orchestrate user create | `admin:user:provision` | ADMIN-FR-004 | delegates USER |
| API-ADMIN-004 | POST | `/v1/admin/organizations/provision` | MVP | Orchestrate org create | `admin:org:provision` | ADMIN-FR-004 | delegates ORG |
| API-ADMIN-005 | GET | `/v1/admin/audit-records` | MVP | Admin audit query | `admin:audit:read` | ADMIN-FR-003 | — |
| API-ADMIN-006 | GET | `/v1/admin/integration-health` | V2 | Integration health | `admin:integration:read` | ADMIN (V2) | — |

---

## WALLET (V2)

| API ID | Method | Path | Release | Purpose | Permissions | Related FRs |
|--------|--------|------|---------|---------|-------------|-------------|
| API-WALLET-001 | GET | `/v1/wallets/profiles/{address}` | V2 | Wallet profile | `wallet:profile:read` | WALLET-FR-001 |
| API-WALLET-002 | GET | `/v1/wallets/addresses/{address}/reputation` | V2 | Reputation | `wallet:reputation:read` | WALLET-FR-002 |
| API-WALLET-003 | GET | `/v1/wallets/addresses/{address}/activity` | V2 | Activity timeline | `wallet:activity:read` | WALLET-FR-003 |
| API-WALLET-004 | GET | `/v1/wallets/graph` | V2 | Relationship graph query | `wallet:graph:read` | WALLET-FR-004 |

---

## SEC (V2)

| API ID | Method | Path | Release | Purpose | Permissions | Related FRs |
|--------|--------|------|---------|---------|-------------|-------------|
| API-SEC-001 | GET | `/v1/security/threats` | V2 | List threats | `sec:threat:read` | SEC-FR-004 |
| API-SEC-002 | GET | `/v1/security/api-activity` | V2 | API monitoring | `sec:api:read` | SEC-FR-001 |
| API-SEC-003 | GET | `/v1/security/sessions/anomalies` | V2 | Session anomalies | `sec:session:read` | SEC-FR-002 |
| API-SEC-004 | GET | `/v1/security/devices/{deviceId}` | V2 | Device intelligence | `sec:device:read` | SEC-FR-003 |
| API-SEC-005 | GET | `/v1/security/signals/{signalId}` | V2 | Signal detail | `sec:signal:read` | SEC-FR-005 |

---

## REPORT (V2)

| API ID | Method | Path | Release | Purpose | Permissions | Related FRs |
|--------|--------|------|---------|---------|-------------|-------------|
| API-REPORT-001 | POST | `/v1/reports/operational` | V2 | Generate report | `report:operational:write` | REPORT-FR-001 |
| API-REPORT-002 | GET | `/v1/reports/kpi-dashboards` | V2 | KPI dashboards | `report:kpi:read` | REPORT-FR-002 |
| API-REPORT-003 | POST | `/v1/reports/exports` | V2 | Export data | `report:export:write` | REPORT-FR-003 |
| API-REPORT-004 | GET | `/v1/reports/{reportId}` | V2 | Get report | `report:report:read` | REPORT-FR-004 |

---

## OPS (V2)

| API ID | Method | Path | Release | Purpose | Permissions | Related FRs |
|--------|--------|------|---------|---------|-------------|-------------|
| API-OPS-001 | GET | `/v1/ops/health` | V2 | Platform health dashboard | `ops:health:read` | OPS-FR-001 |
| API-OPS-002 | GET | `/v1/ops/metrics` | V2 | Metrics catalog | `ops:metrics:read` | OPS-FR-002 |
| API-OPS-003 | GET | `/v1/ops/logs` | V2 | Log query | `ops:logs:read` | OPS-FR-003 |
| API-OPS-004 | GET | `/v1/ops/backups` | V2 | Backup status | `ops:backup:read` | OPS-FR-005 |

---

## Cross-Domain Prohibitions

| Prohibited API | Reason |
|----------------|--------|
| `POST /v1/risk/alerts` | RISK must not create alerts (ALERT owns) |
| `POST /v1/ai/cases` | AI must not create cases (INVEST owns) |
| `POST /v1/ai/compliance/approve` | AI must not approve compliance (COMP owns) |
| `POST /v1/sec/auth/revoke` | SEC must not manage AUTH sessions |
| `CRUD /v1/admin/users/{id}` duplicating USER | ADMIN orchestrates only (ADMIN-FR-004) |

---

## Related Documents

- [OpenAPI.yaml](OpenAPI.yaml)
- [APIStandards.md](APIStandards.md)
- [Phase3Traceability.md](../08-development/Phase3Traceability.md)
