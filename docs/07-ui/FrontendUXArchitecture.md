# Frontend UX Architecture

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Frontend UX Architecture |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 10 |
| Last Updated | 2026-09-11 |
| Authority | UXPrinciples, DomainBoundaries, APIStandards, ADR-016/017, Phase 9 contracts |

---

## 1. Purpose

Define how the MVP frontend presents Sentinel AI capabilities **without owning domain lifecycles**. This is a design/architecture specification for later implementation planning (Phase 11+) — **not application code**.

A frontend engineer should understand responsibilities, boundaries, API consumption, AI presentation, real-time updates, and security/accessibility expectations from this document plus screen/workflow specs.

---

## 2. UX Architecture Principles

1. **Presentation layer only** — UI aggregates and displays; domains mutate via their APIs.
2. **AI assists; humans decide** — no silent lifecycle actions from AI panels.
3. **Evidence before conclusions** — scores and AI text sit next to source evidence.
4. **Least privilege** — nav and actions reflect AUTHZ; tenant context always visible.
5. **Graceful degradation** — AI/SSE failure must not block core triage.
6. **Auditability** — high-impact actions confirm + attribute actor.
7. **Operational density** — queues and tables over decorative dashboards.
8. **Contract fidelity** — only existing APIInventory / OpenAPI operations.

---

## 3. Frontend Responsibility Boundaries

| Frontend MAY | Frontend MUST NOT |
|--------------|-------------------|
| Call domain APIs per inventory | Invent APIs or events |
| Compose DASH BFF + domain reads | Persist authoritative alert/case/compliance state locally as SoT |
| Render AI recommendations with labels | Auto-approve, auto-close, auto-assign from AI |
| Enforce UX confirmation dialogs | Bypass server authorization |
| Cache UI state for UX | Cross-tenant data without audited admin mode |
| Subscribe to SSE for refresh hints | Treat SSE payloads as authoritative writes |

---

## 4. Domain-to-UI Ownership

| Domain | UI owns presentation of | Mutations via |
|--------|-------------------------|---------------|
| DASH | Workspace, widgets, queues views, SSE | API-DASH-* (read/interact/subscribe) |
| ALERT | Alert queues/detail, priority display | API-ALERT-* |
| RISK | Scores, explanations, rules admin | API-RISK-* |
| INVEST | Cases, evidence, timeline, notes | API-INVEST-* |
| COMP | Reviews, screening, audit packages | API-COMP-* |
| AI | Assist panels, recommendation view, prompts (admin) | API-AI-* |
| AUTH | Login, MFA, session, logout | API-AUTH-* |
| AUTHZ | Permission-gated UI | API-AUTHZ-* / session claims |
| USER / ORG | Identity admin screens | API-USER-*, API-ORG-* |
| ADMIN | Settings, integrations, provision orchestration, audit query | API-ADMIN-* |
| CORE | Feature flags / config surfaces where exposed | API-CORE-* |
| WALLET / SEC / REPORT / OPS | **Not in MVP nav** | V2 only |

---

## 5. Presentation vs Business Logic

Business rules (priority ownership, case close authority, compliance approve) live in domain services. Frontend:

- Validates UX form constraints (required fields, confirm text)
- Does not recompute risk scores
- Does not invent alert priority
- Surfaces server validation errors via ErrorResponse

---

## 6. API Consumption Model

| Pattern | Usage |
|---------|-------|
| DASH BFF (ADR-017) | Workspace entry, dashboard, queue projections, widgets |
| Domain REST | Authoritative reads/writes for ALERT, RISK, INVEST, COMP, AI, ADMIN, USER, ORG, AUTH |
| Async AI | `202` + poll `API-AI-004` for recommendations |
| Cursor pagination | ADR-014 “Load more” |
| Correlation | Propagate / display `requestId` / `correlationId` on errors |

**No invented global search API** — SCR-11 composes authorized list endpoints.

---

## 7. Authentication Boundary

- Unauthenticated: login (+ MFA) only
- Session via AUTH (`API-AUTH-001`–`005`)
- Bearer JWT on subsequent calls (OpenAPI `bearerAuth`)
- On `401` / session expiry: clear client session UI → login; preserve deep-link return where safe
- AUTH owns sessions — UI does not invent session storage semantics beyond AUTH contracts

---

## 8. Authorization Boundary

- Permissions from session / AUTHZ evaluation
- `PermissionGuard`: hide vs disable per UXPrinciples
- Denied mutations: show secure error; do not leak existence of unauthorized entities beyond server policy
- Role-based **navigation** filters primary nav items (see InformationArchitecture)

---

## 9. Tenant Isolation Expectations

