# PostgreSQL Relational Model

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | PostgreSQL Schema Design |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 3 |
| Last Updated | 2026-09-03 |

---

## Purpose

Conceptual PostgreSQL relational model per domain. **No SQL migrations in Phase 3.**

Logical schema-per-domain on shared cluster for MVP. Physical separation optional at scale.

---

## Conventions

| Convention | Standard |
|------------|----------|
| Primary keys | `uuid` (`id`) |
| Tenant scope | `organization_id uuid NOT NULL` |
| Timestamps | `created_at`, `updated_at` timestamptz |
| Soft delete | `deleted_at timestamptz` where FR requires history |
| Status fields | `text` or enum type per domain |
| Audit columns | `created_by`, `updated_by` (user UUID) where applicable |
| Cross-domain refs | UUID only; no FK across schemas |

---

## Schema: `core`

| Table | Purpose | Key columns |
|-------|---------|-------------|
| `platform_config` | Active configuration | `id`, `config_key`, `config_value` (jsonb), `organization_id` (nullable for global), `version` |
| `feature_flags` | Feature flags | `id`, `flag_key`, `enabled`, `organization_id`, `metadata` |
| `audit_records` | Shared audit log | `id`, `organization_id`, `actor_id`, `actor_type`, `action`, `resource_type`, `resource_id`, `outcome`, `correlation_id`, `request_id`, `metadata`, `created_at` |
| `platform_health_snapshots` | Health history | `id`, `component`, `status`, `recorded_at` |

**Indexes:** `audit_records(organization_id, created_at)`, `audit_records(correlation_id)`, `feature_flags(organization_id, flag_key)` unique.

**FR mapping:** CORE-FR-007–014, CORE-FR-023–026

---

## Schema: `auth`

| Table | Purpose | Key columns |
|-------|---------|-------------|
| `sessions` | Active sessions | `id`, `user_id`, `organization_id`, `expires_at`, `mfa_verified`, `device_id`, `status` |
| `refresh_tokens` | Refresh token hashes | `id`, `session_id`, `token_hash`, `expires_at`, `revoked_at` |
| `auth_events` | Login/logout audit | `id`, `user_id`, `event_type`, `ip_address`, `created_at` |
| `registered_devices` | Device registration | `id`, `user_id`, `device_fingerprint`, `registered_at`, `status` |

**Indexes:** `sessions(user_id)`, `sessions(expires_at)` where status=active

**Events:** Login → `UserLoggedIn`; expiry → `SessionExpired`

**FR mapping:** AUTH-FR-001–020

---

## Schema: `authz`

| Table | Purpose | Key columns |
|-------|---------|-------------|
| `roles` | Role definitions | `id`, `organization_id`, `name`, `description` |
| `permissions` | Permission catalog | `id`, `code`, `description` |
| `role_permissions` | M2M | `role_id`, `permission_id` |
| `user_role_assignments` | User roles | `id`, `user_id`, `role_id`, `organization_id`, `assigned_at`, `assigned_by` |

**Indexes:** `user_role_assignments(user_id, organization_id)`, `roles(organization_id, name)` unique

**FR mapping:** AUTHZ-FR-001–015

---

## Schema: `user`

| Table | Purpose | Key columns |
|-------|---------|-------------|
| `users` | User profiles | `id`, `organization_id`, `email`, `display_name`, `status`, `deleted_at` |
| `user_profiles` | Extended profile | `user_id`, `attributes` (jsonb) |

**Indexes:** `users(organization_id, email)` unique where deleted_at is null

**Events:** `UserCreated`, `UserUpdated`, `UserDeactivated`

**FR mapping:** USER-FR-001–012

---

## Schema: `org`

| Table | Purpose | Key columns |
|-------|---------|-------------|
| `organizations` | Tenant orgs | `id`, `name`, `status`, `parent_org_id` (V2), `settings` (jsonb) |
| `organization_memberships` | User-org links | `id`, `organization_id`, `user_id`, `status` |

