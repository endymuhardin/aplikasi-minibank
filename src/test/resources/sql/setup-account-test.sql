-- Setup script for account opening tests
-- This script creates test data needed for account opening functionality

-- Insert test branches (required for customers)
INSERT INTO branches (id, branch_code, branch_name, is_main_branch, status, address, city, country, postal_code, phone_number, email, manager_name, created_date, created_by) 
VALUES 
    ('11111111-1111-1111-1111-111111111111', 'MAIN', 'Main Branch', true, 'ACTIVE', 'Jl. Sudirman No. 1', 'Jakarta', 'Indonesia', '10110', '021-1234567', 'main@minibank.com', 'Manager One', CURRENT_TIMESTAMP, 'SYSTEM'),
    ('22222222-2222-2222-2222-222222222222', 'BRANCH2', 'Secondary Branch', false, 'ACTIVE', 'Jl. Thamrin No. 2', 'Jakarta', 'Indonesia', '10230', '021-7654321', 'branch2@minibank.com', 'Manager Two', CURRENT_TIMESTAMP, 'SYSTEM')
ON CONFLICT (branch_code) DO NOTHING;

-- Insert test customers that tests expect to exist
-- Clear any existing test customers first
DELETE FROM personal_customers WHERE id IN (
    SELECT id FROM customers WHERE customer_number IN ('C1000001', 'C1000002', 'C1000004', 'C1000006')
);
DELETE FROM corporate_customers WHERE id IN (
    SELECT id FROM customers WHERE customer_number = 'C1000003'
);
DELETE FROM customers WHERE customer_number IN ('C1000001', 'C1000002', 'C1000003', 'C1000004', 'C1000006');

-- Insert test customers
INSERT INTO customers (
    id, customer_type, customer_number, id_branches, email, phone_number, 
    address, city, postal_code, country, created_by, created_date, updated_by, updated_date
) VALUES 
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'PERSONAL', 'C1000001', '11111111-1111-1111-1111-111111111111', 'ahmad.suharto@email.com', '081234567890', 
 'Jl. Sudirman No. 123', 'Jakarta', '10220', 'Indonesia', 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP),

('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'PERSONAL', 'C1000002', '11111111-1111-1111-1111-111111111111', 'siti.nurhaliza@email.com', '081234567891', 
 'Jl. Thamrin No. 456', 'Jakarta', '10230', 'Indonesia', 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP),

('cccccccc-cccc-cccc-cccc-cccccccccccc', 'CORPORATE', 'C1000003', '11111111-1111-1111-1111-111111111111', 'info@teknologimaju.com', '02123456789', 
 'Jl. HR Rasuna Said No. 789', 'Jakarta', '12950', 'Indonesia', 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP),

('dddddddd-dddd-dddd-dddd-dddddddddddd', 'PERSONAL', 'C1000004', '11111111-1111-1111-1111-111111111111', 'budi.santoso@email.com', '081234567892', 
 'Jl. Gatot Subroto No. 321', 'Jakarta', '12930', 'Indonesia', 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP),

('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'PERSONAL', 'C1000006', '11111111-1111-1111-1111-111111111111', 'dewi.lestari@email.com', '081234567893', 
 'Jl. MH Thamrin No. 654', 'Jakarta', '10350', 'Indonesia', 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP);

-- Insert personal customer specific data
INSERT INTO personal_customers (
    id, first_name, last_name, date_of_birth, identity_number, identity_type
) VALUES 
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Ahmad', 'Suharto', '1985-03-15', '3271081503850001', 'KTP'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Siti', 'Nurhaliza', '1990-07-22', '3271082207900002', 'KTP'),
('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Budi', 'Santoso', '1988-11-10', '3271081011880003', 'KTP'),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Dewi', 'Lestari', '1992-05-18', '3271081805920004', 'KTP');

