package com.sentinel.platform.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.sentinel.platform.domain.AdminAuditItem;
import com.sentinel.platform.domain.AdminSettingRow;
import com.sentinel.platform.domain.CoreException;
import com.sentinel.platform.domain.IntegrationRow;

@Repository
public class AdminStore {

    private final JdbcTemplate jdbc;

    public AdminStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<AdminSettingRow> listSettings(UUID organizationId) {
        return jdbc.query(
                """
                SELECT id, organization_id, setting_key, setting_value::text, updated_by, updated_at, created_at
                FROM admin.admin_settings
                WHERE organization_id = ?
                ORDER BY setting_key
                """,
                this::mapSetting,
                organizationId);
    }

    public Optional<AdminSettingRow> findSetting(UUID organizationId, String key) {
        List<AdminSettingRow> rows = jdbc.query(
                """
                SELECT id, organization_id, setting_key, setting_value::text, updated_by, updated_at, created_at
                FROM admin.admin_settings
                WHERE organization_id = ? AND setting_key = ?
                """,
                this::mapSetting,
                organizationId,
                key);
        return rows.stream().findFirst();
    }

    public void upsertSetting(AdminSettingRow row) {
        jdbc.update(
                """
                INSERT INTO admin.admin_settings
                  (id, organization_id, setting_key, setting_value, updated_by, updated_at, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (organization_id, setting_key) DO UPDATE
                SET setting_value = EXCLUDED.setting_value,
                    updated_by = EXCLUDED.updated_by,
                    updated_at = EXCLUDED.updated_at
                """,
                row.id(),
                row.organizationId(),
                row.settingKey(),
                PlatformConfigRepository.jsonb(row.settingValueJson()),
                row.updatedBy(),
                PlatformConfigRepository.ts(row.updatedAt()),
                PlatformConfigRepository.ts(row.createdAt()));
    }

    public List<IntegrationRow> listIntegrations(UUID organizationId, String cursor, int limit) {
        UUID after = parseCursor(cursor);
        return jdbc.query(
                """
                SELECT id, organization_id, integration_type, config::text, secret_ref, status,
                       configured_by, configured_at, updated_at
                FROM admin.integration_configs
                WHERE organization_id = ?
                  AND (?::uuid IS NULL OR id > ?)
                ORDER BY id
                LIMIT ?
                """,
                this::mapIntegration,
                organizationId,
                after,
                after,
                limit);
    }

    public Optional<IntegrationRow> findIntegrationById(UUID organizationId, UUID id) {
        List<IntegrationRow> rows = jdbc.query(
                """
                SELECT id, organization_id, integration_type, config::text, secret_ref, status,
                       configured_by, configured_at, updated_at
                FROM admin.integration_configs
                WHERE organization_id = ? AND id = ?
                """,
                this::mapIntegration,
                organizationId,
                id);
        return rows.stream().findFirst();
    }

    public Optional<IntegrationRow> findIntegrationByType(UUID organizationId, String type) {
        List<IntegrationRow> rows = jdbc.query(
                """
                SELECT id, organization_id, integration_type, config::text, secret_ref, status,
                       configured_by, configured_at, updated_at
                FROM admin.integration_configs
                WHERE organization_id = ? AND integration_type = ?
                ORDER BY CASE WHEN status = 'active' THEN 0 ELSE 1 END, updated_at DESC
                LIMIT 1
                """,
                this::mapIntegration,
                organizationId,
                type);
        return rows.stream().findFirst();
    }

