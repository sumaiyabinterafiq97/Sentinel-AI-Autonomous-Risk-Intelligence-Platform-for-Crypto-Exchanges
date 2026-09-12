-- Migration 005 — ORG schema init

CREATE SCHEMA IF NOT EXISTS org;

CREATE TABLE org.organizations (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    status TEXT NOT NULL,
    settings JSONB NOT NULL DEFAULT '{}',
    parent_org_id UUID NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID NULL
);

CREATE INDEX idx_organizations_status ON org.organizations (status);
CREATE INDEX idx_organizations_name ON org.organizations (name);

CREATE TABLE org.organization_memberships (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    status TEXT NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uq_memberships_active
    ON org.organization_memberships (organization_id, user_id)
    WHERE status = 'active';

CREATE INDEX idx_memberships_user_org ON org.organization_memberships (user_id, organization_id);
CREATE INDEX idx_memberships_org_status ON org.organization_memberships (organization_id, status);
