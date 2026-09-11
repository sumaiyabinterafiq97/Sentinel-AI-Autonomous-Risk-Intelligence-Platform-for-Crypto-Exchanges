# Phase 4 — Contract Gap Report

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Phase 4 Contract Gap Report |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 4 |
| Last Updated | 2026-09-03 |

---

## 1. Baseline

| Metric | Phase 3 Exit | Phase 4 Exit | Delta |
|--------|--------------|--------------|-------|
| API inventory operations | 89 (66 MVP, 23 V2) | 89 (unchanged) | — |
| OpenAPI HTTP operations | ~20 | 75 (73 MVP, 2 V2 samples) | +55 |
| MVP OpenAPI coverage | ~30% | **100%** inventory IDs | Resolved |
| Event envelope schema | Documented | Hardened + broker architecture | Enhanced |
| ADRs | 001–014 | 001–018 | +4 |
| Migration specs | None | CORE + AUTH logical specs | New |
| CI contract validation | None | Design document only | New |
| Open architecture questions | P3-OQ-001–003, BQ-4 | Resolved/documented | — |

**Validation performed:** Python YAML parse of OpenAPI; inventory ID reconciliation; `git diff --check`; no application code introduced.

---

## 2. API Gaps

| ID | Issue | Classification | Status |
|----|-------|----------------|--------|
| API-GAP-001 | Phase 3 OpenAPI covered ~20 of 66 MVP ops | **BLOCKING** (Phase 3) | **RESOLVED** — full MVP coverage |
| API-GAP-002 | V2 operations (21 of 23) not in OpenAPI | NON-BLOCKING | Deferred to pre-V2 contract phase |
| API-GAP-003 | `x-nfr` extensions not on all MVP operations | NON-BLOCKING | Partial; CI design specifies future enforcement |
| API-GAP-004 | DASH SSE stream endpoint not explicitly in inventory row | INFORMATIONAL | Covered by DASH-FR-011 / ADR-016; may add `API-DASH-007` in Phase 5 |
| API-GAP-005 | Inventory summary (66) vs table rows (67) count mismatch | INFORMATIONAL | Multi-method rows; reconciled in OpenAPI |

---

## 3. Event Gaps

| ID | Issue | Classification | Status |
|----|-------|----------------|--------|
| EVT-GAP-001 | Not all event payloads have formal JSON Schema files | NON-BLOCKING | Envelope + representative payloads in EventContracts.md |
| EVT-GAP-002 | AsyncAPI artifact not created | NON-BLOCKING | Deferred; EventContracts.md authoritative for MVP |
| EVT-GAP-003 | Schema registry deployment undefined | INFORMATIONAL | By design — governance doc only (Phase 4) |
| EVT-GAP-004 | SEC V2 event schemas not expanded | NON-BLOCKING | V2 scope; locked publish/consume preserved |

**No boundary violations found:** SEC does not consume `DeviceSignalReceived` or `EvidenceAttached`. RISK does not publish alert lifecycle events.

---

## 4. Data Gaps

| ID | Issue | Classification | Status |
|----|-------|----------------|--------|
| DATA-GAP-001 | Initial migrations only for CORE + AUTH | NON-BLOCKING | Other domains specified in DataArchitecture; specs Phase 5 |
| DATA-GAP-002 | `DataRetention.md` skeleton not populated | NON-BLOCKING | ADR-018 defaults; NFR-OQ-002 open |
| DATA-GAP-003 | Executable SQL migrations not created | INFORMATIONAL | By Phase 4 scope constraint |
| DATA-GAP-004 | Cross-schema FK prohibition — enforcement in CI TBD | NON-BLOCKING | ContractValidationCI.md defines future check |

---

## 5. NFR Gaps

| ID | Issue | Classification | Status |
|----|-------|----------------|--------|
| NFR-GAP-001 | NFR-OQ-002 jurisdiction retention overrides | NON-BLOCKING | ADR-018 defaults; compliance sign-off pending |
| NFR-GAP-002 | NFR-OQ-001 production SLO approval | NON-BLOCKING | Pre-production gate |
| NFR-GAP-003 | Measurable SLOs not embedded in OpenAPI | INFORMATIONAL | NFR document authoritative |

---

## 6. Traceability Gaps

| ID | Issue | Classification | Status |
|----|-------|----------------|--------|
| TRACE-GAP-001 | Phase 3 traceability was sample-only | **BLOCKING** (Phase 3) | **RESOLVED** — Phase4Traceability.md |
| TRACE-GAP-002 | Event-only FRs lack HTTP mapping | INFORMATIONAL | Documented as intentional |
| TRACE-GAP-003 | Automated traceability CI not implemented | NON-BLOCKING | Design in ContractValidationCI.md |

