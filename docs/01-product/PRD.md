# Product Requirements Document (PRD)

## Sentinel AI — Autonomous Risk Intelligence Platform for Crypto Exchanges

---

## 1. Document Control

| Field | Value |
|--------|-------|
| **Document** | Product Requirements Document (PRD) |
| **Project** | Sentinel AI |
| **Full Name** | Autonomous Risk Intelligence Platform for Crypto Exchanges |
| **Version** | 0.1 Draft |
| **Status** | Draft — Phase 5 PRD consolidation |
| **Date** | 2026-09-03 |
| **Owner** | Product & Engineering Team |
| **Authors** | Product & Engineering Team |
| **Audience** | Product, engineering, architecture, security, compliance, QA, leadership |
| **Authority** | Consolidates Phase 1–4 product artifacts; **subordinate to FDS/FRS for domain behavior** |

### Revision History

| Version | Date | Author | Summary |
|---------|------|--------|---------|
| 0.1 | 2026-09-03 | Product Team | Phase 5 PRD consolidation — 48 sections; traceability to FDS v1.4, FRS v1.9+, NFR v1.0 |

### Related Documents

| Document | Path | Relationship |
|----------|------|--------------|
| Vision | [Vision.md](Vision.md) | Strategic intent |
| Product Scope | [ProductScope.md](ProductScope.md) | Capability boundaries |
| Product Discovery | [ProductDiscovery.md](ProductDiscovery.md) | Phase 1 reconciliation |
| Personas | [Personas.md](Personas.md) | User definitions |
| Principles | [Principles.md](Principles.md) | Durable product constraints |
| Business Requirements | [BusinessRequirements.md](BusinessRequirements.md) | Business objectives |
| Functional Domain Specification | [FunctionalDomainSpecification.md](../02-requirements/FunctionalDomainSpecification.md) | Domain ownership (authoritative) |
| Functional Requirements | [FunctionalRequirements.md](../02-requirements/FunctionalRequirements.md) | Detailed FRs (authoritative) |
| Non-Functional Requirements | [NonFunctionalRequirements.md](../02-requirements/NonFunctionalRequirements.md) | Quality attributes |
| Domain Boundaries | [DomainBoundaries.md](../03-architecture/DomainBoundaries.md) | Architectural boundaries |
| System Architecture | [SystemArchitecture.md](../03-architecture/SystemArchitecture.md) | System design |
| Data Architecture | [DataArchitecture.md](../04-database/DataArchitecture.md) | Data ownership |
| Event Contracts | [EventContracts.md](../06-api/EventContracts.md) | Event schemas |
| API Inventory | [APIInventory.md](../06-api/APIInventory.md) | REST surface |
| Governance Decisions | [GovernanceDecisions.md](../00-project/GovernanceDecisions.md) | Recorded decisions |
| Project Roadmap | [ProjectRoadmap.md](../00-project/ProjectRoadmap.md) | Phase control |

### Phase 5 Consolidation Note

This PRD consolidates product intent from Vision, Product Scope, Product Discovery, Personas, Principles, and Phase 2–4 architecture/API/data artifacts into a single product-facing reference.

**It does not redefine frozen functional requirements** for domains CORE, AUTH, AUTHZ, USER, ORG, DASH, ALERT, RISK, INVEST, WALLET, COMP, or SEC. Where product language differs from FRS, **FRS governs implementation behavior**.

Pending FRS domains (AI, ADMIN, REPORT, OPS) are referenced at capability level only; detailed behavior awaits FRS authoring per [ProjectRoadmap.md](../00-project/ProjectRoadmap.md).

---

## 2. Executive Summary

Sentinel AI is an enterprise **risk intelligence platform** for cryptocurrency exchanges. It unifies transaction risk monitoring, operational alert handling, fraud investigation, compliance workflows, and assistive AI into a single evidence-based, human-governed operational environment.

The platform is designed for internal Risk, Compliance, Fraud, and Security Operations teams. It augments—not replaces—existing exchange infrastructure by providing contextual risk analysis, explainable intelligence, and structured investigation workflows while preserving human accountability for consequential decisions.

**Release strategy:**

| Release | Domains | Intent |
|---------|---------|--------|
| **MVP (Version 1)** | CORE, AUTH, AUTHZ, USER, ORG, DASH, ALERT, RISK, INVEST, COMP, AI, ADMIN | Usable exchange-modeled operational baseline |
| **Version 2** | WALLET, SEC, REPORT, OPS | Extended intelligence, security monitoring, reporting, platform operations |
| **Version 3 / Future** | Deferred SEC patterns, advanced integrations | Insider threat, SIEM sync, automated containment, advanced blockchain, extended AI autonomy |

**Critical ownership boundaries (frozen):**

- **RISK** scores and explains risk; publishes `RiskCalculated` and `HighRiskDetected`; **does not create alerts**
- **ALERT** owns alert lifecycle and **operational queue priority**
- **INVEST** owns investigation cases, evidence, and timelines
- **COMP** owns compliance workflow outcomes
- **AI** is **assistive only** — no lifecycle ownership of alerts, cases, or compliance decisions

**NFR headline targets (simulation/MVP):** risk evaluation P95 ≤ 2s; AI assist P95 ≤ 5s with 10s timeout; platform availability 99.5%.

Sentinel AI is modeled after internal exchange-grade operations. It is **not** a claim of production deployment at a named exchange, autonomous enforcement, or perfect fraud detection until validated in later phases.

---

## 3. Product Vision

**Vision statement:** To become the intelligent decision-support platform that enables cryptocurrency exchanges to detect, investigate, and respond to financial threats through explainable AI, behavioral intelligence, and evidence-driven risk analysis while maintaining human oversight over all critical enforcement actions.

Aligned with [Vision.md](Vision.md) Chapter 2.

### Vision Pillars

Every capability should support one or more pillars defined in Vision:

1. **Risk Intelligence** — Detect and score risk across transactions, accounts, and behavioral signals (RISK domain)
2. **AI Investigation** — Assist analysts with evidence retrieval, summarization, and investigation support (AI + INVEST)
3. **Compliance Automation** — Support KYC, Travel Rule, sanctions, and audit-ready processes (COMP domain)
4. **Security Intelligence** — Monitor API, authentication, and operational signals (SEC domain — V2)
5. **Explainability** — Transparent, evidence-backed, reviewable recommendations
6. **Human Oversight** — Authorized humans responsible for critical enforcement decisions

### Mission

Empower cryptocurrency exchanges with an intelligent, explainable, scalable risk intelligence platform that enhances fraud detection, accelerates investigations, strengthens compliance, and improves operational efficiency through AI-assisted decision support.

---

## 4. Product Background

Sentinel AI emerged from the need to consolidate fragmented operational tooling used by exchange risk, compliance, and security teams. Early product documents (Vision v0.5, Product Scope v1.0) preceded the mature requirements baseline (FDS v1.3+, FRS v1.9).

Phase 1 ([ProductDiscovery.md](ProductDiscovery.md)) reconciled product language with frozen domain requirements. Phase 2 established NFR baselines and architecture foundations. Phases 3–4 delivered API contracts, data architecture, and governance decisions including **GD-001** (REPORT excluded from MVP gate).

The platform follows a **contract-first, domain-owned** architecture: 16 functional domains with explicit data ownership, event publish/consume matrices, and human/AI responsibility boundaries documented in FDS and [DomainBoundaries.md](../03-architecture/DomainBoundaries.md).

Sentinel AI targets medium-to-large centralized exchanges and digital asset platforms requiring enterprise-grade operational intelligence. It integrates with existing exchange core, identity, KYC, and analytics systems rather than replacing them.

---

## 5. Industry Context

Cryptocurrency exchanges process millions of financial and operational events daily: deposits, withdrawals, trades, wallet transfers, API requests, authentication events, and compliance checks.

Industry pressures include:

| Pressure | Operational impact |
|----------|-------------------|
| Evolving fraud tactics | Synthetic identity, ATO, API key compromise, coordinated rings, cross-chain movement |
| Regulatory complexity | Multi-jurisdiction KYC/AML, Travel Rule, sanctions, audit obligations |
| Alert volume | Rule engines generate high volumes with limited contextual enrichment |
| Tool fragmentation | Risk, alerts, cases, compliance, and security data in disconnected systems |
| AI adoption risk | Ungoverned AI experiments lack auditability and human accountability |

Traditional rule-based systems remain effective for known patterns but struggle with evolving threats, relationship visibility, and investigator context assembly. Modern exchanges require platforms that combine deterministic rules, behavioral analytics, graph intelligence (V2+), and assistive AI within auditable workflows.

**Product context — not an implementation requirement:** Cross-exchange threat intelligence sharing and predictive risk forecasting are long-term vision items in [Vision.md](Vision.md) — not MVP or V2 commitments.

---

## 6. Problem Statement

Exchange operational teams face systemic challenges:

| Challenge | Description |
|-----------|-------------|
| Alert fatigue | High alert volumes without sufficient risk context or explainability |
| Investigation friction | Manual evidence assembly across disconnected systems |
| Opaque scoring | Risk conclusions without traceable rules, signals, or evidence |
| Compliance overhead | Audit packages manually reconstructed from multiple sources |
| Security isolation | Security telemetry disconnected from fraud and investigation workflows (pre-SEC) |
| Accountability gap | AI outputs not grounded in auditable evidence or human sign-off |

**Core problem:** Exchange risk, fraud, compliance, and security operations lack a **unified, evidence-based intelligence layer** with clear domain ownership, explainable outputs, and human governance for consequential decisions.

Sentinel AI addresses this as an internal enterprise platform—not as a claim of existing production deployment at a named exchange.

---

## 7. Users and Personas

Primary users are professional operational teams within cryptocurrency exchanges, not retail traders.

| Persona | Primary domains | MVP relevance | Detail |
|---------|----------------|---------------|--------|
| **Risk Analyst** | RISK, ALERT, DASH, INVEST | Primary | [Personas.md](Personas.md) § Persona 1 |
| **Compliance Officer** | COMP, INVEST (read), audit | Primary | [Personas.md](Personas.md) § Persona 2 |
| **Security Engineer** | SEC, INVEST support | Limited pre-SEC; primary V2 | [Personas.md](Personas.md) § Persona 3 |
| **Platform Administrator** | ADMIN, USER, ORG, AUTH, AUTHZ | Primary | [Personas.md](Personas.md) § Persona 4 |

### Stakeholder Groups

| Group | Interest |
|-------|----------|
| Executive (CTO, CISO, CCO) | Risk reduction, regulatory readiness, operational efficiency |
| Risk / Fraud Operations | Alert triage, investigation speed, detection quality |
| Compliance | Repeatable workflows, audit evidence |
| Security Operations (V2+) | API/auth/device threat visibility |
| Platform Engineering | Modular architecture, observability, contract clarity |
| AI Engineering | Assistive agents, evaluation, safe tool boundaries |

