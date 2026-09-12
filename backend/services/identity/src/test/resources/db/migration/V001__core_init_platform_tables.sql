-- Migration 001 — CORE schema init (docs/04-database/InitialMigrationSpecifications.md)
-- Owner: CORE / sentinel-platform. No cross-schema FKs.

CREATE SCHEMA IF NOT EXISTS core;

CREATE TABLE core.platform_config (
    id UUID PRIMARY KEY,
    organization_id UUID NULL,
    config_key TEXT NOT NULL,
    config_value JSONB NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID NULL,
    updated_by UUID NULL
);

CREATE UNIQUE INDEX uq_platform_config_org_key
    ON core.platform_config ((COALESCE(organization_id, '00000000-0000-0000-0000-000000000000')), config_key);

CREATE INDEX idx_platform_config_org_key ON core.platform_config (organization_id, config_key);

CREATE TABLE core.feature_flags (
    id UUID PRIMARY KEY,
    organization_id UUID NULL,
    flag_key TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    metadata JSONB NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uq_feature_flags_org_key
    ON core.feature_flags ((COALESCE(organization_id, '00000000-0000-0000-0000-000000000000')), flag_key);

CREATE TABLE core.audit_records (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    actor_id UUID NULL,
    actor_type TEXT NOT NULL,
    action TEXT NOT NULL,
    resource_type TEXT NOT NULL,
    resource_id UUID NULL,
    outcome TEXT NOT NULL,
    correlation_id UUID NULL,
    request_id UUID NULL,
    metadata JSONB NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_audit_org_created ON core.audit_records (organization_id, created_at DESC);
CREATE INDEX idx_audit_correlation ON core.audit_records (correlation_id);
CREATE INDEX idx_audit_resource ON core.audit_records (resource_type, resource_id);

CREATE TABLE core.platform_health_snapshots (
    id UUID PRIMARY KEY,
    component TEXT NOT NULL,
    status TEXT NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL,
    metadata JSONB NULL
);
