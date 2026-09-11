# Initial Migration Specifications — CORE through DASH (Full MVP Path)

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Initial Migration Specifications |
| Version | 1.3 (Draft) |
| Status | Draft — Phase 7 (COMP, AI, ADMIN, DASH added) |
| Last Updated | 2026-09-03 |

---

## Purpose

Logical migration sequence for first two domain schemas. **Documentation only — no SQL files.**

Reference: [PostgreSQL.md](PostgreSQL.md), [MigrationStrategy.md](MigrationStrategy.md)

---

## Migration 001 — CORE Schema Init

**ID:** `001_core_init_platform_tables`  
**Owner:** Platform Engineering  
**Depends on:** none

### Actions (logical)

1. Create schema `core`
2. Create table `core.platform_config`
3. Create table `core.feature_flags`
4. Create table `core.audit_records`
5. Create table `core.platform_health_snapshots`

### Table: `core.platform_config`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | nullable (null = global) |
| config_key | text | NOT NULL |
| config_value | jsonb | NOT NULL |
| version | integer | NOT NULL default 1 |
| created_at | timestamptz | NOT NULL |
| updated_at | timestamptz | NOT NULL |
| created_by | uuid | nullable |
| updated_by | uuid | nullable |

**Unique:** `(organization_id, config_key)` where soft-delete N/A

**Indexes:** `(organization_id, config_key)`

**FR trace:** CORE-FR-007, CORE-FR-008

---

### Table: `core.feature_flags`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | nullable |
| flag_key | text | NOT NULL |
| enabled | boolean | NOT NULL default false |
| metadata | jsonb | |
| updated_at | timestamptz | NOT NULL |

**Unique:** `(organization_id, flag_key)`

**FR trace:** CORE-FR-009, CORE-FR-010

---

### Table: `core.audit_records`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| actor_id | uuid | nullable |
| actor_type | text | NOT NULL |
| action | text | NOT NULL |
| resource_type | text | NOT NULL |
| resource_id | uuid | nullable |
| outcome | text | NOT NULL |
| correlation_id | uuid | nullable |
| request_id | uuid | nullable |
| metadata | jsonb | |
| created_at | timestamptz | NOT NULL |

**Indexes:** `(organization_id, created_at desc)`, `(correlation_id)`, `(resource_type, resource_id)`

**Retention:** long-term; no hard delete (NFR-AUD-001)

**FR trace:** CORE-FR-012, CORE-FR-013, CORE-FR-014

---

### Table: `core.platform_health_snapshots`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| component | text | NOT NULL |
| status | text | NOT NULL |
| recorded_at | timestamptz | NOT NULL |
| metadata | jsonb | |

**FR trace:** CORE-FR-004, CORE-FR-023

---

## Migration 002 — AUTH Schema Init

**ID:** `002_auth_init_session_tables`  
**Owner:** Identity & Access Engineering  
**Depends on:** `001_core_init_platform_tables` (audit integration contract)

### Actions (logical)

1. Create schema `auth`
2. Create table `auth.sessions`
3. Create table `auth.refresh_tokens`
4. Create table `auth.auth_events`
5. Create table `auth.registered_devices`

### Table: `auth.sessions`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| user_id | uuid | NOT NULL |
| organization_id | uuid | NOT NULL |
| status | text | NOT NULL |
| expires_at | timestamptz | NOT NULL |
| mfa_verified | boolean | NOT NULL default false |
| device_id | uuid | nullable |
| created_at | timestamptz | NOT NULL |
| updated_at | timestamptz | NOT NULL |

**Indexes:** `(user_id, status)`, `(expires_at)` where status=active

**Note:** `user_id` references USER domain logically — **no FK** to `user.users`

**FR trace:** AUTH-FR-002, AUTH-FR-004

**Events:** Login success → `UserLoggedIn`; expiry → `SessionExpired` (not HTTP)

---

### Table: `auth.refresh_tokens`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| session_id | uuid | NOT NULL |
| token_hash | text | NOT NULL |
| expires_at | timestamptz | NOT NULL |
| revoked_at | timestamptz | nullable |

**Indexes:** `(session_id)`, unique `(token_hash)`

**Security:** store hash only, never plaintext (NFR-SEC-005)

**FR trace:** AUTH-FR-002

---

### Table: `auth.auth_events`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| user_id | uuid | nullable |
| organization_id | uuid | nullable |
| event_type | text | NOT NULL |
| ip_address | text | nullable |
| created_at | timestamptz | NOT NULL |
| metadata | jsonb | |

**Classification:** internal/security

**FR trace:** AUTH-FR-001, AUTH-FR-003

---

### Table: `auth.registered_devices`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| user_id | uuid | NOT NULL |
| device_fingerprint | text | NOT NULL |
| registered_at | timestamptz | NOT NULL |
| status | text | NOT NULL |

**Unique:** `(user_id, device_fingerprint)`

**FR trace:** AUTH device registration FRs

