# Phase 8 Handoff — Phases 9 / 10 / 11

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Phase 8 Handoff |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 8 |
| Last Updated | 2026-09-11 |
| Authority | ProjectRoadmap.md phase definitions |

---

## Context

Governance-track Phases 1–8 produced substantial API, data, UX, and AI documentation **ahead of** the numbered roadmap Phase 9–11 sequence. Handoff must follow **ProjectRoadmap** phase purposes while avoiding duplicate reinvention.

**Application Development remains BLOCKED** until Section 10 gate is satisfied.

---

## Phase 9 — API & Event Contracts (Roadmap)

### Purpose (roadmap)

Make internal and external contracts explicit for implementation and testing.

### Inputs

- Existing OpenAPI.yaml, APIInventory, EventContracts, schemas, SchemaRegistryGovernance
- FDS/FRS event matrices
- Phase8GateAssessment

### Required work

1. Formal **Phase 9 exit review** of already-authored API/event artifacts (do not rewrite from scratch)
2. Close remaining optional gaps (AsyncAPI optional; deferred GD-002 events only if consumers require)
3. Confirm SEC lock + AI event boundaries
4. Produce Phase 9 completion report + human approval placeholders

### Deliverables

- Phase9Report.md
- Any delta contract fixes justified by FRS/FDS only
- Updated gate assessment for item #10

### Exit criteria

- MVP and in-scope V2 events have contract definitions
- API ownership matches domains
- Human Phase 9 exit recorded (or explicitly pending)

### Blocking dependencies

- Frozen FRS/FDS unchanged except change control
- AI assistive boundaries (Phase 8)

### Forbidden work

- Application code
- Invented events/APIs
- Promoting V2 REPORT/SEC into MVP

---

## Phase 10 — Frontend / UX Design (Roadmap)

### Purpose (roadmap)

Define operator experience without DASH owning upstream lifecycles.

### Inputs

- docs/07-ui/* (Principles, IA, Workflows, DesignSystem, Accessibility, DashboardScreens, ComponentLibrary)
- Personas; DASH/ALERT/INVEST/COMP FRs
- Phase 8 AI UI boundaries (AIExplanationPanel)

### Required work

1. Formal Phase 10 exit review of UX docs
2. Fill remaining screen-level gaps if any (e.g., Navigation.md alignment)
3. Accessibility acceptance checklist for MVP workflows
4. Phase10Report.md

### Deliverables

- UX baseline marked review-ready
- Updated gate assessment for item #11

### Exit criteria

- Primary workflows mapped to personas
- Presentation-only boundaries preserved
- Human Phase 10 exit recorded (or pending)

### Blocking dependencies

- PRD scope (MVP screens only)
- API inventory (no invented UI backends)

### Forbidden work

- React/TypeScript production app
- UI inventing domain events/requirements
- V2 SEC/REPORT as MVP default nav

---

## Phase 11 — Development Roadmap & Implementation Planning

### Purpose (roadmap)

Translate approved design into an executable build sequence; satisfy Application Development Gate checklist.

### Inputs

- All Phase 1–10 artifacts + approvals
- Phase8GateAssessment
- Migration specs 001–012
- AIArchitecture / AIAgents

### Required work

1. Service/repo layout plan
2. Milestone slicing (CORE→AUTH→… per roadmap §11)
3. Test strategy activation plan
4. Explicit Application Development Gate review meeting
5. ImplementationPlan.md (or update docs/10-roadmap/*)

### Deliverables

- Implementation plan
- Gate checklist with evidence
- Phase11Report.md

### Exit criteria

- Section 10 checklist satisfied **with human approvals**
- First implementation milestone entry/exit criteria defined

### Blocking dependencies

- PRD formal approval
- Phase 8–10 exits
- BQ-4 disposition by product owner
- NFR-OQ-002 accepted as defaults-only or filled

### Forbidden work

- Starting production service implementation before gate
- Rewriting frozen FRs for coding convenience

---

## Related Documents

- [ProjectRoadmap.md](ProjectRoadmap.md)
- [Phase8GateAssessment.md](Phase8GateAssessment.md)
- [Phase8Report.md](Phase8Report.md)
