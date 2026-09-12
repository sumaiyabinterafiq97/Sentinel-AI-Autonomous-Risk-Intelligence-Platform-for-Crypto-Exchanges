package com.sentinel.dash.infrastructure;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DashSseHub {

    public static final Set<String> CHANNELS = Set.of("workspace", "alerts", "cases", "queues");
    private static final int BUFFER = 50;

    public record RefreshEvent(String id, String channel, String reason, Instant at) {}

    private final ObjectMapper json;
    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<RefreshEvent>> buffers = new ConcurrentHashMap<>();

    public DashSseHub(ObjectMapper json) {
        this.json = json;
    }

    public SseEmitter subscribe(UUID organizationId, String channel, String lastEventId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(organizationId, id -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(organizationId, emitter));
        emitter.onTimeout(() -> remove(organizationId, emitter));
        emitter.onError(ex -> remove(organizationId, emitter));
        replay(organizationId, channel, lastEventId, emitter);
        return emitter;
    }

    public void publish(UUID organizationId, String channel, String reason) {
        RefreshEvent event = new RefreshEvent(UUID.randomUUID().toString(), channel, reason, Instant.now());
        buffers.computeIfAbsent(organizationId, id -> new ArrayDeque<>()).addLast(event);
        Deque<RefreshEvent> buf = buffers.get(organizationId);
        while (buf.size() > BUFFER) {
            buf.removeFirst();
        }
        List<SseEmitter> live = emitters.getOrDefault(organizationId, List.of());
        for (SseEmitter emitter : live) {
            send(emitter, event);
        }
    }

    private void replay(UUID organizationId, String channel, String lastEventId, SseEmitter emitter) {
        boolean skip = lastEventId != null && !lastEventId.isBlank();
        for (RefreshEvent event : buffers.getOrDefault(organizationId, new ArrayDeque<>())) {
            if (skip) {
                if (event.id().equals(lastEventId)) {
                    skip = false;
                }
                continue;
            }
            if ("workspace".equals(channel) || event.channel().equals(channel) || "queues".equals(channel)) {
                send(emitter, event);
            }
        }
    }

    private void send(SseEmitter emitter, RefreshEvent event) {
        try {
            Map<String, String> data = Map.of(
                    "channel", event.channel(),
                    "reason", event.reason(),
                    "at", event.at().toString());
            emitter.send(SseEmitter.event()
                    .id(event.id())
                    .name("refresh")
                    .data(json.writeValueAsString(data), MediaType.APPLICATION_JSON));
        } catch (IOException ex) {
            emitter.complete();
        }
    }

    private void remove(UUID organizationId, SseEmitter emitter) {
        List<SseEmitter> live = emitters.get(organizationId);
        if (live != null) {
            live.remove(emitter);
        }
    }
}