**Boundary:** SEC consumes auth events; AUTH owns device registration — SEC does not replace AUTH

---

## Seed Data (002b optional)

No mandatory seed in AUTH. Permission catalog seeds belong to AUTHZ migration `003_authz_init`.

---

## Verification Checklist (implementation phase)

- [ ] Schemas created in order
- [ ] No cross-schema FKs
- [ ] Audit write path from AUTH to CORE audit API functional
- [ ] Session create/read respects organization_id
- [ ] Frozen AUTH FR behavior unchanged

---

## Related Documents

- [PostgreSQL.md](PostgreSQL.md)
- [FunctionalRequirements.md](../02-requirements/FunctionalRequirements.md) — AUTH, CORE chapters

---

## Migration 003 — AUTHZ Schema Init

**ID:** `003_authz_init_roles_permissions`  
**Owner:** Identity & Access Engineering  
**Depends on:** `002_auth_init_session_tables` (logical — sessions exist before role assignment at runtime)

### Purpose

Establish authorization catalog, roles, and user-role bindings. AUTHZ owns permission evaluation; AUTH owns authentication.

### Actions (logical)

1. Create schema `authz`
2. Create table `authz.permissions`
3. Create table `authz.roles`
4. Create table `authz.role_permissions`
5. Create table `authz.user_role_assignments`

### Table: `authz.permissions`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| code | text | NOT NULL |
| description | text | |
| domain | text | NOT NULL |
| created_at | timestamptz | NOT NULL |

**Unique:** `(code)` globally for permission catalog

**Seed/reference data:** MVP permission codes aligned to APIInventory permission strings (e.g. `alert:alert:read`) — seeded in migration 003b

**FR trace:** AUTHZ-FR-003, AUTHZ-FR-004

---

### Table: `authz.roles`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| name | text | NOT NULL |
| description | text | |
| is_system | boolean | NOT NULL default false |
| created_at | timestamptz | NOT NULL |
| updated_at | timestamptz | NOT NULL |
| created_by | uuid | nullable |

**Unique:** `(organization_id, name)`

**Indexes:** `(organization_id, name)`

**Tenant boundary:** All roles scoped by `organization_id`

**FR trace:** AUTHZ-FR-003

---

### Table: `authz.role_permissions`

| Column | Type | Constraints |
|--------|------|-------------|
| role_id | uuid | NOT NULL |
| permission_id | uuid | NOT NULL |

**Primary key:** `(role_id, permission_id)`

**Note:** No cross-schema FK to `user` — logical references only

---

### Table: `authz.user_role_assignments`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| user_id | uuid | NOT NULL |
| role_id | uuid | NOT NULL |
| organization_id | uuid | NOT NULL |
| assigned_at | timestamptz | NOT NULL |
| assigned_by | uuid | nullable |
| revoked_at | timestamptz | nullable |

**Indexes:** `(user_id, organization_id)` where revoked_at is null

**Unique:** `(user_id, role_id, organization_id)` where revoked_at is null

**Audit:** Assignment changes logged via CORE audit API

**FR trace:** AUTHZ-FR-001, AUTHZ-FR-002

---

### Migration 003b — AUTHZ Seed (optional logical)

Seed global permission catalog and default system roles per organization template. **No executable SQL in Phase 5.**

**Validation criteria:**

- [ ] Permission evaluate API (AUTHZ-FR-001) can resolve role bindings
- [ ] Tenant isolation on role queries
- [ ] No AUTHZ ownership of user profile data

**Rollback strategy:** Drop `authz` schema only in non-production; forward-only in production per MigrationStrategy.md

---

## Migration 004 — USER Schema Init

**ID:** `004_user_init_profile_tables`  
**Owner:** Identity & Access Engineering  
**Depends on:** `003_authz_init_roles_permissions` (roles exist for assignment after user create)

### Purpose

Establish authoritative user lifecycle storage. USER owns user profile; AUTH references `user_id` logically.

### Actions (logical)

1. Create schema `user`
2. Create table `user.users`
3. Create table `user.user_profiles`

### Table: `user.users`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| email | text | NOT NULL |
| display_name | text | NOT NULL |
| status | text | NOT NULL |
| created_at | timestamptz | NOT NULL |
| updated_at | timestamptz | NOT NULL |
| deleted_at | timestamptz | nullable |
| created_by | uuid | nullable |
| updated_by | uuid | nullable |

**Unique:** `(organization_id, email)` where `deleted_at` is null

**Indexes:** `(organization_id, status)`, `(organization_id, email)`

**Soft delete:** `deleted_at` per USER-FR deactivation requirements

**Retention:** T4/T5 per DataRetention.md for active users; erasure subject to RET-OQ-001

**Events:** `UserCreated`, `UserUpdated`, `UserDeactivated` (domain logic, not HTTP)

**FR trace:** USER-FR-001, USER-FR-002, USER-FR-003, USER-FR-004

