# Data Retention

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Data Retention Policy |
| Version | 0.2 (Draft) |
| Status | Draft — Phase 6 strengthened |
| Owner | Data Architecture / Compliance (joint) |
| Last Updated | 2026-09-03 |
| Authority | ADR-018 default targets; NFR-PRIV-002; FDS data ownership |

---

## Purpose

Define data retention, archival, deletion, and legal-hold principles for Sentinel AI data stores. This document supports implementation planning and compliance review. It does **not** constitute legal advice or approved regulatory policy.

**Related:** [DataArchitecture.md](DataArchitecture.md), [MigrationStrategy.md](MigrationStrategy.md), ADR-018

---

## 1. Retention Principles

| Principle | Description |
|-----------|-------------|
| Domain ownership | Each domain owns retention configuration for its authoritative data |
| Purpose limitation | Retain data only for operational, security, compliance, and audit purposes defined in requirements |
| Tenant isolation | Retention and purge operations are scoped per `organization_id` |
| Audit immutability | Audit records are append-only; no hard delete without governed exception |
| Derived data | Neo4j, Redis, pgvector projections follow source-domain retention or rebuild rules |
| Legal hold | Legal hold suspends automated purge for affected records |
| Jurisdiction override | Default tiers are superseded by approved jurisdiction-specific policy (NFR-OQ-002) |
| Simulation vs production | Default tiers are **design targets** until compliance stakeholder approval |

---

## 2. Data Categories

| Category | Owning domain(s) | Store | Classification |
|----------|------------------|-------|----------------|
| Platform configuration | CORE | PostgreSQL `core` | Internal |
| Shared audit records | CORE | PostgreSQL `core` | Restricted |
| Sessions and auth events | AUTH | PostgreSQL `auth` | Confidential |
| Authorization catalog | AUTHZ | PostgreSQL `authz` | Internal |
| User profiles | USER | PostgreSQL `user` | Restricted (PII) |
| Organizations | ORG | PostgreSQL `org` | Internal |
| Risk assessments and rules | RISK | PostgreSQL `risk` | Confidential |
| Alerts | ALERT | PostgreSQL `alert` | Confidential |
| Investigation cases and evidence metadata | INVEST | PostgreSQL `invest` | Restricted |
| Compliance records | COMP | PostgreSQL `comp` | Restricted |
| AI recommendations and prompts | AI | PostgreSQL `ai` | Confidential |
| Admin settings | ADMIN | PostgreSQL `admin` | Internal |
| DASH presentation state | DASH | PostgreSQL `dash` | Internal |
| Cache and idempotency | All (via Redis) | Redis | Ephemeral |
| Graph projections | WALLET (V2) | Neo4j | Derived |
| Embeddings | AI | pgvector | Derived |
| Observability telemetry | Platform | Log/metrics backend | Internal |
| V2 security signals | SEC | PostgreSQL `sec` | Confidential |
| V2 reports | REPORT | PostgreSQL `report` | Internal |

---

## 3. Default Retention Tiers (ADR-018)

These are **proposed baselines** for implementation. Production deployment requires compliance review.

| Tier | Data examples | Online retention | Archive | Purge |
|------|---------------|------------------|---------|-------|
| T0 Ephemeral | Redis cache, idempotency keys | Minutes–hours (TTL) | None | Automatic |
| T1 Operational telemetry | Metrics, traces | 90 days | Optional cold | Policy-driven |
| T2 Application logs | Non-PII service logs | 180 days | 1 year cold | Policy-driven |
| T3 Operational business | Alerts, risk assessments | 2 years | 5 years | After archive + hold check |
| T4 Identity audit | Sessions, auth events, CORE audit | 2 years online | 7 years cold | Restricted; legal hold aware |
| T5 Investigation | Cases, evidence metadata | 7 years | Extended on hold | Legal hold extends |
| T6 Compliance | KYC, sanctions, audit packages | 7 years | Extended on hold | Legal hold extends |
| T7 AI assistive | Recommendations, agent runs | 1 year | 2 years | Anonymize where possible |
| T8 V2 security | SEC threat records | 2 years online | 5 years archive | V2 activation |
| T9 V2 reporting | REPORT artifacts | 7 years | Legal hold | V2 activation |

---

## 4. Audit Retention

| Record type | Owner | Retention | Notes |
|-------------|-------|-----------|-------|
| `core.audit_records` | CORE | T4 (2y online / 7y archive) | NFR-AUD-001; immutable append |
| Domain-specific audit columns | Owning domain | Follow entity retention | `created_by`, `updated_by` on domain tables |
| ADMIN action log | ADMIN | T4 minimum | AdminSettingUpdated trace |
| AUTH auth_events | AUTH | T4 | Login/logout/MFA events |

