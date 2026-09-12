package com.sentinel.identity.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.sentinel.identity.application.UserService;

@RestController
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @GetMapping("/v1/users")
    public Map<String, Object> list(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "50") int limit) {
        List<Map<String, Object>> data = users.list(cursor, limit);
        String last = data.isEmpty() ? null : String.valueOf(data.get(data.size() - 1).get("email"));
        return IdentityErrorWriter.success(data, limit, last, false);
    }

    @PostMapping(value = "/v1/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@RequestBody JsonNode body) {
        List<UUID> roleIds = new ArrayList<>();
        if (body.has("roleIds") && body.get("roleIds").isArray()) {
            body.get("roleIds").forEach(n -> roleIds.add(UUID.fromString(n.asText())));
        }
        String email = body.has("email") ? body.get("email").asText() : null;
        return IdentityErrorWriter.success(users.create(email, roleIds), null, null, null);
    }

    @GetMapping("/v1/users/{userId}")
    public Map<String, Object> get(@PathVariable UUID userId) {
        return IdentityErrorWriter.success(users.get(userId), null, null, null);
    }

    @PatchMapping(value = "/v1/users/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> patch(@PathVariable UUID userId, @RequestBody JsonNode body) {
        String displayName = body.has("displayName") ? body.get("displayName").asText() : null;
        String status = body.has("status") ? body.get("status").asText() : null;
        return IdentityErrorWriter.success(users.patch(userId, displayName, status), null, null, null);
    }
}