    public void insertIntegration(IntegrationRow row) {
        try {
            jdbc.update(
                    """
                    INSERT INTO admin.integration_configs
                      (id, organization_id, integration_type, config, secret_ref, status,
                       configured_by, configured_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    row.id(),
                    row.organizationId(),
                    row.integrationType(),
                    PlatformConfigRepository.jsonb(row.configJson()),
                    row.secretRef(),
                    row.status(),
                    row.configuredBy(),
                    PlatformConfigRepository.ts(row.configuredAt()),
                    PlatformConfigRepository.ts(row.updatedAt()));
        } catch (DuplicateKeyException ex) {
            throw CoreException.adminValidation("type", "An active integration of this type already exists");
        }
    }

    public void updateIntegration(IntegrationRow row) {
        try {
            jdbc.update(
                    """
                    UPDATE admin.integration_configs
                    SET config = ?, secret_ref = ?, status = ?, updated_at = ?
                    WHERE id = ? AND organization_id = ?
                    """,
                    PlatformConfigRepository.jsonb(row.configJson()),
                    row.secretRef(),
                    row.status(),
                    PlatformConfigRepository.ts(row.updatedAt()),
                    row.id(),
                    row.organizationId());
        } catch (DuplicateKeyException ex) {
            throw CoreException.adminValidation("status", "An active integration of this type already exists");
        }
    }

    public void insertAction(
            UUID id,
            UUID organizationId,
            String action,
            String targetType,
            UUID targetId,
            UUID actorId,
            String outcome,
            String metadataJson,
            Instant createdAt) {
        jdbc.update(
                """
                INSERT INTO admin.admin_action_log
                  (id, organization_id, action, target_type, target_id, actor_id, outcome, created_at, metadata)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                organizationId,
                action,
                targetType,
                targetId,
                actorId,
                outcome,
                PlatformConfigRepository.ts(createdAt),
                PlatformConfigRepository.jsonb(metadataJson == null ? "{}" : metadataJson));
    }

    public List<AdminAuditItem> listAudit(UUID organizationId, Instant cursorTime, UUID cursorId, int limit) {
        return jdbc.query(
                """
                SELECT id, action, actor_id, occurred_at FROM (
                  SELECT id, action, actor_id, created_at AS occurred_at
                  FROM admin.admin_action_log
                  WHERE organization_id = ?
                  UNION ALL
                  SELECT id, action, actor_id, created_at
                  FROM core.audit_records
                  WHERE organization_id = ?
                ) records
                WHERE (occurred_at, id) < (?, ?)
                ORDER BY occurred_at DESC, id DESC
                LIMIT ?
                """,
                this::mapAudit,
                organizationId,
                organizationId,
                PlatformConfigRepository.ts(cursorTime),
                cursorId,
                limit);
    }

    public int countUserTablesInAdmin() {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'admin' AND table_name IN ('users','organizations')",
                Integer.class);
        return n == null ? 0 : n;
    }

    private AdminSettingRow mapSetting(ResultSet rs, int i) throws SQLException {
        return new AdminSettingRow(
                rs.getObject("id", UUID.class),
                rs.getObject("organization_id", UUID.class),
                rs.getString("setting_key"),
                rs.getString("setting_value"),
                rs.getObject("updated_by", UUID.class),
                PlatformConfigRepository.instant(rs, "updated_at"),
                PlatformConfigRepository.instant(rs, "created_at"));
    }

    private IntegrationRow mapIntegration(ResultSet rs, int i) throws SQLException {
        return new IntegrationRow(
                rs.getObject("id", UUID.class),
                rs.getObject("organization_id", UUID.class),
                rs.getString("integration_type"),
                rs.getString("config"),
                rs.getString("secret_ref"),
                rs.getString("status"),
                rs.getObject("configured_by", UUID.class),
                PlatformConfigRepository.instant(rs, "configured_at"),
                PlatformConfigRepository.instant(rs, "updated_at"));
    }

    private AdminAuditItem mapAudit(ResultSet rs, int i) throws SQLException {
        return new AdminAuditItem(
                rs.getObject("id", UUID.class),
                rs.getString("action"),
                rs.getObject("actor_id", UUID.class),
                PlatformConfigRepository.instant(rs, "occurred_at"));
    }

    private static UUID parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(cursor);
        } catch (IllegalArgumentException ex) {
            throw CoreException.adminValidation("cursor", "Invalid cursor");
        }
    }
}
