package com.sentinel.common.outbox;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * M3 consumer skeleton: in-memory processed {@code eventId} set.
 * Domain {@code processed_event_ids} tables (e.g. AI) belong to later milestones.
 * Duplicate delivery is acked with no additional side effect.
 */
public final class IdempotentConsumerSkeleton {

    private final ConcurrentHashMap<UUID, Integer> processed = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Integer> duplicates = new ConcurrentHashMap<>();

    public boolean accept(Map<String, Object> envelope) {
        UUID eventId = UUID.fromString(String.valueOf(envelope.get("eventId")));
        Integer prior = processed.putIfAbsent(eventId, 1);
        if (prior != null) {
            duplicates.merge(eventId, 1, Integer::sum);
            return false;
        }
        return true;
    }

    public boolean seen(UUID eventId) {
        return processed.containsKey(eventId);
    }

    public int duplicateCount(UUID eventId) {
        return duplicates.getOrDefault(eventId, 0);
    }

    public void reset() {
        processed.clear();
        duplicates.clear();
    }
}
