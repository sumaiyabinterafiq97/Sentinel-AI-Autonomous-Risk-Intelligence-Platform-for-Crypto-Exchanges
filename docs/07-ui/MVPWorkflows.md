# MVP Workflows

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | MVP Workflows |
| Version | 0.2 (Draft) |
| Status | Draft — Phase 10 |
| Last Updated | 2026-09-11 |
| Authority | Personas, FRS, APIInventory (Phase 9), DashboardScreens |

---

## Purpose

Fully specify eight MVP workflows for implementation planning. **API IDs must match APIInventory.md** (corrected in Phase 10 where earlier drafts drifted).

**AI rule:** AI may summarize/retrieve/recommend; AI must **not** approve compliance, close cases/alerts, block transactions, change ownership/priority/risk state, or modify authorization.

---

## Workflow Index

| ID | Name | Primary actor |
|----|------|---------------|
| WF-1 | Alert Triage | Risk Analyst |
| WF-2 | Risk Explanation Review | Risk Analyst |
| WF-3 | Investigation Handoff & Case Management | Risk Analyst |
| WF-4 | Compliance Review | Compliance Officer |
| WF-5 | Audit Preparation | Compliance Officer |
| WF-6 | Platform Administration | Platform Administrator |
| WF-7 | AI-Assisted Investigation | Risk Analyst |
| WF-8 | Real-Time Queue Refresh | System + any queue user |

---

## Workflow 1 — Alert Triage

| Field | Specification |
|-------|---------------|
| Actor | Risk Analyst |
| Preconditions | Authenticated; `alert:alert:read`; tenant context set |
| Trigger | Open Alerts nav or Overview widget |
| Screens | SCR-02 → SCR-03 (± SCR-06, SCR-15) |
| Human decision points | Assign; priority change (if permitted); close; escalate to case |
| Completion | Alert assigned/closed or case created and linked |
| Audit | ALERT mutations; INVEST CaseCreated on escalate |
| FR | ALERT-FR-003–007, DASH-FR-003, AI-FR-002 (optional) |
| NFR | NFR-PERF-003, NFR-EXPL-001 |
| AI | Optional risk explanation — not disposition |

### Steps

| # | UX step | Screen | API | Event | Failure / permission |
|---|---------|--------|-----|-------|----------------------|
| 1 | Open alert queue | SCR-02 | API-DASH-003 and/or API-ALERT-001 | — | 403 → PermissionDenied; retry on 5xx |
| 2 | Filter/sort; review priority | SCR-02 | API-ALERT-001 | SSE Alert* | EmptyState if none |
| 3 | Open alert detail | SCR-03 | API-ALERT-002 | — | 404 secure |
| 4 | View risk context | SCR-03 | API-RISK-003 (assessment) | — | Missing context message |
| 5 | Optional AI explain | SCR-15 | API-AI-002 → API-AI-004 | AIRecommendationGenerated | Timeout/degraded; continue |
| 6 | Assign to self/other | SCR-03 | **API-ALERT-004** | AlertAssigned | Idempotent retry; 403 disable |
| 7 | Optional priority adjust | SCR-03 | **API-ALERT-006** | — | Confirm if policy requires |
| 8 | Close alert | SCR-03 | **API-ALERT-005** | AlertClosed | ConfirmationDialog |
| 9 | Or escalate: create case | SCR-03→05 | **API-INVEST-002**; link **API-ALERT-007** | CaseCreated | Validation failure retains form |

**Validation:** Server schemas; UI required disposition reason if FR requires.
**Retry:** Safe on idempotent assign/close; show conflict on 409.

---

## Workflow 2 — Risk Explanation Review

| Field | Specification |
|-------|---------------|
| Actor | Risk Analyst |
| Preconditions | `risk:assessment:read` |
| Trigger | From alert risk link or Risk nav |
| Screens | SCR-06 (± SCR-15) |
| Boundary | **No create-alert** from RISK screen |
| FR | RISK-FR assessment/explanation; AI-FR-002 optional |
| NFR | NFR-EXPL-001 |
| Completion | Analyst understands score/rule hits; may navigate to alert/case |

### Steps

| # | UX step | API | Notes |
|---|---------|-----|-------|
| 1 | Open assessment | API-RISK-003 | SYSTEM RESULT score/level |
| 2 | Expand rule hits | Embedded in assessment / related reads | Evidence before AI |
| 3 | Optional AI deepen | API-AI-002 → API-AI-004 | AI INTERPRETATION labeled |
| 4 | Navigate to linked alert/case | API-ALERT-002 / API-INVEST-003 | Human path |

---

## Workflow 3 — Investigation Handoff and Case Management

| Field | Specification |
|-------|---------------|
| Actor | Risk Analyst |
| Preconditions | `invest:case:write` (create); read for list |
| Trigger | Escalate from alert or Investigations nav |
| Screens | SCR-04, SCR-05, SCR-15 |
| FR | INVEST-FR case/evidence/notes; AI-FR-001/003 optional |
| NFR | NFR-AUD-001, NFR-PERF-004 |
| Consumers | COMP may consume CaseClosed; SEC **V2** may consume CaseCreated — no SEC MVP UI |
| Completion | Case closed with outcome **by human** |

### Steps

