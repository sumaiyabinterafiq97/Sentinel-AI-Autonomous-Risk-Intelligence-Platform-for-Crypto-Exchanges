package com.sentinel.platform.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Delegates USER/ORG provision to identity. ADMIN must not insert USER/ORG rows.
 */
public interface IdentityOrchestrator {

    Map<String, Object> createUser(
            String email, UUID organizationId, List<UUID> roleIds, String accessToken, UUID callerOrganizationId);

    Map<String, Object> createOrganization(String name, String accessToken, UUID callerOrganizationId);
}
