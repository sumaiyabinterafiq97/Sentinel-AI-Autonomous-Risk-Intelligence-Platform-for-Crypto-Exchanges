# Product Scope

## Document Information

| Field | Value |
|--------|-------|
| Project Name | Sentinel AI |
| Document | Product Scope |
| Version | 1.0 |
| Status | Approved |
| Owner | To Be Assigned |
| Authors | Product & Engineering Team |
| Last Updated | 2026-07-28 |

---

## Revision History

| Version | Date | Author | Notes |
|---------|------|--------|-------|
| 0.1 | 2026-07-28 | — | Initial draft |
| 0.2 | 2026-07-28 | — | Chapter 1 enterprise refinements |
| 0.3 | 2026-07-28 | — | Capability map; Core Product Capabilities; Chapter 3 domains |
| 1.0 | 2026-07-28 | — | Baseline release; domain relationships; Title Case; primary stakeholders |

---

## Purpose

This document defines the scope of the Sentinel AI product.

It describes the product boundaries, business capabilities, target organizations, functional domains, major modules, supported integrations, and release strategy.

Unlike the Vision document, which explains why Sentinel AI exists, this document defines what the product includes and how its capabilities are organized.

This document serves as the primary reference for product managers, architects, engineering teams, QA engineers, UX designers, and project stakeholders before detailed requirements and architecture are produced.

---

## Document Scope

This document defines the functional boundaries and business capabilities of Sentinel AI.

It specifies what the product includes, who it serves, the major functional domains, product boundaries, release strategy, and supported integrations.

It intentionally does not define detailed business requirements, functional requirements, user interface design, system architecture, database schema, API contracts, or implementation details. Those topics are documented separately.

---

## Relationship to Vision

The Product Vision defines the long-term purpose, strategic direction, and guiding principles of Sentinel AI.

The Product Scope translates that vision into a concrete product definition by identifying the capabilities, domains, users, and boundaries that make up the first versions of the platform.

Subsequent documents—including Business Requirements, Functional Requirements, Architecture, Database Design, and API Specifications—must remain consistent with the scope defined in this document.

---

# Chapter 1 — Product Overview

## Product Overview

Sentinel AI is a modular enterprise software platform that enables cryptocurrency exchanges to centralize fraud detection, investigation, compliance operations, security monitoring, and AI-assisted decision support within a single operational environment.

The platform is designed to augment—not replace—existing exchange infrastructure by integrating with operational systems and providing contextual intelligence across the entire investigation lifecycle.

The platform combines deterministic detection techniques with explainable artificial intelligence to help Risk, Compliance, Fraud, and Security Operations teams investigate suspicious activities more efficiently while maintaining full human oversight over enforcement decisions.

Sentinel AI is designed as a modular enterprise platform that allows organizations to adopt capabilities incrementally while maintaining compatibility with existing operational workflows.

---

## Business Context

Modern cryptocurrency exchanges operate within an environment characterized by high transaction volumes, rapidly evolving fraud techniques, increasing regulatory obligations, and strict operational security requirements.

Operational teams often rely on multiple disconnected systems for fraud detection, compliance monitoring, investigation management, security monitoring, and reporting.

This fragmentation increases investigation time, reduces operational efficiency, and makes it difficult to build a complete understanding of suspicious activities.

As exchanges continue to scale globally, operational teams require platforms that improve consistency, reduce investigation time, and support increasingly complex regulatory obligations.

Sentinel AI addresses these operational challenges by providing a unified intelligence platform that consolidates investigation workflows, contextual information, AI-generated recommendations, and operational dashboards into a single enterprise solution.

The platform is intended to complement—not replace—existing exchange infrastructure.

---

## Business Value Proposition

Sentinel AI delivers value by enabling organizations to investigate financial risks faster, improve decision quality, and reduce operational complexity through AI-assisted workflows.

The platform provides value in six primary areas.

### 1. Faster Investigations

Automatically aggregate evidence, transaction history, behavioral signals, and contextual intelligence into a unified investigation workspace.

---

### 2. Better Decision Support

Provide explainable AI recommendations supported by evidence, confidence scores, and reasoning summaries.

---

### 3. Improved Fraud Detection

Combine deterministic rules, behavioral analytics, graph intelligence, and AI reasoning to improve detection quality while reducing false positives.

---

### 4. Compliance Efficiency

Support regulatory compliance through automated validation, standardized workflows, audit logging, and AI-assisted compliance investigations.

---

### 5. Operational Visibility

Provide centralized dashboards that enable operational teams to monitor alerts, investigations, system health, AI performance, and compliance activities from a single platform.

---

### 6. Enterprise Scalability

Support incremental adoption through modular functional domains that can evolve independently without disrupting existing exchange systems.

---

## Target Organizations

