# Phase 12 M5 Completion Report

## Document Information

| Field | Value |
|-------|-------|
| Project | Sentinel AI |
| Document | Phase 12 M5 Completion Report |
| Version | 1.0 |
| Status | **M5 ALERT COMPLETE** (durable log remains M3 in-process simulation) |
| Last Updated | 2026-09-11 |

---

## A. Overall result

**M5 ALERT is complete** on `sentinel-ops` against OpenAPI API-ALERT-001–007, migration 007, and approved ALERT event schemas. RISK still does not write `alert.*`. ALERT does not create INVEST cases. **M6 was not started.**

## B. Objective

Implement the ALERT domain: consume approved RISK events, own alert records/priority/assignment/lifecycle, expose authorized APIs, publish approved ALERT events through the existing transactional outbox, enforce M2 authz and tenant isolation.

## C. Repository audit

Pre-implementation audit: `docs/00-project/Phase12M5PreImplementationAudit.md`.

- Deployable: `sentinel-ops` (same as M4; ModuleImplementationPlan).
- APIs: API-ALERT-001–007. **API-ALERT-008 is V2 — not implemented.**
- Consume: `RiskCalculated`, `HighRiskDetected` via M3 `InMemoryDurableEventLog`.
- Publish: `AlertCreated`, `AlertAssigned`, `AlertClosed`.
- Schema: Flyway `V007__alert_init_alert_tables.sql`.
- Outbox: reuse ops `risk.outbox_events` (per-deployable table from M4). No `alert.outbox_events` (explicitly rejected by `QualifiedOutboxTable` tests).

## D. ALERT FR coverage

| FR | M5 | How |
|----|----|-----|
| ALERT-FR-001 | Yes | Generate from high/critical `RiskCalculated` or any `HighRiskDetected` |
| ALERT-FR-002 | Partial | FR is MVP; HTTP ingest is V2 — no ingest API |
| ALERT-FR-003 | Yes | ALERT-owned integer priority; analyst PATCH; not `prioritySignal` copy |
| ALERT-FR-004 | Yes | Assign API + `AlertAssigned` |
| ALERT-FR-005 | Yes | PATCH open→triaged; assign; close with disposition |
| ALERT-FR-006 | Yes | List/get |
| ALERT-FR-007 | Yes | Investigation **link** stores `case_id` only |
| ALERT-FR-008 | Yes | Approved ALERT events via outbox |
| ALERT-FR-009 | Yes | `core.audit_records` on mutations and event publish |
| ALERT-FR-010 | Yes | HMAC + inventory permissions + org match |
| ALERT-FR-011 | Yes | RISK event consumer |
| ALERT-FR-012 | No | AI triage assist (later) |

## E. ALERT API coverage

| API ID | operationId | Result |
|--------|-------------|--------|
| API-ALERT-001 | `listAlerts` | 200 |
| API-ALERT-002 | `getAlert` | 200 / 404 |
| API-ALERT-003 | `patchAlert` | 200 / 400 / 409 |
| API-ALERT-004 | `assignAlert` | 200 / 400 / 409 |
| API-ALERT-005 | `closeAlert` | 200 / 400 / 409 |
| API-ALERT-006 | `patchAlertPriority` | 200 / 400 / 409 closed |
| API-ALERT-007 | `linkAlertInvestigation` | 200 / 400 |

No search/bulk/admin/debug/ingest endpoints. Contract surface test includes RISK + ALERT + `/health` and excludes investigations/compliance/ingest.

## F. ALERT database/migration coverage

- `alert.alerts`, `alert.alert_risk_context`, `alert.alert_comments`, `alert.alert_investigation_links`
- Comments table exists (authorized) but **no comments HTTP API** in inventory — unused
- Identity `V005_3__authz_seed_alert_permissions.sql` (`write`, `assign`, `close`, `priority`). M2 `alert:alert:read` kept
- Org-admin provisioning includes `alert:*`
- No INVEST/COMP/WALLET/SEC tables

## G. RISK → ALERT event consumption

Listener on `InMemoryDurableEventLog` (not HTTP). New transaction (`REQUIRES_NEW`) so ALERT writes are not part of the RISK outbox claim transaction. RISK service still has no `alert.*` SQL.

Generation (documented smallest behavior):

- `HighRiskDetected` → create if no alert for that assessment
- `RiskCalculated` with `high`/`critical` → create if none
- low/medium `RiskCalculated` → refresh existing only

One alert per `(organization_id, risk_assessment_id)` at application level (no invented UNIQUE constraint). Duplicate `eventId` recorded in context jsonb `sourceEventIds`.

## H. ALERT event publishing

| Event | When | Classification |
|-------|------|----------------|
| `AlertCreated` | New alert row | confidential; idempotency key `{alertId}` |
| `AlertAssigned` | Assign (not no-op re-assign) | internal |
| `AlertClosed` | Close (not identical repeat) | internal |

