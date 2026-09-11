# Event Contract Coverage Matrix

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Event Contract Coverage Matrix |
| Version | 0.2 (Draft) |
| Status | Draft — Phase 7 |
| Last Updated | 2026-09-11 |
| Authority | EventContracts.md, FDS/FRS, event-catalog.v0.2.json, GD-002 |

---

## Purpose

Audit MVP (and referenced V2) event contracts against formal JSON Schema artifacts. Identifies complete, missing, and intentionally deferred schemas.

**Legend:** ✅ Complete | ⚠️ Missing (required) | — Deferred | 🔒 SEC lock

---

## MVP Event Coverage

| Event | Producer | Consumer(s) | Source FR | Schema | Version | Complete | Classification | Idempotency key | Ordering |
|-------|----------|-------------|-----------|--------|---------|----------|----------------|-----------------|----------|
| RiskCalculated | RISK | ALERT, DASH, INVEST, COMP, AI | RISK-FR-010 | risk/RiskCalculated.v1 | 1.0 | ✅ | confidential | assessmentId | per entity partition |
| HighRiskDetected | RISK | ALERT | RISK-FR-010 | risk/HighRiskDetected.v1 | 1.0 | ✅ | confidential | assessmentId | per entity |
| AlertCreated | ALERT | INVEST, DASH, AI, SEC(V2) | ALERT-FR-008 | alert/AlertCreated.v1 | 1.0 | ✅ | confidential | alertId | per alert |
| AlertAssigned | ALERT | DASH | ALERT-FR-006 | alert/AlertAssigned.v1 | 1.0 | ✅ | confidential | alertId:assignedAt | — |
| AlertClosed | ALERT | DASH | ALERT-FR-006 | alert/AlertClosed.v1 | 1.0 | ✅ | confidential | alertId | — |
| CaseCreated | INVEST | DASH, COMP, SEC(V2), WALLET(V2) | INVEST-FR-007 | invest/CaseCreated.v1 | 1.0 | ✅ | restricted | caseId | per case |
| CaseUpdated | INVEST | DASH, COMP, AI | INVEST-FR-004 | invest/CaseUpdated.v1 | 1.0 | ✅ | restricted | caseId:updatedAt | — |
| CaseClosed | INVEST | COMP, REPORT(V2) | INVEST-FR-007 | invest/CaseClosed.v1 | 1.0 | ✅ | restricted | caseId | — |
| CaseAssigned | INVEST | DASH | INVEST-FR-006 | invest/CaseAssigned.v1 | 1.0 | ✅ Phase 6 | restricted | caseId:assignedAt | — |
| EvidenceAttached | INVEST | AI | INVEST-FR-003 | invest/EvidenceAttached.v1 | 1.0 | ✅ | restricted | evidenceId | — |
| ComplianceReviewed | COMP | REPORT(V2) | COMP-FR-007 | comp/ComplianceReviewed.v1 | 1.0 | ✅ | restricted | reviewId | — |
| TravelRuleValidated | COMP | — | COMP-FR-003 | comp/TravelRuleValidated.v1 | 1.0 | ✅ Phase 6 | restricted | validationId | — |
| SanctionsHitDetected | COMP | — | COMP-FR-004 | comp/SanctionsHitDetected.v1 | 1.0 | ✅ Phase 6 | restricted | screeningId | — |
| AuditPackagePrepared | COMP | — | COMP-FR-005 | comp/AuditPackagePrepared.v1 | 1.0 | ✅ Phase 6 | restricted | packageId | — |
| UserLoggedIn | AUTH | SEC(V2) | AUTH-FR-001 | auth/UserLoggedIn.v1 | 1.0 | ✅ | internal | sessionId:loggedInAt | — |
| SessionExpired | AUTH | SEC(V2) | AUTH-FR-002 | auth/SessionExpired.v1 | 1.0 | ✅ | internal | sessionId:expiredAt | — |
| UserUpdated | USER | COMP | USER-FR-002 | user/UserUpdated.v1 | 1.0 | ✅ Phase 6 | restricted | userId:updatedAt | — |
| ConfigurationUpdated | CORE | ADMIN | CORE-FR-007 | core/ConfigurationUpdated.v1 | 1.0 | ✅ | internal | configKey:version | — |
| FeatureFlagChanged | CORE | optional domains | CORE-FR-009 | core/FeatureFlagChanged.v1 | 1.0 | ✅ Phase 6 | internal | flagKey:updatedAt | — |
| AIRecommendationGenerated | AI | DASH | AI-FR-006 | ai/AIRecommendationGenerated.v1 | 1.0 | ✅ | confidential | recommendationId | — |
| PromptUpdated | AI | — | AI-FR-004 | ai/PromptUpdated.v1 | 1.0 | ✅ Phase 6 | internal | promptId:version | — |
| AdminSettingUpdated | ADMIN | OPS(V2) | ADMIN-FR-001 | admin/AdminSettingUpdated.v1 | 1.0 | ✅ Phase 6 | internal | settingKey:updatedAt | — |
| IntegrationConfigured | ADMIN | OPS(V2) | ADMIN-FR-002 | admin/IntegrationConfigured.v1 | 1.0 | ✅ Phase 6 | internal | integrationId | — |
| AdminActionPerformed | ADMIN | Audit | ADMIN-FR-003 | admin/AdminActionPerformed.v1 | 1.0 | ✅ Phase 6 | internal | action:performedAt | — |

