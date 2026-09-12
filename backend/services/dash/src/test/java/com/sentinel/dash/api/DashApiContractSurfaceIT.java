package com.sentinel.dash.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.sentinel.dash.DashPostgresIT;

@SpringBootTest
class DashApiContractSurfaceIT extends DashPostgresIT {

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mappings;

    @Test
    void dashExposesOnlyDocumentedWorkspaceApis() {
        Set<String> documented = Set.of(
                "GET /health",
                "GET /v1/workspace",
                "GET /v1/workspace/dashboard",
                "GET /v1/workspace/queues/{queueType}",
                "GET /v1/workspace/widgets",
                "POST /v1/workspace/widgets/{widgetId}/interactions",
                "GET /v1/workspace/subscriptions/{channel}");
        Set<String> implemented = mappings.getHandlerMethods().keySet().stream()
                .flatMap(info -> info.getPatternValues().stream()
                        .flatMap(pattern -> info.getMethodsCondition().getMethods().stream()
                                .map(method -> method.name() + " " + pattern)))
                .filter(s -> s.contains("/v1/") || s.equals("GET /health"))
                .collect(Collectors.toSet());
        assertEquals(documented, implemented);
        assertTrue(implemented.stream().noneMatch(s -> s.contains("/reports") || s.contains("/admin")));
    }
}
