-- Migration 012 — DASH presentation tables (non-authoritative).
CREATE SCHEMA IF NOT EXISTS dash;

CREATE TABLE dash.workspace_preferences (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    organization_id uuid NOT NULL,
    preferences jsonb NOT NULL DEFAULT '{}'::jsonb,
    updated_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL,
    UNIQUE (user_id, organization_id)
);

CREATE TABLE dash.widget_interactions (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    organization_id uuid NOT NULL,
    widget_id text NOT NULL,
    interaction_type text NOT NULL,
    created_at timestamptz NOT NULL,
    metadata jsonb
);

CREATE INDEX dash_widget_interactions_org_idx
    ON dash.widget_interactions (organization_id, widget_id, created_at DESC);

CREATE TABLE dash.workspace_projection_cache (
    id uuid PRIMARY KEY,
    organization_id uuid NOT NULL,
    projection_key text NOT NULL,
    payload jsonb NOT NULL,
    expires_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    UNIQUE (organization_id, projection_key)
);
