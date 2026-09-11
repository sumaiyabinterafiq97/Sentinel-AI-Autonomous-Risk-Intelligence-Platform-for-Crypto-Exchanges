# Domain Boundaries

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Domain Boundaries |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 2 |
| Owner | Architecture Team |
| Last Updated | 2026-09-03 |
| Authority | Architectural interpretation of FDS/FRS; does not modify frozen FRs |

---

## Purpose

Define architectural boundaries for all 16 Sentinel AI domains: ownership, inputs/outputs, data, events, AI relationship, and human decision boundaries.

Authoritative functional behavior remains in [FunctionalDomainSpecification.md](../02-requirements/FunctionalDomainSpecification.md) and [FunctionalRequirements.md](../02-requirements/FunctionalRequirements.md).

---

## Boundary Template

Each domain entry includes:

| Field | Description |
|-------|-------------|
| **Purpose** | Domain mission |
| **Owns** | Lifecycle and authoritative data |
| **Does not own** | Explicit exclusions |
| **Inputs** | Events, APIs, external feeds |
| **Outputs** | Events, APIs, published signals |
| **Data ownership** | Authoritative stores |
| **Dependencies** | Upstream domains |
| **AI relationship** | Assistive vs none |
| **Human decision boundary** | What requires human authority |

---

## CORE — Core Platform

| Field | Value |
|-------|-------|
| Purpose | Shared platform primitives: configuration, audit infrastructure, health, feature flags |
| Owns | Platform config, shared audit pipeline, health signals, cross-cutting identifiers |
| Does not own | Domain business lifecycles, risk scores, cases, alerts, compliance records |
| Inputs | Platform startup, admin configuration changes |
| Outputs | `PlatformStarted`, `PlatformUnavailable`, `ConfigurationUpdated`, audit events |
| Data ownership | Platform configuration, audit log index (shared) |
| Dependencies | None (foundation) |
| AI relationship | None |
| Human boundary | Platform operators govern configuration changes |

---

## AUTH — Authentication

| Field | Value |
|-------|-------|
| Purpose | Identity verification, session lifecycle, MFA, device registration |
| Owns | Sessions, authentication tokens, login events, device registration records |
| Does not own | Authorization policy (AUTHZ), user profile lifecycle (USER), security threat detection (SEC) |
| Inputs | Credentials, MFA challenges, IdP callbacks |
| Outputs | `UserLoggedIn`, `SessionExpired`, session validation APIs |
| Data ownership | Session store, auth event history |
| Dependencies | CORE |
| AI relationship | None |
| Human boundary | Users authenticate; admins configure auth policies via ADMIN |

**AUTH vs SEC:** AUTH owns authentication mechanisms and session events. SEC consumes auth events for security monitoring; SEC does not replace AUTH.

---

## AUTHZ — Authorization

| Field | Value |
|-------|-------|
| Purpose | Permission evaluation, RBAC policy, access decisions |
| Owns | Role definitions, permission mappings, authorization decisions |
| Does not own | User lifecycle (USER), org lifecycle (ORG), authentication (AUTH) |
| Inputs | Access requests with actor identity and resource context |
| Outputs | Allow/deny decisions; `RoleAssigned` events |
| Data ownership | Policy definitions, role assignments |
| Dependencies | CORE, AUTH, USER, ORG |
| AI relationship | AI tools subject to same AUTHZ checks |
| Human boundary | Privileged role assignment requires admin authority |

---

## USER — User Management

| Field | Value |
|-------|-------|
| Purpose | Platform user lifecycle and profile |
| Owns | User records, profile state, user lifecycle events |
| Does not own | Authentication sessions (AUTH), org membership lifecycle (ORG), authorization policy (AUTHZ) |
| Inputs | Admin/user management requests |
| Outputs | `UserCreated`, `UserUpdated`, `UserDeactivated` |
| Data ownership | User profiles |
| Dependencies | CORE, AUTH, AUTHZ, ORG |
| AI relationship | None |
| Human boundary | User creation/deactivation by authorized admins |

---

## ORG — Organization Management

| Field | Value |
|-------|-------|
| Purpose | Tenant/organization lifecycle and hierarchy |
| Owns | Organization records, membership associations (contextual) |
| Does not own | User lifecycle (USER), admin settings (ADMIN) |
| Inputs | Org management requests |
| Outputs | `OrganizationUpdated`, org context for scoping |
| Data ownership | Organization records |
| Dependencies | CORE, AUTH, AUTHZ |
| AI relationship | None |
| Human boundary | Org structural changes by platform admins |

---

## DASH — Operational Workspace

