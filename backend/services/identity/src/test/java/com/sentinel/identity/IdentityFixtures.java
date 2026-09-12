package com.sentinel.identity;

import java.time.Instant;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.sentinel.identity.application.AuthzService;
import com.sentinel.identity.infrastructure.IdentityStore;
import com.sentinel.identity.infrastructure.IdentityStore.UserRow;

public final class IdentityFixtures {

    public static final String PASSWORD = "CorrectHorseBattery-9";

    private IdentityFixtures() {}

    public static SeededAdmin seedAdmin(IdentityStore store, AuthzService authz, PasswordEncoder encoder) {
        Instant now = Instant.now();
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        store.insertOrganization(orgId, "Fixture Exchange", "active", now, userId);
        authz.provisionSystemAdminRole(orgId, now);
        store.insertUser(
                new UserRow(userId, orgId, "admin-" + orgId + "@example.com", "Admin", "active", null), now, userId);
        store.upsertPasswordHash(userId, encoder.encode(PASSWORD), now);
        store.insertMembership(orgId, userId, now);
        UUID roleId = authz.systemAdminRoleId(orgId);
        store.insertAssignment(userId, roleId, orgId, now, userId);
        return new SeededAdmin(orgId, userId, "admin-" + orgId + "@example.com", PASSWORD, roleId);
    }

    public record SeededAdmin(UUID organizationId, UUID userId, String email, String password, UUID adminRoleId) {}
}