**Boundary:** ADMIN orchestrates via provision API — does not duplicate USER table ownership

---

### Table: `user.user_profiles`

| Column | Type | Constraints |
|--------|------|-------------|
| user_id | uuid | PK |
| organization_id | uuid | NOT NULL |
| attributes | jsonb | NOT NULL default '{}' |
| updated_at | timestamptz | NOT NULL |

**Classification:** Restricted — may contain PII

**FR trace:** USER-FR profile extension FRs

---

### Validation criteria

- [ ] User create/update respects organization_id tenant scope
- [ ] No cross-schema FK to `auth.sessions`
- [ ] COMP consumes `UserUpdated` per event contract

**Rollback strategy:** Forward-only in production

---

## Migration 005 — ORG Schema Init

**ID:** `005_org_init_tenant_tables`  
**Owner:** Platform Engineering  
**Depends on:** `004_user_init_profile_tables` (memberships reference users)

### Purpose

Establish tenant organization structure and membership. ORG owns organization lifecycle.

### Actions (logical)

1. Create schema `org`
2. Create table `org.organizations`
3. Create table `org.organization_memberships`

### Table: `org.organizations`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| name | text | NOT NULL |
| status | text | NOT NULL |
| settings | jsonb | NOT NULL default '{}' |
| parent_org_id | uuid | nullable |
| created_at | timestamptz | NOT NULL |
| updated_at | timestamptz | NOT NULL |
| created_by | uuid | nullable |

**Note:** `parent_org_id` reserved for V2 hierarchy — nullable in MVP

**Indexes:** `(status)`, `(name)` — global org registry for platform admin

**Events:** `OrganizationCreated`, `OrganizationUpdated`

**FR trace:** ORG-FR-001, ORG-FR-002, ORG-FR-003

---

### Table: `org.organization_memberships`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| user_id | uuid | NOT NULL |
| status | text | NOT NULL |
| joined_at | timestamptz | NOT NULL |
| updated_at | timestamptz | NOT NULL |

**Unique:** `(organization_id, user_id)` where status active

**Indexes:** `(user_id, organization_id)`, `(organization_id, status)`

**Tenant boundary:** Membership links user to tenant; AUTHZ uses organization context from session

**FR trace:** ORG-FR membership FRs

**Boundary:** ADMIN provision orchestrates — ORG owns membership records

---

### Validation criteria

- [ ] Organization create/update produces events
- [ ] All downstream domain tables can reference `organization_id`
- [ ] No duplicate org CRUD in ADMIN schema

**Rollback strategy:** Forward-only in production

---

## Migration Order Summary

| Order | Migration ID | Domain | Depends on |
|-------|--------------|--------|------------|
| 1 | 001_core_init_platform_tables | CORE | — |
| 2 | 002_auth_init_session_tables | AUTH | 001 |
| 3 | 003_authz_init_roles_permissions | AUTHZ | 002 |
| 4 | 003b_authz_seed_permissions | AUTHZ | 003 |
| 5 | 004_user_init_profile_tables | USER | 003 |
| 6 | 005_org_init_tenant_tables | ORG | 004 |
| 7 | 006_risk_init_assessment_tables | RISK | 005 |
| 8 | 007_alert_init_alert_tables | ALERT | 006 |
| 9 | 008_invest_init_case_tables | INVEST | 007 |
| 10 | 009_comp_init_compliance_tables | COMP | 008 |
| 11 | 010_ai_init_assistive_tables | AI | 008 |
| 12 | 011_admin_init_platform_admin_tables | ADMIN | 005 |
| 13 | 012_dash_init_presentation_tables | DASH | 005 |

---

## Migration 006 — RISK Schema Init

**ID:** `006_risk_init_assessment_tables`  
**Owner:** Risk Engineering  
**Depends on:** `005_org_init_tenant_tables` (tenant scope required)

### Purpose

Establish authoritative risk rule definitions, assessment results, rule hits, and transaction ingest idempotency. RISK owns risk scoring and explanation data. RISK **does not** create or own alert lifecycle records.

### Actions (logical)

1. Create schema `risk`
2. Create table `risk.risk_rules`
3. Create table `risk.risk_assessments`
4. Create table `risk.risk_rule_hits`
5. Create table `risk.transaction_ingest_log`

### Table: `risk.risk_rules`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| name | text | NOT NULL |
| definition | jsonb | NOT NULL |
| enabled | boolean | NOT NULL default true |
| version | integer | NOT NULL default 1 |
| created_at | timestamptz | NOT NULL |
| updated_at | timestamptz | NOT NULL |
| created_by | uuid | nullable |
| updated_by | uuid | nullable |

**Unique:** `(organization_id, name, version)` — versioned rule definitions

**Indexes:** `(organization_id, enabled)`, `(organization_id, name)`

**Lifecycle states:** enabled / disabled (soft via `enabled`; history via version)

**Retention:** T3 per DataRetention.md

**FR trace:** RISK-FR-002, RISK-FR-008

