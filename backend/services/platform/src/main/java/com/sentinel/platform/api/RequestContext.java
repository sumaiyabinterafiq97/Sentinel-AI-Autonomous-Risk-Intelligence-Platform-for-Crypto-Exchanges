package com.sentinel.platform.api;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Request-scoped CORE audit/tenant context (CORE-FR-012 / CORE-FR-013).
 */
public final class RequestContext {

    private static final ThreadLocal<RequestContext> HOLDER = new ThreadLocal<>();

    private UUID requestId;
    private UUID correlationId;
    private UUID organizationId;
    private UUID actorId;
    private String actorType = "service";
    private boolean bearerPresent;
    private String accessToken;
    private Set<String> permissions = Set.of();

    public static RequestContext get() {
        RequestContext ctx = HOLDER.get();
        if (ctx == null) {
            ctx = new RequestContext();
            HOLDER.set(ctx);
        }
        return ctx;
    }

    public static void clear() {
        HOLDER.remove();
    }

    public UUID requestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public UUID correlationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    public UUID organizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public UUID actorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public String actorType() {
        return actorType;
    }

    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    public boolean bearerPresent() {
        return bearerPresent;
    }

    public void setBearerPresent(boolean bearerPresent) {
        this.bearerPresent = bearerPresent;
    }

    public String accessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Set<String> permissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions == null ? Set.of() : Collections.unmodifiableSet(permissions);
    }
}
