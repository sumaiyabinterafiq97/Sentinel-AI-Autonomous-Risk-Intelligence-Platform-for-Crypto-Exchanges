# Phase 12 M8 Report

## A. Overall M8 status

**COMPLETE** against documented MVP AI APIs API-AI-001–005, FRs AI-FR-001–009, migration 010, and approved AI event schemas.

This is **not** production model evaluation, legal/regulatory certification, WCAG certification, or security certification. The generator and vector index are **simulations**.

**M9 was not started.** No commit. No push.

## B. Objective

Assistive Investigation, Risk explanation, and Retrieval on `sentinel-ai`, with prompt management, recommendation audit, tool AUTHZ, tenant isolation, and M3-style outbox **simulation**.

## C. AI FRs implemented

AI-FR-001 through AI-FR-009 as specified in `Phase12M8PreImplementationAudit.md`.

## D–E. APIs / operationIds

| API ID | operationId |
|--------|-------------|
| API-AI-001 | `assistInvestigation` |
| API-AI-002 | `assistRiskExplanation` |
| API-AI-003 | `assistRetrieve` |
| API-AI-004 | `getRecommendation` |
| API-AI-005 | `listPrompts`, `createPrompt`, `patchPrompt` |

V2 API-AI-006 / API-AI-007 are **not** implemented.

## F. Agents

Investigation, Risk, Retrieval — request-driven, assistive only.

## G. Tools

Allowlisted read tools only: GetCase, ListCaseEvidence, GetAlert, GetRiskAssessment, GetRuleHits, SearchTenantDocuments, QueryEmbeddings.

Forbidden lifecycle tools are implemented as hard denials.

## H. Retrieval / vector

Hashed bag-of-words embeddings (`float8[]` in V010). Tenant `organization_id` filter. Injection-like chunks dropped. Provenance via sourceType/sourceId. **Not** a production pgvector cluster.

## I. Security / tenant isolation

M2 HMAC tokens (`IDENTITY_TOKEN_HMAC_KEY`). Org header must match token org. Permission checks on every AI API. Tool calls require additional domain read permissions. Cross-tenant recommendation GET → 404. Cross-tenant retrieval does not return other-org chunks.

## J. Database

- Identity Flyway `V005_6__authz_seed_ai_permissions.sql`
- AI `ai-service/db/migration/V010__ai_init_assistive_tables.sql` — schema `ai`: prompts, prompt_versions, ai_recommendations, agent_runs, processed_event_ids, document_embeddings, outbox_events, context_records
- Pytest uses an in-memory mirror of those tables (simulation)

## K. Events

**Published:** `AIRecommendationGenerated`, `PromptUpdated` (outbox → in-process durable log **simulation**).

**Consumed:** `CaseUpdated`, `RiskCalculated`, `EvidenceAttached`, `AlertCreated` for context index only. Not auto-invoked. Ops→AI delivery without a broker is **not** wired.

**Not published:** RISK/ALERT/INVEST/COMP/SEC lifecycle events.

## L. Tests

`tests/test_health.py`, `test_authz.py`, `test_assist.py`, `test_tools.py`, `test_events.py`, `test_safety.py` (26 passed).

Java: EventStreams AI mapping; identity admin includes `ai:` prefix.

## M. Validation

| Check | Result |
|--------|--------|
| `./gradlew test` | PASS |
| `python3 contracts/validate.py` | PASS (76 operationIds, SEC lock OK) |
| AI pytest + ruff (`.venv`) | PASS |
| web vitest | PASS |
| `git diff --check` | PASS |

## N. M0–M7 regression

Passed in the same Gradle run. Ops health remains M7. Ops still excludes `/ai/` on sentinel-ops (AI is a separate deployable).

## O. Limitations

- Local deterministic generator, not a hosted LLM
- Tools read AI-side context index, not live ops HTTP
- Embeddings are simulated; pgvector extension not required
- CORE audit HTTP not called (local `agent_runs`)
- No Kafka/Redpanda
- PATCH prompts identified by `name` (OpenAPI has no promptId path)
- NFR-PERF-006 is a simulation target, not a measured production SLO
- GET prompts uses `ai:prompt:write` per inventory (no `ai:prompt:read`)

## P. Documentation gaps

Recorded as M8-G1–G10 in the pre-implementation audit. Frozen FRS/FDS/OpenAPI were **not** rewritten.

## Q–S. Frozen / AI / MVP validation

- FRS unchanged
- FDS unchanged
- SEC consume/exclusion lock unchanged
- AI assistive-only; no COMP approval; RISK scoring unchanged on ops
- V2/V3 AI not implemented
- No M9 DASH code

## T. Git status

Uncommitted local work. No push.

## U. M9 handoff

Next documented milestone is **M9 DASH** (BFF + SSE + queue projections). Do not start M9 in this workstream.
