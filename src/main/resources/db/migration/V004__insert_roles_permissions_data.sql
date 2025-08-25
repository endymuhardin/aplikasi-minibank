-- Insert the three main roles
INSERT INTO roles (role_code, role_name, description, created_by) VALUES
('CUSTOMER_SERVICE', 'Customer Service', 'Handle customer registration and account opening', 'SYSTEM'),
('TELLER', 'Teller', 'Handle financial transactions', 'SYSTEM'),
('BRANCH_MANAGER', 'Branch Manager', 'Monitor operations and provide approvals', 'SYSTEM');

-- Insert permissions for Customer Service
INSERT INTO permissions (permission_code, permission_name, permission_category, description, created_by) VALUES
-- Customer management permissions
('CUSTOMER_VIEW', 'View Customer', 'CUSTOMER', 'View customer information', 'SYSTEM'),
('CUSTOMER_CREATE', 'Create Customer', 'CUSTOMER', 'Register new customers', 'SYSTEM'),
('CUSTOMER_UPDATE', 'Update Customer', 'CUSTOMER', 'Update customer information', 'SYSTEM'),

-- Account management permissions
('ACCOUNT_VIEW', 'View Account', 'ACCOUNT', 'View account information', 'SYSTEM'),
('ACCOUNT_CREATE', 'Create Account', 'ACCOUNT', 'Open new accounts for customers', 'SYSTEM'),
('ACCOUNT_UPDATE', 'Update Account', 'ACCOUNT', 'Update account information', 'SYSTEM'),

-- Product permissions
('PRODUCT_VIEW', 'View Product', 'PRODUCT', 'View banking products', 'SYSTEM');

-- Insert permissions for Teller
INSERT INTO permissions (permission_code, permission_name, permission_category, description, created_by) VALUES
-- Transaction permissions
('TRANSACTION_VIEW', 'View Transaction', 'TRANSACTION', 'View transaction history', 'SYSTEM'),
('TRANSACTION_DEPOSIT', 'Process Deposit', 'TRANSACTION', 'Process deposit transactions', 'SYSTEM'),
('TRANSACTION_WITHDRAWAL', 'Process Withdrawal', 'TRANSACTION', 'Process withdrawal transactions', 'SYSTEM'),
('TRANSACTION_TRANSFER', 'Process Transfer', 'TRANSACTION', 'Process transfer transactions', 'SYSTEM'),

-- Balance inquiry
('BALANCE_VIEW', 'View Balance', 'ACCOUNT', 'View account balance', 'SYSTEM');

-- Insert permissions for Branch Manager
INSERT INTO permissions (permission_code, permission_name, permission_category, description, created_by) VALUES
-- Monitoring and reporting permissions
('REPORT_VIEW', 'View Reports', 'REPORT', 'View business reports and analytics', 'SYSTEM'),
('AUDIT_VIEW', 'View Audit Log', 'AUDIT', 'View system audit logs', 'SYSTEM'),

-- Approval permissions
('TRANSACTION_APPROVE', 'Approve Transaction', 'TRANSACTION', 'Approve high-value transactions', 'SYSTEM'),
('ACCOUNT_APPROVE', 'Approve Account', 'ACCOUNT', 'Approve account opening/closing', 'SYSTEM'),

-- User management permissions
('USER_VIEW', 'View Users', 'USER', 'View system users', 'SYSTEM'),
('USER_CREATE', 'Create User', 'USER', 'Create new system users', 'SYSTEM'),
('USER_UPDATE', 'Update User', 'USER', 'Update user information', 'SYSTEM'),
('USER_DEACTIVATE', 'Deactivate User', 'USER', 'Deactivate system users', 'SYSTEM'),

-- Branch management permissions
('BRANCH_VIEW', 'View Branch', 'BRANCH', 'View branch information', 'SYSTEM'),
('BRANCH_CREATE', 'Create Branch', 'BRANCH', 'Create new branches', 'SYSTEM'),
('BRANCH_UPDATE', 'Update Branch', 'BRANCH', 'Update branch information', 'SYSTEM'),
('BRANCH_DELETE', 'Delete Branch', 'BRANCH', 'Delete branches', 'SYSTEM');

