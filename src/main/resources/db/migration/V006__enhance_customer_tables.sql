-- Enhanced customer tables with complete personal and professional data
-- Add new fields to personal_customers table (only if they don't exist)
ALTER TABLE personal_customers
ADD COLUMN IF NOT EXISTS alias_name VARCHAR(100),
ADD COLUMN IF NOT EXISTS education_level VARCHAR(50),
ADD COLUMN IF NOT EXISTS religion VARCHAR(50),
ADD COLUMN IF NOT EXISTS marital_status VARCHAR(50),
ADD COLUMN IF NOT EXISTS mothers_maiden_name VARCHAR(100),
ADD COLUMN IF NOT EXISTS dependents INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS nationality VARCHAR(20),
ADD COLUMN IF NOT EXISTS residence_code VARCHAR(50),
ADD COLUMN IF NOT EXISTS identity_expiry_date DATE,
ADD COLUMN IF NOT EXISTS job_title VARCHAR(100),
ADD COLUMN IF NOT EXISTS company_name VARCHAR(200),
ADD COLUMN IF NOT EXISTS company_address TEXT,
ADD COLUMN IF NOT EXISTS office_phone VARCHAR(20),
ADD COLUMN IF NOT EXISTS office_email VARCHAR(100),
ADD COLUMN IF NOT EXISTS employment_start_date DATE,
ADD COLUMN IF NOT EXISTS office_fax VARCHAR(20),
ADD COLUMN IF NOT EXISTS profession VARCHAR(100),
ADD COLUMN IF NOT EXISTS company_postal_code VARCHAR(20);

-- Update existing columns to support new enum values and longer fields
-- Update identity_type to support more options
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='personal_customers' AND column_name='identity_type') THEN
        ALTER TABLE personal_customers ALTER COLUMN identity_type TYPE VARCHAR(50);
    END IF;
END $$;

-- Update identity_number to support longer values
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='personal_customers' AND column_name='identity_number') THEN
        ALTER TABLE personal_customers ALTER COLUMN identity_number TYPE VARCHAR(100);
    END IF;
END $$;

-- Update customers base table with additional fields
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS customer_location UUID REFERENCES branches(id),
ADD COLUMN IF NOT EXISTS customer_type VARCHAR(20) DEFAULT 'PERSONAL'; -- Individu / Corporate

-- Update existing records to use proper customer types and extend constraint (only if customer_type column exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='customers' AND column_name='customer_type') THEN
        -- Extend the existing constraint to include INDIVIDU if needed
        -- Keep existing data as PERSONAL (compatible with current constraint)
        -- The form will show "Individu" but internally store as "PERSONAL"
    END IF;
END $$;

-- Extend the constraint to support INDIVIDU values (if needed)
DO $$
BEGIN
    -- First, try to drop and recreate the constraint to include INDIVIDU
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name='customers' AND constraint_name='customers_customer_type_check'
    ) THEN
        ALTER TABLE customers DROP CONSTRAINT customers_customer_type_check;
    END IF;
END $$;

-- Add constraint that supports both old and new values
ALTER TABLE customers ADD CONSTRAINT customers_customer_type_check
CHECK (customer_type IN ('PERSONAL', 'CORPORATE', 'INDIVIDU'));

-- Note: We keep existing data as PERSONAL to maintain backward compatibility
-- The form will display "Individu" for PERSONAL customers for better UX

-- Add indexes for performance (only if they don't exist)
CREATE INDEX IF NOT EXISTS idx_customers_customer_type ON customers(customer_type);
CREATE INDEX IF NOT EXISTS idx_customers_location ON customers(customer_location);
CREATE INDEX IF NOT EXISTS idx_personal_customers_identity_number ON personal_customers(identity_number);
CREATE INDEX IF NOT EXISTS idx_personal_customers_email ON customers(email) WHERE email IS NOT NULL;

-- Set proper discriminator value for PersonalCustomer entity
-- Update the discriminator_value in the entity from 'PERSONAL' to 'INDIVIDU'
-- This change aligns with the form dropdown showing "Individu/Corporate" instead of "Personal/Corporate"