# M0 Developer Setup

## Document Information

| Field | Value |
|-------|-------|
| Milestone | M0 — Foundations |
| Authority | Phase12M0Kickoff.md, ADR-019 |

---

## Prerequisites

| Tool | Version |
|------|---------|
| JDK | 21 (toolchain) |
| Python | 3.11+ |
| Node.js | 20+ |
| PyYAML | for `contracts/validate.py` |

Do **not** start M1 CORE until M0 exit criteria pass.

## Commands

```bash
# Contracts
python3 contracts/validate.py

# Backend
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
cd backend && ./gradlew test && cd ..

# AI service
cd ai-service
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
pytest && ruff check app tests
cd ..

# Frontend
cd web && npm install && npm test && npm run lint && npm run build && cd ..
```

Or: `./scripts/m0-validate.sh`

## Layout

See repository `README.md` and `docs/08-development/ImplementationArchitecture.md`.
