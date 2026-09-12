from __future__ import annotations

from uuid import UUID, uuid4

from fastapi.testclient import TestClient

from app.auth import AccessTokenCodec
from app.main import app

ORG = UUID("11111111-1111-4111-8111-111111111111")
ORG_B = UUID("22222222-2222-4222-8222-222222222222")
USER = UUID("33333333-3333-4333-8333-333333333333")

ALL_AI = {
    "ai:investigation:assist",
    "ai:risk:assist",
    "ai:retrieve:execute",
    "ai:recommendation:read",
    "ai:prompt:write",
    "invest:case:read",
    "alert:alert:read",
    "risk:assessment:read",
}


def token(org: UUID = ORG, perms: set[str] | None = None) -> str:
    codec = AccessTokenCodec()
    return codec.issue(USER, org, uuid4(), perms if perms is not None else set(ALL_AI))


def auth_headers(org: UUID = ORG, perms: set[str] | None = None) -> dict[str, str]:
    return {
        "Authorization": f"Bearer {token(org, perms)}",
        "X-Organization-Id": str(org),
        "X-Correlation-Id": str(uuid4()),
    }


def client_ctx() -> TestClient:
    return TestClient(app)
