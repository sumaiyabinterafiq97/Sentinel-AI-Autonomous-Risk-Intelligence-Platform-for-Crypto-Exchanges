package com.sentinel.ops.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.common.outbox.IdempotentConsumerSkeleton;
import com.sentinel.common.outbox.InMemoryDurableEventLog;
import com.sentinel.common.outbox.JdbcOutboxRepository;
import com.sentinel.common.outbox.OutboxStatus;
import com.sentinel.common.security.AccessTokenCodec;
import com.sentinel.ops.OpsPostgresIT;
import com.sentinel.ops.infrastructure.RiskEventBus;
import com.sentinel.ops.infrastructure.RiskStore;

@SpringBootTest
@AutoConfigureMockMvc
class RiskApiIT extends OpsPostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccessTokenCodec tokens;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RiskEventBus events;

    @Autowired
    private InMemoryDurableEventLog durableLog;

    @Autowired
    private IdempotentConsumerSkeleton consumers;

    @Autowired
    private JdbcOutboxRepository outbox;

    @Autowired
    private RiskStore store;

    private UUID org;

    @BeforeEach
    void reset() {
        events.reset();
        durableLog.reset();
        consumers.reset();
        org = UUID.randomUUID();
    }

    @Test
    void unauthenticatedAndForbiddenAndValidation() throws Exception {
        mockMvc.perform(post("/v1/risk/rules")
                        .header("X-Organization-Id", org.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"definition\":{\"weight\":10}}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_AUTHENTICATION_001"));
        mockMvc.perform(authorized(post("/v1/risk/rules"), org, "risk:rule:read")
                        .content("{\"name\":\"x\",\"definition\":{\"weight\":10}}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTHZ_FORBIDDEN_001"));
        mockMvc.perform(authorized(post("/v1/risk/rules"), org, "risk:rule:write").content("{\"name\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("RISK_VALIDATION_001"));
    }

    @Test
    void rulesLifecycleIsTenantIsolated() throws Exception {
        UUID orgB = UUID.randomUUID();
        MvcResult created = mockMvc.perform(authorized(post("/v1/risk/rules"), org, "risk:rule:write")
                        .content("{\"name\":\"amount-gte\",\"enabled\":true,\"definition\":{\"weight\":60,\"factor\":\"amount\",\"operator\":\"gte\",\"value\":10000,\"appliesTo\":\"transaction\"}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("amount-gte"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andReturn();
        String ruleId = objectMapper.readTree(created.getResponse().getContentAsString()).at("/data/id").asText();
        mockMvc.perform(authorized(get("/v1/risk/rules"), orgB, "risk:rule:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
        mockMvc.perform(authorized(patch("/v1/risk/rules/" + ruleId), orgB, "risk:rule:write")
                        .content("{\"enabled\":false}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(authorized(patch("/v1/risk/rules/" + ruleId), org, "risk:rule:write")
                        .content("{\"enabled\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false));
    }

    @Test
    void ingestScoresPublishesEventsAndIsIdempotent() throws Exception {
        mockMvc.perform(authorized(post("/v1/risk/rules"), org, "risk:rule:write")
                        .content("{\"name\":\"large-tx\",\"definition\":{\"weight\":80,\"factor\":\"amount\",\"operator\":\"gte\",\"value\":1000,\"appliesTo\":\"transaction\"}}"))
                .andExpect(status().isCreated());
        String body = "{\"externalTransactionId\":\"tx-100\",\"amount\":\"5000\",\"asset\":\"USDT\",\"timestamp\":\""
                + Instant.now()
                + "\"}";
        MvcResult first = mockMvc.perform(authorized(post("/v1/risk/transactions/ingest"), org, "risk:ingest:write")
                        .content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").value("accepted"))
                .andReturn();
        String assessmentId = objectMapper.readTree(first.getResponse().getContentAsString()).at("/data/assessmentId").asText();
        mockMvc.perform(authorized(post("/v1/risk/transactions/ingest"), org, "risk:ingest:write").content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").value("duplicate"))
                .andExpect(jsonPath("$.data.assessmentId").value(assessmentId));
        mockMvc.perform(authorized(get("/v1/risk/assessments/" + assessmentId), org, "risk:assessment:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.riskLevel").value("critical"))
                .andExpect(jsonPath("$.data.explanationSummary").isNotEmpty());
        UUID orgB = UUID.randomUUID();
        mockMvc.perform(authorized(get("/v1/risk/assessments/" + assessmentId), orgB, "risk:assessment:read"))
                .andExpect(status().isNotFound());
        assertTrue(events.snapshot().stream().anyMatch(e -> "RiskCalculated".equals(e.get("eventType"))));
        assertTrue(events.snapshot().stream().anyMatch(e -> "HighRiskDetected".equals(e.get("eventType"))));
        assertTrue(events.snapshot().stream().anyMatch(e -> "AlertCreated".equals(e.get("eventType"))));
        assertTrue(store.alertSchemaExists());
        assertEquals(1, events.snapshot().stream().filter(e -> "AlertCreated".equals(e.get("eventType"))).count());
        UUID eventId = UUID.fromString(String.valueOf(events.snapshot().stream()
                .filter(e -> "RiskCalculated".equals(e.get("eventType")))
                .findFirst()
                .orElseThrow()
                .get("eventId")));
        assertEquals(OutboxStatus.PUBLISHED, outbox.find(eventId).orElseThrow().status());
        assertEquals(eventId, UUID.fromString(String.valueOf(durableLog.snapshot().stream()
                .filter(e -> e.stream().equals("sentinel.risk.RiskCalculated.v1"))
                .findFirst()
                .orElseThrow()
                .envelope()
                .get("eventId"))));
        assertTrue(consumers.seen(eventId));
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) events.snapshot().stream()
                .filter(e -> "RiskCalculated".equals(e.get("eventType")))
                .findFirst()
                .orElseThrow()
                .get("payload");
        assertTrue(payload.containsKey("explanationSummary"));
        assertEquals(org.toString(), events.snapshot().getFirst().get("organizationId"));
    }

    @Test
    void evaluateDevicePublishesHighRiskAndAlertConsumerCreatesAlert() throws Exception {
        mockMvc.perform(authorized(post("/v1/risk/rules"), org, "risk:rule:write")
                        .content("{\"name\":\"device-match\",\"definition\":{\"weight\":55,\"factor\":\"entityType\",\"operator\":\"eq\",\"value\":\"device\",\"appliesTo\":\"device\"}}"))
                .andExpect(status().isCreated());
        mockMvc.perform(authorized(post("/v1/risk/evaluate"), org, "risk:evaluate:write")
                        .content("{\"entityType\":\"device\",\"entityId\":\"dev-9\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").value("accepted"))
                .andExpect(jsonPath("$.data.jobId").isNotEmpty());
        mockMvc.perform(authorized(get("/v1/risk/assessments"), org, "risk:assessment:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].entityType").value("device"))
                .andExpect(jsonPath("$.data[0].riskLevel").value("high"));
        assertTrue(events.snapshot().stream().anyMatch(e -> "HighRiskDetected".equals(e.get("eventType"))));
        assertTrue(store.alertSchemaExists());
        assertTrue(events.snapshot().stream().anyMatch(e -> "AlertCreated".equals(e.get("eventType"))));
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("sentinel-ops"));
    }

    @Test
    void contractSurfaceIsRiskOnly() {
        assertEquals("risk:ingest:write", RiskAccessFilter.requiredPermission("POST", "/v1/risk/transactions/ingest"));
        assertEquals("risk:assessment:read", RiskAccessFilter.requiredPermission("GET", "/v1/risk/assessments"));
        assertEquals("risk:rule:write", RiskAccessFilter.requiredPermission("POST", "/v1/risk/rules"));
        assertEquals("risk:evaluate:write", RiskAccessFilter.requiredPermission("POST", "/v1/risk/evaluate"));
    }

    private MockHttpServletRequestBuilder authorized(
            MockHttpServletRequestBuilder request, UUID organization, String... permissions) {
        String token = tokens.issue(
                UUID.randomUUID(), organization, UUID.randomUUID(), Set.of(permissions), Instant.now().plusSeconds(120));
        return request.header("Authorization", "Bearer " + token)
                .header("X-Organization-Id", organization.toString())
                .header("X-Correlation-Id", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }
}
