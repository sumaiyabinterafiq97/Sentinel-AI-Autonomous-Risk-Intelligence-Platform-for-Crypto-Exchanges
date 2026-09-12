package com.sentinel.ops.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AlertPriorityTest {

    @Test
    void mappingDoesNotCopyPrioritySignal() {
        assertEquals(90, AlertPriority.fromRiskLevel("critical"));
        assertEquals(70, AlertPriority.fromRiskLevel("high"));
        assertEquals(40, AlertPriority.fromRiskLevel("medium"));
        assertEquals(20, AlertPriority.fromRiskLevel("low"));
        assertEquals(70, AlertPriority.fromRiskLevel(null));
    }

    @Test
    void generationUsesHighRiskDetectedOrHighCriticalCalculated() {
        assertTrue(AlertPriority.generationRequired("HighRiskDetected", "low"));
        assertTrue(AlertPriority.generationRequired("RiskCalculated", "high"));
        assertTrue(AlertPriority.generationRequired("RiskCalculated", "critical"));
        assertFalse(AlertPriority.generationRequired("RiskCalculated", "low"));
        assertFalse(AlertPriority.generationRequired("RiskCalculated", "medium"));
        assertFalse(AlertPriority.generationRequired("AlertCreated", "high"));
    }
}
