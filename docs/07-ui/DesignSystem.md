# Design System

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Design System |
| Version | 0.2 (Draft) |
| Status | Draft — Phase 10 |
| Last Updated | 2026-09-11 |
| Note | Documentation-only — no CSS/framework lock-in |

---

## Purpose

Enterprise design system for Sentinel AI operational interfaces. Prioritizes clarity, density, auditability, and accessibility over decorative UI.

---

## 1. Design Goals

| Goal | Rationale |
|------|-----------|
| Operational clarity | Analysts scan queues under time pressure |
| Evidence visibility | Risk and case decisions require source context |
| Status unambiguity | Color + label + icon — never color alone |
| Consistent density | More rows visible without clutter |
| Accessible defaults | WCAG-oriented patterns (see Accessibility.md) |
| AI distinction | Assistive content visually separated from authoritative data |

---

## 2. Typography

| Token | Usage | Guideline |
|-------|-------|-----------|
| `--font-sans` | UI default | System or enterprise sans stack — TBD at implementation |
| `--text-xs` | Metadata, timestamps | 12px equivalent |
| `--text-sm` | Table cells, labels | 14px equivalent |
| `--text-base` | Body, form inputs | 16px equivalent |
| `--text-lg` | Section headers | 18px equivalent |
| `--text-xl` | Page titles | 20–24px equivalent |
| `--font-mono` | IDs, correlation IDs, rule codes | Monospace for copy-paste |

**Line height:** 1.4–1.5 for body; 1.2 for dense tables

---

## 3. Spacing Scale

| Token | Value | Usage |
|-------|-------|-------|
| `--space-1` | 4px | Tight inline |
| `--space-2` | 8px | Icon gaps |
| `--space-3` | 12px | Form field padding |
| `--space-4` | 16px | Card padding |
| `--space-6` | 24px | Section gaps |
| `--space-8` | 32px | Page margins |

---

## 4. Layout

| Pattern | Specification |
|---------|---------------|
| App shell | Fixed sidebar nav + top bar (org context, user menu) |
| Content area | Max readable width for forms; full width for tables |
| Split view | Queue list left (40%) + detail right (60%) on wide screens |
| Stacked mobile | Queue → detail navigation on narrow viewports |

---

## 5. Navigation

| Element | Behavior |
|---------|----------|
| Primary nav | 7 MVP items per InformationArchitecture.md |
| Active state | Bold + indicator bar |
| Breadcrumbs | Entity hierarchy: Alerts > ALR-xxx > Linked Case |
| Org switcher | Admin only; audited |

---

## 6. Tables

Operational primary component for alerts, cases, compliance queues.

| Feature | Requirement |
|---------|-------------|
| Sort | Column sort where API supports |
| Filter | Status, assignee, date range |
| Pagination | Cursor-based — "Load more" pattern |
| Row actions | Kebab menu or inline buttons per permissions |
| Selection | Bulk only if FR authorizes |
| Empty | Illustration optional — clear text required |
| Loading | Skeleton rows |
| Density | Compact default; comfortable toggle optional |

---

## 7. Cards

| Usage | Content |
|-------|---------|
| Dashboard widgets | Metric + trend + link |
| Risk summary | Score, level badge, explanation excerpt |
| AI recommendation | Distinct border + "Assistive" label |

---

## 8. Forms

| Pattern | Rule |
|---------|------|
| Labels | Always visible — no placeholder-only labels |
| Errors | Inline below field + summary on submit |
| Required | Asterisk + aria-required |
| Destructive | Confirm dialog with consequence text |
| Idempotency | Submit buttons disabled during in-flight POST |

---

## 9. Alerts (UI Components)

In-app notifications — distinct from ALERT domain entity.

| Type | Usage |
|------|-------|
| Info | Neutral system messages |
| Success | Action confirmed |
| Warning | Degraded mode, AI unavailable |
| Error | API failure with retry |

---

## 10. Badges and Status Indicators

