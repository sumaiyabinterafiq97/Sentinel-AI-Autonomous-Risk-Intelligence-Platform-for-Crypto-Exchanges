package com.sentinel.dash.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Enumeration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.dash.domain.DashException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class BffUpstreamFilter extends OncePerRequestFilter {

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper objectMapper;
    private final DashErrorWriter errors;
    private final String identityBase;
    private final String opsBase;
    private final String aiBase;
    private final String platformBase;

    public BffUpstreamFilter(
            ObjectMapper objectMapper,
            DashErrorWriter errors,
            @Value("${sentinel.bff.identity-base-url:}") String identityBase,
            @Value("${sentinel.bff.ops-base-url:}") String opsBase,
            @Value("${sentinel.bff.ai-base-url:}") String aiBase,
            @Value("${sentinel.bff.platform-base-url:}") String platformBase) {
        this.objectMapper = objectMapper;
        this.errors = errors;
        this.identityBase = identityBase;
        this.opsBase = opsBase;
        this.aiBase = aiBase;
        this.platformBase = platformBase;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!path.startsWith("/v1/") || path.startsWith("/v1/workspace")) {
            filterChain.doFilter(request, response);
            return;
        }
        String base = upstream(path);
        if (base == null || base.isBlank()) {
            write(response, DashException.dependency("Upstream domain service is unavailable"));
            return;
        }
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(trim(base) + path + query(request)))
                    .timeout(Duration.ofSeconds(10));
            Enumeration<String> names = request.getHeaderNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                if ("host".equalsIgnoreCase(name) || "content-length".equalsIgnoreCase(name)) {
                    continue;
                }
                builder.header(name, request.getHeader(name));
            }
            byte[] body = request.getInputStream().readAllBytes();
            builder.method(request.getMethod(), body.length == 0
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofByteArray(body));
            HttpResponse<byte[]> upstream = http.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
            response.setStatus(upstream.statusCode());
            upstream.headers().map().forEach((key, values) -> {
                if (!"transfer-encoding".equalsIgnoreCase(key)) {
                    values.forEach(v -> response.addHeader(key, v));
                }
            });
            response.getOutputStream().write(upstream.body());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            write(response, DashException.dependency("Upstream domain service is unavailable"));
        } catch (Exception ex) {
            write(response, DashException.dependency("Upstream domain service is unavailable"));
        }
    }

    private String upstream(String path) {
        if (path.startsWith("/v1/reports")) {
            return "";
        }
        if (path.startsWith("/v1/admin")) {
            return platformBase;
        }
        if (path.startsWith("/v1/auth")
                || path.startsWith("/v1/users")
                || path.startsWith("/v1/organizations")
                || path.startsWith("/v1/authz")) {
            return identityBase;
        }
        if (path.startsWith("/v1/ai")) {
            return aiBase;
        }
        if (path.startsWith("/v1/risk")
                || path.startsWith("/v1/alerts")
                || path.startsWith("/v1/investigations")
                || path.startsWith("/v1/compliance")) {
            return opsBase;
        }
        return "";
    }

    private static String trim(String base) {
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    private static String query(HttpServletRequest request) {
        return request.getQueryString() == null ? "" : "?" + request.getQueryString();
    }

    private void write(HttpServletResponse response, DashException ex) throws IOException {
        response.setStatus(ex.httpStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errors.body(ex));
    }
}
