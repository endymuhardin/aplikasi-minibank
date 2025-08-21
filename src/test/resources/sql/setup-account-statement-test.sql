-- Setup script for account statement Selenium tests
-- LEVERAGE migration data from V002__insert_initial_data.sql and V004__insert_roles_permissions_data.sql

-- Clean up any existing test accounts and transactions
DELETE FROM transactions WHERE created_by = 'SELENIUM_STATEMENT_TEST';
DELETE FROM accounts WHERE created_by = 'SELENIUM_STATEMENT_TEST' OR account_number LIKE 'STMT_%';

-- NOTE: Migration provides comprehensive base data for Selenium tests:
--
-- BRANCHES (V002): Use existing migration branches
-- - HO001: Kantor Pusat Jakarta (ID: 01234567-8901-2345-6789-012345678901) - MAIN BRANCH
--
-- CUSTOMERS (V002): Use existing migration customers
-- - C1000001: Ahmad Suharto (Personal, HO001 branch)
-- - C1000002: Siti Nurhaliza (Personal, JKT01 branch) 
-- - C1000003: PT. Teknologi Maju (Corporate, HO001 branch)
--
-- PRODUCTS (V002): Use existing migration products 
-- - TAB001: Tabungan Wadiah Basic (PERSONAL, default, min 50k)
-- - TAB002: Tabungan Mudharabah Premium (PERSONAL, min 1M)
--
-- USERS & RBAC (V004): Already leveraged by LoginHelper
-- - cs1, teller1, admin with password: minibank123
-- - Roles: CUSTOMER_SERVICE, TELLER, BRANCH_MANAGER

-- Create test accounts with transactions for statement testing
-- Account 1: Personal customer with multiple transactions
INSERT INTO accounts (
    id, 
    id_customers, 
    id_products, 
    id_branches,
    account_number, 
    account_name, 
    balance, 
    status, 
    opened_date,
    created_by,
    created_date
) VALUES (
    'f47ac10b-58cc-4372-a567-0e02b2c3d479'::uuid,
    (SELECT id FROM customers WHERE customer_number = 'C1000001'), -- Ahmad Suharto
    (SELECT id FROM products WHERE product_code = 'TAB001'), -- Tabungan Wadiah Basic
    (SELECT id FROM branches WHERE branch_code = 'HO001'), -- Kantor Pusat
    'STMT0000001',
    'Ahmad Suharto - Statement Test',
    500000.00,
    'ACTIVE',
    CURRENT_DATE - INTERVAL '6 months',
    'SELENIUM_STATEMENT_TEST',
    CURRENT_TIMESTAMP - INTERVAL '6 months'
);

-- Account 2: Corporate customer with fewer transactions
INSERT INTO accounts (
    id, 
    id_customers, 
    id_products, 
    id_branches,
    account_number, 
    account_name, 
    balance, 
    status, 
    opened_date,
    created_by,
    created_date
) VALUES (
    'f47ac10b-58cc-4372-a567-0e02b2c3d480'::uuid,
    (SELECT id FROM customers WHERE customer_number = 'C1000003'), -- PT. Teknologi Maju
    (SELECT id FROM products WHERE product_code = 'TAB002'), -- Tabungan Mudharabah Premium
    (SELECT id FROM branches WHERE branch_code = 'HO001'), -- Kantor Pusat
    'STMT0000002',
    'PT. Teknologi Maju - Corporate Statement',
    2500000.00,
    'ACTIVE',
    CURRENT_DATE - INTERVAL '3 months',
    'SELENIUM_STATEMENT_TEST',
    CURRENT_TIMESTAMP - INTERVAL '3 months'
);

-- Account 3: Account with no transactions (for empty statement testing)
INSERT INTO accounts (
    id, 
    id_customers, 
    id_products, 
    id_branches,
    account_number, 
    account_name, 
    balance, 
    status, 
    opened_date,
    created_by,
    created_date
) VALUES (
    'f47ac10b-58cc-4372-a567-0e02b2c3d481'::uuid,
    (SELECT id FROM customers WHERE customer_number = 'C1000002'), -- Siti Nurhaliza
    (SELECT id FROM products WHERE product_code = 'TAB001'), -- Tabungan Wadiah Basic
    (SELECT id FROM branches WHERE branch_code = 'HO001'), -- Kantor Pusat (for access control)
    'STMT0000003',
    'Siti Nurhaliza - Empty Statement',
    50000.00,
    'ACTIVE',
    CURRENT_DATE - INTERVAL '1 month',
    'SELENIUM_STATEMENT_TEST',
    CURRENT_TIMESTAMP - INTERVAL '1 month'
);

