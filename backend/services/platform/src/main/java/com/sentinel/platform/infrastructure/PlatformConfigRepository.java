package com.sentinel.platform.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.sentinel.platform.domain.PlatformConfigEntry;

@Repository
public class PlatformConfigRepository {

    private final JdbcTemplate jdbc;

    public PlatformConfigRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<PlatformConfigEntry> findVisible(UUID organizationId) {
        return jdbc.query(
                """
                SELECT id, organization_id, config_key, config_value::text, version,
                       created_at, updated_at, created_by, updated_by
                FROM core.platform_config
                WHERE organization_id IS NULL OR organization_id = ?
                ORDER BY config_key
                """,
                this::mapRow,
                organizationId);
    }

    public Optional<PlatformConfigEntry> findByOrgAndKey(UUID organizationId, String key) {
        List<PlatformConfigEntry> rows = jdbc.query(
                """
                SELECT id, organization_id, config_key, config_value::text, version,
                       created_at, updated_at, created_by, updated_by
                FROM core.platform_config
                WHERE organization_id IS NOT DISTINCT FROM ? AND config_key = ?
                """,
                this::mapRow,
                organizationId,
                key);
        return rows.stream().findFirst();
    }

    public Optional<PlatformConfigEntry> findGlobal(String key) {
        return findByOrgAndKey(null, key);
    }

    public void insert(PlatformConfigEntry entry) {
        jdbc.update(
                """
                INSERT INTO core.platform_config
                  (id, organization_id, config_key, config_value, version, created_at, updated_at, created_by, updated_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                entry.id(),
                entry.organizationId(),
                entry.configKey(),
                jsonb(entry.configValueJson()),
                entry.version(),
                ts(entry.createdAt()),
                ts(entry.updatedAt()),
                entry.createdBy(),
                entry.updatedBy());
    }

    public void update(PlatformConfigEntry entry) {
        jdbc.update(
                """
                UPDATE core.platform_config
                SET config_value = ?, version = ?, updated_at = ?, updated_by = ?
                WHERE id = ?
                """,
                jsonb(entry.configValueJson()),
                entry.version(),
                ts(entry.updatedAt()),
                entry.updatedBy(),
                entry.id());
    }

    public boolean databaseReachable() {
        try {
            Integer one = jdbc.queryForObject("SELECT 1", Integer.class);
            return one != null && one == 1;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private PlatformConfigEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new PlatformConfigEntry(
                rs.getObject("id", UUID.class),
                rs.getObject("organization_id", UUID.class),
                rs.getString("config_key"),
                rs.getString("config_value"),
                rs.getInt("version"),
                instant(rs, "created_at"),
                instant(rs, "updated_at"),
                rs.getObject("created_by", UUID.class),
                rs.getObject("updated_by", UUID.class));
    }

    static PGobject jsonb(String json) {
        try {
            PGobject obj = new PGobject();
            obj.setType("jsonb");
            obj.setValue(json);
            return obj;
        } catch (SQLException e) {
            throw new IllegalArgumentException("Invalid JSON", e);
        }
    }

    static Timestamp ts(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts == null ? null : ts.toInstant();
    }
}
