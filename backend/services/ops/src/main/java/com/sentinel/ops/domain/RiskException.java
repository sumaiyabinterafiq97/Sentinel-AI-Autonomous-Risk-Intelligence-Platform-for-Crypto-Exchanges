package com.sentinel.ops.domain;

public class RiskException extends RuntimeException {

    private final int httpStatus;
    private final String code;
    private final boolean retryable;
    private final String field;

    public RiskException(int httpStatus, String code, String message, boolean retryable, String field) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
        this.retryable = retryable;
        this.field = field;
    }

    public static RiskException validation(String field, String message) {
        return new RiskException(400, "RISK_VALIDATION_001", message, false, field);
    }

    public static RiskException unauthenticated() {
        return new RiskException(401, "AUTH_AUTHENTICATION_001", "Authentication required", false, null);
    }

    public static RiskException forbidden() {
        return new RiskException(403, "AUTHZ_FORBIDDEN_001", "Insufficient permissions", false, null);
    }

    public static RiskException notFound(String message) {
        return new RiskException(404, "RISK_NOT_FOUND_001", message, false, null);
    }

    public static RiskException internal() {
        return new RiskException(500, "PLATFORM_INTERNAL_001", "An internal error occurred", true, null);
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
