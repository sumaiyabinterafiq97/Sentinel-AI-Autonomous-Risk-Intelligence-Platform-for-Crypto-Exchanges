package com.sentinel.common.outbox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/**
 * Local simulation of a durable event log. Not a production broker.
 */
public final class InMemoryDurableEventLog implements DurableEventLog {

    public record Entry(String stream, Map<String, Object> envelope) {}

    private final CopyOnWriteArrayList<Entry> entries = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<BiConsumer<String, Map<String, Object>>> listeners = new CopyOnWriteArrayList<>();

    public void addListener(BiConsumer<String, Map<String, Object>> listener) {
        listeners.add(listener);
    }

    @Override
    public void append(String stream, Map<String, Object> envelope) {
        DeferredEvents.reject(envelope);
        entries.add(new Entry(stream, envelope));
        for (BiConsumer<String, Map<String, Object>> listener : listeners) {
            listener.accept(stream, envelope);
        }
    }

    public List<Entry> snapshot() {
        return new ArrayList<>(entries);
    }

    public void reset() {
        entries.clear();
    }
}
