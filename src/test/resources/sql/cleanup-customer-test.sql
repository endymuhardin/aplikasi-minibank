-- Cleanup test data for customer management tests
-- PRESERVE migration data (C1000001-C1000006), only clean test-created data

-- Clean up accounts linked to test customers
DELETE FROM accounts WHERE id_customers IN (
    SELECT id FROM customers WHERE customer_number LIKE 'TEST_%' OR customer_number LIKE 'EDIT%' 
    OR customer_number LIKE 'SEARCH%' OR customer_number LIKE 'STATUS%' 
    OR customer_number LIKE 'VIEW%' OR customer_number LIKE 'DUPLICATE%'
    OR customer_number LIKE 'CORP_%'
);

-- Clean up test customer data
DELETE FROM personal_customers WHERE id IN (
    SELECT id FROM customers WHERE customer_number LIKE 'TEST_%' OR customer_number LIKE 'EDIT%' 
    OR customer_number LIKE 'SEARCH%' OR customer_number LIKE 'STATUS%' 
    OR customer_number LIKE 'VIEW%' OR customer_number LIKE 'DUPLICATE%'
);

DELETE FROM corporate_customers WHERE id IN (
    SELECT id FROM customers WHERE customer_number LIKE 'CORP_%' OR customer_number LIKE 'TEST_%'
);

DELETE FROM customers WHERE customer_number LIKE 'TEST_%' OR customer_number LIKE 'EDIT%' 
OR customer_number LIKE 'SEARCH%' OR customer_number LIKE 'STATUS%' 
OR customer_number LIKE 'VIEW%' OR customer_number LIKE 'DUPLICATE%'
OR customer_number LIKE 'CORP_%';

-- NOTE: Migration customers (C1000001-C1000006) are preserved for reuse in other tests