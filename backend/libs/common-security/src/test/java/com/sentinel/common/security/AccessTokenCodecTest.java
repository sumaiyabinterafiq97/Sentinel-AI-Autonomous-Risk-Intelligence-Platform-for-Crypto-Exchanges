package com.sentinel.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AccessTokenCodecTest {

    @Test
    void roundTripAndRejectsTamper() {
        AccessTokenCodec codec = new AccessTokenCodec("unit-test-hmac-key");
        UUID user = UUID.randomUUID();
        UUID org = UUID.randomUUID();
        UUID session = UUID.randomUUID();
        String token = codec.issue(user, org, session, Set.of("user:user:read", "org:org:write"), Instant.now().plusSeconds(60));
        AccessPrincipal principal = codec.verify(token).orElseThrow();
        assertEquals(user, principal.userId());
        assertEquals(org, principal.organizationId());
        assertEquals(session, principal.sessionId());
        assertTrue(principal.hasPermission("org:org:write"));
        assertTrue(codec.verify(token + "x").isEmpty());
        assertTrue(codec.verify("not-a-token").isEmpty());
    }
}
