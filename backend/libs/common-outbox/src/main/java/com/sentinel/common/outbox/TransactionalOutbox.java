package com.sentinel.common.outbox;

import java.util.Map;
import java.util.UUID;

/** Application-facing outbox write. Must run inside the domain transaction. */
public interface TransactionalOutbox {

    UUID record(Map<String, Object> envelope);
}
