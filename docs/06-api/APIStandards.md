# API Standards

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | API Standards |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 3 |
| Owner | Architecture Team |
| Last Updated | 2026-09-03 |
| Authority | Governs all REST API contracts; subordinate to FDS/FRS for behavior |

---

## Purpose

Define contract-first REST API standards for Sentinel AI. Implementation must conform to these standards and to [OpenAPI.yaml](OpenAPI.yaml).

---

## Related Documents

| Document | Relationship |
|----------|--------------|
| [APIInventory.md](APIInventory.md) | Operation catalog |
| [OpenAPI.yaml](OpenAPI.yaml) | Machine-readable contract |
| [EventContracts.md](EventContracts.md) | Async event schemas |
| [ArchitecturePrinciples.md](../03-architecture/ArchitecturePrinciples.md) | AP-02 contract-first |
| [SecurityArchitecture.md](../03-architecture/SecurityArchitecture.md) | Auth model |

---

## 1. API Versioning

| Rule | Standard |
|------|----------|
| Version prefix | `/v1` in URL path |
| Major version | Breaking changes require `/v2` |
| Minor evolution | Additive optional fields only within same major version |
| Deprecation | `Sunset` header + 90-day minimum notice (design policy) |
| OpenAPI document version | Matches API major version |

---

## 2. URL Conventions

```
https://{host}/v1/{resource-collection}
https://{host}/v1/{resource-collection}/{resourceId}
https://{host}/v1/{resource-collection}/{resourceId}/{sub-resource}
```

| Rule | Example |
|------|---------|
| Plural nouns for collections | `/v1/alerts`, `/v1/investigations/cases` |
| kebab-case for multi-word paths | `/v1/compliance/sanctions-screenings` |
| No verbs in paths (use HTTP methods) | `POST /v1/alerts/{id}/assign` is acceptable action sub-resource |
| Domain ownership reflected in path prefix | `/v1/risk/*` owned by RISK service |
| No cross-domain internal tables | DASH uses aggregation/BFF paths only |

### Organization scope

Tenant isolation uses **`X-Organization-Id`** header (required when actor has multi-org access) validated against AUTHZ. Path-scoped org (`/v1/organizations/{orgId}/...`) reserved for ADMIN/ORG management APIs.

---

## 3. HTTP Methods

| Method | Usage |
|--------|-------|
| `GET` | Read; idempotent; no body side effects |
| `POST` | Create; non-idempotent unless idempotency key provided |
| `PUT` | Full replace of resource (rare; prefer PATCH) |
| `PATCH` | Partial update of owned resource |
| `DELETE` | Soft-delete or deactivate where FR defines; hard delete only per retention policy |

Internal ingest endpoints (e.g., transaction ingest) may use `POST` with service identity authentication.

---

## 4. Status Codes

| Code | Usage |
|------|-------|
| 200 | Successful GET/PATCH with body |
| 201 | Resource created |
| 202 | Accepted for async processing (AI assist, report generation V2) |
| 204 | Success without body |
| 400 | Validation error |
| 401 | Unauthenticated |
| 403 | Authenticated but unauthorized |
| 404 | Resource not found (or hidden by auth policy) |
| 409 | Conflict (state, idempotency, concurrency) |
| 422 | Semantic validation failure |
| 429 | Rate limited |
| 503 | Dependency unavailable (optional AI, external provider) |
| 500 | Unexpected internal error |

Do not use 200 for error conditions.

---

## 5. Request / Response Conventions

| Field | Rule |
|-------|------|
| Content-Type | `application/json; charset=utf-8` |
| Date/time | ISO-8601 UTC (`2026-09-03T12:00:00Z`) |
| Identifiers | UUID v4 for resource IDs unless domain specifies otherwise |
| Money/amounts | String decimal + currency code (avoid float) |
| Enums | String constants documented in OpenAPI |
| Null | Explicit `null` for absent optional fields in responses; omit vs null policy: prefer omit unset optional fields on create |

### Standard response envelope (single resource)

```json
{
  "data": { },
  "meta": {
    "requestId": "uuid",
    "correlationId": "uuid",
    "timestamp": "2026-09-03T12:00:00Z"
  }
}
```

### Standard list response

```json
{
  "data": [ ],
  "meta": {
    "requestId": "uuid",
    "correlationId": "uuid",
    "timestamp": "2026-09-03T12:00:00Z",
    "pagination": {
      "cursor": "opaque-token",
      "hasMore": true,
      "limit": 50
    }
  }
}
```

---

## 6. Pagination

| Aspect | Standard |
|--------|----------|
| Style | Cursor-based (preferred for operational queues) |
| Query params | `limit` (default 50, max 200), `cursor` |
| Offset pagination | Allowed for admin lists only; not for high-volume alert queues |
| Sorting | `sort=field` or `sort=-field` (desc); whitelist per endpoint |

---

## 7. Filtering and Search

