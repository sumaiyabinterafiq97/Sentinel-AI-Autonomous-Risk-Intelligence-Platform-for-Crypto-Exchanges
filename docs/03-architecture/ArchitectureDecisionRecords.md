# Architecture Decision Records

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Architecture Decision Records (ADR) |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 2 |
| Owner | Architecture Team |
| Last Updated | 2026-09-03 |

---

## Purpose

Record significant architectural decisions made during Phase 2 and prior governance. Each ADR follows:

- **Status:** Proposed | Accepted | Deprecated
- **Context:** Problem and forces
- **Decision:** What was decided
- **Rationale:** Why
- **Consequences:** Positive and negative
- **Alternatives considered**

---

## ADR Index

| ID | Title | Status |
|----|-------|--------|
| ADR-001 | Domain-driven service boundaries | Accepted |
| ADR-002 | AI remains assistive | Accepted |
| ADR-003 | Deterministic risk path independent of AI | Accepted |
| ADR-004 | Event contract governance | Accepted |
| ADR-005 | Data ownership by domain | Accepted |
| ADR-006 | Human approval for consequential actions | Accepted |
| ADR-007 | Vendor-neutral architecture | Accepted |
| ADR-008 | Graceful degradation strategy | Accepted |
| ADR-009 | REST contract-first API standard | Accepted (Phase 3) |
| ADR-010 | PostgreSQL schema-per-domain | Accepted (Phase 3) |
| ADR-011 | Neo4j as derived graph projection (V2) | Accepted (Phase 3) |
| ADR-012 | Redis non-authoritative cache only | Accepted (Phase 3) |
| ADR-013 | pgvector for AI retrieval embeddings | Accepted (Phase 3) |
| ADR-014 | Cursor pagination for operational APIs | Accepted (Phase 3) |
| ADR-015 | Message broker pattern (outbox + durable log) | Accepted (Phase 4) |
| ADR-016 | DASH real-time transport (SSE MVP) | Accepted (Phase 4) |
| ADR-017 | DASH BFF vs direct domain calls | Accepted (Phase 4) |
| ADR-018 | Default data retention targets | Accepted (Phase 4) |
| ADR-019 | MVP implementation technology stack | Accepted (Gate / Phase 12) |

---

## ADR-001 — Domain-Driven Service Boundaries

**Status:** Accepted (Phase 2)

**Context:** Sentinel AI has 16 functional domains with explicit ownership in FDS. Implementation must map to deployable services without duplicating lifecycles or creating ambiguous ownership.

**Decision:** Align service boundaries to FDS domains as the primary decomposition. Initial deployment may co-locate domains (modular monolith or small cluster) but **logical boundaries remain domain-aligned**. Split into separate deployables when scaling, security, or team ownership justify independence.

**Rationale:** FDS/FRS already define events, data ownership, and dependencies. Domain-driven boundaries minimize cross-team conflicts and support Phase 1 ownership decisions.

**Consequences:**
- (+) Clear ownership, traceability to FRs
- (+) Independent evolution per domain
- (−) More integration contracts to maintain
- (−) Potential operational overhead if over-fragmented

**Alternatives considered:**
- *Technical-layer decomposition* (API layer, data layer only) — rejected; obscures business ownership
- *One monolith forever* — rejected; limits scaling and failure isolation for exchange-grade goals

---

## ADR-002 — AI Remains Assistive

**Status:** Accepted (Phase 2; reaffirms Phase 1 PD-07)

**Context:** Product name implies AI autonomy. Frozen FRs assign lifecycle ownership to business domains. Regulatory and operational accountability require human governance.

**Decision:** AI Platform provides recommendations, explanations, retrieval, and summaries. AI does **not** own alert, case, compliance, or enforcement lifecycles unless future FRs explicitly authorize.

**Rationale:** Product Principles, frozen ALERT/RISK/INVEST/COMP boundaries, and enterprise accountability requirements.

