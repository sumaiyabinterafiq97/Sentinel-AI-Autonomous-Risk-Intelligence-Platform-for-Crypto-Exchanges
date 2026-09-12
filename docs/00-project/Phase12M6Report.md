# Phase 12 M6 Completion Report

## Document Information

| Field | Value |
|-------|-------|
| Project | Sentinel AI |
| Document | Phase 12 M6 Completion Report |
| Version | 1.0 |
| Status | **M6 INVEST COMPLETE** (durable log remains M3 in-process simulation) |
| Last Updated | 2026-09-11 |

---

## A. Objective

Implement the INVEST domain on `sentinel-ops`: cases, lifecycle, assignment, evidence references, notes, timeline, consume `AlertCreated` / `RiskCalculated`, publish approved INVEST events via transactional outbox. Do not start M7 COMP.

## B. Implementation summary

INVEST lives on `sentinel-ops` beside RISK and ALERT. Manual `POST /v1/investigations/cases` and alert-context initiation from `AlertCreated` (one case per source alert). RISK scoring and ALERT lifecycle remain owned by those domains. Evidence is a reference string (`evidenceRef`), not a file upload. `invest.case_links` exists with no HTTP API.

## C. APIs

API-INVEST-001–009: `listCases`, `createCase`, `getCase`, `patchCase`, `closeCase`, `assignCase`, `attachEvidence`, `getCaseTimeline`, `listCaseNotes`, `createCaseNote`.

No COMP/AI/V2/search/bulk/upload endpoints.

## D. Database

Ops `V008__invest_init_case_tables.sql`: `invest.investigation_cases`, `case_evidence`, `case_timeline_events`, `case_notes`, `case_links`. Identity `V005_4` INVEST permissions. Outbox remains `risk.outbox_events`.

## E. Events

**Consumed:** `AlertCreated`, `RiskCalculated` (not `HighRiskDetected`).

**Published:** `CaseCreated` (restricted), `CaseUpdated`, `CaseClosed`, `CaseAssigned`, `EvidenceAttached`.

At-least-once via M3 simulation. Duplicate `AlertCreated` does not duplicate cases.

## F. Security/tenancy

M2 HMAC + inventory permissions. Cross-tenant get → 404. Org header must match token.

## G. Tests

`InvestApiIT`, `InvestUpstreamConsumerIT`, `InvestOutboxIT`, `InvestLifecycleTest`; contract surface extended; Alert outbox count updated for CaseCreated follow-on; Alert API no longer asserts missing `invest` schema (link still does not insert a case for a random UUID).

## H. Validation

| Check | Result |
|-------|--------|
| `./gradlew test` | **PASS** |
| `python3 contracts/validate.py` | **PASS** (76 operationIds; SEC lock OK) |
| AI pytest / ruff (ai-service `.venv`) | **PASS** |
| web vitest | **PASS** |
| `git diff --check` | **PASS** |

## I. Limitations / gaps

- Simulation broker.
- Notes GET uses `invest:case:write` per inventory.
- Evidence column gap-fill (`attachment` / `evidenceRef` / `restricted`).
- `case_links` unused by API.
- RiskCalculated before AlertCreated cannot attach to a case; AlertCreated may read the RISK assessment row for initial context.
- No processed_event_ids table.

## J. Frozen requirements

FRS/FDS/OpenAPI not rewritten. SEC lock untouched. AI assistive-only. No M7 COMP schema.

## K. M0–M5 regression

Passed in the same Gradle run.

## L. Git status

Uncommitted local work. **No commit. No push.**

## M. M7 handoff

**M7 was NOT started.** Next authorized coding milestone is COMP (KYC/AML/etc.) after a separate authorization.
