-- Setup test data for customer management tests
DELETE FROM accounts;
DELETE FROM personal_customers;
DELETE FROM corporate_customers;
DELETE FROM customers;

-- Insert base customer records
INSERT INTO customers (id, customer_number, customer_type, email, phone_number, address, city, postal_code, country, created_by, created_date, updated_by, updated_date)
VALUES
    ('22222222-2222-2222-2222-222222222001', 'EDIT001', 'PERSONAL', 'john.doe@example.com', '081234567890', '123 Main Street', 'Jakarta', '12345', 'Indonesia', 'system', NOW(), 'system', NOW()),
    ('22222222-2222-2222-2222-222222222002', 'SEARCH001', 'PERSONAL', 'test.customer@example.com', '081234567891', '456 Search Street', 'Surabaya', '54321', 'Indonesia', 'system', NOW(), 'system', NOW()),
    ('22222222-2222-2222-2222-222222222003', 'STATUS001', 'PERSONAL', 'status.test@example.com', '081234567892', '789 Status Avenue', 'Bandung', '98765', 'Indonesia', 'system', NOW(), 'system', NOW()),
    ('22222222-2222-2222-2222-222222222004', 'VIEW001', 'CORPORATE', 'contact@testcorp.com', '081234567893', '100 Corporate Blvd', 'Jakarta', '11111', 'Indonesia', 'system', NOW(), 'system', NOW()),
    ('22222222-2222-2222-2222-222222222005', 'DUPLICATE001', 'PERSONAL', 'duplicate@example.com', '081234567894', '200 Duplicate Road', 'Yogyakarta', '55555', 'Indonesia', 'system', NOW(), 'system', NOW());

-- Insert personal customer specific data
INSERT INTO personal_customers (id, first_name, last_name, date_of_birth, identity_number, identity_type)
VALUES
    ('22222222-2222-2222-2222-222222222001', 'John', 'Doe', '1990-01-01', '1234567890123456', 'KTP'),
    ('22222222-2222-2222-2222-222222222002', 'Test', 'Customer', '1985-05-15', '1234567890123457', 'KTP'),
    ('22222222-2222-2222-2222-222222222003', 'Status', 'Test', '1992-12-25', '1234567890123458', 'KTP'),
    ('22222222-2222-2222-2222-222222222005', 'Duplicate', 'Test', '1988-08-08', '1234567890123459', 'KTP');

-- Insert corporate customer specific data
INSERT INTO corporate_customers (id, company_name, company_registration_number, tax_identification_number, contact_person_name, contact_person_title)
VALUES
    ('22222222-2222-2222-2222-222222222004', 'Test Corporation', '12345678901234', '12.345.678.9-012.000', 'Jane Smith', 'Manager');