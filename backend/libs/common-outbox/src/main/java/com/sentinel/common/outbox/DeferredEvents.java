package com.sentinel.common.outbox;

import java.util.Map;
import java.util.Set;

/**
 * GD-002: these names have no approved payload schemas and must not be published.
 */
public final class DeferredEvents {

    public static final Set<String> NAMES = Set.of(
            "PlatformStarted",
            "PlatformUnavailable",
            "AgentRunFailed",
            "UserCreated",
            "OrganizationCreated",
            "OrganizationUpdated");

    private DeferredEvents() {}

    public static void reject(Map<String, Object> envelope) {
        Object type = envelope.get("eventType");
        if (type != null && NAMES.contains(String.valueOf(type))) {
            throw new IllegalStateException("GD-002 deferred event must not be published: " + type);
        }
    }
}
