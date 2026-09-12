package com.sentinel.dash.application;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.sentinel.common.security.AccessPrincipal;
import com.sentinel.dash.domain.DashException;
import com.sentinel.dash.infrastructure.DashStore;

@Service
public class DashWorkspaceService {

    public static final UUID WIDGET_ALERTS = UUID.fromString("a1000000-0000-4000-8000-000000000001");
    public static final UUID WIDGET_CASES = UUID.fromString("a1000000-0000-4000-8000-000000000002");
    public static final UUID WIDGET_RISK = UUID.fromString("a1000000-0000-4000-8000-000000000003");
    public static final UUID WIDGET_AI = UUID.fromString("a1000000-0000-4000-8000-000000000004");

    private static final Set<String> QUEUES = Set.of("alerts", "cases");

    private final DashStore store;

    public DashWorkspaceService(DashStore store) {
        this.store = store;
    }

    public Map<String, Object> workspace(AccessPrincipal principal, UUID orgId) {
        store.touchPreferences(principal.userId(), orgId);
        List<String> queues = new ArrayList<>();
        if (principal.hasPermission("alert:alert:read") || principal.hasPermission("dash:queue:read")) {
            queues.add("alerts");
        }
        if (principal.hasPermission("invest:case:read") || principal.hasPermission("dash:queue:read")) {
            queues.add("cases");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("role", roleOf(principal));
        data.put("queues", queues);
        return data;
    }

    public Map<String, Object> dashboard(AccessPrincipal principal, UUID orgId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("role", roleOf(principal));
        data.put("alertCount", store.listProjections(orgId, "alert:").size());
        data.put("caseCount", store.listProjections(orgId, "case:").size());
        data.put("assessmentCount", store.listProjections(orgId, "assessment:").size());
        List<Map<String, Object>> ai = store.listProjections(orgId, "ai:");
        if (!ai.isEmpty()) {
            Map<String, Object> latest = map(ai.get(0).get("payload"));
            data.put(
                    "aiAssist",
                    Map.of(
                            "layer",
                            "AI",
                            "recommendationId",
                            latest.getOrDefault("recommendationId", ""),
                            "status",
                            latest.getOrDefault("status", "completed"),
                            "disclaimer",
                            "Assistive only — not a system result or human decision."));
        }
        return data;
    }

    public List<Map<String, Object>> queue(UUID orgId, String queueType) {
        if ("compliance".equals(queueType)) {
            return List.of();
        }
        if (!QUEUES.contains(queueType)) {
            throw DashException.notFound("Unknown queue");
        }
        String prefix = "alerts".equals(queueType) ? "alert:" : "case:";
        List<Map<String, Object>> items = new ArrayList<>();
        for (Map<String, Object> row : store.listProjections(orgId, prefix)) {
            Map<String, Object> payload = map(row.get("payload"));
            Map<String, Object> item = new LinkedHashMap<>();
            Object id = payload.get("alertId") != null ? payload.get("alertId") : payload.get("caseId");
            item.put("id", id);
            item.put("type", payload.getOrDefault("type", queueType));
            item.put("title", payload.getOrDefault("title", String.valueOf(id)));
            Object priority = payload.get("priority");
            item.put("priority", priority instanceof Number n ? n.intValue() : 0);
            items.add(item);
        }
        return items;
    }

    public List<Map<String, Object>> widgets(UUID orgId) {
        int alerts = store.listProjections(orgId, "alert:").size();
        int cases = store.listProjections(orgId, "case:").size();
        return List.of(
                widget(WIDGET_ALERTS, "alerts", "Open alerts (" + alerts + ")"),
                widget(WIDGET_CASES, "cases", "Open cases (" + cases + ")"),
                widget(WIDGET_RISK, "risk", "Risk assessments"),
                widget(WIDGET_AI, "ai", "AI assist (non-authoritative)"));
    }

    public Map<String, Object> interact(AccessPrincipal principal, UUID orgId, UUID widgetId, String type) {
        if (type == null || type.isBlank()) {
            throw DashException.validation("interactionType", "interactionType is required");
        }
        UUID id = store.logInteraction(principal.userId(), orgId, widgetId.toString(), type);
        return Map.of("id", id.toString(), "logged", true);
    }

    public Map<String, Object> projection(UUID orgId, String key) {
        return store.getProjection(orgId, key);
    }

    private static Map<String, Object> widget(UUID id, String type, String title) {
        Map<String, Object> w = new LinkedHashMap<>();
        w.put("id", id.toString());
        w.put("type", type);
        w.put("title", title);
        return w;
    }

    static String roleOf(AccessPrincipal principal) {
        if (principal.hasPermission("org:org:write") || principal.hasPermission("platform:config:write")) {
            return "administrator";
        }
        if (principal.hasPermission("comp:kyc:write") || principal.hasPermission("comp:kyc:approve")) {
            return "compliance_officer";
        }
        return "risk_analyst";
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        if (value instanceof Map<?, ?> m) {
            return (Map<String, Object>) m;
        }
        return Map.of();
    }
}
