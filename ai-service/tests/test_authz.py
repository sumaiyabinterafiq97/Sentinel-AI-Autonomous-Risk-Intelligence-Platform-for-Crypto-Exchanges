from uuid import uuid4

from tests.helpers import ORG, ORG_B, auth_headers, client_ctx


def test_contract_surface_operation_ids() -> None:
    from app.main import app

    ops = {getattr(route, "name", None) for route in app.routes}
    assert "assist_investigation" in ops
    assert "assist_risk_explanation" in ops
    assert "assist_retrieve" in ops
    assert "get_recommendation" in ops
    assert "list_prompts" in ops
    assert "create_prompt" in ops
    assert "patch_prompt" in ops


def test_unauthenticated_is_401() -> None:
    with client_ctx() as client:
        response = client.post(
            "/v1/ai/assist/investigation",
            headers={"X-Organization-Id": str(ORG)},
            json={"caseId": str(uuid4())},
        )
        assert response.status_code == 401
        assert response.json()["error"]["code"] == "AUTH_AUTHENTICATION_001"


def test_missing_permission_is_403() -> None:
    with client_ctx() as client:
        response = client.post(
            "/v1/ai/assist/investigation",
            headers=auth_headers(perms={"ai:risk:assist"}),
            json={"caseId": str(uuid4())},
        )
        assert response.status_code == 403


def test_org_header_mismatch_is_403() -> None:
    with client_ctx() as client:
        headers = auth_headers(ORG)
        headers["X-Organization-Id"] = str(ORG_B)
        response = client.post(
            "/v1/ai/assist/investigation",
            headers=headers,
            json={"caseId": str(uuid4())},
        )
        assert response.status_code == 403


def test_missing_org_header_is_400() -> None:
    with client_ctx() as client:
        headers = auth_headers()
        del headers["X-Organization-Id"]
        response = client.post(
            "/v1/ai/assist/investigation",
            headers=headers,
            json={"caseId": str(uuid4())},
        )
        assert response.status_code == 400


def test_invalid_body_is_400() -> None:
    with client_ctx() as client:
        response = client.post(
            "/v1/ai/assist/investigation",
            headers=auth_headers(),
            json={},
        )
        assert response.status_code == 400
        assert response.json()["error"]["code"] == "AI_VALIDATION_001"
