# Database Migration Strategy

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Database Migration Strategy |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 4 |
| Last Updated | 2026-09-03 |

---

## Purpose

Define migration philosophy and process for PostgreSQL schema-per-domain. **No executable SQL in Phase 4.**

---

## Principles

| Principle | Policy |
|-----------|--------|
| Domain ownership | Each domain owns migrations in its schema |
| Forward-first | Production migrations are forward-only |
| No cross-schema FK | Logical UUID references only |
| Tenant isolation | `organization_id` on tenant tables from first migration |
| Audit fields | `created_at`, `updated_at`, `created_by` where applicable |

---

## Naming Convention

`{sequence}_{domain}_{description}`

Examples:

- `001_core_init_platform_tables`
- `002_auth_init_session_tables`

---

## Ordering

1. **CORE** — platform config, audit, feature flags
2. **AUTH** — sessions, auth events (depends CORE for audit hooks conceptually)
3. **AUTHZ** — roles, permissions
4. **USER**, **ORG** — identity/tenant
5. Operational domains per FDS dependency order

AUTH migrations MUST NOT depend on business domains (RISK, ALERT, etc.).

---

## Transactional Expectations

- Each migration file runs in a single transaction where DDL allows
- Destructive changes require ADR + manual gate

---

## Rollback Philosophy

| Environment | Policy |
|-------------|--------|
| Development | Drop/recreate acceptable |
| Staging | Forward-fix preferred |
| Production | **Forward-only**; rollbacks via compensating migration |

---

## Testing

- Migrations applied to clean DB in CI (future)
- Verify schema matches PostgreSQL.md logical model
- Tenant isolation smoke test post-migration

---

## Seed / Reference Data

- Permissions catalog seeded in AUTHZ migration (reference data)
- No production secrets in seed scripts
- Feature flags default disabled

---

## Production Safety

- Review required for: column drops, type changes, index drops
- Large table migrations use online strategy (implementation phase)
- Backup before production apply (OPS V2 visibility)

---

## Related Documents

- [InitialMigrationSpecifications.md](InitialMigrationSpecifications.md)
- [PostgreSQL.md](PostgreSQL.md)
