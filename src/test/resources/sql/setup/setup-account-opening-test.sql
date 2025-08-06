-- Setup test data for account opening tests

-- Clean up existing test data first
DELETE FROM accounts WHERE id_customers IN (SELECT id FROM customers WHERE customer_number LIKE 'C%');
DELETE FROM customers WHERE customer_number LIKE 'C%';
DELETE FROM products WHERE product_code IN ('SAV001', 'SAV002', 'SAV003', 'CHK001', 'CHK002');
DELETE FROM sequence_numbers WHERE sequence_name = 'ACCOUNT_NUMBER';

-- Insert test products
INSERT INTO products (id, product_code, product_name, product_type, product_category, description, is_active, is_default, currency, minimum_opening_balance, minimum_balance, maximum_balance, daily_withdrawal_limit, monthly_transaction_limit, interest_rate, interest_calculation_type, interest_payment_frequency, monthly_maintenance_fee, atm_withdrawal_fee, inter_bank_transfer_fee, below_minimum_balance_fee, account_closure_fee, free_transactions_per_month, excess_transaction_fee, allow_overdraft, require_maintaining_balance, min_customer_age, max_customer_age, allowed_customer_types, required_documents, created_date, updated_date) 
VALUES 
('11111111-1111-1111-1111-111111111111', 'SAV001', 'Basic Savings Account', 'SAVINGS', 'Regular Savings', 'Basic savings account for individual customers', true, true, 'IDR', 50000.00, 10000.00, null, 5000000.00, 50, 0.0275, 'DAILY', 'MONTHLY', 2500.00, 5000.00, 7500.00, 10000.00, 0.00, 10, 2500.00, false, true, 17, null, 'PERSONAL', 'KTP, NPWP (optional)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22222222-2222-2222-2222-222222222222', 'SAV002', 'Premium Savings Account', 'SAVINGS', 'Premium Savings', 'Premium savings account with higher interest', true, false, 'IDR', 1000000.00, 500000.00, null, 10000000.00, 100, 0.0350, 'DAILY', 'MONTHLY', 0.00, 0.00, 5000.00, 25000.00, 0.00, 25, 2500.00, false, true, 21, null, 'PERSONAL', 'KTP, NPWP, Slip Gaji', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('33333333-3333-3333-3333-333333333333', 'CHK001', 'Basic Checking Account', 'CHECKING', 'Regular Checking', 'Basic checking account with overdraft facility', true, false, 'IDR', 100000.00, 50000.00, null, 20000000.00, 100, 0.0100, 'DAILY', 'MONTHLY', 5000.00, 5000.00, 7500.00, 15000.00, 10000.00, 20, 3000.00, true, true, 18, null, 'PERSONAL', 'KTP, NPWP, Slip Gaji', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('44444444-4444-4444-4444-444444444444', 'SAV003', 'Corporate Savings Account', 'SAVINGS', 'Corporate', 'Savings account for corporate customers', true, false, 'IDR', 5000000.00, 1000000.00, null, 50000000.00, 200, 0.0300, 'DAILY', 'MONTHLY', 15000.00, 5000.00, 5000.00, 50000.00, 25000.00, 50, 5000.00, false, true, null, null, 'CORPORATE', 'Akta Pendirian, SIUP, TDP, NPWP', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('55555555-5555-5555-5555-555555555555', 'CHK002', 'Premium Checking Account', 'CHECKING', 'Premium Checking', 'Premium checking with higher overdraft limit', true, false, 'IDR', 2000000.00, 1000000.00, null, 50000000.00, 200, 0.0150, 'DAILY', 'MONTHLY', 0.00, 0.00, 5000.00, 25000.00, 15000.00, 50, 3000.00, true, true, 25, null, 'PERSONAL', 'KTP, NPWP, Slip Gaji, Rekening Koran', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test customers (personal)
INSERT INTO customers (id, customer_number, email, phone_number, address, city, postal_code, country, created_date, updated_date, customer_type)
VALUES 
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'C1000001', 'ahmad.suharto@email.com', '081234567890', 'Jl. Sudirman No. 123', 'Jakarta', '10220', 'Indonesia', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PERSONAL'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'C1000002', 'siti.nurhaliza@email.com', '081234567891', 'Jl. Thamrin No. 456', 'Jakarta', '10230', 'Indonesia', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PERSONAL'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'C1000003', 'budi.santoso@email.com', '081234567892', 'Jl. Gatot Subroto No. 789', 'Jakarta', '12950', 'Indonesia', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PERSONAL'),
('ffffffff-ffff-ffff-ffff-ffffffffffff', 'C1000006', 'dewi.lestari@email.com', '081234567893', 'Jl. Senayan No. 321', 'Jakarta', '10270', 'Indonesia', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PERSONAL');

-- Insert personal customer details
INSERT INTO personal_customers (id, first_name, last_name, date_of_birth, identity_number, identity_type)
VALUES 
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Ahmad', 'Suharto', '1985-03-15', '3271081503850001', 'KTP'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Siti', 'Nurhaliza', '1990-07-22', '3271082207900002', 'KTP'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Budi', 'Santoso', '1988-12-10', '3271081012880003', 'KTP'),
('ffffffff-ffff-ffff-ffff-ffffffffffff', 'Dewi', 'Lestari', '1992-05-18', '3271051892920004', 'PASSPORT');

-- Insert corporate customers
INSERT INTO customers (id, customer_number, email, phone_number, address, city, postal_code, country, created_date, updated_date, customer_type)
VALUES 
('dddddddd-dddd-dddd-dddd-dddddddddddd', 'C1000004', 'info@teknologimaju.com', '02123456789', 'Jl. HR Rasuna Said No. 789', 'Jakarta', '12950', 'Indonesia', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'CORPORATE'),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'C1000005', 'admin@berkahjaya.com', '02187654321', 'Jl. Kuningan Raya No. 456', 'Jakarta', '12940', 'Indonesia', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'CORPORATE');

-- Insert corporate customer details
INSERT INTO corporate_customers (id, company_name, company_registration_number, tax_identification_number)
VALUES 
('dddddddd-dddd-dddd-dddd-dddddddddddd', 'PT. Teknologi Maju', '1234567890123456', '01.234.567.8-901.000'),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'CV. Berkah Jaya', '9876543210987654', '98.765.432.1-098.000');

-- Initialize account number sequence
INSERT INTO sequence_numbers (id, sequence_name, last_number, prefix, created_date, updated_date)
VALUES ('99999999-9999-9999-9999-999999999999', 'ACCOUNT_NUMBER', 0, 'ACC', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);