**FR mapping:** ORG-FR-001–011

---

## Schema: `risk`

| Table | Purpose | Key columns |
|-------|---------|-------------|
| `risk_rules` | Rule definitions | `id`, `organization_id`, `name`, `definition` (jsonb), `enabled`, `version` |
| `risk_assessments` | Assessment results | `id`, `organization_id`, `entity_type`, `entity_id`, `score`, `risk_level`, `explanation_summary`, `evaluated_at` |
| `risk_rule_hits` | Rule hit details | `id`, `assessment_id`, `rule_id`, `hit_details` (jsonb) |
| `transaction_ingest_log` | Ingest idempotency | `id`, `organization_id`, `external_tx_id`, `assessment_id`, `ingested_at` |

**Indexes:** `risk_assessments(organization_id, entity_type, entity_id, evaluated_at desc)`, `transaction_ingest_log(organization_id, external_tx_id)` unique

**Events after commit:** `RiskCalculated`, `HighRiskDetected`

**FR mapping:** RISK-FR-001–013

---

## Schema: `alert`

| Table | Purpose | Key columns |
|-------|---------|-------------|
| `alerts` | Alert records | `id`, `organization_id`, `status`, `priority`, `title`, `risk_assessment_id`, `assigned_to`, `closed_at`, `closed_by`, `disposition_reason` |
| `alert_risk_context` | Denormalized risk context | `alert_id`, `risk_score`, `risk_level`, `context` (jsonb) |
| `alert_investigation_links` | Case association | `alert_id`, `case_id`, `linked_at` |

**Indexes:** `alerts(organization_id, status, priority desc, created_at desc)` — queue index

**Events:** `AlertCreated`, `AlertAssigned`, `AlertClosed`

**FR mapping:** ALERT-FR-001–012

---

## Schema: `invest`

| Table | Purpose | Key columns |
|-------|---------|-------------|
| `investigation_cases` | Cases | `id`, `organization_id`, `status`, `title`, `priority`, `assigned_to`, `source_alert_id`, `opened_at`, `closed_at`, `closed_by` |
| `case_evidence` | Evidence metadata | `id`, `case_id`, `evidence_type`, `reference_type`, `reference_id`, `metadata` (jsonb), `attached_by`, `attached_at` |
| `case_timeline_events` | Timeline | `id`, `case_id`, `event_type`, `description`, `occurred_at`, `actor_id` |
| `case_notes` | Analyst notes | `id`, `case_id`, `author_id`, `content`, `created_at` |

**Indexes:** `investigation_cases(organization_id, status, updated_at desc)`, `case_evidence(case_id)`

**Events:** `CaseCreated`, `CaseUpdated`, `CaseClosed`, `CaseAssigned`, `EvidenceAttached`

**FR mapping:** INVEST-FR-001–010

---

## Schema: `comp`

| Table | Purpose | Key columns |
|-------|---------|-------------|
| `kyc_reviews` | KYC workflows | `id`, `organization_id`, `user_id`, `status`, `outcome`, `reviewed_by`, `completed_at` |
| `aml_reviews` | AML workflows | `id`, `organization_id`, `subject_ref`, `status`, `outcome` |
| `travel_rule_validations` | Travel Rule | `id`, `organization_id`, `transaction_ref`, `status`, `validation_result` (jsonb) |
| `sanctions_screenings` | Sanctions | `id`, `organization_id`, `subject_ref`, `match_status`, `disposition`, `screened_at` |
| `audit_packages` | Audit prep | `id`, `organization_id`, `package_type`, `status`, `artifact_refs` (jsonb) |
| `compliance_records` | Generic compliance index | `id`, `organization_id`, `record_type`, `source_id`, `status` |

**Events:** `ComplianceReviewed`, `TravelRuleValidated`, `SanctionsHitDetected`, `AuditPackagePrepared`

