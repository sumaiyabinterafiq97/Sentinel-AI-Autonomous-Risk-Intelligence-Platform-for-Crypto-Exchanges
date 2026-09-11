# Prompt Versioning

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | Prompt Versioning |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 8 |
| Last Updated | 2026-09-11 |
| Authority | AI-FR-004 |

---

## Purpose

Define prompt lifecycle for MVP AI agents. Documentation only.

---

## Rules

| Rule | Specification |
|------|---------------|
| Versioning | Immutable `prompt_versions` rows; monotonic version integers |
| Activation | Explicit `active_version_id` on `prompts` |
| Ownership | AI schema; privileged roles only |
| Event | `PromptUpdated` on activate/update |
| Provenance | Recommendations store `prompt_version_id` |
| Rollback | Activate prior version; do not mutate old version content |
| Evaluation | Compare versions offline before activation (V2 eval runtime deferred) |

---

## Related Documents

- [AIArchitecture.md](../03-architecture/AIArchitecture.md)
- [AIAgents.md](AIAgents.md)
- [InitialMigrationSpecifications.md](../04-database/InitialMigrationSpecifications.md) — migration 010
