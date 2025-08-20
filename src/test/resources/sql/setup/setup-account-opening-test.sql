-- Setup test data for account opening tests
-- LEVERAGE migration data for maximum reuse and consistency

-- Clean up only test-created accounts, preserve all migration data
DELETE FROM accounts WHERE created_by = 'TEST' OR account_number LIKE 'TEST_%';

-- NOTE: Migration V002__insert_initial_data.sql provides:
-- BRANCHES: HO001 (01234567-8901-2345-6789-012345678901), JKT01, BDG01, SBY01, YGY01
-- PRODUCTS: TAB001 (Tabungan Wadiah Basic), TAB002 (Mudharabah Premium), DEP001 (Deposito), PEM001-PEM002 (Financing)
-- CUSTOMERS: C1000001-C1000006 (4 personal + 1 corporate customers)  
-- SEQUENCE: ACCOUNT_NUMBER starting at A2000000

-- Tests should primarily use these migration entities:
-- - Customer: Use C1000001 (Ahmad Suharto) for personal account tests
-- - Customer: Use C1000003 (PT. Teknologi Maju) for corporate account tests  
-- - Product: Use TAB001 for basic savings account tests
-- - Product: Use TAB002 for premium savings account tests
-- - Product: Use DEP001 for deposit account tests
-- - Branch: Use HO001 (main branch) for account opening

-- Add minimal test-specific data only when migration data is insufficient