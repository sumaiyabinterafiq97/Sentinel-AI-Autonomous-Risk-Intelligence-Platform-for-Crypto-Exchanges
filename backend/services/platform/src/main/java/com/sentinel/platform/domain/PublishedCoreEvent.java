package com.sentinel.platform.domain;

import java.util.Map;

public record PublishedCoreEvent(Map<String, Object> envelope, String publicationOutcome) {}
