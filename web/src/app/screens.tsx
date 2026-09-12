import { FormEvent, useCallback, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { AiAssistPanel } from "../shared/AiAssistPanel";
import { api, envelopeData, errorMessage, paginationCursor } from "../shared/api";
import { listOrEmpty, useQueueRefresh } from "../shared/live";
import { currentUserId, getOrg, hasPermission } from "../shared/session";
import { ConfirmDialog, Field, Layer, Page, PermissionGuard, StatusBadge } from "../shared/ui";

type QueueItem = { id: string; type?: string; title?: string; priority?: number; status?: string };

export function OverviewPage() {
  const [state, setState] = useState("loading");
  const [dashboard, setDashboard] = useState<Record<string, unknown>>({});
  const [widgets, setWidgets] = useState<{ id: string; title?: string }[]>([]);
  const { stale, tick } = useQueueRefresh("/v1/workspace/dashboard", true);

  useEffect(() => {
    let cancelled = false;
    Promise.all([api("/v1/workspace"), api("/v1/workspace/dashboard"), api("/v1/workspace/widgets")]).then(
      ([workspace, dash, widgetRes]) => {
        if (cancelled) {
          return;
        }
        if (dash.status === 403 || workspace.status === 403) {
          setState("permission denied");
          return;
        }
        if (dash.status >= 400) {
          setState("error");
          return;
        }
        setDashboard(envelopeData(dash.body, {}));
        setWidgets(envelopeData(widgetRes.body, []));
        setState("loaded");
      },
    );
    return () => {
      cancelled = true;
    };
  }, [tick]);

  async function interact(widgetId: string) {
    await api(`/v1/workspace/widgets/${widgetId}/interactions`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ action: "open" }),
    });
  }

  const ai = dashboard.aiAssist as { disclaimer?: string } | undefined;
  const countsStale = stale ? "Queue updated may be delayed — polling fallback active" : state;
  return (
    <Page title="Operations overview" state={countsStale}>
      <p className="sr-only" aria-live="polite">
        Open alerts {String(dashboard.alertCount ?? 0)}, cases {String(dashboard.caseCount ?? 0)}
      </p>
      <div className="grid gap-3 md:grid-cols-3">
        <p>
          Open alerts: <StatusBadge label={String(dashboard.alertCount ?? 0)} />
        </p>
        <p>
          Open cases: <StatusBadge label={String(dashboard.caseCount ?? 0)} />
        </p>
        <p>Assessments: {String(dashboard.assessmentCount ?? 0)}</p>
      </div>
      <ul className="mt-4 space-y-2">
        {widgets.map((widget) => (
          <li key={widget.id}>
            <button type="button" className="min-h-11 underline" onClick={() => interact(widget.id)}>
              {widget.title || widget.id}
            </button>
          </li>
        ))}
      </ul>
      {ai ? (
        <div className="mt-4">
          <Layer kind="AI">{ai.disclaimer}</Layer>
        </div>
      ) : null}
    </Page>
  );
}

