# Phase 12 M7 Pre-Implementation Audit

## Document Information

| Field | Value |
|-------|-------|
| Milestone | M7 — COMP only |
| Date | 2026-09-11 |

---

## 1. COMP MVP FR IDs

| FR | M7? | Notes |
|----|-----|-------|
| COMP-FR-001 | **Yes** | KYC start + complete |
| COMP-FR-002 | **Yes** | AML start only (no complete API in OpenAPI) |
| COMP-FR-003 | **Yes** | Travel Rule record (no regulatory engine) |
| COMP-FR-004 | **Yes** | Sanctions screen + human disposition |
| COMP-FR-005 | **Yes** | Audit package prepare |
| COMP-FR-006 | **Partial** | FR is MVP retrieve/discover; **no list GET in OpenAPI** (UX-OQ-COMP-LIST). Persist `compliance_records` index only |
| COMP-FR-007 | **Yes** | Approved COMP events |
| COMP-FR-008 | **Yes** | Consume CaseClosed, CaseUpdated, RiskCalculated; UserUpdated **ops-log only** (identity publishes on another deployable — no shared broker) |
| COMP-FR-009 | **Yes** | CORE audit |
| COMP-FR-010 | **Yes** | AUTHZ + tenant |

AI compliance assist is **API-AI-006 V2** — not M7.

## 2–3. APIs / operationIds

| API ID | Method | Path | operationId | Permission |
|--------|--------|------|-------------|------------|
| API-COMP-001 | POST | `/v1/compliance/kyc-reviews` | `startKycReview` | `comp:kyc:write` |
| API-COMP-002 | PATCH | `/v1/compliance/kyc-reviews/{reviewId}` | `completeKycReview` | `comp:kyc:approve` |
| API-COMP-003 | POST | `/v1/compliance/aml-reviews` | `startAmlReview` | `comp:aml:write` |
| API-COMP-004 | POST | `/v1/compliance/travel-rule/validations` | `validateTravelRule` | `comp:travelrule:write` |
| API-COMP-005 | POST | `/v1/compliance/sanctions-screenings` | `createSanctionsScreening` | `comp:sanctions:write` |
| API-COMP-006 | PATCH | `/v1/compliance/sanctions-screenings/{screeningId}` | `dispositionSanctionsMatch` | `comp:sanctions:approve` |
| API-COMP-007 | POST | `/v1/compliance/audit-packages` | `prepareAuditPackage` | `comp:audit:write` |

No GET list/search. No REPORT/AI/DASH.

**Inventory vs events:** API-COMP-001 lists `ComplianceReviewed` on start; FRS publishes on **eligible outcomes**. M7 publishes `ComplianceReviewed` on KYC complete (`approved`/`rejected`) and sanctions disposition — **not** on KYC/AML start.

**AML:** no complete/PATCH in OpenAPI — no invented endpoint; no `ComplianceReviewed` without an outcome field.

## 4. Database / migration 009

Ops Flyway `V009__comp_init_compliance_tables.sql` after V008:

- `comp.kyc_reviews`
- `comp.aml_reviews`
- `comp.travel_rule_validations`
- `comp.sanctions_screenings`
- `comp.audit_packages`
- `comp.compliance_records`

Outbox: reuse ops `risk.outbox_events`. No `comp.outbox_events`.

## 5. Events

**Consume (MVP):** `CaseClosed`, `CaseUpdated`, `RiskCalculated`, `UserUpdated`.

**Publish:** `ComplianceReviewed` (restricted), `TravelRuleValidated`, `SanctionsHitDetected`, `AuditPackagePrepared`.

Schemas under `docs/06-api/schemas/events/mvp/comp/`.

**UserUpdated gap:** identity outbox ≠ ops durable-log simulation. Handler will process `UserUpdated` if delivered on the ops log; cross-process delivery is **not** implemented (no Kafka). Tests may append to the ops log.

## 6. Permissions

Identity `V005_5`: `comp:kyc:write`, `comp:kyc:approve`, `comp:aml:write`, `comp:travelrule:write`, `comp:sanctions:write`, `comp:sanctions:approve`, `comp:audit:write`. Org-admin `comp:*`.

## 7. NFRs / ADRs

NFR-AUD-001/002, tenant security. ADR-001 co-locate COMP on ops; ADR-002 AI assistive-only; ADR-015 outbox; ADR-019 stack.

## 8. Lifecycle / gap-fills (not frozen product rules)

- KYC: start `in_review`; complete `approved`/`rejected`/`pending`→`info_requested`. Event only for approved/rejected. `reviewed_by` = authenticated user.
- `subjectRef` API vs `user_id` UUID column: if `subjectRef` is a UUID, store it; else `UUID.nameUUIDFromBytes` (lossy). Tests use UUIDs.
- Travel Rule: **no validation engine**. Persist `status=recorded`, `valid=true` in API JSON as “recorded”, `validation_result={"mode":"recorded"}`. Not a legal determination.
- Sanctions: **no watchlist**. Default `match_status=no_match` (no hit event). If `subjectRef` starts with `hit:` → `possible_match` + `SanctionsHitDetected` (**test/gap-fill, not a sanctions product**). Disposition required for matches; `ComplianceReviewed` reviewType=sanctions.
- Audit package: OpenAPI `scope` → `package_type`. Lifecycle `requested→preparing→ready` collapsed to **synchronous `ready`** in the request TX (no worker). `AuditPackagePrepared` after commit.
- Event consumption: insert `compliance_records` only (context index). **Do not** auto-create KYC/AML or auto-approve.

## 9. Out of scope

AI agents, REPORT, OPS, WALLET, SEC, V2/V3, Kafka, list GET, compliance scoring, jurisdiction engines.

**M8 is NOT included.**

OpenAPI/FRS/FDS will not be rewritten.
