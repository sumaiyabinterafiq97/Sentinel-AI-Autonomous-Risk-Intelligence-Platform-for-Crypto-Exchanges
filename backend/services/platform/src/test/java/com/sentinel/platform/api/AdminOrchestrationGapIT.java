package com.sentinel.platform.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.sentinel.common.security.AccessTokenCodec;
import com.sentinel.platform.PlatformPostgresIT;

@SpringBootTest
@AutoConfigureMockMvc
class AdminOrchestrationGapIT extends PlatformPostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccessTokenCodec tokens;

    @Test
    void provisionWithoutIdentityUrlReturnsDependencyError() throws Exception {
        UUID org = UUID.randomUUID();
        String token = tokens.issue(
                UUID.randomUUID(), org, UUID.randomUUID(), Set.of("admin:user:provision"), Instant.now().plusSeconds(120));
        mockMvc.perform(post("/v1/admin/users/provision")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Organization-Id", org.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@example.com\",\"organizationId\":\"" + org + "\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error.code").value("ADMIN_DEPENDENCY_001"));
    }
}
