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
INSERT INTO user_passwords (id, id_users, password_hash, created_by, created_date)
VALUES
    ('44444444-4444-4444-4444-444444444001', '33333333-3333-3333-3333-333333333001', '$2a$10$CTxTvN8zAvmIKnfB8nlFcOEuAoitn8Iwh2aWQxccsTfR8dh0nq37O', 'system', NOW()),
    ('44444444-4444-4444-4444-444444444002', '33333333-3333-3333-3333-333333333002', '$2a$10$RIWDsOKBBOxY08JIcTRfH.K2GyY10q/mxql2p/CgXcVWOy7EAw.A.', 'system', NOW()),
    ('44444444-4444-4444-4444-444444444003', '33333333-3333-3333-3333-333333333003', '$2a$10$ZxrYBu/RziSN/oqS/2zToepmeIWcIRHTze.OD3jgH.w6.Y/MQJSYy', 'system', NOW()),
    ('44444444-4444-4444-4444-444444444004', '33333333-3333-3333-3333-333333333004', '$2a$10$2R5aMlfaPfF5xo1koP5U1OpdFvXDQzHzk1oCf5iLyLT2Sb6LuV3S2', 'system', NOW()),
    ('44444444-4444-4444-4444-444444444005', '33333333-3333-3333-3333-333333333005', '$2a$10$Q79S8NWF6kMbCdf78DyZvOHHjDBBqdkT0vwAw9znmCpLQWV6XAu6.', 'system', NOW());

-- Assign roles to test users (using existing role IDs from migration data)
INSERT INTO user_roles (id, id_users, id_roles, assigned_by, assigned_date)
VALUES
    ('55555555-5555-5555-5555-555555555001', '33333333-3333-3333-3333-333333333001', (SELECT id FROM roles WHERE role_code = 'CUSTOMER_SERVICE' LIMIT 1), 'system', NOW()),
    ('55555555-5555-5555-5555-555555555002', '33333333-3333-3333-3333-333333333002', (SELECT id FROM roles WHERE role_code = 'CUSTOMER_SERVICE' LIMIT 1), 'system', NOW()),
    ('55555555-5555-5555-5555-555555555003', '33333333-3333-3333-3333-333333333003', (SELECT id FROM roles WHERE role_code = 'BRANCH_MANAGER' LIMIT 1), 'system', NOW()),
    ('55555555-5555-5555-5555-555555555004', '33333333-3333-3333-3333-333333333004', (SELECT id FROM roles WHERE role_code = 'CUSTOMER_SERVICE' LIMIT 1), 'system', NOW()),
    ('55555555-5555-5555-5555-555555555005', '33333333-3333-3333-3333-333333333005', (SELECT id FROM roles WHERE role_code = 'TELLER' LIMIT 1), 'system', NOW());