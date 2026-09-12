from uuid import uuid4

from tests.helpers import ORG, auth_headers, client_ctx


def test_duplicate_upstream_events_are_idempotent() -> None:
    with client_ctx() as client:
        event_id = str(uuid4())
        envelope = {
            "eventId": event_id,
            "eventType": "AlertCreated",
            "organizationId": str(ORG),
            "payload": {"alertId": str(uuid4()), "priority": "high"},
        }
        first = client.app.state.consumer.consume(envelope)
        second = client.app.state.consumer.consume(envelope)
        assert first == "processed"
        assert second == "duplicate"


def test_evidence_attached_indexes_retrieval() -> None:
    with client_ctx() as client:
        case_id = str(uuid4())
        client.app.state.consumer.consume(
            {
                "eventId": str(uuid4()),
                "eventType": "EvidenceAttached",
                "organizationId": str(ORG),
                "payload": {
                    "caseId": case_id,
                    "evidenceId": str(uuid4()),
                    "evidenceRef": "wallet clustering memo for case",
                },
            }
        )
        created = client.post(
            "/v1/ai/assist/retrieve",
            headers=auth_headers(),
            json={"query": "wallet clustering", "caseId": case_id},
        )
        assert created.status_code == 201
        rec_id = created.json()["data"]["recommendationId"]
        rec = client.get(f"/v1/ai/recommendations/{rec_id}", headers=auth_headers()).json()["data"]
        assert rec["status"] in {"completed", "partial"}
        if rec["status"] == "completed":
            assert "case_evidence" in rec["content"]


def test_ai_publishes_only_approved_events() -> None:
    with client_ctx() as client:
        client.post(
            "/v1/ai/assist/investigation",
            headers=auth_headers(),
            json={"caseId": str(uuid4())},
        )
        types = {e["eventType"] for e in client.app.state.store.durable_log}
        assert "AIRecommendationGenerated" in types
        forbidden = {
            "RiskCalculated",
            "AlertCreated",
            "CaseClosed",
            "ComplianceReviewed",
        }
        assert types.isdisjoint(forbidden)
        payload = next(
            e["payload"] for e in client.app.state.store.durable_log if e["eventType"] == "AIRecommendationGenerated"
        )
        assert payload["status"] in {"completed", "partial", "failed"}
        assert "recommendationId" in payload


def test_outbox_relay_marks_delivered() -> None:
    with client_ctx() as client:
        client.post(
            "/v1/ai/assist/risk-explanation",
            headers=auth_headers(),
            json={"assessmentId": str(uuid4())},
        )
        assert all(item.status == "delivered" for item in client.app.state.store.outbox)
        assert all(item.stream.startswith("sentinel.ai.") for item in client.app.state.store.outbox)
