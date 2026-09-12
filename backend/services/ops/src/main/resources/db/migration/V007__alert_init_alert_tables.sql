-- Migration 007 — ALERT schema init (docs/04-database/InitialMigrationSpecifications.md)
-- Owner: ALERT / sentinel-ops. No INVEST/COMP tables. No RISK mutation. No new outbox table
-- (ops deployable continues to use risk.outbox_events from M4 / ADR-015).

CREATE SCHEMA IF NOT EXISTS alert;

CREATE TABLE alert.alerts (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    status TEXT NOT NULL,
    priority INTEGER NOT NULL,
    title TEXT NOT NULL,
    risk_assessment_id UUID NULL,
    assigned_to UUID NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    closed_at TIMESTAMPTZ NULL,
    closed_by UUID NULL,
    disposition_reason TEXT NULL,
    created_by UUID NULL,
    CONSTRAINT chk_alert_status CHECK (status IN ('open', 'triaged', 'assigned', 'closed'))
);

CREATE INDEX idx_alert_queue
    ON alert.alerts (organization_id, status, priority DESC, created_at DESC);

CREATE TABLE alert.alert_risk_context (
    alert_id UUID PRIMARY KEY,
    risk_score NUMERIC NULL,
    risk_level TEXT NULL,
    context JSONB NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE alert.alert_comments (
    id UUID PRIMARY KEY,
    alert_id UUID NOT NULL,
    author_id UUID NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_alert_comments_alert_created
    ON alert.alert_comments (alert_id, created_at DESC);

CREATE TABLE alert.alert_investigation_links (
    alert_id UUID NOT NULL,
    case_id UUID NOT NULL,
    linked_at TIMESTAMPTZ NOT NULL,
    linked_by UUID NULL,
    PRIMARY KEY (alert_id, case_id)
);
