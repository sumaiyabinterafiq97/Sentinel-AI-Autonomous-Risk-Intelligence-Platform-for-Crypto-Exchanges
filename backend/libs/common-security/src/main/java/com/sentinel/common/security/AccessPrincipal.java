package com.sentinel.common.security;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AccessPrincipal(
        UUID userId, UUID organizationId, UUID sessionId, Set<String> permissions, Instant expiresAt) {

    public boolean hasPermission(String permission) {
        return permission != null && permissions.contains(permission);
    }
}
