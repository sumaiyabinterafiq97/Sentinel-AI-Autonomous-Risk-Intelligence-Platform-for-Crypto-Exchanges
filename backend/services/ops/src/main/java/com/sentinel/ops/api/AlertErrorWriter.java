package com.sentinel.ops.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import com.sentinel.ops.domain.AlertException;

@Component
public class AlertErrorWriter {

    public Map<String, Object> body(AlertException ex) {
        RiskContext ctx = RiskContext.get();
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
}
