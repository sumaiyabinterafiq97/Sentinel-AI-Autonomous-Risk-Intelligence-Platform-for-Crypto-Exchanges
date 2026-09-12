package com.sentinel.common.outbox;

import java.time.Duration;

/**
 * Relay tunables. Numeric defaults are simulation values — EventContracts only requires
 * configurable exponential backoff and a maximum attempt count.
 */
public record OutboxProperties(
        String table,
        int maxAttempts,
        Duration backoffBase,
        Duration claimTimeout,
        int batchSize,
        boolean pollEnabled) {

    public OutboxProperties {
        table = QualifiedOutboxTable.requireAllowed(table);
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize must be >= 1");
        }
    }

    public Duration backoff(int attemptCount) {
        int shift = Math.min(Math.max(attemptCount - 1, 0), 16);
        return backoffBase.multipliedBy(1L << shift);
    }

    public static OutboxProperties simulation(String table) {
        return new OutboxProperties(table, 5, Duration.ofMillis(200), Duration.ofSeconds(30), 25, true);
    }
}
