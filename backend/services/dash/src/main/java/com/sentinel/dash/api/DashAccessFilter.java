package com.sentinel.dash.api;

import java.io.IOException;
import java.util.Optional;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.common.security.AccessPrincipal;
import com.sentinel.common.security.AccessTokenCodec;
import com.sentinel.dash.domain.DashException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class DashAccessFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final DashErrorWriter errors;
    private final AccessTokenCodec tokens;

    public DashAccessFilter(ObjectMapper objectMapper, DashErrorWriter errors, AccessTokenCodec tokens) {
        this.objectMapper = objectMapper;
        this.errors = errors;
        this.tokens = tokens;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (isPublic(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!path.startsWith("/v1/")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (isAnonymousAuth(path, request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        DashContext ctx = DashContext.get();
        String orgHeader = request.getHeader(DashRequestContextFilter.ORGANIZATION_HEADER);
        if (orgHeader == null || orgHeader.isBlank()) {
            write(response, DashException.validation("X-Organization-Id", "X-Organization-Id is required"));
            return;
        }
        if (ctx.organizationId() == null) {
            write(response, DashException.validation("X-Organization-Id", "X-Organization-Id must be a UUID"));
            return;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7) || header.length() <= 7) {
            write(response, DashException.unauthenticated());
            return;
        }
        Optional<AccessPrincipal> principal = tokens.verify(header.substring(7).trim());
        if (principal.isEmpty()) {
            write(response, DashException.unauthenticated());
            return;
        }
        AccessPrincipal access = principal.get();
        if (!access.organizationId().equals(ctx.organizationId())) {
            write(response, DashException.forbidden());
            return;
        }
        ctx.setPrincipal(access);
        String required = requiredPermission(request.getMethod(), path);
        if (required != null && !access.hasPermission(required)) {
            write(response, DashException.forbidden());
            return;
        }
        filterChain.doFilter(request, response);
    }

    static boolean isPublic(String path) {
        return "/health".equals(path) || path.startsWith("/actuator/health");
    }

    static boolean isAnonymousAuth(String path, String method) {
        return ("POST".equals(method) && ("/v1/auth/login".equals(path) || "/v1/auth/refresh".equals(path)));
    }

    static String requiredPermission(String method, String path) {
        if (!path.startsWith("/v1/workspace")) {
            return null;
        }
        if (path.startsWith("/v1/workspace/queues/")) {
            return "dash:queue:read";
        }
        if ("GET".equals(method) && path.startsWith("/v1/workspace/widgets") && !path.contains("/interactions")) {
            return "dash:widget:read";
        }
        return "dash:workspace:read";
    }

    private void write(HttpServletResponse response, DashException ex) throws IOException {
        response.setStatus(ex.httpStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errors.body(ex));
    }
}
