# Dashboard Screens

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Dashboard Screens |
| Version | 0.2 (Draft) |
| Status | Draft — Phase 10 |
| Last Updated | 2026-09-11 |
| Authority | InformationArchitecture, MVPWorkflows, Personas, APIInventory (Phase 9) |

---

## Purpose

MVP screen specifications sufficient for later frontend implementation planning. **No React/TypeScript code.**

Each screen answers: who, why, data, actions, prohibitions, loading/empty/error/permission, audit, AI, evidence, APIs, FR/NFR.

**API IDs** are from APIInventory.md only.

---

## Screen Inventory Principles

1. Operational density
2. Domain ownership visible
3. AI panels assistive only
4. V2 screens out of MVP nav
5. Mutating actions → owning-domain APIs

---

## MVP Screen Index

| ID | Name | Primary personas |
|----|------|------------------|
| SCR-00 | Login / MFA | All |
| SCR-01 | Operations Overview | Risk Analyst (+ role widgets) |
| SCR-02 | Alert Queue | Risk Analyst |
| SCR-03 | Alert Detail | Risk Analyst |
| SCR-04 | Case List | Risk Analyst |
| SCR-05 | Case Detail | Risk Analyst |
| SCR-06 | Risk Assessment Detail | Risk Analyst |
| SCR-07 | Risk Rules Administration | Admin / senior Risk |
| SCR-08 | Compliance Review Queue | Compliance Officer |
| SCR-09 | Compliance Review Detail | Compliance Officer |
| SCR-10 | Audit Package Preparation | Compliance Officer |
| SCR-11 | Search / Intelligence | Analyst / Compliance |
| SCR-12 | User / Organization Administration | Platform Admin |
| SCR-13 | Platform Settings & Integrations | Platform Admin |
| SCR-14 | Admin Audit Review | Platform Admin |
| SCR-15 | AI Assistance Surfaces (embedded) | Risk Analyst |

**MVP screen count: 16** (SCR-00–SCR-15).

---

## SCR-00 — Login / MFA

| Attribute | Specification |
|-----------|---------------|
| Screen ID | SCR-00 |
| Personas | All unauthenticated users |
| Purpose | Establish AUTH session |
| Domain ownership | AUTH |
| Route concept | `/login`, `/login/mfa` |
| Entry points | Session required redirect; logout |
| Permissions | Public login |
| Data dependencies | None (credentials input) |
| API dependencies | API-AUTH-001, API-AUTH-005; then API-AUTH-004 |
| Primary actions | Sign in; verify MFA |
| Secondary actions | — |
| Navigation | Success → SCR-01 (or deep-link return) |
| Components | FormField, LoadingState, ErrorState |
| Loading | Submitting on auth |
| Empty | N/A |
| Error | Generic auth failure |
| Permission-denied | N/A |
| Success | Session established |
| Confirmation | N/A |
| Audit | AUTH domain session events |
| AI | None |
| Accessibility | Labeled fields; error announced |
| Responsive | Single column |
| FR | AUTH-FR-001, AUTH-FR-006 |
| API | API-AUTH-001, 005, 004 |
| NFR | NFR-SEC-003 |

**Must not:** Enumerate valid usernames; show stack traces.

---

## SCR-01 — Operations / Risk Dashboard (Overview)

| Attribute | Specification |
|-----------|---------------|
| Personas | Risk Analyst (primary); others per widgets |
| Purpose | Role-based operational snapshot |
| Domain | DASH (presentation) |
| Route | `/workspace` |
| Entry | Post-login; nav Overview |
| Permissions | `dash:workspace:read`, `dash:widget:read` |
| Data | Widget summaries; queue counts; org context |
| APIs | API-DASH-001, API-DASH-002, API-DASH-004, API-DASH-007; interact API-DASH-005 |
| Events / real-time | SSE workspace/alerts/queues |
| Primary actions | Navigate to queues; open widgets |
| Secondary | Log widget interaction |
| Components | ApplicationShell, widgets, Empty/Loading/Error, AlertBadge |
| Loading | Skeleton widgets |
| Empty | “No items in your queues” |
| Error | Per-widget failure |
| Permission-denied | Hide Overview or empty shell |
| Success | Interactive dashboard |
| Confirmation | N/A |
| Audit | Widget interactions (DASH-FR-007) |
| AI | Optional read-only AI widget; degraded banner |
| Accessibility | Landmarks; skip link; aria-busy |
| Responsive | Widget grid → stack |
| FR | DASH-FR-001, 002, 006 |
| NFR | NFR-PERF-003, NFR-USAB-001, NFR-RES-003 |
| Workflows | WF-Overview entry, WF-8 |

