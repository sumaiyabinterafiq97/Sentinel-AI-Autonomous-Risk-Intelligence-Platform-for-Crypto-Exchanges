# Business Requirements Specification (BRS)

## Document Information

| Field | Value |
|--------|-------|
| Project Name | Sentinel AI |
| Document | Business Requirements Specification |
| Version | 0.5 (Draft) |
| Status | Draft |
| Owner | To Be Assigned |
| Authors | Product & Engineering Team |
| Last Updated | 2026-08-10 |

---

## Version History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 0.1 | 2026-07-20 | Product Team | Initial draft |
| 0.2 | 2026-07-28 | Product Team | Business requirements completed |
| 0.3 | 2026-08-05 | Product Team | Traceability added |
| 0.4 | 2026-08-10 | Product Team | Governance, capability map, glossary, assumptions |
| 0.5 | 2026-08-10 | Product Team | Approval table, status lifecycle, process polish, requirement template |

---

## Approval

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Prepared By | Product & Engineering Team | 2026-08-10 | — |
| Reviewed By | To Be Assigned | — | — |
| Approved By | To Be Assigned | — | — |

| Field | Value |
|-------|-------|
| Approval Date | Pending |
| Document Status | Draft |

---

## Purpose

This document defines the business requirements for Sentinel AI.

It identifies the business problems the platform addresses, the organizational objectives it supports, and the business capabilities required to deliver value to cryptocurrency exchanges.

The Business Requirements Specification serves as the foundation for Functional Requirements, System Architecture, Database Design, API Specifications, and implementation planning.

---

## Related Documents

| Document | Relationship |
|----------|--------------|
| [Vision.md](Vision.md) | Long-term vision, principles, and product direction |
| [ProductScope.md](ProductScope.md) | Product boundaries, functional domains, and capability model |
| Functional Domain Specification | Major capability domains bridging BRS and FRS |
| Functional Requirements Specification | Detailed system behaviors derived from this BRS |
| System Architecture | Technical realization of business capabilities |
| Database Design | Data structures supporting business entities |
| AI Architecture | AI agents, tools, and evaluation aligned to AI business needs |
| API Specification | Interfaces enabling integrations and workflows |
| Deployment Architecture | Operational deployment of the platform |

---

## Business Context

Sentinel AI operates within the operational ecosystem of a cryptocurrency exchange. The platform provides enterprise risk intelligence without replacing core exchange systems.

```text
                Cryptocurrency Exchange

      ┌────────────────────────────────────┐
      │                                    │
      │        Sentinel AI Platform        │
      │                                    │
      └────────────────────────────────────┘

        ↑          ↑          ↑

     Risk Team   Compliance   Security

        ↑          ↑          ↑

      Wallets   Transactions   APIs

        ↑

 Blockchain Intelligence
```

---

## Scope

This document focuses on business needs rather than technical implementation.

It defines:

- Business objectives
- Business stakeholders
- Business requirements
- Business rules
- Business constraints
- Business assumptions
- Business risks
- Business success criteria
- Traceability and requirement governance

Implementation details, system design, user interface behavior, database structures, APIs, and deployment architecture are intentionally excluded.

---

## Relationship to Product Scope

The Product Scope document defines the functional domains and product boundaries of Sentinel AI.

The Business Requirements Specification builds upon those domains by describing the business capabilities and operational outcomes that each domain must support.

Every functional requirement defined in later documentation should be traceable to one or more business requirements defined in this document.

---

# Chapter 1 — Business Background

## Industry Background

Cryptocurrency exchanges operate in a rapidly evolving environment characterized by increasing transaction volumes, sophisticated fraud techniques, regulatory complexity, and growing cybersecurity threats.

Operational teams must investigate suspicious activities quickly while maintaining regulatory compliance, protecting customer assets, and minimizing operational risk.

These responsibilities are often supported by multiple disconnected systems, resulting in fragmented workflows, duplicated effort, inconsistent decision-making, and reduced operational visibility.

---

## Business Need

Organizations require a unified operational platform that improves collaboration across Risk, Fraud, Compliance, and Security teams while providing intelligent decision support throughout the investigation lifecycle.

The platform must enhance operational efficiency, improve investigation quality, strengthen regulatory compliance, and provide enterprise-wide visibility without replacing existing exchange infrastructure.

---

## Business Opportunity

By consolidating operational intelligence into a single enterprise platform, Sentinel AI enables organizations to:

- Improve investigation efficiency
- Reduce operational costs
- Increase fraud detection effectiveness
- Enhance regulatory readiness
- Improve analyst productivity
- Support consistent operational decision-making
- Scale operational capabilities as transaction volumes grow

# Chapter 2 — Business Problem Statement

## Problem Overview

Cryptocurrency exchanges operate in a high-risk, high-volume environment where financial crime, regulatory compliance, and operational security are critical business concerns.

As exchanges expand their customer base and transaction volume, operational teams must process an increasing number of alerts, investigate suspicious activities, comply with evolving regulations, and respond rapidly to emerging threats.

Many organizations perform these activities using multiple disconnected systems, resulting in fragmented workflows, duplicated effort, inconsistent decision-making, and limited operational visibility.

These challenges reduce investigation efficiency, increase operational costs, and make it more difficult to detect and respond to financial crime effectively.

Sentinel AI is designed to address these business challenges by providing a unified enterprise platform that enhances operational intelligence while supporting existing exchange infrastructure.

---

## Primary Business Problems

Sentinel AI addresses the following business problems.

### BP-001 — Fragmented Investigation Workflows

Risk, Fraud, Compliance, and Security teams often rely on separate systems that do not share investigation context efficiently.

This fragmentation increases investigation time, creates duplicated work, and reduces collaboration across operational teams.

**Business Impact**

- Longer investigation cycles
- Reduced operational efficiency
- Inconsistent investigation outcomes
- Increased analyst workload

---

### BP-002 — Limited Operational Visibility

Organizations frequently lack a centralized view of operational risk, ongoing investigations, compliance activities, and security events.

Decision-makers must collect information from multiple systems before understanding the current operational situation.

**Business Impact**

- Delayed decision-making
- Reduced situational awareness
- Limited executive reporting
- Inefficient operational monitoring

---

### BP-003 — Increasing Financial Crime Complexity

Financial crime techniques continue to evolve rapidly, making traditional rule-based detection insufficient on its own.

Analysts require additional contextual intelligence to investigate increasingly sophisticated fraud patterns.

**Business Impact**

- Increased fraud exposure
- Higher false positive rates
- Missed suspicious activities
- Greater investigation complexity

---

### BP-004 — Regulatory Compliance Challenges

Cryptocurrency exchanges operate under evolving regulatory requirements across multiple jurisdictions.

Compliance investigations often involve manual evidence collection, documentation, and audit preparation.

**Business Impact**

- Increased compliance effort
- Higher operational costs
- Audit preparation challenges
- Greater regulatory risk

---

### BP-005 — Inefficient Use of Operational Data

Operational information exists across transaction systems, authentication logs, blockchain intelligence providers, customer records, and security platforms.

Without unified intelligence, analysts spend significant time collecting information before beginning an investigation.

