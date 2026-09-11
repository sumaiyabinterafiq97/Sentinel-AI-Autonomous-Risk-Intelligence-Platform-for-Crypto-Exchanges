# Vector Data Architecture (pgvector)

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Vector Data Architecture |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 3 |
| Last Updated | 2026-09-03 |

> Supersedes conceptual content in [pgvector.md](pgvector.md) for Phase 3.

---

## Purpose

Define embedding storage and retrieval for AI Platform (Retrieval Agent, AI-FR-003). **Vector search is assistive—not authoritative business truth.**

---

## Technology Role

| Component | Role |
|-----------|------|
| pgvector (PostgreSQL extension) | Embedding storage + similarity search in `ai` schema |
| Embedding model | External inference (provider-neutral) |
| Source documents | Domain APIs / approved knowledge corpora |

Alternative dedicated vector DB: deferred unless pgvector limits exceeded (future ADR).

---

## Schema: `ai.document_embeddings`

| Column | Type (conceptual) | Purpose |
|--------|-------------------|---------|
| `id` | uuid | Chunk ID |
| `organization_id` | uuid | Tenant isolation |
| `source_type` | text | `policy`, `case_evidence`, `compliance_kb`, `investigation_note` |
| `source_id` | uuid | Reference to owning domain record |
| `chunk_index` | int | Chunk sequence |
| `content_hash` | text | Deduplication |
| `embedding` | vector(n) | pgvector column |
| `model_id` | text | Embedding model identifier |
| `model_version` | text | Model version |
| `prompt_version_id` | uuid nullable | If derived from prompt context |
| `classification` | text | Data classification |
| `metadata` | jsonb | Title, section, timestamps |
| `created_at` | timestamptz | |
| `deleted_at` | timestamptz | Soft delete |

**Indexes:** HNSW or IVFFlat on `embedding` with `organization_id` filter; `(organization_id, source_type, source_id)`

---

## Embedding Sources

| Source | Owning domain | Ingest trigger |
|--------|---------------|----------------|
| Investigation notes/evidence summaries | INVEST | Evidence attach / note create (authorized) |
| Compliance policies/knowledge | COMP / ADMIN | Admin upload / config |
| Risk explanation snippets | RISK | Optional index for retrieval |
| Historical case summaries | INVEST | Case close (V2 enrichment) |

AI does not embed data without classification review for restricted content.

---

## Retrieval Flow

```text
AI Retrieval Agent → AUTHZ check → pgvector similarity query
  FILTER organization_id + source_type + permission scope
  → Return chunks with source references → LLM context
```

Retrieval results include `sourceType`, `sourceId`, `chunkIndex` for provenance (AI-FR-005).

---

## Tenant Isolation

- Every query includes `organization_id = :orgId`
- Cross-tenant retrieval prohibited at SQL and application layer
- Service accounts scoped per org for batch embedding jobs

---

## Model and Re-embedding

| Event | Action |
|-------|--------|
| Model version change | Background re-embed job; dual-write period |
| Source document update | Invalidate chunks by `content_hash`; re-embed |
| Source delete | Soft-delete embeddings (`deleted_at`) |
| Eval failure (V2) | Block prompt promotion |

Track `model_id` + `model_version` on each chunk (NFR-AI-004).

---

## Deletion Behavior

- Cascade soft-delete when source domain record deleted/archived
- Hard delete after retention period
- Vector data subject to same classification as source

---

## Evaluation Metadata (V2)

Table `ai.retrieval_eval_results`: query, expected sources, retrieved IDs, score, pass/fail—for AI-FR evaluation framework (V2).

---

## Limits and Failure

| Condition | Behavior |
|-----------|----------|
| pgvector unavailable | AI retrieval degrades; deterministic domains unaffected |
| Empty retrieval | AI response states insufficient evidence |
| Hallucination risk | Ground answers in retrieved chunks only (AI-FR-005) |

---

## Related Documents

- [PostgreSQL.md](PostgreSQL.md) — `ai` schema
- [AI-FR-003](../02-requirements/FunctionalRequirements.md) — Retrieval Agent
