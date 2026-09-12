package com.sentinel.identity.api;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.sentinel.identity.application.AuthService;

@RestController
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping(value = "/v1/auth/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> login(@RequestBody JsonNode body) {
        return IdentityErrorWriter.success(
                auth.login(text(body, "email"), text(body, "password")), null, null, null);
    }

    @PostMapping(value = "/v1/auth/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> logout(@RequestBody(required = true) JsonNode body) {
        auth.logout();
        return IdentityErrorWriter.success(Map.of(), null, null, null);
    }

    @PostMapping(value = "/v1/auth/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> refresh(@RequestBody JsonNode body) {
        return IdentityErrorWriter.success(auth.refresh(text(body, "refreshToken")), null, null, null);
    }

    @GetMapping("/v1/auth/session")
    public Map<String, Object> session() {
        return IdentityErrorWriter.success(auth.session(), null, null, null);
    }

    @PostMapping(value = "/v1/auth/mfa/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> mfa(@RequestBody JsonNode body) {
        return IdentityErrorWriter.success(auth.verifyMfa(text(body, "code")), null, null, null);
    }

    private static String text(JsonNode body, String field) {
        if (body == null || body.get(field) == null || body.get(field).isNull()) {
            return null;
        }
        return body.get(field).asText();
    }
}
