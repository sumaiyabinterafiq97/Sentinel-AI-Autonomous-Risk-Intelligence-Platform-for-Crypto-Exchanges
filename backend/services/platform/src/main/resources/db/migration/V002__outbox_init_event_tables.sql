-- M3 transactional outbox for sentinel-platform (ADR-015).
-- InitialMigrationSpecifications does not assign an outbox table (logical 006 is RISK/M4).
-- Placed in CORE schema as the platform-deployable outbox. Not domain source of truth.

CREATE TABLE core.outbox_events (
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
    CONSTRAINT chk_core_outbox_status CHECK (status IN ('PENDING', 'PUBLISHING', 'PUBLISHED', 'DEAD'))
);

CREATE INDEX idx_core_outbox_relay
    ON core.outbox_events (status, next_attempt_at, created_at)
    WHERE published_at IS NULL;

CREATE INDEX idx_core_outbox_org_created
    ON core.outbox_events (organization_id, created_at);
