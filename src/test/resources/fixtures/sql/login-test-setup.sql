-- Roles already exist from migration, no need to insert them

-- Insert test users with predictable UUIDs
INSERT INTO users (id, username, email, full_name, is_active, is_locked, failed_login_attempts, created_date, created_by) 
VALUES 
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'loginuser', 'loginuser@test.com', 'Login User', true, false, 0, NOW(), 'test-setup'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'logoutuser', 'logoutuser@test.com', 'Logout User', true, false, 0, NOW(), 'test-setup'),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'manager', 'manager@test.com', 'Branch Manager', true, false, 0, NOW(), 'test-setup'),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'cs', 'cs@test.com', 'Customer Service', true, false, 0, NOW(), 'test-setup'),
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'teller', 'teller@test.com', 'Teller', true, false, 0, NOW(), 'test-setup'),
    ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'userinfo', 'userinfo@test.com', 'User Info Test', true, false, 0, NOW(), 'test-setup'),
    ('00000000-0000-0000-0000-000000000000', 'statsuser', 'statsuser@test.com', 'Stats User', true, false, 0, NOW(), 'test-setup'),
    ('11111111-2222-3333-4444-555555555555', 'validuser1', 'validuser1@test.com', 'Valid User 1', true, false, 0, NOW(), 'test-setup'),
    ('22222222-3333-4444-5555-666666666666', 'validuser2', 'validuser2@test.com', 'Valid User 2', true, false, 0, NOW(), 'test-setup'),
    ('33333333-4444-5555-6666-777777777777', 'validuser3', 'validuser3@test.com', 'Valid User 3', true, false, 0, NOW(), 'test-setup')
ON CONFLICT (username) DO NOTHING;

-- Insert test user passwords one by one to ensure proper foreign key handling
-- BCrypt hash for 'password123': $2a$10$ueTu8or/oWak8Oxe2ONVyuRwW4R.8yjt6UFYtqQ74U6WbFimlftq6
-- BCrypt hash for 'testpass123': $2a$10$gfO.jUJEDiz0cDSJ4eosgeM3Jslci2kch0kXuLGqY.Aj0aQsHuunW

INSERT INTO user_passwords (id, id_users, password_hash, is_active, created_date, created_by) 
VALUES ('aaaa1111-bbbb-cccc-dddd-eeeeeeeeeeee', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '$2a$10$ueTu8or/oWak8Oxe2ONVyuRwW4R.8yjt6UFYtqQ74U6WbFimlftq6', true, NOW(), 'test-setup')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_passwords (id, id_users, password_hash, is_active, created_date, created_by) 
VALUES ('bbbb1111-cccc-dddd-eeee-ffffffffffff', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '$2a$10$ueTu8or/oWak8Oxe2ONVyuRwW4R.8yjt6UFYtqQ74U6WbFimlftq6', true, NOW(), 'test-setup')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_passwords (id, id_users, password_hash, is_active, created_date, created_by) 
VALUES ('cccc1111-dddd-eeee-ffff-000000000000', 'cccccccc-cccc-cccc-cccc-cccccccccccc', '$2a$10$ueTu8or/oWak8Oxe2ONVyuRwW4R.8yjt6UFYtqQ74U6WbFimlftq6', true, NOW(), 'test-setup')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_passwords (id, id_users, password_hash, is_active, created_date, created_by) 
VALUES ('dddd1111-eeee-ffff-0000-111111111111', 'dddddddd-dddd-dddd-dddd-dddddddddddd', '$2a$10$ueTu8or/oWak8Oxe2ONVyuRwW4R.8yjt6UFYtqQ74U6WbFimlftq6', true, NOW(), 'test-setup')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_passwords (id, id_users, password_hash, is_active, created_date, created_by) 
VALUES ('eeee1111-ffff-0000-1111-222222222222', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '$2a$10$ueTu8or/oWak8Oxe2ONVyuRwW4R.8yjt6UFYtqQ74U6WbFimlftq6', true, NOW(), 'test-setup')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_passwords (id, id_users, password_hash, is_active, created_date, created_by) 
VALUES ('ffff1111-0000-1111-2222-333333333333', 'ffffffff-ffff-ffff-ffff-ffffffffffff', '$2a$10$ueTu8or/oWak8Oxe2ONVyuRwW4R.8yjt6UFYtqQ74U6WbFimlftq6', true, NOW(), 'test-setup')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_passwords (id, id_users, password_hash, is_active, created_date, created_by) 
VALUES ('00001111-1111-2222-3333-444444444444', '00000000-0000-0000-0000-000000000000', '$2a$10$ueTu8or/oWak8Oxe2ONVyuRwW4R.8yjt6UFYtqQ74U6WbFimlftq6', true, NOW(), 'test-setup')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_passwords (id, id_users, password_hash, is_active, created_date, created_by) 
VALUES ('11112222-2222-3333-4444-555555555555', '11111111-2222-3333-4444-555555555555', '$2a$10$gfO.jUJEDiz0cDSJ4eosgeM3Jslci2kch0kXuLGqY.Aj0aQsHuunW', true, NOW(), 'test-setup')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_passwords (id, id_users, password_hash, is_active, created_date, created_by) 
VALUES ('22223333-3333-4444-5555-666666666666', '22222222-3333-4444-5555-666666666666', '$2a$10$gfO.jUJEDiz0cDSJ4eosgeM3Jslci2kch0kXuLGqY.Aj0aQsHuunW', true, NOW(), 'test-setup')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_passwords (id, id_users, password_hash, is_active, created_date, created_by) 
VALUES ('33334444-4444-5555-6666-777777777777', '33333333-4444-5555-6666-777777777777', '$2a$10$gfO.jUJEDiz0cDSJ4eosgeM3Jslci2kch0kXuLGqY.Aj0aQsHuunW', true, NOW(), 'test-setup')
ON CONFLICT (id) DO NOTHING;

