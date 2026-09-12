package com.sentinel.dash.infrastructure;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class DashStore {

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    private final Set<UUID> processed = ConcurrentHashMap.newKeySet();

    public DashStore(JdbcTemplate jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    public boolean markProcessed(UUID eventId) {
        return processed.add(eventId);
    }

    public void upsertProjection(UUID organizationId, String key, Map<String, Object> payload, Instant expiresAt) {
        Instant now = Instant.now();
        jdbc.update(
                """
                INSERT INTO dash.workspace_projection_cache
                    (id, organization_id, projection_key, payload, expires_at, updated_at)
                VALUES (?, ?, ?, ?::jsonb, ?, ?)
                ON CONFLICT (organization_id, projection_key)
                DO UPDATE SET payload = EXCLUDED.payload, expires_at = EXCLUDED.expires_at, updated_at = EXCLUDED.updated_at
                """,
                UUID.randomUUID(),
                organizationId,
                key,
                write(payload),
                Timestamp.from(expiresAt),
                Timestamp.from(now));
    }

    public void deleteProjection(UUID organizationId, String key) {
        jdbc.update(
                "DELETE FROM dash.workspace_projection_cache WHERE organization_id = ? AND projection_key = ?",
                organizationId,
                key);
    }

    public List<Map<String, Object>> listProjections(UUID organizationId, String prefix) {
        return jdbc.query(
                """
                SELECT projection_key, payload FROM dash.workspace_projection_cache
                WHERE organization_id = ? AND projection_key LIKE ? AND expires_at > now()
                ORDER BY updated_at DESC
                """,
                (rs, i) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("key", rs.getString("projection_key"));
                    row.put("payload", read(rs.getString("payload")));
                    return row;
                },
                organizationId,
                prefix + "%");
    }

    public Map<String, Object> getProjection(UUID organizationId, String key) {
        List<Map<String, Object>> rows = jdbc.query(
                """
                SELECT payload FROM dash.workspace_projection_cache
                WHERE organization_id = ? AND projection_key = ? AND expires_at > now()
                """,
                (rs, i) -> read(rs.getString("payload")),
                organizationId,
                key);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public void touchPreferences(UUID userId, UUID organizationId) {
        Instant now = Instant.now();
        jdbc.update(
                """
                INSERT INTO dash.workspace_preferences (id, user_id, organization_id, preferences, updated_at, created_at)
                VALUES (?, ?, ?, '{}'::jsonb, ?, ?)
                ON CONFLICT (user_id, organization_id) DO UPDATE SET updated_at = EXCLUDED.updated_at
                """,
                UUID.randomUUID(),
                userId,
                organizationId,
                Timestamp.from(now),
                Timestamp.from(now));
    }

    public UUID logInteraction(UUID userId, UUID organizationId, String widgetId, String type) {
        UUID id = UUID.randomUUID();
        jdbc.update(
                """
                INSERT INTO dash.widget_interactions
                    (id, user_id, organization_id, widget_id, interaction_type, created_at, metadata)
                VALUES (?, ?, ?, ?, ?, ?, '{}'::jsonb)
                """,
                id,
                userId,
                organizationId,
                widgetId,
                type,
                Timestamp.from(Instant.now()));
        return id;
    }

    private String write(Map<String, Object> payload) {
        try {
            return json.writeValueAsString(payload == null ? Map.of() : payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private Map<String, Object> read(String raw) {
        try {
            if (raw == null || raw.isBlank()) {
                return new LinkedHashMap<>();
            }
            return json.readValue(raw, MAP);
        } catch (JsonProcessingException e) {
            return new LinkedHashMap<>();
        }
    }

    public List<Map<String, Object>> emptyList() {
        return new ArrayList<>();
    }
}
