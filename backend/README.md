# Sentinel AI — Backend

Java 21 / Spring Boot 3 multi-project Gradle build (ADR-019).

## Deployables

| Project | Deployable | Current milestone | Exposes |
|---------|------------|-------------------|---------|
| `services:platform` | sentinel-platform | **M10 ADMIN** (M1 CORE retained) | `GET /health`; API-CORE-001–006; API-ADMIN-001–005 |
| `services:identity` | sentinel-identity | **M2 AUTH/AUTHZ/USER/ORG** | `GET /health`; API-AUTH/AUTHZ/USER/ORG MVP |
| `services:ops` | sentinel-ops | **M7 COMP** (M4–M6 retained) | `GET /health`; API-RISK-001–007; API-ALERT-001–007; API-INVEST-001–009; API-COMP-001–007 |
| `services:dash` | sentinel-dash | **M9 DASH BFF + SSE** | `GET /health`; API-DASH-001–005, 007 |

**M1 CORE (platform):** config, feature flags, health/status, audit, CORE events.

**M2 identity:** login/session/MFA-verify (simulation 6-digit), roles/evaluate, users, organizations. Local HMAC session tokens (not OIDC). CORE protected routes accept those tokens; `X-Sentinel-Permissions` is no longer authoritative.

**M3 events:** transactional outbox + relay (ADR-015). Durable log is an **in-process simulation**, not Kafka. Platform table `core.outbox_events` (Flyway V002). Identity table `auth.outbox_events` (Flyway V005_1).

**M4 RISK (ops):** rules, ingest, evaluate, assessments. Deterministic scoring (no AI). Events via `risk.outbox_events`.

**M5 ALERT (ops):** alert queue, priority, assignment, lifecycle. Consumes `RiskCalculated` / `HighRiskDetected`. Publishes `AlertCreated` / `AlertAssigned` / `AlertClosed` via the same ops outbox. API-ALERT-008 (V2 ingest) not implemented.

**M6 INVEST (ops):** investigation cases, assignment, close, evidence references, notes, timeline. Consumes `AlertCreated` / `RiskCalculated`. Publishes `CaseCreated` / `CaseUpdated` / `CaseClosed` / `CaseAssigned` / `EvidenceAttached`.

**M7 COMP (ops):** KYC/AML start, Travel Rule recording, sanctions screen/disposition, audit packages. Human decisions only. Consumes `CaseClosed` / `CaseUpdated` / `RiskCalculated` (and `UserUpdated` if delivered on the ops simulation log). Publishes `ComplianceReviewed` / `TravelRuleValidated` / `SanctionsHitDetected` / `AuditPackagePrepared`. AI/REPORT not implemented.

**M8 AI (`sentinel-ai` Python):** Investigation/Risk/Retrieval assist APIs. Assistive only. Does not run on this Gradle build. See `ai-service/`.

Shared HMAC key: `IDENTITY_TOKEN_HMAC_KEY` on platform, identity, ops, and ai-service.

**M10 ADMIN (platform):** settings, integrations, USER/ORG provision orchestration, admin audit query. Schema `admin` via Flyway V011. Does not duplicate USER/ORG tables. Events via `core.outbox_events`.

Identity Flyway (`identity_schema_history`): V002–V005_8 (`auth`, `authz`, `"user"`, `org`, RISK/ALERT/INVEST/COMP/AI/DASH/ADMIN permission seeds). Ops Flyway (`ops_schema_history`): V006 `risk`, V007 `alert`, V008 `invest`, V009 `comp`. AI schema `010` lives on `sentinel-ai`. DASH Flyway (`dash_schema_history`): V012 `dash`. Platform Flyway: V001 `core`, V002 outbox, V011 `admin`. Production expects CORE V001 already applied on the same PostgreSQL.

## Database

Flyway migration `V001__core_init_platform_tables.sql` creates schema `core` only.

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sentinel
export SPRING_DATASOURCE_USERNAME=sentinel
export SPRING_DATASOURCE_PASSWORD=...
```

Tests use embedded PostgreSQL (no Docker required).

## Build / test

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
cd backend
./gradlew test
```
