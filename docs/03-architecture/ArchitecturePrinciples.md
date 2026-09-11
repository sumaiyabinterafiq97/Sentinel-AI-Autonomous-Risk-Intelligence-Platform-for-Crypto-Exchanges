# Architecture Principles

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Architecture Principles |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 2 |
| Owner | Architecture Team |
| Last Updated | 2026-09-03 |

---

## Purpose

Durable architectural principles governing Sentinel AI design decisions. These complement [Product Principles](../01-product/Principles.md) and [Project Roadmap Engineering Philosophy](../00-project/ProjectRoadmap.md).

Technology choices are deferred unless explicitly recorded in ADRs. Principles describe **capabilities and constraints**, not vendors.

---

## Principles

### AP-01 — Domain Ownership

Each capability has exactly one owning domain. Cross-domain behavior occurs through contracts (events, APIs), not duplicated lifecycle logic.

**Implication:** ALERT owns alerts; RISK owns risk scores; INVEST owns cases; COMP owns compliance workflows.

---

### AP-02 — Contract-First Interfaces

Events, APIs, and data schemas are defined and versioned before implementation. Consumers depend on contracts, not producer internals.

**Implication:** FDS publish/consume matrices are authoritative integration boundaries.

---

### AP-03 — Security by Design

Authentication, authorization, encryption, and audit are embedded in every domain service—not added post-implementation.

---

### AP-04 — Auditability by Default

Sensitive actions produce durable audit records through CORE shared audit infrastructure. Domains record domain-specific audit outcomes without redefining CORE ownership.

---

### AP-05 — Human Oversight for Consequential Actions

Enforcement, compliance approval, and irreversible operational decisions require authorized human actors and/or deterministic rules—not autonomous AI.

---

### AP-06 — AI Assistive by Default

AI Platform augments analysis, explanation, retrieval, and summarization. Business domains retain lifecycle ownership per frozen FRS.

---

### AP-07 — Observable by Default

Every deployable service emits structured logs, metrics, and trace spans on critical paths. Correlation IDs propagate across synchronous and asynchronous boundaries.

---

### AP-08 — Failure Isolation

Domain failures shall not cascade uncontrolled. Circuit breakers, timeouts, and bulkheads isolate unhealthy dependencies.

---

### AP-09 — Graceful Degradation

Optional capabilities (AI, cache, graph, vector search) may degrade without blocking mandatory deterministic workflows.

---

### AP-10 — Data Ownership

Each domain owns its authoritative data. Other domains consume via APIs or events—no shared mutable state without explicit contract.

---

### AP-11 — Idempotent Event Processing

Event consumers must safely handle duplicate delivery. Producers use stable event identity for deduplication.

---

### AP-12 — Backward-Compatible Contracts

Event and API schema changes use versioning. Breaking changes require explicit migration and consumer coordination.

---

### AP-13 — Least Privilege

Human users, services, and AI agents receive minimum permissions required. AI tool access is explicitly allowlisted per agent.

---

### AP-14 — Configuration over Hardcoding

Environment-specific behavior, feature flags, thresholds, and integration endpoints are configurable—not compiled into code.

---

### AP-15 — Vendor-Neutral Architecture

Architecture describes message delivery, persistence, and model inference as capability boundaries. Specific products are ADR candidates, not NFR mandates.

---

### AP-16 — Testability

Domains expose testable contracts. Critical paths have automated integration tests before production promotion.

---

### AP-17 — Horizontal Scalability

Stateless application tiers scale horizontally. Stateful components use domain-owned persistence with explicit scaling strategy.

---

### AP-18 — Operational Simplicity

Prefer clear service boundaries over excessive microservice fragmentation. Split services when ownership, scaling, security, or deployment independence justify complexity.

---

## Principle Application Matrix

| Decision area | Primary principles |
|---------------|-------------------|
| Service decomposition | AP-01, AP-10, AP-18 |
| Event design | AP-02, AP-11, AP-12 |
| AI integration | AP-05, AP-06, AP-09, AP-13 |
| Security | AP-03, AP-04, AP-13 |
| Operations | AP-07, AP-08, AP-09 |

---

## Related Documents

- [ArchitectureDecisionRecords.md](ArchitectureDecisionRecords.md)
- [DomainBoundaries.md](DomainBoundaries.md)
- [SystemArchitecture.md](SystemArchitecture.md)
