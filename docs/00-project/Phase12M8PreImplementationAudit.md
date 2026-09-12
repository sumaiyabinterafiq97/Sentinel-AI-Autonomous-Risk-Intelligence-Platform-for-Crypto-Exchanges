# Phase 12 M8 Pre-Implementation Audit

## Document Information

| Field | Value |
|-------|-------|
| Milestone | M8 — Assistive AI only |
| Date | 2026-09-11 |
| Status | Authorized implementation basis (not a product requirement rewrite) |

---

## 1. M8 objective

Implement **MVP assistive AI** on deployable `sentinel-ai` (Python FastAPI): Investigation, Risk, and Retrieval agents; prompt management; recommendation fetch; AI-owned persistence; approved AI events via in-process **TransactionalOutbox simulation**.

Human/domain services remain authoritative. AI must not approve COMP, close/assign cases, create/close alerts, change alert priority, or publish RISK/ALERT/INVEST/COMP lifecycle events.

**M9+ is not authorized.**

---

## 2. Applicable AI FRs (MVP)

| FR | M8? | Notes |
|----|-----|-------|
| AI-FR-001 | **Yes** | Investigation assistance via API-AI-001 |
| AI-FR-002 | **Yes** | Risk explanation via API-AI-002; must not block RISK scoring |
| AI-FR-003 | **Yes** | Retrieval via API-AI-003 + retrieval tools |
| AI-FR-004 | **Yes** | Prompt list/create/patch via API-AI-005 |
| AI-FR-005 | **Yes** | SYSTEM / AI / EVIDENCE / HUMAN labeling + provenance |
| AI-FR-006 | **Yes** | Publish only `AIRecommendationGenerated`, `PromptUpdated` |
| AI-FR-007 | **Yes** | Consume exactly `CaseUpdated`, `RiskCalculated`, `EvidenceAttached`, `AlertCreated` for **context freshness**, not autonomous invocation (AIArchitecture §7) |
| AI-FR-008 | **Yes** | Tool allowlist + AUTHZ per invocation |
| AI-FR-009 | **Yes** | Persist recommendation + `agent_runs` audit |

No AI-FR-010. Compliance agent / API-AI-006 / API-AI-007 / `AIEvaluationCompleted` are **V2**.

---

## 3. API IDs and operationIds (OpenAPI is binding)

| API ID | Method | Path | operationId | Permission (APIInventory) | HTTP |
|--------|--------|------|-------------|---------------------------|------|
| API-AI-001 | POST | `/v1/ai/assist/investigation` | `assistInvestigation` | `ai:investigation:assist` | **202** |
| API-AI-002 | POST | `/v1/ai/assist/risk-explanation` | `assistRiskExplanation` | `ai:risk:assist` | **202** |
| API-AI-003 | POST | `/v1/ai/assist/retrieve` | `assistRetrieve` | `ai:retrieve:execute` | **201** |
| API-AI-004 | GET | `/v1/ai/recommendations/{recommendationId}` | `getRecommendation` | `ai:recommendation:read` | **200** |
| API-AI-005 | GET | `/v1/ai/prompts` | `listPrompts` | `ai:prompt:write` | **200** |
| API-AI-005 | POST | `/v1/ai/prompts` | `createPrompt` | `ai:prompt:write` | **201** |
| API-AI-005 | PATCH | `/v1/ai/prompts` | `patchPrompt` | `ai:prompt:write` | **200** |

**Not implemented (V2):** API-AI-006 `POST /v1/ai/assist/compliance`; API-AI-007 evaluations.

OpenAPI AI paths do not declare `security: []`; they require `Authorization` + `X-Organization-Id` like other protected APIs.

---

## 4. Agents and tools

### MVP agents

| Agent | FR | Tools (ToolDefinitions.md) |
|-------|----|----------------------------|
| Investigation | AI-FR-001 | TOOL-INVEST-CASE-GET, TOOL-INVEST-EVIDENCE-LIST, TOOL-ALERT-GET, TOOL-RISK-ASSESSMENT-GET, TOOL-RETRIEVE-SEARCH |
| Risk | AI-FR-002 | TOOL-RISK-ASSESSMENT-GET, TOOL-RISK-RULE-HITS-GET |
| Retrieval | AI-FR-003 | TOOL-RETRIEVE-SEARCH, TOOL-RETRIEVE-EMBED |

