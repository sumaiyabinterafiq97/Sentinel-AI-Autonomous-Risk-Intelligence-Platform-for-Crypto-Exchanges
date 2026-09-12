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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.sentinel.ops.application.RiskService;
import com.sentinel.ops.domain.RiskException;

@RestController
public class RiskController {

    private final RiskService risk;

    public RiskController(RiskService risk) {
        this.risk = risk;
    }

    /** API-RISK-001 ingestTransaction */
    @PostMapping(value = "/v1/risk/transactions/ingest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> ingestTransaction(
            @RequestBody JsonNode body, @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        UUID userId = uuidOrNull(text(body, "userId"));
        Map<String, Object> data = risk.ingest(
                text(body, "externalTransactionId"),
                text(body, "amount"),
                text(body, "asset"),
                text(body, "timestamp"),
                userId,
                idempotencyKey);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(RiskErrorWriter.success(data, null, null, null));
    }

    /** API-RISK-002 listRiskAssessments */
    @GetMapping(value = "/v1/risk/assessments", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> listRiskAssessments(
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", required = false, defaultValue = "50") int limit) {
        List<Map<String, Object>> data = risk.listAssessments(cursor, limit);
        String last = data.isEmpty() ? null : String.valueOf(data.get(data.size() - 1).get("id"));
        boolean hasMore = risk.hasMoreAssessments(RiskContext.get().organizationId(), last, limit);
        return RiskErrorWriter.success(data, limit, last, hasMore);
    }

    /** API-RISK-003 getRiskAssessment */
    @GetMapping(value = "/v1/risk/assessments/{assessmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getRiskAssessment(@PathVariable("assessmentId") UUID assessmentId) {
        return RiskErrorWriter.success(risk.getAssessment(assessmentId), null, null, null);
    }

    /** API-RISK-004 listRiskRules */
    @GetMapping(value = "/v1/risk/rules", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> listRiskRules(
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", required = false, defaultValue = "50") int limit) {
        List<Map<String, Object>> data = risk.listRules(cursor, limit);
        String last = data.isEmpty() ? null : String.valueOf(data.get(data.size() - 1).get("id"));
        boolean hasMore = risk.hasMoreRules(RiskContext.get().organizationId(), last, limit);
        return RiskErrorWriter.success(data, limit, last, hasMore);
    }

    /** API-RISK-005 createRiskRule */
    @PostMapping(value = "/v1/risk/rules", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> createRiskRule(@RequestBody JsonNode body) {
        Map<String, Object> data = risk.createRule(text(body, "name"), body.get("definition"), bool(body, "enabled"));
        return ResponseEntity.status(HttpStatus.CREATED).body(RiskErrorWriter.success(data, null, null, null));
    }

    /** API-RISK-006 patchRiskRule */
    @PatchMapping(value = "/v1/risk/rules/{ruleId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> patchRiskRule(@PathVariable("ruleId") UUID ruleId, @RequestBody JsonNode body) {
        return RiskErrorWriter.success(
                risk.patchRule(ruleId, text(body, "name"), body.get("definition"), bool(body, "enabled")),
                null,
                null,
                null);
    }

    /** API-RISK-007 triggerRiskEvaluation */
    @PostMapping(value = "/v1/risk/evaluate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> triggerRiskEvaluation(@RequestBody JsonNode body) {
        Map<String, Object> data = risk.evaluate(text(body, "entityType"), text(body, "entityId"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(RiskErrorWriter.success(data, null, null, null));
    }

    private static String text(JsonNode body, String field) {
        if (body == null || !body.has(field) || body.get(field).isNull()) {
            return null;
        }
        return body.get(field).asText();
    }

    private static Boolean bool(JsonNode body, String field) {
        if (body == null || !body.has(field) || body.get(field).isNull()) {
            return null;
        }
        if (!body.get(field).isBoolean()) {
            return null;
        }
        return body.get(field).asBoolean();
    }

    private static UUID uuidOrNull(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            throw RiskException.validation("userId", "userId must be a UUID");
        }
    }
}
