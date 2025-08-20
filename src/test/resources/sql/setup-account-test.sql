-- Setup script for account opening Selenium tests
-- LEVERAGE migration data from V002__insert_initial_data.sql and V004__insert_roles_permissions_data.sql

-- Clean up only test-created accounts, preserve migration data
DELETE FROM accounts WHERE created_by = 'SELENIUM_TEST' OR account_number LIKE 'SELENIUM_%';

-- NOTE: Migration provides comprehensive base data for Selenium tests:
--
-- BRANCHES (V002): Use existing migration branches
-- - HO001: Kantor Pusat Jakarta (ID: 01234567-8901-2345-6789-012345678901) - MAIN BRANCH
-- - JKT01: Jakarta Timur, BDG01: Bandung, SBY01: Surabaya, YGY01: Yogyakarta
--
-- CUSTOMERS (V002): Use existing migration customers
-- - C1000001: Ahmad Suharto (Personal, HO001 branch)
-- - C1000002: Siti Nurhaliza (Personal, JKT01 branch) 
-- - C1000004: Budi Santoso (Personal, BDG01 branch)
-- - C1000006: Dewi Lestari (Personal, SBY01 branch)
-- - C1000003: PT. Teknologi Maju (Corporate, HO001 branch)
--
-- PRODUCTS (V002): Use existing migration products 
-- - TAB001: Tabungan Wadiah Basic (PERSONAL, default, min 50k)
-- - TAB002: Tabungan Mudharabah Premium (PERSONAL, min 1M)
-- - DEP001: Deposito Mudharabah (PERSONAL/CORPORATE, min 100k)
-- - PEM001: Pembiayaan Murabahah (CORPORATE only, min 5M)
-- - PEM002: Pembiayaan Musharakah (PERSONAL, min 2M)
--
-- USERS & RBAC (V004): Already leveraged by LoginHelper
-- - cs1, teller1, admin with password: minibank123
-- - Roles: CUSTOMER_SERVICE, TELLER, BRANCH_MANAGER
-- - Full permission system already configured
--
-- SEQUENCES (V002): Account numbers start at A2000001

-- Add minimal test-specific data only for unique Selenium scenarios
-- Most account opening tests should use migration customers + products + branches