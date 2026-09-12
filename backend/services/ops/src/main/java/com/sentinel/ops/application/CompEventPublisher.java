package com.sentinel.ops.application;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.sentinel.common.outbox.TransactionalOutbox;
import com.sentinel.ops.api.RiskContext;
import com.sentinel.ops.infrastructure.RiskStore;

@Service
public class CompEventPublisher {

    public static final String PRODUCER = "sentinel-ops";

    private final TransactionalOutbox outbox;
    private final RiskStore audits;

    public CompEventPublisher(TransactionalOutbox outbox, RiskStore audits) {
        this.outbox = outbox;
        this.audits = audits;
    }

    public UUID complianceReviewed(
            UUID reviewId, UUID organizationId, String reviewType, String outcome, UUID reviewedBy, Instant reviewedAt, String subjectRef) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reviewId", reviewId.toString());
        payload.put("reviewType", reviewType);
        payload.put("outcome", outcome);
        if (reviewedBy != null) {
            payload.put("reviewedBy", reviewedBy.toString());
        }
        payload.put("reviewedAt", reviewedAt.toString());
        if (subjectRef != null) {
            payload.put("subjectRef", subjectRef);
        }
        return publish("ComplianceReviewed", organizationId, payload, "restricted", reviewId + ":" + reviewType);
    }

    public UUID travelRuleValidated(
            UUID validationId, UUID organizationId, String transactionRef, String status, Map<String, Object> result, Instant validatedAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("validationId", validationId.toString());
        payload.put("transactionRef", transactionRef);
        payload.put("status", status);
        payload.put("validationResult", result);
        payload.put("validatedAt", validatedAt.toString());
        return publish("TravelRuleValidated", organizationId, payload, "internal", validationId.toString());
    }

    public UUID sanctionsHitDetected(
            UUID screeningId, UUID organizationId, String subjectRef, String matchStatus, String disposition, Instant screenedAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("screeningId", screeningId.toString());
        payload.put("subjectRef", subjectRef);
        payload.put("matchStatus", matchStatus);
        payload.put("disposition", disposition);
        payload.put("screenedAt", screenedAt.toString());
        return publish("SanctionsHitDetected", organizationId, payload, "restricted", screeningId.toString());
    }

    public UUID auditPackagePrepared(
            UUID packageId, UUID organizationId, String packageType, String status, List<String> artifacts, Instant preparedAt, UUID preparedBy) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("packageId", packageId.toString());
        payload.put("packageType", packageType);
        payload.put("status", status);
        payload.put("artifactRefs", artifacts);
        payload.put("preparedAt", preparedAt.toString());
        payload.put("preparedBy", preparedBy == null ? null : preparedBy.toString());
        return publish("AuditPackagePrepared", organizationId, payload, "internal", packageId.toString());
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
        envelope.put(
                "correlationId",
                ctx.correlationId() == null ? UUID.randomUUID().toString() : ctx.correlationId().toString());
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
