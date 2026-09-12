package com.sentinel.identity.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.sentinel.identity.application.AuthzService;

@RestController
public class AuthzController {

    private final AuthzService authz;

    public AuthzController(AuthzService authz) {
        this.authz = authz;
    }

    @PostMapping(value = "/v1/authz/evaluate", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> evaluate(@RequestBody JsonNode body) {
        String action = text(body, "action");
        String resource = text(body, "resource");
        UUID subject = uuid(body, "subjectId");
        return IdentityErrorWriter.success(authz.evaluate(action, resource, subject), null, null, null);
    }

    @GetMapping("/v1/authz/roles")
    public Map<String, Object> listRoles(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "50") int limit) {
        List<Map<String, Object>> data = authz.listRoles(cursor, limit);
        String last = data.isEmpty() ? null : String.valueOf(data.get(data.size() - 1).get("name"));
        boolean hasMore = authz.listRoles(last, 1).size() == 1 && last != null;
        return IdentityErrorWriter.success(data, limit, last, hasMore);
    }

    @PostMapping(value = "/v1/authz/roles", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createRole(@RequestBody JsonNode body) {
        List<String> permissions = new ArrayList<>();
        if (body.has("permissions") && body.get("permissions").isArray()) {
            body.get("permissions").forEach(n -> permissions.add(n.asText()));
        }
        return IdentityErrorWriter.success(authz.createRole(text(body, "name"), permissions), null, null, null);
    }

    @PostMapping(value = "/v1/authz/role-assignments", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> assign(@RequestBody JsonNode body) {
        return IdentityErrorWriter.success(
                authz.assignRole(uuid(body, "userId"), uuid(body, "roleId")), null, null, null);
    }

    private static String text(JsonNode body, String field) {
        if (body == null || !body.has(field) || body.get(field).isNull()) {
            return null;
        }
        return body.get(field).asText();
    }

    private static UUID uuid(JsonNode body, String field) {
        String raw = text(body, field);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return UUID.fromString(raw);
    }
}
