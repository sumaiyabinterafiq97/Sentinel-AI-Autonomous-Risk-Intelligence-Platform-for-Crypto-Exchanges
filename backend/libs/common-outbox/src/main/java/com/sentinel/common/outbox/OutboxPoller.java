package com.sentinel.common.outbox;

import org.springframework.scheduling.annotation.Scheduled;

public final class OutboxPoller {

    private final OutboxRelay relay;

    public OutboxPoller(OutboxRelay relay) {
        this.relay = relay;
    }

    @Scheduled(fixedDelayString = "${sentinel.outbox.relay.poll-interval-ms:1000}")
    public void poll() {
        relay.poll();
    }
}
