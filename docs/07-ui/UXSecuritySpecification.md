# UX Security Specification

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | UX Security Specification |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 10 |
| Last Updated | 2026-09-11 |
| Authority | SecurityArchitecture, NFR-SEC-*, AUTHZ/AUTH FRs, AISecurityModel |

---

## Purpose

Design-level security expectations for MVP UI. **No security control implementation in this phase.**

---

## 1. Authentication UX

| Topic | Design rule |
|-------|-------------|
| Entry | Login via `API-AUTH-001`; MFA via `API-AUTH-005` when required |
| Session | Reflect `API-AUTH-004`; refresh via `API-AUTH-003` per AUTH rules |
| Logout | Explicit `API-AUTH-002`; clear client UI session state |
| Errors | Generic auth failure messages — no user enumeration |

---

## 2. Authorization UX

- Render actions only with required permissions
- Prefer **hide** for wholly unauthorized modules; **disable** when read-without-write
- Never rely on hidden buttons as security — server enforces

---

## 3. Session Expiration

- On expiry / 401: interrupt destructive in-progress forms with save-safe messaging where possible
- Redirect to login with return URL only for non-sensitive GET deep links
- Announce session end accessibly

---

## 4. Tenant Isolation

- TenantSelector shows active organization
- Queues never show cross-tenant rows in standard mode
- Org switch audited for privileged operators

---

## 5. Privilege-Aware Rendering

| Persona (typical) | MVP nav (design target) |
|-------------------|-------------------------|
| Risk Analyst | Overview, Alerts, Risk, Investigations (+ Search) |
| Compliance Officer | Overview, Compliance, Investigations/Alerts if permitted |
| Platform Administrator | Administration, Identity, Settings, Audit (+ operational nav if granted) |
| Security Engineer | MVP shared areas only; **SEC workspace = V2** |

Exact permission strings from inventory / AUTHZ — UI maps claims → nav.

---

## 6. Sensitive Data Masking

- Mask secrets in integrations (show secret refs only — ADMIN FR)
- Classification badges on evidence (`restricted` / `confidential`)
- Avoid displaying full tokens, raw credentials, or unnecessary PII in toasts

---

## 7. Confirmation for High-Impact Actions

Mandatory ConfirmationDialog for: alert close, case close, compliance decisions, sanctions disposition, user deactivate, destructive settings.

---

## 8. Audit-Sensitive Actions

- Post-action: show success with actor context
- Admin audit screen `API-ADMIN-005`
- Entity timelines where APIs provide (`API-INVEST-008`)

---

## 9. CSRF / XSS (Design Level)

- Follow APIStandards session/cookie model chosen at implementation (Phase 11+)
- Treat AI and evidence text as **untrusted for HTML** — plain text / sanitized markdown only
- No `eval` of model output
- Deep links: validate IDs; do not execute query-string scripts

---

## 10. Secure Error Messages

- Use ErrorResponse fields; show requestId for support
- Do not reveal stack traces, internal hosts, or other tenants’ data

---

## 11. No Secret Exposure

- Client bundles must not embed API secrets
- Prompt content admin: protect `ai:prompt:write`; avoid logging full prompts to browser console in production guidance (Phase 11)

---

## 12. API Error Handling

Map 401→reauth, 403→PermissionDeniedState, 404→Not found (no leak), 409→conflict messaging, 429→backoff copy, 5xx→retry.

---

## 13. AI Security Boundary

- Prompt injection: user-visible disclaimer; no tool buttons that mutate domains from AI text alone
- Cross-tenant retrieval: rely on server; UI must not pass foreign organizationId
- See AISecurityModel.md / AIInteractionPatterns.md

---

## 14. Suspicious Session (MVP Scope)

AUTH/SEC suspicious-session **detection UI** is largely **SEC V2**. MVP: standard session expiry and re-login. Do not invent SEC MVP screens.

---

## Related Documents

- [FrontendUXArchitecture.md](FrontendUXArchitecture.md)
- [Accessibility.md](Accessibility.md)
- [../03-architecture/SecurityArchitecture.md](../03-architecture/SecurityArchitecture.md)
- [../05-ai/AISecurityModel.md](../05-ai/AISecurityModel.md)
