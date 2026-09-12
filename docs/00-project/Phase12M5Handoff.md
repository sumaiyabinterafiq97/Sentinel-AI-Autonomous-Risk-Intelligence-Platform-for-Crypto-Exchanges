# Phase 12 M5 Handoff

## Status

**M5 ALERT is complete** on `sentinel-ops` against OpenAPI/APIInventory/migration 007 and approved ALERT event schemas.

**M6 was not started** and is **not authorized by this workstream**.

## Implemented functionality

- Alert create from RISK events (ALERT-owned; RISK does not insert `alert.*`)
- Queue list/get, PATCH lifecycle (`open` → `triaged`)
- Assignment, close with disposition, ALERT-owned priority PATCH
- Investigation **link** (`case_id` UUID only — no case row)
- Tenant isolation and M2 HMAC/AUTHZ
- `AlertCreated` / `AlertAssigned` / `AlertClosed` via existing ops `TransactionalOutbox` (`risk.outbox_events`)

## APIs implemented

API-ALERT-001–007: `listAlerts`, `getAlert`, `patchAlert`, `assignAlert`, `closeAlert`, `patchAlertPriority`, `linkAlertInvestigation`.

Not implemented: API-ALERT-008 (V2 ingest).

## Events consumed

`RiskCalculated`, `HighRiskDetected` (durable-log listener, at-least-once).

## Events published

`AlertCreated`, `AlertAssigned`, `AlertClosed`.

## Database changes

Ops `V007__alert_init_alert_tables.sql` (`alert.*`). Identity `V005_3` ALERT write/assign/close/priority permissions. No INVEST schema.

## Tests

`AlertApiIT`, `AlertRiskConsumerIT`, `AlertOutboxIT`, `AlertLifecycleTest`, `AlertPriorityTest`, plus updated RISK contract/API tests and EventStreams.

## Known limitations

See `Phase12M5Report.md` sections R/S: simulation broker, priority gap-fill, no comments API, no processed_event_ids table, OpenAPI `triaged` enum gap.

## Unresolved questions

- Exact numeric priority formula (gap-fill in use).
- Whether one alert per assessment should become a SQL unique key (FR says id is authoritative; application-level only).
- Whether OpenAPI should add `triaged` (not edited in M5).

## Intentionally deferred

INVEST, COMP, AI agents, SEC, WALLET, REPORT, OPS business logic, V2 ingest, production broker, auto-triage/escalation/SLA/dedup product feature, ALERT-FR-012.

## M6

**Not started.** M6 is the documented next coding milestone (INVEST) but requires a separate authorization. This handoff only recommends it.

## Recommended next step

Implement **M6 INVEST** on `sentinel-ops` (schema 008), consuming `AlertCreated` and `RiskCalculated`, without creating COMP outcomes or AI authority.
