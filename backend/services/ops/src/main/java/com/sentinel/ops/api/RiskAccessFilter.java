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
import com.sentinel.ops.domain.RiskException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RiskAccessFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final RiskErrorWriter errors;
    private final AccessTokenCodec tokens;

    public RiskAccessFilter(ObjectMapper objectMapper, RiskErrorWriter errors, AccessTokenCodec tokens) {
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
        if (!path.startsWith("/v1/risk/")) {
            filterChain.doFilter(request, response);
            return;
        }
        RiskContext ctx = RiskContext.get();
        String orgHeader = request.getHeader(RiskRequestContextFilter.ORGANIZATION_HEADER);
        if (orgHeader == null || orgHeader.isBlank()) {
            write(response, RiskException.validation("X-Organization-Id", "X-Organization-Id is required"));
            return;
        }
        if (ctx.organizationId() == null) {
            write(response, RiskException.validation("X-Organization-Id", "X-Organization-Id must be a UUID"));
            return;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7) || header.length() <= 7) {
            write(response, RiskException.unauthenticated());
            return;
        }
        Optional<AccessPrincipal> principal = tokens.verify(header.substring(7).trim());
        if (principal.isEmpty()) {
            write(response, RiskException.unauthenticated());
            return;
        }
        AccessPrincipal access = principal.get();
        if (!access.organizationId().equals(ctx.organizationId())) {
            write(response, RiskException.forbidden());
            return;
        }
        ctx.setPrincipal(access);
        ctx.setActorId(access.userId());
        ctx.setActorType("user");
        String required = requiredPermission(request.getMethod(), path);
        if (required != null && !access.hasPermission(required)) {
            write(response, RiskException.forbidden());
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static boolean isPublic(String path) {
        return "/health".equals(path) || path.startsWith("/actuator/health");
    }

    static String requiredPermission(String method, String path) {
        if ("POST".equals(method) && "/v1/risk/transactions/ingest".equals(path)) {
            return "risk:ingest:write";
        }
        if ("GET".equals(method) && "/v1/risk/assessments".equals(path)) {
            return "risk:assessment:read";
        }
        if ("GET".equals(method) && path.startsWith("/v1/risk/assessments/")) {
            return "risk:assessment:read";
        }
        if ("GET".equals(method) && "/v1/risk/rules".equals(path)) {
            return "risk:rule:read";
        }
        if ("POST".equals(method) && "/v1/risk/rules".equals(path)) {
            return "risk:rule:write";
        }
        if ("PATCH".equals(method) && path.startsWith("/v1/risk/rules/")) {
            return "risk:rule:write";
        }
        if ("POST".equals(method) && "/v1/risk/evaluate".equals(path)) {
            return "risk:evaluate:write";
        }
        return "risk:assessment:read";
    }

    private void write(HttpServletResponse response, RiskException ex) throws IOException {
        response.setStatus(ex.httpStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = errors.body(ex);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
