-- Setup test data for permission management tests
DELETE FROM role_permissions;
DELETE FROM permissions;

-- Insert test permissions
INSERT INTO permissions (id, permission_code, permission_name, permission_category, description, resource, action, created_by, created_date)
VALUES
    ('11111111-1111-1111-1111-111111111001', 'EDIT_TEST_PERM', 'Test Edit Permission', 'Test Category', 'Permission for testing edit functionality', 'test_resource', 'UPDATE', 'system', NOW()),
    ('11111111-1111-1111-1111-111111111002', 'VIEW_TEST_PERM', 'Test View Permission', 'Test Category', 'Permission for testing view functionality', 'test_resource', 'READ', 'system', NOW()),
    ('11111111-1111-1111-1111-111111111003', 'DUPLICATE_PERM', 'Duplicate Test Permission', 'Test Category', 'Permission that already exists', 'test_resource', 'CREATE', 'system', NOW()),
    ('11111111-1111-1111-1111-111111111004', 'USER_CREATE', 'Create User', 'User Management', 'Permission to create users', 'user', 'CREATE', 'system', NOW()),
    ('11111111-1111-1111-1111-111111111005', 'USER_READ', 'Read User', 'User Management', 'Permission to view users', 'user', 'READ', 'system', NOW());