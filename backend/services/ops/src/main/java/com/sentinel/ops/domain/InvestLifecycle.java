package com.sentinel.ops.domain;

/** Documented INVEST lifecycle: open → in_progress → pending_review → closed. */
public final class InvestLifecycle {

    public static final String OPEN = "open";
    public static final String IN_PROGRESS = "in_progress";
    public static final String PENDING_REVIEW = "pending_review";
    public static final String CLOSED = "closed";

    private InvestLifecycle() {}

    public static int rank(String status) {
        return switch (status) {
            case OPEN -> 0;
            case IN_PROGRESS -> 1;
            case PENDING_REVIEW -> 2;
            case CLOSED -> 3;
            default -> -1;
        };
    }

    public static void requirePatchable(String current, String target) {
        if (CLOSED.equals(current)) {
            throw InvestException.conflict("Closed cases cannot change status via patch");
        }
        if (target == null || target.isBlank()) {
            throw InvestException.validation("status", "status is required");
        }
        String next = target.trim().toLowerCase();
        if (CLOSED.equals(next)) {
            throw InvestException.validation("status", "Use close to move a case to closed");
        }
        if (rank(next) < 0 || rank(next) > 2) {
            throw InvestException.validation("status", "status must be open, in_progress, or pending_review");
        }
        if (current.equals(next)) {
            return;
        }
        if (rank(next) >= rank(current)) {
            return;
        }
        throw InvestException.validation("status", "Invalid status transition from " + current + " to " + next);
    }

    public static void requireAssignable(String current) {
        if (CLOSED.equals(current)) {
            throw InvestException.conflict("Closed cases cannot be assigned");
        }
    }

    public static void requireCloseable(String current) {
        if (CLOSED.equals(current)) {
            throw InvestException.conflict("Case is already closed");
        }
    }
}
