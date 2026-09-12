package com.sentinel.identity.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sentinel.common.security.AccessPrincipal;
import com.sentinel.common.security.AccessTokenCodec;
import com.sentinel.identity.api.IdentityContext;
import com.sentinel.identity.domain.IdentityException;
import com.sentinel.identity.infrastructure.IdentityStore;
import com.sentinel.identity.infrastructure.IdentityStore.SessionRow;
import com.sentinel.identity.infrastructure.IdentityStore.UserRow;

@Service
public class AuthService {

    private final IdentityStore store;
    private final PasswordEncoder passwords;
    private final AccessTokenCodec tokens;
    private final IdentityEventPublisher events;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public AuthService(
            IdentityStore store,
            PasswordEncoder passwords,
            AccessTokenCodec tokens,
            IdentityEventPublisher events,
            @Value("${sentinel.security.access-token-ttl-seconds}") long accessTtlSeconds,
            @Value("${sentinel.security.refresh-token-ttl-seconds}") long refreshTtlSeconds) {
        this.store = store;
        this.passwords = passwords;
        this.tokens = tokens;
        this.events = events;
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    @Transactional
    public Map<String, Object> login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw IdentityException.validation("email", "email and password are required");
        }
        List<UserRow> matches = store.findUsersByEmail(email.trim());
        if (matches.size() > 1) {
            throw IdentityException.validation("email", "X-Organization-Id is required when the email exists in multiple organizations");
        }
        if (matches.isEmpty()) {
            recordAuthEvent(null, null, "LOGIN_FAILURE", "{\"reason\":\"unknown\"}");
            throw IdentityException.invalidCredentials();
        }
        UserRow user = matches.get(0);
        if (!"active".equals(user.status())) {
            recordAuthEvent(user.id(), user.organizationId(), "LOGIN_FAILURE", "{\"reason\":\"inactive\"}");
            throw IdentityException.invalidCredentials();
        }
        String hash = store.findPasswordHash(user.id()).orElse(null);
        if (hash == null || !passwords.matches(password, hash)) {
            recordAuthEvent(user.id(), user.organizationId(), "LOGIN_FAILURE", "{\"reason\":\"invalid\"}");
            throw IdentityException.invalidCredentials();
        }
        Instant now = Instant.now();
        UUID sessionId = UUID.randomUUID();
        Instant accessExp = now.plusSeconds(accessTtlSeconds);
        Instant refreshExp = now.plusSeconds(refreshTtlSeconds);
        store.insertSession(sessionId, user.id(), user.organizationId(), "active", accessExp, true, now);
        String refreshPlain = UUID.randomUUID() + "." + UUID.randomUUID();
        store.insertRefresh(UUID.randomUUID(), sessionId, sha256(refreshPlain), refreshExp);
        Set<String> perms = store.effectivePermissions(user.id(), user.organizationId());
        String access = tokens.issue(user.id(), user.organizationId(), sessionId, perms, accessExp);
        IdentityContext.get().setPrincipal(new AccessPrincipal(user.id(), user.organizationId(), sessionId, perms, accessExp));
        IdentityContext.get().setOrganizationId(user.organizationId());
        events.userLoggedIn(user.id(), sessionId, user.organizationId(), now);
        recordAuthEvent(user.id(), user.organizationId(), "LOGIN_SUCCESS", "{}");
        store.insertAudit(
                user.organizationId(),
                user.id(),
                "LOGIN",
                "SESSION",
                sessionId,
                "SUCCESS",
                IdentityContext.get().correlationId(),
                IdentityContext.get().requestId(),
                "{}",
                now);
        return tokensBody(access, refreshPlain, accessTtlSeconds);
    }

    @Transactional
    public Map<String, Object> logout() {
        AccessPrincipal principal = requirePrincipal();
        Instant now = Instant.now();
        store.updateSessionStatus(principal.sessionId(), "revoked", now);
        store.revokeRefreshForSession(principal.sessionId(), now);
        events.sessionExpired(principal.sessionId(), principal.userId(), principal.organizationId(), now, "logout");
        recordAuthEvent(principal.userId(), principal.organizationId(), "LOGOUT", "{}");
        return Map.of();
    }

    @Transactional
    public Map<String, Object> refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw IdentityException.validation("refreshToken", "refreshToken is required");
        }
        var row = store.findRefreshByHash(sha256(refreshToken)).orElseThrow(IdentityException::unauthenticated);
        if (row.revokedAt() != null || Instant.now().isAfter(row.expiresAt())) {
            throw IdentityException.unauthenticated();
        }
        SessionRow session = store.findSession(row.sessionId()).orElseThrow(IdentityException::unauthenticated);
        if (!"active".equals(session.status()) || Instant.now().isAfter(session.expiresAt())) {
            throw IdentityException.unauthenticated();
        }
        Instant now = Instant.now();
        store.revokeRefresh(row.id(), now);
        Instant accessExp = now.plusSeconds(accessTtlSeconds);
        Instant refreshExp = now.plusSeconds(refreshTtlSeconds);
        store.updateSessionExpiry(session.id(), accessExp, now);
        String refreshPlain = UUID.randomUUID() + "." + UUID.randomUUID();
        store.insertRefresh(UUID.randomUUID(), session.id(), sha256(refreshPlain), refreshExp);
        Set<String> perms = store.effectivePermissions(session.userId(), session.organizationId());
        String access = tokens.issue(session.userId(), session.organizationId(), session.id(), perms, accessExp);
        return tokensBody(access, refreshPlain, accessTtlSeconds);
    }

    @Transactional
    public Map<String, Object> session() {
        AccessPrincipal principal = requirePrincipal();
        SessionRow session = store.findSession(principal.sessionId()).orElseThrow(IdentityException::unauthenticated);
        if (!"active".equals(session.status()) || Instant.now().isAfter(session.expiresAt())) {
            throw IdentityException.unauthenticated();
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", session.userId().toString());
        data.put("organizationId", session.organizationId().toString());
        data.put("roles", store.roleNames(session.userId(), session.organizationId()));
        return data;
    }

    @Transactional
    public Map<String, Object> verifyMfa(String code) {
        AccessPrincipal principal = requirePrincipal();
        if (code == null || !code.matches("^[0-9]{6}$")) {
            throw IdentityException.validation("code", "MFA code must be a 6-digit value");
        }
        // Simulation MFA: any well-formed 6-digit code verifies the current session. Not TOTP/OIDC.
        store.markMfaVerified(principal.sessionId(), Instant.now());
        return Map.of("verified", true);
    }

    public void setPasswordForUser(UUID userId, String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 8) {
            throw IdentityException.validation("password", "password must be at least 8 characters");
        }
        store.upsertPasswordHash(userId, passwords.encode(rawPassword), Instant.now());
    }

    private AccessPrincipal requirePrincipal() {
        AccessPrincipal principal = IdentityContext.get().principal();
        if (principal == null) {
            throw IdentityException.unauthenticated();
        }
        return principal;
    }

    private void recordAuthEvent(UUID userId, UUID orgId, String type, String metadata) {
        store.insertAuthEvent(
                UUID.randomUUID(),
                userId,
                orgId,
                type,
                IdentityContext.get().clientIp(),
                Instant.now(),
                metadata);
    }

    private static Map<String, Object> tokensBody(String access, String refresh, long expiresIn) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("accessToken", access);
        data.put("refreshToken", refresh);
        data.put("expiresIn", expiresIn);
        return data;
    }

    static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
