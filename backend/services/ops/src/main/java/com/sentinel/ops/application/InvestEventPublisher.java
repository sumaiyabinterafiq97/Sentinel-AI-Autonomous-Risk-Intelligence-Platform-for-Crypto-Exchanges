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
public class InvestEventPublisher {

    public static final String PRODUCER = "sentinel-ops";

    private final TransactionalOutbox outbox;
    private final RiskStore audits;

    public InvestEventPublisher(TransactionalOutbox outbox, RiskStore audits) {
        this.outbox = outbox;
        this.audits = audits;
    }

    public UUID caseCreated(
            UUID caseId,
            UUID organizationId,
            String status,
            String title,
            UUID sourceAlertId,
            Integer priority,
            Instant createdAt,
            UUID createdBy) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("caseId", caseId.toString());
        payload.put("status", status);
        payload.put("title", title);
        payload.put("sourceAlertId", sourceAlertId == null ? null : sourceAlertId.toString());
        if (priority != null) {
            payload.put("priority", priority);
        }
        payload.put("createdAt", createdAt.toString());
        if (createdBy != null) {
            payload.put("createdBy", createdBy.toString());
        }
        return publish("CaseCreated", organizationId, payload, "restricted", caseId.toString());
    }

    public UUID caseUpdated(UUID caseId, UUID organizationId, String status, Instant updatedAt, UUID updatedBy, String summary) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("caseId", caseId.toString());
        payload.put("status", status);
        payload.put("updatedAt", updatedAt.toString());
        if (updatedBy != null) {
            payload.put("updatedBy", updatedBy.toString());
        }
        payload.put("changeSummary", summary);
        return publish("CaseUpdated", organizationId, payload, "internal", caseId + ":updated:" + updatedAt.toEpochMilli());
    }

    public UUID caseClosed(UUID caseId, UUID organizationId, Instant closedAt, UUID closedBy, String outcome) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("caseId", caseId.toString());
        payload.put("closedAt", closedAt.toString());
        payload.put(
                "closedBy",
                closedBy == null ? "00000000-0000-4000-8000-000000000000" : closedBy.toString());
        if (outcome != null) {
            payload.put("outcome", outcome);
        }
        return publish("CaseClosed", organizationId, payload, "internal", caseId + ":closed");
    }

    public UUID caseAssigned(UUID caseId, UUID organizationId, UUID assignedTo, UUID assignedBy, Instant assignedAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("caseId", caseId.toString());
        payload.put("assignedTo", assignedTo.toString());
        if (assignedBy != null) {
            payload.put("assignedBy", assignedBy.toString());
        }
        payload.put("assignedAt", assignedAt.toString());
        return publish("CaseAssigned", organizationId, payload, "internal", caseId + ":assigned");
    }

    public UUID evidenceAttached(
            UUID caseId,
            UUID organizationId,
            UUID evidenceId,
            String evidenceType,
            String referenceType,
            String referenceId,
            UUID attachedBy,
            Instant attachedAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("caseId", caseId.toString());
        payload.put("evidenceId", evidenceId.toString());
        payload.put("evidenceType", evidenceType);
        payload.put("referenceType", referenceType);
        payload.put("referenceId", referenceId);
        if (attachedBy != null) {
            payload.put("attachedBy", attachedBy.toString());
        }
        payload.put("attachedAt", attachedAt.toString());
        return publish("EvidenceAttached", organizationId, payload, "restricted", evidenceId.toString());
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
