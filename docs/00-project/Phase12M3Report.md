# Phase 12 M3 Completion Report

## Document Information

| Field | Value |
|-------|-------|
| Project | Sentinel AI |
| Document | Phase 12 M3 Completion Report |
| Version | 1.0 |
| Status | **M3 OUTBOX + RELAY COMPLETE** (local durable-log **simulation** — not Kafka/Redpanda) |
| Last Updated | 2026-09-11 |

---

## STEP 1–2 audit (pre-code)

### 1. Event publishing before M3
M1 CORE and M2 identity published envelopes **in-process** (`InProcessCoreEventBus`, `IdentityEventBus`) inside the domain transaction. No durable outbox. No relay.

### 2. Envelope
Logical envelope from EventArchitecture / EventContracts / `envelope.schema.json`: `eventId`, `eventType`, `schemaVersion`, `timestamp`, `producer`, `correlationId`, `causationId`, `organizationId`, `payload`, `metadata.{classification,idempotencyKey,actorId,actorType}`.

### 3. Events already emitted
`ConfigurationUpdated`, `FeatureFlagChanged`, `UserLoggedIn`, `SessionExpired`, `UserUpdated`.

### 4. Transaction boundaries
Spring `@Transactional` on CORE config/flag patches and AUTH/USER/ORG writes.

### 5–7. Database
CORE `core.*` (platform Flyway V001). Identity Flyway V002–V005 (`auth`, `authz`, `"user"`, `org`) plus test-copied V001. `common-outbox` was M0 scaffolding only.

### 8. M3 requirements (ADR-015 + plans)
Transactional outbox → relay → durable log; at-least-once; consumer idempotency via `eventId` / `idempotencyKey`; bounded exponential backoff; DLQ after max attempts; broker **product deferred** (ADR-015 / ADR-019). ImplementationPlan M3: outbox, publisher, consumer skeleton, schema validation. No new HTTP APIs.

### 9. Migration
**Gap:** `InitialMigrationSpecifications.md` / `DataImplementationPlan.md` logical IDs 001–012 skip M3 (006 is RISK/M4). No outbox table DDL exists in the data specs.

### 10–14. Relay / idempotency / retry / observability
Documented: poll/publish at-least-once; stable `eventId` on retry; exponential backoff, max attempts configurable; DLQ `{stream}.dlq`; malformed quarantine; metrics “outbox backlog; DLQ depth”. Concurrent workers implied by “safe concurrent relay”. No numeric max-attempts or backoff base specified.

### Ambiguities (not invented as APIs or events)
1. **No outbox table in InitialMigrationSpecifications.** Implemented as platform `core.outbox_events` (Flyway V002) and identity `auth.outbox_events` (Flyway V005_1), matching “per service DB / platform outbox pattern”.
2. **Broker product not authorized** — in-process `InMemoryDurableEventLog` labeled simulation.
3. **Retry numbers unspecified** — simulation defaults: maxAttempts=5, backoff base 200ms, claim timeout 30s.
4. **Consumer `processed_event_ids` tables** are specified for AI (M8), not M3 — in-memory consumer skeleton only.
5. DataArchitecture text “commit then publish” vs ADR-015 same-TX outbox: treated as **commit before visibility on the durable log**, outbox insert atomic with domain state.

No frozen FRS/FDS change was required. No M4+ logic required to make M3 work.

---

## A. Overall result

M3 is implemented for **sentinel-platform** and **sentinel-identity**. Domain writes that already emitted approved events now insert an outbox row in the **same database transaction**. A relay claims rows (`FOR UPDATE SKIP LOCKED`), appends to a **vendor-neutral durable-log interface**, and marks published or retries / dead-letters.

This is **not** production broker infrastructure. The durable log is an in-process simulation. A Kafka-compatible product remains deferred until an ops ADR.

## B. Objective

Establish reliable event publication (ADR-015) without implementing M4 RISK or any later domain.

## C. Authoritative documents inspected

ProjectRoadmap, Phase12 M0 kickoff / M1 / M2 reports, ImplementationPlan and architecture/module/event/data/backend/testing/security/observability/traceability/dependencies/risks plans, EventArchitecture, ADRs (esp. ADR-015, ADR-019), DomainBoundaries, SystemArchitecture, SecurityArchitecture, EventContracts, envelope + catalog + event schemas, APIContractGovernance, SchemaRegistryGovernance, MessageBrokerArchitecture, DataArchitecture, MigrationStrategy, InitialMigrationSpecifications, DataRetention, ObservabilityArchitecture, FRS/FDS (read-only). Existing M0–M2 backend code.

## D. Existing event implementation before M3

Direct in-process `bus.publish(envelope)` from `CoreEventPublisher` / `IdentityEventPublisher`, with `EVENT_PUBLISHED` audit in the same call.

## E. Outbox architecture

```
BUSINESS TX → domain tables + outbox row COMMIT
        → after-commit + scheduled poll
        → RELAY (SKIP LOCKED)
        → DurableEventLog.append(sentinel.{domain}.{eventType}.v{major})
        → mark PUBLISHED | retry PENDING | DEAD + {stream}.dlq
```

