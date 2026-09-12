package com.sentinel.ops.application;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sentinel.ops.api.RiskContext;
import com.sentinel.ops.domain.CompException;
import com.sentinel.ops.infrastructure.CompStore;
import com.sentinel.ops.infrastructure.CompStore.KycRow;
import com.sentinel.ops.infrastructure.CompStore.SanctionsRow;
import com.sentinel.ops.infrastructure.RiskStore;

@Service
public class CompService {

    public static final String HIT_PREFIX = "hit:";

    private final CompStore store;
    private final CompEventPublisher events;
    private final RiskStore audits;

    public CompService(CompStore store, CompEventPublisher events, RiskStore audits) {
        this.store = store;
        this.events = events;
        this.audits = audits;
    }

    @Transactional
    public Map<String, Object> startKyc(String subjectRef) {
        if (subjectRef == null || subjectRef.isBlank()) {
            throw CompException.validation("subjectRef", "subjectRef is required");
        }
        UUID org = requireOrg();
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        UUID userId = CompStore.subjectToUserId(subjectRef.trim());
        store.insertKyc(
                new KycRow(id, org, userId, "in_review", null, null, null, now),
                RiskContext.get().correlationId(),
                now);
        store.insertRecord(org, "kyc_review", id, "in_review", now);
        audit("KYC_STARTED", id, "{}");
        return kycApi(id, "in_review", userId.toString());
    }

    @Transactional
    public Map<String, Object> completeKyc(UUID reviewId, String decision) {
        if (decision == null || decision.isBlank()) {
            throw CompException.validation("decision", "decision is required");
        }
        String next = decision.trim().toLowerCase();
        if (!List.of("approved", "rejected", "pending").contains(next)) {
            throw CompException.validation("decision", "decision must be approved, rejected, or pending");
        }
        UUID org = requireOrg();
        KycRow current = store.findKyc(reviewId, org).orElseThrow(() -> CompException.notFound("KYC review not found"));
        if (List.of("approved", "rejected").contains(current.status())) {
            if (next.equals(current.outcome()) || next.equals(current.status())) {
                return kycApi(current.id(), current.status(), current.userId().toString());
            }
            throw CompException.conflict("KYC review already completed");
        }
        UUID actor = requireActor();
        Instant now = Instant.now();
        String status = "pending".equals(next) ? "info_requested" : next;
        String outcome = next;
        store.updateKyc(
                new KycRow(current.id(), org, current.userId(), status, outcome, actor, now, current.createdAt()), now);
        store.insertRecord(org, "kyc_review", current.id(), status, now);
        if ("approved".equals(next) || "rejected".equals(next)) {
            events.complianceReviewed(
                    current.id(), org, "kyc", outcome, actor, now, current.userId().toString());
        }
        audit("KYC_COMPLETED", current.id(), "{\"decision\":\"" + next + "\"}");
        return kycApi(current.id(), status, current.userId().toString());
    }

    @Transactional
    public Map<String, Object> startAml(String subjectRef) {
        if (subjectRef == null || subjectRef.isBlank()) {
            throw CompException.validation("subjectRef", "subjectRef is required");
        }
        UUID org = requireOrg();
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        store.insertAml(id, org, subjectRef.trim(), null, "in_review", now, RiskContext.get().actorId());
        store.insertRecord(org, "aml_review", id, "in_review", now);
        audit("AML_STARTED", id, "{}");
        return Map.of("id", id.toString(), "status", "in_review");
    }