**Business Impact**

- Time-consuming investigations
- Duplicate data collection
- Reduced analyst productivity
- Delayed response times

---

### BP-006 — Limited Decision Support

Operational decisions frequently depend on individual analyst experience rather than consistent, evidence-based recommendations.

Organizations require intelligent assistance that improves decision quality while maintaining human oversight.

**Business Impact**

- Inconsistent investigation outcomes
- Variable decision quality
- Longer analyst training periods
- Reduced operational consistency

---

## Business Problem Summary

The business problems identified above collectively reduce operational efficiency, increase investigation costs, complicate regulatory compliance, and limit an organization's ability to respond effectively to financial crime and operational security threats.

Sentinel AI addresses these challenges by unifying operational intelligence, enhancing investigation workflows, improving collaboration, and providing explainable AI-assisted decision support that enables organizations to investigate risks more efficiently while maintaining full human control over operational decisions.

# Chapter 3 — Business Goals

## Business Goals Overview

The business goals of Sentinel AI define the strategic outcomes the platform aims to achieve for cryptocurrency exchanges.

These goals provide direction for product planning and establish the long-term business value expected from the platform.

Each goal aligns with the business problems identified in the previous chapter and guides the development of detailed business requirements and functional capabilities.

---

## BG-001 — Improve Operational Efficiency

Enable operational teams to perform investigations more efficiently by reducing manual effort, consolidating workflows, and providing a unified investigation environment.

**Expected Outcome**

- Faster investigation processes
- Reduced operational complexity
- Improved analyst productivity

---

## BG-002 — Strengthen Fraud Detection and Risk Management

Enhance the organization's ability to identify, prioritize, investigate, and respond to financial crime and operational risks.

**Expected Outcome**

- Improved fraud detection quality
- Better risk prioritization
- Faster response to suspicious activities

---

## BG-003 — Enhance Regulatory Compliance

Support compliance teams by simplifying regulatory investigations, improving documentation quality, and maintaining audit-ready operational records.

**Expected Outcome**

- Improved regulatory readiness
- Reduced compliance effort
- More consistent compliance processes

---

## BG-004 — Improve Decision Quality

Provide explainable AI-assisted intelligence that enables analysts to make faster, more informed, and more consistent operational decisions while maintaining full human oversight.

**Expected Outcome**

- Higher-quality investigations
- Increased decision consistency
- Greater analyst confidence

---

## BG-005 — Increase Operational Visibility

Provide centralized visibility into operational activities, investigations, compliance performance, AI insights, and security events through unified dashboards and reporting.

**Expected Outcome**

- Better operational awareness
- Improved executive reporting
- Enhanced organizational transparency

---

## BG-006 — Enable Enterprise Scalability

Provide a modular platform that allows organizations to expand operational capabilities, integrate with existing infrastructure, and support future business growth.

**Expected Outcome**

- Incremental platform adoption
- Reduced implementation complexity
- Long-term operational scalability

---

## BG-007 — Foster Cross-Functional Collaboration

Improve collaboration between Risk, Fraud, Compliance, Security, and Platform Operations teams by providing shared investigation workflows and centralized operational intelligence.

**Expected Outcome**

- Improved information sharing
- Reduced duplication of effort
- More coordinated investigations

---

## BG-008 — Establish Trusted AI Adoption

Promote responsible adoption of artificial intelligence by ensuring AI-generated recommendations are transparent, explainable, and always subject to human review.

**Expected Outcome**

- Increased trust in AI-assisted workflows
- Greater adoption by operational teams
- Responsible AI governance

---

## Business Goal Summary

The business goals defined in this chapter establish the strategic direction for Sentinel AI.

Collectively, these goals aim to improve operational efficiency, strengthen fraud detection, enhance regulatory compliance, increase organizational visibility, promote responsible AI adoption, and support scalable enterprise operations.

These goals provide the foundation for the measurable Business Objectives presented in the next chapter and ensure that every business requirement contributes to a clearly defined organizational outcome.

# Chapter 4 — Business Objectives

## Business Objectives Overview

The Business Objectives define the measurable organizational outcomes that Sentinel AI is expected to achieve.

These objectives translate the strategic Business Goals into specific business outcomes that guide product planning, prioritization, and success evaluation.

Achievement of these objectives demonstrates that the platform is delivering meaningful value to cryptocurrency exchanges and supporting their operational, compliance, and security functions.

---

## BO-001 — Reduce Investigation Time

Enable analysts to complete fraud, compliance, and security investigations more efficiently by providing a centralized investigation workspace, contextual intelligence, and AI-assisted decision support.

**Success Indicators**

- Reduced manual investigation effort
- Faster case resolution
- Improved analyst productivity

---

## BO-002 — Improve Fraud Detection Effectiveness

Increase the organization's ability to identify, prioritize, and investigate suspicious activities through advanced risk intelligence and behavioral analysis.

**Success Indicators**

- Improved detection accuracy
- Reduced false positives
- Faster identification of high-risk activities

---

## BO-003 — Strengthen Regulatory Readiness

Improve organizational preparedness for regulatory reviews by maintaining standardized investigation workflows, complete audit records, and consistent compliance documentation.

**Success Indicators**

- Improved audit readiness
- Reduced compliance effort
- More consistent regulatory reporting

---

## BO-004 — Improve Operational Decision Quality

Support analysts with explainable AI-generated recommendations and contextual evidence that improve consistency and confidence during investigations.

**Success Indicators**

- Increased analyst confidence
- More consistent investigation outcomes
- Improved decision quality

---

## BO-005 — Increase Cross-Functional Collaboration

Enable Risk, Fraud, Compliance, Security, and Platform Operations teams to collaborate through shared investigations, centralized information, and standardized operational workflows.

**Success Indicators**

- Improved information sharing
- Reduced duplicate investigations
- Increased operational coordination

---

## BO-006 — Improve Operational Visibility

Provide centralized dashboards and reporting that allow operational teams and management to monitor investigations, risks, compliance activities, AI performance, and platform health.

**Success Indicators**

- Better operational awareness
- Improved executive reporting
- Faster identification of operational issues

---

## BO-007 — Support Scalable Platform Adoption

Enable organizations to adopt Sentinel AI incrementally through modular functional domains that integrate with existing enterprise systems.

**Success Indicators**

- Simplified platform adoption
- Reduced implementation complexity
- Flexible organizational deployment

---

## BO-008 — Promote Responsible AI Adoption

Ensure artificial intelligence enhances analyst productivity while maintaining explainability, transparency, governance, and human oversight.

**Success Indicators**

- Increased trust in AI-generated recommendations
- High analyst adoption of AI-assisted workflows
- Responsible AI governance across operational teams

---

## BO-009 — Improve Platform Reliability and Governance

Provide secure platform administration, operational monitoring, audit logging, and governance capabilities that support reliable enterprise operations.

**Success Indicators**

- Improved operational reliability
- Increased platform availability
- Better governance and accountability

---

## Business Objectives Summary

The Business Objectives define the measurable business outcomes that Sentinel AI is expected to deliver.

