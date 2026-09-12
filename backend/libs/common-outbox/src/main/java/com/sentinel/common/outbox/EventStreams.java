package com.sentinel.common.outbox;

import java.util.Map;

/**
 * Logical stream names from MessageBrokerArchitecture: {@code sentinel.{domain}.{eventType}.v{major}}.
 */
public final class EventStreams {

    private EventStreams() {}

    public static String of(Map<String, Object> envelope) {
        String eventType = String.valueOf(envelope.get("eventType"));
        String version = envelope.get("schemaVersion") == null ? "1.0" : String.valueOf(envelope.get("schemaVersion"));
        int major = 1;
        int dot = version.indexOf('.');
        if (dot > 0) {
            major = Integer.parseInt(version.substring(0, dot));
        }
        return "sentinel." + domainOf(eventType) + "." + eventType + ".v" + major;
    }

    public static String dlq(String stream) {
        return stream + ".dlq";
    }

    static String domainOf(String eventType) {
        return switch (eventType) {
            case "ConfigurationUpdated", "FeatureFlagChanged" -> "core";
            case "UserLoggedIn", "SessionExpired" -> "auth";
            case "UserUpdated" -> "user";
            case "RiskCalculated", "HighRiskDetected" -> "risk";
            case "AlertCreated", "AlertAssigned", "AlertClosed" -> "alert";
            case "CaseCreated", "CaseUpdated", "CaseClosed", "CaseAssigned", "EvidenceAttached" -> "invest";
            case "ComplianceReviewed", "TravelRuleValidated", "SanctionsHitDetected", "AuditPackagePrepared" -> "comp";
            case "AIRecommendationGenerated", "PromptUpdated" -> "ai";
            case "AdminSettingUpdated", "IntegrationConfigured", "AdminActionPerformed" -> "admin";
            default -> "unknown";
        };
    }
}
