package com.sentinel.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SecurityScaffoldingTest {

    @Test
    void requestIdHeaderNameIsStable() {
        assertEquals("X-Request-Id", SecurityScaffolding.REQUEST_ID_HEADER);
    }
}