Additional personas (dedicated Fraud Investigator, Executive Reporting Consumer) may be defined in later phases. INVEST workflows are shared by Risk Analysts and investigators per [Personas.md](Personas.md) scope notes.

---

## 8. User Problems

| User | Problem | Sentinel response | FR/domain trace |
|------|---------|-------------------|-----------------|
| Risk Analyst | Alert fatigue; opaque scores | Explainable RISK + ALERT queue with context | RISK-FR-003, ALERT-FR-001–012 |
| Risk Analyst | Manual context assembly | INVEST handoff with attached evidence | INVEST-FR-003, INVEST-FR-005 |
| Compliance Officer | Incomplete audit packages | COMP workflows + audit preparation | COMP-FR-001–010 |
| Compliance Officer | Unclear AI accountability | Human sign-off; grounded AI assist only | NFR-AI-002; Principles §3, §7 |
| Security Engineer | Isolated security telemetry | SEC domain (V2) linked to INVEST | SEC-FR-001–009 (V2) |
| Platform Admin | Role sprawl; weak audit | AUTHZ least privilege + admin audit | AUTHZ-FR-001–015, CORE-FR-012–014 |

---

## 9. Product Goals

Product goals align with Vision strategic goals and FDS domain outcomes:

| # | Goal | Primary domains | Release |
|---|------|-----------------|---------|
| PG-01 | Accelerate risk investigations through unified context | RISK, ALERT, INVEST, DASH, AI | MVP |
| PG-02 | Improve fraud detection quality via hybrid intelligence | RISK | MVP; WALLET V2 |
| PG-03 | Strengthen compliance operations with auditable workflows | COMP | MVP |
| PG-04 | Secure exchange infrastructure through operational monitoring | SEC | V2 |
| PG-05 | Increase analyst productivity via assistive AI | AI, DASH, INVEST | MVP |
| PG-06 | Build explainable, evidence-backed intelligence outputs | RISK, AI, COMP | MVP |
| PG-07 | Enable modular adoption and clear domain ownership | All | All |
| PG-08 | Maintain platform operability when AI is degraded | CORE, all MVP domains | MVP |

---

## 10. Business Objectives

Business objectives use BO-xxx IDs from [BusinessRequirements.md](BusinessRequirements.md) and [Vision.md](Vision.md). All targets are **proposed** — not achieved results.

| ID | Objective | Target (proposed) | Supporting capabilities |
|----|-----------|-------------------|------------------------|
| BO-001 | Reduce investigation time | Material reduction via unified workspace | INVEST, DASH, AI assist |
| BO-002 | Improve fraud detection effectiveness | Better detection; reduced false-positive burden | RISK, ALERT explainability |
| BO-003 | Strengthen regulatory readiness | Consistent, auditable compliance workflows | COMP |
| BO-004 | Improve operational decision quality | Explainable recommendations + evidence | RISK, AI |
| BO-005 | Increase cross-functional collaboration | Centralized platform for Risk, Compliance, Security | DASH, INVEST |
| BO-006 | Improve operational visibility | Scalable visibility as volumes grow | DASH; REPORT V2 |

Traceability to business requirements (BRS): RI-BR-001/002/003 (Risk), FI-BR-001/002/003 (Investigation), CP-BR-001/002 (Compliance), SEC-BR-001/002 (Security V2), AI-BR-001/002 (AI), ADM-BR-001 (Admin), OPS-BR-001 (Operations), RA-BR-001 (Reporting V2), WI-BR-001 (Wallet V2).

---

## 11. Technical Objectives

Technical objectives align with Vision engineering goals and NFR baseline:

| ID | Objective | Reference |
|----|-----------|-----------|
| TO-01 | Modular, independently deployable domain services | FDS domain catalog; NFR-MAINT-001 |
| TO-02 | Deterministic core with AI augmentation on non-critical paths | ADR-003; NFR-PERF-001, NFR-PERF-006 |
| TO-03 | Explainability as system requirement — evidence, confidence, sources | RISK-FR explanations; AI architecture |
| TO-04 | Security across every layer — auth, authz, encryption, audit | NFR-SEC-001–010 |
| TO-05 | Observability by default — metrics, logs, traces, health | NFR-OBS-001–005 |
| TO-06 | Contract-first integration — events and APIs before implementation | EventContracts, OpenAPI |
| TO-07 | Graceful degradation — AI, cache, broker partial failures | NFR-RES-001–004 |
| TO-08 | Vendor-neutral product commitments — no mandatory Kafka/AWS/LLM | Principles §18; Project Roadmap |
| TO-09 | Multi-tenant data isolation by organization | DataArchitecture §5 |
| TO-10 | Requirement-to-test traceability | NFR-TEST-001 |

---

## 12. Product Principles

Eighteen durable principles from [Principles.md](Principles.md) govern all releases:

| # | Principle | Key implication |
|---|-----------|-----------------|
| 1 | Production-first design | Audit-ready, operable under load |
| 2 | Security by design | Foundational, not optional |
| 3 | Human oversight for consequential decisions | AI assists; humans/ rules decide |
| 4 | Explainability by default | Traceable conclusions |
| 5 | Evidence-based decisions | Records and provenance required |
| 6 | Deterministic enforcement where required | Rules and authZ, not LLM |
| 7 | AI as assistive capability | Domains retain lifecycle ownership |
| 8 | Graceful AI degradation | Mandatory paths work without AI |
| 9 | Modular architecture & clear domain ownership | One owner per capability |
| 10 | Contract-first integration | Events/APIs are boundaries |
| 11 | Observability by design | Telemetry planned with features |
| 12 | Auditability | Sensitive actions recorded |
| 13 | Least privilege | Role-aware, domain-scoped access |
| 14 | Failure tolerance & operational continuity | Predictable degradation |
| 15 | Data minimization & purpose limitation | FDS governs domain data |
| 16 | Explicit scope control | MVP/V2/V3 separated |
| 17 | Realistic product claims | No unvalidated production claims |
| 18 | Provider neutrality at product level | No vendor lock-in in product docs |

Full text: [Principles.md](Principles.md).

---

## 13. Product Differentiation

### Legitimate differentiation (supported by delivered/planned FRs)

| Differentiator | Evidence |
|----------------|----------|
| Unified risk intelligence workspace | RISK + ALERT + INVEST + DASH with shared context |
| Evidence-based investigation | INVEST case lifecycle (INVEST-FR-001–010) |
| Explainable risk scoring | RISK explanations embedded in `RiskCalculated` |
| Clear domain ownership | FDS interaction matrix; no duplicated lifecycles |
| AI-assisted investigation | Assistive agents; evaluation framework (AI FRS pending) |
| Cross-domain event intelligence | RISK → ALERT → INVEST → COMP pipeline |
| Compliance audit readiness | COMP audit preparation (COMP-FR-010) |
| Contract-first enterprise architecture | Phase 3/4 API, event, data artifacts |

### Explicitly NOT claimed

- Perfect or autonomous fraud detection
- Autonomous account blocking or fund freezing
- Guaranteed zero false positives
- Real-time global blockchain intelligence beyond WALLET V2 scope
- Production Binance or named exchange integration
- AI-owned enforcement or compliance approval

Classification per [ProductDiscovery.md](ProductDiscovery.md) §9.3:

| Class | Examples |
|-------|----------|
| Simulated / internal platform | MVP exchange-modeled workflows, internal event pipelines |
| Conceptual future | SIEM bi-directional sync, insider-threat patterns, automated containment |
| External integration (deferred) | Production chain analytics, third-party KYC providers |

---

## 14. Scope

Sentinel AI scope spans 16 functional domains organized in layers per [ProductDiscovery.md](ProductDiscovery.md) §10:

```text
                    ┌─────────────────┐
                    │   AI Platform   │  (assistive)
                    └────────┬────────┘
                             │ supports
    ┌──────────┐    ┌────────▼────────┐    ┌──────────┐
    │   RISK   │───►│     ALERT       │───►│  DASH    │
    └────┬─────┘    └────────┬────────┘    └────┬─────┘
         │                   │                   │
         └─────────┬─────────┴─────────┬─────────┘
                   ▼                   ▼
              ┌─────────┐        ┌─────────┐
              │ INVEST  │◄───────│  COMP   │
              └────┬────┘        └─────────┘
                   │
         V2: ┌──────┴──────┐
             │ WALLET │ SEC │
             └─────────────┘
```

| Layer | Domains | Role |
|-------|---------|------|
| Foundation | CORE, AUTH, AUTHZ, USER, ORG | Identity, tenant, audit, config |
| Intelligence | RISK; WALLET (V2); SEC (V2) | Scoring, monitoring, signals |
| Operations | ALERT, INVEST, COMP | Alert lifecycle, cases, compliance |
| Experience | DASH | Workspace presentation (not lifecycle owner) |
| Platform | AI, ADMIN; OPS (V2); REPORT (V2) | Assistive AI, admin, ops, reporting |

Detail: [ProductScope.md](ProductScope.md).

---

## 15. MVP Definition

**Version 1 (MVP)** = first **usable** release for exchange-modeled risk, alert, investigation, compliance, and administration workflows.

### MVP domains (FDS authority)

| Domain | MVP deliverable focus | FRS status |
|--------|----------------------|------------|
| CORE | Platform services, audit, health, configuration, maintenance mode | Frozen — CORE-FR-001–026 |
| AUTH | Authentication, sessions, MFA, password reset | Frozen — AUTH-FR-001–020 |
| AUTHZ | RBAC, permissions, authorization decisions | Frozen — AUTHZ-FR-001–015 |
| USER | User lifecycle, profiles | Frozen — USER-FR-001–012 |
| ORG | Organization/tenant lifecycle | Frozen — ORG-FR-001–011 |
| RISK | Transaction/behavioral risk, explanations, signals | Frozen — RISK-FR-001–013 |
| ALERT | Alert creation, lifecycle, **queue priority** | Frozen — ALERT-FR-001–012 |
| INVEST | Cases, evidence, timeline, assignment | Frozen — INVEST-FR-001–010 |
| DASH | Operational workspace and queues | Frozen — DASH-FR-001–013 |
| COMP | KYC, AML support, Travel Rule, sanctions, audit prep | Frozen — COMP-FR-001–010 |
| AI | Assistive agents (FRS pending; assistive-only posture) | **Pending** — AI-FR-* |
| ADMIN | Administration (FRS pending) | **Pending** — ADMIN-FR-* |

### MVP user outcomes

- Analysts triage alerts with risk context in unified DASH workspace
- Investigators manage cases with evidence and complete audit trail
- Compliance officers execute MVP COMP workflows with human approval
- Administrators provision users and roles with least privilege
- Platform operates when AI is degraded for all mandatory paths

### Explicitly excluded from MVP

WALLET, SEC, REPORT, OPS; insider-threat patterns; SIEM sync; automated containment; production external exchange/blockchain integrations.

Per **GD-001**: REPORT is **not** an MVP Application Development Gate dependency — human sign-off pending ([GovernanceDecisions.md](../00-project/GovernanceDecisions.md)).

