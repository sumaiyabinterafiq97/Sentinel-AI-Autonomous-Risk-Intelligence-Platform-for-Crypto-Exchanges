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
class AdminApiContractSurfaceIT extends PlatformPostgresIT {

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mappings;

    @Test
    void platformExposesOnlyDocumentedAdminInventoryPaths() {
        Set<String> documented = Set.of(
                "GET /v1/admin/settings",
                "PATCH /v1/admin/settings",
                "GET /v1/admin/integrations",
                "POST /v1/admin/integrations",
                "PATCH /v1/admin/integrations",
                "POST /v1/admin/users/provision",
                "POST /v1/admin/organizations/provision",
                "GET /v1/admin/audit-records");
        Set<String> implemented = mappings.getHandlerMethods().keySet().stream()
                .flatMap(info -> info.getPatternValues().stream()
                        .flatMap(pattern -> info.getMethodsCondition().getMethods().stream()
                                .map(method -> method.name() + " " + pattern)))
                .filter(s -> s.contains("/v1/admin"))
                .collect(Collectors.toSet());
        assertEquals(documented, implemented);
        assertTrue(implemented.stream().noneMatch(s -> s.contains("integration-health") || s.contains("/reports")));
    }
}
