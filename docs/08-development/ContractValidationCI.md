# Contract Validation CI Design

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Contract Validation CI Design |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 4 (design only) |
| Last Updated | 2026-09-03 |

---

## Purpose

Define future CI validation for API and event contracts. **No workflow files or scripts in Phase 4.**

---

## Validation Stages

| Stage | Trigger | Owner |
|-------|---------|-------|
| V1 — OpenAPI syntax | Every PR | Platform |
| V2 — Inventory reconciliation | Every PR touching `docs/06-api/` | Architecture |
| V3 — Event schema lint | PR touching EventContracts | Domain leads |
| V4 — Traceability matrix | PR touching FR or API | QA |
| V5 — Breaking change detection | PR to main | Architecture |
| V6 — NFR mapping spot-check | Release branch | QA |

---

## V1 — OpenAPI Syntax

**Inputs:** `docs/06-api/OpenAPI.yaml`

**Checks:**

- Valid OpenAPI 3.0.x parse
- All `$ref` resolve
- Unique `operationId`
- Unique path+method pairs

**Failure:** PR blocked

---

## V2 — Inventory Reconciliation

**Inputs:** `APIInventory.md`, `OpenAPI.yaml`

**Checks:**

- Every MVP row in inventory has ≥1 OpenAPI operation with matching `x-api-id`
- Every OpenAPI `x-release: MVP` operation has inventory row
- HTTP method + path match

**Failure:** PR blocked

---

## V3 — Event Schema

**Inputs:** `EventContracts.md`, FDS event matrices

**Checks:**

- Frozen event names unchanged vs FDS baseline file hash
- SEC publish/consume sets exact match
- No new MVP events without FDS change record

**Failure:** PR blocked if frozen contract drift

---

## V4 — Traceability

**Inputs:** `Phase4Traceability.md`, OpenAPI `x-fr` extensions

**Checks:**

- Every MVP operation has non-empty `x-fr`
- Referenced FR IDs exist in FRS index

**Failure:** PR blocked for missing MVP traceability

---

## V5 — Breaking Change Detection

**Inputs:** OpenAPI diff vs base branch

**Checks:**

- Removed operations
- Removed required fields
- Changed field types

**Failure:** Requires ADR link or major version bump

---

## V6 — Release Gate

Before Application Development Gate sign-off:

- MVP OpenAPI coverage 100%
- Event contracts documented
- Phase4ContractGapReport has no BLOCKING items

---

## Outputs

| Output | Format |
|--------|--------|
| Pass/fail | CI status check |
| Coverage report | MVP ops covered / total |
| Diff report | Breaking changes list |

---

## PR Policy

- API changes require architecture reviewer
- Frozen domain FR changes require explicit exemption (expected: fail)

---

## Related Documents

- [APIContractGovernance.md](../06-api/APIContractGovernance.md)
- [Phase4Traceability.md](Phase4Traceability.md)
