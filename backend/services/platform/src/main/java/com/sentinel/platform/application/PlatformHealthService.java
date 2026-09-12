package com.sentinel.platform.application;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;
import com.sentinel.platform.domain.HealthStatus;
import com.sentinel.platform.domain.OperationalStatus;
import com.sentinel.platform.infrastructure.HealthSnapshotRepository;
import com.sentinel.platform.infrastructure.PlatformConfigRepository;

@Service
public class PlatformHealthService {

    private final PlatformConfigRepository configs;
    private final HealthSnapshotRepository snapshots;
    private final PlatformConfigService configService;
    private final AtomicBoolean ready = new AtomicBoolean(false);
    private final AtomicReference<HealthStatus> lastHealth = new AtomicReference<>(HealthStatus.unhealthy);

    public PlatformHealthService(
            PlatformConfigRepository configs,
            HealthSnapshotRepository snapshots,
            PlatformConfigService configService) {
        this.configs = configs;
        this.snapshots = snapshots;
        this.configService = configService;
    }

    public void markReady(boolean value) {
        ready.set(value);
    }

    public boolean ready() {
        return ready.get();
    }

    public HealthStatus evaluateHealth(UUID organizationId) {
        boolean db = configs.databaseReachable();
        if (!db || !ready.get()) {
            HealthStatus status = HealthStatus.unhealthy;
            recordIfChanged("platform", status, Map.of("database", db, "ready", ready.get()));
            lastHealth.set(status);
            return status;
        }
        boolean maintenance = false;
        if (organizationId != null) {
            maintenance = configService.maintenanceMode(organizationId);
        }
        HealthStatus status = maintenance ? HealthStatus.degraded : HealthStatus.healthy;
        recordIfChanged("platform", status, Map.of("database", true, "maintenance", maintenance));
        lastHealth.set(status);
        return status;
    }

    public Map<String, Object> statusPayload(UUID organizationId) {
        HealthStatus health = evaluateHealth(organizationId);
        boolean maintenance = organizationId != null && configService.maintenanceMode(organizationId);
        OperationalStatus operational;
        if (health == HealthStatus.unhealthy) {
            operational = OperationalStatus.unavailable;
        } else if (maintenance) {
            operational = OperationalStatus.maintenance;
        } else {
            operational = OperationalStatus.available;
        }
        Map<String, Object> optional = new LinkedHashMap<>();
        optional.put("ai", "degraded");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("operationalStatus", operational.name());
        data.put("maintenanceMode", maintenance);
        data.put("health", health.name());
        data.put("ready", ready.get() && health != HealthStatus.unhealthy);
        data.put("optionalCapabilities", optional);
        return data;
    }

    public void recordSnapshot(String component, HealthStatus status, Map<String, ?> metadata) {
        snapshots.insert(UUID.randomUUID(), component, status.name(), Instant.now(), json(metadata));
    }

    private void recordIfChanged(String component, HealthStatus status, Map<String, ?> metadata) {
        String previous = snapshots.latestStatus(component).orElse(null);
        if (previous == null || !previous.equals(status.name())) {
            recordSnapshot(component, status, metadata);
        }
    }

    private static String json(Map<String, ?> metadata) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, ?> e : metadata.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(e.getKey()).append("\":");
            Object v = e.getValue();
            if (v instanceof Boolean || v instanceof Number) {
                sb.append(v);
            } else {
                sb.append('"').append(String.valueOf(v)).append('"');
            }
        }
        return sb.append('}').toString();
    }
}
