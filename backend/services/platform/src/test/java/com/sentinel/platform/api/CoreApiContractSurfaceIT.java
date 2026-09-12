package com.sentinel.platform.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.sentinel.platform.PlatformPostgresIT;

@SpringBootTest
class CoreApiContractSurfaceIT extends PlatformPostgresIT {

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mappings;

    @Test
    void platformExposesOnlyDocumentedCoreInventoryPaths() {
        Set<String> documented =
                Set.of(
                        "GET /health",
                        "GET /v1/platform/health",
                        "GET /v1/platform/status",
                        "GET /v1/platform/config",
                        "PATCH /v1/platform/config",
                        "GET /v1/platform/feature-flags",
                        "PATCH /v1/platform/feature-flags/{flagKey}");
        Set<String> implemented = mappings.getHandlerMethods().keySet().stream()
                .flatMap(info -> info.getPatternValues().stream()
                        .flatMap(pattern -> info.getMethodsCondition().getMethods().stream()
                                .map(method -> method.name() + " " + pattern)))
                .filter(s -> s.contains("/v1/platform") || s.equals("GET /health"))
                .collect(Collectors.toSet());
        assertEquals(documented, implemented);
        assertTrue(implemented.stream().noneMatch(s -> s.contains("/auth") || s.contains("/risk") || s.contains("/alerts")));
    }
}
