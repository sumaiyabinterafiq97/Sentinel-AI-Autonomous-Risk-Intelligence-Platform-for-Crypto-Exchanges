-- Migration 010 — AI schema (assistive artifacts only).
-- Does not own alert, case, compliance, risk, user, or org lifecycle tables.
-- Embeddings are float8[] so local PostgreSQL can apply this file without the
-- pgvector extension. Production may map this column to pgvector (ADR-013).

CREATE SCHEMA IF NOT EXISTS ai;

CREATE TABLE ai.prompts (
    id uuid PRIMARY KEY,
    organization_id uuid NOT NULL,
    agent_type text NOT NULL,
    name text NOT NULL,
    active_version_id uuid,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    UNIQUE (organization_id, agent_type, name)
);

CREATE TABLE ai.prompt_versions (
    id uuid PRIMARY KEY,
    prompt_id uuid NOT NULL REFERENCES ai.prompts (id),
    version integer NOT NULL,
    content text NOT NULL,
    model_hint text,
    created_by uuid,
    created_at timestamptz NOT NULL,
    UNIQUE (prompt_id, version)
);

CREATE TABLE ai.ai_recommendations (
    id uuid PRIMARY KEY,
    organization_id uuid NOT NULL,
    agent_type text NOT NULL,
    target_type text NOT NULL,
    target_id text NOT NULL,
    content jsonb NOT NULL,
    provenance jsonb NOT NULL DEFAULT '{}'::jsonb,
    prompt_version_id uuid,
    model_id text,
    status text NOT NULL,
    created_at timestamptz NOT NULL
);

CREATE INDEX ai_recommendations_org_target_idx
    ON ai.ai_recommendations (organization_id, target_type, target_id, created_at DESC);

CREATE TABLE ai.agent_runs (
    id uuid PRIMARY KEY,
    recommendation_id uuid,
    organization_id uuid NOT NULL,
    status text NOT NULL,
    latency_ms integer,
    token_usage jsonb,
    correlation_id uuid,
    error_code text,
    started_at timestamptz NOT NULL,
    completed_at timestamptz
);

CREATE INDEX ai_agent_runs_org_started_idx ON ai.agent_runs (organization_id, started_at DESC);
CREATE INDEX ai_agent_runs_correlation_idx ON ai.agent_runs (correlation_id);

CREATE TABLE ai.processed_event_ids (
    event_id uuid NOT NULL,
    consumer text NOT NULL,
    processed_at timestamptz NOT NULL,
    PRIMARY KEY (event_id, consumer)
);

CREATE TABLE ai.document_embeddings (
    id uuid PRIMARY KEY,
    organization_id uuid NOT NULL,
    source_type text NOT NULL,
    source_id text NOT NULL,
    chunk_index integer NOT NULL,
    content_hash text NOT NULL,
    embedding float8[] NOT NULL,
    model_id text NOT NULL,
    model_version text NOT NULL,
    prompt_version_id uuid,
    classification text NOT NULL,
    metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
    created_at timestamptz NOT NULL,
    deleted_at timestamptz
);

CREATE INDEX ai_document_embeddings_org_source_idx
    ON ai.document_embeddings (organization_id, source_type, source_id);

CREATE TABLE ai.outbox_events (
    event_id uuid PRIMARY KEY,
    event_type text NOT NULL,
    schema_version text NOT NULL,
    payload jsonb NOT NULL,
    organization_id uuid NOT NULL,
    correlation_id uuid,
    stream text NOT NULL,
    status text NOT NULL,
    created_at timestamptz NOT NULL
);

CREATE TABLE ai.context_records (
    id uuid PRIMARY KEY,
    organization_id uuid NOT NULL,
    record_type text NOT NULL,
    record_id text NOT NULL,
    payload jsonb NOT NULL,
    updated_at timestamptz NOT NULL,
    UNIQUE (organization_id, record_type, record_id)
);
