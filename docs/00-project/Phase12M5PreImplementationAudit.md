# Phase 12 M5 Pre-Implementation Audit

## Document Information

| Field | Value |
|-------|-------|
| Milestone | M5 — ALERT only |
| Date | 2026-09-11 |

---

## 1. ALERT MVP FR IDs

| FR | M5? | Notes |
|----|-----|-------|
| ALERT-FR-001 | **Yes** | Generate alerts from RISK signals |
| ALERT-FR-002 | **API no** | FR is MVP; **API-ALERT-008 ingest is V2**. No ingest endpoint |
| ALERT-FR-003 | **Yes** | ALERT-owned queue priority |
| ALERT-FR-004 | **Yes** | Assign |
| ALERT-FR-005 | **Yes** | Lifecycle including close |
| ALERT-FR-006 | **Yes** | List/get |
| ALERT-FR-007 | **Yes** | Investigation **link only** — no case creation |
| ALERT-FR-008 | **Yes** | AlertCreated / AlertAssigned / AlertClosed |
| ALERT-FR-009 | **Yes** | CORE audit records |
| ALERT-FR-010 | **Yes** | AUTHZ + tenant |
| ALERT-FR-011 | **Yes** | Consume RiskCalculated, HighRiskDetected |
| ALERT-FR-012 | **No** | AI triage assist (later AI milestone) |

Deferred by FRS notes: escalation, auto-triage, SLA, SEC consumption, notifications, **deduplication as a named feature**.

## 2–4. APIs / operationIds / events

| API ID | Method | Path | operationId | Permission | Success |
|--------|--------|------|-------------|------------|---------|
| API-ALERT-001 | GET | `/v1/alerts` | `listAlerts` | `alert:alert:read` | 200 |
| API-ALERT-002 | GET | `/v1/alerts/{alertId}` | `getAlert` | `alert:alert:read` | 200 |
| API-ALERT-003 | PATCH | `/v1/alerts/{alertId}` | `patchAlert` | `alert:alert:write` | 200 |
| API-ALERT-004 | POST | `/v1/alerts/{alertId}/assign` | `assignAlert` | `alert:alert:assign` | 200 |
| API-ALERT-005 | POST | `/v1/alerts/{alertId}/close` | `closeAlert` | `alert:alert:close` | 200 |
| API-ALERT-006 | PATCH | `/v1/alerts/{alertId}/priority` | `patchAlertPriority` | `alert:alert:priority` | 200 |
| API-ALERT-007 | POST | `/v1/alerts/{alertId}/investigation-link` | `linkAlertInvestigation` | `alert:alert:write` | 200 |

**Not implemented:** API-ALERT-008 V2.

**Publish:** `AlertCreated`, `AlertAssigned`, `AlertClosed`.

**Consume:** `RiskCalculated`, `HighRiskDetected` only.

## 5. RISK events consumed

Same JVM M3 `InMemoryDurableEventLog` listener (no HTTP from RISK to ALERT). RISK must not insert `alert.*`.

## 6–7. Database / migration

Migration **007** `007_alert_init_alert_tables` on `sentinel-ops` Flyway (`ops_schema_history`), after V006:

- `alert.alerts`
- `alert.alert_risk_context`
- `alert.alert_comments` (no comment API in inventory — table only)
- `alert.alert_investigation_links`

No INVEST/COMP tables. Outbox: **reuse ops `risk.outbox_events`** (per-deployable outbox from M4). Do not invent a second broker.

## 8. NFRs

NFR-AUD-001 (audit), NFR-PERF-003 (queue via DASH — not a load suite in M5), tenant/security.

## 9. ADRs

ADR-001 co-locate ALERT on ops; ADR-003 RISK scoring stays in RISK; ADR-015 outbox; ADR-019 stack.

## 10–11. Integration points

- `TransactionalOutbox.record` for ALERT events in the ALERT transaction
- Durable-log listener after RISK relay delivers envelopes
- `IdempotentConsumerSkeleton` is in-memory and currently marks **all** delivered events — ALERT will use a **separate** consumer dedupe plus lookup by `risk_assessment_id`

## 12–13. Auth / tenant

M2 HMAC tokens. Inventory permissions (seed new codes; keep `alert:alert:read` from M2). Org header must match token. Cross-tenant get → 404.

## 14. Lifecycle

Migration: `open → triaged → assigned → closed`.

OpenAPI `Alert.status` enum is only `open | assigned | closed` (no `triaged`). Event `AlertCreated.status` allows `open | triaged | assigned`.

**Decision:** Persist all four states. PATCH `status` may set `triaged` (or remain `open`). Assign → `assigned`. Close endpoint → `closed`. Invalid transitions → 400/409. `triaged` is returned in API JSON even though OpenAPI enum omitted it (documented gap; OpenAPI not edited).

## 15–16. Gaps and smallest decisions

1. **When to create an alert:** create on `HighRiskDetected`, or `RiskCalculated` with `riskLevel` high/critical. Low/medium `RiskCalculated` refreshes existing context only.
2. **One alert per assessment:** application-level find-or-create by `(organization_id, risk_assessment_id)`. Spec says no business unique key — not added as SQL UNIQUE. Prevents duplicate rows on at-least-once replay.
3. **Priority formula unspecified.** ALERT mapping (not `prioritySignal` copy): critical→90, high→70, medium→40, low→20, HighRiskDetected without level→70. Analyst PATCH is authoritative thereafter until a later high-risk refresh of an **open/triaged** alert (does not override assigned/closed).
4. **Deduplication FR deferred** — eventId skip + assessment find-or-create only.
5. **ALERT-FR-002 / API-ALERT-008 V2** — not implemented.
6. **ALERT-FR-012** — not implemented.
7. **Comments table** has no API — not exposed.
8. **Investigation link** stores `case_id` UUID only; no INVEST case row.
9. **Permission seed:** M2 has `alert:alert:read` only — add `V005_3` write/assign/close/priority.

OpenAPI/FRS/FDS will not be rewritten.
