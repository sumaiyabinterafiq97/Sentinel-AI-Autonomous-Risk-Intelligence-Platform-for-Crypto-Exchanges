import { FormEvent, type ReactElement, useState } from "react";
import { Navigate, Route, Routes, useNavigate } from "react-router-dom";
import { api, envelopeData, errorMessage } from "../shared/api";
import { getToken, saveSession } from "../shared/session";
import { Field } from "../shared/ui";
import { Shell } from "./Shell";
import {
  AdminAuditPage,
  AdminSettingsPage,
  AdminUsersPage,
  AlertDetailPage,
  AlertQueuePage,
  AssessmentPage,
  AuditPage,
  CaseDetailPage,
  CaseListPage,
  ComplianceDetailPage,
  ComplianceQueuePage,
  OverviewPage,
  RulesPage,
  SearchPage,
} from "./screens";

function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError("");
    const { status, body } = await api("/v1/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
    if (status >= 400) {
      setError("Sign-in failed");
      return;
    }
    const data = envelopeData<{ accessToken?: string; organizationId?: string }>(body, {});
    if (!data.accessToken) {
      setError("Sign-in failed");
      return;
    }
    let org = data.organizationId;
    if (!org) {
      try {
        const json = JSON.parse(atob(data.accessToken.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
        org = json.org;
      } catch {
        org = undefined;
      }
    }
    if (!org) {
      setError("Sign-in failed");
      return;
    }
    saveSession(data.accessToken, org);
    navigate("/login/mfa");
  }

  return (
    <main className="mx-auto max-w-sm px-4 py-16 text-slate-100">
      <h1 className="text-2xl font-semibold">Sentinel AI</h1>
      <p className="mt-2 text-sm text-slate-400">Sign in to the operations workspace.</p>
      <form onSubmit={onSubmit} className="mt-6 space-y-3" aria-describedby={error ? "login-error" : undefined}>
        <Field id="email" label="Email">
          <input
            id="email"
            type="email"
            required
            aria-required="true"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="mt-1 min-h-11 w-full rounded bg-slate-900 px-2 py-1"
          />
        </Field>
        <Field id="password" label="Password">
          <input
            id="password"
            type="password"
            required
            aria-required="true"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="mt-1 min-h-11 w-full rounded bg-slate-900 px-2 py-1"
          />
        </Field>
        {error ? (
          <p id="login-error" role="alert" className="text-sm text-red-400">
            {error}
          </p>
        ) : null}
        <button type="submit" className="min-h-11 rounded bg-amber-500 px-3 py-1 text-slate-950">
          Continue
        </button>
      </form>
    </main>
  );
}

function MfaPage() {
  const navigate = useNavigate();
  const [code, setCode] = useState("");
  const [error, setError] = useState("");
  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }
  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    const { status, body } = await api("/v1/auth/mfa/verify", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code }),
    });
    if (status >= 400) {
      setError(errorMessage(body, "MFA verification failed"));
      return;
    }
    await api("/v1/auth/session");
    navigate("/workspace");
  }
  return (
    <main className="mx-auto max-w-sm px-4 py-16 text-slate-100">
      <h1 className="text-2xl font-semibold">Verify MFA</h1>
      <p className="mt-2 text-sm text-slate-400">Enter the 6-digit simulation code. This is not TOTP hardware.</p>
      <form onSubmit={onSubmit} className="mt-6 space-y-3">
        <Field id="mfa-code" label="MFA code" error={error}>
          <input
            id="mfa-code"
            inputMode="numeric"
            pattern="[0-9]{6}"
            required
            aria-required="true"
            aria-describedby={error ? "mfa-code-error" : undefined}
            value={code}
            onChange={(e) => setCode(e.target.value)}
            className="mt-1 min-h-11 w-full rounded bg-slate-900 px-2 py-1"
          />
        </Field>
        <button type="submit" className="min-h-11 rounded bg-amber-500 px-3 text-slate-950">
          Verify
        </button>
      </form>
    </main>
  );
}

function RequireSession({ children }: { children: ReactElement }) {
  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/login/mfa" element={<MfaPage />} />
      <Route
        path="/"
        element={
          <RequireSession>
            <Shell />
          </RequireSession>
        }
      >
        <Route index element={<Navigate to="/workspace" replace />} />
        <Route path="workspace" element={<OverviewPage />} />
        <Route path="alerts" element={<AlertQueuePage />} />
        <Route path="alerts/:alertId" element={<AlertDetailPage />} />
        <Route path="investigations" element={<CaseListPage />} />
        <Route path="investigations/cases/:caseId" element={<CaseDetailPage />} />
        <Route path="risk" element={<Navigate to="/risk/rules" replace />} />
        <Route path="risk/assessments/:assessmentId" element={<AssessmentPage />} />
        <Route path="risk/rules" element={<RulesPage />} />
        <Route path="compliance" element={<ComplianceQueuePage />} />
        <Route path="compliance/audit" element={<AuditPage />} />
        <Route path="compliance/:reviewId" element={<ComplianceDetailPage />} />
        <Route path="search" element={<SearchPage />} />
        <Route path="admin/users" element={<AdminUsersPage />} />
        <Route path="admin/settings" element={<AdminSettingsPage />} />
        <Route path="admin/audit" element={<AdminAuditPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
