package com.sentinel.ops.application;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.sentinel.common.outbox.TransactionalOutbox;
import com.sentinel.ops.api.RiskContext;
import com.sentinel.ops.infrastructure.RiskStore;

@Service
public class AlertEventPublisher {

    public static final String PRODUCER = "sentinel-ops";

    private final TransactionalOutbox outbox;
    private final RiskStore audits;

    public AlertEventPublisher(TransactionalOutbox outbox, RiskStore audits) {
        this.outbox = outbox;
        this.audits = audits;
    }

    public UUID alertCreated(
            UUID alertId,
            UUID organizationId,
            String status,
            int priority,
            UUID riskAssessmentId,
            String title,
            Instant createdAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("alertId", alertId.toString());
        payload.put("status", status);
        payload.put("priority", priority);
        payload.put("riskAssessmentId", riskAssessmentId == null ? null : riskAssessmentId.toString());
        payload.put("title", title);
        payload.put("createdAt", createdAt.toString());
        return publish("AlertCreated", organizationId, payload, "confidential", alertId.toString());
    }

    public UUID alertAssigned(UUID alertId, UUID organizationId, UUID assignedTo, UUID assignedBy, Instant assignedAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("alertId", alertId.toString());
        payload.put("assignedTo", assignedTo.toString());
        if (assignedBy != null) {
            payload.put("assignedBy", assignedBy.toString());
        }
        payload.put("assignedAt", assignedAt.toString());
        return publish("AlertAssigned", organizationId, payload, "internal", alertId + ":assigned");
    }

    public UUID alertClosed(
            UUID alertId, UUID organizationId, Instant closedAt, UUID closedBy, String dispositionReason) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("alertId", alertId.toString());
        payload.put("closedAt", closedAt.toString());
        payload.put("closedBy", closedBy == null ? UUID.fromString("00000000-0000-4000-8000-000000000000").toString() : closedBy.toString());
        if (dispositionReason != null) {
            payload.put("dispositionReason", dispositionReason);
        }
        return publish("AlertClosed", organizationId, payload, "internal", alertId + ":closed");
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
        audits.insertAudit(
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
