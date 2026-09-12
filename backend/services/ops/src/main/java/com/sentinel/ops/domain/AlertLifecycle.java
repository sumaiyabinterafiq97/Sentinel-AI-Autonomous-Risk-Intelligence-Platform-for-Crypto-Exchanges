package com.sentinel.ops.domain;

import java.util.Set;

/** Documented ALERT lifecycle: open → triaged → assigned → closed. */
public final class AlertLifecycle {

    public static final String OPEN = "open";
    public static final String TRIAGED = "triaged";
    public static final String ASSIGNED = "assigned";
    public static final String CLOSED = "closed";

    private static final Set<String> PATCH_TARGETS = Set.of(OPEN, TRIAGED);

    private AlertLifecycle() {}

    public static void requirePatchable(String current, String target) {
        if (CLOSED.equals(current)) {
            throw AlertException.conflict("Closed alerts cannot change status via patch");
        }
        if (target == null || target.isBlank()) {
            throw AlertException.validation("status", "status is required");
        }
        String next = target.trim().toLowerCase();
        if (ASSIGNED.equals(next)) {
            throw AlertException.validation("status", "Use assign to move an alert to assigned");
        }
        if (CLOSED.equals(next)) {
            throw AlertException.validation("status", "Use close to move an alert to closed");
        }
        if (!PATCH_TARGETS.contains(next)) {
            throw AlertException.validation("status", "status must be open or triaged");
        }
        if (current.equals(next)) {
            return;
        }
        if (OPEN.equals(current) && TRIAGED.equals(next)) {
            return;
        }
        throw AlertException.validation("status", "Invalid status transition from " + current + " to " + next);
    }

    public static void requireAssignable(String current) {
        if (CLOSED.equals(current)) {
            throw AlertException.conflict("Closed alerts cannot be assigned");
        }
        if (!OPEN.equals(current) && !TRIAGED.equals(current) && !ASSIGNED.equals(current)) {
            throw AlertException.validation("status", "Alert cannot be assigned from " + current);
        }
    }

    public static void requireCloseable(String current) {
        if (CLOSED.equals(current)) {
            throw AlertException.conflict("Alert is already closed");
        }
        if (!OPEN.equals(current) && !TRIAGED.equals(current) && !ASSIGNED.equals(current)) {
            throw AlertException.validation("status", "Alert cannot be closed from " + current);
        }
    }

    public static boolean mayApplySystemPriority(String current) {
        return OPEN.equals(current) || TRIAGED.equals(current);
    }
}