-- Assign roles to users using dynamic lookups
-- loginuser -> CUSTOMER_SERVICE
INSERT INTO user_roles (id_users, id_roles, assigned_date, assigned_by)
SELECT 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', r.id, NOW(), 'test-setup'
FROM roles r WHERE r.role_code = 'CUSTOMER_SERVICE'
ON CONFLICT (id_users, id_roles) DO NOTHING;

-- logoutuser -> TELLER
INSERT INTO user_roles (id_users, id_roles, assigned_date, assigned_by)
SELECT 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', r.id, NOW(), 'test-setup'
FROM roles r WHERE r.role_code = 'TELLER'
ON CONFLICT (id_users, id_roles) DO NOTHING;

-- manager -> BRANCH_MANAGER
INSERT INTO user_roles (id_users, id_roles, assigned_date, assigned_by)
SELECT 'cccccccc-cccc-cccc-cccc-cccccccccccc', r.id, NOW(), 'test-setup'
FROM roles r WHERE r.role_code = 'BRANCH_MANAGER'
ON CONFLICT (id_users, id_roles) DO NOTHING;

-- cs -> CUSTOMER_SERVICE
INSERT INTO user_roles (id_users, id_roles, assigned_date, assigned_by)
SELECT 'dddddddd-dddd-dddd-dddd-dddddddddddd', r.id, NOW(), 'test-setup'
FROM roles r WHERE r.role_code = 'CUSTOMER_SERVICE'
ON CONFLICT (id_users, id_roles) DO NOTHING;

-- teller -> TELLER
INSERT INTO user_roles (id_users, id_roles, assigned_date, assigned_by)
SELECT 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', r.id, NOW(), 'test-setup'
FROM roles r WHERE r.role_code = 'TELLER'
ON CONFLICT (id_users, id_roles) DO NOTHING;

-- userinfo -> BRANCH_MANAGER
INSERT INTO user_roles (id_users, id_roles, assigned_date, assigned_by)
SELECT 'ffffffff-ffff-ffff-ffff-ffffffffffff', r.id, NOW(), 'test-setup'
FROM roles r WHERE r.role_code = 'BRANCH_MANAGER'
ON CONFLICT (id_users, id_roles) DO NOTHING;

-- statsuser -> CUSTOMER_SERVICE
INSERT INTO user_roles (id_users, id_roles, assigned_date, assigned_by)
SELECT '00000000-0000-0000-0000-000000000000', r.id, NOW(), 'test-setup'
FROM roles r WHERE r.role_code = 'CUSTOMER_SERVICE'
ON CONFLICT (id_users, id_roles) DO NOTHING;

-- validuser1 -> CUSTOMER_SERVICE
INSERT INTO user_roles (id_users, id_roles, assigned_date, assigned_by)
SELECT '11111111-2222-3333-4444-555555555555', r.id, NOW(), 'test-setup'
FROM roles r WHERE r.role_code = 'CUSTOMER_SERVICE'
ON CONFLICT (id_users, id_roles) DO NOTHING;

-- validuser2 -> TELLER
INSERT INTO user_roles (id_users, id_roles, assigned_date, assigned_by)
SELECT '22222222-3333-4444-5555-666666666666', r.id, NOW(), 'test-setup'
FROM roles r WHERE r.role_code = 'TELLER'
ON CONFLICT (id_users, id_roles) DO NOTHING;

-- validuser3 -> BRANCH_MANAGER
INSERT INTO user_roles (id_users, id_roles, assigned_date, assigned_by)
SELECT '33333333-4444-5555-6666-777777777777', r.id, NOW(), 'test-setup'
FROM roles r WHERE r.role_code = 'BRANCH_MANAGER'
ON CONFLICT (id_users, id_roles) DO NOTHING;