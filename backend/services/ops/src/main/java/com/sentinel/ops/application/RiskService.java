package com.sentinel.ops.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.ops.api.RiskContext;
import com.sentinel.ops.domain.RiskException;
import com.sentinel.ops.domain.RiskScoring;
import com.sentinel.ops.infrastructure.RiskStore;
import com.sentinel.ops.infrastructure.RiskStore.AssessmentRow;
import com.sentinel.ops.infrastructure.RiskStore.RuleRow;

@Service
public class RiskService {

    private static final Set<String> ENTITY_TYPES = Set.of("transaction", "user", "device", "session");

    private final RiskStore store;
    private final RiskEventPublisher events;
    private final ObjectMapper objectMapper;

    public RiskService(RiskStore store, RiskEventPublisher events, ObjectMapper objectMapper) {
        this.store = store;
        this.events = events;
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> listRules(String cursor, int limit) {
        UUID org = requireOrg();
        limit = sanitizeLimit(limit);
        List<Map<String, Object>> data = new ArrayList<>();
        for (RuleRow row : store.listRules(org, cursor, limit)) {
            data.add(toRuleApi(row));
        }
        return data;
    }

    @Transactional
    public Map<String, Object> createRule(String name, JsonNode definition, Boolean enabled) {
        UUID org = requireOrg();
        if (name == null || name.isBlank()) {
            throw RiskException.validation("name", "name is required");
        }
        RiskScoring.validateDefinition(definition);
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        RuleRow row = new RuleRow(id, org, name.trim(), json(definition), enabled == null || enabled, 1);
        store.insertRule(row, now, RiskContext.get().actorId());
        audit("RULE_CREATED", "RISK_RULE", id, "{}");
        return toRuleApi(row);
    }

    @Transactional
    public Map<String, Object> patchRule(UUID ruleId, String name, JsonNode definition, Boolean enabled) {
        UUID org = requireOrg();
        RuleRow current = store.findRule(ruleId, org).orElseThrow(() -> RiskException.notFound("Risk rule not found"));
        String nextName = name == null ? current.name() : name.trim();
        if (nextName.isBlank()) {
            throw RiskException.validation("name", "name is required");
        }
        JsonNode nextDef = definition == null ? store.parseJson(current.definitionJson()) : definition;
        RiskScoring.validateDefinition(nextDef);
        boolean nextEnabled = enabled == null ? current.enabled() : enabled;
        int version = current.version();
        if (definition != null) {
            version = current.version() + 1;
        }
        RuleRow updated = new RuleRow(current.id(), org, nextName, json(nextDef), nextEnabled, version);
        store.updateRule(updated, Instant.now(), RiskContext.get().actorId());
        audit("RULE_UPDATED", "RISK_RULE", ruleId, "{}");
        return toRuleApi(updated);
    }

    public List<Map<String, Object>> listAssessments(String cursor, int limit) {
        UUID org = requireOrg();
        limit = sanitizeLimit(limit);
        List<Map<String, Object>> data = new ArrayList<>();
        for (AssessmentRow row : store.listAssessments(org, cursor, limit)) {
            data.add(toAssessmentApi(row));
        }
        return data;
    }

    public Map<String, Object> getAssessment(UUID assessmentId) {
        UUID org = requireOrg();
        return toAssessmentApi(store.findAssessment(assessmentId, org)
                .orElseThrow(() -> RiskException.notFound("Risk assessment not found")));
    }

    @Transactional
    public Map<String, Object> ingest(String externalTxId, String amount, String asset, String timestamp, UUID userId, String idempotencyKey) {
        UUID org = requireOrg();
        if (externalTxId == null || externalTxId.isBlank()) {
            throw RiskException.validation("externalTransactionId", "externalTransactionId is required");
        }
        if (amount == null || amount.isBlank()) {
            throw RiskException.validation("amount", "amount is required");
        }
        if (asset == null || asset.isBlank()) {
            throw RiskException.validation("asset", "asset is required");
        }
        if (timestamp == null || timestamp.isBlank()) {
            throw RiskException.validation("timestamp", "timestamp is required");
        }
        BigDecimal parsedAmount;
        try {
            parsedAmount = new BigDecimal(amount);
        } catch (NumberFormatException ex) {
            throw RiskException.validation("amount", "amount must be numeric");
        }
        try {
            Instant.parse(timestamp);
        } catch (RuntimeException ex) {
            throw RiskException.validation("timestamp", "timestamp must be an ISO-8601 date-time");
        }
        var existing = store.findIngest(org, externalTxId.trim());
        if (existing.isPresent()) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("assessmentId", existing.get().assessmentId() == null ? null : existing.get().assessmentId().toString());
            data.put("status", "duplicate");
            return data;
        }
        UUID assessmentId = evaluateAndPersist(
                org,
                "transaction",
                externalTxId.trim(),
                new RiskScoring.Context("transaction", externalTxId.trim(), parsedAmount, asset.trim(), userId != null),
                externalTxId.trim());
        store.insertIngest(UUID.randomUUID(), org, externalTxId.trim(), assessmentId, Instant.now(), idempotencyKey);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("assessmentId", assessmentId.toString());
        data.put("status", "accepted");
        return data;
    }

