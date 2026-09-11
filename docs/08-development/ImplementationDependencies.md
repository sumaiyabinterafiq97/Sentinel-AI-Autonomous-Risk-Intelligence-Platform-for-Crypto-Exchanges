# Implementation Dependencies

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Implementation Dependencies |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 11 |
| Last Updated | 2026-09-11 |

---

## Purpose

Technical and domain dependency order for MVP build. Derived from ProjectRoadmap §11 and Phase 9–10 contracts.

---

## Recommended Order

```text
M0 Foundations (repo, libs, contracts, lint/test baselines)
        ↓
M1 CORE
        ↓
M2 Identity (AUTH → AUTHZ → USER → ORG)
        ↓
M3 Event infrastructure (outbox/relay/consumer skeleton)
        ↓
M4 RISK
        ↓
M5 ALERT
        ↓
M6 INVEST
        ↓
M7 COMP
        ↓
M8 AI assistance (after RISK/ALERT/INVEST read APIs)
        ↓
M9 DASH BFF + SSE (after Alert/Case events)
        ↓
M10 ADMIN
        ↓
M11 Frontend (progressive earlier with mocks; hard-deps above for E2E)
        ↓
M12 Hardening (security, perf, obs SLOs, contract CI)
```

**V2 after MVP:** WALLET → SEC → REPORT → OPS.

---

## Dependency Classes

| Class | Examples |
|-------|----------|
| Technical | JDK/Gradle; Postgres; Redis; broker; OTel |
| Domain | RISK before ALERT; INVEST handoff after ALERT |
| API | OpenAPI freeze candidate before coding |
| Database | Migrations 001→012 order |
| Event | Outbox before producers; consumers after schemas |
| AI | Tool read APIs; pgvector; prompt store |
| Frontend | Auth session; BFF; domain APIs |
| Security | AUTH/AUTHZ before domain writes |
| Testing | Contract fixtures from M0 |
| Infrastructure | Local Compose plan before multi-service integration |

---

## Parallelism

- Frontend shell/auth can start after M2 contracts stable (mocks OK)
- AI stubs after M4 API shapes known
- Must not parallel-write conflicting schemas

---

## Critical Path

Identity → Event infra → RISK → ALERT → INVEST → (COMP ∥ AI) → DASH → ADMIN → Frontend E2E → Hardening

---

## Related Documents

- [ImplementationPlan.md](ImplementationPlan.md)
- [ModuleImplementationPlan.md](ModuleImplementationPlan.md)