Collectively, these objectives focus on improving investigation efficiency, strengthening fraud detection, enhancing regulatory compliance, increasing operational visibility, promoting responsible AI adoption, and supporting scalable enterprise operations.

These objectives establish the business success criteria that will be realized through the Business Requirements defined in later chapters.

# Chapter 5 — Business Stakeholders

## Business Stakeholders Overview

Business stakeholders are the individuals, teams, and external organizations that influence, use, manage, govern, or are affected by Sentinel AI.

Understanding stakeholder responsibilities and expectations ensures that business requirements align with organizational objectives, regulatory obligations, and operational needs.

The following stakeholders represent the primary business groups involved in the successful adoption and operation of Sentinel AI.

---

## BS-001 — Executive Management

**Role**

Executive Management provides strategic direction, oversees organizational performance, and ensures that operational objectives align with business priorities.

**Business Interests**

- Improve operational efficiency
- Reduce financial and operational risk
- Increase organizational visibility
- Support strategic decision-making
- Measure business performance through analytics and reporting

---

## BS-002 — Risk Operations Team

**Role**

The Risk Operations team identifies, assesses, prioritizes, and manages operational risks associated with transactions, accounts, wallets, and customer activities.

**Business Interests**

- Consistent risk assessment
- Accurate risk prioritization
- Faster investigation workflows
- Explainable risk intelligence
- Improved analyst productivity

---

## BS-003 — Fraud Operations Team

**Role**

The Fraud Operations team investigates suspicious activities and works to prevent financial losses resulting from fraudulent behavior.

**Business Interests**

- Efficient investigation management
- Evidence collection and analysis
- Cross-team collaboration
- AI-assisted investigation support
- Reduced fraud losses

---

## BS-004 — Compliance Team

**Role**

The Compliance team ensures organizational adherence to regulatory requirements, internal policies, and industry standards.

**Business Interests**

- Standardized compliance workflows
- Audit-ready documentation
- Regulatory reporting
- Investigation traceability
- Reduced compliance effort

---

## BS-005 — Security Operations Team

**Role**

The Security Operations team monitors operational security events, investigates potential threats, and protects organizational assets.

**Business Interests**

- Security event visibility
- Incident investigation support
- Operational monitoring
- Improved threat response
- Centralized security intelligence

---

## BS-006 — Platform Administrators

**Role**

Platform Administrators configure organizational settings, manage platform operations, and oversee business-level administration.

**Business Interests**

- Organization management
- Configuration management
- Operational governance
- Platform configuration
- Administrative reporting

---

## BS-007 — System Administrators

**Role**

System Administrators maintain the technical environment supporting Sentinel AI, including platform availability, operational monitoring, and system reliability.

**Business Interests**

- Platform reliability
- System monitoring
- Performance visibility
- Operational stability
- Infrastructure governance

---

## BS-008 — Auditors

**Role**

Internal and external auditors evaluate operational processes, regulatory compliance, investigation records, and governance practices.

**Business Interests**

- Complete audit trails
- Investigation traceability
- Evidence availability
- Compliance documentation
- Regulatory accountability

---

## BS-009 — AI Governance Team

**Role**

The AI Governance team establishes policies that ensure artificial intelligence is deployed responsibly, transparently, and in accordance with organizational standards.

**Business Interests**

- Explainable AI recommendations
- Human oversight
- AI governance compliance
- AI performance monitoring
- Responsible AI adoption

---

## BS-010 — External Regulators

**Role**

Regulatory authorities establish the legal and compliance framework within which cryptocurrency exchanges operate.

Sentinel AI supports organizational compliance with regulatory obligations but does not replace regulatory oversight.

**Business Interests**

- Regulatory transparency
- Audit readiness
- Compliance reporting
- Investigation accountability
- Evidence integrity

---

## Stakeholder Relationships

Successful operation of Sentinel AI requires collaboration among all stakeholder groups.

Executive Management provides strategic oversight, operational teams perform investigations, Compliance and Audit functions ensure regulatory adherence, Platform and System Administrators maintain platform operations, AI Governance oversees responsible AI usage, and External Regulators establish the compliance framework that the organization must satisfy.

Together, these stakeholders ensure that Sentinel AI delivers secure, compliant, explainable, and efficient operational intelligence across the enterprise.

---

## Business Stakeholders Summary

The stakeholders identified in this chapter represent the primary business groups that influence, operate, govern, or are affected by Sentinel AI.

Their business objectives, operational responsibilities, and organizational interests provide essential context for the Business Requirements defined in later chapters and ensure that the platform addresses the needs of all major stakeholders.

# Chapter 6 — Business Context Models

## Business Capability Map

Before defining individual Business Requirements, the following capability map summarizes the business capabilities Sentinel AI must support.

```text
Risk Intelligence
├── Risk Assessment
├── Risk Prioritization
└── Behavioral Analysis

Fraud Investigation
├── Case Management
├── Evidence
└── Collaboration

Compliance Operations
├── Travel Rule
├── AML
└── Audit

Security Intelligence
├── API Security
├── Authentication
└── Device Intelligence

Wallet Intelligence
├── Wallet Profiling
├── Address Reputation
└── Relationship Analysis

AI Platform
├── Investigation Support
├── Evidence Retrieval
└── Report Generation

Reporting & Analytics
├── Operational Dashboards
└── Executive Reporting

Administration
├── User & Access Governance
└── Platform Configuration

Platform Operations
├── Monitoring
├── Alerting
└── Diagnostics
```

---

## Business Process Overview

The following high-level process describes the primary investigation lifecycle supported by Sentinel AI.

```text
Transaction / Event
        ↓
Alert Generated
        ↓
Risk Assessment
        ↓
Evidence Collection
        ↓
AI Investigation Assistant
        ↓
Analyst Investigation
        ↓
Decision
        ↓
Case Closure
        ↓
Audit & Reporting
```

This process provides business context for the requirements that follow. Detailed workflow design is deferred to Functional Requirements and Architecture documents.

---

## Business Domain Model

The following conceptual model identifies the core business entities that Sentinel AI must support.

```text
Organization
  ↓
Users
  ↓
Investigations
  ↓
Alerts
  ↓
Risk
  ↓
Evidence
  ↓
Wallet
  ↓
Transactions
  ↓
Cases
```

This is a business domain model, not a database schema. Physical data design is defined in Database Design documentation.

---

## Capability Ownership

| Capability | Owner | Primary Stakeholders | Priority |
|------------|-------|----------------------|----------|
| Risk Intelligence | Risk Operations | Risk Team, Fraud Team, Compliance | High |
| Fraud Investigation | Fraud Operations | Fraud Team, Risk Team, Compliance | High |
| Compliance Operations | Compliance Team | Compliance, Auditors, Risk Team | High |
| Security Intelligence | Security Operations (SOC) | SOC, Risk Team, Platform Ops | Medium |
| Wallet Intelligence | Financial Crime Investigation | Fraud Team, Risk Team, Compliance | Medium |
| AI Platform | AI Engineering | Risk, Fraud, Compliance, AI Governance | High |
| Reporting & Analytics | Risk Operations | Executive Management, All Ops Teams | Medium |
| Administration | Platform Operations | Platform Admins, System Admins | High |
| Platform Operations | Platform Operations | System Admins, SRE, Executive Management | Medium |

