# Module Implementation Plan

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Module Implementation Plan |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 11 |
| Last Updated | 2026-09-11 |

---

## Purpose

Per-domain MVP implementation mapping: responsibility, deployable, APIs, events, schema, order, verification.

---

## MVP Domains

### CORE

| Field | Value |
|-------|-------|
| Responsibility | Platform config, flags, health, audit context helpers |
| Deployable | `sentinel-platform` |
| Dependencies | None (foundation) |
| APIs | API-CORE-* MVP |
| Events published | ConfigurationUpdated, FeatureFlagChanged |
| Events consumed | — (GD-002 Platform* deferred) |
| Schema | Migration **001** `core` |
| Security | Admin-gated config writes |
| UI | SCR-13 (settings surfaces) |
| AI | None |
| Order | M1 |
| Verify | Health; config audit; contract tests |

### AUTH

| Field | Value |
|-------|-------|
| Responsibility | Login, session, MFA, logout |
| Deployable | `sentinel-identity` |
| Dependencies | CORE (audit/health) |
| APIs | API-AUTH-001–005 |
| Events | UserLoggedIn, SessionExpired |
| Schema | **002** `auth` |
| UI | SCR-00 |
| Order | M2 |
| Verify | Auth flows; no user enumeration |

### AUTHZ

| Field | Value |
|-------|-------|
| Responsibility | Roles, permissions, evaluation |
| Deployable | `sentinel-identity` |
| Dependencies | USER/ORG logical refs |
| APIs | API-AUTHZ-* |
| Events | — |
| Schema | **003** `authz` |
| UI | SCR-12 |
| Order | M2 |
| Verify | Deny-by-default; tenant scope |

### USER

| Field | Value |
|-------|-------|
| Responsibility | User lifecycle |
| Deployable | `sentinel-identity` |
| APIs | API-USER-001–004 |
| Events | UserUpdated (catalog); UserCreated deferred optional |
| Schema | **004** `user` |
| Order | M2 |
| Verify | No AUTH session ownership |

### ORG

| Field | Value |
|-------|-------|
| Responsibility | Organization/tenant lifecycle |
| Deployable | `sentinel-identity` |
| APIs | API-ORG-001–003 (004 V2) |
| Schema | **005** `org` |
| Order | M2 |
| Verify | Tenant isolation |

### RISK

| Field | Value |
|-------|-------|
| Responsibility | Scoring, rules, assessments, explanations |
| Deployable | `sentinel-ops` |
| Dependencies | M2 identity; M3 outbox |
| APIs | API-RISK-001–007 |
| Events pub | RiskCalculated, HighRiskDetected |
| Schema | **006** `risk` |
| UI | SCR-06, SCR-07 |
| AI | Consumed for explain; AI must not score critical path |
| Order | M4 |
| Verify | ADR-003 path without AI; PERF targets |

### ALERT

| Field | Value |
|-------|-------|
| Responsibility | Alert lifecycle + priority |
| Deployable | `sentinel-ops` |
| Dependencies | RISK events |
| APIs | API-ALERT-001–007 |
| Events pub | AlertCreated, AlertAssigned, AlertClosed |
| Events con | RiskCalculated, HighRiskDetected |
| Schema | **007** `alert` |
| UI | SCR-02, SCR-03 |
| Order | M5 |
| Verify | Priority ownership; human close |

### INVEST

| Field | Value |
|-------|-------|
| Responsibility | Cases, evidence, notes, timeline |
| Deployable | `sentinel-ops` |
| Dependencies | ALERT for handoff |
| APIs | API-INVEST-001–009 |
| Events pub | CaseCreated/Updated/Closed/Assigned, EvidenceAttached |
| Events con | RiskCalculated, AlertCreated |
| Schema | **008** `invest` |
| UI | SCR-04, SCR-05 |
| AI | Assist/retrieve only |
| Order | M6 |
| Verify | Human close; evidence classification |

### COMP

| Field | Value |
|-------|-------|
| Responsibility | Compliance outcomes |
| Deployable | `sentinel-ops` |
| APIs | API-COMP-001–007 |
| Events | ComplianceReviewed, TravelRuleValidated, SanctionsHitDetected, AuditPackagePrepared |
| Events con | RiskCalculated, Case*, UserUpdated |
| Schema | **009** `comp` |
| UI | SCR-08–10 |
| AI | No approve; API-AI-006 V2 |
| Order | M7 |
| Verify | Human decisions; gap UX-OQ-COMP-LIST |

### AI

| Field | Value |
|-------|-------|
| Responsibility | Assistive agents, prompts, recommendations |
| Deployable | `sentinel-ai` |
| Dependencies | Read APIs RISK/ALERT/INVEST; pgvector |
| APIs | API-AI-001–005 |
| Events pub | AIRecommendationGenerated, PromptUpdated |
| Events con | RiskCalculated, AlertCreated, CaseUpdated, EvidenceAttached |
| Schema | **010** `ai` + vectors |
| UI | SCR-15 |
| Order | M8 |
| Verify | No lifecycle mutation; tool authz; timeout |

### ADMIN

| Field | Value |
|-------|-------|
| Responsibility | Settings, integrations, provision orchestration, audit query |
| Deployable | `sentinel-platform` or `sentinel-admin` |
| APIs | API-ADMIN-001–005 |
| Events | AdminSettingUpdated, IntegrationConfigured, AdminActionPerformed |
| Schema | **011** `admin` |
| UI | SCR-12–14 |
| Order | M10 |
| Verify | No USER/ORG table duplication |

### DASH

| Field | Value |
|-------|-------|
| Responsibility | Workspace BFF, queues, widgets, SSE |
| Deployable | `sentinel-dash` |
| Dependencies | Upstream events + domain reads |
| APIs | API-DASH-001–005, 007 (006 V2) |
| Events con | Risk/Alert/Case/AI (+ SEC V2 later) |
| Schema | **012** `dash` (presentation) |
| UI | SCR-01 + shell |
| Order | M9 |
| Verify | No SoT writes; SSE + poll fallback |

---

## V2 Domains (Boundary Only)

| Domain | MVP action | Notes |
|--------|------------|-------|
| WALLET | Do not implement | Consumes CaseCreated later |
| SEC | Do not implement | Event lock; sample API only |
| REPORT | Do not implement | GD-001 / BQ-4 |
| OPS | Do not implement | GD-002 consumers deferred |

---

## Verification Approach

Per-module: unit + API contract + event schema + security tenant tests per TestingImplementationPlan.md.

---

## Open Questions

| ID | Item |
|----|------|
| P11-OQ-MOD-001 | ADMIN co-location |
| UX-OQ-COMP-LIST | COMP list API gap (non-blocking) |

---

## Related Documents

- [ImplementationArchitecture.md](ImplementationArchitecture.md)
- [ImplementationTraceability.md](ImplementationTraceability.md)
