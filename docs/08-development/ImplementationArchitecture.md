# Implementation Architecture

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Implementation Architecture |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 11 |
| Last Updated | 2026-09-11 |
| Authority | SystemArchitecture, ADR-001, ADR-015–017, DomainBoundaries |

---

## Purpose

Translate logical architecture into an implementation-oriented blueprint: deployables vs modules, sync/async paths, ownership boundaries.

**No application code in this phase.**

---

## Scope / Inputs

SystemArchitecture.md, DomainBoundaries.md, EventArchitecture.md, MessageBrokerArchitecture.md, AIArchitecture.md, SecurityArchitecture.md, ObservabilityArchitecture.md, OpenAPI, event catalog, Phase 9–10 UX.

---

## Logical vs Physical

| Layer | Rule |
|-------|------|
| Logical | One FDS domain = one ownership module; no lifecycle duplication |
| Physical MVP | Prefer **3–6 backend deployables** + AI service + web app (SystemArchitecture; ADR-001) |
| Anti-pattern | Microservice-per-table or per-screen |

---

## Recommended MVP Deployables

| Deployable ID | Contains (logical modules) | Rationale |
|---------------|----------------------------|-----------|
| `sentinel-platform` | CORE (+ shared libraries host) | Foundation health/config/audit helpers |
| `sentinel-identity` | AUTH, AUTHZ, USER, ORG | Identity/tenancy cohesion |
| `sentinel-ops` | RISK, ALERT, INVEST, COMP | Operational critical path |
| `sentinel-dash` | DASH BFF | Aggregation + SSE (ADR-016/017) |
| `sentinel-admin` | ADMIN | May **co-locate** with `sentinel-platform` if process count must stay ≤5 |
| `sentinel-ai` | AI Platform | Separate runtime (Python direction) |
| `sentinel-web` | Frontend SPA | Presentation only |

**Default MVP process count:** 5 backend-ish (`platform` incl. ADMIN, `identity`, `ops`, `dash`, `ai`) + `web` = within guidance.

**Open:** SA-OQ-001 / P11-OQ-DEP-001 — exact grouping confirmed at gate kickoff.

---

## Synchronous Communication

| From | To | Pattern |
|------|-----|---------|
| Web | DASH BFF | HTTPS REST |
| Web | Domain APIs (as designed) | HTTPS REST when not via BFF |
| DASH | Domain services | Server-side REST |
| ADMIN | USER/ORG | Orchestration REST (no duplicate persistence) |
| AI | Domain read APIs / tools | Authorized HTTP tool calls |
| Clients | AUTH | Login/session REST |

---

## Asynchronous Communication

| Pattern | Use |
|---------|-----|
| Domain events | Lifecycle notifications (ADR-004, ADR-015) |
| Outbox → broker/log | At-least-once publish |
| Consumers | ALERT←RISK; DASH←many; AI←context; COMP←cases; etc. |
| SSE | DASH → browser refresh hints (not SoT writes) |

---

## Ownership Matrix (Summary)

| Concern | Owner |
|---------|-------|
| API operations | Domain of `x-api-id` |
| Event payload | Producer domain |
| Event envelope | Platform/CORE conventions |
| PostgreSQL schema | Domain schema (ADR-010) |
| AI recommendations | AI schema; no ALERT/INVEST writes |
| AuthN session | AUTH |
| AuthZ decisions | AUTHZ |
| Presentation aggregation | DASH |

---

## AI Service Boundary

- Deployable `sentinel-ai` only
- Assistive agents; tool allowlist; no lifecycle ownership
- Publishes AIRecommendationGenerated, PromptUpdated only among AI events
- Does not publish RiskCalculated / AlertCreated / CaseClosed

---

## Frontend / Backend Boundary

- Web holds no authoritative domain state
- Mutations always via owning domain APIs
- BFF may aggregate reads; must not become SoT for alerts/cases

---

## AuthN / AuthZ / Observability Boundaries

| Boundary | Implementation note |
|----------|---------------------|
| AuthN | JWT/session per AUTH APIs; Spring Security direction on Java services |
| AuthZ | Permission checks on every mutating API; AI tools re-check |
| Observability | Correlation IDs end-to-end; OTel direction; see ObservabilityImplementationPlan |

---

## Open Questions

| ID | Question |
|----|----------|
| P11-OQ-DEP-001 | Co-locate ADMIN with platform vs separate deployable |
| P11-OQ-DEP-002 | Split `sentinel-ops` if team/scale requires (post-MVP) |

---

## Verification

Architecture review at gate: deployable list ≤6 backend, domain modules mapped, no V2 in MVP deployables.

---

## Related Documents

- [ImplementationPlan.md](ImplementationPlan.md)
- [ModuleImplementationPlan.md](ModuleImplementationPlan.md)
- [../03-architecture/SystemArchitecture.md](../03-architecture/SystemArchitecture.md)
