package com.sentinel.ops.api;

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
import com.sentinel.ops.domain.AlertException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AlertAccessFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final AlertErrorWriter errors;
    private final AccessTokenCodec tokens;

    public AlertAccessFilter(ObjectMapper objectMapper, AlertErrorWriter errors, AccessTokenCodec tokens) {
        this.objectMapper = objectMapper;
        this.errors = errors;
        this.tokens = tokens;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (isPublic(path) || !path.startsWith("/v1/alerts")) {
            filterChain.doFilter(request, response);
            return;
        }
        RiskContext ctx = RiskContext.get();
        String orgHeader = request.getHeader(RiskRequestContextFilter.ORGANIZATION_HEADER);
        if (orgHeader == null || orgHeader.isBlank()) {
            write(response, AlertException.validation("X-Organization-Id", "X-Organization-Id is required"));
            return;
        }
        if (ctx.organizationId() == null) {
            write(response, AlertException.validation("X-Organization-Id", "X-Organization-Id must be a UUID"));
            return;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7) || header.length() <= 7) {
            write(response, AlertException.unauthenticated());
            return;
        }
        Optional<AccessPrincipal> principal = tokens.verify(header.substring(7).trim());
        if (principal.isEmpty()) {
            write(response, AlertException.unauthenticated());
            return;
        }
        AccessPrincipal access = principal.get();
        if (!access.organizationId().equals(ctx.organizationId())) {
            write(response, AlertException.forbidden());
            return;
        }
        ctx.setPrincipal(access);
        ctx.setActorId(access.userId());
        ctx.setActorType("user");
        String required = requiredPermission(request.getMethod(), path);
        if (required != null && !access.hasPermission(required)) {
            write(response, AlertException.forbidden());
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static boolean isPublic(String path) {
        return "/health".equals(path) || path.startsWith("/actuator/health");
    }

    static String requiredPermission(String method, String path) {
        if ("PATCH".equals(method) && path.endsWith("/priority")) {
            return "alert:alert:priority";
        }
        if ("POST".equals(method) && path.endsWith("/assign")) {
            return "alert:alert:assign";
        }
        if ("POST".equals(method) && path.endsWith("/close")) {
            return "alert:alert:close";
        }
        if ("POST".equals(method) && path.endsWith("/investigation-link")) {
            return "alert:alert:write";
        }
        if ("PATCH".equals(method) && path.startsWith("/v1/alerts/")) {
            return "alert:alert:write";
        }
        return "alert:alert:read";
    }

    private void write(HttpServletResponse response, AlertException ex) throws IOException {
        response.setStatus(ex.httpStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = errors.body(ex);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
