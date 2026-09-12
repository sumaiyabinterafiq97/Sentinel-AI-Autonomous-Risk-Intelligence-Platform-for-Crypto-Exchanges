# Phase 12 M2 Completion Report

## Document Information

| Field | Value |
|-------|-------|
| Project | Sentinel AI |
| Document | Phase 12 M2 Completion Report |
| Version | 1.0 |
| Status | **M2 IDENTITY COMPLETE** (local simulation auth — not OIDC) |
| Last Updated | 2026-09-11 |

---

## STEP 1 audit (pre-code)

### M2 requirements identified
AUTH/AUTHZ/USER/ORG MVP on `sentinel-identity` per ModuleImplementationPlan (order M2). CORE remains on `sentinel-platform`. Outbox/broker remains M3.

### M2 API IDs implemented
API-AUTH-001–005; API-AUTHZ-001–004; API-USER-001–004; API-ORG-001–003. **API-ORG-004 is V2 — not implemented.**

### M2 migrations
002 `auth`, 003 `authz`, 003b permission seed, 004 `user`, 005 `org`. No 006+.

### M2 events
Schemed: `UserLoggedIn`, `SessionExpired`, `UserUpdated`.  
**Not published (no approved schema / GD-002 optional):** `UserCreated`, `OrganizationCreated`, `OrganizationUpdated`, `PlatformStarted`, `PlatformUnavailable`, `AgentRunFailed`.

### Security expectations
AUTH owns credentials/sessions; AUTHZ deny-by-default evaluation; USER/ORG tenant-scoped. SecurityImplementationPlan mentions JWT/session — implemented as **local HMAC JWT-shaped tokens**, explicitly **not OIDC / not a vendor IdP**.

### M1 components replaced
`X-Sentinel-Permissions` is no longer the CORE permission authority. Platform verifies AUTH-issued HMAC access tokens and checks permission claims. Identity re-resolves permissions from AUTHZ on each request.

### Ambiguities / gaps (not invented as APIs)
1. **Migration 002 omitted a credentials table** while FRS states AUTH owns credentials and USER must not store them. M2 added AUTH-owned `auth.credentials` (password **hashes only**) and recorded this as a documentation defect.
2. **No set-password HTTP API** (USER create has no password field). Tests/internal `AuthService.setPasswordForUser` only.
3. **Login OpenAPI lists 400 but not 401**; invalid credentials use `401 AUTH_AUTHENTICATION_001` per ErrorHandling.md.
4. **Evaluate `action` is treated as a permission code** (mapping undocumented).
5. **MFA is simulation:** any 6-digit code verifies; no TOTP enrollment API in inventory (AUTH-FR-009/010/011 have no APIs — not invented).
6. **Token TTL** not specified; defaults 900s access / 28800s refresh, labeled simulation.
7. **Inventory FR numbering vs FRS** mismatches (e.g. logout AUTH-FR-003 vs FRS-002). Behavior followed **OpenAPI + FRS intent**, not invented endpoints.
8. **Bootstrap/provision** of the first admin is test-seeded; ADMIN provision APIs are M10.
9. **ADR-015 outbox is M3**; identity events are in-process like CORE M1.

These gaps did not require frozen FRS/FDS edits. Implementation continued with the narrowest AUTH-owned store and labeled simulation where the contract required an API without a specified crypto/IdP product.

---

## A. Objective

Establish AUTH / AUTHZ / USER / ORG identity foundation for later milestones. Do not start M3.

---

## B. Authoritative documents used

ImplementationPlan, ModuleImplementationPlan, Backend/Security/Data/Event plans, APIInventory, OpenAPI, EventContracts/schemas, InitialMigrationSpecifications, DataArchitecture, DomainBoundaries, SecurityArchitecture, ADRs, FRS (read-only), Phase12M1Report.

---

## C. AUTH implementation

- Login (email/password), session, refresh (hashed refresh tokens), logout (session revoked + `SessionExpired`), simulation MFA verify.
- Sessions, refresh_tokens, auth_events, registered_devices (table only; no device APIs in inventory).
- Local HMAC access tokens (`AccessTokenCodec`). **Not production OIDC.**
- Inactive users denied with the same authentication error as unknown users (no email enumeration).

---

## D. AUTHZ implementation

- Permission catalog seed (003b), org-scoped roles, role-permissions, assignments.
- `POST /v1/authz/evaluate` returns **201** per OpenAPI; deny-by-default if permission not granted.
- List/create roles; assign role. System role `Organization Administrator` provisioned on org create (003b template).

---

## E. USER implementation

- List/get/create/patch (displayName/status). Create does not set credentials. Soft-deactivate via `deleted_at` when status=`inactive`.
- Tenant isolation on `organization_id`. `UserUpdated` published; `UserCreated` not published.

