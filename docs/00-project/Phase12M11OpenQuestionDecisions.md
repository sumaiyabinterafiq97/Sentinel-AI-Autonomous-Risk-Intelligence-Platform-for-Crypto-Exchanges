# Phase 12 M11 Open Question Decisions

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | M11 presentation-gap close-out |
| Date | 2026-09-12 |
| Status | **CLOSED** — documented decisions only; no speculative APIs or stack changes |
| Authority | APIInventory / OpenAPI (MVP COMP = API-COMP-001–007); FrontendUXArchitecture (no invented search API); ADR-007 / ADR-019; GD-006; M11 shipped `web/` |
| Related | [GD-007](GovernanceDecisions.md); [ADR-019 amendment](../03-architecture/ArchitectureDecisionRecords.md) |

---

## Purpose

Close the three M11-retained presentation questions **by decision**, not by inventing endpoints or adopting libraries “to finish the gap.” Frozen FRS/FDS/OpenAPI are unchanged.

| ID | Topic | Decision |
|----|--------|----------|
| UX-OQ-COMP-LIST | COMP list GET | **No dedicated COMP collection GET in MVP.** |
| UX-OQ-SEARCH / CL-OQ-002 | Global search API | **Composed domain list GETs are sufficient for MVP.** |
| P11-OQ-STACK-002 | TanStack Query / Zod / RHF | **Not adopted for MVP.** Lock what shipped. |

NFR measurement, live-mesh E2E, Kafka, and M12 hardening remain **out of this close-out**.

---

## 1. COMP list GET (UX-OQ-COMP-LIST)

### Facts

- MVP inventory and OpenAPI expose **API-COMP-001–007** only: start/complete/PATCH families. There is **no** `listCompliance*` / `GET /v1/compliance` collection operation.
- GD-006 and the Application Development Gate classified inventing a COMP list GET as **NON-BLOCKING — do not invent**.
- COMP-FR-006 (MVP) requires retrieve/discover of compliance records. Phase 9–11 never assigned an HTTP list ID. M7 persisted an index table; M11 queue UX uses **session-local known IDs after start** plus existing by-id mutations.

### Decision

**The upstream COMP list GET will not exist in MVP.** Discovery for MVP is:

1. The identifier returned by an authorized **start** (POST) response.
2. Operator-known IDs (including session-local queue after start).
3. Existing by-id GET/PATCH/complete operations already in API-COMP-001–007.

COMP-FR-006 is **not** interpreted as a mandate to add a collection GET. Adding one would be a **new API ID** and requires a future authorized inventory + OpenAPI + FRS amendment.

### When a list API may exist

**After MVP**, or in a later **gated contract change**, if product explicitly adds a list operation to inventory/OpenAPI. It is **not** an M12 hardening item (M12 does not invent APIs).

### What was not done

No invented `GET /v1/compliance`, no DASH-only list façade, no OpenAPI rewrite.

---

## 2. Global search API (UX-OQ-SEARCH / CL-OQ-002)

### Facts

- DashboardScreens / FrontendUXArchitecture: SCR-11 **must not invent** a global search API; compose authorized list GETs.
- NFR-PERF-005 is a latency target for search/discovery UX, not a new `operationId`.
- CORE-FR-020 (“shared search framework”) is **Version 2** (FRS). No CORE/DASH search `operationId` exists in the MVP inventory.
- M11 SCR-11 already composes `/v1/alerts`, `/v1/investigations/cases`, and `/v1/risk/assessments` (as permitted).

### Decision

**Composed domain list GETs are the MVP search surface.** A dedicated global / federated search API is **not MVP**. Defer to **V2** (aligned with CORE-FR-020) if product later wants a single search contract.

### What was not done

No `GET /v1/search`, no DASH search aggregator ID, no OpenAPI addition.

---

## 3. Frontend stack lock (P11-OQ-STACK-002)

### Facts

- ADR-007: product docs stay vendor-neutral; stack lock is an implementation ADR.
- ADR-019 (Accepted 2026-09-11) listed frontend as React, TypeScript, Vite, Tailwind, **TanStack Query, React Hook Form, Zod**.
- M0–M11 `web/` shipped **React + TypeScript + Vite + Tailwind + native `fetch`** only. M11 explicitly did not add TanStack/RHF/Zod while STACK-002 was unconfirmed.

### Decision

**MVP frontend lock = what shipped:** React, TypeScript, Vite, Tailwind, and the platform `fetch` client through the DASH BFF.

**Do not adopt TanStack Query, React Hook Form, or Zod for MVP.** Treat those ADR-019 names as **candidates that were not implemented**. Optional later adoption needs a **follow-on ADR**, not a silent `package.json` add.

ADR-019 is **amended** on 2026-09-12 to match this lock (backend/AI/data rows unchanged).

### What was not done

No TanStack Query, Zod, or React Hook Form dependencies; no form-library rewrite.

---

## Status of related items that stay open

These are **not** GD-007 carry-overs into M12 as UX/stack work:

| Item | Status |
|------|--------|
| UX-OQ-PKG-STATUS | Remains **open** — OpenAPI shape for API-COMP-007; **do not invent** a solution |
| NFR-OQ-002 | Unchanged — jurisdiction retention |
| GD-002 events | **Deferred** — do not implement unless authorized |
| M12 NFR/SLO measurement | **M12 work** when separately authorized |
| Live Playwright / staging mesh | Remaining validation/hardening. **Environment not present** at M11 close-out (no Playwright, no docker-compose/live mesh). Do not fabricate infrastructure or change M11 `web/` for this. |

---

## Related Documents

- [GovernanceDecisions.md](GovernanceDecisions.md) — GD-007
- [ArchitectureDecisionRecords.md](../03-architecture/ArchitectureDecisionRecords.md) — ADR-019 amendment
- [FrontendImplementationPlan.md](../08-development/FrontendImplementationPlan.md)
- [DashboardScreens.md](../07-ui/DashboardScreens.md)
- [ComponentLibrary.md](../07-ui/ComponentLibrary.md)
- [Phase12M11Handoff.md](Phase12M11Handoff.md)
- [Phase12M11Report.md](Phase12M11Report.md)