**Consequences:**
- (+) Auditable human decisions
- (+) AI outage does not halt core operations
- (−) Less "autonomous" marketing alignment
- (−) More analyst interaction required

**Alternatives considered:**
- *Fully autonomous fraud blocking* — rejected; not supported by FRs, unrealistic claims

---

## ADR-003 — Deterministic Risk Path Independent of AI

**Status:** Accepted (Phase 2)

**Context:** LLM latency is variable (seconds). Risk scoring must feed ALERT workflows within strict bounds (NFR-PERF-001).

**Decision:** RISK critical path (ingest → score → publish `RiskCalculated`/`HighRiskDetected`) SHALL NOT depend on AI runtime. AI may enrich explanations asynchronously or on demand via separate assistive path.

**Rationale:** NFR-PERF-006, FDS RISK boundary ("critical scoring is not mandatory-AI-dependent"), ALERT dependency on timely risk events.

**Consequences:**
- (+) Predictable risk pipeline latency
- (+) AI failures isolated from scoring
- (−) Explanations may lag scores unless pre-computed

**Alternatives considered:**
- *LLM-in-critical-path scoring* — rejected; violates latency and reliability requirements

---

## ADR-004 — Event Contract Governance

**Status:** Accepted (Phase 2)

**Context:** Multiple domains communicate via events defined in FDS. Ad-hoc events cause integration failures and frozen-domain violations.

**Decision:** FDS publish/consume matrices and domain FR event contracts are **frozen governance**. New events require FDS/FR change control. Event envelope and processing semantics follow [EventArchitecture.md](EventArchitecture.md).

**Rationale:** Twelve domains already delivered with locked event contracts (e.g., RISK → ALERT MVP events).

**Consequences:**
- (+) Predictable integration
- (+) Consumer idempotency and evolution rules
- (−) Slower event schema changes

**Alternatives considered:**
- *Ad-hoc per-service events* — rejected; breaks cross-domain consistency

---

## ADR-005 — Data Ownership by Domain

**Status:** Accepted (Phase 2)

**Context:** Shared databases across domains create coupling and ambiguous source of truth.

**Decision:** Each domain owns its authoritative data store logically. Cross-domain access via APIs and events only. No shared mutable tables across domain boundaries without explicit CORE platform exception (audit, config).

**Rationale:** AP-10, FDS data ownership sections, microservice best practice adapted to modular architecture.

**Consequences:**
- (+) Clear source of truth
- (+) Independent schema evolution
- (−) Join queries require API composition or read models

**Alternatives considered:**
- *Single shared operational database* — rejected for long-term; acceptable only as initial co-location with schema separation

---

## ADR-006 — Human Approval for Consequential Actions

**Status:** Accepted (Phase 2)

**Context:** Compliance approvals, investigation closure, alert disposition on high-severity items, and enforcement recommendations have regulatory and operational impact.

**Decision:** Consequential actions require explicit authorized human action in the owning domain UI/API workflow. AI recommendations are displayed as suggestions with provenance—not auto-applied.

**Rationale:** NFR-AI-002, COMP/INVEST/ALERT frozen FRs, Personas non-automation boundaries.

**Consequences:**
- (+) Regulatory defensibility
- (−) Cannot fully automate analyst workflows

**Alternatives considered:**
- *AI auto-close for low-risk alerts* — may be future policy-driven feature; not MVP without explicit FR

---

## ADR-007 — Vendor-Neutral Architecture

**Status:** Accepted (Phase 2)

**Context:** Phase 2 governance prohibits mandating Kafka, PostgreSQL, OpenAI, etc. in product/requirements documents.

**Decision:** Architecture documents describe capabilities (message delivery, relational persistence, model inference). Technology selections recorded in implementation-phase ADRs after evaluation.

**Rationale:** AP-15, Phase 1 PD-09, FDS "No Technology Prescriptions."

**Consequences:**
- (+) Flexibility in implementation
- (−) Delayed specific tooling decisions

