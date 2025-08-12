-- Clean up test data before running customer registration tests
DELETE FROM transactions WHERE id_accounts IN (SELECT id FROM accounts WHERE id_customers IN (SELECT id FROM customers WHERE customer_number LIKE 'C%'));
DELETE FROM accounts WHERE id_customers IN (SELECT id FROM customers WHERE customer_number LIKE 'C%');
DELETE FROM personal_customers WHERE id IN (SELECT id FROM customers WHERE customer_number LIKE 'C%');
DELETE FROM corporate_customers WHERE id IN (SELECT id FROM customers WHERE customer_number LIKE 'C%');
DELETE FROM customers WHERE customer_number LIKE 'C%';