function EntityTable({
  caption,
  items,
  toDetail,
}: {
  caption: string;
  items: QueueItem[];
  toDetail: (id: string) => string;
}) {
  return (
    <table className="w-full text-left text-sm">
      <caption className="sr-only">{caption}</caption>
      <thead>
        <tr>
          <th scope="col">Title</th>
          <th scope="col">Status</th>
          <th scope="col">Priority</th>
        </tr>
      </thead>
      <tbody>
        {items.map((item) => (
          <tr key={item.id}>
            <td>
              <Link className="underline" to={toDetail(item.id)}>
                Open {item.title || item.id}
              </Link>
            </td>
            <td>
              <StatusBadge label={item.status || "unknown"} />
            </td>
            <td>{item.priority ?? "—"}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

export function AlertQueuePage() {
  const [items, setItems] = useState<QueueItem[]>([]);
  const [state, setState] = useState("loading");
  const [cursor, setCursor] = useState<string | undefined>();
  const { stale, tick } = useQueueRefresh("/v1/alerts", true);

  useEffect(() => {
    const path = hasPermission("alert:alert:read") ? "/v1/alerts?limit=50" : "/v1/workspace/queues/alerts";
    api(path).then(({ status, body }) => {
      if (status === 403) {
        setState("permission denied");
        return;
      }
      const data = envelopeData<QueueItem[]>(body, []);
      setItems(data);
      setCursor(paginationCursor(body));
      setState(data.length === 0 ? "empty" : "loaded");
    });
  }, [tick]);

  async function loadMore() {
    if (!cursor) {
      return;
    }
    const { body } = await api(`/v1/alerts?limit=50&cursor=${encodeURIComponent(cursor)}`);
    setItems((current) => [...current, ...envelopeData<QueueItem[]>(body, [])]);
    setCursor(paginationCursor(body));
  }

  return (
    <Page title="Alert queue" state={stale ? "Queue updated (polling fallback)" : state}>
      {state === "empty" ? <p>No items in this queue.</p> : null}
      <EntityTable caption="Alerts" items={items} toDetail={(id) => `/alerts/${id}`} />
      {cursor ? (
        <button type="button" className="mt-3 min-h-11 rounded bg-slate-700 px-3" onClick={loadMore}>
          Load more alerts
        </button>
      ) : null}
    </Page>
  );
}

export function AlertDetailPage() {
  const { alertId } = useParams();
  const navigate = useNavigate();
  const [alert, setAlert] = useState<Record<string, unknown>>({});
  const [assessment, setAssessment] = useState<Record<string, unknown> | null>(null);
  const [confirmClose, setConfirmClose] = useState(false);
  const [reason, setReason] = useState("false_positive");
  const [priority, setPriority] = useState("1");
  const [message, setMessage] = useState("");

  const reload = useCallback(async () => {
    if (!alertId) {
      return;
    }
    const { status, body } = await api(`/v1/alerts/${alertId}`);
    if (status === 404) {
      setMessage("Alert not found");
      return;
    }
    const data = envelopeData<Record<string, unknown>>(body, {});
    setAlert(data);
    if (typeof data.riskAssessmentId === "string") {
      const risk = await api(`/v1/risk/assessments/${data.riskAssessmentId}`);
      setAssessment(envelopeData(risk.body, {}));
    }
  }, [alertId]);

  useEffect(() => {
    void reload();
  }, [reload]);

  async function assign() {
    const assigneeId = currentUserId();
    const { status, body } = await api(`/v1/alerts/${alertId}/assign`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ assigneeId }),
    });
    setMessage(status >= 400 ? errorMessage(body, "Assign failed") : "Alert assigned");
    await reload();
  }

  async function close() {
    const { status, body } = await api(`/v1/alerts/${alertId}/close`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ dispositionReason: reason }),
    });
    setConfirmClose(false);
    setMessage(status >= 400 ? errorMessage(body, "Close failed") : "Alert closed");
    await reload();
  }

  async function changePriority() {
    const { status, body } = await api(`/v1/alerts/${alertId}/priority`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ priority: Number(priority) }),
    });
    setMessage(status >= 400 ? errorMessage(body, "Priority update failed") : "Priority updated");
    await reload();
  }

  async function escalate() {
    const created = await api("/v1/investigations/cases", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ title: `Escalation ${alertId}`, sourceAlertId: alertId }),
    });
    const cse = envelopeData<{ id?: string }>(created.body, {});
    if (created.status >= 400 || !cse.id) {
      setMessage(errorMessage(created.body, "Case create failed"));
      return;
    }
    await api(`/v1/alerts/${alertId}/investigation-link`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ caseId: cse.id }),
    });
    navigate(`/investigations/cases/${cse.id}`);
  }

  return (
    <Page title="Alert detail">
      <Layer kind="SYSTEM">
        Alert {alertId} — status <StatusBadge label={String(alert.status ?? "unknown")} /> priority {String(alert.priority ?? "—")}.
        Lifecycle owned by ALERT. DASH does not set priority.
      </Layer>
      {assessment ? (
        <p className="mt-2 text-sm">
          Risk context score {String(assessment.score)} ({String(assessment.riskLevel)}) — not alert priority.{" "}
          <Link className="underline" to={`/risk/assessments/${alert.riskAssessmentId}`}>
            Open assessment
          </Link>
        </p>
      ) : (
        <p className="mt-2 text-sm text-slate-400">No linked assessment</p>
      )}
      {message ? (
        <p className="mt-2 text-sm" role="status">
          {message}
        </p>
      ) : null}
      <div className="mt-3 flex flex-wrap gap-2">
        <PermissionGuard permission="alert:alert:assign">
          <button type="button" className="min-h-11 rounded bg-slate-700 px-3" onClick={assign}>
            Assign to me
          </button>
        </PermissionGuard>
        <PermissionGuard permission="alert:alert:priority">
          <label htmlFor="alert-priority" className="sr-only">
            Priority
          </label>
          <input
            id="alert-priority"
            value={priority}
            onChange={(e) => setPriority(e.target.value)}
            className="min-h-11 w-16 rounded bg-slate-900 px-2"
          />
          <button type="button" className="min-h-11 rounded bg-slate-700 px-3" onClick={changePriority}>
            Set priority
          </button>
        </PermissionGuard>
        <PermissionGuard permission="alert:alert:close">
          <button type="button" className="min-h-11 rounded bg-slate-700 px-3" onClick={() => setConfirmClose(true)}>
            Close alert
          </button>
        </PermissionGuard>
        <PermissionGuard permission="invest:case:write">
          <button type="button" className="min-h-11 rounded bg-slate-700 px-3" onClick={escalate}>
            Escalate to case
          </button>
        </PermissionGuard>
      </div>
      <div className="mt-4">
        <AiAssistPanel kind="alert" targetId={typeof alert.riskAssessmentId === "string" ? alert.riskAssessmentId : undefined} />
      </div>
      <ConfirmDialog
        open={confirmClose}
        title="Close alert"
        confirmLabel="Confirm close"
        onCancel={() => setConfirmClose(false)}
        onConfirm={close}
      >
        <Field id="disposition" label="Disposition reason">
          <input
            id="disposition"
            required
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            className="min-h-11 w-full rounded bg-slate-800 px-2"
          />
        </Field>
      </ConfirmDialog>
    </Page>
  );
}

