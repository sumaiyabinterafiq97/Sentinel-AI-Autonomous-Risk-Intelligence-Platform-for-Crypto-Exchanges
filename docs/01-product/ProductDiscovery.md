# Product Discovery — Phase 1 Report

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Product Discovery |
| Phase | Phase 1 — Product Discovery |
| Version | 1.0 (Draft) |
| Status | Draft |
| Owner | Product & Engineering Team |
| Last Updated | 2026-09-03 |
| Authority | Phase 1 reconciliation report; subordinate to FDS/FRS for domain behavior |

---

## 1. Executive Summary

Phase 1 establishes a coherent product foundation for Sentinel AI by reconciling early product documents (Vision v0.5, Product Scope v1.0) with the mature requirements baseline (FDS v1.3, FRS v1.9).

**Sentinel AI** is an autonomous **risk intelligence platform** for cryptocurrency exchanges. It unifies transaction risk monitoring, fraud investigation, compliance workflows, and (in Version 2) wallet and security intelligence into an evidence-based, human-governed operational platform with assistive AI.

Phase 1 did **not** modify frozen domain requirements (CORE, AUTH, AUTHZ, USER, ORG, DASH, ALERT, RISK, INVEST, WALLET, COMP, SEC). Reconciliation was achieved through product document updates, explicit terminology mapping, and release boundary clarification.

**Key outcomes:**

- MVP is bounded to FDS MVP domains; WALLET and SEC are Version 2
- AI remains assistive; domains retain lifecycle ownership
- ALERT owns operational alert priority; RISK provides risk context
- Product differentiation is realistic—no claims of perfect detection or autonomous enforcement
- Personas, principles, success metric **targets**, and open questions are documented

---

## 2. Current Product Baseline

### 2.1 Repository documentation state

| Document | Version | Status | Phase 1 action |
|----------|---------|--------|----------------|
| ProjectRoadmap.md | 1.0 Draft | Draft | Referenced as process authority |
| Vision.md | 1.0 Draft | Phase 1 reconciled | Updated MVP, BO IDs, insider-threat scope |
| ProductScope.md | 1.1 | Approved + addendum | Chapter 4 reconciliation added |
| Personas.md | 1.0 Draft | New | Created |
| Principles.md | 1.0 Draft | New | Created |
| ProductDiscovery.md | 1.0 Draft | New | This document |
| FunctionalDomainSpecification.md | 1.3 Draft | Authoritative | Not modified |
| FunctionalRequirements.md | 1.9 Draft | 12 domains delivered | Not modified |
| NonFunctionalRequirements.md | Skeleton | Not started | Out of Phase 1 scope |
| UserPersonas.md / ProductPrinciples.md | Skeleton | Legacy placeholders | Superseded by Personas.md / Principles.md for Phase 1 |

### 2.2 Frozen domain requirements (unchanged)

CORE, AUTH, AUTHZ, USER, ORG, DASH, ALERT, RISK, INVEST, WALLET, COMP, SEC — FRS chapters delivered and frozen post-delivery.

**Pending FRS domains:** AI, REPORT, ADMIN, OPS

### 2.3 Git baseline at Phase 1 execution

| Item | Value |
|------|-------|
| Local HEAD | Includes ProjectRoadmap commit (ahead of origin) |
| FRS delivery HEAD | `634d649` — SEC delivery on origin/main |
| Phase 1 changes | Uncommitted per instruction |

---

## 3. Product Problem

Cryptocurrency exchanges operate under high transaction velocity, evolving fraud tactics, regulatory obligations, and security threats spanning API access, authentication, and on-chain activity. Operational teams typically work across fragmented tools:

- Risk scoring in one system, alerts in another, investigations in a third
- Compliance evidence manually assembled for audits
- Security telemetry disconnected from fraud and investigation workflows
- AI experiments disconnected from auditable operational processes

**Core problem:** Exchange risk, fraud, compliance, and security operations lack a **unified, evidence-based intelligence layer** that supports human decision-making with explainable context, clear domain ownership, and auditability.

