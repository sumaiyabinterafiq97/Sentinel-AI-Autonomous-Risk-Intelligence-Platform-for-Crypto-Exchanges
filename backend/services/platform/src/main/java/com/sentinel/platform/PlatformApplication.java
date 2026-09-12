package com.sentinel.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * sentinel-platform deployable (CORE + optional ADMIN co-location).
 * M1: CORE platform health, configuration, feature flags, and audit.
 */
@SpringBootApplication
public class PlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }
}
