# Phase 12 M4 Pre-Implementation Audit

## Document Information

| Field | Value |
|-------|-------|
| Milestone | M4 — RISK only |
| Status | Audit complete — implementation authorized from existing contracts |
| Date | 2026-09-11 |

---

## RISK MVP FRs

From FRS (RISK-FR-001 – RISK-FR-013). All are labeled MVP. M4 implements RISK-owned behavior that has **authorized APIs and schema**.

| FR | Title | M4? | Notes |
|----|-------|-----|-------|
| RISK-FR-001 | Manage Risk Rules And Configuration | **Yes** | API-RISK-004/005/006 |
| RISK-FR-002 | Evaluate Risk Rules | **Yes** | API-RISK-007 + ingest path |
| RISK-FR-003 | Calculate Transaction Risk Score | **Yes** | Ingest + evaluate `entityType=transaction` |
| RISK-FR-004 | Perform Device Risk Analysis | **Yes (evaluate path only)** | No dedicated device API. `POST /v1/risk/evaluate` with `entityType=device` |
| RISK-FR-005 | Perform Behavioral Risk Analysis | **Yes (evaluate path only)** | `entityType=user` or `session` |
| RISK-FR-006 | Generate Risk Explanation | **Yes** | Embedded in assessment + `RiskCalculated.explanationSummary` — not a separate event (`RiskExplanationGenerated` deferred) |
| RISK-FR-007 | Produce Risk-Derived Priority Signals | **Yes** | `HighRiskDetected.prioritySignal` only. Does **not** set ALERT queue priority |
| RISK-FR-008 | Retrieve And Discover Risk Assessments | **Yes** | API-RISK-002/003 |
| RISK-FR-009 | Ingest External Transaction Inputs | **Yes** | API-RISK-001. `TransactionReceived` is external input, not a published domain event |
| RISK-FR-010 | Enforce Risk Event Publication Contract | **Yes** | `RiskCalculated`; `HighRiskDetected` when threshold met. Via M3 outbox |
| RISK-FR-011 | Record Risk Management Audit Outcomes | **Yes** | `core.audit_records` (CORE-owned audit), same pattern as identity |
| RISK-FR-012 | Restrict Risk Data Access | **Yes** | M2 HMAC tokens + inventory permission strings |
| RISK-FR-013 | Provide Risk Analysis Assistance | **No** | AI assist APIs are M8 (`assistRiskExplanation`). ADR-003: scoring must not depend on AI |

Deferred / not M4: Wallet scoring, `RiskUpdated`, `DeviceSignalReceived` consume, ALERT/INVEST writes, ML scoring.

---

## RISK API IDs

| API ID | Method | Path | operationId | Permission | HTTP success |
|--------|--------|------|-------------|------------|--------------|
| API-RISK-001 | POST | `/v1/risk/transactions/ingest` | `ingestTransaction` | `risk:ingest:write` | 202 |
| API-RISK-002 | GET | `/v1/risk/assessments` | `listRiskAssessments` | `risk:assessment:read` | 200 |
| API-RISK-003 | GET | `/v1/risk/assessments/{assessmentId}` | `getRiskAssessment` | `risk:assessment:read` | 200 |
| API-RISK-004 | GET | `/v1/risk/rules` | `listRiskRules` | `risk:rule:read` | 200 |
| API-RISK-005 | POST | `/v1/risk/rules` | `createRiskRule` | `risk:rule:write` | 201 |
| API-RISK-006 | PATCH | `/v1/risk/rules/{ruleId}` | `patchRiskRule` | `risk:rule:write` | 200 |
| API-RISK-007 | POST | `/v1/risk/evaluate` | `triggerRiskEvaluation` | `risk:evaluate:write` | 202 |

No other RISK HTTP operations exist in OpenAPI/APIInventory. None will be invented.

---

## RISK event IDs

Approved schemas only:

| Event | Schema | When |
|-------|--------|------|
| `RiskCalculated` | `events/mvp/risk/RiskCalculated.v1.schema.json` | After assessment commit |
| `HighRiskDetected` | `events/mvp/risk/HighRiskDetected.v1.schema.json` | When high-risk threshold met |

**Not published:** `RiskUpdated`, `RiskExplanationGenerated`, `TransactionReceived` (external ingest, not a Sentinel publish), `RuleCreated`/`RuleUpdated` (mentioned in migration prose, **absent from event catalog** — will not invent schemas), GD-002 platform events.

Envelope: existing EventArchitecture / `envelope.schema.json`. Classification: `confidential` for `RiskCalculated` (EventContracts).

---

## RISK tables/entities

Migration **006** `006_risk_init_assessment_tables` (InitialMigrationSpecifications):

