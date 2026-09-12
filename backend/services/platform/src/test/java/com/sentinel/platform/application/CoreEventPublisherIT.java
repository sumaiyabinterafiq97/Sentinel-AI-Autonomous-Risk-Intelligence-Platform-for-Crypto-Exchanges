package com.sentinel.platform.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentinel.platform.PlatformPostgresIT;
import com.sentinel.platform.api.RequestContext;
import com.sentinel.platform.infrastructure.InProcessCoreEventBus;

@SpringBootTest
class CoreEventPublisherIT extends PlatformPostgresIT {

    @Autowired
    private PlatformConfigService configs;

    @Autowired
    private InProcessCoreEventBus bus;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlatformHealthService health;

    @org.junit.jupiter.api.AfterEach
    void cleanup() {
        RequestContext.clear();
    }

    @BeforeEach
    void reset() {
        bus.reset();
        RequestContext.clear();
        RequestContext ctx = RequestContext.get();
        ctx.setOrganizationId(UUID.randomUUID());
        ctx.setRequestId(UUID.randomUUID());
        ctx.setCorrelationId(UUID.randomUUID());
        ctx.setActorId(UUID.randomUUID());
        ctx.setActorType("user");
    }

    @Test
    void configurationUpdatedMatchesContractShape() throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("theme.name", "dark");
        configs.patchConfig(RequestContext.get().organizationId(), body);
        var published = bus.snapshot().stream()
                .filter(e -> "ConfigurationUpdated".equals(e.envelope().get("eventType")))
                .findFirst()
                .orElseThrow();
        Map<String, Object> envelope = published.envelope();
        assertEquals("SUCCESS", published.publicationOutcome());
        assertTrue(envelope.keySet()
                .containsAll(Set.of(
                        "eventId",
                        "eventType",
                        "schemaVersion",
                        "timestamp",
                        "producer",
                        "correlationId",
                        "organizationId",
                        "payload")));
        assertEquals("1.0", envelope.get("schemaVersion"));
        assertEquals("sentinel-platform", envelope.get("producer"));
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) envelope.get("payload");
        assertEquals("theme.name", payload.get("configKey"));
        assertTrue(payload.containsKey("updatedAt"));
        assertFalse(payload.containsKey("secret"));
        Set<String> payloadKeys = payload.keySet();
        assertTrue(Set.of("configKey", "organizationId", "version", "updatedAt", "updatedBy").containsAll(payloadKeys));
    }

    @Test
    void gd002LifecycleEventsAreNotPublished() {
        assertTrue(health.ready());
        assertTrue(bus.snapshot().stream()
                .noneMatch(e -> Set.of("PlatformStarted", "PlatformUnavailable").contains(String.valueOf(e.envelope().get("eventType")))));
    }
}