-- Generate transactions for Account 1 (Ahmad Suharto) - Mix of deposits and withdrawals
-- Initial deposit (6 months ago)
INSERT INTO transactions (
    id,
    id_accounts,
    transaction_number,
    transaction_type,
    amount,
    currency,
    balance_before,
    balance_after,
    description,
    channel,
    transaction_date,
    processed_date,
    created_by
) VALUES (
    gen_random_uuid(),
    'f47ac10b-58cc-4372-a567-0e02b2c3d479'::uuid,
    'STMT001',
    'DEPOSIT',
    1000000.00,
    'IDR',
    0.00,
    1000000.00,
    'Initial deposit - Statement test data',
    'TELLER',
    CURRENT_TIMESTAMP - INTERVAL '6 months',
    CURRENT_TIMESTAMP - INTERVAL '6 months',
    'SELENIUM_STATEMENT_TEST'
);

-- Monthly deposits (5 transactions over 5 months)
INSERT INTO transactions (
    id, id_accounts, transaction_number, transaction_type, amount, currency,
    balance_before, balance_after, description, channel, transaction_date, processed_date, created_by
) VALUES 
(gen_random_uuid(), 'f47ac10b-58cc-4372-a567-0e02b2c3d479'::uuid, 'STMT002', 'DEPOSIT', 250000.00, 'IDR', 1000000.00, 1250000.00, 'Salary deposit - Month 1', 'ONLINE', CURRENT_TIMESTAMP - INTERVAL '5 months', CURRENT_TIMESTAMP - INTERVAL '5 months', 'SELENIUM_STATEMENT_TEST'),
(gen_random_uuid(), 'f47ac10b-58cc-4372-a567-0e02b2c3d479'::uuid, 'STMT003', 'WITHDRAWAL', 100000.00, 'IDR', 1250000.00, 1150000.00, 'ATM withdrawal - Month 1', 'ATM', CURRENT_TIMESTAMP - INTERVAL '5 months' + INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 months' + INTERVAL '5 days', 'SELENIUM_STATEMENT_TEST'),
(gen_random_uuid(), 'f47ac10b-58cc-4372-a567-0e02b2c3d479'::uuid, 'STMT004', 'DEPOSIT', 300000.00, 'IDR', 1150000.00, 1450000.00, 'Business income - Month 2', 'MOBILE', CURRENT_TIMESTAMP - INTERVAL '4 months', CURRENT_TIMESTAMP - INTERVAL '4 months', 'SELENIUM_STATEMENT_TEST'),
(gen_random_uuid(), 'f47ac10b-58cc-4372-a567-0e02b2c3d479'::uuid, 'STMT005', 'WITHDRAWAL', 200000.00, 'IDR', 1450000.00, 1250000.00, 'Bill payment - Month 2', 'ONLINE', CURRENT_TIMESTAMP - INTERVAL '4 months' + INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '4 months' + INTERVAL '10 days', 'SELENIUM_STATEMENT_TEST'),
(gen_random_uuid(), 'f47ac10b-58cc-4372-a567-0e02b2c3d479'::uuid, 'STMT006', 'DEPOSIT', 150000.00, 'IDR', 1250000.00, 1400000.00, 'Freelance payment - Month 3', 'TELLER', CURRENT_TIMESTAMP - INTERVAL '3 months', CURRENT_TIMESTAMP - INTERVAL '3 months', 'SELENIUM_STATEMENT_TEST'),
(gen_random_uuid(), 'f47ac10b-58cc-4372-a567-0e02b2c3d479'::uuid, 'STMT007', 'WITHDRAWAL', 50000.00, 'IDR', 1400000.00, 1350000.00, 'Cash withdrawal - Month 3', 'ATM', CURRENT_TIMESTAMP - INTERVAL '3 months' + INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '3 months' + INTERVAL '15 days', 'SELENIUM_STATEMENT_TEST'),
(gen_random_uuid(), 'f47ac10b-58cc-4372-a567-0e02b2c3d479'::uuid, 'STMT008', 'DEPOSIT', 400000.00, 'IDR', 1350000.00, 1750000.00, 'Investment return - Month 4', 'TRANSFER', CURRENT_TIMESTAMP - INTERVAL '2 months', CURRENT_TIMESTAMP - INTERVAL '2 months', 'SELENIUM_STATEMENT_TEST'),
(gen_random_uuid(), 'f47ac10b-58cc-4372-a567-0e02b2c3d479'::uuid, 'STMT009', 'FEE', 5000.00, 'IDR', 1750000.00, 1745000.00, 'Monthly admin fee - Month 4', 'TELLER', CURRENT_TIMESTAMP - INTERVAL '2 months' + INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '2 months' + INTERVAL '1 day', 'SELENIUM_STATEMENT_TEST'),
(gen_random_uuid(), 'f47ac10b-58cc-4372-a567-0e02b2c3d479'::uuid, 'STMT010', 'WITHDRAWAL', 1245000.00, 'IDR', 1745000.00, 500000.00, 'Large withdrawal - Recent', 'TELLER', CURRENT_TIMESTAMP - INTERVAL '1 month', CURRENT_TIMESTAMP - INTERVAL '1 month', 'SELENIUM_STATEMENT_TEST');

-- Generate fewer transactions for Account 2 (PT. Teknologi Maju) - Corporate account
-- Initial deposit
INSERT INTO transactions (
    id, id_accounts, transaction_number, transaction_type, amount, currency,
    balance_before, balance_after, description, channel, transaction_date, processed_date, created_by
) VALUES 
(gen_random_uuid(), 'f47ac10b-58cc-4372-a567-0e02b2c3d480'::uuid, 'STMT011', 'DEPOSIT', 5000000.00, 'IDR', 0.00, 5000000.00, 'Initial corporate deposit', 'TELLER', CURRENT_TIMESTAMP - INTERVAL '3 months', CURRENT_TIMESTAMP - INTERVAL '3 months', 'SELENIUM_STATEMENT_TEST'),
(gen_random_uuid(), 'f47ac10b-58cc-4372-a567-0e02b2c3d480'::uuid, 'STMT012', 'WITHDRAWAL', 1500000.00, 'IDR', 5000000.00, 3500000.00, 'Operational expense', 'ONLINE', CURRENT_TIMESTAMP - INTERVAL '2 months', CURRENT_TIMESTAMP - INTERVAL '2 months', 'SELENIUM_STATEMENT_TEST'),
(gen_random_uuid(), 'f47ac10b-58cc-4372-a567-0e02b2c3d480'::uuid, 'STMT013', 'DEPOSIT', 2000000.00, 'IDR', 3500000.00, 5500000.00, 'Client payment received', 'TRANSFER', CURRENT_TIMESTAMP - INTERVAL '1 month', CURRENT_TIMESTAMP - INTERVAL '1 month', 'SELENIUM_STATEMENT_TEST'),
(gen_random_uuid(), 'f47ac10b-58cc-4372-a567-0e02b2c3d480'::uuid, 'STMT014', 'WITHDRAWAL', 3000000.00, 'IDR', 5500000.00, 2500000.00, 'Equipment purchase', 'TELLER', CURRENT_TIMESTAMP - INTERVAL '2 weeks', CURRENT_TIMESTAMP - INTERVAL '2 weeks', 'SELENIUM_STATEMENT_TEST');

-- No transactions for Account 3 (Empty statement test) - intentionally left empty

-- Update sequence numbers to avoid conflicts
UPDATE sequence_numbers SET last_number = last_number + 20 WHERE sequence_name = 'TRANSACTION_NUMBER';

-- Verify test data creation
-- SELECT 'Accounts created' as status, COUNT(*) as count FROM accounts WHERE created_by = 'SELENIUM_STATEMENT_TEST';
-- SELECT 'Transactions created' as status, COUNT(*) as count FROM transactions WHERE created_by = 'SELENIUM_STATEMENT_TEST';