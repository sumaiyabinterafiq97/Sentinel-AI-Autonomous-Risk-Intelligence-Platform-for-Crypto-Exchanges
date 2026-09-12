package com.sentinel.platform.domain;

import java.time.Instant;
import java.util.UUID;

public record AdminSettingRow(
        UUID id,
        UUID organizationId,
        String settingKey,
        String settingValueJson,
        UUID updatedBy,
        Instant updatedAt,
        Instant createdAt) {}
