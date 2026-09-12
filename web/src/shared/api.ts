import { getOrg, getToken } from "./session";

export const API_BASE = (import.meta.env.VITE_API_BASE_URL as string | undefined) || "http://localhost:8083";

export type UiState = "loading" | "loaded" | "empty" | "error" | "denied" | "unavailable" | "stale";

export async function api(path: string, init: RequestInit = {}) {
  const headers = new Headers(init.headers);
  headers.set("Accept", "application/json");
  const token = getToken();
  const org = getOrg();
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  if (org) {
    headers.set("X-Organization-Id", org);
  }
  const response = await fetch(`${API_BASE}${path}`, { ...init, headers });
  const body = await response.json().catch(() => ({}));
  return { status: response.status, body };
}

export function envelopeData<T>(body: unknown, fallback: T): T {
  if (body && typeof body === "object" && "data" in body) {
    return (body as { data: T }).data;
  }
  return fallback;
}

export function paginationCursor(body: unknown): string | undefined {
  if (!body || typeof body !== "object" || !("meta" in body)) {
    return undefined;
  }
  const meta = (body as { meta?: { pagination?: { cursor?: string; hasMore?: boolean } } }).meta;
  if (!meta?.pagination?.hasMore) {
    return undefined;
  }
  return meta.pagination.cursor || undefined;
}

export function errorMessage(body: unknown, fallback: string) {
  if (body && typeof body === "object" && "error" in body) {
    const err = (body as { error?: { message?: string } }).error;
    if (err?.message) {
      return err.message;
    }
  }
  return fallback;
}