Sentinel AI addresses this as an **internal enterprise platform** modeled after exchange-grade operations—not as a claim of existing production deployment at a named exchange.

---

## 4. Target Users

| Persona | Primary domain interaction | MVP | V2 |
|---------|---------------------------|-----|-----|
| Risk Analyst | RISK, ALERT, DASH, INVEST | Primary | + WALLET, SEC context |
| Compliance Officer | COMP, INVEST (read), audit | Primary | + REPORT |
| Security Engineer | SEC, INVEST support | Limited | Primary |
| Platform Administrator | ADMIN, USER, ORG, AUTH, AUTHZ | Primary | + OPS |

Detail: [Personas.md](Personas.md)

---

## 5. User Problems

| User | Problem | Sentinel response |
|------|---------|-------------------|
| Risk Analyst | Alert fatigue; opaque scores | Explainable RISK + ALERT queue with context |
| Risk Analyst | Manual context assembly for investigations | INVEST handoff with attached evidence |
| Compliance Officer | Incomplete audit packages | COMP workflows + audit preparation |
| Compliance Officer | Unclear AI accountability | Human sign-off; grounded AI assist only |
| Security Engineer | Isolated security telemetry | SEC domain (V2) linked to INVEST |
| Platform Admin | Role sprawl; weak audit | AUTHZ least privilege + admin audit trails |

---

## 6. Product Vision

**Vision statement:** Sentinel AI enables cryptocurrency exchanges to detect, investigate, and govern financial crime and operational risk through unified, explainable, evidence-based intelligence—with human accountability for consequential decisions.

Aligned with [Vision.md](Vision.md). Phase 1 corrected release scope drift (Wallet/API Security listed as MVP in v0.5 Vision).

---

## 7. Product Mission

Deliver a modular risk intelligence platform that:

1. Consolidates operational workflows for risk, investigation, and compliance
2. Produces explainable, evidence-linked intelligence outputs
3. Respects strict domain ownership and event contracts
4. Uses AI to accelerate—not replace—authorized human decisions
5. Scales through domain modularity and observability

---

## 8. Product Value Proposition

| Stakeholder | Value |
|-------------|-------|
| Exchange operations | Faster triage and investigation with unified context |
| Compliance | Repeatable workflows and audit-ready evidence |
| Security (V2) | Operational security signals integrated with investigation |
| Engineering | Clear domain boundaries and contract-first integration |
| Leadership | Realistic roadmap with explicit MVP/V2/V3 separation |

**Business objectives (targets, not achieved results):** BO-001 through BO-006 in Vision, sourced from [BusinessRequirements.md](BusinessRequirements.md).

---

## 9. Product Differentiation

### 9.1 Legitimate differentiation

| Differentiator | Description |
|----------------|-------------|
| Unified risk intelligence | RISK + ALERT + INVEST + DASH in one platform with shared context |
| Evidence-based investigation | INVEST case lifecycle with evidence aggregation and timeline |
| Behavioral context | RISK behavioral analysis and explanations |
| Graph relationships | Relationship context across entities (within delivered domain scope) |
| AI-assisted investigation | Assistive agents for explanation, retrieval, summarization |
| Explainable recommendations | Risk and AI outputs tied to evidence and rules |
| Cross-domain intelligence | Events link RISK → ALERT → INVEST → COMP |
| Analyst workflow support | DASH operational workspace; not a generic BI tool |
| AI evaluation posture | AI Platform designed for observability and evaluation (FRS pending) |

### 9.2 Explicitly NOT claimed

- Perfect or autonomous fraud detection
- Autonomous blocking of all fraud
- Guaranteed zero false positives
- Real-time global blockchain intelligence beyond defined WALLET scope
- Production Binance or named exchange integration
- Access to private exchange production data
- AI-owned enforcement or compliance approval

### 9.3 Capability classification

| Class | Examples |
|-------|----------|
| **Simulated / internal platform** | MVP exchange-modeled workflows, internal event pipelines |
| **Conceptual future** | SIEM bi-directional sync, insider-threat patterns, automated containment |
| **External integration (deferred)** | Production chain analytics, third-party KYC providers, exchange core banking |

