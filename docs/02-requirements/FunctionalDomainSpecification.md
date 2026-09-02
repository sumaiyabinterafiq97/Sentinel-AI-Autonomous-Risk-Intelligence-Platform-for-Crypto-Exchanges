# Functional Domain Specification (FDS)

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Functional Domain Specification |
| Version | 1.3 (Draft) |
| Status | Draft |
| Owner | Product & Engineering Team |
| Last Updated | 2026-09-02 |

---

## Version History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 0.1 | 2026-08-10 | Product Team | Initial FDS with 16 functional domains |
| 0.2 | 2026-08-10 | Product Team | Domain owners, NFRs, events, data ownership, KPIs, risks, roadmaps, AI classification, interaction matrix; renamed Operational Workspace |
| 0.3 | 2026-08-11 | Product Team | Added Maintenance Mode to CORE features, responsibilities, ownership, success criteria, roadmap, and risks |
| 0.4 | 2026-08-11 | Product Team | AUTHZ: added ORG dependency and organization-context relationship; aligned interaction-matrix consume events |
| 0.5 | 2026-08-11 | Product Team | USER: Activity History clarified as Version 2; interaction matrix reconciled to USER domain events |
| 0.6 | 2026-08-11 | Product Team | ORG: Hierarchy and advanced status to V2; Deactivation MVP; matrix/events reconciled; AUTHZ dependent; UserCreated consume deferred; association boundary clarified |
| 0.7 | 2026-08-11 | Product Team | DASH: MVP feature/roadmap alignment; BR traceability via FI-BR-001/RI-BR-002 (MVP) and RA-BR-001 (V2); matrix/events reconciled; V2/V3 data ownership; consumer-domain boundaries |
| 0.8 | 2026-09-02 | Product Team | ALERT: MVP feature/roadmap/event alignment; RISK/INVEST/ORG boundaries; matrix reconciled; escalation and SEC/compliance inputs deferred; consumer sequencing rule |
| 0.9 | 2026-09-02 | Product Team | RISK: MVP feature/roadmap/event alignment; ALERT/DASH producer boundaries; external TransactionReceived ingest; wallet/AI/alert consume deferred; ORG contextual dependency |
| 1.0 | 2026-09-02 | Product Team | INVEST domain correction: MVP/V2 event and BR traceability reconciliation, frozen downstream contract alignment, AI ownership clarification, and dependency/roadmap boundary cleanup |
| 1.1 | 2026-09-02 | Product Team | WALLET: Version 2 event/boundary correction; interaction matrix reconciled; USER/ORG contextual dependencies; AI ownership clarified; platform MVP hooks none; FR planning baseline ~10; downstream consumer deferral for `AddressReputationChanged` and `SuspiciousWalletDetected` |
| 1.2 | 2026-09-02 | Product Team | COMP: MVP event/boundary correction; interaction matrix reconciled; USER/ORG contextual dependencies; AI assistive-only (not hard dependency); AI ownership clarified; FR planning baseline ~11; downstream consumer deferral; `EvidenceAttached` excluded from MVP consume set |
| 1.3 | 2026-09-02 | Product Team | SEC: Version 2 event/boundary correction; interaction matrix reconciled; USER/ORG contextual dependencies; AI assistive-only (not hard dependency); AI ownership clarified; FR planning baseline ~10; downstream consumer deferral; `DeviceSignalReceived` excluded from SEC V2 contract |

---

## Approval

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Prepared By | Product & Engineering Team | 2026-08-10 | — |
| Reviewed By | To Be Assigned | — | — |
| Approved By | To Be Assigned | — | — |

| Field | Value |
|-------|-------|
| Approval Date | Pending |
| Document Status | Draft |

---

## Purpose

The Functional Domain Specification (FDS) defines the major functional capabilities of Sentinel AI.

It answers:

> **What are the major capabilities of Sentinel AI?**

It does **not** define detailed system behavior such as login flows, validation rules, or API interactions. Those details belong in the Functional Requirements Specification (FRS).

The FDS is the bridge between Business Requirements and detailed Functional Requirements:

```text
Vision
  ↓
Product Scope
  ↓
Business Requirements (BRS)
  ↓
Functional Domain Specification (FDS)
  ↓
Functional Requirements (FRS)
  ↓
Architecture
  ↓
Database
  ↓
API
  ↓
Implementation
```

---

## Document Scope

This document defines:

- Functional domains and domain ownership
- Domain purpose, business value, and responsibilities
- Features, future features, and release roadmaps
- Domain data ownership
- Domain events (published and consumed)
- Non-functional business expectations
- Domain KPIs and domain risks
- High-level integrations and AI agent associations
- Estimated Functional Requirement volume

This document intentionally excludes:

- Detailed functional requirements
- Use-case step flows
- API contracts
- Database schemas
- UI wireframes
- Implementation technology choices

---

## Related Documents

| Document | Relationship |
|----------|--------------|
| [Vision.md](../01-product/Vision.md) | Product vision and principles |
| [ProductScope.md](../01-product/ProductScope.md) | Product boundaries and capability model |
| [BusinessRequirements.md](../01-product/BusinessRequirements.md) | Business requirements realized by these domains |
| [FunctionalRequirements.md](FunctionalRequirements.md) | Detailed system behavior within each domain |
| System Architecture | Service boundaries mapped from domains |
| AI Architecture | Agents associated with AI-enabled domains |
| Event-Driven Architecture | Domain event contracts and flows |

---

## Domain Catalog

| Domain ID | Functional Domain | Owner | Estimated FRs | Priority | Release | Related Business Requirements |
|-----------|-------------------|-------|--------------:|----------|---------|-------------------------------|
| CORE | Core Platform | Platform Engineering | 30 | Critical | MVP | OPS-BR-001, ADM-BR-001 |
| AUTH | Authentication | Identity & Access Engineering | 20 | Critical | MVP | ADM-BR-001 |
| AUTHZ | Authorization | Identity & Access Engineering | 15 | Critical | MVP | ADM-BR-001 |
| ORG | Organization Management | Platform Engineering | 12 | High | MVP | ADM-BR-001 |
| USER | User Management | Identity & Access Engineering | 20 | High | MVP | ADM-BR-001 |
| DASH | Operational Workspace | Product Experience / Frontend Platform | 14 | High | MVP | FI-BR-001, RI-BR-002 (MVP); RA-BR-001 (V2) |
| ALERT | Alert Management | Risk Operations Engineering | 20 (planning guideline; not a forced count) | High | MVP | RI-BR-001, RI-BR-002 |
| RISK | Risk Intelligence | Risk Intelligence Team | 45 (planning guideline; not a forced count) | Critical | MVP | RI-BR-001, RI-BR-002, RI-BR-003 |
| INVEST | Investigation Management | Investigation Platform Team | 50 (planning guideline; not a forced count) | Critical | MVP | FI-BR-001, FI-BR-002 (MVP); FI-BR-003 (V2) |
| WALLET | Wallet Intelligence | Financial Crime Intelligence Team | ~10 (planning guideline; not a forced count) | Medium | Version 2 | WI-BR-001 |
| COMP | Compliance & Travel Rule | Compliance Engineering | ~11 (planning guideline; not a forced count) | High | MVP | CP-BR-001, CP-BR-002 |
| SEC | Security Intelligence | Security Engineering | ~10 (planning guideline; not a forced count) | Medium | Version 2 | SEC-BR-001, SEC-BR-002 |
| AI | AI Platform | AI Platform Team | 60 | High | MVP | AI-BR-001, AI-BR-002 |
| REPORT | Reporting & Analytics | Analytics & Insights Team | 20 | Medium | Version 2 | RA-BR-001 |
| ADMIN | Administration | Platform Engineering | 20 | High | MVP | ADM-BR-001 |
| OPS | Platform Operations | Platform Operations / SRE | 20 | Medium | Version 2 | OPS-BR-001 |
| | **Total** | | **386** | | | |

---

## Domain Interaction Matrix

This matrix is the blueprint for architecture, data ownership, events, APIs, and AI workflows.

| Domain | Owns Data | Publishes Events | Consumes Events | Primary AI Agent |
|--------|-----------|------------------|-----------------|------------------|
| CORE | Configurations, Feature Flags | PlatformStarted, ConfigurationUpdated | — | — |
| AUTH | Sessions, MFA Enrollments | UserLoggedIn, UserLoggedOut | UserCreated | — |
| AUTHZ | Roles, Permissions, Role Assignments, Authorization Policies | RoleAssigned, PermissionDenied, PolicyUpdated | UserLoggedIn, UserCreated, OrganizationUpdated | — |
| ORG | Organizations, Organization Settings, Tenant Context | OrganizationCreated, OrganizationUpdated, OrganizationDeactivated | ConfigurationUpdated | — |
| USER | Users, Profiles, Account Status | UserCreated, UserUpdated, UserDeactivated, UserStatusChanged | OrganizationCreated, RoleAssigned, UserLoggedIn | — |
| DASH | Work Queue Presentations | WorkspaceViewed, WorkQueueOpened, WidgetInteracted | AlertCreated, CaseUpdated, RiskCalculated, ReportGenerated | Workspace Summary Assistant |
| ALERT | Alerts, Alert Assignments, Alert Operational Priorities | AlertCreated, AlertAssigned, AlertClosed | RiskCalculated, HighRiskDetected | Alert Triage Assistant |
| RISK | Risk Scores, Rules, Explanations, Priority Signals | RiskCalculated, HighRiskDetected | TransactionReceived | Risk Analysis Agent |
| INVEST | Cases, Evidence, Assignments, Notes, Timeline | CaseCreated, CaseUpdated, CaseClosed, CaseAssigned, EvidenceAttached | AlertCreated, RiskCalculated | — |
| WALLET | Wallet Profiles, Reputation, Graphs | WalletProfileUpdated, AddressReputationChanged, SuspiciousWalletDetected | TransactionReceived, CaseCreated, RiskCalculated | — |
| COMP | Compliance Records, Travel Rule Results, Sanctions Results, Evidence Packages | ComplianceReviewed, TravelRuleValidated, SanctionsHitDetected, AuditPackagePrepared | CaseClosed, CaseUpdated, RiskCalculated, UserUpdated | — |
| SEC | Security Events, Threat Detections, Device Intelligence Records, API Abuse Signals | ThreatDetected, SuspiciousSessionDetected, ApiAbuseDetected | UserLoggedIn, SessionExpired, AlertCreated, CaseCreated | — |
| AI | Prompts, Evaluations, Recommendations | AIRecommendationGenerated | CaseUpdated, RiskCalculated | Multi-Agent Orchestration |
| REPORT | Report Definitions, KPI Snapshots | ReportGenerated | CaseClosed, ComplianceReviewed | Report Generation Agent |
| ADMIN | Admin Settings, Integration Configs | AdminSettingUpdated | UserCreated, RoleAssigned | — |
| OPS | Health Definitions, Ops Alert Rules | PlatformHealthDegraded | PlatformUnavailable, AgentRunFailed | — |

---

## Domain Template

Every domain in this document follows the same structure:

```text
Domain Information (including Domain Owner)
Domain Overview
Responsibilities
Features
Future Features
Domain Data Ownership
Domain Events
Non-Functional Characteristics
External Integrations
Related AI Agents (Primary / Supporting)
Security Considerations
Success Criteria
Domain KPIs
Domain Risks
Domain Roadmap (MVP / V2 / V3)
Domain Relationships
```

---

## Specification Order

Functional Requirements will be authored by domain in dependency order:

```text
1. CORE
2. AUTH
3. AUTHZ
4. USER
5. ORG
6. DASH (Operational Workspace)
7. ALERT
8. RISK
9. INVEST
10. WALLET
11. COMP
12. SEC
13. AI
14. REPORT
15. ADMIN
16. OPS
```

This order mirrors platform dependencies so foundational capabilities are specified before investigation, compliance, and AI domains.

**Consumer/producer sequencing (ALERT / RISK):** ALERT is specified before RISK in this order. ALERT may define consumption of RISK events and risk-derived signals but must not define or invent RISK scoring, rules, or explanation behavior. RISK remains the owner of those capabilities.

**Producer obligation (RISK / ALERT / DASH):** RISK publishes `RiskCalculated` and `HighRiskDetected` for MVP. ALERT consumes both events; DASH consumes `RiskCalculated` (frozen downstream contracts). RISK is upstream of ALERT for MVP risk signals and does not consume `AlertCreated`. Explanation outputs are carried within `RiskCalculated` for MVP; separate explanation publish events are deferred.

---

## Domain Dependency Overview

```text
CORE
 ├── AUTH
 │     └── AUTHZ
 │           ├── ORG  ←→ AUTHZ (contextual): AUTHZ depends on ORG for organization
 │           │           authorization context; ORG depends on AUTHZ for access control
 │           │     └── USER
 │           ├── ADMIN
 │           └── OPS
 ├── ALERT ← RISK
 ├── RISK
 ├── INVEST ← ALERT, RISK, AI
 ├── WALLET ← RISK, INVEST
 ├── COMP ← INVEST, RISK, AI
 ├── SEC ← ALERT, INVEST
 ├── AI ← RISK, INVEST, WALLET, COMP
 ├── REPORT ← RISK, INVEST, COMP, AI, OPS
 └── DASH (Operational Workspace) ← ALERT, RISK, INVEST, REPORT
```

AUTHZ also depends on USER for actor identity references used in role assignment. ORG lifecycle remains ORG-owned; AUTHZ does not manage organizations.

---

# Functional Domains

## Domain — CORE: Core Platform

### Domain Information

| Field | Value |
|-------|-------|
| Domain ID | `CORE` |
| Domain Name | Core Platform |
| Domain Owner | Platform Engineering |
| Purpose | Foundational platform services shared by all domains. |
| Business Value | Reliable shared platform primitives for configuration, search, notifications, and operational lifecycle. |
| Related Business Requirements | OPS-BR-001, ADM-BR-001 |
| Related Business Objectives | BO-007, BO-009 |
| Primary Users | Platform Administrators, System Administrators, Platform Engineers |
| Dependencies | — |
| Dependent Domains | AUTH, AUTHZ, ORG, USER, DASH, ALERT, RISK, INVEST, WALLET, COMP, SEC, AI, REPORT, ADMIN, OPS |
| Estimated Functional Requirements | 30 |
| Priority | Critical |
| Release | MVP |
| FR Prefix | `CORE-FR` |

### Domain Overview

The Core Platform domain provides shared platform capabilities required by every Sentinel AI domain. It establishes common patterns for configuration, feature control, scheduling, search, notifications, error handling, audit context, and maintenance mode so domain teams do not reinvent foundational behavior.

### Responsibilities

The domain is responsible for:

- Platform lifecycle management
- Shared configuration and feature flags
- Cross-domain search and notification frameworks
- Consistent error handling and audit context
- Foundational health signals for platform readiness
- Platform maintenance mode control

### Features

- Platform Lifecycle
- Health Monitoring
- Configuration
- Feature Flags
- Scheduling
- Search Framework
- Notifications
- Error Handling
- Audit Context
- Maintenance Mode

### Future Features

- Plugin Framework Hooks
- Multi-Region Configuration Sync
- Advanced Scheduling Policies

### Domain Data Ownership

This domain owns:

- Platform Configurations
- Feature Flags
- Schedules
- Shared Notification Templates
- Audit Context Metadata
- Maintenance Mode State

### Domain Events

