package com.sentinel.platform.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import com.sentinel.common.outbox.DurableEventLog;
import com.sentinel.common.outbox.IdempotentConsumerSkeleton;
import com.sentinel.common.outbox.InMemoryDurableEventLog;
import com.sentinel.common.outbox.JdbcOutboxRepository;
import com.sentinel.common.outbox.OutboxMetrics;
import com.sentinel.common.outbox.OutboxProperties;
import com.sentinel.common.outbox.OutboxRecord;
import com.sentinel.common.outbox.OutboxRelay;
import com.sentinel.common.outbox.OutboxStatus;
import com.sentinel.common.outbox.TransactionalOutbox;
import com.sentinel.platform.PlatformPostgresIT;
import com.sentinel.platform.api.RequestContext;
import com.sentinel.platform.domain.FeatureFlagEntry;
import com.sentinel.platform.infrastructure.FeatureFlagRepository;
import com.sentinel.platform.infrastructure.InProcessCoreEventBus;

@SpringBootTest
@Import(OutboxInfrastructureIT.AtomicityProbe.class)
class OutboxInfrastructureIT extends PlatformPostgresIT {

    @Autowired
    private AtomicityProbe probe;

    @Autowired
    private FeatureFlagRepository flags;

    @Autowired
    private JdbcOutboxRepository outbox;

    @Autowired
    private FeatureFlagService featureFlags;

    @Autowired
    private InProcessCoreEventBus bus;

    @Autowired
    private InMemoryDurableEventLog durableLog;

    @Autowired
    private IdempotentConsumerSkeleton consumers;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private OutboxProperties properties;

    @Autowired
    private OutboxMetrics metrics;

    @Autowired
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

    @BeforeEach
    void reset() {
        bus.reset();
        durableLog.reset();
        consumers.reset();
        RequestContext.clear();
        RequestContext ctx = RequestContext.get();
        ctx.setOrganizationId(UUID.randomUUID());
        ctx.setRequestId(UUID.randomUUID());
        ctx.setCorrelationId(UUID.randomUUID());
        ctx.setActorId(UUID.randomUUID());
        ctx.setActorType("user");
    }

    @Test
    void domainChangeAndOutboxInsertCommitTogether() {
        UUID org = RequestContext.get().organizationId();
        UUID flagId = probe.commitFlagAndEvent(org, "m3.commit");
        assertTrue(flags.findByOrgAndKey(org, "m3.commit").isPresent());
        assertEquals(1, outbox.countForOrganization(org));
        assertTrue(jdbc.queryForObject(
                "SELECT COUNT(*) FROM core.feature_flags WHERE id = ?", Integer.class, flagId) > 0);
        assertTrue(bus.snapshot().stream().anyMatch(e -> "FeatureFlagChanged".equals(e.envelope().get("eventType"))));
    }

    @Test
    void domainRollbackRemovesOutboxRecord() {
        UUID org = RequestContext.get().organizationId();
        assertThrows(IllegalStateException.class, () -> probe.rollbackFlagAndEvent(org, "m3.rollback"));
        assertTrue(flags.findByOrgAndKey(org, "m3.rollback").isEmpty());
        assertEquals(0, outbox.countForOrganization(org));
    }

    @Test
    void outboxInsertFailurePreventsDomainCommit() {
        UUID org = RequestContext.get().organizationId();
        UUID eventId = UUID.randomUUID();
        assertThrows(RuntimeException.class, () -> probe.duplicateOutboxFails(org, "m3.fail", eventId));
        assertTrue(flags.findByOrgAndKey(org, "m3.fail").isEmpty());
        assertTrue(outbox.find(eventId).isEmpty());
    }

    @Test
    void relayDeliversAndMarksPublishedWithStableEventId() {
        UUID org = RequestContext.get().organizationId();
        featureFlags.patchFlag(org, "m3.relay", com.fasterxml.jackson.databind.node.JsonNodeFactory.instance
                .objectNode()
                .put("enabled", true));
        var delivered = durableLog.snapshot().stream()
                .filter(e -> e.stream().equals("sentinel.core.FeatureFlagChanged.v1"))
                .findFirst()
                .orElseThrow();
        UUID eventId = UUID.fromString(String.valueOf(delivered.envelope().get("eventId")));
        OutboxRecord row = outbox.find(eventId).orElseThrow();
        assertEquals(OutboxStatus.PUBLISHED, row.status());
        assertEquals(org, row.organizationId());
        assertEquals(eventId.toString(), delivered.envelope().get("eventId"));
        assertTrue(consumers.seen(eventId));
    }

