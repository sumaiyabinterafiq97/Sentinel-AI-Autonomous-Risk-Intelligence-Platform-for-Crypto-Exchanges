# Backend Implementation Plan

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Backend Implementation Plan |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 11 |
| Last Updated | 2026-09-11 |

---

## Purpose

Implementation-oriented structure for Java backend services. **No Java source in Phase 11.**

---

## Authoritative Inputs

ADR-001–018, OpenAPI, APIStandards, ErrorHandling, ModuleImplementationPlan, SecurityArchitecture.

---

## Implementation Direction (Candidate)

| Technology | Role | Note |
|------------|------|------|
| Java 21 | Runtime | Candidate under ADR-007 |
| Spring Boot 3 | Service framework | Candidate |
| Spring Security | AuthN/Z filters | Align to AUTH/AUTHZ |
| Spring Data JPA / Hibernate | Persistence | Schema-per-domain |
| Gradle | Build | Multi-project preferred |

**P11-OQ-STACK-001:** Confirm stack via implementation ADR before coding gate closes — ADR-007 requires explicit selection ADR.

---

## Project / Module Structure (Planned)

```text
sentinel-backend/
  build.gradle.kts
  settings.gradle.kts
  libs/
    contracts-openapi/          # generated or checked OpenAPI stubs (post-gate)
    contracts-events/           # schema resources
    common-security/
    common-observability/
    common-outbox/
  services/
    platform/                   # CORE (+ ADMIN optional)
    identity/                   # AUTH AUTHZ USER ORG
    ops/                        # RISK ALERT INVEST COMP
    dash/                       # DASH BFF
```

---

## Package Organization (Per Service)

```text
com.sentinel.<service>/
  api/              # controllers, DTOs, OpenAPI mapping
  application/      # use cases / application services
  domain/           # domain model & domain services
  infrastructure/   # JPA repos, outbox, broker, clients
  config/           # Spring configuration
```

---

## Layer Responsibilities

| Layer | Does | Does not |
|-------|------|----------|
| Controllers | HTTP, validation annotations, map to commands | Business rules |
| Application services | Orchestrate transactions, publish outbox | Cross-domain writes |
| Domain services | Invariants | Call other domains’ DBs |
| Repositories | Persist owned schema only | Cross-schema FK |
| DTOs | API boundary types | Leak JPA entities externally |

---

## Cross-Cutting

| Concern | Plan |
|---------|------|
| Validation | Bean Validation + ErrorResponse |
| Exceptions | Map to APIStandards codes; requestId |
| AuthN | Bearer JWT validation; public health/login |
| AuthZ | Method/HTTP permission checks from AUTHZ |
| Transactions | Domain write + outbox same TX |
| Idempotency | Idempotency-Key → Redis/DB record (ADR-012) |
| Audit | CORE audit + domain trails |
| Event publish | Outbox rows → relay (ADR-015) |
| Config | Externalized; secrets not in repo |
| Dependencies | Gradle version catalog |

---

## Service Mapping

| Deployable | Domains |
|------------|---------|
| platform | CORE, ADMIN (optional) |
| identity | AUTH, AUTHZ, USER, ORG |
| ops | RISK, ALERT, INVEST, COMP |
| dash | DASH |

---

## Verification

Controller contract tests vs OpenAPI; repository isolation; outbox publish tests; security tests — see TestingImplementationPlan.md.

---

## Open Questions

| ID | Item |
|----|------|
| P11-OQ-STACK-001 | Formal Java/Spring ADR acceptance |
| P11-OQ-BE-001 | Monorepo vs polyrepo |

---

## Related Documents

- [ImplementationArchitecture.md](ImplementationArchitecture.md)
- [SecurityImplementationPlan.md](SecurityImplementationPlan.md)
- [EventImplementationPlan.md](EventImplementationPlan.md)
