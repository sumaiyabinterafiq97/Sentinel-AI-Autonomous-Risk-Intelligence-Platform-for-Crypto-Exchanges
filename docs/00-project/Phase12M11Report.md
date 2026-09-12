# Phase 12 M11 Report

## A. Overall result

**PASS WITH OPEN ITEMS**

## B. Objective

Frontend: SCR-00–15 / WF-1–8 against **existing** OpenAPI APIs (ImplementationPlan M11). A11y design-target patterns and vitest workflow tests. No M12.

## C. FRs implemented (presentation)

Existing AUTH/ALERT/INVEST/RISK/COMP/ADMIN/DASH/AI FRs are consumed in the SPA. No new backend FRs.

## D. APIs (consumed, not newly implemented)

| API ID | operationId (OpenAPI) | Method | Path |
|--------|----------------------|--------|------|
| API-AUTH-001 | login | POST | `/v1/auth/login` |
| API-AUTH-002 | logout | POST | `/v1/auth/logout` |
| API-AUTH-004 | getSession | GET | `/v1/auth/session` |
| API-AUTH-005 | verifyMfa | POST | `/v1/auth/mfa/verify` |
| API-DASH-001–005, 007 | workspace/dashboard/queues/widgets/SSE | GET/POST | `/v1/workspace/**` |
| API-ALERT-001–007 | list/get/patch/assign/close/priority/link | GET/PATCH/POST | `/v1/alerts/**` |
| API-INVEST-001–009 | cases CRUD/close/assign/evidence/timeline/notes | GET/POST/PATCH | `/v1/investigations/cases/**` |
| API-RISK-002–006 | assessments + rules | GET/POST/PATCH | `/v1/risk/**` |
| API-COMP-001–007 | KYC/AML/travel/sanctions/audit package | POST/PATCH | `/v1/compliance/**` |
| API-ADMIN-001–005 | settings/integrations/provision/audit | GET/PATCH/POST | `/v1/admin/**` |
| API-USER-001 | listUsers | GET | `/v1/users` |
| API-AUTHZ | assign role | POST | `/v1/authz/role-assignments` |
| API-AI-001–005 | assist/retrieve/recommendations/prompts | POST/GET | `/v1/ai/**` |

No new API IDs. No convenience endpoints.

## E. Database

None. No M11 Flyway. M0–M10 migrations unchanged.

## F. Permissions

Token-enforced (existing catalog). UI PermissionGuard hides mutating controls without the matching `alert:*` / `invest:*` / `comp:*` / `admin:*` / `ai:*` codes. Nav is RBAC-filtered.

## G–H. Events

None published or consumed by the SPA except DASH SSE **hints** (API-DASH-007) plus 15s poll fallback (WF-8). Domain events remain on M3 outbox simulation.

## I. Cross-domain

UI calls owning-domain APIs through the DASH BFF. ADMIN provision still orchestrates USER/ORG. COMP queue uses session-local known IDs (no invented list GET). Search composes list GETs.

## J–K. Security / tenant

M2 HMAC Bearer + `X-Organization-Id` from session (`org` claim). No second auth. Cross-tenant still denied by backends.

## L. Frontend/UI

- `/login` → `/login/mfa` → `/workspace`
- Alert/case/risk/compliance/admin workflows with confirmations
- SCR-15 AI complementary panel (no lifecycle buttons)
- Skip link, banner/nav/main, dialogs, tables, `aria-live` queue summary, reduced motion, 44px targets, status badges with text
- No V2 nav

## M. Tests

`web/src/app/App.test.tsx`: login/MFA, overview, WF-1 assign/close, WF-3/7 case close + AI isolation, WF-4 COMP confirm, admin, skip link/table headers (8 tests).

## N. Validation

| Check | Result |
|-------|--------|
| `./gradlew test` (JDK 21) | **PASS** (M0–M10; no Java changes in M11) |
| `python3 contracts/validate.py` | **PASS** |
| AI pytest + ruff | **PASS** (26 tests) |
| web vitest | **PASS** (8 tests) |
| web eslint | **PASS** |
| `git diff --check` | **PASS** |

Default `java` on PATH was 24 (`Type T not present` configuring Gradle). Re-ran with Temurin 21 as required by the backend toolchain.

## O. M0–M10 regression

Gradle suite green on JDK 21. AI and contracts unchanged. Frontend tests extended, not weakened.

## P. Known limitations

See handoff: no Playwright live-mesh E2E; COMP list GET still absent; MFA UI step even though identity login already sets `mfa_verified`; WCAG not certified; BFF upstreams must be configured for domain mutations.

## Q. Open questions

UX-OQ-COMP-LIST, UX-OQ-SEARCH, P11-OQ-STACK-002 (TanStack/Zod not adopted), live E2E environment.

## R. Frozen FRS/FDS

Unchanged. OpenAPI unchanged. SEC event lock unchanged.

## S. MVP/V2/V3

MVP UI only. SEC/REPORT/OPS/WALLET not added.

## T. AI assistive-only

AI panel has no close/assign/approve/priority/score controls. COMP decisions remain human-confirmed.

## U. Git

Uncommitted working tree. **No commit. No push.** Branch `main` HEAD `4b716f231b2cb85b95435ffeabe23aa6f7daf277`.

## V. M12

**M12 was NOT started.**
