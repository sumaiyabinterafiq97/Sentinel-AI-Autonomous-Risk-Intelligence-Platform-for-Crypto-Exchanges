package com.sentinel.common.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Local HMAC session tokens for M2. This is not OIDC and not a vendor identity provider.
 * Tokens are JWT-shaped (header.payload.signature) signed with a shared HMAC key.
 */
public final class AccessTokenCodec {

    public static final String SIMULATION_NOTICE = "local-hmac-jwt-not-oidc";

    private final byte[] hmacKey;

    public AccessTokenCodec(String hmacKey) {
        if (hmacKey == null || hmacKey.isBlank()) {
            throw new IllegalArgumentException("token HMAC key is required");
        }
        this.hmacKey = hmacKey.getBytes(StandardCharsets.UTF_8);
    }

    public String issue(UUID userId, UUID organizationId, UUID sessionId, Set<String> permissions, Instant expiresAt) {
        String header = b64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        StringBuilder payload = new StringBuilder();
        payload.append('{');
        payload.append("\"sub\":\"").append(userId).append('"');
        payload.append(",\"org\":\"").append(organizationId).append('"');
        payload.append(",\"sid\":\"").append(sessionId).append('"');
        payload.append(",\"exp\":").append(expiresAt.getEpochSecond());
        payload.append(",\"perms\":[");
        boolean first = true;
        List<String> sorted = new ArrayList<>(permissions);
        sorted.sort(String::compareTo);
        for (String p : sorted) {
            if (!first) {
                payload.append(',');
            }
            first = false;
            payload.append('"').append(p.replace("\"", "")).append('"');
        }
        payload.append("]}");
        String payloadEnc = b64(payload.toString());
        String signingInput = header + "." + payloadEnc;
        return signingInput + "." + b64Raw(hmac(signingInput));
    }

    public Optional<AccessPrincipal> verify(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }
        String signingInput = parts[0] + "." + parts[1];
        if (!constantTimeEquals(b64Raw(hmac(signingInput)), parts[2])) {
            return Optional.empty();
        }
        String json = new String(Base64.getUrlDecoder().decode(pad(parts[1])), StandardCharsets.UTF_8);
        try {
            Map<String, String> simple = parseSimple(json);
            Instant exp = Instant.ofEpochSecond(Long.parseLong(simple.get("exp")));
            if (Instant.now().isAfter(exp)) {
                return Optional.empty();
            }
            return Optional.of(new AccessPrincipal(
                    UUID.fromString(simple.get("sub")),
                    UUID.fromString(simple.get("org")),
                    UUID.fromString(simple.get("sid")),
                    parsePerms(json),
                    exp));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    private static Map<String, String> parseSimple(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String key : List.of("sub", "org", "sid", "exp")) {
            String needle = "\"" + key + "\":";
            int i = json.indexOf(needle);
            if (i < 0) {
                throw new IllegalArgumentException(key);
            }
            int start = i + needle.length();
            if (json.charAt(start) == '"') {
                int end = json.indexOf('"', start + 1);
                map.put(key, json.substring(start + 1, end));
            } else {
                int end = start;
                while (end < json.length() && Character.isDigit(json.charAt(end))) {
                    end++;
                }
                map.put(key, json.substring(start, end));
            }
        }
        return map;
    }

    private static Set<String> parsePerms(String json) {
        int i = json.indexOf("\"perms\":[");
        if (i < 0) {
            return Set.of();
        }
        int start = json.indexOf('[', i);
        int end = json.indexOf(']', start);
        String inner = json.substring(start + 1, end).trim();
        if (inner.isEmpty()) {
            return Set.of();
        }
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        for (String part : inner.split(",")) {
            String p = part.trim().replace("\"", "");
            if (!p.isEmpty()) {
                set.add(p);
            }
        }
        return set;
    }

    private byte[] hmac(String input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacKey, "HmacSHA256"));
            return mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC unavailable", e);
        }
    }

    private static String b64(String value) {
        return b64Raw(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String b64Raw(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String pad(String b64) {
        int mod = b64.length() % 4;
        if (mod == 0) {
            return b64;
        }
        return b64 + "====".substring(mod);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r |= a.charAt(i) ^ b.charAt(i);
        }
        return r == 0;
    }
}
