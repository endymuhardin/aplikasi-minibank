-- Create users table for user information
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    id_branches UUID,
    is_active BOOLEAN DEFAULT true,
    is_locked BOOLEAN DEFAULT false,
    last_login TIMESTAMP,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    
    -- Audit fields
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    
    -- Foreign key constraints
    CONSTRAINT fk_users_branches FOREIGN KEY (id_branches) REFERENCES branches(id)
);

-- Create user_passwords table for authentication credentials
CREATE TABLE user_passwords (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_users UUID NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    password_expires_at TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    
    -- Audit fields
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    
    -- Foreign key constraint
    CONSTRAINT fk_user_passwords_users FOREIGN KEY (id_users) REFERENCES users(id) ON DELETE CASCADE
);

-- Create roles table for role-based access control
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_code VARCHAR(50) UNIQUE NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    
    -- Audit fields
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

-- Create permissions table for fine-grained permissions
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    permission_code VARCHAR(100) UNIQUE NOT NULL,
    permission_name VARCHAR(100) NOT NULL,
    permission_category VARCHAR(50) NOT NULL,
    description TEXT,
    
    -- Audit fields
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

-- Create user_roles junction table (many-to-many)
CREATE TABLE user_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_users UUID NOT NULL,
    id_roles UUID NOT NULL,
    assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(100),
    
    -- Foreign key constraints
    CONSTRAINT fk_user_roles_users FOREIGN KEY (id_users) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_roles FOREIGN KEY (id_roles) REFERENCES roles(id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate assignments
    CONSTRAINT uk_user_roles UNIQUE (id_users, id_roles)
);

-- Create role_permissions junction table (many-to-many)
CREATE TABLE role_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_roles UUID NOT NULL,
    id_permissions UUID NOT NULL,
    granted_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by VARCHAR(100),
    
    -- Foreign key constraints
    CONSTRAINT fk_role_permissions_roles FOREIGN KEY (id_roles) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permissions FOREIGN KEY (id_permissions) REFERENCES permissions(id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate permissions
    CONSTRAINT uk_role_permissions UNIQUE (id_roles, id_permissions)
);

-- Create indexes for performance
-- Users table indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email) WHERE email IS NOT NULL;
CREATE INDEX idx_users_is_active ON users(is_active);
CREATE INDEX idx_users_is_locked ON users(is_locked);
CREATE INDEX idx_users_last_login ON users(last_login);
CREATE INDEX idx_users_branch ON users(id_branches) WHERE id_branches IS NOT NULL;

-- User passwords table indexes
CREATE INDEX idx_user_passwords_user ON user_passwords(id_users);
CREATE INDEX idx_user_passwords_is_active ON user_passwords(is_active);
CREATE INDEX idx_user_passwords_expires_at ON user_passwords(password_expires_at);

-- Roles table indexes
CREATE INDEX idx_roles_role_code ON roles(role_code);
CREATE INDEX idx_roles_is_active ON roles(is_active);

-- Permissions table indexes
CREATE INDEX idx_permissions_permission_code ON permissions(permission_code);
CREATE INDEX idx_permissions_category ON permissions(permission_category);

-- Junction table indexes
CREATE INDEX idx_user_roles_user ON user_roles(id_users);
CREATE INDEX idx_user_roles_role ON user_roles(id_roles);
CREATE INDEX idx_role_permissions_role ON role_permissions(id_roles);
CREATE INDEX idx_role_permissions_permission ON role_permissions(id_permissions);