---

# Chapter 7 — Business Assumptions, Constraints, and Risks

## Business Assumptions

The following assumptions underpin the Business Requirements.

### BA-001 — Existing KYC Capability

The exchange already maintains KYC processes and customer identity records outside Sentinel AI.

### BA-002 — Transaction Data Availability

Exchange transaction events are available for risk analysis and investigation workflows.

### BA-003 — Integration Interfaces Available

Approved APIs or integration interfaces exist (or will be provided) for connecting Sentinel AI to exchange operational systems.

### BA-004 — Blockchain Intelligence Sources

Blockchain analytics or wallet intelligence data sources are available through approved providers or internal systems.

### BA-005 — Human Analyst Oversight

Human analysts review AI-generated recommendations before final operational decisions are made.

### BA-006 — Established Operational Procedures

The organization has established procedures for fraud investigation, compliance review, and security incident response.

---

## Business Constraints

The following constraints define non-negotiable business boundaries for Sentinel AI.

### BC-001 — No Automatic Account Freezing

Sentinel AI shall not automatically freeze customer accounts without authorized human approval.

### BC-002 — No Autonomous Trading

Sentinel AI shall not execute trades or operate as a trading engine.

### BC-003 — Human Review of AI Recommendations

AI recommendations require human review before enforcement or final operational decisions.

### BC-004 — Regulatory Compliance Required

Platform usage must support applicable regulatory and organizational compliance obligations.

### BC-005 — Integration With Existing Systems

Sentinel AI must integrate with existing exchange systems rather than replacing core exchange infrastructure.

### BC-006 — No Wallet Custody

Sentinel AI shall not store, custody, or manage customer digital assets.

---

## Business Risks

The following business risks may affect successful adoption and operation of Sentinel AI.

| ID | Risk | Impact | Mitigation |
|----|------|--------|------------|
| BRISK-001 | AI hallucinations | High | Human approval, explainability, evaluation framework |
| BRISK-002 | Regulatory changes | High | Configurable policies and modular compliance workflows |
| BRISK-003 | Data quality issues | High | Validation, confidence scoring, source traceability |
| BRISK-004 | Integration failures | Medium | Retry strategy, standardized interfaces, graceful degradation |
| BRISK-005 | Low AI adoption | Medium | Analyst training, transparent recommendations, feedback loops |
| BRISK-006 | AI model drift | High | Continuous evaluation, prompt optimization, analyst feedback |
| BRISK-007 | False positives / false negatives | High | Hybrid rules + behavioral analysis, continuous tuning |
| BRISK-008 | Unauthorized access to investigations | High | Role-based access, audit trails, separation of duties |

---

# Chapter 8 — Business Requirements

## Business Requirements Overview

The Business Requirements define the organizational capabilities that Sentinel AI must provide to achieve the Business Goals and Business Objectives described in previous chapters.

Each requirement represents a business need rather than a technical implementation detail.

These requirements serve as the primary source for deriving Functional Requirements, System Architecture, API Specifications, Database Design, and acceptance criteria.

### Requirement Governance

Every Business Requirement is managed using a consistent template and shared lifecycle statuses.

### Business Requirement Template

```text
ID
Title
Description
Business Value
Priority
Release
Owner
Stakeholders
Dependencies
Source
Acceptance Criteria Reference
FR Reference
Related APIs
Related Database
Related AI Agents
Status
```

### Requirement Status Lifecycle

All requirements in this document and subsequent specifications use the following status values:

| Status | Meaning |
|--------|---------|
| Draft | Requirement is proposed and may still change |
| Under Review | Requirement is being reviewed by stakeholders |
| Approved | Requirement is accepted as baseline for design and delivery |
| Implemented | Requirement has been delivered and verified |
| Deprecated | Requirement is no longer active and should not guide new work |

### Requirement Attributes

| Attribute | Purpose |
|-----------|---------|
| ID | Domain-prefixed identifier for scalable traceability |
| Title | Short capability name |
| Description | Business need stated as *The organization shall...* |
| Business Value | Organizational outcome delivered |
| Priority | High / Medium |
| Release | MVP / Version 2 |
| Owner | Accountable business or platform owner |
| Stakeholders | Teams affected by or consuming the capability |
| Dependencies | Other Business Requirements required first |
| Source | Related problems, goals, or business drivers |
| Acceptance Criteria Reference | Link to FRS acceptance criteria (future) |
| FR Reference | Provisional Functional Requirement range |
| Related APIs / Database / AI Agents | Implementation mapping placeholders |
| Status | Lifecycle state from the Requirement Status Lifecycle |

### Requirement ID Convention

Business Requirements use domain-prefixed identifiers to support large-scale documentation:

| Prefix | Domain |
|--------|--------|
| `RI-BR-###` | Risk Intelligence |
| `FI-BR-###` | Fraud Investigation |
| `CP-BR-###` | Compliance Operations |
| `SEC-BR-###` | Security Intelligence |
| `WI-BR-###` | Wallet Intelligence |
| `AI-BR-###` | AI Platform |
| `RA-BR-###` | Reporting & Analytics |
| `ADM-BR-###` | Administration |
| `OPS-BR-###` | Platform Operations |

Legacy sequential identifiers (BR-001 – BR-016) are superseded by these domain-prefixed IDs.

### Requirement Statement Style

Business Requirements use:

> The organization shall...

Functional Requirements (FRS) will use:

> The system shall...

This distinction keeps business needs separate from system behavior.

This document intentionally limits Business Requirements to **16 high-level capabilities**.

---

## Requirement Categories

| Domain | Requirements |
|--------|-------------:|
| Risk Intelligence | 3 |
| Fraud Investigation | 3 |
| Compliance Operations | 2 |
| Security Intelligence | 2 |
| Wallet Intelligence | 1 |
| AI Platform | 2 |
| Reporting & Analytics | 1 |
| Administration | 1 |
| Platform Operations | 1 |
| **Total** | **16** |

---

## Requirement Priority Matrix

| Priority | Release Target | Guidance |
|----------|----------------|----------|
| High | MVP | Required for initial operational value |
| Medium | Version 2 | Important expansion after MVP |

```text
High
  ↓
MVP

Medium
  ↓
Version 2
```

A Future Release tier may be introduced later when additional deferred business capabilities are defined. At present, all 16 Business Requirements are assigned to MVP or Version 2.

---

## Business Capability – Risk Intelligence

### RI-BR-001 — Centralized Risk Assessment

| Attribute | Value |
|-----------|-------|
| Priority | High |
| Release | MVP |
| Status | Draft |
| Owner | Risk Operations |
| Stakeholders | Risk Team, Fraud Team, Compliance |
| Related Objectives | BO-001, BO-002 |
| Dependencies | — |
| Source | BP-001, BP-003, BG-002 |
| Acceptance Criteria Reference | To be defined in FRS |
| FR Reference | FR-001 – FR-010 (provisional) |
| Related APIs | To be defined |
| Related Database | To be defined |
| Related AI Agents | To be defined |

