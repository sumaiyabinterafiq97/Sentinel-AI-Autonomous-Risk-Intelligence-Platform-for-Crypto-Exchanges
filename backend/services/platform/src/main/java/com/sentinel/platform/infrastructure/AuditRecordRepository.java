package com.sentinel.platform.infrastructure;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuditRecordRepository {

    private final JdbcTemplate jdbc;

    public AuditRecordRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(
            UUID id,
            UUID organizationId,
            UUID actorId,
            String actorType,
            String action,
            String resourceType,
            UUID resourceId,
            String outcome,
            UUID correlationId,
            UUID requestId,
            String metadataJson,
            Instant createdAt) {
        jdbc.update(
                """
                INSERT INTO core.audit_records
                  (id, organization_id, actor_id, actor_type, action, resource_type, resource_id,
                   outcome, correlation_id, request_id, metadata, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                organizationId,
                actorId,
                actorType,
                action,
                resourceType,
                resourceId,
                outcome,
                correlationId,
                requestId,
                PlatformConfigRepository.jsonb(metadataJson == null ? "{}" : metadataJson),
                PlatformConfigRepository.ts(createdAt));
    }

    public int countByOrgAndAction(UUID organizationId, String action) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM core.audit_records WHERE organization_id = ? AND action = ?",
                Integer.class,
                organizationId,
                action);
        return n == null ? 0 : n;
    }

    public List<String> actionsForOrg(UUID organizationId) {
        return jdbc.query(
                "SELECT action FROM core.audit_records WHERE organization_id = ? ORDER BY created_at",
                (rs, i) -> rs.getString("action"),
                organizationId);
    }
}
