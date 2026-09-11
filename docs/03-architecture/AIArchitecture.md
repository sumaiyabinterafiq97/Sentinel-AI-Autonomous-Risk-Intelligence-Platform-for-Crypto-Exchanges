# AI Architecture

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | AI Architecture |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 8 |
| Last Updated | 2026-09-11 |
| Authority | AI-FR-001–009; FDS AI domain; ADR-003; NFR-PERF-006; PRD §29–30 |
| Related | [AIAgents.md](../05-ai/AIAgents.md), [EvaluationFramework.md](../05-ai/EvaluationFramework.md), [ToolDefinitions.md](../05-ai/ToolDefinitions.md) |

---

## Purpose

Define the **MVP AI Platform architecture** as an assistive subsystem. This document does **not** authorize production agent runtime implementation.

**Coding policy:** Documentation only. No agent runtime, no model provider lock-in as a mandatory product requirement.

---

## 1. AI Platform Purpose

Provide assistive analysis, explanation, retrieval, and recommendation capabilities that accelerate Risk Analyst and investigator workflows while preserving human accountability and domain ownership for consequential decisions.

---

## 2. AI Responsibilities (MVP)

| Responsibility | FR |
|----------------|-----|
| Investigation assistance (summaries, suggested follow-ups) | AI-FR-001 |
| Risk explanation assistance (non-critical path) | AI-FR-002 |
| Contextual evidence retrieval via authorized tools | AI-FR-003 |
| Prompt create/version/activate for MVP agents | AI-FR-004 |
| Explainability metadata on assistive outputs | AI-FR-005 |
| Publish AIRecommendationGenerated / PromptUpdated | AI-FR-006 |
| Consume CaseUpdated, RiskCalculated, EvidenceAttached, AlertCreated | AI-FR-007 |
| Authorize tool invocations (AUTHZ + allowlist) | AI-FR-008 |
| Record recommendation audit outcomes | AI-FR-009 |

---

## 3. AI Non-Responsibilities

AI **must not** own or autonomously perform:

| Forbidden ownership | Owning domain |
|---------------------|---------------|
| Transaction blocking / approval | Exchange systems / policy (outside AI) |
| Risk score ownership / RiskCalculated publication | RISK |
| Alert lifecycle / priority | ALERT |
| Case create/close/assign lifecycle | INVEST |
| Compliance approval / disposition | COMP |
| Security disposition | SEC (V2) |
| User / organization lifecycle | USER / ORG |
| Authentication / authorization policy | AUTH / AUTHZ |
| Autonomous enforcement tools | Excluded |

---

## 4. AI Service Boundary

```text
DASH / Domain APIs (human request)
        ↓
AI Platform Service (assistive)
        ↓
AUTHZ evaluate + tool allowlist (AI-FR-008)
        ↓
Context assembly (events AI-FR-007 + authorized reads)
        ↓
Agent runtime (Investigation / Risk / Retrieval)
        ↓
Guardrails + explainability (AI-FR-005)
        ↓
Persist recommendation + audit (AI-FR-009)
        ↓
Publish AIRecommendationGenerated (AI-FR-006)
        ↓
Human reviews; domain APIs execute consequential actions
```

AI is a **separate domain service**. It reads via authorized tools/APIs; it does not write ALERT/INVEST/COMP/RISK authoritative tables.

---

## 5. Agent Architecture

MVP agents (authoritative AI FRS index):

| Agent | FR | Role |
|-------|-----|------|
| Investigation Agent | AI-FR-001 | Case/context assistive analysis |
| Risk Agent | AI-FR-002 | Narrative risk explanation (non-critical path) |
| Retrieval Agent | AI-FR-003 | Evidence/document retrieval |

Supporting platform capabilities (not separate “agents” that own business outcomes):

