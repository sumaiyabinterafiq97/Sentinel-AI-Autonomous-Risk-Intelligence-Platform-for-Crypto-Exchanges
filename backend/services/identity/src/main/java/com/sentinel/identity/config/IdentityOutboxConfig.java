package com.sentinel.identity.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.common.outbox.IdempotentConsumerSkeleton;
import com.sentinel.common.outbox.InMemoryDurableEventLog;
import com.sentinel.common.outbox.JdbcOutboxRepository;
import com.sentinel.common.outbox.JdbcTransactionalOutbox;
import com.sentinel.common.outbox.OutboxMetrics;
import com.sentinel.common.outbox.OutboxPoller;
import com.sentinel.common.outbox.OutboxProperties;
import com.sentinel.common.outbox.OutboxRelay;
import com.sentinel.common.outbox.TransactionalOutbox;
import com.sentinel.identity.infrastructure.IdentityEventBus;

@Configuration
@EnableScheduling
public class IdentityOutboxConfig {

    @Bean
    OutboxProperties outboxProperties(
            @Value("${sentinel.outbox.table}") String table,
            @Value("${sentinel.outbox.max-attempts}") int maxAttempts,
            @Value("${sentinel.outbox.backoff-base-ms}") long backoffBaseMs,
            @Value("${sentinel.outbox.claim-timeout-seconds}") long claimTimeoutSeconds,
            @Value("${sentinel.outbox.batch-size}") int batchSize,
            @Value("${sentinel.outbox.relay.poll-enabled}") boolean pollEnabled) {
        return new OutboxProperties(
                table,
                maxAttempts,
                Duration.ofMillis(backoffBaseMs),
                Duration.ofSeconds(claimTimeoutSeconds),
                batchSize,
                pollEnabled);
    }

    @Bean
    OutboxMetrics outboxMetrics() {
        return new OutboxMetrics();
    }

    @Bean
    JdbcOutboxRepository jdbcOutboxRepository(
            JdbcTemplate jdbc, ObjectMapper objectMapper, OutboxProperties properties) {
        return new JdbcOutboxRepository(jdbc, objectMapper, properties.table());
    }

    @Bean
    IdempotentConsumerSkeleton idempotentConsumerSkeleton() {
        return new IdempotentConsumerSkeleton();
    }

    @Bean
    InMemoryDurableEventLog inMemoryDurableEventLog(
            IdentityEventBus bus, IdempotentConsumerSkeleton consumers) {
        InMemoryDurableEventLog log = new InMemoryDurableEventLog();
        log.addListener((stream, envelope) -> {
            if (!stream.endsWith(".dlq")) {
                bus.recordDelivered(envelope);
                consumers.accept(envelope);
            }
        });
        return log;
    }

    @Bean
    OutboxRelay outboxRelay(
            JdbcOutboxRepository repository,
            InMemoryDurableEventLog durableEventLog,
            OutboxProperties properties,
            OutboxMetrics metrics,
            PlatformTransactionManager transactionManager) {
        return new OutboxRelay(repository, durableEventLog, properties, metrics, transactionManager);
    }

    @Bean
    TransactionalOutbox transactionalOutbox(
            JdbcOutboxRepository repository, OutboxRelay relay, OutboxMetrics metrics) {
        return new JdbcTransactionalOutbox(repository, relay, metrics);
    }

    @Bean
    OutboxPoller outboxPoller(OutboxRelay relay) {
        return new OutboxPoller(relay);
    }
}
