-- Insert the three main roles
INSERT INTO roles (role_code, role_name, description, created_by) VALUES
('CUSTOMER_SERVICE', 'Customer Service', 'Handle customer registration and account opening', 'SYSTEM'),
('TELLER', 'Teller', 'Handle financial transactions', 'SYSTEM'),
('BRANCH_MANAGER', 'Branch Manager', 'Monitor operations and provide approvals', 'SYSTEM');

-- Insert permissions for Customer Service
INSERT INTO permissions (permission_code, permission_name, permission_category, description, resource, action, created_by) VALUES
-- Customer management permissions
('CUSTOMER_VIEW', 'View Customer', 'CUSTOMER', 'View customer information', 'customer', 'read', 'SYSTEM'),
('CUSTOMER_CREATE', 'Create Customer', 'CUSTOMER', 'Register new customers', 'customer', 'create', 'SYSTEM'),
('CUSTOMER_UPDATE', 'Update Customer', 'CUSTOMER', 'Update customer information', 'customer', 'update', 'SYSTEM'),

-- Account management permissions
('ACCOUNT_VIEW', 'View Account', 'ACCOUNT', 'View account information', 'account', 'read', 'SYSTEM'),
('ACCOUNT_CREATE', 'Create Account', 'ACCOUNT', 'Open new accounts for customers', 'account', 'create', 'SYSTEM'),
('ACCOUNT_UPDATE', 'Update Account', 'ACCOUNT', 'Update account information', 'account', 'update', 'SYSTEM'),

-- Product permissions
('PRODUCT_VIEW', 'View Product', 'PRODUCT', 'View banking products', 'product', 'read', 'SYSTEM');

-- Insert permissions for Teller
INSERT INTO permissions (permission_code, permission_name, permission_category, description, resource, action, created_by) VALUES
-- Transaction permissions
('TRANSACTION_VIEW', 'View Transaction', 'TRANSACTION', 'View transaction history', 'transaction', 'read', 'SYSTEM'),
('TRANSACTION_DEPOSIT', 'Process Deposit', 'TRANSACTION', 'Process deposit transactions', 'transaction', 'deposit', 'SYSTEM'),
('TRANSACTION_WITHDRAWAL', 'Process Withdrawal', 'TRANSACTION', 'Process withdrawal transactions', 'transaction', 'withdrawal', 'SYSTEM'),
('TRANSACTION_TRANSFER', 'Process Transfer', 'TRANSACTION', 'Process transfer transactions', 'transaction', 'transfer', 'SYSTEM'),

-- Balance inquiry
('BALANCE_VIEW', 'View Balance', 'ACCOUNT', 'View account balance', 'account', 'balance', 'SYSTEM');

-- Insert permissions for Branch Manager
INSERT INTO permissions (permission_code, permission_name, permission_category, description, resource, action, created_by) VALUES
-- Monitoring and reporting permissions
('REPORT_VIEW', 'View Reports', 'REPORT', 'View business reports and analytics', 'report', 'read', 'SYSTEM'),
('AUDIT_VIEW', 'View Audit Log', 'AUDIT', 'View system audit logs', 'audit', 'read', 'SYSTEM'),

-- Approval permissions
('TRANSACTION_APPROVE', 'Approve Transaction', 'TRANSACTION', 'Approve high-value transactions', 'transaction', 'approve', 'SYSTEM'),
('ACCOUNT_APPROVE', 'Approve Account', 'ACCOUNT', 'Approve account opening/closing', 'account', 'approve', 'SYSTEM'),

-- User management permissions
('USER_VIEW', 'View Users', 'USER', 'View system users', 'user', 'read', 'SYSTEM'),
('USER_CREATE', 'Create User', 'USER', 'Create new system users', 'user', 'create', 'SYSTEM'),
('USER_UPDATE', 'Update User', 'USER', 'Update user information', 'user', 'update', 'SYSTEM'),
('USER_DEACTIVATE', 'Deactivate User', 'USER', 'Deactivate system users', 'user', 'deactivate', 'SYSTEM');

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
INSERT INTO users (username, email, full_name, created_by) VALUES
-- Branch Manager users
('admin', 'admin@yopmail.com', 'System Administrator', 'SYSTEM'),
('manager1', 'manager1@yopmail.com', 'Branch Manager Jakarta', 'SYSTEM'),
('manager2', 'manager2@yopmail.com', 'Branch Manager Surabaya', 'SYSTEM'),

-- Teller users
('teller1', 'teller1@yopmail.com', 'Teller Counter 1', 'SYSTEM'),
('teller2', 'teller2@yopmail.com', 'Teller Counter 2', 'SYSTEM'),
('teller3', 'teller3@yopmail.com', 'Teller Counter 3', 'SYSTEM'),

-- Customer Service users
('cs1', 'cs1@yopmail.com', 'Customer Service Staff 1', 'SYSTEM'),
('cs2', 'cs2@yopmail.com', 'Customer Service Staff 2', 'SYSTEM'),
('cs3', 'cs3@yopmail.com', 'Customer Service Staff 3', 'SYSTEM');

-- Set passwords for all users (password: minibank123)
-- Note: BCrypt hash for 'minibank123'
INSERT INTO user_passwords (id_users, password_hash, created_by)
SELECT id, '$2a$10$qj8.4l5y5Yb8gYF4H6N2M8A7t9X6V5W4SmKHGLVjFHGHfPGKw5P8/e', 'SYSTEM'
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