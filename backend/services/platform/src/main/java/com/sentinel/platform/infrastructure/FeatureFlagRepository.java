package com.sentinel.platform.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.sentinel.platform.domain.FeatureFlagEntry;

@Repository
public class FeatureFlagRepository {

    private final JdbcTemplate jdbc;

    public FeatureFlagRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<FeatureFlagEntry> findVisible(UUID organizationId) {
        return jdbc.query(
                """
                SELECT id, organization_id, flag_key, enabled, metadata::text, updated_at
                FROM core.feature_flags
                WHERE organization_id IS NULL OR organization_id = ?
                ORDER BY flag_key
                """,
                this::mapRow,
                organizationId);
    }

    public Optional<FeatureFlagEntry> findByOrgAndKey(UUID organizationId, String key) {
        List<FeatureFlagEntry> rows = jdbc.query(
                """
                SELECT id, organization_id, flag_key, enabled, metadata::text, updated_at
                FROM core.feature_flags
                WHERE organization_id IS NOT DISTINCT FROM ? AND flag_key = ?
                """,
                this::mapRow,
                organizationId,
                key);
        return rows.stream().findFirst();
    }

    public void insert(FeatureFlagEntry entry) {
        jdbc.update(
                """
                INSERT INTO core.feature_flags (id, organization_id, flag_key, enabled, metadata, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                entry.id(),
                entry.organizationId(),
                entry.flagKey(),
                entry.enabled(),
                PlatformConfigRepository.jsonb(entry.metadataJson() == null ? "{}" : entry.metadataJson()),
                PlatformConfigRepository.ts(entry.updatedAt()));
    }

    public void update(FeatureFlagEntry entry) {
        jdbc.update(
                """
                UPDATE core.feature_flags
                SET enabled = ?, metadata = ?, updated_at = ?
                WHERE id = ?
                """,
                entry.enabled(),
                PlatformConfigRepository.jsonb(entry.metadataJson() == null ? "{}" : entry.metadataJson()),
                PlatformConfigRepository.ts(entry.updatedAt()),
                entry.id());
    }

    private FeatureFlagEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new FeatureFlagEntry(
                rs.getObject("id", UUID.class),
                rs.getObject("organization_id", UUID.class),
                rs.getString("flag_key"),
                rs.getBoolean("enabled"),
                rs.getString("metadata"),
                PlatformConfigRepository.instant(rs, "updated_at"));
    }
}