**Publishes**

- `PlatformStarted`
- `PlatformUnavailable`
- `ConfigurationUpdated`
- `FeatureFlagChanged`

**Consumes**

- —

### Non-Functional Characteristics

| Characteristic | Business Expectation |
|----------------|----------------------|
| Availability | 99.9% for shared platform services |
| Scalability | Horizontal |
| Performance | Configuration and health checks respond within operational SLAs |
| Security | Privileged configuration access only |
| Auditability | Full audit of configuration and feature-flag changes |
| Observability | Metrics + Logs + Traces |

### External Integrations

- Exchange Event Stream (foundation)
- Notification Providers
- Internal Configuration Store

### Related AI Agents

No AI agents are associated with this domain.

### Security Considerations

Core platform configuration, feature flags, and maintenance mode controls shall be accessible only to authorized platform administrators. Audit context shall be preserved across domain operations.

### Success Criteria

- Shared platform services available to dependent domains
- Feature flags controllable by authorized administrators
- Consistent audit context available across workflows
- Foundational health checks expose platform readiness
- Authorized administrators can enable and disable maintenance mode with auditable controlled behavior

### Domain KPIs

- Platform readiness check success rate
- Configuration change lead time
- Feature-flag evaluation latency
- Shared notification delivery success rate

### Domain Risks

- Incorrect feature-flag configuration
- Shared service outage cascading across domains
- Missing audit context in cross-domain flows
- Improper maintenance-mode activation blocking essential controls

### Domain Roadmap

**MVP**

- Platform Lifecycle
- Health Monitoring
- Configuration
- Feature Flags
- Audit Context
- Maintenance Mode

**Version 2**

- Advanced Scheduling
- Search Framework Expansion
- Notification Framework Hardening

**Version 3**

- Plugin Framework Hooks
- Multi-Region Configuration Sync

### Domain Relationships

**Depends on:** —

**Supports:** AUTH, AUTHZ, ORG, USER, DASH, ALERT, RISK, INVEST, WALLET, COMP, SEC, AI, REPORT, ADMIN, OPS

---

## Domain — AUTH: Authentication

### Domain Information

| Field | Value |
|-------|-------|
| Domain ID | `AUTH` |
| Domain Name | Authentication |
| Domain Owner | Identity & Access Engineering |
| Purpose | Verify identity and manage authenticated sessions. |
| Business Value | Secure access to Sentinel AI for authorized personnel only. |
| Related Business Requirements | ADM-BR-001 |
| Related Business Objectives | BO-007, BO-009 |
| Primary Users | All authenticated users, Platform Administrators |
| Dependencies | CORE |
| Dependent Domains | AUTHZ, USER, ORG, DASH, ALERT, RISK, INVEST, ADMIN |
| Estimated Functional Requirements | 20 |
| Priority | Critical |
| Release | MVP |
| FR Prefix | `AUTH-FR` |

### Domain Overview

The Authentication domain verifies user identity and manages session lifecycle for Sentinel AI. It provides secure login, logout, multi-factor authentication, credential recovery, and session controls required before any operational domain can be used.

### Responsibilities

The domain is responsible for:

- Authenticate users
- Manage sessions and tokens
- Support MFA and credential recovery
- Register trusted devices where required
- Enforce session timeout and refresh behavior

### Features

- Login
- Logout
- MFA
- Password Reset
- Email Verification
- Session Management
- Refresh Tokens
- Device Registration

### Future Features

- Passkey / WebAuthn Support
- Adaptive Authentication
- Federated Identity Provider Expansion

### Domain Data Ownership

This domain owns:

- Sessions
- Authentication Tokens Metadata
- MFA Enrollments
- Password Reset Requests
- Device Registrations

### Domain Events

**Publishes**

- `UserLoggedIn`
- `UserLoggedOut`
- `PasswordResetRequested`
- `MfaChallengeIssued`
- `SessionExpired`

**Consumes**

- `UserCreated`
- `UserDeactivated`
- `UserStatusChanged`

### Non-Functional Characteristics

| Characteristic | Business Expectation |
|----------------|----------------------|
| Availability | 99.95% for authentication endpoints |
| Scalability | Horizontal |
| Performance | Login and token refresh within interactive response targets |
| Security | Credential and token protection required |
| Auditability | Full audit of authentication events |
| Observability | Metrics + Logs + Traces |

### External Integrations

- Identity Provider
- Email Delivery Service
- Device Trust Service

### Related AI Agents

No AI agents are associated with this domain.

### Security Considerations

Authentication credentials, tokens, and MFA secrets shall be protected using secure handling practices. Session fixation and credential-stuffing risks shall be mitigated through platform controls.

### Success Criteria

- Authorized users can authenticate successfully
- Unauthorized users are denied access
- Sessions expire according to policy
- MFA can be enforced for privileged roles

### Domain KPIs

- Login success rate
- MFA challenge completion rate
- Average authentication latency
- Session timeout compliance rate

### Domain Risks

- Credential stuffing or account takeover
- Broken session invalidation
- MFA bypass misconfiguration

### Domain Roadmap

**MVP**

- Login
- Logout
- Session Management
- MFA
- Password Reset

**Version 2**

- Device Registration Expansion
- Refresh Token Hardening
- Email Verification Improvements

**Version 3**

- Passkeys / WebAuthn
- Adaptive Authentication

### Domain Relationships

**Depends on:** CORE

**Supports:** AUTHZ, USER, ORG, DASH, ALERT, RISK, INVEST, ADMIN

---

## Domain — AUTHZ: Authorization

### Domain Information

| Field | Value |
|-------|-------|
| Domain ID | `AUTHZ` |
| Domain Name | Authorization |
| Domain Owner | Identity & Access Engineering |
| Purpose | Control access to capabilities based on roles and permissions. |
| Business Value | Least-privilege access across investigations, compliance, and administration. |
| Related Business Requirements | ADM-BR-001 |
| Related Business Objectives | BO-007, BO-009 |
| Primary Users | Platform Administrators, All operational users |
| Dependencies | CORE, AUTH, USER, ORG |
| Dependent Domains | ORG, DASH, ALERT, RISK, INVEST, COMP, SEC, AI, REPORT, ADMIN, OPS |
| Estimated Functional Requirements | 15 |
| Priority | Critical |
| Release | MVP |
| FR Prefix | `AUTHZ-FR` |

### Domain Overview

The Authorization domain enforces role-based access control and policy evaluation across Sentinel AI. It ensures users can only perform actions permitted by their organizational role and assigned permissions. Where organizational scope affects authorization, AUTHZ uses organization context provided by ORG without owning organization lifecycle.

### Responsibilities

The domain is responsible for:

- Define and evaluate roles and permissions
- Enforce least-privilege access
- Support policy evaluation for sensitive actions
- Prevent unauthorized access to investigations and configuration

### Features

- RBAC
- Permissions
- Role Management
- Policy Evaluation

### Future Features

- Attribute-Based Access Control (ABAC)
- Just-in-Time Privileged Access
- Fine-Grained Resource Policies

### Domain Data Ownership

This domain owns:

- Roles
- Permissions
- Role Assignments
- Authorization Policies

### Domain Events

**Publishes**

- `RoleAssigned`
- `PermissionDenied`
- `PolicyUpdated`

**Consumes**

- `UserLoggedIn`
- `UserCreated`
- `OrganizationUpdated`

### Non-Functional Characteristics

| Characteristic | Business Expectation |
|----------------|----------------------|
| Availability | 99.95% for authorization decisions |
| Scalability | Horizontal |
| Performance | Authorization checks low-latency for interactive and API flows |
| Security | Server-side enforcement mandatory |
| Auditability | Full audit of permission changes and denials |
| Observability | Metrics + Logs + Traces |

### External Integrations

- Identity Provider Role Claims
- Organization Directory

### Related AI Agents

No AI agents are associated with this domain.

### Security Considerations

Authorization decisions shall be enforceable server-side. Privilege elevation and sensitive policy changes shall be audited.

### Success Criteria

- Role-based access enforced across domains
- Unauthorized actions are rejected
- Role and permission changes are auditable

### Domain KPIs

- Authorization decision latency
- Permission denial rate by domain
- Privileged role change count

### Domain Risks

- Incorrect permission configuration
- Over-privileged roles
- Stale role assignments after user changes

### Domain Roadmap

**MVP**

- RBAC
- Permissions
- Role Management
- Policy Evaluation

**Version 2**

- Fine-Grained Resource Policies
- Privileged Access Reviews

**Version 3**

- ABAC
- Just-in-Time Privileged Access

### Domain Relationships

**Depends on:** CORE, AUTH, USER, ORG

**Supports:** ORG, DASH, ALERT, RISK, INVEST, COMP, SEC, AI, REPORT, ADMIN, OPS

**Organization-context relationship:** AUTHZ depends on ORG for organization authorization context. AUTHZ consumes organization context through the Organization Directory integration and the `OrganizationUpdated` event so authorization decisions can reflect organizational scope. ORG lifecycle, tenant administration, and organization profiles remain ORG-owned. ORG remains a Dependent Domain because organization administration actions require AUTHZ enforcement. This is a contextual bidirectional relationship, not a transfer of domain ownership.

---

## Domain — ORG: Organization Management

### Domain Information

| Field | Value |
|-------|-------|
| Domain ID | `ORG` |
| Domain Name | Organization Management |
| Domain Owner | Platform Engineering |
| Purpose | Manage organizational tenants, structure, and organizational settings. |
| Business Value | Supports multi-organization governance and isolation. |
| Related Business Requirements | ADM-BR-001 |
| Related Business Objectives | BO-007, BO-009 |
| Primary Users | Platform Administrators, Executive Management |
| Dependencies | CORE, AUTH, AUTHZ |
| Dependent Domains | USER, AUTHZ, ADMIN, DASH, REPORT |
| Estimated Functional Requirements | 12 (planning guideline; not a forced count) |
| Priority | High |
| Release | MVP |
| FR Prefix | `ORG-FR` |

### Domain Overview

The Organization Management domain manages organizational entities, organization-level settings, tenant context, and organization deactivation. It provides the organizational and tenant context required by dependent domains for governance and operational isolation.

USER associates users with organizational context provided by ORG. ORG does not own user records, authentication, or authorization decisions. Organization hierarchy and advanced organization status controls are Version 2 capabilities.

### Responsibilities

The domain is responsible for:

- Manage organization profiles and organization lifecycle including deactivation
- Maintain organization-level settings
- Establish and provide tenant context to dependent domains
- Support organizational isolation of operational data
- Provide organizational context to dependent domains

Version 2 responsibilities include organization hierarchy management and advanced organization status controls.

### Features

- Organization Profiles
- Organization Settings
- Tenant Context
- Organization Deactivation

### Future Features

- Organization Hierarchy
- Hierarchy Expansion
- Advanced Organization Status Controls
- Multi-Brand Organization Support
- Delegated Organization Administration
- HR / Identity Systems Integration (optional)

### Domain Data Ownership

This domain owns:

- Organizations
- Organization Settings
- Tenant Context
- Organization Hierarchy (Version 2)

### Domain Events

**Publishes**

- `OrganizationCreated`
- `OrganizationUpdated`
- `OrganizationDeactivated`

**Consumes**

- `ConfigurationUpdated` — only when organization settings are configured to adopt platform configuration defaults from CORE

`UserCreated` is not an MVP ORG consume dependency. User lifecycle remains USER-owned; user-to-organization association remains USER-owned (USER consumes `OrganizationCreated`).

### Non-Functional Characteristics

| Characteristic | Business Expectation |
|----------------|----------------------|
| Availability | 99.9% |
| Scalability | Horizontal by tenant growth |
| Performance | Organization context resolution within interactive targets |
| Security | Strict tenant isolation |
| Auditability | Full audit of organization changes |
| Observability | Metrics + Logs + Traces |

### External Integrations

- Enterprise Directory (MVP)
- HR / Identity Systems (optional / Version 2)

### Related AI Agents

No AI agents are associated with this domain.

### Security Considerations

Organization isolation boundaries shall prevent unauthorized cross-organization data access. Authorization of organization administration actions is evaluated by AUTHZ; ORG enforces organization and tenant data boundaries.

### Success Criteria

- Organizations can be created, configured, and deactivated
- Organizational and tenant context is available to dependent domains
- Users can be associated with organizations through USER using ORG-provided context (USER owns association)

### Domain KPIs

- Organization provisioning time
- Cross-tenant access violation attempts
- Organization configuration change volume

### Domain Risks

- Broken tenant isolation
- Incorrect organization hierarchy (Version 2)
- Orphaned user associations after organization deactivation (mitigated by USER consuming organization lifecycle signals; ORG does not manage user records)

### Domain Roadmap

**MVP**

- Organization Profiles
- Tenant Context
- Organization Settings
- Organization Deactivation

**Version 2**

- Organization Hierarchy
- Hierarchy Expansion
- Advanced Organization Status Controls
- HR / Identity Systems Integration (optional)

**Version 3**

- Delegated Organization Administration
- Multi-Brand Support

### Domain Relationships

**Depends on:** CORE, AUTH, AUTHZ

**Supports:** USER, AUTHZ, ADMIN, DASH, REPORT

**Organization-context relationship:** ORG owns organization lifecycle and tenant context. USER depends on ORG for organizational association context and consumes `OrganizationCreated`. AUTHZ depends on ORG for organization authorization context and consumes `OrganizationUpdated`. ORG depends on AUTHZ for authorization of privileged organization administration actions. This is a contextual relationship, not a transfer of domain ownership.

**User-association boundary:** ORG provides organization and tenant context. USER owns user records and user-to-organization association. ORG shall not assign users to organizations as a user-management capability.

---

## Domain — USER: User Management

### Domain Information

| Field | Value |
|-------|-------|
| Domain ID | `USER` |
| Domain Name | User Management |
| Domain Owner | Identity & Access Engineering |
| Purpose | Manage user profiles, preferences, and account status. |
| Business Value | Consistent identity and profile management for operational teams. |
| Related Business Requirements | ADM-BR-001 |
| Related Business Objectives | BO-007, BO-009 |
| Primary Users | Platform Administrators, All operational users |
| Dependencies | CORE, AUTH, AUTHZ, ORG |
| Dependent Domains | DASH, ALERT, INVEST, ADMIN, REPORT |
| Estimated Functional Requirements | 20 |
| Priority | High |
| Release | MVP |
| FR Prefix | `USER-FR` |

### Domain Overview

The User Management domain manages user profiles, preferences, and account status. It provides the user identity context required by authorization, investigation assignment, and administration. Activity history and related administrator insights are Version 2 capabilities.

### Responsibilities

The domain is responsible for:

- Maintain user profiles and preferences
- Track account status
- Provide user identity context to operational domains

Version 2 responsibilities include user activity visibility for administrators.

### Features

- User Profiles
- Preferences
- Account Status

### Future Features

- Activity History
- Expanded Activity History
- Admin User Insights
- User Skill / Specialty Profiles
- Workload Capacity Preferences

### Domain Data Ownership

This domain owns:

- Users
- User Profiles
- User Preferences
- Account Status
- User Activity History (Version 2)

### Domain Events

**Publishes**

- `UserCreated`
- `UserUpdated`
- `UserDeactivated`
- `UserStatusChanged`

**Consumes**

- `OrganizationCreated`
- `RoleAssigned`
- `UserLoggedIn`