export function CaseListPage() {
  const [items, setItems] = useState<QueueItem[]>([]);
  const [title, setTitle] = useState("");
  const [state, setState] = useState("loading");
  const { stale, tick } = useQueueRefresh("/v1/investigations/cases", true);

  useEffect(() => {
    const path = hasPermission("invest:case:read") ? "/v1/investigations/cases" : "/v1/workspace/queues/cases";
    api(path).then(({ status, body }) => {
      if (status === 403) {
        setState("permission denied");
        return;
      }
      const data = envelopeData<QueueItem[]>(body, []);
      setItems(data);
      setState(data.length === 0 ? "empty" : "loaded");
    });
  }, [tick]);

  async function create(event: FormEvent) {
    event.preventDefault();
    const { status, body } = await api("/v1/investigations/cases", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ title }),
    });
    if (status < 400) {
      const created = envelopeData<QueueItem>(body, { id: "", title });
      setItems((current) => [created, ...current]);
      setTitle("");
      setState("loaded");
    }
  }

  return (
    <Page title="Investigation cases" state={stale ? "Queue updated (polling fallback)" : state}>
      <PermissionGuard permission="invest:case:write">
        <form onSubmit={create} className="mb-4 flex gap-2">
          <Field id="case-title" label="New case title">
            <input id="case-title" required value={title} onChange={(e) => setTitle(e.target.value)} className="min-h-11 rounded bg-slate-900 px-2" />
          </Field>
          <button type="submit" className="min-h-11 self-end rounded bg-slate-700 px-3">
            Create case
          </button>
        </form>
      </PermissionGuard>
      {state === "empty" ? <p>No items in this queue.</p> : null}
      <EntityTable caption="Cases" items={items} toDetail={(id) => `/investigations/cases/${id}`} />
    </Page>
  );
}