Streams: `sentinel.alert.{eventType}.v1`. Envelope fields match existing M3/M4 pattern. No GD-002 events.

## I. Transactional outbox integration

`TransactionalOutbox.record` in the ALERT transaction. Alert + outbox commit/rollback tested (`AlertOutboxIT`). Relay remains at-least-once simulation. Failed listener still fails RISK-event publish so M3 retry applies.

## J. Security/authentication/authorization

`AlertAccessFilter` on `/v1/alerts**`: Bearer HMAC (M2), `X-Organization-Id` must match token, inventory permissions. Health remains public. `X-Sentinel-Permissions` is not used.

## K. Tenant isolation

List/get/patch/assign/close scoped by org. Cross-tenant get/patch → 404. Event `organizationId` is the tenant for created alerts; another org cannot read that assessment’s alert.

## L. Alert lifecycle implementation

Persisted states: `open`, `triaged`, `assigned`, `closed` (migration spec).

- PATCH: `open` → `triaged` (or no-op). Cannot PATCH to `assigned`/`closed`.
- POST assign: `open`/`triaged`/`assigned` → `assigned`. Closed → 409. Same assignee → 200 no extra event.
- POST close: open/triaged/assigned → `closed` with `dispositionReason`. Repeat same reason → 200. Closed + different reason → 409.

**Gap:** OpenAPI `Alert.status` enum omits `triaged`; event schema includes it. Implementation returns `triaged` when patched. OpenAPI was **not** edited.

## M. Alert priority implementation

**Gap-fill (not a frozen formula):** critical→90, high→70, medium→40, low→20, missing→70. Analyst PATCH is stored. System refresh of open/triaged alerts uses `max(current, mapped)` so inferred HighRiskDetected `high` does not lower a critical-derived value. `prioritySignal` is never stored as queue priority.

## N. Tests added

- `AlertApiIT` — APIs, authn/authz, tenant, lifecycle, events, investigation link without INVEST schema, low-risk no create
- `AlertRiskConsumerIT` — duplicate RISK delivery, HighRiskDetected refresh, tenant of event
- `AlertOutboxIT` — transactional outbox
- `AlertLifecycleTest`, `AlertPriorityTest`
- M4 `RiskApiIT` / `RiskApiContractSurfaceIT` updated for ALERT consumer + inventory paths
- Identity deny-by-default example moved from `alert:alert:write` (now granted to org-admin) to `invest:case:write`
- `EventStreams` AlertCreated stream name

## O. Full validation results

| Check | Result |
|-------|--------|
| `./gradlew test` | **PASS** |
| `python3 contracts/validate.py` | **PASS** (76 operationIds; SEC lock OK) |
| AI pytest / ruff (ai-service `.venv`) | **PASS** (2 health tests; ruff clean) |
| web vitest | **PASS** (1 test) |
| `git diff --check` | **PASS** |

System `python3 -m pytest` without the venv fails with missing `fastapi` — environment, not an M5 regression. M4 used the project venv the same way.

## P. M0–M4 regression results

Platform, identity, dash, common-outbox, common-security, contracts, ops RISK tests passed in the same Gradle run. RISK still scores and publishes RISK events; ALERT consumes them.

## Q. Frozen FRS/FDS validation

`git diff` on `FunctionalRequirements.md`, `FunctionalDomainSpecification.md`, and `OpenAPI.yaml` is empty. CORE–SEC frozen text not rewritten. SEC event lock intact. AI remains health-only. RISK does not insert alerts. ALERT does not create INVEST cases. No V2 ingest.

## R. Known limitations

- Durable log is still in-process simulation (not Kafka).
- Consumer idempotency is application-level (assessment lookup + jsonb event ids), not a domain `processed_event_ids` table (not in migration 007).
- `IdempotentConsumerSkeleton` remains in-memory and is not the ALERT SoT.
- Priority numeric mapping is a documented gap-fill.
- `alert.alert_comments` has no API.
- API-ALERT-008 / ALERT-FR-012 not implemented.
- ClosedBy on `AlertClosed` uses the authenticated user; system actor close is not an HTTP path.

## S. Documentation gaps

See pre-implementation audit: OpenAPI vs `triaged`; comments table vs API-ALERT-005 mapping in migration spec (inventory maps 005 to close); generation predicate underspecified; no durable processed-event table.

## T. M6 was NOT started

No INVEST APIs, events, or `invest` schema. No CaseCreated. No COMP/AI/WALLET/SEC/REPORT/OPS business logic.

## U. Git status

Uncommitted local work (Phases 1–11 docs + M0–M5 implementation). **No commit. No push.** M5 lives under `backend/services/ops` (alert package + V007), identity `V005_3`, `common-outbox` stream names, `backend/README.md`, and `docs/00-project/Phase12M5*.md`.

## V. Recommended next milestone

**M6 INVEST** — investigation cases consuming `AlertCreated` / `RiskCalculated`, without implementing COMP or AI agents.