---

## Intentionally Deferred (Documented in EventContracts.md)

| Event | Producer | Governance | Classification | Target |
|-------|----------|------------|----------------|--------|
| PlatformStarted | CORE | **GD-002: Deferred** | Not MVP-required | Pre-OPS V2 |
| PlatformUnavailable | CORE | **GD-002: Deferred** | Not MVP-required | Pre-OPS/REPORT V2 |
| AgentRunFailed | AI | **GD-002: Deferred** | Not MVP-required; MVP uses `ai.agent_runs` | Pre-OPS V2 |
| UserCreated | USER | Deferred optional | No MVP frozen consumer | If consumer appears |
| UserDeactivated | USER | Deferred optional | No MVP frozen consumer | If consumer appears |
| OrganizationCreated | ORG | Deferred optional | No MVP frozen consumer | If consumer appears |
| OrganizationUpdated | ORG | Deferred optional | No MVP frozen consumer | If consumer appears |

**Phase 7 decision:** Do **not** create JSON Schemas for GD-002 deferred events. Creating schemas without MVP consumers would invent obligations unsupported by FDS/FRS MVP consumer matrices.

**MVP health/status:** Covered by API-CORE-001 / API-CORE-002 (synchronous), not deferred platform lifecycle events.

---

## V2 SEC Events (Contract Lock)

| Event | Role | Schema | Complete |
|-------|------|--------|----------|
| ThreatDetected | SEC publish | v2/sec/ThreatDetected.v1 | ✅ |
| SuspiciousSessionDetected | SEC publish | v2/sec/SuspiciousSessionDetected.v1 | ✅ |
| ApiAbuseDetected | SEC publish | v2/sec/ApiAbuseDetected.v1 | ✅ |
| UserLoggedIn | SEC consume | auth/UserLoggedIn.v1 | ✅ |
| SessionExpired | SEC consume | auth/SessionExpired.v1 | ✅ |
| AlertCreated | SEC consume | alert/AlertCreated.v1 | ✅ |
| CaseCreated | SEC consume | invest/CaseCreated.v1 | ✅ |
| DeviceSignalReceived | — | **EXCLUDED** | 🔒 Not SEC obligation |
| EvidenceAttached | — | **EXCLUDED** | 🔒 Not SEC obligation |

---

## Envelope Requirements (All Events)

| Requirement | Rule |
|-------------|------|
| Tenant propagation | `organizationId` required in envelope for tenant-scoped events |
| Correlation | `correlationId` required; `causationId` when chained |
| Compatibility | Additive optional fields = backward compatible within major version |
| Validation | Producer validates against schema before publish |
| Consumer | Idempotent handler using idempotency key |

---

## Coverage Summary

| Metric | Count |
|--------|-------|
| MVP events in EventContracts | 27 listed (incl. deferred) |
| MVP events with JSON Schema | 24 |
| Intentionally deferred | 3 (PlatformStarted, PlatformUnavailable, AgentRunFailed) |
| USER/ORG publish deferred | 3 (no MVP consumer) |
| SEC lock violations | 0 |

---

## Related Documents

- [EventContracts.md](EventContracts.md)
- [schemas/event-catalog.v0.2.json](schemas/event-catalog.v0.2.json)
- [SchemaRegistryGovernance.md](SchemaRegistryGovernance.md)
