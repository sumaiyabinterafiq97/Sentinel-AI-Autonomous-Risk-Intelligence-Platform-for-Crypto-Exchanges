package com.sentinel.ops.api;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.sentinel.ops.application.CompService;
import com.sentinel.ops.domain.CompException;

@RestController
public class CompController {

    private final CompService comp;

    public CompController(CompService comp) {
        this.comp = comp;
    }

    @PostMapping(
            value = "/v1/compliance/kyc-reviews",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> startKycReview(@RequestBody JsonNode body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RiskErrorWriter.success(comp.startKyc(text(body, "subjectRef")), null, null, null));
    }

    @PatchMapping(
            value = "/v1/compliance/kyc-reviews/{reviewId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> completeKycReview(@PathVariable("reviewId") UUID reviewId, @RequestBody JsonNode body) {
        return RiskErrorWriter.success(comp.completeKyc(reviewId, text(body, "decision")), null, null, null);
    }

    @PostMapping(
            value = "/v1/compliance/aml-reviews",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> startAmlReview(@RequestBody JsonNode body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RiskErrorWriter.success(comp.startAml(text(body, "subjectRef")), null, null, null));
    }

    @PostMapping(
            value = "/v1/compliance/travel-rule/validations",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> validateTravelRule(@RequestBody JsonNode body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RiskErrorWriter.success(comp.validateTravelRule(text(body, "transactionRef")), null, null, null));
    }

    @PostMapping(
            value = "/v1/compliance/sanctions-screenings",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> createSanctionsScreening(@RequestBody JsonNode body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RiskErrorWriter.success(comp.createSanctions(text(body, "subjectRef")), null, null, null));
    }

    @PatchMapping(
            value = "/v1/compliance/sanctions-screenings/{screeningId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> dispositionSanctionsMatch(
            @PathVariable("screeningId") UUID screeningId, @RequestBody JsonNode body) {
        return RiskErrorWriter.success(
                comp.dispositionSanctions(screeningId, text(body, "disposition")), null, null, null);
    }

    @PostMapping(
            value = "/v1/compliance/audit-packages",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> prepareAuditPackage(@RequestBody JsonNode body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RiskErrorWriter.success(comp.prepareAuditPackage(text(body, "scope")), null, null, null));
    }

    private static String text(JsonNode body, String field) {
        if (body == null || !body.has(field) || body.get(field).isNull()) {
            return null;
        }
        return body.get(field).asText();
    }
}
