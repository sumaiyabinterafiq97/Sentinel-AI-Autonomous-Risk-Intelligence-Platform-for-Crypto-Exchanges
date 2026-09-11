# Sentinel AI — Project Roadmap & Phase Control

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Project Roadmap & Phase Control |
| Version | 1.0 (Draft) |
| Status | Draft |
| Owner | Product & Engineering Team |
| Last Updated | 2026-09-03 |
| Authority | Master control document for project sequencing, gates, and change control |

---

## 1. Document Purpose

This document is the **master control document** for Sentinel AI project progression.

It exists to:

- Define where the project is today and what remains before implementation.
- Sequence work from product definition through architecture, design, implementation, testing, deployment, and portfolio readiness.
- Prevent uncontrolled scope expansion, accidental rewriting of frozen requirements, and premature coding.
- Establish explicit **entry criteria**, **exit criteria**, and **validation** for every phase.
- Make the project executable phase-by-phase in a manner consistent with enterprise engineering practice.

All subsequent documentation work, design work, and implementation work **must follow this roadmap**. If a planned activity is not mapped to a phase in this document, it should not begin without updating this roadmap through controlled change.

This roadmap does **not** replace authoritative requirement or architecture documents. It controls **when** those documents are created, validated, frozen, and consumed.

---

## 2. Project Identity

**Project:** Sentinel AI

**Full name:** Autonomous Risk Intelligence Platform for Crypto Exchanges

**Project type:** Production-grade enterprise risk intelligence platform

**Primary objective:** Build a modular platform that combines:

- Transaction risk monitoring
- Behavioral intelligence
- Fraud detection
- Compliance intelligence
- API security monitoring
- Wallet intelligence
- Graph intelligence
- AI-assisted investigation
- Auditability
- Observability
- Human-in-the-loop decision workflows

**Human accountability:** AI assists analysis, prioritization, explanation, and workflow acceleration. AI does **not** replace human accountability for critical operational, compliance, or security decisions unless explicitly authorized by policy and supported by auditable controls.

**Realism constraint:** Sentinel AI is designed to resemble an internal platform at a major cryptocurrency exchange. It is **not** a claim of Binance integration, exchange-scale proven performance, or production deployment until explicitly demonstrated through later validation phases.

---

## 3. Engineering Philosophy

These principles are permanent project constraints.

| # | Principle | Meaning |
|---|-----------|---------|
| 1 | **Production-first** | Design and build as if the platform will operate under real operational load, audit scrutiny, and incident response expectations. |
| 2 | **Security-by-design** | Security, authorization, and auditability are designed in from the start—not bolted on after implementation. |
| 3 | **AI-assisted, human-controlled** | AI augments analysts and operators; domain ownership and critical decisions remain human-governed unless explicitly delegated by policy. |
| 4 | **Explainability by default** | Risk, compliance, and investigation outputs should be understandable and traceable to evidence and rules where applicable. |
| 5 | **Evidence-backed decisions** | Operational conclusions should be supportable by records, events, and audit trails—not opaque model assertions alone. |
| 6 | **Clear domain ownership** | Each capability has one owning domain. Cross-domain behavior occurs through contracts, not duplicated lifecycle ownership. |
| 7 | **Contract-first engineering** | Events, APIs, data ownership, and AI tool boundaries are defined before implementation to reduce integration ambiguity. |
| 8 | **Modular architecture** | Domains and services evolve independently within explicit dependency and event contracts. |
| 9 | **Observability-first** | Logs, metrics, traces, health signals, and audit telemetry are planned alongside features—not deferred indefinitely. |
| 10 | **Auditability** | Sensitive actions produce durable audit outcomes using shared platform infrastructure where applicable. |
| 11 | **Graceful degradation** | Optional or assistive capabilities may degrade without blocking core platform operations. |
| 12 | **Testability** | Requirements and architecture must be verifiable through automated and manual validation strategies. |
| 13 | **Provider neutrality where appropriate** | External integrations are described as capability boundaries unless a specific provider is explicitly approved. |
| 14 | **Explicit scope control** | MVP, Version 2, and Version 3 capabilities remain separated. Deferred scope must not silently become current scope. |
| 15 | **No accidental cross-domain ownership** | Implementation convenience must not redefine AUTH, ALERT, INVEST, RISK, COMP, SEC, or other frozen domain boundaries. |

---

## 4. Current Repository Baseline

### 4.1 Git baseline (as of roadmap authoring)

| Item | Value |
|------|-------|
| Branch | `main` |
| HEAD | `634d6491c95ff6087653ea3d5f6d1f91f212a4ae` |
| Latest delivery | SEC — `docs: deliver SEC FDS v1.3 and FRS v1.9` |
| Prior delivery | COMP — `0441af5952f955da3e4b66e7657746a142404d9d` |
| Remote sync | `origin/main` synchronized with local `main` |
| Working tree | Expected clean post-delivery |

### 4.2 Authoritative requirements baseline

| Document | Version | Status | Notes |
|----------|---------|--------|-------|
| Functional Domain Specification (FDS) | 1.3 (Draft) | Draft | Defines 16 functional domains, event contracts, ownership, dependencies |
| Functional Requirements Specification (FRS) | 1.9 (Draft) | Draft | Domain chapters delivered for 12 domains; 4 domains pending FRS authoring |
| Business Requirements (BRS) | — | Draft / partial | Source for business requirements and objectives |

**FRS domain delivery status (authoritative as of FRS v1.9):**

| Domain | FRS Chapter | Delivery posture |
|--------|-------------|------------------|
| CORE | Delivered | Frozen after domain delivery |
| AUTH | Delivered | Frozen |
| AUTHZ | Delivered | Frozen |
| USER | Delivered | Frozen |
| ORG | Delivered | Frozen |
| DASH | Delivered | Frozen |
| ALERT | Delivered | Frozen |
| RISK | Delivered | Frozen |
| INVEST | Delivered | Frozen |
| WALLET | Delivered (V2) | Frozen |
| COMP | Delivered (MVP) | Frozen |
| SEC | Delivered (V2) | Frozen |
| AI | Not authored in FRS | Pending |
| REPORT | Not authored in FRS | Pending |
| ADMIN | Not authored in FRS | Pending |
| OPS | Not authored in FRS | Pending |

### 4.3 Product documentation baseline

| Document | Current state |
|----------|---------------|
| `docs/01-product/Vision.md` | Exists — v0.5 (Draft); predates full domain requirements maturity |
| `docs/01-product/ProductScope.md` | Exists — v1.0 (Approved); requires reconciliation with delivered FDS/FRS |
| `docs/01-product/BusinessRequirements.md` | Exists — authoritative for BR IDs and objectives |
| `docs/01-product/UserPersonas.md` | Skeleton — Draft |
| `docs/01-product/ProductPrinciples.md` | Skeleton — Draft |
| `docs/01-product/SuccessMetrics.md` | Exists — maturity to be validated |
| `docs/01-product/CompetitiveAnalysis.md` | Exists — maturity to be validated |

### 4.4 Design / implementation documentation baseline

The repository contains **document skeletons** for architecture, database, AI, API, UI, testing, DevOps, roadmap, and ADRs. As of this roadmap:

- Substantive, implementation-ready content is **not yet established** in those areas.
- `docs/README.md` explicitly marks sections 03–11 as “Content to be defined.”
- **No application source code, service code, infrastructure code, or CI/CD implementation** is the current primary development phase.

### 4.5 Explicit statement

**APPLICATION CODE IS NOT YET THE PRIMARY DEVELOPMENT PHASE.**