---

## 10. Product Capability Model

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
| Intelligence | RISK, WALLET (V2), SEC (V2) | Scoring, monitoring, signals |
| Operations | ALERT, INVEST, COMP | Alert lifecycle, cases, compliance |
| Experience | DASH | Workspace presentation |
| Platform | AI, ADMIN, OPS (V2), REPORT (V2) | Assistive AI, admin, ops, reporting |

---

## 11. Product Boundaries

### 11.1 Sentinel AI owns

| Capability | Owning domain |
|------------|---------------|
| Risk scoring & explanations | RISK |
| Alert lifecycle & queue priority | ALERT |
| Investigation cases & evidence | INVEST |
| Compliance workflows (MVP set) | COMP |
| Operational workspace UI | DASH (presentation) |
| Wallet intelligence (V2) | WALLET |
| Security monitoring (V2) | SEC |
| Assistive AI services | AI Platform |

### 11.2 Sentinel AI consumes

| Consumer | Consumes from |
|----------|---------------|
| RISK | Transaction/activity ingest (external); USER/ORG context |
| ALERT | RISK events (`RiskCalculated`, `HighRiskDetected`) |
| INVEST | ALERT, RISK, COMP, SEC (V2) context |
| DASH | RISK, ALERT, INVEST, COMP (read/present) |
| SEC (V2) | Auth/API/device event streams |

### 11.3 Sentinel AI supports (does not own lifecycle)

| Support role | Owner |
|--------------|-------|
| Account compromise investigation context | INVEST (SEC provides signals) |
| Risk-informed alert handling | ALERT (RISK provides context) |
| AI summaries in case review | INVEST (AI assists) |
| Dashboard visualization of domain data | DASH (domains own data) |

### 11.4 Sentinel AI does not own

| Capability | Reason |
|------------|--------|
| Exchange core ledger / custody | Out of product scope |
| External regulatory filing execution | Human/process outside platform |
| Production API key revocation at exchange | Enforcement in exchange systems |
| SIEM as system of record (V3) | Deferred |
| Identity provider internals | AUTH integrates; does not replace IdP |

---

## 12. MVP Definition

**Version 1 (MVP)** = first **usable** release for exchange-modeled risk, alert, investigation, compliance, and administration workflows.

### 12.1 MVP domains (FDS authority)

| Domain | MVP deliverable focus |
|--------|----------------------|
| CORE | Platform services, audit, health, configuration |
| AUTH / AUTHZ / USER / ORG | Identity, authorization, tenants |
| RISK | Transaction/behavioral risk, explanations, signals |
| ALERT | Alert creation, lifecycle, **queue priority** |
| INVEST | Cases, evidence, timeline, assignment |
| DASH | Operational workspace and queues |
| COMP | KYC, AML support, Travel Rule, sanctions, audit prep |
| AI | Assistive agents (FRS pending; assistive-only posture) |
| ADMIN | Administration (FRS pending) |

### 12.2 MVP user outcomes

- Analysts triage alerts with risk context in a unified workspace
- Investigators manage cases with evidence and audit trail
- Compliance officers execute MVP compliance workflows with human approval
- Administrators provision users and roles with least privilege
- Platform operates when AI is degraded (for mandatory paths)

### 12.3 Explicitly excluded from MVP

- WALLET domain features
- SEC domain features
- REPORT analytics domain
- OPS full operational suite
- Insider-threat patterns, SIEM sync, automated containment
- Production external exchange/blockchain integrations

---

## 13. Version 2

| Domain / capability | V2 scope summary |
|--------------------|------------------|
| **WALLET** | Wallet profiling, address investigation, reputation signals |
| **SEC** | API Monitoring, Authentication Monitoring, Device Monitoring, Threat Detection (4 features) |
| **REPORT** | Reporting & analytics (FRS pending) |
| **OPS** | Platform operations, health, diagnostics (FRS pending) |
| **INVEST** | FI-BR-003 and extended features per FDS |
| **DASH** | RA-BR-001 reporting views |
| **ORG / USER** | Hierarchy, activity history per FDS V2 items |

