package com.sentinel.dash.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;
import com.sentinel.dash.infrastructure.DashSseHub;
import com.sentinel.dash.infrastructure.DashStore;

@Component
public class DashUpstreamConsumer {

    static final Set<String> TYPES = Set.of(
            "RiskCalculated",
            "AlertCreated",
            "AlertAssigned",
            "AlertClosed",
            "CaseCreated",
            "CaseUpdated",
            "CaseAssigned",
            "AIRecommendationGenerated");

    private final DashStore store;
    private final DashSseHub sse;

    public DashUpstreamConsumer(DashStore store, DashSseHub sse) {
        this.store = store;
        this.sse = sse;
    }

    public String consume(Map<String, Object> envelope) {
        String type = String.valueOf(envelope.getOrDefault("eventType", ""));
        if (!TYPES.contains(type)) {
            return "ignored";
        }
        UUID eventId = UUID.fromString(String.valueOf(envelope.get("eventId")));
        if (!store.markProcessed(eventId)) {
            return "duplicate";
        }
        UUID org = UUID.fromString(String.valueOf(envelope.get("organizationId")));
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = envelope.get("payload") instanceof Map<?, ?> map
                ? (Map<String, Object>) map
                : Map.of();
        Instant exp = Instant.now().plus(24, ChronoUnit.HOURS);
        switch (type) {
            case "AlertCreated", "AlertAssigned" -> {
                String id = String.valueOf(payload.get("alertId"));
                Map<String, Object> row = new LinkedHashMap<>(payload);
                row.put("type", "alert");
                store.upsertProjection(org, "alert:" + id, row, exp);
                sse.publish(org, "alerts", type);
                sse.publish(org, "queues", type);
                sse.publish(org, "workspace", type);
            }
            case "AlertClosed" -> {
                String id = String.valueOf(payload.get("alertId"));
                store.deleteProjection(org, "alert:" + id);
                sse.publish(org, "alerts", type);
                sse.publish(org, "queues", type);
                sse.publish(org, "workspace", type);
            }
            case "CaseCreated", "CaseUpdated", "CaseAssigned" -> {
                String id = String.valueOf(payload.get("caseId"));
                Map<String, Object> row = new LinkedHashMap<>(payload);
                row.put("type", "case");
                store.upsertProjection(org, "case:" + id, row, exp);
                sse.publish(org, "cases", type);
                sse.publish(org, "queues", type);
                sse.publish(org, "workspace", type);
            }
            case "RiskCalculated" -> {
                String id = String.valueOf(payload.get("assessmentId"));
                Map<String, Object> row = new LinkedHashMap<>(payload);
                row.put("type", "assessment");
                store.upsertProjection(org, "assessment:" + id, row, exp);
                sse.publish(org, "workspace", type);
            }
            case "AIRecommendationGenerated" -> {
                String id = String.valueOf(payload.get("recommendationId"));
                Map<String, Object> row = new LinkedHashMap<>(payload);
                row.put("type", "ai");
                store.upsertProjection(org, "ai:" + id, row, exp);
                sse.publish(org, "workspace", type);
            }
            default -> {
            }
        }
        return "processed";
    }
}
