package com.sentinel.ops.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
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
import com.sentinel.ops.domain.RiskScoring;
import com.sentinel.ops.infrastructure.RiskStore;

@SpringBootTest
@Import(RiskOutboxIT.RollbackProbe.class)
class RiskOutboxIT extends OpsPostgresIT {

    @Autowired
    private RollbackProbe probe;

    @Autowired
    private RiskStore store;

    @Autowired
    private JdbcOutboxRepository outbox;

    @Autowired
    private RiskService risk;

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
    void assessmentAndOutboxCommitTogether() {
        UUID org = RiskContext.get().organizationId();
        UUID id = risk.evaluateAndPersist(
                org, "user", "u-1", new RiskScoring.Context("user", "u-1", null, null, false), null);
        assertTrue(store.findAssessment(id, org).isPresent());
        assertEquals(1, outbox.countForOrganization(org));
    }

    @Test
    void rollbackRemovesAssessmentAndOutbox() {
        UUID org = RiskContext.get().organizationId();
        assertThrows(IllegalStateException.class, () -> probe.failAfterWrite(org));
        assertEquals(0, outbox.countForOrganization(org));
        assertTrue(store.listAssessments(org, null, 10).isEmpty());
    }

    @Component
    static class RollbackProbe {
        private final RiskStore store;
        private final RiskEventPublisher events;

        RollbackProbe(RiskStore store, RiskEventPublisher events) {
            this.store = store;
            this.events = events;
        }

        @Transactional
        void failAfterWrite(UUID org) {
            Instant now = Instant.now();
            UUID assessmentId = UUID.randomUUID();
            store.insertAssessment(
                    new RiskStore.AssessmentRow(
                            assessmentId,
                            org,
                            "transaction",
                            "tx-roll",
                            BigDecimal.ZERO,
                            "low",
                            "rollback-probe",
                            now,
                            RiskContext.get().correlationId()),
                    now);
            events.riskCalculated(
                    assessmentId,
                    org,
                    "transaction",
                    "tx-roll",
                    BigDecimal.ZERO,
                    "low",
                    "rollback-probe",
                    now,
                    "tx-roll",
                    java.util.List.of());
            throw new IllegalStateException("forced-rollback");
        }
    }
}
