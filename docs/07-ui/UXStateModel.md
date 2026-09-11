# UX State Model

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | UX State Model |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 10 |
| Last Updated | 2026-09-11 |

---

## Purpose

Standardize UI states so screens/workflows do not invent ad-hoc behavior. Documentation only.

---

## Standard States

| State | Meaning | Typical UX |
|-------|---------|------------|
| **loading** | Initial fetch in progress | Skeleton + `aria-busy` |
| **loaded** | Data present and current enough to act | Primary content |
| **empty** | Successful response; zero items | EmptyState + next action |
| **partial** | Some sections ok; some failed | Per-section ErrorState |
| **stale** | Data may be outdated (SSE disconnected / old cache) | Subtle “May be outdated — refresh” |
| **error** | Hard failure for region/page | ErrorState + retry + requestId |
| **retrying** | User/system retry in flight | Loading affordance on control |
| **permission denied** | 403 / missing permission | PermissionDeniedState |
| **unavailable** | Dependency down (AI, SSE) | Banner; alternate path |
| **degraded** | Core works; assistive feature down | Warning + manual path |
| **submitting** | Mutation in flight | Disable submit; prevent double-post |
| **success** | Mutation acknowledged | Toast / inline success; refresh entity |
| **validation failure** | Client or 400 field errors | Inline errors + summary |

---

## Application by Surface

### Dashboard (SCR-01)

| State | Behavior |
|-------|----------|
| loading | Widget skeletons |
| partial | Failed widget isolated |
| degraded | AI widgets unavailable |
| stale | SSE down → poll hint |
| empty | No queue items message |

### Alert list (SCR-02)

loading → loaded/empty/error; stale on SSE loss; permission denied hides queue.

### Risk analysis (SCR-06 / SCR-07)

loading/loaded/empty; AI deepen: loading/timeout/unavailable without blocking SYSTEM RESULT score.

### Investigation (SCR-04 / SCR-05)

list states as alerts; detail: submitting on assign/close/evidence; success refreshes timeline; AI assist independent degraded path.

### Compliance (SCR-08 / SCR-09 / SCR-10)

submitting + confirmation for decisions; validation failure on incomplete forms; async audit package: loading/partial/success/error on job status.

### AI assistant (SCR-15)

idle → loading → loaded | timeout | error | unavailable | degraded; never `success` that implies domain mutation.

### Administration (SCR-12–14)

submitting on settings; success + audit link; permission denied for non-admins.

---

## SSE Interaction with States

| SSE event | UI effect |
|-----------|-----------|
| Connected | Clear stale if refetch succeeds |
| Message | Mark stale or trigger refetch → loading subsection → loaded |
| Error/disconnect | stale + fallback poll |

---

## Related Documents

- [FrontendUXArchitecture.md](FrontendUXArchitecture.md)
- [ComponentLibrary.md](ComponentLibrary.md) — Empty/Loading/Error/PermissionDenied
- [AIInteractionPatterns.md](AIInteractionPatterns.md)
