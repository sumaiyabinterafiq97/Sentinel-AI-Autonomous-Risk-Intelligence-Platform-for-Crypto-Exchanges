package com.sentinel.contracts.events;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class EventCatalogResourceTest {

    @Test
    void packagedEventCatalogIsOnClasspath() {
        assertNotNull(getClass().getClassLoader().getResource("event-catalog.v0.2.json"));
    }
}
