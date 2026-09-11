# Non-Functional Requirements

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Non-Functional Requirements (NFR) |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 2 |
| Owner | Product & Engineering Team |
| Last Updated | 2026-09-03 |
| Authority | Quality attributes and operational constraints; subordinate to FRS for functional behavior |

---

## Revision History

| Version | Date | Author | Notes |
|---------|------|--------|-------|
| 0.1 | 2026-07-25 | — | Initial skeleton |
| 1.0 | 2026-09-03 | Architecture Team | Phase 2 NFR baseline — 20 categories, measurable targets |

---

## Purpose

Define quality attributes, operational constraints, and measurable targets for Sentinel AI. NFRs complement functional requirements (FRS) and architecture foundations.

**Terminology:**

| Term | Meaning |
|------|---------|
| **Target** | Initial design/acceptance objective for simulation or first release |
| **Design objective** | Architectural goal guiding implementation choices |
| **Acceptance threshold** | Minimum acceptable value for release gate |
| **Future target** | Post-MVP improvement goal |

Targets are **not** claims of achieved production capability unless explicitly validated later.

---

## Related Documents

| Document | Relationship |
|----------|--------------|
| [FunctionalRequirements.md](FunctionalRequirements.md) | Functional behavior |
| [FunctionalDomainSpecification.md](FunctionalDomainSpecification.md) | Domain ownership |
| [ProductDiscovery.md](../01-product/ProductDiscovery.md) | Product success metric targets |
| [ArchitecturePrinciples.md](../03-architecture/ArchitecturePrinciples.md) | Architectural constraints |
| [SecurityArchitecture.md](../03-architecture/SecurityArchitecture.md) | Security design |
| [ObservabilityArchitecture.md](../03-architecture/ObservabilityArchitecture.md) | Telemetry design |

---

## Requirements Conventions

Each NFR uses:

| Field | Description |
|-------|-------------|
| **NFR ID** | Unique identifier (`NFR-<category>-<nnn>`) |
| **Category** | Quality attribute category |
| **Requirement** | Observable constraint |
| **Rationale** | Business or engineering justification |
| **Measurement method** | How compliance is measured |
| **Target** | Initial target (simulation/first release) |
| **Priority** | Critical / High / Medium / Low |
| **Release** | MVP / Version 2 / All |
| **Verification** | Test, monitoring, audit, or review method |

---

## NFR Summary Index