The project has made substantial progress on **domain-level functional requirements** for the majority of platform domains, with formal delivery and validation discipline demonstrated through COMP and SEC. The overall product definition, non-functional requirements, consolidated PRD, architecture, and design phases remain incomplete.

---

## 5. Master Phase Map

Phases are logical control gates, not calendar sprints. Status reflects repository evidence at roadmap authoring time.

| Phase | Name | Status | Primary Deliverable | Coding Allowed? |
|-------|------|--------|---------------------|-----------------|
| 0 | Repository / Governance Baseline | **Complete** | Repo structure, contribution norms, domain delivery discipline | No |
| 1 | Product Discovery | **Next** | Validated product discovery pack anchored by Vision | No |
| 2 | Product Scope & Personas | **Partial** | Aligned scope, personas, principles, success metrics | No |
| 3 | Functional Requirements | **In progress** | Complete FDS/FRS for all in-scope domains | No |
| 4 | Non-Functional Requirements | **Not started** (skeleton only) | Approved NFR specification | No |
| 5 | PRD Consolidation | **Not started** | Consolidated PRD | No |
| 6 | System Architecture | **Not started** (skeleton only) | Approved system architecture | No |
| 7 | Data Architecture | **Not started** (skeleton only) | Approved database / persistence design | No |
| 8 | AI Architecture & Agent Specifications | **Not started** (skeleton only) | Approved AI architecture and agent contracts | No |
| 9 | API & Event Contracts | **Not started** (skeleton only) | Approved API and event contract specification | No |
| 10 | Frontend / UX Design | **Not started** (skeleton only) | Approved UX/design system | No |
| 11 | Development Roadmap & Implementation Planning | **Not started** | Implementation plan and service breakdown | Planning only |
| 12 | Application Development | **Not started** | Running application codebase | **Yes — gated** |
| 13 | Integration & End-to-End Testing | **Not started** | Integrated test evidence | Yes |
| 14 | Security / Performance / Reliability Hardening | **Not started** | Hardening evidence and reports | Yes |
| 15 | Observability & Operations | **Not started** | Operational telemetry and runbooks | Yes |
| 16 | Deployment / Infrastructure | **Not started** | Deployable environments | Infra-as-code allowed when gated |
| 17 | Production Readiness Validation | **Not started** | Production readiness report | Yes |
| 18 | Portfolio / Documentation / Interview Readiness | **Not started** | Portfolio-quality artifact pack | Yes |

**Ordering note:** Phase 1 (Product Discovery) is sequenced **before further domain FRS work** not because requirements were written in the wrong order historically, but because the existing Vision and Scope artifacts predate the mature FDS/FRS baseline and must be reconciled before PRD, architecture, and implementation planning proceed.

---

## 6. Phase-by-Phase Definition

### Phase 0 — Repository / Governance Baseline

**Purpose:** Establish a documentation-first repository with engineering discipline suitable for enterprise-style delivery.

**Inputs:** Project intent; initial documentation skeleton.

**Activities:** Repository structure; contribution guidelines; documentation index; domain delivery workflow; git hygiene; phase completion reporting pattern (demonstrated through COMP and SEC deliveries).

**Deliverables:** Repository root docs; `docs/` structure; proven domain delivery commits; validation and push discipline.

**Exit criteria:** Repository is version-controlled; documentation map exists; at least one full domain delivery lifecycle has been executed with validation gates.

**Validation:** Clean working tree after delivery commits; diff scope limited to intended artifacts; remote sync verified when pushed.

**Coding policy:** No application code.

**Change policy:** Governance docs may evolve; delivered domain requirements become controlled once their delivery phase completes.

**Failure conditions:** Uncontrolled edits to frozen domains; undocumented delivery; mixing unrelated changes in domain commits.

**Next phase:** Phase 1 — Product Discovery.

---

### Phase 1 — Product Discovery

**Purpose:** Establish and validate the strategic product foundation before consolidated PRD and architecture.

**Inputs:** Existing `Vision.md`; market/problem context; stakeholder intent; emerging requirements baseline.

**Activities:** Validate vision against delivered domain model; refine problem statement; confirm target users and value proposition; identify non-goals; align terminology with FDS domain names and BRS IDs.

**Deliverables:**
- `docs/01-product/Vision.md` (validated / revised baseline)
- Discovery notes or decision log entries where needed
- Phase 1 completion report

**Exit criteria:** Vision is internally consistent with BRS and delivered FDS domain boundaries; non-goals explicit; no contradiction with frozen domain requirements.

**Validation:** Cross-read Vision vs BRS vs FDS catalog; terminology check; scope leakage check.

**Coding policy:** No application code.

**Change policy:** Vision may change; must not silently rewrite frozen FRS domain chapters.

**Failure conditions:** Vision claims capabilities contradicted by frozen requirements; fake production or exchange integration claims.

**Next phase:** Phase 2 — Product Scope & Personas.

---

### Phase 2 — Product Scope & Personas

**Purpose:** Define what is in/out of the product and who it serves.

**Inputs:** Approved/validated Vision; BRS; FDS domain catalog.

**Activities:** Reconcile Product Scope with delivered requirements; complete personas; complete product principles; define success metrics tied to business objectives.

**Deliverables:**
- `docs/01-product/ProductScope.md` (reconciled)
- `docs/01-product/UserPersonas.md`
- `docs/01-product/ProductPrinciples.md`
- `docs/01-product/SuccessMetrics.md`
- Phase 2 completion report

**Exit criteria:** Scope boundaries align with MVP/V2/V3 release strategy in FDS; personas map to DASH/ALERT/INVEST/COMP/SEC workflows; principles align with engineering philosophy (Section 3).

**Validation:** Scope vs FDS release columns; persona coverage; duplicate capability check.

**Coding policy:** No application code.

**Change policy:** Product Scope changes require explicit impact analysis on FDS/FRS if they affect domain boundaries.

**Failure conditions:** Fifth SEC feature; undeclared MVP expansion; persona-driven invention of new domain ownership.

**Next phase:** Phase 3 — Functional Requirements (remaining domains).

---

### Phase 3 — Functional Requirements

**Purpose:** Define authoritative system behavior at the domain level.

**Inputs:** BRS; Vision; Product Scope; FDS structure; domain delivery playbooks proven by COMP/SEC.

**Activities:** Complete remaining domain FDS/FRS chapters (AI, REPORT, ADMIN, OPS); maintain event contracts; maintain traceability; run cross-domain validation before each domain delivery commit.

**Deliverables:**
- Updated `FunctionalDomainSpecification.md`
- Updated `FunctionalRequirements.md`
- Per-domain validation reports
- Controlled delivery commits per domain or approved batch

**Exit criteria:** All in-scope domains for the current product baseline have authored FRS chapters; traceability matrix complete; no unresolved cross-domain contract conflicts.

**Validation:** Phase 5-style domain validation (inventory, events, boundaries, frozen-domain protection, AI ownership, V2/V3 isolation).

**Coding policy:** No application code.

**Change policy:** Once a domain is **delivered and validated**, its FRS chapter is **frozen** except through explicit change control (Section 19). Currently frozen: CORE, AUTH, AUTHZ, USER, ORG, DASH, ALERT, RISK, INVEST, WALLET, COMP, SEC.

**Failure conditions:** SEC-FR-010-style scope creep; cross-domain lifecycle ownership leakage; rewriting frozen domains to simplify later design.

**Next phase:** Phase 4 — Non-Functional Requirements.

---

### Phase 4 — Non-Functional Requirements

**Purpose:** Define quality attributes and operational constraints that govern architecture and implementation.

**Inputs:** FDS/FRS; Vision quality attributes; Product Scope; regulatory/security expectations.

