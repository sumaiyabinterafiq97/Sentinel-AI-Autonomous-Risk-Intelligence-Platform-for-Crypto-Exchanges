package com.sentinel.common.outbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OutboxUnitTest {

    @Test
    void milestoneIsM3TransactionalOutbox() {
        assertEquals("M3", OutboxScaffolding.MILESTONE);
        assertEquals("transactional-outbox-durable-log", OutboxScaffolding.PATTERN);
    }

    @Test
    void streamNamesFollowBrokerArchitecture() {
        assertEquals(
                "sentinel.core.ConfigurationUpdated.v1",
                EventStreams.of(Map.of("eventType", "ConfigurationUpdated", "schemaVersion", "1.0")));
        assertEquals(
                "sentinel.auth.UserLoggedIn.v1",
                EventStreams.of(Map.of("eventType", "UserLoggedIn", "schemaVersion", "1.0")));
        assertEquals(
                "sentinel.alert.AlertCreated.v1",
                EventStreams.of(Map.of("eventType", "AlertCreated", "schemaVersion", "1.0")));
        assertEquals(
                "sentinel.invest.CaseCreated.v1",
                EventStreams.of(Map.of("eventType", "CaseCreated", "schemaVersion", "1.0")));
        assertEquals(
                "sentinel.comp.ComplianceReviewed.v1",
                EventStreams.of(Map.of("eventType", "ComplianceReviewed", "schemaVersion", "1.0")));
        assertEquals(
                "sentinel.ai.AIRecommendationGenerated.v1",
                EventStreams.of(Map.of("eventType", "AIRecommendationGenerated", "schemaVersion", "1.0")));
        assertEquals(
                "sentinel.ai.PromptUpdated.v1",
                EventStreams.of(Map.of("eventType", "PromptUpdated", "schemaVersion", "1.0")));
        assertEquals(
                "sentinel.admin.AdminSettingUpdated.v1",
                EventStreams.of(Map.of("eventType", "AdminSettingUpdated", "schemaVersion", "1.0")));
        assertEquals(
                "sentinel.admin.IntegrationConfigured.v1",
                EventStreams.of(Map.of("eventType", "IntegrationConfigured", "schemaVersion", "1.0")));
        assertEquals(
                "sentinel.admin.AdminActionPerformed.v1",
                EventStreams.of(Map.of("eventType", "AdminActionPerformed", "schemaVersion", "1.0")));
        assertEquals(
                "sentinel.core.ConfigurationUpdated.v1.dlq",
                EventStreams.dlq("sentinel.core.ConfigurationUpdated.v1"));
    }

    @Test
    void deferredEventsAreRejected() {
        assertThrows(
                IllegalStateException.class,
                () -> DeferredEvents.reject(Map.of("eventType", "PlatformStarted")));
    }

    @Test
    void onlyAuthorizedOutboxTablesAreAccepted() {
        assertEquals("core.outbox_events", QualifiedOutboxTable.requireAllowed("core.outbox_events"));
        assertEquals("auth.outbox_events", QualifiedOutboxTable.requireAllowed("auth.outbox_events"));
        assertEquals("risk.outbox_events", QualifiedOutboxTable.requireAllowed("risk.outbox_events"));
        assertThrows(IllegalArgumentException.class, () -> QualifiedOutboxTable.requireAllowed("alert.outbox_events"));
    }

    @Test
    void consumerSkeletonIsIdempotentOnEventId() {
        IdempotentConsumerSkeleton consumers = new IdempotentConsumerSkeleton();
        UUID eventId = UUID.randomUUID();
        Map<String, Object> envelope = Map.of("eventId", eventId.toString());
        assertTrue(consumers.accept(envelope));
        assertEquals(false, consumers.accept(envelope));
        assertEquals(1, consumers.duplicateCount(eventId));
        assertTrue(consumers.seen(eventId));
    }

    @Test
    void backoffIsExponential() {
        OutboxProperties properties = OutboxProperties.simulation("core.outbox_events");
        assertEquals(Duration.ofMillis(200), properties.backoff(1));
        assertEquals(Duration.ofMillis(400), properties.backoff(2));
        assertEquals(Duration.ofMillis(800), properties.backoff(3));
    }
}