**Alternatives considered:**
- *Early stack lock-in* — rejected; premature for current phase

---

## ADR-008 — Graceful Degradation Strategy

**Status:** Accepted (Phase 2)

**Context:** Optional subsystems (AI, cache, graph, vector search, external providers) may fail independently.

**Decision:** Mandatory workflows degrade predictably: show explicit degraded mode, fall back to authoritative sources, never bypass AUTHZ, never silently drop audit. Optional features fail without blocking critical paths.

**Rationale:** NFR-RES-001, AP-09, AI availability NFRs.

**Consequences:**
- (+) Higher platform resilience
- (−) UX complexity for degraded states

**Alternatives considered:**
- *Fail entire platform if AI down* — rejected

---

## Future ADRs (Not Yet Authored)

| Topic | Trigger |
|-------|---------|
| LLM provider strategy | AI implementation (M8) |
| Message broker product selection | Event infra (M3) |
| API gateway product selection | Edge hardening |
| Dedicated vector database | Scale exceeds pgvector targets |

---

## ADR-009 — REST Contract-First API Standard

**Status:** Accepted (Phase 3)

**Context:** Multiple domains expose HTTP interfaces to DASH and external consumers. Contract-first design required per AP-02.

**Decision:** REST/JSON with OpenAPI 3.0 as authoritative contract. URL version prefix `/v1`. Domain-owned resource paths.

**Rationale:** Team familiarity, tooling maturity, alignment with FRS HTTP-oriented workflows, OpenAPI testability.

**Consequences:** (+) Clear contracts; (−) Aggregation in DASH may require multiple calls or BFF layer.

**Alternatives:** GraphQL — rejected for MVP complexity and caching/event alignment overhead.

---

## ADR-010 — PostgreSQL Schema-per-Domain

**Status:** Accepted (Phase 3)

**Decision:** Single PostgreSQL cluster with separate schema per domain for MVP; logical references only (no cross-schema FK).

**Rationale:** ADR-005 data ownership; operational simplicity vs full database-per-service.

**Alternatives:** Single shared schema — rejected (ownership blur); separate clusters per domain — deferred as over-engineering for MVP.

---

## ADR-011 — Neo4j as Derived Graph Projection (V2)

**Status:** Accepted (Phase 3)

**Decision:** Neo4j stores wallet/investigation graph projections only; PostgreSQL remains source of truth.

**Rationale:** Relationship queries for WALLET V2; rebuildable from events.

**Alternatives:** PostgreSQL recursive CTE only — acceptable fallback if Neo4j deferred; Neo4j as SoT — rejected.

---

## ADR-012 — Redis Non-Authoritative Cache Only

**Status:** Accepted (Phase 3)

**Decision:** Redis for cache, rate limits, idempotency keys only—never sole business record store.

**Rationale:** NFR-RES-002; failure tolerance.

---

## ADR-013 — pgvector for AI Retrieval Embeddings

**Status:** Accepted (Phase 3)

**Decision:** pgvector in `ai` schema for Retrieval Agent embeddings; not authoritative for business facts.

**Rationale:** Operational simplicity vs separate vector DB; sufficient for MVP/V2 scale targets.

**Alternatives:** Dedicated vector database — future ADR if scale requires.

---

## ADR-014 — Cursor Pagination for Operational APIs

**Status:** Accepted (Phase 3)

**Decision:** Cursor-based pagination for alerts, cases, assessments; offset allowed for low-volume admin lists only.

**Rationale:** Stable performance on high-volume alert queues (ALERT).

---

## ADR-015 — Message Broker Pattern (Transactional Outbox + Durable Log)

**Status:** Accepted (Phase 4)

**Context:** Sentinel AI is event-driven across 16 domains. MVP requires at-least-once delivery, consumer groups, replay for recovery, schema governance, and local development suitability. NFRs require durability (NFR-REL-001), observability (NFR-OBS-001), and tenant isolation in event metadata.

