# Phase 12 M12 Kickoff (inventory only)

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | M12 Hardening kickoff / baseline inventory |
| Date | 2026-09-12 |
| Status | **PREPARED — NOT STARTED.** No M12 implementation in this document. |
| Authority | [ImplementationPlan.md](../08-development/ImplementationPlan.md) §8 M12; [Phase12M11Handoff.md](Phase12M11Handoff.md) |
| Prerequisite | M11 **CLOSED** (GD-007). Phase 1 Vision/ProductScope committed separately. |

---

## Authorization

| Field | Value |
|-------|-------|
| M11 | **CLOSED** / decided-or-deferred |
| M12 implementation | **NOT AUTHORIZED by this kickoff** — inventory only |
| Next coding session | Do not begin until an explicit M12 implementation authorization |

This file is the **baseline/inventory** step. It does not start security hardening, NFR harnesses, CI workflows, or live E2E.

---

## Planned sequence (after authorization)

```text
M11 CLOSED
   ↓
Phase 1 docs commit (done)
   ↓
Push M11 + Phase 1 (done if origin matches)
   ↓
M12 authorization (implementation)  ← you are before this
   ↓
This inventory (done)
   ↓
NFR measurement
   ↓
Security hardening
   ↓
Performance measurement
   ↓
Observability / SLOs
   ↓
Contract CI activation
   ↓
M12 validation
```

---

## What M12 is (ImplementationPlan)

| Milestone | Work |
|-----------|------|
| **M12 \| Hardening** | Security, performance, observability SLOs, contract CI activation |

Supporting plans:

- [TestingImplementationPlan.md](../08-development/TestingImplementationPlan.md) — M12 = perf/resilience gates; E2E staging is M11+ *if environment exists*
- [SecurityImplementationPlan.md](../08-development/SecurityImplementationPlan.md)
- [ObservabilityImplementationPlan.md](../08-development/ObservabilityImplementationPlan.md)
- [ContractValidationCI.md](../08-development/ContractValidationCI.md)
- [DevOpsImplementationPlan.md](../08-development/DevOpsImplementationPlan.md)
- [APIAndDataTestingStrategy.md](../08-development/APIAndDataTestingStrategy.md)

MVP completion (ImplementationPlan §14) still requires **NFR simulation targets measured where defined**. That measurement is M12 work, not a new product API.

---

## Explicitly out of M12 (do not start here)

Do **not** install or build these as the first M12 move:

| Item | Why |
|------|-----|
| Playwright / fabricated live E2E | Environment missing; documented M11 dependency — do not change M11 SPA to fake it |
| Docker / Kubernetes | DevOps plan; not automatic M12 entry |
| Kafka / production broker | Deferred (outbox simulation remains) |
| WebSocket | ADR-016: SSE is MVP; WS is V2 |
| TanStack Query / Zod / React Hook Form | **GD-007 closed — no work** |
| COMP list GET | **GD-007 closed — no work** |
| Dedicated global search API | **GD-007 closed — V2** |
| SEC / REPORT / OPS / WALLET | V2 product |
| API-ADMIN-006, API-AI-006/007 | Not MVP M12 |
| GD-002 events | Deferred unless authorized |
| UX-OQ-PKG-STATUS | Remains open; do not invent |

---

## Current inventory (what exists vs what M12 still needs)

### Already present (M0–M11)

| Area | Evidence |
|------|----------|
| Contract parse check | `python3 contracts/validate.py` (OpenAPI parse, JSON schemas, SEC event lock) — **local**, not GitHub Actions |
| Service tests | `backend/gradlew test`; `ai-service` pytest/ruff; `web` vitest + eslint |
| Security (partial) | HMAC Bearer, AUTHZ permissions, tenant `organizationId`, AI tool allowlist tests, PermissionGuard |
| Health | Deployable `/health` skeletons |
| SSE + poll | DASH API-DASH-007 + 15s fallback |
| Outbox | In-process simulation + metrics counters (not a production broker) |