---

## 16. Version 2 Scope

| Domain / capability | V2 scope summary | FRS status |
|--------------------|------------------|------------|
| **WALLET** | Wallet profiling, address reputation, suspicious wallet detection | Frozen — WALLET-FR-001–010 |
| **SEC** | API Monitoring, Authentication Monitoring, Device Monitoring, Threat Detection (4 features) | Frozen — SEC-FR-001–009 |
| **REPORT** | Reporting & analytics, KPI snapshots, executive reporting | Pending — REPORT-FR-* |
| **OPS** | Platform health, diagnostics, operational alerting | Pending — OPS-FR-* |
| **INVEST** | FI-BR-003 and extended features per FDS | Partial V2 in FDS roadmap |
| **DASH** | RA-BR-001 reporting views | DASH V2 items in FDS |
| **ORG / USER** | Hierarchy, activity history | FDS V2 items |

**Terminology:** Account Compromise Investigation is **support context** across SEC + INVEST — not a fifth SEC feature ([ProductScope.md](ProductScope.md) §4.4).

---

## 17. Version 3 / Future Scope

Per FDS deferred scope — **Future/deferred** unless explicitly governed via change control:

| Capability | Domain | Notes |
|------------|--------|-------|
| Insider-threat pattern detection | SEC | Deferred in FDS SEC domain |
| SIEM bi-directional synchronization | SEC / integration | Conceptual future |
| Automated containment actions | SEC / exchange integration | No autonomous enforcement in Sentinel |
| Advanced blockchain intelligence | WALLET | Beyond V2 baseline |
| Production-grade external exchange data feeds | Integration | External integration deferred |
| Extended AI autonomy | AI | Only if future FRs explicitly authorize |
| Cross-exchange threat intelligence | Product vision | [Vision.md](Vision.md) long-term |
| Predictive risk intelligence | RISK / AI | Conceptual future |

**Rule:** V3 items must not appear as MVP or V2 commitments in product, UI, or implementation without change control.

---

## 18. Explicit Out of Scope

Sentinel AI intentionally excludes:

| Exclusion | Rationale |
|-----------|-----------|
| Cryptocurrency trading, order matching | Not a trading platform |
| Wallet custody, blockchain node operation | Not a custody system |
| Asset/portfolio management, investment recommendations | Out of domain |
| Autonomous financial decisions or automated account enforcement | Human oversight principle |
| Exchange core ledger ownership | Integration consumer only |
| External regulatory filing execution | Human/process outside platform |
| Production API key revocation at exchange | Enforcement in exchange systems |
| SIEM as system of record (V3) | Deferred |
| Identity provider replacement | AUTH integrates; does not replace IdP |
| Consumer-facing trading applications | Enterprise internal platform |

Non-goals from [Vision.md](Vision.md): does not replace compliance officers, SOC analysts, KYC providers, or blockchain analytics vendors.

---

## 19. User Journeys

### Journey 1 — Risk Analyst: Alert triage (MVP)

| Step | Actor | System behavior | Domain |
|------|-------|-----------------|--------|
| 1 | Exchange ingest | Transaction received; RISK evaluates | RISK-FR-003 |
| 2 | RISK | Publishes `RiskCalculated`; may publish `HighRiskDetected` | RISK-FR-002, RISK-FR-013 |
| 3 | ALERT | Creates/updates alert; assigns **queue priority** | ALERT-FR-001 |
| 4 | Risk Analyst | Opens DASH alert queue (ALERT priority order) | DASH-FR-001 |
| 5 | Risk Analyst | Reviews RISK explanation and context | RISK-FR-003 |
| 6 | Risk Analyst | Disposition: close, monitor, or escalate | ALERT-FR-004–008 |
| 7 | System | Action audited; AI may summarize (assistive) | CORE-FR-012; AI assist |
| 8 | Risk Analyst | Escalates → INVEST case created/linked | INVEST-FR-001 |

### Journey 2 — Fraud Investigator: Case management (MVP)

| Step | Actor | System behavior | Domain |
|------|-------|-----------------|--------|
| 1 | Investigator | Case created from alert or manual initiation | INVEST-FR-001 |
| 2 | Investigator | Evidence attached from RISK, ALERT sources | INVEST-FR-003, INVEST-FR-005 |
| 3 | Investigator | Timeline reconstructed; notes added | INVEST-FR-004, INVEST-FR-006 |
| 4 | Investigator | Assignment managed | INVEST-FR-002 |
| 5 | Investigator | Case closed with human approval | INVEST-FR-001 |
| 6 | System | Publishes `CaseClosed`; audit trail complete | INVEST-FR-010 |

### Journey 3 — Compliance Officer: Sanctions review (MVP)

| Step | Actor | System behavior | Domain |
|------|-------|-----------------|--------|
| 1 | System | COMP workflow triggered (sanctions screening) | COMP-FR-004 |
| 2 | Compliance Officer | Reviews match details and evidence | COMP-FR-005 |
| 3 | Compliance Officer | Human disposition recorded | COMP-FR-006 |
| 4 | System | Publishes `ComplianceReviewed`; audit updated | COMP-FR-009 |
| 5 | Compliance Officer | Escalates to INVEST if fraud overlap | INVEST-FR-001 (link) |

### Journey 4 — Platform Administrator: User provisioning (MVP)

| Step | Actor | System behavior | Domain |
|------|-------|-----------------|--------|
| 1 | Admin | Creates user, assigns org membership | USER-FR-001, ORG-FR-003 |
| 2 | Admin | Assigns roles via AUTHZ | AUTHZ-FR-003 |
| 3 | User | Authenticates with MFA | AUTH-FR-001, AUTH-FR-007 |
| 4 | System | Audit records admin and auth actions | CORE-FR-012, AUTH-FR-015 |

### Journey 5 — Security Engineer: Threat triage (V2)

| Step | Actor | System behavior | Domain |
|------|-------|-----------------|--------|
| 1 | SEC | Detects API abuse; publishes `ApiAbuseDetected` | SEC-FR-003 |
| 2 | Security Engineer | Triages in DASH with SEC context | DASH + SEC |
| 3 | Security Engineer | Escalates → INVEST case with SEC evidence | INVEST-FR-003 |
| 4 | Security Engineer | Human disposition; no autonomous enforcement | Principles §3 |

---

## 20. Major Workflows

### WF-01 — Risk signal to alert pipeline (MVP)

```text
TransactionReceived (external ingest)
    → RISK evaluate rules (RISK-FR-002)
    → RISK calculate score (RISK-FR-003)
    → publish RiskCalculated
    → ALERT consume → create/update alert (ALERT-FR-001)
    → ALERT set queue priority (ALERT-owned)
    → DASH present queue
```

**Boundary:** RISK provides `prioritySignal` in `HighRiskDetected` as **context only** — ALERT owns operational priority.

### WF-02 — Alert to investigation escalation (MVP)

```text
AlertCreated
    → Analyst reviews in DASH
    → Escalation action
    → INVEST CaseCreated (INVEST-FR-001)
    → EvidenceAttached from RISK/ALERT (INVEST-FR-005)
    → CaseUpdated / CaseClosed events
```

### WF-03 — Compliance review workflow (MVP)

```text
Trigger (KYC / sanctions / Travel Rule)
    → COMP workflow steps (COMP-FR-001–008)
    → Human review and disposition
    → ComplianceReviewed event
    → Audit package updated (COMP-FR-010)
```

### WF-04 — AI assistive explanation (MVP)

```text
Analyst requests explanation (non-blocking)
    → AI retrieves evidence via authorized tools
    → AIRecommendationGenerated event
    → Analyst reviews; no automatic state change
    → Domain retains lifecycle ownership
```

**Constraint:** AI path excluded from risk critical path (NFR-PERF-006, ADR-003).

### WF-05 — Security threat detection (V2)

```text
UserLoggedIn / SessionExpired (AUTH)
    → SEC consume → threat analysis (SEC-FR-001–004)
    → ThreatDetected / ApiAbuseDetected published
    → DASH + optional INVEST linkage
```

### WF-06 — Wallet intelligence (V2)

```text
TransactionReceived + CaseCreated
    → WALLET profile update (WALLET-FR-001)
    → SuspiciousWalletDetected (WALLET-FR-008)
    → INVEST/RISK consume as context
```

---

## 21. Functional Capability Overview

High-level capability map by domain. Detailed behavior: [FunctionalRequirements.md](../02-requirements/FunctionalRequirements.md).

### Foundation domains (MVP)

| Domain | Core capabilities | FR range |
|--------|---------------------|----------|
| CORE | Lifecycle, health, config, feature flags, audit context, maintenance mode | CORE-FR-001–026 |
| AUTH | Login, logout, MFA, sessions, password reset, device registration | AUTH-FR-001–020 |
| AUTHZ | RBAC, permissions, policy evaluation, role assignment | AUTHZ-FR-001–015 |
| USER | User CRUD, profile, lifecycle events | USER-FR-001–012 |
| ORG | Organization CRUD, tenant context, deactivation | ORG-FR-001–011 |

### Intelligence domains

| Domain | Core capabilities | FR range | Release |
|--------|---------------------|----------|---------|
| RISK | Rules, scoring, explanations, high-risk signals | RISK-FR-001–013 | MVP |
| WALLET | Profiling, reputation, suspicious wallet detection | WALLET-FR-001–010 | V2 |
| SEC | API/auth/device monitoring, threat detection | SEC-FR-001–009 | V2 |

### Operations domains (MVP)

| Domain | Core capabilities | FR range |
|--------|---------------------|----------|
| ALERT | Alert CRUD, assignment, closure, **priority** | ALERT-FR-001–012 |
| INVEST | Case lifecycle, evidence, timeline, assignment | INVEST-FR-001–010 |
| COMP | KYC, AML, Travel Rule, sanctions, audit prep | COMP-FR-001–010 |

### Experience domain (MVP)

| Domain | Core capabilities | FR range |
|--------|---------------------|----------|
| DASH | Work queues, workspace views, widget interaction | DASH-FR-001–013 |

**Note:** DASH is presentation-only — does not own alert, case, or compliance lifecycles.

### Platform domains

| Domain | Core capabilities | Release | FRS status |
|--------|---------------------|---------|------------|
| AI | Assistive agents, retrieval, evaluation, prompts | MVP | Pending |
| ADMIN | Admin settings, integration config | MVP | Pending |
| REPORT | Dashboards, KPI snapshots, executive reports | V2 | Pending |
| OPS | Health monitoring, ops alerting, diagnostics | V2 | Pending |

---

## 22. Domain Ownership

Authoritative ownership per FDS v1.4 and [DomainBoundaries.md](../03-architecture/DomainBoundaries.md).