| Field | Value |
|-------|-------|
| Purpose | Presentation and navigation for operational workflows |
| Owns | Workspace layouts, saved views (V2), presentation state |
| Does not own | Alert lifecycle (ALERT), risk scores (RISK), cases (INVEST), reports (REPORT) |
| Inputs | Events from ALERT, RISK, INVEST, REPORT; read APIs from owning domains |
| Outputs | UI rendering; no authoritative business state mutations |
| Data ownership | Presentation preferences only |
| Dependencies | CORE, AUTH, AUTHZ, USER, ALERT, RISK, INVEST, REPORT (V2) |
| AI relationship | Displays AI assistive outputs; does not own AI lifecycle |
| Human boundary | Analyst actions delegate to owning domain APIs |

**Critical:** DASH is a **consumer/presentation** domain. It must not silently redefine ALERT priority or INVEST case state.

---

## ALERT — Alert Management

| Field | Value |
|-------|-------|
| Purpose | Operational alert lifecycle and queue management |
| Owns | Alert records, alert state, **operational alert queue priority** |
| Does not own | Risk scoring (RISK), investigation cases (INVEST), dashboard UI (DASH) |
| Inputs | `RiskCalculated`, `HighRiskDetected` (MVP); SEC signals (V2+) |
| Outputs | `AlertCreated`, `AlertAssigned`, `AlertClosed` |
| Data ownership | Alert records and disposition history |
| Dependencies | CORE, AUTH, AUTHZ, RISK |
| AI relationship | May consume AI context for display; AI does not own priority |
| Human boundary | Analysts disposition alerts; no autonomous AI closure of high-severity alerts |

**ALERT vs RISK:** RISK produces scores and explanations. ALERT operationalizes alert priority and handling state.

---

## RISK — Risk Intelligence

| Field | Value |
|-------|-------|
| Purpose | Transaction and behavioral risk scoring, rules, explanations |
| Owns | Risk scores, rule configurations, risk explanations (embedded in events) |
| Does not own | Alert lifecycle (ALERT), cases (INVEST), compliance (COMP) |
| Inputs | `TransactionReceived` (external), contextual USER/ORG |
| Outputs | `RiskCalculated`, `HighRiskDetected` |
| Data ownership | Risk assessments, rule definitions |
| Dependencies | CORE, AUTH, AUTHZ, USER, ORG |
| AI relationship | AI may assist explanations; **scoring critical path is deterministic/non-AI-mandatory** |
| Human boundary | Analysts interpret scores; enforcement is external |

---

## INVEST — Investigation Management

| Field | Value |
|-------|-------|
| Purpose | Investigation case lifecycle, evidence, timeline, assignment |
| Owns | Cases, evidence attachments, investigation workflow state |
| Does not own | Alerts (ALERT), risk scores (RISK), compliance workflows (COMP), SEC signals (SEC) |
| Inputs | `AlertCreated`, `RiskCalculated`; SEC/WALLET context (V2) |
| Outputs | `CaseCreated`, `CaseUpdated`, `CaseClosed`, `CaseAssigned`, `EvidenceAttached` |
| Data ownership | Case records, evidence metadata |
| Dependencies | CORE, AUTH, AUTHZ, USER, ORG, ALERT, RISK |
| AI relationship | AI summaries assist; **INVEST owns case closure authority** |
| Human boundary | Investigators resolve cases; AI does not autonomously close investigations |

**INVEST vs SEC:** SEC provides security context for account compromise support. INVEST owns investigation lifecycle.

---

## WALLET — Wallet Intelligence (Version 2)

| Field | Value |
|-------|-------|
| Purpose | Wallet profiling, address reputation, relationship graphs |
| Owns | Wallet profiles, reputation records, relationship graphs |
| Does not own | Risk scoring (RISK), cases (INVEST), alerts (ALERT) |
| Inputs | `TransactionReceived`, `CaseCreated`, `RiskCalculated` |
| Outputs | `WalletProfileUpdated`, `AddressReputationChanged`, `SuspiciousWalletDetected` |
| Release | Version 2 only |
| AI relationship | Assistive analysis only |
| Human boundary | Wallet intelligence informs; investigators decide |

---

## COMP — Compliance & Travel Rule (MVP)

| Field | Value |
|-------|-------|
| Purpose | KYC, AML, Travel Rule, sanctions, audit preparation |
| Owns | Compliance records, screening outcomes, audit packages |
| Does not own | Cases (INVEST), risk scores (RISK), reports (REPORT) |
| Inputs | `CaseClosed`, `CaseUpdated`, `RiskCalculated`, `UserUpdated` |
| Outputs | `ComplianceReviewed`, `TravelRuleValidated`, `SanctionsHitDetected`, `AuditPackagePrepared` |
| AI relationship | Assistive; **MVP workflows operable without AI** |
| Human boundary | Compliance officers approve/reject; AI does not approve KYC or clear sanctions |

**COMP vs AI:** AI may summarize evidence. COMP owns compliance disposition.

---

## SEC — Security Intelligence (Version 2)