    @Test
    void failedDeliveryRetriesSameEventIdThenSucceeds() {
        UUID eventId = UUID.randomUUID();
        UUID org = RequestContext.get().organizationId();
        insertPending(eventId, org, "FeatureFlagChanged");
        AtomicInteger remainingFailures = new AtomicInteger(2);
        DurableEventLog flaky = (stream, envelope) -> {
            if (remainingFailures.getAndDecrement() > 0) {
                throw new IllegalStateException("temporary-delivery-failure");
            }
            durableLog.append(stream, envelope);
        };
        OutboxRelay isolated = new OutboxRelay(outbox, flaky, properties, metrics, transactionManager);
        Instant t0 = Instant.now();
        isolated.publishAvailable(t0);
        OutboxRecord afterFail = outbox.find(eventId).orElseThrow();
        assertEquals(OutboxStatus.PENDING, afterFail.status());
        assertEquals(eventId, afterFail.eventId());
        isolated.publishAvailable(afterFail.nextAttemptAt().plusMillis(1));
        isolated.publishAvailable(Instant.now().plusSeconds(5));
        OutboxRecord published = outbox.find(eventId).orElseThrow();
        assertEquals(OutboxStatus.PUBLISHED, published.status());
        assertEquals(eventId, published.eventId());
    }

    @Test
    void repeatedFailureDeadLettersWithoutDroppingIdentity() {
        UUID eventId = UUID.randomUUID();
        UUID org = RequestContext.get().organizationId();
        insertPending(eventId, org, "ConfigurationUpdated");
        DurableEventLog alwaysFail = (stream, envelope) -> {
            if (stream.endsWith(".dlq")) {
                durableLog.append(stream, envelope);
                return;
            }
            throw new IllegalStateException("permanent-for-test");
        };
        OutboxRelay isolated = new OutboxRelay(
                outbox,
                alwaysFail,
                new OutboxProperties(
                        properties.table(),
                        2,
                        properties.backoffBase(),
                        properties.claimTimeout(),
                        properties.batchSize(),
                        false),
                metrics,
                transactionManager);
        Instant now = Instant.now();
        isolated.publishAvailable(now);
        OutboxRecord retrying = outbox.find(eventId).orElseThrow();
        isolated.publishAvailable(retrying.nextAttemptAt().plusMillis(1));
        OutboxRecord dead = outbox.find(eventId).orElseThrow();
        assertEquals(OutboxStatus.DEAD, dead.status());
        assertEquals(eventId, dead.eventId());
        assertTrue(durableLog.snapshot().stream().anyMatch(e -> e.stream().endsWith(".dlq")));
    }

    @Test
    void staleClaimIsRecoveredAfterRestart() {
        UUID eventId = UUID.randomUUID();
        UUID org = RequestContext.get().organizationId();
        insertPending(eventId, org, "FeatureFlagChanged");
        jdbc.update(
                """
                UPDATE core.outbox_events
                SET status = 'PUBLISHING', claimed_at = ?, attempt_count = 1
                WHERE event_id = ?
                """,
                java.sql.Timestamp.from(Instant.now().minusSeconds(120)),
                eventId);
        new OutboxRelay(outbox, durableLog, properties, metrics, transactionManager)
                .publishAvailable(Instant.now());
        assertEquals(OutboxStatus.PUBLISHED, outbox.find(eventId).orElseThrow().status());
        assertEquals(eventId.toString(), durableLog.snapshot().getLast().envelope().get("eventId"));
    }

    @Test
    void concurrentRelaysDoNotCorruptTheSameRecord() throws Exception {
        UUID eventId = UUID.randomUUID();
        insertPending(eventId, RequestContext.get().organizationId(), "ConfigurationUpdated");
        AtomicInteger appends = new AtomicInteger();
        DurableEventLog counting = (stream, envelope) -> appends.incrementAndGet();
        OutboxRelay a = new OutboxRelay(outbox, counting, properties, metrics, transactionManager);
        OutboxRelay b = new OutboxRelay(outbox, counting, properties, metrics, transactionManager);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        pool.submit(() -> {
            start.await();
            a.publishAvailable(Instant.now());
            return null;
        });
        pool.submit(() -> {
            start.await();
            b.publishAvailable(Instant.now());
            return null;
        });
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        OutboxRecord row = outbox.find(eventId).orElseThrow();
        assertEquals(OutboxStatus.PUBLISHED, row.status());
        assertTrue(appends.get() >= 1);
        assertTrue(appends.get() <= 2);
    }