### Forbidden tools (must not exist)

Create/close/priority alerts; create/close/assign cases; approve/reject COMP; publish `RiskCalculated`; mutate users/orgs; cross-tenant search.

### Tool AUTHZ gap-fill (AI-FR-008 + ToolDefinitions “invest case read”)

There is no `invest:evidence:read` seed. ListCaseEvidence uses **`invest:case:read`**. GetAlert uses `alert:alert:read`. Risk tools use `risk:assessment:read`. Retrieval tools use `ai:retrieve:execute` (already required on API-AI-003; Investigation may invoke retrieval only if the actor also has `ai:retrieve:execute`).

---

## 5. Database

| Item | Value |
|------|-------|
| Logical migration | **010** `010_ai_init_assistive_tables` |
| Executable file | `ai-service/db/migration/V010__ai_init_assistive_tables.sql` |
| Schema | `ai` (not risk/alert/invest/comp/auth/core) |
| Documented tables | `prompts`, `prompt_versions`, `ai_recommendations`, `agent_runs`, `processed_event_ids` |

**Gap-fills (not frozen product tables):**

1. `ai.document_embeddings` — authorized by VectorDataArchitecture.md / ADR-013 for Retrieval. Migration 010 lists it only as a “vector note.” Include the table. Store embeddings as `float8[]` so local/tests do not require the `vector` PostgreSQL extension. This is a **simulation** of pgvector, not a production embedding cluster.
2. `ai.outbox_events` — M3 pattern used by CORE/AUTH/RISK; not listed in 010. Required to publish AI events atomically with domain writes **inside sentinel-ai**. Java `QualifiedOutboxTable` stays unchanged (Java does not own AI writes).

**Runtime for pytest:** in-memory store mirroring these tables (same as “simulation” posture). SQL file is the authorized executable schema when PostgreSQL is used.

Do **not** rewrite migrations 001–009.

---

## 6. Events

### Publish (approved schemas only)

| Event | Schema | When |
|-------|--------|------|
| `AIRecommendationGenerated` | `events/mvp/ai/AIRecommendationGenerated.v1.schema.json` | Assist job reaches `completed` / `partial` / `failed` |
| `PromptUpdated` | `events/mvp/ai/PromptUpdated.v1.schema.json` | Prompt create or new version |

### Consume (AI-FR-007 / EventContracts / catalog)

`CaseUpdated`, `RiskCalculated`, `EvidenceAttached`, `AlertCreated` — index tenant-scoped **context documents** for tools/RAG. **Do not** auto-run agents on events.

Cross-process delivery from `sentinel-ops` is **not** wired (no Kafka/Redpanda). Consumers run against the AI service in-process durable-log **simulation** (tests inject envelopes). Same limitation as M7 `UserUpdated` identity→ops.

### Must not publish

`RiskCalculated`, `HighRiskDetected`, `AlertCreated`/`Assigned`/`Closed`, `CaseCreated`/`Updated`/`Closed`/`Assigned`, `EvidenceAttached`, `ComplianceReviewed`, or any SEC event.

`AgentRunFailed` remains deferred (GD-002); failures stored on `agent_runs`.

---

## 7. Permissions to seed (identity V005_6)

`ai:investigation:assist`, `ai:risk:assist`, `ai:retrieve:execute`, `ai:recommendation:read`, `ai:prompt:write`

Do **not** seed `ai:compliance:assist` or `ai:eval:write` (V2).

---

## 8. Relevant NFRs / ADRs / security

