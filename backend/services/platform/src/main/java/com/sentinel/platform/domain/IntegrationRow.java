package com.sentinel.platform.domain;

import java.time.Instant;
import java.util.UUID;

public record IntegrationRow(
        UUID id,
        UUID organizationId,
        String integrationType,
        String configJson,
        String secretRef,
        String status,
        UUID configuredBy,
        Instant configuredAt,
        Instant updatedAt) {}
