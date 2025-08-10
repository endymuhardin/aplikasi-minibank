-- Clean up test data in reverse dependency order
DELETE FROM user_roles WHERE assigned_by = 'test-setup';
DELETE FROM user_passwords WHERE created_by = 'test-setup';
DELETE FROM users WHERE created_by = 'test-setup';
DELETE FROM roles WHERE created_by = 'test-setup';