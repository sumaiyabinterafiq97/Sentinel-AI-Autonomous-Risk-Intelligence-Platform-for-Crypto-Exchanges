package com.sentinel.platform.application;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.sentinel.platform.api.RequestContext;
import com.sentinel.platform.domain.CoreKeys;
import com.sentinel.platform.infrastructure.AuditRecordRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.common.outbox.TransactionalOutbox;

@Service
public class CoreEventPublisher {

    private final TransactionalOutbox outbox;
    private final AuditRecordRepository audit;
    private final ObjectMapper objectMapper;

    public CoreEventPublisher(
            TransactionalOutbox outbox, AuditRecordRepository audit, ObjectMapper objectMapper) {
        this.outbox = outbox;
        this.audit = audit;
        this.objectMapper = objectMapper;
    }

    public void publishConfigurationUpdated(
            String configKey, UUID organizationId, int version, Instant updatedAt, UUID updatedBy) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("configKey", configKey);
        payload.put("organizationId", organizationId == null ? null : organizationId.toString());
        payload.put("version", version);
        payload.put("updatedAt", updatedAt.toString());
        payload.put("updatedBy", updatedBy == null ? null : updatedBy.toString());
        publish("ConfigurationUpdated", organizationId, payload, configKey + ":" + version);
    }

    public void publishFeatureFlagChanged(
            String flagKey, UUID organizationId, boolean enabled, Instant updatedAt, UUID updatedBy) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("flagKey", flagKey);
        payload.put("organizationId", organizationId == null ? null : organizationId.toString());
        payload.put("enabled", enabled);
        payload.put("updatedAt", updatedAt.toString());
        payload.put("updatedBy", updatedBy == null ? null : updatedBy.toString());
        publish("FeatureFlagChanged", organizationId, payload, flagKey + ":" + updatedAt);
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
        metadata.put("actorType", ctx.actorType() == null ? "service" : ctx.actorType());

        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("eventId", eventId.toString());
        envelope.put("eventType", eventType);
        envelope.put("schemaVersion", CoreKeys.EVENT_SCHEMA_VERSION);
        envelope.put("timestamp", Instant.now().toString());
        envelope.put("producer", CoreKeys.PRODUCER);
        envelope.put("correlationId", ctx.correlationId() == null ? UUID.randomUUID().toString() : ctx.correlationId().toString());
        envelope.put("causationId", ctx.requestId() == null ? null : ctx.requestId().toString());
        envelope.put("organizationId", organizationId.toString());
        envelope.put("payload", payload);
        envelope.put("metadata", metadata);

        outbox.record(envelope);
        audit.insert(
                UUID.randomUUID(),
                organizationId,
                ctx.actorId(),
                ctx.actorType() == null ? "service" : ctx.actorType(),
                "EVENT_PUBLISHED",
                "EVENT",
                eventId,
                "SUCCESS",
                ctx.correlationId(),
                ctx.requestId(),
                json(Map.of("eventType", eventType, "outcome", "SUCCESS")),
                Instant.now());
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
