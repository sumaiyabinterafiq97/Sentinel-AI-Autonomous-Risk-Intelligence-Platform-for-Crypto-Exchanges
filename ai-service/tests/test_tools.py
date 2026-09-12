from uuid import uuid4

from app.tools import FORBIDDEN_TOOLS, TOOL_GET_CASE, ToolDenied, ToolRegistry
from tests.helpers import ALL_AI, ORG, USER, auth_headers, client_ctx, token


def test_forbidden_tools_cannot_be_invoked() -> None:
    with client_ctx() as client:
        from app.auth import AccessPrincipal

        principal = AccessPrincipal(USER, ORG, uuid4(), frozenset(ALL_AI), 9999999999)
        registry: ToolRegistry = client.app.state.tools
        log: list[dict] = []
        for tool_id in FORBIDDEN_TOOLS:
            try:
                registry.invoke(
                    agent_type="investigation",
                    tool_id=tool_id,
                    principal=principal,
                    organization_id=ORG,
                    arguments={},
                    run_log=log,
                )
                raise AssertionError(f"{tool_id} should be denied")
            except ToolDenied:
                pass
        assert any(entry["outcome"] == "denied" for entry in log)


def test_get_case_requires_invest_read() -> None:
    with client_ctx() as client:
        from app.auth import AccessPrincipal

        principal = AccessPrincipal(
            USER, ORG, uuid4(), frozenset({"ai:investigation:assist"}), 9999999999
        )
        log: list[dict] = []
        try:
            client.app.state.tools.invoke(
                agent_type="investigation",
                tool_id=TOOL_GET_CASE,
                principal=principal,
                organization_id=ORG,
                arguments={"caseId": str(uuid4())},
                run_log=log,
            )
            raise AssertionError("expected deny")
        except ToolDenied:
            assert log[-1]["reason"] == "authz"


def test_risk_agent_cannot_use_case_tool() -> None:
    with client_ctx() as client:
        from app.auth import AccessPrincipal

        principal = AccessPrincipal(USER, ORG, uuid4(), frozenset(ALL_AI), 9999999999)
        log: list[dict] = []
        try:
            client.app.state.tools.invoke(
                agent_type="risk",
                tool_id=TOOL_GET_CASE,
                principal=principal,
                organization_id=ORG,
                arguments={"caseId": str(uuid4())},
                run_log=log,
            )
            raise AssertionError("expected deny")
        except ToolDenied:
            assert log[-1]["reason"] == "not-allowlisted"


def test_prompt_crud_and_event() -> None:
    with client_ctx() as client:
        created = client.post(
            "/v1/ai/prompts",
            headers=auth_headers(),
            json={"name": "investigation-default", "template": "Summarize with citations."},
        )
        assert created.status_code == 201
        listed = client.get("/v1/ai/prompts", headers=auth_headers())
        assert listed.status_code == 200
        assert listed.json()["data"][0]["name"] == "investigation-default"
        patched = client.patch(
            "/v1/ai/prompts",
            headers=auth_headers(),
            json={"name": "investigation-default", "template": "Version 2 template"},
        )
        assert patched.status_code == 200
        assert patched.json()["data"]["template"] == "Version 2 template"
        types = {e["eventType"] for e in client.app.state.store.durable_log}
        assert "PromptUpdated" in types


def test_token_helper_roundtrip() -> None:
    raw = token()
    assert raw.count(".") == 2
