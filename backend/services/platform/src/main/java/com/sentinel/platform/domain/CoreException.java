package com.sentinel.platform.domain;

public class CoreException extends RuntimeException {

    private final int httpStatus;
    private final String code;
    private final boolean retryable;
    private final String field;

    public CoreException(int httpStatus, String code, String message, boolean retryable, String field) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
        this.retryable = retryable;
        this.field = field;
    }

    public static CoreException validation(String field, String message) {
        return new CoreException(400, "CORE_VALIDATION_001", message, false, field);
    }

    public static CoreException unauthenticated() {
        return new CoreException(401, "AUTH_AUTHENTICATION_001", "Authentication required", false, null);
    }

    public static CoreException forbidden() {
        return new CoreException(403, "AUTHZ_FORBIDDEN_001", "Insufficient permissions", false, null);
    }

    public static CoreException dependency(String message) {
        return new CoreException(503, "CORE_DEPENDENCY_001", message, true, null);
    }

    public static CoreException adminValidation(String field, String message) {
        return new CoreException(400, "ADMIN_VALIDATION_001", message, false, field);
    }

    public static CoreException adminNotFound(String message) {
        return new CoreException(404, "ADMIN_NOT_FOUND_001", message, false, null);
    }

    public static CoreException adminDependency(String message) {
        return new CoreException(503, "ADMIN_DEPENDENCY_001", message, true, null);
    }

    public static CoreException internal() {
        return new CoreException(500, "PLATFORM_INTERNAL_001", "An internal error occurred", true, null);
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
