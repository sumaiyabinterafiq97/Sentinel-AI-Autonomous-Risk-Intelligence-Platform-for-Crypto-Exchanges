"""Sentinel AI service — M8 assistive Investigation / Risk / Retrieval agents.

Does not own RISK, ALERT, INVEST, COMP, AUTH, or USER/ORG lifecycle.
"""

from __future__ import annotations

from contextlib import asynccontextmanager
from datetime import UTC, datetime
from typing import Any
from uuid import UUID, uuid4

from fastapi import FastAPI, Header, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from pydantic import BaseModel

from app.auth import AccessTokenCodec
from app.consumers import AiUpstreamConsumer
from app.errors import AiError, error_body, forbidden, unauthenticated, validation
from app.events import AiEventPublisher
from app.service import AiService
from app.store import AiStore
from app.tools import ToolRegistry

PERMISSIONS = {
    "assistInvestigation": "ai:investigation:assist",
    "assistRiskExplanation": "ai:risk:assist",
    "assistRetrieve": "ai:retrieve:execute",
    "getRecommendation": "ai:recommendation:read",
    "listPrompts": "ai:prompt:write",
    "createPrompt": "ai:prompt:write",
    "patchPrompt": "ai:prompt:write",
}


class AIAssistInvestigationRequest(BaseModel):
    caseId: UUID
    query: str | None = None


class AIAssistRiskExplanationRequest(BaseModel):
    assessmentId: UUID


class AIAssistRetrieveRequest(BaseModel):
    query: str
    caseId: UUID | None = None


class PromptCreateRequest(BaseModel):
    name: str
    template: str


class PromptPatchRequest(BaseModel):
    name: str | None = None
    template: str | None = None


def _meta(request_id: UUID, correlation_id: UUID | None, extra: dict[str, Any] | None = None) -> dict[str, Any]:
    meta: dict[str, Any] = {
        "requestId": str(request_id),
        "correlationId": None if correlation_id is None else str(correlation_id),
        "timestamp": datetime.now(UTC).strftime("%Y-%m-%dT%H:%M:%SZ"),
    }
    if extra:
        meta["pagination"] = extra
    return meta


@asynccontextmanager
async def lifespan(app: FastAPI):
    store = AiStore()
    tools = ToolRegistry(store)
    publisher = AiEventPublisher(store)
    app.state.store = store
    app.state.tools = tools
    app.state.publisher = publisher
    app.state.service = AiService(store, tools, publisher)
    app.state.consumer = AiUpstreamConsumer(store, tools)
    app.state.tokens = AccessTokenCodec()
    yield


app = FastAPI(
    title="Sentinel AI Service",
    version="0.8.0-m8",
    description="Assistive-only AI Platform (ADR-002). M8 Investigation/Risk/Retrieval agents.",
    lifespan=lifespan,
)


@app.exception_handler(AiError)
async def ai_error_handler(request: Request, exc: AiError) -> JSONResponse:
    request_id = getattr(request.state, "request_id", uuid4())
    correlation_id = getattr(request.state, "correlation_id", None)
    return JSONResponse(status_code=exc.status, content=error_body(exc, request_id, correlation_id))


@app.exception_handler(RequestValidationError)
async def request_validation_handler(request: Request, exc: RequestValidationError) -> JSONResponse:
    request_id = getattr(request.state, "request_id", uuid4())
    correlation_id = getattr(request.state, "correlation_id", None)
    loc = ".".join(str(part) for part in (exc.errors()[0].get("loc") or ("body",)))
    return JSONResponse(
        status_code=400,
        content=error_body(validation(loc, "Invalid request"), request_id, correlation_id),
    )


@app.middleware("http")
async def context_middleware(request: Request, call_next):
    request.state.request_id = uuid4()
    raw_corr = request.headers.get("X-Correlation-Id")
    try:
        request.state.correlation_id = UUID(raw_corr) if raw_corr else None
    except ValueError:
        request.state.correlation_id = None
    return await call_next(request)


def _principal(
    request: Request,
    authorization: str | None,
    organization_id: str | None,
    permission: str,
):
    if not organization_id:
        raise validation("X-Organization-Id", "X-Organization-Id is required")
    try:
        org = UUID(organization_id)
    except ValueError:
        raise validation("X-Organization-Id", "X-Organization-Id must be a UUID") from None
    if not authorization or not authorization.lower().startswith("bearer "):
        raise unauthenticated()
    principal = request.app.state.tokens.verify(authorization[7:].strip())
    if principal is None:
        raise unauthenticated()
    if principal.organization_id != org:
        raise forbidden()
    if not principal.has(permission):
        raise forbidden()
    request.state.organization_id = org
    request.state.principal = principal
    return principal, org


@app.get("/health")
def health() -> dict[str, str]:
    return {
        "status": "UP",
        "service": "sentinel-ai",
        "milestone": "M8",
        "mode": "assistive-only",
    }