Sentinel AI is designed primarily for medium-to-large cryptocurrency exchanges that require enterprise-grade operational intelligence, fraud prevention, compliance automation, and security monitoring.

### Market Segment

Sentinel AI is intended for organizations operating digital asset platforms where fraud prevention, compliance, and operational security are critical business functions.

### Primary Customers

- Centralized Cryptocurrency Exchanges (CEX)
- Digital Asset Trading Platforms
- Institutional Digital Asset Custodians
- Crypto Brokerage Platforms
- Financial Institutions offering Digital Asset Services

### Business Units

Within each customer organization, Sentinel AI primarily serves:

- Risk Operations
- Fraud Operations
- Compliance
- Security Operations (SOC)
- Financial Crime Investigation
- Platform Operations
- Executive Risk Management

### User Personas

The platform is designed for professional operational teams rather than retail cryptocurrency users.

Primary personas include:

- Risk Analysts
- Fraud Investigators
- Compliance Officers
- Security Operations Engineers
- Platform Administrators
- Risk Managers

---

## Product Ecosystem

Sentinel AI operates within the operational ecosystem of a cryptocurrency exchange.

The platform integrates with existing enterprise systems rather than replacing them.

Typical integrations include:

- Exchange Core Platform
- Authentication Services
- User Management
- KYC Providers
- Blockchain Analytics Providers
- SIEM Platforms
- Notification Services
- Ticketing Systems
- Audit & Logging Platforms

---

## Scope Philosophy

Sentinel AI focuses on providing intelligent operational capabilities rather than replacing existing business systems.

The product emphasizes augmentation, interoperability, explainability, and modular adoption over end-to-end platform replacement.

---

# Chapter 2 — Product Boundaries

## Product Capability Map

Sentinel AI is organized around the following business capability domains:

```text
Sentinel AI

├── Risk Intelligence
├── Fraud Investigation
├── Compliance Operations
├── Security Intelligence
├── Wallet Intelligence
├── AI Platform
├── Reporting & Analytics
├── Administration
└── Platform Operations
```

This capability map provides a product-level mental model. It is not a system architecture diagram. Detailed capability definitions appear below; release sequencing is defined in the Release Strategy chapter.

---

## Core Product Capabilities

The initial releases of Sentinel AI focus on providing a unified enterprise platform for AI-assisted risk intelligence within cryptocurrency exchanges.

The product includes capabilities that support the complete investigation lifecycle, from event ingestion and risk analysis to case management, compliance workflows, reporting, and operational monitoring.

The following functional domains define the core product capability model.

### Risk Intelligence

- Transaction Risk Assessment
- Behavioral Risk Analysis
- Rule-Based Risk Detection
- Risk Scoring
- Risk Explanation
- Alert Prioritization

---

### Fraud Investigation

- Investigation Workspace
- Case Management
- Evidence Aggregation
- Timeline Reconstruction
- Investigation Notes
- Investigation Summaries

---

### Compliance Operations

- KYC Investigation Support
- Travel Rule Validation
- Sanctions Screening Workflow
- Compliance Evidence Collection
- Audit Support

---

### Security Intelligence

- API Activity Monitoring
- Authentication Anomaly Detection
- Device Intelligence
- Operational Security Monitoring
- Account Compromise Investigation

---

### Wallet Intelligence

- Wallet Profiling
- Address Reputation
- Wallet Relationship Analysis
- Transaction History Visualization
- Cross-Wallet Investigation

---

### AI Platform

- AI Investigation Support
- Knowledge & Evidence Retrieval
- AI Report Generation
- AI Workflow Orchestration
- Prompt & Context Management
- AI Evaluation Framework

---

### Reporting & Analytics

- Operational Dashboards
- Risk Dashboards
- Investigation Dashboards
- Compliance Dashboards
- Executive Reporting

---

### Administration

- User Management
- Role Management
- Access Control
- Permission Management
- Organization Settings
- Platform Configuration
- Feature Flag Management
- System Settings

---

### Platform Operations

- Audit Logging
- Notification Management
- Health Monitoring
- Metrics Collection
- Alerting
- Operational Diagnostics

---

## Product Constraints

The following constraints define the operational boundaries of Sentinel AI.

### Human Decision Authority

Artificial intelligence provides recommendations but does not make final enforcement decisions.

Only authorized personnel may:

- Suspend accounts
- Freeze assets
- Reject transactions
- Close investigations
- Approve compliance decisions

---

### Integration First

Sentinel AI is designed to integrate with existing exchange infrastructure rather than replace core exchange systems.

---

### Explainability Requirement

Every AI-generated recommendation must provide sufficient evidence, reasoning, and supporting references to enable analyst review.

---

### Regulatory Alignment

The platform must support regulatory compliance requirements while remaining adaptable to evolving regional regulations.