| Domain | Owns (lifecycle + data) | Does NOT own |
|--------|-------------------------|--------------|
| CORE | Platform config, audit infrastructure, health, feature flags | Business lifecycles |
| AUTH | Sessions, MFA, authentication events | Authorization (AUTHZ), user profiles (USER) |
| AUTHZ | Roles, permissions, access decisions | User/org lifecycle |
| USER | User records, profile state | Sessions (AUTH), org membership (ORG) |
| ORG | Organizations, tenant context | Authorization policy (AUTHZ) |
| RISK | Risk scores, rules, explanations | **Alerts (ALERT)**, cases (INVEST) |
| ALERT | Alerts, assignments, **operational priority** | Risk scoring (RISK), cases (INVEST) |
| INVEST | Cases, evidence, timelines, notes | Alerts (ALERT), compliance outcomes (COMP) |
| COMP | Compliance records, screening results, audit packages | Investigation lifecycle (INVEST) |
| DASH | Work queue presentations, view state | All business lifecycles |
| WALLET (V2) | Wallet profiles, reputation, graphs | Cases (INVEST) |
| SEC (V2) | Security signals, threat detections | Auth mechanisms (AUTH), cases (INVEST) |
| AI | Prompts, recommendations, evaluations | Alert/case/compliance/enforcement lifecycles |
| REPORT (V2) | Report definitions, KPI snapshots | Source domain data |
| ADMIN | Admin settings, integration configs | Domain business rules |
| OPS (V2) | Health definitions, ops alert rules | Domain business state |

### Critical boundary rules (frozen)

1. **RISK does not create alerts** — publishes events consumed by ALERT
2. **ALERT owns alert priority** — RISK `prioritySignal` is context only
3. **INVEST owns cases** — all investigation lifecycle
4. **COMP owns compliance outcomes** — human-approved dispositions
5. **AI is assistive** — no silent lifecycle ownership transfer

---

## 23. Functional Requirement Traceability

### FR inventory by domain (frozen chapters)

| Domain | FR prefix | Count (delivered) | Release | Status |
|--------|-----------|-------------------|---------|--------|
| CORE | CORE-FR | 26 | MVP | Frozen |
| AUTH | AUTH-FR | 20 | MVP | Frozen |
| AUTHZ | AUTHZ-FR | 15 | MVP | Frozen |
| USER | USER-FR | 12 | MVP | Frozen |
| ORG | ORG-FR | 11 | MVP | Frozen |
| DASH | DASH-FR | 13 | MVP | Frozen |
| ALERT | ALERT-FR | 12 | MVP | Frozen |
| RISK | RISK-FR | 13 | MVP | Frozen |
| INVEST | INVEST-FR | 10 | MVP | Frozen |
| COMP | COMP-FR | 10 | MVP | Frozen |
| WALLET | WALLET-FR | 10 | V2 | Frozen |
| SEC | SEC-FR | 9 | V2 | Frozen |
| AI | AI-FR | ~60 (planned) | MVP | **Pending FRS** |
| REPORT | REPORT-FR | ~20 (planned) | V2 | **Pending FRS** |
| ADMIN | ADMIN-FR | ~20 (planned) | MVP | **Pending FRS** |
| OPS | OPS-FR | ~20 (planned) | V2 | **Pending FRS** |

### Business requirement to domain mapping (selected)

| Business requirement | Domain(s) | Example FRs |
|---------------------|-----------|-------------|
| RI-BR-001 Risk detection | RISK, ALERT | RISK-FR-003, ALERT-FR-001 |
| RI-BR-002 Alert handling | ALERT, DASH | ALERT-FR-001–012, DASH-FR-001 |
| FI-BR-001 Investigation workspace | INVEST, DASH | INVEST-FR-001–010 |
| CP-BR-001 Compliance workflows | COMP | COMP-FR-001–010 |
| SEC-BR-001 Security monitoring | SEC (V2) | SEC-FR-001–009 |
| WI-BR-001 Wallet intelligence | WALLET (V2) | WALLET-FR-001–010 |
| ADM-BR-001 Administration | ADMIN, USER, ORG, AUTH | USER-FR-*, ORG-FR-* |
| AI-BR-001 AI assistance | AI | AI-FR-* (pending) |
| RA-BR-001 Reporting | REPORT (V2) | REPORT-FR-* (pending) |
| OPS-BR-001 Platform operations | CORE, OPS | CORE-FR-004, OPS-FR-* (pending) |

### MVP critical path FR dependencies

| Workflow step | Required FRs |
|---------------|--------------|
| Authenticated access | AUTH-FR-001–005, AUTHZ-FR-001–004 |
| Risk evaluation | RISK-FR-002, RISK-FR-003 |
| Alert creation | ALERT-FR-001 (consumes RISK events) |
| Case management | INVEST-FR-001, INVEST-FR-003 |
| Compliance review | COMP-FR-001–006 |
| Audit trail | CORE-FR-012, CORE-FR-013 |

Full traceability matrix: [FunctionalRequirements.md](../02-requirements/FunctionalRequirements.md) inventory tables; Phase 3/4: [Phase4Traceability.md](../08-development/Phase4Traceability.md).

---

## 24. Non-Functional Requirements Summary

Authoritative detail: [NonFunctionalRequirements.md](../02-requirements/NonFunctionalRequirements.md).

### Performance targets (MVP simulation)

| NFR ID | Requirement | Target |
|--------|-------------|--------|
| NFR-PERF-001 | Transaction ingest-to-risk evaluation | **P95 ≤ 2s**; P99 ≤ 5s |
| NFR-PERF-002 | Synchronous API read latency | P95 ≤ 300ms |
| NFR-PERF-003 | Dashboard workspace load | P95 ≤ 2s |
| NFR-PERF-004 | Investigation case retrieval | P95 ≤ 500ms |
| NFR-PERF-006 | AI explanation (assistive, non-critical) | **P95 ≤ 5s**; **timeout 10s** |
| NFR-PERF-008 | Authentication operations | P95 ≤ 500ms |
| NFR-PERF-009 | Event propagation (RISK → ALERT) | P95 ≤ 1s |

### Path classification

| Path type | Examples | Latency | AI allowed? |
|-----------|----------|---------|-------------|
| Critical / deterministic | Risk scoring, alert creation, authZ | Strict (≤2s risk) | **No** |
| Interactive synchronous | Case retrieval, dashboard reads | Moderate | Optional assist |
| Assistive / AI | Explanations, summaries | Relaxed (≤5s P95) | Yes |
| Background / batch | Reports (V2), exports | Window-based | Yes |

### Availability and reliability (MVP)

| NFR ID | Target |
|--------|--------|
| NFR-AVAIL-001 | **99.5%** simulation/staging; 99.9% production design objective |
| NFR-AVAIL-002 | Deterministic paths available when AI down |
| NFR-REL-001 | Event processing success ≥ 99.9% |
| NFR-RES-001 | Graceful AI degradation — mandatory paths operational |

### Scalability (MVP simulation)

| NFR ID | Target |
|--------|--------|
| NFR-SCAL-001 | 10,000 transactions/minute (design objective) |
| NFR-SCAL-002 | 100 concurrent analyst sessions |

### Security, privacy, audit (summary — see Sections 25–27)

NFR-SEC-001–010, NFR-PRIV-001–003, NFR-AUD-001–003 apply to MVP.

## 25. Security Requirements

Security requirements derive from NFR-SEC-* and [SecurityArchitecture.md](../03-architecture/SecurityArchitecture.md). Functional security behavior is also specified in domain FRs (e.g., AUTH-FR-013, AUTHZ-FR-*, CORE-FR-011).

### Authentication and authorization (MVP)

| NFR ID | Requirement | Verification |
|--------|-------------|--------------|
| NFR-SEC-001 | Strong authentication — MFA support, session policies | AUTH-FR-007, AUTH-FR-008 |
| NFR-SEC-002 | RBAC enforcement on all protected resources | AUTHZ-FR-001–015 |
| NFR-SEC-008 | Service-to-service authentication for inter-domain calls | Architecture phase |

### Data protection (MVP)

| NFR ID | Requirement |
|--------|-------------|
| NFR-SEC-003 | Encryption in transit (TLS) for all external and inter-service communication |
| NFR-SEC-004 | Encryption at rest for sensitive/confidential/restricted data classes |
| NFR-SEC-005 | Secrets managed via secure store — not embedded in code or config |

### API and input security (MVP)

| NFR ID | Requirement |
|--------|-------------|
| NFR-SEC-006 | API rate limiting and abuse protection |
| NFR-SEC-007 | Input validation on all external interfaces |
| NFR-SEC-009 | AI prompt injection resistance for assistive endpoints |
| NFR-SEC-010 | AI tool authorization — tools subject to AUTHZ |

### Domain security considerations (frozen FRs)

| Domain | Security FR examples |
|--------|---------------------|
| CORE | CORE-FR-011 — privileged config access restriction |
| AUTH | AUTH-FR-012–013 — failure handling, protective controls |
| AUTHZ | PermissionDenied events; least-privilege role model |
| RISK | Rule configuration access restricted to authorized roles |
| COMP | Sanctions and PII handling per data classification |
| SEC (V2) | Threat detection without replacing AUTH mechanisms |

### Security testing

Security validation per [SecurityTesting.md](../08-testing/SecurityTesting.md) and NFR-TEST-001 traceability. Penetration testing scope: **Open question** for MVP gate definition.

---

## 26. Privacy Requirements

| NFR ID | Requirement | Implementation guidance |
|--------|-------------|------------------------|
| NFR-PRIV-001 | PII minimization — collect only data required by FRs | FDS domain data ownership |
| NFR-PRIV-002 | Data retention limits per classification and policy | [DataRetention.md](../04-database/DataRetention.md); ADR-018 |
| NFR-PRIV-003 | Data classification enforcement (public → restricted) | Event envelope `metadata.classification` |

### Data classification (from Data Architecture)

| Class | Examples | Handling |
|-------|----------|----------|
| Public | Platform version | Standard access |
| Internal | Operational metrics | Access controlled |
| Confidential | Alerts, risk scores, cases | Encryption at rest, RBAC |
| Restricted | PII, compliance, sanctions | Encryption, audit, minimal exposure |

### Privacy boundaries by domain

| Domain | Privacy-sensitive data | Cross-domain access |
|--------|------------------------|---------------------|
| USER | Profile PII | Via API with AUTHZ; no direct DB access |
| COMP | KYC, sanctions matches | INVEST read-only linkage where authorized |
| RISK | Transaction references, behavioral signals | ALERT/INVEST consume via events |
| AI | Retrieved context for assistive responses | Must not persist beyond AI schema without authorization |

Jurisdiction-specific retention overrides: **Open question** (NFR-OQ-002; ADR-018).

---

## 27. Auditability

Auditability ensures sensitive actions produce durable, reconstructable records.

| NFR ID | Requirement | Domain support |
|--------|-------------|----------------|
| NFR-AUD-001 | 100% sensitive action audit completeness | CORE-FR-012, CORE-FR-013, CORE-FR-014 |
| NFR-AUD-002 | Decision provenance reconstruction | RISK explanations, INVEST timeline, COMP dispositions |
| NFR-AUD-003 | Configuration change audit | CORE-FR-014, ADMIN (pending) |

