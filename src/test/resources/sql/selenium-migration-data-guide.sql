-- SELENIUM TEST MIGRATION DATA GUIDE
-- Comprehensive reference for Selenium tests to leverage existing migration data
-- Updated: August 2025
-- Sources: V002__insert_initial_data.sql, V004__insert_roles_permissions_data.sql

-- =============================================================================
-- 🏛️ BRANCHES (V002) - Use existing branches instead of creating test ones
-- =============================================================================

-- PRIMARY BRANCH (use for most tests):
-- ID: '01234567-8901-2345-6789-012345678901'
-- Code: 'HO001', Name: 'Kantor Pusat Jakarta' 
-- Manager: 'H. Ahmad Surya', is_main_branch: true
-- Address: 'Jl. Sudirman Kav. 10-11', City: 'Jakarta Pusat'

-- SECONDARY BRANCHES (use for multi-branch scenarios):
-- JKT01: Jakarta Timur (Manager: Drs. Budi Pratama)
-- BDG01: Bandung (Manager: H. Siti Nurhalimah)  
-- SBY01: Surabaya (Manager: Ir. Wahyu Setiawan)
-- YGY01: Yogyakarta (Manager: Dr. Retno Wulandari)

-- =============================================================================
-- 👥 CUSTOMERS (V002) - Use existing customers for account/transaction tests
-- =============================================================================

-- PERSONAL CUSTOMERS:
-- C1000001: Ahmad Suharto (HO001) - ahmad.suharto@email.com - KTP: 3271081503850001
-- C1000002: Siti Nurhaliza (JKT01) - siti.nurhaliza@email.com - KTP: 3271082207900002  
-- C1000004: Budi Santoso (BDG01) - budi.santoso@email.com - KTP: 3271081011880003
-- C1000006: Dewi Lestari (SBY01) - dewi.lestari@email.com - KTP: 3271081805920004

-- CORPORATE CUSTOMERS:
-- C1000003: PT. Teknologi Maju (HO001) - info@teknologimaju.com
--           Registration: 1234567890123456, NPWP: 01.234.567.8-901.000

-- =============================================================================
-- 🏦 PRODUCTS (V002) - Use existing Islamic banking products
-- =============================================================================

-- SAVINGS PRODUCTS:
-- TAB001: Tabungan Wadiah Basic
--         Type: TABUNGAN_WADIAH, Default: true, Active: true
--         Min Opening: 50,000 IDR, Min Balance: 10,000 IDR
--         For: PERSONAL customers, Contract: WADIAH
--         Use for: Basic savings account tests, default product selection

-- TAB002: Tabungan Mudharabah Premium  
--         Type: TABUNGAN_MUDHARABAH, Default: false, Active: true
--         Min Opening: 1,000,000 IDR, Min Balance: 500,000 IDR
--         Profit Sharing: 70% customer, 30% bank
--         For: PERSONAL customers, Contract: MUDHARABAH
--         Use for: Premium account tests, profit sharing scenarios

-- DEPOSIT PRODUCTS:
-- DEP001: Deposito Mudharabah
--         Type: DEPOSITO_MUDHARABAH, Active: true
--         Min Opening: 100,000 IDR, Min Balance: 50,000 IDR
--         Profit Sharing: 70% customer, 30% bank
--         For: PERSONAL/CORPORATE customers
--         Use for: Time deposit tests, deposit account opening

-- FINANCING PRODUCTS:
-- PEM001: Pembiayaan Murabahah (CORPORATE only)
--         Type: PEMBIAYAAN_MURABAHAH, Active: true
--         Min Opening: 5,000,000 IDR
--         For: CORPORATE customers only
--         Use for: Corporate financing tests

-- PEM002: Pembiayaan Musharakah (PERSONAL)
--         Type: PEMBIAYAAN_MUSHARAKAH, Active: true
--         Min Opening: 2,000,000 IDR
--         Profit Sharing: 60% customer, 40% bank
--         For: PERSONAL customers
--         Use for: Personal financing tests

-- =============================================================================
-- 👨‍💼 USERS & RBAC (V004) - Use existing users with known credentials
-- =============================================================================

-- ALL USERS have password: 'minibank123'

-- BRANCH MANAGERS (full permissions):
-- admin: System Administrator (HO001) - admin@yopmail.com
-- manager1: Branch Manager Jakarta (JKT01) - manager1@yopmail.com
-- manager2: Branch Manager Surabaya (SBY01) - manager2@yopmail.com

-- TELLERS (transaction permissions):
-- teller1: Teller Counter 1 (HO001) - teller1@yopmail.com
-- teller2: Teller Counter 2 (JKT01) - teller2@yopmail.com
-- teller3: Teller Counter 3 (BDG01) - teller3@yopmail.com

