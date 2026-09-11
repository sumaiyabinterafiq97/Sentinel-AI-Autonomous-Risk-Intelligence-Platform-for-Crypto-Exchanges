# Data Implementation Plan

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Data Implementation Plan |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 11 |
| Last Updated | 2026-09-11 |

---

## Purpose

Plan persistence implementation from existing specs. **No SQL files in Phase 11.**

---

## Authoritative Inputs

DataArchitecture, PostgreSQL, Redis, pgvector, Neo4j, InitialMigrationSpecifications (001–012), MigrationStrategy, DataRetention, RetentionJurisdictionGovernance, ADR-010–013, ADR-018.

---

## Store Roles

| Store | Role | MVP |
|-------|------|-----|
| PostgreSQL | Authoritative domain schemas | Yes |
| Redis | Cache, idempotency, session aids — non-authoritative | Yes |
| pgvector | AI embeddings | Yes (AI) |
| Neo4j | Derived graph | **V2** (ADR-011) |

---

## Migration Order (Logical → Future SQL)

| ID | Schema | Milestone |
|----|--------|-----------|
| 001 | core | M1 |
| 002 | auth | M2 |
| 003 | authz | M2 |
| 004 | user | M2 |
| 005 | org | M2 |
| 006 | risk | M4 |
| 007 | alert | M5 |
| 008 | invest | M6 |
| 009 | comp | M7 |
| 010 | ai | M8 |
| 011 | admin | M10 |
| 012 | dash | M9 |

Rules: no cross-schema FKs; logical refs only; forward-only in production (MigrationStrategy).

---

## Ownership / Authoritative vs Derived

| Data | Authoritative owner | Derived consumers |
|------|---------------------|-------------------|
| Risk assessments | RISK | ALERT context, DASH widgets |
| Alerts | ALERT | DASH queues |
| Cases/evidence | INVEST | COMP/DASH/AI reads |
| Compliance reviews | COMP | REPORT V2 |
| Recommendations | AI | DASH display |
| Users/orgs | USER/ORG | ADMIN orchestration only |

---

## Cross-Cutting Data Rules

| Topic | Plan |
|-------|------|
| Tenant isolation | `organization_id` on tenant-scoped tables |
| Identifiers | UUID PKs per specs |
| Timestamps | created_at/updated_at; audit fields where specified |
| Indexes | Per InitialMigrationSpecifications |
| Soft delete | Only where specs allow |
| Retention | ADR-018 defaults; jurisdiction **PENDING LEGAL** (NFR-OQ-002) |
| Cache | Redis TTL; never SoT |
| Idempotency storage | Redis/DB keys per APIStandards |
| Vector | ai schema + pgvector; tenant filters |

---

## Verification

Migration apply/rollback in non-prod; schema isolation tests; retention job design later — no SQL now.

---

## Open Questions

| ID | Item |
|----|------|
| NFR-OQ-002 | Jurisdiction retention durations |
| P11-OQ-DATA-001 | Flyway vs Liquibase (post-gate tooling) |

---

## Related Documents

- [../04-database/InitialMigrationSpecifications.md](../04-database/InitialMigrationSpecifications.md)
- [../04-database/DataArchitecture.md](../04-database/DataArchitecture.md)
