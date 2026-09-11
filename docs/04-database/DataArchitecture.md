# Data Architecture

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Data Architecture |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 3 |
| Owner | Data Architecture |
| Last Updated | 2026-09-03 |

---

## Purpose

Define data ownership, storage roles, consistency boundaries, and lifecycle policies for Sentinel AI.

**Authority:** FDS data ownership sections > this document > technology-specific docs.

---

## 1. Database-per-Domain Principle

Each domain owns its **authoritative relational schema** in PostgreSQL (logical database or schema-per-domain). No cross-domain direct table access.

| Store | Role | Source of truth? |
|-------|------|------------------|
| PostgreSQL (domain schemas) | Authoritative business state | Yes |
| Neo4j | Graph projections for relationships | No (derived) |
| Redis | Cache, rate limits, idempotency | No |
| pgvector | Embedding index for retrieval | No (derived) |

Co-location in one PostgreSQL cluster with separate schemas is acceptable for MVP (ADR-001 deployment option).

---

## 2. Consistency Boundaries

| Boundary | Model |
|----------|-------|
| Single domain write | ACID transaction in owning schema |
| Cross-domain workflow | Eventual consistency via events after commit |
| Read models (DASH, REPORT) | Eventually consistent projections |
| AI recommendations | Written to AI schema; business state unchanged until human API action |

**Rule:** Publish events only after durable commit in producer schema.

---

## 3. Event-to-Data Flow

```text
HTTP Command → Domain Service → Domain DB commit → Domain Event publish → Consumer updates own DB / projection
```

See [EventContracts.md](../06-api/EventContracts.md) and event-to-table mapping in [PostgreSQL.md](PostgreSQL.md).

---

## 4. Data Ownership Registry

| Entity | Owning Domain | PostgreSQL Schema | Write Authority | Read Consumers |
|--------|---------------|-------------------|-----------------|----------------|
| PlatformConfig | CORE | `core` | CORE | All (read via API) |
| AuditRecord | CORE | `core` | CORE (+ domain extensions) | ADMIN, OPS V2 |
| Session | AUTH | `auth` | AUTH | AUTHZ |
| Role, Permission | AUTHZ | `authz` | AUTHZ | All via AUTHZ |
| User | USER | `user` | USER | DASH, ADMIN orchestration |
| Organization | ORG | `org` | ORG | All (scoped) |
| RiskAssessment, RiskRule | RISK | `risk` | RISK | ALERT, INVEST, COMP, DASH, AI |
| Alert | ALERT | `alert` | ALERT | INVEST, DASH, SEC V2, AI |
| InvestigationCase, Evidence | INVEST | `invest` | INVEST | COMP, DASH, AI, SEC V2 |
| ComplianceRecord | COMP | `comp` | COMP | REPORT V2, AI |
| AIRecommendation, Prompt | AI | `ai` | AI | DASH |
| AdminSetting, Integration | ADMIN | `admin` | ADMIN | OPS V2 |
| WalletProfile | WALLET | `wallet` | WALLET (V2) | INVEST V2, AI |
| SecuritySignal | SEC | `sec` | SEC (V2) | DASH, INVEST context |
| ReportDefinition | REPORT | `report` | REPORT (V2) | DASH V2 |

---

## 5. Multi-Tenancy

| Aspect | Design |
|--------|--------|
| Tenant key | `organization_id` (UUID) on all tenant-scoped tables |
| Enforcement | Application + AUTHZ; optional PostgreSQL RLS in implementation |
| Cross-tenant queries | Prohibited except platform super-admin (audited) |
| Index pattern | Composite indexes leading with `organization_id` |
| Test requirement | Tenant isolation integration tests mandatory |

---

## 6. Read Models and Projections

| Projection | Owner | Source | Consistency |
|------------|-------|--------|-------------|
| DASH queue views | DASH | ALERT/RISK/INVEST APIs or local cache | Eventual |
| REPORT KPI snapshots | REPORT (V2) | Domain events | Eventual |
| Neo4j wallet graph | WALLET (V2) | WALLET + ingest events | Eventual |
| pgvector chunks | AI | Ingestion pipeline | Eventual |

Projections MUST be rebuildable from authoritative domain data + event log.

---

## 7. Data Classification

| Class | Examples | Storage requirements |
|-------|----------|---------------------|
| Public | Platform version | Standard |
| Internal | Operational metrics | Access controlled |
| Confidential | Alerts, risk scores, cases | Encryption at rest, RBAC |
| Restricted | PII, compliance, sanctions | Encryption, audit, minimal exposure |

See Security Architecture for field-level handling.

---

## 8. Retention and Lifecycle

