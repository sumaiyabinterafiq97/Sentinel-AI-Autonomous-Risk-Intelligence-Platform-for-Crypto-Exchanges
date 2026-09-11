# User Personas

## Document Information

| Field | Value |
|--------|-------|
| Project | Sentinel AI |
| Document | User Personas |
| Version | 1.0 (Draft) |
| Status | Draft — Phase 1 |
| Owner | Product & Engineering Team |
| Last Updated | 2026-09-03 |
| Authority | Product discovery input; must align with FDS/FRS domain ownership |

---

## Purpose

This document defines enterprise personas for Sentinel AI. Personas describe **who** uses the platform, **what decisions they make**, and **what Sentinel must and must not automate** for them.

Personas are product-level constructs. Domain lifecycles remain governed by frozen functional requirements.

---

## Persona 1 — Risk Analyst

### Role

Operational analyst responsible for monitoring transaction and behavioral risk signals, triaging alerts, and initiating or supporting fraud investigations.

### Primary responsibilities

- Review risk assessments and explanations for flagged activity
- Triage operational alerts using risk context and business rules
- Escalate high-risk activity to investigation or compliance workflows
- Document analyst rationale for disposition decisions
- Identify patterns requiring rule or policy review

### Goals

- Reduce time spent on low-value alert review
- Understand **why** activity was flagged without reading raw logs
- Prioritize work using consistent, explainable signals
- Initiate investigations with sufficient context pre-attached

### Pain points

- Fragmented tools for risk scores, alerts, and case history
- High false-positive volume without sufficient context
- Opaque scoring with no traceable evidence
- Manual copy-paste between systems to build investigation context

### Information needs

- Current risk score, contributing factors, and explanation
- Related alerts, user/account context, and recent activity timeline
- Rule hits, behavioral indicators, and historical disposition patterns
- Queue position and SLA indicators for alert handling

### Key workflows

1. **Alert triage** — Review ALERT queue; consume RISK context; assign, escalate, or close
2. **Risk review** — Deep-dive on `HighRiskDetected` or elevated scores before escalation
3. **Investigation handoff** — Create or link INVEST case with risk evidence attached
4. **Pattern review** — Identify recurring false positives or emerging fraud patterns

### Decisions they make

- Alert disposition (close, monitor, escalate)
- Whether to open or upgrade an investigation
- Whether additional evidence or compliance review is required
- Recommended rule or threshold adjustments (advisory; policy change is separate)

### What Sentinel AI provides

- Unified DASH workspace surfacing RISK, ALERT, and INVEST context
- Explainable risk assessments and behavioral context
- AI-assisted summaries and evidence retrieval (assistive)
- Audit trail of analyst actions and dispositions

### What Sentinel AI must NOT automate for them

- Final alert closure on consequential or high-severity items without human review where policy requires it
- Autonomous account blocking, fund holds, or enforcement actions
- Overriding deterministic rule outcomes without authorized workflow
- Replacing analyst judgment on ambiguous high-impact cases

### Success criteria

- Median alert triage time reduced versus baseline (target TBD in Product Discovery)
- Analyst can trace every displayed risk conclusion to evidence or rules
- Escalation to investigation includes complete risk context without manual assembly

---

## Persona 2 — Compliance Officer

### Role

Compliance analyst or officer responsible for regulatory workflows including KYC review, AML monitoring support, Travel Rule validation, sanctions screening, and audit preparation.

### Primary responsibilities

- Execute and review compliance workflows per organizational policy
- Validate screening results and Travel Rule message completeness
- Maintain evidence packages for regulatory and internal audit
- Coordinate with investigation teams on compliance-related cases
- Ensure decisions are documented and auditable

### Goals

- Consistent, repeatable compliance processing
- Complete evidence trails for every compliance decision
- Reduced manual document gathering during audits
- Clear separation between compliance outcomes and fraud/risk dispositions

### Pain points

- Disconnected KYC, screening, and case management systems
- Incomplete audit packages requiring manual reconstruction
- Unclear ownership between compliance, fraud, and security teams
- AI outputs that cannot be cited in regulatory review

### Information needs

- Case status across KYC, AML, Travel Rule, and sanctions workflows
- Screening match details, source references, and analyst review history
- Linked investigation and risk context (read-only where appropriate)
- Audit logs and exportable evidence bundles

### Key workflows

1. **KYC review** — Review submitted KYC data; approve, reject, or request information
2. **Sanctions screening** — Review matches; document disposition with evidence
3. **Travel Rule validation** — Verify counterparty data completeness and policy compliance
4. **Audit preparation** — Compile standardized evidence packages from COMP and platform audit sources

### Decisions they make

- KYC approval, rejection, or information request
- Sanctions match disposition (false positive, escalate, report)
- Travel Rule exception handling per policy
- Whether compliance escalation to investigation is required

### What Sentinel AI provides

- COMP domain workflows for MVP compliance capabilities
- Standardized evidence collection and audit preparation support
- AI-assisted document summarization and retrieval (assistive; human approves outcomes)
- Cross-reference to related INVEST cases without owning investigation lifecycle

### What Sentinel AI must NOT automate for them

- Autonomous KYC approval or sanctions clearance without human sign-off
- Regulatory filing or external reporting without authorized human action
- Retroactive alteration of compliance audit records
- Compliance decisions based solely on ungrounded AI assertions

### Success criteria

- Audit evidence package completeness meets defined target (Product Discovery)
- Compliance workflow steps are traceable end-to-end in audit logs
- AI assistance reduces document retrieval time without reducing human accountability

