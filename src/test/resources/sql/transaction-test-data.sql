-- Transaction Test Data Setup
-- Leverages existing seed data (customers C1000001-C1000006, accounts A2000001-A2000004)
-- Adds minimal additional test accounts with TXN_ prefix for specific test scenarios
-- Uses timestamp-based suffixes to avoid collisions: #{T-sql.nanoTime()%100000000}
-- Mindful of column length constraints: account_number (50), account_name (200)

-- Note: Seed data already provides:
-- A2000001: Ahmad Suharto - 1,000,000 IDR (C1000001)  
-- A2000002: Siti Nurhaliza - 2,500,000 IDR (C1000002)
-- A2000003: Budi Santoso - 750,000 IDR (C1000004)
-- A2000004: Dewi Lestari - 3,200,000 IDR (C1000006)
-- These are sufficient for most transaction tests!

-- Additional test account for specific edge cases only
-- High balance account for large transaction testing
INSERT INTO accounts (id, id_customers, id_products, id_branches, account_number, account_name, balance, status, created_by, created_date)
SELECT 
    gen_random_uuid(),
    (SELECT id FROM customers WHERE customer_number = 'C1000001'),  -- Use existing Ahmad Suharto
    (SELECT id FROM products WHERE product_code = 'TAB001'),         -- Use existing Tabungan Wadiah  
    (SELECT id FROM branches LIMIT 1),
    'TXN_A_' || LPAD((EXTRACT(EPOCH FROM NOW())::BIGINT % 100000000)::TEXT, 8, '0'),  -- TXN_A_12345678 format
    'TXN Test Account - High Balance Test',
    10000000.00,  -- 10 million for high-value transaction tests
    'ACTIVE',
    'TEST_SYSTEM',
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM accounts 
    WHERE account_name = 'TXN Test Account - High Balance Test' 
    AND id_customers = (SELECT id FROM customers WHERE customer_number = 'C1000001')
);

-- Low balance account for insufficient fund testing  
INSERT INTO accounts (id, id_customers, id_products, id_branches, account_number, account_name, balance, status, created_by, created_date)
SELECT 
    gen_random_uuid(),
    (SELECT id FROM customers WHERE customer_number = 'C1000002'),  -- Use existing Siti Nurhaliza
    (SELECT id FROM products WHERE product_code = 'TAB002'),         -- Use existing Tabungan Mudharabah
    (SELECT id FROM branches LIMIT 1),
    'TXN_B_' || LPAD((EXTRACT(EPOCH FROM NOW())::BIGINT % 100000000 + 1)::TEXT, 8, '0'),  -- TXN_B_12345679 format (+1 for uniqueness)
    'TXN Test Account - Low Balance Test',
    50000.00,  -- 50k for insufficient fund tests
    'ACTIVE',
    'TEST_SYSTEM',
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM accounts 
    WHERE account_name = 'TXN Test Account - Low Balance Test' 
    AND id_customers = (SELECT id FROM customers WHERE customer_number = 'C1000002')
);