package com.sentinel.platform.api;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.sentinel.platform.application.AdminService;

@RestController
public class AdminController {

    private final AdminService admin;

    public AdminController(AdminService admin) {
        this.admin = admin;
    }

    @GetMapping("/v1/admin/settings")
    public Map<String, Object> getSettings() {
        return ApiExceptionHandler.success(admin.getSettings(), null, null, null);
    }

    @PatchMapping(value = "/v1/admin/settings", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> patchSettings(@RequestBody JsonNode body) {
        return ApiExceptionHandler.success(admin.patchSettings(body), null, null, null);
    }

    @GetMapping("/v1/admin/integrations")
    public Map<String, Object> listIntegrations(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "50") int limit) {
        List<Map<String, Object>> data = admin.listIntegrations(cursor, limit);
        String last = data.isEmpty() ? null : String.valueOf(data.get(data.size() - 1).get("id"));
        return ApiExceptionHandler.success(data, limit, last, data.size() == limit);
    }

    @PostMapping(value = "/v1/admin/integrations", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createIntegration(@RequestBody JsonNode body) {
        return ApiExceptionHandler.success(admin.createIntegration(body), null, null, null);
    }

    @PatchMapping(value = "/v1/admin/integrations", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> patchIntegration(@RequestBody JsonNode body) {
        return ApiExceptionHandler.success(admin.patchIntegration(body), null, null, null);
    }

    @PostMapping(value = "/v1/admin/users/provision", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> provisionUser(@RequestBody JsonNode body) {
        return ApiExceptionHandler.success(admin.provisionUser(body), null, null, null);
    }

    @PostMapping(value = "/v1/admin/organizations/provision", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> provisionOrganization(@RequestBody JsonNode body) {
        return ApiExceptionHandler.success(admin.provisionOrganization(body), null, null, null);
    }

    @GetMapping("/v1/admin/audit-records")
    public Map<String, Object> listAuditRecords(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "50") int limit) {
        List<Map<String, Object>> data = admin.listAuditRecords(cursor, limit);
        String last = null;
        if (!data.isEmpty()) {
            Map<String, Object> row = data.get(data.size() - 1);
            last = row.get("occurredAt") + "|" + row.get("id");
        }
        return ApiExceptionHandler.success(data, limit, last, data.size() == limit);
    }
}
