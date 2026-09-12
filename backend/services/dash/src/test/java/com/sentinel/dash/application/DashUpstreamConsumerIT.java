package com.sentinel.dash.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.sentinel.dash.DashPostgresIT;
import com.sentinel.dash.infrastructure.DashStore;

@SpringBootTest
class DashUpstreamConsumerIT extends DashPostgresIT {

    @Autowired
    private DashUpstreamConsumer consumer;

    @Autowired
    private DashStore store;

    @Test
    void duplicateDeliveryIsIdempotentAndClosedAlertsLeaveQueue() {
        UUID org = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID alertId = UUID.randomUUID();
        Map<String, Object> created = Map.of(
                "eventId",
                eventId.toString(),
                "eventType",
                "AlertCreated",
                "organizationId",
                org.toString(),
                "payload",
                Map.of("alertId", alertId.toString(), "status", "open", "priority", 1, "createdAt", "2026-09-11T00:00:00Z"));
        assertEquals("processed", consumer.consume(created));
        assertEquals("duplicate", consumer.consume(created));
        assertEquals(1, store.listProjections(org, "alert:").size());
        consumer.consume(Map.of(
                "eventId",
                UUID.randomUUID().toString(),
                "eventType",
                "AlertClosed",
                "organizationId",
                org.toString(),
                "payload",
                Map.of("alertId", alertId.toString())));
        assertEquals(0, store.listProjections(org, "alert:").size());
        assertEquals("ignored", consumer.consume(Map.of(
                "eventId", UUID.randomUUID().toString(),
                "eventType", "ReportGenerated",
                "organizationId", org.toString(),
                "payload", Map.of())));
    }
}
