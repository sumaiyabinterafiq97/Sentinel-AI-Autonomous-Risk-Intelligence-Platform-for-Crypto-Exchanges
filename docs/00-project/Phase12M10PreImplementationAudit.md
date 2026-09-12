# Phase 12 M10 Pre-Implementation Audit

## Document Information

| Field | Value |
|-------|-------|
| Milestone | M10 — ADMIN only |
| Date | 2026-09-11 |
| Status | Authorized implementation basis |

---

## 1. Objective

Implement MVP ADMIN on `sentinel-platform`: settings, integrations, USER/ORG provision **orchestration**, admin audit query, approved ADMIN events via M3 outbox **simulation**.

ADMIN must not duplicate USER/ORG/AUTH/AUTHZ/RISK/ALERT/INVEST/COMP/AI/DASH ownership.

**M11/M12 not authorized.**

---

## 2. Sources inspected

Roadmap, ImplementationPlan/Architecture/Module/Backend/Data/Event/Security/Testing plans, ADRs, PRD, APIInventory, OpenAPI, EventContracts, event catalog, InitialMigrationSpecifications, DataArchitecture, DashboardScreens, MVPWorkflows, ComponentLibrary, Phase12 M9 report/handoff, platform/identity/dash code, permission seeds, DeferredEvents, QualifiedOutboxTable.

---

## 3. FR → API → DB → event → permission → UI

| FR | API | operationIds | DB | Event | Permission | UI |
|----|-----|--------------|----|-------|------------|-----|
| ADMIN-FR-001 | API-ADMIN-001 | `getAdminSettings`, `patchAdminSettings` | `admin.admin_settings` | `AdminSettingUpdated` | `admin:settings:write` (GET and PATCH per inventory) | SCR-13 |
| ADMIN-FR-002 | API-ADMIN-002 | `listIntegrations`, `createIntegration`, `patchIntegration` | `admin.integration_configs` | `IntegrationConfigured` | `admin:integration:write` | SCR-13 |
| ADMIN-FR-003 | API-ADMIN-005 | `listAdminAuditRecords` | `admin.admin_action_log` + `core.audit_records` | `AdminActionPerformed` on mutating admin actions | `admin:audit:read` | SCR-14 |
| ADMIN-FR-004 | API-ADMIN-003, 004 | `provisionUser`, `provisionOrganization` | none (delegate USER/ORG) | `AdminActionPerformed` | `admin:user:provision`, `admin:org:provision` | SCR-12 |
| ADMIN-FR-005 | (publish contract) | — | outbox `core.outbox_events` | approved ADMIN events only | — | — |
| ADMIN-FR-006 | consumer | — | action log context only | consume `ConfigurationUpdated`; handlers exist for FRS names that are **GD-002 deferred** as publishes | — | — |
| ADMIN-FR-007 | all ADMIN APIs | HMAC + permission | — | — | privileged codes above | — |
| ADMIN-FR-008 | GET 001/002/005 | discovery | settings, integrations, audit | — | same as inventory | SCR-13/14 |

**V2 not implemented:** API-ADMIN-006 integration health.

---

## 4. Deployable / migration

| Item | Decision |
|------|----------|
| Deployable | `sentinel-platform` (ModuleImplementationPlan + ImplementationTraceability). P11-OQ-MOD-001 recorded as resolved by that traceability |
| Flyway | `V011__admin_init_platform_admin_tables.sql` on platform (does not rewrite 001–010). Identity `V005_8` permission seed |
| Outbox | Reuse `core.outbox_events` (already allowed). Producer `ADMIN` |
| Dash V012 | Untouched |

---

## 5. Events

**Publish:** `AdminSettingUpdated`, `IntegrationConfigured`, `AdminActionPerformed` (approved schemas).

**Consume:** `ConfigurationUpdated` (catalog CORE→ADMIN). FRS also lists `UserCreated`, `OrganizationUpdated`, `RoleAssigned` — those names are **GD-002 deferred for publication**. Consume if delivered; **do not publish** them from identity/ADMIN.

**Must not publish:** PlatformStarted, PlatformUnavailable, AgentRunFailed, UserCreated, OrganizationCreated, OrganizationUpdated.

---

## 6. Orchestration

API-ADMIN-003/004 call existing identity HTTP `POST /v1/users` and `POST /v1/organizations` with the caller’s token (AUTHZ not bypassed). If identity URL unset → 503 `ADMIN_DEPENDENCY_001`. Tests use a test double; no `admin.users` / `admin.organizations` tables.

---

## 7. Gaps / gap-fills

| ID | Gap | Decision |
|----|-----|----------|
| M10-G1 | Inventory GET settings/integrations uses **write** permissions | Follow inventory; no invented `:read` |
| M10-G2 | PATCH `/v1/admin/integrations` has no `{id}` | Identify by `type` or `id` in body (schema allows additional properties) |
| M10-G3 | No `secret_ref` in OpenAPI create body | Reject plaintext `password`/`secret`/`apiKey` in `config`; optional `secretRef` copied to column |
| M10-G4 | ADMIN-FR-006 vs DeferredEvents | Consume-only; do not un-defer publishes |
| M10-G5 | Login tokens need both `admin:*` and `user:user:write` / `org:org:write` for live provision | Documented; AUTHZ remains on USER/ORG |
| M10-G6 | DASH BFF currently 503s `/v1/admin` | Forward to platform when `DASH_PLATFORM_BASE_URL` set |
| M10-G7 | SCR-12–14 were M9-unavailable | Wire to ADMIN APIs only (no V2 nav, no redesign) |

---

## 8. Security / tenant

M2 HMAC. `X-Organization-Id` must match token org. Cross-tenant 403. Privileged ADMIN permissions. Secrets never stored in `config` jsonb.

---

## 9. Out of scope

M11 frontend polish, M12 hardening, API-ADMIN-006, SEC/REPORT/OPS/WALLET, Kafka, Docker/K8s/CI, inventing USER CRUD under `/v1/admin/users/{id}`, AI agents.

---

## 10. Confirmation

No undocumented API IDs or event types will be added. OpenAPI/FRS/FDS will not be rewritten.
