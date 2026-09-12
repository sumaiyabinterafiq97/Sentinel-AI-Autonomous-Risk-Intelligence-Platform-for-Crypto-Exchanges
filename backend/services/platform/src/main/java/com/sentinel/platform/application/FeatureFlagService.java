package com.sentinel.platform.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
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
import com.sentinel.platform.domain.FeatureFlagEntry;
import com.sentinel.platform.infrastructure.FeatureFlagRepository;

@Service
public class FeatureFlagService {

    private final FeatureFlagRepository flags;
    private final AuditService audit;
    private final CoreEventPublisher events;
    private final ObjectMapper objectMapper;

    public FeatureFlagService(
            FeatureFlagRepository flags,
            AuditService audit,
            CoreEventPublisher events,
            ObjectMapper objectMapper) {
        this.flags = flags;
        this.audit = audit;
        this.events = events;
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> listResolved(UUID organizationId, String cursor, int limit) {
        if (limit < 1 || limit > 200) {
            throw CoreException.validation("limit", "limit must be between 1 and 200");
        }
        Map<String, FeatureFlagEntry> merged = new LinkedHashMap<>();
        for (FeatureFlagEntry entry : flags.findVisible(organizationId)) {
            if (entry.organizationId() == null) {
                merged.putIfAbsent(entry.flagKey(), entry);
            } else {
                merged.put(entry.flagKey(), entry);
            }
        }
        List<FeatureFlagEntry> ordered = merged.values().stream()
                .sorted(Comparator.comparing(FeatureFlagEntry::flagKey))
                .toList();
        List<Map<String, Object>> all = new ArrayList<>();
        boolean skip = cursor != null && !cursor.isBlank();
        for (FeatureFlagEntry entry : ordered) {
            if (skip) {
                if (entry.flagKey().equals(cursor)) {
                    skip = false;
                }
                continue;
            }
            all.add(toApi(entry));
            if (all.size() == limit) {
                break;
            }
        }
        return all;
    }

    public boolean hasMore(UUID organizationId, String lastKey) {
        if (lastKey == null) {
            return false;
        }
        List<Map<String, Object>> next = listResolved(organizationId, lastKey, 1);
        return !next.isEmpty();
    }

    @Transactional
    public Map<String, Object> patchFlag(UUID organizationId, String flagKey, JsonNode body) {
        CoreKeys.requireFlagKey(flagKey);
        if (body == null || !body.isObject()) {
            throw CoreException.validation("body", "Feature flag patch must be an object");
        }
        if (!body.has("enabled") || !body.get("enabled").isBoolean()) {
            throw CoreException.validation("enabled", "enabled must be a boolean");
        }
        boolean enabled = body.get("enabled").asBoolean();
        RequestContext ctx = RequestContext.get();
        Instant now = Instant.now();
        var existing = flags.findByOrgAndKey(organizationId, flagKey);
        if (existing.isPresent() && existing.get().enabled() == enabled) {
            return toApi(existing.get());
        }
        if (existing.isPresent()) {
            FeatureFlagEntry current = existing.get();
            FeatureFlagEntry updated = new FeatureFlagEntry(
                    current.id(), organizationId, flagKey, enabled, current.metadataJson(), now);
            flags.update(updated);
            audit.record(
                    "FEATURE_FLAG_UPDATED",
                    "FEATURE_FLAG",
                    updated.id(),
                    "SUCCESS",
                    Map.of("flagKey", flagKey, "enabled", enabled));
            events.publishFeatureFlagChanged(flagKey, organizationId, enabled, now, ctx.actorId());
            return toApi(updated);
        }
        FeatureFlagEntry created = new FeatureFlagEntry(
                UUID.randomUUID(), organizationId, flagKey, enabled, "{}", now);
        flags.insert(created);
        audit.record(
                "FEATURE_FLAG_UPDATED",
                "FEATURE_FLAG",
                created.id(),
                "SUCCESS",
                Map.of("flagKey", flagKey, "enabled", enabled));
        events.publishFeatureFlagChanged(flagKey, organizationId, enabled, now, ctx.actorId());
        return toApi(created);
    }

    private Map<String, Object> toApi(FeatureFlagEntry entry) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("key", entry.flagKey());
        data.put("enabled", entry.enabled());
        data.put("description", description(entry.metadataJson()));
        return data;
    }

    private String description(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(metadataJson);
            if (node.has("description") && !node.get("description").isNull()) {
                return node.get("description").asText();
            }
        } catch (JsonProcessingException ignored) {
            return null;
        }
        return null;
    }
}
