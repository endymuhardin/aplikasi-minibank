-- Add customer activation/deactivation permission
-- Only Branch Manager should be able to activate/deactivate customers

-- Insert CUSTOMER_ACTIVATE permission
INSERT INTO permissions (permission_code, permission_name, permission_category, description, created_by) VALUES
('CUSTOMER_ACTIVATE', 'Activate/Deactivate Customer', 'CUSTOMER', 'Activate or deactivate customer accounts', 'SYSTEM')
ON CONFLICT (permission_code) DO NOTHING;

-- Grant CUSTOMER_ACTIVATE permission to Branch Manager role ONLY
INSERT INTO role_permissions (id_roles, id_permissions, granted_by)
SELECT r.id, p.id, 'SYSTEM'
FROM roles r, permissions p
WHERE r.role_code = 'BRANCH_MANAGER'
AND p.permission_code = 'CUSTOMER_ACTIVATE'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.id_roles = r.id AND rp.id_permissions = p.id
);
