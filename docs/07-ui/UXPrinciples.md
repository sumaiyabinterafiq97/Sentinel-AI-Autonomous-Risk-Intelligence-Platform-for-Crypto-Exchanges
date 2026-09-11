# UX Principles

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | UX Principles |
| Version | 0.2 (Draft) |
| Status | Draft — Phase 10 |
| Last Updated | 2026-09-11 |
| Authority | Personas.md, PRD.md, Principles.md |

---

## Purpose

Documentation-level UX principles for Sentinel AI operator interfaces. **No frontend code.** Governs Phase 6+ UI design and future implementation.

---

## 1. Operational First

Sentinel AI is an **operational risk platform**, not a marketing dashboard. Interfaces prioritize task completion, evidence visibility, and decision accountability over visual novelty.

**Implication:** Information density, scannable tables, and explicit status labels over decorative charts.

---

## 2. Domain Ownership Visible

Users must understand which system owns each lifecycle:

| Domain | UI must communicate |
|--------|---------------------|
| ALERT | Alert status and queue priority owned here |
| RISK | Scores and explanations are context — not alert disposition |
| INVEST | Cases and evidence owned here |
| COMP | Compliance outcomes require human action |
| AI | Recommendations are assistive — labeled distinctly |

Never present AI output as finalized domain state without human action indicator.

---

## 3. Explainability in Context

Risk scores, rule hits, and AI summaries appear **adjacent to the entity they explain** (alert, case, assessment) with links to source evidence.

**FR alignment:** RISK-FR-001, NFR-EXPL-001, AI-FR-002

---

## 4. Human Accountability

Consequential actions (close alert, close case, compliance decision) require:

- Explicit confirmation where policy demands
- Actor attribution in UI copy ("You are closing this alert")
- Audit trail visibility post-action

**FR alignment:** ALERT-FR-006, INVEST-FR-007, COMP-FR-002

---

## 5. Graceful AI Degradation

When AI is unavailable (NFR-RES-003):

- Core workflows remain usable
- AI panels show degraded state with retry/fallback
- No blocking modals for AI failure on non-AI paths

---

## 6. Least-Privilege Presentation

Navigation and actions reflect AUTHZ permissions. Hidden vs disabled:

- **Hidden:** User lacks permission entirely
- **Disabled:** User has read but not write; show why if helpful

**FR alignment:** AUTHZ-FR-001, DASH-FR-001

---

## 7. Tenant Context Always Visible

Multi-tenant operators see active `organization` context. Cross-tenant navigation requires explicit super-admin mode (audited).

**FR alignment:** ORG-FR-*, NFR-SEC-005

---

## 8. Predictable Work Queue Patterns

Alert and case queues use consistent patterns: sort, filter, assign, open detail, bulk actions only where FR authorizes.

**NFR alignment:** NFR-PERF-003, NFR-USAB-001

---

## 9. Auditability by Design

Sensitive UI actions map to audit events. Users can view "who changed what" on entities they can read.

**FR alignment:** CORE-FR-012, ADMIN-FR-003

---

## 10. Accessibility Non-Negotiable

Operational platforms must be usable via keyboard and assistive technology. See [Accessibility.md](Accessibility.md).

**NFR alignment:** NFR-USAB-002 (if defined), WCAG-oriented design targets

---

## 11. Four-Layer Information Honesty

UI must distinguish **SYSTEM RESULT**, **AI INTERPRETATION**, **SOURCE EVIDENCE**, and **HUMAN DECISION**. See [AIInteractionPatterns.md](AIInteractionPatterns.md).

---

## Related Documents

- [InformationArchitecture.md](InformationArchitecture.md)
- [MVPWorkflows.md](MVPWorkflows.md)
- [DesignSystem.md](DesignSystem.md)
- [FrontendUXArchitecture.md](FrontendUXArchitecture.md)
- [AIInteractionPatterns.md](AIInteractionPatterns.md)
