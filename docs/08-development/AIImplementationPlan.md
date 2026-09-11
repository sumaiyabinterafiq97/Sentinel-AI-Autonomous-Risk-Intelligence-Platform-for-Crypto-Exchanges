# AI Implementation Plan

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | AI Implementation Plan |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 11 |
| Last Updated | 2026-09-11 |
| Authority | AIArchitecture, AIAgents, AI-FR-001–009, ADR-002/003/013 |

---

## Purpose

Plan MVP AI Platform implementation. **Assistive only. No agent/runtime code in Phase 11.**

---

## MVP Agents (Required)

| Agent | FR | API |
|-------|-----|-----|
| Investigation Agent | AI-FR-001 | API-AI-001 |
| Risk Agent | AI-FR-002 | API-AI-002 |
| Retrieval Agent | AI-FR-003 | API-AI-003 |

Plus: prompts API-AI-005; recommendations API-AI-004; events AIRecommendationGenerated, PromptUpdated.

**V2 separated:** Compliance assist (API-AI-006), evaluations runtime (API-AI-007), Report agents.

---

## Implementation Direction (Candidate)

| Technology | Role | Note |
|------------|------|------|
| Python 3.11+ | AI service runtime | Candidate (ADR-007) |
| FastAPI | HTTP boundary for AI APIs | Candidate |
| LangGraph | Agent orchestration | Candidate — swap allowed if ADR updated |
| Model provider SDK | Abstracted behind interface | No hard OpenAI requirement in FRS |
| pgvector | Embeddings (ADR-013) | |

---

## Service Boundary

Deployable: `sentinel-ai`

| May | Must not |
|-----|----------|
| Explain, summarize, retrieve, recommend | Own alert/case/compliance lifecycle |
| Call allowlisted read tools | Bypass AUTHZ |
| Write `ai` schema recommendations/runs | Write ALERT/INVEST/COMP/RISK authoritative tables |
| Publish AI events only | Publish RiskCalculated/AlertCreated/CaseClosed |
| Fail open to degraded UX | Block RISK scoring path (ADR-003) |

---

## Planned Components

| Component | Responsibility |
|-----------|----------------|
| AI Gateway / API layer | AuthZ, request validation, timeouts |
| Agent orchestrator | LangGraph (or equiv.) graph per agent |
| Prompt manager | Versioned prompts (API-AI-005) |
| Tool registry | Allowlist from ToolDefinitions.md |
| Tool authorizer | Permission check per tool |
| Retriever | Vector + metadata filters; tenant scoped |
| Model abstraction | Provider interface + fallback |
| Response validator | Schema + grounding checks |
| Telemetry | Latency, tokens/cost, tool failures |
| Audit | recommendationId, correlationId, actor |

---

## Runtime Behaviors

| Topic | Plan |
|-------|------|
| Timeout | ~10s design target (NFR-PERF-006) then fallback |
| Retry | Bounded; idempotent recommend fetch |
| Hallucination controls | Citations required; disclaimer; eval hooks |
| Confidence | Pass-through when model provides; never set alert priority |
| Evidence | Cite system artifacts; no fabricated IDs |
| Cost/latency telemetry | Required for ops (V2 OPS may consume later) |

---

## Data

- Migration **010** `ai` schema
- pgvector embeddings; processed_event_ids for ingest idempotency
- No Neo4j required for MVP AI

---

## Verification

Tool denial tests; no case mutation tests; prompt regression; hallucination suites (docs/05-ai); timeout/fallback — TestingImplementationPlan.md.

---

## Open Questions

| ID | Item |
|----|------|
| P11-OQ-AI-001 | Exact model provider ADR |
| P11-OQ-AI-002 | LangGraph vs alternative orchestrator confirmation |

---

## Related Documents

- [../03-architecture/AIArchitecture.md](../03-architecture/AIArchitecture.md)
- [../05-ai/AIAgents.md](../05-ai/AIAgents.md)
- [../05-ai/ToolDefinitions.md](../05-ai/ToolDefinitions.md)
- [../07-ui/AIInteractionPatterns.md](../07-ui/AIInteractionPatterns.md)
