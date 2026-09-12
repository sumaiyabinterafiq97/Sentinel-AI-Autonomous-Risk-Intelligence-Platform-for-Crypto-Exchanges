package com.sentinel.ops.domain;

public class CompException extends RuntimeException {

    private final int httpStatus;
    private final String code;
    private final boolean retryable;
    private final String field;

    public CompException(int httpStatus, String code, String message, boolean retryable, String field) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
        this.retryable = retryable;
        this.field = field;
    }

    public static CompException validation(String field, String message) {
        return new CompException(400, "COMP_VALIDATION_001", message, false, field);
    }

    public static CompException unauthenticated() {
        return new CompException(401, "AUTH_AUTHENTICATION_001", "Authentication required", false, null);
    }

    public static CompException forbidden() {
        return new CompException(403, "AUTHZ_FORBIDDEN_001", "Insufficient permissions", false, null);
    }

    public static CompException notFound(String message) {
        return new CompException(404, "COMP_NOT_FOUND_001", message, false, null);
    }

    public static CompException conflict(String message) {
        return new CompException(409, "COMP_CONFLICT_001", message, false, null);
    }

    public int httpStatus() {
        return httpStatus;
    }

    public String code() {
        return code;
    }

    public boolean retryable() {
        return retryable;
    }

    public String field() {
        return field;
    }
}