**Decision:** Adopt **Transactional Outbox + Durable Event Log** as the canonical messaging pattern. Select a **log-oriented broker** (Kafka-compatible or Apache Kafka) at implementation time, subject to Phase 11 infrastructure planning. Do not bind contracts to broker-specific configuration in Phase 4.

**Options evaluated:**

| Option | Throughput | Durability | Ordering | Replay | Consumer groups | Local dev | Schema gov | Ops complexity | Fit |
|--------|------------|------------|----------|--------|-----------------|-----------|------------|----------------|-----|
| **A. Log-oriented (Kafka / Redpanda / compatible)** | High | Strong | Per-partition | Native | Native | Good (containers) | Mature (Schema Registry ecosystem) | Medium–High | **Selected pattern** |
| B. RabbitMQ (AMQP) | Medium | Strong (with DLX) | Queue-level | Limited | Competing consumers | Good | External registry | Medium | Rejected for MVP replay/retention needs |
| C. Apache Pulsar | High | Strong | Flexible | Native | Native | Heavier | Built-in | High | Rejected — ops complexity vs team size |
| D. NATS JetStream | Medium–High | Configurable | Stream-level | Supported | Queue groups | Excellent | External | Low–Medium | Deferred — smaller ecosystem for schema governance |
| E. PostgreSQL-only (LISTEN/NOTIFY + polling) | Low | DB-bound | Weak | Manual | Manual | Excellent | N/A | Low | Rejected — insufficient for multi-domain fan-out at scale |

**Rationale:**

- FDS event matrix requires many publishers and consumers with at-least-once semantics and DLQ handling (EventContracts.md).
- Outbox pattern preserves **DB commit before publish** invariant (AP-07, EventArchitecture.md).
- Log retention supports audit reconstruction (NFR-AUD-002) and consumer replay after deployment.
- Kafka-compatible ecosystem aligns with schema registry tooling without mandating a vendor in Phase 4.

**Consequences:**

- (+) Clear producer/consumer scaling model; replay and retention policies implementable
- (+) Schema Registry governance (SchemaRegistryGovernance.md) maps naturally
- (−) Operational overhead vs simpler queues — mitigated by managed offerings in production
- (−) Exact broker product deferred to Phase 11 — documented as implementation choice, not contract change

**Reconsideration triggers:** Sustained MVP traffic below 100 events/sec with no replay requirement; managed Pulsar offering simplifies ops; regulatory mandate for alternative messaging.

**Related:** [MessageBrokerArchitecture.md](../06-api/MessageBrokerArchitecture.md), [SchemaRegistryGovernance.md](../06-api/SchemaRegistryGovernance.md)

---

## ADR-016 — DASH Real-Time Transport (P3-OQ-001)

**Status:** Accepted (Phase 4)

**Context:** DASH must refresh workspaces, work queues, and alert summaries without owning upstream lifecycles (DASH-FR-011, DASH-FR-003). Operators need near-real-time visibility; NFRs require graceful degradation (NFR-RES-003).

**Problem:** Choose transport for MVP browser clients: WebSocket, Server-Sent Events (SSE), or polling.

**Alternatives:**

| Option | Pros | Cons |
|--------|------|------|
| **SSE** | Unidirectional server→client fits event-driven refresh; simpler than WS; HTTP-friendly through proxies; auto-reconnect | No client→server over same channel |
| WebSocket | Bidirectional; single connection | Higher complexity; connection state; overkill for MVP refresh-only |
| Polling | Simplest | Higher load; stale UX; poor at scale |

**Decision:** **SSE for MVP** DASH workspace and queue refresh channels. Polling as explicit fallback when SSE unavailable. WebSocket deferred to V2 if bidirectional collaboration (live cursors, multi-user case editing) is approved.

**Rationale:** MVP DASH refresh is predominantly server-push of read-model updates after domain events. SSE aligns with BFF aggregation (ADR-017), standard HTTP observability, and assistive-only AI updates without opening bidirectional attack surface.

