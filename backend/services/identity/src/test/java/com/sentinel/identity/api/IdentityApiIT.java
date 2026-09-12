package com.sentinel.identity.api;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.identity.IdentityFixtures;
import com.sentinel.identity.IdentityFixtures.SeededAdmin;
import com.sentinel.identity.IdentityPostgresIT;
import com.sentinel.identity.application.AuthzService;
import com.sentinel.identity.infrastructure.IdentityEventBus;
import com.sentinel.identity.infrastructure.IdentityStore;

@SpringBootTest
@AutoConfigureMockMvc
class IdentityApiIT extends IdentityPostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IdentityStore store;

    @Autowired
    private AuthzService authz;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IdentityEventBus events;

    private SeededAdmin admin;

    @BeforeEach
    void seed() {
        events.reset();
        admin = IdentityFixtures.seedAdmin(store, authz, encoder);
    }

    @Test
    void loginRejectsInvalidCredentialsWithoutEnumeratingUsers() throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_AUTHENTICATION_001"));
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + admin.email() + "\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_AUTHENTICATION_001"));
    }

    @Test
    void loginSessionLogoutAndRefresh() throws Exception {
        JsonNode tokens = login(admin.email(), admin.password());
        String access = tokens.get("accessToken").asText();
        String refresh = tokens.get("refreshToken").asText();
        mockMvc.perform(get("/v1/auth/session").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(admin.userId().toString()))
                .andExpect(jsonPath("$.data.organizationId").value(admin.organizationId().toString()));
        mockMvc.perform(post("/v1/auth/mfa/verify")
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verified").value(true));
        mockMvc.perform(post("/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
        mockMvc.perform(post("/v1/auth/logout")
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/auth/session").header("Authorization", "Bearer " + access))
                .andExpect(status().isUnauthorized());
        assertTrue(events.snapshot().stream().anyMatch(e -> "UserLoggedIn".equals(e.get("eventType"))));
        assertTrue(events.snapshot().stream().anyMatch(e -> "SessionExpired".equals(e.get("eventType"))));
    }

    @Test
    void authzDenyByDefaultAndPermitAssigned() throws Exception {
        JsonNode tokens = login(admin.email(), admin.password());
        String access = tokens.get("accessToken").asText();
        mockMvc.perform(post("/v1/authz/evaluate")
                        .header("Authorization", "Bearer " + access)
                        .header("X-Organization-Id", admin.organizationId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"user:user:read\",\"resource\":\"user\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.allowed").value(true));
        mockMvc.perform(post("/v1/authz/evaluate")
                        .header("Authorization", "Bearer " + access)
                        .header("X-Organization-Id", admin.organizationId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"report:report:write\",\"resource\":\"report\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.allowed").value(false));
        mockMvc.perform(get("/v1/authz/roles")
                        .header("Authorization", "Bearer " + access)
                        .header("X-Organization-Id", admin.organizationId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Organization Administrator"));
    }

    @Test
    void usersAreTenantIsolatedAndUpdatesPublishUserUpdated() throws Exception {
        JsonNode tokens = login(admin.email(), admin.password());
        String access = tokens.get("accessToken").asText();
        mockMvc.perform(post("/v1/users")
                        .header("Authorization", "Bearer " + access)
                        .header("X-Organization-Id", admin.organizationId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"analyst@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("analyst@example.com"));
        MvcResult created = mockMvc.perform(get("/v1/users")
                        .header("Authorization", "Bearer " + access)
                        .header("X-Organization-Id", admin.organizationId().toString()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode list = objectMapper.readTree(created.getResponse().getContentAsByteArray());
        String analystId = null;
        for (JsonNode u : list.get("data")) {
            if ("analyst@example.com".equals(u.get("email").asText())) {
                analystId = u.get("id").asText();
            }
        }
        mockMvc.perform(patch("/v1/users/" + analystId)
                        .header("Authorization", "Bearer " + access)
                        .header("X-Organization-Id", admin.organizationId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Analyst One\"}"))
                .andExpect(status().isOk());
        SeededAdmin other = IdentityFixtures.seedAdmin(store, authz, encoder);
        JsonNode otherTokens = login(other.email(), other.password());
        mockMvc.perform(get("/v1/users/" + analystId)
                        .header("Authorization", "Bearer " + otherTokens.get("accessToken").asText())
                        .header("X-Organization-Id", other.organizationId().toString()))
                .andExpect(status().isNotFound());
        assertTrue(events.snapshot().stream().anyMatch(e -> "UserUpdated".equals(e.get("eventType"))));
        assertFalse(events.snapshot().stream().anyMatch(e -> "UserCreated".equals(e.get("eventType"))));
    }

    @Test
    void organizationsAreMembershipScoped() throws Exception {
        JsonNode tokens = login(admin.email(), admin.password());
        String access = tokens.get("accessToken").asText();
        mockMvc.perform(get("/v1/organizations").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].id", hasItem(admin.organizationId().toString())));
        mockMvc.perform(post("/v1/organizations")
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Second Exchange\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Second Exchange"));
        SeededAdmin other = IdentityFixtures.seedAdmin(store, authz, encoder);
        mockMvc.perform(patch("/v1/organizations/" + admin.organizationId())
                        .header("Authorization", "Bearer " + login(other.email(), other.password()).get("accessToken").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hijack\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedProtectedRoutesFail() throws Exception {
        mockMvc.perform(get("/v1/users").header("X-Organization-Id", admin.organizationId().toString()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/v1/authz/roles").header("X-Organization-Id", admin.organizationId().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthRemainsPublic() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("sentinel-identity"));
    }

    private JsonNode login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).get("data");
    }
}
