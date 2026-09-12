-- Migration 011 — ADMIN schema init (docs/04-database/InitialMigrationSpecifications.md)
-- Owner: ADMIN / sentinel-platform. No USER/ORG entity tables. No rewrite of 001–010.

CREATE SCHEMA IF NOT EXISTS admin;

CREATE TABLE admin.admin_settings (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    setting_key TEXT NOT NULL,
    setting_value JSONB NOT NULL,
    updated_by UUID NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uq_admin_settings_org_key
    ON admin.admin_settings (organization_id, setting_key);

CREATE INDEX idx_admin_settings_org ON admin.admin_settings (organization_id);

CREATE TABLE admin.integration_configs (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    integration_type TEXT NOT NULL,
    config JSONB NOT NULL,
    secret_ref TEXT NULL,
    status TEXT NOT NULL,
    configured_by UUID NULL,
    configured_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uq_admin_integration_active
    ON admin.integration_configs (organization_id, integration_type)
    WHERE status = 'active';

CREATE INDEX idx_admin_integrations_org ON admin.integration_configs (organization_id, integration_type);

CREATE TABLE admin.admin_action_log (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    action TEXT NOT NULL,
    target_type TEXT NOT NULL,
    target_id UUID NULL,
    actor_id UUID NOT NULL,
    outcome TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    metadata JSONB NULL
);

CREATE INDEX idx_admin_action_log_org_created
    ON admin.admin_action_log (organization_id, created_at DESC);

CREATE INDEX idx_admin_action_log_actor_created
    ON admin.admin_action_log (actor_id, created_at DESC);