The organization shall be able to assess operational risk across transactions, accounts, wallets, and behavioral activities through a unified risk intelligence capability.

**Business Value**

Provides consistent and standardized risk evaluation across the organization.

---

### RI-BR-002 — Risk Prioritization

| Attribute | Value |
|-----------|-------|
| Priority | High |
| Release | MVP |
| Status | Draft |
| Owner | Risk Operations |
| Stakeholders | Risk Team, Fraud Team |
| Related Objectives | BO-001, BO-002 |
| Dependencies | RI-BR-001 |
| Source | BP-001, BG-001, BG-002 |
| Acceptance Criteria Reference | To be defined in FRS |
| FR Reference | FR-001 – FR-010 (provisional) |
| Related APIs | To be defined |
| Related Database | To be defined |
| Related AI Agents | To be defined |

The organization shall be able to prioritize suspicious activities based on configurable risk criteria to improve investigation efficiency.

**Business Value**

Enables analysts to focus on the highest-risk activities first.

---

### RI-BR-003 — Explainable Risk Analysis

| Attribute | Value |
|-----------|-------|
| Priority | High |
| Release | MVP |
| Status | Draft |
| Owner | Risk Operations |
| Stakeholders | Risk Team, Compliance, AI Governance |
| Related Objectives | BO-004, BO-008 |
| Dependencies | RI-BR-001 |
| Source | BP-006, BG-004, BG-008 |
| Acceptance Criteria Reference | To be defined in FRS |
| FR Reference | FR-001 – FR-010 (provisional) |
| Related APIs | To be defined |
| Related Database | To be defined |
| Related AI Agents | To be defined |

The organization shall receive understandable explanations supporting every calculated risk assessment.

**Business Value**

Improves analyst confidence and supports transparent decision-making.

---

## Business Capability – Fraud Investigation

### FI-BR-001 — Investigation Management

| Attribute | Value |
|-----------|-------|
| Priority | High |
| Release | MVP |
| Status | Draft |
| Owner | Fraud Operations |
| Stakeholders | Fraud Team, Risk Team, Compliance |
| Related Objectives | BO-001, BO-005 |
| Dependencies | RI-BR-001 |
| Source | BP-001, BG-001, BG-007 |
| Acceptance Criteria Reference | To be defined in FRS |
| FR Reference | FR-020 – FR-035 (provisional) |
| Related APIs | To be defined |
| Related Database | To be defined |
| Related AI Agents | To be defined |

The organization shall be able to manage fraud investigations throughout their complete lifecycle.

**Business Value**

Supports consistent investigation processes and improves case management.

---

### FI-BR-002 — Evidence Management

| Attribute | Value |
|-----------|-------|
| Priority | High |
| Release | MVP |
| Status | Draft |
| Owner | Fraud Operations |
| Stakeholders | Fraud Team, Compliance, Auditors |
| Related Objectives | BO-001, BO-004 |
| Dependencies | FI-BR-001 |
| Source | BP-005, BG-001, BG-004 |
| Acceptance Criteria Reference | To be defined in FRS |
| FR Reference | FR-020 – FR-035 (provisional) |
| Related APIs | To be defined |
| Related Database | To be defined |
| Related AI Agents | To be defined |

The organization shall be able to collect, organize, and review investigation evidence within a centralized investigation workspace.

**Business Value**

Improves investigation quality and reduces duplicated effort.

---

### FI-BR-003 — Investigation Collaboration

| Attribute | Value |
|-----------|-------|
| Priority | Medium |
| Release | Version 2 |
| Status | Draft |
| Owner | Fraud Operations |
| Stakeholders | Fraud Team, Risk Team, Compliance, Security |
| Related Objectives | BO-005 |
| Dependencies | FI-BR-001 |
| Source | BP-001, BG-007 |
| Acceptance Criteria Reference | To be defined in FRS |
| FR Reference | FR-020 – FR-035 (provisional) |
| Related APIs | To be defined |
| Related Database | To be defined |
| Related AI Agents | To be defined |

The organization shall enable operational teams to collaborate on investigations through shared information and standardized workflows.

**Business Value**

Improves cross-functional coordination.

---

## Business Capability – Compliance Operations

### CP-BR-001 — Regulatory Investigation Support

| Attribute | Value |
|-----------|-------|
| Priority | High |
| Release | MVP |
| Status | Draft |
| Owner | Compliance Team |
| Stakeholders | Compliance, Auditors, Risk Team |
| Related Objectives | BO-003 |
| Dependencies | FI-BR-001 |
| Source | BP-004, BG-003 |
| Acceptance Criteria Reference | To be defined in FRS |
| FR Reference | FR-040 – FR-055 (provisional) |
| Related APIs | To be defined |
| Related Database | To be defined |
| Related AI Agents | To be defined |

The organization shall be able to perform compliance investigations using standardized operational workflows.

**Business Value**

Improves regulatory readiness and audit preparedness.

---

### CP-BR-002 — Compliance Evidence Management

| Attribute | Value |
|-----------|-------|
| Priority | High |
| Release | MVP |
| Status | Draft |
| Owner | Compliance Team |
| Stakeholders | Compliance, Auditors |
| Related Objectives | BO-003 |
| Dependencies | CP-BR-001, FI-BR-002 |
| Source | BP-004, BG-003 |
| Acceptance Criteria Reference | To be defined in FRS |
| FR Reference | FR-040 – FR-055 (provisional) |
| Related APIs | To be defined |
| Related Database | To be defined |
| Related AI Agents | To be defined |

The organization shall maintain complete compliance documentation and audit evidence throughout each investigation.

**Business Value**

Supports regulatory reporting and audit activities.

---

## Business Capability – Security Intelligence

### SEC-BR-001 — Security Event Monitoring

| Attribute | Value |
|-----------|-------|
| Priority | Medium |
| Release | Version 2 |
| Status | Draft |
| Owner | Security Operations (SOC) |
| Stakeholders | SOC, Risk Team, Platform Operations |
| Related Objectives | BO-002, BO-006 |
| Dependencies | — |
| Source | BP-002, BP-003, BG-002 |
| Acceptance Criteria Reference | To be defined in FRS |
| FR Reference | FR-060 – FR-070 (provisional) |
| Related APIs | To be defined |
| Related Database | To be defined |
| Related AI Agents | To be defined |

The organization shall monitor operational security activities that may indicate unauthorized access or account compromise.

**Business Value**

Improves organizational security awareness.

---

### SEC-BR-002 — Security Investigation Support

| Attribute | Value |
|-----------|-------|
| Priority | Medium |
| Release | Version 2 |
| Status | Draft |
| Owner | Security Operations (SOC) |
| Stakeholders | SOC, Fraud Team, Risk Team |
| Related Objectives | BO-001, BO-002 |
| Dependencies | SEC-BR-001, FI-BR-001 |
| Source | BP-001, BG-001 |
| Acceptance Criteria Reference | To be defined in FRS |
| FR Reference | FR-060 – FR-070 (provisional) |
| Related APIs | To be defined |
| Related Database | To be defined |
| Related AI Agents | To be defined |

