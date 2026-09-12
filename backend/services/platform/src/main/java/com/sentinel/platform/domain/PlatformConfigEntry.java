package com.sentinel.platform.domain;

import java.time.Instant;
import java.util.UUID;

public record PlatformConfigEntry(
        UUID id,
        UUID organizationId,
        String configKey,
        String configValueJson,
        int version,
        Instant createdAt,
        Instant updatedAt,
        UUID createdBy,
        UUID updatedBy) {}
