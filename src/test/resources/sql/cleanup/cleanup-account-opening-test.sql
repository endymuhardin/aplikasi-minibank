-- Clean up test data after account opening tests
DELETE FROM transactions WHERE id_accounts IN (SELECT id FROM accounts WHERE account_number LIKE 'ACC%');
DELETE FROM accounts WHERE account_number LIKE 'ACC%';
DELETE FROM personal_customers WHERE id IN (SELECT id FROM customers WHERE customer_number LIKE 'C%');
DELETE FROM corporate_customers WHERE id IN (SELECT id FROM customers WHERE customer_number LIKE 'C%');
DELETE FROM customers WHERE customer_number LIKE 'C%';
DELETE FROM products WHERE product_code IN ('SAV001', 'SAV002', 'SAV003', 'CHK001', 'CHK002');
DELETE FROM sequence_numbers WHERE sequence_name = 'ACCOUNT_NUMBER';