-- Fix sequence numbers to account for existing sample data
-- Update sequence numbers to start after the highest existing numbers

-- Account sequence: highest existing is A2000006, so start from 2000007
UPDATE sequence_numbers
SET last_number = 2000006, prefix = 'A'
WHERE sequence_name = 'ACCOUNT_NUMBER';

-- Customer sequence: highest existing is C1000006, so start from 1000007
UPDATE sequence_numbers
SET last_number = 1000007, prefix = 'C'
WHERE sequence_name = 'CUSTOMER_NUMBER';

-- Add missing sequence for corporate accounts if not exists
INSERT INTO sequence_numbers (sequence_name, last_number, prefix)
VALUES ('CORPORATE_ACCOUNT_NUMBER', 0, 'CORP')
ON CONFLICT (sequence_name) DO NOTHING;

-- Transaction sequence: ensure we start after any sample transactions
UPDATE sequence_numbers
SET last_number = 3000000, prefix = 'TXN'
WHERE sequence_name = 'TRANSACTION_NUMBER';