**Activities:** Define performance, scalability, availability, security, privacy, reliability, observability, usability, maintainability targets; tie NFRs to domains where relevant.

**Deliverables:** `docs/02-requirements/NonFunctionalRequirements.md` (complete baseline)

**Exit criteria:** NFR document is testable; major quality attributes have measurable targets or explicit TBD gates; conflicts with FDS NFR tables resolved.

**Validation:** NFR traceability to architecture drivers; no impossible claims without validation plan.

**Coding policy:** No application code.

**Change policy:** NFR baseline frozen after approval; changes require change control.

**Failure conditions:** Vanity metrics; exchange-scale claims without test plan; NFRs that contradict domain event contracts.

**Next phase:** Phase 5 — PRD Consolidation.

---

### Phase 5 — PRD Consolidation

**Purpose:** Produce a single consolidated product definition for engineering consumption.

**Inputs:** Vision; Scope; Personas; BRS; FDS; FRS; NFR.

**Activities:** Consolidate product behavior, releases, personas, objectives, and constraints into a PRD; resolve terminology; include MVP vs V2 vs V3 roadmap summary.

**Deliverables:** `docs/01-product/PRD.md` (or equivalent consolidated artifact)

**Exit criteria:** PRD references authoritative requirement IDs; no orphan features; release scope explicit.

**Validation:** PRD ↔ FRS traceability sampling; scope leakage review.

**Coding policy:** No application code.

**Change policy:** PRD frozen after approval; downstream architecture must trace to PRD and FRS.

**Failure conditions:** PRD rewrites frozen FR semantics; PRD adds features without BRS/FRS backing.

**Next phase:** Phase 6 — System Architecture.

---

### Phase 6 — System Architecture

**Purpose:** Define technical structure, major components, and cross-cutting concerns.

**Inputs:** PRD; FDS; FRS; NFR; ADRs as applicable.

**Activities:** Define service boundaries aligned to domains; event-driven integration; security architecture; deployment topology concept; quality attribute mapping.

**Deliverables:**
- `docs/03-architecture/SystemArchitecture.md`
- Supporting architecture docs (microservices, event-driven, security, data flow, deployment concept)

**Exit criteria:** Every MVP domain has an architectural home; event/API ownership matches FDS; no domain boundary violations.

**Validation:** Architecture ↔ FDS matrix review; failure mode and degradation review.

**Coding policy:** Diagrams and ADRs only; no production service code.

**Change policy:** Architecture baseline frozen after approval; changes via ADR + change control.

**Failure conditions:** Microservice explosion without domain justification; architecture contradicting frozen event contracts.

**Next phase:** Phase 7 — Data Architecture.

---

### Phase 7 — Data Architecture

**Purpose:** Define persistence, retention, and data ownership implementation strategy.

**Inputs:** FDS data ownership; FRS data expectations; NFR; system architecture.

**Activities:** Define PostgreSQL schemas strategy; graph store role; cache role; vector store role; retention; PII handling boundaries.

**Deliverables:** `docs/04-database/*` (complete baseline, not skeleton)

**Exit criteria:** Each domain’s owned data has a persistence strategy; cross-domain reads respect ownership; retention aligned with compliance/security needs.

**Validation:** Data ownership matrix vs FDS; no duplicated source of truth.

**Coding policy:** Schema design docs and migration planning only.

**Change policy:** Frozen after approval; schema changes later via migration discipline.

**Failure conditions:** Database design redefining domain ownership; technology choices without NFR backing.

**Next phase:** Phase 8 — AI Architecture & Agent Specifications.

---

### Phase 8 — AI Architecture & Agent Specifications

**Purpose:** Define AI platform architecture and agent contracts without transferring domain ownership to AI.

**Inputs:** FDS AI domain; FRS AI ownership rules from other domains; PRD; NFR.

**Activities:** Define agent catalog; orchestration; tool boundaries; retrieval strategy; evaluation framework; guardrails; human-in-the-loop points.

**Deliverables:** `docs/05-ai/*` (complete baseline)

**Exit criteria:** Every agent maps to assistive use cases; no agent owns ALERT/INVEST/COMP/SEC lifecycles; evaluation and safety requirements defined.

**Validation:** AI ownership cross-check against frozen domains; assistive-only rules preserved.

**Coding policy:** Prompt/spec prototyping only if explicitly approved; no production agent runtime.

**Change policy:** Frozen after approval.

**Failure conditions:** “Magic chatbot” architecture; agents publishing domain-owned events directly.

**Next phase:** Phase 9 — API & Event Contracts.

---

### Phase 9 — API & Event Contracts

**Purpose:** Make internal and external contracts explicit for implementation and testing.

**Inputs:** FDS event matrix; FRS publication/consumption FRs; system architecture.

**Activities:** Define REST/API conventions; event schemas; versioning; error model; auth integration; contract test strategy.

**Deliverables:** `docs/06-api/*`; event contract appendix aligned with FDS

**Exit criteria:** MVP and in-scope V2 events have contract definitions; API ownership matches domains.

**Validation:** Contract ↔ FDS/FRS diff check; breaking change rules documented.

**Coding policy:** OpenAPI/AsyncAPI artifacts allowed; no business logic implementation.

**Change policy:** Contract baseline frozen after approval.

**Failure conditions:** New events invented outside FDS authority FRs; API ownership crossing domains.

**Next phase:** Phase 10 — Frontend / UX Design.

---

### Phase 10 — Frontend / UX Design

**Purpose:** Define operator and analyst experience without DASH owning upstream lifecycles.

**Inputs:** Personas; DASH/ALERT/INVEST FRS; PRD; NFR usability.

**Activities:** Design system; navigation; workspace/work queue patterns; investigation/compliance/security views; accessibility baseline.

**Deliverables:** `docs/07-ui/*`

**Exit criteria:** Primary workflows mapped to personas; presentation-only boundaries preserved.

**Validation:** UX ↔ DASH-FR alignment; no lifecycle actions assigned to UI that belong to domain services.

**Coding policy:** Design artifacts and optional UI prototypes only.

**Change policy:** Frozen after approval.

**Failure conditions:** UI design inventing new domain events or requirements.

**Next phase:** Phase 11 — Development Roadmap & Implementation Planning.

---

### Phase 11 — Development Roadmap & Implementation Planning

**Purpose:** Translate approved design into an executable build sequence.

**Inputs:** All approved artifacts from Phases 1–10.

**Activities:** Service/repo layout; milestone slicing; dependency-ordered build plan; test strategy activation plan; staffing/sequencing assumptions.

**Deliverables:** Implementation plan; updated `docs/10-roadmap/*` aligned to this master roadmap; service milestone map

**Exit criteria:** Application Development Gate checklist (Section 10) satisfied; first implementation milestone defined with entry/exit criteria.

**Validation:** Gate review; no missing upstream artifact.

**Coding policy:** Planning, spikes, and throwaway prototypes only with explicit scope and discard plan.

**Change policy:** Implementation plan mutable until Phase 12 kickoff; must respect frozen requirements.

**Failure conditions:** Starting coding without gate approval; plan bypassing CORE/AUTH/AUTHZ foundations.

**Next phase:** Phase 12 — Application Development (gated).

---

### Phase 12 — Application Development

**Purpose:** Implement the platform according to approved requirements and design.

**Inputs:** Approved PRD, architecture, data, AI, API, UX, implementation plan.

**Activities:** Build services, integrations, and frontend per implementation plan (Section 11).

**Deliverables:** Application source code; service README files; local dev setup; initial unit tests.

**Exit criteria:** MVP-critical vertical slices demonstrable locally; contracts implemented for first milestone domains.

**Validation:** Build passes; unit tests pass; contract tests for implemented APIs/events.

