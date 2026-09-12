package com.sentinel.ops.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sentinel.ops.api.RiskContext;
import com.sentinel.ops.domain.InvestException;
import com.sentinel.ops.domain.InvestLifecycle;
import com.sentinel.ops.infrastructure.InvestStore;
import com.sentinel.ops.infrastructure.InvestStore.CaseRow;
import com.sentinel.ops.infrastructure.InvestStore.EvidenceRow;
import com.sentinel.ops.infrastructure.InvestStore.NoteRow;
import com.sentinel.ops.infrastructure.InvestStore.TimelineRow;
import com.sentinel.ops.infrastructure.RiskStore;

@Service
public class InvestService {

    private final InvestStore store;
    private final InvestEventPublisher events;
    private final RiskStore audits;

    public InvestService(InvestStore store, InvestEventPublisher events, RiskStore audits) {
        this.store = store;
        this.events = events;
        this.audits = audits;
    }

    public List<Map<String, Object>> list(String cursor, int limit) {
        int safe = sanitizeLimit(limit);
        List<Map<String, Object>> data = new ArrayList<>();
        for (CaseRow row : store.list(requireOrg(), cursor, safe)) {
            data.add(toCaseApi(row));
        }
        return data;
    }

    public boolean hasMore(String lastId) {
        return store.hasMore(requireOrg(), lastId);
    }

    public Map<String, Object> get(UUID caseId) {
        return toCaseApi(requireCase(caseId));
    }

    @Transactional
    public Map<String, Object> create(String title, UUID sourceAlertId) {
        if (title == null || title.isBlank()) {
            throw InvestException.validation("title", "title is required");
        }
        UUID org = requireOrg();
        if (sourceAlertId != null && store.findBySourceAlert(org, sourceAlertId).isPresent()) {
            throw InvestException.conflict("A case already exists for this sourceAlertId");
        }
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        UUID actor = RiskContext.get().actorId();
        CaseRow row = new CaseRow(
                id,
                org,
                InvestLifecycle.OPEN,
                title.trim(),
                null,
                null,
                sourceAlertId,
                now,
                null,
                null,
                null,
                now,
                now,
                actor);
        persistNewCase(row, actor, now, "manual");
        return toCaseApi(row);
    }

    @Transactional
    public Map<String, Object> patch(UUID caseId, String title, String status) {
        CaseRow current = requireCase(caseId);
        if (title == null && status == null) {
            throw InvestException.validation("body", "title or status is required");
        }
        String nextStatus = current.status();
        String nextTitle = current.title();
        if (status != null) {
            InvestLifecycle.requirePatchable(current.status(), status);
            nextStatus = status.trim().toLowerCase();
        }
        if (title != null) {
            if (title.isBlank()) {
                throw InvestException.validation("title", "title must not be blank");
            }
            if (InvestLifecycle.CLOSED.equals(current.status())) {
                throw InvestException.conflict("Closed cases cannot be updated");
            }
            nextTitle = title.trim();
        }
        Instant now = Instant.now();
        CaseRow updated = copy(current, nextStatus, nextTitle, current.assignedTo(), current.closedAt(), current.closedBy(), current.outcome(), now);
        store.updateCase(updated);
        store.insertTimeline(UUID.randomUUID(), caseId, "CASE_UPDATED", "Case patched", now, RiskContext.get().actorId());
        events.caseUpdated(caseId, updated.organizationId(), nextStatus, now, RiskContext.get().actorId(), "patched");
        audit("CASE_UPDATED", caseId, "{}");
        return toCaseApi(updated);
    }

    @Transactional
    public Map<String, Object> assign(UUID caseId, UUID assigneeId) {
        if (assigneeId == null) {
            throw InvestException.validation("assigneeId", "assigneeId is required");
        }
        CaseRow current = requireCase(caseId);
        InvestLifecycle.requireAssignable(current.status());
        if (assigneeId.equals(current.assignedTo())) {
            return toCaseApi(current);
        }
        Instant now = Instant.now();
        CaseRow updated = copy(
                current,
                current.status(),
                current.title(),
                assigneeId,
                current.closedAt(),
                current.closedBy(),
                current.outcome(),
                now);
        store.updateCase(updated);
        store.insertTimeline(UUID.randomUUID(), caseId, "CASE_ASSIGNED", "Assigned to " + assigneeId, now, RiskContext.get().actorId());
        events.caseAssigned(caseId, updated.organizationId(), assigneeId, RiskContext.get().actorId(), now);
        audit("CASE_ASSIGNED", caseId, "{\"assigneeId\":\"" + assigneeId + "\"}");
        return toCaseApi(updated);
    }