### Non-Functional Characteristics

| Characteristic | Business Expectation |
|----------------|----------------------|
| Availability | 99.9% |
| Scalability | Horizontal |
| Performance | Profile retrieval within interactive targets |
| Security | Profile and status changes privileged |
| Auditability | Full audit of user lifecycle changes |
| Observability | Metrics + Logs + Traces |

### External Integrations

- Identity Provider Profile Attributes

### Related AI Agents

No AI agents are associated with this domain.

### Security Considerations

User profile and account status changes shall be restricted to authorized administrators and fully audited.

### Success Criteria

- User profiles are maintainable
- Account status controls access eligibility
- User identity is available for assignment and audit

### Domain KPIs

- User provisioning time
- Inactive account rate
- Profile update success rate

### Domain Risks

- Stale or duplicate user records
- Incorrect account deactivation
- Missing organizational association

### Domain Roadmap

**MVP**

- User Profiles
- Account Status
- Preferences

**Version 2**

- Activity History
- Admin User Insights
- Expanded Activity History

**Version 3**

- Skill / Specialty Profiles
- Workload Capacity Preferences

### Domain Relationships

**Depends on:** CORE, AUTH, AUTHZ, ORG

**Supports:** DASH, ALERT, INVEST, ADMIN, REPORT

---

## Domain — DASH: Operational Workspace

### Domain Information

| Field | Value |
|-------|-------|
| Domain ID | `DASH` |
| Domain Name | Operational Workspace |
| Domain Owner | Product Experience / Frontend Platform |
| Purpose | Provide operational workspaces, queues, and analyst workflows. |
| Business Value | Centralized visibility and efficient navigation into alerts, cases, and insights. |
| Related Business Requirements | FI-BR-001, RI-BR-002 (MVP); RA-BR-001 (Version 2 reporting/analytics visibility) |
| Related Business Objectives | BO-001, BO-005, BO-002 (MVP); BO-006 (Version 2 via RA-BR-001) |
| Primary Users | Risk Analysts, Fraud Investigators, Compliance Officers, Security Engineers, Managers |
| Dependencies | CORE, AUTH, AUTHZ, USER, ALERT, RISK, INVEST, REPORT |
| Dependent Domains | — |
| Estimated Functional Requirements | 14 (planning guideline; not a forced count) |
| Priority | High |
| Release | MVP |
| FR Prefix | `DASH-FR` |

### Domain Overview

The Operational Workspace domain provides the primary operational capability for analysts and managers. It consolidates role-appropriate workspaces, work queues, summary widgets, thin quick navigation, and entry points into alerts, investigations, risk insights, and reporting. It is a business capability domain, not a single UI screen.

DASH is a consumer/presentation domain. It presents and navigates to information owned by ALERT, RISK, INVEST, and REPORT. It does not create alerts, calculate risk, manage investigation lifecycle, or generate reports.

By providing a centralized workspace, prioritized queues, and rapid navigation into alerts and investigations, DASH reduces time-to-first-work-item and supports faster investigation handling (BO-001) under FI-BR-001 and RI-BR-002. Broader operational reporting/analytics dashboard visibility remains aligned to RA-BR-001 in Version 2.

### Responsibilities

The domain is responsible for:

- Present role-based operational workspaces
- Provide thin quick navigation into alerts and investigations
- Surface prioritized work queues using upstream domain information
- Host reusable summary widgets that present authorized upstream summaries
- Support executive and operational visibility entry points without owning upstream domain behavior

### Features

- Operational Workspace
- Role-Based Dashboards
- Work Queues
- Quick Navigation
- Summary Widgets

### Future Features

- Summary Widgets Expansion
- Saved Workspace Views
- Personalized Workspace Layouts
- Cross-Domain Command Palette

### Domain Data Ownership

This domain owns:

- Work Queue Presentations (MVP)
- Saved Views (Version 2)
- Workspace Layout Preferences (Version 3)

### Domain Events

**Publishes**

- `WorkspaceViewed`
- `WorkQueueOpened`
- `WidgetInteracted`

**Consumes**

- `AlertCreated`
- `CaseUpdated`
- `RiskCalculated`
- `ReportGenerated`

DASH consumes these events to refresh workspace presentations only. It does not redefine ALERT, INVEST, RISK, or REPORT lifecycle behavior.

### Non-Functional Characteristics

| Characteristic | Business Expectation |
|----------------|----------------------|
| Availability | 99.9% for analyst workspace access |
| Scalability | Horizontal by concurrent analyst sessions |
| Performance | Workspace load within interactive targets |
| Security | Strict authorization on all widgets and queues |
| Auditability | Access to sensitive workspace content audited |
| Observability | Metrics + Logs + Traces |

### External Integrations

- Reporting Service — navigate/consume report outputs only; REPORT owns report definitions and generation
- Notification Service — consume/display notifications only; CORE owns shared notification framework capabilities

### Related AI Agents

**Primary Agent:** Workspace Summary Assistant

**Supporting Agents:**
- Investigation Summarization Agent

AI agents in this domain are assistive only. They may generate summaries and suggestions for human review. They shall not make alert, investigation, authorization, or risk enforcement decisions.

### Security Considerations

Workspace content shall respect authorization boundaries and avoid exposing unauthorized investigation or risk data. AUTHZ evaluates access; DASH presents only authorized content.

### Success Criteria

- Users can access role-appropriate workspaces
- Priority work items are visible from upstream domains
- Navigation into alerts and investigations is available
- Workspace access reduces friction to begin investigation work

### Domain KPIs

- Time-to-first-work-item
- Work queue abandonment rate
- Workspace adoption by role

### Domain Risks

- Information overload reducing analyst focus
- Unauthorized data exposure through widgets
- Stale queue counts misleading prioritization when upstream events are delayed

### Domain Roadmap

**MVP**

- Operational Workspace
- Role-Based Dashboards
- Work Queues
- Quick Navigation
- Summary Widgets

**Version 2**

- Summary Widgets Expansion
- Saved Views

**Version 3**

- Personalized Layouts
- Command Palette

### Domain Relationships

**Depends on:** CORE, AUTH, AUTHZ, USER, ALERT, RISK, INVEST, REPORT

**Supports:** —

**Consumer-domain relationship:** DASH depends on ALERT, RISK, INVEST, and REPORT for work-item and insight content. DASH may be authored before those domains’ FRs are complete by specifying presentation and consumption behavior only. Upstream domains remain owners of their lifecycle, scoring, investigation, and reporting capabilities.

**Business traceability relationship:**
- MVP Operational Workspace capabilities trace to existing BRS requirements **FI-BR-001** and **RI-BR-002** and objectives **BO-001**, **BO-005**, and **BO-002**.
- Version 2 reporting/analytics visibility capabilities additionally trace to **RA-BR-001** and **BO-006**.
- No new Business Requirement is introduced for DASH.

---

## Domain — ALERT: Alert Management

### Domain Information

| Field | Value |
|-------|-------|
| Domain ID | `ALERT` |
| Domain Name | Alert Management |
| Domain Owner | Risk Operations Engineering |
| Purpose | Generate, prioritize, assign, and manage operational alerts. |
| Business Value | Faster triage and consistent alert lifecycle management. |
| Related Business Requirements | RI-BR-001, RI-BR-002 |
| Related Business Objectives | BO-001, BO-002 |
| Primary Users | Risk Analysts, Fraud Investigators, Security Engineers |
| Dependencies | CORE, AUTH, AUTHZ, USER, ORG (contextual — tenant/organization scope; lifecycle owned by ORG), RISK |
| Dependent Domains | INVEST, DASH, REPORT, AI |
| Estimated Functional Requirements | 20 (planning guideline; not a forced count) |
| Priority | High |
| Release | MVP |
| FR Prefix | `ALERT-FR` |

### Domain Overview

The Alert Management domain manages the operational lifecycle of alerts generated from risk signals and permitted external ingestion inputs. It enables generation, operational prioritization, assignment, and closure of alerts that analysts triage and that may be associated with investigations owned by INVEST.

RI-BR-002 supports risk-derived prioritization through RISK. ALERT operationalizes alert priority and handling state for alert queues and lifecycle management. ALERT does not duplicate RISK scoring, rules, or risk-derived priority signal calculation.

### Responsibilities

The domain is responsible for:

- Generate and ingest alerts from risk signals and permitted external exchange event inputs
- Apply operational alert priority and assignment for alert handling and queue ordering (RISK owns risk scoring and risk-derived priority signals)
- Manage alert lifecycle states
- Associate alert context with investigations (INVEST owns investigation cases and workflow; ALERT does not create or own investigation cases)

### Features

- Alert Generation
- Alert Assignment
- Alert Prioritization
- Alert Lifecycle

### Future Features

- Alert Deduplication Intelligence
- Auto-Triage Suggestions
- SLA-Based Escalation Policies

### Domain Data Ownership

This domain owns:

- Alerts
- Alert Assignments
- Alert Operational Priorities

Alert operational priorities represent alert handling and queue-ordering state used for alert management. RISK owns risk scores, rules, and risk-derived priority signals.

**Version 2 data ownership (when applicable):**

- Alert Escalation Records (alert-level escalation only; distinct from INVEST case-level escalation records)

### Domain Events

**Publishes (MVP)**

- `AlertCreated`
- `AlertAssigned`
- `AlertClosed`

**Publishes (Version 2 — when Alert Escalation Policies apply)**

- `AlertEscalated`

**Consumes (MVP)**

- `RiskCalculated`
- `HighRiskDetected`

**Deferred consumption (Version 2+)**

- `ThreatDetected` — deferred until SEC Version 2 integration is available

**External integration inputs (not ALERT-domain published events)**

- `TransactionReceived` — classified as an external integration input via Exchange Event Stream. It is not published by ALERT or by any Sentinel domain in the current FDS baseline. ALERT may ingest permitted external signals without treating `TransactionReceived` as an ALERT-domain published event.

### Non-Functional Characteristics

| Characteristic | Business Expectation |
|----------------|----------------------|
| Availability | 99.9% |
| Scalability | Horizontal with event volume |
| Performance | Alert creation and prioritization near-real-time for supported streams |
| Security | Role-restricted alert access |
| Auditability | Full alert lifecycle audit |
| Observability | Metrics + Logs + Traces |

### External Integrations

- Exchange Event Stream (external ingestion boundary; may include transaction-oriented inputs such as `TransactionReceived` classified as external integration events)
- RISK (risk signals via `RiskCalculated` and `HighRiskDetected`; RISK owns scoring behavior)

Notification delivery remains deferred to the CORE Version 2 shared notification framework. No MVP ALERT notification event is defined.

Compliance-sourced alert generation remains outside the current MVP FDS scope.

### Related AI Agents

**Primary Agent:** Alert Triage Assistant

Alert Triage Assistant is assistive only. It may suggest triage actions for human review. It shall not autonomously assign, close, escalate, or enforce alert outcomes.

ALERT does not establish a duplicate explanation platform capability. Explanation assistance is owned by RISK and the AI Platform according to the existing domain architecture. ALERT may present or consume permitted explanation outputs where appropriate for triage context. Humans remain decision-makers for all alert disposition actions.

### Security Considerations

Alert access and reassignment shall be restricted by role (AUTHZ evaluates access). Alert status changes shall be auditable. ALERT shall respect organization scope through contextual ORG tenant boundaries without managing organization lifecycle.

### Success Criteria

- Alerts can be created and operationally prioritized
- Alerts can be assigned
- Alerts can be closed through lifecycle management
- Alert context can be associated with investigations (INVEST owns case creation; INVEST consumes `AlertCreated`)

### Domain KPIs

- Alert response time
- Alert acknowledgement SLA attainment
- Escalation rate (Version 2+ when escalation policies apply)
- False-positive alert rate

### Domain Risks

- Alert fatigue from high volume
- Missed escalations (Version 2+ escalation scope)
- Duplicate alerts fragmenting triage

### Domain Roadmap

**MVP**

- Alert Generation
- Alert Assignment
- Alert Prioritization
- Alert Lifecycle

**Version 2**

- Alert Escalation Policies
- Deduplication

**Version 3**

- Auto-Triage Suggestions
- SLA Automation

### Domain Relationships

**Depends on:** CORE, AUTH, AUTHZ, USER, ORG (contextual — tenant/organization scope; organization lifecycle owned by ORG), RISK (risk signals; scoring and risk-derived priority signals owned by RISK)

**Supports:** INVEST, DASH, REPORT, AI

**Prioritization boundary:** RISK owns risk scoring, rules, and risk-derived priority signals aligned to RI-BR-002. ALERT owns operational alert priority and handling state used for alert management and queue ordering. ALERT does not duplicate RISK scoring or rule evaluation.

**Investigation boundary:** INVEST owns investigation cases and investigation workflow. ALERT does not create or own investigation cases. ALERT may associate alert context with investigations. INVEST consumes `AlertCreated` to initiate investigation workflows.

**Escalation boundary:** Alert-level escalation records and Alert Escalation Policies are Version 2 ALERT scope. INVEST owns case-level escalation records. MVP does not include alert escalation behavior.

**DASH downstream contract:** DASH consumes `AlertCreated` to refresh work queue presentations (DASH-FR-011). ALERT publishes `AlertCreated` for eligible alert creation outcomes.

**Authorization and tenant scope:** AUTHZ owns authorization decisions. ORG provides tenant context; USER owns user-organization association. ALERT applies authorized alert lifecycle outcomes within permitted scope.

**Deferred integration notes:** `ThreatDetected` consumption and SEC-sourced alert inputs remain deferred until SEC Version 2. Compliance-sourced alert generation remains outside MVP FDS scope. Autonomous triage or enforcement is out of scope.

**Consumer-domain sequencing:** ALERT may be authored before RISK FRs are complete by specifying alert lifecycle and RISK-event consumption behavior only. ALERT must not invent RISK scoring, rules, or explanation behavior.

---

## Domain — RISK: Risk Intelligence

### Domain Information

| Field | Value |
|-------|-------|
| Domain ID | `RISK` |
| Domain Name | Risk Intelligence |
| Domain Owner | Risk Intelligence Team |
| Purpose | Assess, prioritize, and explain operational risk. |
| Business Value | Faster, consistent, explainable risk evaluation across transactions, accounts, devices, and user behavior. |
| Related Business Requirements | RI-BR-001, RI-BR-002, RI-BR-003 |
| Related Business Objectives | BO-001, BO-002, BO-004, BO-008 |
| Primary Users | Risk Analysts, Fraud Investigators, Compliance Officers |
| Dependencies | CORE, AUTH, AUTHZ, USER, ORG (contextual — tenant/organization scope; lifecycle owned by ORG) |
| Dependent Domains | ALERT, INVEST, COMP, REPORT, DASH, AI |
| Estimated Functional Requirements | 45 (planning guideline; not a forced count) |
| Priority | Critical |
| Release | MVP |
| FR Prefix | `RISK-FR` |

### Domain Overview

The Risk Intelligence domain provides centralized capabilities for assessing, explaining, and producing risk-derived priority signals across transactions, devices, and user behavior. It is a primary decision-support domain for detecting activity that requires analyst review.

RISK owns risk scoring, rules/configuration, explanations, and risk-derived priority signals. Critical risk assessment remains capable of operating without a mandatory AI or large-language-model dependency. AI assistance, where used, is contextual and assistive only.

