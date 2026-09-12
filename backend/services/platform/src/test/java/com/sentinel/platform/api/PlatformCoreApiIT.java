package com.sentinel.platform.api;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import com.sentinel.common.security.AccessTokenCodec;
import com.sentinel.platform.PlatformPostgresIT;
import com.sentinel.platform.infrastructure.InProcessCoreEventBus;

@SpringBootTest
@AutoConfigureMockMvc
class PlatformCoreApiIT extends PlatformPostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InProcessCoreEventBus events;

    @Autowired
    private AccessTokenCodec tokens;

    @BeforeEach
    void resetEvents() {
        events.reset();
    }

    @Test
    void getPlatformHealthIsPublicAndHealthy() throws Exception {
        mockMvc.perform(get("/v1/platform/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("healthy"))
                .andExpect(jsonPath("$.meta.requestId").isNotEmpty())
                .andExpect(jsonPath("$.meta.timestamp").isNotEmpty());
    }

    @Test
    void getPlatformStatusRequiresAuthAndPermission() throws Exception {
        UUID org = UUID.randomUUID();
        mockMvc.perform(get("/v1/platform/status").header("X-Organization-Id", org.toString()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_AUTHENTICATION_001"))
                .andExpect(jsonPath("$.error.requestId").isNotEmpty())
                .andExpect(jsonPath("$.error.retryable").value(false));

        mockMvc.perform(authorized(get("/v1/platform/status"), org, "platform:config:read"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTHZ_FORBIDDEN_001"));

        mockMvc.perform(authorized(get("/v1/platform/status"), org, "platform:status:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.operationalStatus").value("available"))
                .andExpect(jsonPath("$.data.maintenanceMode").value(false))
                .andExpect(jsonPath("$.data.optionalCapabilities.ai").value("degraded"));
    }

    @Test
    void invalidOrganizationHeaderIsValidationError() throws Exception {
        mockMvc.perform(get("/v1/platform/config")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Organization-Id", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CORE_VALIDATION_001"));
    }

    @Test
    void configReadWriteAndAuditEvent() throws Exception {
        UUID org = UUID.randomUUID();
        mockMvc.perform(authorized(get("/v1/platform/config"), org, "platform:config:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data['maintenance.mode']").value(false));

        mockMvc.perform(authorized(patch("/v1/platform/config"), org, "platform:config:write")
                        .content("{\"retention.days\": 30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data['retention.days']").value(30));

        mockMvc.perform(authorized(patch("/v1/platform/config"), org, "platform:config:write").content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CORE_VALIDATION_001"))
                .andExpect(jsonPath("$.error.details[0].field").value("body"));

        long configEvents = events.snapshot().stream()
                .filter(e -> "ConfigurationUpdated".equals(e.envelope().get("eventType")))
                .count();
        assertEquals(1, configEvents);
    }

    @Test
    void tenantIsolationOnConfig() throws Exception {
        UUID orgA = UUID.randomUUID();
        UUID orgB = UUID.randomUUID();
        mockMvc.perform(authorized(patch("/v1/platform/config"), orgA, "platform:config:write")
                        .content("{\"tenant.secret\": \"alpha\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(authorized(get("/v1/platform/config"), orgB, "platform:config:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data['tenant.secret']").doesNotExist());
        mockMvc.perform(authorized(get("/v1/platform/config"), orgA, "platform:config:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data['tenant.secret']").value("alpha"));
    }

    @Test
    void featureFlagsPatchListAndPagination() throws Exception {
        UUID org = UUID.randomUUID();
        mockMvc.perform(authorized(patch("/v1/platform/feature-flags/alpha.flag"), org, "platform:flags:write")
                        .content("{\"enabled\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.key").value("alpha.flag"))
                .andExpect(jsonPath("$.data.enabled").value(true));
        mockMvc.perform(authorized(patch("/v1/platform/feature-flags/beta.flag"), org, "platform:flags:write")
                        .content("{\"enabled\": false}"))
                .andExpect(status().isOk());
        mockMvc.perform(authorized(get("/v1/platform/feature-flags"), org, "platform:flags:read")
                        .param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].key").value("alpha.flag"))
                .andExpect(jsonPath("$.meta.pagination.hasMore").value(true))
                .andExpect(jsonPath("$.meta.pagination.limit").value(1));

        mockMvc.perform(authorized(patch("/v1/platform/feature-flags/alpha.flag"), org, "platform:flags:write")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CORE_VALIDATION_001"));

        long flagEvents = events.snapshot().stream()
                .filter(e -> "FeatureFlagChanged".equals(e.envelope().get("eventType")))
                .count();
        assertEquals(2, flagEvents);
    }

    @Test
    void tenantIsolationOnFeatureFlags() throws Exception {
        UUID orgA = UUID.randomUUID();
        UUID orgB = UUID.randomUUID();
        mockMvc.perform(authorized(patch("/v1/platform/feature-flags/org.only"), orgA, "platform:flags:write")
                        .content("{\"enabled\": true}"))
                .andExpect(status().isOk());
        mockMvc.perform(authorized(get("/v1/platform/feature-flags"), orgB, "platform:flags:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].key", not(hasItem("org.only"))));
    }

    @Test
    void maintenanceModeChangesStatusAndCanBeRestored() throws Exception {
        UUID org = UUID.randomUUID();
        mockMvc.perform(authorized(patch("/v1/platform/config"), org, "platform:config:write")
                        .content("{\"maintenance.mode\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data['maintenance.mode']").value(true));
        mockMvc.perform(authorized(get("/v1/platform/status"), org, "platform:status:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.operationalStatus").value("maintenance"))
                .andExpect(jsonPath("$.data.maintenanceMode").value(true));
        mockMvc.perform(get("/v1/platform/health").header("X-Organization-Id", org.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("degraded"));
        mockMvc.perform(authorized(patch("/v1/platform/config"), org, "platform:config:write")
                        .content("{\"maintenance.mode\": false}"))
                .andExpect(status().isOk());
        mockMvc.perform(authorized(get("/v1/platform/status"), org, "platform:status:read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.operationalStatus").value("available"));
    }

    @Test
    void missingOrganizationHeaderIsValidationError() throws Exception {
        mockMvc.perform(get("/v1/platform/config").header("Authorization", "Bearer test-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CORE_VALIDATION_001"));
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
