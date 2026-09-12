package com.sentinel.platform.application;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.platform.api.RequestContext;
import com.sentinel.platform.domain.CoreException;

public class HttpIdentityOrchestrator implements IdentityOrchestrator {

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public HttpIdentityOrchestrator(String baseUrl, ObjectMapper objectMapper) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> createUser(
            String email, UUID organizationId, List<UUID> roleIds, String accessToken, UUID callerOrganizationId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", email);
        if (roleIds != null && !roleIds.isEmpty()) {
            List<String> ids = new ArrayList<>();
            roleIds.forEach(id -> ids.add(id.toString()));
            body.put("roleIds", ids);
        }
        return post("/v1/users", body, accessToken, callerOrganizationId);
    }

    @Override
    public Map<String, Object> createOrganization(String name, String accessToken, UUID callerOrganizationId) {
        return post("/v1/organizations", Map.of("name", name), accessToken, callerOrganizationId);
    }

    private Map<String, Object> post(String path, Map<String, Object> body, String accessToken, UUID callerOrganizationId) {
        if (baseUrl.isBlank()) {
            throw CoreException.adminDependency("Identity service is unavailable");
        }
        if (accessToken == null || accessToken.isBlank()) {
            throw CoreException.unauthenticated();
        }
        try {
            RequestContext ctx = RequestContext.get();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(trim(baseUrl) + path))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("X-Organization-Id", callerOrganizationId.toString())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header(
                            "X-Correlation-Id",
                            ctx.correlationId() == null ? UUID.randomUUID().toString() : ctx.correlationId().toString())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status == 401) {
                throw CoreException.unauthenticated();
            }
            if (status == 403) {
                throw CoreException.forbidden();
            }
            if (status >= 400 && status < 500) {
                throw CoreException.adminValidation("body", "Identity rejected the provision request");
            }
            if (status >= 500) {
                throw CoreException.adminDependency("Identity service is unavailable");
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode data = root.has("data") ? root.get("data") : root;
            @SuppressWarnings("unchecked")
            Map<String, Object> mapped = objectMapper.convertValue(data, Map.class);
            return mapped;
        } catch (CoreException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw CoreException.adminDependency("Identity service is unavailable");
        } catch (Exception ex) {
            throw CoreException.adminDependency("Identity service is unavailable");
        }
    }

    private static String trim(String base) {
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }
}