---

### Modular Adoption

Organizations should be able to adopt individual functional domains without requiring deployment of the entire platform.

---

## Product Assumptions

The product assumes that the customer organization already operates core exchange infrastructure.

The following assumptions apply.

### Business Assumptions

- Organizations have established operational procedures for fraud investigation and regulatory compliance.

---

### Operational Assumptions

- Exchange transaction events are available.
- Authentication events can be consumed.
- User identity data exists.
- KYC information is maintained externally.
- Operational audit logs are available.

---

### Technical Assumptions

- APIs are available for system integration.
- Customer infrastructure supports secure communication.
- Required operational data can be accessed through approved integrations.
- AI services are available when enabled.

---

### Organizational Assumptions

- Dedicated Risk teams exist.
- Compliance personnel review investigations.
- Security teams manage operational incidents.
- Administrators manage system configuration.

---

## Product Risks

The following risks may influence successful adoption and operation of Sentinel AI.

| Risk | Description | Mitigation |
|------|-------------|------------|
| AI Hallucinations | AI may generate inaccurate recommendations. | Human review, evidence-backed responses, evaluation framework |
| AI Model Drift | AI performance may degrade as fraud patterns evolve. | Continuous evaluation, retraining strategies, prompt optimization, analyst feedback |
| False Positives | Legitimate activity may be incorrectly flagged. | Hybrid rule engine and behavioral analysis |
| False Negatives | Fraudulent activity may remain undetected. | Continuous rule updates and AI improvement |
| Regulatory Change | Compliance requirements evolve over time. | Configurable policies and modular compliance engine |
| Integration Complexity | Existing systems differ across organizations. | Standardized integration interfaces |
| Data Quality | Incomplete or inconsistent operational data. | Data validation and confidence scoring |
| Service Availability | External AI services may become unavailable. | Graceful degradation and deterministic fallback |
| Security Threats | Platform components may become attack targets. | Secure architecture, monitoring, and regular security reviews |

---

## Out of Scope

The following capabilities are intentionally excluded from the product.

- Cryptocurrency trading
- Order matching
- Wallet custody
- Blockchain node operation
- Asset management
- Portfolio management
- Investment recommendations
- Autonomous financial decisions
- Automated account enforcement
- Consumer-facing trading applications

These capabilities fall outside the intended purpose of Sentinel AI and are not considered part of the product roadmap.

---

# Chapter 3 — Functional Domains

## Functional Domain Overview

Sentinel AI is organized into a collection of functional domains that represent the platform's major business capabilities.

Each domain addresses a specific operational responsibility while collaborating with other domains to support the complete risk investigation lifecycle.

The functional domains are designed to remain logically independent, allowing organizations to adopt capabilities incrementally while maintaining a consistent operational experience.

These domains define **what the platform does** from a business perspective. They do not prescribe implementation details such as services, APIs, databases, or deployment architecture.

---

## Domain 1 — Risk Intelligence

**Primary Stakeholder:** Risk Operations

### Purpose

The Risk Intelligence domain identifies, evaluates, and prioritizes operational risk across transactions, accounts, wallets, and behavioral activities.

It serves as the primary decision-support capability for detecting potentially suspicious behavior requiring analyst review.

### Responsibilities

- Assess transaction risk
- Calculate contextual risk scores
- Prioritize operational alerts
- Aggregate behavioral indicators
- Explain risk assessments
- Support investigation initiation

### Core Capabilities

- Transaction Risk Assessment
- Behavioral Risk Analysis
- Rule-Based Detection
- Risk Scoring
- Risk Explanation
- Alert Prioritization

### Business Value

Risk Intelligence enables analysts to identify high-risk activity more quickly, improve detection accuracy, and reduce time spent investigating low-priority alerts.

---

## Domain 2 — Fraud Investigation

**Primary Stakeholder:** Fraud Operations

### Purpose

The Fraud Investigation domain provides analysts with a centralized environment for investigating suspicious activities and managing investigation cases.

It consolidates evidence, investigation history, and AI-assisted insights into a single operational workspace.

### Responsibilities

- Manage investigation cases
- Collect investigation evidence
- Maintain investigation history
- Support analyst collaboration
- Produce investigation summaries

### Core Capabilities

- Investigation Workspace
- Case Management
- Evidence Aggregation
- Investigation Timeline
- Investigation Notes
- Investigation Summaries

### Business Value

Fraud Investigation reduces investigation complexity by providing analysts with a structured workflow for collecting evidence, documenting findings, and reaching consistent conclusions.

---

## Domain 3 — Compliance Operations

**Primary Stakeholder:** Compliance Team

### Purpose

The Compliance Operations domain supports regulatory compliance activities by assisting analysts with verification, documentation, and audit-ready investigation processes.

