package com.sentinel.identity.api;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.common.security.AccessPrincipal;
import com.sentinel.common.security.AccessTokenCodec;
import com.sentinel.identity.domain.IdentityException;
import com.sentinel.identity.infrastructure.IdentityStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class IdentityAccessFilter extends OncePerRequestFilter {

    private final AccessTokenCodec tokens;
    private final IdentityStore store;
    private final ObjectMapper objectMapper;
    private final IdentityErrorWriter errors;

    public IdentityAccessFilter(
            AccessTokenCodec tokens, IdentityStore store, ObjectMapper objectMapper, IdentityErrorWriter errors) {
        this.tokens = tokens;
        this.store = store;
        this.objectMapper = objectMapper;
        this.errors = errors;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (isPublic(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.regionMatches(true, 0, "Bearer ", 0, 7) || auth.length() <= 7) {
            write(response, IdentityException.unauthenticated());
            return;
        }
        Optional<AccessPrincipal> verified = tokens.verify(auth.substring(7).trim());
        if (verified.isEmpty()) {
            write(response, IdentityException.unauthenticated());
            return;
        }
        AccessPrincipal token = verified.get();
        var session = store.findSession(token.sessionId());
        if (session.isEmpty()
                || !"active".equals(session.get().status())
                || Instant.now().isAfter(session.get().expiresAt())) {
            write(response, IdentityException.unauthenticated());
            return;
        }
        var live = new AccessPrincipal(
                token.userId(),
                token.organizationId(),
                token.sessionId(),
                store.effectivePermissions(token.userId(), token.organizationId()),
                token.expiresAt());
        IdentityContext ctx = IdentityContext.get();
        ctx.setPrincipal(live);
        if (ctx.organizationId() == null) {
            ctx.setOrganizationId(live.organizationId());
        } else if (!ctx.organizationId().equals(live.organizationId())
                && !path.startsWith("/v1/organizations")) {
            write(response, IdentityException.forbidden());
            return;
        }
        if (request.getHeader(IdentityRequestContextFilter.ORGANIZATION_HEADER) != null
                && ctx.organizationId() == null) {
            write(response, IdentityException.validation("X-Organization-Id", "X-Organization-Id must be a UUID"));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static boolean isPublic(String path) {
        return "/health".equals(path)
                || "/v1/auth/login".equals(path)
                || "/v1/auth/refresh".equals(path)
                || path.startsWith("/actuator/health");
    }

    private void write(HttpServletResponse response, IdentityException ex) throws IOException {
        response.setStatus(ex.httpStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errors.body(ex));
    }
}