**Coding policy:** **Yes — primary coding phase begins here.**

**Change policy:** Requirements changes require change control; no drive-by edits to frozen FRS.

**Failure conditions:** Domain logic implemented in wrong service; events published outside contract FR authority.

**Next phase:** Phase 13 — Integration & E2E Testing.

---

### Phase 13 — Integration & End-to-End Testing

**Purpose:** Prove cross-service and cross-domain workflows.

**Inputs:** Implemented services; testing strategy; event/API contracts.

**Activities:** Integration tests; event flow tests; E2E workflow tests for analyst/investigator/compliance/security paths.

**Deliverables:** Test suites; test reports; defect backlog

**Exit criteria:** Critical workflows pass integration/E2E tests; known failures documented with owners.

**Validation:** CI test stages; traceability from tests to FR IDs where feasible.

**Coding policy:** Yes — including test code.

**Change policy:** Test baselines evolve with controlled requirement changes.

**Failure conditions:** Untested cross-domain flows; silent contract drift.

**Next phase:** Phase 14 — Security / Performance / Reliability Hardening.

---

### Phase 14 — Security / Performance / Reliability Hardening

**Purpose:** Move from “works locally” to “defensible under scrutiny.”

**Inputs:** NFR; security architecture; implemented system.

**Activities:** Threat modeling validation; security testing; performance testing; chaos/resilience testing; dependency scanning.

**Deliverables:** Hardening reports; remediated findings; updated threat model

**Exit criteria:** Critical/high security findings addressed or accepted with documented risk; core NFR targets measured.

**Validation:** Security test pass; performance benchmark report; failure injection results.

**Coding policy:** Yes.

**Change policy:** Hardening may require design revisions via change control.

**Failure conditions:** Unaddressed authz bypass; unaudited privileged actions; unbounded resource failures.

**Next phase:** Phase 15 — Observability & Operations.

---

### Phase 15 — Observability & Operations

**Purpose:** Make the platform operable by SRE/operations teams.

**Inputs:** NFR observability; OPS domain requirements; implemented services.

**Activities:** Logging standards; metrics; tracing; dashboards; alerts; runbooks; audit telemetry verification.

**Deliverables:** `docs/09-devops/Observability.md` (complete); dashboards; runbooks

**Exit criteria:** SLOs monitorable; on-call runbooks exist for core failure modes; audit trails queryable.

**Validation:** Trace end-to-end workflow; alert fire/recover drill.

**Coding policy:** Yes.

**Change policy:** Operational baselines adjusted with change control.

**Failure conditions:** Undebuggable distributed flows; missing audit evidence for sensitive actions.

**Next phase:** Phase 16 — Deployment / Infrastructure.

---

### Phase 16 — Deployment / Infrastructure

**Purpose:** Establish reproducible environments from local to staging.

**Inputs:** Deployment architecture; observability; hardened services.

**Activities:** Docker Compose local stack; CI pipelines; staging environment; infrastructure-as-code where justified.

**Deliverables:** Deployment docs; CI config; environment definitions

**Exit criteria:** Fresh environment deploy documented and repeatable; CI validates build+test on push.

**Validation:** Clean deploy run; rollback drill for at least one component.

**Coding policy:** Infra-as-code allowed.

**Change policy:** Environment definitions version-controlled.

**Failure conditions:** Manual-only deploy; secrets in repo; irreproducible environments.

**Next phase:** Phase 17 — Production Readiness Validation.

---

### Phase 17 — Production Readiness Validation

**Purpose:** Determine whether the platform meets production-readiness criteria—not marketing claims.

**Inputs:** All prior phase evidence.

**Activities:** Readiness review against NFR, security, observability, testing, deployment, and documentation gates.

**Deliverables:** Production Readiness Report; known limitations register

**Exit criteria:** Readiness checklist passed or explicit waivers documented with risk acceptance.

**Validation:** Independent review; scenario walkthroughs; recovery drills.

**Coding policy:** Yes — fixes allowed via controlled changes.

**Change policy:** Waivers require documented approval.

**Failure conditions:** Claiming production readiness without evidence; missing audit/security controls.

**Next phase:** Phase 18 — Portfolio / Interview Readiness.

---

### Phase 18 — Portfolio / Documentation / Interview Readiness

**Purpose:** Present the project credibly to hiring managers and senior engineers.

**Inputs:** Working system; architecture; test evidence; trade-off decisions.

**Activities:** README polish; architecture diagrams; demo script; interview prep pack; limitations and future work documented honestly.

**Deliverables:** Portfolio artifact pack; demo workflow; technical narrative

**Exit criteria:** Project explainable end-to-end with evidence; no undemonstrated claims in primary README.

**Validation:** Mock interview walkthrough; demo rehearsal; documentation link check.

**Coding policy:** Yes — polish allowed.

**Change policy:** Portfolio docs may evolve without changing frozen requirements.

**Failure conditions:** Misleading “production at Binance scale” claims; AI capabilities overstated beyond evaluation evidence.

**Next phase:** Ongoing maintenance or Version 2 expansion per product roadmap—not uncontrolled scope creep.

---

## 7. Documentation Dependency Graph

```text
Product Discovery (Vision)
        ↓
Product Scope / Personas / Principles / Success Metrics
        ↓
Business Requirements (BRS)
        ↓
Functional Domain Specification (FDS)
        ↓
Functional Requirements (FRS)
        ↓
Non-Functional Requirements (NFR)
        ↓
PRD (consolidation)
        ↓
System Architecture
        ↓
Data Architecture
        ↓
AI Architecture & Agent Specifications
        ↓
API / Event Contracts
        ↓
Frontend / UX Design
        ↓
Implementation Plan
        ↓
Application Code
        ↓
Testing → Hardening → Observability → Deployment → Production Validation → Portfolio
```

**Authoritative decision map:**

| Decision type | Authoritative document |
|---------------|------------------------|
| Strategic purpose and direction | Vision |
| Feature boundaries and releases | Product Scope + FDS release columns |
| Business intent and objectives | BRS |
| Domain capabilities and ownership | FDS |
| Detailed system behavior | FRS |
| Quality attributes | NFR |
| Consolidated product definition | PRD |
| Technical structure | System Architecture |
| Persistence model | Database Design |
| Agent behavior and AI contracts | AI Specifications |
| Service interfaces | API Specification |
| Operator experience rules | Frontend Design |
| Phase sequencing and gates | **This roadmap** |

If documents conflict, **do not silently pick a winner**. Raise a change control item (Section 19).

---

## 8. Document Ownership / Authority Model

| Document | Controls | Does not control |
|----------|----------|------------------|
| **Project Roadmap** | Phase order, gates, freeze policy, change process | Individual FR semantics |
| **Vision** | Why the product exists; strategic non-goals | API or schema design |
| **Product Scope** | In-scope capabilities and module boundaries | Detailed event payloads |
| **BRS** | Business requirements and objectives | Implementation technology |
| **FDS** | Domains, ownership, events, dependencies, release scope | Step-by-step workflows |
| **FRS** | Testable functional behavior | Infrastructure vendor selection |
| **NFR** | Quality attributes and constraints | Business strategy |
| **PRD** | Consolidated product definition for delivery | Low-level code structure |
| **Architecture** | Components, integration, cross-cutting design | Rewriting FR behavior |
| **Database Design** | Persistence, retention, indexing strategy | Domain lifecycle ownership |
| **AI Specifications** | Agents, tools, evaluation, guardrails | ALERT/INVEST/COMP/SEC ownership |
| **API Specification** | Endpoint and schema contracts | Authorization policy definition (AUTHZ owns policy) |
| **Frontend Design** | UX patterns and presentation | Alert/case/risk lifecycle logic |

