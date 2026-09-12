package com.sentinel.identity.infrastructure;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IdentityStore {

    private final JdbcTemplate jdbc;

    public IdentityStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<UserRow> findUserById(UUID id, UUID organizationId) {
        List<UserRow> rows = jdbc.query(
                """
                SELECT id, organization_id, email, display_name, status, deleted_at
                FROM "user".users
                WHERE id = ? AND organization_id = ? AND deleted_at IS NULL
                """,
                this::mapUser,
                id,
                organizationId);
        return rows.stream().findFirst();
    }

    public Optional<UserRow> findUserByIdAnyOrg(UUID id) {
        List<UserRow> rows = jdbc.query(
                """
                SELECT id, organization_id, email, display_name, status, deleted_at
                FROM "user".users WHERE id = ? AND deleted_at IS NULL
                """,
                this::mapUser,
                id);
        return rows.stream().findFirst();
    }

    public List<UserRow> findUsersByEmail(String email) {
        return jdbc.query(
                """
                SELECT id, organization_id, email, display_name, status, deleted_at
                FROM "user".users WHERE lower(email) = lower(?) AND deleted_at IS NULL
                """,
                this::mapUser,
                email);
    }

    public List<UserRow> listUsers(UUID organizationId, String cursor, int limit) {
        if (cursor == null || cursor.isBlank()) {
            return jdbc.query(
                    """
                    SELECT id, organization_id, email, display_name, status, deleted_at
                    FROM "user".users
                    WHERE organization_id = ? AND deleted_at IS NULL
                    ORDER BY email
                    LIMIT ?
                    """,
                    this::mapUser,
                    organizationId,
                    limit);
        }
        return jdbc.query(
                """
                SELECT id, organization_id, email, display_name, status, deleted_at
                FROM "user".users
                WHERE organization_id = ? AND deleted_at IS NULL AND email > ?
                ORDER BY email
                LIMIT ?
                """,
                this::mapUser,
                organizationId,
                cursor,
                limit);
    }

    public void insertUser(UserRow user, Instant now, UUID actor) {
        jdbc.update(
                """
                INSERT INTO "user".users
                  (id, organization_id, email, display_name, status, created_at, updated_at, deleted_at, created_by, updated_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, NULL, ?, ?)
                """,
                user.id(),
                user.organizationId(),
                user.email(),
                user.displayName(),
                user.status(),
                JdbcTypes.ts(now),
                JdbcTypes.ts(now),
                actor,
                actor);
        jdbc.update(
                """
                INSERT INTO "user".user_profiles (user_id, organization_id, attributes, updated_at)
                VALUES (?, ?, ?, ?)
                """,
                user.id(),
                user.organizationId(),
                JdbcTypes.jsonb("{}"),
                JdbcTypes.ts(now));
    }

    public void updateUser(UserRow user, Instant now, UUID actor) {
        jdbc.update(
                """
                UPDATE "user".users
                SET display_name = ?, status = ?, updated_at = ?, updated_by = ?, deleted_at = ?
                WHERE id = ?
                """,
                user.displayName(),
                user.status(),
                JdbcTypes.ts(now),
                actor,
                user.deletedAt() == null ? null : JdbcTypes.ts(user.deletedAt()),
                user.id());
    }

    public Optional<String> findPasswordHash(UUID userId) {
        List<String> hashes = jdbc.query(
                "SELECT password_hash FROM auth.credentials WHERE user_id = ?",
                (rs, i) -> rs.getString(1),
                userId);
        return hashes.stream().findFirst();
    }

    public void upsertPasswordHash(UUID userId, String hash, Instant now) {
        jdbc.update(
                """
                INSERT INTO auth.credentials (user_id, password_hash, created_at, updated_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (user_id) DO UPDATE SET password_hash = EXCLUDED.password_hash, updated_at = EXCLUDED.updated_at
                """,
                userId,
                hash,
                JdbcTypes.ts(now),
                JdbcTypes.ts(now));
    }

    public void insertSession(
            UUID id, UUID userId, UUID orgId, String status, Instant expiresAt, boolean mfa, Instant now) {
        jdbc.update(
                """
                INSERT INTO auth.sessions
                  (id, user_id, organization_id, status, expires_at, mfa_verified, device_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, NULL, ?, ?)
                """,
                id,
                userId,
                orgId,
                status,
                JdbcTypes.ts(expiresAt),
                mfa,
                JdbcTypes.ts(now),
                JdbcTypes.ts(now));
    }

    public Optional<SessionRow> findSession(UUID id) {
        List<SessionRow> rows = jdbc.query(
                """
                SELECT id, user_id, organization_id, status, expires_at, mfa_verified
                FROM auth.sessions WHERE id = ?
                """,
                (rs, i) -> new SessionRow(
                        rs.getObject("id", UUID.class),
                        rs.getObject("user_id", UUID.class),
                        rs.getObject("organization_id", UUID.class),
                        rs.getString("status"),
                        rs.getTimestamp("expires_at").toInstant(),
                        rs.getBoolean("mfa_verified")),
                id);
        return rows.stream().findFirst();
    }

    public void updateSessionExpiry(UUID id, Instant expiresAt, Instant now) {
        jdbc.update(
                "UPDATE auth.sessions SET expires_at = ?, updated_at = ? WHERE id = ?",
                JdbcTypes.ts(expiresAt),
                JdbcTypes.ts(now),
                id);
    }

    public void updateSessionStatus(UUID id, String status, Instant now) {
        jdbc.update(
                "UPDATE auth.sessions SET status = ?, updated_at = ? WHERE id = ?",
                status,
                JdbcTypes.ts(now),
                id);
    }

    public void markMfaVerified(UUID id, Instant now) {
        jdbc.update(
                "UPDATE auth.sessions SET mfa_verified = TRUE, updated_at = ? WHERE id = ?",
                JdbcTypes.ts(now),
                id);
    }

    public void insertRefresh(UUID id, UUID sessionId, String hash, Instant expiresAt) {
        jdbc.update(
                """
                INSERT INTO auth.refresh_tokens (id, session_id, token_hash, expires_at, revoked_at)
                VALUES (?, ?, ?, ?, NULL)
                """,
                id,
                sessionId,
                hash,
                JdbcTypes.ts(expiresAt));
    }

    public Optional<RefreshRow> findRefreshByHash(String hash) {
        List<RefreshRow> rows = jdbc.query(
                """
                SELECT id, session_id, token_hash, expires_at, revoked_at
                FROM auth.refresh_tokens WHERE token_hash = ?
                """,
                (rs, i) -> new RefreshRow(
                        rs.getObject("id", UUID.class),
                        rs.getObject("session_id", UUID.class),
                        rs.getString("token_hash"),
                        rs.getTimestamp("expires_at").toInstant(),
                        toInstant(rs.getTimestamp("revoked_at"))),
                hash);
        return rows.stream().findFirst();
    }

    public void revokeRefresh(UUID id, Instant now) {
        jdbc.update("UPDATE auth.refresh_tokens SET revoked_at = ? WHERE id = ?", JdbcTypes.ts(now), id);
    }

    public void revokeRefreshForSession(UUID sessionId, Instant now) {
        jdbc.update(
                "UPDATE auth.refresh_tokens SET revoked_at = ? WHERE session_id = ? AND revoked_at IS NULL",
                JdbcTypes.ts(now),
                sessionId);
    }

    public void insertAuthEvent(
            UUID id, UUID userId, UUID orgId, String type, String ip, Instant now, String metadata) {
        jdbc.update(
                """
                INSERT INTO auth.auth_events (id, user_id, organization_id, event_type, ip_address, created_at, metadata)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                userId,
                orgId,
                type,
                ip,
                JdbcTypes.ts(now),
                JdbcTypes.jsonb(metadata));
    }

    public void insertAudit(
            UUID orgId,
            UUID actorId,
            String action,
            String resourceType,
            UUID resourceId,
            String outcome,
            UUID correlationId,
            UUID requestId,
            String metadata,
            Instant now) {
        jdbc.update(
                """
                INSERT INTO core.audit_records
                  (id, organization_id, actor_id, actor_type, action, resource_type, resource_id,
                   outcome, correlation_id, request_id, metadata, created_at)
                VALUES (?, ?, ?, 'user', ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                orgId,
                actorId,
                action,
                resourceType,
                resourceId,
                outcome,
                correlationId,
                requestId,
                JdbcTypes.jsonb(metadata),
                JdbcTypes.ts(now));
    }

    public Optional<UUID> permissionId(String code) {
        List<UUID> ids = jdbc.query(
                "SELECT id FROM authz.permissions WHERE code = ?", (rs, i) -> rs.getObject(1, UUID.class), code);
        return ids.stream().findFirst();
    }

    public List<String> permissionCodes() {
        return jdbc.query("SELECT code FROM authz.permissions ORDER BY code", (rs, i) -> rs.getString(1));
    }

    public Set<String> effectivePermissions(UUID userId, UUID organizationId) {
        List<String> codes = jdbc.query(
                """
                SELECT DISTINCT p.code
                FROM authz.user_role_assignments a
                JOIN authz.roles r ON r.id = a.role_id
                JOIN authz.role_permissions rp ON rp.role_id = r.id
                JOIN authz.permissions p ON p.id = rp.permission_id
                WHERE a.user_id = ? AND a.organization_id = ? AND a.revoked_at IS NULL
                  AND r.organization_id = ?
                """,
                (rs, i) -> rs.getString(1),
                userId,
                organizationId,
                organizationId);
        return Set.copyOf(codes);
    }

    public List<String> roleNames(UUID userId, UUID organizationId) {
        return jdbc.query(
                """
                SELECT r.name
                FROM authz.user_role_assignments a
                JOIN authz.roles r ON r.id = a.role_id
                WHERE a.user_id = ? AND a.organization_id = ? AND a.revoked_at IS NULL
                ORDER BY r.name
                """,
                (rs, i) -> rs.getString(1),
                userId,
                organizationId);
    }

    public List<RoleRow> listRoles(UUID organizationId, String cursor, int limit) {
        if (cursor == null || cursor.isBlank()) {
            return jdbc.query(
                    """
                    SELECT id, organization_id, name, is_system
                    FROM authz.roles WHERE organization_id = ?
                    ORDER BY name LIMIT ?
                    """,
                    this::mapRole,
                    organizationId,
                    limit);
        }
        return jdbc.query(
                """
                SELECT id, organization_id, name, is_system
                FROM authz.roles WHERE organization_id = ? AND name > ?
                ORDER BY name LIMIT ?
                """,
                this::mapRole,
                organizationId,
                cursor,
                limit);
    }

    public List<String> rolePermissionCodes(UUID roleId) {
        return jdbc.query(
                """
                SELECT p.code FROM authz.role_permissions rp
                JOIN authz.permissions p ON p.id = rp.permission_id
                WHERE rp.role_id = ?
                ORDER BY p.code
                """,
                (rs, i) -> rs.getString(1),
                roleId);
    }

    public Optional<RoleRow> findRole(UUID roleId, UUID organizationId) {
        List<RoleRow> rows = jdbc.query(
                """
                SELECT id, organization_id, name, is_system
                FROM authz.roles WHERE id = ? AND organization_id = ?
                """,
                this::mapRole,
                roleId,
                organizationId);
        return rows.stream().findFirst();
    }

    public void insertRole(UUID id, UUID orgId, String name, boolean system, Instant now, UUID actor) {
        jdbc.update(
                """
                INSERT INTO authz.roles
                  (id, organization_id, name, description, is_system, created_at, updated_at, created_by)
                VALUES (?, ?, ?, NULL, ?, ?, ?, ?)
                """,
                id,
                orgId,
                name,
                system,
                JdbcTypes.ts(now),
                JdbcTypes.ts(now),
                actor);
    }

    public void addRolePermission(UUID roleId, UUID permissionId) {
        jdbc.update(
                "INSERT INTO authz.role_permissions (role_id, permission_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
                roleId,
                permissionId);
    }

    public UUID insertAssignment(UUID userId, UUID roleId, UUID orgId, Instant now, UUID actor) {
        UUID id = UUID.randomUUID();
        jdbc.update(
                """
                INSERT INTO authz.user_role_assignments
                  (id, user_id, role_id, organization_id, assigned_at, assigned_by, revoked_at)
                VALUES (?, ?, ?, ?, ?, ?, NULL)
                """,
                id,
                userId,
                roleId,
                orgId,
                JdbcTypes.ts(now),
                actor);
        return id;
    }

    public void insertOrganization(UUID id, String name, String status, Instant now, UUID actor) {
        jdbc.update(
                """
                INSERT INTO org.organizations
                  (id, name, status, settings, parent_org_id, created_at, updated_at, created_by)
                VALUES (?, ?, ?, '{}'::jsonb, NULL, ?, ?, ?)
                """,
                id,
                name,
                status,
                JdbcTypes.ts(now),
                JdbcTypes.ts(now),
                actor);
    }

    public void updateOrganization(UUID id, String name, String status, Instant now) {
        jdbc.update(
                "UPDATE org.organizations SET name = ?, status = ?, updated_at = ? WHERE id = ?",
                name,
                status,
                JdbcTypes.ts(now),
                id);
    }

    public Optional<OrgRow> findOrganization(UUID id) {
        List<OrgRow> rows = jdbc.query(
                "SELECT id, name, status FROM org.organizations WHERE id = ?",
                (rs, i) -> new OrgRow(rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("status")),
                id);
        return rows.stream().findFirst();
    }

    public List<OrgRow> listOrganizationsForUser(UUID userId, String cursor, int limit) {
        if (cursor == null || cursor.isBlank()) {
            return jdbc.query(
                    """
                    SELECT o.id, o.name, o.status
                    FROM org.organizations o
                    JOIN org.organization_memberships m ON m.organization_id = o.id
                    WHERE m.user_id = ? AND m.status = 'active'
                    ORDER BY o.name
                    LIMIT ?
                    """,
                    (rs, i) -> new OrgRow(rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("status")),
                    userId,
                    limit);
        }
        return jdbc.query(
                """
                SELECT o.id, o.name, o.status
                FROM org.organizations o
                JOIN org.organization_memberships m ON m.organization_id = o.id
                WHERE m.user_id = ? AND m.status = 'active' AND o.name > ?
                ORDER BY o.name
                LIMIT ?
                """,
                (rs, i) -> new OrgRow(rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("status")),
                userId,
                cursor,
                limit);
    }

    public boolean isMember(UUID userId, UUID organizationId) {
        Integer n = jdbc.queryForObject(
                """
                SELECT COUNT(*) FROM org.organization_memberships
                WHERE user_id = ? AND organization_id = ? AND status = 'active'
                """,
                Integer.class,
                userId,
                organizationId);
        return n != null && n > 0;
    }

    public void insertMembership(UUID orgId, UUID userId, Instant now) {
        jdbc.update(
                """
                INSERT INTO org.organization_memberships
                  (id, organization_id, user_id, status, joined_at, updated_at)
                VALUES (?, ?, ?, 'active', ?, ?)
                """,
                UUID.randomUUID(),
                orgId,
                userId,
                JdbcTypes.ts(now),
                JdbcTypes.ts(now));
    }

    public List<String> identityAdminPermissionCodes() {
        List<String> all = new ArrayList<>();
        for (String code : permissionCodes()) {
            if (code.startsWith("platform:")
                    || code.startsWith("authz:")
                    || code.startsWith("user:")
                    || code.startsWith("org:")
                    || code.startsWith("risk:")
                    || code.startsWith("alert:")
                    || code.startsWith("invest:")
                    || code.startsWith("comp:")
                    || code.startsWith("ai:")
                    || code.startsWith("dash:")
                    || code.startsWith("admin:")) {
                all.add(code);
            }
        }
        return all;
    }

    private UserRow mapUser(java.sql.ResultSet rs, int i) throws java.sql.SQLException {
        Timestamp deleted = rs.getTimestamp("deleted_at");
        return new UserRow(
                rs.getObject("id", UUID.class),
                rs.getObject("organization_id", UUID.class),
                rs.getString("email"),
                rs.getString("display_name"),
                rs.getString("status"),
                deleted == null ? null : deleted.toInstant());
    }

    private RoleRow mapRole(java.sql.ResultSet rs, int i) throws java.sql.SQLException {
        return new RoleRow(
                rs.getObject("id", UUID.class),
                rs.getObject("organization_id", UUID.class),
                rs.getString("name"),
                rs.getBoolean("is_system"));
    }

    private static Instant toInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }

    public record UserRow(
            UUID id, UUID organizationId, String email, String displayName, String status, Instant deletedAt) {}

    public record SessionRow(
            UUID id, UUID userId, UUID organizationId, String status, Instant expiresAt, boolean mfaVerified) {}

    public record RefreshRow(UUID id, UUID sessionId, String tokenHash, Instant expiresAt, Instant revokedAt) {}

    public record RoleRow(UUID id, UUID organizationId, String name, boolean system) {}

    public record OrgRow(UUID id, String name, String status) {}
}
