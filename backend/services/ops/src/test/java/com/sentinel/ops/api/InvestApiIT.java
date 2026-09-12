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
import com.sentinel.ops.infrastructure.InvestStore;
import com.sentinel.ops.infrastructure.RiskEventBus;

@SpringBootTest
@AutoConfigureMockMvc
class InvestApiIT extends OpsPostgresIT {

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
    private InvestStore store;

    @Autowired
    private CompStore comp;

    private UUID org;

    @BeforeEach
    void reset() {
        events.reset();
        durableLog.reset();
        org = UUID.randomUUID();
    }

    @Test
    void unauthenticatedForbiddenValidationAndTenantIsolation() throws Exception {
        mockMvc.perform(get("/v1/investigations/cases").header("X-Organization-Id", org.toString()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_AUTHENTICATION_001"));
        mockMvc.perform(authorized(get("/v1/investigations/cases"), org, "invest:case:write"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTHZ_FORBIDDEN_001"));
        mockMvc.perform(authorized(post("/v1/investigations/cases"), org, "invest:case:write").content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVEST_VALIDATION_001"));
        String caseId = createCase("manual-case");
        UUID orgB = UUID.randomUUID();
        mockMvc.perform(authorized(get("/v1/investigations/cases/" + caseId), orgB, "invest:case:read"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("INVEST_NOT_FOUND_001"));
    }

    @Test
    void caseLifecycleEvidenceNotesTimelineAndEvents() throws Exception {
        String caseId = createCase("kyc-review");
        mockMvc.perform(authorized(patch("/v1/investigations/cases/" + caseId), org, "invest:case:write")
                        .content("{\"status\":\"in_progress\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("in_progress"));
        mockMvc.perform(authorized(patch("/v1/investigations/cases/" + caseId), org, "invest:case:write")
                        .content("{\"status\":\"open\"}"))
                .andExpect(status().isBadRequest());
        UUID assignee = UUID.randomUUID();
        mockMvc.perform(authorized(post("/v1/investigations/cases/" + caseId + "/assign"), org, "invest:case:assign")
                        .content("{\"assigneeId\":\"" + assignee + "\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(authorized(post("/v1/investigations/cases/" + caseId + "/evidence"), org, "invest:evidence:write")
                        .content("{\"evidenceRef\":\"tx:abc\",\"description\":\"ledger\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.evidenceRef").value("tx:abc"));
        mockMvc.perform(authorized(post("/v1/investigations/cases/" + caseId + "/evidence"), org, "invest:evidence:write")
                        .content("{\"evidenceRef\":\"tx:abc\"}"))
                .andExpect(status().isConflict());
        mockMvc.perform(authorized(post("/v1/investigations/cases/" + caseId + "/notes"), org, "invest:case:write")
                        .content("{\"content\":\"analyst note\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.content").value("analyst note"));
        mockMvc.perform(authorized(get("/v1/investigations/cases/" + caseId + "/notes"), org, "invest:case:write"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
        mockMvc.perform(authorized(get("/v1/investigations/cases/" + caseId + "/timeline"), org, "invest:case:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(5));
        mockMvc.perform(authorized(post("/v1/investigations/cases/" + caseId + "/close"), org, "invest:case:close")
                        .content("{\"resolutionSummary\":\"cleared\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("closed"));
        mockMvc.perform(authorized(post("/v1/investigations/cases/" + caseId + "/assign"), org, "invest:case:assign")
                        .content("{\"assigneeId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isConflict());
        assertTrue(events.snapshot().stream().anyMatch(e -> "CaseCreated".equals(e.get("eventType"))));
        assertTrue(events.snapshot().stream().anyMatch(e -> "CaseUpdated".equals(e.get("eventType"))));
        assertTrue(events.snapshot().stream().anyMatch(e -> "CaseAssigned".equals(e.get("eventType"))));
        assertTrue(events.snapshot().stream().anyMatch(e -> "EvidenceAttached".equals(e.get("eventType"))));
        assertTrue(events.snapshot().stream().anyMatch(e -> "CaseClosed".equals(e.get("eventType"))));
        UUID createdId = UUID.fromString(String.valueOf(events.snapshot().stream()
                .filter(e -> "CaseCreated".equals(e.get("eventType")))
                .findFirst()
                .orElseThrow()
                .get("eventId")));
        assertEquals(OutboxStatus.PUBLISHED, outbox.find(createdId).orElseThrow().status());
        assertTrue(store.complianceSchemaExists());
        assertEquals(0, comp.countKyc(org));
        mockMvc.perform(get("/health")).andExpect(jsonPath("$.milestone").value("M7"));
    }

    @Test
    void alertCreatedOpensCaseAndDuplicateSourceConflicts() throws Exception {
        mockMvc.perform(authorized(post("/v1/risk/rules"), org, "risk:rule:write")
                        .content("{\"name\":\"large-tx\",\"definition\":{\"weight\":80,\"factor\":\"amount\",\"operator\":\"gte\",\"value\":1000,\"appliesTo\":\"transaction\"}}"))
                .andExpect(status().isCreated());
        mockMvc.perform(authorized(post("/v1/risk/transactions/ingest"), org, "risk:ingest:write")
                        .content("{\"externalTransactionId\":\"tx-inv\",\"amount\":\"5000\",\"asset\":\"USDT\",\"timestamp\":\""
                                + Instant.now()
                                + "\"}"))
                .andExpect(status().isAccepted());
        MvcResult listed = mockMvc.perform(authorized(get("/v1/investigations/cases"), org, "invest:case:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andReturn();
        String sourceAlertId = objectMapper.readTree(listed.getResponse().getContentAsString()).at("/data/0/sourceAlertId").asText();
        mockMvc.perform(authorized(post("/v1/investigations/cases"), org, "invest:case:write")
                        .content("{\"title\":\"dup\",\"sourceAlertId\":\"" + sourceAlertId + "\"}"))
                .andExpect(status().isConflict());
        assertEquals(1, events.snapshot().stream().filter(e -> "CaseCreated".equals(e.get("eventType"))).count());
        assertFalse(events.snapshot().stream().anyMatch(e -> "ComplianceReviewed".equals(e.get("eventType"))));
    }

    @Test
    void permissionsAreDistinct() {
        assertEquals("invest:case:read", InvestAccessFilter.requiredPermission("GET", "/v1/investigations/cases"));
        assertEquals("invest:case:write", InvestAccessFilter.requiredPermission("POST", "/v1/investigations/cases"));
        assertEquals("invest:case:close", InvestAccessFilter.requiredPermission("POST", "/v1/investigations/cases/x/close"));
        assertEquals("invest:case:assign", InvestAccessFilter.requiredPermission("POST", "/v1/investigations/cases/x/assign"));
        assertEquals("invest:evidence:write", InvestAccessFilter.requiredPermission("POST", "/v1/investigations/cases/x/evidence"));
        assertEquals("invest:case:write", InvestAccessFilter.requiredPermission("GET", "/v1/investigations/cases/x/notes"));
    }

    private String createCase(String title) throws Exception {
        MvcResult created = mockMvc.perform(authorized(post("/v1/investigations/cases"), org, "invest:case:write")
                        .content("{\"title\":\"" + title + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(created.getResponse().getContentAsString()).at("/data/id").asText();
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