---

## 7. Boundary Violations Discovered

| Check | Result |
|-------|--------|
| RISK → ALERT lifecycle | ✅ No violation |
| AI → domain lifecycle ownership | ✅ No violation |
| ADMIN → USER/ORG duplication | ✅ Orchestration only |
| SEC event contract lock | ✅ Preserved |
| Frozen domain FR substantive edits | ✅ None in Phase 4 |
| V2/V3 leakage into MVP OpenAPI | ✅ None (2 V2 samples only) |

**Violations found:** None.

---

## 8. Decisions Made

| ID | Decision | ADR / Doc |
|----|----------|-----------|
| GD-001 / BQ-4 | REPORT is V2; not MVP gate dependency | GovernanceDecisions.md |
| P3-OQ-001 | SSE for DASH MVP refresh | ADR-016 |
| P3-OQ-002 | DASH BFF for browser; direct S2S | ADR-017 |
| P3-OQ-003 | Tiered default retention targets | ADR-018 |
| MSG-001 | Transactional Outbox + Durable Log pattern | ADR-015 |
| GOV-001 | API contract governance baseline | APIContractGovernance.md |
| SCH-001 | Schema registry governance model | SchemaRegistryGovernance.md |

---

## 9. Decisions Deferred

| ID | Topic | Reason | Target |
|----|-------|--------|--------|
| DEF-001 | Exact message broker product (Kafka vs Redpanda vs managed) | Implementation phase | Phase 11 |
| DEF-002 | Full V2 OpenAPI (21 remaining ops) | V2 not MVP gate | Pre-V2 |
| DEF-003 | AsyncAPI parallel artifact | Optional tooling | Phase 5 |
| DEF-004 | Jurisdiction retention table | NFR-OQ-002 stakeholder input | Compliance workshop |
| DEF-005 | BQ-4 human product owner sign-off | Governance recorded only | Product approval |
| DEF-006 | LLM provider | AI implementation | Phase 8+ |

---

## 10. Remaining Blockers (Application Development Gate)

| Blocker | Gate item | Classification |
|---------|-----------|----------------|
| Application Development Gate not satisfied | Section 10 ProjectRoadmap | **BLOCKING** |
| PRD not consolidated | Phase 5 (roadmap numbering) | **BLOCKING** |
| Full domain migration specs beyond CORE/AUTH | Phase 7 | NON-BLOCKING for gate start planning |
| Executable CI workflows not created | Phase 11+ | NON-BLOCKING |
| Human approval on phase reports | Multiple phases | NON-BLOCKING (process) |
| NFR-OQ-002 jurisdiction retention | Compliance | NON-BLOCKING for MVP dev start planning |

**Phase 4-specific blockers:** None. Phase 4 exit criteria for contract hardening are met with documented non-blocking gaps.

---

## 11. Recommendations for Phase 5

1. **Expand initial migration specifications** for AUTHZ, USER, ORG (identity foundation).
2. **Formalize remaining event payload JSON Schemas** for MVP publish/consume matrix.
3. **Populate DataRetention.md** from ADR-018 defaults with compliance review.
4. **Add DASH SSE operation** to API inventory and OpenAPI explicitly.
5. **Implement ContractValidationCI** as executable pipeline (Phase 11).
6. **Obtain BQ-4 product owner sign-off** on REPORT V2 exclusion.
7. **PRD consolidation** per master roadmap Phase 5.

---

## 12. Application Development Gate Impact

| Gate criterion | Phase 4 contribution | Satisfied? |
|----------------|---------------------|------------|
| API / Event Contracts (roadmap #10) | MVP OpenAPI 100%; governance; events hardened | **Partial** — contracts ready; formal phase approval pending |
| Database Design (roadmap #8) | CORE/AUTH migration specs | **Partial** |
| NFR (roadmap #5) | Retention defaults via ADR-018 | **Partial** — NFR doc exists; OQ-002 open |
| Overall Application Development Gate | — | **NOT SATISFIED** |

Phase 4 removes the **MVP OpenAPI coverage blocker** identified in Phase 3. Application development remains blocked by upstream roadmap phases (PRD, full architecture approval, implementation plan) per ProjectRoadmap.md Section 10.

---

## Related Documents

- [Phase4Report.md](Phase4Report.md)
- [Phase4Traceability.md](../08-development/Phase4Traceability.md)
- [ProjectRoadmap.md](ProjectRoadmap.md)
