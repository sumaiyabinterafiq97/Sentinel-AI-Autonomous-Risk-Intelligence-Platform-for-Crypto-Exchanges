package com.sentinel.ops.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InvestStore {

    public record CaseRow(
            UUID id,
            UUID organizationId,
            String status,
            String title,
            Integer priority,
            UUID assignedTo,
            UUID sourceAlertId,
            Instant openedAt,
            Instant closedAt,
            UUID closedBy,
            String outcome,
            Instant createdAt,
            Instant updatedAt,
            UUID createdBy) {}

    public record EvidenceRow(
            UUID id, UUID caseId, String evidenceType, String referenceType, String referenceId, Instant attachedAt) {}

    public record TimelineRow(UUID id, UUID caseId, String eventType, String description, Instant occurredAt, UUID actorId) {}

    public record NoteRow(UUID id, UUID caseId, UUID authorId, String content, Instant createdAt) {}

    private final JdbcTemplate jdbc;

    public InvestStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insertCase(CaseRow row) {
        jdbc.update(
                """
                INSERT INTO invest.investigation_cases
                  (id, organization_id, status, title, priority, assigned_to, source_alert_id,
                   opened_at, closed_at, closed_by, outcome, created_at, updated_at, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                row.id(),
                row.organizationId(),
                row.status(),
                row.title(),
                row.priority(),
                row.assignedTo(),
                row.sourceAlertId(),
                JdbcTypes.ts(row.openedAt()),
                JdbcTypes.ts(row.closedAt()),
                row.closedBy(),
                row.outcome(),
                JdbcTypes.ts(row.createdAt()),
                JdbcTypes.ts(row.updatedAt()),
                row.createdBy());
    }

    public void updateCase(CaseRow row) {
        jdbc.update(
                """
                UPDATE invest.investigation_cases
                SET status = ?, title = ?, priority = ?, assigned_to = ?, source_alert_id = ?,
                    closed_at = ?, closed_by = ?, outcome = ?, updated_at = ?
                WHERE id = ? AND organization_id = ?
                """,
                row.status(),
                row.title(),
                row.priority(),
                row.assignedTo(),
                row.sourceAlertId(),
                JdbcTypes.ts(row.closedAt()),
                row.closedBy(),
                row.outcome(),
                JdbcTypes.ts(row.updatedAt()),
                row.id(),
                row.organizationId());
    }

    public Optional<CaseRow> find(UUID id, UUID org) {
        List<CaseRow> rows = jdbc.query(
                """
                SELECT id, organization_id, status, title, priority, assigned_to, source_alert_id,
                       opened_at, closed_at, closed_by, outcome, created_at, updated_at, created_by
                FROM invest.investigation_cases WHERE id = ? AND organization_id = ?
                """,
                this::mapCase,
                id,
                org);
        return rows.stream().findFirst();
    }

    public Optional<CaseRow> findBySourceAlert(UUID org, UUID sourceAlertId) {
        List<CaseRow> rows = jdbc.query(
                """
                SELECT id, organization_id, status, title, priority, assigned_to, source_alert_id,
                       opened_at, closed_at, closed_by, outcome, created_at, updated_at, created_by
                FROM invest.investigation_cases
                WHERE organization_id = ? AND source_alert_id = ?
                ORDER BY created_at ASC LIMIT 1
                """,
                this::mapCase,
                org,
                sourceAlertId);
        return rows.stream().findFirst();
    }

    public List<CaseRow> list(UUID org, String cursor, int limit) {
        if (cursor == null || cursor.isBlank()) {
            return jdbc.query(
                    """
                    SELECT id, organization_id, status, title, priority, assigned_to, source_alert_id,
                           opened_at, closed_at, closed_by, outcome, created_at, updated_at, created_by
                    FROM invest.investigation_cases
                    WHERE organization_id = ?
                    ORDER BY updated_at DESC, id
                    LIMIT ?
                    """,
                    this::mapCase,
                    org,
                    limit);
        }
        return jdbc.query(
                """
                SELECT id, organization_id, status, title, priority, assigned_to, source_alert_id,
                       opened_at, closed_at, closed_by, outcome, created_at, updated_at, created_by
                FROM invest.investigation_cases
                WHERE organization_id = ? AND id::text > ?
                ORDER BY updated_at DESC, id
                LIMIT ?
                """,
                this::mapCase,
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

    public int countForOrg(UUID org) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM invest.investigation_cases WHERE organization_id = ?", Integer.class, org);
        return n == null ? 0 : n;
    }

    public void insertEvidence(UUID id, UUID caseId, String type, String refType, String refId, String metadata, UUID actor, Instant now) {
        jdbc.update(
                """
                INSERT INTO invest.case_evidence
                  (id, case_id, evidence_type, reference_type, reference_id, metadata, classification, attached_by, attached_at)
                VALUES (?, ?, ?, ?, ?, ?, 'restricted', ?, ?)
                """,
                id,
                caseId,
                type,
                refType,
                refId,
                JdbcTypes.jsonb(metadata),
                actor,
                JdbcTypes.ts(now));
    }

    public Optional<EvidenceRow> findEvidenceByRef(UUID caseId, String referenceId) {
        List<EvidenceRow> rows = jdbc.query(
                """
                SELECT id, case_id, evidence_type, reference_type, reference_id, attached_at
                FROM invest.case_evidence WHERE case_id = ? AND reference_id = ?
                LIMIT 1
                """,
                this::mapEvidence,
                caseId,
                referenceId);
        return rows.stream().findFirst();
    }

    public void insertTimeline(UUID id, UUID caseId, String eventType, String description, Instant at, UUID actor) {
        jdbc.update(
                """
                INSERT INTO invest.case_timeline_events (id, case_id, event_type, description, occurred_at, actor_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                id,
                caseId,
                eventType,
                description,
                JdbcTypes.ts(at),
                actor);
    }

    public boolean timelineHasEventId(UUID caseId, UUID eventId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM invest.case_timeline_events WHERE case_id = ? AND description LIKE ?",
                Integer.class,
                caseId,
                "%eventId=" + eventId + "%");
        return n != null && n > 0;
    }

    public List<TimelineRow> listTimeline(UUID caseId) {
        return jdbc.query(
                """
                SELECT id, case_id, event_type, description, occurred_at, actor_id
                FROM invest.case_timeline_events WHERE case_id = ? ORDER BY occurred_at DESC, id
                """,
                this::mapTimeline,
                caseId);
    }

    public void insertNote(UUID id, UUID caseId, UUID authorId, String content, Instant now) {
        jdbc.update(
                """
                INSERT INTO invest.case_notes (id, case_id, author_id, content, created_at)
                VALUES (?, ?, ?, ?, ?)
                """,
                id,
                caseId,
                authorId,
                content,
                JdbcTypes.ts(now));
    }

    public List<NoteRow> listNotes(UUID caseId, String cursor, int limit) {
        if (cursor == null || cursor.isBlank()) {
            return jdbc.query(
                    """
                    SELECT id, case_id, author_id, content, created_at
                    FROM invest.case_notes WHERE case_id = ? ORDER BY created_at DESC, id LIMIT ?
                    """,
                    this::mapNote,
                    caseId,
                    limit);
        }
        return jdbc.query(
                """
                SELECT id, case_id, author_id, content, created_at
                FROM invest.case_notes WHERE case_id = ? AND id::text > ? ORDER BY created_at DESC, id LIMIT ?
                """,
                this::mapNote,
                caseId,
                cursor,
                limit);
    }

    public List<UUID> alertIdsForAssessment(UUID org, UUID assessmentId) {
        return jdbc.query(
                "SELECT id FROM alert.alerts WHERE organization_id = ? AND risk_assessment_id = ?",
                (rs, i) -> rs.getObject("id", UUID.class),
                org,
                assessmentId);
    }

    public Optional<String> assessmentSummary(UUID org, UUID assessmentId) {
        List<String> rows = jdbc.query(
                """
                SELECT risk_level || ' score=' || score::text
                FROM risk.risk_assessments WHERE organization_id = ? AND id = ?
                """,
                (rs, i) -> rs.getString(1),
                org,
                assessmentId);
        return rows.stream().findFirst();
    }

    public boolean complianceSchemaExists() {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = 'comp'", Integer.class);
        return n != null && n > 0;
    }

    private CaseRow mapCase(ResultSet rs, int i) throws SQLException {
        Integer priority = rs.getObject("priority") == null ? null : rs.getInt("priority");
        return new CaseRow(
                rs.getObject("id", UUID.class),
                rs.getObject("organization_id", UUID.class),
                rs.getString("status"),
                rs.getString("title"),
                priority,
                rs.getObject("assigned_to", UUID.class),
                rs.getObject("source_alert_id", UUID.class),
                JdbcTypes.instant(rs.getTimestamp("opened_at")),
                JdbcTypes.instant(rs.getTimestamp("closed_at")),
                rs.getObject("closed_by", UUID.class),
                rs.getString("outcome"),
                JdbcTypes.instant(rs.getTimestamp("created_at")),
                JdbcTypes.instant(rs.getTimestamp("updated_at")),
                rs.getObject("created_by", UUID.class));
    }

    private EvidenceRow mapEvidence(ResultSet rs, int i) throws SQLException {
        return new EvidenceRow(
                rs.getObject("id", UUID.class),
                rs.getObject("case_id", UUID.class),
                rs.getString("evidence_type"),
                rs.getString("reference_type"),
                rs.getString("reference_id"),
                JdbcTypes.instant(rs.getTimestamp("attached_at")));
    }

    private TimelineRow mapTimeline(ResultSet rs, int i) throws SQLException {
        return new TimelineRow(
                rs.getObject("id", UUID.class),
                rs.getObject("case_id", UUID.class),
                rs.getString("event_type"),
                rs.getString("description"),
                JdbcTypes.instant(rs.getTimestamp("occurred_at")),
                rs.getObject("actor_id", UUID.class));
    }

    private NoteRow mapNote(ResultSet rs, int i) throws SQLException {
        return new NoteRow(
                rs.getObject("id", UUID.class),
                rs.getObject("case_id", UUID.class),
                rs.getObject("author_id", UUID.class),
                rs.getString("content"),
                JdbcTypes.instant(rs.getTimestamp("created_at")));
    }
}
