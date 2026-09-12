package com.sentinel.platform.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
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
public class CoreRequestContextFilter extends OncePerRequestFilter {

    public static final String CORRELATION_HEADER = "X-Correlation-Id";
    public static final String ORGANIZATION_HEADER = "X-Organization-Id";
    public static final String ACTOR_ID_HEADER = "X-Actor-Id";
    public static final String ACTOR_TYPE_HEADER = "X-Actor-Type";
    public static final String PERMISSIONS_HEADER = "X-Sentinel-Permissions";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        RequestContext ctx = RequestContext.get();
        try {
            ctx.setRequestId(parseUuidOrRandom(request.getHeader(SecurityScaffolding.REQUEST_ID_HEADER)));
            ctx.setCorrelationId(parseUuidOrRandom(request.getHeader(CORRELATION_HEADER)));
            ctx.setOrganizationId(parseUuidOrNull(request.getHeader(ORGANIZATION_HEADER)));
            ctx.setActorId(parseUuidOrNull(request.getHeader(ACTOR_ID_HEADER)));
            String actorType = request.getHeader(ACTOR_TYPE_HEADER);
            if (actorType != null && (actorType.equals("user") || actorType.equals("service") || actorType.equals("system"))) {
                ctx.setActorType(actorType);
            } else if (ctx.actorId() != null) {
                ctx.setActorType("user");
            } else {
                ctx.setActorType("service");
            }
            String auth = request.getHeader("Authorization");
            ctx.setBearerPresent(auth != null && auth.regionMatches(true, 0, "Bearer ", 0, 7) && auth.length() > 7);
            ctx.setPermissions(parsePermissions(request.getHeader(PERMISSIONS_HEADER)));
            response.setHeader(SecurityScaffolding.REQUEST_ID_HEADER, ctx.requestId().toString());
            response.setHeader(CORRELATION_HEADER, ctx.correlationId().toString());
            MDC.put("requestId", ctx.requestId().toString());
            MDC.put(ObservabilityScaffolding.CORRELATION_MDC_KEY, ctx.correlationId().toString());
            MDC.put("domain", "CORE");
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
            RequestContext.clear();
        }
    }

    static UUID parseUuidOrRandom(String raw) {
        UUID parsed = parseUuidOrNull(raw);
        return parsed == null ? UUID.randomUUID() : parsed;
    }

    static UUID parseUuidOrNull(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    static Set<String> parsePermissions(String header) {
        Set<String> set = new LinkedHashSet<>();
        if (header == null || header.isBlank()) {
            return set;
        }
        Arrays.stream(header.split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(set::add);
        return set;
    }
}