@app.post("/v1/ai/assist/investigation", status_code=202)
def assist_investigation(
    body: AIAssistInvestigationRequest,
    request: Request,
    authorization: str | None = Header(default=None),
    x_organization_id: str | None = Header(default=None, alias="X-Organization-Id"),
    idempotency_key: str | None = Header(default=None, alias="Idempotency-Key"),
):
    principal, org = _principal(request, authorization, x_organization_id, PERMISSIONS["assistInvestigation"])
    data = request.app.state.service.assist_investigation(
        principal=principal,
        organization_id=org,
        correlation_id=request.state.correlation_id,
        case_id=str(body.caseId),
        query=body.query,
        idempotency_key=idempotency_key,
    )
    return {"data": data, "meta": _meta(request.state.request_id, request.state.correlation_id)}


@app.post("/v1/ai/assist/risk-explanation", status_code=202)
def assist_risk_explanation(
    body: AIAssistRiskExplanationRequest,
    request: Request,
    authorization: str | None = Header(default=None),
    x_organization_id: str | None = Header(default=None, alias="X-Organization-Id"),
    idempotency_key: str | None = Header(default=None, alias="Idempotency-Key"),
):
    principal, org = _principal(request, authorization, x_organization_id, PERMISSIONS["assistRiskExplanation"])
    data = request.app.state.service.assist_risk(
        principal=principal,
        organization_id=org,
        correlation_id=request.state.correlation_id,
        assessment_id=str(body.assessmentId),
        idempotency_key=idempotency_key,
    )
    return {"data": data, "meta": _meta(request.state.request_id, request.state.correlation_id)}


@app.post("/v1/ai/assist/retrieve", status_code=201)
def assist_retrieve(
    body: AIAssistRetrieveRequest,
    request: Request,
    authorization: str | None = Header(default=None),
    x_organization_id: str | None = Header(default=None, alias="X-Organization-Id"),
    idempotency_key: str | None = Header(default=None, alias="Idempotency-Key"),
):
    principal, org = _principal(request, authorization, x_organization_id, PERMISSIONS["assistRetrieve"])
    data = request.app.state.service.assist_retrieve(
        principal=principal,
        organization_id=org,
        correlation_id=request.state.correlation_id,
        query=body.query,
        case_id=None if body.caseId is None else str(body.caseId),
        idempotency_key=idempotency_key,
    )
    return {"data": data, "meta": _meta(request.state.request_id, request.state.correlation_id)}


@app.get("/v1/ai/recommendations/{recommendation_id}")
def get_recommendation(
    recommendation_id: UUID,
    request: Request,
    authorization: str | None = Header(default=None),
    x_organization_id: str | None = Header(default=None, alias="X-Organization-Id"),
):
    _, org = _principal(request, authorization, x_organization_id, PERMISSIONS["getRecommendation"])
    rec = request.app.state.service.get_recommendation(org, recommendation_id)
    return {
        "data": {"id": str(rec.id), "content": rec.content.get("text", ""), "status": rec.status},
        "meta": _meta(request.state.request_id, request.state.correlation_id),
    }


@app.get("/v1/ai/prompts")
def list_prompts(
    request: Request,
    authorization: str | None = Header(default=None),
    x_organization_id: str | None = Header(default=None, alias="X-Organization-Id"),
    cursor: str | None = None,
    limit: int = 50,
):
    _principal(request, authorization, x_organization_id, PERMISSIONS["listPrompts"])
    data, has_more = request.app.state.service.list_prompts(
        request.state.organization_id, cursor, max(1, min(limit, 100))
    )
    return {
        "data": data,
        "meta": _meta(
            request.state.request_id,
            request.state.correlation_id,
            {"cursor": data[-1]["id"] if data else None, "hasMore": has_more, "limit": limit},
        ),
    }


@app.post("/v1/ai/prompts", status_code=201)
def create_prompt(
    body: PromptCreateRequest,
    request: Request,
    authorization: str | None = Header(default=None),
    x_organization_id: str | None = Header(default=None, alias="X-Organization-Id"),
):
    principal, org = _principal(request, authorization, x_organization_id, PERMISSIONS["createPrompt"])
    data = request.app.state.service.create_prompt(
        principal=principal,
        organization_id=org,
        correlation_id=request.state.correlation_id,
        name=body.name,
        template=body.template,
    )
    return {"data": data, "meta": _meta(request.state.request_id, request.state.correlation_id)}


@app.patch("/v1/ai/prompts")
def patch_prompt(
    body: PromptPatchRequest,
    request: Request,
    authorization: str | None = Header(default=None),
    x_organization_id: str | None = Header(default=None, alias="X-Organization-Id"),
):
    principal, org = _principal(request, authorization, x_organization_id, PERMISSIONS["patchPrompt"])
    data = request.app.state.service.patch_prompt(
        principal=principal,
        organization_id=org,
        name=body.name,
        template=body.template,
    )
    return {"data": data, "meta": _meta(request.state.request_id, request.state.correlation_id)}
