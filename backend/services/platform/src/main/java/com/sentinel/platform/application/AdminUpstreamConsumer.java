package com.sentinel.platform.application;

import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import com.sentinel.common.outbox.DeferredEvents;
import com.sentinel.platform.api.RequestContext;

@Component
public class AdminUpstreamConsumer {

    private final AdminService admin;

    public AdminUpstreamConsumer(AdminService admin) {
        this.admin = admin;
    }

    public void consume(Map<String, Object> envelope) {
        if (envelope == null || envelope.get("eventType") == null) {
            return;
        }
        String type = String.valueOf(envelope.get("eventType"));
        if ("PlatformStarted".equals(type) || "PlatformUnavailable".equals(type) || "AgentRunFailed".equals(type)) {
            return;
        }
        UUID org = parseUuid(envelope.get("organizationId"));
        if (org == null) {
            return;
        }
        RequestContext ctx = RequestContext.get();
        ctx.setOrganizationId(org);
        Object payload = envelope.get("payload");
        String target = payload instanceof Map<?, ?> map && map.get("configKey") != null
                ? String.valueOf(map.get("configKey"))
                : type;
        switch (type) {
            case "ConfigurationUpdated" -> admin.recordConsumedContext(
                    "CONTEXT_SYNC", "CONFIGURATION", parseUuid(envelope.get("eventId")), org, Map.of("source", type, "key", target));
            case "UserCreated", "OrganizationUpdated", "RoleAssigned" -> admin.recordConsumedContext(
                    "CONTEXT_SYNC", type.toUpperCase(), parseUuid(envelope.get("eventId")), org, Map.of("source", type));
            default -> {
                // ADMIN does not consume other domains.
            }
        }
    }

    public static void assertNotDeferredPublish(Map<String, Object> envelope) {
        DeferredEvents.reject(envelope);
    }

    private static UUID parseUuid(Object raw) {
        if (raw == null) {
            return null;
        }
        try {
            return UUID.fromString(String.valueOf(raw));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
