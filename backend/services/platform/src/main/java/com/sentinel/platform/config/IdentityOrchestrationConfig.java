package com.sentinel.platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.platform.application.HttpIdentityOrchestrator;
import com.sentinel.platform.application.IdentityOrchestrator;

@Configuration
public class IdentityOrchestrationConfig {

    @Bean
    IdentityOrchestrator identityOrchestrator(
            @Value("${sentinel.identity.base-url:}") String baseUrl, ObjectMapper objectMapper) {
        return new HttpIdentityOrchestrator(baseUrl, objectMapper);
    }
}