Account Compromise Investigation is **support context** across SEC + INVEST—not a fifth SEC feature.

---

## 14. Version 3 / Future

Per FDS deferred scope (not exhaustive):

- SEC insider-threat pattern detection
- SIEM bi-directional synchronization
- Automated containment actions
- Advanced blockchain intelligence integrations
- Production-grade external exchange data feeds
- Extended AI autonomy (only if explicitly governed in future requirements)

**Rule:** V3 items must not appear as MVP or V2 commitments in product or UI without change control.

---

## 15. Product Principles

Eighteen durable principles documented in [Principles.md](Principles.md), including:

- Production-first design
- Security by design
- Human oversight
- Explainability
- Evidence-based decisions
- Deterministic enforcement
- AI as assistive capability
- Graceful AI degradation
- Clear domain ownership
- Explicit scope control

Aligned with Project Roadmap Engineering Philosophy.

---

## 16. Success Metrics

All values below are **proposed targets** for later validation—not achieved results.

### 16.1 Product metrics

| Metric | Proposed target | Notes |
|--------|-----------------|-------|
| Alert triage time (median) | 30–40% reduction vs baseline | Baseline TBD at implementation |
| Investigation context assembly time | 50% reduction | Time to attach risk/alert context to case |
| Case resolution cycle time | 20% reduction | End-to-end for standard cases |
| False-positive investigation rate | 15–25% reduction | Via explainability and context |
| Audit evidence completeness | ≥95% required artifacts auto-collected | COMP audit prep |
| Cross-team handoff failures | <5% missing context | INVEST handoffs from ALERT/RISK |

### 16.2 AI metrics

| Metric | Proposed target | Notes |
|--------|-----------------|-------|
| Explanation groundedness (eval pass rate) | ≥90% | Human or eval framework |
| Hallucination rate (critical fields) | <2% | Measured on eval set |
| Tool retrieval success rate | ≥95% | Evidence retrieval agents |
| AI suggestion acceptance rate | Track; target 60–70% | Informational, not autonomous |
| P95 assistive latency | <5s interactive | Degraded mode beyond threshold |
| Cost per assisted investigation | Track against budget | FinOps target TBD |

### 16.3 Platform metrics

| Metric | Proposed target | Notes |
|--------|-----------------|-------|
| Platform availability | 99.5% MVP; 99.9% production goal | OPS domain V2+ |
| Event processing success rate | ≥99.9% | RISK → ALERT pipeline |
| API P95 latency (read operations) | <300ms | Domain APIs |
| Dashboard load (P95) | <2s primary views | DASH |
| Audit log completeness | 100% sensitive actions | CORE audit |
| AI degradation failover | 100% mandatory paths operational | No AI hard dependency |

---

## 17. Key User Workflows

### 17.1 Risk alert triage (MVP)

1. RISK publishes risk events → ALERT creates/updates alerts
2. Risk Analyst opens DASH alert queue (ALERT priority ordering)
3. Analyst reviews RISK explanation and context
4. Disposition: close, monitor, or escalate to INVEST
5. Action audited; AI may summarize (assistive)

### 17.2 Investigation case management (MVP)

1. Case created from alert or manual initiation (INVEST)
2. Evidence attached from RISK, ALERT, COMP sources
3. Timeline reconstructed; notes and assignment managed
4. Resolution with human approval; audit trail complete

### 17.3 Compliance review (MVP)

1. COMP workflow triggered (KYC, sanctions, Travel Rule)
2. Compliance Officer reviews with evidence
3. Human decision recorded; audit package updated
4. Escalation to INVEST if fraud overlap suspected

### 17.4 Security incident support (V2)

1. SEC detects API/auth/device threat signal
2. Security Engineer triages in DASH with SEC context
3. Escalation creates/links INVEST case
4. No autonomous enforcement in Sentinel

---

## 18. AI Role and Human Oversight

