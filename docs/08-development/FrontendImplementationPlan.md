# Frontend Implementation Plan

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Frontend Implementation Plan |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 11 |
| Last Updated | 2026-09-11 |
| Authority | Phase 10 UX docs, OpenAPI, ADR-016/017 |

---

## Purpose

Plan React SPA implementation for SCR-00–15 / WF-1–8. **No React/TypeScript application code in Phase 11.**

---

## Implementation Direction (Candidate)

| Technology | Role |
|------------|------|
| React + TypeScript | UI |
| Vite | Bundler |
| Tailwind | Styling tokens mapped from DesignSystem |
| TanStack Query | Server state |
| React Hook Form + Zod | Forms/validation aligned to API schemas |

Stack confirmation: **P11-OQ-STACK-002** (ADR-007).

---

## Application Structure (Planned)

```text
sentinel-web/
  src/
    app/                 # shell, routes, providers
    features/
      auth/              # SCR-00
      workspace/         # SCR-01, SSE
      alerts/            # SCR-02–03
      investigations/    # SCR-04–05
      risk/              # SCR-06–07
      compliance/        # SCR-08–10
      search/            # SCR-11
      admin/             # SCR-12–14
      ai/                # SCR-15 panels
    shared/ui/           # design-system components
    shared/api/          # OpenAPI client
```

---

## Feature → Screen Map

| Feature | Screens | Primary APIs |
|---------|---------|--------------|
| Auth | SCR-00 | API-AUTH-* |
| Workspace | SCR-01 | API-DASH-001/002/004/005/007 |
| Alerts | SCR-02–03 | API-ALERT-*, API-RISK-003, API-AI-002 |
| Investigations | SCR-04–05 | API-INVEST-*, API-AI-001/003 |
| Risk | SCR-06–07 | API-RISK-* |
| Compliance | SCR-08–10 | API-COMP-* |
| Search | SCR-11 | Composed list GETs |
| Admin/Identity | SCR-12–14 | API-ADMIN/USER/ORG/AUTHZ/CORE/AI-005 |
| AI panels | SCR-15 | API-AI-001–004 |

**Excluded MVP:** SEC, REPORT, OPS, WALLET routes.

---

## Cross-Cutting UX Implementation

| Concern | Plan |
|---------|------|
| Shell / nav | RBAC-filtered per InformationArchitecture |
| Tables/filters/pagination | Cursor load-more (ADR-014) |
| SSE | EventSource → API-DASH-007; poll fallback |
| States | UXStateModel (loading/empty/error/denied/degraded) |
| AI | AIInteractionPatterns four-layer distinction |
| A11y | Accessibility.md design targets |
| Permissions | PermissionGuard; never client-only security |

---

## Workflow Implementation Order

1. SCR-00 + shell
2. SCR-01 + SSE stub
3. WF-1 alerts
4. WF-2 risk
5. WF-3/7 investigations + AI panels
6. WF-4/5 compliance
7. WF-6 admin
8. SCR-11 search compose
9. A11y pass on WF-1/3/4/7

---

## Verification

Component tests; workflow tests; a11y checks; API contract consumer tests — TestingImplementationPlan.md.

---

## Open Questions

| ID | Item |
|----|------|
| UX-OQ-COMP-LIST | COMP queue without list GET |
| UX-OQ-SEARCH | No global search API |
| P11-OQ-STACK-002 | Confirm React stack ADR |

---

## Related Documents

- [../07-ui/FrontendUXArchitecture.md](../07-ui/FrontendUXArchitecture.md)
- [../07-ui/DashboardScreens.md](../07-ui/DashboardScreens.md)
- [../07-ui/MVPWorkflows.md](../07-ui/MVPWorkflows.md)
