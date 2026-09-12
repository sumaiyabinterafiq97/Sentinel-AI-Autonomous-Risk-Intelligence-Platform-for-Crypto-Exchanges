package com.sentinel.ops.api;

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
import com.sentinel.ops.domain.CompException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class CompAccessFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final CompErrorWriter errors;
    private final AccessTokenCodec tokens;

    public CompAccessFilter(ObjectMapper objectMapper, CompErrorWriter errors, AccessTokenCodec tokens) {
        this.objectMapper = objectMapper;
        this.errors = errors;
        this.tokens = tokens;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (isPublic(path) || !path.startsWith("/v1/compliance")) {
            filterChain.doFilter(request, response);
            return;
        }
        RiskContext ctx = RiskContext.get();
        String orgHeader = request.getHeader(RiskRequestContextFilter.ORGANIZATION_HEADER);
        if (orgHeader == null || orgHeader.isBlank()) {
            write(response, CompException.validation("X-Organization-Id", "X-Organization-Id is required"));
            return;
        }
        if (ctx.organizationId() == null) {
            write(response, CompException.validation("X-Organization-Id", "X-Organization-Id must be a UUID"));
            return;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7) || header.length() <= 7) {
            write(response, CompException.unauthenticated());
            return;
        }
        Optional<AccessPrincipal> principal = tokens.verify(header.substring(7).trim());
        if (principal.isEmpty()) {
            write(response, CompException.unauthenticated());
            return;
        }
        AccessPrincipal access = principal.get();
        if (!access.organizationId().equals(ctx.organizationId())) {
            write(response, CompException.forbidden());
            return;
        }
        ctx.setPrincipal(access);
        ctx.setActorId(access.userId());
        ctx.setActorType("user");
        String required = requiredPermission(request.getMethod(), path);
        if (required != null && !access.hasPermission(required)) {
            write(response, CompException.forbidden());
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static boolean isPublic(String path) {
        return "/health".equals(path) || path.startsWith("/actuator/health");
    }

    static String requiredPermission(String method, String path) {
        if ("POST".equals(method) && "/v1/compliance/kyc-reviews".equals(path)) {
            return "comp:kyc:write";
        }
        if ("PATCH".equals(method) && path.startsWith("/v1/compliance/kyc-reviews/")) {
            return "comp:kyc:approve";
        }
        if ("POST".equals(method) && "/v1/compliance/aml-reviews".equals(path)) {
            return "comp:aml:write";
        }
        if ("POST".equals(method) && "/v1/compliance/travel-rule/validations".equals(path)) {
            return "comp:travelrule:write";
        }
        if ("POST".equals(method) && "/v1/compliance/sanctions-screenings".equals(path)) {
            return "comp:sanctions:write";
        }
        if ("PATCH".equals(method) && path.startsWith("/v1/compliance/sanctions-screenings/")) {
            return "comp:sanctions:approve";
        }
        if ("POST".equals(method) && "/v1/compliance/audit-packages".equals(path)) {
            return "comp:audit:write";
        }
        return "comp:kyc:write";
    }

    private void write(HttpServletResponse response, CompException ex) throws java.io.IOException {
        response.setStatus(ex.httpStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errors.body(ex));
    }
}
