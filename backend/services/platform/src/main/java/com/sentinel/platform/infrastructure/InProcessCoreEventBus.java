package com.sentinel.platform.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;
import com.sentinel.common.outbox.DeferredEvents;
import com.sentinel.platform.domain.PublishedCoreEvent;

/**
 * Test/in-process view of events after durable-log delivery.
 * Publication itself is transactional outbox + relay (M3 / ADR-015).
 */
@Component
public class InProcessCoreEventBus {

    private final CopyOnWriteArrayList<PublishedCoreEvent> published = new CopyOnWriteArrayList<>();

    public void recordDelivered(Map<String, Object> envelope) {
        DeferredEvents.reject(envelope);
        published.add(new PublishedCoreEvent(envelope, "SUCCESS"));
    }

    public List<PublishedCoreEvent> snapshot() {
        return new ArrayList<>(published);
    }

    public void reset() {
        published.clear();
    }
}
