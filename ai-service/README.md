# Sentinel AI — AI Service (M8)

Python 3.11+ / FastAPI deployable `sentinel-ai` (ADR-019). **Assistive-only (ADR-002).**

Deterministic local generator and hashed embeddings (**simulation**). Not a hosted LLM. Not legal/regulatory certification.

## M8 scope

- `GET /health`
- API-AI-001–005 (investigation, risk explanation, retrieve, recommendations, prompts)
- Read-only tools from ToolDefinitions.md
- Migration `V010` `ai` schema
- Events `AIRecommendationGenerated` / `PromptUpdated` via in-process outbox **simulation**
- Consume `CaseUpdated` / `RiskCalculated` / `EvidenceAttached` / `AlertCreated` for context only

Not in M8: API-AI-006/007, COMP approval, Kafka, LangGraph, vendor SDKs.

## Setup

```bash
cd ai-service
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
pytest
ruff check app tests
uvicorn app.main:app --port 8090
```

Shared HMAC: `IDENTITY_TOKEN_HMAC_KEY` (same default as identity/ops).
