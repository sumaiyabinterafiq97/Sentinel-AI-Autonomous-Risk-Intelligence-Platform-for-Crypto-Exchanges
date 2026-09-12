package com.sentinel.platform.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.platform.api.RequestContext;
import com.sentinel.platform.domain.AdminAuditItem;
import com.sentinel.platform.domain.AdminSettingRow;
import com.sentinel.platform.domain.CoreException;
import com.sentinel.platform.domain.CoreKeys;
import com.sentinel.platform.domain.IntegrationRow;
import com.sentinel.platform.infrastructure.AdminStore;

@Service
public class AdminService {

    private static final Pattern INTEGRATION_TYPE = Pattern.compile("^[A-Za-z][A-Za-z0-9._:-]{0,127}$");
    private static final Set<String> SECRET_KEYS =
            Set.of("password", "secret", "apikey", "api_key", "token", "accesskey", "access_key");
    private static final UUID CURSOR_SENTINEL = UUID.fromString("ffffffff-ffff-4fff-8fff-ffffffffffff");

    private final AdminStore store;
    private final AuditService audit;
    private final AdminEventPublisher events;
    private final IdentityOrchestrator identity;
    private final ObjectMapper objectMapper;

    public AdminService(
            AdminStore store,
            AuditService audit,
            AdminEventPublisher events,
            IdentityOrchestrator identity,
            ObjectMapper objectMapper) {
        this.store = store;
        this.audit = audit;
        this.events = events;
        this.identity = identity;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> getSettings() {
        UUID org = requireOrg();
        Map<String, Object> data = new LinkedHashMap<>();
        for (AdminSettingRow row : store.listSettings(org)) {
            data.put(row.settingKey(), parseJson(row.settingValueJson()));
        }
        return data;
    }

    @Transactional
    public Map<String, Object> patchSettings(JsonNode body) {
        UUID org = requireOrg();
        if (body == null || !body.isObject() || body.isEmpty()) {
            throw CoreException.adminValidation("body", "Settings patch must be a non-empty object");
        }
        RequestContext ctx = RequestContext.get();
        Instant now = Instant.now();
        body.fields().forEachRemaining(field -> {
            String key = field.getKey();
            try {
                CoreKeys.requireConfigKey(key);
            } catch (CoreException ex) {
                throw CoreException.adminValidation(key, ex.getMessage());
            }
            JsonNode value = field.getValue();
            if (value == null || value.isNull()) {
                throw CoreException.adminValidation(key, "Setting value must not be null");
            }
            UUID id = store.findSetting(org, key).map(AdminSettingRow::id).orElse(UUID.randomUUID());
            Instant created = store.findSetting(org, key).map(AdminSettingRow::createdAt).orElse(now);
            store.upsertSetting(new AdminSettingRow(id, org, key, value.toString(), ctx.actorId(), now, created));
            recordAction("SETTING_UPDATED", "SETTING", id, "SUCCESS", Map.of("settingKey", key));
            events.publishSettingUpdated(key, org, now, ctx.actorId());
            events.publishActionPerformed("SETTING_UPDATED", "SETTING", id, ctx.actorId(), now, "SUCCESS", org);
            audit.record("SETTING_UPDATED", "SETTING", id, "SUCCESS", Map.of("settingKey", key));
        });
        return getSettings();
    }

    public List<Map<String, Object>> listIntegrations(String cursor, int limit) {
        UUID org = requireOrg();
        int safe = clampLimit(limit);
        List<Map<String, Object>> data = new ArrayList<>();
        for (IntegrationRow row : store.listIntegrations(org, cursor, safe)) {
            data.add(toIntegrationApi(row));
        }
        return data;
    }

    @Transactional
    public Map<String, Object> createIntegration(JsonNode body) {
        UUID org = requireOrg();
        if (body == null || !body.isObject()) {
            throw CoreException.adminValidation("body", "Integration body is required");
        }
        String type = text(body, "type");
        if (type == null || !INTEGRATION_TYPE.matcher(type).matches()) {
            throw CoreException.adminValidation("type", "A valid integration type is required");
        }
        JsonNode configNode = body.has("config") && !body.get("config").isNull() ? body.get("config") : objectMapper.createObjectNode();
        rejectPlaintextSecrets(configNode);
        String secretRef = text(body, "secretRef");
        RequestContext ctx = RequestContext.get();
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        IntegrationRow row = new IntegrationRow(
                id,
                org,
                type,
                configNode.toString(),
                secretRef,
                "active",
                ctx.actorId(),
                now,
                now);
        store.insertIntegration(row);
        recordAction("INTEGRATION_CONFIGURED", "INTEGRATION", id, "SUCCESS", Map.of("type", type));
        events.publishIntegrationConfigured(id, type, "active", now, ctx.actorId(), org);
        events.publishActionPerformed("INTEGRATION_CONFIGURED", "INTEGRATION", id, ctx.actorId(), now, "SUCCESS", org);
        audit.record("INTEGRATION_CONFIGURED", "INTEGRATION", id, "SUCCESS", Map.of("type", type));
        return toIntegrationApi(row);
    }

    @Transactional
    public Map<String, Object> patchIntegration(JsonNode body) {
        UUID org = requireOrg();
        if (body == null || !body.isObject() || body.isEmpty()) {
            throw CoreException.adminValidation("body", "Integration patch must be a non-empty object");
        }
        IntegrationRow existing = locateIntegration(org, body);
        JsonNode configNode = body.has("config") && !body.get("config").isNull()
                ? body.get("config")
                : parseTree(existing.configJson());
        rejectPlaintextSecrets(configNode);
        String status = text(body, "status");
        if (status == null) {
            status = existing.status();
        }
        if (status.isBlank()) {
            throw CoreException.adminValidation("status", "Status must not be blank");
        }
        String secretRef = body.has("secretRef") ? text(body, "secretRef") : existing.secretRef();
        Instant now = Instant.now();
        IntegrationRow updated = new IntegrationRow(
                existing.id(),
                org,
                existing.integrationType(),
                configNode.toString(),
                secretRef,
                status,
                existing.configuredBy(),
                existing.configuredAt(),
                now);
        store.updateIntegration(updated);
        RequestContext ctx = RequestContext.get();
        recordAction("INTEGRATION_CONFIGURED", "INTEGRATION", existing.id(), "SUCCESS", Map.of("type", existing.integrationType()));
        events.publishIntegrationConfigured(
                existing.id(), existing.integrationType(), status, now, ctx.actorId(), org);
        events.publishActionPerformed(
                "INTEGRATION_CONFIGURED", "INTEGRATION", existing.id(), ctx.actorId(), now, "SUCCESS", org);
        audit.record("INTEGRATION_CONFIGURED", "INTEGRATION", existing.id(), "SUCCESS", Map.of("status", status));
        return toIntegrationApi(updated);
    }

    @Transactional
    public Map<String, Object> provisionUser(JsonNode body) {
        UUID org = requireOrg();
        String email = text(body, "email");
        UUID targetOrg = uuid(body, "organizationId");
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw CoreException.adminValidation("email", "A valid email is required");
        }
        if (targetOrg == null) {
            throw CoreException.adminValidation("organizationId", "organizationId is required");
        }
        if (!targetOrg.equals(org)) {
            throw CoreException.forbidden();
        }
        List<UUID> roleIds = new ArrayList<>();
        if (body != null && body.has("roleIds") && body.get("roleIds").isArray()) {
            body.get("roleIds").forEach(n -> roleIds.add(UUID.fromString(n.asText())));
        }
        RequestContext ctx = RequestContext.get();
        Map<String, Object> created = identity.createUser(email, targetOrg, roleIds, ctx.accessToken(), org);
        UUID userId = created.get("id") == null ? null : UUID.fromString(String.valueOf(created.get("id")));
        Instant now = Instant.now();
        recordAction("USER_PROVISIONED", "USER", userId, "SUCCESS", Map.of("email", email));
        events.publishActionPerformed("USER_PROVISIONED", "USER", userId, ctx.actorId(), now, "SUCCESS", org);
        audit.record("USER_PROVISIONED", "USER", userId, "SUCCESS", Map.of("email", email));
        return created;
    }

