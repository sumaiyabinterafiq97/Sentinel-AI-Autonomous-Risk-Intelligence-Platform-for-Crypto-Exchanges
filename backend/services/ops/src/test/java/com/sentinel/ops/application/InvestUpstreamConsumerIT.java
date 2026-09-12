package com.sentinel.ops.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import com.sentinel.ops.infrastructure.InvestStore;
import com.sentinel.ops.infrastructure.RiskEventBus;

@SpringBootTest
class InvestUpstreamConsumerIT extends OpsPostgresIT {

    @Autowired
    private InMemoryDurableEventLog durableLog;

    @Autowired
    private InvestStore store;

    @Autowired
    private RiskEventBus events;

    @BeforeEach
    void reset() {
        events.reset();
        durableLog.reset();
        RiskContext.clear();
    }

    @Test
    void duplicateAlertCreatedDoesNotCreateDuplicateCases() {
        UUID org = UUID.randomUUID();
        UUID alertId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Map<String, Object> envelope = alertCreated(org, alertId, eventId);
        String stream = EventStreams.of(envelope);
        durableLog.append(stream, envelope);
        durableLog.append(stream, envelope);
        assertEquals(1, store.countForOrg(org));
        assertEquals(1, events.snapshot().stream().filter(e -> "CaseCreated".equals(e.get("eventType"))).count());
        assertTrue(store.findBySourceAlert(org, alertId).isPresent());
    }

    @Test
    void lowRiskCalculatedDoesNotCreateACase() {
        UUID org = UUID.randomUUID();
        Map<String, Object> envelope = riskCalculated(org, UUID.randomUUID(), UUID.randomUUID(), "low", 5);
        durableLog.append(EventStreams.of(envelope), envelope);
        assertEquals(0, store.countForOrg(org));
    }

    @Test
    void eventTenantCannotCreateCaseInAnotherOrganization() {
        UUID orgA = UUID.randomUUID();
        UUID orgB = UUID.randomUUID();
        Map<String, Object> envelope = alertCreated(orgA, UUID.randomUUID(), UUID.randomUUID());
        durableLog.append(EventStreams.of(envelope), envelope);
        assertEquals(1, store.countForOrg(orgA));
        assertEquals(0, store.countForOrg(orgB));
    }

    private static Map<String, Object> alertCreated(UUID org, UUID alertId, UUID eventId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("alertId", alertId.toString());
        payload.put("status", "open");
        payload.put("priority", 90);
        payload.put("title", "High risk transaction");
        payload.put("createdAt", Instant.now().toString());
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("eventId", eventId.toString());
        envelope.put("eventType", "AlertCreated");
        envelope.put("schemaVersion", "1.0");
        envelope.put("timestamp", Instant.now().toString());
        envelope.put("producer", "sentinel-ops");
        envelope.put("correlationId", UUID.randomUUID().toString());
        envelope.put("organizationId", org.toString());
        envelope.put("payload", payload);
        envelope.put("metadata", Map.of("classification", "confidential"));
        return envelope;
    }

    private static Map<String, Object> riskCalculated(UUID org, UUID assessment, UUID eventId, String level, int score) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("assessmentId", assessment.toString());
        payload.put("entityType", "transaction");
        payload.put("entityId", "tx-x");
        payload.put("score", score);
        payload.put("riskLevel", level);
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
}