**Must not:** Mutate alert/case SoT from widgets.

---

## SCR-02 — Alert Queue

| Attribute | Specification |
|-----------|---------------|
| Personas | Risk Analyst |
| Purpose | Triage by ALERT-owned priority |
| Domain | ALERT (+ DASH queue projection) |
| Route | `/alerts` |
| Entry | Nav; Overview |
| Permissions | `alert:alert:read`, `dash:queue:read` |
| Data | alert list + risk context denorm |
| APIs | API-ALERT-001, API-DASH-003 |
| Events | AlertCreated/Assigned/Closed via SSE hint |
| Primary actions | Open detail |
| Secondary | Assign (if permitted from row) |
| Components | FilterPanel, DataTable/AlertTable, Pagination, StatusFilter |
| Loading / Empty / Error | Skeleton / no matches / retry + requestId |
| Permission-denied | Hide Alerts nav |
| Confirmation | N/A on list |
| Audit | Via ALERT on mutations |
| AI | N/A on queue |
| Accessibility | Sortable headers; keyboard row open |
| Responsive | Full-width table → card list |
| FR | ALERT-FR-003–006, DASH-FR-003 |
| NFR | NFR-PERF-003; ADR-014 |
| Workflows | WF-1 |

**Must not:** Show RISK score as alert priority.

---

## SCR-03 — Alert Detail

| Attribute | Specification |
|-----------|---------------|
| Personas | Risk Analyst |
| Purpose | Triage with risk context + handoff |
| Domain | ALERT owns lifecycle; RISK context; INVEST on escalate |
| Route | `/alerts/{alertId}` |
| APIs | API-ALERT-002, API-ALERT-003 (PATCH lifecycle), API-ALERT-004 (assign), API-ALERT-005 (close), API-ALERT-006 (priority), API-ALERT-007 (investigation-link), API-RISK-003, API-INVEST-002, API-AI-002, API-AI-004 |
| Primary actions | Assign, update status, close, escalate/create case |
| Secondary | Comment if supported by PATCH/notes pattern; AI explain |
| Components | StatusBadge, RiskScore, InvestigationTimeline, AIExplanationPanel, ConfirmationDialog, PermissionGuard |
| Loading | Section skeletons |
| Empty | “No linked assessment” |
| Error | Toast; retain form |
| Permission-denied | Read-only or denied |
| Success | Updated entity + toast |
| Confirmation | Close; optional priority |
| Audit | Assign/close audited |
| AI | SCR-15 panel — cannot close/set priority |
| Evidence | Rule hits / assessment links |
| Accessibility | Confirm focus trap; restore focus |
| FR | ALERT-FR-004–007; RISK explanation; AI-FR-002 |
| NFR | NFR-EXPL-001, NFR-PERF-006 |
| Workflows | WF-1, WF-2 |

---

## SCR-04 — Investigation Workspace (Case List)

| Attribute | Specification |
|-----------|---------------|
| Personas | Risk Analyst |
| Purpose | Case work queue |
| Domain | INVEST |
| Route | `/investigations` |
| APIs | **API-INVEST-001** list; API-DASH-003; create **API-INVEST-002** |
| Events | CaseCreated/Updated/Assigned SSE |
| Primary actions | Open case; create case |
| Components | DataTable, FilterPanel, Pagination |
| States | Same queue patterns as SCR-02 |
| FR | INVEST list/create FRs; DASH-FR-004 |
| Workflows | WF-3 |

---

## SCR-05 — Case Detail

