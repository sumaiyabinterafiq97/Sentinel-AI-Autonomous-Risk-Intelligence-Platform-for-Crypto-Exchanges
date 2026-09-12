package com.sentinel.dash.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import com.sentinel.dash.domain.DashException;

@Component
public class DashErrorWriter {

    public Map<String, Object> body(DashException ex) {
        DashContext ctx = DashContext.get();
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", ex.code());
        error.put("message", ex.getMessage());
        error.put("requestId", ctx.requestId() == null ? null : ctx.requestId().toString());
        error.put("correlationId", ctx.correlationId() == null ? null : ctx.correlationId().toString());
        error.put("timestamp", Instant.now().toString());
        error.put("retryable", ex.retryable());
        if (ex.field() != null) {
            List<Map<String, String>> details = new ArrayList<>();
            details.add(Map.of("field", ex.field(), "message", ex.getMessage()));
            error.put("details", details);
        }
        return Map.of("error", error);
    }

    public static Map<String, Object> success(Object data, Integer limit, String cursor, Boolean hasMore) {
        DashContext ctx = DashContext.get();
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("requestId", ctx.requestId() == null ? UUID.randomUUID().toString() : ctx.requestId().toString());
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