| ID | Category | Title | Release |
|----|----------|-------|---------|
| NFR-PERF-001 | Performance | Transaction ingest-to-risk evaluation latency | MVP |
| NFR-PERF-002 | Performance | Synchronous API read latency (P95) | MVP |
| NFR-PERF-003 | Performance | Dashboard workspace load latency | MVP |
| NFR-PERF-004 | Performance | Investigation case retrieval latency | MVP |
| NFR-PERF-005 | Performance | Search and discovery latency | MVP |
| NFR-PERF-006 | Performance | AI explanation latency (assistive path) | MVP |
| NFR-PERF-007 | Performance | Report generation latency | V2 |
| NFR-PERF-008 | Performance | Authentication operation latency | MVP |
| NFR-PERF-009 | Performance | Event propagation latency (domain-to-domain) | MVP |
| NFR-SCAL-001 | Scalability | Transaction processing throughput | MVP |
| NFR-SCAL-002 | Scalability | Concurrent analyst sessions | MVP |
| NFR-SCAL-003 | Scalability | Horizontal domain scaling | All |
| NFR-AVAIL-001 | Availability | Platform availability (MVP) | MVP |
| NFR-AVAIL-002 | Availability | Critical path availability (deterministic) | MVP |
| NFR-AVAIL-003 | Availability | AI assistive availability (degraded OK) | MVP |
| NFR-REL-001 | Reliability | Event processing success rate | MVP |
| NFR-REL-002 | Reliability | Data durability for authoritative records | MVP |
| NFR-RES-001 | Resilience | Graceful degradation when AI unavailable | MVP |
| NFR-RES-002 | Resilience | Graceful degradation when cache unavailable | MVP |
| NFR-RES-003 | Resilience | Event broker failure behavior | MVP |
| NFR-RES-004 | Resilience | Duplicate and out-of-order event handling | MVP |
| NFR-SEC-001 | Security | Authentication strength | MVP |
| NFR-SEC-002 | Security | Authorization enforcement (RBAC) | MVP |
| NFR-SEC-003 | Security | Encryption in transit | MVP |
| NFR-SEC-004 | Security | Encryption at rest (sensitive data) | MVP |
| NFR-SEC-005 | Security | Secret management | MVP |
| NFR-SEC-006 | Security | API security and rate limiting | MVP |
| NFR-SEC-007 | Security | Input validation | MVP |
| NFR-SEC-008 | Security | Service-to-service authentication | MVP |
| NFR-SEC-009 | Security | AI prompt injection resistance | MVP |
| NFR-SEC-010 | Security | AI tool authorization | MVP |
| NFR-PRIV-001 | Privacy | PII minimization | MVP |
| NFR-PRIV-002 | Privacy | Data retention limits | MVP |
| NFR-PRIV-003 | Privacy | Data classification enforcement | MVP |
| NFR-AUD-001 | Auditability | Sensitive action audit completeness | MVP |
| NFR-AUD-002 | Auditability | Decision provenance reconstruction | MVP |
| NFR-AUD-003 | Auditability | Configuration change audit | MVP |
| NFR-OBS-001 | Observability | Structured logging coverage | MVP |
| NFR-OBS-002 | Observability | Distributed tracing coverage | MVP |
| NFR-OBS-003 | Observability | Correlation ID propagation | MVP |
| NFR-OBS-004 | Observability | Health and readiness endpoints | MVP |
| NFR-OBS-005 | Observability | SLO definition for critical paths | MVP |
| NFR-MAINT-001 | Maintainability | Modular domain deployability | All |
| NFR-TEST-001 | Testability | Requirement-to-test traceability | All |
| NFR-A11Y-001 | Accessibility | WCAG-oriented workspace baseline | MVP |
| NFR-INT-001 | Data integrity | Authoritative source-of-truth per domain | MVP |
| NFR-INT-002 | Data integrity | Idempotent event processing | MVP |
| NFR-DR-001 | Disaster recovery | RPO/RTO targets (simulation) | MVP |
| NFR-AI-001 | AI reliability | AI fallback without blocking core workflows | MVP |
| NFR-AI-002 | AI safety | Human approval for consequential recommendations | MVP |
| NFR-AI-003 | AI evaluation | Evaluation pass rate target | V2 |
| NFR-AI-004 | AI observability | Model/prompt version tracking | MVP |
| NFR-COST-001 | Cost awareness | AI cost monitoring | MVP |
| NFR-OPS-001 | Operational support | Incident diagnosability | V2 |
| NFR-CFG-001 | Configuration | Environment-separated configuration | MVP |

---

## 1. Performance

### NFR-PERF-001 — Transaction Ingest-to-Risk Evaluation Latency

| Field | Value |
|-------|-------|
| Category | Performance |
| Requirement | Deterministic risk evaluation for a single eligible transaction shall complete within defined latency bounds on the **critical path**, independent of AI assistive processing. |
| Rationale | Risk signals must reach ALERT workflows within analyst-acceptable time for operational value. |
| Measurement | End-to-end timer from ingest acceptance to `RiskCalculated` publication (simulation environment). |
| Target | **P95 ≤ 2 seconds**; P99 ≤ 5 seconds (initial simulation target). |
| Priority | Critical |
| Release | MVP |
| Verification | Load test + distributed trace spans |

### NFR-PERF-002 — Synchronous API Read Latency

