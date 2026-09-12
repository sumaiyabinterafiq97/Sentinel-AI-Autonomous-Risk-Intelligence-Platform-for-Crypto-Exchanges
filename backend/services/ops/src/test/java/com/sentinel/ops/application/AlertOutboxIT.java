package com.sentinel.ops.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.sentinel.common.outbox.JdbcOutboxRepository;
import com.sentinel.ops.OpsPostgresIT;
import com.sentinel.ops.api.RiskContext;
import com.sentinel.ops.domain.AlertLifecycle;
import com.sentinel.ops.infrastructure.AlertStore;
import com.sentinel.ops.infrastructure.AlertStore.AlertRow;

@SpringBootTest
@Import(AlertOutboxIT.RollbackProbe.class)
class AlertOutboxIT extends OpsPostgresIT {

    @Autowired
    private RollbackProbe probe;

    @Autowired
    private AlertStore store;

    @Autowired
    private JdbcOutboxRepository outbox;

    @BeforeEach
    void ctx() {
        RiskContext.clear();
        RiskContext context = RiskContext.get();
        context.setOrganizationId(UUID.randomUUID());
        context.setRequestId(UUID.randomUUID());
        context.setCorrelationId(UUID.randomUUID());
        context.setActorId(UUID.randomUUID());
        context.setActorType("user");
    }

    @Test
    void alertAndOutboxCommitTogether() {
        UUID org = RiskContext.get().organizationId();
        UUID alertId = UUID.randomUUID();
        probe.commitAlert(org, alertId);
        assertTrue(store.find(alertId, org).isPresent());
        assertEquals(2, outbox.countForOrganization(org));
    }

    @Test
    void rollbackRemovesAlertAndOutbox() {
        UUID org = RiskContext.get().organizationId();
        assertThrows(IllegalStateException.class, () -> probe.failAfterWrite(org));
        assertEquals(0, outbox.countForOrganization(org));
        assertEquals(0, store.countForOrg(org));
    }

    @Component
    static class RollbackProbe {
        private final AlertStore store;
        private final AlertEventPublisher events;

        RollbackProbe(AlertStore store, AlertEventPublisher events) {
            this.store = store;
            this.events = events;
        }

        @Transactional
        void commitAlert(UUID org, UUID alertId) {
            Instant now = Instant.now();
            store.insert(new AlertRow(
                    alertId,
                    org,
                    AlertLifecycle.OPEN,
                    70,
                    "test",
                    UUID.randomUUID(),
                    null,
                    now,
                    now,
                    null,
                    null,
                    null,
                    RiskContext.get().actorId()));
            events.alertCreated(alertId, org, AlertLifecycle.OPEN, 70, null, "test", now);
        }

        @Transactional
        void failAfterWrite(UUID org) {
            commitAlert(org, UUID.randomUUID());
            throw new IllegalStateException("forced-rollback");
        }
    }
}
