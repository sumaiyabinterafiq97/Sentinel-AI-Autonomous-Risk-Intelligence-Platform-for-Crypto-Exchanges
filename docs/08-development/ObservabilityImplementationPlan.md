# Observability Implementation Plan

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Observability Implementation Plan |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 11 |
| Last Updated | 2026-09-11 |

---

## Purpose

Plan logs/metrics/traces aligned to ObservabilityArchitecture and NFRs. **No Prometheus/Grafana config files in Phase 11.**

---

## Signals

| Signal | Plan |
|--------|------|
| Structured logs | JSON; correlationId; organizationId; actor where safe |
| Metrics | RED for APIs; domain SLIs; queue lag; AI latency/cost |
| Traces | OTel across web→BFF→services→AI |
| Audit events | Distinct from debug logs (CORE/ADMIN) |
| Health | CORE/liveness readiness per deployable |

---

## NFR Mapping (Examples)

| NFR | Observability |
|-----|---------------|
| NFR-PERF-001/002/003 | RISK/API/queue latency histograms |
| NFR-PERF-006 | AI assist latency + timeout rate |
| NFR-RES-003 | AI degraded mode counters |
| NFR-AUD-* | Audit query success; tamper alerts (later) |
| Event lag | Consumer lag gauges |

---

## Domain Highlights

| Area | Metrics |
|------|---------|
| RISK | Score latency; publish success |
| ALERT/INVEST | Mutation rates; assign/close |
| AI | Tokens/cost; tool failures; hallucination eval (batch) |
| Events | Outbox backlog; DLQ depth |
| DB | Connections; slow queries |
| SSE | Connected clients; fallback poll rate |

---

## Direction (Candidate)

OpenTelemetry + Prometheus + Grafana (DevOps plan) — confirm under ADR-007.

---

## Verification

Golden signals present in M12 hardening checklist; no silent drop of audit.

---

## Related Documents

- [../03-architecture/ObservabilityArchitecture.md](../03-architecture/ObservabilityArchitecture.md)
- [DevOpsImplementationPlan.md](DevOpsImplementationPlan.md)
