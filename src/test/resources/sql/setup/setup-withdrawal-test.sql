-- Setup test data for withdrawal functionality tests
-- Clear existing data first
DELETE FROM transactions;
DELETE FROM accounts;
DELETE FROM personal_customers;
DELETE FROM corporate_customers;
DELETE FROM customers;
DELETE FROM products;
DELETE FROM sequence_numbers;

-- Insert test customers
INSERT INTO customers (id, customer_number, customer_type, address, city, country, email, phone_number, postal_code, id_branches, created_by, created_date, updated_by, updated_date) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'C1000001', 'PERSONAL', 'Jl. Sudirman No. 1', 'Jakarta', 'Indonesia', 'john.doe@email.com', '+62123456789', '12345', '01234567-8901-2345-6789-012345678901', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'C1000002', 'CORPORATE', 'Jl. Thamrin No. 2', 'Jakarta', 'Indonesia', 'info@company.com', '+62123456790', '12346', '01234567-8901-2345-6789-012345678901', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'C1000003', 'PERSONAL', 'Jl. Gatot Subroto No. 3', 'Jakarta', 'Indonesia', 'jane.smith@email.com', '+62123456791', '12347', '01234567-8901-2345-6789-012345678901', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Insert personal customer details
INSERT INTO personal_customers (id, first_name, last_name, identity_type, identity_number, date_of_birth) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'John', 'Doe', 'KTP', '3175012345678901', '1990-01-15'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Jane', 'Smith', 'KTP', '3175012345678902', '1985-05-20');

-- Insert corporate customer details
INSERT INTO corporate_customers (id, company_name, company_registration_number, tax_identification_number, contact_person_name, contact_person_title) VALUES
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'ABC Company Ltd', '1234567890123456', '01.234.567.8-901.000', 'Robert Johnson', 'CEO');

-- Insert test products
INSERT INTO products (id, product_code, product_name, product_type, product_category, description, minimum_opening_balance, minimum_balance, maximum_balance, profit_sharing_ratio, profit_sharing_type, profit_distribution_frequency, nisbah_customer, nisbah_bank, is_shariah_compliant, monthly_maintenance_fee, currency, is_active, is_default, launch_date, created_by, created_date, updated_by, updated_date,
free_transactions_per_month, excess_transaction_fee, allow_overdraft, require_maintaining_balance, allowed_customer_types, required_documents,
atm_withdrawal_fee, inter_bank_transfer_fee, below_minimum_balance_fee, account_closure_fee) VALUES
('11111111-1111-1111-1111-111111111111', 'SAV001', 'Basic Savings', 'SAVINGS', 'RETAIL', 'Basic savings account for individuals', 100.00, 10.00, 1000000.00, 0.015, 'WADIAH', 'MONTHLY', NULL, NULL, true, 0.00, 'IDR', true, false, '2024-01-01', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP,
10, 2500, false, true, 'PERSONAL', 'KTP', 5000, 7500, 10000, 0),
('22222222-2222-2222-2222-222222222222', 'CHK001', 'Business Checking', 'CHECKING', 'BUSINESS', 'Business checking account for companies', 1000.00, 100.00, 10000000.00, 0.005, 'WADIAH', 'MONTHLY', NULL, NULL, true, 5.00, 'IDR', true, false, '2024-01-01', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP,
20, 3000, true, true, 'CORPORATE', 'Akta Pendirian, SIUP', 5000, 7500, 15000, 10000),
('33333333-3333-3333-3333-333333333333', 'SAV002', 'Premium Savings', 'SAVINGS', 'RETAIL', 'Premium savings account with higher interest', 500.00, 50.00, 5000000.00, 0.020, 'MUDHARABAH', 'MONTHLY', 0.7000, 0.3000, true, 0.00, 'IDR', true, false, '2024-01-01', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP,
25, 2500, false, true, 'PERSONAL', 'KTP, NPWP', 0, 5000, 25000, 0);

-- Insert test accounts with sufficient balance for withdrawal tests
INSERT INTO accounts (id, id_customers, id_products, id_branches, account_number, account_name, balance, status, opened_date, created_by, created_date, updated_by, updated_date) VALUES
('11111111-1111-1111-aaaa-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', '01234567-8901-2345-6789-012345678901', 'ACC0000001', 'John Doe Savings', 5000.00, 'ACTIVE', CURRENT_DATE, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('22222222-2222-2222-bbbb-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', '01234567-8901-2345-6789-012345678901', 'ACC0000002', 'ABC Company Checking', 100000.00, 'ACTIVE', CURRENT_DATE, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('33333333-3333-3333-cccc-333333333333', 'cccccccc-cccc-cccc-cccc-cccccccccccc', '33333333-3333-3333-3333-333333333333', '01234567-8901-2345-6789-012345678901', 'ACC0000003', 'Jane Smith Premium Savings', 1500.00, 'ACTIVE', CURRENT_DATE, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('44444444-4444-4444-dddd-444444444444', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', '01234567-8901-2345-6789-012345678901', 'ACC0000004', 'John Doe Second Account', 250.00, 'INACTIVE', CURRENT_DATE, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('55555555-5555-5555-eeee-555555555555', 'cccccccc-cccc-cccc-cccc-cccccccccccc', '33333333-3333-3333-3333-333333333333', '01234567-8901-2345-6789-012345678901', 'ACC0000005', 'Jane Low Balance Account', 10.00, 'ACTIVE', CURRENT_DATE, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Insert sequence numbers for transaction numbering
INSERT INTO sequence_numbers (sequence_name, prefix, last_number, created_date, updated_date) VALUES
('TRANSACTION_NUMBER', 'TXN', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);