package com.sentinel.ops.infrastructure;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CompStore {

    public record KycRow(
            UUID id,
            UUID organizationId,
            UUID userId,
            String status,
            String outcome,
            UUID reviewedBy,
            Instant completedAt,
            Instant createdAt) {}

    public record AmlRow(UUID id, UUID organizationId, String subjectRef, String status) {}

    public record TravelRow(UUID id, UUID organizationId, String transactionRef, String status) {}

    public record SanctionsRow(
            UUID id,
            UUID organizationId,
            String subjectRef,
            String matchStatus,
            String disposition,
            Instant screenedAt) {}

    public record AuditRow(UUID id, UUID organizationId, String packageType, String status) {}

    private final JdbcTemplate jdbc;

    public CompStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public static UUID subjectToUserId(String subjectRef) {
        try {
            return UUID.fromString(subjectRef);
        } catch (IllegalArgumentException ex) {
            return UUID.nameUUIDFromBytes(subjectRef.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void insertKyc(KycRow row, UUID correlationId, Instant now) {
        jdbc.update(
                """
                INSERT INTO comp.kyc_reviews
                  (id, organization_id, user_id, status, outcome, reviewed_by, completed_at,
                   created_at, updated_at, correlation_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                row.id(),
                row.organizationId(),
                row.userId(),
                row.status(),
                row.outcome(),
                row.reviewedBy(),
                JdbcTypes.ts(row.completedAt()),
                JdbcTypes.ts(now),
                JdbcTypes.ts(now),
                correlationId);
    }

    public void updateKyc(KycRow row, Instant now) {
        jdbc.update(
                """
                UPDATE comp.kyc_reviews
                SET status = ?, outcome = ?, reviewed_by = ?, completed_at = ?, updated_at = ?
                WHERE id = ? AND organization_id = ?
                """,
                row.status(),
                row.outcome(),
                row.reviewedBy(),
                JdbcTypes.ts(row.completedAt()),
                JdbcTypes.ts(now),
                row.id(),
                row.organizationId());
    }

    public Optional<KycRow> findKyc(UUID id, UUID org) {
        List<KycRow> rows = jdbc.query(
                """
                SELECT id, organization_id, user_id, status, outcome, reviewed_by, completed_at, created_at
                FROM comp.kyc_reviews WHERE id = ? AND organization_id = ?
                """,
                this::mapKyc,
                id,
                org);
        return rows.stream().findFirst();
    }

    public int countKyc(UUID org) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM comp.kyc_reviews WHERE organization_id = ?", Integer.class, org);
        return n == null ? 0 : n;
    }

    public void insertAml(UUID id, UUID org, String subjectRef, UUID caseId, String status, Instant now, UUID actor) {
        jdbc.update(
                """
                INSERT INTO comp.aml_reviews
                  (id, organization_id, subject_ref, case_id, status, outcome, reviewed_by, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, NULL, ?, ?, ?)
                """,
                id,
                org,
                subjectRef,
                caseId,
                status,
                actor,
                JdbcTypes.ts(now),
                JdbcTypes.ts(now));
    }

    public void insertTravel(UUID id, UUID org, String txRef, String status, String resultJson, Instant now, UUID actor) {
        jdbc.update(
                """
                INSERT INTO comp.travel_rule_validations
                  (id, organization_id, transaction_ref, status, validation_result, validated_at, validated_by)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                org,
                txRef,
                status,
                JdbcTypes.jsonb(resultJson),
                JdbcTypes.ts(now),
                actor);
    }

    public void insertSanctions(SanctionsRow row, UUID disposedBy, Instant disposedAt) {
        jdbc.update(
                """
                INSERT INTO comp.sanctions_screenings
                  (id, organization_id, subject_ref, match_status, disposition, screened_at, disposed_by, disposed_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                row.id(),
                row.organizationId(),
                row.subjectRef(),
                row.matchStatus(),
                row.disposition(),
                JdbcTypes.ts(row.screenedAt()),
                disposedBy,
                JdbcTypes.ts(disposedAt));
    }

    public void updateSanctionsDisposition(UUID id, UUID org, String disposition, UUID actor, Instant now) {
        jdbc.update(
                """
                UPDATE comp.sanctions_screenings
                SET disposition = ?, disposed_by = ?, disposed_at = ?
                WHERE id = ? AND organization_id = ?
                """,
                disposition,
                actor,
                JdbcTypes.ts(now),
                id,
                org);
    }

    public Optional<SanctionsRow> findSanctions(UUID id, UUID org) {
        List<SanctionsRow> rows = jdbc.query(
                """
                SELECT id, organization_id, subject_ref, match_status, disposition, screened_at
                FROM comp.sanctions_screenings WHERE id = ? AND organization_id = ?
                """,
                this::mapSanctions,
                id,
                org);
        return rows.stream().findFirst();
    }

    public void insertAuditPackage(UUID id, UUID org, String packageType, String status, String artifacts, UUID actor, Instant now) {
        jdbc.update(
                """
                INSERT INTO comp.audit_packages
                  (id, organization_id, package_type, status, artifact_refs, prepared_by, prepared_at, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                org,
                packageType,
                status,
                JdbcTypes.jsonb(artifacts),
                actor,
                JdbcTypes.ts(now),
                JdbcTypes.ts(now));
    }

    public void insertRecord(UUID org, String recordType, UUID sourceId, String status, Instant now) {
        jdbc.update(
                """
                INSERT INTO comp.compliance_records (id, organization_id, record_type, source_id, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                org,
                recordType,
                sourceId,
                status,
                JdbcTypes.ts(now));
    }

    public boolean recordExists(UUID org, String recordType, UUID sourceId) {
        Integer n = jdbc.queryForObject(
                """
                SELECT COUNT(*) FROM comp.compliance_records
                WHERE organization_id = ? AND record_type = ? AND source_id = ?
                """,
                Integer.class,
                org,
                recordType,
                sourceId);
        return n != null && n > 0;
    }

    public int countRecords(UUID org, String recordType) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM comp.compliance_records WHERE organization_id = ? AND record_type = ?",
                Integer.class,
                org,
                recordType);
        return n == null ? 0 : n;
    }

    public boolean aiSchemaExists() {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = 'ai'", Integer.class);
        return n != null && n > 0;
    }

    private KycRow mapKyc(ResultSet rs, int i) throws SQLException {
        return new KycRow(
                rs.getObject("id", UUID.class),
                rs.getObject("organization_id", UUID.class),
                rs.getObject("user_id", UUID.class),
                rs.getString("status"),
                rs.getString("outcome"),
                rs.getObject("reviewed_by", UUID.class),
                JdbcTypes.instant(rs.getTimestamp("completed_at")),
                JdbcTypes.instant(rs.getTimestamp("created_at")));
    }

    private SanctionsRow mapSanctions(ResultSet rs, int i) throws SQLException {
        return new SanctionsRow(
                rs.getObject("id", UUID.class),
                rs.getObject("organization_id", UUID.class),
                rs.getString("subject_ref"),
                rs.getString("match_status"),
                rs.getString("disposition"),
                JdbcTypes.instant(rs.getTimestamp("screened_at")));
    }
}
