package com.sentinel.platform.contracts;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class CoreOpenApiContractCompatibilityTest {

    @Test
    void packagedOpenApiContainsCoreOperationIds() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("OpenAPI.yaml")) {
            assertNotNull(in);
            String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(yaml.contains("operationId: getPlatformHealth"));
            assertTrue(yaml.contains("operationId: getPlatformStatus"));
            assertTrue(yaml.contains("operationId: getPlatformConfig"));
            assertTrue(yaml.contains("operationId: patchPlatformConfig"));
            assertTrue(yaml.contains("operationId: listFeatureFlags"));
            assertTrue(yaml.contains("operationId: patchFeatureFlag"));
            assertTrue(yaml.contains("/v1/platform/health"));
            assertTrue(yaml.contains("x-api-id: API-CORE-001"));
        }
    }
}
