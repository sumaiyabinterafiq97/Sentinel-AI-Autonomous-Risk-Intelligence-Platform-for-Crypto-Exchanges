-- M5 ALERT permission catalog (APIInventory). Does not modify M2 V003_1 placeholder read code.

INSERT INTO authz.permissions (id, code, description, domain, created_at) VALUES
('a0000000-0000-4000-8000-000000000047', 'alert:alert:write', 'Update alert lifecycle and investigation links', 'ALERT', NOW()),
('a0000000-0000-4000-8000-000000000048', 'alert:alert:assign', 'Assign alerts', 'ALERT', NOW()),
('a0000000-0000-4000-8000-000000000049', 'alert:alert:close', 'Close alerts', 'ALERT', NOW()),
('a0000000-0000-4000-8000-000000000050', 'alert:alert:priority', 'Set alert queue priority', 'ALERT', NOW())
ON CONFLICT (code) DO NOTHING;
