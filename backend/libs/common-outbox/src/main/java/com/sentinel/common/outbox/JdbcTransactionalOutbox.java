package com.sentinel.common.outbox;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public final class JdbcTransactionalOutbox implements TransactionalOutbox {

    private final JdbcOutboxRepository repository;
    private final OutboxRelay relay;
    private final OutboxMetrics metrics;

    public JdbcTransactionalOutbox(JdbcOutboxRepository repository, OutboxRelay relay, OutboxMetrics metrics) {
        this.repository = repository;
        this.relay = relay;
        this.metrics = metrics;
    }

    @Override
    public UUID record(Map<String, Object> envelope) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Outbox record requires an active database transaction");
        }
        DeferredEvents.reject(envelope);
        requireEnvelope(envelope);
        Instant now = Instant.now();
        UUID eventId = UUID.fromString(String.valueOf(envelope.get("eventId")));
        UUID organizationId = UUID.fromString(String.valueOf(envelope.get("organizationId")));
        UUID correlationId = envelope.get("correlationId") == null
                ? null
                : UUID.fromString(String.valueOf(envelope.get("correlationId")));
        repository.insert(new OutboxRecord(
                eventId,
                String.valueOf(envelope.get("eventType")),
                String.valueOf(envelope.get("schemaVersion")),
                organizationId,
                String.valueOf(envelope.get("producer")),
                correlationId,
                envelope,
                now,
                OutboxStatus.PENDING,
                0,
                now,
                null,
                null,
                null));
        metrics.recordCreated();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                relay.publishAvailable(Instant.now());
            }
        });
        return eventId;
    }

    private static void requireEnvelope(Map<String, Object> envelope) {
        for (String field : new String[] {
            "eventId", "eventType", "schemaVersion", "timestamp", "producer", "correlationId", "organizationId", "payload"
        }) {
            if (envelope.get(field) == null || String.valueOf(envelope.get(field)).isBlank()) {
                throw new IllegalArgumentException("Event envelope missing required field: " + field);
            }
        }
    }
}