export function CaseDetailPage() {
  const { caseId } = useParams();
  const [cse, setCse] = useState<Record<string, unknown>>({});
  const [timeline, setTimeline] = useState<{ id?: string; type?: string; detail?: string }[]>([]);
  const [notes, setNotes] = useState<{ id: string; content: string }[]>([]);
  const [evidenceRef, setEvidenceRef] = useState("");
  const [note, setNote] = useState("");
  const [confirmClose, setConfirmClose] = useState(false);
  const [summary, setSummary] = useState("");
  const [message, setMessage] = useState("");

  const reload = useCallback(async () => {
    if (!caseId) {
      return;
    }
    const [detail, tl, nt] = await Promise.all([
      api(`/v1/investigations/cases/${caseId}`),
      api(`/v1/investigations/cases/${caseId}/timeline`),
      api(`/v1/investigations/cases/${caseId}/notes`),
    ]);
    setCse(envelopeData(detail.body, {}));
    setTimeline(envelopeData(tl.body, []));
    setNotes(envelopeData(nt.body, []));
  }, [caseId]);

  useEffect(() => {
    void reload();
  }, [reload]);

  async function assign() {
    await api(`/v1/investigations/cases/${caseId}/assign`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ assigneeId: currentUserId() }),
    });
    await reload();
  }

  async function attach(event: FormEvent) {
    event.preventDefault();
    const { status, body } = await api(`/v1/investigations/cases/${caseId}/evidence`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ evidenceRef, description: "operator attach" }),
    });
    setMessage(status >= 400 ? errorMessage(body, "Evidence attach failed") : "Evidence attached");
    setEvidenceRef("");
    await reload();
  }

  async function addNote(event: FormEvent) {
    event.preventDefault();
    await api(`/v1/investigations/cases/${caseId}/notes`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ content: note }),
    });
    setNote("");
    await reload();
  }

  async function close() {
    const { status, body } = await api(`/v1/investigations/cases/${caseId}/close`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ resolutionSummary: summary }),
    });
    setConfirmClose(false);
    setMessage(status >= 400 ? errorMessage(body, "Close failed") : "Case closed");
    await reload();
  }

  return (
    <Page title="Case detail">
      <Layer kind="SYSTEM">
        Case {caseId} — <StatusBadge label={String(cse.status ?? "unknown")} /> {String(cse.title ?? "")}. Lifecycle owned by INVEST.
      </Layer>
      {message ? (
        <p className="mt-2" role="status">
          {message}
        </p>
      ) : null}
      <div className="mt-3 flex flex-wrap gap-2">
        <PermissionGuard permission="invest:case:assign">
          <button type="button" className="min-h-11 rounded bg-slate-700 px-3" onClick={assign}>
            Assign to me
          </button>
        </PermissionGuard>
        <PermissionGuard permission="invest:case:close">
          <button type="button" className="min-h-11 rounded bg-slate-700 px-3" onClick={() => setConfirmClose(true)}>
            Close case
          </button>
        </PermissionGuard>
      </div>
      <h2 className="mt-4 text-lg">Timeline</h2>
      <ol className="mt-2 list-decimal pl-5 text-sm">
        {timeline.map((item, index) => (
          <li key={item.id || index}>{item.type || item.detail || JSON.stringify(item)}</li>
        ))}
      </ol>
      <PermissionGuard permission="invest:evidence:write">
        <form onSubmit={attach} className="mt-4 space-y-2">
          <Field id="evidence-ref" label="Evidence reference">
            <input id="evidence-ref" required value={evidenceRef} onChange={(e) => setEvidenceRef(e.target.value)} className="min-h-11 rounded bg-slate-900 px-2" />
          </Field>
          <button type="submit" className="min-h-11 rounded bg-slate-700 px-3">
            Attach evidence
          </button>
        </form>
      </PermissionGuard>
      <PermissionGuard permission="invest:case:write">
        <form onSubmit={addNote} className="mt-4 space-y-2">
          <Field id="case-note" label="Note">
            <textarea id="case-note" required value={note} onChange={(e) => setNote(e.target.value)} className="min-h-11 w-full rounded bg-slate-900 px-2" />
          </Field>
          <button type="submit" className="min-h-11 rounded bg-slate-700 px-3">
            Add note
          </button>
        </form>
      </PermissionGuard>
      <ul className="mt-2 text-sm">
        {notes.map((item) => (
          <li key={item.id}>{item.content}</li>
        ))}
      </ul>
      <div className="mt-4">
        <AiAssistPanel kind="case" targetId={caseId} />
      </div>
      <ConfirmDialog
        open={confirmClose}
        title="Close case"
        confirmLabel="Confirm close"
        onCancel={() => setConfirmClose(false)}
        onConfirm={close}
      >
        <Field id="resolution" label="Resolution summary">
          <textarea id="resolution" required value={summary} onChange={(e) => setSummary(e.target.value)} className="min-h-11 w-full rounded bg-slate-800 px-2" />
        </Field>
      </ConfirmDialog>
    </Page>
  );
}

export function AssessmentPage() {
  const { assessmentId } = useParams();
  const [data, setData] = useState<Record<string, unknown>>({});
  useEffect(() => {
    if (!assessmentId) {
      return;
    }
    api(`/v1/risk/assessments/${assessmentId}`).then(({ body }) => setData(envelopeData(body, {})));
  }, [assessmentId]);
  return (
    <Page title="Risk assessment">
      <Layer kind="SYSTEM">
        Assessment {assessmentId} — score {String(data.score ?? "—")} level {String(data.riskLevel ?? "—")}. Score owned by RISK.
      </Layer>
      <p className="mt-2 text-sm">{String(data.explanationSummary ?? "")}</p>
      <p className="mt-2 text-sm text-slate-400">No create-alert control on this screen.</p>
      <div className="mt-4">
        <AiAssistPanel kind="assessment" targetId={assessmentId} />
      </div>
    </Page>
  );
}

