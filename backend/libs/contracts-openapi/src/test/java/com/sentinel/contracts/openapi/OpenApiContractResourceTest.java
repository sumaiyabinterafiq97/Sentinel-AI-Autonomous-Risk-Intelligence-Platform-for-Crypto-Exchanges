package com.sentinel.contracts.openapi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class OpenApiContractResourceTest {

    @Test
    void packagedOpenApiIsOnClasspath() {
        assertNotNull(getClass().getClassLoader().getResource("OpenAPI.yaml"));
    }
}