Explicit continuous monitoring automation and background re-evaluation are deferred unless required by an approved MVP feature or business requirement.

### Responsibilities

The domain is responsible for:

- Risk scoring and rule evaluation
- Risk configuration (coherent with Rule Engine capability)
- Behavioral analysis
- Risk explanation (carried within MVP `RiskCalculated` outcomes)
- Risk prioritization through **risk-derived priority signals** (RI-BR-002)

RISK does not own operational alert priority, alert lifecycle, investigation cases, sanctions screening workflow, wallet profile ownership, or dashboard/workspace presentation.

### Features

- Rule Engine / Risk Configuration
- Transaction Risk Scoring
- Device Risk Analysis
- Behavioral Risk Analysis
- Risk Explanation
- Risk Prioritization (risk-derived priority signals)

### Future Features

- Wallet Risk Scoring
- Risk Timeline
- Risk History
- Risk Dashboard (presentation deferred — DASH owns workspace/dashboard presentation)
- Adaptive Risk Models
- ML-based Scoring
- Graph-enhanced Risk Signals
- Cross-Exchange Intelligence
- Federated Risk Sharing

### Domain Data Ownership

This domain owns:

- Risk Scores
- Risk Rules / Risk Configuration
- Risk Explanations
- Behavioral Risk Features
- Risk-derived Priority Signals

Risk-derived priority signals support RI-BR-002. ALERT owns operational alert priority and handling state used for alert queues (frozen ALERT-FR-003). RISK does not own operational alert records or alert lifecycle.

### Domain Events

**Publishes (MVP)**

- `RiskCalculated` (includes embedded risk explanation outputs for MVP)
- `HighRiskDetected`

**Publishes (deferred — not MVP)**

- `RiskUpdated`
- `RiskExplanationGenerated` (separate explanation publish event deferred; explanation is embedded in `RiskCalculated` for MVP)

**Consumes (MVP — external integration input only)**

- `TransactionReceived` — classified as an external integration input via Exchange Event Stream. It is not published by RISK or by any Sentinel domain in the current FDS baseline.

**Deferred consumption (Version 2+)**

- Wallet-related events (including wallet profile/reputation updates) — deferred until WALLET Version 2
- `AlertCreated` — not consumed in MVP; no feedback/rescoring loop in MVP baseline
- `DeviceSignalReceived` — deferred until applicable upstream/device or SEC integration is available

### Non-Functional Characteristics

| Characteristic | Business Expectation |
|----------------|----------------------|
| Availability | 99.9% for risk evaluation path |
| Scalability | Horizontal with transaction volume |
| Performance | Risk evaluation within interactive business targets for supported synchronous paths |
| Security | Policy configuration privileged (AUTHZ evaluates access) |
| Auditability | Full audit of scores, rules, and explanations |
| Observability | Metrics + Logs + Traces |

### External Integrations

- Exchange Event Stream (external ingestion boundary; includes transaction-oriented inputs such as `TransactionReceived` classified as external integration events)
- Device Fingerprinting (device-oriented signal boundary for Device Risk Analysis)
- Blockchain Intelligence (high-level intelligence boundary; wallet-oriented scoring remains Version 2 with WALLET)

Sanctions screening workflow remains COMP-owned. RISK does not own sanctions processing in MVP. External sanctions intelligence may be considered a future signal boundary outside approved MVP behavior and is not an MVP RISK integration requirement.

### Related AI Agents

**Primary Agent:** Risk Analysis Agent

**Supporting Agents:**
- Behavior Analysis Agent

AI agents referenced for RISK are assistive only. They may suggest analyses or explanations for human review. They shall not autonomously enforce, block, or disposition transactions, accounts, or alerts.

RISK owns the risk explanation domain capability embedded in MVP outcomes. AI Platform orchestration owns agent lifecycle and orchestration. RISK does not establish a duplicate Explanation Agent platform capability.

Critical risk scoring and rule evaluation must remain operable without mandatory AI dependency, consistent with Vision deterministic-capable foundation requirements.

### Security Considerations

Risk rules/configuration shall be manageable only by authorized administrators (AUTHZ evaluates access). Risk explanations shall not expose unauthorized sensitive internals beyond analyst need. RISK shall respect organization scope through contextual ORG tenant boundaries without managing organization lifecycle.

### Success Criteria

- Risk scores can be generated
- Explainable results are available within MVP `RiskCalculated` outcomes
- Configurable risk rules/policies are supported
- Risk-derived priority signals are produced for downstream consumers
- Near-real-time evaluation is feasible for supported event types

### Domain KPIs

- Risk score latency
- High-risk precision/recall proxies
- Explanation coverage rate
- Rule change-to-effect time

### Domain Risks

- High false positives
- Slow risk evaluation under load
- Opaque or incomplete explanations
- Stale risk rules

### Domain Roadmap

**MVP**

- Rule Engine / Risk Configuration
- Transaction Risk Scoring
- Device Risk Analysis
- Behavioral Risk Analysis
- Risk Explanation
- Risk Prioritization (risk-derived priority signals)

**Version 2**

- Wallet Risk Scoring
- Risk History Expansion
- Risk Timeline
- ML-based Scoring
- Graph-enhanced Risk Signals

**Version 3**

- Adaptive Learning
- Cross-Exchange Intelligence
- Federated Risk Sharing

### Domain Relationships

**Depends on:** CORE, AUTH, AUTHZ, USER, ORG (contextual — tenant/organization scope; lifecycle owned by ORG)

**Supports:** ALERT, INVEST, COMP, REPORT, DASH, AI

**Prioritization boundary:** RISK owns risk scoring, rules, and **risk-derived priority signals** (RI-BR-002). ALERT owns **operational alert priority** and alert handling state (frozen ALERT-FR-003). RISK does not own alert queues or alert lifecycle.

**Producer/downstream contract:** RISK publishes `RiskCalculated` and `HighRiskDetected` for MVP. ALERT consumes both (frozen ALERT-FR-001, ALERT-FR-011). DASH consumes `RiskCalculated` (frozen DASH-FR-011). INVEST and COMP consume `RiskCalculated`. AI may consume `RiskCalculated` for assistive workflows.

**Investigation boundary:** INVEST owns investigation cases and workflow. RISK does not create or own investigation cases.

**Presentation boundary:** DASH owns workspace/dashboard presentation. RISK owns risk data, assessments, scores, explanations, and signals — not dashboard presentation. Risk Dashboard is not an MVP RISK-owned presentation capability.

**Sanctions boundary:** COMP owns sanctions screening workflow. RISK does not duplicate sanctions processing in MVP.

**Wallet boundary:** Wallet Risk Scoring and wallet-event consumption are Version 2, aligned with WALLET.

**AI boundary:** AI assistance is contextual and assistive. AI Platform owns agent orchestration/lifecycle. RISK core scoring is not mandatory-AI-dependent.

**Authorization and tenant scope:** AUTHZ owns authorization decisions. ORG provides tenant context. RISK applies authorized outcomes within permitted scope.

**External ingestion:** `TransactionReceived` is an external integration input only. RISK does not invent an upstream Sentinel domain publisher for it.

**Deferred events and consumption:** `RiskUpdated`, separate `RiskExplanationGenerated`, wallet-related events, `AlertCreated`, and `DeviceSignalReceived` are not MVP RISK baseline events/consumes.

---

## Domain — INVEST: Investigation Management

### Domain Information

| Field | Value |
|-------|-------|
| Domain ID | `INVEST` |
| Domain Name | Investigation Management |
| Domain Owner | Investigation Platform Team |
| Purpose | Manage investigation cases, evidence, assignments, and investigation lifecycle. |
| Business Value | Unified investigation workspace that reduces manual effort and improves consistency. |
| Related Business Requirements | FI-BR-001, FI-BR-002 (MVP); FI-BR-003 (Version 2) |
| Related Business Objectives | BO-001, BO-004, BO-005 |
| Primary Users | Fraud Investigators, Risk Analysts, Compliance Officers, Security Engineers |
| Dependencies | CORE, AUTH, AUTHZ, USER, ORG (contextual — tenant/organization scope; lifecycle owned by ORG), ALERT, RISK, AI (assistive integration — agent orchestration owned by AI Platform) |
| Dependent Domains | COMP, REPORT, DASH, WALLET, SEC |
| Estimated Functional Requirements | 50 (planning guideline; not a forced count) |
| Priority | Critical |
| Release | MVP |
| FR Prefix | `INVEST-FR` |

### Domain Overview

The Investigation Management domain consolidates case management, evidence collection, timelines, notes, and investigator assignments into a single operational investigation capability. It merges what were previously separate case, evidence, and fraud investigation concerns into one enterprise investigation domain.

INVEST owns investigation cases and investigation workflow authority. It does not own risk scoring (RISK), operational alert lifecycle (ALERT), sanctions/compliance workflows (COMP), dashboard/workspace presentation (DASH), or AI agent orchestration (AI Platform). AI assistance integrated into investigation workflows remains assistive and human-controlled.

Version 2 expands INVEST with collaboration (FI-BR-003), AI-assisted summaries, and escalation workflows.

### Responsibilities

The domain is responsible for:

- Manage investigation cases end-to-end
- Collect and organize evidence, including attached artifacts where appropriate
- Maintain investigation timelines and notes
- Support investigator assignments
- Consume upstream alert and risk context without redefining ALERT or RISK lifecycle behavior

**Version 2 responsibilities (deferred):**

- Support cross-functional investigation collaboration (FI-BR-003)
- Enable AI-assisted investigation summaries through AI Platform integration
- Support case-level escalation workflows

INVEST does not create or own operational alerts, calculate risk scores, own sanctions/compliance workflows, or own dashboard presentation.

### Features

- Case Management
- Evidence Collection (includes attached artifacts/evidence attachments as part of evidence records)
- Investigation Timeline
- Notes
- Assignments

### Future Features

- Collaboration Expansion (FI-BR-003)
- AI Summary
- Escalation Workflows
- Investigation Playbooks
- Cross-Case Link Analysis
- Automated Evidence Packaging for Audit

### Domain Data Ownership

This domain owns:

- Cases
- Evidence (including attached artifacts where applicable as part of evidence records)
- Assignments
- Notes
- Timeline Events
- Escalation Records (data ownership; escalation workflow behavior is Version 2)

INVEST does not own risk scores, operational alerts, wallet profiles, AI recommendation records, or compliance records. Those remain owned by RISK, ALERT, WALLET, AI, and COMP respectively. INVEST may consume permitted upstream context from those domains without claiming ownership.

### Domain Events

**Publishes (MVP)**

- `CaseCreated`
- `CaseUpdated`
- `CaseClosed`
- `CaseAssigned`
- `EvidenceAttached`

**Publishes (Version 2 — when Escalation Workflows apply)**

- `InvestigationEscalated`

**Consumes (MVP)**

- `AlertCreated`
- `RiskCalculated`

**Deferred consumption (Version 2+)**

- `AIRecommendationGenerated` — deferred until AI Summary and applicable AI Platform integration are Version 2 scope for INVEST
- `WalletProfileUpdated` — deferred until WALLET Version 2

INVEST does not consume `HighRiskDetected` in MVP. Upstream alert context is consumed via `AlertCreated` where applicable.

### Non-Functional Characteristics

| Characteristic | Business Expectation |
|----------------|----------------------|
| Availability | 99.9% |
| Scalability | Horizontal by case volume and concurrent investigators |
| Performance | Case open and evidence retrieval within interactive targets |
| Security | Strict case/evidence access control |
| Auditability | Full investigation audit trail required |
| Observability | Metrics + Logs + Traces |

### External Integrations

- Document Storage
- Ticketing Systems (Version 2)
- Notification Service (Version 2 — notification infrastructure owned by CORE shared notification framework; INVEST does not own notification delivery)

### Related AI Agents

No AI agents are owned or orchestrated by INVEST.

AI Platform owns agent lifecycle and orchestration, including Investigation Agent, Evidence Retrieval Agent, Report Generation Agent, and Summarization Agent capabilities. INVEST may integrate assistive AI Platform outputs in authorized investigation workflows when applicable. AI assistance does not replace human decision authority for investigation disposition, enforcement, or case closure.

AI Summary remains Version 2 INVEST scope. MVP INVEST does not establish a separate AI-assistance capability solely because AI Platform provides an Investigation Agent.

### Security Considerations

Investigation access shall be restricted by role and assignment (AUTHZ evaluates access). Evidence integrity and investigation audit trails shall be preserved. INVEST shall respect organization scope through contextual ORG tenant boundaries without managing organization lifecycle.

### Success Criteria

- Cases can be created, updated, and closed
- Evidence can be collected, attached, and reviewed
- Investigator assignments are supported
- Investigation timelines and notes are maintained
- Investigation history is auditable

### Domain KPIs

- Average investigation time
- Case backlog age
- Evidence completeness rate
- Reopened case rate

### Domain Risks

- Incomplete evidence
- Unowned or stalled cases
- Collaboration conflicts (Version 2+ collaboration scope)
- Evidence integrity issues

### Domain Roadmap

**MVP**

- Case Management
- Evidence Collection
- Assignments
- Notes
- Investigation Timeline

**Version 2**

- Collaboration Expansion (FI-BR-003)
- AI Summary
- Escalation Workflows

**Version 3**

- Investigation Playbooks
- Cross-Case Link Analysis

### Domain Relationships

**Depends on:** CORE, AUTH, AUTHZ, USER, ORG (contextual — tenant/organization scope; lifecycle owned by ORG), ALERT, RISK, AI (assistive integration only)

**Supports:** COMP, REPORT, DASH, WALLET, SEC

**Investigation boundary:** INVEST owns investigation cases and investigation workflow. ALERT does not create or own investigation cases. ALERT may associate alert context with investigations. INVEST consumes `AlertCreated` for applicable upstream alert context.

**Risk boundary:** RISK owns risk scoring, rules, and risk-derived priority signals. INVEST consumes `RiskCalculated` for contextual enrichment only. INVEST does not score risk and does not consume `HighRiskDetected` in MVP.

**Alert boundary:** ALERT owns operational alert records, lifecycle, and operational alert priority. INVEST does not publish alerts.

**Presentation boundary:** DASH owns workspace/dashboard presentation. INVEST publishes investigation case events for downstream refresh; DASH consumes `CaseUpdated` (frozen DASH-FR-011).

**Compliance boundary:** COMP owns sanctions and compliance workflows. INVEST supports compliance through owned case/evidence outcomes and published case events without duplicating compliance processing.

**Escalation boundary:** INVEST owns case-level Escalation Records as data. Escalation Workflows and `InvestigationEscalated` publication are Version 2. Alert-level escalation remains ALERT Version 2 scope.

**AI boundary:** AI Platform owns agent orchestration and recommendation generation. INVEST owns investigation workflow authority. AI assistance is assistive only and does not own INVEST lifecycle. `AIRecommendationGenerated` consumption is deferred to Version 2.

**Wallet boundary:** `WalletProfileUpdated` consumption is deferred until WALLET Version 2.

**Authorization and tenant scope:** AUTHZ owns authorization decisions. ORG provides tenant context. INVEST applies authorized investigation outcomes within permitted scope.

**Producer/downstream contract:** MVP INVEST publishes `CaseCreated`, `CaseUpdated`, `CaseClosed`, `CaseAssigned`, and `EvidenceAttached`. DASH consumes `CaseUpdated` (frozen DASH-FR-011). COMP, REPORT, WALLET, SEC, and AI may consume applicable INVEST events according to their domain contracts without INVEST redefining their behavior.

