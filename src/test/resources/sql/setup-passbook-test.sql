-- Setup script for passbook Selenium tests  
-- LEVERAGE migration data from V002__insert_initial_data.sql

-- Clean up only test accounts and transactions, preserve migration data
DELETE FROM transactions WHERE id_accounts IN (
    SELECT id FROM accounts WHERE created_by = 'PASSBOOK_TEST' OR account_number LIKE 'PB_%'
);
DELETE FROM accounts WHERE created_by = 'PASSBOOK_TEST' OR account_number LIKE 'PB_%';

-- NOTE: Migration provides all necessary base data for passbook tests:
--
-- CUSTOMERS (V002): Use existing migration customers
-- - C1000001: Ahmad Suharto (HO001 branch) - Use for passbook printing tests
-- - C1000002: Siti Nurhaliza (JKT01 branch) - Use for transaction history tests
-- 
-- PRODUCTS (V002): Use existing migration products
-- - TAB001: Tabungan Wadiah Basic (default product, min 50k)
-- - TAB002: Tabungan Mudharabah Premium (min 1M, profit sharing)
--
-- BRANCHES (V002): Use existing migration branches  
-- - HO001: Kantor Pusat Jakarta (main branch)
-- - JKT01: Jakarta Timur (secondary branch)
--
-- Create test accounts using migration customers + products + branches
INSERT INTO accounts (id, id_customers, id_products, id_branches, account_number, account_name, balance, status, opened_date, created_by, created_date)
SELECT 
    gen_random_uuid(),
    c.id,
    p.id, 
    c.id_branches,
    'PB_' || TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDD_HH24MISS') || '_' || substr(md5(random()::text), 1, 6),
    c.customer_number || ' - ' || p.product_name,
    500000.00,
    'ACTIVE',
    CURRENT_DATE - INTERVAL '30 days',
    'PASSBOOK_TEST',
    CURRENT_TIMESTAMP
FROM customers c, products p 
WHERE c.customer_number = 'C1000001' 
  AND p.product_code = 'TAB001'
ON CONFLICT (account_number) DO NOTHING;

-- Insert sample transactions for passbook testing using the created account
-- Split complex UNION ALL into separate statements for better reliability

-- First transaction: Account opening deposit
INSERT INTO transactions (id, id_accounts, transaction_number, transaction_type, amount, currency, balance_before, balance_after, description, reference_number, channel, transaction_date, processed_date, created_by)
SELECT 
    gen_random_uuid(),
    a.id,
    'T3000000',  -- Fixed transaction number for consistency
    'DEPOSIT',
    50000.00,
    'IDR',
    450000.00,
    500000.00,
    'Account opening deposit',
    'OPEN_' || TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDD'),
    'TELLER',
    CURRENT_DATE - INTERVAL '30 days',
    CURRENT_DATE - INTERVAL '30 days',
    'PASSBOOK_TEST'
FROM accounts a 
WHERE a.created_by = 'PASSBOOK_TEST'
LIMIT 1;

-- Second transaction: Monthly salary deposit  
INSERT INTO transactions (id, id_accounts, transaction_number, transaction_type, amount, currency, balance_before, balance_after, description, reference_number, channel, transaction_date, processed_date, created_by)
SELECT 
    gen_random_uuid(),
    a.id,
    'T3000001',  -- Fixed transaction number for consistency
    'DEPOSIT', 
    100000.00,
    'IDR',
    500000.00,
    600000.00,
    'Monthly salary deposit',
    'SAL_' || TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDD'), 
    'ONLINE',
    CURRENT_DATE - INTERVAL '15 days',
    CURRENT_DATE - INTERVAL '15 days',
    'PASSBOOK_TEST'
FROM accounts a
WHERE a.created_by = 'PASSBOOK_TEST' 
LIMIT 1;