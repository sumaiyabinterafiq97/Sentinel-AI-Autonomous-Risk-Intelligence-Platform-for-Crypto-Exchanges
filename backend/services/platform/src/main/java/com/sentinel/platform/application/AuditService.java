package com.sentinel.platform.application;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.platform.api.RequestContext;
import com.sentinel.platform.infrastructure.AuditRecordRepository;

@Service
public class AuditService {

    private final AuditRecordRepository audit;
    private final ObjectMapper objectMapper;

    public AuditService(AuditRecordRepository audit, ObjectMapper objectMapper) {
        this.audit = audit;
        this.objectMapper = objectMapper;
    }

    public void record(String action, String resourceType, UUID resourceId, String outcome, Map<String, ?> metadata) {
        RequestContext ctx = RequestContext.get();
        UUID orgId = ctx.organizationId();
        if (orgId == null) {
            orgId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        }
        String json;
        try {
            json = objectMapper.writeValueAsString(metadata == null ? Map.of() : metadata);
        } catch (JsonProcessingException e) {
            json = "{}";
        }
        audit.insert(
                UUID.randomUUID(),
                orgId,
                ctx.actorId(),
                ctx.actorType() == null ? "system" : ctx.actorType(),
                action,
                resourceType,
                resourceId,
                outcome,
                ctx.correlationId(),
                ctx.requestId(),
                json,
                Instant.now());
    }
}
