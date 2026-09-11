# AI Security Model

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | AI Security Model |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 8 |
| Last Updated | 2026-09-11 |

---

## Purpose

Document AI-specific threats and mitigations for Sentinel AI. **Architecture/documentation only** — no executable controls in Phase 8.

---

## Threat Matrix

| Threat | Affected component | Mitigation | Detection | Fallback | Audit |
|--------|-------------------|------------|-----------|----------|-------|
| Prompt injection | Agent prompts / model | Untrusted retrieved content; instruction hierarchy; allowlist tools | Anomalous tool requests | Deny tool; fail closed on high risk | Log prompt/tool traces (redacted) |
| Indirect prompt injection | Retrieved docs | Sanitize/segment retrieved text; no tool from untrusted instructions | Content policy heuristics | Drop chunk; continue without it | Retrieval provenance |
| Tool abuse | Tool layer | Allowlist + AUTHZ per call (AI-FR-008) | Denied call spikes | Block agent run | Denial audit |
| Unauthorized tool invocation | Tool layer | Permission + agent allowlist | AuthZ denies | 403 path | Audit |
| Data exfiltration | Model outputs / tools | Output filtering; minimize PII in context; no secrets in prompts | DLP-style pattern checks (future) | Redact / fail | Audit |
| Cross-tenant retrieval | Retrieval / pgvector | organization_id filters mandatory | Cross-tenant test suite | Hard fail | Security audit |
| Malicious retrieved content | RAG | Treat as data not instructions | Injection detectors | Quarantine source | Provenance |
| Model hallucination | Responses | Grounding requirements (AI-FR-005) | Eval sampling | Ask human verify | Recommendation ID |
| Fabricated evidence | Responses | Require source refs; forbid invented IDs | Golden negative tests | Reject ungrounded claims in UI labeling | Audit |
| Prompt leakage | Outputs / logs | Redact system prompts in logs; least privilege log access | Log review | Rotate prompts | Restricted logs |
| Sensitive information disclosure | Outputs | Classification-aware context assembly | Classification violations | Omit fields | Audit |
| Excessive agency | Orchestration | No write tools to domain lifecycles | Mutation monitors | Kill switch / disable AI flag | CORE audit |
| Runaway loops | Orchestration | Max tool iterations; timeouts | Loop counters | Abort run | agent_runs |
| Cost abuse | Provider calls | Budgets/rate limits (config) | Usage telemetry | Throttle / deny | Usage logs |
| Model/provider failure | Model layer | Timeout, fallback, degraded mode | Error rates | Non-AI workflows continue | agent_runs failed |

---

## Related Documents

- [AIArchitecture.md](../03-architecture/AIArchitecture.md)
- [SecurityArchitecture.md](../03-architecture/SecurityArchitecture.md)
- [NonFunctionalRequirements.md](../02-requirements/NonFunctionalRequirements.md) — NFR-SEC-009, NFR-SEC-010
