package com.sentinel.identity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.sentinel.common.security.AccessTokenCodec;

@Configuration
public class IdentitySecurityConfig {

    @Bean
    AccessTokenCodec accessTokenCodec(@Value("${sentinel.security.token-hmac-key}") String key) {
        return new AccessTokenCodec(key);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