**Consequences:**

- (+) Lower implementation risk for MVP; aligns with HTTP infrastructure
- (−) Bidirectional features require V2 transport review
- API: `GET /v1/dash/subscriptions/{channel}` returns SSE stream (documented in OpenAPI as MVP)

**Future reconsideration:** V2 collaborative investigation features; operator feedback requiring sub-second bidirectional signaling.

---

## ADR-017 — BFF vs Direct Multi-Domain Client Calls (P3-OQ-002)

**Status:** Accepted (Phase 4)

**Context:** DASH aggregates ALERT, INVEST, RISK, COMP, and AI read models. Browser clients need consistent auth, tenant scope, and correlation. Service-to-service integrations require direct domain APIs.

**Problem:** Should all clients call domain services directly, or introduce a Backend-for-Frontend (BFF)?

**Alternatives:**

| Option | Pros | Cons |
|--------|------|------|
| **Hybrid: DASH BFF + direct S2S** | Browser gets aggregated, scoped APIs; domains remain authoritative; S2S avoids BFF hop | Two client patterns to document |
| Universal BFF | Single client entry | Bottleneck; BFF owns orchestration risk |
| Direct-only | Fewest layers | Browser N+1 calls; token scope leakage risk; CORS complexity |

**Decision:** **DASH acts as BFF for browser and human-operator clients** in MVP. **Service-to-service and batch integrators call domain APIs directly** with service credentials and AUTHZ evaluation per request.

**Rationale:** Preserves domain ownership (AP-06); reduces browser fan-out; centralizes session/tenant context for DASH-FR-001–003; ADMIN and AI services already use direct domain APIs per FRS.

**Consequences:**

- (+) Clear boundary: DASH = presentation + aggregation, not lifecycle owner
- (−) BFF must not cache authoritative state beyond read models
- OpenAPI tags distinguish `DASH` (BFF) vs domain tags

**Future reconsideration:** Mobile native apps; third-party API marketplace requiring unified external API gateway.

---

## ADR-018 — Default Data Retention Targets (P3-OQ-003)

**Status:** Accepted (Phase 4) — jurisdiction overrides remain open (NFR-OQ-002)

**Context:** NFR-PRIV-002 requires data retention limits for MVP. FDS assigns data ownership per domain. Compliance (COMP) and security (SEC V2) have distinct evidence retention needs. Exact jurisdictional periods are stakeholder-dependent.

**Problem:** Define implementable default retention targets without contradicting frozen FRs or claiming legal approval.

**Decision:** Adopt **tiered default retention** for implementation planning. **Jurisdiction-specific schedules override defaults** via COMP/ADMIN configuration when approved by compliance stakeholders.

| Data class | Default retention | Archive | Notes |
|------------|-------------------|---------|-------|
| Operational telemetry (metrics, traces) | 90 days | Optional cold storage | NFR-OBS-002 alignment |
| Application logs (non-PII) | 180 days | 1 year cold | Security investigation support |
| Session/auth audit (AUTH, CORE audit_records) | 2 years | 7 years cold | NFR-AUD-001; regulatory common baseline |
| Risk assessments & rule hits (RISK) | 2 years online | 5 years archive | Investigation lookback |
| Alerts (ALERT) | 2 years online | 5 years archive | Operational + audit |
| Investigation cases & evidence metadata (INVEST) | 7 years | Legal hold extends | Case lifecycle; evidence blobs follow classification |
| Compliance reviews (COMP) | 7 years | Legal hold extends | KYC/AML evidence patterns |
| AI recommendations & prompts (AI) | 1 year | 2 years archive | Assistive audit; no training reuse without policy |
| Cache/ephemeral (Redis) | TTL-based (minutes–hours) | None | Non-authoritative per ADR-012 |
| Graph projections (Neo4j) | Rebuild from source | N/A | Derived; retention follows source domains |
| V2: SEC threat records | 2 years online | 5 years archive | SEC-FR scope |
| V2: REPORT artifacts | 7 years | Legal hold | REPORT V2 only |

