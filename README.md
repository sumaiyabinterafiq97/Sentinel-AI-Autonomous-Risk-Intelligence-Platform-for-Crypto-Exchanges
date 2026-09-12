# Sentinel AI

## Purpose

Repository overview and entry point for the Sentinel AI project documentation and engineering standards.

## Status

Draft

## Owner

To Be Assigned

## Last Updated

2026-09-11

## Revision History

| Version | Date | Author | Notes |
|---------|------|--------|-------|
| 0.1 | 2026-07-25 | — | Initial repository skeleton |

## Table of Contents

1. [Overview](#overview)
2. [Project Vision](#project-vision)
3. [Documentation](#documentation)
4. [Repository Structure](#repository-structure)
5. [Getting Started](#getting-started)
6. [Contributing](#contributing)
7. [Security](#security)
8. [License](#license)
9. [Changelog](#changelog)

---

## Overview

Sentinel AI is an autonomous **risk intelligence** platform for crypto exchanges. Product requirements and architecture live under `docs/`. Phase 12 **M0** added implementation scaffolding (buildable skeletons only).

Application Development Gate: **SATISFIED**. Current coding milestone: **M0 foundations**. Do not implement M1+ domain features until M0 exit criteria are met.

## Project Vision

### Tagline

Autonomous Risk Intelligence Platform for Crypto Exchanges

### Vision Statement

See [`docs/01-product/Vision.md`](docs/01-product/Vision.md).

## Documentation

Documentation lives under [`docs/`](docs/README.md). Implementation plans: [`docs/08-development/`](docs/08-development/). M0 setup: [`docs/08-development/M0DeveloperSetup.md`](docs/08-development/M0DeveloperSetup.md).

## Repository Structure

```text
backend/       Java 21 / Spring Boot Gradle multi-project (platform, identity, ops, dash)
ai-service/    Python FastAPI AI Platform (assistive-only; health in M0)
web/           React + TypeScript + Vite SPA (placeholder shell in M0)
contracts/     Packaged OpenAPI + event JSON Schemas (source: docs/06-api)
docs/          Authoritative product, architecture, and contract documentation
scripts/       Validation helpers (m0-validate.sh)
```

## Getting Started

See [`docs/08-development/M0DeveloperSetup.md`](docs/08-development/M0DeveloperSetup.md) or run `./scripts/m0-validate.sh`.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Security

See [SECURITY.md](SECURITY.md).

## License

See [LICENSE](LICENSE).

## Changelog

See [CHANGELOG.md](CHANGELOG.md).