| Field | Value |
|-------|-------|
| Category | Performance |
| Requirement | Authorized synchronous read APIs for operational entities (alerts, cases, risk assessments) shall meet interactive latency targets under expected MVP load. |
| Rationale | Analyst workflows require responsive retrieval during triage and investigation. |
| Measurement | P95 latency from API gateway to response for read operations. |
| Target | **P95 ≤ 300 ms** under MVP simulation load (100 concurrent analysts). |
| Priority | High |
| Release | MVP |
| Verification | API performance tests |

### NFR-PERF-003 — Dashboard Workspace Load Latency

| Field | Value |
|-------|-------|
| Category | Performance |
| Requirement | Primary DASH workspace views (alert queue, case list) shall load within interactive targets. |
| Rationale | Unified workspace is core MVP value proposition. |
| Measurement | Time to first meaningful content for primary views. |
| Target | **P95 ≤ 2 seconds** (initial simulation target). |
| Priority | High |
| Release | MVP |
| Verification | Frontend performance tests + API aggregation metrics |

### NFR-PERF-004 — Investigation Case Retrieval Latency

| Field | Value |
|-------|-------|
| Category | Performance |
| Requirement | Retrieval of investigation case detail including timeline and evidence index shall meet investigation workflow targets. |
| Rationale | Investigators require rapid case context assembly. |
| Measurement | P95 latency for case detail retrieval API. |
| Target | **P95 ≤ 500 ms** for standard cases (≤100 evidence references). |
| Priority | High |
| Release | MVP |
| Verification | API performance tests |

### NFR-PERF-005 — Search and Discovery Latency

| Field | Value |
|-------|-------|
| Category | Performance |
| Requirement | Cross-entity search and discovery within authorized scope shall return results within defined bounds. |
| Rationale | Analysts need to locate cases, alerts, and entities quickly. |
| Measurement | P95 search query latency. |
| Target | **P95 ≤ 1 second** for standard scoped queries (simulation). |
| Priority | Medium |
| Release | MVP |
| Verification | Search performance tests |

### NFR-PERF-006 — AI Explanation Latency (Assistive Path)

| Field | Value |
|-------|-------|
| Category | Performance |
| Requirement | AI-generated explanations and summaries shall operate on a **non-critical path** and not block deterministic risk evaluation or alert lifecycle operations. |
| Rationale | LLM latency is variable; must not contaminate deterministic critical path (ADR-003). |
| Measurement | P95 latency for AI assistive requests; separate from risk critical path. |
| Target | **P95 ≤ 5 seconds** interactive assist; timeout with graceful fallback at **10 seconds**. |
| Priority | High |
| Release | MVP |
| Verification | AI service tests + timeout/fallback tests |

### NFR-PERF-007 — Report Generation Latency

| Field | Value |
|-------|-------|
| Category | Performance |
| Requirement | Standard operational reports shall complete within agreed generation windows. |
| Rationale | REPORT is Version 2; batch/report workloads differ from interactive APIs. |
| Measurement | Time from report request to `ReportGenerated`. |
| Target | **Standard reports ≤ 5 minutes**; large exports ≤ 30 minutes (simulation target). |
| Priority | Medium |
| Release | Version 2 |
| Verification | REPORT workflow tests |

### NFR-PERF-008 — Authentication Operation Latency

| Field | Value |
|-------|-------|
| Category | Performance |
| Requirement | Authentication and session validation operations shall meet interactive security targets. |
| Rationale | Auth latency affects every workflow. |
| Measurement | P95 login and token validation latency. |
| Target | **P95 ≤ 500 ms** (simulation). |
| Priority | Critical |
| Release | MVP |
| Verification | AUTH performance tests |

### NFR-PERF-009 — Event Propagation Latency

