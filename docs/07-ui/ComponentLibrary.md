# Component Library

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Component Library |
| Version | 0.2 (Draft) |
| Status | Draft — Phase 10 |
| Last Updated | 2026-09-11 |
| Note | Documentation-only — **no React implementation** |

---

## Purpose

Enterprise components for MVP screens. Specs include purpose, data, states, a11y, permissions, audit, domain ownership, reuse.

---

## Component Principles

1. Dense operational patterns
2. Color never sole status channel
3. Permission-aware (hide vs disable)
4. AI always labeled assistive
5. Map to owning domain APIs
6. No invented backend capabilities

---

## Shell & Navigation

### ApplicationShell

| Field | Value |
|-------|-------|
| Purpose | App frame: sidebar + top + main |
| Inputs | Nav model, tenant, user, children |
| States | authenticated, session-expired |
| A11y | Landmarks; skip link |
| Domain | DASH presentation |
| Reuse | All authenticated SCR-* |

### Sidebar / TopNavigation / Breadcrumbs / TenantSelector / UserMenu / Search

| Component | Purpose | Permissions | Notes |
|-----------|---------|-------------|-------|
| Sidebar | Primary nav | RBAC filter | No V2 items |
| TopNavigation | Brand, search, notices, user | Session | |
| Breadcrumbs | Entity hierarchy | Read parent | |
| TenantSelector | Active org | Org switch policy | Audited switch |
| UserMenu | Profile, logout | API-AUTH-002 | |
| Search | Scoped query UI | Domain read ∩ | Composes list APIs |

---

## Data Display

### DataTable / Pagination / FilterPanel / StatusFilter / DateRangeSelector / Tabs

| Component | Purpose | States | A11y |
|-----------|---------|--------|------|
| DataTable | Operational lists | loading, empty, error | Column headers; keyboard |
| Pagination | Cursor load-more | hasMore, end, error | Labeled button |
| FilterPanel | Queue filters | applied, cleared | Labeled controls |
| StatusFilter | Status facet | — | Not color-only |
| DateRangeSelector | Time bounds | invalid range | |
| Tabs | Section switch | selected | Keyboard tabs |

### AlertBadge / RiskScore / RiskLevelIndicator / CaseStatus / ConfidenceIndicator

| Component | Domain | Boundary |
|-----------|--------|----------|
| AlertBadge | ALERT | Status text+icon |
| RiskScore | RISK | Does not set alert priority |
| RiskLevelIndicator | RISK | Shape + label |
| CaseStatus | INVEST | |
| ConfidenceIndicator | AI | Never authoritative |

---

## Investigation & Evidence

### EvidencePanel / Timeline / InvestigationTimeline / CaseAssignment / ActivityLog / AuditLog / SourceCitation

| Component | Purpose | API / data | Audit |
|-----------|---------|------------|-------|
| EvidencePanel | Evidence metadata + classification | API-INVEST-007 / detail | Attach audited |
| Timeline / InvestigationTimeline | Chronology | API-INVEST-008 / alert detail | Display |
| CaseAssignment | Assign control | API-INVEST-006 | Yes |
| ActivityLog | Entity activity | Domain embeds | Display |
| AuditLog | Admin/platform audit | API-ADMIN-005 | Read |
| SourceCitation | AI/system sources | Recommendation payload | Display |

**SEC does not consume EvidenceAttached** — UI must not imply SEC MVP evidence feed.

---

## AI Components

### AIExplanationPanel / AIRecommendationPanel

| Field | Value |
|-------|-------|
| Purpose | Assistive explanation / recommendations |
| States | idle, loading, completed, partial, failed, degraded, timeout |
| Interactions | Copy ID; open sources; **no lifecycle buttons** |
| A11y | complementary; “AI Assistive” label |
| Permissions | `ai:*` assist |
| APIs | API-AI-001–004 |
| Audit | Correlatable assist requests |

---

## Feedback & Forms

### EmptyState / LoadingState / ErrorState / PermissionDeniedState / ConfirmationDialog / Toast/Notification / FormField / Modal/Drawer

| Component | Purpose | Key rule |
|-----------|---------|----------|
| EmptyState | Zero data + next action | |
| LoadingState | Skeleton / aria-busy | |
| ErrorState | Retry + requestId | No secrets |
| PermissionDeniedState | 403 UX | Secure |
| ConfirmationDialog | High-impact confirm | Focus trap |
| Toast/Notification | Transient feedback | Not audit SoT |
| FormField | Labeled inputs | Errors via aria-describedby |
| Modal/Drawer | Overlay detail | Escape rules per DesignSystem |

### PermissionGuard

Hide vs disable-with-tooltip; AUTHZ / session claims; API-AUTHZ-001 or claims.

---

## Composition Patterns

| Pattern | Components |
|---------|------------|
| App chrome | ApplicationShell + Sidebar + TopNavigation + TenantSelector + UserMenu |
| Alert queue | FilterPanel + DataTable + Pagination + states |
| Alert detail | StatusBadge + RiskScore + Timeline + AIExplanationPanel + ConfirmationDialog |
| Case detail | CaseStatus + CaseAssignment + EvidencePanel + Timeline + AI panels |
| Compliance | FormField + ConfirmationDialog + PermissionGuard |
| Admin | FormField + AuditLog + PermissionGuard |

---

## Coverage Checklist (Phase 10)

ApplicationShell, Sidebar, TopNavigation, TenantSelector, UserMenu, Search, DataTable, Pagination, FilterPanel, AlertBadge, RiskScore, RiskLevelIndicator, ConfidenceIndicator, EvidencePanel, Timeline, InvestigationTimeline, CaseStatus, CaseAssignment, ActivityLog, AuditLog, AIExplanationPanel, AIRecommendationPanel, SourceCitation, EmptyState, LoadingState, ErrorState, PermissionDeniedState, ConfirmationDialog, Toast/Notification, FormField, DateRangeSelector, StatusFilter, Modal/Drawer, Tabs, Breadcrumbs, PermissionGuard, StatusBadge, AlertTable (specialized DataTable), TransactionSummary (risk context).

---

## Open Questions

| ID | Question | Status |
|----|----------|--------|
| CL-OQ-001 | Bulk alert actions in MVP? | Default **no** |
| CL-OQ-002 | Global SearchBar backend | **CLOSED** — compose domain list GETs; no global search API in MVP (GD-007) |

---

## Related Documents

- [DesignSystem.md](DesignSystem.md)
- [DashboardScreens.md](DashboardScreens.md)
- [AIInteractionPatterns.md](AIInteractionPatterns.md)
- [Accessibility.md](Accessibility.md)
- [../00-project/Phase12M11OpenQuestionDecisions.md](../00-project/Phase12M11OpenQuestionDecisions.md)
