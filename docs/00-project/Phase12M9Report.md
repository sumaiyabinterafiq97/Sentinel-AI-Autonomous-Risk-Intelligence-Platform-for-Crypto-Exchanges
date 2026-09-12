# Phase 12 M9 Report

## 1. Objective

DASH BFF, tenant-safe projections, SSE API-DASH-007, and MVP SPA screens/workflows against the DASH contract. Presentation only.

## 2. Pre-implementation audit

`docs/00-project/Phase12M9PreImplementationAudit.md`

## 3. DASH FRs implemented

DASH-FR-001–007, 010, 011. DASH-FR-008/009 partial (no unpublished events; local interaction log). DASH-FR-012/013 partial (labeled AI panel / last recommendation projection).

## 4–6. APIs / IDs / operationIds

API-DASH-001 `getWorkspace` · 002 `getWorkspaceDashboard` · 003 `getWorkQueue` · 004 `listWorkspaceWidgets` · 005 `logWidgetInteraction` · 007 `subscribeWorkspaceChannel`

API-DASH-006 not implemented (V2).

## 7. BFF

`sentinel-dash` owns workspace APIs. Existing OpenAPI domain paths are forwarded when `DASH_IDENTITY_BASE_URL` / `DASH_OPS_BASE_URL` / `DASH_AI_BASE_URL` are set. Unconfigured → 503. No `/v1/admin` or `/v1/reports`.

## 8. Projections

`dash.workspace_projection_cache` rebuilt from consumed events. Non-authoritative. Tenant key `organization_id`. Stale after expiry / SSE loss (UI stale banner). Rebuild by replaying injected events (simulation).

## 9. Events consumed

`RiskCalculated`, `AlertCreated`, `AlertAssigned`, `AlertClosed`, `CaseCreated`, `CaseUpdated`, `CaseAssigned`, `AIRecommendationGenerated`. Duplicate event IDs ignored. Not `ReportGenerated`. Not SEC V2.

## 10. SSE

`GET /v1/workspace/subscriptions/{channel}` `text/event-stream`. Channels: workspace, alerts, cases, queues. `Last-Event-ID` bounded in-process replay. Auth + tenant required. Browser uses `fetch` (EventSource cannot send Bearer).

## 11–13. Auth / AUTHZ / tenant

M2 HMAC. Org header must match token. Permissions `dash:workspace:read`, `dash:queue:read`, `dash:widget:read`. Cross-org header → 403.

## 14–16. Frontend / screens / workflows

React routes for SCR-00–15. WF-1–8 mapped as navigation + screen states. V2 SEC/REPORT/OPS/WALLET absent. Domain mutations require BFF upstreams.

## 17. Accessibility

Semantic headings, skip link, labeled inputs, focus styles, `role=status`/`alert`. **Not WCAG certified.**

## 18. AI UX

SYSTEM / AI / EVIDENCE / HUMAN layers. No AI close/approve/priority controls.

## 19. Database

Identity `V005_7` DASH permissions. Dash `V012` `dash` schema. No M0–M8 migration rewrites. No ADMIN 011.

## 20–22. Tests / validation / regression

DashApiIT, DashApiContractSurfaceIT, DashUpstreamConsumerIT, HealthControllerTest. Web vitest 4 tests.

`./gradlew test` PASS · contracts PASS · AI pytest/ruff PASS · web vitest + eslint PASS · `git diff --check` PASS.

## 23. Frozen FRS/FDS

Unchanged. OpenAPI unchanged. SEC lock unchanged.

## 24. MVP/V2/V3

MVP DASH only. No WebSocket. No V2 report workspace.

## 25–27. Limitations / gaps / unresolved

See handoff. COMP list GET empty. Search composes queues. ADMIN screens unavailable (M10). DASH-FR-008 events unpublished (no schemas). Ops→dash no broker. ImplementationPlan listed full UI as M11 — this task authorized SPA integration.

## 28. Git

Uncommitted. Not pushed.

## 29. M10 handoff

ADMIN settings/integrations/provision. Do not start in this workstream.