| Attribute | Specification |
|-----------|---------------|
| Personas | Risk Analyst |
| Purpose | Case lifecycle, evidence, timeline, notes |
| Domain | INVEST |
| Route | `/investigations/cases/{caseId}` |
| APIs | API-INVEST-003, 004, **005 close**, **006 assign**, **007 evidence**, **008 timeline**, **009 notes**; API-AI-001, 003, 004 |
| Primary actions | Assign, attach evidence, note, update, close |
| Secondary | AI assist / retrieve |
| Components | CaseHeader/CaseStatus/CaseAssignment, EvidencePanel, InvestigationTimeline, ActivityLog, AI panels, ConfirmationDialog |
| Empty evidence | Prompt attach |
| Degraded AI | Manual path remains |
| Confirmation | Close case |
| Audit | Close + evidence attach |
| AI | Assistive only |
| Accessibility | Timeline semantics; classification announced |
| FR | INVEST-FR-*; AI-FR-001, 003 |
| NFR | NFR-PERF-004, NFR-AUD-001, NFR-SEC-006 |
| Workflows | WF-3, WF-7 |

**Must not:** SEC MVP UI; EvidenceAttached not for SEC UI consume.

---

## SCR-06 — Transaction / Risk Assessment Detail

| Attribute | Specification |
|-----------|---------------|
| Personas | Risk Analyst |
| Purpose | View assessment + rule hits |
| Domain | RISK |
| Route | `/risk/assessments/{assessmentId}` |
| APIs | **API-RISK-003**; related list **API-RISK-002**; AI deepen API-AI-002/004 |
| Primary actions | Navigate to alert/case |
| Secondary | AI deepen |
| Boundary | **No create-alert** control |
| Components | RiskScore, RiskLevelIndicator, Evidence/rule list, AIExplanationPanel |
| FR | RISK assessment/explanation FRs |
| NFR | NFR-EXPL-001 |
| Workflows | WF-2 |

---

## SCR-07 — Risk Rules Administration

| Attribute | Specification |
|-----------|---------------|
| Personas | Platform Admin / senior Risk with `risk:rule:write` |
| Purpose | Manage rules |
| APIs | **API-RISK-004** list, **API-RISK-005** create, **API-RISK-006** patch |
| Actions | Create/update/enable |
| Audit | Rule changes |
| Confirmation | Destructive disable if policy |
| FR | RISK rule FRs |
| NFR | NFR-AUD-001 |

---

## SCR-08 — Compliance Review Queue

| Attribute | Specification |
|-----------|---------------|
| Personas | Compliance Officer |
| Purpose | KYC/AML/sanctions/travel-rule queue entry |
| Domain | COMP |
| Route | `/compliance` |
| APIs | API-COMP-001–006 as entry actions (start/list via create+known IDs — list-all may be composed from known reviews; **do not invent list API**) |
| Open item | **Closed (GD-007):** no COMP list GET in MVP. Queue UX uses start-response IDs / operator-known IDs / existing by-id COMP operations. |
| Actions | Start review types; open detail |
| Permissions | `comp:*` |
| FR | COMP-FR-001–004 |
| Workflows | WF-4 |

---

## SCR-09 — Compliance Review Detail

| Attribute | Specification |
|-----------|---------------|
| Personas | Compliance Officer |
| Purpose | Human compliance decision |
| APIs | API-COMP-002, 003, 006; context reads API-INVEST-003 / API-RISK-003 |
| Actions | Approve/reject/info-request; sanctions disposition |
| Confirmation | **Required** on decisions |
| AI | **No** MVP compliance approve assist (API-AI-006 = V2) |
| Audit | Decision actor |
| FR | COMP-FR-001–004, 006 |
| NFR | NFR-AUD-001, NFR-PRIV-001 |
| Workflows | WF-4 |

---

## SCR-10 — Audit Package Preparation

| Attribute | Specification |
|-----------|---------------|
| Personas | Compliance Officer |
| Purpose | Request/monitor audit packages |
| APIs | **API-COMP-007** |
| Actions | Request; monitor async; export when ready |
| Note | REPORT V2 not required (GD-001) |
| FR | COMP-FR-005, 006 |
| NFR | NFR-AUD-002 |
| Workflows | WF-5 |

---

## SCR-11 — Search / Intelligence

