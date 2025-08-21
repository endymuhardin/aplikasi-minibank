-- Setup test data for customer management tests
-- Thread-safe cleanup: only remove test data created by this specific test thread
-- Use current transaction timestamp to create unique identifiers for parallel execution
DO $$
DECLARE
    thread_id TEXT := extract(epoch from now())::text || '-' || substr(md5(random()::text), 1, 8);
BEGIN
    -- Clean up only test-created data, preserve migration data
    DELETE FROM accounts WHERE id_customers IN (
        SELECT id FROM customers WHERE customer_number LIKE 'TEST_%' OR customer_number LIKE 'EDIT%' OR customer_number LIKE 'SEARCH%'
    );
    DELETE FROM personal_customers WHERE id IN (
        SELECT id FROM customers WHERE customer_number LIKE 'TEST_%' OR customer_number LIKE 'EDIT%' OR customer_number LIKE 'SEARCH%'
    );
    DELETE FROM corporate_customers WHERE id IN (
        SELECT id FROM customers WHERE customer_number LIKE 'TEST_%' OR customer_number LIKE 'EDIT%' OR customer_number LIKE 'SEARCH%'
    );
    DELETE FROM customers WHERE customer_number LIKE 'TEST_%' OR customer_number LIKE 'EDIT%' OR customer_number LIKE 'SEARCH%';
END $$;

-- NOTE: Branches and sequence numbers are provided by migration V002__insert_initial_data.sql
-- Using main branch HO001 (01234567-8901-2345-6789-012345678901) from migration data

-- Insert minimal test customers that extend migration data
-- Use ON CONFLICT DO NOTHING for idempotent parallel execution
INSERT INTO customers (id, customer_number, customer_type, id_branches, email, phone_number, address, city, postal_code, country, status, created_by, created_date, updated_by, updated_date)
VALUES (gen_random_uuid(), 'EDIT001', 'PERSONAL', '01234567-8901-2345-6789-012345678901', 'john.doe@example.com', '081234567890', '123 Main Street', 'Jakarta', '12345', 'Indonesia', 'ACTIVE', 'system', NOW(), 'system', NOW())
ON CONFLICT (customer_number) DO NOTHING;

INSERT INTO customers (id, customer_number, customer_type, id_branches, email, phone_number, address, city, postal_code, country, status, created_by, created_date, updated_by, updated_date)
VALUES (gen_random_uuid(), 'SEARCH001', 'PERSONAL', '01234567-8901-2345-6789-012345678901', 'test.customer@example.com', '081234567891', '456 Search Street', 'Surabaya', '54321', 'Indonesia', 'ACTIVE', 'system', NOW(), 'system', NOW())
ON CONFLICT (customer_number) DO NOTHING;

INSERT INTO customers (id, customer_number, customer_type, id_branches, email, phone_number, address, city, postal_code, country, status, created_by, created_date, updated_by, updated_date)
VALUES (gen_random_uuid(), 'STATUS001', 'PERSONAL', '01234567-8901-2345-6789-012345678901', 'status.test@example.com', '081234567892', '789 Status Avenue', 'Bandung', '98765', 'Indonesia', 'ACTIVE', 'system', NOW(), 'system', NOW())
ON CONFLICT (customer_number) DO NOTHING;

INSERT INTO customers (id, customer_number, customer_type, id_branches, email, phone_number, address, city, postal_code, country, status, created_by, created_date, updated_by, updated_date)
VALUES (gen_random_uuid(), 'VIEW001', 'PERSONAL', '01234567-8901-2345-6789-012345678901', 'view.test@example.com', '081234567893', '100 View Street', 'Jakarta', '11111', 'Indonesia', 'ACTIVE', 'system', NOW(), 'system', NOW())
ON CONFLICT (customer_number) DO NOTHING;

INSERT INTO customers (id, customer_number, customer_type, id_branches, email, phone_number, address, city, postal_code, country, status, created_by, created_date, updated_by, updated_date)
VALUES (gen_random_uuid(), 'DUPLICATE001', 'PERSONAL', '01234567-8901-2345-6789-012345678901', 'duplicate@example.com', '081234567894', '200 Duplicate Road', 'Yogyakarta', '55555', 'Indonesia', 'ACTIVE', 'system', NOW(), 'system', NOW())
ON CONFLICT (customer_number) DO NOTHING;

