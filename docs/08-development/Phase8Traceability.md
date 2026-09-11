# Phase 8 Traceability Matrix

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Phase 8 Traceability |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 8 |
| Last Updated | 2026-09-11 |

---

## Purpose

Map Product Goal → Persona → PRD → FR → NFR → API → Event → Data → UI → Architecture → AI → Validation for MVP, with emphasis on AI architecture completion.

---

## AI Capability Chains

### Investigation Assistance

| Layer | Reference | Status |
|-------|-----------|--------|
| Product Goal | PG assistive investigation | ✅ |
| Persona | Risk Analyst | ✅ |
| PRD | §29–30 | ✅ |
| FR | AI-FR-001, AI-FR-005–009 | ✅ |
| NFR | NFR-PERF-006, NFR-RES-003, NFR-SEC-010 | ✅ |
| API | API-AI-001, API-AI-004 | ✅ |
| Event | AIRecommendationGenerated; consumes CaseUpdated, EvidenceAttached, AlertCreated | ✅ |
| Data | ai_recommendations, agent_runs | ✅ mig 010 |
| UI | SCR-05, SCR-15 | ✅ |
| Architecture | AIArchitecture Investigation Agent | ✅ |
| Validation | No case mutation; timeout fallback | ⚠️ design only |

### Risk Explanation Assistance

| Layer | Reference | Status |
|-------|-----------|--------|
| FR | AI-FR-002 | ✅ |
| API | API-AI-002 | ✅ |
| Event | AIRecommendationGenerated; consumes RiskCalculated | ✅ |
| Data | ai_* | ✅ |
| UI | SCR-03, SCR-06, SCR-15 | ✅ |
| Architecture | Risk Agent; non-critical path ADR-003 | ✅ |
| Boundary | No RiskCalculated publish; no alert create | ✅ |

### Retrieval

| Layer | Reference | Status |
|-------|-----------|--------|
| FR | AI-FR-003, AI-FR-008 | ✅ |
| API | API-AI-003 | ✅ |
| Data | pgvector + processed_event_ids | ✅ |
| Architecture | Retrieval Agent + ToolDefinitions | ✅ |

### Prompt Management

| Layer | Reference | Status |
|-------|-----------|--------|
| FR | AI-FR-004 | ✅ |
| API | API-AI-005 | ✅ |
| Event | PromptUpdated | ✅ |
| UI | Admin/AI config (privileged) | ⚠️ partial screen detail |

---

## Governance Chains

| Topic | Trace | Status |
|-------|-------|--------|
| PRD approval | PRD → PRDReviewRecord R0–R8 → PRDApprovalRecord | PENDING HUMAN APPROVAL |
| BQ-4 | FDS REPORT V2 → GD-001 → PRD §16 | PENDING HUMAN DECISION (sign-off) |
| NFR-OQ-002 | NFR → RetentionDecisionMatrix | PENDING COMPLIANCE / LEGAL |

---

## Gap Classification

| ID | Gap | Class |
|----|-----|-------|
| TR8-001 | PRD formal signatures | Pending human decision / **Blocking for gate** |
| TR8-002 | BQ-4 PO sign-off | Pending human decision |
| TR8-003 | NFR-OQ-002 durations | Deferred / Pending compliance |
| TR8-004 | Implementation plan | Not started / **Blocking** |
| TR8-005 | Executable AI eval harness | Deferred (V2 runtime) / Non-blocking |
| TR8-006 | Roadmap Phase 9/10 formal exits | Partial docs exist / Blocking until approved |
| TR8-007 | Prompt admin dedicated screen detail | Non-blocking |

---

## Related Documents

- [AIArchitecture.md](../03-architecture/AIArchitecture.md)
- [Phase7Traceability.md](Phase7Traceability.md)
- [Phase8GateAssessment.md](../00-project/Phase8GateAssessment.md)
