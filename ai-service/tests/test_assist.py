from uuid import uuid4

from tests.helpers import ORG, ORG_B, auth_headers, client_ctx


def _seed_case(client, org, case_id: str, **extra) -> None:
    payload = {"caseId": case_id, "status": "open", **extra}
    client.app.state.consumer.consume(
        {
            "eventId": str(uuid4()),
            "eventType": "CaseUpdated",
            "organizationId": str(org),
            "payload": payload,
        }
    )


def test_investigation_partial_without_context() -> None:
    with client_ctx() as client:
        case_id = str(uuid4())
        created = client.post(
            "/v1/ai/assist/investigation",
            headers=auth_headers(),
            json={"caseId": case_id},
        )
        assert created.status_code == 202
        rec_id = created.json()["data"]["recommendationId"]
        fetched = client.get(f"/v1/ai/recommendations/{rec_id}", headers=auth_headers())
        assert fetched.status_code == 200
        content = fetched.json()["data"]["content"]
        assert "SYSTEM:" in content
        assert "AI:" in content
        assert "EVIDENCE:" in content
        assert "HUMAN:" in content
        assert fetched.json()["data"]["status"] == "partial"
        assert case_id not in content or "No authorized case context" in content or "incomplete" in content


def test_investigation_grounded_when_case_context_exists() -> None:
    with client_ctx() as client:
        case_id = str(uuid4())
        _seed_case(client, ORG, case_id, status="open")
        created = client.post(
            "/v1/ai/assist/investigation",
            headers=auth_headers(),
            json={"caseId": case_id, "query": "summarize"},
        )
        assert created.status_code == 202
        rec_id = created.json()["data"]["recommendationId"]
        content = client.get(f"/v1/ai/recommendations/{rec_id}", headers=auth_headers()).json()["data"]["content"]
        assert f"caseId={case_id}" in content
        assert "cannot close" in content.lower() or "AI cannot close" in content


def test_cross_tenant_recommendation_is_404() -> None:
    with client_ctx() as client:
        created = client.post(
            "/v1/ai/assist/investigation",
            headers=auth_headers(ORG),
            json={"caseId": str(uuid4())},
        )
        rec_id = created.json()["data"]["recommendationId"]
        other = client.get(
            f"/v1/ai/recommendations/{rec_id}",
            headers=auth_headers(ORG_B, perms={"ai:recommendation:read", "ai:investigation:assist"}),
        )
        assert other.status_code == 404


def test_risk_explanation_does_not_invent_score() -> None:
    with client_ctx() as client:
        assessment_id = str(uuid4())
        created = client.post(
            "/v1/ai/assist/risk-explanation",
            headers=auth_headers(),
            json={"assessmentId": assessment_id},
        )
        assert created.status_code == 202
        rec_id = created.json()["data"]["recommendationId"]
        content = client.get(f"/v1/ai/recommendations/{rec_id}", headers=auth_headers()).json()["data"]["content"]
        assert "No score was invented" in content or "cannot be grounded" in content


def test_risk_explanation_uses_system_score() -> None:
    with client_ctx() as client:
        assessment_id = str(uuid4())
        client.app.state.consumer.consume(
            {
                "eventId": str(uuid4()),
                "eventType": "RiskCalculated",
                "organizationId": str(ORG),
                "payload": {"assessmentId": assessment_id, "score": 72, "ruleHits": ["R1"]},
            }
        )
        created = client.post(
            "/v1/ai/assist/risk-explanation",
            headers=auth_headers(),
            json={"assessmentId": assessment_id},
        )
        rec_id = created.json()["data"]["recommendationId"]
        content = client.get(f"/v1/ai/recommendations/{rec_id}", headers=auth_headers()).json()["data"]["content"]
        assert "score=72" in content
        assert "does not replace deterministic RISK scoring" in content


def test_retrieve_does_not_cross_tenant() -> None:
    with client_ctx() as client:
        client.app.state.tools.index_document(
            organization_id=ORG,
            source_type="policy",
            source_id="doc-a",
            text="travel rule wallet screening policy excerpt",
        )
        client.app.state.tools.index_document(
            organization_id=ORG_B,
            source_type="policy",
            source_id="doc-b",
            text="secret other tenant sanctions list",
        )
        created = client.post(
            "/v1/ai/assist/retrieve",
            headers=auth_headers(ORG),
            json={"query": "sanctions list"},
        )
        assert created.status_code == 201
        rec_id = created.json()["data"]["recommendationId"]
        content = client.get(f"/v1/ai/recommendations/{rec_id}", headers=auth_headers(ORG)).json()["data"]["content"]
        assert "doc-b" not in content
        assert "secret other tenant" not in content
