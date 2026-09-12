package com.sentinel.ops.infrastructure;

import java.sql.Timestamp;
import java.time.Instant;
import org.postgresql.util.PGobject;
import java.sql.SQLException;

public final class JdbcTypes {

    private JdbcTypes() {}

    public static Timestamp ts(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    public static Instant instant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }

    public static PGobject jsonb(String json) {
        try {
            PGobject obj = new PGobject();
            obj.setType("jsonb");
            obj.setValue(json == null ? "{}" : json);
            return obj;
        } catch (SQLException e) {
            throw new IllegalArgumentException("Invalid JSON", e);
        }
    }
}
