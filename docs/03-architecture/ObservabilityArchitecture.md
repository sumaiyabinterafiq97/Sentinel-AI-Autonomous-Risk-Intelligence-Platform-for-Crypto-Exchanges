# Observability Architecture

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Observability Architecture |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 2 |
| Owner | Platform Operations / Architecture |
| Last Updated | 2026-09-03 |

---

## Purpose

Define what must be observable in Sentinel AI: logs, metrics, traces, audit telemetry, AI-specific signals, SLOs, and operational ownership.

**No implementation:** Specific tools (Prometheus, Grafana, Datadog, etc.) are not mandated.

---

## Observability Pillars

| Pillar | Purpose |
|--------|---------|
| **Logs** | Discrete events for debugging and security |
| **Metrics** | Aggregated measurements for SLOs and capacity |
| **Traces** | End-to-end latency and dependency analysis |
| **Audit telemetry** | Compliance and accountability (distinct from debug logs) |

---

## Correlation Model

| Identifier | Propagation |
|------------|-------------|
| `requestId` | Single HTTP/API request |
| `correlationId` | End-to-end workflow (request → events → downstream) |
| `eventId` | Single event emission |
| `auditId` | Audit record reference |
| `organizationId` | Tenant scope tag on all telemetry |

All services SHALL propagate `correlationId` from inbound request through outbound calls and published events.

---

## Log Categories

| Category | Examples | Retention (design objective) |
|----------|----------|------------------------------|
| Application | Service start, handler errors, business warnings | 30–90 days |
| Security | Auth failures, access denied, privilege use | 1 year+ |
| Audit | Sensitive actions (via CORE) | Per compliance policy |
| AI | Agent runs, tool invocations, failures | 90 days+ |

Logs SHALL be structured (JSON) with standard fields: timestamp, level, service, correlationId, organizationId, message.

**No secrets or full PII in application logs.**

---

## Metrics Categories

### Technical metrics (all services)

| Metric | Type |
|--------|------|
| Request rate | Counter |
| Error rate | Counter |
| Latency (P50/P95/P99) | Histogram |
| Saturation (CPU, memory, queue depth) | Gauge |

### Business metrics (domain-specific)

| Domain | Examples |
|--------|----------|
| RISK | Evaluations/min, rule hit rate |
| ALERT | Alerts created, triage time |
| INVEST | Cases opened/closed, time to resolution |
| COMP | Reviews completed, screening matches |
| AI | Recommendations generated, acceptance rate |

### AI-specific metrics

| Metric | Purpose |
|--------|---------|
| LLM latency | Performance monitoring |
| Token usage / cost | Cost awareness (NFR-COST-001) |
| Tool invocation success rate | Reliability |
| Model version / prompt version | Traceability |
| Eval pass rate | Quality gate (V2) |
| Hallucination flags | Safety monitoring |

---

## Distributed Tracing

Critical paths SHALL emit trace spans:

| Path | Spans |
|------|-------|
| Risk evaluation | Ingest → RISK score → publish |
| Alert triage | DASH → ALERT read → disposition |
| Investigation | Case open → evidence attach → close |
| AI assist | Request → retrieval → LLM → response |
| Auth | Login → token issue → AUTHZ check |

Target: **≥90%** critical path coverage (NFR-OBS-002).

---

## Health Checks

| Endpoint type | Purpose |
|---------------|---------|
| **Liveness** | Process running — restart if failing |
| **Readiness** | Ready for traffic — dependencies available |

CORE provides platform health hooks in MVP. OPS domain (V2) expands operational visibility.

---

## Service Level Objectives (Initial Targets)

| Service / path | SLI | SLO (simulation target) |
|----------------|-----|-------------------------|
| AUTH login | Success rate | 99.9% |
| Risk evaluation | P95 latency | ≤ 2s |
| Alert read API | P95 latency | ≤ 300ms |
| Case read API | P95 latency | ≤ 500ms |
| AI assist | Availability | Best-effort; not in platform SLO |
| Platform overall | Uptime | 99.5% MVP |

SLO breach triggers operational review—not automatic in Phase 2.

---

## Dashboards (Conceptual)

| Dashboard | Audience | Owner |
|-----------|----------|-------|
| Platform health | SRE | OPS (V2) / CORE (MVP hooks) |
| Risk pipeline | Risk engineering | RISK |
| Alert operations | Risk ops | ALERT |
| AI quality | AI platform | AI |
| Security overview | Security | SEC (V2) |

REPORT domain (V2) may formalize KPI dashboards for business users.

---

## Alerting (Operational)

| Alert type | Source | Example |
|------------|--------|---------|
| Platform | OPS/CORE | Service unhealthy, broker DLQ depth |
| Security | Security logs | Repeated auth failures |
| Business | Domain metrics | Event processing failure rate |
| AI | AI metrics | Agent failure spike, cost anomaly |

Business alerts (fraud) belong to ALERT domain. Platform alerts belong to OPS.

---

## Incident Investigation

Operators SHALL be able to:

1. Trace a user request by `correlationId`
2. Identify failing service and dependency from traces
3. Correlate events by `causationId` chain
4. Review audit records for sensitive actions during incident window

---

## OPS Domain Relationship

| Release | Observability scope |
|---------|---------------------|
| MVP | CORE health, per-service logs/metrics/traces (implementation) |
| V2 | OPS domain formalizes ops catalog, backup status, ops alerting |

---

## Related Documents

- [NonFunctionalRequirements.md](../02-requirements/NonFunctionalRequirements.md) — NFR-OBS-*
- [EventArchitecture.md](EventArchitecture.md)
- [SystemArchitecture.md](SystemArchitecture.md)
