-- Cleanup script for account opening tests
-- This script removes test data created during account opening tests

-- First, delete all transactions for test accounts (if any)
DELETE FROM transactions WHERE id_accounts IN (
    SELECT id FROM accounts WHERE 
    (created_by IN ('teller1', 'cs1', 'manager1', 'admin', 'SYSTEM', 'TEST_USER') 
     OR account_number LIKE 'ACC%' 
     OR account_number LIKE 'A%')
    AND created_date > CURRENT_TIMESTAMP - INTERVAL '2 hours'
);

-- Delete all test accounts (broader criteria to catch all test accounts)
DELETE FROM accounts WHERE 
    (created_by IN ('teller1', 'cs1', 'manager1', 'admin', 'SYSTEM', 'TEST_USER') 
     OR account_number LIKE 'ACC%' 
     OR account_number LIKE 'A%')
    AND created_date > CURRENT_TIMESTAMP - INTERVAL '2 hours';

-- Now safe to delete test products (no foreign key references remain)
DELETE FROM products WHERE product_code IN ('SAV001', 'SAV002', 'CHK001', 'SAV003', 'SAV004', 'DEP001', 'DEP002');

-- Delete test branches (check for no remaining references)
DELETE FROM branches WHERE branch_code IN ('MAIN', 'BRANCH2') 
    AND NOT EXISTS (SELECT 1 FROM customers WHERE id_branches = branches.id);