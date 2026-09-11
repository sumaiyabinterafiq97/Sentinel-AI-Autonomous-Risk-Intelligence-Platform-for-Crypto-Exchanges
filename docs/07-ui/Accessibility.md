# Accessibility Requirements

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Accessibility Requirements |
| Version | 0.2 (Draft) |
| Status | Draft — Phase 10 |
| Last Updated | 2026-09-11 |

---

## Purpose

Documentation-level accessibility requirements for Sentinel AI UI. Maps to NFR usability expectations. **No implementation code.**

**Target orientation:** WCAG 2.1 Level AA as design target — not claimed as certified.

---

## 1. Keyboard Navigation

| Requirement | Specification |
|-------------|---------------|
| All interactive elements | Reachable via Tab order |
| Skip link | "Skip to main content" on app load |
| Queue tables | Arrow key navigation optional enhancement; Tab minimum |
| Modals | Focus trap; restore focus on close |
| Shortcuts | Documented shortcuts must not conflict with assistive tech |

---

## 2. Focus Management

| Scenario | Behavior |
|----------|----------|
| Open modal | Focus first focusable element |
| Close modal | Return focus to trigger |
| Route change | Focus main heading |
| SSE update | No focus steal on background refresh |
| Error banner | Focus optional — aria-live sufficient |

---

## 3. Semantic Structure

| Element | Usage |
|---------|-------|
| `<main>` | Primary content |
| `<nav>` | Primary navigation |
| `<h1>` | One per view |
| Headings | Logical hierarchy h1→h2→h3 |
| Landmarks | banner, navigation, main, complementary for AI panel |

---

## 4. Color-Independent Status

Risk severity, alert status, and compliance outcomes MUST communicate via:

- Text label
- Icon shape or pattern
- Not color alone

**Design system:** DesignSystem.md §10

---

## 5. Screen Reader Considerations

| Pattern | Requirement |
|---------|-------------|
| Tables | `<th scope="col">`; caption or aria-label |
| Dynamic updates | `aria-live="polite"` for queue refresh |
| Loading | `aria-busy="true"` on loading regions |
| AI panel | `role="complementary"` + assistive label |
| Icons | `aria-hidden` if decorative; `aria-label` if functional |

---

## 6. Accessible Tables

| Feature | Requirement |
|---------|-------------|
| Sort | Announce sort direction change |
| Pagination | "Load more" button labeled with context |
| Row actions | Accessible name includes row identifier |
| Empty state | Readable message in live region |

---

## 7. Form Errors

| Requirement | Specification |
|-------------|---------------|
| Association | `aria-describedby` links field to error |
| Summary | Error list at top on submit failure |
| Required fields | Programmatically indicated |
| Format hints | Visible before error where format is strict |

---

## 8. Dialogs

| Requirement | Specification |
|-------------|---------------|
| Role | `role="dialog"` + `aria-modal="true"` |
| Title | `aria-labelledby` references dialog title |
| Escape | Closes non-destructive dialogs |
| Destructive confirm | Focus on cancel default or explicit confirm text |

---

## 9. Loading States

| State | Accessible behavior |
|-------|---------------------|
| Initial page load | Page title + skeleton aria-busy |
| Button submit | `aria-disabled` + loading text |
| AI generation | Progress announced at start; timeout announced |

---

## 10. Reduced Motion

| Requirement | Specification |
|-------------|---------------|
| prefers-reduced-motion | Disable non-essential animations |
| SSE updates | Instant DOM update — no slide animations required |
| Charts | Static fallback if motion reduced |

---

## 11. Responsive / Zoom

| Requirement | Target |
|-------------|--------|
| Zoom 200% | Usable without horizontal scroll on primary flows |
| Touch targets | Minimum 44×44px equivalent for primary actions |

---

## 12. NFR Mapping

| NFR | Accessibility relevance |
|-----|-------------------------|
| NFR-USAB-001 | Usability baseline |
| NFR-USAB-002 | Accessibility design target (if defined in NFR doc) |
| NFR-SEC-003 | Auth flows accessible |
| NFR-RES-003 | Degraded states communicated accessibly |

---

## 13. Live Regions (MVP Design Target)

| Event | Live region |
|-------|-------------|
| Queue item count change (SSE) | `aria-live="polite"` |
| Form validation summary | Assertive on submit failure |
| AI timeout / unavailable | Polite banner |
| Session expired | Assertive |

Avoid announcing every SSE row mutation — summarize (“Queue updated”).

---

## 14. Contrast Expectations (Design Target)

| Element | Expectation |
|---------|-------------|
| Body text | WCAG 2.1 AA contrast **design target** |
| Status badges | Text remains readable on badge background |
| Focus ring | Visible non-color-only indicator |

**Not claimed certified.**

---

## 15. MVP Workflow Acceptance Checklist (Design)

For WF-1, WF-3, WF-4, WF-7 before implementation sign-off (Phase 11/12):

- [ ] Keyboard-only completion path documented
- [ ] Modal focus trap specified
- [ ] Table headers / empty states specified
- [ ] AI panel labeled for AT
- [ ] Errors associated with fields
- [ ] Reduced motion respected for non-essential motion

---

## 16. Validation Method (Future Implementation)

- Automated axe/lighthouse in CI (extension of ContractValidationCI design)
- Manual keyboard-only test pass per major workflow
- Screen reader sampling (VoiceOver/NVDA) on alert triage + case close

---

## Related Documents

- [DesignSystem.md](DesignSystem.md)
- [MVPWorkflows.md](MVPWorkflows.md)
- [FrontendUXArchitecture.md](FrontendUXArchitecture.md)
- [NonFunctionalRequirements.md](../02-requirements/NonFunctionalRequirements.md)
