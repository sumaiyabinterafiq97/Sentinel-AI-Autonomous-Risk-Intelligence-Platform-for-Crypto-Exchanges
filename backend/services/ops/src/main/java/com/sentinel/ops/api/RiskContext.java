package com.sentinel.ops.api;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import com.sentinel.common.security.AccessPrincipal;

public final class RiskContext {
    private static final ThreadLocal<RiskContext> HOLDER = new ThreadLocal<>();

    private UUID requestId;
    private UUID correlationId;
    private UUID organizationId;
    private UUID actorId;
    private String actorType;
    private AccessPrincipal principal;
    private Set<String> permissions = Set.of();

    public static RiskContext get() {
        RiskContext ctx = HOLDER.get();
        if (ctx == null) {
            ctx = new RiskContext();
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

    public AccessPrincipal principal() {
        return principal;
    }

    public void setPrincipal(AccessPrincipal principal) {
        this.principal = principal;
        if (principal != null) {
            this.permissions = principal.permissions();
        }
    }

    public Set<String> permissions() {
        return permissions == null ? Set.of() : Collections.unmodifiableSet(permissions);
    }
}
