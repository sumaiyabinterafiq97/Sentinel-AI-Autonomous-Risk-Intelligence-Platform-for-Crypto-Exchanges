package com.sentinel.ops.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;
import com.sentinel.common.outbox.DeferredEvents;

@Component
public class RiskEventBus {

    private final CopyOnWriteArrayList<Map<String, Object>> published = new CopyOnWriteArrayList<>();

    public void recordDelivered(Map<String, Object> envelope) {
        DeferredEvents.reject(envelope);
        published.add(envelope);
    }

    public List<Map<String, Object>> snapshot() {
        return new ArrayList<>(published);
    }

    public void reset() {
        published.clear();
    }
}