**Precedence for conflicts:**

1. Frozen FRS behavior for delivered domains (highest for functional behavior)
2. FDS for domain ownership and event contracts
3. BRS for business intent
4. Vision/Scope for strategic boundaries
5. Architecture/NFR for quality and structure—**unless** they contradict 1–4, in which case change control is mandatory

---

## 9. Frozen vs Mutable Artifacts

### 9.1 Freeze rule

When a domain requirements delivery phase completes with validation and an explicit delivery commit:

- That domain’s FRS chapter becomes **controlled/frozen**.
- Matching FDS domain sections become **controlled/frozen** except for typo-level fixes via change control.
- Later phases **must not silently redefine** ownership, events, releases, or FR semantics.

### 9.2 Currently frozen domains (do not rewrite)

| Domain | Notes |
|--------|-------|
| CORE | MVP — frozen |
| AUTH | MVP — frozen |
| AUTHZ | MVP — frozen |
| USER | MVP — frozen |
| ORG | MVP — frozen |
| DASH | MVP — frozen |
| ALERT | MVP — frozen |
| RISK | MVP — frozen |
| INVEST | MVP — frozen |
| WALLET | Version 2 — frozen |
| COMP | MVP — frozen (delivered `0441af5`) |
| SEC | Version 2 — frozen (delivered `634d649`) |

**Do not invent additional requirements for these domains** during architecture or implementation planning except through explicit change control.

### 9.3 Mutable artifacts (until their phase exit)

- Vision (until Phase 1 exit)
- Product Scope / Personas / Principles (until Phase 2 exit)
- AI, REPORT, ADMIN, OPS FRS chapters (until Phase 3 domain delivery)
- NFR, PRD, architecture, database, AI specs, API, UX (until respective phase approval)

### 9.4 Implementation convenience is not a change request justification

Frozen requirements must not be edited to simplify coding, reduce service count, or merge domains.

---

## 10. Coding Gate

### 10.1 When may application development begin?

**Application development (production service implementation) begins only after the Application Development Gate is satisfied.**

Until then:

- Documentation, diagrams, ADRs, OpenAPI drafts, and throwaway spikes may be allowed only if explicitly scoped in Phase 11 and do not live in the main production codebase path.

### 10.2 Application Development Gate — minimum required approvals

| # | Required artifact | Approval meaning |
|---|-------------------|------------------|
| 1 | Product Vision | Phase 1 exit — validated and internally consistent |
| 2 | Product Scope | Phase 2 exit — reconciled with FDS/FRS |
| 3 | User Personas | Phase 2 exit — complete baseline |
| 4 | Functional Requirements | Phase 3 exit — all in-scope domains delivered |
| 5 | Non-Functional Requirements | Phase 4 exit — testable NFR baseline |
| 6 | PRD | Phase 5 exit — consolidated product definition |
| 7 | System Architecture | Phase 6 exit — domain-aligned architecture |
| 8 | Database Design | Phase 7 exit — ownership-aligned persistence plan |
| 9 | AI Architecture | Phase 8 exit — assistive agent contracts |
| 10 | API / Event Contracts | Phase 9 exit — contracts match FDS/FRS |
| 11 | Frontend Design | Phase 10 exit — primary workflows designed |
| 12 | Development Roadmap / Implementation Plan | Phase 11 exit — sequenced build plan |

**“Approved” means:**

- Phase exit criteria met
- Phase completion report produced (Section 18)
- No unresolved blockers
- Explicit gate sign-off recorded in the phase report (Reviewed By / Approved By fields may remain TBD until human review)

### 10.3 Current gate status

**Application Development Gate: SATISFIED**

**Phase 12 Application Development: AUTHORIZED** (begin at milestone **M0**).

| Field | Value |
|-------|-------|
| Decision record | [ApplicationDevelopmentGateDecision.md](ApplicationDevelopmentGateDecision.md) |
| Approver | Project Owner — approved |
| Date | 2026-09-11 |
| M0 kickoff | [Phase12M0Kickoff.md](Phase12M0Kickoff.md) |
| Stack | ADR-019 Accepted |

Historical note: Earlier drafts recorded NOT SATISFIED due to missing human approvals and incomplete design track; those blockers were cleared by Project Owner approval and Phases 8–11 technical exits.

---

## 11. Application Development Strategy

After the coding gate, implementation proceeds **domain-aligned**, not screen-aligned.

**Recommended build order (conceptual — finalize in Phase 11):**

1. **Repository structure and shared libraries** — contracts, common types, lint/test baselines
2. **CORE platform foundation** — configuration, feature flags, health, audit context, shared events
3. **AUTH + AUTHZ + USER + ORG** — identity, session, authorization, tenant scope
4. **Event infrastructure** — outbox/publisher/consumer patterns per approved architecture
5. **RISK engine** — scoring, rules, explanations (upstream of ALERT)
6. **ALERT** — operational alert lifecycle consuming RISK signals
7. **INVEST** — case lifecycle, evidence, workflow
8. **DASH** — presentation and work queues consuming upstream events
9. **COMP** — compliance workflows (MVP)
10. **WALLET** — wallet intelligence (V2)
11. **SEC** — security intelligence (V2)
12. **AI Platform services** — assistive agents, tool execution, evaluation hooks
13. **REPORT / ADMIN / OPS** — as scoped by approved PRD and remaining FRS delivery
14. **Frontend application** — workspace UI aligned to DASH boundaries
15. **Observability instrumentation** — continuous from step 2 onward, finalized in Phase 15
16. **Test harness and CI** — continuous from step 2 onward

This order respects established dependencies: AUTH/AUTHZ before domain services; RISK before ALERT; INVEST/COMP/SEC consume upstream context without owning foreign lifecycles.

---

## 12. AI Development Strategy

AI work follows a **contract-first, assistive-only** path:

```text
Requirements (FDS/FRS AI ownership rules)
        ↓
AI use cases (assistive workflows only)
        ↓
AI architecture (orchestration, tool boundaries)
        ↓
Agent specifications (prompts, tools, inputs/outputs)
        ↓
Retrieval design (if applicable)
        ↓
Evaluation framework (quality, safety, hallucination controls)
        ↓
Guardrails and human-in-the-loop checkpoints
        ↓
Implementation (AI Platform domain)
        ↓
Observability (latency, cost, tool failures, eval metrics)
```

**Rules:**

- AI Platform owns agent lifecycle and orchestration.
- Business domains (ALERT, INVEST, RISK, COMP, SEC, etc.) do **not** own agents per frozen FRS posture.
- No domain workflow may require AI to operate in MVP/V2 baselines unless explicitly approved in requirements.
- AI must not publish domain-owned events directly without going through the domain’s publication authority FR pattern.

---

## 13. Testing Strategy Roadmap

Testing is **continuous**, not a final-phase afterthought.

| When | Testing focus |
|------|---------------|
| Phase 3+ | Requirements testability review; acceptance criteria quality |
| Phase 9 | Contract tests planned for APIs and events |
| Phase 11 | Test strategy activated per service; CI test stages defined |
| Phase 12+ | Unit tests alongside service code |
| Phase 13 | Integration, API, event, database, and E2E tests |
| Phase 14 | Security, performance, resilience, regression tests |
| Phase 8/13+ | AI evaluation tests (offline + gated online eval) |
| Phase 10+ | Frontend component and workflow tests |

**Test types to cover:** unit, integration, contract, API, database, event, security, AI evaluation, frontend, E2E, performance, resilience, regression.

---

## 14. Security Strategy Roadmap

Security is embedded across phases:

