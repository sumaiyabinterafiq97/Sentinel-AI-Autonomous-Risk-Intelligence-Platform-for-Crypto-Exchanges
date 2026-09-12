package com.sentinel.dash.domain;

public class DashException extends RuntimeException {

    private final int httpStatus;
    private final String code;
    private final boolean retryable;
    private final String field;

    public DashException(int httpStatus, String code, String message, boolean retryable, String field) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
        this.retryable = retryable;
        this.field = field;
    }

    public static DashException validation(String field, String message) {
        return new DashException(400, "DASH_VALIDATION_001", message, false, field);
    }

    public static DashException unauthenticated() {
        return new DashException(401, "AUTH_AUTHENTICATION_001", "Authentication required", false, null);
    }

    public static DashException forbidden() {
        return new DashException(403, "AUTHZ_FORBIDDEN_001", "Insufficient permissions", false, null);
    }

    public static DashException notFound(String message) {
        return new DashException(404, "DASH_NOT_FOUND_001", message, false, null);
    }

    public static DashException dependency(String message) {
        return new DashException(503, "DASH_DEPENDENCY_001", message, true, null);
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
