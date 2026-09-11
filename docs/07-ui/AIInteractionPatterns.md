# AI Interaction Patterns

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | AI Interaction Patterns |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 10 |
| Last Updated | 2026-09-11 |
| Authority | AIArchitecture, AIAgents, AI-FR-001–009, DesignSystem §14 |

---

## Purpose

Define how assistive AI appears in MVP UX. **AI does not own lifecycles.** Documentation only — no agent implementation.

---

## Mandatory Content Distinction

Every AI surface must visually and textually separate:

| Layer | Label (example) | Source |
|-------|-----------------|--------|
| **SYSTEM RESULT** | “Risk score (system)” | RISK APIs / domain fields |
| **AI INTERPRETATION** | “AI Assistive Summary” | API-AI-001–003 → recommendation |
| **SOURCE EVIDENCE** | “Sources / citations” | Evidence links, rule hits, retrieved docs |
| **HUMAN DECISION** | “Your action” | Assign, close, approve via domain APIs |

Never merge AI text into status badges or priority chips as if authoritative.

---

## 1. AI Explanation Pattern (Risk)

| Field | Specification |
|-------|---------------|
| Trigger | User clicks “Explain risk” on Alert Detail / Risk Detail |
| API | `API-AI-002` then poll `API-AI-004` |
| Screens | SCR-03, SCR-06, SCR-15 |
| Display | AIExplanationPanel + ConfidenceIndicator + SourceCitation |
| Human path | Analyst still assigns/closes via ALERT APIs |

---

## 2. AI Recommendation Pattern (Investigation)

| Field | Specification |
|-------|---------------|
| Trigger | “Assist investigation” on Case Detail |
| API | `API-AI-001` (202) → `API-AI-004` |
| Display | Recommended next steps as suggestions — **buttons that call INVEST APIs are separate, human-initiated** |
| Prohibited | Auto-close case; auto-attach evidence without user confirm |

---

## 3. Evidence Retrieval Pattern

| Field | Specification |
|-------|---------------|
| Trigger | “Retrieve related evidence” |
| API | `API-AI-003` |
| Display | Candidate list with citations; user selects attach via `API-INVEST-007` |
| Boundary | Retrieval ≠ attach |

---

## 4. Source Citation Pattern

- Show recommendationId, prompt version when available
- Link to underlying assessment/alert/case IDs
- Empty citations → show “No sources returned — verify manually”

---

## 5. Confidence Presentation

- Use ConfidenceIndicator only when payload includes confidence
- If absent: state “Confidence not provided”
- Never map confidence to alert priority

---

## 6–9. Loading / Timeout / Unavailable / Tool Failure

| State | UX |
|-------|-----|
| Loading | Progress text; `aria-busy`; cancel if supported |
| Timeout | After design target (~10s, NFR-PERF-006): “AI assist timed out — continue manually” |
| Unavailable | Banner; core workflow enabled (NFR-RES-003) |
| Tool failure | ErrorState + requestId; retry; no fabricated content |

---

## 10. Hallucination-Risk Messaging

Static disclaimer on every completed AI panel:

> “AI output may be incomplete or incorrect. Verify against source evidence before acting.”

Do not present AI prose as quoted evidence unless citation links resolve to system artifacts.

---

## 11. Human Override

- Ignore / dismiss recommendation without side effects
- Proceed with contradictory human disposition
- No forced acceptance

---

## 12. User Feedback (MVP Design Target)

Optional thumbs / “not helpful” if FR/API later supports — **not required for MVP** unless inventory gains endpoint. Do not invent feedback API in Phase 10.

---

## 13. Prompt / Version Visibility

- Prompt admin: SCR under Administration for users with `ai:prompt:write` (`API-AI-005`)
- On recommendations: show version metadata when returned

---

## 14. Auditability

- Assist invocations correlatable via recommendationId / correlationId
- Human actions that follow remain on domain audit trails

---

## 15. Permission-Aware AI

| Permission | Capability |
|------------|------------|
| `ai:risk:assist` | Risk explanation |
| `ai:investigation:assist` | Investigation assist |
| `ai:retrieve:execute` | Retrieval |
| `ai:recommendation:read` | View recommendation |
| `ai:prompt:write` | Prompt management |

Without permission: hide AI entry points (or disable with reason).

---

## 16. Prohibited AI Actions (UX)

AI panels and AI-labeled controls MUST NOT:

- Close / assign alerts or cases
- Set alert priority
- Approve/reject compliance
- Block transactions
- Change roles/users/orgs
- Publish RiskCalculated / AlertCreated

Compliance AI assist (`API-AI-006`) is **V2** — not MVP UI.

---

## Related Documents

- [FrontendUXArchitecture.md](FrontendUXArchitecture.md)
- [ComponentLibrary.md](ComponentLibrary.md) — AIExplanationPanel
- [DashboardScreens.md](DashboardScreens.md) — SCR-15
- [../05-ai/AIAgents.md](../05-ai/AIAgents.md)