**Events:** RuleCreated, RuleUpdated (domain logic)

**API:** API-RISK-002, API-RISK-005, API-RISK-006

**NFR:** NFR-PERF-001 (ingest path), NFR-EXPL-001

**Security:** `risk:rule:read`, `risk:rule:write`; confidential classification

---

### Table: `risk.risk_assessments`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| entity_type | text | NOT NULL |
| entity_id | text | NOT NULL |
| score | numeric | NOT NULL |
| risk_level | text | NOT NULL |
| explanation_summary | text | nullable |
| evaluated_at | timestamptz | NOT NULL |
| created_at | timestamptz | NOT NULL |
| correlation_id | uuid | nullable |

**Indexes:** `(organization_id, entity_type, entity_id, evaluated_at desc)`, `(organization_id, evaluated_at desc)`

**Idempotency:** New assessment per evaluation cycle; ingest links via `transaction_ingest_log`

**Events after commit:** `RiskCalculated`; `HighRiskDetected` when threshold met

**FR trace:** RISK-FR-001, RISK-FR-003, RISK-FR-010

**API:** API-RISK-001 (ingest), API-RISK-003, API-RISK-004, API-RISK-007

**Boundary:** ALERT consumes events — RISK does not write to `alert.*`

**Cross-domain ref:** `entity_id` may reference external transaction/user IDs — UUID logical refs only, no FK

---

### Table: `risk.risk_rule_hits`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| assessment_id | uuid | NOT NULL |
| rule_id | uuid | NOT NULL |
| hit_details | jsonb | NOT NULL |
| created_at | timestamptz | NOT NULL |

**Indexes:** `(assessment_id)`, `(rule_id)`

**Note:** `assessment_id` references `risk.risk_assessments.id` within schema only

**FR trace:** RISK-FR-003

---

### Table: `risk.transaction_ingest_log`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| external_tx_id | text | NOT NULL |
| assessment_id | uuid | nullable |
| ingested_at | timestamptz | NOT NULL |
| idempotency_key | text | nullable |

**Unique:** `(organization_id, external_tx_id)`

**Purpose:** Idempotent transaction ingest (API-RISK-001 Idempotency-Key)

**Retention:** T3

**FR trace:** RISK-FR-009

---

### Validation criteria

- [ ] Assessment commit precedes `RiskCalculated` publish (outbox pattern)
- [ ] No tables in `alert` schema created by RISK migration
- [ ] Tenant isolation on all queries
- [ ] P95 ingest-to-assessment path testable (NFR-PERF-001 simulation target)

**Rollback strategy:** Forward-only in production; drop schema only in dev

---

## Migration 007 — ALERT Schema Init

**ID:** `007_alert_init_alert_tables`  
**Owner:** Alert Operations Engineering  
**Depends on:** `006_risk_init_assessment_tables` (logical — `risk_assessment_id` reference)

### Purpose

Establish alert lifecycle storage. ALERT owns alert status, disposition, assignment, and **queue priority**. RISK provides context via events and optional `risk_assessment_id` reference — ALERT does not defer priority to RISK.

### Actions (logical)

1. Create schema `alert`
2. Create table `alert.alerts`
3. Create table `alert.alert_risk_context`
4. Create table `alert.alert_comments`
5. Create table `alert.alert_investigation_links`

### Table: `alert.alerts`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| status | text | NOT NULL |
| priority | integer | NOT NULL |
| title | text | NOT NULL |
| risk_assessment_id | uuid | nullable |
| assigned_to | uuid | nullable |
| created_at | timestamptz | NOT NULL |
| updated_at | timestamptz | NOT NULL |
| closed_at | timestamptz | nullable |
| closed_by | uuid | nullable |
| disposition_reason | text | nullable |
| created_by | uuid | nullable |

**Indexes:** `(organization_id, status, priority desc, created_at desc)` — primary queue index

**Lifecycle states:** open → triaged → assigned → closed (per ALERT-FR)

**Unique:** None on business key — `id` is authoritative

**Retention:** T3 online / T3 archive per DataRetention.md

**Events:** `AlertCreated`, `AlertAssigned`, `AlertClosed`

**FR trace:** ALERT-FR-003–009, ALERT-FR-011 (event consumer)

**API:** API-ALERT-001–006

**NFR:** NFR-PERF-003 (queue load via DASH), NFR-AUD-001

**Security:** `alert:*` permissions; confidential

**Boundary:** ALERT consumes `RiskCalculated`, `HighRiskDetected` only — does not mutate RISK tables

**Audit:** Status/priority/assignment changes → CORE audit API

---

### Table: `alert.alert_risk_context`

| Column | Type | Constraints |
|--------|------|-------------|
| alert_id | uuid | PK |
| risk_score | numeric | nullable |
| risk_level | text | nullable |
| context | jsonb | NOT NULL default '{}' |
| updated_at | timestamptz | NOT NULL |

