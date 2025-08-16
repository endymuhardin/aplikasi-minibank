-- Cleanup test data for branch management tests

-- Delete any test branches created during testing
DELETE FROM branches WHERE branch_code LIKE 'TEST%';

-- Reset main branches to their original state from migration
UPDATE branches SET 
    status = 'ACTIVE',
    branch_name = 'Kantor Pusat Jakarta',
    manager_name = 'H. Ahmad Surya',
    is_main_branch = true
WHERE branch_code = 'HO001';

UPDATE branches SET 
    status = 'ACTIVE',
    branch_name = 'Cabang Jakarta Timur', 
    manager_name = 'Drs. Budi Pratama',
    is_main_branch = false
WHERE branch_code = 'JKT01';