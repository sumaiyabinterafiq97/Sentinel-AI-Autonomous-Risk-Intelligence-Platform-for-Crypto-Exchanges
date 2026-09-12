package com.sentinel.ops.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.sentinel.ops.application.InvestService;
import com.sentinel.ops.domain.InvestException;

@RestController
public class InvestController {

    private final InvestService invest;

    public InvestController(InvestService invest) {
        this.invest = invest;
    }

    @GetMapping(value = "/v1/investigations/cases", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> listCases(
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", required = false, defaultValue = "50") int limit) {
        List<Map<String, Object>> data = invest.list(cursor, limit);
        String last = data.isEmpty() ? null : String.valueOf(data.get(data.size() - 1).get("id"));
        return RiskErrorWriter.success(data, limit, last, invest.hasMore(last));
    }

    @PostMapping(
            value = "/v1/investigations/cases",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> createCase(@RequestBody JsonNode body) {
        Map<String, Object> data = invest.create(text(body, "title"), uuidOrNull(text(body, "sourceAlertId"), "sourceAlertId"));
        return ResponseEntity.status(HttpStatus.CREATED).body(RiskErrorWriter.success(data, null, null, null));
    }

    @GetMapping(value = "/v1/investigations/cases/{caseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getCase(@PathVariable("caseId") UUID caseId) {
        return RiskErrorWriter.success(invest.get(caseId), null, null, null);
    }

    @PatchMapping(
            value = "/v1/investigations/cases/{caseId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> patchCase(@PathVariable("caseId") UUID caseId, @RequestBody JsonNode body) {
        return RiskErrorWriter.success(invest.patch(caseId, text(body, "title"), text(body, "status")), null, null, null);
    }

    @PostMapping(
            value = "/v1/investigations/cases/{caseId}/close",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> closeCase(@PathVariable("caseId") UUID caseId, @RequestBody JsonNode body) {
        return RiskErrorWriter.success(invest.close(caseId, text(body, "resolutionSummary")), null, null, null);
    }

    @PostMapping(
            value = "/v1/investigations/cases/{caseId}/assign",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> assignCase(@PathVariable("caseId") UUID caseId, @RequestBody JsonNode body) {
        return RiskErrorWriter.success(invest.assign(caseId, uuid(text(body, "assigneeId"), "assigneeId")), null, null, null);
    }

    @PostMapping(
            value = "/v1/investigations/cases/{caseId}/evidence",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> attachEvidence(@PathVariable("caseId") UUID caseId, @RequestBody JsonNode body) {
        return RiskErrorWriter.success(
                invest.attachEvidence(caseId, text(body, "evidenceRef"), text(body, "description")), null, null, null);
    }

    @GetMapping(value = "/v1/investigations/cases/{caseId}/timeline", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getCaseTimeline(@PathVariable("caseId") UUID caseId) {
        return RiskErrorWriter.success(invest.timeline(caseId), null, null, null);
    }

    @GetMapping(value = "/v1/investigations/cases/{caseId}/notes", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> listCaseNotes(
            @PathVariable("caseId") UUID caseId,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", required = false, defaultValue = "50") int limit) {
        List<Map<String, Object>> data = invest.listNotes(caseId, cursor, limit);
        String last = data.isEmpty() ? null : String.valueOf(data.get(data.size() - 1).get("id"));
        return RiskErrorWriter.success(data, limit, last, false);
    }

    @PostMapping(
            value = "/v1/investigations/cases/{caseId}/notes",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> createCaseNote(
            @PathVariable("caseId") UUID caseId, @RequestBody JsonNode body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RiskErrorWriter.success(invest.createNote(caseId, text(body, "content")), null, null, null));
    }

    private static String text(JsonNode body, String field) {
        if (body == null || !body.has(field) || body.get(field).isNull()) {
            return null;
        }
        return body.get(field).asText();
    }

    private static UUID uuid(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            throw InvestException.validation(field, field + " is required");
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            throw InvestException.validation(field, field + " must be a UUID");
        }
    }

    private static UUID uuidOrNull(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            throw InvestException.validation(field, field + " must be a UUID");
        }
    }
}
