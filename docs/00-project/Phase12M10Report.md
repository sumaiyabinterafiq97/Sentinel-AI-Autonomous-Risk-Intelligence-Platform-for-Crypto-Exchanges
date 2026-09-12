# Phase 12 M10 Report

## 1. Objective

Implement MVP ADMIN on `sentinel-platform`: settings, integrations, USER/ORG provision orchestration, admin audit query, approved ADMIN events via the existing M3 outbox simulation.

## 2. Pre-implementation audit

`docs/00-project/Phase12M10PreImplementationAudit.md`

## 3. ADMIN FRs implemented

| FR | Status |
|----|--------|
| ADMIN-FR-001 | Implemented (`admin.admin_settings` + `AdminSettingUpdated`) |
| ADMIN-FR-002 | Implemented (`admin.integration_configs` + `IntegrationConfigured`; plaintext secrets rejected) |
| ADMIN-FR-003 | Implemented (`admin.admin_action_log` + `core.audit_records` query) |
| ADMIN-FR-004 | Implemented as orchestration only (identity HTTP / test double; no `admin.users` / `admin.organizations`) |
| ADMIN-FR-005 | Implemented for approved ADMIN events only |
| ADMIN-FR-006 | Consume `ConfigurationUpdated` (and FRS names if delivered). **Does not publish** GD-002 deferred `UserCreated` / `OrganizationUpdated` |
| ADMIN-FR-007 | HMAC + ADMIN permissions + tenant match |
| ADMIN-FR-008 | GET settings / integrations / audit |

V2 ADMIN-FR integration health / API-ADMIN-006 **not** implemented.

## 4. API IDs and operationIds

| API ID | Methods | Path | operationIds |
|--------|---------|------|----------------|
| API-ADMIN-001 | GET/PATCH | `/v1/admin/settings` | `getAdminSettings`, `patchAdminSettings` |
| API-ADMIN-002 | GET/POST/PATCH | `/v1/admin/integrations` | `listIntegrations`, `createIntegration`, `patchIntegration` |
| API-ADMIN-003 | POST | `/v1/admin/users/provision` | `provisionUser` |
| API-ADMIN-004 | POST | `/v1/admin/organizations/provision` | `provisionOrganization` |
| API-ADMIN-005 | GET | `/v1/admin/audit-records` | `listAdminAuditRecords` |

API-ADMIN-006 not implemented. No convenience endpoints. OpenAPI unchanged.

## 5. Database

| Item | Detail |
|------|--------|
| Platform Flyway | `V011__admin_init_platform_admin_tables.sql` (schema `admin`) |
| Identity Flyway | `V005_8__authz_seed_admin_permissions.sql` |
| Tables | `admin.admin_settings`, `admin.integration_configs`, `admin.admin_action_log` |
| Outbox | Reused `core.outbox_events` |
| Not created | `admin.users`, `admin.organizations`, `admin.outbox_events` |
| Unchanged | Migrations 001–010, dash V012, ops/ai schemas |

## 6. Permissions

`admin:settings:write`, `admin:integration:write`, `admin:user:provision`, `admin:org:provision`, `admin:audit:read`

GET settings/integrations use **write** codes per APIInventory (gap M10-G1). Org-admin identity bootstrap includes `admin:` via `IdentityStore.identityAdminPermissionCodes()`.

## 7. Events published

`AdminSettingUpdated`, `IntegrationConfigured`, `AdminActionPerformed`  
Producer `ADMIN`. Stream names `sentinel.admin.{type}.v1`. Durable log remains **in-process simulation**.

## 8. Events consumed

`ConfigurationUpdated` (CORE). Handlers exist for `UserCreated`, `OrganizationUpdated`, `RoleAssigned` if delivered. Those USER/ORG names remain **GD-002 unpublished**.

Not published: `PlatformStarted`, `PlatformUnavailable`, `AgentRunFailed`, `UserCreated`, `OrganizationCreated`, `OrganizationUpdated`.

## 9. Orchestration

API-ADMIN-003/004 call identity `POST /v1/users` and `POST /v1/organizations` with the caller token (`IDENTITY_BASE_URL`). AUTHZ is not bypassed. Unset URL → `503 ADMIN_DEPENDENCY_001`. Tests use a Mockito double for happy-path and the real HTTP orchestrator for the dependency gap.

Cross-tenant provision (`organizationId` ≠ token org) → `403 AUTHZ_FORBIDDEN_001`.

## 10. DASH / UI

BFF forwards `/v1/admin` when `DASH_PLATFORM_BASE_URL` is set. `/v1/reports` still empty (V2). SPA SCR-12–14 call ADMIN APIs. No V2 navigation. DASH remains presentation/BFF only (no ADMIN controllers on dash).

## 11. Security / tenant

M2 HMAC. `X-Organization-Id` must match token org. ADMIN permissions enforced on `/v1/admin/**`. CORE `/v1/platform/**` behavior unchanged.

## 12. AI

No new agents. No ADMIN AI authority. Assistive SYSTEM/AI/EVIDENCE/HUMAN layers on existing domain screens unchanged.

## 13. Tests

- `AdminApiContractSurfaceIT`, `AdminOpenApiContractCompatibilityTest`
- `AdminApiIT` (authn/authz/tenant, settings, integrations/secrets, provision mock, audit, events)
- `AdminOrchestrationGapIT` (503 without identity URL)
- `AdminUpstreamConsumerIT`
- `AdminPermissionSeedIT`
- `OutboxUnitTest` ADMIN stream names
- Web vitest admin users screen
- Existing M0–M9 tests retained

## 14. Validation

| Check | Result |
|-------|--------|
| `./gradlew test` | **PASS** |
| `python3 contracts/validate.py` | **PASS** |
| AI pytest + ruff (`.venv`) | **PASS** (26 tests) |
| web vitest | **PASS** (4 tests) |
| web eslint | **PASS** |
| `git diff --check` | **PASS** |

## 15. Frozen FRS/FDS / OpenAPI

Unchanged. SEC event lock unchanged. No undocumented APIs or events invented.

## 16. MVP / V2 / V3

MVP ADMIN only. V2 API-ADMIN-006 not implemented. SEC/REPORT/OPS/WALLET not implemented.

## 17. Known limitations (open items)

1. Live provision requires `IDENTITY_BASE_URL` and caller `user:user:write` / `org:org:write` in addition to ADMIN provision permissions.
2. ADMIN-FR-006 cannot observe `UserCreated` / `OrganizationUpdated` until GD-002 is lifted (product decision; not done here).
3. Collection PATCH `/v1/admin/integrations` has no `{id}`; identify by `id` or `type` in body (M10-G2).
4. `secretRef` is a gap-fill column not in the OpenAPI create schema (M10-G3).
5. No Kafka/Redpanda. No Docker/K8s/CI added.
6. Cross-process identity HTTP is not an integration test (unit/API mock + 503 gap).
7. Shared access-boundary missing-org errors remain `CORE_VALIDATION_001` (same filter as CORE).

## 18. Git

Uncommitted working tree. **Not committed. Not pushed.** M0–M9 uncommitted work preserved.

## 19. M11

**M11 was NOT started.**