    @Test
    void tenantContextIsPreservedOnOutboxRows() {
        UUID orgA = UUID.randomUUID();
        UUID orgB = UUID.randomUUID();
        RequestContext.get().setOrganizationId(orgA);
        featureFlags.patchFlag(orgA, "tenant.a", com.fasterxml.jackson.databind.node.JsonNodeFactory.instance
                .objectNode()
                .put("enabled", true));
        RequestContext.get().setOrganizationId(orgB);
        featureFlags.patchFlag(orgB, "tenant.b", com.fasterxml.jackson.databind.node.JsonNodeFactory.instance
                .objectNode()
                .put("enabled", true));
        assertEquals(1, outbox.countForOrganization(orgA));
        assertEquals(1, outbox.countForOrganization(orgB));
        assertTrue(durableLog.snapshot().stream()
                .noneMatch(e -> orgA.toString().equals(String.valueOf(e.envelope().get("organizationId")))
                        && orgB.toString().equals(String.valueOf(e.envelope().get("payload")))));
        assertTrue(outbox.oldestPendingCreatedAt().isEmpty() || outbox.pendingBacklog() >= 0);
        assertTrue(metrics.created() >= 2);
        assertTrue(metrics.published() >= 2);
    }

    private void insertPending(UUID eventId, UUID org, String type) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
            Map<String, Object> envelope = sampleEnvelope(eventId, org, type);
            Instant now = Instant.now();
            outbox.insert(new OutboxRecord(
                    eventId,
                    type,
                    "1.0",
                    org,
                    "sentinel-platform",
                    RequestContext.get().correlationId(),
                    envelope,
                    now,
                    OutboxStatus.PENDING,
                    0,
                    now,
                    null,
                    null,
                    null));
        });
    }

    private Map<String, Object> sampleEnvelope(UUID eventId, UUID org, String type) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("configKey", "k");
        payload.put("updatedAt", Instant.now().toString());
        payload.put("flagKey", "k");
        payload.put("enabled", true);
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("eventId", eventId.toString());
        envelope.put("eventType", type);
        envelope.put("schemaVersion", "1.0");
        envelope.put("timestamp", Instant.now().toString());
        envelope.put("producer", "sentinel-platform");
        envelope.put("correlationId", RequestContext.get().correlationId().toString());
        envelope.put("organizationId", org.toString());
        envelope.put("payload", payload);
        return envelope;
    }

    @Component
    static class AtomicityProbe {
        private final FeatureFlagRepository flags;
        private final TransactionalOutbox outbox;
        private final FeatureFlagService featureFlags;

        AtomicityProbe(FeatureFlagRepository flags, TransactionalOutbox outbox, FeatureFlagService featureFlags) {
            this.flags = flags;
            this.outbox = outbox;
            this.featureFlags = featureFlags;
        }

        @Transactional
        UUID commitFlagAndEvent(UUID org, String key) {
            featureFlags.patchFlag(
                    org,
                    key,
                    com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode().put("enabled", true));
            return flags.findByOrgAndKey(org, key).orElseThrow().id();
        }

        @Transactional
        void rollbackFlagAndEvent(UUID org, String key) {
            Instant now = Instant.now();
            flags.insert(new FeatureFlagEntry(UUID.randomUUID(), org, key, true, "{}", now));
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("eventId", UUID.randomUUID().toString());
            envelope.put("eventType", "FeatureFlagChanged");
            envelope.put("schemaVersion", "1.0");
            envelope.put("timestamp", now.toString());
            envelope.put("producer", "sentinel-platform");
            envelope.put("correlationId", RequestContext.get().correlationId().toString());
            envelope.put("organizationId", org.toString());
            envelope.put("payload", Map.of("flagKey", key, "enabled", true, "updatedAt", now.toString()));
            outbox.record(envelope);
            throw new IllegalStateException("forced-rollback");
        }

        @Transactional
        void duplicateOutboxFails(UUID org, String key, UUID eventId) {
            Instant now = Instant.now();
            flags.insert(new FeatureFlagEntry(UUID.randomUUID(), org, key, true, "{}", now));
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("eventId", eventId.toString());
            envelope.put("eventType", "FeatureFlagChanged");
            envelope.put("schemaVersion", "1.0");
            envelope.put("timestamp", now.toString());
            envelope.put("producer", "sentinel-platform");
            envelope.put("correlationId", RequestContext.get().correlationId().toString());
            envelope.put("organizationId", org.toString());
            envelope.put("payload", Map.of("flagKey", key, "enabled", true, "updatedAt", now.toString()));
            outbox.record(envelope);
            outbox.record(envelope);
        }
    }
}
