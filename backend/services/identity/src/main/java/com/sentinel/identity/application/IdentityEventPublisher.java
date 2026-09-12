package com.sentinel.identity.application;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.sentinel.common.outbox.TransactionalOutbox;
import com.sentinel.identity.api.IdentityContext;
import com.sentinel.identity.infrastructure.IdentityStore;

@Service
public class IdentityEventPublisher {

    public static final String PRODUCER = "sentinel-identity";

    private final TransactionalOutbox outbox;
    private final IdentityStore store;

    public IdentityEventPublisher(TransactionalOutbox outbox, IdentityStore store) {
        this.outbox = outbox;
        this.store = store;
    }

    public void userLoggedIn(UUID userId, UUID sessionId, UUID organizationId, Instant at) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", userId.toString());
        payload.put("sessionId", sessionId.toString());
        payload.put("organizationId", organizationId.toString());
        payload.put("loggedInAt", at.toString());
        payload.put("deviceId", null);
        publish("UserLoggedIn", organizationId, payload, sessionId + ":" + at);
    }

    public void sessionExpired(UUID sessionId, UUID userId, UUID organizationId, Instant at, String reason) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionId", sessionId.toString());
        payload.put("userId", userId.toString());
        payload.put("expiredAt", at.toString());
        payload.put("reason", reason);
        publish("SessionExpired", organizationId, payload, sessionId + ":" + at);
    }

    public void userUpdated(UUID userId, UUID organizationId, String status, Instant at, UUID updatedBy, java.util.List<String> fields) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", userId.toString());
        payload.put("organizationId", organizationId.toString());
        payload.put("status", status);
        payload.put("updatedAt", at.toString());
        payload.put("updatedBy", updatedBy == null ? null : updatedBy.toString());
        payload.put("changeFields", fields);
        publish("UserUpdated", organizationId, payload, userId + ":" + at);
    }

    private void publish(String type, UUID organizationId, Map<String, Object> payload, String idempotencyKey) {
        IdentityContext ctx = IdentityContext.get();
        UUID eventId = UUID.randomUUID();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("classification", "internal");
        metadata.put("idempotencyKey", idempotencyKey);
        if (ctx.actorId() != null) {
            metadata.put("actorId", ctx.actorId().toString());
        }
        metadata.put("actorType", "user");
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
    }
}