**Rationale:** Provides migration and implementation targets; satisfies NFR-PRIV-002 testability; leaves NFR-OQ-002 open for jurisdiction tables without blocking schema design.

**Consequences:**

- (+) InitialMigrationSpecifications and MigrationStrategy can reference retention columns/partitioning
- (−) Production deployment requires compliance sign-off on override matrix
- DataRetention.md skeleton should be expanded in Phase 7 exit, not rewritten here

**Future reconsideration:** New jurisdiction onboarding; SEC V2 activation; regulatory audit findings.

---

## ADR-019 — MVP Implementation Technology Stack

**Status:** Accepted (2026-09-11); **frontend row amended 2026-09-12** (GD-007 / P11-OQ-STACK-002 closed)

**Context:** ADR-007 requires vendor-neutral product requirements and delayed stack lock until implementation. Phase 11 recorded Java/Spring, Python/FastAPI, and React/TypeScript as **candidates** (P11-OQ-STACK-*). Application Development Gate is satisfied; M0 requires an explicit implementation stack ADR.

**Decision:** For MVP Phase 12 implementation, adopt the following stack direction:

| Layer | Selection |
|-------|-----------|
| Backend services | Java 21, Spring Boot 3, Spring Security, Spring Data JPA/Hibernate, Gradle |
| AI service | Python 3.11+, FastAPI; agent orchestration (LangGraph or equivalent behind interface) |
| Frontend | React, TypeScript, Vite, Tailwind, native `fetch` (DASH BFF client) |
| Data | PostgreSQL (schema-per-domain), Redis (non-authoritative), pgvector (AI) |
| Events | Transactional outbox + durable log (ADR-015); broker product remains capability-level until ops ADR |
| Observability direction | OpenTelemetry; Prometheus/Grafana (config in DevOps milestones) |
| Local/CI direction | Docker, Docker Compose, GitHub Actions, NGINX edge (introduced in M0+ as planned — not prematurely for V2 cloud lock-in) |

**Neo4j:** Remains **V2** derived graph (ADR-011) — not MVP mandatory runtime.

**Amendment (2026-09-12):** The original frontend cell also named TanStack Query, React Hook Form, and Zod as Phase 11 **candidates**. M0–M11 did not implement them. **MVP does not adopt those libraries.** Reconsideration requires a follow-on ADR, not a silent dependency add. Backend/AI/data rows are unchanged.

**Rationale:** Aligns with Phase 11 ImplementationPlan; preserves ADR-007 for product docs; enables M0 scaffolding without inventing requirements. The amendment matches the shipped SPA and closes P11-OQ-STACK-002 without speculative client libraries.

**Consequences:**

- (+) Clear M0 repo layout
- (−) Provider-specific LLM/broker still need follow-on ADRs when selected
- Product FRS/FDS remain technology-agnostic
- MVP forms and server state use platform `fetch` + React state, not TanStack Query / RHF / Zod

**Alternatives considered:** Continue indefinite candidate status — rejected once gate opened. Adopting TanStack/RHF/Zod in M11 “to match the original cell” — rejected (GD-007); would be speculative stack expansion.

**Authority:** Project Owner gate approval; Phase 11 plans; ADR-007 process; GD-007.

---

## Related Documents

- [ArchitecturePrinciples.md](ArchitecturePrinciples.md)
- [EventArchitecture.md](EventArchitecture.md)
- [MessageBrokerArchitecture.md](../06-api/MessageBrokerArchitecture.md)
- [GovernanceDecisions.md](../00-project/GovernanceDecisions.md)
- [ImplementationPlan.md](../08-development/ImplementationPlan.md)
- [ProductDiscovery.md](../01-product/ProductDiscovery.md) — Phase 1 decisions