| Field | Value |
|-------|-------|
| Category | Performance |
| Requirement | Domain event propagation (e.g., RISK → ALERT) shall complete within defined bounds under normal conditions. |
| Rationale | Event-driven workflows depend on timely signal delivery. |
| Measurement | Producer publish to consumer processing complete. |
| Target | **P95 ≤ 1 second** intra-platform (simulation); **P99 ≤ 3 seconds**. |
| Priority | High |
| Release | MVP |
| Verification | Event pipeline integration tests |

### Performance Path Classification

| Path type | Examples | Latency expectation |
|-----------|----------|---------------------|
| **Critical / deterministic** | Risk scoring, alert creation, authZ checks | Strict; AI excluded |
| **Interactive synchronous** | Case retrieval, dashboard reads | Moderate |
| **Assistive / AI** | Explanations, summaries, retrieval agents | Relaxed; async preferred |
| **Background / batch** | Reports, exports, evaluations | Window-based |

---

## 2. Scalability

### NFR-SCAL-001 — Transaction Processing Throughput

| Field | Value |
|-------|-------|
| Category | Scalability |
| Requirement | The platform shall support scalable transaction ingest and risk evaluation throughput via horizontal scaling of RISK and ingest components. |
| Rationale | Exchange-modeled workloads require growth headroom. |
| Measurement | Sustained transactions processed per minute in simulation. |
| Target | **Initial simulation target: 10,000 transactions/minute** aggregate ingest with linear scale-out design objective. |
| Priority | High |
| Release | MVP |
| Verification | Load tests; capacity planning review |

### NFR-SCAL-002 — Concurrent Analyst Sessions

| Field | Value |
|-------|-------|
| Category | Scalability |
| Requirement | MVP shall support concurrent operational analyst sessions without unacceptable degradation of critical paths. |
| Measurement | Concurrent authenticated sessions with SLA adherence. |
| Target | **100 concurrent analysts** (simulation MVP target); **500** future target. |
| Priority | High |
| Release | MVP |
| Verification | Load tests |

### NFR-SCAL-003 — Horizontal Domain Scaling

| Field | Value |
|-------|-------|
| Category | Scalability |
| Requirement | Domain services shall be independently scalable where data ownership and contracts permit. |
| Rationale | ALERT, RISK, INVEST, and AI workloads scale differently. |
| Measurement | Scale-out test per domain without cross-domain data coupling violations. |
| Target | Design objective — at least **RISK, ALERT, AI** independently scalable. |
| Priority | High |
| Release | All |
| Verification | Architecture review + scale tests |

---

## 3. Availability

### NFR-AVAIL-001 — Platform Availability (MVP)

| Field | Value |
|-------|-------|
| Category | Availability |
| Requirement | MVP platform shall target defined availability for operational use in simulation/staging environments progressing toward production goals. |
| Measurement | Uptime of critical user-facing paths excluding planned maintenance. |
| Target | **99.5%** MVP simulation/staging target; **99.9%** production design objective. |
| Priority | High |
| Release | MVP |
| Verification | Uptime monitoring |

### NFR-AVAIL-002 — Critical Path Availability (Deterministic)

| Field | Value |
|-------|-------|
| Category | Availability |
| Requirement | Deterministic workflows (auth, risk scoring, alert lifecycle, case management) shall remain available independent of AI service availability. |
| Rationale | AI is assistive; core operations must survive AI outage. |
| Target | **Same as NFR-AVAIL-001** for deterministic paths when AI is down. |
| Priority | Critical |
| Release | MVP |
| Verification | Chaos test — AI disabled |

### NFR-AVAIL-003 — AI Assistive Availability

| Field | Value |
|-------|-------|
| Category | Availability |
| Requirement | AI assistive features may degrade without constituting platform outage. |
| Measurement | AI request success rate; core workflow success rate during AI outage. |
| Target | Core workflows **100% operable** without AI; AI features unavailable or delayed is acceptable. |
| Priority | High |
| Release | MVP |
| Verification | Failure injection tests |

---

## 4. Reliability

### NFR-REL-001 — Event Processing Success Rate

