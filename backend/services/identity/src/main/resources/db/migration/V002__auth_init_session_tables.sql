-- Migration 002 — AUTH schema init (docs/04-database/InitialMigrationSpecifications.md)
-- Owner: AUTH / sentinel-identity. No cross-schema FKs.
-- Gap fill: auth.credentials holds password hashes because AUTH owns credentials (USER-FR-001)
-- and migration 002 did not list a credentials table. Hashes only; never plaintext.

CREATE SCHEMA IF NOT EXISTS auth;

CREATE TABLE auth.sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    status TEXT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    mfa_verified BOOLEAN NOT NULL DEFAULT FALSE,
    device_id UUID NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_sessions_user_status ON auth.sessions (user_id, status);
CREATE INDEX idx_sessions_expires_active ON auth.sessions (expires_at) WHERE status = 'active';

CREATE TABLE auth.refresh_tokens (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    token_hash TEXT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ NULL
);

CREATE INDEX idx_refresh_session ON auth.refresh_tokens (session_id);
CREATE UNIQUE INDEX uq_refresh_token_hash ON auth.refresh_tokens (token_hash);

CREATE TABLE auth.auth_events (
    id UUID PRIMARY KEY,
    user_id UUID NULL,
    organization_id UUID NULL,
    event_type TEXT NOT NULL,
    ip_address TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    metadata JSONB NULL
);

CREATE TABLE auth.registered_devices (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    device_fingerprint TEXT NOT NULL,
    registered_at TIMESTAMPTZ NOT NULL,
    status TEXT NOT NULL,
    CONSTRAINT uq_registered_device UNIQUE (user_id, device_fingerprint)
);

CREATE TABLE auth.credentials (
    user_id UUID PRIMARY KEY,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
