-- Migration 003b — AUTHZ permission catalog seed (APIInventory permission strings)

INSERT INTO authz.permissions (id, code, description, domain, created_at) VALUES
('a0000000-0000-4000-8000-000000000001', 'platform:status:read', 'Read platform status', 'CORE', NOW()),
('a0000000-0000-4000-8000-000000000002', 'platform:config:read', 'Read platform config', 'CORE', NOW()),
('a0000000-0000-4000-8000-000000000003', 'platform:config:write', 'Write platform config', 'CORE', NOW()),
('a0000000-0000-4000-8000-000000000004', 'platform:flags:read', 'Read feature flags', 'CORE', NOW()),
('a0000000-0000-4000-8000-000000000005', 'platform:flags:write', 'Write feature flags', 'CORE', NOW()),
('a0000000-0000-4000-8000-000000000010', 'authz:role:read', 'List roles', 'AUTHZ', NOW()),
('a0000000-0000-4000-8000-000000000011', 'authz:role:write', 'Create roles', 'AUTHZ', NOW()),
('a0000000-0000-4000-8000-000000000012', 'authz:assignment:write', 'Assign roles', 'AUTHZ', NOW()),
('a0000000-0000-4000-8000-000000000020', 'user:user:read', 'Read users', 'USER', NOW()),
('a0000000-0000-4000-8000-000000000021', 'user:user:write', 'Write users', 'USER', NOW()),
('a0000000-0000-4000-8000-000000000030', 'org:org:read', 'Read organizations', 'ORG', NOW()),
('a0000000-0000-4000-8000-000000000031', 'org:org:write', 'Write organizations', 'ORG', NOW()),
('a0000000-0000-4000-8000-000000000040', 'alert:alert:read', 'Catalog only — ALERT not implemented in M2', 'ALERT', NOW()),
('a0000000-0000-4000-8000-000000000041', 'risk:risk:read', 'Catalog only — RISK not implemented in M2', 'RISK', NOW())
ON CONFLICT (code) DO NOTHING;
