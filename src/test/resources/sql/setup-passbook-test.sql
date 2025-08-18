-- Setup script for passbook controller tests
-- This script creates test data needed for passbook functionality

-- Clear existing test data
DELETE FROM transactions WHERE id_accounts IN (
    SELECT id FROM accounts WHERE account_number IN ('A2000001', 'A2000002')
);
DELETE FROM accounts WHERE account_number IN ('A2000001', 'A2000002');
DELETE FROM personal_customers WHERE id IN (
    SELECT id FROM customers WHERE customer_number IN ('C1000001', 'C1000002')
);
DELETE FROM customers WHERE customer_number IN ('C1000001', 'C1000002');
DELETE FROM products WHERE product_code IN ('SAV001');
DELETE FROM branches WHERE branch_code = 'TEST';

-- Insert test branch
INSERT INTO branches (id, branch_code, branch_name, is_main_branch, status, address, city, country, postal_code, phone_number, email, manager_name, created_date, created_by) 
VALUES 
    ('11111111-1111-1111-1111-111111111111', 'TEST', 'Test Branch', false, 'ACTIVE', 'Test Address', 'Test City', 'Indonesia', '12345', '021-1234567', 'test@minibank.com', 'Test Manager', CURRENT_TIMESTAMP, 'TEST')
ON CONFLICT (branch_code) DO NOTHING;

-- Insert test customers
INSERT INTO customers (
    id, customer_type, customer_number, id_branches, email, phone_number, 
    address, city, postal_code, country, created_by, created_date
) VALUES 
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'PERSONAL', 'C1000001', '11111111-1111-1111-1111-111111111111', 'ahmad.suharto@email.com', '081234567890', 
 'Jl. Sudirman No. 123', 'Jakarta', '10220', 'Indonesia', 'TEST', CURRENT_TIMESTAMP),

('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'PERSONAL', 'C1000002', '11111111-1111-1111-1111-111111111111', 'siti.nurhaliza@email.com', '081234567891', 
 'Jl. Thamrin No. 456', 'Jakarta', '10230', 'Indonesia', 'TEST', CURRENT_TIMESTAMP);

-- Insert personal customer specific data
INSERT INTO personal_customers (
    id, first_name, last_name, date_of_birth, identity_number, identity_type
) VALUES 
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Ahmad', 'Suharto', '1985-03-15', '3271081503850001', 'KTP'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Siti', 'Nurhaliza', '1990-07-22', '3271082207900002', 'KTP');

-- Insert test product
INSERT INTO products (id, product_code, product_name, product_type, product_category, description, is_active, is_default, currency, minimum_opening_balance, minimum_balance, profit_sharing_ratio, profit_sharing_type, profit_distribution_frequency, created_by, created_date)
VALUES 
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'SAV001', 'Basic Savings Account', 'TABUNGAN_WADIAH', 'Islamic Savings', 'Basic Islamic savings account', true, true, 'IDR', 50000.00, 10000.00, 0.0275, 'WADIAH', 'MONTHLY', 'TEST', CURRENT_TIMESTAMP);

-- Insert test accounts
INSERT INTO accounts (id, id_customers, id_products, id_branches, account_number, account_name, balance, status, opened_date, created_by, created_date)
VALUES 
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'dddddddd-dddd-dddd-dddd-dddddddddddd', '11111111-1111-1111-1111-111111111111', 'A2000001', 'Ahmad Suharto - Savings', 500000.00, 'ACTIVE', '2024-01-15', 'TEST', CURRENT_TIMESTAMP),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'dddddddd-dddd-dddd-dddd-dddddddddddd', '11111111-1111-1111-1111-111111111111', 'A2000002', 'Siti Nurhaliza - Savings', 750000.00, 'ACTIVE', '2024-01-20', 'TEST', CURRENT_TIMESTAMP);

-- Insert test transactions
INSERT INTO transactions (id, id_accounts, transaction_number, transaction_type, amount, currency, balance_before, balance_after, description, reference_number, channel, transaction_date, processed_date, created_by)
VALUES 
-- Account A2000001 transactions
('f1111111-1111-1111-1111-111111111111', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'T4000001', 'DEPOSIT', 1000000.00, 'IDR', 0.00, 1000000.00, 'Account opening deposit', 'OPEN001', 'TELLER', '2024-01-01 09:00:00', '2024-01-01 09:00:00', 'TEST'),
('f2222222-2222-2222-2222-222222222222', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'T4000002', 'DEPOSIT', 500000.00, 'IDR', 1000000.00, 1500000.00, 'Salary deposit January', 'SAL001', 'ONLINE', '2024-01-05 08:30:00', '2024-01-05 08:30:00', 'TEST'),
('f3333333-3333-3333-3333-333333333333', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'T4000003', 'WITHDRAWAL', 200000.00, 'IDR', 1500000.00, 1300000.00, 'ATM withdrawal', 'ATM001', 'ATM', '2024-01-10 18:45:00', '2024-01-10 18:45:00', 'TEST'),
('f4444444-4444-4444-4444-444444444444', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'T4000004', 'WITHDRAWAL', 50000.00, 'IDR', 1300000.00, 1250000.00, 'Bill payment - Electricity', 'BILL001', 'ONLINE', '2024-01-15 14:20:00', '2024-01-15 14:20:00', 'TEST'),
('f5555555-5555-5555-5555-555555555555', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'T4000005', 'DEPOSIT', 100000.00, 'IDR', 1250000.00, 1350000.00, 'Cash deposit', 'CASH001', 'TELLER', '2024-01-20 11:15:00', '2024-01-20 11:15:00', 'TEST');