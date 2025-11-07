-- Add approval workflow permissions
-- These permissions control access to the approval queue and approval actions

-- Insert new approval permissions
INSERT INTO permissions (permission_code, permission_name, permission_category, description, created_by) VALUES
('APPROVAL_VIEW', 'View Approval Queue', 'APPROVAL', 'View and access approval queue', 'SYSTEM'),
('CUSTOMER_APPROVE', 'Approve Customer', 'APPROVAL', 'Approve or reject customer creation requests', 'SYSTEM'),
('ACCOUNT_APPROVE', 'Approve Account', 'APPROVAL', 'Approve or reject account opening requests', 'SYSTEM')
ON CONFLICT (permission_code) DO NOTHING;

-- Grant approval permissions to Branch Manager role only
-- Branch Manager should be the only role with approval authority
INSERT INTO role_permissions (id_roles, id_permissions, granted_by)
SELECT r.id, p.id, 'SYSTEM'
FROM roles r, permissions p
WHERE r.role_code = 'BRANCH_MANAGER'
AND p.permission_code IN ('APPROVAL_VIEW', 'CUSTOMER_APPROVE', 'ACCOUNT_APPROVE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.id_roles = r.id AND rp.id_permissions = p.id
);

-- Verify: Branch Manager should have approval permissions, CS and Teller should NOT
-- This ensures proper segregation of duties