### Audited action categories (MVP)

| Category | Examples | Owner |
|----------|----------|-------|
| Authentication | Login, logout, MFA, session expiry | AUTH-FR-015 |
| Authorization | Role assignment, permission denied | AUTHZ |
| Risk | Rule changes, score overrides (if authorized) | RISK |
| Alerts | Create, assign, close, priority changes | ALERT |
| Investigations | Case create, evidence attach, close | INVEST |
| Compliance | Review dispositions, audit package generation | COMP |
| Administration | User create/deactivate, config changes | USER, ORG, ADMIN |
| Platform | Feature flag changes, maintenance mode | CORE-FR-025 |

### Audit record requirements

- Actor identity (user or service) with correlation ID
- Timestamp, action type, target entity, organization scope
- Before/after state where applicable
- Immutable storage — no hard delete (NFR-AUD-001)
- Minimum retention: 7 years design objective ([DataArchitecture.md](../04-database/DataArchitecture.md) §8)

COMP audit preparation (COMP-FR-010) produces exportable evidence packages for regulatory review — distinct from platform audit log but cross-referenced.

---

## 28. Observability

Observability enables operational monitoring, troubleshooting, and SLO tracking.

| NFR ID | Requirement | Reference |
|--------|-------------|-----------|
| NFR-OBS-001 | Structured logging coverage across domain services | [Logging.md](../09-devops/Logging.md) |
| NFR-OBS-002 | Distributed tracing on critical paths | [ObservabilityArchitecture.md](../03-architecture/ObservabilityArchitecture.md) |
| NFR-OBS-003 | Correlation ID propagation end-to-end | Event envelope `correlationId`; API headers |
| NFR-OBS-004 | Health and readiness endpoints per service | CORE-FR-004, CORE-FR-023 |
| NFR-OBS-005 | SLO definitions for critical paths | Risk pipeline, alert creation, auth |

### Telemetry by domain (MVP)

| Signal type | Scope | Consumer |
|-------------|-------|----------|
| Metrics | Request latency, error rates, event processing lag | OPS (V2), platform engineering |
| Logs | Structured JSON with correlationId, organizationId | Incident response |
| Traces | Risk evaluation span, alert creation span | Performance debugging |
| Health | `/health`, `/ready` per domain service | Load balancers, CORE aggregation |
| AI metrics | Assist latency, timeout rate, tool success | AI evaluation framework |

### Critical path SLOs (simulation targets)

| Path | SLO indicator | Target |
|------|---------------|--------|
| Risk evaluation | P95 latency | ≤ 2s (NFR-PERF-001) |
| Event RISK→ALERT | P95 propagation | ≤ 1s (NFR-PERF-009) |
| API reads | P95 latency | ≤ 300ms (NFR-PERF-002) |
| AI assist | P95 latency / timeout | ≤ 5s / 10s (NFR-PERF-006) |
| Platform uptime | Availability | 99.5% (NFR-AVAIL-001) |

OPS domain (V2) surfaces operational dashboards for platform health (OPS-FR-* pending).

---

## 29. AI Capabilities

AI Platform provides **assistive** capabilities across MVP workflows. Detailed FRs pending; capability outline from FDS AI domain and [AIArchitecture.md](../03-architecture/AIArchitecture.md).

### MVP AI capabilities (assistive posture)

| Capability | Description | Domain interaction |
|------------|-------------|-------------------|
| Risk explanation narrative | Natural language expansion of RISK explanations | Reads RISK context; does not alter scores |
| Alert triage assistance | Summarization and recommendation for analyst review | Reads ALERT/RISK; ALERT owns priority |
| Case summarization | Investigation timeline and evidence summary | Reads INVEST; INVEST owns case state |
| Evidence retrieval | RAG-based document and record retrieval | Authorized tool access via AUTHZ |
| Compliance document assist | Summarize KYC/screening documents | Reads COMP; human approves outcomes |
| Workspace summary | DASH widget assistive content | Presentation layer only |

### AI agents (FDS association)

| Agent | Primary domain | Role |
|-------|---------------|------|
| Risk Analysis Agent | RISK | Assistive analysis (not score authority) |
| Alert Triage Assistant | ALERT | Assistive triage recommendations |
| Workspace Summary Assistant | DASH | Queue/case summaries |
| Multi-Agent Orchestration | AI | Coordinate assistive workflows |

### AI non-capabilities (explicit)

- Creating, closing, or assigning alerts autonomously
- Opening, resolving, or closing investigation cases
- Approving KYC, sanctions clearance, or compliance outcomes
- Blocking users, freezing funds, or revoking API keys
- Owning any business lifecycle state transition

### AI evaluation (MVP/V2)

| NFR ID | Target | Release |
|--------|--------|---------|
| NFR-AI-001 | Fallback without blocking core workflows | MVP |
| NFR-AI-002 | Human approval for consequential recommendations | MVP |
| NFR-AI-003 | Evaluation pass rate ≥ 90% | V2 |
| NFR-AI-004 | Model/prompt version tracking | MVP |
| NFR-COST-001 | AI cost monitoring | MVP |

Detail: [EvaluationFramework.md](../05-ai/EvaluationFramework.md), [AIEvaluationTesting.md](../08-testing/AIEvaluationTesting.md).

**Open question (BQ-1):** Authoritative AI FRS scope for MVP agents — blocking for full implementation planning.

---

## 30. AI/Human Responsibility Boundaries

Per Phase 1 decision PD-07 and [ProductDiscovery.md](ProductDiscovery.md) §18:

| Activity | AI role | Human / deterministic role |
|----------|---------|---------------------------|
| Risk scoring | May narrate explanation | **RISK** rules/models per FRs determine score |
| Alert priority | May recommend | **ALERT owns queue priority** |
| Alert disposition | May summarize context | **Analyst** closes/escalates per policy |
| Case lifecycle | May summarize, retrieve evidence | **INVEST owns** create/update/close |
| Evidence attachment | May suggest sources | **Investigator** confirms attachment |
| Compliance decision | Retrieve/summarize documents | **Compliance officer** approves outcome |
| KYC approval | Assist review | **Human sign-off** required |
| Sanctions disposition | Highlight match context | **Human disposition** per COMP-FR |
| Security threat triage (V2) | Assistive timeline summary | **Security engineer** disposition |
| Enforcement actions | **Not authorized** | Exchange systems / authorized human process |
| Configuration changes | **Not authorized** | Platform administrator |
| Role assignment | **Not authorized** | Administrator via AUTHZ |

### Degradation behavior

When AI is unavailable (NFR-RES-001, NFR-AI-001):

- Risk scoring, alert creation, case management, compliance workflows **continue**
- AI-dependent UI elements show degraded state with clear messaging
- No silent fallback to autonomous decisions
- Analysts retain full access to deterministic data (scores, rules, evidence)

### AI output requirements (EG-02 / Principle 4)

Every AI recommendation should include where applicable:

- Confidence indicator
- Supporting evidence references
- Reasoning summary
- Source references (retrievable)

---

## 31. API and Event Contract Summary

Authoritative contracts: [EventContracts.md](../06-api/EventContracts.md), [APIInventory.md](../06-api/APIInventory.md), [OpenAPI.yaml](../06-api/OpenAPI.yaml).

### API design principles

| Principle | Source |
|-----------|--------|
| REST for synchronous commands and queries | [APIStandards.md](../06-api/APIStandards.md) |
| Domain-scoped API namespaces | APIInventory |
| DASH BFF for browser clients (MVP) | ADR-017 / GD P3-OQ-002 |
| Service-to-service direct domain APIs | ADR-017 |
| Versioned OpenAPI with CI validation | [ContractValidationCI.md](../08-development/ContractValidationCI.md) |
| Vendor-neutral message broker | [MessageBrokerArchitecture.md](../06-api/MessageBrokerArchitecture.md) — no mandatory Kafka |

### MVP event catalog (published)

| Producer | Events | Key consumers |
|----------|--------|---------------|
| CORE | `PlatformStarted`, `PlatformUnavailable`, `ConfigurationUpdated`, `FeatureFlagChanged` | ADMIN, OPS (V2) |
| AUTH | `UserLoggedIn`, `SessionExpired` | SEC (V2) |
| RISK | `RiskCalculated`, `HighRiskDetected` | ALERT, DASH, INVEST, COMP, AI |
| ALERT | `AlertCreated`, `AlertAssigned`, `AlertClosed` | INVEST, DASH, AI, SEC (V2) |
| INVEST | `CaseCreated`, `CaseUpdated`, `CaseClosed`, `EvidenceAttached` | DASH, COMP, SEC (V2), WALLET (V2) |
| COMP | `ComplianceReviewed`, `TravelRuleValidated`, `SanctionsHitDetected`, `AuditPackagePrepared` | REPORT (V2), AI |
| AI | `AIRecommendationGenerated` | DASH |

### Event delivery semantics

| Aspect | Policy |
|--------|--------|
| Guarantee | At-least-once |
| Ordering | Per aggregate key partition where noted |
| Retry | Exponential backoff |
| DLQ | After retry exhaustion |
| Malformed events | Quarantine; no state mutation |

### External ingest boundary

RISK consumes external `TransactionReceived` — ingest boundary event, not a Sentinel domain publish event.

### V2 event additions (frozen)

| Producer | Events |
|----------|--------|
| WALLET | `WalletProfileUpdated`, `AddressReputationChanged`, `SuspiciousWalletDetected` |
| SEC | `ThreatDetected`, `SuspiciousSessionDetected`, `ApiAbuseDetected` |

JSON Schema artifacts: [schemas/](../06-api/schemas/).

**Governance:** Do not rename or add events to frozen domains without change control ([SchemaRegistryGovernance.md](../06-api/SchemaRegistryGovernance.md)).

---

## 32. Data Ownership Summary

Per [DataArchitecture.md](../04-database/DataArchitecture.md) and FDS data ownership sections.

### Storage roles

| Store | Role | Source of truth? |
|-------|------|------------------|
| PostgreSQL (domain schemas) | Authoritative business state | **Yes** |
| Neo4j | Graph projections (WALLET V2) | No — derived |
| Redis | Cache, rate limits, idempotency | No |
| pgvector | Embedding index for AI retrieval | No — derived |

### Entity ownership registry (selected)

| Entity | Owning domain | Schema | Write authority |
|--------|---------------|--------|-----------------|
| PlatformConfig | CORE | `core` | CORE |
| Session | AUTH | `auth` | AUTH |
| Role, Permission | AUTHZ | `authz` | AUTHZ |
| User | USER | `user` | USER |
| Organization | ORG | `org` | ORG |
| RiskAssessment, RiskRule | RISK | `risk` | RISK |
| Alert | ALERT | `alert` | ALERT |
| InvestigationCase, Evidence | INVEST | `invest` | INVEST |
| ComplianceRecord | COMP | `comp` | COMP |
| AIRecommendation, Prompt | AI | `ai` | AI |
| WalletProfile | WALLET (V2) | `wallet` | WALLET |
| SecuritySignal | SEC (V2) | `sec` | SEC |
| ReportDefinition | REPORT (V2) | `report` | REPORT |

