# Redis Usage Model

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Redis Design |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 3 |
| Last Updated | 2026-09-03 |

---

## Purpose

Define non-authoritative Redis use cases. **Redis is never the system of record** (NFR-RES-002, Data Architecture).

---

## Use Cases

| Use case | Owner service | TTL | Failure behavior |
|----------|---------------|-----|------------------|
| API idempotency keys | Gateway / domain services | 24h | Fall back to DB dedup; accept duplicate risk |
| Rate limiting counters | API Gateway | 1–60 min windows | Fail open with audit OR fail closed for auth (config) |
| Session assist cache | AUTH | Match session TTL | Fall back to AUTH DB |
| Risk assessment read cache | RISK | 30–300s | Fall back to PostgreSQL |
| Alert queue snapshot cache | DASH | 15–60s | Refresh from ALERT API |
| AI assist job status | AI | 1h | Poll AI DB `agent_runs` |
| Distributed locks (optional) | CORE | Short (30s) | Skip lock; use DB optimistic concurrency |

---

## Key Pattern Concepts

| Pattern | Example | Notes |
|---------|---------|-------|
| Idempotency | `idem:{orgId}:{idempotencyKey}` → response hash | APIStandards §8 |
| Rate limit | `rl:{orgId}:{userId}:{endpoint}:{window}` → count | Sliding window |
| Session cache | `sess:{sessionId}` → session JSON | Encrypted payload optional |
| Risk cache | `risk:assess:{orgId}:{assessmentId}` → JSON | Invalidate on new assessment |
| Feature flag cache | `ff:{orgId}:{flagKey}` → boolean | Invalidate on ConfigurationUpdated event |
| AI job | `ai:job:{jobId}` → status | Paired with PostgreSQL authoritative record |

**No Redis keys store sole copy of business entities.**

---

## Invalidation

| Trigger | Action |
|---------|--------|
| Domain event (e.g., `AlertCreated`) | Invalidate DASH queue cache pattern for org |
| Config/flag change | Invalidate `ff:*` for org |
| Risk recalculation | Invalidate specific assessment key |
| TTL expiry | Automatic |

Event-driven invalidation preferred over long TTL for operational queues.

---

## Consistency

| Model | Application |
|-------|-------------|
| Cache-aside | Read through; write to PostgreSQL first |
| Write-through | Not used for business state |
| Stale reads | Acceptable for DASH widgets; not for authorization |

AUTHZ decisions: **never** rely solely on stale Redis cache for deny/allow; short TTL + DB fallback.

---

## Security

- No secrets, tokens, or PII in keys
- Redis AUTH/TLS in non-local environments
- Separate logical databases or key prefixes per environment

---

## Operational Notes

- Memory limits and eviction policy: `volatile-lru` for cache keys with TTL
- Persistence: AOF optional; not relied on for recovery of business data
- Monitor: memory usage, evictions, hit rate (OPS V2)

---

## Related Documents

- [APIStandards.md](../06-api/APIStandards.md)
- [DataArchitecture.md](DataArchitecture.md)