    @Transactional
    public Map<String, Object> evaluate(String entityType, String entityId) {
        UUID org = requireOrg();
        if (entityType == null || !ENTITY_TYPES.contains(entityType)) {
            throw RiskException.validation("entityType", "entityType must be transaction, user, device, or session");
        }
        if (entityId == null || entityId.isBlank()) {
            throw RiskException.validation("entityId", "entityId is required");
        }
        UUID assessmentId = evaluateAndPersist(
                org,
                entityType,
                entityId.trim(),
                new RiskScoring.Context(entityType, entityId.trim(), null, null, false),
                "transaction".equals(entityType) ? entityId.trim() : null);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("jobId", assessmentId.toString());
        data.put("status", "accepted");
        return data;
    }

    public boolean hasMoreRules(UUID org, String lastId, int limit) {
        if (lastId == null) {
            return false;
        }
        return store.listRules(org, lastId, 1).size() == 1;
    }

    public boolean hasMoreAssessments(UUID org, String lastId, int limit) {
        if (lastId == null) {
            return false;
        }
        return store.listAssessments(org, lastId, 1).size() == 1;
    }

    @Transactional
    UUID evaluateAndPersist(
            UUID org, String entityType, String entityId, RiskScoring.Context context, String transactionRef) {
        Instant now = Instant.now();
        List<RiskScoring.Rule> rules = new ArrayList<>();
        for (RuleRow row : store.enabledRules(org)) {
            rules.add(new RiskScoring.Rule(row.id(), row.name(), row.enabled(), store.parseJson(row.definitionJson())));
        }
        RiskScoring.Result result = RiskScoring.score(rules, context);
        UUID assessmentId = UUID.randomUUID();
        store.insertAssessment(
                new AssessmentRow(
                        assessmentId,
                        org,
                        entityType,
                        entityId,
                        result.score(),
                        result.riskLevel(),
                        result.explanation(),
                        now,
                        RiskContext.get().correlationId()),
                now);
        List<Map<String, Object>> hitPayload = new ArrayList<>();
        for (RiskScoring.Hit hit : result.hits()) {
            Map<String, Object> details = RiskScoring.hitDetails(hit);
            store.insertHit(UUID.randomUUID(), assessmentId, hit.ruleId(), json(details), now);
            hitPayload.add(details);
        }
        events.riskCalculated(
                assessmentId,
                org,
                entityType,
                entityId,
                result.score(),
                result.riskLevel(),
                result.explanation(),
                now,
                transactionRef,
                hitPayload);
        if (RiskScoring.highRisk(result.riskLevel())) {
            events.highRiskDetected(assessmentId, org, entityType, entityId, result.score(), now);
        }
        audit("ASSESSMENT_CREATED", "RISK_ASSESSMENT", assessmentId, "{\"score\":" + result.score() + "}");
        return assessmentId;
    }

    private Map<String, Object> toRuleApi(RuleRow row) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", row.id().toString());
        data.put("name", row.name());
        data.put("enabled", row.enabled());
        data.put("definition", store.parseJson(row.definitionJson()));
        return data;
    }

    private Map<String, Object> toAssessmentApi(AssessmentRow row) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", row.id().toString());
        data.put("entityType", row.entityType());
        data.put("entityId", row.entityId());
        data.put("score", row.score());
        data.put("riskLevel", row.riskLevel());
        data.put("explanationSummary", row.explanation());
        data.put("evaluatedAt", row.evaluatedAt().toString());
        return data;
    }

    private UUID requireOrg() {
        UUID org = RiskContext.get().organizationId();
        if (org == null) {
            throw RiskException.validation("X-Organization-Id", "X-Organization-Id is required");
        }
        return org;
    }

    private int sanitizeLimit(int limit) {
        if (limit < 1 || limit > 200) {
            throw RiskException.validation("limit", "limit must be between 1 and 200");
        }
        return limit;
    }

    private void audit(String action, String resourceType, UUID resourceId, String metadata) {
        RiskContext ctx = RiskContext.get();
        store.insertAudit(
                requireOrg(),
                ctx.actorId(),
                action,
                resourceType,
                resourceId,
                "SUCCESS",
                ctx.correlationId(),
                ctx.requestId(),
                metadata,
                Instant.now());
    }

    private String json(Object value) {
        try {
            if (value instanceof String s) {
                return s;
            }
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
