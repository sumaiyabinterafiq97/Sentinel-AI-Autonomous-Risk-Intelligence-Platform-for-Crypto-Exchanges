package com.sentinel.platform.api;

import java.io.IOException;
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
import com.sentinel.platform.domain.CoreException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * M2 CORE access: validates local HMAC access tokens issued by AUTH.
 * Permission authority is the signed token (AUTHZ-resolved at login/refresh), not X-Sentinel-Permissions.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AccessBoundaryFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final ApiErrorWriter errorWriter;
    private final AccessTokenCodec tokens;

    public AccessBoundaryFilter(ObjectMapper objectMapper, ApiErrorWriter errorWriter, AccessTokenCodec tokens) {
        this.objectMapper = objectMapper;
        this.errorWriter = errorWriter;
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
        if (!path.startsWith("/v1/platform/") && !path.startsWith("/v1/admin")) {
            filterChain.doFilter(request, response);
            return;
        }
        RequestContext ctx = RequestContext.get();
        String orgHeader = request.getHeader(CoreRequestContextFilter.ORGANIZATION_HEADER);
        if (orgHeader == null || orgHeader.isBlank()) {
            write(response, CoreException.validation("X-Organization-Id", "X-Organization-Id is required"));
            return;
        }
        if (ctx.organizationId() == null) {
            write(response, CoreException.validation("X-Organization-Id", "X-Organization-Id must be a UUID"));
            return;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7) || header.length() <= 7) {
            write(response, CoreException.unauthenticated());
            return;
        }
        Optional<AccessPrincipal> principal = tokens.verify(header.substring(7).trim());
        if (principal.isEmpty()) {
            write(response, CoreException.unauthenticated());
            return;
        }
        AccessPrincipal access = principal.get();
        if (!access.organizationId().equals(ctx.organizationId())) {
            write(response, CoreException.forbidden());
            return;
        }
        ctx.setActorId(access.userId());
        ctx.setActorType("user");
        ctx.setAccessToken(header.substring(7).trim());
        ctx.setPermissions(access.permissions());
        String required = requiredPermission(request.getMethod(), path);
        if (required != null && !access.hasPermission(required)) {
            write(response, CoreException.forbidden());
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static boolean isPublic(String path) {
        return "/health".equals(path)
                || "/v1/platform/health".equals(path)
                || path.startsWith("/actuator/health");
    }

    private static String requiredPermission(String method, String path) {
        if ("GET".equals(method) && "/v1/platform/status".equals(path)) {
            return "platform:status:read";
        }
        if ("GET".equals(method) && "/v1/platform/config".equals(path)) {
            return "platform:config:read";
        }
        if ("PATCH".equals(method) && "/v1/platform/config".equals(path)) {
            return "platform:config:write";
        }
        if ("GET".equals(method) && "/v1/platform/feature-flags".equals(path)) {
            return "platform:flags:read";
        }
        if ("PATCH".equals(method) && path.startsWith("/v1/platform/feature-flags/")) {
            return "platform:flags:write";
        }
        if (path.equals("/v1/admin/settings")) {
            return "admin:settings:write";
        }
        if (path.equals("/v1/admin/integrations")) {
            return "admin:integration:write";
        }
        if (path.equals("/v1/admin/users/provision") && "POST".equals(method)) {
            return "admin:user:provision";
        }
        if (path.equals("/v1/admin/organizations/provision") && "POST".equals(method)) {
            return "admin:org:provision";
        }
        if (path.equals("/v1/admin/audit-records") && "GET".equals(method)) {
            return "admin:audit:read";
        }
        return null;
    }

    private void write(HttpServletResponse response, CoreException ex) throws IOException {
        response.setStatus(ex.httpStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = errorWriter.body(ex);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