**Purge safety:** Audit records require governed exception process; default policy is **no hard delete**.

---

## 5. Security Data Retention

| Data | Domain | Retention tier | Notes |
|------|--------|----------------|-------|
| Session records | AUTH | T4 | Expired sessions may be archived |
| Registered devices | AUTH | T4 | AUTH owns registration |
| API activity (V2) | SEC | T8 | SEC-FR scope |
| Security signals (V2) | SEC | T8 | ThreatDetected source data |
| Failed auth attempts | AUTH | T2–T4 | Security investigation support |

SEC does not retain AUTH session lifecycle data authoritatively — consumes events only.

---

## 6. Transaction and Risk Data Retention

| Data | Domain | Retention tier | Notes |
|------|--------|----------------|-------|
| Risk assessments | RISK | T3 | Investigation lookback |
| Rule hits | RISK | T3 | Linked to assessment |
| Transaction ingest log | RISK | T3 | Idempotency + audit |
| Risk rules | RISK | Life of rule + T3 after deactivation | Version history preserved |

---

## 7. Investigation Data Retention

| Data | Domain | Retention tier | Notes |
|------|--------|----------------|-------|
| Investigation cases | INVEST | T5 | Case lifecycle owner |
| Case evidence metadata | INVEST | T5 | Blob storage follows classification |
| Case notes and timeline | INVEST | T5 | Analyst accountability |
| Alert–case links | ALERT/INVEST | T5 | Reference only |

Evidence blob storage (external object store) retention follows INVEST metadata classification — **implementation detail, not defined here**.

---

## 8. AI and Evaluation Telemetry Retention

| Data | Domain | Retention tier | Notes |
|------|--------|----------------|-------|
| AI recommendations | AI | T7 | Assistive audit trail |
| Agent runs | AI | T7 | Latency, token usage |
| Prompt versions | AI | T7 | Prompt governance |
| Processed event IDs | AI | T0–T1 | Consumer idempotency |
| pgvector embeddings | AI | Tied to source | Rebuild on model change |
| Evaluation results (V2) | AI | T7 | AI eval framework |

**Policy placeholder:** Training reuse of production data requires explicit organizational AI policy — not authorized by default FRs.

---

## 9. Deletion Requirements

| Requirement | Implementation expectation |
|-------------|---------------------------|
| Tenant offboarding | Purge or export per contract; legal hold check first |
| User erasure requests | Coordinate USER + dependent domains; **jurisdiction-dependent** |
| Right-to-erasure vs audit | **Open question** — legal/compliance must define conflict resolution |
| Soft delete | USER uses `deleted_at` where FR requires history |
| Hard delete | Prohibited for audit records without exception |
| Cascade | No cross-schema FK cascades; orchestrated purge per domain |

---

## 10. Archival Strategy

| Approach | Applicability |
|----------|---------------|
| Partition by time | Large tables (audit, assessments, alerts) |
| Cold object storage | Archive tier after online retention |
| Event log retention | Aligns with broker retention policy (ADR-015) |
| Rebuild over retain | Neo4j, pgvector — prefer rebuild from source |

Archival jobs are **domain-owned** with COMP/ADMIN configuration hooks for jurisdiction overrides.

---

## 11. Tenant Isolation

| Rule | Description |
|------|-------------|
| Scope | All purge/archive jobs filter by `organization_id` |
| Cross-tenant | Prohibited except platform super-admin (audited) |
| Super-admin purge | Requires break-glass procedure — **policy placeholder** |
| Test data | Separate tenant IDs; accelerated TTL in non-production |

---

## 12. Jurisdiction Overrides

| Status | Description |
|--------|-------------|
| NFR-OQ-002 | **OPEN** — exact periods per jurisdiction require compliance stakeholder |
| Mechanism | COMP/ADMIN configuration stores override matrix when approved |
| Precedence | Jurisdiction override > ADR-018 default > platform default |
| Documentation | Override matrix maintained outside this document once approved |

**Do not invent** EU/US/UK specific retention periods without stakeholder input.

---

## 13. Legal Hold Considerations

| Aspect | Policy |
|--------|--------|
| Trigger | Legal/compliance notification — **process placeholder** |
| Effect | Suspend automated purge for held records |
| Scope | Record, case, user, or organization level |
| Release | Documented authorization required to lift hold |
| Implementation | `legal_hold` flag or external hold registry — design TBD |

---

## 14. Backup Retention

| Component | Backup retention | RPO/RTO reference |
|-----------|------------------|-------------------|
| PostgreSQL | 30 days minimum rolling (design target) | NFR-DR-001 RPO ≤ 1h |
| Neo4j | 7 days or rebuild-from-source | Derived |
| Redis | No backup (non-authoritative) | ADR-012 |
| Event log | Broker retention policy | ADR-015 |