| Activity | AI role | Human / deterministic role |
|----------|---------|---------------------------|
| Risk explanation | Assistive narrative | RISK score from rules/models per FRs |
| Alert priority | May recommend | **ALERT owns queue priority** |
| Case summarization | Assistive | **INVEST owns case lifecycle** |
| Compliance decision | Retrieve/summarize evidence | **Human approves COMP outcomes** |
| Security threat triage | Assistive context (V2) | **Human disposition; SEC owns signals** |
| Enforcement | Not authorized | Exchange systems / authorized process |

**Phase 1 decision:** AI Platform owns agents and evaluation; business domains do not delegate lifecycle ownership to AI unless future FRs explicitly authorize.

---

## 19. Scope Risks

| Risk | Description | Mitigation |
|------|-------------|------------|
| Vision/marketing drift | Product language broader than FRS | Chapter 4 mapping; FDS authority |
| AI scope creep | Autonomous enforcement implied by name | Principles 3, 6, 7; explicit boundaries |
| ALERT/RISK confusion | Risk prioritization duplicated | Terminology reconciliation complete |
| V2 promoted to MVP | SEC/WALLET in early Vision MVP | Vision corrected; exit checklist |
| Pending FRS gaps | AI, REPORT, ADMIN, OPS undefined | Flagged as blocking for full implementation gate |
| NFR absence | No performance/security NFR doc | Phase 2+ requirement |

---

## 20. Open Questions

### 20.1 Blocking (before architecture / full implementation planning)

| # | Question | Why blocking |
|---|----------|--------------|
| BQ-1 | What is the authoritative AI FRS scope for MVP agents (which agents, which domains, tool boundaries)? | AI domain FRS pending |
| BQ-2 | What is ADMIN FRS minimum for MVP launch (vs deferral)? | ADMIN FRS pending |
| BQ-3 | What baseline metrics collection exists for proposed product targets? | Targets need baseline methodology |
| BQ-4 | Is REPORT required for any MVP stakeholder sign-off, or strictly V2? | Release scope confirmation |

### 20.2 Non-blocking (resolve in architecture / design)

| # | Question |
|---|----------|
| NBQ-1 | Exact eval framework ownership and pass/fail gates for AI explanations |
| NBQ-2 | Graph visualization depth in DASH vs domain APIs |
| NBQ-3 | Simulated vs recorded demo data strategy for portfolio |
| NBQ-4 | Multi-region deployment assumptions |
| NBQ-5 | External KYC/sanctions provider integration model (COMP) |

---

## 21. Phase 1 Decisions

| ID | Decision | Reason | Source | Impact | Downstream change? |
|----|----------|--------|--------|--------|-------------------|
| PD-01 | FDS/FRS govern release scope over Vision v0.5 MVP list | Vision predates domain delivery | FDS catalog | Vision MVP rewritten | Vision, ProductScope |
| PD-02 | WALLET and SEC are V2, not MVP | Frozen FDS release column | FDS v1.3 | Removed from MVP claims | Vision, ProductDiscovery |
| PD-03 | ALERT owns alert queue priority | Frozen ALERT/RISK boundary | FRS ALERT, FDS | Renamed "Alert Prioritization" in RISK context | ProductScope |
| PD-04 | Account Compromise Investigation is support, not SEC feature #5 | SEC has 4 V2 features | FDS SEC | Mapped in ProductScope Ch.4 | ProductScope |
| PD-05 | Insider-threat patterns V3 deferred | SEC FDS deferred scope | FDS SEC | Vision Goal 4 updated | Vision |
| PD-06 | Business objectives use BO-00x IDs; metrics are targets | Align to BRS; no fake results | BRS, governance | Vision BO section updated | Vision |
| PD-07 | AI is assistive-only unless FRs say otherwise | Governance rules + frozen domains | Roadmap, FRS | Principles, Personas | Architecture input |
| PD-08 | Personas.md and Principles.md are Phase 1 canonical | User Phase 1 structure | Phase 1 spec | New files | Supersedes skeletons |
| PD-09 | No vendor lock-in in product docs | Phase 1 governance rule | Roadmap | Principles §18 | Architecture phase |
| PD-10 | Product Scope stays Approved with v1.1 addendum | Preserve approval; reconcile explicitly | Change control | Chapter 4 added | No FRS change |

