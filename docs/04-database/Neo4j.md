# Neo4j Graph Model

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Neo4j Graph Design |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 3 |
| Last Updated | 2026-09-03 |

---

## Purpose

Define graph projections for relationship intelligence. **Neo4j is not the source of truth** for relational domain entities (ADR-005, Data Architecture).

**Release:** Version 2 primary use (WALLET, INVEST graph exploration). MVP may omit Neo4j deployment.

---

## Role in Platform

| Use case | Value |
|----------|-------|
| Wallet address relationships | Fraud ring visualization (WALLET V2) |
| Transaction flow paths | Investigation context |
| Entity linkage (user ↔ wallet ↔ device) | Analyst exploration—not authoritative identity |
| Investigation graph view | INVEST/DASH read-only exploration (V2) |

---

## Ownership

| Aspect | Owner |
|--------|-------|
| Graph write pipeline | WALLET (V2) for wallet graph; INVEST for case-linked projections |
| Graph schema evolution | Wallet Intelligence team |
| Source of truth | PostgreSQL domain schemas |
| Sync direction | PostgreSQL/events → Neo4j (one-way) |

---

## Node Labels

| Label | Properties (conceptual) | Source |
|-------|-------------------------|--------|
| `WalletAddress` | `address`, `chain`, `organizationId`, `reputationScore`, `updatedAt` | WALLET |
| `Transaction` | `txId`, `organizationId`, `timestamp`, `amount`, `asset` | Ingest/RISK |
| `User` | `userId`, `organizationId` (reference only) | USER (ID ref) |
| `InvestigationCase` | `caseId`, `organizationId`, `status` | INVEST (ID ref) |
| `Device` | `deviceId`, `fingerprint` | AUTH/SEC context |

**Note:** `User` nodes store IDs only—profile data fetched from USER API.

---

## Relationship Types

| Relationship | From → To | Meaning |
|--------------|-----------|---------|
| `SENT_TO` | WalletAddress → WalletAddress | Transfer flow |
| `INTERACTED_WITH` | WalletAddress → Transaction | Participation |
| `ASSOCIATED_WITH` | User → WalletAddress | Known association |
| `LINKED_TO_CASE` | InvestigationCase → WalletAddress | Investigation context |
| `SHARED_DEVICE` | User → Device | Device linkage |
| `FLAGGED_SUSPICIOUS` | WalletAddress → WalletAddress | Derived suspicion (WALLET event) |

---

## Synchronization

```text
WALLET: WalletProfileUpdated / SuspiciousWalletDetected
    → Graph sync worker → MERGE nodes/relationships

INVEST: CaseCreated + evidence wallet refs
    → LINKED_TO_CASE edges

RISK: TransactionReceived (via ingest)
    → Transaction nodes, SENT_TO edges (optional V2)
```

| Aspect | Policy |
|--------|--------|
| Consistency | Eventual (seconds to minutes) |
| Idempotency | MERGE on stable keys (`address+chain`, `txId`) |
| Deletion | Soft-unlink; rebuild from PostgreSQL if corrupted |
| Lag monitoring | OPS metric (V2) |

---

## Query Patterns

| Pattern | Cypher intent | Persona |
|---------|---------------|---------|
| N-hop neighbors | Address → SENT_TO*1..3→ Address | Security Engineer, Investigator |
| Case subgraph | Case → LINKED_TO_CASE → addresses | Risk Analyst |
| Shared device cluster | User → SHARED_DEVICE → Device ← SHARED_DEVICE ← User | SEC (V2) |

Queries enforce `organizationId` filter on every match.

---

## Security

| Control | Implementation |
|---------|----------------|
| Tenant isolation | Mandatory `organizationId` in all queries |
| Access | Service account per consumer; human via API gateway |
| PII | No raw PII on nodes; reference IDs only |
| Classification | Confidential/restricted graph data |

---

## Retention

- Graph may be rebuilt from WALLET PostgreSQL + event log
- Retain rolling window (e.g., 24 months of edges) configurable
- Full rebuild procedure documented in implementation phase

---

## When NOT to Use Neo4j

| Anti-pattern | Use instead |
|--------------|-------------|
| Alert queue ordering | PostgreSQL `alert.alerts` |
| Case lifecycle | PostgreSQL `invest` schema |
| Risk scoring | PostgreSQL `risk` schema |
| Authoritative user records | PostgreSQL `user` schema |

---

## Related Documents

- [DataArchitecture.md](DataArchitecture.md)
- [PostgreSQL.md](PostgreSQL.md) — `wallet_relationship_refs`