| Field | Value |
|-------|-------|
| Category | Reliability |
| Requirement | Domain event processing shall achieve high success rate with retry and dead-letter handling for failures. |
| Measurement | Successfully processed events / total eligible events. |
| Target | **≥ 99.9%** success under normal conditions (simulation). |
| Priority | Critical |
| Release | MVP |
| Verification | Event pipeline monitoring |

### NFR-REL-002 — Data Durability

| Field | Value |
|-------|-------|
| Category | Reliability |
| Requirement | Authoritative domain records (cases, alerts, compliance records, audit logs) shall be durably persisted before acknowledging successful operations. |
| Measurement | Durability audit; recovery tests. |
| Target | **Zero acknowledged-write loss** under single-component failure scenarios (design objective). |
| Priority | Critical |
| Release | MVP |
| Verification | Failure injection + recovery tests |

---

## 5. Resilience

### NFR-RES-001 — Graceful Degradation (AI Unavailable)

| Field | Value |
|-------|-------|
| Category | Resilience |
| Requirement | When AI services are unavailable, deterministic risk evaluation, alert handling, investigation, and compliance workflows shall continue; AI explanations may be unavailable or queued. |
| Rationale | ADR-003; Principles AP-06, AP-09. |
| Target | No critical workflow blocked solely by AI outage. |
| Priority | Critical |
| Release | MVP |
| Verification | Chaos tests |

### NFR-RES-002 — Graceful Degradation (Cache Unavailable)

| Field | Value |
|-------|-------|
| Category | Resilience |
| Requirement | Cache unavailability shall fall back to authoritative data stores with increased latency, not incorrect results or authorization bypass. |
| Priority | High |
| Release | MVP |
| Verification | Cache failure tests |

### NFR-RES-003 — Event Broker Failure

| Field | Value |
|-------|-------|
| Category | Resilience |
| Requirement | Event broker unavailability shall trigger retry, buffering where safe, and operator visibility; producers shall not silently drop authoritative events without audit/error signaling. |
| Priority | Critical |
| Release | MVP |
| Verification | Broker failure simulation |

### NFR-RES-004 — Duplicate, Out-of-Order, and Malformed Events

| Field | Value |
|-------|-------|
| Category | Resilience |
| Requirement | Consumers shall handle duplicate events idempotently, tolerate out-of-order delivery within documented windows, reject or quarantine malformed events without corrupting authoritative state. |
| Priority | Critical |
| Release | MVP |
| Verification | Event contract tests |

### Failure Mode Summary

| Failure | Expected behavior |
|---------|-------------------|
| AI unavailable | Core deterministic paths continue; AI UI shows degraded state |
| Primary database unavailable | Affected domain fails closed; health degraded |
| Cache unavailable | Fallback to source; latency increase |
| Event broker unavailable | Retry/buffer/DLQ; alert operators |
| External compliance provider unavailable | COMP workflows queue or fail gracefully per policy |
| Graph/vector service unavailable | AI retrieval degraded; deterministic paths continue |
| Individual service unavailable | Isolated domain failure; others continue if dependencies allow |
| Notification unavailable | Non-blocking; audit record of delivery failure |

---

## 6. Security

### NFR-SEC-001 through NFR-SEC-010

See [SecurityArchitecture.md](../03-architecture/SecurityArchitecture.md) for architectural detail. Summary targets:

| ID | Requirement summary | Target |
|----|---------------------|--------|
| NFR-SEC-001 | Strong authentication for human and service actors | MFA for privileged roles (MVP design objective) |
| NFR-SEC-002 | RBAC enforced on every protected operation | **100%** API enforcement |
| NFR-SEC-003 | TLS for external and inter-service communication | **Required** in non-local environments |
| NFR-SEC-004 | Encryption at rest for sensitive categories | **Required** for PII, credentials, investigation evidence |
| NFR-SEC-005 | Secrets not stored in source code; rotated via secure mechanism | **Zero** secrets in repository |
| NFR-SEC-006 | Rate limiting on authentication and public APIs | Configurable thresholds per environment |
| NFR-SEC-007 | Input validation on all external inputs | **100%** external API endpoints |
| NFR-SEC-008 | Mutual authentication or signed tokens for service-to-service | Required for cross-domain service calls |
| NFR-SEC-009 | Prompt injection detection/mitigation for AI inputs | Defense-in-depth; human review for sensitive outputs |
| NFR-SEC-010 | AI tools invoke only authorized domain operations | Tool allowlist per agent; AUTHZ on every tool call |

