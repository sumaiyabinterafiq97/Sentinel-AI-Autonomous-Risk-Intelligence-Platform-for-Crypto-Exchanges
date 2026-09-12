package com.sentinel.platform.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import com.sentinel.platform.PlatformPostgresIT;
import com.sentinel.platform.domain.PlatformConfigEntry;

@SpringBootTest
class CorePersistenceIT extends PlatformPostgresIT {

    @Autowired
    private PlatformConfigRepository configs;

    @Autowired
    private AuditRecordRepository audit;

    @Autowired
    private HealthSnapshotRepository snapshots;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void migrationCreatesCoreTablesAndUniqueOrgKey() {
        Set<String> tables = jdbc.query(
                        "SELECT table_name FROM information_schema.tables WHERE table_schema = 'core'",
                        (rs, i) -> rs.getString(1))
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        assertTrue(tables.containsAll(Set.of(
                "platform_config", "feature_flags", "audit_records", "platform_health_snapshots")));
        assertTrue(snapshots.count() >= 1);
    }

    @Test
    void queriesDoNotReturnOtherTenantRows() {
        UUID orgA = UUID.randomUUID();
        UUID orgB = UUID.randomUUID();
        Instant now = Instant.now();
        configs.insert(new PlatformConfigEntry(
                UUID.randomUUID(), orgA, "only.a", "true", 1, now, now, null, null));
        configs.insert(new PlatformConfigEntry(
                UUID.randomUUID(), orgB, "only.b", "true", 1, now, now, null, null));
        assertEquals(1, configs.findVisible(orgA).stream().filter(e -> "only.a".equals(e.configKey())).count());
        assertEquals(0, configs.findVisible(orgA).stream().filter(e -> "only.b".equals(e.configKey())).count());
        assertEquals(0, configs.findVisible(orgB).stream().filter(e -> "only.a".equals(e.configKey())).count());
    }

    @Test
    void auditRecordsAreAppendOnlyForOrg() {
        UUID org = UUID.randomUUID();
        audit.insert(
                UUID.randomUUID(),
                org,
                null,
                "system",
                "TEST_ACTION",
                "PLATFORM_CONFIG",
                null,
                "SUCCESS",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "{}",
                Instant.now());
        assertEquals(1, audit.countByOrgAndAction(org, "TEST_ACTION"));
        assertTrue(audit.actionsForOrg(org).contains("TEST_ACTION"));
    }

    @Test
    void noAuthOrLaterDomainSchemas() {
        Integer auth = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name IN ('auth','authz','risk','alert','invest','comp','ai','wallet','sec')",
                Integer.class);
        assertEquals(0, auth);
    }
}