### Cross-domain rules

1. **Database-per-domain** — no cross-domain direct table access (NFR-INT-001)
2. **Publish after commit** — events only after durable domain DB commit
3. **Eventual consistency** — cross-domain workflows via events
4. **Read models** — DASH and REPORT (V2) are eventually consistent projections
5. **AI writes** — recommendations to AI schema only; business state unchanged until human API action

Detail: [PostgreSQL.md](../04-database/PostgreSQL.md), [Neo4j.md](../04-database/Neo4j.md), [Redis.md](../04-database/Redis.md).

---

## 33. Multi-Tenancy

Sentinel AI is multi-tenant by organization.

| Aspect | Design |
|--------|--------|
| Tenant key | `organization_id` (UUID) on all tenant-scoped tables and events |
| Enforcement | Application layer + AUTHZ; optional PostgreSQL RLS in implementation |
| Cross-tenant queries | **Prohibited** except platform super-admin (audited) |
| Index pattern | Composite indexes leading with `organization_id` |
| Event isolation | `organizationId` required in event envelope |
| API isolation | All operational APIs scoped to authenticated user's organization |
| Test requirement | Tenant isolation integration tests mandatory |

### Tenant lifecycle (ORG domain)

| Event | Meaning |
|-------|---------|
| `OrganizationCreated` | New tenant provisioned |
| `OrganizationUpdated` | Settings or metadata changed |
| `OrganizationDeactivated` | Tenant deactivated; access restricted |

ORG-FR-001–011 govern organization lifecycle. USER and AUTHZ depend on ORG for tenant context.

### Super-admin access

Platform super-admin cross-tenant access (if implemented) requires explicit authorization, full audit trail, and minimal use policy — **Product context — not an implementation requirement** until ADMIN FRS defines behavior.

---

## 34. Error and Failure Handling Expectations

Per [ErrorHandling.md](../06-api/ErrorHandling.md), CORE-FR-016, and NFR-RES-*.

### Error response standards

| Aspect | Expectation |
|--------|-------------|
| Format | Consistent error envelope with code, message, correlationId |
| Client errors (4xx) | Actionable messages; no internal stack traces |
| Server errors (5xx) | Generic external message; detailed internal logging |
| Authorization failures | 403 with `PermissionDenied` audit event |
| Validation failures | 400 with field-level detail where safe |

### Failure modes and expected behavior

| Failure | Expected behavior | NFR |
|---------|-------------------|-----|
| AI service unavailable | Assistive features degrade; core workflows continue | NFR-RES-001 |
| Redis/cache unavailable | Increased latency; no incorrect state | NFR-RES-002 |
| Event broker partial failure | Retry with backoff; DLQ after exhaustion | NFR-RES-003 |
| Duplicate/out-of-order events | Idempotent processing; no duplicate state | NFR-RES-004, NFR-INT-002 |
| RISK service degraded | ALERT creation delayed; health signals visible | NFR-AVAIL-002 |
| Single domain outage | Other domains continue within dependency limits | NFR-RES-* |
| Maintenance mode | CORE-FR-025 controls controlled degradation | CORE |

### Event processing failures

- Malformed events: quarantined; no domain state mutation
- Consumer failure: retry with exponential backoff
- Poison messages: DLQ with alerting (OPS V2)
- Idempotency: `idempotencyKey` in event metadata where applicable

### User-facing degradation

DASH must surface domain unavailability clearly — stale data indicators, retry options, and no silent empty states that imply "no alerts/cases" when service is down.

---

## 35. Availability and Resilience Expectations

### Availability targets

| Scope | Target | NFR | Notes |
|-------|--------|-----|-------|
| Platform overall (MVP simulation) | **99.5%** | NFR-AVAIL-001 | Staging/simulation environment |
| Production design objective | 99.9% | NFR-AVAIL-001 | Future production goal |
| Deterministic critical paths | Same as platform when AI down | NFR-AVAIL-002 | Risk, alert, auth, cases |
| AI assistive path | Degraded OK | NFR-AVAIL-003 | Non-blocking by design |

### Resilience patterns

| Pattern | Application |
|---------|-------------|
| Graceful degradation | AI, cache, optional features |
| Circuit breaking | Inter-service calls (architecture phase) |
| Health/readiness probes | All domain services (CORE-FR-004) |
| Event replay | Rebuild projections from event log |
| Maintenance mode | CORE-FR-025 — controlled platform state |
| Backup/DR | PostgreSQL PITR; RPO ≤ 1h target (NFR-DR-001) |

### Critical path definition (must remain available)

1. Authentication and authorization (AUTH, AUTHZ)
2. Risk evaluation and event publication (RISK)
3. Alert creation and queue presentation (ALERT, DASH)
4. Investigation case read/write (INVEST)
5. Compliance workflow execution (COMP)
6. Audit logging (CORE)

AI assist, search (V2), reporting (V2), and graph projections are **non-critical** and may degrade.

### Chaos testing expectations

- AI disabled: all mandatory paths operational (NFR-AVAIL-002 verification)
- Broker latency injection: eventual consistency maintained
- Single pod failure: horizontal scale recovery

Detail: [DisasterRecovery.md](../09-devops/DisasterRecovery.md).

---

## 36. Success Metrics

All values are **proposed targets** for validation — not achieved results. Sources: [ProductDiscovery.md](ProductDiscovery.md) §16, [NonFunctionalRequirements.md](../02-requirements/NonFunctionalRequirements.md).

### Product metrics (MVP)

| Metric | Proposed target | Domain |
|--------|-----------------|--------|
| Alert triage time (median) | 30–40% reduction vs baseline | ALERT, DASH |
| Investigation context assembly time | 50% reduction | INVEST, RISK |
| Case resolution cycle time | 20% reduction | INVEST |
| False-positive investigation rate | 15–25% reduction | RISK, ALERT |
| Audit evidence completeness | ≥ 95% required artifacts auto-collected | COMP |
| Cross-team handoff failures | < 5% missing context | ALERT → INVEST |

### AI metrics (MVP/V2)

| Metric | Proposed target | NFR mapping |
|--------|-----------------|-------------|
| Explanation groundedness (eval pass) | ≥ 90% | NFR-AI-003 (V2) |
| Hallucination rate (critical fields) | < 2% | AI evaluation |
| Tool retrieval success rate | ≥ 95% | AI evaluation |
| AI suggestion acceptance rate | Track; target 60–70% | Informational |
| P95 assistive latency | < 5s | NFR-PERF-006 |
| Cost per assisted investigation | Track against budget | NFR-COST-001 |

### Platform metrics (MVP)

| Metric | Proposed target | NFR mapping |
|--------|-----------------|-------------|
| Platform availability | **99.5%** simulation | NFR-AVAIL-001 |
| Event processing success rate | ≥ 99.9% | NFR-REL-001 |
| API P95 read latency | < 300ms | NFR-PERF-002 |
| Dashboard load P95 | < 2s | NFR-PERF-003 |
| Audit log completeness | 100% sensitive actions | NFR-AUD-001 |
| AI degradation failover | 100% mandatory paths operational | NFR-AI-001 |

### Business metrics (qualitative tracking)

- Reduction in operational costs — track; baseline TBD
- Reduction in fraud losses — track; requires exchange integration (deferred)
- Regulatory compliance consistency — qualitative audit feedback
- Analyst satisfaction — survey instrument TBD
- User satisfaction — NPS or CSAT TBD

---

## 37. KPI Measurement Methodology

### Measurement principles

1. **Baselines before targets** — proposed percentage improvements require measured baselines (BQ-3)
2. **Simulation first** — MVP metrics collected in exchange-modeled simulation environment
3. **Domain-attributable** — KPIs map to owning domain where possible
4. **No fabricated results** — report targets as design objectives until validated
5. **Continuous collection** — instrument during implementation, not post-launch only

### Data sources

| Metric category | Primary source | Collection method |
|-----------------|----------------|-------------------|
| Latency SLOs | Service metrics + distributed traces | Prometheus/Datadog-class tooling (vendor-neutral) |
| Availability | Uptime probes on critical paths | Synthetic monitoring |
| Event processing | Broker consumer lag + success counters | Event pipeline metrics |
| Audit completeness | Audit log analysis vs action inventory | Automated audit coverage test |
| AI quality | Evaluation framework offline + online sampling | [AIEvaluationTesting.md](../08-testing/AIEvaluationTesting.md) |
| Product workflow times | Application event timestamps | Instrumented workflow spans |
| Analyst productivity | Case/alert timestamps, session analytics | DASH interaction events |

### Reporting cadence (proposed)

| Cadence | Audience | Content |
|---------|----------|---------|
| Daily | Engineering | SLO dashboards, error rates, event lag |
| Weekly | Product + operations | Workflow time trends, AI acceptance |
| Monthly | Leadership | Business objective progress, risk register |
| Per release | QA + product | Gate criteria verification |

### Baseline establishment (BQ-3 — Open question)

Before claiming percentage improvements:

1. Define simulation workload profile (transaction volume, alert rate, analyst count)
2. Run unassisted baseline workflow timings for triage, context assembly, case resolution
3. Document baseline methodology in test plan
4. Re-measure after MVP feature complete

---

## 38. Simulation vs Production Distinction

Sentinel AI deliberately separates **simulation/internal platform capability** from **production deployment claims**.

| Aspect | Simulation / MVP staging | Production (future) |
|--------|-------------------------|---------------------|
| Data source | Simulated or recorded exchange-modeled events | Live exchange integrations (deferred) |
| Transaction volume | 10K txn/min design target | Exchange-scale TBD |
| Availability claim | 99.5% simulation target | 99.9% design objective |
| Fraud detection accuracy | Measured on simulation/eval datasets | Requires production validation |
| Exchange integration | Internal ingest adapters | External production feeds (V3/deferred) |
| Blockchain analytics | WALLET V2 with test/sandbox data | Production chain providers (deferred) |
| AI evaluation | Offline eval sets + staging sampling | Production monitoring with governance |
| Regulatory filing | Not executed | Human process outside Sentinel |

### Product claim rules (Principle 17)

**May claim (when implemented and tested in simulation):**

- Platform supports exchange-modeled risk, alert, investigation, and compliance workflows
- Domain ownership boundaries and event contracts as specified
- NFR targets met in defined simulation environment

**Must NOT claim without explicit validation:**

- Production deployment at a named exchange
- Perfect or autonomous fraud detection
- Guaranteed zero false positives/negatives
- Real-time global blockchain coverage beyond defined WALLET scope
- Production-scale proven performance

### Portfolio demonstration strategy

**Open question (NBQ-3):** Simulated vs recorded demo data strategy for portfolio presentations.

---

## 39. Risks

Consolidated from [ProductScope.md](ProductScope.md), [ProductDiscovery.md](ProductDiscovery.md) §19, FDS domain risks, and [RiskRegister.md](../02-requirements/RiskRegister.md).

