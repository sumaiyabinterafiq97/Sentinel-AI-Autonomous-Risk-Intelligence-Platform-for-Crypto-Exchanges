# Evaluation Framework

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | AI Evaluation Framework |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 8 |
| Last Updated | 2026-09-11 |
| Authority | AI-FR-001–009; NFR-PERF-006; AI FRS intentional deferrals |

---

## Purpose

Define how Sentinel AI will evaluate assistive AI quality and safety.

**Note:** Full evaluation runtime / `AIEvaluationCompleted` is **Version 2 deferred** per AI FRS. This document defines MVP **architecture and proposed simulation targets**, not achieved production metrics.

---

## Evaluation Dimensions

| Dimension | Description | MVP method |
|-----------|-------------|------------|
| Correctness | Output useful for analyst task | Human review golden cases |
| Groundedness | Claims tied to sources | Citation checks |
| Evidence quality | Relevant retrieved items | Retrieval precision sampling |
| Hallucination rate | Fabricated facts/IDs | Adversarial + golden negatives |
| Tool selection | Correct tools chosen | Trace review |
| Tool-call correctness | Args/tenant/authz valid | Automated contract tests (future CI) |
| Policy compliance | No lifecycle mutation | Integration negative tests |
| Refusal behavior | Unauthorized requests denied | AuthZ deny cases |
| Latency | Assist path timing | NFR-PERF-006 simulation |
| Reliability | Success vs timeout/fail | agent_runs metrics |
| Cost | Token/usage | Telemetry dashboards (future) |
| Regression | Prompt/model changes | Version comparison suites |
| Prompt-version comparison | A vs B prompts | Offline eval sets (V2 runtime) |

---

## Dataset Types

| Type | Purpose |
|------|---------|
| Golden cases | Expected grounded assists |
| Adversarial cases | Injection, jailbreak, exfil attempts |
| Regression suites | Lock quality across prompt versions |
| Policy cases | Forbidden action attempts |

---

## Proposed Acceptance Targets (Simulation — Not Achieved Claims)

| Metric | Proposed target | Status |
|--------|-----------------|--------|
| Interactive assist P95 | ≤ 5s | NFR-PERF-006 simulation target |
| Assist timeout | 10s fallback | NFR-PERF-006 |
| Lifecycle mutation by AI | 0 incidents | Hard requirement |
| Unauthorized tool success | 0 | Hard requirement |
| Groundedness (sampled) | TBD human baseline | PENDING measurement methodology |

---

## Human vs Automated Evaluation

| Mode | Use |
|------|-----|
| Human evaluation | Qualitative usefulness, groundedness sampling |
| Automated | AuthZ denials, schema, no-mutation checks, latency harness |
| Hybrid | Prompt regression before activation |

---

## Related Documents

- [AIArchitecture.md](../03-architecture/AIArchitecture.md)
- [HallucinationTesting.md](HallucinationTesting.md)
- [NonFunctionalRequirements.md](../02-requirements/NonFunctionalRequirements.md)
