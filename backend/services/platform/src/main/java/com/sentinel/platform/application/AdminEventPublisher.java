package com.sentinel.platform.application;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.common.outbox.TransactionalOutbox;
import com.sentinel.platform.api.RequestContext;
import com.sentinel.platform.domain.CoreKeys;

@Service
public class AdminEventPublisher {

    public static final String PRODUCER = "ADMIN";

    private final TransactionalOutbox outbox;
    private final ObjectMapper objectMapper;

    public AdminEventPublisher(TransactionalOutbox outbox, ObjectMapper objectMapper) {
        this.outbox = outbox;
        this.objectMapper = objectMapper;
    }

    public void publishSettingUpdated(String settingKey, UUID organizationId, Instant updatedAt, UUID updatedBy) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("settingKey", settingKey);
        payload.put("organizationId", organizationId.toString());
        payload.put("updatedAt", updatedAt.toString());
        payload.put("updatedBy", updatedBy == null ? null : updatedBy.toString());
        publish("AdminSettingUpdated", organizationId, payload, settingKey + ":" + updatedAt);
    }

    public void publishIntegrationConfigured(
            UUID integrationId,
            String integrationType,
            String status,
            Instant configuredAt,
            UUID configuredBy,
            UUID organizationId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("integrationId", integrationId.toString());
        payload.put("integrationType", integrationType);
        payload.put("status", status);
        payload.put("configuredAt", configuredAt.toString());
        payload.put("configuredBy", configuredBy == null ? null : configuredBy.toString());
        publish("IntegrationConfigured", organizationId, payload, integrationId.toString());
    }

    public void publishActionPerformed(
            String action, String targetType, UUID targetId, UUID actorId, Instant performedAt, String outcome, UUID organizationId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("action", action);
        payload.put("targetType", targetType);
        payload.put("targetId", targetId == null ? null : targetId.toString());
        payload.put("actorId", actorId == null ? null : actorId.toString());
        payload.put("performedAt", performedAt.toString());
        payload.put("outcome", outcome);
        publish("AdminActionPerformed", organizationId, payload, action + ":" + performedAt);
    }

    private void publish(String eventType, UUID organizationId, Map<String, Object> payload, String idempotencyKey) {
        RequestContext ctx = RequestContext.get();
        UUID eventId = UUID.randomUUID();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("classification", "internal");
        metadata.put("idempotencyKey", idempotencyKey);
        if (ctx.actorId() != null) {
            metadata.put("actorId", ctx.actorId().toString());
        }
        metadata.put("actorType", ctx.actorType() == null ? "user" : ctx.actorType());

        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("eventId", eventId.toString());
        envelope.put("eventType", eventType);
        envelope.put("schemaVersion", CoreKeys.EVENT_SCHEMA_VERSION);
        envelope.put("timestamp", Instant.now().toString());
        envelope.put("producer", PRODUCER);
        envelope.put(
                "correlationId",
                ctx.correlationId() == null ? UUID.randomUUID().toString() : ctx.correlationId().toString());
        envelope.put("causationId", ctx.requestId() == null ? null : ctx.requestId().toString());
        envelope.put("organizationId", organizationId.toString());
        envelope.put("payload", payload);
        envelope.put("metadata", metadata);
        outbox.record(envelope);
        unused(objectMapper);
    }

    private static void unused(ObjectMapper objectMapper) {
        // retained for stable JSON encoding if envelope validation is added later
        if (objectMapper == null) {
            throw new IllegalStateException("objectMapper required");
        }
    }
}
