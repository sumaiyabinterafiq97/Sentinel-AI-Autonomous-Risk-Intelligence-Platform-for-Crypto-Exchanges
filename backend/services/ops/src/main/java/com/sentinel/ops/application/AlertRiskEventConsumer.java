package com.sentinel.ops.application;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import com.sentinel.common.outbox.InMemoryDurableEventLog;

/**
 * RISK → ALERT consumer over the M3 durable-log simulation. At-least-once; duplicates are
 * tolerated by eventId (context jsonb) and one-alert-per-assessment lookup.
 */
@Component
public class AlertRiskEventConsumer {

    public AlertRiskEventConsumer(
            InMemoryDurableEventLog durableLog, PlatformTransactionManager transactionManager, AlertService alerts) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        durableLog.addListener((stream, envelope) -> {
            if (stream.endsWith(".dlq")) {
                return;
            }
            String type = String.valueOf(envelope.get("eventType"));
            if (!"RiskCalculated".equals(type) && !"HighRiskDetected".equals(type)) {
                return;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> copy = envelope;
            tx.executeWithoutResult(status -> alerts.consumeRiskEvent(copy));
        });
    }
}