    @Transactional
    public Map<String, Object> close(UUID caseId, String resolutionSummary) {
        if (resolutionSummary == null || resolutionSummary.isBlank()) {
            throw InvestException.validation("resolutionSummary", "resolutionSummary is required");
        }
        CaseRow current = requireCase(caseId);
        if (InvestLifecycle.CLOSED.equals(current.status())) {
            if (resolutionSummary.equals(current.outcome())) {
                return toCaseApi(current);
            }
            throw InvestException.conflict("Case is already closed");
        }
        InvestLifecycle.requireCloseable(current.status());
        Instant now = Instant.now();
        UUID actor = RiskContext.get().actorId();
        CaseRow updated = copy(current, InvestLifecycle.CLOSED, current.title(), current.assignedTo(), now, actor, resolutionSummary, now);
        store.updateCase(updated);
        store.insertTimeline(UUID.randomUUID(), caseId, "CASE_CLOSED", resolutionSummary, now, actor);
        events.caseClosed(caseId, updated.organizationId(), now, actor, resolutionSummary);
        audit("CASE_CLOSED", caseId, "{}");
        return toCaseApi(updated);
    }

    @Transactional
    public Map<String, Object> attachEvidence(UUID caseId, String evidenceRef, String description) {
        if (evidenceRef == null || evidenceRef.isBlank()) {
            throw InvestException.validation("evidenceRef", "evidenceRef is required");
        }
        CaseRow current = requireCase(caseId);
        if (InvestLifecycle.CLOSED.equals(current.status())) {
            throw InvestException.conflict("Cannot attach evidence to a closed case");
        }
        Optional<EvidenceRow> existing = store.findEvidenceByRef(caseId, evidenceRef);
        if (existing.isPresent()) {
            throw InvestException.conflict("Evidence reference already attached");
        }
        Instant now = Instant.now();
        UUID evidenceId = UUID.randomUUID();
        UUID actor = RiskContext.get().actorId();
        if (actor == null) {
            throw InvestException.unauthenticated();
        }
        String metadata = description == null ? "{}" : "{\"description\":\"" + description.replace("\"", "") + "\"}";
        store.insertEvidence(evidenceId, caseId, "attachment", "evidenceRef", evidenceRef, metadata, actor, now);
        store.insertTimeline(UUID.randomUUID(), caseId, "EVIDENCE_ATTACHED", evidenceRef, now, actor);
        events.evidenceAttached(
                caseId, current.organizationId(), evidenceId, "attachment", "evidenceRef", evidenceRef, actor, now);
        audit("EVIDENCE_ATTACHED", caseId, "{\"evidenceId\":\"" + evidenceId + "\"}");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", evidenceId.toString());
        data.put("caseId", caseId.toString());
        data.put("evidenceRef", evidenceRef);
        return data;
    }

