package com.sentinel.platform.application;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.platform.api.RequestContext;
import com.sentinel.platform.domain.CoreException;
import com.sentinel.platform.domain.CoreKeys;
import com.sentinel.platform.domain.PlatformConfigEntry;
import com.sentinel.platform.infrastructure.PlatformConfigRepository;

@Service
public class PlatformConfigService {

    private final PlatformConfigRepository configs;
    private final AuditService audit;
    private final CoreEventPublisher events;
    private final ObjectMapper objectMapper;

    public PlatformConfigService(
            PlatformConfigRepository configs,
            AuditService audit,
            CoreEventPublisher events,
            ObjectMapper objectMapper) {
        this.configs = configs;
        this.audit = audit;
        this.events = events;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> getActiveConfig(UUID organizationId) {
        Map<String, Object> merged = new LinkedHashMap<>();
        Map<String, Object> orgOverlay = new LinkedHashMap<>();
        for (PlatformConfigEntry entry : configs.findVisible(organizationId)) {
            Object value = parseJson(entry.configValueJson());
            if (entry.organizationId() == null) {
                merged.put(entry.configKey(), value);
            } else {
                orgOverlay.put(entry.configKey(), value);
            }
        }
        merged.putAll(orgOverlay);
        return merged;
    }

    @Transactional
    public Map<String, Object> patchConfig(UUID organizationId, JsonNode body) {
        if (body == null || !body.isObject() || body.isEmpty()) {
            throw CoreException.validation("body", "Configuration patch must be a non-empty object");
        }
        RequestContext ctx = RequestContext.get();
        Instant now = Instant.now();
        body.fields().forEachRemaining(field -> {
            String key = field.getKey();
            CoreKeys.requireConfigKey(key);
            JsonNode value = field.getValue();
            if (value == null || value.isNull()) {
                throw CoreException.validation(key, "Configuration value must not be null");
            }
            if (CoreKeys.MAINTENANCE_MODE.equals(key) && !value.isBoolean()) {
                throw CoreException.validation(key, "maintenance.mode must be a boolean");
            }
            String json = value.toString();
            var existing = configs.findByOrgAndKey(organizationId, key);
            if (existing.isPresent() && existing.get().configValueJson().equals(json)) {
                return;
            }
            if (existing.isPresent()) {
                PlatformConfigEntry current = existing.get();
                PlatformConfigEntry updated = new PlatformConfigEntry(
                        current.id(),
                        organizationId,
                        key,
                        json,
                        current.version() + 1,
                        current.createdAt(),
                        now,
                        current.createdBy(),
                        ctx.actorId());
                configs.update(updated);
                audit.record(
                        CoreKeys.MAINTENANCE_MODE.equals(key) ? "MAINTENANCE_MODE_UPDATED" : "CONFIG_UPDATED",
                        "PLATFORM_CONFIG",
                        updated.id(),
                        "SUCCESS",
                        Map.of("configKey", key, "version", updated.version()));
                events.publishConfigurationUpdated(key, organizationId, updated.version(), now, ctx.actorId());
            } else {
                PlatformConfigEntry created = new PlatformConfigEntry(
                        UUID.randomUUID(),
                        organizationId,
                        key,
                        json,
                        1,
                        now,
                        now,
                        ctx.actorId(),
                        ctx.actorId());
                configs.insert(created);
                audit.record(
                        CoreKeys.MAINTENANCE_MODE.equals(key) ? "MAINTENANCE_MODE_UPDATED" : "CONFIG_CREATED",
                        "PLATFORM_CONFIG",
                        created.id(),
                        "SUCCESS",
                        Map.of("configKey", key, "version", 1));
                events.publishConfigurationUpdated(key, organizationId, 1, now, ctx.actorId());
            }
        });
        return getActiveConfig(organizationId);
    }

    public boolean maintenanceMode(UUID organizationId) {
        Map<String, Object> active = getActiveConfig(organizationId);
        Object value = active.get(CoreKeys.MAINTENANCE_MODE);
        return Boolean.TRUE.equals(value);
    }

    private Object parseJson(String json) {
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            return json;
        }
    }
}