**Purpose:** Denormalized read context for queue display — sourced from RISK events/API, owned by ALERT for alert presentation

**Note:** Not authoritative for risk — `risk.risk_assessments` remains source of truth

---

### Table: `alert.alert_comments`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| alert_id | uuid | NOT NULL |
| author_id | uuid | NOT NULL |
| content | text | NOT NULL |
| created_at | timestamptz | NOT NULL |

**Indexes:** `(alert_id, created_at desc)`

**FR trace:** ALERT-FR-007

**API:** API-ALERT-005

---

### Table: `alert.alert_investigation_links`

| Column | Type | Constraints |
|--------|------|-------------|
| alert_id | uuid | NOT NULL |
| case_id | uuid | NOT NULL |
| linked_at | timestamptz | NOT NULL |
| linked_by | uuid | nullable |

**Primary key:** `(alert_id, case_id)`

**Purpose:** Association only — INVEST owns case lifecycle

**Boundary:** Link creation via INVEST or ALERT API per FRS — no case data stored here

---

### Validation criteria

- [ ] Priority assigned by ALERT domain logic, not copied blindly from RISK `prioritySignal`
- [ ] AlertCreated published after alert row commit
- [ ] SEC (V2) can consume AlertCreated without schema change
- [ ] Cursor pagination on queue index performs within simulation targets

**Rollback strategy:** Forward-only in production

---

## Migration 008 — INVEST Schema Init

**ID:** `008_invest_init_case_tables`  
**Owner:** Investigation Engineering  
**Depends on:** `007_alert_init_alert_tables` (optional `source_alert_id` reference)

### Purpose

Establish investigation case lifecycle, evidence metadata, timeline, and notes. INVEST owns cases and evidence. AI may consume `EvidenceAttached` — AI does not own case records.

### Actions (logical)

1. Create schema `invest`
2. Create table `invest.investigation_cases`
3. Create table `invest.case_evidence`
4. Create table `invest.case_timeline_events`
5. Create table `invest.case_notes`
6. Create table `invest.case_links`

### Table: `invest.investigation_cases`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| status | text | NOT NULL |
| title | text | NOT NULL |
| priority | integer | nullable |
| assigned_to | uuid | nullable |
| source_alert_id | uuid | nullable |
| opened_at | timestamptz | NOT NULL |
| closed_at | timestamptz | nullable |
| closed_by | uuid | nullable |
| outcome | text | nullable |
| created_at | timestamptz | NOT NULL |
| updated_at | timestamptz | NOT NULL |
| created_by | uuid | nullable |

**Indexes:** `(organization_id, status, updated_at desc)`, `(source_alert_id)` where not null

**Lifecycle states:** open → in_progress → pending_review → closed (per INVEST-FR)

**Retention:** T5 per DataRetention.md

**Events:** `CaseCreated`, `CaseUpdated`, `CaseClosed`, `CaseAssigned`

**FR trace:** INVEST-FR-001–007, INVEST-FR-008 (event consumer)

**API:** API-INVEST-001–009

**NFR:** NFR-PERF-004, NFR-AUD-001, NFR-AUD-002

**Security:** Restricted classification; `invest:*` permissions

**Boundary:** INVEST consumes `AlertCreated`, `RiskCalculated` — does not create alerts

**Cross-domain ref:** `source_alert_id` → `alert.alerts.id` logical UUID only

---

### Table: `invest.case_evidence`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| case_id | uuid | NOT NULL |
| evidence_type | text | NOT NULL |
| reference_type | text | NOT NULL |
| reference_id | text | NOT NULL |
| metadata | jsonb | NOT NULL default '{}' |
| classification | text | NOT NULL |
| attached_by | uuid | NOT NULL |
| attached_at | timestamptz | NOT NULL |

**Indexes:** `(case_id, attached_at desc)`

**Events:** `EvidenceAttached` after commit

**FR trace:** INVEST-FR-003, INVEST-FR-005

**API:** API-INVEST-005

**Security:** NFR-SEC-006 field classification; SEC **does not** consume this event

---

### Table: `invest.case_timeline_events`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| case_id | uuid | NOT NULL |
| event_type | text | NOT NULL |
| description | text | NOT NULL |
| occurred_at | timestamptz | NOT NULL |
| actor_id | uuid | nullable |

**Indexes:** `(case_id, occurred_at desc)`

**FR trace:** INVEST-FR timeline FRs

---

### Table: `invest.case_notes`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| case_id | uuid | NOT NULL |
| author_id | uuid | NOT NULL |
| content | text | NOT NULL |
| created_at | timestamptz | NOT NULL |

**Indexes:** `(case_id, created_at desc)`

**FR trace:** INVEST-FR-008

**API:** API-INVEST-008

---

### Table: `invest.case_links`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| case_id | uuid | NOT NULL |
| linked_case_id | uuid | NOT NULL |
| link_type | text | NOT NULL |
| created_at | timestamptz | NOT NULL |
| created_by | uuid | nullable |