| ID | Risk | Impact | Likelihood | Mitigation |
|----|------|--------|------------|------------|
| PR-01 | Vision/marketing drift — product language broader than FRS | Scope creep, incorrect implementation | Medium | FDS/FRS authority; PRD traceability; change control |
| PR-02 | AI scope creep — autonomous enforcement implied | Regulatory/trust failure | Medium | Principles §3, §7; PD-07; NFR-AI-002 |
| PR-03 | ALERT/RISK boundary confusion | Duplicated or missing priority logic | Low | Frozen boundaries; terminology reconciliation complete |
| PR-04 | V2 promoted to MVP (SEC/WALLET/REPORT) | Schedule slip, incomplete MVP | Medium | Explicit MVP/V2 sections; GD-001 for REPORT |
| PR-05 | Pending FRS gaps (AI, ADMIN, REPORT, OPS) | Implementation blocking | High | Flagged open questions; roadmap gate |
| PR-06 | AI hallucinations in investigations | Wrong analyst decisions | Medium | Human review, evidence grounding, eval framework |
| PR-07 | False positives — analyst fatigue | Reduced platform value | Medium | Explainability, RISK context, hybrid rules |
| PR-08 | False negatives — undetected fraud | Financial/regulatory harm | Medium | Continuous rule updates; eval; not claimed zero |
| PR-09 | Integration complexity across exchanges | Adoption friction | Medium | Standardized integration interfaces; deferred production |
| PR-10 | Data quality from external ingest | Incorrect risk scores | Medium | Validation, confidence scoring |
| PR-11 | NFR targets not met in implementation | Failed release gate | Medium | Performance testing from Phase 3 strategy |
| PR-12 | Regulatory change | Compliance feature obsolescence | Medium | Configurable COMP policies |
| PR-13 | Security threats to platform itself | Data breach, trust loss | Low | Security architecture, testing |
| PR-14 | Vendor lock-in pressure | Reduced flexibility | Low | Provider neutrality principle |

---

## 40. Assumptions

### Business assumptions

- Customer operates core exchange infrastructure (trading, custody, ledger)
- Established operational procedures for fraud investigation and compliance exist
- Dedicated Risk, Compliance, and Security teams available
- Organization accepts human-in-the-loop decision model

### Operational assumptions

- Exchange transaction events available for ingest (simulation or integration)
- Authentication events consumable by platform
- User identity data exists externally or via USER domain
- KYC information maintained (externally or via COMP workflows)
- Operational audit logs available for correlation

### Technical assumptions

- APIs available for system integration
- Customer infrastructure supports secure communication (TLS)
- Message broker infrastructure available (vendor-neutral)
- LLM/AI services available when AI features enabled — **not mandatory for MVP critical paths**
- PostgreSQL-class relational store available for domain schemas
- Container/orchestration environment available for deployment (architecture choice)

### Organizational assumptions

- Platform administrators available for user/role provisioning
- Compliance stakeholders available for retention policy decisions (NFR-OQ-002)
- Product owner available for pending sign-offs (GD-001 REPORT, BQ-1 AI scope)

### Documentation assumptions

- FDS/FRS remain authoritative for frozen domains
- Pending FRS domains will be authored before Application Development Gate per roadmap
- Architecture artifacts remain consistent with domain boundaries

---

## 41. Dependencies

### Documentation dependencies

| Dependency | Required for | Status |
|------------|--------------|--------|
| FDS v1.4 | Domain ownership, events | Delivered |
| FRS v1.9 (frozen domains) | Implementation behavior | Delivered |
| FRS AI chapter | AI implementation | **Pending (BQ-1)** |
| FRS ADMIN chapter | Admin implementation | **Pending (BQ-2)** |
| FRS REPORT chapter | V2 reporting | Pending |
| FRS OPS chapter | V2 operations | Pending |
| NFR v1.0 | Quality gates | Delivered |
| OpenAPI + Event schemas | Contract CI | Delivered (Phase 4/5) |
| Data architecture | Database design | Delivered (Phase 3) |

### Phase dependencies (from Project Roadmap)

| Phase | Output | PRD dependency |
|-------|--------|----------------|
| Phase 1 | Product discovery | Complete — informs this PRD |
| Phase 2 | NFR + architecture foundations | Complete |
| Phase 3 | API + data design | Complete |
| Phase 4 | Contract validation CI | Complete |
| Phase 5 | PRD consolidation | **This document** |
| Application Development Gate | Implementation start | Requires pending FRS + sign-offs |

### External dependencies (deferred)

| Dependency | Consumer | Release |
|------------|----------|---------|
| Exchange transaction feed | RISK | MVP (simulated); production deferred |
| KYC/sanctions provider | COMP | Integration model open (NBQ-5) |
| Blockchain analytics provider | WALLET | V2 |
| LLM provider | AI | MVP (vendor-neutral) |
| SIEM platform | SEC | V3 deferred |
| Identity provider | AUTH | MVP integration |

### Internal domain dependencies (MVP critical path)

```text
CORE → AUTH → AUTHZ → USER/ORG
RISK → ALERT → DASH
ALERT → INVEST
RISK → INVEST (evidence context)
COMP ↔ INVEST (cross-reference)
AI → (reads all; writes AI schema only)
```

---

## 42. Open Questions

### Blocking (before full implementation planning)

| ID | Question | Why blocking | Status |
|----|----------|--------------|--------|
| BQ-1 | Authoritative AI FRS scope for MVP agents (which agents, domains, tool boundaries)? | AI domain FRS pending | Open |
| BQ-2 | ADMIN FRS minimum for MVP launch vs deferral? | ADMIN FRS pending | Open |
| BQ-3 | Baseline metrics collection methodology for proposed product targets? | Percentage targets need baselines | Open |
| BQ-4 | Is REPORT required for MVP stakeholder sign-off? | Release scope | **GD-001: V2, not MVP gate — human sign-off PENDING** |

### Non-blocking (architecture / design)

| ID | Question |
|----|----------|
| NBQ-1 | Exact eval framework ownership and pass/fail gates for AI explanations |
| NBQ-2 | Graph visualization depth in DASH vs domain APIs |
| NBQ-3 | Simulated vs recorded demo data strategy for portfolio |
| NBQ-4 | Multi-region deployment assumptions |
| NBQ-5 | External KYC/sanctions provider integration model (COMP) |
| NFR-OQ-002 | Jurisdiction-specific data retention overrides |

### Resolved (reference)

| ID | Decision | Document |
|----|----------|----------|
| P3-OQ-001 | SSE for MVP DASH refresh; WebSocket V2 | ADR-016 |
| P3-OQ-002 | DASH BFF for browser; direct S2S APIs | ADR-017 |
| P3-OQ-003 | Default retention targets; jurisdiction overrides open | ADR-018 |
| BQ-4 (recommendation) | REPORT not MVP gate dependency | GD-001 |

---

## 43. Governance and Change Control

Governance follows [ProjectRoadmap.md](../00-project/ProjectRoadmap.md) phase control and documented decision logs.

### Authority hierarchy

```text
1. Frozen FRS (domain behavior for delivered domains)
2. FDS (domain ownership, events, release boundaries)
3. NFR (quality attributes)
4. Architecture decisions (ADRs)
5. Product documents (Vision, Scope, PRD, Discovery)
6. Implementation code
```

Lower levels must not contradict higher levels without governed change.

### Change control rules

| Change type | Process |
|-------------|---------|
| Frozen FR modification | Formal change request; impact analysis; re-approval |
| New event in frozen domain | Change control; schema registry update; consumer impact |
| MVP scope expansion | Product owner approval; PRD/roadmap update; gate re-evaluation |
| ADR supersession | New ADR with rationale; architecture review |
| AI autonomy expansion | Explicit FR authorization; security/compliance review |

### Decision log

Recorded decisions: [GovernanceDecisions.md](../00-project/GovernanceDecisions.md)

Phase 1 product decisions PD-01 through PD-10: [ProductDiscovery.md](ProductDiscovery.md) §21.

### Review cadence (proposed)

| Artifact | Review trigger |
|----------|---------------|
| PRD | Major release boundary change |
| FRS | Per domain delivery + change requests |
| OpenAPI/Event schemas | Any contract change |
| NFR | Performance/security baseline change |

---

## 44. Release Acceptance Criteria

### MVP release gate (Application Development complete → MVP release)

| Criterion | Verification method |
|-----------|---------------------|
| All MVP domain FRs implemented and tested (frozen chapters) | FR traceability matrix; test coverage |
| AI FRS MVP scope implemented per BQ-1 resolution | AI test suite |
| ADMIN FRS MVP scope implemented per BQ-2 resolution | Admin test suite |
| Event contracts validated in CI | [ContractValidationCI.md](../08-development/ContractValidationCI.md) |
| OpenAPI validated against implementation | API integration tests |
| NFR critical targets met in simulation | Performance test report |
| Risk P95 ≤ 2s | NFR-PERF-001 load test |
| AI assist P95 ≤ 5s; 10s timeout | NFR-PERF-006 test |
| Platform availability 99.5% in staging | NFR-AVAIL-001 monitoring |
| Mandatory paths operational with AI disabled | Chaos test |
| Tenant isolation verified | Integration test suite |
| Audit completeness for sensitive actions | NFR-AUD-001 audit coverage test |
| ALERT/RISK/INVEST/COMP boundaries verified | Domain integration tests |
| No V2/V3 features presented as MVP | Scope review checklist |

### Explicitly excluded from MVP gate (GD-001)

| Exclusion | Rationale |
|-----------|-----------|
| REPORT domain APIs and features | V2 per FDS; DASH/COMP satisfy MVP visibility |
| WALLET domain | V2 |
| SEC domain | V2 |
| OPS full suite | V2 (CORE health satisfies MVP minimum) |
| Production exchange integration | Deferred |
| SIEM sync, insider threat, automated containment | V3/future |

**Human sign-off pending:** GD-001 REPORT gate exclusion requires formal product-owner approval.

### V2 release gate (summary)

| Criterion | Verification |
|-----------|--------------|
| WALLET-FR-001–010 implemented | Domain test suite |
| SEC-FR-001–009 implemented | Security test suite |
| REPORT-FR-* implemented | Reporting test suite |
| OPS-FR-* implemented | Ops test suite |
| V2 event schemas in CI | Schema validation |
| NFR-PERF-007 report generation targets | Performance test |

---

## 45. MVP Definition of Done

MVP is **done** when all conditions below are satisfied:

### Functional completeness

- [ ] CORE-FR-001–026 implemented (MVP subset; V2 items excluded)
- [ ] AUTH-FR-001–020 implemented
- [ ] AUTHZ-FR-001–015 implemented
- [ ] USER-FR-001–012 implemented
- [ ] ORG-FR-001–011 implemented
- [ ] RISK-FR-001–013 implemented
- [ ] ALERT-FR-001–012 implemented
- [ ] INVEST-FR-001–010 implemented
- [ ] DASH-FR-001–013 implemented
- [ ] COMP-FR-001–010 implemented
- [ ] AI-FR MVP subset implemented (per BQ-1 resolution when available)
- [ ] ADMIN-FR MVP subset implemented (per BQ-2 resolution when available)

