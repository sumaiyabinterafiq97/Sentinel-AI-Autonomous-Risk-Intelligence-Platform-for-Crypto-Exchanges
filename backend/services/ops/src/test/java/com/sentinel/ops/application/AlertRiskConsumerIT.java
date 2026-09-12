package com.sentinel.ops.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.sentinel.common.outbox.EventStreams;
import com.sentinel.common.outbox.InMemoryDurableEventLog;
import com.sentinel.ops.OpsPostgresIT;
import com.sentinel.ops.api.RiskContext;
import com.sentinel.ops.infrastructure.AlertStore;
import com.sentinel.ops.infrastructure.RiskEventBus;

@SpringBootTest
class AlertRiskConsumerIT extends OpsPostgresIT {

    @Autowired
    private InMemoryDurableEventLog durableLog;

    @Autowired
    private AlertStore alerts;

    @Autowired
    private RiskEventBus events;

    @BeforeEach
    void reset() {
        events.reset();
        durableLog.reset();
        RiskContext.clear();
    }

    @Test
    void duplicateRiskEventDoesNotCreateDuplicateAlerts() {
        UUID org = UUID.randomUUID();
        UUID assessment = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Map<String, Object> envelope = riskCalculated(org, assessment, eventId, "critical");
        String stream = EventStreams.of(envelope);
        durableLog.append(stream, envelope);
        durableLog.append(stream, envelope);
        assertEquals(1, alerts.countForOrg(org));
        Map<String, Object> high = highRisk(org, assessment, UUID.randomUUID());
        durableLog.append(EventStreams.of(high), high);
        assertEquals(1, alerts.countForOrg(org));
        long created = events.snapshot().stream().filter(e -> "AlertCreated".equals(e.get("eventType"))).count();
        assertEquals(1, created);
        assertEquals(eventId.toString(), envelope.get("eventId"));
        assertTrue(alerts.findByAssessment(org, assessment).isPresent());
        assertEquals("open", alerts.findByAssessment(org, assessment).orElseThrow().status());
    }

    @Test
    void eventTenantCannotCreateAlertInAnotherOrganization() {
        UUID orgA = UUID.randomUUID();
        UUID orgB = UUID.randomUUID();
        UUID assessment = UUID.randomUUID();
        Map<String, Object> envelope = riskCalculated(orgA, assessment, UUID.randomUUID(), "high");
        durableLog.append(EventStreams.of(envelope), envelope);
        assertEquals(1, alerts.countForOrg(orgA));
        assertEquals(0, alerts.countForOrg(orgB));
        assertTrue(alerts.findByAssessment(orgB, assessment).isEmpty());
    }

    private static Map<String, Object> riskCalculated(UUID org, UUID assessment, UUID eventId, String level) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("assessmentId", assessment.toString());
        payload.put("entityType", "transaction");
        payload.put("entityId", "tx-dup");
        payload.put("score", new BigDecimal("80"));
        payload.put("riskLevel", level);
        payload.put("explanationSummary", "test");
        payload.put("evaluatedAt", Instant.now().toString());
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("eventId", eventId.toString());
        envelope.put("eventType", "RiskCalculated");
        envelope.put("schemaVersion", "1.0");
        envelope.put("timestamp", Instant.now().toString());
        envelope.put("producer", "sentinel-ops");
        envelope.put("correlationId", UUID.randomUUID().toString());
        envelope.put("organizationId", org.toString());
        envelope.put("payload", payload);
        envelope.put("metadata", Map.of("classification", "confidential"));
        return envelope;
    }

    private static Map<String, Object> highRisk(UUID org, UUID assessment, UUID eventId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("assessmentId", assessment.toString());
        payload.put("entityType", "transaction");
        payload.put("entityId", "tx-dup");
        payload.put("score", new BigDecimal("80"));
        payload.put("detectedAt", Instant.now().toString());
        payload.put("prioritySignal", new BigDecimal("80"));
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("eventId", eventId.toString());
        envelope.put("eventType", "HighRiskDetected");
        envelope.put("schemaVersion", "1.0");
        envelope.put("timestamp", Instant.now().toString());
        envelope.put("producer", "sentinel-ops");
        envelope.put("correlationId", UUID.randomUUID().toString());
        envelope.put("organizationId", org.toString());
        envelope.put("payload", payload);
        envelope.put("metadata", Map.of("classification", "internal"));
        return envelope;
    }
}