---

## F. ORG implementation

- List membership-scoped orgs; create org; patch own org only (cross-tenant 403).
- Memberships created for org creator and for newly created users.
- `parent_org_id` unused (V2 hierarchy). Org lifecycle events not published (no schemas).

---

## G. API coverage

| API ID | operationId | Status |
|--------|-------------|--------|
| API-AUTH-001 | `login` | Implemented |
| API-AUTH-002 | `logout` | Implemented |
| API-AUTH-003 | `refreshToken` | Implemented |
| API-AUTH-004 | `getSession` | Implemented |
| API-AUTH-005 | `verifyMfa` | Implemented (simulation) |
| API-AUTHZ-001 | `evaluatePermission` | Implemented |
| API-AUTHZ-002 | `listRoles` | Implemented |
| API-AUTHZ-003 | `createRole` | Implemented |
| API-AUTHZ-004 | `assignRole` | Implemented |
| API-USER-001–004 | `listUsers` / `createUser` / `getUser` / `patchUser` | Implemented |
| API-ORG-001–003 | `listOrganizations` / `createOrganization` / `patchOrganization` | Implemented |
| API-ORG-004 | hierarchy | **V2 skipped** |

No undocumented routes (`IdentityApiContractSurfaceIT`).

---

## H. Database migrations

Identity Flyway table `identity_schema_history`:

- V002 auth (+ `auth.credentials` gap-fill)
- V003 authz
- V003_1 permission seed
- V004 `"user"` schema
- V005 org

Tests also apply CORE V001 from identity **test** resources so audit inserts work. Production identity `baseline-version=1` expects platform V001 already on the shared database.

---

## I. Events

Published with envelope + in-process bus: `UserLoggedIn`, `SessionExpired`, `UserUpdated`. SEC consume lock unchanged. GD-002 events not published.

---

## J. Security / tenant isolation

- Public: `/health`, `/v1/auth/login`, `/v1/auth/refresh`.
- Bearer HMAC token required otherwise; identity reloads AUTHZ permissions from DB.
- CORE: token org must match `X-Organization-Id`; permissions from token claims.
- Users of org A cannot read org B users (404). Org patch across tenants 403.
- Passwords stored as BCrypt hashes; refresh tokens SHA-256 hashed. Tokens/secrets not logged.

---

## K. Tests and validation

| Check | Result |
|-------|--------|
| `./gradlew test` | **PASS** |
| `python3 contracts/validate.py` | **PASS** (76 operationIds, 30 JSON files, SEC lock) |
| `git diff --check` | **PASS** |
| AI pytest + ruff | **PASS** |
| Web vitest | **PASS** |

Identity tests: login success/failure, session/logout/refresh, MFA simulation, AUTHZ allow/deny, user tenant isolation + UserUpdated, org membership isolation, unauthenticated 401, health, contract surface.

---

## L. M1 compatibility / regression

Platform CORE tests updated to mint HMAC tokens instead of `X-Sentinel-Permissions`. Health/config/flags/status behavior unchanged. Identity/ops/dash M0 health tests pass.

---

## M. Files created

Identity domain/application/API/infrastructure, Flyway V002–V005, identity tests, `AccessTokenCodec` / `AccessPrincipal` in `common-security`, this report.

---

## N. Files modified

`backend/services/identity/*` (from M0 skeleton), `backend/services/platform` access filter + CORE API tests + security config/yml, `backend/libs/common-security`, `backend/README.md`.

FRS/FDS/OpenAPI/event schemas **not** modified.

---

## O. Limitations

Local HMAC JWT is **simulation-grade identity**, not a production IdP. MFA is not TOTP. No password-reset APIs. No durable outbox. First admin is test-seeded. CORE does not call live AUTHZ evaluate (uses token claims). Default HMAC key in yaml is a non-production placeholder — override via env.

---

## P. Deferred

M3 outbox; AUTH-FR-009–011/017–019 without APIs; AUTH-FR-020 IdP (V2); API-ORG-004; ADMIN provision (M10); later-domain business APIs.

---

## Q. Documentation gaps

See STEP 1 audit items 1–9. Most important: **credential table missing from migration 002**; **UserCreated event listed on API-USER-002 but schema deferred**.

---

## R. Later milestones not implemented

**Confirmed.** No DASH/ALERT/RISK/INVEST/COMP/AI-agent/WALLET/SEC/REPORT/OPS business logic. No M3 broker. AI remains assistive-only (`GET /health`).

---

## S. Git status

No commit, no push. Working tree still contains prior uncommitted Phase 1–11 + M0/M1 plus M2 identity/platform/common-security changes and this report.

---

## T. Recommended M3

**M3 — transactional outbox / event relay (ADR-015)** for CORE and identity events, without starting RISK/ALERT domain engines.
