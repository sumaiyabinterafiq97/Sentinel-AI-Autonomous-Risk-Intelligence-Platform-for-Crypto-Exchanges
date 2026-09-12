# Phase 12 M4 Completion Report

## Document Information

| Field | Value |
|-------|-------|
| Project | Sentinel AI |
| Document | Phase 12 M4 Completion Report |
| Version | 1.0 |
| Status | **M4 RISK COMPLETE** (deterministic scoring; durable log remains M3 simulation) |
| Last Updated | 2026-09-11 |

---

## A. Objective

Implement the RISK domain on `sentinel-ops`: rules, ingest, evaluate, assessments, deterministic scoring, approved events via M3 outbox. Do not start M5 ALERT.

## B. Repository audit findings

Pre-implementation audit: `docs/00-project/Phase12M4PreImplementationAudit.md`.

- APIs: API-RISK-001–007 only.
- Events: `RiskCalculated`, `HighRiskDetected` only.
- Schema: migration 006 `risk.*`.
- Deployable: `sentinel-ops` (ModuleImplementationPlan).
- Scoring formula and high-risk threshold are **not specified** in FRS — documented decisions used.

## C. Implemented RISK FRs

| FR | How |
|----|-----|
| RISK-FR-001 | Create/list/patch rules |
| RISK-FR-002 | Enabled rules evaluated on ingest/evaluate |
| RISK-FR-003 | Transaction ingest scoring |
| RISK-FR-004/005 | Evaluate `entityType=device\|user\|session` |
| RISK-FR-006 | `explanationSummary` on assessment + `RiskCalculated` |
| RISK-FR-007 | `HighRiskDetected.prioritySignal` = score (not ALERT priority) |
| RISK-FR-008 | List/get assessments, tenant-scoped |
| RISK-FR-009 | Ingest + duplicate `externalTransactionId` |
| RISK-FR-010 | Events after assessment persist, via outbox |
| RISK-FR-011 | `core.audit_records` |
| RISK-FR-012 | HMAC token + inventory permissions |
| RISK-FR-013 | **Not implemented** (AI M8) |

## D. Implemented API IDs and operationIds

| API ID | operationId | Result |
|--------|-------------|--------|
| API-RISK-001 | `ingestTransaction` | 202 accepted / duplicate |
| API-RISK-002 | `listRiskAssessments` | 200 |
| API-RISK-003 | `getRiskAssessment` | 200 / 404 |
| API-RISK-004 | `listRiskRules` | 200 |
| API-RISK-005 | `createRiskRule` | 201 |
| API-RISK-006 | `patchRiskRule` | 200 / 404 |
| API-RISK-007 | `triggerRiskEvaluation` | 202; `jobId` = assessment id |

No extra endpoints. Contract surface test asserts RISK + `/health` only.

## E. Database/migration changes

- Ops Flyway `ops_schema_history`, `ignore-missing-migrations`, baseline 1.
- `V006__risk_init_assessment_tables.sql`: `risk.risk_rules`, `risk.risk_assessments`, `risk.risk_rule_hits`, `risk.transaction_ingest_log`, plus `risk.outbox_events` (M3 per-deployable outbox).
- Identity `V005_2__authz_seed_risk_permissions.sql`: inventory permission codes. **V003_1 not rewritten.**
- Org-admin provisioning now includes `risk:*` for newly created orgs.
- Tests copy CORE V001 so `core.audit_records` exists.
- No `alert`/`invest` schemas.

## F. Events published

- `RiskCalculated` (confidential) after every committed assessment.
- `HighRiskDetected` when level is `high` or `critical` (score ≥ 50).
- Not published: `RiskUpdated`, `RuleCreated`, `AlertCreated`, GD-002, `TransactionReceived`.

## G. Outbox integration

`TransactionalOutbox.record` in the assessment transaction. Relay + `InMemoryDurableEventLog` (simulation). Event ID stable. At-least-once. Rollback test: assessment + outbox both revert.

## H. Security and tenant isolation

M2 HMAC tokens. `X-Organization-Id` must match token org. Permissions from APIInventory. `X-Sentinel-Permissions` is not authority. Cross-org get/patch returns 404/empty. Standard error envelope.

## I. Tests added

- `RiskScoringTest` — deterministic scores, levels
- `RiskApiIT` — authn/authz/validation, rules isolation, ingest idempotency, events, no alert schema, outbox published
- `RiskOutboxIT` — commit atomicity, rollback
- `RiskApiContractSurfaceIT` — no ALERT/INVEST paths
- `HealthControllerTest` — still public `/health`

## J. Validation results

| Check | Result |
|-------|--------|
| `./gradlew test` | **PASS** |
| `python3 contracts/validate.py` | **PASS** (76 operationIds, SEC lock) |
| AI pytest / ruff | **PASS** |
| web vitest | **PASS** |
| `git diff --check` | **PASS** |

## K. Frozen FRS/FDS validation

`FunctionalRequirements.md` and `FunctionalDomainSpecification.md` were **not modified**. OpenAPI was **not modified**.

## L. M0–M3 regression results

Platform, identity, dash, common-outbox tests passed as part of `./gradlew test`.

## M. Known limitations

- Scoring weights/thresholds are an **explicit gap fill**, not a frozen formula.
- 202 responses complete evaluation **synchronously** (meets NFR-PERF-001; no job table).
- Durable log is still the M3 **in-process simulation**.
- Device/behavioral analysis has no extra payload beyond `entityType`/`entityId`.
- RISK-FR-013 AI assist not implemented.
- Existing org-admin roles created before V005_2 are not backfilled (new orgs get `risk:*`).
- No P95 load test harness (NFR-PERF-001 simulation target is architectural, not a k6 suite).

## N. Documentation gaps

See pre-implementation audit: formula, rule JSON, 202 vs async, ingest Idempotency-Key vs OpenAPI, evaluate `jobId`, M2 permission placeholder, RuleCreated events without schemas.

## O. M5 was NOT started

No ALERT APIs, tables, events, or lifecycle. No INVEST/COMP/AI agents/WALLET/SEC/REPORT.

## P. Git status

**No commit. No push.** See terminal `git status --short` at handoff time. M4 lives mainly under `backend/services/ops`, identity `V005_2`, `common-outbox` allowlist/stream names, `backend/README.md`, and `docs/00-project/Phase12M4*.md`.

## Q. Recommended next milestone

**M5 — ALERT**, consuming `RiskCalculated` / `HighRiskDetected` without RISK owning alert priority or lifecycle.