- schema `risk`
- `risk.risk_rules`
- `risk.risk_assessments`
- `risk.risk_rule_hits`
- `risk.transaction_ingest_log`

No `alert.*` / `invest.*` / `comp.*` / `ai.*`.

Deployable: **`sentinel-ops`** (ModuleImplementationPlan). Ops Flyway table `ops_schema_history` so it does not collide with platform/identity histories.

M3 outbox for this deployable: `risk.outbox_events` (per-service outbox; not in the RISK logical spec — same M3 documentation gap as CORE/AUTH). `QualifiedOutboxTable` must allow this name.

---

## Applicable NFRs

| NFR | Relevance |
|-----|-----------|
| NFR-PERF-001 | Ingest → `RiskCalculated` P95 ≤ 2s (simulation). Critical path **without AI** (ADR-003) |
| NFR-PERF-002 | Assessment read P95 target (not a load test in M4; functional tests only) |
| NFR-EXPL-001 | Explanation coverage via embedded summary |
| NFR-AUD-001 | Audit outcomes on rule changes and assessments |
| NFR-SEC / tenant | `organization_id` on tenant-scoped tables; token org must match `X-Organization-Id` |

---

## Relevant ADRs

- ADR-003 — Deterministic risk path independent of AI
- ADR-005 / ADR-010 — Domain data ownership, schema-per-domain
- ADR-009 — Contract-first REST
- ADR-014 — Cursor pagination
- ADR-015 / M3 — Transactional outbox + simulation durable log
- ADR-019 — Java 21 / Spring Boot 3

---

## Existing M3 integration points

- `TransactionalOutbox.record(envelope)` inside the assessment transaction
- `OutboxRelay` after commit
- `InMemoryDurableEventLog` simulation (no Kafka)
- Event ID minted once at outbox insert
- At-least-once delivery

RISK application code must not call the durable log directly.

---

## Existing M2 security integration

- HMAC access tokens (`AccessTokenCodec`) shared via `IDENTITY_TOKEN_HMAC_KEY`
- Permission strings from APIInventory (not `X-Sentinel-Permissions`)
- M2 seed currently has placeholder `risk:risk:read` only — **not** the inventory codes

---

## Documentation gaps and smallest supported decisions

1. **No numeric scoring formula or high-risk threshold in FRS/FDS.**  
   Decision: deterministic weighted-sum of matching enabled rules, capped at 100. Levels: `low` 0–24, `medium` 25–49, `high` 50–74, `critical` 75–100. `HighRiskDetected` when level is `high` or `critical` (score ≥ 50). `prioritySignal` = score (schema’s only numeric signal). Labeled as implementation of unspecified formula, not a frozen-requirement change.

2. **Rule `definition` JSON schema is unspecified.**  
   Decision: object requiring numeric `weight` (0–100); optional `factor` (`amount` \| `asset` \| `userIdPresent` \| `entityType`), `operator` (`gte` \| `lte` \| `eq` \| `neq`), `value`, `appliesTo` (`transaction` \| `user` \| `device` \| `session`). Unknown extra keys rejected (`additional` not stored as executable conditions).

3. **OpenAPI 202 “async” vs NFR-PERF-001 (evaluate within 2s).**  
   Decision: evaluate **inside the request transaction** (deterministic, no AI), return **202** with `assessmentId` / `jobId`. Meets latency target and outbox atomicity. Not a background worker (would require inventing job tables).

4. **API-RISK-001 Idempotency-Key** is in inventory/migration but **not** an OpenAPI parameter on ingest.  
   Decision: uniqueness is `(organization_id, external_tx_id)` as specified. Optional `Idempotency-Key` header stored if present; not required. Duplicate external id returns `status=duplicate` without a second assessment/event.

5. **Evaluate 202 body is `jobId` + `accepted|processing`**, not `assessmentId`.  
   Decision: `jobId` = assessment id after synchronous evaluation; `status=accepted`. Do not invent a job API.

6. **RISK permission codes** in inventory are not in M2 seed (only `risk:risk:read`).  
   Decision: new identity migration `V005_2` seeds inventory codes. Do **not** rewrite `V003_1`. Org-admin provisioning includes `risk:*` for newly created orgs. Tests may mint tokens with explicit permissions (same as CORE).

7. **Device/behavioral FRs have no dedicated APIs.**  
   Decision: same evaluate endpoint + rule `appliesTo`. No invented device ingest API. No `DeviceSignalReceived`.

8. **Migration prose “RuleCreated / RuleUpdated” events** have no catalog schemas.  
   Decision: **do not publish**. Audit records only (RISK-FR-011).

9. **RISK-FR-013** maps to AI APIs — **not implemented in M4**.

These decisions do not modify frozen FRS/FDS/OpenAPI.
