import { useEffect, useState } from "react";
import { API_BASE, api, envelopeData } from "./api";
import { getOrg, getToken } from "./session";

export function useQueueRefresh(path: string, enabled: boolean) {
  const [stale, setStale] = useState(false);
  const [tick, setTick] = useState(0);
  useEffect(() => {
    if (!enabled) {
      return;
    }
    const token = getToken();
    const org = getOrg();
    if (!token || !org) {
      return;
    }
    const ac = new AbortController();
    fetch(`${API_BASE}/v1/workspace/subscriptions/workspace`, {
      headers: { Authorization: `Bearer ${token}`, "X-Organization-Id": org, Accept: "text/event-stream" },
      signal: ac.signal,
    }).catch(() => setStale(true));
    const poll = window.setInterval(() => {
      setTick((n) => n + 1);
    }, 15000);
    return () => {
      ac.abort();
      window.clearInterval(poll);
    };
  }, [enabled, path]);
  return { stale, tick };
}

export async function listOrEmpty<T>(path: string) {
  const { status, body } = await api(path);
  if (status === 403) {
    return { status, items: [] as T[] };
  }
  return { status, items: envelopeData<T[]>(body, []) };
}