### Responsibilities

- Support KYC investigations
- Validate Travel Rule requirements
- Assist sanctions screening
- Maintain compliance evidence
- Support regulatory reporting

### Core Capabilities

- KYC Investigation Support
- Travel Rule Validation
- Sanctions Screening Workflow
- Compliance Evidence Collection
- Audit Support

### Business Value

Compliance Operations improves regulatory readiness while reducing manual effort associated with compliance investigations and documentation.

---

## Domain 4 — Security Intelligence

**Primary Stakeholder:** Security Operations (SOC)

### Purpose

The Security Intelligence domain monitors operational security events that may indicate account compromise, unauthorized access, or abuse of exchange services.

### Responsibilities

- Monitor authentication activity
- Detect API abuse
- Analyze device behavior
- Identify operational anomalies
- Support incident investigation

### Core Capabilities

- API Activity Monitoring
- Authentication Anomaly Detection
- Device Intelligence
- Operational Security Monitoring
- Account Compromise Investigation

### Business Value

Security Intelligence strengthens exchange security by providing early visibility into suspicious operational behavior and potential security incidents.

---

## Domain 5 — Wallet Intelligence

**Primary Stakeholder:** Financial Crime Investigation

### Purpose

The Wallet Intelligence domain analyzes blockchain wallet activity, relationships, and historical behavior to support investigations involving digital asset movement.

### Responsibilities

- Analyze wallet reputation
- Investigate wallet relationships
- Review transaction history
- Identify suspicious wallet behavior
- Support blockchain investigations

### Core Capabilities

- Wallet Profiling
- Address Reputation
- Wallet Relationship Analysis
- Transaction History Visualization
- Cross-Wallet Investigation

### Business Value

Wallet Intelligence provides investigators with contextual blockchain insights that improve fraud detection and digital asset investigations.

---

## Domain 6 — AI Platform

**Primary Stakeholder:** AI Engineering

### Purpose

The AI Platform domain provides intelligent assistance that augments human analysts throughout the investigation lifecycle.

### Responsibilities

- Assist investigations
- Retrieve contextual knowledge
- Generate operational reports
- Explain AI recommendations
- Evaluate AI quality

### Core Capabilities

- AI Investigation Support
- Knowledge & Evidence Retrieval
- AI Report Generation
- AI Workflow Orchestration
- Prompt & Context Management
- AI Evaluation Framework

### Business Value

The AI Platform increases analyst productivity while maintaining explainability, transparency, and human oversight.

---

## Domain 7 — Reporting & Analytics

**Primary Stakeholder:** Risk Operations

### Purpose

The Reporting & Analytics domain provides operational visibility into investigations, compliance activities, AI performance, and platform operations.

### Responsibilities

- Present operational dashboards
- Generate executive reports
- Support KPI monitoring
- Visualize investigation trends

### Core Capabilities

- Operational Dashboards
- Executive Reporting
- KPI Monitoring
- Investigation Analytics
- Compliance Analytics

### Business Value

Reporting & Analytics enables data-driven operational decisions through centralized visibility and performance measurement.

---

## Domain 8 — Administration

**Primary Stakeholder:** Platform Operations

### Purpose

The Administration domain manages organizational configuration, user access, and platform governance.

### Responsibilities

- Manage users
- Manage organizations
- Configure permissions
- Maintain platform settings
- Govern platform access

### Core Capabilities

- User Management
- Organization Management
- Role & Permission Management
- Configuration Management
- Feature Flag Management
- System Settings

### Business Value

Administration ensures secure, scalable, and well-governed platform operations across multiple organizations.

---

## Domain 9 — Platform Operations

**Primary Stakeholder:** Platform Operations

### Purpose

The Platform Operations domain provides operational monitoring, diagnostics, and reliability capabilities for the Sentinel AI platform.

### Responsibilities

- Monitor platform health
- Collect operational metrics
- Generate alerts
- Maintain audit trails
- Support troubleshooting

### Core Capabilities

- Health Monitoring
- Metrics Collection
- Alerting
- Audit Logging
- Notification Management
- Operational Diagnostics

### Business Value

Platform Operations improves system reliability, operational awareness, and maintainability through comprehensive monitoring and diagnostics.

---

## Domain Relationships

Although each functional domain has a distinct business responsibility, they collaborate throughout the investigation lifecycle.

```text
Risk Intelligence
        ↓
Fraud Investigation
        ↓
Compliance Operations
        ↓
Reporting & Analytics
        ↓
Administration & Platform Operations

            ↑
      AI Platform supports every domain
```

Security Intelligence and Wallet Intelligence provide supporting context throughout investigation and compliance workflows. AI Platform capabilities assist analysts across domains while Administration and Platform Operations enable secure, reliable platform use.
