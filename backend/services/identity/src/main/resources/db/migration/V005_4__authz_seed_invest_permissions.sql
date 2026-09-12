-- M6 INVEST permission catalog (APIInventory). Does not rewrite earlier seeds.

INSERT INTO authz.permissions (id, code, description, domain, created_at) VALUES
('a0000000-0000-4000-8000-000000000051', 'invest:case:read', 'Read investigation cases and timelines', 'INVEST', NOW()),
('a0000000-0000-4000-8000-000000000052', 'invest:case:write', 'Create and update investigation cases and notes', 'INVEST', NOW()),
('a0000000-0000-4000-8000-000000000053', 'invest:case:close', 'Close investigation cases', 'INVEST', NOW()),
('a0000000-0000-4000-8000-000000000054', 'invest:case:assign', 'Assign investigation cases', 'INVEST', NOW()),
('a0000000-0000-4000-8000-000000000055', 'invest:evidence:write', 'Attach evidence references to cases', 'INVEST', NOW())
ON CONFLICT (code) DO NOTHING;
