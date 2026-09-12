package com.sentinel.ops.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.sentinel.ops.OpsPostgresIT;

@SpringBootTest
class RiskApiContractSurfaceIT extends OpsPostgresIT {

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mappings;

    @Test
    void opsExposesHealthRiskAlertInvestAndCompInventoryPaths() {
        Set<String> documented = Set.of(
                "GET /health",
                "POST /v1/risk/transactions/ingest",
                "GET /v1/risk/assessments",
                "GET /v1/risk/assessments/{assessmentId}",
                "GET /v1/risk/rules",
                "POST /v1/risk/rules",
                "PATCH /v1/risk/rules/{ruleId}",
                "POST /v1/risk/evaluate",
                "GET /v1/alerts",
                "GET /v1/alerts/{alertId}",
                "PATCH /v1/alerts/{alertId}",
                "POST /v1/alerts/{alertId}/assign",
                "POST /v1/alerts/{alertId}/close",
                "PATCH /v1/alerts/{alertId}/priority",
                "POST /v1/alerts/{alertId}/investigation-link",
                "GET /v1/investigations/cases",
                "POST /v1/investigations/cases",
                "GET /v1/investigations/cases/{caseId}",
                "PATCH /v1/investigations/cases/{caseId}",
                "POST /v1/investigations/cases/{caseId}/close",
                "POST /v1/investigations/cases/{caseId}/assign",
                "POST /v1/investigations/cases/{caseId}/evidence",
                "GET /v1/investigations/cases/{caseId}/timeline",
                "GET /v1/investigations/cases/{caseId}/notes",
                "POST /v1/investigations/cases/{caseId}/notes",
                "POST /v1/compliance/kyc-reviews",
                "PATCH /v1/compliance/kyc-reviews/{reviewId}",
                "POST /v1/compliance/aml-reviews",
                "POST /v1/compliance/travel-rule/validations",
                "POST /v1/compliance/sanctions-screenings",
                "PATCH /v1/compliance/sanctions-screenings/{screeningId}",
                "POST /v1/compliance/audit-packages");
        Set<String> implemented = mappings.getHandlerMethods().keySet().stream()
                .flatMap(info -> info.getPatternValues().stream()
                        .flatMap(pattern -> info.getMethodsCondition().getMethods().stream()
                                .map(method -> method.name() + " " + pattern)))
                .filter(s -> s.contains("/v1/") || s.equals("GET /health"))
                .collect(Collectors.toSet());
        assertEquals(documented, implemented);
        assertTrue(implemented.stream()
                .noneMatch(s -> s.contains("/alerts/ingest") || s.contains("/ai/") || s.contains("/workspace")));
    }
}