| Phase | Security focus |
|-------|----------------|
| 1–2 | Threat landscape; persona-based abuse scenarios; non-goals |
| 3 | Domain authorization FRs; audit FRs; SEC/ AUTH / AUTHZ boundaries |
| 4 | Security NFRs; compliance constraints |
| 6 | Security architecture; trust boundaries; secrets strategy |
| 9 | API authn/authz integration; input validation standards |
| 12 | Secure coding; RBAC enforcement; audit logging implementation |
| 14 | OWASP testing; dependency scanning; penetration test findings |
| 15–16 | Security monitoring; incident runbooks |

Topics to address over time: authentication, authorization, RBAC, secrets management, encryption, input validation, audit logs, API security, rate limiting, threat detection (SEC domain), dependency scanning, container security, OWASP considerations.

---

## 15. Observability Strategy

| Signal | Purpose |
|--------|---------|
| **Logs** | Debugging, audit support, security investigations |
| **Metrics** | SLOs, throughput, error rates, queue depth |
| **Traces** | Cross-service workflow diagnosis |
| **Health checks** | Dependency readiness (CORE domain alignment) |
| **Dashboards** | Operator and engineering visibility |
| **Alerts** | SLO breaches, error budget burn, security signals |
| **Audit trails** | Privileged and sensitive action traceability |
| **AI telemetry** | Model latency, token/cost usage, tool failures |
| **Evaluation metrics** | AI quality regression detection |

Observability requirements finalize in Phase 15 but **instrumentation hooks** should be planned from Phase 6 onward.

---

## 16. Deployment Strategy

Progression (no premature production claims):

```text
Local developer environment
        ↓
Docker Compose (integrated local stack)
        ↓
CI validation (build, test, lint, security scans)
        ↓
Staging environment
        ↓
Production-like environment (scaled-down)
        ↓
Production readiness validation
        ↓
Optional production deployment (only after Phase 17 gate)
```

Kubernetes, cloud provider selection, and multi-region deployment are **decisions for Phase 11/16**, not assumptions for Phase 1.

---

## 17. Definition of Done

A phase is **not complete** when a file merely exists. Phase completion requires:

| Criterion | Required |
|-----------|----------|
| Deliverables exist in agreed locations | Yes |
| Content reviewed for internal consistency | Yes |
| Cross-domain consistency checked | Yes |
| Terminology aligned with FDS/BRS | Yes |
| Traceability complete (where applicable) | Yes |
| Scope leakage reviewed | Yes |
| Frozen domains protected | Yes |
| Validation performed per phase definition | Yes |
| Git diff reviewed (where changes occur) | Yes |
| Working tree clean after delivery commit (where applicable) | Yes |
| Phase completion report produced | Yes |
| Exit criteria explicitly marked pass/fail | Yes |

---

## 18. Phase Completion Report Standard

Every future phase must end with a report using this structure:

```markdown
# Sentinel AI — Phase X Completion Report

## Objective
## Inputs
## Work Completed
## Files Created/Modified
## Validation
## Cross-Domain Impact
## Scope Changes
## Open Issues
## Blockers
## Exit Criteria
## Git Status
## Verdict
```

**Verdict values:** `PASS`, `PASS WITH NON-BLOCKING NOTES`, `BLOCKED`.

COMP and SEC domain deliveries established the expected validation discipline; future phases must follow the same auditable pattern.

---

## 19. Change Control

If a later phase discovers an earlier decision is wrong:

1. **Identify the conflict** — cite documents and sections.
2. **Document the reason** — why change is needed.
3. **Identify impacted artifacts** — FDS, FRS, architecture, etc.
4. **Determine frozen impact** — if frozen domains affected, escalation required.
5. **Propose revision** — explicit diff intent, not silent edits.
6. **Validate cross-domain impact** — event contracts, dependencies, AI ownership.
7. **Update affected documents** — controlled commits only.
8. **Re-run relevant validation** — domain validation or phase gate checks.
9. **Record decision** — ADR if architectural; phase note if procedural.

**Never silently rewrite earlier decisions.**

---

## 20. Scope Control

This roadmap exists to prevent:

| Anti-pattern | Control |
|--------------|---------|
| Feature creep | Phase gates + FDS release columns |
| AI feature creep | Assistive-only rules; AI Platform ownership |
| Unnecessary microservices | Domain-aligned architecture review in Phase 6 |
| Unnecessary agents | AI agent catalog gate in Phase 8 |
| Premature Kubernetes | Deployment decisions deferred to Phase 16 |
| Premature cloud deployment | No production claims before Phase 17 |
| Vendor lock-in | Provider-neutral requirements; ADRs for choices |
| Cross-domain ownership leakage | Frozen FRS + validation checks |
| Implementation-driven requirements | Change control + freeze policy |
| Demo-only shortcuts | Definition of Done + production readiness gate |
| Fake scalability claims | NFR + performance validation required |
| Fake AI claims | Evaluation framework required |
| Fake production claims | Phase 17 readiness gate |

---

## 21. Portfolio Quality Gate

Before treating the project as portfolio-complete, aim to demonstrate:

- Architecture diagrams tied to real code structure
- Meaningful root README with honest status
- Clean, auditable Git history
- Meaningful automated test coverage
- CI/CD pipeline evidence
- Observability screenshots or exported dashboards
- Security documentation and test results
- API documentation generated from contracts
- AI evaluation results with limitations stated
- Deployment documentation with reproducible steps
- Screenshots or screen recordings of primary workflows
- Demo script for investigator/analyst/compliance/security paths
- Documented engineering trade-offs
- Known limitations and future roadmap

Avoid marketing language that exceeds demonstrated evidence.

---

## 22. Interview Readiness Gate

Prepare to explain technically:

- End-to-end system design and domain boundaries
- Why services/events are split the way they are
- Database and graph store trade-offs
- Event-driven design and contract enforcement
- AI architecture, retrieval, evaluation, and guardrails
- Security model (AUTH, AUTHZ, audit)
- Scalability approach and current measured limits
- Observability and incident response
- Failure handling and degradation modes
- Testing strategy and coverage philosophy
- Deployment approach and environment progression

The goal is **credible senior-engineer depth**, not surface-level demo polish.

---

## 23. Current Next Action

### Next phase

**Phase 1 — Product Discovery**

### Next concrete deliverable

**`docs/01-product/Vision.md`** — validation and reconciliation baseline

**Note:** `Vision.md` already exists (v0.5 Draft) but predates the current FDS v1.3 / FRS v1.9 requirements maturity. Phase 1 work is to **validate, reconcile, and elevate** Vision against the authoritative requirements baseline—not to ignore the existing document.

### Do not start in this roadmap phase

- Vision rewriting that contradicts frozen domain requirements
- PRD authoring
- Architecture implementation
- Application code
- New domain FR authoring (AI, REPORT, ADMIN, OPS) before Product Discovery exit unless explicitly reprioritized via change control

---

## 24. Current Status Summary

| Area | Status |
|------|--------|
| Repository governance | Complete — domain delivery discipline proven (COMP, SEC) |
| Master project roadmap | **This document — Draft v1.0** |
| COMP domain | Delivered — frozen (`0441af5`) |
| SEC domain | Delivered — frozen (`634d649`) |
| Other domain FRS (10 domains) | Delivered — frozen per FRS version history |
| Remaining domain FRS (AI, REPORT, ADMIN, OPS) | Pending |
| Product Discovery | **Next** |
| Product Vision | Exists — Draft v0.5; requires Phase 1 reconciliation |
| Product Scope | Exists — v1.0 Approved; requires reconciliation with FDS/FRS |
| User Personas | Skeleton — pending Phase 2 |
| Product Principles | Skeleton — pending Phase 2 |
| Business Requirements (BRS) | Exists — authoritative for BR/BO IDs |
| Functional Requirements (overall) | **Partially established** — 12/16 domains in FRS |
| Non-Functional Requirements | Skeleton — pending Phase 4 |
| PRD | Not started |
| System Architecture | Skeleton — pending Phase 6 |
| Database Design | Skeleton — pending Phase 7 |
| AI Specifications | Skeleton — pending Phase 8 |
| API Specification | Skeleton — pending Phase 9 |
| Frontend Design | Skeleton — pending Phase 10 |
| Application Development | **Not started** |
| Testing (implementation) | **Not started** |
| Deployment (implementation) | **Not started** |
| Production readiness | **Not claimed** |

