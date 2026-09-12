-- Migration 006 — RISK schema init (docs/04-database/InitialMigrationSpecifications.md)
-- Owner: RISK / sentinel-ops. No alert/invest tables. No cross-schema FKs.
-- risk.outbox_events is the M3 per-deployable outbox (ADR-015); not domain SoT.

CREATE SCHEMA IF NOT EXISTS risk;

CREATE TABLE risk.risk_rules (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name TEXT NOT NULL,
    definition JSONB NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID NULL,
    updated_by UUID NULL
);

CREATE UNIQUE INDEX uq_risk_rules_org_name_version
    ON risk.risk_rules (organization_id, name, version);

CREATE INDEX idx_risk_rules_org_enabled ON risk.risk_rules (organization_id, enabled);
CREATE INDEX idx_risk_rules_org_name ON risk.risk_rules (organization_id, name);

CREATE TABLE risk.risk_assessments (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    score NUMERIC NOT NULL,
    risk_level TEXT NOT NULL,
    explanation_summary TEXT NULL,
    evaluated_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    correlation_id UUID NULL
);

CREATE INDEX idx_risk_assessments_entity
    ON risk.risk_assessments (organization_id, entity_type, entity_id, evaluated_at DESC);

CREATE INDEX idx_risk_assessments_org_evaluated
    ON risk.risk_assessments (organization_id, evaluated_at DESC);

CREATE TABLE risk.risk_rule_hits (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL,
    rule_id UUID NOT NULL,
    hit_details JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_risk_rule_hits_assessment ON risk.risk_rule_hits (assessment_id);
CREATE INDEX idx_risk_rule_hits_rule ON risk.risk_rule_hits (rule_id);

CREATE TABLE risk.transaction_ingest_log (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    external_tx_id TEXT NOT NULL,
    assessment_id UUID NULL,
    ingested_at TIMESTAMPTZ NOT NULL,
    idempotency_key TEXT NULL
);

CREATE UNIQUE INDEX uq_risk_ingest_org_tx
    ON risk.transaction_ingest_log (organization_id, external_tx_id);

CREATE TABLE risk.outbox_events (
    event_id UUID PRIMARY KEY,
    event_type TEXT NOT NULL,
    schema_version TEXT NOT NULL,
    organization_id UUID NOT NULL,
    producer TEXT NOT NULL,
    correlation_id UUID NULL,
    envelope JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    status TEXT NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL,
    claimed_at TIMESTAMPTZ NULL,
    published_at TIMESTAMPTZ NULL,
    last_error TEXT NULL,
    CONSTRAINT chk_risk_outbox_status CHECK (status IN ('PENDING', 'PUBLISHING', 'PUBLISHED', 'DEAD'))
);

CREATE INDEX idx_risk_outbox_relay
    ON risk.outbox_events (status, next_attempt_at, created_at)
    WHERE published_at IS NULL;

CREATE INDEX idx_risk_outbox_org_created
    ON risk.outbox_events (organization_id, created_at);
