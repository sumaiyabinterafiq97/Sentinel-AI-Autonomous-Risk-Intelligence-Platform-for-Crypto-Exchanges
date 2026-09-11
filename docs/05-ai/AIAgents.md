# AI Agents

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | AI Agents |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 8 |
| Last Updated | 2026-09-11 |
| Authority | AI-FR-001–009 |

---

## Agent Design Principles

1. Assistive only — no domain lifecycle ownership
2. Explicit human invocation for MVP assists
3. Tool allowlists + AUTHZ on every tool call
4. Explainability metadata required (AI-FR-005)
5. Graceful degradation when AI unavailable
6. Provider-neutral model abstraction

---

## REQUIRED MVP AI CAPABILITIES

### Agent A — Investigation Agent (AI-FR-001)

| Field | Specification |
|-------|---------------|
| Purpose | Assistive investigation summaries, contextual analysis, suggested follow-ups |
| Trigger | Authorized request via API-AI-001 |
| Inputs | caseId / workflow context; permitted case/alert/risk reads |
| Outputs | Assistive recommendation content + provenance |
| Tools | Case read, evidence metadata read, related alert/risk read, retrieval |
| Tool permissions | `ai:investigation:assist` + domain read permissions via tools |
| Context sources | INVEST/ALERT/RISK authorized reads; events AI-FR-007 |
| Retrieval | May invoke Retrieval Agent tools |
| Prompt responsibility | Investigation prompt versions (AI-FR-004) |
| Model abstraction | Provider-neutral |
| Expected latency | P95 ≤ 5s interactive (**simulation target** NFR-PERF-006) |
| Timeout | 10s graceful fallback |
| Retry | Limited transient retries; no unbounded loops |
| Failure behavior | `failed`/`partial` recommendation; INVEST continues |
| Confidence | Optional; never authoritative |
| Evidence requirements | Source references where applicable |
| Human review | Required before case mutations |
| Audit | AI-FR-009 |
| Evaluation | Groundedness, no case mutation, authz deny paths |
| Security | No write tools to INVEST/ALERT; tenant scoped |
| Data access | Classification-aware; minimize PII |

---

### Agent B — Risk Agent (AI-FR-002)

| Field | Specification |
|-------|---------------|
| Purpose | Narrative risk explanation for analysts |
| Trigger | API-AI-002 |
| Inputs | assessmentId / entity context from RISK outputs |
| Outputs | Explanation text + provenance |
| Tools | Assessment read, rule-hit read |
| Tool permissions | `ai:risk:assist` |
| Context sources | RISK assessments; optional related entity reads |
| Critical path | **Non-critical** — must not block deterministic RISK scoring (ADR-003) |
| Prompt responsibility | Risk explanation prompts |
| Latency / timeout | Same as NFR-PERF-006 / 10s |
| Failure behavior | Degraded; RISK/ALERT unaffected |
| Human review | Analyst uses explanation as input only |
| Boundary | Must not publish RiskCalculated; must not create alerts; must not change scores |
| Audit | AI-FR-009 |
| API | API-AI-002 → API-AI-004 |

---

### Agent C — Retrieval Agent (AI-FR-003)

| Field | Specification |
|-------|---------------|
| Purpose | Retrieve documents/records/permitted context for other agents or direct assist |
| Trigger | API-AI-003 or internal tool call from Investigation/Risk agents |
| Inputs | Query, target refs, classification constraints |
| Outputs | Retrieved chunks/refs with provenance |
| Tools | Tenant-scoped search/index/read tools only |
| Tool permissions | `ai:retrieve:execute` |
| RAG / vector | pgvector non-authoritative embeddings |
| Failure behavior | Empty result set + explicit error; no fabricated docs |
| Security | No cross-tenant retrieval; no AUTHZ bypass |
| Audit | Tool invocations correlatable |

---

### Capability D — Prompt Management (AI-FR-004)

Not a user-facing “agent”; platform capability.

| Field | Specification |
|-------|---------------|
| Purpose | Create/update/version/activate prompts for MVP agents |
| API | API-AI-005 |
| Event | PromptUpdated |
| Access | Privileged AI/platform roles |
| Audit | Prompt changes audited |

---

### Capability E — Explainability Controls (AI-FR-005)

Cross-cutting requirement on Investigation and Risk agent outputs: provenance, source refs, explicit assistive labeling.

---

## FUTURE / V2 / V3 AI CAPABILITIES

| Capability | Release | Notes |
|------------|---------|-------|
| Compliance Agent | V2 | Not in AI-FR-001–009; API-AI-006 |
| Report Agent | V2 | Deferred |
| Evaluation Framework runtime | V2 | `AIEvaluationCompleted`; API-AI-007 |
| Autonomous enforcement | Excluded | Violates assistive posture |
| Extended AI autonomy | V3/future | Requires new FRs + governance |

---

## Orchestration

MVP orchestration is **request-driven** within the AI Platform service:

1. Authorize
2. Select agent by endpoint
3. Assemble context
4. Optional retrieval
5. Generate
6. Validate explainability
7. Persist + audit + event
8. Return 202/200 to caller

No multi-agent autonomous loops that mutate business state.

---

## Human-in-the-Loop

| Decision | Owner |
|----------|-------|
| Close/assign alert | ALERT human/API |
| Create/close case | INVEST human/API |
| Compliance approve/reject | COMP human/API |
| Change risk rules | RISK authorized human/API |
| Accept AI suggestion | Human only |

---

## Safety Guardrails

- Tool allowlist
- AUTHZ per tool
- Output must not include executable domain commands
- Timeout/circuit break
- Prompt injection defenses (treat retrieved content untrusted)
- Tenant isolation

---

## Related Documents

- [AIArchitecture.md](../03-architecture/AIArchitecture.md)
- [ToolDefinitions.md](ToolDefinitions.md)
- [EvaluationFramework.md](EvaluationFramework.md)
