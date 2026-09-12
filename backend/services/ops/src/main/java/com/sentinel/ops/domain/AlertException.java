package com.sentinel.ops.domain;

public class AlertException extends RuntimeException {

    private final int httpStatus;
    private final String code;
    private final boolean retryable;
    private final String field;

    public AlertException(int httpStatus, String code, String message, boolean retryable, String field) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
        this.retryable = retryable;
        this.field = field;
    }

    public static AlertException validation(String field, String message) {
        return new AlertException(400, "ALERT_VALIDATION_001", message, false, field);
    }

    public static AlertException unauthenticated() {
        return new AlertException(401, "AUTH_AUTHENTICATION_001", "Authentication required", false, null);
    }

    public static AlertException forbidden() {
        return new AlertException(403, "AUTHZ_FORBIDDEN_001", "Insufficient permissions", false, null);
    }

    public static AlertException notFound(String message) {
        return new AlertException(404, "ALERT_NOT_FOUND_001", message, false, null);
    }

    public static AlertException conflict(String message) {
        return new AlertException(409, "ALERT_CONFLICT_001", message, false, null);
    }

    public static AlertException internal() {
        return new AlertException(500, "PLATFORM_INTERNAL_001", "An internal error occurred", true, null);
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
