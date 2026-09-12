# Phase 12 M10 Handoff

## Status

**M10 = PASS WITH OPEN ITEMS** — ADMIN implemented on `sentinel-platform` against API-ADMIN-001–005, migration 011, and approved ADMIN events.

**M11 = NOT STARTED.** Do not start M11 automatically.

No commit. No push.

## What M10 delivered

- ADMIN settings, integrations, audit query
- USER/ORG provision **orchestration** (identity APIs; no ADMIN user/org tables)
- Identity permission seed `V005_8`
- Platform schema `admin` via `V011`
- `AdminSettingUpdated`, `IntegrationConfigured`, `AdminActionPerformed` via existing outbox **simulation**
- Consume `ConfigurationUpdated` for ADMIN-FR-006 context
- DASH BFF optional forward of `/v1/admin` (`DASH_PLATFORM_BASE_URL`)
- SPA admin screens (SCR-12–14) wired to ADMIN APIs

## Open items

- Identity URL and dual USER/ORG write permissions required for live provision
- GD-002 still blocks publishing `UserCreated` / `OrganizationUpdated`
- Integration collection PATCH identification gap (body `id`/`type`)
- No production broker
- API-ADMIN-006 (V2) not implemented
- M11 frontend polish / a11y certification not started
- M12 hardening / production infrastructure not started

## Next authorized milestone

**M11** only when a new authorization is issued. M11 must not be started as a continuation of this workstream.

Do not implement SEC, REPORT, OPS, WALLET, or other V2/V3 scope as a substitute for M11.

## Operator notes

```
IDENTITY_BASE_URL=http://localhost:8081
DASH_PLATFORM_BASE_URL=http://localhost:8080
IDENTITY_TOKEN_HMAC_KEY=<same as identity>
```

Provision callers need `admin:user:provision` plus `user:user:write` (and org equivalents for org provision).
