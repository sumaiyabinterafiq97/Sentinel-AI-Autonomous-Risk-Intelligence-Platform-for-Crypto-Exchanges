package com.sentinel.ops.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.common.outbox.InMemoryDurableEventLog;
import com.sentinel.common.outbox.JdbcOutboxRepository;
import com.sentinel.common.outbox.OutboxStatus;
import com.sentinel.common.security.AccessTokenCodec;
import com.sentinel.ops.OpsPostgresIT;
import com.sentinel.ops.infrastructure.AlertStore;
import com.sentinel.ops.infrastructure.InvestStore;
import com.sentinel.ops.infrastructure.RiskEventBus;

@SpringBootTest
@AutoConfigureMockMvc
class AlertApiIT extends OpsPostgresIT {

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
    private JdbcOutboxRepository outbox;

    @Autowired
    private AlertStore alerts;

    @Autowired
    private InvestStore invest;

    private UUID org;

    @BeforeEach
    void reset() {
        events.reset();
        durableLog.reset();
        org = UUID.randomUUID();
    }

    @Test
    void unauthenticatedForbiddenValidationAndTenantIsolation() throws Exception {
        mockMvc.perform(get("/v1/alerts").header("X-Organization-Id", org.toString()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_AUTHENTICATION_001"));
        mockMvc.perform(authorized(get("/v1/alerts"), org, "alert:alert:write"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTHZ_FORBIDDEN_001"));
        String alertId = createHighRiskAlert();
        UUID orgB = UUID.randomUUID();
        mockMvc.perform(authorized(get("/v1/alerts/" + alertId), orgB, "alert:alert:read"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("ALERT_NOT_FOUND_001"));
        mockMvc.perform(authorized(patch("/v1/alerts/" + alertId), orgB, "alert:alert:write")
                        .content("{\"status\":\"triaged\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(authorized(patch("/v1/alerts/" + UUID.randomUUID() + "/priority"), org, "alert:alert:priority")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ALERT_VALIDATION_001"));
    }

    @Test
    void listGetLifecycleAssignClosePriorityAndInvestigationLink() throws Exception {
        String alertId = createHighRiskAlert();
        mockMvc.perform(authorized(get("/v1/alerts"), org, "alert:alert:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(alertId))
                .andExpect(jsonPath("$.data[0].status").value("open"))
                .andExpect(jsonPath("$.data[0].priority").value(90));
        mockMvc.perform(authorized(get("/v1/alerts/" + alertId), org, "alert:alert:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").isNotEmpty());
        mockMvc.perform(authorized(patch("/v1/alerts/" + alertId), org, "alert:alert:write")
                        .content("{\"status\":\"triaged\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("triaged"));
        mockMvc.perform(authorized(patch("/v1/alerts/" + alertId), org, "alert:alert:write")
                        .content("{\"status\":\"assigned\"}"))
                .andExpect(status().isBadRequest());
        UUID assignee = UUID.randomUUID();
        mockMvc.perform(authorized(post("/v1/alerts/" + alertId + "/assign"), org, "alert:alert:assign")
                        .content("{\"assigneeId\":\"" + assignee + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("assigned"));
        mockMvc.perform(authorized(post("/v1/alerts/" + alertId + "/assign"), org, "alert:alert:assign")
                        .content("{\"assigneeId\":\"" + assignee + "\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(authorized(patch("/v1/alerts/" + alertId + "/priority"), org, "alert:alert:priority")
                        .content("{\"priority\":15}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.priority").value(15));
        UUID caseId = UUID.randomUUID();
        mockMvc.perform(authorized(post("/v1/alerts/" + alertId + "/investigation-link"), org, "alert:alert:write")
                        .content("{\"caseId\":\"" + caseId + "\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(authorized(post("/v1/alerts/" + alertId + "/investigation-link"), org, "alert:alert:write")
                        .content("{\"caseId\":\"" + caseId + "\"}"))
                .andExpect(status().isOk());
        assertTrue(alerts.investigationSchemaExists());
        assertTrue(invest.find(caseId, org).isEmpty());
        mockMvc.perform(authorized(post("/v1/alerts/" + alertId + "/close"), org, "alert:alert:close")
                        .content("{\"dispositionReason\":\"false_positive\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("closed"));
        mockMvc.perform(authorized(post("/v1/alerts/" + alertId + "/close"), org, "alert:alert:close")
                        .content("{\"dispositionReason\":\"false_positive\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(authorized(post("/v1/alerts/" + alertId + "/assign"), org, "alert:alert:assign")
                        .content("{\"assigneeId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("ALERT_CONFLICT_001"));
        mockMvc.perform(authorized(patch("/v1/alerts/" + alertId), org, "alert:alert:write")
                        .content("{\"status\":\"triaged\"}"))
                .andExpect(status().isConflict());
        assertTrue(events.snapshot().stream().anyMatch(e -> "AlertCreated".equals(e.get("eventType"))));
        assertTrue(events.snapshot().stream().anyMatch(e -> "AlertAssigned".equals(e.get("eventType"))));
        assertTrue(events.snapshot().stream().anyMatch(e -> "AlertClosed".equals(e.get("eventType"))));
        long created = events.snapshot().stream().filter(e -> "AlertCreated".equals(e.get("eventType"))).count();
        assertEquals(1, created);
        UUID createdEventId = UUID.fromString(String.valueOf(events.snapshot().stream()
                .filter(e -> "AlertCreated".equals(e.get("eventType")))
                .findFirst()
                .orElseThrow()
                .get("eventId")));
        assertEquals(OutboxStatus.PUBLISHED, outbox.find(createdEventId).orElseThrow().status());
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) events.snapshot().stream()
                .filter(e -> "AlertCreated".equals(e.get("eventType")))
                .findFirst()
                .orElseThrow()
                .get("payload");
        assertEquals("open", payload.get("status"));
        assertEquals(org.toString(), events.snapshot().stream()
                .filter(e -> "AlertCreated".equals(e.get("eventType")))
                .findFirst()
                .orElseThrow()
                .get("organizationId"));
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.milestone").value("M7"));
    }

    @Test
    void lowRiskDoesNotCreateAlert() throws Exception {
        mockMvc.perform(authorized(post("/v1/risk/rules"), org, "risk:rule:write")
                        .content("{\"name\":\"tiny\",\"definition\":{\"weight\":5,\"factor\":\"amount\",\"operator\":\"gte\",\"value\":999999,\"appliesTo\":\"transaction\"}}"))
                .andExpect(status().isCreated());
        mockMvc.perform(authorized(post("/v1/risk/transactions/ingest"), org, "risk:ingest:write")
                        .content("{\"externalTransactionId\":\"tx-low\",\"amount\":\"1\",\"asset\":\"USDT\",\"timestamp\":\""
                                + Instant.now()
                                + "\"}"))
                .andExpect(status().isAccepted());
        mockMvc.perform(authorized(get("/v1/alerts"), org, "alert:alert:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
        assertTrue(events.snapshot().stream().anyMatch(e -> "RiskCalculated".equals(e.get("eventType"))));
        assertFalse(events.snapshot().stream().anyMatch(e -> "AlertCreated".equals(e.get("eventType"))));
    }

    @Test
    void permissionsAreDistinct() {
        assertEquals("alert:alert:read", AlertAccessFilter.requiredPermission("GET", "/v1/alerts"));
        assertEquals("alert:alert:write", AlertAccessFilter.requiredPermission("PATCH", "/v1/alerts/x"));
        assertEquals("alert:alert:assign", AlertAccessFilter.requiredPermission("POST", "/v1/alerts/x/assign"));
        assertEquals("alert:alert:close", AlertAccessFilter.requiredPermission("POST", "/v1/alerts/x/close"));
        assertEquals("alert:alert:priority", AlertAccessFilter.requiredPermission("PATCH", "/v1/alerts/x/priority"));
        assertEquals("alert:alert:write", AlertAccessFilter.requiredPermission("POST", "/v1/alerts/x/investigation-link"));
    }

    private String createHighRiskAlert() throws Exception {
        mockMvc.perform(authorized(post("/v1/risk/rules"), org, "risk:rule:write")
                        .content("{\"name\":\"large-tx\",\"definition\":{\"weight\":80,\"factor\":\"amount\",\"operator\":\"gte\",\"value\":1000,\"appliesTo\":\"transaction\"}}"))
                .andExpect(status().isCreated());
        MvcResult ingest = mockMvc.perform(authorized(post("/v1/risk/transactions/ingest"), org, "risk:ingest:write")
                        .content("{\"externalTransactionId\":\"tx-alert\",\"amount\":\"5000\",\"asset\":\"USDT\",\"timestamp\":\""
                                + Instant.now()
                                + "\"}"))
                .andExpect(status().isAccepted())
                .andReturn();
        objectMapper.readTree(ingest.getResponse().getContentAsString());
        MvcResult listed = mockMvc.perform(authorized(get("/v1/alerts"), org, "alert:alert:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andReturn();
        return objectMapper.readTree(listed.getResponse().getContentAsString()).at("/data/0/id").asText();
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
