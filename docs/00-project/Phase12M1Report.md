# Phase 12 M1 Completion Report

## Document Information

| Field | Value |
|-------|-------|
| Project | Sentinel AI |
| Document | Phase 12 M1 Completion Report |
| Version | 1.0 |
| Status | **M1 CORE COMPLETE** |
| Last Updated | 2026-09-11 |
| Authority | Phase12M0Kickoff.md; ApplicationDevelopmentGateDecision.md |

---

## A. M1 objective

Implement **CORE-only** business functionality on the existing M0 `sentinel-platform` deployable: platform health/status, configuration, feature flags, audit context, tenant isolation, contract-compatible errors, and CORE event publication for schemed events. **Do not start M2.**

---

## B. CORE requirements implemented

| FR | Implementation | Primary test |
|----|----------------|--------------|
| CORE-FR-001 | Startup init, seed `maintenance.mode`, mark ready | `PlatformInitializationIT` |
| CORE-FR-002 / CORE-FR-003 | Operational ready/unavailable via health/status + `PLATFORM_INITIALIZED` audit. **`PlatformStarted` / `PlatformUnavailable` not published** (GD-002 — no approved JSON schemas) | `CoreEventPublisherIT.gd002LifecycleEventsAreNotPublished` |
| CORE-FR-004 | `GET /v1/platform/health` (`getPlatformHealth`) | `PlatformCoreApiIT.getPlatformHealthIsPublicAndHealthy` |
| CORE-FR-005 | Database readiness in health evaluation | `PlatformInitializationIT`, health API |
| CORE-FR-006 | Optional AI listed `degraded` without blocking CORE ready | `PlatformInitializationIT` |
| CORE-FR-007 | `PATCH /v1/platform/config` | `PlatformCoreApiIT.configReadWriteAndAuditEvent` |
| CORE-FR-008 | `GET /v1/platform/config` (global + org overlay) | same |
| CORE-FR-009 | `PATCH /v1/platform/feature-flags/{flagKey}` | `PlatformCoreApiIT.featureFlagsPatchListAndPagination` |
| CORE-FR-010 | `GET /v1/platform/feature-flags` (resolved state, cursor pagination) | same |
| CORE-FR-011 | Privileged writes/reads require Bearer presence + permission stand-in (**not AUTH/AUTHZ domain**) | `getPlatformStatusRequiresAuthAndPermission` |
| CORE-FR-012 / CORE-FR-013 | Request/correlation/actor/org on audit + events | `CoreEventPublisherIT`, audit inserts |
| CORE-FR-014 | Append-only `core.audit_records` on config/flag changes | `CorePersistenceIT.auditRecordsAreAppendOnlyForOrg` |
| CORE-FR-015 | Key/body/limit/org UUID validation | `CoreKeysTest`, API 400 cases |
| CORE-FR-016 | `ErrorResponse` `{ error: { code, message, requestId, timestamp, ... } }` | 401/403/400 API tests |
| CORE-FR-017 | Publish `ConfigurationUpdated` / `FeatureFlagChanged`; record `EVENT_PUBLISHED` outcome. Lifecycle events deferred GD-002 | `CoreEventPublisherIT` |
| CORE-FR-018–022 | **Not implemented** (Version 2) | contract surface test |
| CORE-FR-023 | `core.platform_health_snapshots` | `CorePersistenceIT` |
| CORE-FR-024 | `GET /v1/platform/status` | `PlatformCoreApiIT` |
| CORE-FR-025 | `maintenance.mode` boolean config | `maintenanceModeChangesStatusAndCanBeRestored` |
| CORE-FR-026 | Disable maintenance restores `available` | same |

---

## C. Files created

CORE implementation under `backend/services/platform/` (domain, application, infrastructure, API, Flyway `V001__core_init_platform_tables.sql`, tests). This report: `docs/00-project/Phase12M1Report.md`.

---

## D. Files modified

- `backend/services/platform/build.gradle.kts` — JDBC, Flyway, PostgreSQL, embedded Postgres tests
- `backend/services/platform/src/main/resources/application.yml` — datasource + Flyway
- `backend/services/platform/src/main/java/com/sentinel/platform/PlatformApplication.java` — M1 comment
- `backend/services/platform/src/main/java/com/sentinel/platform/api/HealthController.java` — retain `/health`; milestone M1
- `backend/services/platform/src/test/java/com/sentinel/platform/api/HealthControllerTest.java`
- `backend/README.md` — M1 CORE notes

Not modified: frozen FRS/FDS, ProductScope/PRD (pre-existing uncommitted product docs left untouched by this task’s CORE work).

---

## E. CORE APIs implemented

| ID | Method | Path | operationId |
|----|--------|------|-------------|
| API-CORE-001 | GET | `/v1/platform/health` | `getPlatformHealth` |
| API-CORE-002 | GET | `/v1/platform/status` | `getPlatformStatus` |
| API-CORE-003 | GET | `/v1/platform/config` | `getPlatformConfig` |
| API-CORE-004 | PATCH | `/v1/platform/config` | `patchPlatformConfig` |
| API-CORE-005 | GET | `/v1/platform/feature-flags` | `listFeatureFlags` |
| API-CORE-006 | PATCH | `/v1/platform/feature-flags/{flagKey}` | `patchFeatureFlag` |

M0 `GET /health` retained. No AUTH/RISK/ALERT/… routes on platform (`CoreApiContractSurfaceIT`).

---

## F. CORE database/migrations implemented

Flyway `V001__core_init_platform_tables.sql` (logical `001_core_init_platform_tables`):