### Missing for M12 hardening

| Track | Gap |
|-------|-----|
| **Running services / harness** | No docker-compose, no staging mesh, no documented one-command multi-service up |
| **NFR measurement** | No recorded P95/P99 against NFR-PERF-001–006/008/009, NFR-SCAL-*, NFR-RES-* in a simulation environment |
| **Security hardening** | No edge rate limits (NFR-SEC-006), no TLS/HSTS/CSP at edge (NFR-SEC-003; local HTTP OK), no gateway ADR (P11-OQ-SEC-001) |
| **Observability / SLOs** | No OTel/Prometheus/Grafana; NFR-OBS-002/005 not evidenced; NFR-OBS-005 SLOs “documented before MVP gate” still need a **measured** hardening checklist |
| **Contract CI** | No `.github/workflows`; ContractValidationCI V1–V5 not activated on PRs |
| **Live E2E** | No Playwright; no compose/staging — **dependency**, not a reason to edit M11 `web/` |
| **Perf/resilience gates** | TestingImplementationPlan M12 items not executed |

---

## NFR measurement set (MVP — measure, do not invent new FRs)

Priority simulation targets when a harness exists (from NonFunctionalRequirements + APIAndDataTestingStrategy):

| ID | Target (simulation) | M12 note |
|----|---------------------|----------|
| NFR-PERF-001 | Ingest → RiskCalculated P95 ≤ 2s | Needs ingest + RISK path |
| NFR-PERF-002 | API read P95 ≤ 300 ms (100 analysts) | Needs load tool + running APIs |
| NFR-PERF-003 | Dashboard load | DASH workspace |
| NFR-PERF-006 | AI assist latency / timeout | Must not block RISK (NFR-RES-001 / NFR-AI-001) |
| NFR-RES-001 | AI down; triage still works | Chaos/disable AI |
| NFR-RES-004 | Duplicate delivery idempotency | Events |
| NFR-SEC-002 | 403 without permission | Already unit/API-tested; keep as regression |
| NFR-AUD-001 | Audit on alert close | Regression |
| NFR-OBS-004 | Health per deployable | Present; confirm all deployables |
| NFR-OBS-005 | SLOs for auth, risk, alert, case | Define+measure in hardening checklist — do not claim production |

V2 NFRs (e.g. NFR-PERF-007 reports, NFR-AI-003 eval pass rate, NFR-OPS-001) are **not** M12 product scope.

NFR-OQ-002 (jurisdiction retention) remains **open**; use simulation defaults.

---

## Suggested M12 workstreams (when authorized — still not started)

1. **Baseline** — this document; confirm no GD-007 reverse-work
2. **NFR measurement** — pick a **local multi-process** or later compose harness; record numbers; do not invent APIs
3. **Security** — rate limiting, secure headers at edge, secret-scan in CI, remaining AUTHZ/tenant gaps — **no SEC V2 features**
4. **Performance** — PERF-001/002/006 first; stop if environment is absent and record blocker
5. **Observability** — structured log/correlation completeness; optional OTel **only if** an implementation ADR covers it; SLO checklist
6. **Contract CI** — GitHub Action wrapping existing `contracts/validate.py` + inventory checks (ContractValidationCI V1–V3 first)
7. **Validation** — re-run M0–M11 suites; add only tests that match an existing environment

Live E2E is **optional later** in this milestone if (and only if) identity + ops + AI + platform + DASH + web can be run together. If they cannot, keep the documented dependency.

---

## Related Documents

- [Phase12M11Handoff.md](Phase12M11Handoff.md)
- [Phase12M11OpenQuestionDecisions.md](Phase12M11OpenQuestionDecisions.md) — GD-007
- [ImplementationPlan.md](../08-development/ImplementationPlan.md)
- [NonFunctionalRequirements.md](../02-requirements/NonFunctionalRequirements.md)
