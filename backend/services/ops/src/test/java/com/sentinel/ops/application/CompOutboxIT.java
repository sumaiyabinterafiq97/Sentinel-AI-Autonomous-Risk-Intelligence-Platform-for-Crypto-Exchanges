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
import com.sentinel.ops.infrastructure.CompStore;
import com.sentinel.ops.infrastructure.CompStore.KycRow;

@SpringBootTest
@Import(CompOutboxIT.RollbackProbe.class)
class CompOutboxIT extends OpsPostgresIT {

    @Autowired
    private RollbackProbe probe;

    @Autowired
    private CompStore store;

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
    void kycDecisionAndOutboxCommitTogether() {
        UUID org = RiskContext.get().organizationId();
        UUID reviewId = UUID.randomUUID();
        probe.commitDecision(org, reviewId);
        assertTrue(store.findKyc(reviewId, org).isPresent());
        assertEquals(1, outbox.countForOrganization(org));
    }

    @Test
    void rollbackRemovesKycAndOutbox() {
        UUID org = RiskContext.get().organizationId();
        assertThrows(IllegalStateException.class, () -> probe.failAfterWrite(org));
        assertEquals(0, outbox.countForOrganization(org));
        assertEquals(0, store.countKyc(org));
    }

    @Component
    static class RollbackProbe {
        private final CompStore store;
        private final CompEventPublisher events;

        RollbackProbe(CompStore store, CompEventPublisher events) {
            this.store = store;
            this.events = events;
        }

        @Transactional
        void commitDecision(UUID org, UUID reviewId) {
            Instant now = Instant.now();
            UUID userId = UUID.randomUUID();
            store.insertKyc(
                    new KycRow(reviewId, org, userId, "approved", "approved", RiskContext.get().actorId(), now, now),
                    RiskContext.get().correlationId(),
                    now);
            events.complianceReviewed(
                    reviewId, org, "kyc", "approved", RiskContext.get().actorId(), now, userId.toString());
        }

        @Transactional
        void failAfterWrite(UUID org) {
            commitDecision(org, UUID.randomUUID());
            throw new IllegalStateException("forced-rollback");
        }
    }
}