export function RulesPage() {
  const [rules, setRules] = useState<{ id: string; name?: string; enabled?: boolean }[]>([]);
  const [name, setName] = useState("operator-rule");
  useEffect(() => {
    api("/v1/risk/rules").then(({ body }) => setRules(envelopeData(body, [])));
  }, []);
  async function create(event: FormEvent) {
    event.preventDefault();
    const { body } = await api("/v1/risk/rules", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name, enabled: true, definition: { type: "threshold" } }),
    });
    const created = envelopeData<{ id: string; name?: string; enabled?: boolean }>(body, { id: "", name });
    setRules((current) => [...current, created]);
  }
  async function toggle(id: string, enabled: boolean) {
    await api(`/v1/risk/rules/${id}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ enabled: !enabled }),
    });
    setRules((current) => current.map((rule) => (rule.id === id ? { ...rule, enabled: !enabled } : rule)));
  }
  return (
    <Page title="Risk rules">
      <p className="mb-3 text-sm text-slate-400">Rules are authored in RISK. AI cannot publish RiskCalculated.</p>
      <PermissionGuard permission="risk:rule:write">
        <form onSubmit={create} className="mb-4 flex gap-2">
          <Field id="rule-name" label="Rule name">
            <input id="rule-name" required value={name} onChange={(e) => setName(e.target.value)} className="min-h-11 rounded bg-slate-900 px-2" />
          </Field>
          <button type="submit" className="min-h-11 self-end rounded bg-slate-700 px-3">
            Create rule
          </button>
        </form>
      </PermissionGuard>
      <ul>
        {rules.map((rule) => (
          <li key={rule.id} className="flex items-center gap-2">
            <StatusBadge label={rule.enabled ? "enabled" : "disabled"} />
            {rule.name || rule.id}
            <PermissionGuard permission="risk:rule:write">
              <button type="button" className="min-h-11 underline" onClick={() => toggle(rule.id, Boolean(rule.enabled))}>
                Toggle {rule.name || rule.id}
              </button>
            </PermissionGuard>
          </li>
        ))}
      </ul>
    </Page>
  );
}

const COMP_KEY = "sentinel.knownReviews";

function knownReviews(): { id: string; type: string }[] {
  try {
    return JSON.parse(sessionStorage.getItem(COMP_KEY) || "[]") as { id: string; type: string }[];
  } catch {
    return [];
  }
}

function rememberReview(id: string, type: string) {
  const next = [...knownReviews().filter((item) => item.id !== id), { id, type }];
  sessionStorage.setItem(COMP_KEY, JSON.stringify(next));
}

export function ComplianceQueuePage() {
  const [subject, setSubject] = useState("user-subject");
  const [tx, setTx] = useState("tx-1");
  const [items, setItems] = useState(knownReviews());
  async function start(path: string, body: Record<string, string>, type: string) {
    const { status, body: res } = await api(path, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    if (status < 400) {
      const data = envelopeData<{ id?: string }>(res, {});
      if (data.id) {
        rememberReview(data.id, type);
        setItems(knownReviews());
      }
    }
  }
  return (
    <Page title="Compliance reviews" state={items.length ? "loaded" : "empty"}>
      <p className="mb-3 text-sm text-slate-400">
        No compliance list API is authorized (COMP list GET gap). Start a review, then open it from this session list.
      </p>
      <div className="flex flex-wrap gap-2">
        <Field id="subject-ref" label="Subject reference">
          <input id="subject-ref" value={subject} onChange={(e) => setSubject(e.target.value)} className="min-h-11 rounded bg-slate-900 px-2" />
        </Field>
        <PermissionGuard permission="comp:kyc:write">
          <button type="button" className="min-h-11 self-end rounded bg-slate-700 px-3" onClick={() => start("/v1/compliance/kyc-reviews", { subjectRef: subject }, "kyc")}>
            Start KYC
          </button>
        </PermissionGuard>
        <PermissionGuard permission="comp:aml:write">
          <button type="button" className="min-h-11 self-end rounded bg-slate-700 px-3" onClick={() => start("/v1/compliance/aml-reviews", { subjectRef: subject }, "aml")}>
            Start AML
          </button>
        </PermissionGuard>
        <PermissionGuard permission="comp:sanctions:write">
          <button
            type="button"
            className="min-h-11 self-end rounded bg-slate-700 px-3"
            onClick={() => start("/v1/compliance/sanctions-screenings", { subjectRef: subject }, "sanctions")}
          >
            Screen sanctions
          </button>
        </PermissionGuard>
      </div>
      <form
        className="mt-4 flex gap-2"
        onSubmit={(event) => {
          event.preventDefault();
          start("/v1/compliance/travel-rule/validations", { transactionRef: tx }, "travel");
        }}
      >
        <Field id="tx-ref" label="Transaction reference">
          <input id="tx-ref" value={tx} onChange={(e) => setTx(e.target.value)} className="min-h-11 rounded bg-slate-900 px-2" />
        </Field>
        <PermissionGuard permission="comp:travelrule:write">
          <button type="submit" className="min-h-11 self-end rounded bg-slate-700 px-3">
            Record travel rule
          </button>
        </PermissionGuard>
      </form>
      <ul className="mt-4">
        {items.map((item) => (
          <li key={item.id}>
            <Link className="underline" to={`/compliance/${item.id}`}>
              {item.type} {item.id}
            </Link>
          </li>
        ))}
      </ul>
    </Page>
  );
}

export function ComplianceDetailPage() {
  const { reviewId } = useParams();
  const [decision, setDecision] = useState("approved");
  const [confirm, setConfirm] = useState(false);
  const [message, setMessage] = useState("");
  async function decide() {
    const { status, body } = await api(`/v1/compliance/kyc-reviews/${reviewId}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ decision }),
    });
    setConfirm(false);
    setMessage(status >= 400 ? errorMessage(body, "Decision failed") : "Human decision recorded");
  }
  async function disposition() {
    const { status, body } = await api(`/v1/compliance/sanctions-screenings/${reviewId}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ disposition: "false_positive" }),
    });
    setMessage(status >= 400 ? errorMessage(body, "Disposition failed") : "Sanctions disposition recorded");
  }
  return (
    <Page title="Compliance review">
      <Layer kind="HUMAN">Review {reviewId} — approve/reject is a human COMP action. AI cannot approve.</Layer>
      {message ? (
        <p className="mt-2" role="status">
          {message}
        </p>
      ) : null}
      <PermissionGuard permission="comp:kyc:approve">
        <Field id="kyc-decision" label="KYC decision">
          <select id="kyc-decision" value={decision} onChange={(e) => setDecision(e.target.value)} className="min-h-11 rounded bg-slate-900 px-2">
            <option value="approved">approved</option>
            <option value="rejected">rejected</option>
            <option value="pending">pending</option>
          </select>
        </Field>
        <button type="button" className="mt-2 min-h-11 rounded bg-slate-700 px-3" onClick={() => setConfirm(true)}>
          Record KYC decision
        </button>
      </PermissionGuard>
      <PermissionGuard permission="comp:sanctions:approve">
        <button type="button" className="mt-2 min-h-11 rounded bg-slate-700 px-3" onClick={disposition}>
          Disposition sanctions
        </button>
      </PermissionGuard>
      <ConfirmDialog open={confirm} title="Confirm compliance decision" confirmLabel="Confirm" onCancel={() => setConfirm(false)} onConfirm={decide}>
        <p>This records a human COMP decision. AI recommendations are not used.</p>
      </ConfirmDialog>
    </Page>
  );
}

export function AuditPage() {
  const [message, setMessage] = useState("");
  async function prepare() {
    const { status, body } = await api("/v1/compliance/audit-packages", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ scope: "mvp" }),
    });
    setMessage(status >= 400 ? errorMessage(body, "Package request failed") : `Package ${envelopeData<{ id?: string }>(body, {}).id ?? "ready"}`);
  }
  return (
    <Page title="Audit package">
      <p className="mb-3 text-sm">Prepare packages via API-COMP-007. DASH does not own package status. REPORT is V2.</p>
      <PermissionGuard permission="comp:audit:write">
        <button type="button" className="min-h-11 rounded bg-slate-700 px-3" onClick={prepare}>
          Prepare audit package
        </button>
      </PermissionGuard>
      {message ? (
        <p className="mt-2" role="status">
          {message}
        </p>
      ) : null}
    </Page>
  );
}

export function SearchPage() {
  const [q, setQ] = useState("");
  const [hits, setHits] = useState<QueueItem[]>([]);
  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    const [alerts, cases, assessments] = await Promise.all([
      listOrEmpty<QueueItem>("/v1/alerts"),
      listOrEmpty<QueueItem>("/v1/investigations/cases"),
      listOrEmpty<QueueItem>("/v1/risk/assessments"),
    ]);
    const needle = q.toLowerCase();
    setHits(
      [...alerts.items, ...cases.items, ...assessments.items].filter((item) =>
        `${item.title || ""} ${item.id}`.toLowerCase().includes(needle),
      ),
    );
  }
  return (
    <Page title="Search / intelligence">
      <p className="mb-3 text-sm text-slate-400">Composed from authorized list APIs. No global search API.</p>
      <form onSubmit={onSubmit} className="flex gap-2">
        <label className="sr-only" htmlFor="q">
          Query
        </label>
        <input id="q" value={q} onChange={(e) => setQ(e.target.value)} className="min-h-11 rounded bg-slate-900 px-2 py-1" />
        <button type="submit" className="min-h-11 rounded bg-slate-700 px-3">
          Search
        </button>
      </form>
      <ul className="mt-3">
        {hits.map((hit) => (
          <li key={hit.id}>{hit.title || hit.id}</li>
        ))}
      </ul>
    </Page>
  );
}

export function AdminUsersPage() {
  const [email, setEmail] = useState("");
  const [orgName, setOrgName] = useState("");
  const [users, setUsers] = useState<{ id: string; email?: string }[]>([]);
  const [roleId, setRoleId] = useState("");
  const [userId, setUserId] = useState("");
  const [message, setMessage] = useState("");
  useEffect(() => {
    api("/v1/users").then(({ body }) => setUsers(envelopeData(body, [])));
  }, []);
  async function provisionUser(event: FormEvent) {
    event.preventDefault();
    const org = getOrg();
    const { status, body } = await api("/v1/admin/users/provision", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, organizationId: org }),
    });
    setMessage(status >= 400 ? "User provision failed" : `Provisioned ${envelopeData<{ email?: string }>(body, {}).email ?? email}`);
  }
  async function provisionOrg(event: FormEvent) {
    event.preventDefault();
    const { status, body } = await api("/v1/admin/organizations/provision", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name: orgName }),
    });
    setMessage(status >= 400 ? "Organization provision failed" : `Provisioned ${envelopeData<{ name?: string }>(body, {}).name ?? orgName}`);
  }
  async function assignRole(event: FormEvent) {
    event.preventDefault();
    const { status } = await api("/v1/authz/role-assignments", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId, roleId }),
    });
    setMessage(status >= 400 ? "Role assignment failed" : "Role assigned");
  }
  return (
    <Page title="Users and organizations">
      <p className="mb-3 text-sm text-slate-400">Orchestrates USER and ORG APIs. Does not replace identity ownership.</p>
      <p className="mb-3 text-sm">
        <Link className="underline" to="/admin/settings">
          Settings
        </Link>
        {" · "}
        <Link className="underline" to="/admin/audit">
          Audit
        </Link>
      </p>
      <form onSubmit={provisionUser} className="mb-4 space-y-2">
        <Field id="provision-email" label="User email">
          <input id="provision-email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} className="min-h-11 rounded bg-slate-900 px-2 py-1" />
        </Field>
        <button type="submit" className="min-h-11 rounded bg-slate-700 px-3">
          Provision user
        </button>
      </form>
      <form onSubmit={provisionOrg} className="space-y-2">
        <Field id="provision-org" label="Organization name">
          <input id="provision-org" required value={orgName} onChange={(e) => setOrgName(e.target.value)} className="min-h-11 rounded bg-slate-900 px-2 py-1" />
        </Field>
        <button type="submit" className="min-h-11 rounded bg-slate-700 px-3">
          Provision organization
        </button>
      </form>
      <form onSubmit={assignRole} className="mt-4 space-y-2">
        <Field id="assign-user" label="User id">
          <input id="assign-user" value={userId} onChange={(e) => setUserId(e.target.value)} className="min-h-11 rounded bg-slate-900 px-2" />
        </Field>
        <Field id="assign-role" label="Role id">
          <input id="assign-role" value={roleId} onChange={(e) => setRoleId(e.target.value)} className="min-h-11 rounded bg-slate-900 px-2" />
        </Field>
        <button type="submit" className="min-h-11 rounded bg-slate-700 px-3">
          Assign role
        </button>
      </form>
      <ul className="mt-3 text-sm">
        {users.map((user) => (
          <li key={user.id}>{user.email || user.id}</li>
        ))}
      </ul>
      {message ? <p className="mt-3 text-sm">{message}</p> : null}
    </Page>
  );
}

export function AdminSettingsPage() {
  const [settings, setSettings] = useState<Record<string, unknown>>({});
  const [integrations, setIntegrations] = useState<{ id: string; type: string; status: string }[]>([]);
  const [keyName, setKeyName] = useState("retention.days");
  const [value, setValue] = useState("30");
  const [intType, setIntType] = useState("webhooks");
  const [promptName, setPromptName] = useState("investigation-default");
  const [template, setTemplate] = useState("Assist only.");
  useEffect(() => {
    api("/v1/admin/settings").then(({ body }) => setSettings(envelopeData(body, {})));
    api("/v1/admin/integrations").then(({ body }) => setIntegrations(envelopeData(body, [])));
  }, []);
  async function save(event: FormEvent) {
    event.preventDefault();
    let parsed: unknown = value;
    try {
      parsed = JSON.parse(value);
    } catch {
      parsed = value;
    }
    const { body } = await api("/v1/admin/settings", {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ [keyName]: parsed }),
    });
    setSettings(envelopeData(body, {}));
  }
  async function addIntegration(event: FormEvent) {
    event.preventDefault();
    const { body } = await api("/v1/admin/integrations", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ type: intType, config: {} }),
    });
    const created = envelopeData<{ id: string; type: string; status: string }>(body, {
      id: "",
      type: intType,
      status: "active",
    });
    setIntegrations((current) => [...current, created]);
  }
  async function savePrompt(event: FormEvent) {
    event.preventDefault();
    await api("/v1/ai/prompts", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name: promptName, template }),
    });
  }
  return (
    <Page title="Platform settings">
      <form onSubmit={save} className="mb-4 space-y-2">
        <Field id="setting-key" label="Setting key">
          <input id="setting-key" value={keyName} onChange={(e) => setKeyName(e.target.value)} className="min-h-11 rounded bg-slate-900 px-2 py-1" />
        </Field>
        <Field id="setting-value" label="Value">
          <input id="setting-value" value={value} onChange={(e) => setValue(e.target.value)} className="min-h-11 rounded bg-slate-900 px-2 py-1" />
        </Field>
        <button type="submit" className="min-h-11 rounded bg-slate-700 px-3">
          Save setting
        </button>
      </form>
      <pre className="mb-4 overflow-auto rounded bg-slate-900 p-2 text-xs">{JSON.stringify(settings, null, 2)}</pre>
      <form onSubmit={addIntegration} className="mb-3 space-y-2">
        <Field id="integration-type" label="Integration type">
          <input id="integration-type" value={intType} onChange={(e) => setIntType(e.target.value)} className="min-h-11 rounded bg-slate-900 px-2 py-1" />
        </Field>
        <button type="submit" className="min-h-11 rounded bg-slate-700 px-3">
          Create integration
        </button>
      </form>
      <ul>
        {integrations.map((item) => (
          <li key={item.id}>
            {item.type} ({item.status})
          </li>
        ))}
      </ul>
      <PermissionGuard permission="ai:prompt:write">
        <form onSubmit={savePrompt} className="mt-6 space-y-2">
          <Field id="prompt-name" label="Prompt name">
            <input id="prompt-name" value={promptName} onChange={(e) => setPromptName(e.target.value)} className="min-h-11 rounded bg-slate-900 px-2" />
          </Field>
          <Field id="prompt-template" label="Prompt template">
            <textarea id="prompt-template" value={template} onChange={(e) => setTemplate(e.target.value)} className="min-h-11 w-full rounded bg-slate-900 px-2" />
          </Field>
          <button type="submit" className="min-h-11 rounded bg-slate-700 px-3">
            Save prompt
          </button>
        </form>
      </PermissionGuard>
    </Page>
  );
}

export function AdminAuditPage() {
  const [rows, setRows] = useState<{ id: string; action: string; occurredAt: string }[]>([]);
  useEffect(() => {
    api("/v1/admin/audit-records").then(({ body }) => setRows(envelopeData(body, [])));
  }, []);
  return (
    <Page title="Admin audit">
      <table className="w-full text-left text-sm">
        <caption className="sr-only">Admin audit records</caption>
        <thead>
          <tr>
            <th scope="col">Action</th>
            <th scope="col">Occurred</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.id}>
              <td>{row.action}</td>
              <td>{row.occurredAt}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </Page>
  );
}
