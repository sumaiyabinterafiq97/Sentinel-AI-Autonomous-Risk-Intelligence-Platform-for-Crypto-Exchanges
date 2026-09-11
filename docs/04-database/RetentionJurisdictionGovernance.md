# Retention Jurisdiction Governance

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Retention Jurisdiction Governance |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 7 |
| Last Updated | 2026-09-11 |
| Related open question | **NFR-OQ-002** — OPEN |
| Authority | ADR-018; DataRetention.md; NFR-PRIV-002 |

---

## Purpose

Define the **decision framework** for jurisdiction-specific data retention overrides.

This document does **not**:

- provide legal advice
- claim compliance with any jurisdiction
- invent specific statutory retention periods
- approve production retention policy

Exact jurisdiction durations remain **TBD** until compliance/legal stakeholders provide approved input.

---

## 1. Policy Layers (Reaffirmed)

| Layer | Owner | Status |
|-------|-------|--------|
| Engineering defaults (ADR-018 tiers) | Architecture | Documented |
| Organization configurable overrides | ADMIN + COMP | Mechanism defined; MVP defaults only |
| Jurisdiction-specific schedules | Compliance / Legal | **OPEN (NFR-OQ-002)** |

Precedence: **Jurisdiction > Org override > Engineering default**

---

## 2. Default Retention Tiers (Reference)

See [DataRetention.md](DataRetention.md) §3 and ADR-018.

| Tier | Online (design target) | Archive |
|------|------------------------|---------|
| T0 Ephemeral | TTL minutes–hours | None |
| T1 Telemetry | 90 days | Optional |
| T2 Logs | 180 days | 1 year |
| T3 Operational (alerts, risk) | 2 years | 5 years |
| T4 Identity/audit | 2 years | 7 years |
| T5 Investigation | 7 years | Hold extends |
| T6 Compliance | 7 years | Hold extends |
| T7 AI assistive | 1 year | 2 years |

These are **simulation/design targets**, not measured production achievements or legal mandates.

---

## 3. Jurisdiction Override Mechanism

| Aspect | Specification |
|--------|---------------|
| Configuration store | COMP/ADMIN policy configuration (placeholder until implementation) |
| Scope keys | `organization_id` + `jurisdiction_code` + `data_category` |
| Fields | `min_retention_days`, `max_retention_days`, `archive_days`, `purge_allowed`, `legal_hold_default`, `effective_from`, `policy_version` |
| Validation | Override cannot reduce below approved jurisdiction minimum when jurisdiction policy exists |
| Audit | All policy changes → CORE audit (NFR-AUD-001) |
| Versioning | Policy versions immutable once effective; new version supersedes |

**MVP:** Ship with ADR-018 defaults only. Jurisdiction matrix empty until approved.

---

## 4. Decision Ownership

| Decision | Owner | Approver |
|----------|-------|----------|
| Engineering default tier change | Data Architect | Architecture + Compliance review |
| Org-level override within bounds | Platform Admin | Compliance (policy placeholder) |
| Jurisdiction matrix entry | Compliance | Legal + Compliance sign-off |
| Legal hold apply/lift | Legal/Compliance | Documented authorization |
| Production purge (restricted tiers) | Operations | Compliance dual-control placeholder |

---

## 5. Approval Workflow for Jurisdiction Policy

```text
1. Compliance drafts jurisdiction schedule (external legal input)
2. Legal reviews / marks conflicts
3. Data Architect maps schedule → data categories / tiers
4. Security reviews tenant isolation + deletion safety
5. Product Owner acknowledges MVP impact
6. Record approval in GovernanceDecisions + policy version
7. Effective date set; migrations/jobs updated if needed
```

Until step 6 completes: **NFR-OQ-002 remains OPEN**.

---

## 6. Data Classification Mapping

| Classification | Retention posture |
|----------------|-------------------|
| Restricted (PII, compliance) | Prefer T5/T6; legal hold aware |
| Confidential (alerts, risk) | T3 defaults |
| Internal (ops metrics, admin) | T1–T4 |
| Public | Minimal |

---

## 7. Deletion Eligibility

| Condition | Eligible? |
|-----------|-----------|
| Past online + archive window; no legal hold | Yes (after dry-run) |
| Legal hold active | No |
| Unresolved investigation (open case) | No automatic purge of T5 case data |
| Audit records (CORE) | Hard delete prohibited by default |
| Jurisdiction matrix missing | Use engineering default; do not invent periods |

---

## 8. Legal Hold Concept

| Aspect | Policy |
|--------|--------|
| Trigger | Legal/compliance notification process (placeholder) |
| Effect | Suspend automated purge |
| Scope | Record, case, user, or organization |
| Registry | External hold registry or `legal_hold` flag — design TBD |
| Lift | Documented authorization required |

---

## 9. Conflicting Jurisdiction Rules

When an organization operates across jurisdictions:

1. Apply **most restrictive** overlapping requirement for the affected data category
2. Document conflict in policy version notes
3. Escalate unresolved conflicts to Legal — do not auto-resolve by engineering preference

**Status:** Framework only — no jurisdiction matrix entries exist in-repo.

---

## 10. Tenant-Specific Configuration

- Overrides scoped by `organization_id`
- Cross-tenant policy application prohibited except audited platform super-admin
- Test tenants may use accelerated TTL in non-production only

---

## 11. Policy Versioning and Effective Dates

| Field | Requirement |
|-------|-------------|
| `policy_version` | Monotonic integer or semver |
| `effective_from` | UTC timestamp |
| `supersedes` | Prior version ID |
| `approved_by` | Role + name (human) |
| `evidence_ref` | Link to external legal memo / ticket |

---

## 12. Migration Implications

| Implication | Guidance |
|-------------|----------|
| Schema design | Support retention class metadata / partition keys |
| COMP/INVEST tables | Align to T5/T6 design targets |
| AI tables | T7; anonymize where policy allows |
| No SQL in Phase 7 | Logical expectations only |

---

## 13. Data Minimization

Collect and retain only data justified by FDS/FRS purpose. Speculative retention beyond ADR-018 without business purpose is prohibited.

---

## 14. Explicit Open Items (NFR-OQ-002)

| Code | Item | Status |
|------|------|--------|
| JUR-TBD-001 | EU/EEA retention schedule | TBD — legal input |
| JUR-TBD-002 | US federal / state schedules | TBD — legal input |
| JUR-TBD-003 | APAC / other operating jurisdictions | TBD — legal input |
| JUR-TBD-004 | Cross-border transfer retention interaction | TBD — legal input |
| JUR-TBD-005 | Erasure vs audit conflict resolution (RET-OQ-001) | TBD — legal input |

**No jurisdiction durations are approved in this repository.**

---

## Related Documents

- [DataRetention.md](DataRetention.md)
- [ArchitectureDecisionRecords.md](../03-architecture/ArchitectureDecisionRecords.md) — ADR-018
- [NonFunctionalRequirements.md](../02-requirements/NonFunctionalRequirements.md) — NFR-PRIV-002, NFR-OQ-002
- [GovernanceDecisions.md](../00-project/GovernanceDecisions.md)
