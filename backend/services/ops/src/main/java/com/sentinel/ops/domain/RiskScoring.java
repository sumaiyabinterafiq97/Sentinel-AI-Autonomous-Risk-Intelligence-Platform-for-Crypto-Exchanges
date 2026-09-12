package com.sentinel.ops.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Deterministic weighted-sum scoring. Formula is not specified in FRS; see Phase12M4PreImplementationAudit.
 */
public final class RiskScoring {

    public static final BigDecimal HIGH_THRESHOLD = new BigDecimal("50");

    private static final Set<String> FACTORS = Set.of("amount", "asset", "userIdPresent", "entityType");
    private static final Set<String> OPERATORS = Set.of("gte", "lte", "eq", "neq");
    private static final Set<String> APPLIES = Set.of("transaction", "user", "device", "session");

    private RiskScoring() {}

    public record Hit(java.util.UUID ruleId, String name, int weight, String reason) {}

    public record Result(BigDecimal score, String riskLevel, String explanation, List<Hit> hits) {}

    public record Context(
            String entityType, String entityId, BigDecimal amount, String asset, boolean userPresent) {}

    public static void validateDefinition(JsonNode definition) {
        if (definition == null || !definition.isObject()) {
            throw RiskException.validation("definition", "definition must be a JSON object");
        }
        if (!definition.has("weight") || !definition.get("weight").isNumber()) {
            throw RiskException.validation("definition.weight", "weight must be a number between 0 and 100");
        }
        int weight = definition.get("weight").asInt();
        if (weight < 0 || weight > 100) {
            throw RiskException.validation("definition.weight", "weight must be a number between 0 and 100");
        }
        if (definition.has("factor") && !FACTORS.contains(definition.get("factor").asText())) {
            throw RiskException.validation("definition.factor", "factor is not a supported deterministic factor");
        }
        if (definition.has("operator") && !OPERATORS.contains(definition.get("operator").asText())) {
            throw RiskException.validation("definition.operator", "operator must be gte, lte, eq, or neq");
        }
        if (definition.has("appliesTo") && !APPLIES.contains(definition.get("appliesTo").asText())) {
            throw RiskException.validation("definition.appliesTo", "appliesTo must be transaction, user, device, or session");
        }
    }

    public static Result score(List<Rule> rules, Context context) {
        List<Hit> hits = new ArrayList<>();
        int total = 0;
        for (Rule rule : rules) {
            if (!rule.enabled()) {
                continue;
            }
            if (!applies(rule.definition(), context.entityType())) {
                continue;
            }
            if (matches(rule.definition(), context)) {
                int weight = rule.definition().path("weight").asInt();
                total += weight;
                hits.add(new Hit(rule.id(), rule.name(), weight, factorLabel(rule.definition())));
            }
        }
        int capped = Math.min(100, total);
        BigDecimal score = BigDecimal.valueOf(capped);
        String level = levelOf(score);
        String explanation = hits.isEmpty()
                ? "No enabled rules matched; score is 0."
                : "Matched " + hits.size() + " rule(s): " + names(hits) + ". Deterministic weighted sum (capped at 100) = " + capped + ".";
        return new Result(score, level, explanation, hits);
    }

    public static String levelOf(BigDecimal score) {
        if (score.compareTo(new BigDecimal("75")) >= 0) {
            return "critical";
        }
        if (score.compareTo(HIGH_THRESHOLD) >= 0) {
            return "high";
        }
        if (score.compareTo(new BigDecimal("25")) >= 0) {
            return "medium";
        }
        return "low";
    }

    public static boolean highRisk(String level) {
        return "high".equals(level) || "critical".equals(level);
    }

    public static Map<String, Object> hitDetails(Hit hit) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("ruleName", hit.name());
        details.put("weight", hit.weight());
        details.put("reason", hit.reason());
        return details;
    }

    private static boolean applies(JsonNode definition, String entityType) {
        if (!definition.has("appliesTo")) {
            return true;
        }
        return entityType.equalsIgnoreCase(definition.get("appliesTo").asText());
    }

    private static boolean matches(JsonNode definition, Context context) {
        if (!definition.has("factor")) {
            return true;
        }
        String factor = definition.get("factor").asText();
        String operator = definition.has("operator") ? definition.get("operator").asText() : "eq";
        JsonNode expected = definition.get("value");
        return switch (factor) {
            case "amount" -> compareNumber(context.amount(), operator, expected);
            case "asset" -> compareText(context.asset(), operator, expected);
            case "userIdPresent" -> compareBoolean(context.userPresent(), operator, expected);
            case "entityType" -> compareText(context.entityType(), operator, expected);
            default -> false;
        };
    }

    private static boolean compareNumber(BigDecimal actual, String operator, JsonNode expected) {
        if (actual == null || expected == null || !expected.isNumber()) {
            return false;
        }
        int cmp = actual.compareTo(expected.decimalValue());
        return switch (operator) {
            case "gte" -> cmp >= 0;
            case "lte" -> cmp <= 0;
            case "eq" -> cmp == 0;
            case "neq" -> cmp != 0;
            default -> false;
        };
    }

    private static boolean compareText(String actual, String operator, JsonNode expected) {
        if (actual == null || expected == null || !expected.isTextual()) {
            return false;
        }
        boolean eq = actual.equalsIgnoreCase(expected.asText());
        return switch (operator) {
            case "eq" -> eq;
            case "neq" -> !eq;
            default -> false;
        };
    }

    private static boolean compareBoolean(boolean actual, String operator, JsonNode expected) {
        boolean value = expected != null && expected.isBoolean() && expected.asBoolean();
        boolean eq = actual == value;
        return switch (operator) {
            case "eq" -> eq;
            case "neq" -> !eq;
            default -> false;
        };
    }

    private static String factorLabel(JsonNode definition) {
        if (!definition.has("factor")) {
            return "unconditional";
        }
        return definition.get("factor").asText() + " " + definition.path("operator").asText("") + " " + definition.path("value").asText("");
    }

    private static String names(List<Hit> hits) {
        List<String> names = new ArrayList<>();
        for (Hit hit : hits) {
            names.add(hit.name() + "(" + hit.weight() + ")");
        }
        return String.join(", ", names);
    }

    public record Rule(java.util.UUID id, String name, boolean enabled, JsonNode definition) {}
}
