# Tool Definitions

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | AI Tool Definitions |
| Version | 0.1 (Draft) |
| Status | Draft — Phase 8 |
| Last Updated | 2026-09-11 |
| Authority | AI-FR-008; APIInventory domain read APIs |

---

## Purpose

Documentation-only catalog of **conceptual tools** available to MVP AI agents. Tools are allowlisted and authorized via AUTHZ. **No executable tool runtime in Phase 8.**

---

## Tool Design Rules

1. Read-oriented for domain data in MVP
2. Every invocation requires AI-FR-008 authorization
3. Tenant (`organizationId`) mandatory
4. No tools that create/close alerts, cases, or compliance decisions
5. Classification-aware responses

---

## MVP Tool Catalog

| Tool ID | Name | Agents | Permission concept | Domain source | Writes? |
|---------|------|--------|--------------------|---------------|---------|
| TOOL-INVEST-CASE-GET | GetCase | Investigation | invest case read | INVEST API | No |
| TOOL-INVEST-EVIDENCE-LIST | ListCaseEvidence | Investigation, Retrieval | invest evidence read | INVEST API | No |
| TOOL-ALERT-GET | GetAlert | Investigation | alert read | ALERT API | No |
| TOOL-RISK-ASSESSMENT-GET | GetRiskAssessment | Risk, Investigation | risk assessment read | RISK API | No |
| TOOL-RISK-RULE-HITS-GET | GetRuleHits | Risk | risk assessment read | RISK API | No |
| TOOL-RETRIEVE-SEARCH | SearchTenantDocuments | Retrieval, Investigation | ai:retrieve:execute | AI index / docs | No |
| TOOL-RETRIEVE-EMBED | QueryEmbeddings | Retrieval | ai:retrieve:execute | pgvector | No |

---

## Explicitly Forbidden Tools (MVP)

| Forbidden capability | Reason |
|----------------------|--------|
| CreateAlert / CloseAlert / SetAlertPriority | ALERT owns lifecycle |
| CreateCase / CloseCase / AssignCase | INVEST owns lifecycle |
| ApproveCompliance / DisposeSanctions | COMP owns outcomes |
| PublishRiskCalculated / SetRiskScore | RISK owns scoring |
| RevokeSession / ChangeRoles | AUTH/AUTHZ ownership |
| CrossTenantSearch | Tenant isolation |

---

## Invocation Contract (Logical)

```text
Agent requests tool
  → AUTHZ evaluate (actor + tool + tenant + resource)
  → Allowlist check (agent_type)
  → Execute read
  → Return sanitized payload + classification
  → Log tool call (correlationId, latency, outcome)
```

Denied calls: no data returned; audit/log denial.

---

## Related Documents

- [AIArchitecture.md](../03-architecture/AIArchitecture.md)
- [AIAgents.md](AIAgents.md)
- [APIInventory.md](../06-api/APIInventory.md)
