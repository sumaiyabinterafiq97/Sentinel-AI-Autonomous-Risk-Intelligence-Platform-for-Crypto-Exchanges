package com.sentinel.ops.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.sentinel.common.outbox.TransactionalOutbox;
import com.sentinel.ops.api.RiskContext;

@Service
public class RiskEventPublisher {

    public static final String PRODUCER = "sentinel-ops";

    private final TransactionalOutbox outbox;
    private final com.sentinel.ops.infrastructure.RiskStore store;

    public RiskEventPublisher(TransactionalOutbox outbox, com.sentinel.ops.infrastructure.RiskStore store) {
        this.outbox = outbox;
        this.store = store;
    }

    public UUID riskCalculated(
            UUID assessmentId,
            UUID organizationId,
            String entityType,
            String entityId,
            java.math.BigDecimal score,
            String riskLevel,
            String explanation,
            Instant evaluatedAt,
            String transactionRef,
            List<Map<String, Object>> ruleHits) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("assessmentId", assessmentId.toString());
        payload.put("entityType", entityType);
        payload.put("entityId", entityId);
        payload.put("score", score);
        payload.put("riskLevel", riskLevel);
        payload.put("explanationSummary", explanation);
        payload.put("ruleHits", ruleHits);
        payload.put("evaluatedAt", evaluatedAt.toString());
        payload.put("transactionRef", transactionRef);
        return publish("RiskCalculated", organizationId, payload, "confidential", assessmentId.toString());
    }

    public UUID highRiskDetected(
            UUID assessmentId,
            UUID organizationId,
            String entityType,
            String entityId,
            java.math.BigDecimal score,
            Instant detectedAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("assessmentId", assessmentId.toString());
        payload.put("entityType", entityType);
        payload.put("entityId", entityId);
        payload.put("score", score);
        payload.put("detectedAt", detectedAt.toString());
        payload.put("prioritySignal", score);
        return publish("HighRiskDetected", organizationId, payload, "internal", assessmentId + ":high");
    }

    private UUID publish(
            String type, UUID organizationId, Map<String, Object> payload, String classification, String idempotencyKey) {
        RiskContext ctx = RiskContext.get();
        UUID eventId = UUID.randomUUID();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("classification", classification);
        metadata.put("idempotencyKey", idempotencyKey);
        if (ctx.actorId() != null) {
            metadata.put("actorId", ctx.actorId().toString());
        }
        metadata.put("actorType", ctx.actorType() == null ? "service" : ctx.actorType());
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("eventId", eventId.toString());
        envelope.put("eventType", type);
        envelope.put("schemaVersion", "1.0");
        envelope.put("timestamp", Instant.now().toString());
        envelope.put("producer", PRODUCER);
        envelope.put("correlationId", ctx.correlationId() == null ? UUID.randomUUID().toString() : ctx.correlationId().toString());
        envelope.put("causationId", ctx.requestId() == null ? null : ctx.requestId().toString());
        envelope.put("organizationId", organizationId.toString());
        envelope.put("payload", payload);
        envelope.put("metadata", metadata);
        outbox.record(envelope);
        store.insertAudit(
                organizationId,
                ctx.actorId(),
                "EVENT_PUBLISHED",
                "EVENT",
                eventId,
                "SUCCESS",
                ctx.correlationId(),
                ctx.requestId(),
                "{\"eventType\":\"" + type + "\"}",
                Instant.now());
        return eventId;
    }
}
