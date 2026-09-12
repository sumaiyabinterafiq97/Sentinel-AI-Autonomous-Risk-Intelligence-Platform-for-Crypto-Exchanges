const TOKEN_KEY = "sentinel.accessToken";
const ORG_KEY = "sentinel.organizationId";

export function saveSession(token: string, organizationId: string) {
  sessionStorage.setItem(TOKEN_KEY, token);
  sessionStorage.setItem(ORG_KEY, organizationId);
}

export function clearSession() {
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(ORG_KEY);
}

export function getToken() {
  return sessionStorage.getItem(TOKEN_KEY);
}

export function getOrg() {
  return sessionStorage.getItem(ORG_KEY);
}

function payload(): Record<string, unknown> {
  const token = getToken();
  if (!token || token.split(".").length !== 3) {
    return {};
  }
  try {
    return JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/"))) as Record<string, unknown>;
  } catch {
    return {};
  }
}

export function permissions(): string[] {
  const json = payload();
  return Array.isArray(json.perms) ? (json.perms as string[]) : [];
}

export function hasPermission(code: string) {
  return permissions().includes(code);
}

export function hasAnyPermission(...codes: string[]) {
  return codes.some((code) => hasPermission(code));
}

export function currentUserId() {
  const sub = payload().sub;
  return typeof sub === "string" ? sub : undefined;
}
