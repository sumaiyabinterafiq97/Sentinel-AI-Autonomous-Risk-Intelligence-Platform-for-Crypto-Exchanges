package com.sentinel.platform.domain;

import java.util.regex.Pattern;

public final class CoreKeys {

    public static final String MAINTENANCE_MODE = "maintenance.mode";
    public static final String PRODUCER = "sentinel-platform";
    public static final String EVENT_SCHEMA_VERSION = "1.0";

    private static final Pattern KEY = Pattern.compile("^[A-Za-z][A-Za-z0-9._:-]{0,127}$");

    private CoreKeys() {}

    public static void requireConfigKey(String key) {
        if (key == null || !KEY.matcher(key).matches()) {
            throw CoreException.validation("configKey", "Invalid configuration key");
        }
    }

    public static void requireFlagKey(String key) {
        if (key == null || !KEY.matcher(key).matches()) {
            throw CoreException.validation("flagKey", "Invalid feature flag key");
        }
    }
}