The organization shall enable security teams to investigate operational security incidents using centralized investigation capabilities.

**Business Value**

Improves incident response effectiveness.

---

## Business Capability – Wallet Intelligence

### WI-BR-001 — Blockchain Investigation Support

| Attribute | Value |
|-----------|-------|
| Priority | Medium |
| Release | Version 2 |
| Status | Draft |
| Owner | Financial Crime Investigation |
| Stakeholders | Fraud Team, Risk Team, Compliance |
| Related Objectives | BO-002 |
| Dependencies | RI-BR-001 |
| Source | BP-003, BP-005, BG-002 |
| Acceptance Criteria Reference | To be defined in FRS |
| FR Reference | FR-071 – FR-079 (provisional) |
| Related APIs | To be defined |
| Related Database | To be defined |
| Related AI Agents | To be defined |

The organization shall analyze wallet activity and blockchain relationships to support financial crime investigations.

**Business Value**

Provides contextual intelligence for blockchain investigations.

---

## Business Capability – AI Platform

### AI-BR-001 — AI-Assisted Decision Support

| Attribute | Value |
|-----------|-------|
| Priority | High |
| Release | MVP |
| Status | Draft |
| Owner | AI Engineering |
| Stakeholders | Risk Team, Fraud Team, Compliance, AI Governance |
| Related Objectives | BO-001, BO-004, BO-008 |
| Dependencies | RI-BR-001, FI-BR-002; WI-BR-001 (enhancing / Version 2) |
| Source | BP-006, BG-004, BG-008 |
| Acceptance Criteria Reference | To be defined in FRS |
| FR Reference | FR-080 – FR-100 (provisional) |
| Related APIs | To be defined |
| Related Database | To be defined |
| Related AI Agents | To be defined |

The organization shall provide analysts with explainable AI-generated recommendations throughout the investigation lifecycle.

**Business Value**

Improves productivity while maintaining human oversight.

---

### AI-BR-002 — Knowledge Retrieval

| Attribute | Value |
|-----------|-------|
| Priority | Medium |
| Release | Version 2 |
| Status | Draft |
| Owner | AI Engineering |
| Stakeholders | Risk Team, Fraud Team, Compliance |
| Related Objectives | BO-001, BO-004 |
| Dependencies | AI-BR-001, FI-BR-002 |
| Source | BP-005, BG-001, BG-004 |
| Acceptance Criteria Reference | To be defined in FRS |
| FR Reference | FR-080 – FR-100 (provisional) |
| Related APIs | To be defined |
| Related Database | To be defined |
| Related AI Agents | To be defined |

The organization shall provide contextual information and historical knowledge that assists analysts during investigations.

**Business Value**

Reduces manual research effort.

---

## Business Capability – Reporting & Analytics

### RA-BR-001 — Operational Reporting

| Attribute | Value |
|-----------|-------|
| Priority | Medium |
| Release | Version 2 |
| Status | Draft |
| Owner | Risk Operations |
| Stakeholders | Executive Management, All Operational Teams |
| Related Objectives | BO-006 |
| Dependencies | FI-BR-001, RI-BR-001 |
| Source | BP-002, BG-005 |
| Acceptance Criteria Reference | To be defined in FRS |
| FR Reference | FR-101 – FR-110 (provisional) |
| Related APIs | To be defined |
| Related Database | To be defined |
| Related AI Agents | To be defined |

The organization shall monitor operational performance through centralized dashboards, reports, and business analytics.

**Business Value**

Improves organizational visibility and decision-making.

---

## Business Capability – Administration

### ADM-BR-001 — Organizational Governance

| Attribute | Value |
|-----------|-------|
| Priority | High |
| Release | MVP |
| Status | Draft |
| Owner | Platform Operations |
| Stakeholders | Platform Administrators, System Administrators, Executive Management |
| Related Objectives | BO-007, BO-009 |
| Dependencies | — |
| Source | BG-006, BG-008 |
| Acceptance Criteria Reference | To be defined in FRS |
| FR Reference | FR-111 – FR-120 (provisional) |
| Related APIs | To be defined |
| Related Database | To be defined |
| Related AI Agents | To be defined |

The organization shall manage users, permissions, organizations, and platform configuration through centralized administrative capabilities.

**Business Value**

Supports secure enterprise governance.

---

## Business Capability – Platform Operations

### OPS-BR-001 — Operational Monitoring

| Attribute | Value |
|-----------|-------|
| Priority | Medium |
| Release | Version 2 |
| Status | Draft |
| Owner | Platform Operations |
| Stakeholders | System Administrators, SRE, Executive Management |
| Related Objectives | BO-006, BO-009 |
| Dependencies | ADM-BR-001 |
| Source | BG-006 |
| Acceptance Criteria Reference | To be defined in FRS |
| FR Reference | FR-121 – FR-130 (provisional) |
| Related APIs | To be defined |
| Related Database | To be defined |
| Related AI Agents | To be defined |

The organization shall continuously monitor platform health, operational metrics, audit activities, and system reliability.

**Business Value**

Supports reliable enterprise operations.

---

## Requirement Dependencies

The following dependency relationships guide sequencing for MVP and later releases.

```text
RI-BR-001
  ├── RI-BR-002
  ├── RI-BR-003
  ├── FI-BR-001
  │     ├── FI-BR-002
  │     │     ├── CP-BR-002
  │     │     └── AI-BR-001
  │     ├── FI-BR-003
  │     ├── CP-BR-001
  │     └── SEC-BR-002
  ├── WI-BR-001
  │     └── AI-BR-001
  └── RA-BR-001

SEC-BR-001
  └── SEC-BR-002

AI-BR-001
  └── AI-BR-002

ADM-BR-001
  └── OPS-BR-001
```

Notable dependency:

```text
AI-BR-001
  depends on
    RI-BR-001
    FI-BR-002
  enhanced by
    WI-BR-001 (Version 2)
```

MVP AI-assisted decision support can operate with Risk Intelligence and Evidence Management. Wallet Intelligence enhances AI recommendations when available in Version 2.

---

## Business Requirements Summary

The Business Requirements defined in this chapter describe the organizational capabilities required for Sentinel AI to achieve its strategic goals and business objectives.

This document intentionally limits Business Requirements to **16 high-level capabilities**. Detailed system behavior belongs in the Functional Requirements Specification (FRS), where each Business Requirement may expand into multiple Functional Requirements.

Traceability follows:

```text
Problem
  ↓
Goal
  ↓
Objective
  ↓
Business Requirement
  ↓
Functional Domain
  ↓
Functional Requirement
  ↓
Microservice
  ↓
API
  ↓
Database
  ↓
Testing
```

---

# Chapter 9 — Business Rules

## Business Rules Overview

Business Rules define the organizational policies, governance principles, and operational standards that govern how Sentinel AI is used within cryptocurrency exchange operations.

These rules represent business policies rather than software functionality. They ensure that investigations, risk assessments, compliance activities, and AI-assisted decisions are performed consistently, transparently, and in accordance with organizational governance.

