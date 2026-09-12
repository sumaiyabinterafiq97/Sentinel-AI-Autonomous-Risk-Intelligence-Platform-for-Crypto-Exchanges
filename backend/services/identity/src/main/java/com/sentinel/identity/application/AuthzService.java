package com.sentinel.identity.application;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sentinel.common.security.AccessPrincipal;
import com.sentinel.identity.api.IdentityContext;
import com.sentinel.identity.domain.IdentityException;
import com.sentinel.identity.infrastructure.IdentityStore;
import com.sentinel.identity.infrastructure.IdentityStore.RoleRow;

@Service
public class AuthzService {

    private final IdentityStore store;

    public AuthzService(IdentityStore store) {
        this.store = store;
    }

    public boolean allows(UUID userId, UUID organizationId, String permission) {
        if (userId == null || organizationId == null || permission == null || permission.isBlank()) {
            return false;
        }
        return store.effectivePermissions(userId, organizationId).contains(permission);
    }

    public void require(String permission) {
        AccessPrincipal principal = IdentityContext.get().principal();
        if (principal == null) {
            throw IdentityException.unauthenticated();
        }
        UUID org = IdentityContext.get().organizationId() == null
                ? principal.organizationId()
                : IdentityContext.get().organizationId();
        if (!org.equals(principal.organizationId())) {
            throw IdentityException.forbidden();
        }
        if (!allows(principal.userId(), org, permission)) {
            throw IdentityException.forbidden();
        }
    }

    public Map<String, Object> evaluate(String action, String resource, UUID subjectId) {
        AccessPrincipal principal = IdentityContext.get().principal();
        if (principal == null) {
            throw IdentityException.unauthenticated();
        }
        UUID org = requireOrg();
        UUID actor = subjectId == null ? principal.userId() : subjectId;
        if (subjectId != null && !subjectId.equals(principal.userId()) && !allows(principal.userId(), org, "authz:role:read")) {
            throw IdentityException.forbidden();
        }
        boolean allowed = allows(actor, org, action);
        store.insertAudit(
                org,
                principal.userId(),
                "AUTHZ_EVALUATE",
                "PERMISSION",
                null,
                allowed ? "SUCCESS" : "DENIED",
                IdentityContext.get().correlationId(),
                IdentityContext.get().requestId(),
                "{\"action\":\"" + action + "\",\"resource\":\"" + resource + "\"}",
                Instant.now());
        return Map.of("allowed", allowed);
    }

    public List<Map<String, Object>> listRoles(String cursor, int limit) {
        require("authz:role:read");
        UUID org = requireOrg();
        if (limit < 1 || limit > 200) {
            throw IdentityException.validation("limit", "limit must be between 1 and 200");
        }
        List<Map<String, Object>> data = new java.util.ArrayList<>();
        for (RoleRow role : store.listRoles(org, cursor, limit)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", role.id().toString());
            item.put("name", role.name());
            item.put("permissions", store.rolePermissionCodes(role.id()));
            data.add(item);
        }
        return data;
    }

    @Transactional
    public Map<String, Object> createRole(String name, List<String> permissionCodes) {
        require("authz:role:write");
        UUID org = requireOrg();
        if (name == null || name.isBlank()) {
            throw IdentityException.validation("name", "name is required");
        }
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        store.insertRole(id, org, name.trim(), false, now, IdentityContext.get().actorId());
        if (permissionCodes != null) {
            for (String code : permissionCodes) {
                UUID permissionId = store.permissionId(code).orElseThrow(
                        () -> IdentityException.validation("permissions", "Unknown permission: " + code));
                store.addRolePermission(id, permissionId);
            }
        }
        store.insertAudit(
                org,
                IdentityContext.get().actorId(),
                "ROLE_CREATED",
                "ROLE",
                id,
                "SUCCESS",
                IdentityContext.get().correlationId(),
                IdentityContext.get().requestId(),
                "{}",
                now);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id.toString());
        data.put("name", name.trim());
        data.put("permissions", store.rolePermissionCodes(id));
        return data;
    }

    @Transactional
    public Map<String, Object> assignRole(UUID userId, UUID roleId) {
        require("authz:assignment:write");
        UUID org = requireOrg();
        if (userId == null || roleId == null) {
            throw IdentityException.validation("userId", "userId and roleId are required");
        }
        store.findUserById(userId, org)
                .orElseThrow(() -> IdentityException.notFound("USER_NOT_FOUND_001", "User not found"));
        store.findRole(roleId, org)
                .orElseThrow(() -> IdentityException.validation("roleId", "Role not found in organization"));
        UUID assignmentId = store.insertAssignment(userId, roleId, org, Instant.now(), IdentityContext.get().actorId());
        store.insertAudit(
                org,
                IdentityContext.get().actorId(),
                "ROLE_ASSIGNED",
                "ROLE_ASSIGNMENT",
                assignmentId,
                "SUCCESS",
                IdentityContext.get().correlationId(),
                IdentityContext.get().requestId(),
                "{}",
                Instant.now());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", assignmentId.toString());
        data.put("userId", userId.toString());
        data.put("roleId", roleId.toString());
        return data;
    }

    @Transactional
    public void provisionSystemAdminRole(UUID organizationId, Instant now) {
        UUID roleId = UUID.randomUUID();
        store.insertRole(roleId, organizationId, "Organization Administrator", true, now, IdentityContext.get().actorId());
        for (String code : store.identityAdminPermissionCodes()) {
            store.permissionId(code).ifPresent(pid -> store.addRolePermission(roleId, pid));
        }
    }

    public UUID systemAdminRoleId(UUID organizationId) {
        return store.listRoles(organizationId, null, 200).stream()
                .filter(r -> r.system() && "Organization Administrator".equals(r.name()))
                .map(RoleRow::id)
                .findFirst()
                .orElse(null);
    }

    private UUID requireOrg() {
        UUID org = IdentityContext.get().organizationId();
        if (org == null && IdentityContext.get().principal() != null) {
            org = IdentityContext.get().principal().organizationId();
        }
        if (org == null) {
            throw IdentityException.validation("X-Organization-Id", "X-Organization-Id is required");
        }
        return org;
    }
}
