package com.sentinel.ops.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class InvestLifecycleTest {

    @Test
    void forwardTransitionsAreAllowed() {
        assertDoesNotThrow(() -> InvestLifecycle.requirePatchable("open", "in_progress"));
        assertDoesNotThrow(() -> InvestLifecycle.requirePatchable("in_progress", "pending_review"));
        assertDoesNotThrow(() -> InvestLifecycle.requirePatchable("open", "pending_review"));
        assertDoesNotThrow(() -> InvestLifecycle.requirePatchable("open", "open"));
    }

    @Test
    void reverseAndCloseViaPatchAreRejected() {
        assertThrows(InvestException.class, () -> InvestLifecycle.requirePatchable("pending_review", "open"));
        InvestException close = assertThrows(InvestException.class, () -> InvestLifecycle.requirePatchable("open", "closed"));
        assertEquals("INVEST_VALIDATION_001", close.code());
        InvestException already = assertThrows(InvestException.class, () -> InvestLifecycle.requirePatchable("closed", "open"));
        assertEquals(409, already.httpStatus());
    }

    @Test
    void closedCannotAssign() {
        assertEquals(409, assertThrows(InvestException.class, () -> InvestLifecycle.requireAssignable("closed")).httpStatus());
        assertDoesNotThrow(() -> InvestLifecycle.requireAssignable("open"));
    }
}
