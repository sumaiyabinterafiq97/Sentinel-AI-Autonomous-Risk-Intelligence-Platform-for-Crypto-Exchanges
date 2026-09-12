package com.sentinel.ops.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.sentinel.ops.application.AlertService;
import com.sentinel.ops.domain.AlertException;

@RestController
public class AlertController {

    private final AlertService alerts;

    public AlertController(AlertService alerts) {
        this.alerts = alerts;
    }

    /** API-ALERT-001 listAlerts */
    @GetMapping(value = "/v1/alerts", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> listAlerts(
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", required = false, defaultValue = "50") int limit) {
        List<Map<String, Object>> data = alerts.list(cursor, limit);
        String last = data.isEmpty() ? null : String.valueOf(data.get(data.size() - 1).get("id"));
        boolean hasMore = alerts.hasMore(last, limit);
        return RiskErrorWriter.success(data, limit, last, hasMore);
    }

    /** API-ALERT-002 getAlert */
    @GetMapping(value = "/v1/alerts/{alertId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getAlert(@PathVariable("alertId") UUID alertId) {
        return RiskErrorWriter.success(alerts.get(alertId), null, null, null);
    }

    /** API-ALERT-003 patchAlert */
    @PatchMapping(
            value = "/v1/alerts/{alertId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> patchAlert(@PathVariable("alertId") UUID alertId, @RequestBody JsonNode body) {
        return RiskErrorWriter.success(alerts.patch(alertId, text(body, "status")), null, null, null);
    }

    /** API-ALERT-004 assignAlert */
    @PostMapping(
            value = "/v1/alerts/{alertId}/assign",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> assignAlert(@PathVariable("alertId") UUID alertId, @RequestBody JsonNode body) {
        return RiskErrorWriter.success(alerts.assign(alertId, uuid(text(body, "assigneeId"), "assigneeId")), null, null, null);
    }

    /** API-ALERT-005 closeAlert */
    @PostMapping(
            value = "/v1/alerts/{alertId}/close",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> closeAlert(@PathVariable("alertId") UUID alertId, @RequestBody JsonNode body) {
        return RiskErrorWriter.success(alerts.close(alertId, text(body, "dispositionReason")), null, null, null);
    }

    /** API-ALERT-006 patchAlertPriority */
    @PatchMapping(
            value = "/v1/alerts/{alertId}/priority",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> patchAlertPriority(@PathVariable("alertId") UUID alertId, @RequestBody JsonNode body) {
        if (body == null || !body.has("priority") || body.get("priority").isNull()) {
            throw AlertException.validation("priority", "priority is required");
        }
        if (!body.get("priority").canConvertToInt()) {
            throw AlertException.validation("priority", "priority must be an integer");
        }
        return RiskErrorWriter.success(alerts.patchPriority(alertId, body.get("priority").asInt()), null, null, null);
    }

    /** API-ALERT-007 linkAlertInvestigation */
    @PostMapping(
            value = "/v1/alerts/{alertId}/investigation-link",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> linkAlertInvestigation(
            @PathVariable("alertId") UUID alertId, @RequestBody JsonNode body) {
        return RiskErrorWriter.success(alerts.linkInvestigation(alertId, uuid(text(body, "caseId"), "caseId")), null, null, null);
    }

    private static String text(JsonNode body, String field) {
        if (body == null || !body.has(field) || body.get(field).isNull()) {
            return null;
        }
        return body.get(field).asText();
    }

    private static UUID uuid(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            throw AlertException.validation(field, field + " is required");
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            throw AlertException.validation(field, field + " must be a UUID");
        }
    }
}