-- Insert corporate customer specific data
INSERT INTO corporate_customers (
    id, company_name, company_registration_number, tax_identification_number, contact_person_name, contact_person_title
) VALUES 
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'PT. Teknologi Maju', '1234567890123456', '01.234.567.8-901.000', 'John Doe', 'Director');

-- Insert test products suitable for account opening (savings and checking accounts)
INSERT INTO products (id, product_code, product_name, product_type, product_category, description, is_active, is_default, currency, minimum_opening_balance, minimum_balance, allowed_customer_types, profit_sharing_type, profit_distribution_frequency, is_shariah_compliant, nisbah_customer, nisbah_bank, created_date, created_by)
VALUES 
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'SAV001', 'Tabungan Wadiah Personal', 'TABUNGAN_WADIAH', 'Personal Banking', 'Islamic savings account based on Wadiah (safekeeping) contract for personal customers', true, true, 'IDR', 50000.00, 10000.00, 'PERSONAL', 'WADIAH', 'MONTHLY', true, 1.0000, 0.0000, CURRENT_TIMESTAMP, 'SYSTEM'),
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'SAV002', 'Tabungan Mudharabah Personal', 'TABUNGAN_MUDHARABAH', 'Personal Banking', 'Islamic savings account based on Mudharabah (profit sharing) contract for personal customers', true, false, 'IDR', 100000.00, 25000.00, 'PERSONAL', 'MUDHARABAH', 'MONTHLY', true, 0.7000, 0.3000, CURRENT_TIMESTAMP, 'SYSTEM'),
    ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'CHK001', 'Checking Account Personal', 'CHECKING', 'Personal Banking', 'Personal checking account for daily transactions', true, false, 'IDR', 250000.00, 50000.00, 'PERSONAL', 'WADIAH', 'MONTHLY', true, 1.0000, 0.0000, CURRENT_TIMESTAMP, 'SYSTEM'),
    ('99999999-9999-9999-9999-999999999999', 'SAV003', 'Tabungan Corporate', 'TABUNGAN_WADIAH', 'Corporate Banking', 'Islamic savings account for corporate customers', true, false, 'IDR', 500000.00, 100000.00, 'CORPORATE', 'WADIAH', 'MONTHLY', true, 1.0000, 0.0000, CURRENT_TIMESTAMP, 'SYSTEM'),
    ('88888888-8888-8888-8888-888888888888', 'SAV004', 'Tabungan Universal', 'SAVINGS', 'Universal Banking', 'Universal savings account for all customer types', true, false, 'IDR', 75000.00, 15000.00, 'PERSONAL,CORPORATE', 'WADIAH', 'MONTHLY', true, 1.0000, 0.0000, CURRENT_TIMESTAMP, 'SYSTEM'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'DEP001', 'Deposito Mudharabah Personal', 'DEPOSITO_MUDHARABAH', 'Personal Banking', 'Islamic term deposit based on Mudharabah (profit sharing) contract', true, false, 'IDR', 10000000.00, 10000000.00, 'PERSONAL', 'MUDHARABAH', 'MONTHLY', true, 0.6000, 0.4000, CURRENT_TIMESTAMP, 'SYSTEM'),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'DEP002', 'Deposito Mudharabah Corporate', 'DEPOSITO_MUDHARABAH', 'Corporate Banking', 'Islamic term deposit for corporate customers', true, false, 'IDR', 50000000.00, 50000000.00, 'CORPORATE', 'MUDHARABAH', 'MONTHLY', true, 0.5000, 0.5000, CURRENT_TIMESTAMP, 'SYSTEM')
ON CONFLICT (product_code) DO NOTHING;

-- Ensure sequence numbers are set up for account number generation
INSERT INTO sequence_numbers (id, sequence_name, last_number, prefix, created_date, updated_date)
VALUES 
    ('77777777-7777-7777-7777-777777777777', 'ACCOUNT_NUMBER', 2000000, 'ACC', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (sequence_name) DO UPDATE SET
    last_number = GREATEST(sequence_numbers.last_number, 2000000),
    updated_date = CURRENT_TIMESTAMP;