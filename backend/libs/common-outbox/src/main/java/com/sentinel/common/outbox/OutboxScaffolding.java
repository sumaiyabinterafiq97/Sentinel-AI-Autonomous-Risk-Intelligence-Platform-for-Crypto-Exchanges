package com.sentinel.common.outbox;

/**
 * M3 transactional outbox + event relay (ADR-015).
 * Durable log product remains capability-level; local implementation is an in-memory simulation.
 */
public final class OutboxScaffolding {

    public static final String MILESTONE = "M3";
    public static final String PATTERN = "transactional-outbox-durable-log";

    private OutboxScaffolding() {}
}