Application API: `TransactionalOutbox.record(envelope)` — no broker types leak into domain services.

## F. Database migration

| Service | Flyway | Table |
|---------|--------|-------|
| platform | `V002__outbox_init_event_tables.sql` | `core.outbox_events` |
| identity | `V005_1__outbox_init_event_tables.sql` | `auth.outbox_events` |

Columns: `event_id` (PK), `event_type`, `schema_version`, `organization_id`, `producer`, `correlation_id`, `envelope` (jsonb), `created_at`, `status` (PENDING/PUBLISHING/PUBLISHED/DEAD), `attempt_count`, `next_attempt_at`, `claimed_at`, `published_at`, `last_error`. Relay index on unpublished rows; org+created index. M0–M2 migrations were not modified. No RISK/ALERT/… schemas.

Identity V005_1 is **not** logical data-spec 006 (RISK).

## G. Event publishing abstraction

`com.sentinel.common.outbox.TransactionalOutbox` / `JdbcTransactionalOutbox`. Refuses writes outside a Spring transaction. Rejects GD-002 / unpublished USER/ORG create events. After commit, triggers relay.

## H. Relay architecture

`OutboxRelay` + `OutboxPoller` (`@Scheduled`, disabled in tests via `sentinel.outbox.relay.poll-enabled=false`). Claim uses PostgreSQL `FOR UPDATE SKIP LOCKED`. Stale `PUBLISHING` rows (claim timeout) are reclaimed (restart recovery).

`DurableEventLog` is the vendor-neutral append API. **`InMemoryDurableEventLog` is the M3 simulation.** In-process buses now record **delivered** envelopes for existing tests.

## I. Delivery semantics

**At-least-once.** Duplicate delivery is possible if append succeeds and `PUBLISHED` is not persisted before crash. Exactly-once is **not** claimed. Consumers must be idempotent.

## J. Idempotency

`eventId` is created once at outbox insert and reused on retry/redelivery. `metadata.idempotencyKey` remains the business key in the envelope. `IdempotentConsumerSkeleton` tracks processed `eventId` in memory (duplicate → no extra side effect). Domain `processed_event_ids` tables are **not** created (AI/M8).

## K. Retry / failure handling

Exponential backoff (`base * 2^(attempt-1)`, simulation base 200ms). Configurable `max-attempts` (default 5). Exhaustion → status `DEAD` and append to `{stream}.dlq`. Malformed envelope → DEAD. Events are not discarded silently. Unbounded retry is not used.

## L. Security / tenant behavior

No new public APIs. Relay is internal. Logs include `eventId`, `eventType`, `organizationId`, `correlationId`, stream, attempt — **not** payloads, tokens, or passwords. `organization_id` stored and asserted per event. AUTH/AUTHZ filters unchanged. Health remains public.

## M. Observability

In-process `OutboxMetrics` (created, attempts, published, failed, dead-lettered) plus repository backlog / oldest-pending queries. Structured relay logs with correlation identifiers. Full OTel/Prometheus remains later (ObservabilityImplementationPlan / M12).

## N. Tests

| Area | Coverage |
|------|----------|
| Atomicity | commit together; rollback clears outbox; duplicate `event_id` rolls back domain |
| Relay | discover, deliver, mark PUBLISHED, retry same id, DLQ, stale-claim recovery, concurrent workers |
| Idempotency | consumer skeleton duplicate `eventId` |
| Contracts | existing CORE envelope/payload tests; GD-002 still unpublished |
| Tenancy | outbox/log `organizationId` preserved |
| Identity | login/logout outbox + UserLoggedIn/SessionExpired delivery |
| Regression | M0 dash/ops health; M1 CORE API/IT; M2 identity API/IT |

## O. Contract validation

`python3 contracts/validate.py` — **PASS** (76 operationIds, 30 JSON schemas, SEC lock OK). No new event schemas. No new OpenAPI operations.

## P. Regression validation

| Check | Result |
|-------|--------|
| `./gradlew test` | **PASS** |
| `python3 contracts/validate.py` | **PASS** |
| AI pytest + ruff | **PASS** |
| web vitest | **PASS** |
| `git diff --check` | **PASS** |
| Frozen FRS/FDS | **Unchanged** |
| SEC event schemas | **Unchanged** |

## Q. Files created (M3)

- `backend/libs/common-outbox/src/main/java/com/sentinel/common/outbox/*` (outbox/relay/log/metrics/consumer skeleton)
- `backend/libs/common-outbox/src/test/java/com/sentinel/common/outbox/OutboxUnitTest.java`
- `backend/services/platform/src/main/resources/db/migration/V002__outbox_init_event_tables.sql`
- `backend/services/platform/src/main/java/com/sentinel/platform/config/PlatformOutboxConfig.java`
- `backend/services/platform/src/test/java/com/sentinel/platform/application/OutboxInfrastructureIT.java`
- `backend/services/identity/src/main/resources/db/migration/V005_1__outbox_init_event_tables.sql`
- `backend/services/identity/src/main/java/com/sentinel/identity/config/IdentityOutboxConfig.java`
- `backend/services/identity/src/test/java/com/sentinel/identity/IdentityOutboxIT.java`
- `docs/00-project/Phase12M3Report.md`

