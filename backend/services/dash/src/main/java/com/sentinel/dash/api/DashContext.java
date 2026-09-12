package com.sentinel.dash.api;

import java.util.UUID;
import com.sentinel.common.security.AccessPrincipal;

public final class DashContext {

    private static final ThreadLocal<DashContext> HOLDER = ThreadLocal.withInitial(DashContext::new);

    private UUID requestId;
    private UUID correlationId;
    private UUID organizationId;
    private AccessPrincipal principal;

    private DashContext() {}

    public static DashContext get() {
        return HOLDER.get();
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
}