OPS domain (V2) surfaces backup status via OPS-FR-005.

---

## 15. Purge Safety

| Control | Requirement |
|---------|-------------|
| Dry run | Purge jobs support count/preview mode |
| Audit | Purge execution logged to CORE audit |
| Two-person rule | Production purge — **policy placeholder** for restricted tiers |
| Dependency check | Cross-domain references validated before purge |
| Legal hold | Mandatory pre-check |

---

## 16. Ownership

| Responsibility | Owner |
|----------------|-------|
| Default tier definition | Architecture (ADR-018) |
| Jurisdiction approval | Compliance stakeholder |
| Domain purge implementation | Domain engineering team |
| Audit record policy | CORE platform team |
| Legal hold process | Legal/compliance — **TBD** |

---

## 17. Unresolved Questions

| ID | Question | Status |
|----|----------|--------|
| NFR-OQ-002 | Jurisdiction-specific retention matrix | OPEN |
| RET-OQ-001 | User erasure vs audit retention conflict | OPEN — legal input |
| RET-OQ-002 | Evidence blob storage retention SLA | OPEN — infrastructure phase |
| RET-OQ-003 | Event log retention vs audit reconstruction | OPEN — messaging implementation |

---

---

## 18. Policy Layers (Phase 6)

Retention policy operates at three distinct layers. Do not conflate them.

| Layer | Description | Authority | Example |
|-------|-------------|-----------|---------|
| **Engineering default** | ADR-018 tier baselines for schema design | Architecture | T3 = 2y online for alerts |
| **Configurable policy** | Organization-level overrides within approved bounds | ADMIN + COMP config | Extended case retention flag |
| **Jurisdiction-specific** | Legally mandated schedules | Compliance/legal approval required | **Not defined — NFR-OQ-002** |

Implementation MUST support configuration without code deploy for layer 2, subject to governance approval.

---

## 19. Retention Override Mechanism

| Aspect | Design |
|--------|--------|
| Storage | COMP or ADMIN configuration store (policy placeholder) |
| Scope | Per `organization_id` + data category |
| Precedence | Jurisdiction > org override > ADR-018 default |
| Audit | All override changes → CORE audit (NFR-AUD-001) |
| Validation | Override cannot reduce below jurisdiction minimum when jurisdiction policy exists |
| MVP | Defaults only — override UI/API deferred to implementation |

---

## 20. Approval Requirements for Policy Changes

| Change type | Required approver | Evidence |
|-------------|-------------------|----------|
| Engineering tier default change | Architecture + Compliance review | ADR + Phase report |
| Org-level override | Platform Administrator + Compliance (policy placeholder) | Audit record |
| Jurisdiction matrix entry | Compliance/Legal sign-off | External policy document reference |
| Purge execution in production | Operations + Compliance (restricted tiers) | Change ticket + dry-run log |
| Legal hold lift | Legal/compliance authorization | Hold registry update |

**No approval is claimed for any jurisdiction matrix in this repository.**

---

## 21. Operational vs Compliance Retention Distinction

| Category | Primary driver | Deletion posture |
|----------|----------------|------------------|
| Operational (alerts, assessments) | Investigation lookback, queue performance | Archive then purge after tier |
| Compliance (KYC, sanctions) | Regulatory evidence | Long retention; legal hold extends |
| Audit (CORE audit_records) | Accountability | No hard delete default |
| AI assistive | Model accountability | Anonymize where policy allows |
| Ephemeral (Redis) | Performance | TTL automatic |

RISK/ALERT/INVEST operational data (Phase 6 migrations): classified T3 (assessments, alerts) and T5 (cases, evidence metadata).

---

## 22. Unresolved Legal/Compliance Questions (Expanded)

| ID | Question | Owner | Blocks implementation? |
|----|----------|-------|------------------------|
| NFR-OQ-002 | Jurisdiction retention matrix | Compliance | No — defaults suffice for MVP schema |
| RET-OQ-001 | Erasure vs audit conflict | Legal | No — document conflict handling at implementation |
| RET-OQ-004 | Cross-border data residency | Legal/compliance | No for documentation phase |
| RET-OQ-005 | Minimum retention for unfinalized investigations | Compliance | No — use T5 default |
| RET-OQ-006 | Sanctions list data provider retention terms | Compliance/vendor | No — reference external provider policy |

---

## Related Documents

- [DataArchitecture.md](DataArchitecture.md)
- [InitialMigrationSpecifications.md](InitialMigrationSpecifications.md)
- [ArchitectureDecisionRecords.md](../03-architecture/ArchitectureDecisionRecords.md) — ADR-018
- [NonFunctionalRequirements.md](../02-requirements/NonFunctionalRequirements.md) — NFR-PRIV-002
