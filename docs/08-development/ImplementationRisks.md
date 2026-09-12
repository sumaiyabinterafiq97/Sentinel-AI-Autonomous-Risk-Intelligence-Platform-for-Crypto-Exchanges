# Implementation Risks

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Implementation Risks |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 11 |
| Last Updated | 2026-09-11 |

---

## Purpose

Realistic engineering risks for MVP implementation. Not a business threat model.

---

## Risk Register

| ID | Description | Impact | Likelihood | Mitigation | Detection | Owner | Phase |
|----|-------------|--------|------------|------------|-----------|-------|-------|
| P11-R001 | Cross-domain coupling / shared DB writes | High | Med | Schema-per-domain; no cross-schema FK; code review | Architecture tests | Architect | M1+ |
| P11-R002 | Scope creep / V2 into MVP | High | Med | Release tags; gate reviews | Inventory audit | TPM | All |
| P11-R003 | AI latency / timeouts | Med | High | NFR-PERF-006; degrade UX | AI latency metrics | AI Lead | M8 |
| P11-R004 | AI hallucination / fabricated evidence | High | Med | Citations; eval; disclaimers | Hallucination tests | AI Lead | M8 |
| P11-R005 | Event delivery failure / lag | High | Med | Outbox; DLQ; lag alerts | Consumer lag | Backend | M3+ |
| P11-R006 | Duplicate processing | High | Med | Idempotency keys; consumer dedupe | Dup metrics | Backend | M3+ |
| P11-R007 | Tenant isolation failure | Critical | Low | Mandatory org filters; tests | Security suite | Security | M2+ |
| P11-R008 | Migration errors | High | Med | Spec-first; expand/contract; non-prod drills | Migration CI | Data | M1+ |
| P11-R009 | Contract drift | High | Med | OpenAPI/event CI; governance | Contract diff fail | API | M0+ |
| P11-R010 | FE/BE mismatch | Med | Med | Shared OpenAPI client; WF tests | E2E fail | FE/BE | M11 |
| P11-R011 | Observability gaps | Med | Med | Golden signals checklist | Blind incidents | Ops | M12 |
| P11-R012 | Test coverage gaps | High | Med | Milestone exit criteria | Coverage reports | QA | All |
| P11-R013 | Perf targets missed | High | Med | Early RISK/ALERT load tests | Perf dashboards | Backend | M4+ |
| P11-R014 | Excessive microservice complexity | Med | Med | Cap 3–6 deployables | Deployable count | Architect | M0 |
| P11-R015 | Coding before human gate approvals | Critical | Med | Gate checklist honesty | Process audit | TPM | Gate |
| P11-R016 | COMP list API gap slows FE | Low | Med | Accepted known-ID queue; no invented list GET | **CLOSED** GD-007 | Product | M7/M11 |
| P11-R017 | Stack choice vs ADR-007 ambiguity | Med | Med | Implementation ADR before code | ADR-019 + GD-007 frontend lock | Architect | Gate |

---

## Related Documents

- [ImplementationPlan.md](ImplementationPlan.md)
- [Phase11GateAssessment.md](../00-project/Phase11GateAssessment.md)
