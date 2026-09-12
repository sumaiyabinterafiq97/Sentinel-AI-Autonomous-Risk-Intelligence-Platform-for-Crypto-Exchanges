package com.sentinel.ops.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class RiskScoringTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void identicalInputsProduceIdenticalScores() throws Exception {
        ObjectNode def = mapper.createObjectNode();
        def.put("weight", 40);
        def.put("factor", "amount");
        def.put("operator", "gte");
        def.put("value", 1000);
        def.put("appliesTo", "transaction");
        RiskScoring.Rule rule = new RiskScoring.Rule(UUID.randomUUID(), "amount-high", true, def);
        RiskScoring.Context ctx = new RiskScoring.Context("transaction", "tx-1", new BigDecimal("5000"), "USDT", true);
        RiskScoring.Result a = RiskScoring.score(List.of(rule), ctx);
        RiskScoring.Result b = RiskScoring.score(List.of(rule), ctx);
        assertEquals(a.score(), b.score());
        assertEquals(a.riskLevel(), b.riskLevel());
        assertEquals("medium", a.riskLevel());
        assertEquals(1, a.hits().size());
    }

    @Test
    void highThresholdPublishesHighLevel() throws Exception {
        ObjectNode def = mapper.createObjectNode();
        def.put("weight", 80);
        def.put("factor", "entityType");
        def.put("operator", "eq");
        def.put("value", "device");
        RiskScoring.Rule rule = new RiskScoring.Rule(UUID.randomUUID(), "device", true, def);
        RiskScoring.Result result =
                RiskScoring.score(List.of(rule), new RiskScoring.Context("device", "dev-1", null, null, false));
        assertEquals("critical", result.riskLevel());
        assertTrue(RiskScoring.highRisk(result.riskLevel()));
        RiskScoring.Result miss =
                RiskScoring.score(List.of(rule), new RiskScoring.Context("user", "u-1", null, null, false));
        assertEquals("low", miss.riskLevel());
        assertFalse(RiskScoring.highRisk(miss.riskLevel()));
    }
}
