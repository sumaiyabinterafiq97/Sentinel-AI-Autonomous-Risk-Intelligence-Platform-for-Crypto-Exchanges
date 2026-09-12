package com.sentinel.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * sentinel-identity deployable (AUTH, AUTHZ, USER, ORG).
 * M2: AUTH, AUTHZ, USER, and ORG identity foundation.
 */
@SpringBootApplication
public class IdentityApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityApplication.class, args);
    }
}
