package com.sentinel.platform.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.sentinel.platform.PlatformPostgresIT;
import com.sentinel.platform.domain.HealthStatus;

@SpringBootTest
class PlatformInitializationIT extends PlatformPostgresIT {

    @Autowired
    private PlatformHealthService health;

    @Test
    void initializesReadyHealthyPlatformWithoutBlockingOnOptionalAi() {
        assertTrue(health.ready());
        assertEquals(HealthStatus.healthy, health.evaluateHealth(null));
        var status = health.statusPayload(java.util.UUID.randomUUID());
        assertEquals("degraded", ((java.util.Map<?, ?>) status.get("optionalCapabilities")).get("ai"));
        assertEquals(Boolean.TRUE, status.get("ready"));
    }
}
