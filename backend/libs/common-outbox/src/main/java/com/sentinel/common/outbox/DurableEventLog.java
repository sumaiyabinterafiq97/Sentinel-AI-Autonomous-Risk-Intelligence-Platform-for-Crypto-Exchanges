package com.sentinel.common.outbox;

import java.util.Map;

/**
 * Vendor-neutral durable log. M3 uses an in-process simulation; a Kafka-compatible
 * broker remains deferred (ADR-015 / ADR-019) until an ops product ADR.
 */
public interface DurableEventLog {

    void append(String stream, Map<String, Object> envelope);
}
