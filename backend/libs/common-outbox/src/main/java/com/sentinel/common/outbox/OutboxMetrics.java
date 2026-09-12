package com.sentinel.common.outbox;

import java.util.concurrent.atomic.AtomicLong;

/** In-process counters for M3 (ObservabilityImplementationPlan: outbox backlog / DLQ depth via repository). */
public final class OutboxMetrics {

    private final AtomicLong created = new AtomicLong();
    private final AtomicLong attempts = new AtomicLong();
    private final AtomicLong published = new AtomicLong();
    private final AtomicLong failed = new AtomicLong();
    private final AtomicLong deadLettered = new AtomicLong();

    public void recordCreated() {
        created.incrementAndGet();
    }

    public void recordAttempt() {
        attempts.incrementAndGet();
    }

    public void recordPublished() {
        published.incrementAndGet();
    }

    public void recordFailed() {
        failed.incrementAndGet();
    }

    public void recordDeadLettered() {
        deadLettered.incrementAndGet();
    }

    public long created() {
        return created.get();
    }

    public long attempts() {
        return attempts.get();
    }

    public long published() {
        return published.get();
    }

    public long failed() {
        return failed.get();
    }

    public long deadLettered() {
        return deadLettered.get();
    }
}
