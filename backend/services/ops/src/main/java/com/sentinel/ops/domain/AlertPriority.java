package com.sentinel.ops.domain;

/**
 * ALERT-owned queue priority mapping (ALERT-FR-003).
 * Implementation gap-fill: FRS does not specify a numeric formula. This does not copy RISK
 * {@code prioritySignal} and is not a frozen requirement.
 */
public final class AlertPriority {

    private AlertPriority() {}

    public static int fromRiskLevel(String riskLevel) {
        if (riskLevel == null) {
            return 70;
        }
        return switch (riskLevel.trim().toLowerCase()) {
            case "critical" -> 90;
            case "high" -> 70;
            case "medium" -> 40;
            case "low" -> 20;
            default -> 70;
        };
    }

    public static boolean generationRequired(String eventType, String riskLevel) {
        if ("HighRiskDetected".equals(eventType)) {
            return true;
        }
        if (!"RiskCalculated".equals(eventType)) {
            return false;
        }
        if (riskLevel == null) {
            return false;
        }
        String level = riskLevel.trim().toLowerCase();
        return "high".equals(level) || "critical".equals(level);
    }
}
