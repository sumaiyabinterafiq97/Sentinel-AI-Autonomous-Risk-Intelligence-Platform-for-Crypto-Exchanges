package com.sentinel.ops.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AlertLifecycleTest {

    @Test
    void openMayPatchToTriaged() {
        assertDoesNotThrow(() -> AlertLifecycle.requirePatchable("open", "triaged"));
        assertDoesNotThrow(() -> AlertLifecycle.requirePatchable("open", "open"));
    }

    @Test
    void patchCannotCloseOrAssign() {
        AlertException assign = assertThrows(AlertException.class, () -> AlertLifecycle.requirePatchable("open", "assigned"));
        assertEquals("ALERT_VALIDATION_001", assign.code());
        AlertException close = assertThrows(AlertException.class, () -> AlertLifecycle.requirePatchable("open", "closed"));
        assertEquals("ALERT_VALIDATION_001", close.code());
    }

    @Test
    void cannotPatchClosed() {
        AlertException ex = assertThrows(AlertException.class, () -> AlertLifecycle.requirePatchable("closed", "triaged"));
        assertEquals(409, ex.httpStatus());
    }

    @Test
    void assignedCannotReturnToTriagedViaPatch() {
        assertThrows(AlertException.class, () -> AlertLifecycle.requirePatchable("assigned", "triaged"));
    }

    @Test
    void assignAndCloseGuards() {
        assertDoesNotThrow(() -> AlertLifecycle.requireAssignable("open"));
        assertDoesNotThrow(() -> AlertLifecycle.requireAssignable("triaged"));
        AlertException closedAssign = assertThrows(AlertException.class, () -> AlertLifecycle.requireAssignable("closed"));
        assertEquals(409, closedAssign.httpStatus());
        assertDoesNotThrow(() -> AlertLifecycle.requireCloseable("assigned"));
        AlertException closedClose = assertThrows(AlertException.class, () -> AlertLifecycle.requireCloseable("closed"));
        assertEquals(409, closedClose.httpStatus());
    }
}
