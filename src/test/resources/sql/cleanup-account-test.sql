-- Cleanup script for account opening tests
-- This script removes test data created during account opening tests

-- Delete test accounts (this will also clean up any transactions via cascade)
DELETE FROM accounts WHERE created_by IN ('teller1', 'SYSTEM', 'TEST_USER') 
    AND account_number LIKE 'ACC%' 
    AND created_date > CURRENT_TIMESTAMP - INTERVAL '1 hour';

-- Note: We don't delete customers since we're using migration data
-- Just clean up test accounts that were created during tests

-- Delete test products
DELETE FROM products WHERE product_code IN ('SAV001', 'SAV002', 'CHK001', 'SAV003', 'SAV004');

-- Delete test branches
DELETE FROM branches WHERE branch_code IN ('MAIN', 'BRANCH2');