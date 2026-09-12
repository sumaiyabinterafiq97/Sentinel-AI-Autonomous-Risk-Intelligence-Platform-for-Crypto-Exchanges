package com.sentinel.identity.infrastructure;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.sentinel.identity.IdentityPostgresIT;

@SpringBootTest
class AdminPermissionSeedIT extends IdentityPostgresIT {

    @Autowired
    private IdentityStore store;

    @Test
    void adminPermissionsAreSeededAndGrantedToIdentityAdmins() {
        assertTrue(store.permissionCodes().contains("admin:settings:write"));
        assertTrue(store.permissionCodes().contains("admin:integration:write"));
        assertTrue(store.permissionCodes().contains("admin:user:provision"));
        assertTrue(store.permissionCodes().contains("admin:org:provision"));
        assertTrue(store.permissionCodes().contains("admin:audit:read"));
        assertTrue(store.identityAdminPermissionCodes().contains("admin:settings:write"));
    }
}