| Capability | FR |
|------------|-----|
| Prompt Management | AI-FR-004 |
| Explainability Controls | AI-FR-005 |
| Event publish/consume contracts | AI-FR-006, AI-FR-007 |
| Tool authorization | AI-FR-008 |
| Recommendation audit | AI-FR-009 |

**Deferred (not MVP):** Compliance Agent, Report Agent, Agent Evaluation Framework runtime (`AIEvaluationCompleted`), autonomous enforcement.

See [AIAgents.md](../05-ai/AIAgents.md) for per-agent contracts.

---

## 6. Agent Lifecycle

| State | Meaning |
|-------|---------|
| Idle | No active run |
| Authorized | AUTHZ + permission checked |
| Assembling | Context + retrieval |
| Reasoning | Model invocation |
| Tooling | Authorized tool calls |
| Validating | Guardrails / explainability checks |
| Completed / Partial / Failed | Terminal recommendation status |
| Audited | AI-FR-009 record written |

Runs stored in `ai.agent_runs` (migration 010). `AgentRunFailed` event deferred (GD-002).

---

## 7. Agent Invocation Model

| Aspect | Policy |
|--------|--------|
| Trigger | Explicit human/API request (API-AI-001/002/003) |
| Sync vs async | Assistive investigation/risk typically **202 Accepted**; poll API-AI-004 |
| Correlation | `correlationId` required end-to-end |
| Tenant | `organizationId` mandatory |
| Idempotency | Client Idempotency-Key on mutating assist POSTs where applicable |

AI does **not** auto-invoke on every domain event for MVP; AI-FR-007 enables context freshness for subsequent assists, not autonomous action.

---

## 8–9. Tool Authorization and Allowlists

Per AI-FR-008:

- Every tool call requires AUTHZ evaluation
- Tools are **allowlisted per agent type**
- Denied calls are logged; no silent bypass
- Tools are **read-oriented** for MVP domain data (no write tools that mutate ALERT/INVEST/COMP/RISK state)

Conceptual allowlist categories:

| Agent | Allowed tool categories |
|-------|-------------------------|
| Investigation | Case read, evidence metadata read, related alert/risk read, retrieval |
| Risk | Assessment read, rule-hit read, related entity read |
| Retrieval | Document/index search within tenant + classification constraints |

---

## 10–12. Context Retrieval, RAG, Vector

| Aspect | Design |
|--------|--------|
| Context sources | Authorized APIs + AI-FR-007 events |
| RAG | Optional retrieval over permitted evidence/docs |
| Vector store | pgvector in AI schema — **non-authoritative** (ADR-013) |
| Rebuild | Embeddings rebuildable on model/prompt change |
| Isolation | Tenant-scoped retrieval only |

---

## 13–14. Prompt Management and Versioning

Per AI-FR-004:

- Prompts versioned in `ai.prompts` / `ai.prompt_versions`
- Activate version explicitly
- `PromptUpdated` published when applicable
- Restricted to authorized AI/platform roles
- Recommendation provenance references `prompt_version_id`

---

## 15–17. Model Abstraction, Selection, Fallback

| Aspect | Policy |
|--------|--------|
| Abstraction | Provider-neutral model interface — **no mandatory vendor** |
| Selection | Configured per agent + organization (ADMIN/AI settings) |
| Fallback | Secondary model or explicit failure; never silent domain mutation |
| Hint field | `model_hint` on prompt versions is advisory |

---

## 18–19. Timeout and Retry

| Aspect | Target |
|--------|--------|
| Interactive assist P95 | ≤ 5 seconds (**NFR-PERF-006 simulation target**) |
| Hard timeout | **10 seconds** then graceful fallback |
| Retry | Limited idempotent retries on transient provider errors; no unbounded loops |
| Degraded mode | Explicit “AI assist unavailable”; core workflows continue (NFR-RES-003) |

---

## 20–22. Hallucination Mitigation, Grounding, Confidence