    @Transactional
    public Map<String, Object> provisionOrganization(JsonNode body) {
        UUID org = requireOrg();
        String name = text(body, "name");
        if (name == null || name.isBlank()) {
            throw CoreException.adminValidation("name", "Organization name is required");
        }
        RequestContext ctx = RequestContext.get();
        Map<String, Object> created = identity.createOrganization(name, ctx.accessToken(), org);
        UUID createdId = created.get("id") == null ? null : UUID.fromString(String.valueOf(created.get("id")));
        Instant now = Instant.now();
        recordAction("ORG_PROVISIONED", "ORGANIZATION", createdId, "SUCCESS", Map.of("name", name));
        events.publishActionPerformed("ORG_PROVISIONED", "ORGANIZATION", createdId, ctx.actorId(), now, "SUCCESS", org);
        audit.record("ORG_PROVISIONED", "ORGANIZATION", createdId, "SUCCESS", Map.of("name", name));
        return created;
    }

    public List<Map<String, Object>> listAuditRecords(String cursor, int limit) {
        UUID org = requireOrg();
        int safe = clampLimit(limit);
        Instant cursorTime = Instant.parse("9999-12-31T23:59:59.999999999Z");
        UUID cursorId = CURSOR_SENTINEL;
        if (cursor != null && !cursor.isBlank()) {
            int sep = cursor.indexOf('|');
            if (sep < 0) {
                throw CoreException.adminValidation("cursor", "Invalid cursor");
            }
            cursorTime = Instant.parse(cursor.substring(0, sep));
            cursorId = UUID.fromString(cursor.substring(sep + 1));
        }
        List<Map<String, Object>> data = new ArrayList<>();
        for (AdminAuditItem item : store.listAudit(org, cursorTime, cursorId, safe)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.id().toString());
            row.put("action", item.action());
            row.put("actorId", item.actorId() == null ? null : item.actorId().toString());
            row.put("occurredAt", item.occurredAt().toString());
            data.add(row);
        }
        return data;
    }

    public void recordConsumedContext(String action, String targetType, UUID targetId, UUID organizationId, Map<String, ?> metadata) {
        UUID actor = RequestContext.get().actorId();
        if (actor == null) {
            actor = UUID.fromString("00000000-0000-0000-0000-000000000000");
        }
        store.insertAction(
                UUID.randomUUID(),
                organizationId,
                action,
                targetType,
                targetId,
                actor,
                "SUCCESS",
                json(metadata),
                Instant.now());
    }

    private IntegrationRow locateIntegration(UUID org, JsonNode body) {
        String idText = text(body, "id");
        String type = text(body, "type");
        if (idText != null) {
            try {
                return store.findIntegrationById(org, UUID.fromString(idText))
                        .orElseThrow(() -> CoreException.adminNotFound("Integration not found"));
            } catch (IllegalArgumentException ex) {
                throw CoreException.adminValidation("id", "Invalid integration id");
            }
        }
        if (type != null) {
            return store.findIntegrationByType(org, type)
                    .orElseThrow(() -> CoreException.adminNotFound("Integration not found"));
        }
        throw CoreException.adminValidation("id", "Integration id or type is required");
    }

    private void recordAction(String action, String targetType, UUID targetId, String outcome, Map<String, ?> metadata) {
        RequestContext ctx = RequestContext.get();
        UUID actor = ctx.actorId();
        if (actor == null) {
            throw CoreException.unauthenticated();
        }
        store.insertAction(
                UUID.randomUUID(),
                requireOrg(),
                action,
                targetType,
                targetId,
                actor,
                outcome,
                json(metadata),
                Instant.now());
    }

    private Map<String, Object> toIntegrationApi(IntegrationRow row) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", row.id().toString());
        data.put("type", row.integrationType());
        data.put("status", row.status());
        return data;
    }

    private void rejectPlaintextSecrets(JsonNode config) {
        if (config == null || config.isNull()) {
            return;
        }
        if (config.isObject()) {
            Iterator<String> names = config.fieldNames();
            while (names.hasNext()) {
                String name = names.next();
                if (SECRET_KEYS.contains(name.toLowerCase(Locale.ROOT))) {
                    throw CoreException.adminValidation(name, "Secrets must be provided as secretRef, not plaintext config");
                }
                rejectPlaintextSecrets(config.get(name));
            }
        } else if (config.isArray()) {
            for (JsonNode child : config) {
                rejectPlaintextSecrets(child);
            }
        }
    }

    private UUID requireOrg() {
        UUID org = RequestContext.get().organizationId();
        if (org == null) {
            throw CoreException.adminValidation("X-Organization-Id", "X-Organization-Id is required");
        }
        return org;
    }

    private static int clampLimit(int limit) {
        if (limit < 1) {
            return 50;
        }
        return Math.min(limit, 200);
    }

    private static String text(JsonNode body, String field) {
        if (body == null || !body.has(field) || body.get(field).isNull()) {
            return null;
        }
        String value = body.get(field).asText();
        return value == null || value.isBlank() ? null : value;
    }

    private static UUID uuid(JsonNode body, String field) {
        String raw = text(body, field);
        if (raw == null) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            throw CoreException.adminValidation(field, "Must be a UUID");
        }
    }

    private Object parseJson(String json) {
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            return json;
        }
    }

    private JsonNode parseTree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            return objectMapper.createObjectNode();
        }
    }

    private String json(Map<String, ?> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata == null ? Map.of() : metadata);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
