from app.main import app
from tests.helpers import client_ctx


def test_health_up() -> None:
    with client_ctx() as client:
        response = client.get("/health")
        assert response.status_code == 200
        body = response.json()
        assert body["status"] == "UP"
        assert body["service"] == "sentinel-ai"
        assert body["mode"] == "assistive-only"
        assert body["milestone"] == "M8"


def test_mvp_assist_routes_exist_and_v2_compliance_does_not() -> None:
    paths = {route.path for route in app.routes}
    assert "/v1/ai/assist/investigation" in paths
    assert "/v1/ai/assist/risk-explanation" in paths
    assert "/v1/ai/assist/retrieve" in paths
    assert "/v1/ai/recommendations/{recommendation_id}" in paths
    assert "/v1/ai/prompts" in paths
    forbidden = {
        "/v1/ai/assist/compliance",
        "/v1/ai/evaluations",
        "/v1/alerts",
        "/v1/investigations/cases",
        "/v1/compliance/kyc-reviews",
    }
    assert forbidden.isdisjoint(paths)
