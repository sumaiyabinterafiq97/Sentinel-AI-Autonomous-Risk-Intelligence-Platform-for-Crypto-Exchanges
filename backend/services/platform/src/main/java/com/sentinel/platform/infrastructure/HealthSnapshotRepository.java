package com.sentinel.platform.infrastructure;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HealthSnapshotRepository {

    private final JdbcTemplate jdbc;

    public HealthSnapshotRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(UUID id, String component, String status, Instant recordedAt, String metadataJson) {
        jdbc.update(
                """
                INSERT INTO core.platform_health_snapshots (id, component, status, recorded_at, metadata)
                VALUES (?, ?, ?, ?, ?)
                """,
                id,
                component,
                status,
                PlatformConfigRepository.ts(recordedAt),
                PlatformConfigRepository.jsonb(metadataJson == null ? "{}" : metadataJson));
    }

    public Optional<String> latestStatus(String component) {
        List<String> rows = jdbc.query(
                """
                SELECT status FROM core.platform_health_snapshots
                WHERE component = ?
                ORDER BY recorded_at DESC
                LIMIT 1
                """,
                (rs, i) -> rs.getString("status"),
                component);
        return rows.stream().findFirst();
    }

    public int count() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM core.platform_health_snapshots", Integer.class);
        return n == null ? 0 : n;
    }
}
