package com.sentinel.common.outbox;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Polls the outbox and appends to the durable-log abstraction (at-least-once).
 * Simulation broker: {@link InMemoryDurableEventLog}.
 */
public final class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final JdbcOutboxRepository repository;
    private final DurableEventLog durableLog;
    private final OutboxProperties properties;
    private final OutboxMetrics metrics;
    private final TransactionTemplate transaction;

    public OutboxRelay(
            JdbcOutboxRepository repository,
            DurableEventLog durableLog,
            OutboxProperties properties,
            OutboxMetrics metrics,
            PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.durableLog = durableLog;
        this.properties = properties;
        this.metrics = metrics;
        this.transaction = new TransactionTemplate(transactionManager);
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public int publishAvailable(Instant now) {
        List<OutboxRecord> claimed = transaction.execute(status -> repository.claim(
                now, properties.batchSize(), properties.claimTimeout()));
        if (claimed == null || claimed.isEmpty()) {
            return 0;
        }
        int delivered = 0;
        for (OutboxRecord record : claimed) {
            metrics.recordAttempt();
            UUID eventId = record.eventId();
            Map<String, Object> envelope = record.envelope();
            if (!isWellFormed(envelope, eventId)) {
                transaction.executeWithoutResult(status -> quarantine(record, now, "malformed-envelope"));
                continue;
            }
            String stream = EventStreams.of(envelope);
            try {
                durableLog.append(stream, envelope);
                transaction.executeWithoutResult(status -> repository.markPublished(eventId, now));
                metrics.recordPublished();
                delivered++;
                log.info(
                        "outbox delivered eventId={} eventType={} organizationId={} correlationId={} stream={} attempt={}",
                        eventId,
                        record.eventType(),
                        record.organizationId(),
                        record.correlationId(),
                        stream,
                        record.attemptCount());
            } catch (RuntimeException ex) {
                int attempts = record.attemptCount();
                metrics.recordFailed();
                if (attempts >= properties.maxAttempts()) {
                    transaction.executeWithoutResult(status -> quarantine(record, now, ex.getClass().getSimpleName()));
                } else {
                    Instant next = now.plus(properties.backoff(attempts));
                    transaction.executeWithoutResult(
                            status -> repository.markRetry(eventId, attempts, next, ex.getClass().getSimpleName()));
                    log.warn(
                            "outbox retry scheduled eventId={} eventType={} organizationId={} attempt={} nextAttemptAt={}",
                            eventId,
                            record.eventType(),
                            record.organizationId(),
                            attempts,
                            next);
                }
            }
        }
        return delivered;
    }

    public void poll() {
        if (!properties.pollEnabled()) {
            return;
        }
        publishAvailable(Instant.now());
    }

    private void quarantine(OutboxRecord record, Instant now, String reason) {
        repository.markDead(record.eventId(), record.attemptCount(), now, reason);
        metrics.recordDeadLettered();
        Map<String, Object> envelope = record.envelope();
        String stream = EventStreams.dlq(EventStreams.of(envelope));
        try {
            durableLog.append(stream, envelope);
        } catch (RuntimeException ex) {
            log.error(
                    "outbox dlq append failed eventId={} eventType={} organizationId={}",
                    record.eventId(),
                    record.eventType(),
                    record.organizationId());
        }
        log.error(
                "outbox dead-lettered eventId={} eventType={} organizationId={} reason={} attempts={}",
                record.eventId(),
                record.eventType(),
                record.organizationId(),
                reason,
                record.attemptCount());
    }

    private static boolean isWellFormed(Map<String, Object> envelope, UUID eventId) {
        if (envelope == null || envelope.get("eventId") == null) {
            return false;
        }
        try {
            return eventId.equals(UUID.fromString(String.valueOf(envelope.get("eventId"))));
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
