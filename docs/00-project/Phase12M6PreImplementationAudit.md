# Phase 12 M6 Pre-Implementation Audit

## Document Information

| Field | Value |
|-------|-------|
| Milestone | M6 — INVEST only |
| Date | 2026-09-11 |

---

## 1. INVEST MVP FR IDs

| FR | M6? | Notes |
|----|-----|-------|
| INVEST-FR-001 | **Yes** | Create/update/close; alert-context initiation |
| INVEST-FR-002 | **Yes** | Assign |
| INVEST-FR-003 | **Yes** | Evidence **metadata/reference** (no upload API) |
| INVEST-FR-004 | **Yes** | Notes GET/POST |
| INVEST-FR-005 | **Yes** | Timeline GET |
| INVEST-FR-006 | **Yes** | List/get |
| INVEST-FR-007 | **Yes** | CaseCreated/Updated/Closed/Assigned, EvidenceAttached |
| INVEST-FR-008 | **Yes** | Consume `AlertCreated`, `RiskCalculated` only |
| INVEST-FR-009 | **Yes** | CORE audit records |
| INVEST-FR-010 | **Yes** | AUTHZ + tenant |

No INVEST-FR-011 in FRS. AI assistance on cases is not an INVEST FR for M6.

## 2–3. APIs / operationIds (OpenAPI + inventory)

| API ID | Method | Path | operationId | Permission |
|--------|--------|------|-------------|------------|
| API-INVEST-001 | GET | `/v1/investigations/cases` | `listCases` | `invest:case:read` |
| API-INVEST-002 | POST | `/v1/investigations/cases` | `createCase` | `invest:case:write` |
| API-INVEST-003 | GET | `/v1/investigations/cases/{caseId}` | `getCase` | `invest:case:read` |
| API-INVEST-004 | PATCH | `/v1/investigations/cases/{caseId}` | `patchCase` | `invest:case:write` |
| API-INVEST-005 | POST | `.../close` | `closeCase` | `invest:case:close` |
| API-INVEST-006 | POST | `.../assign` | `assignCase` | `invest:case:assign` |
| API-INVEST-007 | POST | `.../evidence` | `attachEvidence` | `invest:evidence:write` |
| API-INVEST-008 | GET | `.../timeline` | `getCaseTimeline` | `invest:case:read` |
| API-INVEST-009 | GET | `.../notes` | `listCaseNotes` | inventory: `invest:case:write` |
| API-INVEST-009 | POST | `.../notes` | `createCaseNote` | `invest:case:write` |

**Not implemented:** COMP/AI/V2 paths. No case-link HTTP API (table `invest.case_links` is authorized; no inventory path).

**Inventory vs OpenAPI:** API IDs match OpenAPI `x-api-id`. Migration spec maps evidence to API-INVEST-005 and notes to 008 — **documentation error**; OpenAPI/inventory win (005=close, 007=evidence, 008=timeline, 009=notes).

**Notes GET permission:** inventory assigns `invest:case:write` to the notes resource (GET+POST). M6 follows inventory (not a separate read code).

## 4–5. Events

**Consume:** `AlertCreated`, `RiskCalculated` only. **Not** `HighRiskDetected`.

**Publish:** `CaseCreated` (restricted), `CaseUpdated`, `CaseClosed`, `CaseAssigned`, `EvidenceAttached` (SEC must not consume).

Schemas: `docs/06-api/schemas/events/mvp/invest/*.v1.schema.json`.

## 6–7. Database / migration 008

Ops Flyway after V007: `V008__invest_init_case_tables.sql`

- `invest.investigation_cases`
- `invest.case_evidence`
- `invest.case_timeline_events`
- `invest.case_notes`
- `invest.case_links` (no public API)

Outbox: reuse ops `risk.outbox_events`. No `invest.outbox_events`.

## 8. NFRs

NFR-AUD-001/002 (audit), NFR-PERF-004 (not a load suite in M6), NFR-SEC-006 classification on evidence.

## 9. ADRs

ADR-001 co-locate INVEST on ops; ADR-002 AI assistive-only; ADR-015 outbox; ADR-019 stack.

## 10. Integration points

- M3 `InMemoryDurableEventLog` listener (`REQUIRES_NEW` TX), same pattern as ALERT
- `TransactionalOutbox.record` for INVEST events
- Optional **read-only** `alert.alerts` / `risk.risk_assessments` for context mapping (no mutation)

## 11. Auth / tenant

M2 HMAC. Seed `V005_4` INVEST permission codes. Cross-tenant get → 404.

## 12. Lifecycle

Migration: `open → in_progress → pending_review → closed`.

OpenAPI `Case.status` is unconstrained string.

**Decision:** persist those four states. PATCH may set `open` / `in_progress` / `pending_review` (forward or no-op). Close endpoint → `closed` with `resolutionSummary` stored as `outcome`. Assign does not require a status change if already in_progress; if `open`, assign may remain `open` (assignment is orthogonal) or move to `in_progress`. **Smallest:** assign sets `assigned_to` only; status unchanged unless closed (409).

## 13. Gaps and smallest decisions

1. **Alert-driven create:** `AlertCreated` find-or-create one case per `(org, source_alert_id)`. Title from alert title. Duplicate eventId skipped via timeline/evidence ids or source_alert lookup.
2. **RiskCalculated:** do **not** create cases. If a case exists for an alert with that `assessmentId`, append a timeline entry. If the RISK event arrives *before* AlertCreated (typical M5 order), skip; AlertCreated may **read** the RISK assessment row for an initial timeline/context line so FR-008 is still satisfied without a pending-event table.
3. **Evidence HTTP** has `evidenceRef` (+ optional description), not the full evidence table columns. Gap-fill: `evidence_type=attachment`, `reference_type=evidenceRef`, `reference_id=evidenceRef`, `classification=restricted`.
4. **No evidence binary upload.**
5. **case_links** unused by HTTP.
6. **API-INVEST-009 GET** uses write permission per inventory.
7. **Idempotency-Key** has no column — state-based: duplicate `sourceAlertId` on create → 409; duplicate `evidenceRef` on a case → 409; repeat close/assign same as ALERT.

## 14. Out of scope (M7+)

COMP, AI agents, WALLET, SEC, REPORT, OPS, V2, Kafka/Redpanda, case-link APIs, evidence upload.

**M7 is NOT included.**

OpenAPI/FRS/FDS will not be rewritten.
