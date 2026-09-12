# Phase 12 M11 Handoff

## Status

**M11 = CLOSED / PASS WITH OPEN ITEMS THAT ARE EITHER DECIDED OR DEFERRED.**

SPA workflows SCR-00–15 / WF-1–8 wired to existing APIs; a11y design-target patterns; vitest workflow tests. UX/stack questions closed by **GD-007** — see [Phase12M11OpenQuestionDecisions.md](Phase12M11OpenQuestionDecisions.md). OpenAPI, FRS, and `web/` were **not** changed to “implement” those decisions.

**M12 = NOT STARTED.** Do not start M12 automatically.

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
- GD-007 recorded: COMP collection GET, global search API, and TanStack/Zod/RHF are **not** M12 carry-overs

## Closed by decision (GD-007) — no further M11/M12 work on these

| Item | Decision |
|------|----------|
| COMP list GET (UX-OQ-COMP-LIST) | **Closed — no work.** No MVP collection GET. Continue start-response IDs, operator-known IDs, existing by-id COMP operations. |
| Global search API (UX-OQ-SEARCH / CL-OQ-002) | **Closed — no work.** Composed ALERT/INVEST/RISK list APIs remain MVP; dedicated search is V2. |
| TanStack Query / Zod / RHF (P11-OQ-STACK-002) | **Closed — no work.** Keep shipped React + TypeScript + Vite + Tailwind + native `fetch`. New ADR required to change. |

## Remaining items (not M11 UX/stack questions)

| Item | What to do |
|------|------------|
| M12 NFR measurement | **M12 work** when separately authorized |
| Live E2E | Remaining validation/hardening. **Environment does not exist** (no Playwright, no docker-compose/staging mesh). Documented dependency — do not fabricate infrastructure or change M11 application code for this. |
| UX-OQ-PKG-STATUS | Remains **open**; do not invent a solution |
| GD-002 events | **Deferred**; do not implement unless authorized |

## Known limitations

- Domain mutations still require DASH BFF upstream URLs
- Identity login already marks MFA verified; UI still runs AUTH-005 for SCR-00
- COMP queue is session-local known IDs (**accepted MVP discovery**; GD-007)
- No Playwright/staging live-mesh E2E (missing runner **and** missing composed local/staging services)
- Not WCAG certified
- No Kafka / production broker (unchanged)

## Live E2E environment check (M11 close-out)

Inspected at close-out; **not present**:

- No Playwright (or other E2E) dependency in `web/package.json`; no `*playwright*` / `*e2e*` specs
- No `docker-compose` (or equivalent) stack in the repository for a live service mesh
- M11 validation remains vitest contract-consumer tests with mocked `fetch`

Therefore live E2E is **not** added as M11 validation. Adding it requires an authorized environment (running identity/ops/AI/platform + DASH BFF + web, plus a runner). That is hardening/ops, not an M11 SPA change.

## Deferred work / M12 dependencies

M12 is **hardening**: security, performance, observability SLOs, contract CI activation (`ImplementationPlan.md`). Requires running services, NFR harnesses, and CI — not started here.

Also still deferred: production broker, WebSocket, V2 SEC/REPORT/OPS/WALLET, API-ADMIN-006, API-AI-006/007, GD-002 identity events.

## Recommended next milestone

**M12 Hardening only**, when separately authorized.

Do not implement M12, Kafka, Docker/K8s, or V2 product modules as a continuation of this workstream.
