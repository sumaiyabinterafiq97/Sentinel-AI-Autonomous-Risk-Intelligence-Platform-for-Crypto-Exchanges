# Phase 12 M11 Pre-Implementation Audit

## Document Information

| Field | Value |
|-------|-------|
| Milestone | M11 — Frontend |
| Date | 2026-09-12 |
| Status | Authorized implementation basis |

---

## 1. Objective (authoritative)

From `docs/08-development/ImplementationPlan.md` §8:

> **M11 | Frontend | SCR-00–15 / WF-1–8 against real APIs**

`TestingImplementationPlan.md`: **M11+ = E2E + a11y**.  
`FrontendImplementationPlan.md`: React SPA for SCR-00–15 / WF-1–8; a11y pass on WF-1/3/4/7; no V2 routes.  
`Phase12M10Handoff.md`: M11 is frontend polish / a11y (not ADMIN, not M12).

M9 already delivered an SPA shell against DASH. M10 wired ADMIN screens. **M11 does not reimplement M0–M10 backends.** It completes operator workflows against **existing** OpenAPI paths via the DASH BFF.

---

## 2. Exact M11 scope

| In scope | Detail |
|----------|--------|
| SCR-00 | Login + `/login/mfa` (API-AUTH-001, 005, then 004). Generic failures |
| SCR-01 | Overview `/workspace`; widgets; SSE + **poll fallback** (WF-8) |
| SCR-02–03 | Alert queue/detail: list GET `/v1/alerts` and/or DASH queue; assign/close/priority/link; optional AI-002/004 |
| SCR-04–05 | Case list/create/detail: INVEST-001–009; optional AI-001/003/004 |
| SCR-06–07 | Assessment GET RISK-003; rules list/create/patch RISK-004–006; **no create-alert** |
| SCR-08–10 | COMP start/decision/travel/sanctions/audit-package; **no invented COMP list GET** |
| SCR-11 | Compose ALERT-001, INVEST-001, RISK-002 (as permitted) |
| SCR-12–14 | Already M10; add USER list, AUTHZ role assign, AI-005 prompts on settings |
| SCR-15 | Embedded AI panels calling API-AI-001–004; four layers; no lifecycle buttons in the panel |
| A11y | Skip link, landmarks, labeled errors, dialogs, `aria-live` queue summary, reduced motion, status not color-only |
| Tests | Vitest workflow tests WF-1–8 (mocked fetch = contract consumer). Attribute a11y checks. No Playwright staging environment exists |

---

## 3. Out of scope

M12 hardening, perf/SLO gates, Kafka, Docker/K8s/CI, WebSocket, API-ADMIN-006, API-AI-006/007, SEC/REPORT/OPS/WALLET screens, global search API, COMP list GET, GD-002 events, TanStack Query / RHF/Zod (P11-OQ-STACK-002 / ADR-007 **unconfirmed** — keep existing fetch + React). Formal WCAG certification. Production E2E against a live mesh.

---

## 4. Dependencies on M0–M10

HMAC session, AUTHZ permissions, domain APIs, DASH BFF + SSE, ADMIN APIs. Frontend continues to call `VITE_API_BASE_URL` (dash `:8083`). Domain mutations require BFF upstreams (`DASH_OPS_BASE_URL`, `DASH_IDENTITY_BASE_URL`, `DASH_AI_BASE_URL`, `DASH_PLATFORM_BASE_URL`).

---

## 5. APIs / operationIds (existing only — no new IDs)

AUTH-001/002/004/005 · DASH-001–005/007 · ALERT-001–007 · INVEST-001–009 · RISK-002–006 · COMP-001–007 · ADMIN-001–005 · USER-001/002 · AUTHZ role-assignments · AI-001–005.

---

## 6. FRs

Presentation of AUTH-FR-001/006, ALERT/INVEST/RISK/COMP/ADMIN/DASH/AI FRs already implemented server-side. M11 does not add backend FRs.

---

## 7–9. Database / events / permissions

**None new.** UI consumes existing permissions from the signed token. Events remain server-side; UI uses SSE hints + poll.

---

## 10. Frontend / UI

Reuse `web/` (not a new `sentinel-web` tree). Preserve SCR routes. Add `/login/mfa` and `/workspace`. RBAC-filter nav. PermissionGuard on mutating controls. Confirmation dialogs for close/approve.

---

## 11–13. Security / tenant / AI

Reuse M2 Bearer + `X-Organization-Id` from session (must match token `org`). No second auth. AI panels: SYSTEM / AI / EVIDENCE / HUMAN; no close/assign/approve/priority/score in the AI panel.

---

## 14. MVP/V2/V3

MVP screens only. V2 nav remains absent.

---

## 15. Reuse

Existing `web/src/shared/api.ts`, session HMAC decode, DASH screens, BFF filter, M10 admin pages.

---

## 16. Gaps / gap-fills

| ID | Gap | Decision |
|----|-----|----------|
| M11-G1 | Login OpenAPI has no `mfaRequired`; identity login sets `mfa_verified=true` immediately | After login, UI still runs SCR-00 MFA (`API-AUTH-005` simulation 6-digit) then `API-AUTH-004` before workspace |
| M11-G2 | Overview spec route `/workspace` vs current `/` | Add `/workspace`; keep `/` as alias |
| M11-G3 | COMP list GET missing | Session-local known IDs after start (UX-OQ-COMP-LIST) |
| M11-G4 | No staging E2E runner | Vitest workflow tests with fetch mocks; live mesh E2E remains M12/ops |
| M11-G5 | GET recommendation returns `content` text not layer object | Render text in AI layer; keep four-layer chrome |
| M11-G6 | Collection PATCH integrations (M10-G2) | Unchanged |

---

## 17. Risks

P11-R010 FE/BE mismatch — mitigate with OpenAPI-aligned payloads. P11-R016 COMP list — compose, do not invent. Unconfigured BFF still 503 — ErrorState + retry.

---

## 18. Acceptance

- WF-1, 3, 4, 7 keyboard-capable controls exist (buttons/dialogs labeled)
- Mutating actions call documented domain paths only
- AI cannot mutate
- No V2 nav
- `./gradlew test`, contracts, AI pytest/ruff, vitest, eslint, `git diff --check` pass
- M0–M10 tests not weakened

---

## 19. Confirmation

No undocumented API, event, table, or permission will be added. FRS/FDS/OpenAPI unchanged. **M12 not started.**
