-- Setup test data for dashboard tests
DELETE FROM user_passwords WHERE id_users IN (SELECT id FROM users WHERE username IN ('dashuser', 'statsuser', 'manager', 'cs', 'teller'));
DELETE FROM user_roles WHERE id_users IN (SELECT id FROM users WHERE username IN ('dashuser', 'statsuser', 'manager', 'cs', 'teller'));
DELETE FROM users WHERE username IN ('dashuser', 'statsuser', 'manager', 'cs', 'teller');

-- Insert test users for dashboard testing
INSERT INTO users (id, username, full_name, email, is_active, created_by, created_date, updated_by, updated_date)
VALUES
    ('33333333-3333-3333-3333-333333333001', 'dashuser', 'Dashboard Test User', 'dashuser@example.com', true, 'system', NOW(), 'system', NOW()),
    ('33333333-3333-3333-3333-333333333002', 'statsuser', 'Statistics Test User', 'statsuser@example.com', true, 'system', NOW(), 'system', NOW()),
    ('33333333-3333-3333-3333-333333333003', 'manager', 'Branch Manager', 'manager@example.com', true, 'system', NOW(), 'system', NOW()),
    ('33333333-3333-3333-3333-333333333004', 'cs', 'Customer Service', 'cs@example.com', true, 'system', NOW(), 'system', NOW()),
    ('33333333-3333-3333-3333-333333333005', 'teller', 'Teller User', 'teller@example.com', true, 'system', NOW(), 'system', NOW());

-- Insert passwords for test users  
-- Insert passwords for test users (password: minibank123)
-- Note: BCrypt hash for 'minibank123' is $2a$10$6tjICoD1DhK3r82bD4NiSuJ8A4xvf5osh96V7Q4BXFvIXZB3/s7da
INSERT INTO user_passwords (id_users, password_hash, created_by)
SELECT id, '$2a$10$6tjICoD1DhK3r82bD4NiSuJ8A4xvf5osh96V7Q4BXFvIXZB3/s7da', 'system'
FROM users WHERE username IN ('dashuser', 'statsuser', 'manager', 'cs', 'teller');

-- Assign roles to test users (using existing role IDs from migration data)
INSERT INTO user_roles (id, id_users, id_roles, assigned_by, assigned_date)
VALUES
    ('55555555-5555-5555-5555-555555555001', '33333333-3333-3333-3333-333333333001', (SELECT id FROM roles WHERE role_code = 'CUSTOMER_SERVICE' LIMIT 1), 'system', NOW()),
    ('55555555-5555-5555-5555-555555555002', '33333333-3333-3333-3333-333333333002', (SELECT id FROM roles WHERE role_code = 'CUSTOMER_SERVICE' LIMIT 1), 'system', NOW()),
    ('55555555-5555-5555-5555-555555555003', '33333333-3333-3333-3333-333333333003', (SELECT id FROM roles WHERE role_code = 'BRANCH_MANAGER' LIMIT 1), 'system', NOW()),
    ('55555555-5555-5555-5555-555555555004', '33333333-3333-3333-3333-333333333004', (SELECT id FROM roles WHERE role_code = 'CUSTOMER_SERVICE' LIMIT 1), 'system', NOW()),
    ('55555555-5555-5555-5555-555555555005', '33333333-3333-3333-3333-333333333005', (SELECT id FROM roles WHERE role_code = 'TELLER' LIMIT 1), 'system', NOW());