| Item | Application |
|------|-------------|
| NFR-PERF-006 | Assist P95 ≤ 5s / 10s timeout — **simulation target**; local generator, not a hosted LLM SLO |
| NFR-SEC-009/010 | Tenant isolation; tool AUTHZ |
| ADR-002 | Assistive only |
| ADR-003 | RISK scoring independent of AI |
| ADR-007 | No OpenAI/Anthropic hard requirement — **deterministic local generator** |
| ADR-013 | Vector retrieval in `ai` schema (simulated embeddings in M8 tests) |
| ADR-015 | Outbox; durable log simulation |
| ADR-019 | FastAPI `sentinel-ai` |
| AISecurityModel | Prompt injection treated as data; no write tools; no cross-tenant retrieval |
| AIInteractionPatterns | SYSTEM / AI / EVIDENCE / HUMAN in recommendation `content` string |
| Auth | Existing M2 HMAC (`IDENTITY_TOKEN_HMAC_KEY`); do not reimplement identity; `X-Sentinel-Permissions` is not authority |

---

## 9. Human-in-the-loop / UI

M8 is API-only. UI remains M11. Responses must still be labeled so DASH can render SYSTEM/AI/EVIDENCE/HUMAN later. No frontend work.

---

## 10. Known documentation gaps and decisions

| ID | Gap | Governance | M8 decision |
|----|-----|------------|-------------|
| M8-G1 | PATCH `/v1/ai/prompts` has no `{promptId}`; `PromptPatchRequest` has `name`/`template` only | Do not change OpenAPI | Locate prompt by `(organization_id, name)`; template creates a new **immutable** version and activates it. Missing name → 400 |
| M8-G2 | OpenAPI `AIRecommendation.content` is a string; DB `content` is jsonb | Smallest mapping | Persist structured jsonb; serialize labeled text for the HTTP `content` field |
| M8-G3 | Job status enum is `pending\|completed\|failed`; DB also has `partial` | Map | Job HTTP `status=completed` when recommendation is `completed` or `partial`; GET returns actual `partial` |
| M8-G4 | API-AI-001/002 are 202 async; no worker bus | Smallest | Run agent **in-process** before returning 202/201 (still Accepted/Created). Not a background job platform |
| M8-G5 | No live INVEST/ALERT/RISK HTTP from AI in M8 tests | Tools are conceptual | Tools read **event-indexed context** in the AI store, not ops tables. Does not invent mutation APIs |
| M8-G6 | API-AI-005 GET requires `ai:prompt:write` | Follow inventory | Do not invent `ai:prompt:read` |
| M8-G7 | AI-FR-009 mentions CORE audit API | No M8 CORE client authorized | Audit locally via `agent_runs` + recommendation rows |
| M8-G8 | Prompt `agent_type` not in OpenAPI create body | Infer | `risk` / `retrieval` / `investigation` from name keywords; default `investigation` |
| M8-G9 | LangGraph / model provider (P11-OQ-AI-001/002) | ADR-007 | **No LangGraph, no vendor SDK.** Deterministic generator |
| M8-G10 | OpenAPI job lacks `partial` | See M8-G3 | Document only |

If a gap would require inventing an API, event, or COMP approval tool: **leave unimplemented**.

---

## 11. Explicitly NOT authorized for M8

- M9 DASH BFF/SSE, M10 ADMIN, M11 frontend, M12 hardening
- API-AI-006 / API-AI-007 / Compliance Agent / Report Agent / eval runtime
- Production Kafka/Redpanda/RabbitMQ
- Docker/K8s/CI unless already present
- LLM provider calls
- Autonomous loops that mutate business state
- Changing frozen FRS/FDS/OpenAPI
- SEC contract changes
- WALLET, REPORT, OPS enhancements

---

## 12. Domain ownership verification

| Domain | M8 AI |
|--------|--------|
| RISK | Read-only context/tools. No scores, no `RiskCalculated` |
| ALERT | Read-only GetAlert. No lifecycle |
| INVEST | Read-only case/evidence metadata. No close/assign |
| COMP | **No APIs, no tools, no events** |
| AUTH/AUTHZ/USER/ORG | Identity remains source of tokens/permissions |
| DASH | Consumer of `AIRecommendationGenerated` later (M9). Not implemented |
| AI | Owns prompts, recommendations, runs, embeddings, AI events only |
