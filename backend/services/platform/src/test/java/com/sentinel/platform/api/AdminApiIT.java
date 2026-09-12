package com.sentinel.platform.api;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import com.sentinel.common.security.AccessTokenCodec;
import com.sentinel.platform.PlatformPostgresIT;
import com.sentinel.platform.application.IdentityOrchestrator;
import com.sentinel.platform.infrastructure.AdminStore;
import com.sentinel.platform.infrastructure.InProcessCoreEventBus;

@SpringBootTest
@AutoConfigureMockMvc
class AdminApiIT extends PlatformPostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccessTokenCodec tokens;

    @Autowired
    private InProcessCoreEventBus events;

    @Autowired
    private AdminStore store;

    @MockitoBean
    private IdentityOrchestrator identity;

    @BeforeEach
    void resetEvents() {
        events.reset();
    }

    @Test
    void healthReportsM10() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.milestone").value("M10"));
    }

    @Test
    void settingsRequireAuthPermissionAndIsolateTenants() throws Exception {
        UUID org = UUID.randomUUID();
        mockMvc.perform(get("/v1/admin/settings").header("X-Organization-Id", org.toString()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_AUTHENTICATION_001"));
        mockMvc.perform(authorized(get("/v1/admin/settings"), org, "admin:audit:read"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTHZ_FORBIDDEN_001"));

        mockMvc.perform(authorized(patch("/v1/admin/settings"), org, "admin:settings:write")
                        .content("{\"retention.days\": 14}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data['retention.days']").value(14));
        mockMvc.perform(authorized(get("/v1/admin/settings"), org, "admin:settings:write"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data['retention.days']").value(14));

        UUID other = UUID.randomUUID();
        mockMvc.perform(authorized(get("/v1/admin/settings"), other, "admin:settings:write"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data['retention.days']").doesNotExist());

        mockMvc.perform(authorized(patch("/v1/admin/settings"), org, "admin:settings:write").content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ADMIN_VALIDATION_001"));
    }

    @Test
    void integrationsRejectPlaintextSecretsAndPatchByType() throws Exception {
        UUID org = UUID.randomUUID();
        mockMvc.perform(authorized(post("/v1/admin/integrations"), org, "admin:integration:write")
                        .content("{\"type\":\"webhooks\",\"config\":{\"password\":\"plaintext\"}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ADMIN_VALIDATION_001"));

        mockMvc.perform(authorized(post("/v1/admin/integrations"), org, "admin:integration:write")
                        .content("{\"type\":\"webhooks\",\"config\":{\"url\":\"https://example.invalid\"},\"secretRef\":\"vault:wh\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("webhooks"))
                .andExpect(jsonPath("$.data.status").value("active"));

        mockMvc.perform(authorized(patch("/v1/admin/integrations"), org, "admin:integration:write")
                        .content("{\"type\":\"webhooks\",\"status\":\"inactive\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("inactive"));

        UUID other = UUID.randomUUID();
        mockMvc.perform(authorized(get("/v1/admin/integrations"), other, "admin:integration:write"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].type", not(hasItem("webhooks"))));
    }

    @Test
    void provisionDelegatesToIdentityAndDoesNotCreateAdminUserTables() throws Exception {
        UUID org = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", userId.toString());
        user.put("email", "analyst@example.com");
        user.put("status", "active");
        user.put("organizationId", org.toString());
        when(identity.createUser(eq("analyst@example.com"), eq(org), any(), any(), eq(org))).thenReturn(user);

        Map<String, Object> createdOrg = new LinkedHashMap<>();
        UUID newOrg = UUID.randomUUID();
        createdOrg.put("id", newOrg.toString());
        createdOrg.put("name", "Exchange Two");
        createdOrg.put("status", "active");
        when(identity.createOrganization(eq("Exchange Two"), any(), eq(org))).thenReturn(createdOrg);

        mockMvc.perform(authorized(post("/v1/admin/users/provision"), org, "admin:user:provision")
                        .content("{\"email\":\"analyst@example.com\",\"organizationId\":\"" + org + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("analyst@example.com"));

        UUID other = UUID.randomUUID();
        mockMvc.perform(authorized(post("/v1/admin/users/provision"), org, "admin:user:provision")
                        .content("{\"email\":\"x@example.com\",\"organizationId\":\"" + other + "\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(authorized(post("/v1/admin/organizations/provision"), org, "admin:org:provision")
                        .content("{\"name\":\"Exchange Two\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Exchange Two"));

        assertEquals(0, store.countUserTablesInAdmin());
    }

    @Test
    void auditListsAdminAndCoreRecords() throws Exception {
        UUID org = UUID.randomUUID();
        mockMvc.perform(authorized(patch("/v1/admin/settings"), org, "admin:settings:write")
                        .content("{\"theme\":\"dark\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(authorized(get("/v1/admin/audit-records"), org, "admin:audit:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].action", hasItem("SETTING_UPDATED")));
    }

    @Test
    void settingAndIntegrationEventsArePublished() throws Exception {
        UUID org = UUID.randomUUID();
        mockMvc.perform(authorized(patch("/v1/admin/settings"), org, "admin:settings:write")
                        .content("{\"locale\":\"en\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(authorized(post("/v1/admin/integrations"), org, "admin:integration:write")
                        .content("{\"type\":\"email\"}"))
                .andExpect(status().isCreated());
        long settings = events.snapshot().stream()
                .filter(e -> "AdminSettingUpdated".equals(e.envelope().get("eventType")))
                .count();
        long integrations = events.snapshot().stream()
                .filter(e -> "IntegrationConfigured".equals(e.envelope().get("eventType")))
                .count();
        long actions = events.snapshot().stream()
                .filter(e -> "AdminActionPerformed".equals(e.envelope().get("eventType")))
                .count();
        assertTrue(settings >= 1);
        assertTrue(integrations >= 1);
        assertTrue(actions >= 2);
        events.snapshot().forEach(e -> assertEquals("ADMIN", e.envelope().get("producer")));
    }

    private MockHttpServletRequestBuilder authorized(
            MockHttpServletRequestBuilder request, UUID org, String... permissions) {
        String token = tokens.issue(
                UUID.randomUUID(), org, UUID.randomUUID(), Set.of(permissions), Instant.now().plusSeconds(120));
        return request.header("Authorization", "Bearer " + token)
                .header("X-Organization-Id", org.toString())
                .header("X-Correlation-Id", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }
}
