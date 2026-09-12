package com.sentinel.platform.domain;

import java.time.Instant;
import java.util.UUID;

public record AdminAuditItem(UUID id, String action, UUID actorId, Instant occurredAt) {}
