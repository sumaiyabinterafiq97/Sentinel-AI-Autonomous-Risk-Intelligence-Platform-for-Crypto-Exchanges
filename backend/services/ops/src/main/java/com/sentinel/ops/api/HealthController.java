package com.sentinel.ops.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Process liveness. M4–M7 (RISK/ALERT/INVEST/COMP) APIs are on this deployable. */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "sentinel-ops", "milestone", "M7");
    }
}
