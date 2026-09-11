# API Error Handling

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | API Error Handling |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 3 |
| Last Updated | 2026-09-03 |

---

## Purpose

Standardized error contract for all Sentinel AI REST APIs. Implemented in [OpenAPI.yaml](OpenAPI.yaml) `ErrorResponse` schema.

---

## Error Response Shape

```json
{
  "error": {
    "code": "DOMAIN_CATEGORY_NNN",
    "message": "Human-readable summary safe for clients",
    "requestId": "uuid",
    "correlationId": "uuid",
    "timestamp": "2026-09-03T12:00:00Z",
    "retryable": false,
    "details": [
      { "field": "priority", "message": "Must be a positive integer" }
    ]
  }
}
```

---

## Error Categories

| HTTP | Category | Code prefix | retryable |
|------|----------|-------------|-----------|
| 400 | validation | `*_VALIDATION_*` | false |
| 401 | authentication | `AUTH_AUTHENTICATION_*` | false |
| 403 | authorization | `AUTHZ_FORBIDDEN_*` | false |
| 404 | not_found | `*_NOT_FOUND_*` | false |
| 409 | conflict | `*_CONFLICT_*` | false |
| 422 | semantic | `*_SEMANTIC_*` | false |
| 429 | rate_limit | `PLATFORM_RATE_LIMIT_*` | true |
| 503 | dependency | `*_DEPENDENCY_*` | true |
| 504 | timeout | `*_TIMEOUT_*` | true |
| 500 | internal | `PLATFORM_INTERNAL_*` | true |

---

## Disclosure Policy

| Include | Exclude |
|---------|---------|
| Stable error codes | Stack traces |
| Field validation messages | SQL errors |
| requestId, correlationId | Internal service hostnames |
| retryable flag | Database schema details |

AI unavailable: return `503` with `AI_DEPENDENCY_001`, message "Assistive AI temporarily unavailable" — not platform outage.

---

## Related Documents

- [APIStandards.md](APIStandards.md)
- [OpenAPI.yaml](OpenAPI.yaml)
