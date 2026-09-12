package com.sentinel.identity.domain;

public class IdentityException extends RuntimeException {
    private final int httpStatus;
    private final String code;
    private final boolean retryable;
    private final String field;

    public IdentityException(int httpStatus, String code, String message, boolean retryable, String field) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
        this.retryable = retryable;
        this.field = field;
    }

    public static IdentityException validation(String field, String message) {
        return new IdentityException(400, "AUTH_VALIDATION_001", message, false, field);
    }

    public static IdentityException userValidation(String field, String message) {
        return new IdentityException(400, "USER_VALIDATION_001", message, false, field);
    }

    public static IdentityException orgValidation(String field, String message) {
        return new IdentityException(400, "ORG_VALIDATION_001", message, false, field);
    }

    public static IdentityException unauthenticated() {
        return new IdentityException(401, "AUTH_AUTHENTICATION_001", "Authentication required", false, null);
    }

    public static IdentityException invalidCredentials() {
        return new IdentityException(401, "AUTH_AUTHENTICATION_001", "Authentication failed", false, null);
    }

    public static IdentityException forbidden() {
        return new IdentityException(403, "AUTHZ_FORBIDDEN_001", "Insufficient permissions", false, null);
    }

    public static IdentityException notFound(String code, String message) {
        return new IdentityException(404, code, message, false, null);
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