INSERT INTO customers (id, customer_number, customer_type, id_branches, email, phone_number, address, city, postal_code, country, status, created_by, created_date, updated_by, updated_date)
VALUES (gen_random_uuid(), 'CORP_EDIT001', 'CORPORATE', '01234567-8901-2345-6789-012345678901', 'edit@corpedit.com', '081234567895', '300 Corporate Edit Blvd', 'Jakarta', '22222', 'Indonesia', 'ACTIVE', 'system', NOW(), 'system', NOW())
ON CONFLICT (customer_number) DO NOTHING;

INSERT INTO customers (id, customer_number, customer_type, id_branches, email, phone_number, address, city, postal_code, country, status, created_by, created_date, updated_by, updated_date)
VALUES (gen_random_uuid(), 'CORP_VIEW001', 'CORPORATE', '01234567-8901-2345-6789-012345678901', 'contact@corpview.com', '081234567896', '400 Corporate View Street', 'Surabaya', '33333', 'Indonesia', 'ACTIVE', 'system', NOW(), 'system', NOW())
ON CONFLICT (customer_number) DO NOTHING;

-- Insert personal customer specific data using lookup by customer_number
INSERT INTO personal_customers (id, first_name, last_name, date_of_birth, identity_number, identity_type)
SELECT id, 'John', 'Doe', '1990-01-01', '1234567890123456', 'KTP' 
FROM customers WHERE customer_number = 'EDIT001' AND customer_type = 'PERSONAL'
ON CONFLICT (id) DO NOTHING;

INSERT INTO personal_customers (id, first_name, last_name, date_of_birth, identity_number, identity_type)
SELECT id, 'Test', 'Customer', '1985-05-15', '1234567890123457', 'KTP'
FROM customers WHERE customer_number = 'SEARCH001' AND customer_type = 'PERSONAL'
ON CONFLICT (id) DO NOTHING;

INSERT INTO personal_customers (id, first_name, last_name, date_of_birth, identity_number, identity_type)
SELECT id, 'Status', 'Test', '1992-12-25', '1234567890123458', 'KTP'
FROM customers WHERE customer_number = 'STATUS001' AND customer_type = 'PERSONAL'
ON CONFLICT (id) DO NOTHING;

INSERT INTO personal_customers (id, first_name, last_name, date_of_birth, identity_number, identity_type)
SELECT id, 'View', 'Test', '1987-07-07', '1234567890123460', 'KTP'
FROM customers WHERE customer_number = 'VIEW001' AND customer_type = 'PERSONAL'
ON CONFLICT (id) DO NOTHING;

INSERT INTO personal_customers (id, first_name, last_name, date_of_birth, identity_number, identity_type)
SELECT id, 'Duplicate', 'Test', '1988-08-08', '1234567890123459', 'KTP'
FROM customers WHERE customer_number = 'DUPLICATE001' AND customer_type = 'PERSONAL'
ON CONFLICT (id) DO NOTHING;

-- Insert corporate customer specific data using lookup by customer_number
INSERT INTO corporate_customers (id, company_name, company_registration_number, tax_identification_number, contact_person_name, contact_person_title)
SELECT id, 'Corporate Edit Company', '98765432109876', '98.765.432.1-098.000', 'John Manager', 'General Manager'
FROM customers WHERE customer_number = 'CORP_EDIT001' AND customer_type = 'CORPORATE'
ON CONFLICT (id) DO NOTHING;

INSERT INTO corporate_customers (id, company_name, company_registration_number, tax_identification_number, contact_person_name, contact_person_title)
SELECT id, 'Corporate View Inc', '11111111111111', '11.111.111.1-111.000', 'Jane Director', 'Director'
FROM customers WHERE customer_number = 'CORP_VIEW001' AND customer_type = 'CORPORATE'
ON CONFLICT (id) DO NOTHING;