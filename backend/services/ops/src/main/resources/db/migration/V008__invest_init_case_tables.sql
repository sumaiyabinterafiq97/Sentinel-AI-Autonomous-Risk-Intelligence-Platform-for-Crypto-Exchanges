-- Migration 008 — INVEST schema init (docs/04-database/InitialMigrationSpecifications.md)
-- Owner: INVEST / sentinel-ops. No COMP tables. No ALERT/RISK mutation. No new outbox table.

CREATE SCHEMA IF NOT EXISTS invest;

CREATE TABLE invest.investigation_cases (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    status TEXT NOT NULL,
    title TEXT NOT NULL,
    priority INTEGER NULL,
    assigned_to UUID NULL,
    source_alert_id UUID NULL,
    opened_at TIMESTAMPTZ NOT NULL,
    closed_at TIMESTAMPTZ NULL,
    closed_by UUID NULL,
    outcome TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID NULL,
    CONSTRAINT chk_invest_status CHECK (status IN ('open', 'in_progress', 'pending_review', 'closed'))
);

CREATE INDEX idx_invest_cases_org_status_updated
    ON invest.investigation_cases (organization_id, status, updated_at DESC);

CREATE INDEX idx_invest_cases_source_alert
    ON invest.investigation_cases (source_alert_id)
    WHERE source_alert_id IS NOT NULL;

CREATE TABLE invest.case_evidence (
    id UUID PRIMARY KEY,
    case_id UUID NOT NULL,
    evidence_type TEXT NOT NULL,
    reference_type TEXT NOT NULL,
    reference_id TEXT NOT NULL,
    metadata JSONB NOT NULL DEFAULT '{}',
    classification TEXT NOT NULL,
    attached_by UUID NOT NULL,
    attached_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_invest_evidence_case_attached
    ON invest.case_evidence (case_id, attached_at DESC);

CREATE TABLE invest.case_timeline_events (
    id UUID PRIMARY KEY,
    case_id UUID NOT NULL,
    event_type TEXT NOT NULL,
    description TEXT NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    actor_id UUID NULL
);

CREATE INDEX idx_invest_timeline_case_occurred
    ON invest.case_timeline_events (case_id, occurred_at DESC);

CREATE TABLE invest.case_notes (
    id UUID PRIMARY KEY,
    case_id UUID NOT NULL,
    author_id UUID NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_invest_notes_case_created
    ON invest.case_notes (case_id, created_at DESC);

CREATE TABLE invest.case_links (
    id UUID PRIMARY KEY,
    case_id UUID NOT NULL,
    linked_case_id UUID NOT NULL,
    link_type TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    created_by UUID NULL
);

CREATE UNIQUE INDEX uq_invest_case_links
    ON invest.case_links (case_id, linked_case_id, link_type);