**FR mapping:** COMP-FR-001–010

---

## Schema: `ai`

| Table | Purpose | Key columns |
|-------|---------|-------------|
| `prompts` | Prompt definitions | `id`, `organization_id`, `agent_type`, `name`, `active_version_id` |
| `prompt_versions` | Version history | `id`, `prompt_id`, `version`, `content`, `model_hint`, `created_by`, `created_at` |
| `ai_recommendations` | Recommendations | `id`, `organization_id`, `agent_type`, `target_type`, `target_id`, `content`, `provenance` (jsonb), `prompt_version_id`, `model_id`, `status` |
| `agent_runs` | Run metadata | `id`, `recommendation_id`, `status`, `latency_ms`, `token_usage`, `correlation_id`, `started_at`, `completed_at` |
| `processed_event_ids` | Consumer idempotency | `event_id`, `consumer`, `processed_at` |

**FR mapping:** AI-FR-001–009

---

## Schema: `admin`

| Table | Purpose | Key columns |
|-------|---------|-------------|
| `admin_settings` | Settings | `id`, `organization_id`, `setting_key`, `setting_value` (jsonb), `updated_by` |
| `integration_configs` | Integrations | `id`, `organization_id`, `integration_type`, `config` (jsonb), `secret_ref`, `status` |
| `admin_action_log` | Admin actions | `id`, `organization_id`, `action`, `target_type`, `target_id`, `actor_id`, `created_at` |

**FR mapping:** ADMIN-FR-001–008

---

## Schema: `dash` (presentation state only)

| Table | Purpose | Key columns |
|-------|---------|-------------|
| `workspace_preferences` | User UI prefs | `id`, `user_id`, `organization_id`, `preferences` (jsonb) |
| `widget_interactions` | Interaction log | `id`, `user_id`, `widget_id`, `interaction_type`, `created_at` |
| `workspace_projection_cache` | Optional cache | `id`, `organization_id`, `projection_key`, `payload` (jsonb), `expires_at` |

**Note:** DASH does not store alerts/cases—references domain APIs.

**FR mapping:** DASH-FR-001–013

---

## V2 Schemas (design only)

### Schema: `wallet`

| Table | Purpose |
|-------|---------|
| `wallet_profiles` | Address/profile data |
| `address_reputation` | Reputation scores |
| `wallet_activity_events` | Activity timeline |
| `wallet_relationship_refs` | Pointers to Neo4j edges |

### Schema: `sec`

| Table | Purpose |
|-------|---------|
| `security_signals` | Threat/signal records |
| `api_activity_records` | API monitoring |
| `device_intelligence` | Device records |
| `session_anomalies` | Session anomalies |

### Schema: `report`

| Table | Purpose |
|-------|---------|
| `report_definitions` | Report templates |
| `generated_reports` | Report outputs |
| `kpi_snapshots` | KPI data |
| `export_jobs` | Export tracking |

### Schema: `ops`

| Table | Purpose |
|-------|---------|
| `health_check_definitions` | Health rules |
| `operational_alert_rules` | Ops alerting |
| `backup_status_records` | Backup tracking |

---

## Event-to-Table Mapping (MVP)

| Event | Producer table(s) |
|-------|-------------------|
| `RiskCalculated` | `risk.risk_assessments`, `risk.risk_rule_hits` |
| `HighRiskDetected` | `risk.risk_assessments` (threshold flag) |
| `AlertCreated` | `alert.alerts`, `alert.alert_risk_context` |
| `CaseCreated` | `invest.investigation_cases` |
| `EvidenceAttached` | `invest.case_evidence` |
| `ComplianceReviewed` | `comp.*` outcome tables |
| `AIRecommendationGenerated` | `ai.ai_recommendations`, `ai.agent_runs` |

---

## Related Documents

- [DataArchitecture.md](DataArchitecture.md)
- [EventContracts.md](../06-api/EventContracts.md)
