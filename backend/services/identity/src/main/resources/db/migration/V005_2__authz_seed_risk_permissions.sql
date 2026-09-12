-- M4 RISK permission catalog (APIInventory). Does not modify M2 V003_1 placeholder.

INSERT INTO authz.permissions (id, code, description, domain, created_at) VALUES
('a0000000-0000-4000-8000-000000000042', 'risk:ingest:write', 'Ingest transactions for risk evaluation', 'RISK', NOW()),
('a0000000-0000-4000-8000-000000000043', 'risk:assessment:read', 'Read risk assessments', 'RISK', NOW()),
('a0000000-0000-4000-8000-000000000044', 'risk:rule:read', 'Read risk rules', 'RISK', NOW()),
('a0000000-0000-4000-8000-000000000045', 'risk:rule:write', 'Create and update risk rules', 'RISK', NOW()),
('a0000000-0000-4000-8000-000000000046', 'risk:evaluate:write', 'Trigger risk evaluation', 'RISK', NOW())
ON CONFLICT (code) DO NOTHING;