    @Transactional
    public Map<String, Object> validateTravelRule(String transactionRef) {
        if (transactionRef == null || transactionRef.isBlank()) {
            throw CompException.validation("transactionRef", "transactionRef is required");
        }
        UUID org = requireOrg();
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        Map<String, Object> result = Map.of("mode", "recorded");
        store.insertTravel(id, org, transactionRef.trim(), "recorded", "{\"mode\":\"recorded\"}", now, requireActor());
        store.insertRecord(org, "travel_rule", id, "recorded", now);
        events.travelRuleValidated(id, org, transactionRef.trim(), "recorded", result, now);
        audit("TRAVEL_RULE_RECORDED", id, "{}");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id.toString());
        data.put("valid", true);
        return data;
    }

    @Transactional
    public Map<String, Object> createSanctions(String subjectRef) {
        if (subjectRef == null || subjectRef.isBlank()) {
            throw CompException.validation("subjectRef", "subjectRef is required");
        }
        UUID org = requireOrg();
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        String trimmed = subjectRef.trim();
        boolean possibleHit = trimmed.regionMatches(true, 0, HIT_PREFIX, 0, HIT_PREFIX.length());
        String matchStatus = possibleHit ? "possible_match" : "no_match";
        store.insertSanctions(new SanctionsRow(id, org, trimmed, matchStatus, null, now), null, null);
        store.insertRecord(org, "sanctions_screening", id, matchStatus, now);
        if (possibleHit) {
            events.sanctionsHitDetected(id, org, trimmed, matchStatus, null, now);
        }
        audit("SANCTIONS_SCREENED", id, "{\"matchStatus\":\"" + matchStatus + "\"}");
        return Map.of("id", id.toString(), "status", matchStatus);
    }

    @Transactional
    public Map<String, Object> dispositionSanctions(UUID screeningId, String disposition) {
        if (disposition == null || disposition.isBlank()) {
            throw CompException.validation("disposition", "disposition is required");
        }
        UUID org = requireOrg();
        SanctionsRow current =
                store.findSanctions(screeningId, org).orElseThrow(() -> CompException.notFound("Screening not found"));
        if (!"possible_match".equals(current.matchStatus())) {
            throw CompException.validation("disposition", "Only possible matches can be disposed");
        }
        if (current.disposition() != null) {
            if (current.disposition().equals(disposition)) {
                return Map.of("id", current.id().toString(), "status", current.matchStatus());
            }
            throw CompException.conflict("Screening already disposed");
        }
        UUID actor = requireActor();
        Instant now = Instant.now();
        store.updateSanctionsDisposition(current.id(), org, disposition.trim(), actor, now);
        store.insertRecord(org, "sanctions_screening", current.id(), "disposed", now);
        events.complianceReviewed(
                current.id(), org, "sanctions", disposition.trim(), actor, now, current.subjectRef());
        audit("SANCTIONS_DISPOSED", current.id(), "{}");
        return Map.of("id", current.id().toString(), "status", current.matchStatus());
    }

    @Transactional
    public Map<String, Object> prepareAuditPackage(String scope) {
        if (scope == null || scope.isBlank()) {
            throw CompException.validation("scope", "scope is required");
        }
        UUID org = requireOrg();
        UUID actor = requireActor();
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        store.insertAuditPackage(id, org, scope.trim(), "ready", "[\"" + scope.replace("\"", "") + "\"]", actor, now);
        store.insertRecord(org, "audit_package", id, "ready", now);
        events.auditPackagePrepared(id, org, scope.trim(), "ready", List.of(scope.trim()), now, actor);
        audit("AUDIT_PACKAGE_PREPARED", id, "{}");
        return Map.of("id", id.toString(), "status", "ready");
    }

    public void consumeUpstream(Map<String, Object> envelope) {
        String type = String.valueOf(envelope.get("eventType"));
        if (!List.of("CaseClosed", "CaseUpdated", "RiskCalculated", "UserUpdated").contains(type)) {
            return;
        }
        UUID org;
        UUID eventId;
        try {
            org = UUID.fromString(String.valueOf(envelope.get("organizationId")));
            eventId = UUID.fromString(String.valueOf(envelope.get("eventId")));
        } catch (RuntimeException ex) {
            return;
        }
        applyEnvelope(envelope, org);
        if (store.recordExists(org, "upstream_" + type, eventId)) {
            return;
        }
        store.insertRecord(org, "upstream_" + type, eventId, "recorded", Instant.now());
        audit("COMP_CONTEXT_RECORDED", eventId, "{\"eventType\":\"" + type + "\"}");
    }

    private Map<String, Object> kycApi(UUID id, String status, String subjectRef) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id.toString());
        data.put("status", status);
        data.put("subjectRef", subjectRef);
        return data;
    }

    private UUID requireOrg() {
        UUID org = RiskContext.get().organizationId();
        if (org == null) {
            throw CompException.validation("X-Organization-Id", "X-Organization-Id is required");
        }
        return org;
    }

    private UUID requireActor() {
        UUID actor = RiskContext.get().actorId();
        if (actor == null) {
            throw CompException.unauthenticated();
        }
        return actor;
    }

    private void audit(String action, UUID resourceId, String metadata) {
        RiskContext ctx = RiskContext.get();
        audits.insertAudit(
                requireOrg(),
                ctx.actorId(),
                action,
                "COMP",
                resourceId,
                "SUCCESS",
                ctx.correlationId(),
                ctx.requestId(),
                metadata,
                Instant.now());
    }

    private void applyEnvelope(Map<String, Object> envelope, UUID organizationId) {
        RiskContext ctx = RiskContext.get();
        ctx.setOrganizationId(organizationId);
        try {
            if (envelope.get("correlationId") != null) {
                ctx.setCorrelationId(UUID.fromString(String.valueOf(envelope.get("correlationId"))));
            }
            if (envelope.get("eventId") != null) {
                ctx.setRequestId(UUID.fromString(String.valueOf(envelope.get("eventId"))));
            }
        } catch (IllegalArgumentException ignored) {
            ctx.setCorrelationId(UUID.randomUUID());
        }
        if (ctx.actorType() == null) {
            ctx.setActorType("service");
        }
    }
}