-- CUSTOMER SERVICE (customer/account management):
-- cs1: Customer Service Staff 1 (HO001) - cs1@yopmail.com
-- cs2: Customer Service Staff 2 (JKT01) - cs2@yopmail.com
-- cs3: Customer Service Staff 3 (YGY01) - cs3@yopmail.com

-- ROLES & PERMISSIONS:
-- CUSTOMER_SERVICE: CUSTOMER_*, ACCOUNT_*, PRODUCT_VIEW
-- TELLER: CUSTOMER_VIEW, ACCOUNT_VIEW, TRANSACTION_*, BALANCE_VIEW, PRODUCT_VIEW
-- BRANCH_MANAGER: ALL permissions (USER_*, REPORT_*, AUDIT_*, TRANSACTION_APPROVE, etc.)

-- =============================================================================
-- 🔢 SEQUENCES (V002) - Automatic number generation
-- =============================================================================
-- CUSTOMER_NUMBER: Starts at C1000001 (next: C1000007)
-- ACCOUNT_NUMBER: Starts at A2000001
-- TRANSACTION_NUMBER: Starts at T3000001

-- =============================================================================
-- 🧪 SELENIUM TEST PATTERNS - Best practices for using migration data
-- =============================================================================

/*
-- ✅ GOOD: Use migration customer and product for account opening
INSERT INTO accounts (id_customers, id_products, id_branches, ...)
SELECT c.id, p.id, c.id_branches, ...
FROM customers c, products p  
WHERE c.customer_number = 'C1000001'  -- Migration customer
  AND p.product_code = 'TAB001';      -- Migration product

-- ✅ GOOD: Use migration users in LoginHelper (already implemented)
LoginHelper.loginAsCustomerServiceUser() // uses cs1
LoginHelper.loginAsTeller()              // uses teller1  
LoginHelper.loginAsManager()             // uses admin

-- ❌ AVOID: Creating duplicate entities
INSERT INTO customers (customer_number, ...) VALUES ('TEST001', ...);
INSERT INTO products (product_code, ...) VALUES ('TESTSAV', ...);
*/

-- =============================================================================
-- 🧹 CLEANUP PATTERNS - Preserve migration data
-- =============================================================================

/*
-- ✅ GOOD: Clean up only test-created data
DELETE FROM accounts WHERE created_by = 'SELENIUM_TEST';
DELETE FROM transactions WHERE reference_number LIKE 'TEST_%';

-- ❌ AVOID: Deleting migration data  
DELETE FROM customers WHERE customer_number LIKE 'C%';
DELETE FROM products WHERE product_code IN ('TAB001', 'TAB002');
*/

-- =============================================================================
-- 📋 SELENIUM TEST SCENARIOS - Migration data usage examples
-- =============================================================================

-- ACCOUNT OPENING TESTS:
-- Customer: C1000001 (Ahmad Suharto) + Product: TAB001 (Tabungan Wadiah) + Branch: HO001

-- TRANSACTION TESTS:
-- Use accounts created from migration customers, process deposits/withdrawals

-- CUSTOMER MANAGEMENT TESTS:
-- Search/edit existing customers: C1000001-C1000006

-- PRODUCT MANAGEMENT TESTS:
-- Display/filter existing products: TAB001, TAB002, DEP001, PEM001, PEM002

-- RBAC TESTS:
-- Test role permissions using existing users: admin, teller1, cs1

-- PASSBOOK TESTS:
-- Create test account for C1000001 with TAB001, generate transaction history

-- BRANCH MANAGEMENT TESTS:
-- Use existing branches: HO001, JKT01, BDG01, SBY01, YGY01

-- =============================================================================
-- 🚀 MIGRATION BENEFITS FOR SELENIUM TESTS
-- =============================================================================

-- 1. CONSISTENCY: Same data as production application
-- 2. PERFORMANCE: No need to create/delete base entities repeatedly  
-- 3. RELIABILITY: Stable, well-tested entity relationships
-- 4. MAINTENANCE: Changes to migration data automatically propagate
-- 5. REALISM: Tests use actual business data patterns
-- 6. SPEED: Faster test setup and execution

-- =============================================================================
-- ⚠️ IMPORTANT NOTES
-- =============================================================================

-- 1. Always reference migration entities by business keys (customer_number, product_code, etc.)
-- 2. Use meaningful created_by values for test data (e.g., 'SELENIUM_TEST', 'PASSBOOK_TEST')
-- 3. Preserve migration data in all cleanup operations
-- 4. Document any custom test data requirements clearly
-- 5. Test both individual and batch operations using migration entities