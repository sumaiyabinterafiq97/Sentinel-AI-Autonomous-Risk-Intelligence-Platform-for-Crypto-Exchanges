package com.sentinel.platform.contracts;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class AdminOpenApiContractCompatibilityTest {

    @Test
    void packagedOpenApiContainsAdminOperationIds() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("OpenAPI.yaml")) {
            assertNotNull(in);
            String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(yaml.contains("operationId: getAdminSettings"));
            assertTrue(yaml.contains("operationId: patchAdminSettings"));
            assertTrue(yaml.contains("operationId: listIntegrations"));
            assertTrue(yaml.contains("operationId: createIntegration"));
            assertTrue(yaml.contains("operationId: patchIntegration"));
            assertTrue(yaml.contains("operationId: provisionUser"));
            assertTrue(yaml.contains("operationId: provisionOrganization"));
            assertTrue(yaml.contains("operationId: listAdminAuditRecords"));
            assertTrue(yaml.contains("x-api-id: API-ADMIN-001"));
            assertTrue(yaml.contains("x-api-id: API-ADMIN-005"));
            assertFalse(yaml.contains("operationId: getAdminIntegrationHealth"));
        }
    }
}
