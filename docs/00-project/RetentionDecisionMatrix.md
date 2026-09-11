# Retention Decision Matrix

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Retention Decision Matrix |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 8 |
| Last Updated | 2026-09-11 |
| NFR-OQ-002 | **HUMAN-DECISION-RESOLVED for MVP gate** — legal matrix fill remains FUTURE; simulation defaults authoritative |
| Authority | ADR-018; DataRetention.md; RetentionJurisdictionGovernance.md |

---

## Disposition of NFR-OQ-002

| Field | Value |
|-------|-------|
| Question | Exact retention periods per jurisdiction |
| Formal disposition | **HUMAN-DECISION-RESOLVED for Application Development Gate** (2026-09-11) — Project Owner confirmed non-blocking for MVP |
| Production jurisdiction claims | Still **FUTURE** — require compliance/legal before asserting regulatory periods |
| MVP implementation posture | ADR-018 / this matrix **MVP SIMULATION TARGET** defaults remain authoritative |
| Safe MVP posture | Use **MVP SIMULATION TARGET** defaults (ADR-018) only |
| Jurisdiction matrix entries in-repo | **None approved** |
| Legal compliance claim | **None** |

---

## Decision Matrix

| Data category | Default online | Default archive | Legal hold | Deletion eligibility | Tenant override | Jurisdiction override | Label |
|---------------|----------------|-----------------|------------|----------------------|-----------------|----------------------|-------|
| Ephemeral cache (Redis) | TTL minutes–hours | None | N/A | Automatic TTL | No | N/A | MVP SIMULATION TARGET |
| Operational telemetry | 90 days | Optional | Rare | After window | Limited | PENDING COMPLIANCE / LEGAL | MVP SIMULATION TARGET |
| Application logs (non-PII) | 180 days | 1 year | Possible | After window | Limited | PENDING COMPLIANCE / LEGAL | MVP SIMULATION TARGET |
| Auth sessions / auth events | 2 years | 7 years cold | Yes | Restricted | No for audit floor | PENDING COMPLIANCE / LEGAL | MVP SIMULATION TARGET |
| CORE audit_records | 2 years online | 7 years cold | Yes | **No hard delete default** | No | PENDING COMPLIANCE / LEGAL | MVP SIMULATION TARGET |
| Risk assessments / rule hits | 2 years | 5 years | Yes | After archive + hold check | Org config within bounds | PENDING COMPLIANCE / LEGAL | MVP SIMULATION TARGET |
| Alerts | 2 years | 5 years | Yes | After archive + hold check | Org config within bounds | PENDING COMPLIANCE / LEGAL | MVP SIMULATION TARGET |
| Investigation cases / evidence metadata | 7 years | Extended on hold | Yes | Hold-aware | Org config within bounds | PENDING COMPLIANCE / LEGAL | MVP SIMULATION TARGET |
| Compliance evidence (KYC/AML/sanctions/audit pkgs) | 7 years | Extended on hold | Yes | Hold-aware | Org config within bounds | PENDING COMPLIANCE / LEGAL | MVP SIMULATION TARGET |
| AI recommendations / prompts / agent_runs | 1 year | 2 years | Possible | Anonymize where policy allows | Limited | PENDING COMPLIANCE / LEGAL | MVP SIMULATION TARGET |
| Security telemetry (SEC V2) | 2 years | 5 years | Yes | After archive + hold | Org config | PENDING COMPLIANCE / LEGAL | V2 design target |
| REPORT artifacts (V2) | 7 years | Hold extends | Yes | Hold-aware | Org config | PENDING COMPLIANCE / LEGAL | V2 design target |

---

## Policy Distinctions

| Type | Meaning |
|------|---------|
| MVP SIMULATION TARGET | Engineering default for design/implementation planning — **not** regulatory compliance |
| PENDING COMPLIANCE / LEGAL DECISION | Requires human legal/compliance input before production jurisdiction claims |
| Configurable org override | Allowed within approved bounds; audited |
| Jurisdiction override | Empty until approved schedule recorded |

---

## Archival and Legal Hold

| Concept | Status |
|---------|--------|
| Archival | Defined conceptually in DataRetention.md |
| Legal hold | Concept defined; process placeholder |
| Cross-jurisdiction conflict | Most restrictive rule — framework only |

---

## Related Documents

- [RetentionJurisdictionGovernance.md](../04-database/RetentionJurisdictionGovernance.md)
- [DataRetention.md](../04-database/DataRetention.md)
- [GovernanceDecisions.md](GovernanceDecisions.md)
