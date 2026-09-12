package com.sentinel.dash.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import com.sentinel.common.security.AccessTokenCodec;
import com.sentinel.dash.DashPostgresIT;
import com.sentinel.dash.application.DashUpstreamConsumer;
import com.sentinel.dash.application.DashWorkspaceService;

@SpringBootTest
@AutoConfigureMockMvc
class DashApiIT extends DashPostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccessTokenCodec tokens;

    @Autowired
    private DashUpstreamConsumer consumer;

    private final UUID org = UUID.randomUUID();

    @Test
    void healthIsM9() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.milestone").value("M9"));
    }

    @Test
    void unauthenticatedForbiddenValidationAndTenantIsolation() throws Exception {
        mockMvc.perform(get("/v1/workspace").header("X-Organization-Id", org.toString()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(authorized(get("/v1/workspace"), org, "dash:queue:read")).andExpect(status().isForbidden());
        mockMvc.perform(authorized(get("/v1/workspace"), org)).andExpect(status().isOk());
        UUID other = UUID.randomUUID();
        String tokenA = tokens.issue(
                UUID.randomUUID(), org, UUID.randomUUID(), Set.of("dash:workspace:read"), Instant.now().plusSeconds(600));
        mockMvc.perform(get("/v1/workspace")
                        .header("Authorization", "Bearer " + tokenA)
                        .header("X-Organization-Id", other.toString()))
                .andExpect(status().isForbidden());
        mockMvc.perform(authorized(get("/v1/workspace/queues/unknown"), org, "dash:queue:read"))
                .andExpect(status().isNotFound());
    }

    @Test
    void workspaceDashboardQueuesWidgetsAndComplianceGap() throws Exception {
        UUID alertId = UUID.randomUUID();
        consumer.consume(Map.of(
                "eventId",
                UUID.randomUUID().toString(),
                "eventType",
                "AlertCreated",
                "organizationId",
                org.toString(),
                "payload",
                Map.of(
                        "alertId",
                        alertId.toString(),
                        "status",
                        "open",
                        "priority",
                        10,
                        "title",
                        "High risk",
                        "createdAt",
                        Instant.now().toString())));
        mockMvc.perform(authorized(get("/v1/workspace"), org))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.queues[0]").value("alerts"));
        mockMvc.perform(authorized(get("/v1/workspace/dashboard"), org))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alertCount").value(1));
        mockMvc.perform(authorized(get("/v1/workspace/queues/alerts"), org, "dash:queue:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(alertId.toString()));
        mockMvc.perform(authorized(get("/v1/workspace/queues/compliance"), org, "dash:queue:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
        mockMvc.perform(authorized(get("/v1/workspace/widgets"), org, "dash:widget:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(DashWorkspaceService.WIDGET_ALERTS.toString()));
        mockMvc.perform(authorized(
                                post("/v1/workspace/widgets/" + DashWorkspaceService.WIDGET_ALERTS + "/interactions"),
                                org)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"interactionType\":\"open\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.logged").value(true));
    }

    @Test
    void sseRequiresAuthAndRejectsUnknownChannel() throws Exception {
        mockMvc.perform(get("/v1/workspace/subscriptions/alerts").header("X-Organization-Id", org.toString()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(authorized(get("/v1/workspace/subscriptions/not-a-channel"), org))
                .andExpect(status().isNotFound());
    }

    @Test
    void bffWithoutUpstreamIs503NotNewApi() throws Exception {
        mockMvc.perform(authorized(get("/v1/alerts"), org, "alert:alert:read"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error.code").value("DASH_DEPENDENCY_001"));
    }

    @Test
    void permissionsMatchInventory() {
        assertEquals("dash:workspace:read", DashAccessFilter.requiredPermission("GET", "/v1/workspace"));
        assertEquals("dash:queue:read", DashAccessFilter.requiredPermission("GET", "/v1/workspace/queues/alerts"));
        assertEquals("dash:widget:read", DashAccessFilter.requiredPermission("GET", "/v1/workspace/widgets"));
        assertEquals(
                "dash:workspace:read",
                DashAccessFilter.requiredPermission("POST", "/v1/workspace/widgets/x/interactions"));
        assertEquals("dash:workspace:read", DashAccessFilter.requiredPermission("GET", "/v1/workspace/subscriptions/alerts"));
    }

    private MockHttpServletRequestBuilder authorized(
            MockHttpServletRequestBuilder req, UUID organization, String... extra) {
        Set<String> perms = extra.length == 0
                ? Set.of("dash:workspace:read", "dash:queue:read", "dash:widget:read", "alert:alert:read")
                : Set.of(extra);
        String token =
                tokens.issue(UUID.randomUUID(), organization, UUID.randomUUID(), perms, Instant.now().plusSeconds(600));
        return req.header("Authorization", "Bearer " + token).header("X-Organization-Id", organization.toString());
    }
}
