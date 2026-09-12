# Phase 10 Traceability Matrix

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Phase 10 Traceability |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 10 |
| Last Updated | 2026-09-11 |

---

## Purpose

Map Product Goal → Persona → Workflow → Screen → Component → API → Event → FR → NFR → Accessibility for MVP UX. Gaps are explicit.

**Legend:** COMPLETE · PARTIAL · UNRESOLVED · ASSUMPTION · DEFERRED

---

## Chain 1 — Alert Triage

| Layer | Reference | Status |
|-------|-----------|--------|
| Product Goal | Faster, explainable alert disposition | COMPLETE |
| Persona | Risk Analyst | COMPLETE |
| Workflow | WF-1 | COMPLETE |
| Screens | SCR-02, SCR-03, SCR-15 | COMPLETE |
| Components | DataTable, RiskScore, AIExplanationPanel, ConfirmationDialog | COMPLETE |
| APIs | API-ALERT-001/002/004/005/006/007, API-DASH-003, API-RISK-003, API-AI-002/004 | COMPLETE |
| Events | AlertAssigned, AlertClosed; SSE | COMPLETE |
| FR | ALERT-FR-*; DASH-FR-003; AI-FR-002 | COMPLETE |
| NFR | NFR-PERF-003, NFR-EXPL-001 | COMPLETE |
| A11y | Table + confirm dialog | COMPLETE (design target) |

---

## Chain 2 — Risk Explanation

| Layer | Reference | Status |
|-------|-----------|--------|
| Persona | Risk Analyst | COMPLETE |
| Workflow | WF-2 | COMPLETE |
| Screens | SCR-06, SCR-15 | COMPLETE |
| APIs | API-RISK-003, API-AI-002/004 | COMPLETE |
| Boundary | No create-alert from RISK | COMPLETE |
| NFR | NFR-EXPL-001 | COMPLETE |

---

## Chain 3 — Investigation / Case

| Layer | Reference | Status |
|-------|-----------|--------|
| Persona | Risk Analyst | COMPLETE |
| Workflow | WF-3, WF-7 | COMPLETE |
| Screens | SCR-04, SCR-05, SCR-15 | COMPLETE |
| APIs | API-INVEST-001–009; API-AI-001/003/004 | COMPLETE |
| Events | Case*, EvidenceAttached | COMPLETE |
| FR | INVEST-*; AI-FR-001/003 | COMPLETE |
| NFR | NFR-AUD-001, NFR-PERF-004 | COMPLETE |
| A11y | Timeline + close confirm | COMPLETE (design target) |

---

## Chain 4 — Compliance

| Layer | Reference | Status |
|-------|-----------|--------|
| Persona | Compliance Officer | COMPLETE |
| Workflow | WF-4, WF-5 | COMPLETE |
| Screens | SCR-08, SCR-09, SCR-10 | COMPLETE |
| APIs | API-COMP-001–007; context API-INVEST-003 / API-RISK-003 | COMPLETE |
| Events | ComplianceReviewed, Sanctions*, TravelRule*, AuditPackagePrepared | COMPLETE |
| AI approve | Prohibited MVP | COMPLETE |
| COMP list GET | Not in inventory | **CLOSED** — not MVP (GD-007) |
| Package status GET | Shape via OpenAPI | PARTIAL |
| A11y | Confirm decisions | COMPLETE (design target) |

---

## Chain 5 — Dashboard / SSE

| Layer | Reference | Status |
|-------|-----------|--------|
| Workflow | WF-8 | COMPLETE |
| Screens | SCR-01 | COMPLETE |
| APIs | API-DASH-001/002/004/005/007 | COMPLETE |
| Transport | SSE MVP; WS V2 | COMPLETE |
| A11y | No focus steal on SSE | COMPLETE |

---

## Chain 6 — Administration / Identity

| Layer | Reference | Status |
|-------|-----------|--------|
| Persona | Platform Administrator | COMPLETE |
| Workflow | WF-6 | COMPLETE |
| Screens | SCR-12, SCR-13, SCR-14 | COMPLETE |
| APIs | API-ADMIN-*, USER-*, ORG-*, AUTHZ-*, CORE-*, API-AI-005 | COMPLETE |
| Boundary | ADMIN ≠ USER/ORG owner | COMPLETE |

---

## Chain 7 — Auth Entry

| Layer | Reference | Status |
|-------|-----------|--------|
| Screen | SCR-00 | COMPLETE |
| APIs | API-AUTH-001/005/004/002 | COMPLETE |
| NFR | NFR-SEC-003 | COMPLETE |

---

## Chain 8 — Search

| Layer | Reference | Status |
|-------|-----------|--------|
| Screen | SCR-11 | COMPLETE (composed) |
| APIs | Composed list GETs | **CLOSED** — sufficient for MVP (GD-007); no global search API |
| Gap | UX-OQ-SEARCH / CL-OQ-002 | **CLOSED** (GD-007) |

---

## Incomplete / Deferred

| Item | Classification |
|------|----------------|
| Human Phase 10 approval | PENDING HUMAN APPROVAL |
| COMP dedicated list API | **CLOSED** — not MVP (GD-007) |
| Global search API | **CLOSED** — composed lists sufficient for MVP (GD-007) |
| V2 SEC/REPORT/OPS/WALLET UX | DEFERRED |
| Formal a11y certification | DEFERRED (design target only) |
| Claim→nav binding table | ASSUMPTION until Phase 11 AUTHZ detail |

---

## Related Documents

- [Phase9Traceability.md](Phase9Traceability.md)
- [DashboardScreens.md](../07-ui/DashboardScreens.md)
- [MVPWorkflows.md](../07-ui/MVPWorkflows.md)
