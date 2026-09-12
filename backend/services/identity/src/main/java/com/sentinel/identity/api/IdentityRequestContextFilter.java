package com.sentinel.identity.api;

import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.sentinel.common.observability.ObservabilityScaffolding;
import com.sentinel.common.security.SecurityScaffolding;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IdentityRequestContextFilter extends OncePerRequestFilter {

    public static final String ORGANIZATION_HEADER = "X-Organization-Id";
    public static final String CORRELATION_HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        IdentityContext ctx = IdentityContext.get();
        try {
            ctx.setRequestId(parseOrRandom(request.getHeader(SecurityScaffolding.REQUEST_ID_HEADER)));
            ctx.setCorrelationId(parseOrRandom(request.getHeader(CORRELATION_HEADER)));
            ctx.setOrganizationId(parseOrNull(request.getHeader(ORGANIZATION_HEADER)));
            ctx.setClientIp(request.getRemoteAddr());
            response.setHeader(SecurityScaffolding.REQUEST_ID_HEADER, ctx.requestId().toString());
            MDC.put("requestId", ctx.requestId().toString());
            MDC.put(ObservabilityScaffolding.CORRELATION_MDC_KEY, ctx.correlationId().toString());
            MDC.put("domain", "IDENTITY");
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
            IdentityContext.clear();
        }
    }

    static UUID parseOrRandom(String raw) {
        UUID parsed = parseOrNull(raw);
        return parsed == null ? UUID.randomUUID() : parsed;
    }

    static UUID parseOrNull(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
