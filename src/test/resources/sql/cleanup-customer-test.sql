-- Cleanup test data for customer management tests
DELETE FROM accounts WHERE id_customers IN (
    SELECT id FROM customers WHERE customer_number IN (
        'EDIT001', 'SEARCH001', 'STATUS001', 'VIEW001', 'DUPLICATE001'
    )
);

DELETE FROM personal_customers WHERE id IN (
    SELECT id FROM customers WHERE customer_number IN (
        'EDIT001', 'SEARCH001', 'STATUS001', 'VIEW001', 'DUPLICATE001'
    )
);

DELETE FROM corporate_customers WHERE id IN (
    SELECT id FROM customers WHERE customer_number IN (
        'EDIT001', 'SEARCH001', 'STATUS001', 'VIEW001', 'DUPLICATE001'
    )
);

DELETE FROM customers WHERE customer_number IN (
    'EDIT001', 'SEARCH001', 'STATUS001', 'VIEW001', 'DUPLICATE001'
);

-- Cleanup any test customers created during tests (with timestamp suffix)
DELETE FROM personal_customers WHERE id IN (
    SELECT id FROM customers WHERE customer_number LIKE 'PERS%' OR customer_number LIKE 'CORP%' OR customer_number LIKE 'TEST%'
);
DELETE FROM corporate_customers WHERE id IN (
    SELECT id FROM customers WHERE customer_number LIKE 'PERS%' OR customer_number LIKE 'CORP%' OR customer_number LIKE 'TEST%'
);
DELETE FROM customers WHERE customer_number LIKE 'PERS%' OR customer_number LIKE 'CORP%' OR customer_number LIKE 'TEST%';