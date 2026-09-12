package com.sentinel.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.sentinel.common.outbox.IdempotentConsumerSkeleton;
import com.sentinel.common.outbox.InMemoryDurableEventLog;
import com.sentinel.common.outbox.JdbcOutboxRepository;
import com.sentinel.common.outbox.OutboxStatus;
import com.sentinel.identity.IdentityFixtures.SeededAdmin;
import com.sentinel.identity.api.IdentityContext;
import com.sentinel.identity.application.AuthService;
import com.sentinel.identity.application.AuthzService;
import com.sentinel.identity.infrastructure.IdentityEventBus;
import com.sentinel.identity.infrastructure.IdentityStore;

@SpringBootTest
class IdentityOutboxIT extends IdentityPostgresIT {

    @Autowired
    private AuthService auth;

    @Autowired
    private IdentityStore store;

    @Autowired
    private AuthzService authz;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JdbcOutboxRepository outbox;

    @Autowired
    private IdentityEventBus bus;

    @Autowired
    private InMemoryDurableEventLog durableLog;

    @Autowired
    private IdempotentConsumerSkeleton consumers;

    private SeededAdmin admin;

    @BeforeEach
    void seed() {
        bus.reset();
        durableLog.reset();
        consumers.reset();
        IdentityContext.clear();
        admin = IdentityFixtures.seedAdmin(store, authz, encoder);
    }

    @Test
    void loginWritesOutboxAndRelaysUserLoggedIn() {
        auth.login(admin.email(), admin.password());
        assertEquals(1, outbox.countForOrganization(admin.organizationId()));
        var delivered = durableLog.snapshot().stream()
                .filter(e -> "sentinel.auth.UserLoggedIn.v1".equals(e.stream()))
                .findFirst()
                .orElseThrow();
        assertEquals(admin.organizationId().toString(), delivered.envelope().get("organizationId"));
        UUID eventId = UUID.fromString(String.valueOf(delivered.envelope().get("eventId")));
        assertEquals(OutboxStatus.PUBLISHED, outbox.find(eventId).orElseThrow().status());
        assertTrue(consumers.seen(eventId));
        assertTrue(bus.snapshot().stream().anyMatch(e -> "UserLoggedIn".equals(e.get("eventType"))));
    }

    @Test
    void logoutRelaysSessionExpiredForSameTenant() {
        auth.login(admin.email(), admin.password());
        auth.logout();
        assertTrue(durableLog.snapshot().stream().anyMatch(e -> "sentinel.auth.SessionExpired.v1".equals(e.stream())));
        assertTrue(durableLog.snapshot().stream()
                .allMatch(e -> admin.organizationId().toString().equals(String.valueOf(e.envelope().get("organizationId")))
                        || e.stream().endsWith(".dlq")));
    }
}
