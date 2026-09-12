-- Migration 009 — COMP schema init (docs/04-database/InitialMigrationSpecifications.md)
-- Owner: COMP / sentinel-ops. No AI/REPORT/SEC tables. No INVEST/ALERT/RISK mutation.

CREATE SCHEMA IF NOT EXISTS comp;

CREATE TABLE comp.kyc_reviews (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    status TEXT NOT NULL,
    outcome TEXT NULL,
    reviewed_by UUID NULL,
    completed_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    correlation_id UUID NULL,
    CONSTRAINT chk_kyc_status CHECK (status IN ('open', 'in_review', 'approved', 'rejected', 'info_requested'))
);

CREATE INDEX idx_comp_kyc_org_status_updated
    ON comp.kyc_reviews (organization_id, status, updated_at DESC);

CREATE INDEX idx_comp_kyc_org_user
    ON comp.kyc_reviews (organization_id, user_id);

CREATE TABLE comp.aml_reviews (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    subject_ref TEXT NOT NULL,
    case_id UUID NULL,
    status TEXT NOT NULL,
    outcome TEXT NULL,
    reviewed_by UUID NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_comp_aml_org_status ON comp.aml_reviews (organization_id, status);

CREATE INDEX idx_comp_aml_case ON comp.aml_reviews (case_id) WHERE case_id IS NOT NULL;

CREATE TABLE comp.travel_rule_validations (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    transaction_ref TEXT NOT NULL,
    status TEXT NOT NULL,
    validation_result JSONB NOT NULL DEFAULT '{}',
    validated_at TIMESTAMPTZ NOT NULL,
    validated_by UUID NULL
);

CREATE UNIQUE INDEX uq_comp_travel_org_tx_validated
    ON comp.travel_rule_validations (organization_id, transaction_ref, validated_at);

CREATE TABLE comp.sanctions_screenings (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    subject_ref TEXT NOT NULL,
    match_status TEXT NOT NULL,
    disposition TEXT NULL,
    screened_at TIMESTAMPTZ NOT NULL,
    disposed_by UUID NULL,
    disposed_at TIMESTAMPTZ NULL
);

CREATE INDEX idx_comp_sanctions_org_match_screened
    ON comp.sanctions_screenings (organization_id, match_status, screened_at DESC);

CREATE TABLE comp.audit_packages (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    package_type TEXT NOT NULL,
    status TEXT NOT NULL,
    artifact_refs JSONB NOT NULL DEFAULT '[]',
    prepared_by UUID NULL,
    prepared_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_audit_pkg_status CHECK (status IN ('requested', 'preparing', 'ready', 'failed'))
);

CREATE TABLE comp.compliance_records (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    record_type TEXT NOT NULL,
    source_id UUID NOT NULL,
    status TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_comp_records_org_type_created
    ON comp.compliance_records (organization_id, record_type, created_at DESC);
