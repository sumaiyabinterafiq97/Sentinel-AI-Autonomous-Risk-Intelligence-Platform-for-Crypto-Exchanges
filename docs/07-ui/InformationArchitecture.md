# Information Architecture

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Information Architecture |
| Version | 0.2 (Draft) |
| Status | Draft — Phase 10 |
| Last Updated | 2026-09-11 |
| Authority | Personas, PRD, DASH FRs, APIInventory, Phase 9 contracts |

---

## Purpose

MVP application shell, navigation, and screen map. **Not implementation code.** Supersedes skeleton [Navigation.md](Navigation.md) for Phase 10 wayfinding.

---

## 1. Application Shell

```text
┌─ TopNavigation ─────────────────────────────────────────┐
│ Brand │ TenantSelector │ GlobalSearch │ Notices │ UserMenu │
├─ Sidebar ──────────────┬─ Main ─────────────────────────┤
│ Primary nav (RBAC)     │ Breadcrumbs                     │
│                        │ Page content (SCR-*)            │
│                        │ Complementary: AI panel (opt.)  │
└────────────────────────┴────────────────────────────────┘
```

| Shell element | Purpose | API / note |
|---------------|---------|------------|
| Authentication entry | Login / MFA | API-AUTH-001, API-AUTH-005 |
| TenantSelector | Active organization context | Session + ORG read as permitted |
| UserMenu | Profile, logout | API-AUTH-004, API-AUTH-002 |
| GlobalSearch | Scoped entity search | Composes list APIs — **no invented search API** |
| Notices | Toasts / SSE connection status | Local UX + API-DASH-007 health |
| Sidebar | Primary navigation | Role-filtered |
| System status | CORE health if exposed | API-CORE health (public health already in OpenAPI) — ops deep health is **V2/OPS** |

---

## 2. MVP Application Structure

```text
Sentinel AI (authenticated workspace)
├── Overview              [DASH] SCR-01
├── Alerts                [ALERT] SCR-02, SCR-03
├── Investigations        [INVEST] SCR-04, SCR-05
├── Risk                  [RISK] SCR-06, SCR-07
├── Compliance            [COMP] SCR-08, SCR-09, SCR-10
├── Search                [composed] SCR-11
├── Administration        [ADMIN/CORE] SCR-13, SCR-14, AI prompts if permitted
└── Identity              [USER/ORG/AUTHZ] SCR-12
```

**Excluded from MVP sidebar (V2):** Security (SEC), Reports (REPORT), Operations (OPS), Wallet (WALLET).

**AI Assist** is **contextual** (SCR-15 embedded) — not a top-level fake “AI owns work” nav item. Optional admin “Prompts” under Administration.

---

## 3. Primary Navigation (MVP)

| Nav item | Route concept | Domain | Entry APIs |
|----------|---------------|--------|------------|
| Overview | `/workspace` | DASH | API-DASH-001, 002, 004, 007 |
| Alerts | `/alerts` | ALERT | API-DASH-003, API-ALERT-001 |
| Investigations | `/investigations` | INVEST | API-INVEST-001, API-DASH-003 |
| Risk | `/risk` | RISK | API-RISK-002 |
| Compliance | `/compliance` | COMP | API-COMP-* entry screens |
| Search | `/search` | composed | Domain list GETs |
| Administration | `/admin` | ADMIN/CORE | API-ADMIN-*, API-CORE-* |
| Identity | `/identity` | USER/ORG/AUTHZ | API-USER-*, API-ORG-*, API-AUTHZ-* |

Real-time: `API-DASH-007` on Overview, Alerts, Investigations queues.

---

## 4. Role-Based Navigation

Permissions are authoritative; personas are **design defaults**.

### Risk Analyst

| Visible | Hidden by default |
|---------|-------------------|
| Overview, Alerts, Risk, Investigations, Search | Administration, Identity (unless granted), Compliance (unless granted) |

### Compliance Officer

| Visible | Hidden by default |
|---------|-------------------|
| Overview, Compliance, Search | Risk rules admin write; full Admin |
| Alerts / Investigations | Only if `alert:*` / `invest:*` granted |

### Security Engineer

| Visible | Note |
|---------|------|
| Shared MVP areas if granted (Overview, Alerts, Investigations) | **No SEC sidebar in MVP** — SEC is V2 |
| Account-compromise work uses INVEST + AUTH context | Not a SEC MVP module |

### Platform Administrator

| Visible | Note |
|---------|------|
| Administration, Identity, Audit | Settings, integrations, provision |
| Operational nav | Only if also granted analyst permissions |
| OPS/REPORT | **V2** — not MVP default |

---

## 5. Secondary Navigation

| Area | Secondary items |
|------|-----------------|
| Risk | Assessments list → Detail; Rules (write-gated) |
| Compliance | Reviews queue; Audit packages |
| Administration | Settings; Integrations; AI Prompts (`API-AI-005`); Audit |
| Identity | Users; Organizations; Roles/bindings (AUTHZ) |
| Alerts / Cases | List ↔ Detail via split view or stacked |

---

## 6. Global Actions & Notifications

| Action | Behavior |
|--------|----------|
| Logout | API-AUTH-002 |
| Refresh queues | Manual + SSE |
| Toast notifications | Success/error/warning; not a substitute for audit |
| SSE status | Indicator when degraded → poll fallback |

---

## 7. Screen Catalog (Index)

Authoritative detail: [DashboardScreens.md](DashboardScreens.md) (SCR-01–SCR-15 + SCR-00 auth).

| ID | Name |
|----|------|
| SCR-00 | Login / MFA |
| SCR-01 | Operations Overview |
| SCR-02 | Alert Queue |
| SCR-03 | Alert Detail |
| SCR-04 | Case List |
| SCR-05 | Case Detail |
| SCR-06 | Risk Assessment Detail |
| SCR-07 | Risk Rules Administration |
| SCR-08 | Compliance Review Queue |
| SCR-09 | Compliance Review Detail |
| SCR-10 | Audit Package Preparation |
| SCR-11 | Search / Intelligence |
| SCR-12 | User / Organization Administration |
| SCR-13 | Platform Settings & Integrations |
| SCR-14 | Admin Audit Review |
| SCR-15 | AI Assistance Surfaces (embedded) |

---

## 8. V2 Placeholders (Not MVP)

| Screen | Domain | Label |
|--------|--------|-------|
| Security Signals | SEC | V2 |
| Reports / KPI | REPORT | V2 |
| Platform Ops | OPS | V2 |
| Wallet Intelligence | WALLET | V2 |

---

## Related Documents

- [Navigation.md](Navigation.md) — points here
- [DashboardScreens.md](DashboardScreens.md)
- [MVPWorkflows.md](MVPWorkflows.md)
- [FrontendUXArchitecture.md](FrontendUXArchitecture.md)
- [UXSecuritySpecification.md](UXSecuritySpecification.md)
