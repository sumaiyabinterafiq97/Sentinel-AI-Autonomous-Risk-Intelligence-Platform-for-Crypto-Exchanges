# Event Contracts

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Event Contracts |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 3/5 |
| Last Updated | 2026-09-03 |
| Schema artifacts | [schemas/](schemas/) — Phase 5 JSON Schema formalization |
| Authority | FDS/FRS frozen contracts; this document adds schemas without renaming events |

---

## Purpose

Phase 3 event schema definitions aligned to [EventArchitecture.md](../03-architecture/EventArchitecture.md) and frozen FDS publish/consume matrices.

**Governance:** Do not rename or add events to frozen domains without change control. This document schemas **existing** authorized events.

---

## Envelope Schema (All Events)

```yaml
EventEnvelope:
  type: object
  required: [eventId, eventType, schemaVersion, timestamp, producer, correlationId, organizationId, payload]
  properties:
    eventId: { type: string, format: uuid }
    eventType: { type: string }
    schemaVersion: { type: string, example: "1.0" }
    timestamp: { type: string, format: date-time }
    producer: { type: string, description: Domain service identifier }
    correlationId: { type: string, format: uuid }
    causationId: { type: string, format: uuid, nullable: true }
    organizationId: { type: string, format: uuid }
    payload: { type: object }
    metadata:
      type: object
      properties:
        classification: { enum: [public, internal, confidential, restricted] }
        idempotencyKey: { type: string }
        actorId: { type: string, format: uuid, nullable: true }
        actorType: { enum: [user, service, system] }
```

---

## Delivery Semantics

| Aspect | Policy |
|--------|--------|
| Guarantee | At-least-once |
| Ordering | Per aggregate key partition where noted |
| Retry | Exponential backoff; max attempts configurable |
| DLQ | After retry exhaustion |
| Malformed | Quarantine; no state mutation |

---

## MVP Domain Events

### CORE (Producer: CORE)

| Event | Purpose | Consumers |
|-------|---------|-----------|
| `PlatformStarted` | Platform ready | OPS (V2) |
| `PlatformUnavailable` | Platform degraded | OPS (V2), REPORT (V2) |
| `ConfigurationUpdated` | Config changed | ADMIN |
| `FeatureFlagChanged` | Flag changed | Domains (optional) |

### AUTH (Producer: AUTH)

| Event | Purpose | Consumers |
|-------|---------|-----------|
| `UserLoggedIn` | Successful login | SEC (V2) |
| `SessionExpired` | Session ended | SEC (V2) |

### RISK (Producer: RISK) — MVP publish set locked

| Event | schemaVersion | Purpose | Consumers |
|-------|---------------|---------|-----------|
| `RiskCalculated` | 1.0 | Risk assessment completed | ALERT, DASH, INVEST, COMP, AI |
| `HighRiskDetected` | 1.0 | High-risk threshold met | ALERT |

**RiskCalculated payload (v1.0):**

```yaml
RiskCalculatedPayload:
  required: [assessmentId, entityType, entityId, score, evaluatedAt]
  properties:
    assessmentId: { type: string, format: uuid }
    entityType: { enum: [transaction, user, device, session] }
    entityId: { type: string }
    score: { type: number, minimum: 0, maximum: 100 }
    riskLevel: { enum: [low, medium, high, critical] }
    explanationSummary: { type: string, description: Embedded MVP explanation }
    ruleHits: { type: array, items: { type: object } }
    evaluatedAt: { type: string, format: date-time }
    transactionRef: { type: string, nullable: true }
```

**HighRiskDetected payload (v1.0):**

```yaml
HighRiskDetectedPayload:
  required: [assessmentId, entityType, entityId, score, detectedAt]
  properties:
    assessmentId: { type: string, format: uuid }
    entityType: { type: string }
    entityId: { type: string }
    score: { type: number }
    detectedAt: { type: string, format: date-time }
    prioritySignal: { type: number, description: Risk-derived signal for ALERT context only }
```

**RISK consumes:** external `TransactionReceived` (ingest boundary—not a domain publish event).

### ALERT (Producer: ALERT) — MVP publish set locked

| Event | Consumers |
|-------|-----------|
| `AlertCreated` | INVEST, DASH, AI, SEC (V2) |
| `AlertAssigned` | DASH |
| `AlertClosed` | DASH |

**AlertCreated payload (v1.0):**

```yaml
AlertCreatedPayload:
  required: [alertId, status, priority, createdAt]
  properties:
    alertId: { type: string, format: uuid }
    status: { enum: [open, triaged, assigned] }
    priority: { type: integer, description: ALERT-owned queue priority }
    riskAssessmentId: { type: string, format: uuid, nullable: true }
    title: { type: string }
    createdAt: { type: string, format: date-time }
```