**Consumer-domain sequencing:** INVEST may be authored before upstream ALERT/RISK FRs are complete by specifying investigation lifecycle, upstream consumption (`AlertCreated`, `RiskCalculated`), and downstream publication behavior only. INVEST must not invent ALERT alert lifecycle, RISK scoring, or AI orchestration behavior. INVEST consumes upstream context; INVEST owns investigation workflow after receiving applicable upstream context; downstream consumers receive INVEST-owned case events.

---

## Domain — WALLET: Wallet Intelligence

### Domain Information

| Field | Value |
|-------|-------|
| Domain ID | `WALLET` |
| Domain Name | Wallet Intelligence |
| Domain Owner | Financial Crime Intelligence Team |
| Purpose | Analyze wallet profiles, reputation, and blockchain relationships. |
| Business Value | Contextual blockchain intelligence for financial crime investigations. |
| Related Business Requirements | WI-BR-001 |
| Related Business Objectives | BO-002 |
| Primary Users | Fraud Investigators, Risk Analysts, Financial Crime Investigators |
| Dependencies | CORE, AUTH, AUTHZ, USER (contextual — authenticated actor identity; lifecycle owned by USER), ORG (contextual — tenant/organization scope; lifecycle owned by ORG), RISK, INVEST |
| Dependent Domains | AI, REPORT, COMP |
| Estimated Functional Requirements | ~10 (planning guideline; not a forced count) |
| Priority | Medium |
| Release | Version 2 |
| FR Prefix | `WALLET-FR` |

### Domain Overview

The Wallet Intelligence domain analyzes blockchain wallet activity, address reputation, transaction history, and relationships to support financial crime investigations and risk assessment.

WALLET is a **Version 2 domain only**. It has **no platform-MVP capabilities, foundational hooks, or MVP Functional Requirements**. All WALLET features and event contracts defined in this specification apply to Version 2 release scope.

WALLET owns wallet intelligence data — profiles, reputation records, activity timelines, and relationship graphs. It does not own risk scoring (RISK), investigation case lifecycle (INVEST), operational alert lifecycle (ALERT), dashboard/workspace presentation (DASH), compliance records (COMP), reporting definitions (REPORT), user lifecycle (USER), organization lifecycle (ORG), or AI agent orchestration (AI Platform). WALLET may consume permitted upstream context from RISK and INVEST without redefining their behavior.

### Responsibilities

The domain is responsible for:

- Profile wallets and addresses
- Assess address reputation
- Present transaction history
- Expose relationship graphs for investigation
- Consume upstream investigation and risk context for enrichment without redefining INVEST or RISK lifecycle behavior

WALLET does not create or own operational alerts, calculate risk scores, create or manage investigation cases, own sanctions/compliance workflows, own dashboard presentation, or orchestrate AI agents.

### Features (Version 2)

- Wallet Profiles
- Address Reputation
- Transaction History
- Relationship Graph

### Future Features (Version 3 — not Version 2)

- Multi-Chain Expansion
- Cluster Detection
- Cross-Exchange Correlation

These future capabilities are explicitly deferred beyond Version 2 and shall not be authored as Version 2 Functional Requirements.

### Functional Requirements Planning Baseline

The intended Version 2 FRS inventory is approximately 10 requirements (`WALLET-FR-001` through `WALLET-FR-010`), determined from locked Version 2 capabilities rather than a mandatory count:

- `WALLET-FR-001` — Manage Wallet Profiles
- `WALLET-FR-002` — Assess Address Reputation
- `WALLET-FR-003` — Present Wallet Transaction History
- `WALLET-FR-004` — Expose Wallet Relationship Graph
- `WALLET-FR-005` — Retrieve And Discover Wallet Intelligence
- `WALLET-FR-006` — Ingest External Transaction Inputs For Wallet Enrichment
- `WALLET-FR-007` — Enforce Wallet Event Publication Contract
- `WALLET-FR-008` — Consume Upstream Investigation And Risk Context Events
- `WALLET-FR-009` — Record Wallet Intelligence Audit Outcomes
- `WALLET-FR-010` — Restrict Wallet Data Access To Authorized Actors

Detailed Functional Requirement authoring is deferred to the FRS phase. This baseline is a planning guide only.

### Domain Data Ownership

This domain owns:

- Wallet Profiles
- Address Reputation Records
- Wallet Relationship Graphs
- Wallet Activity Timelines

WALLET does not own risk scores, investigation cases, operational alerts, compliance records, AI recommendation records, or reporting definitions. Those remain owned by RISK, INVEST, ALERT, COMP, AI, and REPORT respectively.

### Domain Events

**Publishes (Version 2)**

- `WalletProfileUpdated`
- `AddressReputationChanged`
- `SuspiciousWalletDetected`

**Consumes (Version 2)**

- `TransactionReceived` — classified as an external integration input via Exchange Event Stream. It is not published by WALLET or by any Sentinel domain in the current FDS baseline.
- `CaseCreated` — INVEST-owned publication consumed by WALLET for investigation context enrichment only
- `RiskCalculated` — RISK-owned publication consumed by WALLET for risk context enrichment only

**Explicit exclusions (not WALLET Version 2 baseline consumption)**

WALLET does not consume `HighRiskDetected`, `AlertCreated`, `CaseUpdated`, `CaseClosed`, `EvidenceAttached`, `AIRecommendationGenerated`, or any WALLET self-publication events in Version 2 baseline.

**Downstream consumption (deferred / unspecified)**

- `WalletProfileUpdated` — INVEST may consume when INVEST Version 2 wallet-context integration is authored (frozen INVEST baseline defers this consumption until WALLET Version 2). No other frozen downstream consumer is currently established.
- `AddressReputationChanged` — WALLET-owned Version 2 publication. No frozen downstream consumer is currently established; downstream routing remains a future contract decision.
- `SuspiciousWalletDetected` — WALLET-owned Version 2 publication. No frozen downstream consumer is currently established; downstream routing remains a future contract decision. This is not a platform-MVP or Version 2 cross-domain obligation until explicitly contracted.

WALLET does not publish `TransactionReceived`. Upstream transaction-oriented inputs are external integration boundaries only.

### Non-Functional Characteristics

| Characteristic | Business Expectation |
|----------------|----------------------|
| Availability | 99.5% (provider-dependent paths may degrade gracefully) |
| Scalability | Horizontal with address/transaction growth |
| Performance | Profile and history retrieval within investigation workflow targets |
| Security | Investigator-only access to wallet intelligence |
| Auditability | Access and enrichment actions audited |
| Observability | Metrics + Logs + Traces |

### External Integrations

- Blockchain Analytics Providers
- Exchange Wallet Event Sources (external ingestion boundary; may include transaction-oriented inputs such as `TransactionReceived` classified as external integration events)

### Related AI Agents

No AI agents are owned or orchestrated by WALLET.

AI Platform owns agent lifecycle and orchestration, including Wallet Analysis Agent, Explanation Agent, and Evidence Retrieval Agent capabilities where applicable. WALLET may integrate assistive AI Platform outputs in authorized wallet intelligence workflows when applicable. AI assistance does not replace human decision authority for investigation disposition, enforcement, or wallet intelligence conclusions.

### Security Considerations

Wallet intelligence data shall be accessed only by authorized investigators and shall not expose unnecessary customer PII beyond investigation need. WALLET shall respect organization scope through contextual ORG tenant boundaries without managing organization lifecycle. AUTHZ evaluates access; USER provides authenticated actor identity context.

### Success Criteria

- Wallet profiles are retrievable
- Reputation signals are available
- Relationship views support investigations

### Domain KPIs

- Wallet profile enrichment latency
- Reputation coverage rate
- Investigation usage rate of wallet views

### Domain Risks

- Provider data quality gaps
- Stale reputation signals
- Over-linking false relationship edges

### Domain Roadmap

**MVP (platform release)**

- None. WALLET is a Version 2 domain with no platform-MVP Functional Requirements, event contracts, or foundational hooks.

**Version 2**

- Wallet Profiles
- Address Reputation
- Transaction History
- Relationship Graph

**Version 3**

- Multi-Chain Expansion
- Cluster Detection
- Cross-Exchange Correlation

### Domain Relationships

**Depends on:** CORE, AUTH, AUTHZ, USER (contextual — authenticated actor identity; lifecycle owned by USER), ORG (contextual — tenant/organization scope; lifecycle owned by ORG), RISK, INVEST

**Supports:** AI, REPORT, COMP

**Risk boundary:** RISK owns risk scoring, rules, and risk-derived priority signals. WALLET consumes `RiskCalculated` for contextual enrichment only. WALLET does not score risk, does not publish `RiskCalculated` or `HighRiskDetected`, and does not consume `HighRiskDetected`.

**Investigation boundary:** INVEST owns investigation cases and investigation workflow. WALLET consumes `CaseCreated` for investigation context enrichment only. WALLET does not create or manage investigation cases and does not consume `CaseUpdated`, `CaseClosed`, or `EvidenceAttached` in Version 2 baseline.

**Alert boundary:** ALERT owns operational alert records, lifecycle, and operational alert priority. WALLET does not create or manage alerts and does not publish alert events.

**Presentation boundary:** DASH owns workspace/dashboard presentation. WALLET owns wallet intelligence data and does not own dashboard UI or workspace widgets.

**Compliance boundary:** COMP owns compliance records and regulatory workflows. WALLET supports investigations through owned wallet intelligence outcomes without duplicating compliance processing.

**Reporting boundary:** REPORT owns reporting definitions and report presentation. WALLET may supply data for downstream reporting without owning report definitions.

**AI boundary:** AI Platform owns agent orchestration and recommendation generation. WALLET owns wallet intelligence data and enrichment authority. AI assistance is assistive only and does not own WALLET lifecycle.

**Authorization and tenant scope:** AUTHZ owns authorization decisions. ORG provides tenant context. USER provides authenticated actor identity. WALLET applies authorized outcomes within permitted scope without managing USER or ORG lifecycle.

**External ingestion:** `TransactionReceived` is an external integration input only. WALLET does not invent an upstream Sentinel domain publisher for it and does not publish `TransactionReceived`.

**Producer/downstream contract:** Version 2 WALLET publishes `WalletProfileUpdated`, `AddressReputationChanged`, and `SuspiciousWalletDetected`. INVEST may consume `WalletProfileUpdated` when INVEST Version 2 wallet-context integration is authored. No frozen downstream consumer is currently established for `AddressReputationChanged` or `SuspiciousWalletDetected`; downstream routing remains a future contract decision.

**Platform scope:** WALLET has no platform-MVP Functional Requirements. All WALLET capabilities in this specification are Version 2 scope.

---

## Domain — COMP: Compliance & Travel Rule

### Domain Information

| Field | Value |
|-------|-------|
| Domain ID | `COMP` |
| Domain Name | Compliance & Travel Rule |
| Domain Owner | Compliance Engineering |
| Purpose | Support KYC, AML, Travel Rule, sanctions, and audit preparation. |
| Business Value | Improved regulatory readiness and audit-ready compliance workflows. |
| Related Business Requirements | CP-BR-001, CP-BR-002 |
| Related Business Objectives | BO-003 |
| Primary Users | Compliance Officers, Auditors, Risk Managers |
| Dependencies | CORE, AUTH, AUTHZ, USER (contextual — authenticated actor identity; lifecycle owned by USER), ORG (contextual — tenant/organization scope; lifecycle owned by ORG), INVEST, RISK, AI (assistive integration only — agent orchestration owned by AI Platform; not a mandatory MVP runtime dependency) |
| Dependent Domains | REPORT, ADMIN |
| Estimated Functional Requirements | ~11 (planning guideline; not a forced count) |
| Priority | High |
| Release | MVP |
| FR Prefix | `COMP-FR` |

### Domain Overview

The Compliance & Travel Rule domain supports regulatory compliance workflows including KYC review, AML review, Travel Rule validation, sanctions screening, and audit preparation. It standardizes compliance evidence and operational documentation for regulatory readiness (BO-003, CP-BR-001, CP-BR-002).

COMP is a **platform MVP domain**. All COMP features and event contracts defined in this specification apply to MVP release scope unless explicitly marked Version 2 or Version 3.

COMP owns compliance workflows, compliance records, Travel Rule validations, sanctions screening workflow, compliance evidence packages, and audit preparation packages. It does not own risk scoring (RISK), investigation case lifecycle (INVEST), operational alert lifecycle (ALERT), wallet intelligence (WALLET), dashboard/workspace presentation (DASH), reporting definitions (REPORT), user lifecycle (USER), organization lifecycle (ORG), or AI agent orchestration (AI Platform). COMP may consume permitted upstream context from INVEST, RISK, and USER without redefining their behavior.

COMP supports compliance workflows around investigations but does not own INVEST case lifecycle. CP-BR-002 compliance evidence management is satisfied through COMP-owned compliance data and applicable investigation/context information available through the locked MVP consumption model. COMP does not consume `EvidenceAttached` in MVP baseline.

ProductScope terminology may refer to “KYC Investigation Support” and related compliance capabilities; in this specification, **KYC Review** and **AML Review** are distinct MVP features. Generic regulatory-report generation and Compliance Analytics remain deferred beyond MVP COMP scope and belong to REPORT or Version 2/Version 3 capabilities as defined below.

### Responsibilities

The domain is responsible for:

- Support KYC review workflows
- Support AML review workflows
- Validate Travel Rule requirements
- Support sanctions screening workflows
- Maintain compliance evidence packages
- Support audit preparation
- Consume upstream investigation, risk, and user context for enrichment without redefining INVEST, RISK, or USER lifecycle behavior

COMP does not create or own operational alerts, calculate risk scores, create or manage investigation cases, own wallet intelligence, own dashboard presentation, generate REPORT-owned regulatory reports, or orchestrate AI agents. MVP COMP core workflows are operable without AI.

### Features (MVP)

- KYC Review
- AML Review
- Travel Rule
- Sanctions Screening
- Audit Preparation

KYC Review and AML Review are distinct MVP features and shall not be merged.

### Future Features (Version 2 — not MVP)

- Jurisdiction Policy Packs (also referred to as Jurisdiction-Specific Policy Packs in product materials)
- Compliance Analytics

These capabilities are explicitly deferred beyond MVP and shall not be authored as MVP Functional Requirements.

### Future Features (Version 3 — not MVP)

- Automated Regulatory Drafting (also referred to as Automated Regulatory Report Drafting in product materials)
- Continuous Control Monitoring

These capabilities are explicitly deferred beyond MVP and shall not be authored as MVP Functional Requirements.

### Functional Requirements Planning Baseline

The intended MVP FRS inventory is approximately 11 requirements (`COMP-FR-001` through `COMP-FR-010`), determined from locked MVP capabilities rather than a mandatory count:

- `COMP-FR-001` — Support KYC Review Workflows
- `COMP-FR-002` — Support AML Review Workflows
- `COMP-FR-003` — Validate Travel Rule Requirements
- `COMP-FR-004` — Support Sanctions Screening Workflows
- `COMP-FR-005` — Support Audit Preparation
- `COMP-FR-006` — Retrieve And Discover Compliance Records
- `COMP-FR-007` — Enforce Compliance Event Publication Contract
- `COMP-FR-008` — Consume Upstream Investigation Risk And User Context Events
- `COMP-FR-009` — Record Compliance Audit Outcomes
- `COMP-FR-010` — Restrict Compliance Data Access To Authorized Actors

