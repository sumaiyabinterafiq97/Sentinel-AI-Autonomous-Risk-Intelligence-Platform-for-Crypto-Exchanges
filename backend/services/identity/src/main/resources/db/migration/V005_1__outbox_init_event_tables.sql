-- M3 transactional outbox for sentinel-identity (ADR-015).
-- Identity Flyway version 5.1 (after ORG V005). Not logical RISK migration 006.
-- Table lives in AUTH schema because identity owns auth/user/org on this deployable.
-- Envelope JSON is the event payload; USER/ORG tables remain source of truth.

CREATE TABLE auth.outbox_events (
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
    CONSTRAINT chk_auth_outbox_status CHECK (status IN ('PENDING', 'PUBLISHING', 'PUBLISHED', 'DEAD'))
);

CREATE INDEX idx_auth_outbox_relay
    ON auth.outbox_events (status, next_attempt_at, created_at)
    WHERE published_at IS NULL;

CREATE INDEX idx_auth_outbox_org_created
    ON auth.outbox_events (organization_id, created_at);
