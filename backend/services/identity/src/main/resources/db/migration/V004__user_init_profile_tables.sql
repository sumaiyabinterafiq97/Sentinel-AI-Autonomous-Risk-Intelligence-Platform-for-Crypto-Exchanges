-- Migration 004 — USER schema init

CREATE SCHEMA IF NOT EXISTS "user";

CREATE TABLE "user".users (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    email TEXT NOT NULL,
    display_name TEXT NOT NULL,
    status TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ NULL,
    created_by UUID NULL,
    updated_by UUID NULL
);

CREATE UNIQUE INDEX uq_users_org_email_active
    ON "user".users (organization_id, email)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_users_org_status ON "user".users (organization_id, status);
CREATE INDEX idx_users_org_email ON "user".users (organization_id, email);

CREATE TABLE "user".user_profiles (
    user_id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    attributes JSONB NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ NOT NULL
);
