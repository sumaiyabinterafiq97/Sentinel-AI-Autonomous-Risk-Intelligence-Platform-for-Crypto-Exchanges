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
import com.sentinel.ops.infrastructure.CompStore;
import com.sentinel.ops.infrastructure.RiskEventBus;

@SpringBootTest
@AutoConfigureMockMvc
class CompApiIT extends OpsPostgresIT {

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
    private CompStore store;

    private UUID org;

    @BeforeEach
    void reset() {
        events.reset();
        durableLog.reset();
        org = UUID.randomUUID();
    }

    @Test
    void unauthenticatedForbiddenValidationAndTenantIsolation() throws Exception {
        UUID subject = UUID.randomUUID();
        mockMvc.perform(post("/v1/compliance/kyc-reviews")
                        .header("X-Organization-Id", org.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subjectRef\":\"" + subject + "\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(authorized(post("/v1/compliance/kyc-reviews"), org, "comp:kyc:approve")
                        .content("{\"subjectRef\":\"" + subject + "\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(authorized(post("/v1/compliance/kyc-reviews"), org, "comp:kyc:write").content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMP_VALIDATION_001"));
        MvcResult created = mockMvc.perform(authorized(post("/v1/compliance/kyc-reviews"), org, "comp:kyc:write")
                        .content("{\"subjectRef\":\"" + subject + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String reviewId = objectMapper.readTree(created.getResponse().getContentAsString()).at("/data/id").asText();
        UUID orgB = UUID.randomUUID();
        mockMvc.perform(authorized(patch("/v1/compliance/kyc-reviews/" + reviewId), orgB, "comp:kyc:approve")
                        .content("{\"decision\":\"approved\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void kycAmlTravelSanctionsAuditAndHumanDecisionEvents() throws Exception {
        UUID subject = UUID.randomUUID();
        MvcResult kyc = mockMvc.perform(authorized(post("/v1/compliance/kyc-reviews"), org, "comp:kyc:write")
                        .content("{\"subjectRef\":\"" + subject + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("in_review"))
                .andReturn();
        String reviewId = objectMapper.readTree(kyc.getResponse().getContentAsString()).at("/data/id").asText();
        assertFalse(events.snapshot().stream().anyMatch(e -> "ComplianceReviewed".equals(e.get("eventType"))));
        mockMvc.perform(authorized(patch("/v1/compliance/kyc-reviews/" + reviewId), org, "comp:kyc:approve")
                        .content("{\"decision\":\"approved\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("approved"));
        mockMvc.perform(authorized(post("/v1/compliance/aml-reviews"), org, "comp:aml:write")
                        .content("{\"subjectRef\":\"" + subject + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("in_review"));
        mockMvc.perform(authorized(post("/v1/compliance/travel-rule/validations"), org, "comp:travelrule:write")
                        .content("{\"transactionRef\":\"tx-1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.valid").value(true));
        mockMvc.perform(authorized(post("/v1/compliance/sanctions-screenings"), org, "comp:sanctions:write")
                        .content("{\"subjectRef\":\"" + subject + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("no_match"));
        MvcResult hit = mockMvc.perform(authorized(post("/v1/compliance/sanctions-screenings"), org, "comp:sanctions:write")
                        .content("{\"subjectRef\":\"hit:" + subject + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("possible_match"))
                .andReturn();
        String screeningId = objectMapper.readTree(hit.getResponse().getContentAsString()).at("/data/id").asText();
        mockMvc.perform(authorized(patch("/v1/compliance/sanctions-screenings/" + screeningId), org, "comp:sanctions:approve")
                        .content("{\"disposition\":\"false_positive\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(authorized(post("/v1/compliance/audit-packages"), org, "comp:audit:write")
                        .content("{\"scope\":\"quarterly\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("ready"));
        assertTrue(events.snapshot().stream().anyMatch(e -> "ComplianceReviewed".equals(e.get("eventType"))));
        assertTrue(events.snapshot().stream().anyMatch(e -> "TravelRuleValidated".equals(e.get("eventType"))));
        assertTrue(events.snapshot().stream().anyMatch(e -> "SanctionsHitDetected".equals(e.get("eventType"))));
        assertTrue(events.snapshot().stream().anyMatch(e -> "AuditPackagePrepared".equals(e.get("eventType"))));
        UUID reviewed = UUID.fromString(String.valueOf(events.snapshot().stream()
                .filter(e -> "ComplianceReviewed".equals(e.get("eventType")))
                .findFirst()
                .orElseThrow()
                .get("eventId")));
        assertEquals(OutboxStatus.PUBLISHED, outbox.find(reviewed).orElseThrow().status());
        assertFalse(store.aiSchemaExists());
        mockMvc.perform(get("/health")).andExpect(jsonPath("$.milestone").value("M7"));
        assertEquals("comp:kyc:write", CompAccessFilter.requiredPermission("POST", "/v1/compliance/kyc-reviews"));
        assertEquals("comp:kyc:approve", CompAccessFilter.requiredPermission("PATCH", "/v1/compliance/kyc-reviews/x"));
        assertEquals("comp:sanctions:approve", CompAccessFilter.requiredPermission("PATCH", "/v1/compliance/sanctions-screenings/x"));
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
