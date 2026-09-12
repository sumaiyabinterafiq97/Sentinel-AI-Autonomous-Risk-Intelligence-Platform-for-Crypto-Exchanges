import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { api } from "../shared/api";
import { clearSession, getOrg, hasAnyPermission, hasPermission } from "../shared/session";

const items = [
  { to: "/workspace", label: "Overview", show: () => hasPermission("dash:workspace:read") },
  { to: "/alerts", label: "Alerts", show: () => hasAnyPermission("dash:queue:read", "alert:alert:read") },
  { to: "/investigations", label: "Investigations", show: () => hasAnyPermission("dash:queue:read", "invest:case:read") },
  { to: "/risk/rules", label: "Risk", show: () => hasAnyPermission("dash:workspace:read", "risk:rule:read", "risk:assessment:read") },
  { to: "/compliance", label: "Compliance", show: () => hasAnyPermission("comp:kyc:write", "comp:aml:write", "comp:audit:write", "dash:workspace:read") },
  { to: "/search", label: "Search", show: () => hasPermission("dash:workspace:read") },
  {
    to: "/admin/users",
    label: "Administration",
    show: () => hasAnyPermission("admin:user:provision", "admin:settings:write", "admin:audit:read", "dash:workspace:read"),
  },
];

export function Shell() {
  const navigate = useNavigate();
  const org = getOrg();
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100">
      <a href="#main" className="sr-only focus:not-sr-only focus:absolute focus:left-2 focus:top-2">
        Skip to content
      </a>
      <header className="border-b border-slate-800 px-4 py-3" role="banner">
        <p className="text-sm font-semibold">Sentinel AI</p>
        <p className="font-mono text-xs text-slate-400">Org {org}</p>
      </header>
      <div className="flex">
        <nav aria-label="Primary" className="w-56 border-r border-slate-800 p-3 text-sm">
          <ul className="space-y-1">
            {items
              .filter((item) => item.show())
              .map((item) => (
                <li key={item.to}>
                  <NavLink
                    to={item.to}
                    className={({ isActive }) =>
                      `block min-h-11 rounded px-2 py-2 focus:outline focus:outline-2 focus:outline-amber-400 ${
                        isActive ? "bg-slate-800" : "hover:bg-slate-900"
                      }`
                    }
                  >
                    {item.label}
                  </NavLink>
                </li>
              ))}
          </ul>
          <button
            type="button"
            className="mt-6 min-h-11 text-xs text-slate-400 underline"
            onClick={async () => {
              await api("/v1/auth/logout", { method: "POST" });
              clearSession();
              navigate("/login");
            }}
          >
            Sign out
          </button>
        </nav>
        <main id="main" className="flex-1 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
