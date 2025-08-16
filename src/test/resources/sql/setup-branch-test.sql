-- Setup test data for branch management tests
-- Note: Base branches are already created by migration V002__insert_initial_data.sql
-- This script ensures they are in the expected state for testing

-- Reset any test branches that might have been modified
DELETE FROM branches WHERE branch_code LIKE 'TEST%';

-- Ensure the main branches exist and are in expected state
-- (These should already exist from migration, but reset their status if needed)
UPDATE branches SET status = 'ACTIVE' WHERE branch_code IN ('HO001', 'JKT01');
UPDATE branches SET is_main_branch = true WHERE branch_code = 'HO001';
UPDATE branches SET is_main_branch = false WHERE branch_code = 'JKT01';