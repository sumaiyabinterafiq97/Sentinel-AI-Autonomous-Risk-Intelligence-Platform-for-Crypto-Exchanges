package com.sentinel.ops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * sentinel-ops deployable (RISK, ALERT, INVEST, COMP).
 * M4: RISK APIs only. ALERT/INVEST/COMP remain unimplemented.
 */
@SpringBootApplication
public class OpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpsApplication.class, args);
    }
}
