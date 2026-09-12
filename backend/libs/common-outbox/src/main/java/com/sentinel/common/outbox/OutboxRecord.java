package com.sentinel.common.outbox;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record OutboxRecord(
        UUID eventId,
        String eventType,
        String schemaVersion,
        UUID organizationId,
        String producer,
        UUID correlationId,
        Map<String, Object> envelope,
        Instant createdAt,
        String status,
        int attemptCount,
        Instant nextAttemptAt,
        Instant claimedAt,
        Instant publishedAt,
        String lastError) {}
