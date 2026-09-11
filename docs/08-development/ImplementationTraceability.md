# Implementation Traceability

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Implementation Traceability |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 11 |
| Last Updated | 2026-09-11 |

---

## Purpose

FR → API → event → data → backend module → AI → UI → test → NFR → observability for MVP domains.

**Legend:** COMPLETE (planned) · PARTIAL · UNRESOLVED · DEFERRED · V2

Coverage is **planning completeness**, not implemented code.

---

## MVP Domain Matrix (Summary)

| Domain | FR | APIs | Events | Migration | Module | UI | Tests | Status |
|--------|----|------|--------|-----------|--------|-----|-------|--------|
| CORE | CORE-FR | API-CORE-* | Config/Flag | 001 | platform | SCR-13 | Contract+API | COMPLETE |
| AUTH | AUTH-FR | API-AUTH-* | Login/Session | 002 | identity | SCR-00 | Security | COMPLETE |
| AUTHZ | AUTHZ-FR | API-AUTHZ-* | — | 003 | identity | SCR-12 | Security | COMPLETE |
| USER | USER-FR | API-USER-* | UserUpdated | 004 | identity | SCR-12 | API | COMPLETE |
| ORG | ORG-FR | API-ORG-001–003 | — | 005 | identity | SCR-12 | Tenant | COMPLETE |
| RISK | RISK-FR | API-RISK-* | Risk* | 006 | ops | SCR-06–07 | Perf+Event | COMPLETE |
| ALERT | ALERT-FR | API-ALERT-001–007 | Alert* | 007 | ops | SCR-02–03 | API | COMPLETE |
| INVEST | INVEST-FR | API-INVEST-* | Case*/Evidence | 008 | ops | SCR-04–05 | Audit | COMPLETE |
| COMP | COMP-FR | API-COMP-* | Comp* | 009 | ops | SCR-08–10 | Audit | PARTIAL (list API gap) |
| AI | AI-FR-001–009 | API-AI-001–005 | AI* | 010 | ai | SCR-15 | AI suites | COMPLETE |
| ADMIN | ADMIN-FR | API-ADMIN-* | Admin* | 011 | platform | SCR-12–14 | Orchestration | COMPLETE |
| DASH | DASH-FR | API-DASH-* MVP | consumes | 012 | dash | SCR-01 | SSE | COMPLETE |

---

## Critical Flow Examples

### Alert triage (WF-1)

ALERT-FR → API-ALERT-* / API-DASH-003 / API-RISK-003 / API-AI-002 → Alert* events → alert+risk schemas → ops+dash+ai → SCR-02/03/15 → API+FE+a11y tests → NFR-PERF-003/EXPL-001 → API latency + SSE metrics

**Status:** COMPLETE (planned)

### AI investigation assist (WF-7)

AI-FR-001 → API-AI-001/004 → AIRecommendationGenerated → ai schema → sentinel-ai → SCR-15 → AI boundary tests → NFR-PERF-006/RES-003 → AI latency/cost

**Status:** COMPLETE (planned) — must prove no INVEST mutation

### Compliance (WF-4)

COMP-FR → API-COMP-* → Comp events → 009 → ops → SCR-08/09 → audit tests → NFR-AUD → audit metrics

**Status:** PARTIAL — UX-OQ-COMP-LIST unresolved

---

## V2

| Domain | Status |
|--------|--------|
| WALLET / SEC / REPORT / OPS | V2 — not MVP implementation trace |

---

## Unresolved

| Item | Classification |
|------|----------------|
| COMP list GET | UNRESOLVED |
| Global search API | UNRESOLVED (compose) |
| GD-002 events | DEFERRED |
| Human gate approvals | PENDING HUMAN APPROVAL |
| Stack ADRs | PENDING (P11-OQ-STACK-*) |

---

## Related Documents

- [Phase10Traceability.md](Phase10Traceability.md)
- [Phase9Traceability.md](Phase9Traceability.md)
- [ModuleImplementationPlan.md](ModuleImplementationPlan.md)