**Unique:** `(case_id, linked_case_id, link_type)`

**FR trace:** INVEST-FR-008, INVEST-FR-009

**API:** API-INVEST-009

---

### Validation criteria

- [ ] CaseCreated published after case commit; SEC (V2) consumption compatible
- [ ] EvidenceAttached does not trigger SEC consumption paths
- [ ] Case close produces CaseClosed for COMP consumer
- [ ] AI reads evidence via authorized tools — no direct case ownership

**Rollback strategy:** Forward-only in production

---

## Migration 009 — COMP Schema Init

**ID:** `009_comp_init_compliance_tables`  
**Owner:** Compliance Engineering  
**Depends on:** `008_invest_init_case_tables` (case context references); `004_user_init_profile_tables` (subject `user_id` logical refs)

### Purpose

Establish authoritative compliance workflow storage for KYC, AML, Travel Rule, sanctions, and audit package preparation. COMP owns compliance outcomes. Humans approve/reject — AI remains assistive only.

### Actions (logical)

1. Create schema `comp`
2. Create table `comp.kyc_reviews`
3. Create table `comp.aml_reviews`
4. Create table `comp.travel_rule_validations`
5. Create table `comp.sanctions_screenings`
6. Create table `comp.audit_packages`
7. Create table `comp.compliance_records`

### Table: `comp.kyc_reviews`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| user_id | uuid | NOT NULL |
| status | text | NOT NULL |
| outcome | text | nullable |
| reviewed_by | uuid | nullable |
| completed_at | timestamptz | nullable |
| created_at | timestamptz | NOT NULL |
| updated_at | timestamptz | NOT NULL |
| correlation_id | uuid | nullable |

**Lifecycle:** `open` → `in_review` → `approved` \| `rejected` \| `info_requested`

**Indexes:** `(organization_id, status, updated_at desc)`, `(organization_id, user_id)`

**Events:** `ComplianceReviewed` on decision

**FR:** COMP-FR-001 | **API:** API-COMP-001, API-COMP-002 | **Retention:** T6

---

### Table: `comp.aml_reviews`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| subject_ref | text | NOT NULL |
| case_id | uuid | nullable |
| status | text | NOT NULL |
| outcome | text | nullable |
| reviewed_by | uuid | nullable |
| created_at | timestamptz | NOT NULL |
| updated_at | timestamptz | NOT NULL |

**Indexes:** `(organization_id, status)`, `(case_id)` where not null

**Cross-domain ref:** `case_id` → INVEST logical UUID only — no FK

**FR:** COMP-FR-002 | **API:** API-COMP-003 | **Events:** `ComplianceReviewed`

---

### Table: `comp.travel_rule_validations`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| transaction_ref | text | NOT NULL |
| status | text | NOT NULL |
| validation_result | jsonb | NOT NULL default '{}' |
| validated_at | timestamptz | NOT NULL |
| validated_by | uuid | nullable |

**Unique:** `(organization_id, transaction_ref, validated_at)` for audit history; latest query by `validated_at desc`

**FR:** COMP-FR-003 | **API:** API-COMP-004 | **Events:** `TravelRuleValidated` | **Retention:** T6

---

### Table: `comp.sanctions_screenings`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| subject_ref | text | NOT NULL |
| match_status | text | NOT NULL |
| disposition | text | nullable |
| screened_at | timestamptz | NOT NULL |
| disposed_by | uuid | nullable |
| disposed_at | timestamptz | nullable |

**Indexes:** `(organization_id, match_status, screened_at desc)`

**FR:** COMP-FR-004 | **API:** API-COMP-005, API-COMP-006 | **Events:** `SanctionsHitDetected`, `ComplianceReviewed` on disposition

**Security:** Restricted; human disposition required for matches

---

### Table: `comp.audit_packages`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| package_type | text | NOT NULL |
| status | text | NOT NULL |
| artifact_refs | jsonb | NOT NULL default '[]' |
| prepared_by | uuid | nullable |
| prepared_at | timestamptz | nullable |
| created_at | timestamptz | NOT NULL |

**Lifecycle:** `requested` → `preparing` → `ready` \| `failed`

**FR:** COMP-FR-005 | **API:** API-COMP-007 | **Events:** `AuditPackagePrepared` | **NFR:** NFR-AUD-002

---

### Table: `comp.compliance_records`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| record_type | text | NOT NULL |
| source_id | uuid | NOT NULL |
| status | text | NOT NULL |
| created_at | timestamptz | NOT NULL |

**Purpose:** Cross-workflow compliance index for search and audit prep

**Indexes:** `(organization_id, record_type, created_at desc)`

---

### Boundary / integrity

- COMP does not own INVEST cases or ALERT records
- Consumes `CaseClosed`, `CaseUpdated`, `RiskCalculated`, `UserUpdated` (event-driven context only)
- No AI write path into `outcome` without human actor

### Validation criteria

