package com.sentinel.dash.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Process liveness. DASH BFF and SSE are milestone M9. */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "sentinel-dash", "milestone", "M9");
    }
}
