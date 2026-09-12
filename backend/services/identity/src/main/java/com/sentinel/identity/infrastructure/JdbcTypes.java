package com.sentinel.identity.infrastructure;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import org.postgresql.util.PGobject;

public final class JdbcTypes {
    private JdbcTypes() {}

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

    public static Timestamp ts(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }
}