| Field | Value |
|-------|-------|
| Purpose | API, session, device monitoring; threat detection |
| Owns | Security signals, device intelligence, threat detection results |
| Does not own | Authentication (AUTH), alerts (ALERT), cases (INVEST) |
| Inputs | `UserLoggedIn`, `SessionExpired`, `AlertCreated`, `CaseCreated` |
| Outputs | `ThreatDetected`, `SuspiciousSessionDetected`, `ApiAbuseDetected` |
| Release | Version 2 only |
| AI relationship | Assistive; V2 operable without AI |
| Human boundary | Security engineers triage; no autonomous containment (V3 deferred) |

**SEC vs AUTH:** SEC monitors and detects; AUTH authenticates.

---

## AI — AI Platform (MVP)

| Field | Value |
|-------|-------|
| Purpose | Assistive agents, retrieval, prompts, evaluation |
| Owns | Prompts, prompt versions, AI recommendation records, agent run metadata, evaluations |
| Does not own | Any business domain lifecycle (alerts, cases, compliance, risk scores) |
| Inputs | `CaseUpdated`, `RiskCalculated`, `EvidenceAttached`, `AlertCreated` |
| Outputs | `AIRecommendationGenerated`, `AIEvaluationCompleted`, `PromptUpdated`, `AgentRunFailed` |
| AI relationship | Self — orchestrates agents with tool authorization |
| Human boundary | Recommendations only; humans execute consequential actions in owning domains |

**AI vs every business domain:** AI never owns lifecycle state in ALERT, RISK, INVEST, COMP, SEC, WALLET.

---

## ADMIN — Administration (MVP)

| Field | Value |
|-------|-------|
| Purpose | Platform settings, integration governance, admin action orchestration |
| Owns | Admin settings, integration configurations, administrative action records |
| Does not own | User lifecycle (USER), org lifecycle (ORG), role policy definitions (AUTHZ) |
| Inputs | `UserCreated`, `OrganizationUpdated`, `RoleAssigned`, `ConfigurationUpdated` |
| Outputs | `AdminSettingUpdated`, `IntegrationConfigured`, `AdminActionPerformed` |
| AI relationship | None |
| Human boundary | Privileged admins only; all actions audited |

**ADMIN vs USER/ORG/AUTHZ:** ADMIN orchestrates administrative operations and settings. USER/ORG/AUTHZ remain lifecycle/policy owners.

---

## REPORT — Reporting & Analytics (Version 2)

| Field | Value |
|-------|-------|
| Purpose | Operational reports, KPI dashboards, exports |
| Owns | Report definitions, generated reports, KPI snapshots, export jobs |
| Does not own | Source domain data (RISK, INVEST, COMP, etc.) |
| Inputs | `CaseClosed`, `RiskCalculated`, `ComplianceReviewed`, `AIEvaluationCompleted`, `PlatformUnavailable` |
| Outputs | `ReportGenerated`, `ReportExported`, `KpiSnapshotCreated` |
| Release | Version 2 only |
| AI relationship | May use Report Generation Agent (V2) |
| Human boundary | Exports require authorization; no unauthorized disclosure |

**REPORT vs source domains:** REPORT aggregates and presents; domains remain source of truth.

---

## OPS — Platform Operations (Version 2)

| Field | Value |
|-------|-------|
| Purpose | Health monitoring, metrics, logs, tracing, backup visibility |
| Owns | Health definitions, ops alert rules, operational metrics catalog |
| Does not own | Business alerts (ALERT), business cases (INVEST) |
| Inputs | `PlatformStarted`, `PlatformUnavailable`, `AgentRunFailed`, `IntegrationConfigured` |
| Outputs | `PlatformHealthDegraded`, `OperationalAlertRaised`, `BackupStatusUpdated` |
| Release | Version 2 (CORE health hooks in MVP) |
| AI relationship | Monitors AI failures via `AgentRunFailed` |
| Human boundary | SRE/operators respond to platform incidents |

**OPS vs business domains:** OPS monitors platform health—not fraud alerts or compliance cases.

---

## High-Risk Boundary Summary

| Boundary | Owner A | Owner B | Rule |
|----------|---------|---------|------|
| Alert priority | ALERT | RISK | RISK provides context; ALERT owns queue priority |
| Case lifecycle | INVEST | SEC | SEC provides signals; INVEST owns cases |
| Compliance disposition | COMP | AI | AI assists; humans approve |
| Audit infrastructure | CORE | All domains | CORE provides pipeline; domains record outcomes |
| Admin user management | USER | ADMIN | USER owns lifecycle; ADMIN orchestrates admin UX/settings |
| Reporting data | Source domains | REPORT | REPORT reads aggregates; no lifecycle ownership |

---

## Related Documents

- [EventArchitecture.md](EventArchitecture.md)
- [SystemArchitecture.md](SystemArchitecture.md)
- [FunctionalDomainSpecification.md](../02-requirements/FunctionalDomainSpecification.md)
