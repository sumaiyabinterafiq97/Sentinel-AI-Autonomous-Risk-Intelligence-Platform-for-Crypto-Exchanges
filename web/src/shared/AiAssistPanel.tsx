import { useState } from "react";
import { api, envelopeData, errorMessage } from "./api";
import { Layer } from "./ui";

type AssistKind = "alert" | "case" | "assessment";

export function AiAssistPanel({
  kind,
  targetId,
}: {
  kind: AssistKind;
  targetId?: string;
}) {
  const [busy, setBusy] = useState(false);
  const [timeoutBanner, setTimeoutBanner] = useState("");
  const [text, setText] = useState("");
  const [query, setQuery] = useState("");

  async function run() {
    if (!targetId) {
      return;
    }
    setBusy(true);
    setTimeoutBanner("");
    const controller = window.setTimeout(() => {
      setTimeoutBanner("AI assistance timed out. Continue with SYSTEM evidence.");
      setBusy(false);
    }, 10000);
    const path =
      kind === "case"
        ? "/v1/ai/assist/investigation"
        : kind === "assessment" || kind === "alert"
          ? "/v1/ai/assist/risk-explanation"
          : "/v1/ai/assist/retrieve";
    const body =
      kind === "case"
        ? { caseId: targetId, query: query || undefined }
        : { assessmentId: targetId };
    const started = await api(path, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    window.clearTimeout(controller);
    if (started.status >= 400) {
      setTimeoutBanner(errorMessage(started.body, "AI assistance unavailable"));
      setBusy(false);
      return;
    }
    const job = envelopeData<{ recommendationId?: string }>(started.body, {});
    if (job.recommendationId) {
      const rec = await api(`/v1/ai/recommendations/${job.recommendationId}`);
      const data = envelopeData<{ content?: string }>(rec.body, {});
      setText(typeof data.content === "string" ? data.content : "");
    }
    setBusy(false);
  }

  async function retrieve() {
    if (!targetId) {
      return;
    }
    setBusy(true);
    const started = await api("/v1/ai/assist/retrieve", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ query: query || targetId, caseId: kind === "case" ? targetId : undefined }),
    });
    const job = envelopeData<{ recommendationId?: string }>(started.body, {});
    if (job.recommendationId) {
      const rec = await api(`/v1/ai/recommendations/${job.recommendationId}`);
      const data = envelopeData<{ content?: string }>(rec.body, {});
      setText(typeof data.content === "string" ? data.content : "");
    }
    setBusy(false);
  }

  return (
    <aside className="space-y-2" role="complementary" aria-label="AI assistance">
      <Layer kind="SYSTEM">Authoritative data is on the left. AI does not own lifecycle.</Layer>
      <Layer kind="AI">
        {busy ? "Generating assistive summary…" : text || `Assistive summary for ${kind} ${targetId ?? ""}. Not evidence. Not a human decision.`}
      </Layer>
      <Layer kind="EVIDENCE">Citations appear only when retrieval returns IDs. Do not treat AI text as evidence.</Layer>
      <Layer kind="HUMAN">Use domain APIs to close, assign, approve, or score. AI cannot perform those actions.</Layer>
      {timeoutBanner ? (
        <p role="status" aria-live="polite" className="text-sm text-amber-300">
          {timeoutBanner}
        </p>
      ) : null}
      <div className="flex flex-wrap gap-2">
        <label className="sr-only" htmlFor={`ai-query-${kind}`}>
          Assist query
        </label>
        <input
          id={`ai-query-${kind}`}
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className="min-h-11 rounded bg-slate-900 px-2 py-1"
        />
        <button type="button" className="min-h-11 rounded bg-slate-700 px-3" onClick={run} aria-disabled={busy} disabled={busy}>
          Request AI assist
        </button>
        {kind === "case" ? (
          <button type="button" className="min-h-11 rounded bg-slate-700 px-3" onClick={retrieve} disabled={busy}>
            Retrieve
          </button>
        ) : null}
      </div>
    </aside>
  );
}
