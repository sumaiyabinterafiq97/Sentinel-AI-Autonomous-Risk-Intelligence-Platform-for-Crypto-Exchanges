# Phase 12 M7 Handoff

## Status

**M7 = COMPLETE** on `sentinel-ops` against OpenAPI API-COMP-001–007, migration 009, and approved COMP event schemas.

**M8 = NOT STARTED.**

## What M7 delivered

- KYC start/complete (human `approved`/`rejected`/`pending`)
- AML start (no complete API in contract)
- Travel Rule **recording** (not a regulatory engine)
- Sanctions screen + human disposition
- Audit package prepare (synchronous `ready`)
- `compliance_records` index (no list GET)
- COMP events via M3 `TransactionalOutbox` (**simulation**)
- Upstream index of `CaseClosed` / `CaseUpdated` / `RiskCalculated` (and `UserUpdated` if present on the ops log)

## Known limitations / deferred

- No Kafka/Redpanda; UserUpdated does not cross identity→ops in this architecture
- UX-OQ-COMP-LIST (no COMP list API)
- No AML complete endpoint
- Sanctions `hit:` prefix is a documented gap-fill, not a watchlist
- No AI compliance assist (V2)
- No REPORT

## Unresolved

Whether AML should ever publish `ComplianceReviewed` without a complete API (inventory vs schema). M7 does not publish on AML start.

## Recommended next milestone

**M8 AI** — assistive investigation/risk/retrieve APIs only. Must not approve COMP, close cases, or create alerts.

Do not implement M8 in this workstream.
