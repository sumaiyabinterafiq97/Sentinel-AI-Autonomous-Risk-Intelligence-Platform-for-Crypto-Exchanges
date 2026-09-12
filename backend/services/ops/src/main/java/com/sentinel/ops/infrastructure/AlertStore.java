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

@Repository
public class AlertStore {

    public record AlertRow(
            UUID id,
            UUID organizationId,
            String status,
            int priority,
            String title,
            UUID riskAssessmentId,
            UUID assignedTo,
            Instant createdAt,
            Instant updatedAt,
            Instant closedAt,
            UUID closedBy,
            String dispositionReason,
            UUID createdBy) {}

    private final JdbcTemplate jdbc;

    public AlertStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(AlertRow row) {
        jdbc.update(
                """
                INSERT INTO alert.alerts
                  (id, organization_id, status, priority, title, risk_assessment_id, assigned_to,
                   created_at, updated_at, closed_at, closed_by, disposition_reason, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                row.id(),
                row.organizationId(),
                row.status(),
                row.priority(),
                row.title(),
                row.riskAssessmentId(),
                row.assignedTo(),
                JdbcTypes.ts(row.createdAt()),
                JdbcTypes.ts(row.updatedAt()),
                JdbcTypes.ts(row.closedAt()),
                row.closedBy(),
                row.dispositionReason(),
                row.createdBy());
    }

    public void update(AlertRow row) {
        jdbc.update(
                """
                UPDATE alert.alerts
                SET status = ?, priority = ?, title = ?, risk_assessment_id = ?, assigned_to = ?,
                    updated_at = ?, closed_at = ?, closed_by = ?, disposition_reason = ?
                WHERE id = ? AND organization_id = ?
                """,
                row.status(),
                row.priority(),
                row.title(),
                row.riskAssessmentId(),
                row.assignedTo(),
                JdbcTypes.ts(row.updatedAt()),
                JdbcTypes.ts(row.closedAt()),
                row.closedBy(),
                row.dispositionReason(),
                row.id(),
                row.organizationId());
    }

    public Optional<AlertRow> find(UUID id, UUID org) {
        List<AlertRow> rows = jdbc.query(
                """
                SELECT id, organization_id, status, priority, title, risk_assessment_id, assigned_to,
                       created_at, updated_at, closed_at, closed_by, disposition_reason, created_by
                FROM alert.alerts WHERE id = ? AND organization_id = ?
                """,
                this::mapAlert,
                id,
                org);
        return rows.stream().findFirst();
    }

    public Optional<AlertRow> findByAssessment(UUID org, UUID assessmentId) {
        List<AlertRow> rows = jdbc.query(
                """
                SELECT id, organization_id, status, priority, title, risk_assessment_id, assigned_to,
                       created_at, updated_at, closed_at, closed_by, disposition_reason, created_by
                FROM alert.alerts
                WHERE organization_id = ? AND risk_assessment_id = ?
                ORDER BY created_at ASC, id ASC
                LIMIT 1
                """,
                this::mapAlert,
                org,
                assessmentId);
        return rows.stream().findFirst();
    }

    public List<AlertRow> list(UUID org, String cursor, int limit) {
        if (cursor == null || cursor.isBlank()) {
            return jdbc.query(
                    """
                    SELECT id, organization_id, status, priority, title, risk_assessment_id, assigned_to,
                           created_at, updated_at, closed_at, closed_by, disposition_reason, created_by
                    FROM alert.alerts
                    WHERE organization_id = ?
                    ORDER BY priority DESC, created_at DESC, id
                    LIMIT ?
                    """,
                    this::mapAlert,
                    org,
                    limit);
        }
        return jdbc.query(
                """
                SELECT id, organization_id, status, priority, title, risk_assessment_id, assigned_to,
                       created_at, updated_at, closed_at, closed_by, disposition_reason, created_by
                FROM alert.alerts
                WHERE organization_id = ? AND id::text > ?
                ORDER BY priority DESC, created_at DESC, id
                LIMIT ?
                """,
                this::mapAlert,
                org,
                cursor,
                limit);
    }

    public boolean hasMore(UUID org, String lastId) {
        if (lastId == null) {
            return false;
        }
        return list(org, lastId, 1).size() == 1;
    }

    public void upsertRiskContext(UUID alertId, BigDecimal score, String level, String contextJson, Instant now) {
        jdbc.update(
                """
                INSERT INTO alert.alert_risk_context (alert_id, risk_score, risk_level, context, updated_at)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (alert_id) DO UPDATE
                SET risk_score = EXCLUDED.risk_score,
                    risk_level = EXCLUDED.risk_level,
                    context = EXCLUDED.context,
                    updated_at = EXCLUDED.updated_at
                """,
                alertId,
                score,
                level,
                JdbcTypes.jsonb(contextJson),
                JdbcTypes.ts(now));
    }

    public Optional<String> riskContextJson(UUID alertId) {
        List<String> rows = jdbc.query(
                "SELECT context::text FROM alert.alert_risk_context WHERE alert_id = ?",
                (rs, i) -> rs.getString(1),
                alertId);
        return rows.stream().findFirst();
    }

    public void insertInvestigationLink(UUID alertId, UUID caseId, UUID linkedBy, Instant now) {
        jdbc.update(
                """
                INSERT INTO alert.alert_investigation_links (alert_id, case_id, linked_at, linked_by)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (alert_id, case_id) DO NOTHING
                """,
                alertId,
                caseId,
                JdbcTypes.ts(now),
                linkedBy);
    }

    public int countForOrg(UUID org) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM alert.alerts WHERE organization_id = ?", Integer.class, org);
        return n == null ? 0 : n;
    }

    public boolean investigationSchemaExists() {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = 'invest'", Integer.class);
        return n != null && n > 0;
    }

    private AlertRow mapAlert(ResultSet rs, int i) throws SQLException {
        return new AlertRow(
                rs.getObject("id", UUID.class),
                rs.getObject("organization_id", UUID.class),
                rs.getString("status"),
                rs.getInt("priority"),
                rs.getString("title"),
                rs.getObject("risk_assessment_id", UUID.class),
                rs.getObject("assigned_to", UUID.class),
                JdbcTypes.instant(rs.getTimestamp("created_at")),
                JdbcTypes.instant(rs.getTimestamp("updated_at")),
                JdbcTypes.instant(rs.getTimestamp("closed_at")),
                rs.getObject("closed_by", UUID.class),
                rs.getString("disposition_reason"),
                rs.getObject("created_by", UUID.class));
    }
}
