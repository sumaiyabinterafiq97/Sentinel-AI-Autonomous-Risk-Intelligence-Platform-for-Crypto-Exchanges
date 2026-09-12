package com.sentinel.common.outbox;

public final class OutboxStatus {

    public static final String PENDING = "PENDING";
    public static final String PUBLISHING = "PUBLISHING";
    public static final String PUBLISHED = "PUBLISHED";
    public static final String DEAD = "DEAD";

    private OutboxStatus() {}
}
