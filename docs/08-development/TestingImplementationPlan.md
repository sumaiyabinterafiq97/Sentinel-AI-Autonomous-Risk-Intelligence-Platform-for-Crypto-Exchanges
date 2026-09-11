# Testing Implementation Plan

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Testing Implementation Plan |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 11 |
| Last Updated | 2026-09-11 |

---

## Purpose

Map verification strategy for post-gate implementation. Extends APIAndDataTestingStrategy.md and ContractValidationCI.md. **No executable test suites in Phase 11.**

---

## Test Layers

| Layer | Scope |
|-------|-------|
| Unit | Domain logic, validators, pure AI helpers |
| Integration | API + DB + outbox |
| Contract | OpenAPI, event JSON Schema, compatibility |
| Security | AuthZ, tenant, AI tools |
| AI | Agents, retrieval, hallucination, timeout |
| Frontend | Components, workflows, a11y |
| E2E | WF-1–8 persona paths |
| NFR | Perf, load, resilience, obs |

---

## FR → Component → Test (MVP Critical)

| FR area | Component | Test layer | Method |
|---------|-----------|------------|--------|
| RISK scoring | ops/risk | Unit+Integration+Event | Score without AI; schema RiskCalculated |
| ALERT priority/close | ops/alert | API+Security | Human close; priority ownership |
| INVEST case close | ops/invest | API+Audit | Human only; EvidenceAttached consumers |
| COMP decisions | ops/comp | API+Audit | Confirm required; no AI approve |
| AI-FR-001–003 | sentinel-ai | AI+Security | No case mutation; tool deny |
| ADMIN provision | platform/admin | Integration | Delegates USER/ORG |
| DASH SSE | dash | Integration+FE | SSE + poll fallback |
| AUTH/AUTHZ | identity | Security | Session; deny-by-default |

Full chains: ImplementationTraceability.md + Phase3–10 matrices.

---

## Backend Tests (Planned)

Unit, repository (Testcontainers), controller/OpenAPI, outbox publish, consumer idempotency, security tenant isolation.

---

## AI Tests (Planned)

Agent golden cases; tool authorization; prompt regression; hallucination/groundedness; retrieval tenant filter; timeout/fallback; eval hooks (runtime V2).

---

## Frontend Tests (Planned)

Component; workflow (WF-1/3/4/7 priority); a11y automated+manual; MSW/contract consumer; E2E staging.

---

## Contract / NFR

OpenAPI diff CI (design exists); event schema CI; perf vs NFR-PERF-*; resilience AI down (ADR-008).

---

## Activation Timing

| When | What |
|------|------|
| M0–M1 | Unit+lint+contract fixtures |
| Each domain M | Integration + security for that domain |
| M8+ | AI suites |
| M11+ | E2E + a11y |
| M12 | Perf/resilience gates |

---

## Related Documents

- [APIAndDataTestingStrategy.md](APIAndDataTestingStrategy.md)
- [ContractValidationCI.md](ContractValidationCI.md)
- [../05-ai/EvaluationFramework.md](../05-ai/EvaluationFramework.md)
