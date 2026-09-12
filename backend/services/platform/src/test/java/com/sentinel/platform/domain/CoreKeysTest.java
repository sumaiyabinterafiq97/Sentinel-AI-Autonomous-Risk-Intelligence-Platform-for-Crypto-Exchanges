package com.sentinel.platform.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CoreKeysTest {

    @Test
    void acceptsValidKeys() {
        assertDoesNotThrow(() -> CoreKeys.requireConfigKey("maintenance.mode"));
        assertDoesNotThrow(() -> CoreKeys.requireFlagKey("risk-engine:v1"));
    }

    @Test
    void rejectsInvalidKeys() {
        CoreException ex = assertThrows(CoreException.class, () -> CoreKeys.requireConfigKey(""));
        assertEquals("CORE_VALIDATION_001", ex.code());
        assertEquals(400, ex.httpStatus());
        assertThrows(CoreException.class, () -> CoreKeys.requireFlagKey("1bad"));
        assertThrows(CoreException.class, () -> CoreKeys.requireConfigKey("has space"));
    }
}
