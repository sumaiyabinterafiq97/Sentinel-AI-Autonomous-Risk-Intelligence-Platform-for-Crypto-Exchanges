# Implementation Plan

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Implementation Plan |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 11 |
| Last Updated | 2026-09-11 |
| Authority | ProjectRoadmap §11, ADR-001–018, Phase 9–10 contracts, FDS/FRS |

---

## 1. Purpose

Provide a sequenced implementation blueprint so engineering can later open the Application Development Gate and build MVP without redesigning the system.

**APPLICATION DEVELOPMENT IS STILL BLOCKED DURING PHASE 11.**

This document is planning only — no application source code.

---

## 2. Implementation Principles

1. Contract-first (OpenAPI, event schemas, migration specs)
2. Domain ownership preserved (ADR-001, ADR-005)
3. AI assistive-only (ADR-002)
4. Deterministic RISK path independent of AI (ADR-003)
5. Human approval for consequential actions (ADR-006)
6. Vendor-neutral product requirements (ADR-007); stack below is **implementation direction candidate**
7. Logical domain boundaries ≠ mandatory microservice-per-domain
8. No invented APIs/events/tables
9. MVP / V2 / V3 isolation
10. Observability and tests continuous from foundation onward

---

## 3. Implementation Scope

| In scope (planning) | Out of scope (Phase 11) |
|---------------------|-------------------------|
| Module/service blueprint | Java/Python/TS source |
| Milestone sequence | SQL migration files |
| Tech direction candidates | Docker/K8s/CI YAML |
| Test/obs/devops plans | Production deploy |
| Gate assessment honesty | Fabricated approvals |

---

## 4. MVP Implementation Boundary

**Domains:** CORE, AUTH, AUTHZ, USER, ORG, DASH, ALERT, RISK, INVEST, COMP, AI, ADMIN

**APIs:** 68 MVP inventory IDs (Phase 9)
**Events:** 24 MVP schemed + deferred GD-002
**UI:** SCR-00–15, WF-1–8
**AI:** Investigation, Risk, Retrieval agents (AI-FR-001–009)
**Migrations (logical):** 001–012

---

## 5. V2 Boundary

WALLET, SEC, REPORT, OPS — plan dependencies only; do not implement in MVP milestones. SEC event lock preserved. OpenAPI V2 samples remain samples.

---

## 6. V3 / Future Boundary

Insider-threat patterns, automated containment, extended AI autonomy, advanced blockchain — deferred; require future FRs + governance.

---

## 7. Implementation Strategy

| Approach | Decision |
|----------|----------|
| Decomposition | Domain-aligned modules; **3–6 backend deployables** + web + AI service (ADR-001, SystemArchitecture) |
| API | REST contract-first (ADR-009) |
| Events | Transactional outbox + durable log (ADR-015) |
| DASH | BFF aggregation (ADR-017); SSE MVP (ADR-016) |
| Data | PostgreSQL schema-per-domain (ADR-010); Redis cache (ADR-012); pgvector AI (ADR-013); Neo4j V2 (ADR-011) |
| Stack direction | See Backend/AI/Frontend/DevOps plans — **candidate** pending implementation ADR under ADR-007 |

---

## 8. Recommended Implementation Sequence

Derived from ProjectRoadmap §11, refined for MVP AI/DASH placement:

| Milestone | ID | Work |
|-----------|-----|------|
| M0 | Foundations | Repo layout, shared libs, lint/test baselines, contract packages |
| M1 | CORE | Config, flags, health, audit context |
| M2 | Identity | AUTH + AUTHZ + USER + ORG |
| M3 | Event infra | Outbox, publisher, consumer skeleton, schema validation |
| M4 | RISK | Scoring, rules, assessments, RiskCalculated/HighRiskDetected |
| M5 | ALERT | Lifecycle consuming RISK; Alert* events |
| M6 | INVEST | Cases, evidence, notes, Case*/EvidenceAttached |
| M7 | COMP | KYC/AML/sanctions/travel-rule/audit packages |
| M8 | AI | Assistive agents + tools (Python service); AI events |
| M9 | DASH | BFF + SSE + queue projections |
| M10 | ADMIN | Settings, integrations, provision orchestration, audit query |
| M11 | Frontend | SCR-00–15 / WF-1–8 against real APIs |
| M12 | Hardening | Security, performance, obs SLOs, contract CI activation |

Observability and automated tests start in M0–M1 and continue.

**V2 milestones (post-MVP):** WALLET → SEC → REPORT → OPS (not MVP critical path).

---

## 9. Dependency Ordering

See [ImplementationDependencies.md](ImplementationDependencies.md).

Critical: Identity before domain writes; RISK before ALERT; INVEST/COMP after upstream context; AI after case/alert/risk read APIs; DASH after events exist; Frontend after stable contracts.

---

## 10. Parallelizable Work

| Track A | Track B | Track C |
|---------|---------|---------|
| M4–M7 domain backends | M8 AI service stubs | M11 frontend shell + auth against mocks → live |
| Contract CI tooling design | Migration SQL authoring (post-gate) | UX a11y fixtures |

Parallelism must not invent contracts or cross domain ownership.

---

## 11. Critical Path

```text
M0 → M1 → M2 → M3 → M4 → M5 → M6 → M7
                ↘ M8 (after M4–M6 reads)
                ↘ M9 (after M5–M6 events)
→ M10 → M11 → M12
```

---

## 12. Definition of Implementation Readiness (Phase 11 exit)

- Implementation plan package complete
- First milestone (M0/M1) entry/exit criteria defined
- Gate assessment updated with evidence
- Open conflicts documented
- Human gate approvals still may be pending

---

## 13. Definition of Development Readiness (Gate open)

Roadmap §10.2 all 12 items **APPROVED** with human sign-off recorded — see Phase11GateAssessment.md.

---

## 14. Definition of MVP Completion (post-gate)

- All MVP domain FRs verified via planned tests
- 68 MVP APIs implemented per OpenAPI
- MVP events produced/consumed per catalog (excl. GD-002 deferred)
- SCR-00–15 / WF-1–8 operable without AI required
- AI assistive paths degrade gracefully
- NFR simulation targets measured where defined

---

## 15–17. Risks, Open Decisions, Gate Dependencies

- Risks: [ImplementationRisks.md](ImplementationRisks.md)
- Open decisions: stack lock ADR (ADR-007); COMP list API gap; NFR-OQ-002; BQ-4; human approvals
- Gate: [Phase11GateAssessment.md](../00-project/Phase11GateAssessment.md) — **BLOCKED**

---

## Terminology

| Term | Meaning |
|------|---------|
| Logical module | Domain-aligned code boundary |
| Deployable | Independently runnable process/artifact |
| Implementation direction | Candidate stack for Phase 12+ — not FRS technology mandate |

---

## Related Documents

- [ImplementationArchitecture.md](ImplementationArchitecture.md)
- [ModuleImplementationPlan.md](ModuleImplementationPlan.md)
- [ImplementationDependencies.md](ImplementationDependencies.md)
- [ProjectRoadmap.md](../00-project/ProjectRoadmap.md)