- schema `core`
- `platform_config`, `feature_flags`, `audit_records`, `platform_health_snapshots`
- unique org+key via `COALESCE(organization_id, nil-uuid)` indexes (portable equivalent of unique `(organization_id, config_key)` including global null)
- audit indexes as specified
- **No** migrations 002–012; no auth/risk/alert/… schemas

---

## G. CORE events implemented

| Event | Schema | Delivery |
|-------|--------|----------|
| `ConfigurationUpdated` | `events/mvp/core/ConfigurationUpdated.v1.schema.json` | In-process bus + audit `EVENT_PUBLISHED` |
| `FeatureFlagChanged` | `events/mvp/core/FeatureFlagChanged.v1.schema.json` | Same |

Envelope: `eventId`, `eventType`, `schemaVersion` `1.0`, `timestamp`, `producer` `sentinel-platform`, `correlationId`, `organizationId`, `payload`, `metadata` (classification, idempotencyKey, actor).

**Not published:** `PlatformStarted`, `PlatformUnavailable` (GD-002). Durable outbox relay remains **M3**.

---

## H. Security/tenancy behavior

- Health (`/health`, `/v1/platform/health`) is public.
- Other CORE routes: `X-Organization-Id` UUID required; missing/invalid → `400 CORE_VALIDATION_001`.
- Missing Bearer → `401 AUTH_AUTHENTICATION_001`.
- Missing permission in `X-Sentinel-Permissions` → `403 AUTHZ_FORBIDDEN_001`.
- Permissions: `platform:status:read`, `platform:config:read|write`, `platform:flags:read|write`.
- **This is an M1 access boundary adapter**, not AUTH login/JWT verification or AUTHZ role/permission lifecycle (M2).
- Queries scoped `organization_id IS NULL OR organization_id = :tenant`. Org A config/flags are not returned to org B.
- Client-supplied org is taken only from the validated header, not from body ownership claims.
- No hardcoded credentials/secrets.

---

## I. Tests added

- Domain: `CoreKeysTest`
- API: `PlatformCoreApiIT`, `HealthControllerTest` (M0 retained), `CoreApiContractSurfaceIT`
- Application: `CoreEventPublisherIT`, `PlatformInitializationIT`
- Persistence/tenant: `CorePersistenceIT`
- Contract: `CoreOpenApiContractCompatibilityTest`
- Embedded PostgreSQL: `PlatformPostgresIT` (Zonky; no Docker)

---

## J. Validation results

| Check | Result |
|-------|--------|
| CORE tests | **PASS** (`:services:platform:test`) |
| Full `./gradlew test` | **PASS** |
| AI pytest | **PASS** (2 tests, local venv) |
| AI ruff | **PASS** |
| Web vitest | **PASS** (1 test) |
| Web lint | **PASS** (ran with vitest session) |

---

## K. Build/lint/format results

- Backend build/test: **PASS**
- Java Spotless: **not configured** (same as M0)
- `git diff --check`: **PASS**

---

## L. OpenAPI validation

`python3 contracts/validate.py`: **PASS** — 76 operationIds, openapi 3.0.3. OpenAPI.yaml **not modified**. CORE operationIds unchanged.

---

## M. JSON Schema validation

Same script: **PASS** — 30 JSON files parse. SEC consume lock unchanged.

---

## N. Frozen FRS/FDS validation

`docs/02-requirements/FunctionalRequirements.md` and `FunctionalDomainSpecification.md` were **not modified** for M1.

---

## O. Domain-boundary validation

CORE lives in `services:platform` only. Identity/ops/dash remain M0 health skeletons. No RISK scoring, ALERT priority, INVEST cases, COMP decisions, AI agents, USER/ORG lifecycle, DASH orchestration.

---

## P. V2/V3 isolation validation

Not implemented: WALLET, SEC, REPORT, OPS, CORE-FR-018–022, insider threat, autonomous AI, automated containment, advanced chain integrations.

---

## Q. M0 regression validation

Identity/ops/dash `/health` tests still pass. AI `GET /health` only. Web placeholder `/` only. Contract packaging intact.

---

## R. Git status

No commit, no push (per instructions). M1 lives in untracked/modified `backend/` plus this report. Substantial pre-existing uncommitted Phase 1–11 documentation remains. `ai-service/.venv` may appear locally and must not be committed.

---

## S. Known limitations / open questions

1. **GD-002:** CORE-FR-002/003/017 lifecycle events have no approved payload schemas; M1 records init/health instead of inventing events.
2. **Outbox (ADR-015 / M3):** events are in-process, not broker-durable.
3. **M2 AUTH/AUTHZ:** Bearer is presence-checked; JWTs are not verified; permissions come from `X-Sentinel-Permissions` until AUTHZ exists.
4. **Config value vocabulary:** OpenAPI `ConfigPatchRequest` is free-form; M1 validates key shape and `maintenance.mode` boolean only.
5. **Unique (null org, key):** implemented with `COALESCE` unique indexes for PostgreSQL versions without `UNIQUE NULLS NOT DISTINCT`.
6. **Local `bootRun`** requires a real PostgreSQL (`SPRING_DATASOURCE_*`). Tests use embedded Postgres.
7. Java formatter still not configured.

---

## T. Explicit confirmation that M2+ was NOT implemented

**Confirmed.** No AUTH/AUTHZ/USER/ORG/DASH/ALERT/RISK/INVEST/COMP/AI-agent/WALLET/SEC/REPORT/OPS business functionality was added. No M2 work was started.

---

## U. Recommendation for the next milestone

**M2 — AUTH / AUTHZ / USER / ORG** on `sentinel-identity`, replacing the M1 permission header adapter with real authentication and authorization, without expanding CORE beyond audit/health integration contracts.
