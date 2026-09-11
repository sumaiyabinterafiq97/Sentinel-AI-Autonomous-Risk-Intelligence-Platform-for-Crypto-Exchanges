# Phase 5 Traceability Matrix

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Phase 5 Traceability |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 5 |
| Last Updated | 2026-09-03 |

---

## Purpose

End-to-end traceability from **product goals** through personas, business objectives, functional requirements, contracts, data, NFRs, and validation methods. Extends [Phase4Traceability.md](Phase4Traceability.md).

**Focus:** MVP release boundary.

---

## Traceability Chain Model

```text
Product Goal (PRD §9)
    ↓
Persona (Personas.md)
    ↓
Business Objective (BRS / PRD §10)
    ↓
Functional Requirement (FRS)
    ↓
API / Event Contract
    ↓
Data Model (PostgreSQL schema owner)
    ↓
NFR
    ↓
Validation Method
```

---

## MVP Product Goal Chains

### PG-01 — Unified alert triage with explainable risk context

| Layer | Reference | Status |
|-------|-----------|--------|
| Persona | Risk Analyst | ✅ |
| Business objective | BO-001 (BRS) | ✅ |
| FRs | RISK-FR-001,003,008; ALERT-FR-003–006; DASH-FR-003 | ✅ |
| APIs | API-RISK-003–007; API-ALERT-001–006; API-DASH-003 | ✅ |
| Events | RiskCalculated, HighRiskDetected → AlertCreated | ✅ |
| Data | `risk.*`, `alert.*` | ✅ |
| NFR | NFR-PERF-001, NFR-PERF-003, NFR-EXPL-001 | ✅ |
| Validation | Contract + integration + performance simulation | ⚠️ CI design only |

### PG-02 — Investigation workflow with evidence

| Layer | Reference | Status |
|-------|-----------|--------|
| Persona | Risk Analyst | ✅ |
| FRs | INVEST-FR-001–007; ALERT-FR-009 | ✅ |
| APIs | API-INVEST-001–009; API-ALERT-006 | ✅ |
| Events | AlertCreated → CaseCreated; EvidenceAttached | ✅ |
| Data | `invest.*`, `alert.alert_investigation_links` | ✅ |
| NFR | NFR-PERF-004, NFR-AUD-001 | ✅ |
| Validation | Case lifecycle integration tests (planned) | ⚠️ Partial |

### PG-03 — Compliance workflows and audit evidence

| Layer | Reference | Status |
|-------|-----------|--------|
| Persona | Compliance Officer | ✅ |
| FRs | COMP-FR-001–006 | ✅ |
| APIs | API-COMP-001–007 | ✅ |
| Events | ComplianceReviewed; consumes CaseClosed | ✅ |
| Data | `comp.*` | ✅ |
| NFR | NFR-AUD-002, NFR-PRIV-001 | ✅ |
| Validation | Audit package workflow test (planned) | ⚠️ Partial |

### PG-04 — Secure multi-tenant platform access

| Layer | Reference | Status |
|-------|-----------|--------|
| Persona | Platform Administrator | ✅ |
| FRs | AUTH-FR-001–006; AUTHZ-FR-001–004; USER-FR-001–004; ORG-FR-001–003 | ✅ |
| APIs | API-AUTH-*; API-AUTHZ-*; API-USER-*; API-ORG-* | ✅ |
| Data | `auth.*`, `authz.*`, `user.*`, `org.*` | ✅ Migrations 002–005 |
| NFR | NFR-SEC-001–005, NFR-PRIV-003 | ✅ |
| Validation | Tenant isolation + auth integration tests | ⚠️ Planned |

### PG-05 — Assistive AI without lifecycle takeover

| Layer | Reference | Status |
|-------|-----------|--------|
| Persona | Risk Analyst, Compliance Officer | ✅ |
| FRs | AI-FR-001–004,009 (FRS chapter — pending full delivery) | ⚠️ Partial |
| APIs | API-AI-001–005 | ✅ |
| Events | AIRecommendationGenerated | ✅ Schema |
| Data | `ai.*`, pgvector | ✅ |
| NFR | NFR-PERF-006, NFR-SEC-009, NFR-SEC-010 | ✅ |
| Validation | AI eval harness (Phase 8+) | ⚠️ Deferred |

### PG-06 — Real-time workspace visibility

| Layer | Reference | Status |
|-------|-----------|--------|
| Persona | Risk Analyst | ✅ |
| FRs | DASH-FR-001–003, DASH-FR-011 | ✅ |
| APIs | API-DASH-001–003, **API-DASH-007 (SSE)** | ✅ Phase 5 |
| Events | Consumes Alert*, Case*, RiskCalculated | ✅ |
| Data | `dash.*` (prefs only); read models derived | ✅ |
| NFR | NFR-PERF-003, NFR-RES-003 | ✅ |
| Validation | SSE contract + fallback poll test | ⚠️ Planned |

---

## Complete vs Partial vs Missing

| Category | Count | Notes |
|----------|-------|-------|
| Complete MVP chains | 4 | PG-01, 02, 03, 06 (with validation planned) |
| Partial chains | 2 | PG-04 (migrations spec'd, not executable); PG-05 (AI FRS pending) |
| Intentional no-API FRs | 5+ | Event-only paths — see Phase4Traceability |
| V2 deferred chains | 4 domains | WALLET, SEC, REPORT, OPS |

---

## Gaps (Explicit)

| Gap ID | Description | Classification |
|--------|-------------|----------------|
| TR5-001 | AI/ADMIN/REPORT/OPS FRS chapters not fully delivered in FRS v1.9 baseline | NON-BLOCKING for MVP planning |
| TR5-002 | Executable validation CI not implemented | NON-BLOCKING |
| TR5-003 | NFR-OQ-002 jurisdiction retention | NON-BLOCKING |
| TR5-004 | BQ-4 human sign-off pending | NON-BLOCKING (governance recorded) |
| TR5-005 | RISK–ALERT event-only path (ALERT-FR-011) | INFORMATIONAL — by design |
| TR5-006 | Remaining MVP domain migrations (RISK, ALERT, INVEST, COMP, AI, ADMIN, DASH) | NON-BLOCKING Phase 6 |

---

## Ambiguous Ownership (Resolved — No Action)

| Question | Resolution |
|----------|------------|
| Who sets alert priority? | ALERT — RISK provides prioritySignal context only |
| Who creates cases? | INVEST — via API or analyst action |
| Who approves compliance? | COMP human decision — AI assistive only |
| Who owns device registration? | AUTH — SEC does not replace AUTH |
| Does ADMIN duplicate USER CRUD? | No — orchestration only (ADMIN-FR-004) |

---

## Related Documents

- [PRD.md](../01-product/PRD.md)
- [Phase4Traceability.md](Phase4Traceability.md)
- [Phase5Report.md](../00-project/Phase5Report.md)
- [event-catalog.v0.1.json](../06-api/schemas/event-catalog.v0.1.json)
