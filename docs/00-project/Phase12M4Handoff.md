# Phase 12 M4 Handoff

## Status

**M4 RISK is complete** on `sentinel-ops` against OpenAPI/APIInventory/migration 006 and approved RISK event schemas.

M5 is **not** authorized by this workstream and **was not started**.

## What was implemented

- API-RISK-001–007 (`ingestTransaction`, `listRiskAssessments`, `getRiskAssessment`, `listRiskRules`, `createRiskRule`, `patchRiskRule`, `triggerRiskEvaluation`)
- Deterministic weighted-sum scoring (ADR-003; no AI)
- `risk` schema tables + `risk.outbox_events`
- `RiskCalculated` / `HighRiskDetected` via M3 `TransactionalOutbox`
- Tenant isolation and M2 HMAC/AUTHZ permission checks
- Identity permission seed `V005_2` for inventory RISK codes

## What was intentionally not implemented

- ALERT / INVEST / COMP / DASH / AI business logic
- RISK-FR-013 assistive explanation agent
- Wallet scoring, `DeviceSignalReceived`, `RiskUpdated`
- Production Kafka/Redpanda
- Alert creation from RISK
- Extra HTTP endpoints

## Known limitations

Scoring bands and high-risk cutoff (score ≥ 50) are documented gap fills. Evaluation is synchronous behind 202. Durable log remains in-process simulation. See `Phase12M4Report.md` section M/N and `Phase12M4PreImplementationAudit.md`.

## Blockers

None that prevent M5 from being planned. M5 should consume RISK events; it must not require RISK to write `alert.*`.

## Is M5 authorized to begin?

**No — not by this agent.** A separate M5 authorization is required. This handoff only recommends M5 as the next milestone.

## Recommended next step

Implement **M5 ALERT** on `sentinel-ops` (same deployable, new schema 007), consuming RISK events, owning alert lifecycle and queue priority.