## R. Files modified (M3)

- `backend/libs/common-outbox/build.gradle.kts`
- `backend/services/platform/build.gradle.kts`, `application.yml`, `CoreEventPublisher.java`, `InProcessCoreEventBus.java`, `PlatformPostgresIT.java`
- `backend/services/identity/build.gradle.kts`, `application.yml`, `IdentityEventPublisher.java`, `IdentityEventBus.java`, `IdentityPostgresIT.java`
- `backend/README.md`

Pre-existing uncommitted Phase 1–11 / M0–M2 tree is preserved (`.gitignore`, docs, `ai-service/`, `contracts/`, `web/`, M0–M2 reports, etc.).

## S. Events integrated

`ConfigurationUpdated`, `FeatureFlagChanged`, `UserLoggedIn`, `SessionExpired`, `UserUpdated`.

## T. Events intentionally not published

GD-002: `PlatformStarted`, `PlatformUnavailable`, `AgentRunFailed`.  
No approved payload schema: `UserCreated`, `OrganizationCreated`, `OrganizationUpdated`.  
No M4+ domain events.

## U. Limitations

- Durable log is **in-process simulation**, not a clustered log-oriented broker.
- Cross-process consumption (identity → future SEC/COMP) is not wired; each JVM has its own simulation log.
- Retry/claim numeric defaults are simulation values.
- Consumer dedupe is in-memory (lost on process restart) — matches M3 skeleton, not production consumers.
- `EVENT_PUBLISHED` audit means **outbox insert succeeded**, not broker ack.
- Shared Postgres with two Flyway histories: two outbox tables to avoid DDL collision.

## V. Deferred items

- Kafka/Redpanda (or other) product ADR and real relay producer
- Cross-service consumer groups and lag metrics
- Domain `processed_event_ids` (except AI spec at M8)
- OPS alerts on poison messages (MessageBrokerArchitecture: OPS V2)
- Outbox row retention/purge (ADR-018 event-log retention is broker-side)
- Logical migration ID for outbox in InitialMigrationSpecifications

## W. Documentation gaps discovered

1. No outbox DDL or logical migration ID for M3 (006 reserved for RISK).
2. No numeric retry policy.
3. Outbox ownership (`core` vs per-domain vs dedicated schema) only described as “owned schema or platform outbox pattern”.
4. Consumer idempotency storage specified for `ai.processed_event_ids`, not for CORE/identity.

These were implemented with the narrowest documented pattern (per-deployable outbox + simulation log) and labeled honestly. Frozen FRS/FDS were not edited.

## X. Confirmation — no M4+ business logic

No DASH/ALERT/RISK/INVEST/COMP/AI-agent/WALLET/SEC/REPORT/OPS business features. No new HTTP APIs. No new event schemas. AI remains assistive-only (`GET /health`). SEC consume/publish lock unchanged. AUTH/AUTHZ behavior preserved.

## Y. Git status

**No commit. No push.**

```
 M .gitignore
 M README.md
 M docs/00-project/Phase12M0Kickoff.md
 M docs/01-product/ProductScope.md
 M docs/01-product/Vision.md
?? .env.example
?? ai-service/
?? backend/
?? contracts/
?? docs/00-project/Phase12M0Report.md
?? docs/00-project/Phase12M1Report.md
?? docs/00-project/Phase12M2Report.md
?? docs/00-project/Phase12M3Report.md
?? docs/08-development/M0DeveloperSetup.md
?? scripts/m0-validate.sh
?? web/
```

M3 changes live under `backend/libs/common-outbox`, `backend/services/platform`, `backend/services/identity`, `backend/README.md`, and this report. The rest is pre-existing uncommitted Phase 1–11 / M0–M2 work.

## Z. Recommended M4

**M4 — RISK** (scoring, rules, assessments, `RiskCalculated` / `HighRiskDetected`) using this outbox, without starting ALERT lifecycle.

---

## Acceptance checklist

- [x] M3 documents audited
- [x] Transactional outbox implemented
- [x] Correct next Flyway migrations (platform V002, identity V005_1)
- [x] Domain TX + outbox atomic (tested)
- [x] Publishing abstraction integrated
- [x] Relay implemented
- [x] Durable-log abstraction (simulation implementation)
- [x] Retry + stable eventId
- [x] Rollback does not leave outbox rows
- [x] Stale-claim restart recovery tested
- [x] Concurrent relay tested
- [x] Approved schemas only; GD-002 unpublished
- [x] SEC lock unchanged
- [x] Tenant context preserved
- [x] M2 security preserved
- [x] M3 observability (metrics + logs + backlog)
- [x] M0/M1/M2 regression tests pass
- [x] Contract / OpenAPI / JSON validation pass
- [x] `git diff --check` pass
- [x] FRS/FDS untouched
- [x] No M4+ business logic
- [x] Phase12M3Report.md created
- [x] No commit
- [x] No push
