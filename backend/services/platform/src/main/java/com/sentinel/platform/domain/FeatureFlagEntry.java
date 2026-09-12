package com.sentinel.platform.domain;

import java.time.Instant;
import java.util.UUID;

public record FeatureFlagEntry(
        UUID id,
        UUID organizationId,
        String flagKey,
        boolean enabled,
        String metadataJson,
        Instant updatedAt) {}
