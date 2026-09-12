package com.sentinel.platform.application;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.sentinel.platform.api.RequestContext;
import com.sentinel.platform.domain.CoreKeys;
import com.sentinel.platform.domain.HealthStatus;
import com.sentinel.platform.domain.PlatformConfigEntry;
import com.sentinel.platform.infrastructure.PlatformConfigRepository;

@Component
@Order(1)
public class PlatformInitializationService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PlatformInitializationService.class);

    private final PlatformConfigRepository configs;
    private final PlatformHealthService health;
    private final AuditService audit;

    public PlatformInitializationService(
            PlatformConfigRepository configs, PlatformHealthService health, AuditService audit) {
        this.configs = configs;
        this.health = health;
        this.audit = audit;
    }

    @Override
    public void run(ApplicationArguments args) {
        RequestContext ctx = RequestContext.get();
        ctx.setActorType("system");
        ctx.setRequestId(UUID.randomUUID());
        ctx.setCorrelationId(UUID.randomUUID());
        try {
            if (!configs.databaseReachable()) {
                health.markReady(false);
                log.error("CORE initialization failed: database not reachable");
                return;
            }
            Instant now = Instant.now();
            if (configs.findGlobal(CoreKeys.MAINTENANCE_MODE).isEmpty()) {
                configs.insert(new PlatformConfigEntry(
                        UUID.randomUUID(),
                        null,
                        CoreKeys.MAINTENANCE_MODE,
                        "false",
                        1,
                        now,
                        now,
                        null,
                        null));
            }
            health.markReady(true);
            health.recordSnapshot("platform", HealthStatus.healthy, Map.of("phase", "init"));
            audit.record("PLATFORM_INITIALIZED", "PLATFORM", null, "SUCCESS", Map.of("ready", true));
            log.info(
                    "CORE platform initialized requestId={} correlationId={}",
                    ctx.requestId(),
                    ctx.correlationId());
        } catch (RuntimeException ex) {
            health.markReady(false);
            log.error("CORE initialization failed");
        } finally {
            RequestContext.clear();
        }
    }
}
