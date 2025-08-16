-- Setup test data for customer management tests
DELETE FROM accounts;
DELETE FROM personal_customers;
DELETE FROM corporate_customers;
DELETE FROM customers;

-- Note: Branches are created by migration, using main branch from V002__insert_initial_data.sql

-- Insert base customer records with branch assignments (using main branch from initial data)
INSERT INTO customers (id, customer_number, customer_type, id_branches, email, phone_number, address, city, postal_code, country, status, created_by, created_date, updated_by, updated_date)
VALUES ('22222222-2222-2222-2222-222222222001', 'EDIT001', 'PERSONAL', '01234567-8901-2345-6789-012345678901', 'john.doe@example.com', '081234567890', '123 Main Street', 'Jakarta', '12345', 'Indonesia', 'ACTIVE', 'system', NOW(), 'system', NOW());

INSERT INTO customers (id, customer_number, customer_type, id_branches, email, phone_number, address, city, postal_code, country, status, created_by, created_date, updated_by, updated_date)
VALUES ('22222222-2222-2222-2222-222222222002', 'SEARCH001', 'PERSONAL', '01234567-8901-2345-6789-012345678901', 'test.customer@example.com', '081234567891', '456 Search Street', 'Surabaya', '54321', 'Indonesia', 'ACTIVE', 'system', NOW(), 'system', NOW());

INSERT INTO customers (id, customer_number, customer_type, id_branches, email, phone_number, address, city, postal_code, country, status, created_by, created_date, updated_by, updated_date)
VALUES ('22222222-2222-2222-2222-222222222003', 'STATUS001', 'PERSONAL', '01234567-8901-2345-6789-012345678901', 'status.test@example.com', '081234567892', '789 Status Avenue', 'Bandung', '98765', 'Indonesia', 'ACTIVE', 'system', NOW(), 'system', NOW());

INSERT INTO customers (id, customer_number, customer_type, id_branches, email, phone_number, address, city, postal_code, country, status, created_by, created_date, updated_by, updated_date)
VALUES ('22222222-2222-2222-2222-222222222004', 'VIEW001', 'PERSONAL', '01234567-8901-2345-6789-012345678901', 'view.test@example.com', '081234567893', '100 View Street', 'Jakarta', '11111', 'Indonesia', 'ACTIVE', 'system', NOW(), 'system', NOW());

INSERT INTO customers (id, customer_number, customer_type, id_branches, email, phone_number, address, city, postal_code, country, status, created_by, created_date, updated_by, updated_date)
VALUES ('22222222-2222-2222-2222-222222222005', 'DUPLICATE001', 'PERSONAL', '01234567-8901-2345-6789-012345678901', 'duplicate@example.com', '081234567894', '200 Duplicate Road', 'Yogyakarta', '55555', 'Indonesia', 'ACTIVE', 'system', NOW(), 'system', NOW());

INSERT INTO customers (id, customer_number, customer_type, id_branches, email, phone_number, address, city, postal_code, country, status, created_by, created_date, updated_by, updated_date)
VALUES ('22222222-2222-2222-2222-222222222006', 'CORP_EDIT001', 'CORPORATE', '01234567-8901-2345-6789-012345678901', 'edit@corpedit.com', '081234567895', '300 Corporate Edit Blvd', 'Jakarta', '22222', 'Indonesia', 'ACTIVE', 'system', NOW(), 'system', NOW());

INSERT INTO customers (id, customer_number, customer_type, id_branches, email, phone_number, address, city, postal_code, country, status, created_by, created_date, updated_by, updated_date)
VALUES ('22222222-2222-2222-2222-222222222007', 'CORP_VIEW001', 'CORPORATE', '01234567-8901-2345-6789-012345678901', 'contact@corpview.com', '081234567896', '400 Corporate View Street', 'Surabaya', '33333', 'Indonesia', 'ACTIVE', 'system', NOW(), 'system', NOW());

-- Insert personal customer specific data
INSERT INTO personal_customers (id, first_name, last_name, date_of_birth, identity_number, identity_type)
VALUES ('22222222-2222-2222-2222-222222222001', 'John', 'Doe', '1990-01-01', '1234567890123456', 'KTP');

INSERT INTO personal_customers (id, first_name, last_name, date_of_birth, identity_number, identity_type)
VALUES ('22222222-2222-2222-2222-222222222002', 'Test', 'Customer', '1985-05-15', '1234567890123457', 'KTP');

INSERT INTO personal_customers (id, first_name, last_name, date_of_birth, identity_number, identity_type)
VALUES ('22222222-2222-2222-2222-222222222003', 'Status', 'Test', '1992-12-25', '1234567890123458', 'KTP');

INSERT INTO personal_customers (id, first_name, last_name, date_of_birth, identity_number, identity_type)
VALUES ('22222222-2222-2222-2222-222222222004', 'View', 'Test', '1987-07-07', '1234567890123460', 'KTP');

INSERT INTO personal_customers (id, first_name, last_name, date_of_birth, identity_number, identity_type)
VALUES ('22222222-2222-2222-2222-222222222005', 'Duplicate', 'Test', '1988-08-08', '1234567890123459', 'KTP');

-- Insert corporate customer specific data
INSERT INTO corporate_customers (id, company_name, company_registration_number, tax_identification_number, contact_person_name, contact_person_title)
VALUES ('22222222-2222-2222-2222-222222222006', 'Corporate Edit Company', '98765432109876', '98.765.432.1-098.000', 'John Manager', 'General Manager');

INSERT INTO corporate_customers (id, company_name, company_registration_number, tax_identification_number, contact_person_name, contact_person_title)
VALUES ('22222222-2222-2222-2222-222222222007', 'Corporate View Inc', '11111111111111', '11.111.111.1-111.000', 'Jane Director', 'Director');