---

## Persona 3 — Security Engineer

### Role

Security operations engineer or analyst monitoring API abuse, authentication anomalies, device signals, and operational security threats (Version 2 SEC domain scope).

### Primary responsibilities

- Monitor SEC-derived security signals and threat detections
- Correlate security events with user, device, and API activity context
- Support incident investigation with security-specific evidence
- Tune detection rules and thresholds within authorized scope
- Coordinate with fraud/investigation teams on account compromise scenarios

### Goals

- Early detection of API key abuse and authentication anomalies
- Clear device and session context for security incidents
- Actionable signals without drowning in raw security logs
- Clean handoff to INVEST for account compromise cases

### Pain points

- Security telemetry isolated from fraud and risk platforms
- Alert fatigue from undifferentiated security events
- Unclear boundary between security monitoring and investigation ownership
- Lack of graph or relationship context across accounts and devices

### Information needs

- API activity patterns, authentication events, device fingerprints
- SEC threat detection outcomes with rule/signal provenance
- Related RISK and ALERT context for the same user or session
- Investigation case linkage when account compromise is suspected

### Key workflows

1. **Security signal review** — Triage SEC threat detections and anomalies
2. **API abuse investigation support** — Provide SEC context to INVEST cases
3. **Device/session correlation** — Review device intelligence across related accounts
4. **Incident coordination** — Escalate to investigation with structured security evidence

### Decisions they make

- Security signal disposition (benign, monitor, escalate)
- Whether to recommend investigation opening for suspected compromise
- Detection rule tuning recommendations (within change-management process)
- Escalation to broader incident response (outside Sentinel scope)

### What Sentinel AI provides (V2+)

- SEC domain capabilities: API Monitoring, Authentication Monitoring, Device Monitoring, Threat Detection
- Security context consumed by DASH and linked to INVEST workflows
- AI-assisted summarization of security event timelines (assistive)

### What Sentinel AI must NOT automate for them

- Autonomous API key revocation or account lockout (enforcement belongs to exchange systems / authorized human process)
- SIEM bi-directional sync or automated containment (V3 deferred)
- Insider-threat pattern enforcement (V3 deferred)
- Replacing SOC runbooks or external incident response tooling

### Success criteria

- Security signals include sufficient context for triage without raw log diving
- Account compromise scenarios reach INVEST with SEC evidence attached
- False-positive rate for SEC threat detections tracked against target

---

## Persona 4 — Platform Administrator

### Role

Platform administrator responsible for tenant configuration, user access, organizational structure, feature governance, and day-to-day platform operability.

### Primary responsibilities

- Manage users, roles, and organization membership (USER, ORG, AUTH, AUTHZ, ADMIN)
- Configure platform settings and feature flags within policy
- Support analyst onboarding and access provisioning
- Monitor platform health and coordinate with operations (OPS, V2)
- Ensure least-privilege access across operational teams

### Goals

- Secure, scalable multi-tenant administration
- Clear role-based access aligned to operational responsibilities
- Minimal friction for provisioning new analysts and teams
- Observable platform health and auditability of admin actions

### Pain points

- Overlapping admin tools across identity and application layers
- Role sprawl without clear domain-aligned permissions
- Lack of visibility into who changed configuration and when
- Difficulty separating admin actions from operational analyst actions in audit logs

### Information needs

- User and organization directory with status and membership
- Role and permission mappings per AUTHZ model
- Configuration change history and audit trail
- Platform health indicators (CORE/OPS)

### Key workflows

1. **User provisioning** — Create users, assign roles, manage org association
2. **Access review** — Periodic review of permissions and deactivated accounts
3. **Configuration management** — Update platform settings and feature flags
4. **Operational support** — Diagnose access issues; coordinate with engineering on incidents

### Decisions they make

- User creation, deactivation, and role assignment
- Organization structure and membership changes
- Feature flag and configuration changes within approved policy
- Escalation to engineering for platform incidents

### What Sentinel AI provides

- ADMIN, USER, ORG, AUTH, AUTHZ domain capabilities
- Auditable administration actions via CORE audit infrastructure
- DASH access scoped by authorization policies

### What Sentinel AI must NOT automate for them

- Autonomous privilege elevation without authorized approval workflow
- Bulk access changes without audit and confirmation
- Bypass of AUTH/AUTHZ policies for convenience
- AI-driven modification of security-critical configuration

### Success criteria

- All admin actions produce durable audit records
- Role assignments align to persona responsibilities with least privilege
- Platform remains operable when assistive AI services are degraded

---

## Persona Scope Notes

| Persona | MVP relevance | V2 relevance |
|---------|---------------|--------------|
| Risk Analyst | Primary | Enhanced with WALLET/SEC context |
| Compliance Officer | Primary | Enhanced reporting (REPORT) |
| Security Engineer | Limited (pre-SEC) | Primary (SEC domain) |
| Platform Administrator | Primary | Enhanced ops visibility (OPS) |

Additional personas (e.g., Fraud Investigator as distinct from Risk Analyst, Executive Reporting Consumer) may be defined in later phases if warranted. INVEST workflows are primarily used by Risk Analysts and dedicated investigators; both share the investigation persona behaviors described under Risk Analyst handoff workflows.

---

## Related Documents

- [Product Discovery](ProductDiscovery.md)
- [Product Scope](ProductScope.md)
- [Vision](Vision.md)
- [Functional Domain Specification](../02-requirements/FunctionalDomainSpecification.md)
