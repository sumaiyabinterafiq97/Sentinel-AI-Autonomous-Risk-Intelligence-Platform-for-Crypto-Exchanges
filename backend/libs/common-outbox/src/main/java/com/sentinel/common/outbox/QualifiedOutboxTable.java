package com.sentinel.common.outbox;

import java.util.Set;

public final class QualifiedOutboxTable {

    static final Set<String> ALLOWED = Set.of("core.outbox_events", "auth.outbox_events", "risk.outbox_events");

    private QualifiedOutboxTable() {}

    public static String requireAllowed(String table) {
        if (table == null || !ALLOWED.contains(table)) {
            throw new IllegalArgumentException("Outbox table is not an authorized M3 location: " + table);
        }
        return table;
    }
}