All functional requirements, workflows, and business processes must comply with these rules.

---

## BRULE-001 — Investigation Ownership

Every investigation shall have a designated owner who is responsible for managing the investigation throughout its lifecycle.

**Business Purpose**

- Ensure accountability
- Prevent unassigned investigations
- Improve operational ownership

---

## BRULE-002 — Human Decision Authority

Final operational decisions shall always be made by authorized personnel.

Artificial intelligence may provide recommendations but shall not make autonomous operational decisions.

**Business Purpose**

- Maintain human accountability
- Support responsible AI adoption
- Reduce operational risk

---

## BRULE-003 — Explainable AI

AI-generated recommendations shall be accompanied by sufficient explanation to enable analysts to understand the reasoning behind each recommendation.

**Business Purpose**

- Increase analyst trust
- Support transparent decision-making
- Improve AI governance

---

## BRULE-004 — Risk Assessment Traceability

Every risk assessment shall be traceable to the supporting evidence, business rules, and operational data used during the assessment.

**Business Purpose**

- Improve transparency
- Support audits
- Enable investigation review

---

## BRULE-005 — Investigation Audit Trail

All investigation activities shall be recorded to provide a complete historical record of operational actions.

**Business Purpose**

- Support accountability
- Enable regulatory audits
- Maintain investigation history

---

## BRULE-006 — Evidence Integrity

Investigation evidence shall remain accurate, complete, and protected from unauthorized modification.

**Business Purpose**

- Preserve investigation quality
- Support regulatory compliance
- Maintain evidentiary integrity

---

## BRULE-007 — Access Authorization

Only authorized personnel shall access investigations, operational information, and administrative functions appropriate to their organizational responsibilities.

**Business Purpose**

- Protect sensitive information
- Support organizational governance
- Reduce unauthorized access

---

## BRULE-008 — Regulatory Compliance

Operational activities performed using Sentinel AI shall comply with applicable organizational policies and regulatory obligations.

**Business Purpose**

- Maintain regulatory readiness
- Support organizational compliance
- Reduce compliance risk

---

## BRULE-009 — Standardized Investigation Process

Investigations shall follow standardized organizational workflows to ensure consistency across operational teams.

**Business Purpose**

- Improve investigation quality
- Reduce process variation
- Increase operational efficiency

---

## BRULE-010 — Separation of Duties

Critical operational activities, including investigation approval, administrative configuration, and governance functions, shall be performed according to organizational separation-of-duty policies.

**Business Purpose**

- Reduce operational risk
- Prevent conflicts of interest
- Strengthen governance

---

## BRULE-011 — Operational Record Retention

Investigation records, audit logs, and compliance documentation shall be retained in accordance with organizational retention policies and applicable regulatory requirements.

**Business Purpose**

- Support audits
- Preserve historical records
- Meet regulatory obligations

---

## BRULE-012 — Continuous Governance

Operational policies, AI governance standards, and business rules shall be periodically reviewed to ensure continued alignment with organizational objectives and regulatory requirements.

**Business Purpose**

- Support continuous improvement
- Adapt to regulatory changes
- Maintain effective governance

---

## Business Rules Summary

The Business Rules defined in this chapter establish the organizational policies that govern the use of Sentinel AI.

These rules ensure that investigations, AI-assisted decision support, compliance activities, security operations, and administrative processes are performed consistently, transparently, and responsibly.

All Business Requirements, Functional Requirements, operational workflows, and implementation decisions shall conform to these Business Rules.



# Chapter 10 — Business Success Metrics

## Business Success Metrics Overview

Business Success Metrics define the key performance indicators (KPIs) used to evaluate whether Sentinel AI achieves its intended business outcomes.

These metrics enable organizations to measure improvements in operational efficiency, fraud detection, regulatory compliance, AI adoption, and overall business performance.

The metrics presented in this chapter provide a framework for evaluating the business value delivered by Sentinel AI throughout its operational lifecycle.

---

## BSM-001 — Investigation Turnaround Time

**Objective**

Measure the time required to complete operational investigations from initiation to closure.

**Business Value**

- Evaluate investigation efficiency
- Identify workflow bottlenecks
- Improve analyst performance

**Measurement Criteria**

- Average investigation completion time
- Investigation backlog
- Percentage of investigations completed within organizational targets

---

## BSM-002 — Fraud Detection Effectiveness

**Objective**

Measure the organization's ability to identify and investigate fraudulent activities accurately.

**Business Value**

- Improve fraud prevention
- Reduce financial losses
- Increase operational confidence

**Measurement Criteria**

- Fraud detection rate
- False positive rate
- False negative rate
- Confirmed fraud investigations

---

## BSM-003 — Analyst Productivity

**Objective**

Measure improvements in analyst efficiency through standardized workflows and AI-assisted decision support.

**Business Value**

- Increase operational efficiency
- Reduce manual effort
- Improve resource utilization

**Measurement Criteria**

- Investigations completed per analyst
- Average investigation workload
- Reduction in manual processing activities

---

## BSM-004 — Audit Readiness

**Objective**

Measure the organization's ability to support internal and external audits using complete operational records.

**Business Value**

- Improve regulatory preparedness
- Reduce audit effort
- Strengthen governance

**Measurement Criteria**

- Availability of audit evidence
- Completeness of investigation records
- Audit findings related to operational processes

---

## BSM-005 — Compliance Reporting Timeliness

**Objective**

Measure the organization's ability to prepare and deliver compliance reports within required timeframes.

**Business Value**

- Improve regulatory responsiveness
- Reduce compliance delays
- Enhance reporting consistency

**Measurement Criteria**

- On-time compliance report submission
- Average report preparation time
- Regulatory reporting accuracy

---

## BSM-006 — AI Adoption Rate

**Objective**

Measure the extent to which operational teams utilize AI-assisted capabilities during investigations.

**Business Value**

- Evaluate AI adoption
- Increase analyst confidence
- Support responsible AI implementation

**Measurement Criteria**

- Percentage of investigations using AI recommendations
- Analyst acceptance of AI-assisted workflows
- User satisfaction with AI capabilities

---

## BSM-007 — Operational Visibility

**Objective**

Measure improvements in organizational awareness through centralized dashboards and reporting.

**Business Value**

- Improve management oversight
- Support informed decision-making
- Increase operational transparency

**Measurement Criteria**

- Dashboard utilization
- Report usage frequency
- Availability of operational insights

---

## BSM-008 — Cross-Functional Collaboration

**Objective**

Measure the effectiveness of collaboration between Risk, Fraud, Compliance, Security, and Platform Operations teams.

**Business Value**

- Improve information sharing
- Reduce duplicated effort
- Increase operational coordination

**Measurement Criteria**

- Shared investigations across teams
- Cross-functional case participation
- Reduction in duplicate investigations

---

## BSM-009 — Platform Availability

**Objective**

Measure the operational availability and reliability of Sentinel AI.

**Business Value**

- Ensure continuous business operations
- Minimize operational disruption
- Support enterprise reliability

**Measurement Criteria**

- Platform uptime
- Service availability
- Business-impacting operational interruptions

---

## BSM-010 — Business Value Realization