| Data class | Active retention | Archive | Deletion |
|------------|------------------|---------|----------|
| Operational (alerts, cases) | 2 years default (configurable) | 3–7 years | Policy-driven |
| Compliance records | Per regulatory config | Long-term archive | Legal hold aware |
| Audit records | 7 years minimum (design objective) | Immutable | No hard delete |
| AI recommendations | 1 year | 2 years | Anonymize where possible |
| Redis cache | TTL-based | N/A | Automatic |
| Vector embeddings | Tied to source document lifecycle | Re-embed on model change | Delete with source |
| Neo4j graph projections | Rebuildable | Snapshot optional | Rebuild preferred over long retention |

Exact periods: **OPEN — requires compliance stakeholder** (NFR-OQ-002).

---

## 9. Backup and DR

| Component | Strategy |
|-----------|----------|
| PostgreSQL | Continuous backup; PITR; RPO ≤ 1h target (NFR-DR-001) |
| Neo4j | Periodic export; rebuild from WALLET/RISK source acceptable |
| Redis | No backup required for cache; idempotency keys ephemeral |
| pgvector | Rebuild from source documents |

OPS domain (V2) surfaces backup status via OPS-FR-005.

---

## 10. Encryption

| State | Requirement |
|-------|-------------|
| At rest | Confidential/restricted schemas encrypted (NFR-SEC-004) |
| In transit | TLS (NFR-SEC-003) |
| Secrets | Secret store; never in PostgreSQL plaintext |

---

## 11. AI Data Boundary

AI domain stores: prompts, recommendations, agent run metadata, embedding references.

AI does NOT store authoritative: alerts, cases, compliance outcomes. Tool calls read via authorized APIs; writes go through owning domain APIs only when human-initiated.

---

## 12. Audit Model

CORE `audit_records` table receives:

- Actor, actor type, action, resource type, resource ID, organization ID, outcome, correlation ID, request ID, timestamp, metadata JSON

Domain services call CORE audit API after successful state change (SEC-FR-008 pattern—uses CORE, does not redefine).

---

## 13. Migration Policy

- Schema migrations owned per domain team
- Forward-only in production
- No cross-schema FK constraints (use logical references UUID only)

---

## 14. MVP Data Model Hardening (Phase 5)

This section consolidates authoritative store rules for MVP implementation planning.

### 14.1 Authoritative stores (MVP)

| Domain | Authoritative store | Non-authoritative |
|--------|---------------------|-------------------|
| CORE | PostgreSQL `core` | — |
| AUTH | PostgreSQL `auth` | — |
| AUTHZ | PostgreSQL `authz` | Redis permission cache |
| USER | PostgreSQL `user` | — |
| ORG | PostgreSQL `org` | — |
| RISK | PostgreSQL `risk` | — |
| ALERT | PostgreSQL `alert` | DASH projection cache |
| INVEST | PostgreSQL `invest` | — |
| COMP | PostgreSQL `comp` | — |
| AI | PostgreSQL `ai` + pgvector | Recommendations not business state |
| ADMIN | PostgreSQL `admin` | — |
| DASH | PostgreSQL `dash` (prefs only) | All alert/case data via APIs |

### 14.2 Lifecycle ownership (MVP)

| Lifecycle | Owner | Others may |
|-----------|-------|------------|
| Session/auth | AUTH | SEC consumes events (V2) |
| Permission decision | AUTHZ | All domains call evaluate |
| User profile | USER | ADMIN orchestrates create |
| Organization | ORG | ADMIN orchestrates create |
| Risk assessment | RISK | Publish events; no alert create |
| Alert | ALERT | Consume risk; own priority |
| Investigation case | INVEST | Link alerts; attach evidence |
| Compliance outcome | COMP | Human decision required |
| AI recommendation | AI | Display only until human acts |

### 14.3 Primary identifiers

All domain entities use UUID `id` as primary key. External references use domain-prefixed logical IDs in APIs (`alertId`, `caseId`, etc.) mapping to UUID.

Tenant scope: `organization_id` mandatory on tenant-scoped tables. See InitialMigrationSpecifications.md for CORE through ORG.

### 14.4 Derived data rules

| Derived | Source | Rebuild |
|---------|--------|---------|
| Neo4j graph | WALLET/RISK (V2) | From domain events + ingest |
| pgvector embeddings | INVEST evidence, case docs | Re-embed on model change |
| DASH projection cache | ALERT/RISK/INVEST APIs | TTL expiry; event invalidation |
| Redis cache | Domain read APIs | TTL; never authoritative |

### 14.5 Retention references

Default retention tiers: [DataRetention.md](DataRetention.md) and ADR-018. Jurisdiction overrides: NFR-OQ-002 (open).

---

## Related Documents

- [PostgreSQL.md](PostgreSQL.md)
- [Neo4j.md](Neo4j.md)
- [Redis.md](Redis.md)
- [VectorDataArchitecture.md](VectorDataArchitecture.md)