Detailed Functional Requirement authoring is deferred to the FRS phase. This baseline is a planning guide only. No `COMP-FR-011` is defined unless a future locked FDS decision introduces a genuinely distinct capability.

### Domain Data Ownership

This domain owns:

- Compliance Records
- Travel Rule Validations
- Sanctions Screening Results
- Compliance Evidence Packages
- Audit Preparation Packages

COMP does not own risk scores, investigation cases, operational alerts, wallet profiles, AI recommendation records, reporting definitions, or user/organization lifecycle records. Those remain owned by RISK, INVEST, ALERT, WALLET, AI, REPORT, USER, and ORG respectively.

### Domain Events

**Publishes (MVP)**

- `ComplianceReviewed`
- `TravelRuleValidated`
- `SanctionsHitDetected`
- `AuditPackagePrepared`

**Consumes (MVP)**

- `CaseClosed` — INVEST-owned publication consumed by COMP for investigation context enrichment only
- `CaseUpdated` — INVEST-owned publication consumed by COMP for investigation context enrichment only
- `RiskCalculated` — RISK-owned publication consumed by COMP for risk context enrichment only
- `UserUpdated` — USER-owned publication consumed by COMP for user/KYC context enrichment only

**Explicit exclusions (not COMP MVP baseline consumption)**

COMP does not consume `HighRiskDetected`, `AlertCreated`, `EvidenceAttached`, `CaseCreated`, `WalletProfileUpdated`, `AIRecommendationGenerated`, `TransactionReceived`, or any COMP self-publication events in MVP baseline.

**Downstream consumption (deferred / unspecified)**

- `ComplianceReviewed` — REPORT may consume when REPORT Version 2 scope is authored. REPORT is not an MVP COMP dependency. No frozen MVP downstream consumer is currently established.
- `TravelRuleValidated` — COMP-owned MVP publication. No frozen downstream consumer is currently established; downstream routing remains a future contract decision.
- `SanctionsHitDetected` — COMP-owned MVP publication. No frozen downstream consumer is currently established; downstream routing remains a future contract decision. COMP does not create operational alerts. ALERT compliance-sourced alert generation remains deferred.
- `AuditPackagePrepared` — COMP-owned MVP publication. No frozen downstream consumer is currently established; downstream routing remains a future contract decision.

COMP does not publish `AlertCreated`, `RiskCalculated`, `HighRiskDetected`, `CaseCreated`, `CaseUpdated`, `CaseClosed`, `EvidenceAttached`, `TransactionReceived`, `WalletProfileUpdated`, or `AIRecommendationGenerated`.

### Non-Functional Characteristics

| Characteristic | Business Expectation |
|----------------|----------------------|
| Availability | 99.9% |
| Scalability | Horizontal with compliance case volume |
| Performance | Compliance workflow actions within operational targets |
| Security | Strict compliance-role access and retention controls |
| Auditability | Full regulatory auditability required |
| Observability | Metrics + Logs + Traces |

### External Integrations

Provider-neutral external integration boundaries embedded within MVP capabilities (no separate external-ingest event contract in MVP):

- KYC Providers
- Sanctions Screening Providers
- Travel Rule Networks

Automated regulatory report generation and regulatory reporting presentation remain REPORT or Version 2/Version 3 deferred scope, not MVP COMP-owned capabilities.

### Related AI Agents

No AI agents are owned or orchestrated by COMP.

AI Platform owns agent lifecycle and orchestration, including Compliance Agent, Audit Preparation Assistant, and Evidence Retrieval Agent capabilities where applicable. COMP may integrate assistive AI Platform outputs in authorized compliance workflows when applicable. AI assistance does not replace human decision authority for compliance disposition or regulatory outcomes. MVP COMP core workflows are not mandatory-AI-dependent.

### Security Considerations

Compliance evidence and regulatory records shall be protected, retention-aware, and accessible only to authorized compliance and audit roles. COMP shall respect organization scope through contextual ORG tenant boundaries without managing organization lifecycle. AUTHZ evaluates access; USER provides authenticated actor identity context.

### Success Criteria

- Compliance workflows follow standardized MVP capabilities
- Travel Rule validation is supported
- Sanctions screening workflow is supported within COMP ownership
- Audit preparation packages can be assembled from COMP-owned compliance data and applicable context

### Domain KPIs

- Compliance completion rate
- Travel Rule validation turnaround
- Audit package preparation time
- Sanctions review cycle time

### Domain Risks

- Regulatory rule changes
- Incomplete compliance evidence
- False sanctions matches
- Retention policy violations

### Domain Roadmap

**MVP**

- KYC Review
- AML Review
- Travel Rule
- Sanctions Screening
- Audit Preparation

**Version 2**

- Jurisdiction Policy Packs
- Compliance Analytics

**Version 3**

- Automated Regulatory Drafting
- Continuous Control Monitoring

### Domain Relationships

**Depends on (MVP):** CORE, AUTH, AUTHZ, USER (contextual — authenticated actor identity; lifecycle owned by USER), ORG (contextual — tenant/organization scope; lifecycle owned by ORG), INVEST, RISK

**Assistive integration (not a mandatory MVP runtime dependency):** AI Platform (agent orchestration owned by AI Platform; COMP core workflows operable without AI)

**Supports (downstream / future scope):** REPORT, ADMIN

**Boundary references (not MVP dependencies):** ALERT (operational alert lifecycle), DASH (presentation), WALLET (wallet intelligence — Version 2 domain)

**External boundaries:** KYC Providers, Sanctions Screening Providers, Travel Rule Networks (provider-neutral; embedded in MVP capabilities)

**Risk boundary:** RISK owns risk scoring, rules, and risk-derived priority signals. COMP consumes `RiskCalculated` for contextual enrichment only. COMP owns sanctions screening workflow; COMP does not score risk and does not publish `RiskCalculated` or `HighRiskDetected`.

**Investigation boundary:** INVEST owns investigation cases and investigation workflow. COMP consumes `CaseClosed` and `CaseUpdated` for investigation context enrichment only. COMP does not create or manage investigation cases and does not consume `CaseCreated` or `EvidenceAttached` in MVP baseline.

**Alert boundary:** ALERT owns operational alert records, lifecycle, and operational alert priority. COMP does not create or manage alerts and does not publish alert events. `SanctionsHitDetected` does not imply MVP ALERT routing.

**User boundary:** USER owns user lifecycle. COMP consumes `UserUpdated` for user/KYC context enrichment only.

**Presentation boundary:** DASH owns workspace/dashboard presentation. COMP owns compliance data and workflows and does not own dashboard UI or workspace widgets.

**Reporting boundary:** REPORT owns reporting definitions and report presentation. COMP may supply compliance outcomes for downstream reporting without owning report definitions. REPORT is not an MVP COMP dependency.

**Wallet boundary:** WALLET owns wallet intelligence (Version 2). COMP does not consume wallet events in MVP baseline.

**AI boundary:** AI Platform owns agent orchestration and recommendation generation. COMP owns compliance workflow authority. AI assistance is assistive only and is not a mandatory MVP runtime dependency for COMP core workflows.

**Authorization and tenant scope:** AUTHZ owns authorization decisions. ORG provides tenant context. USER provides authenticated actor identity. COMP applies authorized outcomes within permitted scope without managing USER or ORG lifecycle.

**Evidence boundary (CP-BR-002):** COMP owns compliance evidence packages and audit preparation packages. Evidence management does not require COMP to consume `EvidenceAttached` in MVP baseline. Applicable investigation context is obtained through `CaseClosed` and `CaseUpdated` consumption and COMP-owned compliance records.

**Producer/downstream contract:** MVP COMP publishes `ComplianceReviewed`, `TravelRuleValidated`, `SanctionsHitDetected`, and `AuditPackagePrepared`. Downstream routing for all COMP publish events except potential future REPORT Version 2 consumption of `ComplianceReviewed` remains deferred or unspecified and is not an MVP cross-domain obligation until explicitly contracted.

**Platform scope:** COMP is a platform MVP domain. Jurisdiction Policy Packs, Compliance Analytics, Automated Regulatory Drafting, and Continuous Control Monitoring are Version 2/Version 3 scope and shall not become MVP Functional Requirements.

---

## Domain — SEC: Security Intelligence

### Domain Information

| Field | Value |
|-------|-------|
| Domain ID | `SEC` |
| Domain Name | Security Intelligence |
| Domain Owner | Security Engineering |
| Purpose | Monitor API, session, and device signals for operational security threats. |
| Business Value | Earlier detection of compromise, abuse, and unauthorized access patterns. |
| Related Business Requirements | SEC-BR-001, SEC-BR-002 |
| Related Business Objectives | BO-001, BO-002, BO-006 |
| Primary Users | Security Operations Engineers, Risk Analysts, Platform Administrators |
| Dependencies | CORE, AUTH, AUTHZ, USER (contextual — authenticated actor identity; lifecycle owned by USER), ORG (contextual — tenant/organization scope; lifecycle owned by ORG), ALERT, INVEST, AI (assistive integration only — agent orchestration owned by AI Platform; not a mandatory Version 2 runtime dependency) |
| Dependent Domains | REPORT, AI, OPS |
| Estimated Functional Requirements | ~10 (planning guideline; not a forced count) |
| Priority | Medium |
| Release | Version 2 |
| FR Prefix | `SEC-FR` |

### Domain Overview

The Security Intelligence domain monitors operational security signals such as API activity, sessions, and device behavior to detect potential account compromise, abuse, and unauthorized access. It supports security investigations using shared investigation capabilities (SEC-BR-002, FI-BR-001 context) without owning INVEST case lifecycle.

SEC is a **Version 2 domain only**. It has **no platform-MVP Functional Requirements, foundational hooks, or platform-MVP event contracts**. Foundational MVP authentication and session capabilities remain AUTH-owned; they do not create SEC MVP Functional Requirements.

ProductScope may refer to API Activity Monitoring, Authentication Anomaly Detection, Operational Security Monitoring, and Account Compromise Investigation. In this specification, **Version 2 SEC features are exactly four** (see Features below). Operational Security Monitoring is broader SEC framing, not a separate feature. Account Compromise Investigation is SEC investigation support under SEC-BR-002 using INVEST context, not a fifth standalone Version 2 feature. API Activity Monitoring maps to API Monitoring; Authentication Anomaly Detection maps to Session Monitoring and Threat Detection as applicable; Device Intelligence maps to Device Intelligence.

### Responsibilities

The domain is responsible for:

- Monitor API and authentication-related security signals
- Monitor session and security anomaly patterns
- Analyze and maintain device intelligence records derived from authorized inputs
- Detect operational security threats
- Support security incident investigation context (SEC-BR-002)

SEC does not authenticate users, establish sessions, create operational alerts, create investigation cases, score risk, own dashboard presentation, generate reports, or orchestrate AI agents. Version 2 SEC core workflows are operable without AI.

### Features (Version 2)

- API Monitoring
- Session Monitoring
- Device Intelligence
- Threat Detection

These four features are distinct Version 2 capabilities and shall not be merged or expanded into additional Version 2 features in this baseline.

### Future Features (Version 3 — not Version 2)

- Insider Threat Patterns
- Automated Containment Recommendations
- SIEM Bi-Directional Sync

These capabilities are explicitly deferred beyond Version 2 and shall not be authored as Version 2 Functional Requirements.

### Functional Requirements Planning Baseline

The intended Version 2 FRS inventory is approximately 10 requirements, determined from locked Version 2 capabilities rather than a mandatory count:

- 4 feature-covering requirements aligned to API Monitoring, Session Monitoring, Device Intelligence, and Threat Detection
- 1 cross-feature retrieval/discovery requirement
- Supporting requirements for event publication contract integrity, upstream event consumption integrity, audit outcomes, and authorization/access boundaries

Detailed Functional Requirement authoring is deferred to the FRS phase. This baseline is a planning guide only. Legacy BRS provisional references `FR-060 – FR-070` are superseded during FRS authoring; the BRS has not been migrated in this phase. No `SEC-FR-*` IDs are pre-authored here.

### Domain Data Ownership

This domain owns:

- Security Events
- Device Intelligence Records
- Threat Detections
- API Abuse Signals

SEC does not own authentication sessions, MFA enrollments, device registrations, user lifecycle records, organization lifecycle records, operational alerts, investigation cases, risk scores, AI recommendation records, or reporting definitions. Those remain owned by AUTH, USER, ORG, ALERT, INVEST, RISK, AI, and REPORT respectively.

### Domain Events

**Publishes (Version 2)**

- `ThreatDetected`
- `SuspiciousSessionDetected`
- `ApiAbuseDetected`

**Consumes (Version 2)**

- `UserLoggedIn` — AUTH-owned publication consumed by SEC for authentication/session security context enrichment only
- `SessionExpired` — AUTH-owned publication consumed by SEC for session security context enrichment only
- `AlertCreated` — ALERT-owned publication consumed by SEC for operational alert context enrichment only
- `CaseCreated` — INVEST-owned publication consumed by SEC for investigation context enrichment only

**Explicit exclusions (not SEC Version 2 baseline events)**

SEC does not publish or consume `DeviceSignalReceived` in Version 2 baseline. RISK's existing deferred `DeviceSignalReceived` integration remains unchanged and is not satisfied by inventing a SEC event contract in this baseline. Any future SEC↔RISK device-signal contract requires a later locked domain decision.

SEC does not consume `HighRiskDetected`, `RiskCalculated`, `CaseUpdated`, `CaseClosed`, `EvidenceAttached`, `UserUpdated`, `WalletProfileUpdated`, `AIRecommendationGenerated`, `TransactionReceived`, or any SEC self-publication events in Version 2 baseline.

**Downstream consumption (deferred / unspecified)**

- `ThreatDetected` — ALERT may consume when ALERT Version 2+ SEC integration is authored (frozen ALERT MVP baseline defers `ThreatDetected` consumption until SEC Version 2). ALERT is not an MVP dependency on SEC. No frozen MVP downstream consumer obligation exists.
- `SuspiciousSessionDetected` — downstream routing deferred/unspecified; not a Version 2 cross-domain obligation until explicitly contracted.
- `ApiAbuseDetected` — downstream routing deferred/unspecified; not a Version 2 cross-domain obligation until explicitly contracted.

SEC does not publish `UserLoggedIn`, `SessionExpired`, `AlertCreated`, or `CaseCreated`. SEC does not create operational alerts or investigation cases through event publication in this baseline.

### Non-Functional Characteristics

| Characteristic | Business Expectation |
|----------------|----------------------|
| Availability | 99.9% |
| Scalability | Horizontal with telemetry volume |
| Performance | Threat signal processing near-real-time for priority detectors |
| Security | Security telemetry highly restricted |
| Auditability | Full audit of detections and access |
| Observability | Metrics + Logs + Traces |

### External Integrations

External integrations remain provider-neutral capability boundaries embedded within Version 2 features. No separate external-provider ingestion event contract is defined for Version 2 SEC.

- SIEM Platforms
- Device Fingerprinting / Device Intelligence Providers
- Exchange API Gateway Logs

### Related AI Agents

No AI agents are owned or orchestrated by SEC.

N/A — AI Platform owns and orchestrates agents; SEC does not own or orchestrate AI agents.