- [ ] KYC decision requires `reviewed_by` human actor
- [ ] Events published after durable commit
- [ ] Tenant isolation on all queries
- [ ] Audit package async lifecycle testable

**Rollback strategy:** Forward-only in production

---

## Migration 010 — AI Schema Init

**ID:** `010_ai_init_assistive_tables`  
**Owner:** AI Platform Engineering  
**Depends on:** `008_invest_init_case_tables` (assist targets); `005_org_init_tenant_tables`

### Purpose

Establish assistive AI storage for prompts, recommendations, agent runs, and consumer idempotency. AI owns assistive artifacts only — **not** alerts, cases, compliance outcomes, or authorization decisions.

### Actions (logical)

1. Create schema `ai`
2. Create table `ai.prompts`
3. Create table `ai.prompt_versions`
4. Create table `ai.ai_recommendations`
5. Create table `ai.agent_runs`
6. Create table `ai.processed_event_ids`

### Table: `ai.prompts`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| agent_type | text | NOT NULL |
| name | text | NOT NULL |
| active_version_id | uuid | nullable |
| created_at | timestamptz | NOT NULL |
| updated_at | timestamptz | NOT NULL |

**Unique:** `(organization_id, agent_type, name)`

**FR:** AI-FR-004 | **API:** API-AI-005 | **Events:** `PromptUpdated`

---

### Table: `ai.prompt_versions`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| prompt_id | uuid | NOT NULL |
| version | integer | NOT NULL |
| content | text | NOT NULL |
| model_hint | text | nullable |
| created_by | uuid | nullable |
| created_at | timestamptz | NOT NULL |

**Unique:** `(prompt_id, version)`

**Audit:** Version history immutable after create

---

### Table: `ai.ai_recommendations`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| agent_type | text | NOT NULL |
| target_type | text | NOT NULL |
| target_id | text | NOT NULL |
| content | jsonb | NOT NULL |
| provenance | jsonb | NOT NULL default '{}' |
| prompt_version_id | uuid | nullable |
| model_id | text | nullable |
| status | text | NOT NULL |
| created_at | timestamptz | NOT NULL |

**Lifecycle:** `pending` → `completed` \| `partial` \| `failed`

**Indexes:** `(organization_id, target_type, target_id, created_at desc)`

**FR:** AI-FR-001, AI-FR-002, AI-FR-009 | **API:** API-AI-001–004 | **Events:** `AIRecommendationGenerated`

**Retention:** T7 | **NFR:** NFR-PERF-006 (P95 ≤ 5s / 10s timeout — simulation targets)

**Boundary:** `target_id` references ALERT/INVEST/RISK entities logically — AI never mutates those tables

---

### Table: `ai.agent_runs`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| recommendation_id | uuid | nullable |
| organization_id | uuid | NOT NULL |
| status | text | NOT NULL |
| latency_ms | integer | nullable |
| token_usage | jsonb | nullable |
| correlation_id | uuid | nullable |
| error_code | text | nullable |
| started_at | timestamptz | NOT NULL |
| completed_at | timestamptz | nullable |

**Indexes:** `(organization_id, started_at desc)`, `(correlation_id)`

**Note:** `AgentRunFailed` event deferred to V2/OPS consumer (GD-002) — run failure stored here for MVP observability

---

### Table: `ai.processed_event_ids`

| Column | Type | Constraints |
|--------|------|-------------|
| event_id | uuid | NOT NULL |
| consumer | text | NOT NULL |
| processed_at | timestamptz | NOT NULL |

**Primary key:** `(event_id, consumer)`

**Purpose:** At-least-once consumer idempotency for CaseUpdated, RiskCalculated, EvidenceAttached, AlertCreated

**Retention:** T0–T1 ephemeral operational

---

### Vector note

pgvector embeddings live in AI-owned vector store per VectorDataArchitecture.md — **not** authoritative business state; rebuildable from source docs.

### Validation criteria

- [ ] No tables that own alert/case/compliance lifecycle
- [ ] Recommendation create does not write ALERT/INVEST/COMP schemas
- [ ] Tool authorization enforceable (NFR-SEC-010)
- [ ] Timeout/degraded path leaves recommendation `failed` without blocking core workflows

**Rollback strategy:** Forward-only in production

---

## Migration 011 — ADMIN Schema Init

**ID:** `011_admin_init_platform_admin_tables`  
**Owner:** Platform Engineering  
**Depends on:** `005_org_init_tenant_tables`; consumes CORE audit API

### Purpose

Establish platform administration settings, integration configuration, and admin action log. ADMIN orchestrates USER/ORG provisioning — **does not** duplicate USER or ORG lifecycle tables.

### Actions (logical)

1. Create schema `admin`
2. Create table `admin.admin_settings`
3. Create table `admin.integration_configs`
4. Create table `admin.admin_action_log`

### Table: `admin.admin_settings`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| setting_key | text | NOT NULL |
| setting_value | jsonb | NOT NULL |
| updated_by | uuid | nullable |
| updated_at | timestamptz | NOT NULL |
| created_at | timestamptz | NOT NULL |