### Alert / case status

| Status | Visual |
|--------|--------|
| open | Neutral badge |
| assigned | Info badge |
| closed | Muted badge |

### Risk severity

| Level | Color name | Label always shown |
|-------|------------|-------------------|
| low | green | "Low" |
| medium | amber | "Medium" |
| high | orange | "High" |
| critical | red | "Critical" |

**Accessibility:** Icon shape differs per level — not color-only

---

## 11. Dialogs and Drawers

| Component | Usage |
|-----------|-------|
| Modal dialog | Confirm close alert/case; compliance decision |
| Drawer | Evidence detail, AI explanation panel |
| Focus trap | Required in modals |
| Escape | Closes non-destructive; destructive requires confirm |

---

## 12. Timelines

Case and alert activity timelines:

- Chronological descending default
- Actor + action + timestamp
- System events distinguished from user events
- Correlation ID copy action for support

**FR:** INVEST-FR timeline requirements

---

## 13. Evidence Panels

| Section | Content |
|---------|---------|
| Header | Evidence type, classification badge |
| Body | Reference link, metadata JSON formatted |
| Actions | Open source (authorized), attach another |
| Classification | restricted/confidential labels visible |

---

## 14. AI Explanation Panels

| Requirement | Implementation guidance |
|-------------|------------------------|
| Label | "AI Assistive Summary" header always visible |
| Provenance | Link to recommendation ID, prompt version |
| Disclaimer | "Requires analyst verification" |
| Loading | Progress ≤10s then timeout message |
| Degraded | "AI assist unavailable" with manual path |
| No auto-action | No approve/close buttons in AI panel |

---

## 15. Responsive Behavior

| Breakpoint | Behavior |
|------------|----------|
| ≥1280px | Split queue/detail |
| 768–1279px | Collapsible sidebar |
| <768px | Single column; simplified tables |

---

## 16. Notification Conventions

| Type | Persistence | Use |
|------|-------------|-----|
| Toast success | Short | Mutation confirmed |
| Toast error | Until dismiss | Include requestId |
| Inline banner | Until resolved | Degraded AI/SSE |
| Modal | Blocking | Destructive confirm only |

Toasts are **not** the audit system of record.

---

## 17. Keyboard Interaction (Design Target)

| Pattern | Behavior |
|---------|----------|
| Tab | All interactive controls |
| Enter/Space | Activate buttons/rows (where documented) |
| Escape | Close non-destructive modal/drawer |
| Shortcuts | Optional; must not trap assistive tech |

Full rules: [Accessibility.md](Accessibility.md).

---

## 18. Focus Behavior

| Event | Focus |
|-------|-------|
| Route change | Main `h1` |
| Open modal | First focusable |
| Close modal | Trigger control |
| SSE refresh | **Do not** move focus |

---

## 19. Color-Independent Status (Mandatory)

Risk, severity, errors, success, warning, and permissions MUST use **text and/or icon shape** in addition to any color. See §10 and Accessibility.md §4.

---

## 20. Content Layer Tokens (AI vs System)

| Token / pattern | Usage |
|-----------------|-------|
| `--surface-system` | Deterministic domain data |
| `--surface-ai` | Assistive panels (distinct border + label) |
| `--surface-evidence` | Citations / attachments |
| `--surface-decision` | Human action controls |

See [AIInteractionPatterns.md](AIInteractionPatterns.md).

---

## 21. Open Questions

| ID | Question |
|----|----------|
| DS-OQ-001 | Dark mode required for MVP? — Default light; dark deferred |
| DS-OQ-002 | Brand color palette — enterprise neutral TBD |

---

## Related Documents

- [Accessibility.md](Accessibility.md)
- [ComponentLibrary.md](ComponentLibrary.md)
- [FrontendUXArchitecture.md](FrontendUXArchitecture.md)
- [UXPrinciples.md](UXPrinciples.md)
- [AIInteractionPatterns.md](AIInteractionPatterns.md)
