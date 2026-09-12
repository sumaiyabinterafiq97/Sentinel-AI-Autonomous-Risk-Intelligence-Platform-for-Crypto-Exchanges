package com.sentinel.dash.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.sentinel.common.security.AccessTokenCodec;

@Configuration
public class DashSecurityConfig {

    @Bean
    AccessTokenCodec accessTokenCodec(@Value("${sentinel.security.token-hmac-key}") String key) {
        return new AccessTokenCodec(key);
    }
}