---

## 25. Final Roadmap Verdict

Sentinel AI is currently in the **pre-implementation product definition stage**.

The repository has established **domain-level functional requirements** for twelve of sixteen FDS domains, with formal delivery, cross-domain validation, traceability, and event contract discipline demonstrated through COMP and SEC deliveries. Application development has **not** begun and must **not** begin until the Application Development Gate (Section 10) is satisfied.

**Immediate next step:** Phase 1 — Product Discovery, producing a validated and reconciled `docs/01-product/Vision.md`.

**Coding begins only after** Vision, Scope, Personas, complete Functional Requirements, NFR, PRD, System Architecture, Database Design, AI Architecture, API/Event Contracts, Frontend Design, and Implementation Plan are approved through their phase exit criteria.

---

## Appendix A — Related repository documents (reference only)

| Path | Role |
|------|------|
| `docs/README.md` | Documentation index |
| `docs/01-product/` | Product definition artifacts |
| `docs/02-requirements/FunctionalDomainSpecification.md` | FDS — domain capabilities |
| `docs/02-requirements/FunctionalRequirements.md` | FRS — detailed behavior |
| `docs/02-requirements/NonFunctionalRequirements.md` | NFR — pending completion |
| `docs/03-architecture/` through `docs/11-decisions/` | Design-phase skeletons |
| `docs/10-roadmap/` | Release/milestone placeholders — align to this master roadmap in Phase 11 |

---

## Appendix B — Revision history

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-09-03 | Product & Engineering Team | Initial master project roadmap and phase control document |
| 1.1 | 2026-09-03 | Product & Engineering Team | Appendix C — governance-track Phase 4 completion overlay |

---

## Appendix C — Governance Track Execution Overlay

This appendix records progress on the **documentation governance track** (Phases 1–4 as executed in product/engineering sessions). It does **not** renumber or replace Sections 5–12 phase definitions; it reconciles actual deliverables against the Application Development Gate.

### Governance track status

| Governance Phase | Focus | Status | Key deliverables |
|------------------|-------|--------|------------------|
| Phase 1 | Product Discovery | Complete (uncommitted) | Vision, Scope, Personas, Principles, ProductDiscovery |
| Phase 2 | NFR + Architecture Foundations | Complete (uncommitted) | NFR.md, architecture docs, ADR-001–008, Phase2Report |
| Phase 3 | API & Data Design | Complete (uncommitted) | APIInventory, EventContracts, partial OpenAPI, data docs, Phase3Report |
| **Phase 4** | **Contract Hardening & Implementation Foundations** | **Complete (uncommitted)** | Full MVP OpenAPI, APIContractGovernance, MessageBrokerArchitecture, SchemaRegistryGovernance, MigrationStrategy, InitialMigrationSpecifications (CORE/AUTH), ContractValidationCI design, ADR-015–018, Phase4Report |
| Phase 5 (recommended) | PRD consolidation + extended migrations | **Complete (uncommitted)** | PRD.md, DataRetention.md, event schemas, AUTHZ/USER/ORG migrations, Phase5Report |
| Phase 6 (recommended) | Operational data + events + UX foundations | **Complete (uncommitted)** | RISK/ALERT/INVEST migrations, event schemas, UX docs, Phase6Report |
| Phase 7 (recommended) | Remaining MVP migrations + retention/UX/PRD workflow | **Complete (uncommitted) — PASS WITH OPEN ITEMS** | COMP/AI/ADMIN/DASH migrations, RetentionJurisdictionGovernance, DashboardScreens, ComponentLibrary, GD-002/003, Phase7Report |
| Phase 8 (recommended) | AI architecture completion + PRD R0–R8 docs review | **Complete (uncommitted) — PASS WITH OPEN ITEMS** | AIArchitecture, AIAgents, AISecurityModel, PRDReviewRecord, RetentionDecisionMatrix, Phase8GateAssessment, Phase8Handoff, Phase8Report |
| Phase 9 (recommended) | API/Event formal exit review | **Complete (uncommitted) — PASS WITH OPEN ITEMS** | Phase9ContractExitMatrix, Phase9APIContractExitReport, Phase9GapRegister, Phase9Traceability, Phase9Handoff, Phase9Report; inventory arithmetic fix |
| Phase 10 (recommended) | Frontend / UX Design formal exit | **Complete (uncommitted) — PASS WITH OPEN ITEMS** | FrontendUXArchitecture, AIInteractionPatterns, UXSecuritySpecification, UXStateModel, expanded IA/Screens/Workflows/Components, Phase10Traceability, Phase10Handoff, Phase10Report |
| Phase 11 (recommended) | Implementation planning + Application Development Gate prep | **Complete (uncommitted) — PASS WITH OPEN ITEMS** | ImplementationPlan + full Phase 11 package; Phase11GateAssessment (gate still BLOCKED); Phase11Handoff; Phase11Report |
| Phase 12 (recommended) | Application Development | **AUTHORIZED** — Gate SATISFIED; begin **M0** | ApplicationDevelopmentGateDecision.md; Phase12M0Kickoff.md |

### Phase 4 exit summary

| Criterion | Met? |
|-----------|------|
| MVP OpenAPI 100% inventory coverage | Yes |
| API governance documented | Yes |
| Event/messaging architecture hardened | Yes |
| Schema governance documented | Yes |
| Message broker ADR | Yes (ADR-015) |
| P3-OQ-001/002/003 resolved | Yes (ADR-016–018) |
| BQ-4 documented (REPORT V2, non-MVP gate) | Yes — human sign-off pending |
| CORE/AUTH migration specs | Yes (logical) |
| CI contract validation design | Yes (design only) |
| MVP traceability | Yes |
| No application code | Yes |
| Frozen domains unchanged | Yes |

### Remaining blockers (post–Phase 4)

| Blocker | Classification |
|---------|----------------|
| Application Development Gate (Section 10) | **BLOCKING** — not satisfied |
| PRD consolidation (roadmap Phase 5) | **BLOCKING** for full gate |
| BQ-4 explicit product owner sign-off | NON-BLOCKING (governance recorded) |
| NFR-OQ-002 jurisdiction retention | NON-BLOCKING |
| V2 OpenAPI completion (21 ops) | NON-BLOCKING (V2) |
| Executable CI workflows | NON-BLOCKING until Phase 11 |

### Phase 5 exit summary

| Criterion | Met? |
|-----------|------|
| PRD consolidated (48 sections) | Yes (draft v0.1) |
| Data retention documented | Yes |
| Event JSON schemas formalized | Yes (MVP core + SEC V2 publish) |
| MVP data ownership hardened | Yes |
| AUTHZ/USER/ORG migration specs | Yes |
| API inventory ↔ OpenAPI reconciled | Yes (67 MVP incl. SSE) |
| BQ-4 accurately recorded | Yes — human sign-off pending |
| MVP traceability | Yes (Phase5Traceability.md) |
| Frozen domains unchanged | Yes |
| No application code | Yes |