**Objective**

Measure the overall business value delivered through adoption of Sentinel AI.

**Business Value**

- Demonstrate return on investment
- Support strategic planning
- Evaluate long-term organizational impact

**Measurement Criteria**

- Reduction in operational costs
- Improvement in investigation efficiency
- Increased stakeholder satisfaction
- Achievement of business objectives

---

## Business Success Metrics Summary

The Business Success Metrics defined in this chapter provide a structured framework for evaluating the effectiveness of Sentinel AI from a business perspective.

Collectively, these metrics measure improvements in investigation efficiency, fraud detection, regulatory compliance, analyst productivity, AI adoption, operational visibility, platform reliability, and overall business value.

These metrics enable organizations to assess whether Sentinel AI successfully delivers the strategic goals and business objectives established throughout this Business Requirements Specification.

---

# Chapter 11 — Requirement Traceability Matrix

## Traceability Overview

Every Functional Requirement, design artifact, and test case should ultimately trace to one or more Business Requirements in this document.

```text
Problem
  ↓
Goal
  ↓
Objective
  ↓
Business Requirement
  ↓
Functional Domain
  ↓
Functional Requirement
  ↓
Microservice
  ↓
API
  ↓
Database
  ↓
Testing
```

---

## Business Requirement Traceability Matrix

Functional Requirement identifiers and ranges are **provisional placeholders** and will be finalized in the Functional Requirements Specification.

| Business Requirement | Domain | Priority | Release | Related Objectives | Dependencies | Maps to (Provisional) |
|----------------------|--------|----------|---------|--------------------|--------------|------------------------|
| RI-BR-001 | Risk Intelligence | High | MVP | BO-001, BO-002 | — | FR-001 – FR-010 |
| RI-BR-002 | Risk Intelligence | High | MVP | BO-001, BO-002 | RI-BR-001 | FR-001 – FR-010 |
| RI-BR-003 | Risk Intelligence | High | MVP | BO-004, BO-008 | RI-BR-001 | FR-001 – FR-010 |
| FI-BR-001 | Fraud Investigation | High | MVP | BO-001, BO-005 | RI-BR-001 | FR-020 – FR-035 |
| FI-BR-002 | Fraud Investigation | High | MVP | BO-001, BO-004 | FI-BR-001 | FR-020 – FR-035 |
| FI-BR-003 | Fraud Investigation | Medium | Version 2 | BO-005 | FI-BR-001 | FR-020 – FR-035 |
| CP-BR-001 | Compliance Operations | High | MVP | BO-003 | FI-BR-001 | FR-040 – FR-055 |
| CP-BR-002 | Compliance Operations | High | MVP | BO-003 | CP-BR-001, FI-BR-002 | FR-040 – FR-055 |
| SEC-BR-001 | Security Intelligence | Medium | Version 2 | BO-002, BO-006 | — | FR-060 – FR-070 |
| SEC-BR-002 | Security Intelligence | Medium | Version 2 | BO-001, BO-002 | SEC-BR-001, FI-BR-001 | FR-060 – FR-070 |
| WI-BR-001 | Wallet Intelligence | Medium | Version 2 | BO-002 | RI-BR-001 | FR-071 – FR-079 |
| AI-BR-001 | AI Platform | High | MVP | BO-001, BO-004, BO-008 | RI-BR-001, FI-BR-002; WI-BR-001 (enhancing) | FR-080 – FR-100 |
| AI-BR-002 | AI Platform | Medium | Version 2 | BO-001, BO-004 | AI-BR-001, FI-BR-002 | FR-080 – FR-100 |
| RA-BR-001 | Reporting & Analytics | Medium | Version 2 | BO-006 | FI-BR-001, RI-BR-001 | FR-101 – FR-110 |
| ADM-BR-001 | Administration | High | MVP | BO-007, BO-009 | — | FR-111 – FR-120 |
| OPS-BR-001 | Platform Operations | Medium | Version 2 | BO-006, BO-009 | ADM-BR-001 | FR-121 – FR-130 |

---

## Problem-to-Requirement Coverage (Preview)

| Business Problem | Supporting Goals | Supporting Objectives | Supporting Requirements |
|------------------|------------------|------------------------|-------------------------|
| BP-001 Fragmented Investigation Workflows | BG-001, BG-007 | BO-001, BO-005 | FI-BR-001, FI-BR-002, FI-BR-003 |
| BP-002 Limited Operational Visibility | BG-005 | BO-006 | RA-BR-001, SEC-BR-001, OPS-BR-001 |
| BP-003 Increasing Financial Crime Complexity | BG-002 | BO-002 | RI-BR-001, RI-BR-002, WI-BR-001, AI-BR-001 |
| BP-004 Regulatory Compliance Challenges | BG-003 | BO-003 | CP-BR-001, CP-BR-002 |
| BP-005 Inefficient Use of Operational Data | BG-001, BG-004 | BO-001, BO-004 | FI-BR-002, AI-BR-002, WI-BR-001 |
| BP-006 Limited Decision Support | BG-004, BG-008 | BO-004, BO-008 | RI-BR-003, AI-BR-001 |

---

# Chapter 12 — Business Glossary

| Term | Definition |
|------|------------|
| Alert | An operational signal indicating potentially suspicious or high-risk activity requiring review. |
| AML | Anti-Money Laundering — controls and processes that detect and prevent money-laundering activity. |
| Analyst | An authorized operational user who investigates alerts, cases, and recommendations. |
| Audit Trail | A complete historical record of investigation and operational actions for accountability. |
| Behavioral Analysis | Evaluation of user, account, or system behavior patterns to identify anomalies. |
| Case | A managed investigation record that tracks status, ownership, evidence, and outcomes. |
| Compliance | Organizational adherence to regulatory requirements, policies, and industry standards. |
| Evidence | Information collected and retained to support investigation findings and decisions. |
| Fraud Ring | A coordinated group of entities collaborating to commit fraudulent activity. |
| Investigation | The structured process of reviewing alerts, evidence, and context to reach an operational decision. |
| KYC | Know Your Customer — processes used to verify customer identity and assess customer risk. |
| Risk | The assessed likelihood and impact of harmful financial, operational, or compliance outcomes. |
| Risk Score | A quantified representation of assessed risk used to prioritize operational attention. |
| Travel Rule | Regulatory requirement for sharing originator and beneficiary information in certain transfers. |
| Wallet | A blockchain address or related set of addresses associated with digital asset activity. |
| AI Agent | An AI-assisted capability that supports analysts with recommendations, retrieval, or reporting. |
| Explainability | The ability to understand and review the evidence and reasoning behind a recommendation or score. |
| Human Oversight | The requirement that authorized humans retain authority over final enforcement decisions. |
| MVP | Minimum Viable Product — the first release delivering core High-priority business capabilities. |

---

## Document Closing

This Business Requirements Specification establishes the business foundation for Sentinel AI.

It intentionally remains focused on organizational needs, capabilities, governance, and outcomes. Detailed system behavior, architecture, interfaces, and implementation design are deferred to subsequent engineering documents that must remain traceable to the requirements defined herein.
