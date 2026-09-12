-- M9 DASH permission catalog (APIInventory). Does not rewrite earlier seeds.

INSERT INTO authz.permissions (id, code, description, domain, created_at) VALUES
('a0000000-0000-4000-8000-000000000068', 'dash:workspace:read', 'Read operational workspace', 'DASH', NOW()),
('a0000000-0000-4000-8000-000000000069', 'dash:queue:read', 'Read work queues', 'DASH', NOW()),
('a0000000-0000-4000-8000-000000000070', 'dash:widget:read', 'Read workspace widgets', 'DASH', NOW())
ON CONFLICT (code) DO NOTHING;