AI Platform owns agent lifecycle and orchestration, including Security Investigation Assistant, Explanation Agent, and Summarization Agent capabilities where applicable. SEC may integrate assistive AI Platform outputs in authorized security workflows when applicable. AI assistance does not replace human decision authority for security disposition, enforcement, or investigation conclusions. Version 2 SEC core workflows remain operable without AI.

### Security Considerations

Security telemetry and threat findings shall be restricted to authorized security roles and protected as sensitive operational data. SEC shall respect organization scope through contextual ORG tenant boundaries without managing organization lifecycle. AUTHZ evaluates access; USER provides authenticated actor identity context; AUTH provides authentication and session event context.

### Success Criteria

- Security events are visible to authorized users
- Suspicious API/session/device patterns can be investigated
- Security investigation support can leverage INVEST context without SEC owning case lifecycle

### Domain KPIs

- Mean time to detect (MTTD)
- Mean time to respond (MTTR)
- API abuse detection rate
- Suspicious session investigation rate

### Domain Risks

- Telemetry blind spots
- High noise in detections
- Delayed escalation of true threats

### Domain Roadmap

**MVP (platform release)**

- None. SEC is a Version 2 domain with no platform-MVP Functional Requirements, event contracts, or foundational hooks. Foundational MVP authentication, session management, and device registration remain AUTH-owned capabilities and do not create SEC MVP Functional Requirements.

**Version 2**

- API Monitoring
- Session Monitoring
- Device Intelligence
- Threat Detection

**Version 3**

- Insider Threat Patterns
- Automated Containment Recommendations
- SIEM Bi-Directional Sync

### Domain Relationships

**Depends on (Version 2):** CORE, AUTH, AUTHZ, USER (contextual — authenticated actor identity; lifecycle owned by USER), ORG (contextual — tenant/organization scope; lifecycle owned by ORG), ALERT, INVEST

**Assistive integration (not a hard Version 2 runtime dependency):** AI Platform

**Boundary references (not Version 2 dependencies):** RISK (risk scoring — no locked SEC dependency solely for deferred `DeviceSignalReceived`), DASH (presentation), REPORT (report definitions), COMP (compliance workflows), WALLET (wallet intelligence)

**Supports:** REPORT, AI, OPS

**Authentication boundary:** AUTH owns authentication, credential verification, MFA, session establishment/expiry, device registration, and publication of `UserLoggedIn` and `SessionExpired`. SEC consumes those events for security context only and does not redefine AUTH ownership.

**Authorization boundary:** AUTHZ owns authorization policies and role decisions. SEC applies authorization outcomes and does not redefine AUTHZ.

**User boundary:** USER owns user lifecycle. SEC consumes upstream context where applicable and does not create, update, or deactivate users.

**Organization boundary:** ORG owns organization/tenant lifecycle. SEC applies tenant scope contextually and does not manage organizations.

**Alert boundary:** ALERT owns operational alert records, lifecycle, and operational alert priority. SEC consumes `AlertCreated` for context only. SEC does not create or manage alerts. `ThreatDetected` does not imply MVP or mandatory Version 2 ALERT routing unless explicitly contracted in a future ALERT revision.

**Investigation boundary:** INVEST owns investigation cases and investigation workflow. SEC consumes `CaseCreated` for investigation context enrichment only (SEC-BR-002). SEC does not create or manage investigation cases and does not consume `CaseUpdated`, `CaseClosed`, or `EvidenceAttached` in Version 2 baseline.

**Risk / device-signal boundary:** RISK owns risk scoring and rules. SEC does not score risk. `DeviceSignalReceived` is not an SEC Version 2 publish or consume event. RISK's deferred device-signal integration remains unchanged.

**Audit boundary:** CORE owns shared audit infrastructure. SEC records security detection and access outcomes using shared audit capabilities without redefining CORE ownership.

**Device boundary:** AUTH owns device registration and authentication mechanisms. SEC owns device intelligence records and derived security intelligence from authorized signals; SEC does not redefine AUTH device/session ownership.

**Presentation boundary:** DASH owns workspace/dashboard presentation. SEC owns security data and workflows and does not own dashboard UI.

**Report boundary:** REPORT owns reporting definitions and presentation. SEC does not own regulatory or operational report generation.

**AI boundary:** AI Platform owns AI agents and orchestration. SEC does not own Security Investigation Assistant or other agents.

External SIEM, device intelligence, and API gateway log integrations remain embedded within Version 2 feature workflows; no separate provider-ingest event contract is defined.

---

## Domain — AI: AI Platform

### Domain Information

| Field | Value |
|-------|-------|
| Domain ID | `AI` |
| Domain Name | AI Platform |
| Domain Owner | AI Platform Team |
| Purpose | Provide explainable AI assistance across investigation and risk workflows. |
| Business Value | Higher analyst productivity with transparent, human-supervised recommendations. |
| Related Business Requirements | AI-BR-001, AI-BR-002 |
| Related Business Objectives | BO-001, BO-004, BO-008 |
| Primary Users | Risk Analysts, Fraud Investigators, Compliance Officers, AI Governance |
| Dependencies | CORE, AUTH, AUTHZ, USER, RISK, INVEST, WALLET, COMP |
| Dependent Domains | DASH, REPORT, ALERT |
| Estimated Functional Requirements | 60 |
| Priority | High |
| Release | MVP |
| FR Prefix | `AI-FR` |

### Domain Overview

The AI Platform domain provides explainable AI-assisted capabilities that augment analysts across risk, investigation, compliance, and reporting workflows. It includes agent support, retrieval, prompt management, and evaluation while preserving human oversight.

### Responsibilities

The domain is responsible for:

- Provide AI investigation and risk assistance
- Retrieve contextual knowledge and evidence
- Generate operational summaries and reports
- Manage prompts and evaluate AI quality
- Preserve explainability and human review

### Features

- Investigation Agent
- Risk Agent
- Compliance Agent
- Report Agent
- Retrieval Agent
- Prompt Management
- Agent Evaluation

### Future Features

- Adaptive Agent Specialization
- Continuous Learning from Analyst Feedback
- Multi-Agent Orchestration Expansion

### Domain Data Ownership

This domain owns:

- Prompts
- Prompt Versions
- AI Evaluations
- AI Recommendation Records
- Agent Run Metadata

### Domain Events

**Publishes**

- `AIRecommendationGenerated`
- `AIEvaluationCompleted`
- `PromptUpdated`
- `AgentRunFailed`

**Consumes**

- `CaseUpdated`
- `RiskCalculated`
- `EvidenceAttached`
- `AlertCreated`

### Non-Functional Characteristics

| Characteristic | Business Expectation |
|----------------|----------------------|
| Availability | Degradable; core platform must continue if AI unavailable |
| Scalability | Horizontal by request/agent workload |
| Performance | Recommendation latency within analyst workflow tolerance |
| Security | Prompt/data minimization and privileged prompt management |
| Auditability | Full audit of recommendations, prompts, and evaluations |
| Observability | Metrics + Logs + Traces + AI quality signals |

### External Integrations

- Model Providers
- Vector Knowledge Sources
- Evaluation Datasets

### Related AI Agents

**Primary Agent:** Multi-Agent Orchestration

**Supporting Agents:**
- Investigation Agent
- Risk Agent
- Compliance Agent
- Report Agent
- Retrieval Agent

### Security Considerations

AI outputs shall be treated as recommendations, not enforcement actions. Prompt and model configuration shall be restricted to authorized AI and platform roles. Sensitive data in prompts/context shall be minimized and audited.

### Success Criteria

- Explainable AI recommendations are available in investigations
- Human review remains required for enforcement decisions
- Prompt and evaluation governance is supported

### Domain KPIs

- AI acceptance rate
- Hallucination / rejection rate
- Recommendation latency
- Evaluation pass rate

### Domain Risks

- Hallucinations
- Over-reliance on AI recommendations
- Prompt drift
- Model or provider outages

### Domain Roadmap

**MVP**

- Investigation Agent
- Risk Agent
- Retrieval Agent
- Prompt Management
- Explainability Controls

**Version 2**

- Compliance Agent
- Report Agent
- Agent Evaluation Framework

**Version 3**

- Adaptive Specialization
- Continuous Learning Loops
- Expanded Multi-Agent Orchestration

### Domain Relationships

**Depends on:** CORE, AUTH, AUTHZ, USER, RISK, INVEST, WALLET, COMP

**Supports:** DASH, REPORT, ALERT

---

## Domain — REPORT: Reporting & Analytics

### Domain Information

| Field | Value |
|-------|-------|
| Domain ID | `REPORT` |
| Domain Name | Reporting & Analytics |
| Domain Owner | Analytics & Insights Team |
| Purpose | Deliver operational and executive reports, KPIs, and exports. |
| Business Value | Improved organizational visibility and data-driven oversight. |
| Related Business Requirements | RA-BR-001 |
| Related Business Objectives | BO-006 |
| Primary Users | Executive Management, Risk Managers, Compliance Officers, Platform Administrators |
| Dependencies | CORE, AUTH, AUTHZ, USER, RISK, INVEST, COMP, AI, OPS |
| Dependent Domains | DASH, ADMIN |
| Estimated Functional Requirements | 20 |
| Priority | Medium |
| Release | Version 2 |
| FR Prefix | `REPORT-FR` |

### Domain Overview

The Reporting & Analytics domain provides operational reports, executive reports, KPI dashboards, and export capabilities that improve organizational visibility into investigations, risk, compliance, AI adoption, and platform performance.

### Responsibilities

The domain is responsible for:

- Produce operational and executive reports
- Expose KPI dashboards
- Support export of authorized reporting data
- Measure operational performance trends

### Features

- Operational Reports
- Executive Reports
- KPI Dashboards
- Export

### Future Features

- Scheduled Report Delivery
- Custom Report Builder
- Regulatory Report Packs

### Domain Data Ownership

This domain owns:

- Report Definitions
- Generated Reports
- KPI Snapshots
- Export Jobs

### Domain Events

**Publishes**

- `ReportGenerated`
- `ReportExported`
- `KpiSnapshotCreated`

**Consumes**

- `CaseClosed`
- `RiskCalculated`
- `ComplianceReviewed`
- `AIEvaluationCompleted`
- `PlatformUnavailable`

### Non-Functional Characteristics

| Characteristic | Business Expectation |
|----------------|----------------------|
| Availability | 99.5% |
| Scalability | Horizontal for report generation workloads |
| Performance | Standard operational reports within agreed generation windows |
| Security | Authorization-enforced exports |
| Auditability | Report access and export audited |
| Observability | Metrics + Logs + Traces |

### External Integrations

- BI Export Targets
- Notification Service

### Related AI Agents

**Primary Agent:** Report Generation Agent

**Supporting Agents:**
- Summarization Agent

### Security Considerations

Reports and exports shall enforce authorization and avoid unauthorized disclosure of investigation or personal data.

### Success Criteria

- Authorized users can access KPI dashboards
- Operational and executive reports are available
- Exports respect access controls

### Domain KPIs

- Report generation success rate
- Dashboard adoption
- Export turnaround time
- Executive report freshness

### Domain Risks

- Stale KPIs
- Unauthorized export leakage
- Inconsistent metric definitions across domains

### Domain Roadmap

**MVP**

- — (Version 2 domain; KPI hooks prepared by producing domains)

**Version 2**

- Operational Reports
- KPI Dashboards
- Export

**Version 3**

- Executive Report Packs
- Custom Report Builder
- Scheduled Delivery

### Domain Relationships

**Depends on:** CORE, AUTH, AUTHZ, USER, RISK, INVEST, COMP, AI, OPS

**Supports:** DASH, ADMIN

---

## Domain — ADMIN: Administration

### Domain Information

| Field | Value |
|-------|-------|
| Domain ID | `ADMIN` |
| Domain Name | Administration |
| Domain Owner | Platform Engineering |
| Purpose | Govern organizations, users, roles, settings, and integrations. |
| Business Value | Secure enterprise governance and controlled platform configuration. |
| Related Business Requirements | ADM-BR-001 |
| Related Business Objectives | BO-007, BO-009 |
| Primary Users | Platform Administrators, System Administrators |
| Dependencies | CORE, AUTH, AUTHZ, ORG, USER |
| Dependent Domains | OPS, REPORT |
| Estimated Functional Requirements | 20 |
| Priority | High |
| Release | MVP |
| FR Prefix | `ADMIN-FR` |

### Domain Overview

The Administration domain provides organizational governance capabilities including management of organizations, users, roles, settings, and integrations. It operationalizes administrative control over Sentinel AI configuration and access.

### Responsibilities

The domain is responsible for:

- Manage organizations and users
- Configure roles and settings
- Govern integrations
- Support administrative reporting and control

### Features

- Organizations
- Users
- Roles
- Settings
- Integrations

### Future Features

- Delegated Administration
- Configuration Change Approvals
- Admin Activity Insights

### Domain Data Ownership

This domain owns:

- Admin Settings
- Integration Configurations
- Administrative Action Records

### Domain Events

**Publishes**

- `AdminSettingUpdated`
- `IntegrationConfigured`
- `AdminActionPerformed`

**Consumes**

- `UserCreated`
- `OrganizationUpdated`
- `RoleAssigned`
- `ConfigurationUpdated`

### Non-Functional Characteristics

| Characteristic | Business Expectation |
|----------------|----------------------|
| Availability | 99.9% |
| Scalability | Horizontal |
| Performance | Admin operations within interactive targets |
| Security | Privileged access only |
| Auditability | Full audit of all admin actions |
| Observability | Metrics + Logs + Traces |

### External Integrations

- Identity Provider
- External Integration Connectors

### Related AI Agents

No AI agents are associated with this domain.

### Security Considerations

Administrative actions shall require privileged authorization and produce complete audit records.

### Success Criteria

- Administrators can manage users and roles
- Platform settings are configurable
- Integrations can be governed centrally

### Domain KPIs

- Admin action success rate
- Integration configuration lead time
- Privileged action audit coverage

### Domain Risks

- Misconfigured integrations
- Unauthorized privileged actions
- Configuration drift

### Domain Roadmap

**MVP**

- Organizations
- Users
- Roles
- Settings
- Integrations

**Version 2**

- Admin Activity Insights
- Integration Health Views

**Version 3**

- Delegated Administration
- Change Approval Workflows

### Domain Relationships

**Depends on:** CORE, AUTH, AUTHZ, ORG, USER

**Supports:** OPS, REPORT

---

## Domain — OPS: Platform Operations

### Domain Information

| Field | Value |
|-------|-------|
| Domain ID | `OPS` |
| Domain Name | Platform Operations |
| Domain Owner | Platform Operations / SRE |
| Purpose | Monitor platform health, metrics, logs, tracing, and backup status. |
| Business Value | Reliable enterprise operations and faster operational diagnosis. |
| Related Business Requirements | OPS-BR-001 |
| Related Business Objectives | BO-006, BO-009 |
| Primary Users | System Administrators, SRE, Platform Engineers |
| Dependencies | CORE, AUTH, AUTHZ, ADMIN |
| Dependent Domains | REPORT, DASH |
| Estimated Functional Requirements | 20 |
| Priority | Medium |
| Release | Version 2 |
| FR Prefix | `OPS-FR` |

### Domain Overview

The Platform Operations domain provides monitoring, metrics, logging, tracing, and backup-status visibility required to operate Sentinel AI reliably in an enterprise environment.

