# Phase 12 M9 Pre-Implementation Audit

## Document Information

| Field | Value |
|-------|-------|
| Milestone | M9 — DASH BFF + SSE + presentation (frontend integration) |
| Date | 2026-09-11 |
| Status | Authorized implementation basis |

---

## 1. M9 objective

Implement **DASH only**: Backend-for-Frontend on `sentinel-dash` (ADR-017), tenant-safe read projections, SSE API-DASH-007 (ADR-016), and the documented MVP SPA screens/workflows **through the DASH contract**.

DASH is presentation/composition. It must not own RISK/ALERT/INVEST/COMP/AUTH/USER/ORG/AI lifecycle.

**M10 ADMIN, M11 “polish frontend against live mesh”, M12 hardening are not authorized as separate deliveries in this workstream.** ImplementationPlan lists full SCR-00–15 as M11; **this M9 task explicitly authorizes frontend integration with the DASH BFF/SSE contract.** Recorded as M9-G-PLAN (plan vs task). Screens are implemented against DASH + BFF forwarding of **existing** OpenAPI paths — not new API IDs.

---

## 2. DASH FRs

| FR | M9? | Notes |
|----|-----|-------|
| DASH-FR-001 | Yes | `GET /v1/workspace` |
| DASH-FR-002 | Yes | `GET /v1/workspace/dashboard` |
| DASH-FR-003 | Yes | Queue presentation |
| DASH-FR-004 | Yes | Open queue via API-DASH-003 |
| DASH-FR-005 | Yes | Nav in SPA from workspace `queues` + permissions (no extra API) |
| DASH-FR-006 | Yes | Widgets |
| DASH-FR-007 | Yes | Widget interaction log |
| DASH-FR-008 | **Partial** | FRS names `WorkspaceViewed`, `WorkQueueOpened`, `WidgetInteracted` — **no approved JSON schemas / catalog entries**. GD-002: **do not publish**. Persist interaction/audit locally |
| DASH-FR-009 | Partial | Local `widget_interactions` + request correlation; no CORE audit HTTP client |
| DASH-FR-010 | Yes | AUTHZ + tenant on all DASH APIs |
| DASH-FR-011 | Yes | Consume approved upstream events; SSE refresh |
| DASH-FR-012 | Partial | Dashboard may surface last `AIRecommendationGenerated` as labeled AI text — no new AI API |
| DASH-FR-013 | Partial | Same; case assist remains API-AI-001 via BFF forward, not DASH-owned |

---

## 3. API IDs / operationIds (OpenAPI binding)

| API ID | Method | Path | operationId | Permission |
|--------|--------|------|-------------|------------|
| API-DASH-001 | GET | `/v1/workspace` | `getWorkspace` | `dash:workspace:read` |
| API-DASH-002 | GET | `/v1/workspace/dashboard` | `getWorkspaceDashboard` | `dash:workspace:read` |
| API-DASH-003 | GET | `/v1/workspace/queues/{queueType}` | `getWorkQueue` | `dash:queue:read` |
| API-DASH-004 | GET | `/v1/workspace/widgets` | `listWorkspaceWidgets` | `dash:widget:read` |
| API-DASH-005 | POST | `/v1/workspace/widgets/{widgetId}/interactions` | `logWidgetInteraction` | `dash:workspace:read` |
| API-DASH-007 | GET | `/v1/workspace/subscriptions/{channel}` | `subscribeWorkspaceChannel` | `dash:workspace:read` |

**Not implemented:** API-DASH-006 (V2 reports).

ADR-016 still mentions `/v1/dash/subscriptions/{channel}`; **OpenAPI/inventory path wins:** `/v1/workspace/subscriptions/{channel}`.

---

## 4. SSE

- Channels enum: `workspace`, `alerts`, `cases`, `queues`
- Header `Last-Event-ID` optional reconnection cursor
- Auth + `X-Organization-Id` required
- Unknown channel → 404
- **Not** WebSocket
- EventSource cannot set `Authorization`. **Gap-fill:** browser uses `fetch` + `text/event-stream` with Bearer token. Not a new endpoint
- Replay: bounded in-process buffer only (**simulation**). Not a durable broker replay

---

## 5. Events consumed (catalog ∩ DASH + queues)

| Event | Why |
|-------|-----|
| `RiskCalculated` | DASH-FR-011 + catalog |
| `AlertCreated` | DASH-FR-011 + catalog |
| `AlertAssigned` | catalog DASH consumer; queue freshness |
| `AlertClosed` | catalog DASH consumer; queue freshness |
| `CaseCreated` | catalog DASH consumer |
| `CaseUpdated` | DASH-FR-011 + catalog |
| `CaseAssigned` | catalog DASH consumer |
| `AIRecommendationGenerated` | catalog DASH consumer; DASH-FR-012/013 display |

**Not consumed:** `ReportGenerated` (FRS mentions it; **no MVP schema / REPORT is V2**). SEC V2 threat events. COMP events (DASH is not a catalog consumer). `CaseClosed` (COMP/REPORT only in catalog).

Cross-process ops→dash delivery remains **unwired** (no Kafka). Tests inject envelopes into the dash in-process log **simulation**.

---

## 6. Events published

None with approved schemas. DASH-FR-008 names are **unpublished** (GD-002).

---

## 7. Database

Logical **012** `012_dash_init_presentation_tables`.

Executable: `backend/services/dash/.../V012__dash_init_presentation_tables.sql`

Tables: `dash.workspace_preferences`, `dash.widget_interactions`, `dash.workspace_projection_cache`.

Do **not** create migration 011 ADMIN (M10).

---

## 8. Permissions (identity seed)

`dash:workspace:read`, `dash:queue:read`, `dash:widget:read`

---

## 9. Screens / workflows

SCR-00–15 and WF-1–8 as in DashboardScreens.md / MVPWorkflows.md.

**Known UX gaps (do not invent APIs):**

| Gap | Handling |
|-----|----------|
| No COMP list GET | SCR-08 empty authorized state |
| No global search API | SCR-11 composes DASH queues / projected IDs client-side |
| ADMIN APIs are M10 | SCR-12–14 **unavailable** (not fake admin backends) |
| NAV-OQ-001 claim→nav | Permission-driven hide (Navigation.md) |

V2 SEC/REPORT/OPS/WALLET **not in nav**.

---

## 10. BFF forwarding (not new API IDs)

ADR-017: browser → DASH BFF → domain services.

**Gap-fill:** `sentinel-dash` forwards **existing** OpenAPI paths (`/v1/auth`, `/v1/users`, `/v1/organizations`, `/v1/authz`, `/v1/risk`, `/v1/alerts`, `/v1/investigations`, `/v1/compliance`, `/v1/ai`) to configured upstreams when set. Unconfigured upstream → 503 dependency. Does not forward `/v1/admin` or `/v1/reports`.

---

## 11. NFRs / ADRs

ADR-016 SSE, ADR-017 BFF, ADR-002 assistive AI, ADR-014 cursor lists, NFR-RES-003 degraded SSE, NFR-SEC tenant isolation. WCAG 2.1 AA **design target** — not certification.

---

## 12. Out of scope

M10 ADMIN domain, M11 as a separate milestone claim, M12, Kafka, WebSocket, V2 screens, Docker/K8s/CI, invented search/COMP-list APIs, DASH lifecycle events without schemas, AI autonomy.
