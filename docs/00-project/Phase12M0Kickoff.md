# Phase 12 / M0 Kickoff

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Phase 12 M0 Kickoff |
| Version | 0.1 |
| Status | **AUTHORIZED** — Gate SATISFIED |
| Last Updated | 2026-09-11 |
| Authority | ApplicationDevelopmentGateDecision.md; ImplementationPlan.md M0 |

---

## Authorization

| Field | Value |
|-------|-------|
| Gate | **SATISFIED** |
| Phase 12 | **AUTHORIZED** |
| First milestone | **M0 — Foundations only** |
| Approver | Project Owner — approved |

Do **not** skip to M1–M12 product features in the first coding session without completing M0 exit criteria.

---

## M0 Purpose

Repository structure and shared libraries: contracts packaging, common types placeholders, lint/test baselines — per ProjectRoadmap §11 step 1 and ImplementationPlan M0.

---

## M0 Entry Criteria (Met)

- [x] Application Development Gate SATISFIED
- [x] Implementation plan approved
- [x] Stack ADR-019 accepted
- [x] OpenAPI + event schemas present under docs/06-api
- [x] Migration specs 001–012 documented (SQL not yet authored — M1+)

---

## M0 Work Items (Next Engineering Actions)

| ID | Task | Notes |
|----|------|-------|
| M0-1 | Create monorepo roots: `backend/`, `ai-service/`, `web/`, `contracts/` | **Done** |
| M0-2 | Package OpenAPI + event schemas into `contracts/` | **Done** — `sync-from-docs.sh` |
| M0-3 | Gradle multi-project skeleton for Java services (no domain business logic yet) | **Done** — health only |
| M0-4 | Python/FastAPI project skeleton for AI (health only acceptable) | **Done** |
| M0-5 | Vite/React TS app shell skeleton (routing placeholder) | **Done** |
| M0-6 | Lint/format/test baseline configs | **Done** (ruff, eslint, pytest, gradle test, vitest) |
| M0-7 | README architecture map linking to ImplementationPlan | **Done** |

**Forbidden in M0:** Implementing RISK/ALERT/INVEST business logic; V2 domains; inventing APIs; SQL production migrations without following InitialMigrationSpecifications.

---

## M0 Exit Criteria

- [x] Monorepo layout exists matching ImplementationArchitecture deployables
- [x] Contracts consumable from build paths
- [x] Empty/skeleton services start (health) without domain features
- [x] Lint/test commands documented
- [x] No V2 nav/modules introduced
- [x] AI service has no lifecycle mutation endpoints beyond approved assist stubs (stubs may wait for M8)

Completion report: [Phase12M0Report.md](Phase12M0Report.md)

---

## Non-Blocking Gaps (Carry Forward)

| Gap | Handling |
|-----|----------|
| COMP list API | Per Phase 10/11 — compose/widgets; do not invent |
| Global search | Compose domain list APIs |
| NFR-OQ-002 legal matrix | Use simulation defaults |
| GD-002 events | Deferred |
| V2 OpenAPI | Deferred |

---

## Recommended Immediate Next Action

1. Begin **M0-1** monorepo directory scaffolding and contract packaging.
2. Do not implement M4 RISK engine until M0–M3 exit criteria are met.
3. Keep all work uncommitted until the Project Owner requests a commit (current instruction: do not commit unless asked).

---

## Related Documents

- [ApplicationDevelopmentGateDecision.md](ApplicationDevelopmentGateDecision.md)
- [Phase11Handoff.md](Phase11Handoff.md)
- [../08-development/ImplementationPlan.md](../08-development/ImplementationPlan.md)
- [../03-architecture/ArchitectureDecisionRecords.md](../03-architecture/ArchitectureDecisionRecords.md) — ADR-019
