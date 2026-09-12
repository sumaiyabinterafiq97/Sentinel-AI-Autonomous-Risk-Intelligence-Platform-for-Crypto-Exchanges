-- M7 COMP permission catalog (APIInventory). Does not rewrite earlier seeds.

INSERT INTO authz.permissions (id, code, description, domain, created_at) VALUES
('a0000000-0000-4000-8000-000000000056', 'comp:kyc:write', 'Start KYC reviews', 'COMP', NOW()),
('a0000000-0000-4000-8000-000000000057', 'comp:kyc:approve', 'Complete KYC reviews', 'COMP', NOW()),
('a0000000-0000-4000-8000-000000000058', 'comp:aml:write', 'Start AML reviews', 'COMP', NOW()),
('a0000000-0000-4000-8000-000000000059', 'comp:travelrule:write', 'Record Travel Rule validations', 'COMP', NOW()),
('a0000000-0000-4000-8000-000000000060', 'comp:sanctions:write', 'Create sanctions screenings', 'COMP', NOW()),
('a0000000-0000-4000-8000-000000000061', 'comp:sanctions:approve', 'Disposition sanctions matches', 'COMP', NOW()),
('a0000000-0000-4000-8000-000000000062', 'comp:audit:write', 'Prepare audit packages', 'COMP', NOW())
ON CONFLICT (code) DO NOTHING;