**Unique:** `(organization_id, setting_key)`

**FR:** ADMIN-FR-001 | **API:** API-ADMIN-001 | **Events:** `AdminSettingUpdated` | **Audit:** CORE audit on change

---

### Table: `admin.integration_configs`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| integration_type | text | NOT NULL |
| config | jsonb | NOT NULL |
| secret_ref | text | nullable |
| status | text | NOT NULL |
| configured_by | uuid | nullable |
| configured_at | timestamptz | NOT NULL |
| updated_at | timestamptz | NOT NULL |

**Unique:** `(organization_id, integration_type)` where status active (soft deactivate via status)

**Security:** Secrets via `secret_ref` only — never plaintext secrets in `config`

**FR:** ADMIN-FR-002 | **API:** API-ADMIN-002 | **Events:** `IntegrationConfigured`

---

### Table: `admin.admin_action_log`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| action | text | NOT NULL |
| target_type | text | NOT NULL |
| target_id | uuid | nullable |
| actor_id | uuid | NOT NULL |
| outcome | text | NOT NULL |
| created_at | timestamptz | NOT NULL |
| metadata | jsonb | nullable |

**Indexes:** `(organization_id, created_at desc)`, `(actor_id, created_at desc)`

**FR:** ADMIN-FR-003 | **Events:** `AdminActionPerformed` | **API:** API-ADMIN-005 reads CORE audit + this log

**Boundary:**

- User provision: API-ADMIN-003 → USER domain APIs (no `admin.users` table)
- Org provision: API-ADMIN-004 → ORG domain APIs (no `admin.organizations` table)

### Validation criteria

- [ ] No USER/ORG entity tables in `admin` schema
- [ ] Setting changes produce AdminSettingUpdated + CORE audit
- [ ] Integration secrets never stored plaintext
- [ ] Tenant isolation enforced

**Rollback strategy:** Forward-only in production

---

## Migration 012 — DASH Schema Init

**ID:** `012_dash_init_presentation_tables`  
**Owner:** Experience Engineering  
**Depends on:** `005_org_init_tenant_tables` (tenant + user context)

### Purpose

Establish presentation-only workspace preferences, widget interactions, and optional projection cache. DASH is a BFF/presentation domain — **not** authoritative for RISK, ALERT, INVEST, COMP, or AI business state.

### Actions (logical)

1. Create schema `dash`
2. Create table `dash.workspace_preferences`
3. Create table `dash.widget_interactions`
4. Create table `dash.workspace_projection_cache`

### Table: `dash.workspace_preferences`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| user_id | uuid | NOT NULL |
| organization_id | uuid | NOT NULL |
| preferences | jsonb | NOT NULL default '{}' |
| updated_at | timestamptz | NOT NULL |
| created_at | timestamptz | NOT NULL |

**Unique:** `(user_id, organization_id)`

**FR:** DASH-FR-001 | **API:** API-DASH-001 | **Retention:** Internal / user preference lifecycle

---

### Table: `dash.widget_interactions`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| user_id | uuid | NOT NULL |
| organization_id | uuid | NOT NULL |
| widget_id | text | NOT NULL |
| interaction_type | text | NOT NULL |
| created_at | timestamptz | NOT NULL |
| metadata | jsonb | nullable |

**Indexes:** `(organization_id, widget_id, created_at desc)`, `(user_id, created_at desc)`

**FR:** DASH-FR-007 | **API:** API-DASH-005 | **Audit:** Optional analytics — sensitive actions still via CORE

---

### Table: `dash.workspace_projection_cache`

| Column | Type | Constraints |
|--------|------|-------------|
| id | uuid | PK |
| organization_id | uuid | NOT NULL |
| projection_key | text | NOT NULL |
| payload | jsonb | NOT NULL |
| expires_at | timestamptz | NOT NULL |
| updated_at | timestamptz | NOT NULL |

**Unique:** `(organization_id, projection_key)`

**Purpose:** Optional read-model cache for widgets/queues — rebuildable from domain APIs/events

**SSE:** API-DASH-007 invalidates or refreshes projections; cache is non-authoritative (ADR-012 pattern)

**FR:** DASH-FR-002, DASH-FR-003, DASH-FR-006, DASH-FR-011

### Boundary / integrity

- No alert, case, risk assessment, or compliance outcome tables
- Queue data fetched via ALERT/INVEST/COMP APIs (or event-fed projections)
- Permission checks via AUTHZ; DASH does not redefine roles

### Validation criteria

- [ ] Projection cache TTL/expiry works; stale data never treated as SoT
- [ ] Widget interaction does not mutate upstream domains
- [ ] SSE subscription path does not require DASH-owned alert tables
- [ ] Tenant isolation on preferences and cache

**Rollback strategy:** Forward-only in production; cache rebuild preferred over restore

---

## Related Documents