    public List<Map<String, Object>> timeline(UUID caseId) {
        requireCase(caseId);
        List<Map<String, Object>> data = new ArrayList<>();
        for (TimelineRow row : store.listTimeline(caseId)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.id().toString());
            item.put("eventType", row.eventType());
            item.put("occurredAt", row.occurredAt().toString());
            item.put("summary", row.description());
            data.add(item);
        }
        return data;
    }

    public List<Map<String, Object>> listNotes(UUID caseId, String cursor, int limit) {
        requireCase(caseId);
        int safe = sanitizeLimit(limit);
        List<Map<String, Object>> data = new ArrayList<>();
        for (NoteRow row : store.listNotes(caseId, cursor, safe)) {
            data.add(toNoteApi(row));
        }
        return data;
    }

    @Transactional
    public Map<String, Object> createNote(UUID caseId, String content) {
        if (content == null || content.isBlank()) {
            throw InvestException.validation("content", "content is required");
        }
        requireCase(caseId);
        UUID author = RiskContext.get().actorId();
        if (author == null) {
            throw InvestException.unauthenticated();
        }
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        store.insertNote(id, caseId, author, content, now);
        store.insertTimeline(UUID.randomUUID(), caseId, "NOTE_ADDED", "Note added", now, author);
        audit("CASE_NOTE_CREATED", caseId, "{\"noteId\":\"" + id + "\"}");
        return toNoteApi(new NoteRow(id, caseId, author, content, now));
    }

    public void consumeUpstream(Map<String, Object> envelope) {
        String type = String.valueOf(envelope.get("eventType"));
        if ("AlertCreated".equals(type)) {
            consumeAlertCreated(envelope);
        } else if ("RiskCalculated".equals(type)) {
            consumeRiskCalculated(envelope);
        }
    }

    private void consumeAlertCreated(Map<String, Object> envelope) {
        UUID org;
        UUID eventId;
        try {
            org = uuid(envelope.get("organizationId"));
            eventId = uuid(envelope.get("eventId"));
        } catch (InvestException ex) {
            return;
        }
        applyEnvelope(envelope, org);
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = envelope.get("payload") instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
        UUID alertId;
        try {
            alertId = uuid(payload.get("alertId"));
        } catch (InvestException ex) {
            return;
        }
        Optional<CaseRow> existing = store.findBySourceAlert(org, alertId);
        if (existing.isPresent()) {
            if (store.timelineHasEventId(existing.get().id(), eventId)) {
                return;
            }
            store.insertTimeline(
                    UUID.randomUUID(),
                    existing.get().id(),
                    "ALERT_CREATED",
                    "Alert context eventId=" + eventId,
                    Instant.now(),
                    null);
            return;
        }
        Instant now = Instant.now();
        UUID caseId = UUID.randomUUID();
        String title = payload.get("title") == null ? "Investigation" : String.valueOf(payload.get("title"));
        CaseRow row = new CaseRow(
                caseId,
                org,
                InvestLifecycle.OPEN,
                title,
                null,
                null,
                alertId,
                now,
                null,
                null,
                null,
                now,
                now,
                null);
        persistNewCase(row, null, now, "alert-context eventId=" + eventId);
        UUID assessmentId = tryUuid(payload.get("riskAssessmentId"));
        if (assessmentId != null) {
            store.assessmentSummary(org, assessmentId)
                    .ifPresent(summary -> store.insertTimeline(
                            UUID.randomUUID(),
                            caseId,
                            "RISK_CONTEXT",
                            "RiskCalculated context " + summary + " eventId=" + eventId,
                            now,
                            null));
        }
    }

    private void consumeRiskCalculated(Map<String, Object> envelope) {
        UUID org;
        UUID eventId;
        try {
            org = uuid(envelope.get("organizationId"));
            eventId = uuid(envelope.get("eventId"));
        } catch (InvestException ex) {
            return;
        }
        applyEnvelope(envelope, org);
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = envelope.get("payload") instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
        UUID assessmentId = tryUuid(payload.get("assessmentId"));
        if (assessmentId == null) {
            return;
        }
        Instant now = Instant.now();
        for (UUID alertId : store.alertIdsForAssessment(org, assessmentId)) {
            store.findBySourceAlert(org, alertId).ifPresent(row -> {
                if (!store.timelineHasEventId(row.id(), eventId)) {
                    store.insertTimeline(
                            UUID.randomUUID(),
                            row.id(),
                            "RISK_CALCULATED",
                            "RiskCalculated eventId=" + eventId + " assessment=" + assessmentId,
                            now,
                            null);
                }
            });
        }
    }

    private void persistNewCase(CaseRow row, UUID actor, Instant now, String reason) {
        store.insertCase(row);
        store.insertTimeline(UUID.randomUUID(), row.id(), "CASE_CREATED", reason, now, actor);
        events.caseCreated(
                row.id(),
                row.organizationId(),
                row.status(),
                row.title(),
                row.sourceAlertId(),
                row.priority(),
                now,
                actor);
        audit("CASE_CREATED", row.id(), "{}");
    }

    private CaseRow requireCase(UUID caseId) {
        return store.find(caseId, requireOrg()).orElseThrow(() -> InvestException.notFound("Case not found"));
    }

    private static CaseRow copy(
            CaseRow current,
            String status,
            String title,
            UUID assignedTo,
            Instant closedAt,
            UUID closedBy,
            String outcome,
            Instant updatedAt) {
        return new CaseRow(
                current.id(),
                current.organizationId(),
                status,
                title,
                current.priority(),
                assignedTo,
                current.sourceAlertId(),
                current.openedAt(),
                closedAt,
                closedBy,
                outcome,
                current.createdAt(),
                updatedAt,
                current.createdBy());
    }

    private Map<String, Object> toCaseApi(CaseRow row) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", row.id().toString());
        data.put("organizationId", row.organizationId().toString());
        data.put("status", row.status());
        data.put("title", row.title());
        data.put("sourceAlertId", row.sourceAlertId() == null ? null : row.sourceAlertId().toString());
        return data;
    }

    private Map<String, Object> toNoteApi(NoteRow row) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", row.id().toString());
        data.put("caseId", row.caseId().toString());
        data.put("content", row.content());
        data.put("createdAt", row.createdAt().toString());
        return data;
    }

    private UUID requireOrg() {
        UUID org = RiskContext.get().organizationId();
        if (org == null) {
            throw InvestException.validation("X-Organization-Id", "X-Organization-Id is required");
        }
        return org;
    }

    private int sanitizeLimit(int limit) {
        if (limit < 1 || limit > 200) {
            throw InvestException.validation("limit", "limit must be between 1 and 200");
        }
        return limit;
    }

    private void audit(String action, UUID resourceId, String metadata) {
        RiskContext ctx = RiskContext.get();
        audits.insertAudit(
                requireOrg(),
                ctx.actorId(),
                action,
                "INVEST",
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
        UUID corr = tryUuid(envelope.get("correlationId"));
        if (corr != null) {
            ctx.setCorrelationId(corr);
        }
        UUID eventId = tryUuid(envelope.get("eventId"));
        if (eventId != null) {
            ctx.setRequestId(eventId);
        }
        if (ctx.actorType() == null) {
            ctx.setActorType("service");
        }
    }

    private static UUID uuid(Object raw) {
        if (raw == null) {
            throw InvestException.validation("id", "id is required");
        }
        return UUID.fromString(String.valueOf(raw));
    }

    private static UUID tryUuid(Object raw) {
        if (raw == null || String.valueOf(raw).isBlank() || "null".equals(String.valueOf(raw))) {
            return null;
        }
        try {
            return UUID.fromString(String.valueOf(raw));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
