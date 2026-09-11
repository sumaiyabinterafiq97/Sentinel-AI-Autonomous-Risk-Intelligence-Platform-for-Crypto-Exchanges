# DevOps Implementation Plan

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | DevOps Implementation Plan |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 11 |
| Last Updated | 2026-09-11 |

---

## Purpose

Plan future local/CI/CD/runtime operations. **Do not create Dockerfiles, Compose, K8s, or GitHub Actions in Phase 11.**

---

## Planned vs Current

| Item | Current repo | Planned (post-gate) |
|------|--------------|---------------------|
| Docker / Compose | Not present | Local multi-service + deps |
| GitHub Actions | Not present | Lint, contract, test, build |
| K8s / Terraform | Not present | Later (roadmap Phase 11/16 decision) |
| Prometheus/Grafana/OTel | Not present | Observability stack |
| NGINX | Not present | Edge TLS/headers |

---

## Direction (Candidate)

Docker, Docker Compose, GitHub Actions, Prometheus, Grafana, OpenTelemetry, NGINX — subject to ADR-007 implementation ADRs.

---

## Planned Capabilities

| Area | Plan |
|------|------|
| Local dev | Compose: Postgres, Redis, broker, services, web, AI |
| Config | Env per environment; no secrets in git |
| Secrets | Vault/CI secrets / cloud secret manager |
| CI/CD | PR: contracts+unit; main: images; staged deploy |
| Artifacts | Container images + SBOM (later) |
| Migrations | Job per release; forward-only prod |
| Rollback | Prior image + expand/contract schema discipline |
| Health | `/health` readiness/liveness |
| Backups/DR | Postgres backup policy; RPO/RTO from NFR when set |

---

## Gate Note

Deployment readiness documentation exists as **plan**; actual pipelines remain **NOT STARTED** until after Application Development Gate.

---

## Open Questions

| ID | Item |
|----|------|
| P11-OQ-OPS-001 | K8s timing vs Compose-only MVP |
| P11-OQ-OPS-002 | Cloud provider selection |

---

## Related Documents

- [ObservabilityImplementationPlan.md](ObservabilityImplementationPlan.md)
- [ContractValidationCI.md](ContractValidationCI.md)
- [../00-project/ProjectRoadmap.md](../00-project/ProjectRoadmap.md)