### Workflow completeness

- [ ] End-to-end: transaction ingest → risk → alert → triage → investigation
- [ ] End-to-end: compliance review with human approval and audit trail
- [ ] End-to-end: user provisioning → authentication → authorized workspace access
- [ ] AI assist available on non-critical paths with degradation behavior

### Quality completeness

- [ ] NFR critical performance targets met (risk ≤ 2s P95, AI ≤ 5s P95)
- [ ] 99.5% availability demonstrated in staging/simulation
- [ ] Event processing ≥ 99.9% success rate
- [ ] 100% sensitive action audit coverage
- [ ] Tenant isolation tests pass
- [ ] Security test suite pass (AUTH, AUTHZ, input validation, encryption)

### Contract completeness

- [ ] MVP event schemas validated in CI
- [ ] OpenAPI MVP surface validated in CI
- [ ] No unauthorized events in frozen domain schemas

### Documentation completeness

- [ ] FR traceability matrix updated
- [ ] Runbook for simulation environment deployment
- [ ] Known limitations documented (V2/V3 deferrals explicit)

### Explicitly NOT required for MVP Done

- REPORT domain (GD-001)
- WALLET, SEC, OPS domains
- Production exchange integration
- SIEM, insider threat, automated containment
- 99.9% production availability (design objective only)
- Achieved business metric percentages (targets only until baselines exist)

---

## 46. Future Roadmap

High-level roadmap aligned with [ProjectRoadmap.md](../00-project/ProjectRoadmap.md) and FDS domain roadmaps.

### Phase timeline (conceptual)

| Phase | Focus | Key deliverables |
|-------|-------|------------------|
| Phase 1 ✅ | Product discovery | Vision, Scope, Personas, Principles, Discovery |
| Phase 2 ✅ | NFR + architecture foundations | NFR, Domain Boundaries, System Architecture |
| Phase 3 ✅ | API + data design | OpenAPI, Event Contracts, Data Architecture |
| Phase 4 ✅ | Contract validation CI | Schema registry, CI gates, governance decisions |
| Phase 5 | PRD consolidation | **This document** |
| Phase 6+ | Application development | MVP implementation per FRs |
| Post-MVP | Version 2 | WALLET, SEC, REPORT, OPS |
| Future | Version 3 | Insider threat, SIEM, containment, advanced blockchain, extended AI |

### Version 2 roadmap items (from FDS)

| Domain | V2 features (summary) |
|--------|----------------------|
| WALLET | Wallet profiling, reputation, suspicious detection, graph projections |
| SEC | API/auth/device monitoring, threat detection |
| REPORT | Operational/executive dashboards, KPI snapshots |
| OPS | Advanced health, ops alerting, diagnostics, backup visibility |
| CORE | Advanced scheduling, search expansion, notification hardening |
| INVEST | FI-BR-003 extended features |
| DASH | RA-BR-001 reporting views |
| USER | Activity history |
| ORG | Hierarchy management |

### Version 3 / future (deferred)

- SEC insider-threat pattern detection
- SIEM bi-directional synchronization
- Automated containment (exchange-side enforcement integration)
- Advanced blockchain intelligence and production chain feeds
- Cross-exchange threat intelligence sharing
- Predictive risk intelligence
- Extended AI autonomy (governed FR required)
- Multi-region active-active deployment

Detail: [FutureFeatures.md](../10-roadmap/FutureFeatures.md), [ReleasePlan.md](../10-roadmap/ReleasePlan.md).

---

## 47. Traceability Summary

### Document traceability chain

```text
Vision / Business Requirements (BO-xxx, xx-BR-xxx)
    ↓
Product Scope / PRD (this document)
    ↓
Functional Domain Specification (domain ownership, events)
    ↓
Functional Requirements ({DOMAIN}-FR-xxx)
    ↓
Non-Functional Requirements (NFR-xxx)
    ↓
Architecture (ADRs, Domain Boundaries, System Architecture)
    ↓
API / Events (OpenAPI, EventContracts, schemas)
    ↓
Data (DataArchitecture, PostgreSQL, migrations)
    ↓
Testing (TestPlan, PerformanceTesting, traceability matrices)
    ↓
Implementation
```

### Phase 1 reconciliation matrix (selected)

| Topic | Phase 1 decision | PRD section |
|-------|-------------------|-------------|
| MVP scope | FDS governs; WALLET/SEC excluded | §15, §16 |
| ALERT/RISK boundary | ALERT owns priority | §22, §30 |
| AI posture | Assistive only | §29, §30 |
| REPORT release | V2; not MVP gate (GD-001) | §15, §44, §45 |
| Insider threat | V3 deferred | §17 |
| Business objectives | BO-001–006 as targets | §10 |
| Personas | Personas.md canonical | §7 |
| Principles | Principles.md canonical | §12 |

Full matrix: [ProductDiscovery.md](ProductDiscovery.md) §22.

### FR → NFR → Test traceability

| Domain FR | Related NFR | Test reference |
|-----------|-------------|----------------|
| RISK-FR-003 | NFR-PERF-001 | PerformanceTesting |
| ALERT-FR-001 | NFR-PERF-009, NFR-REL-001 | APIAndDataTestingStrategy |
| AUTH-FR-001 | NFR-PERF-008, NFR-SEC-001 | SecurityTesting |
| INVEST-FR-001 | NFR-PERF-004, NFR-AUD-002 | TestPlan |
| CORE-FR-012 | NFR-AUD-001 | SecurityTesting |
| AI-FR-* | NFR-PERF-006, NFR-AI-* | AIEvaluationTesting |

Phase traceability artifacts: [Phase3Traceability.md](../08-development/Phase3Traceability.md), [Phase4Traceability.md](../08-development/Phase4Traceability.md).

---

## 48. Final Product Decisions

Consolidated binding decisions from Phase 1–5. These govern product and implementation unless changed via governance process.

| ID | Decision | Rationale | Impact |
|----|----------|-----------|--------|
| PD-01 | FDS/FRS govern release scope over Vision v0.5 MVP list | Vision predates domain delivery | MVP excludes WALLET, SEC |
| PD-02 | WALLET and SEC are V2, not MVP | Frozen FDS release column | No MVP claims for wallet/security |
| PD-03 | ALERT owns alert queue priority | Frozen ALERT/RISK boundary | RISK provides context only |
| PD-04 | Account Compromise Investigation is INVEST support, not SEC feature #5 | SEC has 4 V2 features | Terminology mapped |
| PD-05 | Insider-threat patterns V3 deferred | FDS SEC deferred scope | Vision Goal 4 updated |
| PD-06 | Business objectives BO-001–006; metrics are targets | No fabricated results | Success metrics labeled proposed |
| PD-07 | AI is assistive-only unless FRs say otherwise | Governance + frozen domains | No AI lifecycle ownership |
| PD-08 | Personas.md and Principles.md are Phase 1 canonical | Phase 1 structure | Supersede skeleton docs |
| PD-09 | No vendor lock-in in product docs | Roadmap governance | Vendor-neutral architecture |
| PD-10 | Product Scope v1.1 addendum preserves approval | Change control | Chapter 4 reconciliation |
| GD-001 | REPORT is V2; NOT MVP gate dependency | FDS + BQ-4 resolution | MVP excludes REPORT; **sign-off pending** |
| ADR-003 | Deterministic risk on critical path; AI non-blocking | LLM latency variability | NFR-PERF-001/006 separation |
| ADR-016 | SSE for MVP DASH refresh | Simplicity | WebSocket deferred V2 |
| ADR-017 | DASH BFF for browser clients | Aggregation boundary | Direct S2S for services |

### Non-negotiable boundaries (implementation checklist)

1. RISK publishes events; **does not create alerts**
2. ALERT owns alert lifecycle and **operational priority**
3. INVEST owns investigation cases and evidence
4. COMP owns compliance workflow outcomes
5. DASH presents data; **does not own lifecycles**
6. AI assists; **does not own lifecycles or enforcement**
7. Frozen FR domains **must not be redefined** in product or UI copy
8. V2/V3 capabilities **must not appear as MVP** without change control
9. NFR targets are **simulation objectives** until production validation
10. Provider neutrality — **no mandatory Kafka/AWS/LLM** at product level

---

## Appendix A — Glossary (selected)

| Term | Definition |
|------|------------|
| Alert | Operational work item owned by ALERT domain with lifecycle and priority |
| Case | Investigation record owned by INVEST domain |
| Domain | Bounded functional capability with single ownership (16 domains) |
| Assistive AI | AI that recommends/explains without owning business state |
| Frozen FR | Delivered functional requirement chapter not open for casual modification |
| MVP | Version 1 — first usable exchange-modeled release |
| Priority signal | Risk-derived context for ALERT — not ALERT queue priority |
| Simulation target | NFR/metric objective for staging — not production claim |
| Tenant | Organization scoped by `organization_id` |

---

## Appendix B — Document Index

| Section | Title |
|---------|-------|
| 1 | Document Control |
| 2 | Executive Summary |
| 3 | Product Vision |
| 4 | Product Background |
| 5 | Industry Context |
| 6 | Problem Statement |
| 7 | Users and Personas |
| 8 | User Problems |
| 9 | Product Goals |
| 10 | Business Objectives |
| 11 | Technical Objectives |
| 12 | Product Principles |
| 13 | Product Differentiation |
| 14 | Scope |
| 15 | MVP Definition |
| 16 | Version 2 Scope |
| 17 | Version 3 / Future Scope |
| 18 | Explicit Out of Scope |
| 19 | User Journeys |
| 20 | Major Workflows |
| 21 | Functional Capability Overview |
| 22 | Domain Ownership |
| 23 | Functional Requirement Traceability |
| 24 | Non-Functional Requirements Summary |
| 25 | Security Requirements |
| 26 | Privacy Requirements |
| 27 | Auditability |
| 28 | Observability |
| 29 | AI Capabilities |
| 30 | AI/Human Responsibility Boundaries |
| 31 | API and Event Contract Summary |
| 32 | Data Ownership Summary |
| 33 | Multi-Tenancy |
| 34 | Error and Failure Handling Expectations |
| 35 | Availability and Resilience Expectations |
| 36 | Success Metrics |
| 37 | KPI Measurement Methodology |
| 38 | Simulation vs Production Distinction |
| 39 | Risks |
| 40 | Assumptions |
| 41 | Dependencies |
| 42 | Open Questions |
| 43 | Governance and Change Control |
| 44 | Release Acceptance Criteria |
| 45 | MVP Definition of Done |
| 46 | Future Roadmap |
| 47 | Traceability Summary |
| 48 | Final Product Decisions |

---

*End of PRD v0.1 Draft — 2026-09-03*