**ALERT consumes:** `RiskCalculated`, `HighRiskDetected` only (MVP).

### INVEST (Producer: INVEST) — MVP publish set locked

| Event | Consumers |
|-------|-----------|
| `CaseCreated` | DASH, COMP (context), SEC (V2), WALLET (V2) |
| `CaseUpdated` | DASH, COMP, AI |
| `CaseClosed` | COMP, REPORT (V2) |
| `CaseAssigned` | DASH |
| `EvidenceAttached` | AI |

**INVEST consumes (MVP):** `AlertCreated`, `RiskCalculated` only.

### COMP (Producer: COMP) — MVP publish set locked

| Event | Consumers |
|-------|-----------|
| `ComplianceReviewed` | REPORT (V2) |
| `TravelRuleValidated` | — (no frozen MVP consumer) |
| `SanctionsHitDetected` | — |
| `AuditPackagePrepared` | — |

**COMP consumes (MVP):** `CaseClosed`, `CaseUpdated`, `RiskCalculated`, `UserUpdated`.

### AI (Producer: AI) — MVP publish set

| Event | Consumers |
|-------|-----------|
| `AIRecommendationGenerated` | DASH (display) |
| `PromptUpdated` | — |
| `AgentRunFailed` | OPS (V2) |

**AI consumes (MVP):** `CaseUpdated`, `RiskCalculated`, `EvidenceAttached`, `AlertCreated`.

### ADMIN (Producer: ADMIN)

| Event | Consumers |
|-------|-----------|
| `AdminSettingUpdated` | OPS (V2) |
| `IntegrationConfigured` | OPS (V2) |
| `AdminActionPerformed` | Audit |

---

## Version 2 Domain Events (Locked Contracts)

### SEC — Publication (SEC-FR-006 authority)

**Publishes exactly:**
- `ThreatDetected`
- `SuspiciousSessionDetected`
- `ApiAbuseDetected`

**Consumes exactly (SEC-FR-007 authority):**
- `UserLoggedIn` (AUTH)
- `SessionExpired` (AUTH)
- `AlertCreated` (ALERT)
- `CaseCreated` (INVEST)

**Explicitly excluded from SEC V2 consumption:**
- `DeviceSignalReceived`
- `EvidenceAttached`
- `HighRiskDetected`, `RiskCalculated`, `CaseUpdated`, `CaseClosed`, etc.

### WALLET — V2

**Publishes:** `WalletProfileUpdated`, `AddressReputationChanged`, `SuspiciousWalletDetected`

**Consumes:** external `TransactionReceived`, `CaseCreated`, `RiskCalculated`

### REPORT — V2

**Publishes:** `ReportGenerated`, `ReportExported`, `KpiSnapshotCreated`

**Consumes:** `CaseClosed`, `RiskCalculated`, `ComplianceReviewed`, `AIEvaluationCompleted`, `PlatformUnavailable`

### OPS — V2

**Publishes:** `PlatformHealthDegraded`, `OperationalAlertRaised`, `BackupStatusUpdated`

**Consumes:** `PlatformStarted`, `PlatformUnavailable`, `AgentRunFailed`, `IntegrationConfigured`

---

## COMMAND vs QUERY vs EVENT

| Type | Mechanism | Example |
|------|-----------|---------|
| COMMAND | HTTP POST/PATCH to owning domain API | `PATCH /v1/alerts/{id}` close alert |
| QUERY | HTTP GET | `GET /v1/risk/assessments/{id}` |
| EVENT | Async envelope after successful commit | `AlertClosed` after API success |

Consumers MUST NOT treat events as commands to mutate producer state directly.

---

## Idempotency Keys by Event

| Event | Idempotency key source |
|-------|------------------------|
| `RiskCalculated` | `{assessmentId}` or `{entityId}:{evaluationVersion}` |
| `AlertCreated` | `{alertId}` |
| `CaseCreated` | `{caseId}` |
| `AIRecommendationGenerated` | `{recommendationId}` |

---

## Security Classification

| Event | Default classification |
|-------|------------------------|
| `RiskCalculated` | confidential |
| `AlertCreated` | confidential |
| `CaseCreated` | restricted |
| `ComplianceReviewed` | restricted |
| `UserLoggedIn` | internal |
| `AIRecommendationGenerated` | confidential |

---

## Schema Evolution

Additive optional payload fields: compatible within same `schemaVersion` minor. Breaking changes increment major `schemaVersion` with consumer coordination.

---

## Related Documents

- [EventArchitecture.md](../03-architecture/EventArchitecture.md)
- [DataArchitecture.md](../04-database/DataArchitecture.md) — event-to-persistence mapping
