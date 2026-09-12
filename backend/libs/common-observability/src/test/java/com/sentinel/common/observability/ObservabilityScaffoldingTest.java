package com.sentinel.common.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ObservabilityScaffoldingTest {

    @Test
    void correlationMdcKeyIsStable() {
        assertEquals("correlationId", ObservabilityScaffolding.CORRELATION_MDC_KEY);
    }
}
