import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { saveSession } from "../shared/session";
import { App } from "./App";

function tokenWith(perms: string[]) {
  const payload = btoa(JSON.stringify({ perms, sub: "11111111-1111-4111-8111-111111111111", org: "11111111-1111-4111-8111-111111111111" }));
  saveSession(`hdr.${payload}.sig`, "11111111-1111-4111-8111-111111111111");
}

function json(data: unknown, status = 200) {
  return { status, json: async () => ({ data }) };
}

describe("M11 frontend workflows", () => {
  beforeEach(() => {
    sessionStorage.clear();
    vi.stubGlobal(
      "fetch",
      vi.fn(async (input: RequestInfo, init?: RequestInit) => {
        const url = String(input);
        const method = (init?.method || "GET").toUpperCase();
        if (url.includes("/v1/auth/login") && method === "POST") {
          return json({
            accessToken: `hdr.${btoa(JSON.stringify({ perms: ["dash:workspace:read"], org: "11111111-1111-4111-8111-111111111111" }))}.sig`,
            organizationId: "11111111-1111-4111-8111-111111111111",
          });
        }
        if (url.includes("/v1/auth/mfa/verify")) {
          return json({ verified: true });
        }
        if (url.includes("/v1/auth/session")) {
          return json({ userId: "11111111-1111-4111-8111-111111111111", organizationId: "11111111-1111-4111-8111-111111111111" });
        }
        if (url.includes("/v1/workspace/dashboard")) {
          return json({ alertCount: 1, caseCount: 0, assessmentCount: 0 });
        }
        if (url.includes("/v1/workspace/widgets")) {
          return json([]);
        }
        if (url.includes("/v1/workspace") && !url.includes("subscriptions")) {
          return json({ organizationId: "11111111-1111-4111-8111-111111111111" });
        }
        if (url.includes("/v1/alerts/") && url.includes("/close") && method === "POST") {
          return json({ id: "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa", status: "closed", priority: 1 });
        }
        if (url.includes("/v1/alerts/") && url.includes("/assign") && method === "POST") {
          return json({ id: "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa", status: "assigned", priority: 1 });
        }
        if (url.endsWith("/v1/alerts") || url.includes("/v1/alerts?")) {
          return json([{ id: "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa", title: "High risk transfer", status: "open", priority: 1 }]);
        }
        if (url.includes("/v1/alerts/")) {
          return json({
            id: "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa",
            title: "High risk transfer",
            status: "open",
            priority: 1,
            riskAssessmentId: "bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb",
          });
        }
        if (url.includes("/v1/risk/assessments/")) {
          return json({ id: "bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb", score: 82, riskLevel: "high", explanationSummary: "rule hits" });
        }
        if (url.includes("/v1/investigations/cases/") && url.includes("/close")) {
          return json({ id: "cccccccc-cccc-4ccc-8ccc-cccccccccccc", status: "closed", title: "Case" });
        }
        if (url.includes("/v1/investigations/cases/") && url.includes("/assign")) {
          return json({ id: "cccccccc-cccc-4ccc-8ccc-cccccccccccc", status: "open", title: "Case" });
        }
        if (url.includes("/v1/investigations/cases/") && url.includes("/evidence")) {
          return json({ id: "ev-1" });
        }
        if (url.includes("/v1/investigations/cases/") && url.includes("/notes")) {
          return method === "POST" ? json({ id: "n1", content: "note" }, 201) : json([]);
        }
        if (url.includes("/v1/investigations/cases/") && url.includes("/timeline")) {
          return json([{ id: "t1", type: "CASE_CREATED" }]);
        }
        if (url.includes("/v1/investigations/cases/") && method === "GET") {
          return json({ id: "cccccccc-cccc-4ccc-8ccc-cccccccccccc", status: "open", title: "Case" });
        }
        if (url.includes("/v1/investigations/cases") && method === "POST") {
          return json({ id: "cccccccc-cccc-4ccc-8ccc-cccccccccccc", status: "open", title: "Escalation" }, 201);
        }
        if (url.includes("/v1/compliance/kyc-reviews/") && method === "PATCH") {
          return json({ id: "dddddddd-dddd-4ddd-8ddd-dddddddddddd", status: "approved" });
        }
        if (url.includes("/v1/compliance/kyc-reviews") && method === "POST") {
          return json({ id: "dddddddd-dddd-4ddd-8ddd-dddddddddddd", status: "in_review" }, 201);
        }
        if (url.includes("/v1/ai/assist/")) {
          return json({ jobId: "job-1", recommendationId: "rec-1", status: "completed" }, 202);
        }
        if (url.includes("/v1/ai/recommendations/")) {
          return json({ id: "rec-1", content: "Assistive only.", status: "completed" });
        }
        if (url.includes("/v1/workspace/queues/")) {
          return json([]);
        }
        return { status: 404, json: async () => ({}) };
      }),
    );
  });

  it("shows login and no V2 nav", () => {
    render(
      <MemoryRouter initialEntries={["/login"]}>
        <App />
      </MemoryRouter>,
    );
    expect(screen.getByRole("heading", { name: "Sentinel AI" })).toBeInTheDocument();
    expect(screen.getByLabelText("Email")).toBeInTheDocument();
    expect(screen.queryByText(/Security Operations/i)).not.toBeInTheDocument();
    expect(screen.queryByText(/Wallet Intelligence/i)).not.toBeInTheDocument();
  });

  it("SCR-00 login continues to MFA then workspace", async () => {
    render(
      <MemoryRouter initialEntries={["/login"]}>
        <App />
      </MemoryRouter>,
    );
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "analyst@example.com" } });
    fireEvent.change(screen.getByLabelText("Password"), { target: { value: "password1" } });
    fireEvent.click(screen.getByRole("button", { name: "Continue" }));
    expect(await screen.findByRole("heading", { name: "Verify MFA" })).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText("MFA code"), { target: { value: "123456" } });
    fireEvent.click(screen.getByRole("button", { name: "Verify" }));
    expect(await screen.findByRole("heading", { name: "Operations overview" })).toBeInTheDocument();
  });

  it("renders overview after session via workspace alias", async () => {
    tokenWith(["dash:workspace:read", "dash:queue:read"]);
    render(
      <MemoryRouter initialEntries={["/"]}>
        <App />
      </MemoryRouter>,
    );
    expect(await screen.findByRole("heading", { name: "Operations overview" })).toBeInTheDocument();
    expect(screen.getByRole("navigation", { name: "Primary" })).toBeInTheDocument();
    expect(screen.getByRole("banner")).toBeInTheDocument();
  });

  it("WF-1 alert triage assign and confirmed close", async () => {
    tokenWith(["dash:workspace:read", "dash:queue:read", "alert:alert:read", "alert:alert:assign", "alert:alert:close"]);
    render(
      <MemoryRouter initialEntries={["/alerts/aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa"]}>
        <App />
      </MemoryRouter>,
    );
    expect((await screen.findAllByText("SYSTEM RESULT")).length).toBeGreaterThan(0);
    expect(screen.getAllByText("AI INTERPRETATION").length).toBeGreaterThan(0);
    expect(screen.getAllByText("HUMAN DECISION").length).toBeGreaterThan(0);
    expect(screen.getByRole("complementary", { name: "AI assistance" })).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "Assign to me" }));
    fireEvent.click(screen.getByRole("button", { name: "Close alert" }));
    expect(screen.getByRole("dialog", { name: "Close alert" })).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "Confirm close" }));
    await waitFor(() => expect(screen.getByText("Alert closed")).toBeInTheDocument());
  });

  it("WF-3 case close requires confirmation and WF-7 AI panel has no close control", async () => {
    tokenWith(["dash:workspace:read", "invest:case:read", "invest:case:close", "ai:investigation:assist"]);
    render(
      <MemoryRouter initialEntries={["/investigations/cases/cccccccc-cccc-4ccc-8ccc-cccccccccccc"]}>
        <App />
      </MemoryRouter>,
    );
    expect(await screen.findByRole("heading", { name: "Case detail" })).toBeInTheDocument();
    const ai = screen.getByRole("complementary", { name: "AI assistance" });
    expect(ai).not.toHaveTextContent("Close case");
    fireEvent.click(screen.getByRole("button", { name: "Close case" }));
    fireEvent.change(screen.getByLabelText("Resolution summary"), { target: { value: "complete" } });
    fireEvent.click(screen.getByRole("button", { name: "Confirm close" }));
    await waitFor(() => expect(screen.getByText("Case closed")).toBeInTheDocument());
  });

  it("WF-4 compliance decision is confirmed and not AI", async () => {
    tokenWith(["dash:workspace:read", "comp:kyc:write", "comp:kyc:approve"]);
    render(
      <MemoryRouter initialEntries={["/compliance/dddddddd-dddd-4ddd-8ddd-dddddddddddd"]}>
        <App />
      </MemoryRouter>,
    );
    fireEvent.click(await screen.findByRole("button", { name: "Record KYC decision" }));
    expect(screen.getByRole("dialog", { name: "Confirm compliance decision" })).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "Confirm" }));
    await waitFor(() => expect(screen.getByText("Human decision recorded")).toBeInTheDocument());
    expect(screen.queryByRole("button", { name: /AI approve/i })).not.toBeInTheDocument();
  });

  it("shows admin users orchestration screen", async () => {
    tokenWith(["dash:workspace:read", "admin:user:provision"]);
    render(
      <MemoryRouter initialEntries={["/admin/users"]}>
        <App />
      </MemoryRouter>,
    );
    expect(await screen.findByRole("heading", { name: "Users and organizations" })).toBeInTheDocument();
    expect(screen.getByLabelText("User email")).toBeInTheDocument();
    expect(screen.queryByText(/milestone M10/)).not.toBeInTheDocument();
    expect(screen.queryByText(/Security Operations/i)).not.toBeInTheDocument();
  });

  it("exposes skip link and table headers on alert queue", async () => {
    tokenWith(["dash:workspace:read", "dash:queue:read", "alert:alert:read"]);
    render(
      <MemoryRouter initialEntries={["/alerts"]}>
        <App />
      </MemoryRouter>,
    );
    expect(screen.getByText("Skip to content")).toBeInTheDocument();
    expect(await screen.findByRole("columnheader", { name: "Title" })).toBeInTheDocument();
  });
});