---

## 7. Privacy

### NFR-PRIV-001 — PII Minimization

| Field | Value |
|-------|-------|
| Requirement | Collect and expose PII only where required for operational, compliance, or security purposes. |
| Target | Data classification applied to all new domain stores. |
| Release | MVP |

### NFR-PRIV-002 — Data Retention

| Field | Value |
|-------|-------|
| Requirement | Retention policies shall be definable per data classification and jurisdiction requirements. |
| Target | Retention configuration supported via ADMIN/CORE; default policies documented before production. |
| Release | MVP |

### NFR-PRIV-003 — Data Classification

| Field | Value |
|-------|-------|
| Requirement | Data shall be classified (e.g., public, internal, confidential, restricted) and handled per classification. |
| Release | MVP |

---

## 8. Auditability

### NFR-AUD-001 — Sensitive Action Audit Completeness

| Field | Value |
|-------|-------|
| Requirement | **100%** of defined sensitive actions (admin, compliance disposition, alert/case state changes, AI recommendation issuance) shall produce durable audit records via CORE audit infrastructure. |
| Release | MVP |
| Verification | Audit coverage test matrix |

### NFR-AUD-002 — Decision Provenance

| Field | Value |
|-------|-------|
| Requirement | Risk scores, compliance dispositions, and investigation resolutions shall be reconstructable with actor, timestamp, evidence references, and applicable rule/signal provenance. |
| Release | MVP |

### NFR-AUD-003 — Configuration Change Audit

| Field | Value |
|-------|-------|
| Requirement | Administrative and domain configuration changes shall be audited with before/after state where applicable. |
| Release | MVP |

---

## 9. Observability

See [ObservabilityArchitecture.md](../03-architecture/ObservabilityArchitecture.md).

| ID | Summary | Target |
|----|---------|--------|
| NFR-OBS-001 | Structured logs for all domain services | **100%** services |
| NFR-OBS-002 | Distributed tracing on critical paths | **≥90%** critical path spans |
| NFR-OBS-003 | Correlation ID (`X-Correlation-Id`) end-to-end | **100%** user-initiated requests |
| NFR-OBS-004 | Liveness and readiness health endpoints per service | **100%** deployable services |
| NFR-OBS-005 | SLOs defined for auth, risk, alert, case APIs | Documented before MVP gate |

---

## 10. Maintainability

### NFR-MAINT-001 — Modular Domain Deployability

| Field | Value |
|-------|-------|
| Requirement | Domains shall be deployable and evolvable independently within explicit contract constraints. |
| Release | All |

---

## 11. Testability

### NFR-TEST-001 — Traceability to Tests

| Field | Value |
|-------|-------|
| Requirement | Every Critical and High priority FR and NFR shall map to at least one verification test or monitoring check before release. |
| Release | All |

---

## 12. Accessibility

### NFR-A11Y-001 — Workspace Accessibility Baseline

| Field | Value |
|-------|-------|
| Requirement | DASH operational workspace shall target WCAG 2.1 Level AA for core analyst workflows (design objective). |
| Release | MVP |
| Note | Full certification is a future validation activity—not claimed in Phase 2. |

---

## 13. Data Integrity

### NFR-INT-001 — Domain Source of Truth

| Field | Value |
|-------|-------|
| Requirement | Each data entity has exactly one authoritative owning domain; consumers reference, not duplicate, lifecycle state. |
| Release | MVP |

