# Event Implementation Plan

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Event Implementation Plan |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 11 |
| Last Updated | 2026-09-11 |

---

## Purpose

Plan production/consumption of existing event contracts. **Do not invent events. No broker code in Phase 11.**

---

## Authoritative Inputs

EventArchitecture, EventContracts, EventContractCoverageMatrix, event-catalog.v0.2.json, schemas/, MessageBrokerArchitecture, SchemaRegistryGovernance, ADR-004, ADR-015, GD-002.

---

## Delivery Semantics (Planned)

| Topic | Plan |
|-------|------|
| Delivery | At-least-once |
| Publish | Transactional outbox → relay → durable log |
| Consumers | Idempotent via eventId / catalog idempotencyKey |
| Ordering | Per-entity partition where matrix specifies |
| Retry | Bounded with backoff |
| DLQ | Poison messages after max attempts |
| Correlation | envelope.correlationId / causationId |
| Tenant | envelope.organizationId required |
| Compatibility | SchemaRegistryGovernance rules |

Broker product: capability-level until implementation ADR (ADR-007 / ADR-015).

---

## MVP Events (Catalog)

Implement producers/consumers per catalog v0.2 — **24 MVP + 3 SEC V2 schemas exist; SEC consumers only when SEC V2 built**.

| Producer | Events |
|----------|--------|
| RISK | RiskCalculated, HighRiskDetected |
| ALERT | AlertCreated, AlertAssigned, AlertClosed |
| INVEST | CaseCreated, CaseUpdated, CaseClosed, CaseAssigned, EvidenceAttached |
| COMP | ComplianceReviewed, TravelRuleValidated, SanctionsHitDetected, AuditPackagePrepared |
| AUTH | UserLoggedIn, SessionExpired |
| CORE | ConfigurationUpdated, FeatureFlagChanged |
| AI | AIRecommendationGenerated, PromptUpdated |
| ADMIN | AdminSettingUpdated, IntegrationConfigured, AdminActionPerformed |
| USER | UserUpdated |

Consumer mapping: event-catalog.v0.2.json (authoritative).

---

## Deferred (GD-002)

| Event | Status |
|-------|--------|
| PlatformStarted | Deferred — no schema |
| PlatformUnavailable | Deferred |
| AgentRunFailed | Deferred — use ai.agent_runs in MVP |

Optional USER/ORG create events remain deferred without frozen consumers.

---

## SEC Lock (Preserve)

Publish only: ThreatDetected, SuspiciousSessionDetected, ApiAbuseDetected (V2).
Consume only: UserLoggedIn, SessionExpired, AlertCreated, CaseCreated.
Never: DeviceSignalReceived, EvidenceAttached. No SEC self-consume.

---

## Outbox / Relay / Consumer Groups

| Component | Plan |
|-----------|------|
| Outbox table | Per service DB (owned schema or platform outbox pattern) |
| Relay | Poll/publish with at-least-once |
| Consumer groups | Per consuming domain deployable |
| DASH | Consume for SSE invalidation/refetch |
| AI | Consume context events; never mutate foreign SoT |

---

## Verification

Schema validation in CI design; producer contract tests; consumer idempotency tests; SEC lock regression — TestingImplementationPlan / ContractValidationCI.

---

## Related Documents

- [../06-api/EventContracts.md](../06-api/EventContracts.md)
- [../06-api/schemas/event-catalog.v0.2.json](../06-api/schemas/event-catalog.v0.2.json)
- [BackendImplementationPlan.md](BackendImplementationPlan.md)