| Control | Requirement |
|---------|-------------|
| Grounding | Cite retrieved/source references where applicable (AI-FR-005) |
| Fabricated evidence | Forbidden; outputs must not invent IDs/facts without sources |
| Confidence | Optional indicator in UI; never implies authoritative decision |
| Human review | Required before consequential domain actions |

---

## 23–25. Human Oversight, Auditability, Telemetry

| Aspect | Requirement |
|--------|-------------|
| Oversight | Human executes ALERT/INVEST/COMP/RISK mutations via owning APIs |
| Audit | AI-FR-009 + CORE audit patterns |
| Telemetry | Latency, token usage, status, correlation in `agent_runs` |
| Retention | T7 AI assistive tier (ADR-018 / DataRetention) |

---

## 26–28. Evaluation and Regression

MVP: evaluation **architecture** defined; full evaluation framework runtime is **V2 deferred** per AI FRS intentional deferrals.

Proposed MVP simulation evaluation (not production gate claims):

| Dimension | Method |
|-----------|--------|
| Groundedness | Human + automated citation checks |
| Hallucination | Adversarial / golden cases |
| Tool correctness | Allowlist + denied-call tests |
| Latency | NFR-PERF-006 simulation |
| Policy compliance | No lifecycle mutation tests |

See [EvaluationFramework.md](../05-ai/EvaluationFramework.md).

---

## 29–31. Prompt Injection, Sensitive Data, Minimization, Tenant Isolation

| Control | Design |
|---------|--------|
| Prompt injection | Treat retrieved content as untrusted; tool allowlist; output validation |
| Sensitive data | Classification-aware retrieval; minimize PII in prompts |
| Tenant isolation | organization_id on all AI tables and retrieval filters |
| Cross-tenant | Prohibited |

---

## 32–35. Failure, Cost, Latency, Security Boundaries

| Topic | Policy |
|-------|--------|
| Failure | Partial/failed recommendation; no blocking of INVEST/RISK critical paths |
| Cost | Token/usage telemetry; org-level budget controls as future ADMIN config |
| Latency | Non-critical path for risk assist (ADR-003) |
| Security | NFR-SEC-009, NFR-SEC-010; no privilege escalation via tools |

---

## 36–37. API and Event Boundaries

**MVP APIs:** API-AI-001–005 (see APIInventory.md)

**MVP events published:** `AIRecommendationGenerated`, `PromptUpdated`

**MVP events consumed:** `CaseUpdated`, `RiskCalculated`, `EvidenceAttached`, `AlertCreated`

**V2 APIs deferred:** API-AI-006 (compliance assist), API-AI-007 (evaluations)

---

## 38. Future AI Autonomy Governance

Any move beyond assistive-only requires:

1. New/changed FRs via change control
2. Explicit human-accountability model
3. Security architecture update
4. Gate re-evaluation

Until then: **assistive-only is mandatory**.

---

## AI Data Flow (Logical)

```text
User/Domain Request
        ↓
AI Gateway / AI Service
        ↓
Authorization (AUTHZ + AI-FR-008)
        ↓
Context Assembly (AI-FR-007 + tools)
        ↓
Retrieval (AI-FR-003 / RAG / pgvector)
        ↓
Agent Reasoning (AI-FR-001 / AI-FR-002)
        ↓
Authorized Tool Calls
        ↓
Evidence Collection
        ↓
Response Generation + Explainability (AI-FR-005)
        ↓
Validation / Guardrails
        ↓
Human/User (DASH/UI)
        ↓
Audit + Telemetry (AI-FR-009) + Event (AI-FR-006)
```

---

## Related Documents

- [AIAgents.md](../05-ai/AIAgents.md)
- [DomainBoundaries.md](DomainBoundaries.md)
- [ArchitectureDecisionRecords.md](ArchitectureDecisionRecords.md) — ADR-003, ADR-013
- [FunctionalRequirements.md](../02-requirements/FunctionalRequirements.md) — AI chapter
- [APIInventory.md](../06-api/APIInventory.md)
