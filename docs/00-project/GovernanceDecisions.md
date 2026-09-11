# Governance Decisions Log

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Governance Decisions Log |
| Version | 1.3 |
| Status | Active — Gate approvals recorded |
| Last Updated | 2026-09-11 |

---

## BQ-4 — REPORT and MVP Application Development Gate

| Field | Value |
|-------|-------|
| **Decision ID** | GD-001 |
| **Topic** | BQ-4: Is REPORT required for MVP sign-off? |
| **Decision** | **REPORT is Version 2 and is NOT an MVP Application Development Gate dependency.** |
| **Authority** | FDS domain catalog (REPORT release = Version 2); ProductDiscovery Phase 1; FRS REPORT chapter (V2 only) |
| **Rationale** | MVP operable without REPORT per DASH/FDS; COMP audit prep satisfies MVP compliance evidence; REPORT consumes domain events in V2 |
| **Impact** | MVP implementation, OpenAPI gate, and CI validation exclude REPORT APIs |
| **Human sign-off** | **CONFIRMED** — Project Owner — approved — 2026-09-11 |
| **Status** | **HUMAN-DECISION-RESOLVED** — REPORT remains V2; not MVP Application Development Gate dependency |
| **Phase 8 note** | Verified against FDS (REPORT = Version 2), PRD §16, ProductScope; governance position unchanged |
| **Gate note** | Project Owner confirmed BQ-4 does not block MVP implementation |

---

## P3-OQ-001 — DASH Real-Time Transport

See ADR-016 in ArchitectureDecisionRecords.md.

**Decision summary:** Server-Sent Events (SSE) for MVP workspace refresh; WebSocket deferred to V2 if bidirectional needs emerge.

---

## P3-OQ-002 — BFF vs Direct Domain Calls

See ADR-017.

**Decision summary:** DASH BFF layer for MVP browser clients; service-to-service direct domain APIs.

---

## P3-OQ-003 — Data Retention Periods

See ADR-018 and [RetentionJurisdictionGovernance.md](../04-database/RetentionJurisdictionGovernance.md).

**Decision summary:** Default retention targets documented; jurisdiction-specific overrides remain compliance stakeholder input (NFR-OQ-002 OPEN).

---

## GD-002 — Deferred MVP Event Schemas (Phase 7)

| Field | Value |
|-------|-------|
| **Decision ID** | GD-002 |
| **Topic** | Are PlatformStarted, PlatformUnavailable, AgentRunFailed required for MVP? |
| **Decision** | **Deferred — not MVP-required formal JSON Schema obligations** |
| **Authority** | EventContracts.md; EventContractCoverageMatrix.md; OPS/REPORT are V2 consumers |
| **Classification** | |
| PlatformStarted | Deferred / V2-oriented (primary consumer OPS V2) |
| PlatformUnavailable | Deferred / V2-oriented (OPS V2, REPORT V2) |
| AgentRunFailed | Deferred / V2-oriented (OPS V2); MVP stores failure in `ai.agent_runs` |
| UserCreated / UserDeactivated | Optional deferred — no MVP frozen consumer |
| OrganizationCreated / OrganizationUpdated | Optional deferred — no MVP frozen consumer |
| **Rationale** | Adding schemas without MVP consumers invents obligations; CORE health remains via API-CORE-001/002; AI failures observable via agent_runs + NFR observability |
| **Reconsideration trigger** | OPS V2 activation; REPORT V2 activation; explicit FRS requiring domain-broadcast platform lifecycle events for MVP consumers |
| **Status** | Recorded (Phase 7) — no fabricated MVP requirement |

---

## GD-003 — PRD Formal Approval Status

| Field | Value |
|-------|-------|
| **Decision ID** | GD-003 |
| **Topic** | PRD v0.1 formal approval |
| **Decision** | **APPROVED** |
| **Approver** | Project Owner — approved |
| **Date** | 2026-09-11 |
| **Evidence** | PRDApprovalRecord.md v1.0; ApplicationDevelopmentGateDecision.md |
| **Status** | **APPROVED** — no fabricated third-party identities |

---

## GD-004 — NFR-OQ-002 Disposition

| Field | Value |
|-------|-------|
| **Decision ID** | GD-004 |
| **Topic** | Jurisdiction-specific retention periods |
| **Decision** | Durations remain pending future legal/compliance fill; **MVP implementation is NOT blocked** |
| **MVP posture** | ADR-018 / RetentionDecisionMatrix defaults labeled **MVP SIMULATION TARGET** remain authoritative |
| **Project Owner confirmation** | **CONFIRMED** 2026-09-11 — non-blocking for MVP |
| **Classification** | **HUMAN-DECISION-RESOLVED** (for gate); legal matrix fill = **FUTURE** |
| **Authority** | NFR-OQ-002; RetentionJurisdictionGovernance.md; Project Owner gate approval |

---

## GD-005 — Phase 8 AI Architecture Baseline

| Field | Value |
|-------|-------|
| **Decision ID** | GD-005 |
| **Topic** | MVP AI agent set |
| **Decision** | MVP agents/capabilities limited to AI-FR-001–009: Investigation, Risk, Retrieval + Prompt/Explainability/Events/Tools/Audit supports |
| **Deferred** | Compliance Agent, Report Agent, Evaluation runtime (V2); autonomous enforcement excluded |
| **Status** | Recorded Phase 8 — AIArchitecture.md; Phase 8 exit **APPROVED** by Project Owner 2026-09-11 |

---

## GD-006 — Application Development Gate

| Field | Value |
|-------|-------|
| **Decision ID** | GD-006 |
| **Topic** | Application Development Gate (ProjectRoadmap §10) |
| **Decision** | **SATISFIED** — Phase 12 application development **AUTHORIZED** (start at M0) |
| **Approver** | Project Owner — approved |
| **Date** | 2026-09-11 |
| **Evidence** | ApplicationDevelopmentGateDecision.md |
| **Non-blocking gaps retained** | COMP list API; global search compose; V2 OpenAPI; GD-002 events |
| **Status** | Recorded — gate open for M0+ per ImplementationPlan |

---

## Related Documents

- [ApplicationDevelopmentGateDecision.md](ApplicationDevelopmentGateDecision.md)
- [ProjectRoadmap.md](ProjectRoadmap.md)
- [PRDApprovalRecord.md](PRDApprovalRecord.md)
- [ProductDiscovery.md](../01-product/ProductDiscovery.md)
- [Phase4Report.md](Phase4Report.md)
