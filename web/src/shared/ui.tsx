import { type ReactNode, useEffect, useRef } from "react";
import { hasPermission } from "./session";

export function Page({
  title,
  state,
  children,
}: {
  title: string;
  state?: string;
  children: ReactNode;
}) {
  const heading = useRef<HTMLHeadingElement>(null);
  useEffect(() => {
    heading.current?.focus();
  }, [title]);
  return (
    <section aria-labelledby="page-title">
      <h1 id="page-title" tabIndex={-1} ref={heading} className="text-xl font-semibold text-slate-100 outline-none">
        {title}
      </h1>
      {state ? (
        <p className="mt-2 text-sm text-slate-400" role="status" aria-live="polite">
          {state}
        </p>
      ) : null}
      <div className="mt-4">{children}</div>
    </section>
  );
}

export function Layer({ kind, children }: { kind: "SYSTEM" | "AI" | "EVIDENCE" | "HUMAN"; children: ReactNode }) {
  const label =
    kind === "SYSTEM"
      ? "SYSTEM RESULT"
      : kind === "AI"
        ? "AI INTERPRETATION"
        : kind === "EVIDENCE"
          ? "SOURCE EVIDENCE"
          : "HUMAN DECISION";
  return (
    <div className="rounded border border-slate-700 p-3" data-layer={kind}>
      <p className="text-xs font-semibold uppercase tracking-wide text-amber-400">{label}</p>
      <div className="mt-1 text-sm text-slate-200">{children}</div>
    </div>
  );
}

export function StatusBadge({ label }: { label: string }) {
  return (
    <span className="inline-flex items-center gap-1 rounded border border-slate-500 px-2 py-0.5 text-xs">
      <span aria-hidden="true">●</span>
      <span>{label}</span>
    </span>
  );
}

export function PermissionGuard({
  permission,
  children,
}: {
  permission: string;
  children: ReactNode;
}) {
  if (!hasPermission(permission)) {
    return null;
  }
  return children;
}

export function ConfirmDialog({
  open,
  title,
  children,
  confirmLabel,
  onConfirm,
  onCancel,
}: {
  open: boolean;
  title: string;
  children: ReactNode;
  confirmLabel: string;
  onConfirm: () => void;
  onCancel: () => void;
}) {
  const first = useRef<HTMLButtonElement>(null);
  useEffect(() => {
    if (open) {
      first.current?.focus();
    }
  }, [open]);
  if (!open) {
    return null;
  }
  function onKey(event: KeyboardEvent) {
    if (event.key === "Escape") {
      onCancel();
    }
  }
  return (
    <div className="fixed inset-0 z-20 flex items-center justify-center bg-black/60 p-4" onKeyDown={onKey}>
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="dialog-title"
        className="w-full max-w-md rounded border border-slate-600 bg-slate-900 p-4"
      >
        <h2 id="dialog-title" className="text-lg font-semibold">
          {title}
        </h2>
        <div className="mt-3 text-sm">{children}</div>
        <div className="mt-4 flex justify-end gap-2">
          <button ref={first} type="button" className="min-h-11 rounded bg-slate-700 px-3 py-2" onClick={onCancel}>
            Cancel
          </button>
          <button type="button" className="min-h-11 rounded bg-amber-500 px-3 py-2 text-slate-950" onClick={onConfirm}>
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}

export function Field({
  id,
  label,
  error,
  children,
}: {
  id: string;
  label: string;
  error?: string;
  children: ReactNode;
}) {
  return (
    <div className="space-y-1">
      <label htmlFor={id} className="block text-sm">
        {label}
      </label>
      {children}
      {error ? (
        <p id={`${id}-error`} className="text-sm text-red-400" role="alert">
          {error}
        </p>
      ) : null}
    </div>
  );
}

