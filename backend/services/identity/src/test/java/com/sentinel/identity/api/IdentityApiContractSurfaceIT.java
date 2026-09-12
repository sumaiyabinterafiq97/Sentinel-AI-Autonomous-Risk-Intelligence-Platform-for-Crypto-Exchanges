package com.sentinel.identity.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.sentinel.identity.IdentityPostgresIT;

@SpringBootTest
class IdentityApiContractSurfaceIT extends IdentityPostgresIT {

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mappings;

    @Test
    void identityExposesOnlyM2InventoryPaths() {
        Set<String> documented = Set.of(
                "GET /health",
                "POST /v1/auth/login",
                "POST /v1/auth/logout",
                "POST /v1/auth/refresh",
                "GET /v1/auth/session",
                "POST /v1/auth/mfa/verify",
                "POST /v1/authz/evaluate",
                "GET /v1/authz/roles",
                "POST /v1/authz/roles",
                "POST /v1/authz/role-assignments",
                "GET /v1/users",
                "POST /v1/users",
                "GET /v1/users/{userId}",
                "PATCH /v1/users/{userId}",
                "GET /v1/organizations",
                "POST /v1/organizations",
                "PATCH /v1/organizations/{orgId}");
        Set<String> implemented = mappings.getHandlerMethods().keySet().stream()
                .flatMap(info -> info.getPatternValues().stream()
                        .flatMap(pattern -> info.getMethodsCondition().getMethods().stream()
                                .map(method -> method.name() + " " + pattern)))
                .filter(s -> s.contains("/v1/") || s.equals("GET /health"))
                .collect(Collectors.toSet());
        assertEquals(documented, implemented);
        assertTrue(implemented.stream().noneMatch(s -> s.contains("/alerts") || s.contains("/risk") || s.contains("/platform")));
    }
}
