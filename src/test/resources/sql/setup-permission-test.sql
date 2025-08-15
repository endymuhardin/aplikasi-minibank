-- Setup test data for permission management tests
-- Don't delete all permissions - keep basic ones for authentication to work
DELETE FROM role_permissions WHERE id_permissions IN (
    SELECT id FROM permissions WHERE permission_code LIKE '%TEST%'
);
DELETE FROM permissions WHERE permission_code LIKE '%TEST%';

-- Insert test permissions
INSERT INTO permissions (id, permission_code, permission_name, permission_category, description, created_by, created_date)
VALUES
    ('11111111-1111-1111-1111-111111111001', 'EDIT_TEST_PERM', 'Test Edit Permission', 'Test Category', 'Permission for testing edit functionality', 'system', NOW()),
    ('11111111-1111-1111-1111-111111111002', 'VIEW_TEST_PERM', 'Test View Permission', 'Test Category', 'Permission for testing view functionality', 'system', NOW()),
    ('11111111-1111-1111-1111-111111111003', 'DUPLICATE_PERM', 'Duplicate Test Permission', 'Test Category', 'Permission that already exists', 'system', NOW()),
    ('11111111-1111-1111-1111-111111111005', 'USER_READ_TEST', 'Read User Test', 'User Management', 'Permission for testing user read functionality', 'system', NOW());