- Active `organizationId` visible in shell (TenantSelector)
- All list/detail calls scoped by server tenant rules
- Cross-tenant switch: admin-only, auditable
- UI never mixes rows from multiple tenants in one queue without explicit admin mode

---

## 10. AI Interaction Boundary

See [AIInteractionPatterns.md](AIInteractionPatterns.md).

Mandatory visual/content distinction:

| Layer | Meaning |
|-------|---------|
| SYSTEM RESULT | Deterministic domain data (score, status, rule hit) |
| AI INTERPRETATION | Assistive summary/recommendation |
| SOURCE EVIDENCE | Linked artifacts / citations |
| HUMAN DECISION | Explicit user action (close, approve, assign) |

---

## 11. Error Handling Strategy

Align with ErrorHandling.md / APIStandards:

- Toast or inline ErrorState with code + message + requestId
- Retry for retryable failures
- Form validation: field-level + summary
- Partial page: widget/section errors without full blank page
- Never show stack traces or secrets

---

## 12. Loading Strategy

- Skeleton for tables/widgets (`aria-busy`)
- Button submitting state disables duplicate POSTs
- AI: explicit loading ≤ timeout (NFR-PERF-006 design target 10s) then timeout state
- Prefer progressive section load over single blocking splash

---

## 13–14. Real-Time Update Strategy (SSE MVP)

| Item | Specification |
|------|---------------|
| Mechanism | **SSE** via `API-DASH-007` `GET /v1/workspace/subscriptions/{channel}` |
| Channels | `workspace`, `alerts`, `cases`, `queues` (OpenAPI enum) |
| Auth | bearerAuth |
| Behavior | Hint to refetch authoritative REST; do not treat event stream as SoT write |
| Fallback | Poll `API-DASH-003` / list APIs if SSE fails (ADR-016) |
| WebSocket | **V2** — not MVP |
| Focus | SSE updates must not steal focus (Accessibility) |

---

## 15. State Management Principles

- Server state is authoritative after mutations
- Client cache is UX convenience; invalidate on mutation success and SSE hint
- Optimistic UI only where safe and reversible; never for compliance approve / case close without confirm
- See [UXStateModel.md](UXStateModel.md)

---

## 16. Form Handling Principles

- Visible labels; required indicated
- Disable submit while in-flight
- Idempotency-Key on mutating POSTs where inventory marks Idem
- Destructive actions: ConfirmationDialog with consequence text
- Retain field values on recoverable API error

---

## 17. Audit-Sensitive Interaction Rules

Require confirmation + clear actor copy for:

- Close alert / close case
- Compliance approve / reject / sanctions disposition
- Role assignment / user deactivate
- Platform setting / integration changes

Display post-action audit affordance where APIs allow (`API-ADMIN-005`, entity timelines).

---

## 18. Navigation Rules

- Primary nav role-filtered
- Breadcrumbs for entity hierarchy
- Deep links to alert/case/assessment IDs
- V2 domains absent from MVP sidebar
- See InformationArchitecture.md

---

## 19. V2 Isolation

Do not ship MVP routes for SEC, REPORT, OPS, WALLET workspaces. Feature flags may hide stubs; default **off**. Label any future references “V2”.

---

## 20. Accessibility Requirements

WCAG 2.1 AA **design target** (not certified). See Accessibility.md.

---

## 21. Responsive Behavior

| Breakpoint (design target) | Behavior |
|----------------------------|----------|
| ≥1280px | Split queue/detail |
| 768–1279px | Collapsible sidebar |
| <768px | Single column; stack detail |

---

## 22. Observability Expectations (Frontend)

- Surface correlation/request IDs on failures
- Log widget interactions via `API-DASH-005` where required
- Do not send PII to third-party analytics without policy (future)
- Client error boundaries → ErrorState, not silent fail

---

## 23. Security Considerations

See [UXSecuritySpecification.md](UXSecuritySpecification.md). Design-level: XSS-safe rendering of AI/evidence text, no secret display, CSRF posture per APIStandards session model, least privilege.

---

## 24. Performance Expectations (Design Targets)

| Area | Target reference |
|------|------------------|
| Queue interactions | NFR-PERF-003 |
| AI assist | NFR-PERF-006 (~10s then fallback) |
| Search compose | NFR-PERF-005 simulation target |
| Initial dashboard | Progressive widgets; avoid blocking all on one API |

Library choices deferred to Phase 11 — no framework lock-in in Phase 10.

---

## Related Documents

- [UXPrinciples.md](UXPrinciples.md)
- [InformationArchitecture.md](InformationArchitecture.md)
- [AIInteractionPatterns.md](AIInteractionPatterns.md)
- [UXSecuritySpecification.md](UXSecuritySpecification.md)
- [UXStateModel.md](UXStateModel.md)
- [APIInventory.md](../06-api/APIInventory.md)
