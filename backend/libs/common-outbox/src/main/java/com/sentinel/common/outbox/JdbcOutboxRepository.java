package com.sentinel.common.outbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JdbcOutboxRepository {

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final String table;

    public JdbcOutboxRepository(JdbcTemplate jdbc, ObjectMapper objectMapper, String table) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
        this.table = QualifiedOutboxTable.requireAllowed(table);
    }

    public String table() {
        return table;
    }

    public void insert(OutboxRecord record) {
        jdbc.update(
                """
                INSERT INTO %s (
                  event_id, event_type, schema_version, organization_id, producer, correlation_id,
                  envelope, created_at, status, attempt_count, next_attempt_at, claimed_at, published_at, last_error)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.formatted(table),
                record.eventId(),
                record.eventType(),
                record.schemaVersion(),
                record.organizationId(),
                record.producer(),
                record.correlationId(),
                jsonb(writeJson(record.envelope())),
                ts(record.createdAt()),
                record.status(),
                record.attemptCount(),
                ts(record.nextAttemptAt()),
                ts(record.claimedAt()),
                ts(record.publishedAt()),
                record.lastError());
    }

    public List<OutboxRecord> claim(Instant now, int limit, Duration claimTimeout) {
        Instant staleBefore = now.minus(claimTimeout);
        return jdbc.query(
                """
                WITH next_row AS (
                  SELECT event_id
                  FROM %s
                  WHERE published_at IS NULL
                    AND (
                      (status = 'PENDING' AND next_attempt_at <= ?)
                      OR (status = 'PUBLISHING' AND claimed_at IS NOT NULL AND claimed_at <= ?)
                    )
                  ORDER BY created_at ASC
                  FOR UPDATE SKIP LOCKED
                  LIMIT ?
                )
                UPDATE %s o
                SET status = 'PUBLISHING', claimed_at = ?, attempt_count = o.attempt_count + 1
                FROM next_row
                WHERE o.event_id = next_row.event_id
                RETURNING o.event_id, o.event_type, o.schema_version, o.organization_id, o.producer,
                          o.correlation_id, o.envelope::text, o.created_at, o.status, o.attempt_count,
                          o.next_attempt_at, o.claimed_at, o.published_at, o.last_error
                """.formatted(table, table),
                this::mapRow,
                ts(now),
                ts(staleBefore),
                limit,
                ts(now));
    }

    public void markPublished(UUID eventId, Instant publishedAt) {
        jdbc.update(
                "UPDATE %s SET status = 'PUBLISHED', published_at = ?, last_error = NULL WHERE event_id = ?"
                        .formatted(table),
                ts(publishedAt),
                eventId);
    }

    public void markRetry(UUID eventId, int attemptCount, Instant nextAttemptAt, String error) {
        jdbc.update(
                """
                UPDATE %s
                SET status = 'PENDING', attempt_count = ?, next_attempt_at = ?, last_error = ?, claimed_at = NULL
                WHERE event_id = ?
                """.formatted(table),
                attemptCount,
                ts(nextAttemptAt),
                truncate(error),
                eventId);
    }

    public void markDead(UUID eventId, int attemptCount, Instant at, String error) {
        jdbc.update(
                """
                UPDATE %s
                SET status = 'DEAD', attempt_count = ?, last_error = ?, claimed_at = ?, published_at = NULL
                WHERE event_id = ?
                """.formatted(table),
                attemptCount,
                truncate(error),
                ts(at),
                eventId);
    }

    public Optional<OutboxRecord> find(UUID eventId) {
        List<OutboxRecord> rows = jdbc.query(
                """
                SELECT event_id, event_type, schema_version, organization_id, producer, correlation_id,
                       envelope::text, created_at, status, attempt_count, next_attempt_at, claimed_at,
                       published_at, last_error
                FROM %s WHERE event_id = ?
                """.formatted(table),
                this::mapRow,
                eventId);
        return rows.stream().findFirst();
    }

    public int countByStatus(String status) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM %s WHERE status = ?".formatted(table), Integer.class, status);
        return n == null ? 0 : n;
    }

    public int pendingBacklog() {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM %s WHERE published_at IS NULL AND status <> 'DEAD'".formatted(table),
                Integer.class);
        return n == null ? 0 : n;
    }

    public Optional<Instant> oldestPendingCreatedAt() {
        Instant value = jdbc.query(
                """
                SELECT MIN(created_at) AS oldest
                FROM %s
                WHERE published_at IS NULL AND status <> 'DEAD'
                """.formatted(table),
                rs -> rs.next() ? instant(rs, "oldest") : null);
        return Optional.ofNullable(value);
    }

    public int countForOrganization(UUID organizationId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM %s WHERE organization_id = ?".formatted(table),
                Integer.class,
                organizationId);
        return n == null ? 0 : n;
    }

    private OutboxRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new OutboxRecord(
                rs.getObject("event_id", UUID.class),
                rs.getString("event_type"),
                rs.getString("schema_version"),
                rs.getObject("organization_id", UUID.class),
                rs.getString("producer"),
                rs.getObject("correlation_id", UUID.class),
                readJson(rs.getString("envelope")),
                instant(rs, "created_at"),
                rs.getString("status"),
                rs.getInt("attempt_count"),
                instant(rs, "next_attempt_at"),
                instant(rs, "claimed_at"),
                instant(rs, "published_at"),
                rs.getString("last_error"));
    }

    private Map<String, Object> readJson(String json) {
        try {
            return objectMapper.readValue(json, MAP);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Outbox envelope is not valid JSON", e);
        }
    }

    private String writeJson(Map<String, Object> envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize outbox envelope", e);
        }
    }

    private static PGobject jsonb(String json) {
        try {
            PGobject obj = new PGobject();
            obj.setType("jsonb");
            obj.setValue(json);
            return obj;
        } catch (SQLException e) {
            throw new IllegalArgumentException("Invalid JSON", e);
        }
    }

    private static Timestamp ts(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts == null ? null : ts.toInstant();
    }

    private static String truncate(String error) {
        if (error == null) {
            return null;
        }
        return error.length() <= 500 ? error : error.substring(0, 500);
    }
}
