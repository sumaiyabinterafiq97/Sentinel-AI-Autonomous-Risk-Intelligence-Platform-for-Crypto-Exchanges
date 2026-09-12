package com.sentinel.ops.application;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import com.sentinel.common.outbox.InMemoryDurableEventLog;

@Component
public class CompUpstreamEventConsumer {

    public CompUpstreamEventConsumer(
            InMemoryDurableEventLog durableLog, PlatformTransactionManager transactionManager, CompService comp) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        durableLog.addListener((stream, envelope) -> {
            if (stream.endsWith(".dlq")) {
                return;
            }
            String type = String.valueOf(envelope.get("eventType"));
            if (!"CaseClosed".equals(type)
                    && !"CaseUpdated".equals(type)
                    && !"RiskCalculated".equals(type)
                    && !"UserUpdated".equals(type)) {
                return;
            }
            tx.executeWithoutResult(status -> comp.consumeUpstream(envelope));
        });
    }
}