| Param | Usage |
|-------|-------|
| `filter[field]=value` | Equality filter |
| `filter[status]=OPEN` | Enum filter |
| `filter[createdAt][gte]` | Range filters where supported |
| `q` | Full-text search where FR supports discovery (RISK, INVEST, ALERT) |

Filters MUST be enforced in owning domain service with AUTHZ scope—not client-side only.

---

## 8. Idempotency

| Rule | Standard |
|------|----------|
| Header | `Idempotency-Key: {uuid}` |
| Required on | POST creating operational resources (alerts manual create, cases, compliance submissions) |
| Behavior | Same key + same payload → same result within 24h window |
| Storage | Redis idempotency record (see [Redis.md](../04-database/Redis.md)) |
| Response | Repeat request returns original 201/200 with same resource ID |

---

## 9. Correlation and Request IDs

| Header | Direction | Purpose |
|--------|-----------|---------|
| `X-Request-Id` | Client may supply; server generates if absent | Single HTTP request |
| `X-Correlation-Id` | Client or gateway supplies; propagated | End-to-end workflow |
| Response echo | Server returns both in `meta` and response headers | Observability |

All domain services and event producers propagate `correlationId`.

---

## 10. Authentication

| Mechanism | Usage |
|-----------|-------|
| Bearer token | `Authorization: Bearer {access_token}` — human sessions (AUTH) |
| Service token | `Authorization: Bearer {service_jwt}` — domain-to-domain |
| Token validation | AUTH service; gateway may cache short TTL |

Unauthenticated requests receive `401`. Token validation failures fail closed.

---

## 11. Authorization (RBAC)

| Rule | Standard |
|------|----------|
| Evaluation | AUTHZ before domain handler execution |
| Permission format | `{domain}:{resource}:{action}` e.g. `alert:alert:read` |
| Deny default | Missing permission → 403 |
| Tenant scope | All data access filtered by organization |
| AI tools | Same permission model via AI-FR-008 |

---

## 12. Error Model

See [ErrorHandling.md](ErrorHandling.md) and OpenAPI `ErrorResponse` schema.

| Field | Required |
|-------|----------|
| `error.code` | Machine-readable stable code |
| `error.message` | Human-readable, non-sensitive |
| `error.requestId` | Yes |
| `error.correlationId` | When available |
| `error.timestamp` | ISO-8601 |
| `error.details` | Field-level errors for 400/422 |
| `error.retryable` | Boolean for client retry guidance |

Error codes: `{DOMAIN}_{CATEGORY}_{NNN}` e.g. `ALERT_VALIDATION_001`.

Do not expose stack traces, SQL, or internal service names in production responses.

---

## 13. Rate Limiting

| Tier | Target |
|------|--------|
| Auth endpoints | Stricter limits (NFR-SEC-006) |
| Read APIs | Per-user + per-org limits |
| Ingest APIs | Service identity limits |
| AI assist | Lower limits; cost control (NFR-COST-001) |

Response headers: `RateLimit-Limit`, `RateLimit-Remaining`, `RateLimit-Reset`. Exceeded → `429`.

---

## 14. Audit Requirements

State-changing APIs on sensitive resources MUST emit audit records via CORE audit pipeline:

- Actor, action, resource type, resource ID, outcome, correlation ID, organization ID
- Domains record domain-specific audit extensions (ALERT-FR-009, INVEST-FR-009, etc.)

---

## 15. Observability Headers

| Header | Purpose |
|--------|---------|
| `X-Request-Id` | Trace single request |
| `X-Correlation-Id` | Trace workflow |
| `Traceparent` | W3C trace context (implementation phase) |

---

## 16. Sensitive Data Handling

| Rule | Standard |
|------|----------|
| PII minimization | Return only fields required for operation |
| Redaction | Mask document numbers, full API keys in list views |
| Secrets | Never in responses; integration secrets referenced by ID only |
| Classification | Responses tagged internally; gateway may strip restricted fields by role |

---

## 17. Sync vs Async

| Pattern | Usage |
|---------|-------|
| Synchronous | CRUD, reads, deterministic risk query, alert disposition |
| Async 202 | AI assist requests, V2 report generation |
| Event-driven | State changes publish domain events after commit |

AI assist endpoints return `202` with `jobId` or synchronous `200` if completed within timeout (NFR-PERF-006)—design choice documented in APIInventory per endpoint.

---

## 18. Backward Compatibility Policy

- Add optional request/response fields: compatible
- Add enum values: compatible with client tolerance documentation
- Remove/rename fields: new major API version
- Event schema: per [EventArchitecture.md](../03-architecture/EventArchitecture.md)

---

## 19. Release Tagging

Every operation in APIInventory and OpenAPI includes `x-release: MVP | V2 | V3`.

MVP implementation gate uses MVP-tagged operations only.

---

## Open Questions

| ID | Question |
|----|----------|
| API-OQ-001 | BFF consolidation: single DASH gateway vs direct domain calls — see Phase3Report |
| API-OQ-002 | Webhook callbacks for external ingest — deferred |
