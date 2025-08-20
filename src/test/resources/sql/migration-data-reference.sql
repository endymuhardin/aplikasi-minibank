-- MIGRATION DATA REFERENCE
-- Use this reference when writing tests to leverage existing migration data
-- Data source: V002__insert_initial_data.sql and V004__insert_roles_permissions_data.sql

-- =============================================================================
-- BRANCHES (V002__insert_initial_data.sql)
-- =============================================================================
-- Available branches for testing:

-- Main Branch (use for primary tests):
-- ID: '01234567-8901-2345-6789-012345678901'
-- Code: 'HO001', Name: 'Kantor Pusat Jakarta'
-- Manager: 'H. Ahmad Surya', is_main_branch: true

-- Other Branches (use for multi-branch tests):
-- JKT01: Jakarta Timur, Manager: Drs. Budi Pratama
-- BDG01: Bandung, Manager: H. Siti Nurhalimah  
-- SBY01: Surabaya, Manager: Ir. Wahyu Setiawan
-- YGY01: Yogyakarta, Manager: Dr. Retno Wulandari

-- =============================================================================
-- SEQUENCE NUMBERS (V002__insert_initial_data.sql)
-- =============================================================================
-- CUSTOMER_NUMBER: starts at C1000001 (last: 1000000, prefix: 'C')
-- ACCOUNT_NUMBER: starts at A2000001 (last: 2000000, prefix: 'A')  
-- TRANSACTION_NUMBER: starts at T3000001 (last: 3000000, prefix: 'T')

-- =============================================================================
-- PRODUCTS (V002__insert_initial_data.sql)
-- =============================================================================

-- TAB001: Tabungan Wadiah Basic
-- Type: TABUNGAN_WADIAH, Active: true, Default: true
-- Min Opening: 50000, Min Balance: 10000, Currency: IDR
-- Use for: Basic savings account tests, default product tests

-- TAB002: Tabungan Mudharabah Premium  
-- Type: TABUNGAN_MUDHARABAH, Active: true, Default: false
-- Min Opening: 1000000, Min Balance: 500000
-- Nisbah: 70% customer, 30% bank
-- Use for: Premium account tests, profit sharing tests

-- DEP001: Deposito Mudharabah
-- Type: DEPOSITO_MUDHARABAH, Active: true, Default: false
-- Min Opening: 100000, Min Balance: 50000
-- Use for: Deposit account tests, time deposit features

-- PEM001: Pembiayaan Murabahah (CORPORATE only)
-- Type: PEMBIAYAAN_MURABAHAH, Active: true
-- Min Opening: 5000000, For: CORPORATE customers only
-- Use for: Corporate financing tests

-- PEM002: Pembiayaan Musharakah (PERSONAL)
-- Type: PEMBIAYAAN_MUSHARAKAH, Active: true  
-- Min Opening: 2000000, For: PERSONAL customers
-- Nisbah: 60% customer, 40% bank
-- Use for: Personal financing tests

-- =============================================================================
-- CUSTOMERS (V002__insert_initial_data.sql)
-- =============================================================================

-- PERSONAL CUSTOMERS (use for individual account tests):

-- C1000001: Ahmad Suharto
-- Email: ahmad.suharto@email.com, Phone: 081234567890
-- Address: Jl. Sudirman No. 123, Jakarta 10220
-- Identity: KTP 3271081503850001, DOB: 1985-03-15

-- C1000002: Siti Nurhaliza
-- Email: siti.nurhaliza@email.com, Phone: 081234567891
-- Address: Jl. Thamrin No. 456, Jakarta 10230
-- Identity: KTP 3271082207900002, DOB: 1990-07-22

-- C1000004: Budi Santoso
-- Email: budi.santoso@email.com, Phone: 081234567892
-- Address: Jl. Gatot Subroto No. 321, Jakarta 12930
-- Identity: KTP 3271081011880003, DOB: 1988-11-10

-- C1000006: Dewi Lestari
-- Email: dewi.lestari@email.com, Phone: 081234567893
-- Address: Jl. MH Thamrin No. 654, Jakarta 10350
-- Identity: KTP 3271081805920004, DOB: 1992-05-18

-- CORPORATE CUSTOMERS (use for business account tests):

-- C1000003: PT. Teknologi Maju
-- Email: info@teknologimaju.com, Phone: 02123456789
-- Address: Jl. HR Rasuna Said No. 789, Jakarta 12950
-- Registration: 1234567890123456, NPWP: 01.234.567.8-901.000

-- =============================================================================
-- RBAC SYSTEM (V004__insert_roles_permissions_data.sql)
-- =============================================================================

-- ROLES (use existing roles instead of creating custom ones):
-- CUSTOMER_SERVICE: Customer registration, account opening
-- TELLER: Financial transactions, balance inquiries
-- BRANCH_MANAGER: All permissions, approvals, reports

-- SAMPLE USERS (password: 'minibank123' for all):

-- Branch Managers:
-- admin: System Administrator (HO001)
-- manager1: Branch Manager Jakarta (JKT01) 
-- manager2: Branch Manager Surabaya (SBY01)

-- Tellers:
-- teller1: Teller Counter 1 (HO001)
-- teller2: Teller Counter 2 (JKT01)
-- teller3: Teller Counter 3 (BDG01)

-- Customer Service:
-- cs1: Customer Service Staff 1 (HO001)
-- cs2: Customer Service Staff 2 (JKT01)
-- cs3: Customer Service Staff 3 (YGY01)

-- PERMISSIONS (comprehensive set already available):
-- CUSTOMER_*: VIEW, CREATE, UPDATE
-- ACCOUNT_*: VIEW, CREATE, UPDATE, APPROVE
-- TRANSACTION_*: VIEW, DEPOSIT, WITHDRAWAL, TRANSFER, APPROVE
-- BALANCE_VIEW, PRODUCT_VIEW
-- REPORT_VIEW, AUDIT_VIEW
-- USER_*: VIEW, CREATE, UPDATE, DEACTIVATE

-- =============================================================================
-- TESTING BEST PRACTICES
-- =============================================================================

-- 1. USE migration data as primary test data source
-- 2. CREATE minimal custom test data only when migration data is insufficient
-- 3. PRESERVE migration data in cleanup scripts (only delete test-created data)
-- 4. REFERENCE migration entities by their business keys (customer numbers, product codes, etc.)
-- 5. DOCUMENT any custom test data requirements clearly

-- =============================================================================
-- EXAMPLE USAGE IN TESTS
-- =============================================================================

/*
-- Good: Use migration customer
INSERT INTO accounts (customer_id, product_id, branch_id, ...) 
SELECT c.id, p.id, b.id, ... 
FROM customers c, products p, branches b
WHERE c.customer_number = 'C1000001'  -- Migration customer
  AND p.product_code = 'TAB001'       -- Migration product  
  AND b.branch_code = 'HO001';        -- Migration branch

-- Avoid: Creating duplicate customer data
INSERT INTO customers (id, customer_number, ...) 
VALUES ('custom-uuid', 'TEST001', ...);
*/