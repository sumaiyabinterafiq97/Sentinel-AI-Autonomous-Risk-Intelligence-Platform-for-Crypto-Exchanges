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
import com.sentinel.identity.infrastructure.IdentityStore.UserRow;

@Service
public class UserService {

    private final IdentityStore store;
    private final AuthzService authz;
    private final IdentityEventPublisher events;

    public UserService(IdentityStore store, AuthzService authz, IdentityEventPublisher events) {
        this.store = store;
        this.authz = authz;
        this.events = events;
    }

    public List<Map<String, Object>> list(String cursor, int limit) {
        authz.require("user:user:read");
        UUID org = requireOrg();
        if (limit < 1 || limit > 200) {
            throw IdentityException.userValidation("limit", "limit must be between 1 and 200");
        }
        List<Map<String, Object>> data = new ArrayList<>();
        for (UserRow user : store.listUsers(org, cursor, limit)) {
            data.add(toApi(user));
        }
        return data;
    }

    public Map<String, Object> get(UUID userId) {
        authz.require("user:user:read");
        UUID org = requireOrg();
        return toApi(store.findUserById(userId, org)
                .orElseThrow(() -> IdentityException.notFound("USER_NOT_FOUND_001", "User not found")));
    }

    @Transactional
    public Map<String, Object> create(String email, List<UUID> roleIds) {
        authz.require("user:user:write");
        UUID org = requireOrg();
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw IdentityException.userValidation("email", "A valid email is required");
        }
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        String display = email.substring(0, email.indexOf('@'));
        UserRow user = new UserRow(id, org, email.trim().toLowerCase(), display, "active", null);
        store.insertUser(user, now, IdentityContext.get().actorId());
        store.insertMembership(org, id, now);
        if (roleIds != null) {
            for (UUID roleId : roleIds) {
                store.findRole(roleId, org)
                        .orElseThrow(() -> IdentityException.userValidation("roleIds", "Role not found in organization"));
                store.insertAssignment(id, roleId, org, now, IdentityContext.get().actorId());
            }
        }
        store.insertAudit(
                org,
                IdentityContext.get().actorId(),
                "USER_CREATED",
                "USER",
                id,
                "SUCCESS",
                IdentityContext.get().correlationId(),
                IdentityContext.get().requestId(),
                "{}",
                now);
        return toApi(user);
    }

    @Transactional
    public Map<String, Object> patch(UUID userId, String displayName, String status) {
        authz.require("user:user:write");
        UUID org = requireOrg();
        UserRow current = store.findUserById(userId, org)
                .orElseThrow(() -> IdentityException.notFound("USER_NOT_FOUND_001", "User not found"));
        Instant now = Instant.now();
        String nextName = displayName == null ? current.displayName() : displayName;
        String nextStatus = status == null ? current.status() : status;
        if (!"active".equals(nextStatus) && !"inactive".equals(nextStatus)) {
            throw IdentityException.userValidation("status", "status must be active or inactive");
        }
        Instant deletedAt = "inactive".equals(nextStatus) ? now : null;
        UserRow updated = new UserRow(current.id(), org, current.email(), nextName, nextStatus, deletedAt);
        store.updateUser(updated, now, IdentityContext.get().actorId());
        List<String> fields = new ArrayList<>();
        if (displayName != null) {
            fields.add("displayName");
        }
        if (status != null) {
            fields.add("status");
        }
        events.userUpdated(userId, org, nextStatus, now, IdentityContext.get().actorId(), fields);
        store.insertAudit(
                org,
                IdentityContext.get().actorId(),
                "USER_UPDATED",
                "USER",
                userId,
                "SUCCESS",
                IdentityContext.get().correlationId(),
                IdentityContext.get().requestId(),
                "{}",
                now);
        return toApi(store.findUserByIdAnyOrg(userId).orElse(updated));
    }

    private Map<String, Object> toApi(UserRow user) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.id().toString());
        data.put("email", user.email());
        data.put("status", user.status());
        data.put("organizationId", user.organizationId().toString());
        return data;
    }

    private UUID requireOrg() {
        UUID org = IdentityContext.get().organizationId();
        if (org == null) {
            throw IdentityException.userValidation("X-Organization-Id", "X-Organization-Id is required");
        }
        return org;
    }
}
