package com.sentinel.ops.infrastructure;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class RiskStore {

    public record RuleRow(
            UUID id, UUID organizationId, String name, String definitionJson, boolean enabled, int version) {}

    public record AssessmentRow(
            UUID id,
            UUID organizationId,
            String entityType,
            String entityId,
            BigDecimal score,
            String riskLevel,
            String explanation,
            Instant evaluatedAt,
            UUID correlationId) {}

    public record IngestRow(UUID id, UUID organizationId, String externalTxId, UUID assessmentId) {}

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public RiskStore(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public void insertRule(RuleRow row, Instant now, UUID actor) {
        jdbc.update(
                """
                INSERT INTO risk.risk_rules
                  (id, organization_id, name, definition, enabled, version, created_at, updated_at, created_by, updated_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                row.id(),
                row.organizationId(),
                row.name(),
                JdbcTypes.jsonb(row.definitionJson()),
                row.enabled(),
                row.version(),
                JdbcTypes.ts(now),
                JdbcTypes.ts(now),
                actor,
                actor);
    }

    public void updateRule(RuleRow row, Instant now, UUID actor) {
        jdbc.update(
                """
                UPDATE risk.risk_rules
                SET name = ?, definition = ?, enabled = ?, version = ?, updated_at = ?, updated_by = ?
                WHERE id = ? AND organization_id = ?
                """,
                row.name(),
                JdbcTypes.jsonb(row.definitionJson()),
                row.enabled(),
                row.version(),
                JdbcTypes.ts(now),
                actor,
                row.id(),
                row.organizationId());
    }

    public Optional<RuleRow> findRule(UUID id, UUID org) {
        List<RuleRow> rows = jdbc.query(
                """
                SELECT id, organization_id, name, definition::text, enabled, version
                FROM risk.risk_rules WHERE id = ? AND organization_id = ?
                """,
                this::mapRule,
                id,
                org);
        return rows.stream().findFirst();
    }

    public List<RuleRow> listRules(UUID org, String cursor, int limit) {
        if (cursor == null || cursor.isBlank()) {
            return jdbc.query(
                    """
                    SELECT id, organization_id, name, definition::text, enabled, version
                    FROM risk.risk_rules
                    WHERE organization_id = ?
                    ORDER BY name, version DESC, id
                    LIMIT ?
                    """,
                    this::mapRule,
                    org,
                    limit);
        }
        return jdbc.query(
                """
                SELECT id, organization_id, name, definition::text, enabled, version
                FROM risk.risk_rules
                WHERE organization_id = ? AND id::text > ?
                ORDER BY name, version DESC, id
                LIMIT ?
                """,
                this::mapRule,
                org,
                cursor,
                limit);
    }

    public List<RuleRow> enabledRules(UUID org) {
        return jdbc.query(
                """
                SELECT id, organization_id, name, definition::text, enabled, version
                FROM risk.risk_rules
                WHERE organization_id = ? AND enabled = TRUE
                ORDER BY name
                """,
                this::mapRule,
                org);
    }

    public void insertAssessment(AssessmentRow row, Instant createdAt) {
        jdbc.update(
                """
                INSERT INTO risk.risk_assessments
                  (id, organization_id, entity_type, entity_id, score, risk_level, explanation_summary,
                   evaluated_at, created_at, correlation_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                row.id(),
                row.organizationId(),
                row.entityType(),
                row.entityId(),
                row.score(),
                row.riskLevel(),
                row.explanation(),
                JdbcTypes.ts(row.evaluatedAt()),
                JdbcTypes.ts(createdAt),
                row.correlationId());
    }

    public void insertHit(UUID id, UUID assessmentId, UUID ruleId, String detailsJson, Instant now) {
        jdbc.update(
                """
                INSERT INTO risk.risk_rule_hits (id, assessment_id, rule_id, hit_details, created_at)
                VALUES (?, ?, ?, ?, ?)
                """,
                id,
                assessmentId,
                ruleId,
                JdbcTypes.jsonb(detailsJson),
                JdbcTypes.ts(now));
    }

    public Optional<AssessmentRow> findAssessment(UUID id, UUID org) {
        List<AssessmentRow> rows = jdbc.query(
                """
                SELECT id, organization_id, entity_type, entity_id, score, risk_level, explanation_summary,
                       evaluated_at, correlation_id
                FROM risk.risk_assessments WHERE id = ? AND organization_id = ?
                """,
                this::mapAssessment,
                id,
                org);
        return rows.stream().findFirst();
    }

    public List<AssessmentRow> listAssessments(UUID org, String cursor, int limit) {
        if (cursor == null || cursor.isBlank()) {
            return jdbc.query(
                    """
                    SELECT id, organization_id, entity_type, entity_id, score, risk_level, explanation_summary,
                           evaluated_at, correlation_id
                    FROM risk.risk_assessments
                    WHERE organization_id = ?
                    ORDER BY evaluated_at DESC, id DESC
                    LIMIT ?
                    """,
                    this::mapAssessment,
                    org,
                    limit);
        }
        return jdbc.query(
                """
                SELECT id, organization_id, entity_type, entity_id, score, risk_level, explanation_summary,
                       evaluated_at, correlation_id
                FROM risk.risk_assessments
                WHERE organization_id = ? AND id::text < ?
                ORDER BY evaluated_at DESC, id DESC
                LIMIT ?
                """,
                this::mapAssessment,
                org,
                cursor,
                limit);
    }

    public Optional<IngestRow> findIngest(UUID org, String externalTxId) {
        List<IngestRow> rows = jdbc.query(
                """
                SELECT id, organization_id, external_tx_id, assessment_id
                FROM risk.transaction_ingest_log
                WHERE organization_id = ? AND external_tx_id = ?
                """,
                this::mapIngest,
                org,
                externalTxId);
        return rows.stream().findFirst();
    }

    public void insertIngest(UUID id, UUID org, String externalTxId, UUID assessmentId, Instant now, String idempotencyKey) {
        jdbc.update(
                """
                INSERT INTO risk.transaction_ingest_log
                  (id, organization_id, external_tx_id, assessment_id, ingested_at, idempotency_key)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                id,
                org,
                externalTxId,
                assessmentId,
                JdbcTypes.ts(now),
                idempotencyKey);
    }

    public void insertAudit(
            UUID orgId,
            UUID actorId,
            String action,
            String resourceType,
            UUID resourceId,
            String outcome,
            UUID correlationId,
            UUID requestId,
            String metadata,
            Instant now) {
        jdbc.update(
                """
                INSERT INTO core.audit_records
                  (id, organization_id, actor_id, actor_type, action, resource_type, resource_id,
                   outcome, correlation_id, request_id, metadata, created_at)
                VALUES (?, ?, ?, 'user', ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                orgId,
                actorId,
                action,
                resourceType,
                resourceId,
                outcome,
                correlationId,
                requestId,
                JdbcTypes.jsonb(metadata == null ? "{}" : metadata),
                JdbcTypes.ts(now));
    }

    public boolean alertSchemaExists() {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = 'alert'", Integer.class);
        return n != null && n > 0;
    }

    public JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid stored JSON", e);
        }
    }

    private RuleRow mapRule(ResultSet rs, int i) throws SQLException {
        return new RuleRow(
                rs.getObject("id", UUID.class),
                rs.getObject("organization_id", UUID.class),
                rs.getString("name"),
                rs.getString("definition"),
                rs.getBoolean("enabled"),
                rs.getInt("version"));
    }

    private AssessmentRow mapAssessment(ResultSet rs, int i) throws SQLException {
        return new AssessmentRow(
                rs.getObject("id", UUID.class),
                rs.getObject("organization_id", UUID.class),
                rs.getString("entity_type"),
                rs.getString("entity_id"),
                rs.getBigDecimal("score"),
                rs.getString("risk_level"),
                rs.getString("explanation_summary"),
                JdbcTypes.instant(rs.getTimestamp("evaluated_at")),
                rs.getObject("correlation_id", UUID.class));
    }

    private IngestRow mapIngest(ResultSet rs, int i) throws SQLException {
        return new IngestRow(
                rs.getObject("id", UUID.class),
                rs.getObject("organization_id", UUID.class),
                rs.getString("external_tx_id"),
                rs.getObject("assessment_id", UUID.class));
    }
}
