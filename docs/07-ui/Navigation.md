# Navigation

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Navigation |
| Version | 0.2 (Draft) |
| Status | Draft — Phase 10 |
| Last Updated | 2026-09-11 |
| Note | Wayfinding summary; **authoritative IA is InformationArchitecture.md** |

---

## Navigation Goals

1. Fast access to persona-relevant work queues
2. Domain ownership visible in labels (Alerts ≠ Risk ≠ Cases)
3. Least-privilege nav (hide unauthorized modules)
4. No V2 SEC/REPORT/OPS/WALLET in MVP sidebar
5. AI assist is contextual, not a lifecycle owner in nav

---

## Primary Navigation

See [InformationArchitecture.md](InformationArchitecture.md) §3–4.

MVP items: Overview · Alerts · Investigations · Risk · Compliance · Search · Administration · Identity — **filtered by role**.

---

## Secondary Navigation

See InformationArchitecture.md §5 (Risk assessments/rules, Compliance packages, Admin settings/prompts/audit, Identity users/orgs/roles).

---

## Deep Links

| Pattern | Example concept | Permission |
|---------|-----------------|------------|
| Alert | `/alerts/{alertId}` | `alert:alert:read` |
| Case | `/investigations/cases/{caseId}` | `invest:case:read` |
| Assessment | `/risk/assessments/{assessmentId}` | `risk:assessment:read` |
| Compliance review | `/compliance/.../{id}` | matching `comp:*` |
| Post-login return | Safe GET deep links only | After AUTH |

Invalid/unauthorized IDs → Not found or PermissionDenied (no cross-tenant leak).

---

## Permissions and Visibility

| Rule | Behavior |
|------|----------|
| No permission for module | Hide nav item |
| Read without write | Show module; disable mutating controls |
| Tenant | All nav operates in active organization context |
| V2 modules | Not in MVP nav |

---

## Open Questions

| ID | Question | Status |
|----|----------|--------|
| NAV-OQ-001 | Exact claim→nav mapping table at implementation | Deferred Phase 11 (AUTHZ binding design) |
| NAV-OQ-002 | Whether Compliance Officers see Alerts by default | **Permission-driven** — not assumed |

---

## Related Documents

- [InformationArchitecture.md](InformationArchitecture.md)
- [UXSecuritySpecification.md](UXSecuritySpecification.md)
- [Personas.md](../01-product/Personas.md)
