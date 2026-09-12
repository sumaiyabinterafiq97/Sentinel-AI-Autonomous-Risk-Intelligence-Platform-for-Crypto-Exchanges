-- Migration 003 — AUTHZ schema init

CREATE SCHEMA IF NOT EXISTS authz;

CREATE TABLE authz.permissions (
    id UUID PRIMARY KEY,
    code TEXT NOT NULL,
    description TEXT NULL,
    domain TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_permissions_code UNIQUE (code)
);

CREATE TABLE authz.roles (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name TEXT NOT NULL,
    description TEXT NULL,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID NULL,
    CONSTRAINT uq_roles_org_name UNIQUE (organization_id, name)
);

CREATE INDEX idx_roles_org_name ON authz.roles (organization_id, name);

CREATE TABLE authz.role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE authz.user_role_assignments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL,
    assigned_by UUID NULL,
    revoked_at TIMESTAMPTZ NULL
);

CREATE INDEX idx_assignments_user_org_active
    ON authz.user_role_assignments (user_id, organization_id)
    WHERE revoked_at IS NULL;

CREATE UNIQUE INDEX uq_assignments_active
    ON authz.user_role_assignments (user_id, role_id, organization_id)
    WHERE revoked_at IS NULL;