| # | UX step | API | Event |
|---|---------|-----|-------|
| 1 | Create case | **API-INVEST-002** | CaseCreated |
| 2 | Open case | API-INVEST-003 | — |
| 3 | Attach evidence | **API-INVEST-007** | EvidenceAttached |
| 4 | Optional AI retrieve | API-AI-003 | — |
| 5 | Add note | **API-INVEST-009** POST | — |
| 6 | View timeline | **API-INVEST-008** | — |
| 7 | Assign | **API-INVEST-006** | CaseAssigned |
| 8 | Update fields | API-INVEST-004 | CaseUpdated |
| 9 | Close case | **API-INVEST-005** | CaseClosed |

**AI:** May recommend steps; close/assign only via human controls.
**Failure:** Confirmation on close; 409 conflict messaging; evidence classification enforced by server.

---

## Workflow 4 — Compliance Review

| Field | Specification |
|-------|---------------|
| Actor | Compliance Officer |
| Preconditions | Matching `comp:*` permissions |
| Trigger | Compliance nav |
| Screens | SCR-08, SCR-09 |
| Human decision | Approve/reject/info-request/sanctions disposition — **never AI** |
| AI | Compliance assist API-AI-006 is **V2** — not MVP |
| FR | COMP-FR-001–004, COMP-FR-006 |
| NFR | NFR-AUD-001, NFR-PRIV-001 |
| Completion | Decision recorded; optional case link via authorized reads |

### Steps

| # | UX step | API | Event |
|---|---------|-----|-------|
| 1 | Open queue / start KYC | API-COMP-001 | — |
| 2 | Complete KYC decision | API-COMP-002 | ComplianceReviewed |
| 3 | AML review | API-COMP-003 | ComplianceReviewed |
| 4 | Travel rule validation | API-COMP-004 | TravelRuleValidated |
| 5 | Sanctions screen | API-COMP-005 | SanctionsHitDetected |
| 6 | Disposition match | API-COMP-006 | ComplianceReviewed |
| 7 | View linked case/risk (read) | API-INVEST-003 / API-RISK-003 as permitted | — |

**Note:** Linked-context reads use **existing** INVEST/RISK APIs — do not invent COMP “context” API.

---

## Workflow 5 — Audit Preparation

| Field | Specification |
|-------|---------------|
| Actor | Compliance Officer |
| Preconditions | `comp:audit:write` |
| Trigger | SCR-10 |
| API | **API-COMP-007** prepare package |
| Event | AuditPackagePrepared |
| FR | COMP-FR-005, COMP-FR-006 |
| NFR | NFR-AUD-002 |
| Note | REPORT V2 not required (BQ-4 / GD-001) |
| Completion | Package ready; export/download per API response capabilities |
| Failure | Async job error → ErrorState + retry; poll status if exposed by same resource family |

**Gap (non-blocking):** If package status GET is not a separate inventory ID, UX monitors via response/job pattern documented in OpenAPI for API-COMP-007 — do not invent IDs.

---

## Workflow 6 — Platform Administration

| Field | Specification |
|-------|---------------|
| Actor | Platform Administrator |
| Screens | SCR-12, SCR-13, SCR-14 |
| Boundary | ADMIN orchestrates provision; USER/ORG own lifecycle APIs |
| FR | ADMIN-FR-001–004, USER/ORG/AUTHZ FRs |
| NFR | NFR-AUD-001 |
| Completion | Setting/user/org/role change audited |

### Steps

| # | UX step | API |
|---|---------|-----|
| 1 | Provision user | API-ADMIN-003 → delegates USER |
| 2 | Or direct USER create | API-USER-002 |
| 3 | Assign roles | API-AUTHZ-* |
| 4 | Provision/update org | API-ADMIN-004 / API-ORG-002 / API-ORG-003 |
| 5 | Update settings | API-ADMIN-001 |
| 6 | Configure integrations | API-ADMIN-002 |
| 7 | Review audit | API-ADMIN-005 |
| 8 | Optional AI prompts | API-AI-005 |

---

## Workflow 7 — AI-Assisted Investigation (Cross-cutting)

| Field | Specification |
|-------|---------------|
| Actor | Risk Analyst with AI permissions |
| Screens | SCR-05 + SCR-15 |
| APIs | API-AI-001 → API-AI-004; human follow-up API-INVEST-* |
| Timeout | Design target ~10s then fallback (NFR-PERF-006) |
| FR | AI-FR-001, AI-FR-009 |
| NFR | NFR-RES-003, NFR-SEC-010 |
| Completion | Recommendation displayed; **case state unchanged until human API** |
| Prohibited | Silent case mutation |

---

## Workflow 8 — Real-Time Queue Refresh

| Field | Specification |
|-------|---------------|
| Actor | Any user on Overview/Alerts/Investigations |
| API | **API-DASH-007** SSE; fallback poll API-DASH-003 / list APIs |
| FR | DASH-FR-011 |
| ADR | ADR-016 (SSE MVP; WebSocket V2) |
| Events | Alert*/Case* (and workspace) as consumed by DASH |
| Failure | Stale indicator + poll; no focus steal |
| Completion | Continuous while subscribed |

---

## V2 Placeholder — Security Signal Review

**Persona:** Security Engineer · **Domain:** SEC · **Status:** **Not MVP**

---

## Open Questions

| ID | Question | Classification |
|----|----------|----------------|
| WF-OQ-001 | Exact audit-package status polling shape | PARTIAL — follow OpenAPI for API-COMP-007 |
| WF-OQ-002 | Default Compliance visibility of Alerts | Permission-driven |

---

## Related Documents

- [DashboardScreens.md](DashboardScreens.md)
- [AIInteractionPatterns.md](AIInteractionPatterns.md)
- [APIInventory.md](../06-api/APIInventory.md)
- [Phase10Traceability.md](../08-development/Phase10Traceability.md)
