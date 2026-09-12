package com.sentinel.platform.api;

import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.sentinel.platform.application.FeatureFlagService;
import com.sentinel.platform.application.PlatformConfigService;
import com.sentinel.platform.application.PlatformHealthService;
import com.sentinel.platform.domain.HealthStatus;

@RestController
public class PlatformController {

    private final PlatformHealthService health;
    private final PlatformConfigService configs;
    private final FeatureFlagService flags;

    public PlatformController(
            PlatformHealthService health, PlatformConfigService configs, FeatureFlagService flags) {
        this.health = health;
        this.configs = configs;
        this.flags = flags;
    }

    /** API-CORE-001 getPlatformHealth CORE-FR-004 */
    @GetMapping(value = "/v1/platform/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getPlatformHealth() {
        HealthStatus status = health.evaluateHealth(RequestContext.get().organizationId());
        return ApiExceptionHandler.success(Map.of("status", status.name()), null, null, null);
    }

    /** API-CORE-002 getPlatformStatus CORE-FR-024 */
    @GetMapping(value = "/v1/platform/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getPlatformStatus() {
        return ApiExceptionHandler.success(health.statusPayload(RequestContext.get().organizationId()), null, null, null);
    }

    /** API-CORE-003 getPlatformConfig CORE-FR-008 */
    @GetMapping(value = "/v1/platform/config", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getPlatformConfig() {
        return ApiExceptionHandler.success(configs.getActiveConfig(RequestContext.get().organizationId()), null, null, null);
    }

    /** API-CORE-004 patchPlatformConfig CORE-FR-007 */
    @PatchMapping(value = "/v1/platform/config", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> patchPlatformConfig(@RequestBody JsonNode body) {
        return ApiExceptionHandler.success(configs.patchConfig(RequestContext.get().organizationId(), body), null, null, null);
    }

    /** API-CORE-005 listFeatureFlags CORE-FR-010 */
    @GetMapping(value = "/v1/platform/feature-flags", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> listFeatureFlags(
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", required = false, defaultValue = "50") int limit) {
        List<Map<String, Object>> data = flags.listResolved(RequestContext.get().organizationId(), cursor, limit);
        String last = data.isEmpty() ? null : String.valueOf(data.get(data.size() - 1).get("key"));
        boolean hasMore = flags.hasMore(RequestContext.get().organizationId(), last);
        return ApiExceptionHandler.success(data, limit, last, hasMore);
    }

    /** API-CORE-006 patchFeatureFlag CORE-FR-009 */
    @PatchMapping(
            value = "/v1/platform/feature-flags/{flagKey}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> patchFeatureFlag(@PathVariable("flagKey") String flagKey, @RequestBody JsonNode body) {
        return ApiExceptionHandler.success(
                flags.patchFlag(RequestContext.get().organizationId(), flagKey, body), null, null, null);
    }
}
