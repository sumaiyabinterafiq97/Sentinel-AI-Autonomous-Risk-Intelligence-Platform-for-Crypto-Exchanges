# Phase 12 M6 Handoff

## Status

**M6 = complete** on `sentinel-ops` against OpenAPI API-INVEST-001–009, migration 008, and approved INVEST event schemas.

**M7 = NOT STARTED.**

## Implemented functionality

- Manual case create/list/get/patch
- Lifecycle `open` → `in_progress` → `pending_review` → `closed` (close is a dedicated API)
- Assignment, evidence reference attach, notes, timeline
- Alert-context case creation from `AlertCreated` (one case per source alert)
- `RiskCalculated` timeline enrichment when a matching alert/case already exists
- INVEST events via M3 `TransactionalOutbox` (ops `risk.outbox_events` simulation)

## APIs implemented

`listCases`, `createCase`, `getCase`, `patchCase`, `closeCase`, `assignCase`, `attachEvidence`, `getCaseTimeline`, `listCaseNotes`, `createCaseNote`.

## Events consumed

`AlertCreated`, `RiskCalculated`.

## Events published

`CaseCreated`, `CaseUpdated`, `CaseClosed`, `CaseAssigned`, `EvidenceAttached`.

## Database

Ops `V008__invest_init_case_tables.sql`. Identity `V005_4` INVEST permission seed.

## Tests

`InvestApiIT`, `InvestUpstreamConsumerIT`, `InvestOutboxIT`, `InvestLifecycleTest`, plus M0–M5 regression in `./gradlew test`.

## Known limitations

Simulation broker; evidence is a reference string not an upload; `case_links` has no API; notes GET requires `invest:case:write` per inventory; RiskCalculated arriving before AlertCreated cannot attach until a case exists (AlertCreated may read the RISK assessment row).

## Intentionally deferred

COMP, AI agents, WALLET, SEC, REPORT, OPS, V2, Kafka/Redpanda, evidence upload, case-link HTTP.

## M7

**Not started** and not authorized by this workstream. Recommended next milestone: **M7 COMP** on `sentinel-ops` (schema 009), consuming `CaseClosed` / `CaseUpdated` / `RiskCalculated` / `UserUpdated`, without AI agents.
