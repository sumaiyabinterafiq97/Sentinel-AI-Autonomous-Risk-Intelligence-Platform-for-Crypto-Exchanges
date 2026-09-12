# Phase 12 M8 Handoff

## Status

**M8 = COMPLETE** on `sentinel-ai` against OpenAPI API-AI-001–005, AI-FR-001–009, migration 010, and approved AI event schemas.

**M9 = NOT STARTED.**

No commit. No push.

## What M8 delivered

- Assistive Investigation / Risk / Retrieval APIs
- Prompt list/create/patch with `PromptUpdated`
- Recommendation fetch with SYSTEM/AI/EVIDENCE/HUMAN labeling
- Read-only tool allowlist + AUTHZ
- Tenant-scoped retrieval **simulation**
- In-process outbox **simulation** for `AIRecommendationGenerated`
- Upstream context consumer for CaseUpdated, RiskCalculated, EvidenceAttached, AlertCreated
- Identity permission seed `V005_6`

## Known limitations

See `Phase12M8Report.md` §O. Generator and vectors are simulations. Tools do not call live ops HTTP. No production broker.

## Deferred

- API-AI-006 / Compliance Agent (V2)
- API-AI-007 / evaluation runtime (V2)
- LangGraph / vendor LLM (P11-OQ-AI-001/002)
- pgvector extension in CI
- DASH rendering (M9)
- Cross-service event bus

## Unresolved

Whether prompt PATCH should use a path id (OpenAPI collection PATCH). M8 locates by `name`.

## Recommended next milestone

**M9 DASH only** — BFF, SSE, workspace projections. Must not implement ADMIN, frontend SCR-00–15 as a substitute for DASH, WALLET, SEC, or REPORT.

Do not implement M9 in this workstream.