### NFR-INT-002 — Idempotent Event Processing

| Field | Value |
|-------|-------|
| Requirement | Event consumers shall process duplicate deliveries without duplicate side effects. |
| Release | MVP |

---

## 14. Disaster Recovery

### NFR-DR-001 — RPO/RTO (Simulation Targets)

| Field | Value |
|-------|-------|
| Requirement | Disaster recovery objectives shall be defined before production deployment. |
| Target (simulation) | **RPO ≤ 1 hour**; **RTO ≤ 4 hours** for MVP staging (design objective—not validated achievement). |
| Release | MVP |

---

## 15. AI Reliability

### NFR-AI-001 — AI Fallback

| Field | Value |
|-------|-------|
| Requirement | Mandatory business workflows shall not depend on AI runtime availability. |
| Release | MVP |

### NFR-AI-002 — Human Approval

| Field | Value |
|-------|-------|
| Requirement | AI recommendations for consequential actions shall require explicit human approval or deterministic rule execution—not autonomous AI execution. |
| Release | MVP |

### NFR-AI-003 — Evaluation Pass Rate

| Field | Value |
|-------|-------|
| Requirement | AI evaluation framework shall measure explanation quality before promoting prompt/model changes. |
| Target | **≥90%** eval pass rate for promoted prompts (Version 2 target). |
| Release | Version 2 |

### NFR-AI-004 — Model and Prompt Version Tracking

| Field | Value |
|-------|-------|
| Requirement | Every AI recommendation record shall reference prompt version and model identifier metadata. |
| Release | MVP |

---

## 16. AI Safety

Covered by NFR-AI-002, NFR-SEC-009, NFR-SEC-010, and Security Architecture AI sections.

---

## 17. AI Evaluation

Version 2 scope per FDS AI roadmap (Agent Evaluation Framework). MVP includes explainability controls and audit of recommendations.

---

## 18. Cost Awareness

### NFR-COST-001 — AI Cost Monitoring

| Field | Value |
|-------|-------|
| Requirement | AI Platform shall expose token/cost metrics per agent and organization where provider APIs permit. |
| Target | Cost dashboards for operators (design objective). |
| Release | MVP |

---

## 19. Operational Support

### NFR-OPS-001 — Incident Diagnosability

| Field | Value |
|-------|-------|
| Requirement | OPS domain (V2) and CORE health hooks shall enable operators to diagnose platform incidents using logs, metrics, and traces. |
| Release | Version 2 (CORE health MVP) |

---

## 20. Configuration Management

### NFR-CFG-001 — Environment Separation

| Field | Value |
|-------|-------|
| Requirement | Configuration shall be environment-separated (dev/staging/production) with no production secrets in non-production code paths. |
| Release | MVP |

---

## Traceability to Product Discovery Metrics

| Product Discovery metric | NFR alignment |
|--------------------------|---------------|
| Alert triage time reduction | NFR-PERF-002, NFR-PERF-003 |
| AI explanation latency | NFR-PERF-006 |
| Platform availability 99.5% | NFR-AVAIL-001 |
| Event processing ≥99.9% | NFR-REL-001 |
| Audit completeness 100% | NFR-AUD-001 |
| AI eval pass ≥90% | NFR-AI-003 |

---

## Open Questions

| ID | Question | Status |
|----|----------|--------|
| NFR-OQ-001 | Production SLO approval for exchange-scale deployment | OPEN — requires ops stakeholder |
| NFR-OQ-002 | Exact retention periods per jurisdiction | OPEN — requires compliance stakeholder |
| NFR-OQ-003 | Baseline measurement methodology for Product Discovery KPIs (BQ-3) | Phase 2 decision — see Phase2Report |

---

## Document Closing

This NFR baseline is architecture-ready. Implementation phases shall map each Critical/High NFR to tests, monitors, and ADRs as appropriate.