-- Grant permissions to Customer Service role
INSERT INTO role_permissions (id_roles, id_permissions, granted_by)
SELECT r.id, p.id, 'SYSTEM'
FROM roles r, permissions p 
WHERE r.role_code = 'CUSTOMER_SERVICE' 
AND p.permission_code IN (
    'CUSTOMER_VIEW', 'CUSTOMER_CREATE', 'CUSTOMER_UPDATE',
    'ACCOUNT_VIEW', 'ACCOUNT_CREATE', 'ACCOUNT_UPDATE',
    'PRODUCT_VIEW'
);

-- Grant permissions to Teller role
INSERT INTO role_permissions (id_roles, id_permissions, granted_by)
SELECT r.id, p.id, 'SYSTEM'
FROM roles r, permissions p 
WHERE r.role_code = 'TELLER' 
AND p.permission_code IN (
    'CUSTOMER_VIEW', 'ACCOUNT_VIEW', 'BALANCE_VIEW',
    'TRANSACTION_VIEW', 'TRANSACTION_DEPOSIT', 'TRANSACTION_WITHDRAWAL', 'TRANSACTION_TRANSFER',
    'PRODUCT_VIEW'
);

-- Grant permissions to Branch Manager role (has all permissions)
INSERT INTO role_permissions (id_roles, id_permissions, granted_by)
SELECT r.id, p.id, 'SYSTEM'
FROM roles r, permissions p 
WHERE r.role_code = 'BRANCH_MANAGER';

-- Create sample users for each role
INSERT INTO users (username, email, full_name, id_branches, created_by) VALUES
-- Branch Manager users
('admin', 'admin@yopmail.com', 'System Administrator', '01234567-8901-2345-6789-012345678901', 'SYSTEM'),
('manager1', 'manager1@yopmail.com', 'Branch Manager Jakarta', '01234567-8901-2345-6789-012345678902', 'SYSTEM'),
('manager2', 'manager2@yopmail.com', 'Branch Manager Surabaya', '01234567-8901-2345-6789-012345678904', 'SYSTEM'),

-- Teller users
('teller1', 'teller1@yopmail.com', 'Teller Counter 1', '01234567-8901-2345-6789-012345678901', 'SYSTEM'),
('teller2', 'teller2@yopmail.com', 'Teller Counter 2', '01234567-8901-2345-6789-012345678902', 'SYSTEM'),
('teller3', 'teller3@yopmail.com', 'Teller Counter 3', '01234567-8901-2345-6789-012345678903', 'SYSTEM'),

-- Customer Service users
('cs1', 'cs1@yopmail.com', 'Customer Service Staff 1', '01234567-8901-2345-6789-012345678901', 'SYSTEM'),
('cs2', 'cs2@yopmail.com', 'Customer Service Staff 2', '01234567-8901-2345-6789-012345678902', 'SYSTEM'),
('cs3', 'cs3@yopmail.com', 'Customer Service Staff 3', '01234567-8901-2345-6789-012345678905', 'SYSTEM');

-- Set passwords for all users (password: minibank123)
-- Note: BCrypt hash for 'minibank123'
INSERT INTO user_passwords (id_users, password_hash, created_by)
SELECT id, '$2a$10$6tjICoD1DhK3r82bD4NiSuJ8A4xvf5osh96V7Q4BXFvIXZB3/s7da', 'SYSTEM'
FROM users WHERE username IN ('admin', 'manager1', 'manager2', 'teller1', 'teller2', 'teller3', 'cs1', 'cs2', 'cs3');

-- Assign Branch Manager role
INSERT INTO user_roles (id_users, id_roles, assigned_by)
SELECT u.id, r.id, 'SYSTEM'
FROM users u, roles r 
WHERE u.username IN ('admin', 'manager1', 'manager2') AND r.role_code = 'BRANCH_MANAGER';

-- Assign Teller role
INSERT INTO user_roles (id_users, id_roles, assigned_by)
SELECT u.id, r.id, 'SYSTEM'
FROM users u, roles r 
WHERE u.username IN ('teller1', 'teller2', 'teller3') AND r.role_code = 'TELLER';

-- Assign Customer Service role
INSERT INTO user_roles (id_users, id_roles, assigned_by)
SELECT u.id, r.id, 'SYSTEM'
FROM users u, roles r 
WHERE u.username IN ('cs1', 'cs2', 'cs3') AND r.role_code = 'CUSTOMER_SERVICE';