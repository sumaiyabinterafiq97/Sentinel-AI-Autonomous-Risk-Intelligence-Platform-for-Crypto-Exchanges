package com.sentinel.platform.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Process liveness retained from M0. Contract health is GET /v1/platform/health. */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "sentinel-platform", "milestone", "M10");
    }
}