| Attribute | Specification |
|-----------|---------------|
| Personas | Risk Analyst / Compliance Officer |
| Purpose | Scoped search across authorized entities |
| APIs | Compose API-ALERT-001, API-INVEST-001, API-RISK-002, API-USER-001 as permitted |
| Boundary | **No invented global search API** |
| Permissions | Intersection of domain reads |
| Components | Search, DataTable, EmptyState |
| NFR | NFR-PERF-005 (simulation target) |
| Open | **Closed (GD-007):** composed list GETs are MVP search; no dedicated global search API |

---

## SCR-12 — User / Organization Administration

| Attribute | Specification |
|-----------|---------------|
| Personas | Platform Administrator |
| Purpose | Identity & tenant admin |
| APIs | API-USER-*, API-ORG-001–003, API-AUTHZ-*, API-ADMIN-003, API-ADMIN-004 |
| Actions | List/create/update users/orgs; roles; provision |
| Boundary | ADMIN orchestrates — does not replace USER/ORG ownership |
| Confirmation | Deactivate user |
| FR | USER-*, ORG-*, AUTHZ-*, ADMIN-FR-004 |
| Workflows | WF-6 |

---

## SCR-13 — Platform Settings & Integrations

| Attribute | Specification |
|-----------|---------------|
| Personas | Platform Administrator |
| Purpose | Settings & integrations |
| APIs | API-ADMIN-001, API-ADMIN-002, API-CORE-003–006 as exposed; API-AI-005 prompts |
| Security | Secrets via refs only |
| Audit | AdminSettingUpdated, IntegrationConfigured |
| FR | ADMIN-FR-001–002, CORE config FRs |
| Workflows | WF-6 |

---

## SCR-14 — Admin Audit Review

| Attribute | Specification |
|-----------|---------------|
| Personas | Platform Administrator |
| Purpose | Query admin/platform audit |
| APIs | API-ADMIN-005 |
| Components | AuditLog, FilterPanel, DataTable |
| FR | ADMIN-FR-003, CORE audit FRs |
| NFR | NFR-AUD-001, NFR-AUD-002 |

---

## SCR-15 — AI Assistance Surfaces (Embedded)

| Attribute | Specification |
|-----------|---------------|
| Personas | Risk Analyst |
| Purpose | Embedded assist on SCR-03/05/06 |
| APIs | API-AI-001–004 |
| Presentation | Distinct AI panel; four-layer distinction |
| States | idle/loading/completed/timeout/failed/degraded |
| Boundary | No lifecycle controls inside panel |
| FR | AI-FR-001–003, 009 |
| NFR | NFR-PERF-006, NFR-SEC-009/010, NFR-RES-003 |
| Workflows | WF-7 |
| Spec | AIInteractionPatterns.md |

---

## V2 Screens (Not MVP Default Nav)

| Screen | Domain | Persona |
|--------|--------|---------|
| Security Signals | SEC | Security Engineer |
| Reports / KPI | REPORT | Compliance / leadership |
| Platform Ops Health | OPS | Ops / Admin |
| Wallet Intelligence | WALLET | Risk / Compliance |

---

## Open Questions

| ID | Item | Classification |
|----|------|----------------|
| UX-OQ-COMP-LIST | Dedicated COMP list GET not in inventory | **CLOSED** — no list GET in MVP (GD-007) |
| UX-OQ-SEARCH | Global search API absent | **CLOSED** — composed list GETs sufficient for MVP (GD-007) |
| UX-OQ-PKG-STATUS | Package status GET shape | PARTIAL — OpenAPI for API-COMP-007 |

---

## Related Documents

- [InformationArchitecture.md](InformationArchitecture.md)
- [MVPWorkflows.md](MVPWorkflows.md)
- [ComponentLibrary.md](ComponentLibrary.md)
- [AIInteractionPatterns.md](AIInteractionPatterns.md)
- [FrontendUXArchitecture.md](FrontendUXArchitecture.md)
- [../00-project/Phase12M11OpenQuestionDecisions.md](../00-project/Phase12M11OpenQuestionDecisions.md)
- [APIInventory.md](../06-api/APIInventory.md)
