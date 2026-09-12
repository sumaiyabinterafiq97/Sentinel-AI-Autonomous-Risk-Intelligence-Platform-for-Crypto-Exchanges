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
import com.sentinel.ops.domain.InvestLifecycle;
import com.sentinel.ops.infrastructure.InvestStore;
import com.sentinel.ops.infrastructure.InvestStore.CaseRow;

@SpringBootTest
@Import(InvestOutboxIT.RollbackProbe.class)
class InvestOutboxIT extends OpsPostgresIT {

    @Autowired
    private RollbackProbe probe;

    @Autowired
    private InvestStore store;

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
    void caseAndOutboxCommitTogether() {
        UUID org = RiskContext.get().organizationId();
        UUID caseId = UUID.randomUUID();
        probe.commitCase(org, caseId);
        assertTrue(store.find(caseId, org).isPresent());
        assertEquals(1, outbox.countForOrganization(org));
    }

    @Test
    void rollbackRemovesCaseAndOutbox() {
        UUID org = RiskContext.get().organizationId();
        assertThrows(IllegalStateException.class, () -> probe.failAfterWrite(org));
        assertEquals(0, outbox.countForOrganization(org));
        assertEquals(0, store.countForOrg(org));
    }

    @Component
    static class RollbackProbe {
        private final InvestStore store;
        private final InvestEventPublisher events;

        RollbackProbe(InvestStore store, InvestEventPublisher events) {
            this.store = store;
            this.events = events;
        }

        @Transactional
        void commitCase(UUID org, UUID caseId) {
            Instant now = Instant.now();
            store.insertCase(new CaseRow(
                    caseId,
                    org,
                    InvestLifecycle.OPEN,
                    "rollback-ok",
                    null,
                    null,
                    null,
                    now,
                    null,
                    null,
                    null,
                    now,
                    now,
                    RiskContext.get().actorId()));
            events.caseCreated(caseId, org, InvestLifecycle.OPEN, "rollback-ok", null, null, now, RiskContext.get().actorId());
        }

        @Transactional
        void failAfterWrite(UUID org) {
            commitCase(org, UUID.randomUUID());
            throw new IllegalStateException("forced-rollback");
        }
    }
}
