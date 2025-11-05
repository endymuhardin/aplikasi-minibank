-- Add missing fields to personal_customers table to comply with FR.002

-- Personal Data fields
ALTER TABLE personal_customers
ADD COLUMN education VARCHAR(20) CHECK (education IN ('SD', 'SMP', 'SMA', 'D3', 'S1', 'S2', 'S3')),
ADD COLUMN religion VARCHAR(20) CHECK (religion IN ('ISLAM', 'KRISTEN_PROTESTAN', 'KATOLIK', 'HINDU', 'BUDDHA', 'KONGHUCU', 'LAINNYA')),
ADD COLUMN marital_status VARCHAR(20) CHECK (marital_status IN ('BELUM_KAWIN', 'KAWIN', 'CERAI_HIDUP', 'CERAI_MATI')),
ADD COLUMN dependents INTEGER DEFAULT 0 CHECK (dependents >= 0);

-- Identity fields
ALTER TABLE personal_customers
ADD COLUMN citizenship VARCHAR(20) DEFAULT 'WNI' CHECK (citizenship IN ('WNI', 'WNA')),
ADD COLUMN residency_status VARCHAR(100),
ADD COLUMN identity_expiry_date DATE;

-- Employment Data fields
ALTER TABLE personal_customers
ADD COLUMN occupation VARCHAR(100),
ADD COLUMN company_name VARCHAR(200),
ADD COLUMN company_address TEXT,
ADD COLUMN business_field VARCHAR(100),
ADD COLUMN monthly_income DECIMAL(20,2),
ADD COLUMN source_of_funds VARCHAR(100),
ADD COLUMN account_purpose VARCHAR(200),
ADD COLUMN estimated_monthly_transactions INTEGER,
ADD COLUMN estimated_transaction_amount DECIMAL(20,2);

-- Add missing field to customers table
ALTER TABLE customers
ADD COLUMN alias_name VARCHAR(100);

-- Add province field if not exists (it exists in entity but checking)
-- The province field already exists in personal_customers table from entity definition

-- Create indexes for better query performance
CREATE INDEX idx_personal_customers_education ON personal_customers(education) WHERE education IS NOT NULL;
CREATE INDEX idx_personal_customers_religion ON personal_customers(religion) WHERE religion IS NOT NULL;
CREATE INDEX idx_personal_customers_marital_status ON personal_customers(marital_status) WHERE marital_status IS NOT NULL;
CREATE INDEX idx_personal_customers_citizenship ON personal_customers(citizenship);
CREATE INDEX idx_personal_customers_occupation ON personal_customers(occupation) WHERE occupation IS NOT NULL;

-- Add uniqueness constraint for phone number (FR.002 requirement)
CREATE UNIQUE INDEX idx_customers_phone_number_unique ON customers(phone_number) WHERE phone_number IS NOT NULL;

-- Add uniqueness constraint for email (FR.002 requirement)
CREATE UNIQUE INDEX idx_customers_email_unique ON customers(email) WHERE email IS NOT NULL;

-- Add uniqueness constraint for identity_number (FR.002 requirement)
CREATE UNIQUE INDEX idx_personal_customers_identity_number_unique ON personal_customers(identity_number) WHERE identity_number IS NOT NULL;

-- Add comments for documentation
COMMENT ON COLUMN personal_customers.education IS 'Education level: SD/SMP/SMA/D3/S1/S2/S3';
COMMENT ON COLUMN personal_customers.religion IS 'Religion: ISLAM/KRISTEN_PROTESTAN/KATOLIK/HINDU/BUDDHA/KONGHUCU/LAINNYA';
COMMENT ON COLUMN personal_customers.marital_status IS 'Marital status: BELUM_KAWIN/KAWIN/CERAI_HIDUP/CERAI_MATI';
COMMENT ON COLUMN personal_customers.dependents IS 'Number of dependents: 0, 1, 2, 3+';
COMMENT ON COLUMN personal_customers.citizenship IS 'Citizenship: WNI (Indonesian) or WNA (Foreigner)';
COMMENT ON COLUMN personal_customers.residency_status IS 'Residency/domicile status information';
COMMENT ON COLUMN personal_customers.identity_expiry_date IS 'Identity document expiry date';
COMMENT ON COLUMN personal_customers.occupation IS 'Job position/title';
COMMENT ON COLUMN personal_customers.company_name IS 'Employer company name';
COMMENT ON COLUMN personal_customers.company_address IS 'Company address';
COMMENT ON COLUMN personal_customers.business_field IS 'Industry/business sector';
COMMENT ON COLUMN personal_customers.monthly_income IS 'Monthly income in IDR';
COMMENT ON COLUMN personal_customers.source_of_funds IS 'Source of funds for banking activities';
COMMENT ON COLUMN personal_customers.account_purpose IS 'Purpose of opening the account';
COMMENT ON COLUMN personal_customers.estimated_monthly_transactions IS 'Estimated number of transactions per month';
COMMENT ON COLUMN personal_customers.estimated_transaction_amount IS 'Estimated total transaction amount per month in IDR';
COMMENT ON COLUMN customers.alias_name IS 'Customer alias/nickname (optional)';
