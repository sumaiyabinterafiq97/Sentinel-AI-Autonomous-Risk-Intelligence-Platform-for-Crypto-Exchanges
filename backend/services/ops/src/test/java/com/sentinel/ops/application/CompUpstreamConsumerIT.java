package com.sentinel.ops.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import com.sentinel.ops.infrastructure.CompStore;
import com.sentinel.ops.infrastructure.RiskEventBus;

@SpringBootTest
class CompUpstreamConsumerIT extends OpsPostgresIT {

    @Autowired
    private InMemoryDurableEventLog durableLog;

    @Autowired
    private CompStore store;

    @Autowired
    private RiskEventBus events;

    @BeforeEach
    void reset() {
        events.reset();
        durableLog.reset();
        RiskContext.clear();
    }

    @Test
    void duplicateCaseClosedDoesNotDuplicateIndexRows() {
        UUID org = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Map<String, Object> envelope = caseClosed(org, eventId);
        durableLog.append(EventStreams.of(envelope), envelope);
        durableLog.append(EventStreams.of(envelope), envelope);
        assertEquals(1, store.countRecords(org, "upstream_CaseClosed"));
        assertEquals(0, store.countKyc(org));
    }

    @Test
    void riskCalculatedIsIndexedWithoutCreatingKyc() {
        UUID org = UUID.randomUUID();
        Map<String, Object> envelope = riskCalculated(org, UUID.randomUUID());
        durableLog.append(EventStreams.of(envelope), envelope);
        assertEquals(1, store.countRecords(org, "upstream_RiskCalculated"));
        assertEquals(0, store.countKyc(org));
    }

    private static Map<String, Object> caseClosed(UUID org, UUID eventId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("caseId", UUID.randomUUID().toString());
        payload.put("closedAt", Instant.now().toString());
        payload.put("closedBy", UUID.randomUUID().toString());
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("eventId", eventId.toString());
        envelope.put("eventType", "CaseClosed");
        envelope.put("schemaVersion", "1.0");
        envelope.put("timestamp", Instant.now().toString());
        envelope.put("producer", "sentinel-ops");
        envelope.put("correlationId", UUID.randomUUID().toString());
        envelope.put("organizationId", org.toString());
        envelope.put("payload", payload);
        envelope.put("metadata", Map.of("classification", "internal"));
        return envelope;
    }

    private static Map<String, Object> riskCalculated(UUID org, UUID eventId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("assessmentId", UUID.randomUUID().toString());
        payload.put("entityType", "user");
        payload.put("entityId", "u-1");
        payload.put("score", 10);
        payload.put("riskLevel", "low");
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