### Phase 5 recommendation

Proceed with RISK/ALERT/INVEST migration specifications, formal PRD approval, BQ-4 human sign-off, and frontend design phase.

### Phase 6 exit summary

| Criterion | Met? |
|-----------|------|
| RISK/ALERT/INVEST migration specs | Yes |
| MVP event schemas (required set) | Yes |
| Event coverage matrix | Yes |
| Data retention governance strengthened | Yes |
| PRD approval record (no false approval) | Yes — status PENDING |
| UX foundations (docs/07-ui/) | Yes |
| Phase 6 traceability | Yes |
| Frozen domains unchanged | Yes |
| No application code | Yes |

### Phase 7 exit summary

| Criterion | Met? |
|-----------|------|
| COMP/AI/ADMIN/DASH migration specs | Yes (logical only) |
| Full MVP migration path 001–012 documented | Yes |
| Deferred event governance (GD-002) | Yes — schemas not invented |
| Retention jurisdiction framework (NFR-OQ-002) | Yes — durations still OPEN |
| DashboardScreens + ComponentLibrary | Yes (docs-only) |
| PRD formal review workflow | Yes — **FORMAL APPROVAL PENDING** |
| BQ-4 remains V2 / non-MVP gate | Yes — human sign-off pending |
| Phase 7 traceability | Yes |
| Frozen domains unchanged | Yes |
| No application code | Yes |

### Phase 7 open items

| Item | Classification |
|------|----------------|
| PRD formal signatures | PENDING HUMAN DECISION |
| BQ-4 product-owner sign-off | PENDING HUMAN DECISION |
| NFR-OQ-002 jurisdiction durations | OPEN |
| Implementation plan / coding gate prerequisites | BLOCKING for Application Development Gate |

### Phase 8 exit summary

| Criterion | Met? |
|-----------|------|
| AIArchitecture.md complete (draft) | Yes |
| MVP agents = AI-FR-001–009 only | Yes |
| AI security + evaluation docs | Yes |
| AI assistive-only validated | Yes |
| PRD R0–R8 documentation review | Yes — **PENDING HUMAN APPROVAL** |
| BQ-4 accurate (REPORT V2) | Yes — **PENDING HUMAN SIGN-OFF** |
| NFR-OQ-002 formal disposition | Yes — remains **PENDING COMPLIANCE / LEGAL** |
| Gate assessment explicit | Yes — gate **BLOCKED** |
| Phase 9/10/11 handoff | Yes |
| Frozen domains unchanged | Yes |
| No application code | Yes |

### Phase 8 open items

| Item | Classification |
|------|----------------|
| PRD formal signatures | PENDING HUMAN APPROVAL — BLOCKING for gate |
| BQ-4 product-owner sign-off | PENDING HUMAN DECISION |
| NFR-OQ-002 jurisdiction durations | PENDING COMPLIANCE / LEGAL |
| Implementation plan (Phase 11) | NOT STARTED — BLOCKING |
| Human Phase 8/9/10 exit approvals | PENDING HUMAN APPROVAL |

### Phase 8 recommendation

Proceed to Phase 9 formal API/event contract exit review per Phase8Handoff.md. Obtain human PRD/BQ-4 approvals in parallel. **Do not open Application Development Gate.**

**Application Development Gate: NOT SATISFIED** (unchanged).

### Phase 9 exit summary

| Criterion | Met? |
|-----------|------|
| Formal API inventory ↔ OpenAPI audit | Yes — **68/68 MVP** |
| OpenAPI structural validation (YAML parse, unique operationIds) | Yes |
| Event catalog ↔ JSON Schema coverage | Yes — **27/27** (GD-002 deferred) |
| SEC event lock | Yes — **PASS** |
| AI assistive API/event boundaries | Yes |
| Domain ownership / MVP·V2·V3 | Yes |
| DASH SSE (API-DASH-007) | Yes |
| Contract exit matrix + gap register | Yes |
| Human Phase 9 exit approval | **PENDING HUMAN APPROVAL** |
| Frozen FRS/FDS unchanged | Yes |
| No application code | Yes |

### Phase 9 open items

| Item | Classification |
|------|----------------|
| Human Phase 9 exit signatures | PENDING HUMAN APPROVAL — BLOCKING for gate item #10 |
| PRD / BQ-4 / NFR-OQ-002 | PENDING (unchanged from Phase 8) |
| V2 OpenAPI full expansion / AsyncAPI / GD-002 schemas | DEFERRED |
| Phase 10 UX formal exit | NOT STARTED |
| Implementation plan (Phase 11) | NOT STARTED — BLOCKING |

### Phase 9 recommendation

Proceed to **Phase 10 — Frontend / UX Design** formal exit review per Phase9Handoff.md. Obtain human Phase 9/PRD approvals in parallel. **Do not open Application Development Gate.**

**Application Development Gate: NOT SATISFIED** (unchanged).

### Phase 10 exit summary

| Criterion | Met? |
|-----------|------|
| Frontend UX architecture documented | Yes |
| MVP screens SCR-00–15 specified | Yes (16) |
| WF-1–8 fully specified; API IDs reconciled | Yes |
| Role-based IA / navigation | Yes |
| Component library expanded | Yes |
| AI UX patterns (assistive-only) | Yes |
| UX security + state model | Yes |
| Accessibility design targets (not certified) | Yes |
| V2 SEC/REPORT/OPS/WALLET excluded from MVP nav | Yes |
| Human Phase 10 exit approval | **PENDING HUMAN APPROVAL** |
| Frozen FRS/FDS unchanged | Yes |
| No application code | Yes |

### Phase 10 open items

| Item | Classification |
|------|----------------|
| Human Phase 10 exit signatures | PENDING HUMAN APPROVAL — BLOCKING for gate item #11 |
| COMP list GET / global search API | NON-BLOCKING gaps — do not invent |
| PRD / BQ-4 / NFR-OQ-002 | PENDING (unchanged) |
| Implementation plan (Phase 11) | NOT STARTED — BLOCKING |

### Phase 10 recommendation

Proceed to **Phase 11 — Development Roadmap & Implementation Planning** per Phase10Handoff.md. Obtain human Phase 10/PRD approvals in parallel. **Do not open Application Development Gate.**

**Application Development Gate: NOT SATISFIED** (unchanged).

### Phase 11 exit summary

| Criterion | Met? |
|-----------|------|
| Implementation plan package (docs/08-development) | Yes |
| Sequenced milestones M0–M12 | Yes |
| Deployable blueprint (3–6) | Yes |
| Backend/AI/Frontend/Data/Event/Security/Test/Obs/DevOps plans | Yes |
| Implementation traceability + dependencies + risks | Yes |
| First milestone entry/exit criteria | Yes (M0/M1 in Handoff) |
| Human Phase 11 exit approval | **PENDING HUMAN APPROVAL** |
| Application Development Gate §10 | **NOT SATISFIED** |
| Frozen FRS/FDS unchanged | Yes |
| No application code | Yes |

### Phase 11 open items

| Item | Classification |
|------|----------------|
| Human approvals (PRD, Phases 8–11 exits, gate meeting) | PENDING HUMAN APPROVAL — BLOCKING |
| Stack implementation ADRs | PENDING (recommended) |
| BQ-4 / NFR-OQ-002 | PENDING (soft) |
| COMP list API / search API gaps | NON-BLOCKING |
| Phase 12 coding | **BLOCKED** |

### Phase 11 recommendation

Hold **Application Development Gate review** with human sign-offs. Until §10 is satisfied, **do not start Phase 12 application development.** Use Phase11Handoff.md when authorized.

**Application Development Gate: NOT SATISFIED** (unchanged).
