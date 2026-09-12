package com.sentinel.ops.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentinel.ops.api.RiskContext;
import com.sentinel.ops.domain.AlertException;
import com.sentinel.ops.domain.AlertLifecycle;
import com.sentinel.ops.domain.AlertPriority;
import com.sentinel.ops.infrastructure.AlertStore;
import com.sentinel.ops.infrastructure.AlertStore.AlertRow;
import com.sentinel.ops.infrastructure.RiskStore;

@Service
public class AlertService {

    private final AlertStore store;
    private final AlertEventPublisher events;
    private final RiskStore audits;
    private final ObjectMapper objectMapper;

    public AlertService(AlertStore store, AlertEventPublisher events, RiskStore audits, ObjectMapper objectMapper) {
        this.store = store;
        this.events = events;
        this.audits = audits;
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> list(String cursor, int limit) {
        int safe = sanitizeLimit(limit);
        List<Map<String, Object>> data = new ArrayList<>();
        for (AlertRow row : store.list(requireOrg(), cursor, safe)) {
            data.add(toApi(row));
        }
        return data;
    }

    public boolean hasMore(String lastId, int limit) {
        return store.hasMore(requireOrg(), lastId) && limit > 0;
    }

    public Map<String, Object> get(UUID alertId) {
        return toApi(requireAlert(alertId));
    }

    @Transactional
    public Map<String, Object> patch(UUID alertId, String status) {
        AlertRow current = requireAlert(alertId);
        AlertLifecycle.requirePatchable(current.status(), status);
        String next = status.trim().toLowerCase();
        if (current.status().equals(next)) {
            return toApi(current);
        }
        Instant now = Instant.now();
        AlertRow updated = withStatus(current, next, now);
        store.update(updated);
        audit("ALERT_STATUS_UPDATED", alertId, "{\"status\":\"" + next + "\"}");
        return toApi(updated);
    }

    @Transactional
    public Map<String, Object> assign(UUID alertId, UUID assigneeId) {
        if (assigneeId == null) {
            throw AlertException.validation("assigneeId", "assigneeId is required");
        }
        AlertRow current = requireAlert(alertId);
        AlertLifecycle.requireAssignable(current.status());
        if (AlertLifecycle.ASSIGNED.equals(current.status()) && assigneeId.equals(current.assignedTo())) {
            return toApi(current);
        }
        Instant now = Instant.now();
        AlertRow updated = new AlertRow(
                current.id(),
                current.organizationId(),
                AlertLifecycle.ASSIGNED,
                current.priority(),
                current.title(),
                current.riskAssessmentId(),
                assigneeId,
                current.createdAt(),
                now,
                current.closedAt(),
                current.closedBy(),
                current.dispositionReason(),
                current.createdBy());
        store.update(updated);
        events.alertAssigned(updated.id(), updated.organizationId(), assigneeId, RiskContext.get().actorId(), now);
        audit("ALERT_ASSIGNED", alertId, "{\"assigneeId\":\"" + assigneeId + "\"}");
        return toApi(updated);
    }

    @Transactional
    public Map<String, Object> close(UUID alertId, String dispositionReason) {
        if (dispositionReason == null || dispositionReason.isBlank()) {
            throw AlertException.validation("dispositionReason", "dispositionReason is required");
        }
        AlertRow current = requireAlert(alertId);
        if (AlertLifecycle.CLOSED.equals(current.status())) {
            if (dispositionReason.equals(current.dispositionReason())) {
                return toApi(current);
            }
            throw AlertException.conflict("Alert is already closed");
        }
        AlertLifecycle.requireCloseable(current.status());
        Instant now = Instant.now();
        UUID actor = RiskContext.get().actorId();
        AlertRow updated = new AlertRow(
                current.id(),
                current.organizationId(),
                AlertLifecycle.CLOSED,
                current.priority(),
                current.title(),
                current.riskAssessmentId(),
                current.assignedTo(),
                current.createdAt(),
                now,
                now,
                actor,
                dispositionReason,
                current.createdBy());
        store.update(updated);
        events.alertClosed(updated.id(), updated.organizationId(), now, actor, dispositionReason);
        audit("ALERT_CLOSED", alertId, "{\"dispositionReason\":\"" + dispositionReason.replace("\"", "") + "\"}");
        return toApi(updated);
    }

    @Transactional
    public Map<String, Object> patchPriority(UUID alertId, Integer priority) {
        if (priority == null) {
            throw AlertException.validation("priority", "priority is required");
        }
        AlertRow current = requireAlert(alertId);
        if (AlertLifecycle.CLOSED.equals(current.status())) {
            throw AlertException.conflict("Closed alerts cannot change priority");
        }
        Instant now = Instant.now();
        AlertRow updated = new AlertRow(
                current.id(),
                current.organizationId(),
                current.status(),
                priority,
                current.title(),
                current.riskAssessmentId(),
                current.assignedTo(),
                current.createdAt(),
                now,
                current.closedAt(),
                current.closedBy(),
                current.dispositionReason(),
                current.createdBy());
        store.update(updated);
        audit("ALERT_PRIORITY_UPDATED", alertId, "{\"priority\":" + priority + "}");
        return toApi(updated);
    }

    @Transactional
    public Map<String, Object> linkInvestigation(UUID alertId, UUID caseId) {
        if (caseId == null) {
            throw AlertException.validation("caseId", "caseId is required");
        }
        AlertRow current = requireAlert(alertId);
        store.insertInvestigationLink(alertId, caseId, RiskContext.get().actorId(), Instant.now());
        audit("ALERT_INVESTIGATION_LINKED", alertId, "{\"caseId\":\"" + caseId + "\"}");
        return toApi(current);
    }

    /**
     * Consumes approved RISK events. Does not score risk. One alert per assessment (application-level).
     */
    public void consumeRiskEvent(Map<String, Object> envelope) {
        String eventType = String.valueOf(envelope.get("eventType"));
        if (!"RiskCalculated".equals(eventType) && !"HighRiskDetected".equals(eventType)) {
            return;
        }
        UUID organizationId;
        UUID eventId;
        try {
            organizationId = uuid(envelope.get("organizationId"), "organizationId");
            eventId = uuid(envelope.get("eventId"), "eventId");
        } catch (AlertException ex) {
            return;
        }
        applyEnvelopeContext(envelope, organizationId);
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = envelope.get("payload") instanceof Map<?, ?> map
                ? (Map<String, Object>) map
                : Map.of();
        UUID assessmentId;
        try {
            assessmentId = uuid(payload.get("assessmentId"), "assessmentId");
        } catch (AlertException ex) {
            return;
        }
        String entityType = text(payload.get("entityType"));
        String entityId = text(payload.get("entityId"));
        String riskLevel = text(payload.get("riskLevel"));
        if ("HighRiskDetected".equals(eventType) && riskLevel == null) {
            riskLevel = "high";
        }
        BigDecimal score = decimal(payload.get("score"));
        Optional<AlertRow> existing = store.findByAssessment(organizationId, assessmentId);
        if (existing.isPresent() && alreadyProcessed(existing.get().id(), eventId)) {
            return;
        }
        Instant now = Instant.now();
        if (existing.isEmpty()) {
            if (!AlertPriority.generationRequired(eventType, riskLevel)) {
                return;
            }
            int priority = AlertPriority.fromRiskLevel(riskLevel);
            String title = "High risk " + (entityType == null ? "entity" : entityType) + " " + (entityId == null ? "" : entityId);
            UUID alertId = UUID.randomUUID();
            AlertRow created = new AlertRow(
                    alertId,
                    organizationId,
                    AlertLifecycle.OPEN,
                    priority,
                    title.trim(),
                    assessmentId,
                    null,
                    now,
                    now,
                    null,
                    null,
                    null,
                    null);
            store.insert(created);
            store.upsertRiskContext(alertId, score, riskLevel, contextJson(payload, eventType, eventId), now);
            events.alertCreated(
                    alertId, organizationId, AlertLifecycle.OPEN, priority, assessmentId, created.title(), now);
            audit("ALERT_CREATED", alertId, "{\"eventType\":\"" + eventType + "\"}");
            return;
        }
        AlertRow row = existing.get();
        int priority = row.priority();
        if (AlertLifecycle.mayApplySystemPriority(row.status())) {
            int mapped = AlertPriority.fromRiskLevel(riskLevel);
            // Do not let HighRiskDetected's inferred "high" lower ALERT priority already set from RiskCalculated.
            priority = Math.max(row.priority(), mapped);
        }
        AlertRow updated = new AlertRow(
                row.id(),
                row.organizationId(),
                row.status(),
                priority,
                row.title(),
                row.riskAssessmentId(),
                row.assignedTo(),
                row.createdAt(),
                now,
                row.closedAt(),
                row.closedBy(),
                row.dispositionReason(),
                row.createdBy());
        store.update(updated);
        store.upsertRiskContext(row.id(), score, riskLevel, mergeContext(row.id(), payload, eventType, eventId), now);
        audit("ALERT_CONTEXT_REFRESHED", row.id(), "{\"eventType\":\"" + eventType + "\"}");
    }

    private boolean alreadyProcessed(UUID alertId, UUID eventId) {
        Optional<String> json = store.riskContextJson(alertId);
        if (json.isEmpty()) {
            return false;
        }
        try {
            var node = objectMapper.readTree(json.get());
            var ids = node.get("sourceEventIds");
            if (ids != null && ids.isArray()) {
                for (var id : ids) {
                    if (eventId.toString().equals(id.asText())) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    private String contextJson(Map<String, Object> payload, String eventType, UUID eventId) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("lastEventType", eventType);
        ArrayNode ids = node.putArray("sourceEventIds");
        ids.add(eventId.toString());
        if (payload.get("entityType") != null) {
            node.put("entityType", String.valueOf(payload.get("entityType")));
        }
        if (payload.get("entityId") != null) {
            node.put("entityId", String.valueOf(payload.get("entityId")));
        }
        return node.toString();
    }

    private String mergeContext(UUID alertId, Map<String, Object> payload, String eventType, UUID eventId) {
        ObjectNode node = objectMapper.createObjectNode();
        ArrayNode ids = node.putArray("sourceEventIds");
        store.riskContextJson(alertId).ifPresent(existing -> {
            try {
                var prev = objectMapper.readTree(existing);
                if (prev.has("sourceEventIds") && prev.get("sourceEventIds").isArray()) {
                    prev.get("sourceEventIds").forEach(v -> ids.add(v.asText()));
                }
            } catch (Exception ignored) {
                // keep new ids only
            }
        });
        ids.add(eventId.toString());
        node.put("lastEventType", eventType);
        if (payload.get("entityType") != null) {
            node.put("entityType", String.valueOf(payload.get("entityType")));
        }
        if (payload.get("entityId") != null) {
            node.put("entityId", String.valueOf(payload.get("entityId")));
        }
        return node.toString();
    }

    private void applyEnvelopeContext(Map<String, Object> envelope, UUID organizationId) {
        RiskContext ctx = RiskContext.get();
        ctx.setOrganizationId(organizationId);
        if (envelope.get("correlationId") != null) {
            try {
                ctx.setCorrelationId(UUID.fromString(String.valueOf(envelope.get("correlationId"))));
            } catch (IllegalArgumentException ignored) {
                ctx.setCorrelationId(UUID.randomUUID());
            }
        }
        if (envelope.get("eventId") != null) {
            try {
                ctx.setRequestId(UUID.fromString(String.valueOf(envelope.get("eventId"))));
            } catch (IllegalArgumentException ignored) {
                ctx.setRequestId(UUID.randomUUID());
            }
        }
        if (ctx.actorType() == null) {
            ctx.setActorType("service");
        }
    }

    private AlertRow requireAlert(UUID alertId) {
        return store.find(alertId, requireOrg())
                .orElseThrow(() -> AlertException.notFound("Alert not found"));
    }

    private static AlertRow withStatus(AlertRow current, String status, Instant now) {
        return new AlertRow(
                current.id(),
                current.organizationId(),
                status,
                current.priority(),
                current.title(),
                current.riskAssessmentId(),
                current.assignedTo(),
                current.createdAt(),
                now,
                current.closedAt(),
                current.closedBy(),
                current.dispositionReason(),
                current.createdBy());
    }

    private Map<String, Object> toApi(AlertRow row) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", row.id().toString());
        data.put("organizationId", row.organizationId().toString());
        data.put("status", row.status());
        data.put("priority", row.priority());
        data.put("title", row.title());
        data.put("riskAssessmentId", row.riskAssessmentId() == null ? null : row.riskAssessmentId().toString());
        data.put("createdAt", row.createdAt().toString());
        return data;
    }

    private UUID requireOrg() {
        UUID org = RiskContext.get().organizationId();
        if (org == null) {
            throw AlertException.validation("X-Organization-Id", "X-Organization-Id is required");
        }
        return org;
    }

    private int sanitizeLimit(int limit) {
        if (limit < 1 || limit > 200) {
            throw AlertException.validation("limit", "limit must be between 1 and 200");
        }
        return limit;
    }

    private void audit(String action, UUID resourceId, String metadata) {
        RiskContext ctx = RiskContext.get();
        audits.insertAudit(
                requireOrg(),
                ctx.actorId(),
                action,
                "ALERT",
                resourceId,
                "SUCCESS",
                ctx.correlationId(),
                ctx.requestId(),
                metadata,
                Instant.now());
    }

    private static UUID uuid(Object raw, String field) {
        if (raw == null) {
            throw AlertException.validation(field, field + " is required");
        }
        try {
            return UUID.fromString(String.valueOf(raw));
        } catch (IllegalArgumentException ex) {
            throw AlertException.validation(field, field + " must be a UUID");
        }
    }

    private static String text(Object raw) {
        return raw == null ? null : String.valueOf(raw);
    }

    private static BigDecimal decimal(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof BigDecimal bd) {
            return bd;
        }
        if (raw instanceof Number n) {
            return new BigDecimal(n.toString());
        }
        try {
            return new BigDecimal(String.valueOf(raw));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
