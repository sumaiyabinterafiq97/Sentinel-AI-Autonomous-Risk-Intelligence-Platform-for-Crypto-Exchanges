package com.sentinel.identity.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sentinel.identity.api.IdentityContext;
import com.sentinel.identity.domain.IdentityException;
import com.sentinel.identity.infrastructure.IdentityStore;
import com.sentinel.identity.infrastructure.IdentityStore.OrgRow;

@Service
public class OrgService {

    private final IdentityStore store;
    private final AuthzService authz;

    public OrgService(IdentityStore store, AuthzService authz) {
        this.store = store;
        this.authz = authz;
    }

    public List<Map<String, Object>> list(String cursor, int limit) {
        authz.require("org:org:read");
        if (limit < 1 || limit > 200) {
            throw IdentityException.orgValidation("limit", "limit must be between 1 and 200");
        }
        UUID userId = IdentityContext.get().actorId();
        List<Map<String, Object>> data = new ArrayList<>();
        for (OrgRow org : store.listOrganizationsForUser(userId, cursor, limit)) {
            data.add(toApi(org));
        }
        return data;
    }

    @Transactional
    public Map<String, Object> create(String name) {
        authz.require("org:org:write");
        if (name == null || name.isBlank()) {
            throw IdentityException.orgValidation("name", "name is required");
        }
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        UUID actor = IdentityContext.get().actorId();
        store.insertOrganization(id, name.trim(), "active", now, actor);
        authz.provisionSystemAdminRole(id, now);
        if (actor != null) {
            store.insertMembership(id, actor, now);
            UUID adminRole = authz.systemAdminRoleId(id);
            if (adminRole != null) {
                store.insertAssignment(actor, adminRole, id, now, actor);
            }
        }
        store.insertAudit(
                id,
                actor,
                "ORG_CREATED",
                "ORGANIZATION",
                id,
                "SUCCESS",
                IdentityContext.get().correlationId(),
                IdentityContext.get().requestId(),
                "{}",
                now);
        return toApi(new OrgRow(id, name.trim(), "active"));
    }

    @Transactional
    public Map<String, Object> patch(UUID orgId, String name, String status) {
        authz.require("org:org:write");
        OrgRow current = store.findOrganization(orgId)
                .orElseThrow(() -> IdentityException.notFound("ORG_NOT_FOUND_001", "Organization not found"));
        UUID actor = IdentityContext.get().actorId();
        if (actor == null || !store.isMember(actor, orgId)) {
            throw IdentityException.forbidden();
        }
        if (IdentityContext.get().principal() != null
                && !IdentityContext.get().principal().organizationId().equals(orgId)) {
            throw IdentityException.forbidden();
        }
        String nextName = name == null ? current.name() : name;
        String nextStatus = status == null ? current.status() : status;
        if (nextName.isBlank()) {
            throw IdentityException.orgValidation("name", "name must not be blank");
        }
        store.updateOrganization(orgId, nextName, nextStatus, Instant.now());
        store.insertAudit(
                orgId,
                actor,
                "ORG_UPDATED",
                "ORGANIZATION",
                orgId,
                "SUCCESS",
                IdentityContext.get().correlationId(),
                IdentityContext.get().requestId(),
                "{}",
                Instant.now());
        return toApi(new OrgRow(orgId, nextName, nextStatus));
    }

    private Map<String, Object> toApi(OrgRow org) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", org.id().toString());
        data.put("name", org.name());
        data.put("status", org.status());
        return data;
    }
}
