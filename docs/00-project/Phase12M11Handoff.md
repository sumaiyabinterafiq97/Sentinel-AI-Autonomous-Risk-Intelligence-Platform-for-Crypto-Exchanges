# Phase 12 M11 Handoff

## Status

**M11 = PASS WITH OPEN ITEMS** — SPA workflows SCR-00–15 / WF-1–8 wired to existing APIs; a11y design-target patterns; vitest workflow tests.

**M12 = NOT STARTED.** Do not start M12 automatically.

No commit. No push.

## What M11 delivered

- Login + MFA (`/login/mfa`) then `/workspace`
- Alert triage (list, assign, close with confirmation, priority, escalate)
- Investigation list/create/detail (evidence, notes, timeline, close confirmation)
- Risk assessment view + rules admin
- Compliance start/decision/sanctions/travel/audit package (no invented list GET)
- Search composed from list APIs
- Admin users/settings/audit plus USER list, role assign, AI prompts
- SSE + poll fallback; RBAC nav; AI complementary panel
- Vitest coverage for WF-1, WF-3/7, WF-4, SCR-00, a11y landmarks

## Known limitations

- Domain mutations still require DASH BFF upstream URLs
- Identity login already marks MFA verified; UI still runs AUTH-005 for SCR-00
- COMP queue is session-local known IDs (UX-OQ-COMP-LIST)
- No Playwright/staging live-mesh E2E
- Not WCAG certified
- No Kafka / production broker (unchanged)

## Remaining open questions

COMP list GET; global search API; stack lock (TanStack/Zod); NFR measurement (M12).

## Deferred work / M12 dependencies

M12 is **hardening**: security, performance, observability SLOs, contract CI activation (`ImplementationPlan.md`). Requires running services, NFR harnesses, and CI — not started here.

Also still deferred: production broker, WebSocket, V2 SEC/REPORT/OPS/WALLET, API-ADMIN-006, API-AI-006/007, GD-002 identity events.

## Recommended next milestone

**M12 Hardening only**, when separately authorized.

Do not implement M12, Kafka, Docker/K8s, or V2 product modules as a continuation of this workstream.
