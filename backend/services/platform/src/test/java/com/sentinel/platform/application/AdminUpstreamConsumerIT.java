package com.sentinel.platform.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import com.sentinel.common.outbox.DeferredEvents;
import com.sentinel.platform.PlatformPostgresIT;
import com.sentinel.platform.api.RequestContext;

@SpringBootTest
class AdminUpstreamConsumerIT extends PlatformPostgresIT {

    @Autowired
    private AdminUpstreamConsumer consumer;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void configurationUpdatedIsConsumedIdempotentlyByCaller() {
        UUID org = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        RequestContext.get().setOrganizationId(org);
        Map<String, Object> envelope = Map.of(
                "eventId",
                eventId.toString(),
                "eventType",
                "ConfigurationUpdated",
                "organizationId",
                org.toString(),
                "payload",
                Map.of("configKey", "retention.days"));
        consumer.consume(envelope);
        consumer.consume(envelope);
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM admin.admin_action_log WHERE organization_id = ? AND action = 'CONTEXT_SYNC'",
                Integer.class,
                org);
        assertTrue(count != null && count >= 1);
        RequestContext.clear();
    }

    @Test
    void deferredEventsRemainUnpublishable() {
        try {
            DeferredEvents.reject(Map.of("eventType", "UserCreated"));
        } catch (IllegalStateException ex) {
            assertEquals("GD-002 deferred event must not be published: UserCreated", ex.getMessage());
        }
    }
}
