package com.sentinel.platform.api;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.sentinel.platform.domain.CoreException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private final ApiErrorWriter writer;

    public ApiExceptionHandler(ApiErrorWriter writer) {
        this.writer = writer;
    }

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<Map<String, Object>> handleCore(CoreException ex) {
        return ResponseEntity.status(ex.httpStatus()).body(writer.body(ex));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return handleCore(CoreException.validation("body", "Request body is invalid JSON"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleType(MethodArgumentTypeMismatchException ex) {
        return handleCore(CoreException.validation(ex.getName(), "Invalid parameter"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnknown(Exception ex) {
        return handleCore(CoreException.internal());
    }

    public static Map<String, Object> success(Object data, Integer limit, String cursor, Boolean hasMore) {
        RequestContext ctx = RequestContext.get();
        Map<String, Object> meta = new LinkedHashMap<>();
        UUID requestId = ctx.requestId() == null ? UUID.randomUUID() : ctx.requestId();
        meta.put("requestId", requestId.toString());
        meta.put("correlationId", ctx.correlationId() == null ? null : ctx.correlationId().toString());
        meta.put("timestamp", Instant.now().toString());
        if (limit != null) {
            Map<String, Object> pagination = new LinkedHashMap<>();
            pagination.put("cursor", cursor);
            pagination.put("hasMore", hasMore);
            pagination.put("limit", limit);
            meta.put("pagination", pagination);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("data", data);
        body.put("meta", meta);
        return body;
    }
}
