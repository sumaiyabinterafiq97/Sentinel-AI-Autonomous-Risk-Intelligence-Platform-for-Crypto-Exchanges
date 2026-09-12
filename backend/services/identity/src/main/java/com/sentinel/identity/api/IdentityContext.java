package com.sentinel.identity.api;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import com.sentinel.common.security.AccessPrincipal;

public final class IdentityContext {
    private static final ThreadLocal<IdentityContext> HOLDER = new ThreadLocal<>();

    private UUID requestId;
    private UUID correlationId;
    private UUID organizationId;
    private AccessPrincipal principal;
    private String clientIp;

    public static IdentityContext get() {
        IdentityContext ctx = HOLDER.get();
        if (ctx == null) {
            ctx = new IdentityContext();
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

    public AccessPrincipal principal() {
        return principal;
    }

    public void setPrincipal(AccessPrincipal principal) {
        this.principal = principal;
    }

    public String clientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public UUID actorId() {
        return principal == null ? null : principal.userId();
    }

    public Set<String> permissions() {
        return principal == null ? Set.of() : Collections.unmodifiableSet(principal.permissions());
    }
}