---

## 22. Reconciliation Matrix

| Topic | Existing Source | Current State | Phase 1 Decision | Downstream Impact |
|-------|-----------------|---------------|------------------|-------------------|
| Product vision | Vision v0.5 | Strong intent; MVP drift | Reconciled to FDS; v1.0 draft | Vision updated |
| Product scope | ProductScope v1.0 Approved | Broader than FRS in places | Ch.4 mapping; v1.1 addendum | ProductScope updated |
| MVP | Vision listed Wallet, API Security | FDS: MVP excludes WALLET, SEC | MVP = CORE…COMP + AI + ADMIN | Roadmap Phase 2 input |
| V2 | FDS catalog | WALLET, SEC, REPORT, OPS | Confirmed V2 | No FRS change |
| V3 | FDS SEC deferred | Insider, SIEM, containment | Confirmed deferred | Vision Goal 4 |
| Personas | UserPersonas skeleton | Empty | Personas.md created | UX/architecture input |
| AI ownership | FDS AI domain | Assistive | AI Platform owns agents; domains own lifecycles | AI FRS authoring |
| Human oversight | Roadmap philosophy | Stated | Explicit in PD-07, §18 | All phases |
| Security | ProductScope 5 SEC capabilities | SEC 4 features | 5th = INVEST support | SEC unchanged |
| Compliance | COMP FRS frozen | MVP delivered | MVP includes COMP | None |
| Investigation | INVEST FRS frozen | MVP delivered | INVEST owns cases | None |
| Risk | RISK FRS frozen | MVP delivered | RISK context only for alerts | ALERT unchanged |
| API security | Vision MVP | SEC V2 | SEC V2 for API monitoring | None |
| Dashboard | DASH FRS frozen | Presentation domain | DASH does not own lifecycles | None |
| Reporting | ProductScope implied | REPORT V2 | Reporting V2 | REPORT FRS needed |
| Domain ownership | FDS v1.3 | 16 domains defined | Boundaries in §11 | Architecture |
| Event-driven behavior | FDS matrices | RISK→ALERT→INVEST | Preserved | Contract-first design |

---

## 23. Phase 1 Exit Assessment

| Criterion | Status |
|-----------|--------|
| Vision validated/reconciled | ✅ |
| Product scope reconciled | ✅ |
| Personas defined | ✅ |
| Product principles defined | ✅ |
| Product boundaries explicit | ✅ |
| MVP/V2/V3 explicit | ✅ |
| Differentiation realistic | ✅ |
| Success metrics as targets | ✅ |
| AI/human responsibility explicit | ✅ |
| FDS/FRS constraints respected | ✅ |
| Frozen domains untouched | ✅ (verify via git diff) |
| No implementation code | ✅ (verify via git status) |
| No vendor-specific architecture | ✅ |
| Open questions documented | ✅ |
| Phase 1 decisions documented | ✅ |
| Reconciliation matrix complete | ✅ |
| ProductDiscovery.md complete | ✅ |
| Internal consistency | ✅ |
| git diff --check | ⏳ Run at validation |
| Changes limited to Phase 1 docs | ⏳ Run at validation |

**Phase 1 status:** Complete pending validation checks.

**Recommended next phase:** Phase 2 — Non-Functional Requirements & Architecture Foundations (per ProjectRoadmap.md), including AI, ADMIN, REPORT, OPS FRS authoring before Application Development Gate.

---

## Related Documents

- [Vision.md](Vision.md)
- [ProductScope.md](ProductScope.md)
- [Personas.md](Personas.md)
- [Principles.md](Principles.md)
- [Project Roadmap](../00-project/ProjectRoadmap.md)
- [Functional Domain Specification](../02-requirements/FunctionalDomainSpecification.md)
- [Functional Requirements Specification](../02-requirements/FunctionalRequirements.md)
