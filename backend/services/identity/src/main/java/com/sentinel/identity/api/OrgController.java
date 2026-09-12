package com.sentinel.identity.api;

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
import com.sentinel.identity.application.OrgService;

@RestController
public class OrgController {

    private final OrgService orgs;

    public OrgController(OrgService orgs) {
        this.orgs = orgs;
    }

    @GetMapping("/v1/organizations")
    public Map<String, Object> list(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "50") int limit) {
        List<Map<String, Object>> data = orgs.list(cursor, limit);
        String last = data.isEmpty() ? null : String.valueOf(data.get(data.size() - 1).get("name"));
        return IdentityErrorWriter.success(data, limit, last, false);
    }

    @PostMapping(value = "/v1/organizations", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@RequestBody JsonNode body) {
        String name = body.has("name") ? body.get("name").asText() : null;
        return IdentityErrorWriter.success(orgs.create(name), null, null, null);
    }

    @PatchMapping(value = "/v1/organizations/{orgId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> patch(@PathVariable UUID orgId, @RequestBody JsonNode body) {
        String name = body.has("name") ? body.get("name").asText() : null;
        String status = body.has("status") ? body.get("status").asText() : null;
        return IdentityErrorWriter.success(orgs.patch(orgId, name, status), null, null, null);
    }
}
