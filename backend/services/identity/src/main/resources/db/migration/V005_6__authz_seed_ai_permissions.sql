-- M8 AI permission catalog (APIInventory). Does not rewrite earlier seeds.
-- V2 codes (ai:compliance:assist, ai:eval:write) are intentionally omitted.

INSERT INTO authz.permissions (id, code, description, domain, created_at) VALUES
('a0000000-0000-4000-8000-000000000063', 'ai:investigation:assist', 'Request investigation assistance', 'AI', NOW()),
('a0000000-0000-4000-8000-000000000064', 'ai:risk:assist', 'Request risk explanation assistance', 'AI', NOW()),
('a0000000-0000-4000-8000-000000000065', 'ai:retrieve:execute', 'Execute evidence retrieval', 'AI', NOW()),
('a0000000-0000-4000-8000-000000000066', 'ai:recommendation:read', 'Read AI recommendations', 'AI', NOW()),
('a0000000-0000-4000-8000-000000000067', 'ai:prompt:write', 'Manage AI prompts', 'AI', NOW())
ON CONFLICT (code) DO NOTHING;
