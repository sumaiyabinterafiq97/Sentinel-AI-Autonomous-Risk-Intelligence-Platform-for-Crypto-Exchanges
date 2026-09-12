package com.sentinel.ops.application;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import com.sentinel.common.outbox.InMemoryDurableEventLog;

@Component
public class InvestUpstreamEventConsumer {

    public InvestUpstreamEventConsumer(
            InMemoryDurableEventLog durableLog, PlatformTransactionManager transactionManager, InvestService invest) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        durableLog.addListener((stream, envelope) -> {
            if (stream.endsWith(".dlq")) {
                return;
            }
            String type = String.valueOf(envelope.get("eventType"));
            if (!"AlertCreated".equals(type) && !"RiskCalculated".equals(type)) {
                return;
            }
            tx.executeWithoutResult(status -> invest.consumeUpstream(envelope));
        });
    }
}