### Responsibilities

The domain is responsible for:

- Monitor platform health and metrics
- Provide logging and tracing visibility
- Surface backup and operational status
- Support diagnostics and operational alerting

### Features

- Monitoring
- Metrics
- Logging
- Tracing
- Backup Status

### Future Features

- SLO Dashboards
- Automated Runbook Triggers
- Capacity Forecasting

### Domain Data Ownership

This domain owns:

- Operational Metrics Catalog
- Health Check Definitions
- Operational Alert Rules
- Backup Status Records

### Domain Events

**Publishes**

- `PlatformHealthDegraded`
- `OperationalAlertRaised`
- `BackupStatusUpdated`

**Consumes**

- `PlatformStarted`
- `PlatformUnavailable`
- `AgentRunFailed`
- `IntegrationConfigured`

### Non-Functional Characteristics

| Characteristic | Business Expectation |
|----------------|----------------------|
| Availability | 99.9% for operations visibility plane |
| Scalability | Horizontal with telemetry volume |
| Performance | Operational dashboards and alerts within ops response targets |
| Security | Ops telemetry restricted to platform/security roles |
| Auditability | Access to sensitive ops data audited |
| Observability | Metrics + Logs + Traces (dogfooding) |

### External Integrations

- Observability Stack
- Alerting Systems
- Backup Systems

### Related AI Agents

No AI agents are associated with this domain.

### Security Considerations

Operational telemetry access shall be restricted to authorized platform and security roles. Logs shall avoid unnecessary sensitive payload exposure.

### Success Criteria

- Platform health is observable
- Metrics and traces support diagnostics
- Operational alerts can be generated for platform issues

### Domain KPIs

- Platform availability
- Alert noise ratio
- Mean time to detect platform incidents
- Backup success rate

### Domain Risks

- Observability gaps
- Alert noise
- Incomplete backup visibility

### Domain Roadmap

**MVP**

- — (Version 2 domain; CORE health hooks in MVP)

**Version 2**

- Monitoring
- Metrics
- Logging
- Tracing
- Backup Status

**Version 3**

- SLO Dashboards
- Automated Runbooks
- Capacity Forecasting

### Domain Relationships

**Depends on:** CORE, AUTH, AUTHZ, ADMIN

**Supports:** REPORT, DASH

---

## Cross-Domain Notes

### Investigation Consolidation

Investigation Management (`INVEST`) consolidates case management, evidence management, and fraud investigation into a single enterprise domain. Detailed Functional Requirements within `INVEST-FR-*` may still be grouped by feature area (cases, evidence, assignments), but they share one domain boundary. Collaboration (FI-BR-003), AI Summary, and Escalation Workflows are Version 2 scope within the same domain boundary.

### Investigation Management Boundaries

INVEST owns investigation cases and investigation workflow. ALERT owns operational alert lifecycle and publishes `AlertCreated`. RISK owns risk scoring and publishes `RiskCalculated`. DASH owns workspace presentation and consumes `CaseUpdated` (frozen DASH-FR-011). COMP owns sanctions/compliance workflows. AI Platform owns agent orchestration; AI assistance remains assistive.

MVP INVEST publishes `CaseCreated`, `CaseUpdated`, `CaseClosed`, `CaseAssigned`, and `EvidenceAttached`. MVP INVEST consumes `AlertCreated` and `RiskCalculated` only. `InvestigationEscalated`, `AIRecommendationGenerated` consumption, and `WalletProfileUpdated` consumption are Version 2/deferred. INVEST does not consume `HighRiskDetected`, publish alerts, score risk, or own dashboard presentation.

INVEST owns Escalation Records as data; Escalation Workflows are Version 2. Evidence Collection includes attached artifacts without a separate Attachments MVP capability. Notification delivery is not an MVP INVEST-owned capability.

### Operational Workspace Naming

`DASH` is retained as the stable Domain ID and FR prefix (`DASH-FR`) for continuity. The business capability name is **Operational Workspace** to reflect queues, widgets, navigation, and analyst workflows—not a single dashboard screen.

DASH is a consumer/presentation domain relative to ALERT, RISK, INVEST, and REPORT. MVP business traceability uses existing BRS requirements FI-BR-001 and RI-BR-002; RA-BR-001 applies to Version 2 reporting/analytics visibility capabilities.

### Alert Management Boundaries

ALERT owns alert records and operational alert lifecycle. RISK owns risk scoring, rules, and risk-derived priority signals. ALERT operationalizes alert priority and handling state for alert queues without duplicating RISK scoring behavior.

INVEST owns investigation cases and workflow. ALERT does not create or own investigation cases. ALERT may associate alert context with investigations. INVEST consumes `AlertCreated`. INVEST publishes `CaseUpdated` for downstream presentation refresh (frozen DASH-FR-011).

DASH consumes `AlertCreated` for work queue presentation refresh (downstream contract with frozen DASH-FR-011). AUTHZ owns authorization decisions. ORG provides contextual tenant scope. CORE owns shared platform primitives including deferred Version 2 notification capabilities.

MVP ALERT publishes `AlertCreated`, `AlertAssigned`, and `AlertClosed`. Version 2 adds `AlertEscalated` when Alert Escalation Policies apply. MVP ALERT consumes `RiskCalculated` and `HighRiskDetected` only. `ThreatDetected`, compliance-sourced alert inputs, and autonomous triage/enforcement remain deferred.

### Risk Intelligence Boundaries

RISK owns risk scoring, rules/configuration, explanations (embedded in MVP `RiskCalculated`), and risk-derived priority signals. ALERT owns operational alert records, lifecycle, and operational alert priority. DASH owns workspace/dashboard presentation and consumes `RiskCalculated` (frozen DASH-FR-011). INVEST owns investigation cases.

MVP RISK publishes `RiskCalculated` and `HighRiskDetected`. MVP RISK consumes external `TransactionReceived` only. Wallet-event consumption, `AlertCreated` consumption, `DeviceSignalReceived`, `RiskUpdated`, and separate `RiskExplanationGenerated` are deferred. COMP owns sanctions workflow. AI assistance is contextual and assistive; critical scoring is not mandatory-AI-dependent.

### Wallet Intelligence Boundaries

WALLET is a Version 2 domain only. It has no platform-MVP Functional Requirements, foundational hooks, or MVP event contracts.

CORE owns shared platform and audit primitives. AUTH authenticates actors. AUTHZ determines authorization. USER provides user identity context; WALLET does not own user lifecycle. ORG provides tenant/organizational context; WALLET does not own organization lifecycle. RISK owns risk scoring; WALLET does not score risk and does not publish `RiskCalculated` or `HighRiskDetected`. INVEST owns investigation case lifecycle; WALLET does not create or manage cases. ALERT owns alert lifecycle; WALLET does not create or manage alerts. DASH owns presentation and workspace concerns; WALLET does not own dashboard UI. COMP owns compliance workflows and records; WALLET does not own them. AI Platform owns AI agents and orchestration; WALLET does not own or orchestrate agents. REPORT owns reporting definitions and presentation.

WALLET owns wallet intelligence data: wallet profiles, address reputation records, wallet activity timelines, and wallet relationship graphs.

Version 2 WALLET publishes exactly `WalletProfileUpdated`, `AddressReputationChanged`, and `SuspiciousWalletDetected`. Version 2 WALLET consumes exactly external `TransactionReceived`, INVEST-owned `CaseCreated`, and RISK-owned `RiskCalculated`. WALLET does not publish `TransactionReceived`. WALLET does not consume `HighRiskDetected`, `AlertCreated`, `CaseUpdated`, `CaseClosed`, `EvidenceAttached`, or `AIRecommendationGenerated` in Version 2 baseline.

`TransactionReceived` is an external upstream integration input consumed by WALLET for enrichment. `WalletProfileUpdated`, `AddressReputationChanged`, and `SuspiciousWalletDetected` are WALLET-owned publications. `CaseCreated` is an INVEST-owned publication consumed by WALLET for context. `RiskCalculated` is a RISK-owned publication consumed by WALLET for context.

INVEST may consume `WalletProfileUpdated` when INVEST Version 2 wallet-context integration is authored (frozen INVEST baseline defers this until WALLET Version 2). No frozen downstream consumer is currently established for `AddressReputationChanged` or `SuspiciousWalletDetected`; downstream routing remains a future contract decision.

Version 2 WALLET features are exactly Wallet Profiles, Address Reputation, Transaction History, and Relationship Graph. Multi-Chain Expansion, Cluster Detection, and Cross-Exchange Correlation are Version 3 and shall not become Version 2 Functional Requirements.

Wallet Risk Scoring and RISK wallet-event consumption remain Version 2+ RISK scope aligned with WALLET; they are not WALLET requirements.

### Compliance & Travel Rule Boundaries

COMP is a platform MVP domain.

CORE owns shared platform and audit primitives. AUTH authenticates actors. AUTHZ determines authorization. USER provides user identity context; COMP does not own user lifecycle. ORG provides tenant/organizational context; COMP does not own organization lifecycle. RISK owns risk scoring and risk rules; COMP owns sanctions screening workflow and does not score risk. INVEST owns investigation case lifecycle; COMP does not create or manage cases. ALERT owns alert lifecycle and operational alert priority; COMP does not create or manage alerts. WALLET owns wallet intelligence (Version 2). DASH owns presentation and workspace concerns; COMP does not own dashboard UI. REPORT owns reporting definitions and presentation; COMP does not own regulatory report generation. AI Platform owns AI agents and orchestration; COMP does not own or orchestrate agents. MVP COMP core workflows are operable without AI.

COMP owns compliance workflows, compliance records, Travel Rule validations, sanctions screening workflow, compliance evidence packages, and audit preparation packages.

MVP COMP publishes exactly `ComplianceReviewed`, `TravelRuleValidated`, `SanctionsHitDetected`, and `AuditPackagePrepared`. MVP COMP consumes exactly INVEST-owned `CaseClosed` and `CaseUpdated`, RISK-owned `RiskCalculated`, and USER-owned `UserUpdated`. COMP does not consume `HighRiskDetected`, `AlertCreated`, `EvidenceAttached`, `CaseCreated`, `WalletProfileUpdated`, `AIRecommendationGenerated`, or `TransactionReceived` in MVP baseline.

`CaseClosed` and `CaseUpdated` are INVEST-owned publications consumed by COMP for context. `RiskCalculated` is a RISK-owned publication consumed by COMP for context. `UserUpdated` is a USER-owned publication consumed by COMP for context. CP-BR-002 evidence packages are assembled from COMP-owned compliance data plus applicable investigation/context information through this consumption model without MVP consumption of `EvidenceAttached`.

REPORT may consume `ComplianceReviewed` when REPORT Version 2 scope is authored; REPORT is not an MVP COMP dependency. No frozen downstream consumer is currently established for `TravelRuleValidated`, `SanctionsHitDetected`, or `AuditPackagePrepared`; downstream routing remains a future contract decision. `SanctionsHitDetected` does not imply MVP ALERT routing; compliance-sourced alert generation remains deferred.

MVP COMP features are exactly KYC Review, AML Review, Travel Rule, Sanctions Screening, and Audit Preparation. Jurisdiction Policy Packs and Compliance Analytics are Version 2. Automated Regulatory Drafting and Continuous Control Monitoring are Version 3 and shall not become MVP Functional Requirements.

External KYC, sanctions screening, and Travel Rule provider integrations remain provider-neutral capability boundaries embedded within MVP features; no separate external-ingest event contract is defined for MVP COMP.

### Security Intelligence Boundaries

SEC is a Version 2 domain only. It has no platform-MVP Functional Requirements, foundational hooks, or MVP event contracts.

CORE owns shared platform and audit primitives. AUTH authenticates actors, owns sessions, MFA, device registration, and publishes `UserLoggedIn` and `SessionExpired`. AUTHZ determines authorization. USER provides user identity context; SEC does not own user lifecycle. ORG provides tenant/organizational context; SEC does not own organization lifecycle. ALERT owns operational alert lifecycle; SEC consumes `AlertCreated` for context only and does not create or manage alerts. INVEST owns investigation case lifecycle; SEC consumes `CaseCreated` for investigation context enrichment only (SEC-BR-002) and does not create or manage cases. RISK owns risk scoring and rules; SEC does not score risk. DASH owns presentation and workspace concerns; SEC does not own dashboard UI. REPORT owns reporting definitions and presentation. COMP owns compliance workflows and records; SEC does not own them. WALLET owns wallet intelligence (Version 2). AI Platform owns AI agents and orchestration; SEC does not own or orchestrate agents. Version 2 SEC core workflows remain operable without AI.

SEC owns security event monitoring, API activity/security monitoring, session/security anomaly monitoring, device intelligence records and derived intelligence, threat detection workflow/results, API abuse signal handling, and security investigation support/context.

Version 2 SEC publishes exactly `ThreatDetected`, `SuspiciousSessionDetected`, and `ApiAbuseDetected`. Version 2 SEC consumes exactly AUTH-owned `UserLoggedIn` and `SessionExpired`, ALERT-owned `AlertCreated`, and INVEST-owned `CaseCreated`. SEC does not publish or consume `DeviceSignalReceived` in Version 2 baseline. SEC does not consume `HighRiskDetected`, `RiskCalculated`, `CaseUpdated`, `CaseClosed`, `EvidenceAttached`, `UserUpdated`, `WalletProfileUpdated`, `AIRecommendationGenerated`, or `TransactionReceived`.

`UserLoggedIn` and `SessionExpired` are AUTH-owned publications consumed by SEC for security context. `AlertCreated` is an ALERT-owned publication consumed by SEC for context. `CaseCreated` is an INVEST-owned publication consumed by SEC for context.

ALERT may consume `ThreatDetected` when ALERT Version 2+ SEC integration is authored; ALERT is not an MVP dependency on SEC. No frozen downstream consumer is currently established for `SuspiciousSessionDetected` or `ApiAbuseDetected`; downstream routing remains a future contract decision.

Version 2 SEC features are exactly API Monitoring, Session Monitoring, Device Intelligence, and Threat Detection. Insider Threat Patterns, Automated Containment Recommendations, and SIEM Bi-Directional Sync are Version 3 and shall not become Version 2 Functional Requirements.

AUTH owns device registration and authentication/session mechanisms. SEC owns device intelligence records and derived security intelligence from authorized upstream signals without redefining AUTH ownership. SEC records security outcomes using CORE shared audit capabilities without redefining CORE audit infrastructure ownership.

External SIEM, device intelligence, and API gateway log integrations remain provider-neutral capability boundaries embedded within Version 2 features; no separate external-ingest event contract is defined for Version 2 SEC.

RISK's existing deferred `DeviceSignalReceived` integration remains unchanged. `DeviceSignalReceived` is not an SEC Version 2 publish or consume event. Any future SEC↔RISK device-signal contract requires a later locked domain decision.

### AI Assistive Role

AI Platform capabilities augment domain workflows. AI recommendations do not replace human decision authority for enforcement actions.

### No Technology Prescriptions

Domains describe capabilities, not implementation stacks. Choices such as message brokers, databases, frameworks, and orchestration tools belong in Architecture Decision Records and architecture documents.

---

## Document Closing

This Functional Domain Specification establishes the capability map, ownership model, event boundaries, and data ownership for Sentinel AI.

Next step: author Functional Requirements domain-by-domain in the dependency order defined above, beginning with `CORE`.
