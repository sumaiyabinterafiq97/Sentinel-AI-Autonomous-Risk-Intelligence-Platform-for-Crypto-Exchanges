package com.sentinel.identity.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Process liveness for M0. AUTH/AUTHZ/USER/ORG APIs are milestone M2. */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "sentinel-identity", "milestone", "M2");
    }
}
