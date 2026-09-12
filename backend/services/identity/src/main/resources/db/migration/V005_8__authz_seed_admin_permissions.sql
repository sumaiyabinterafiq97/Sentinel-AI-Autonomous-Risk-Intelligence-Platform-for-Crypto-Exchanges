-- M10 ADMIN permission catalog (APIInventory). Does not rewrite earlier seeds.

INSERT INTO authz.permissions (id, code, description, domain, created_at) VALUES
('a0000000-0000-4000-8000-000000000071', 'admin:settings:write', 'Read and write platform admin settings', 'ADMIN', NOW()),
('a0000000-0000-4000-8000-000000000072', 'admin:integration:write', 'Read and write platform integrations', 'ADMIN', NOW()),
('a0000000-0000-4000-8000-000000000073', 'admin:user:provision', 'Orchestrate USER provisioning', 'ADMIN', NOW()),
('a0000000-0000-4000-8000-000000000074', 'admin:org:provision', 'Orchestrate ORG provisioning', 'ADMIN', NOW()),
('a0000000-0000-4000-8000-000000000075', 'admin:audit:read', 'Read admin audit records', 'ADMIN', NOW())
ON CONFLICT (code) DO NOTHING;
