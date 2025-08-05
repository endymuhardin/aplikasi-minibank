-- Create customers table for both personal and corporate customers
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_type VARCHAR(20) NOT NULL CHECK (customer_type IN ('PERSONAL', 'CORPORATE')),
    customer_number VARCHAR(50) UNIQUE NOT NULL,
    
    -- Personal customer fields
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    date_of_birth DATE,
    identity_number VARCHAR(50),
    identity_type VARCHAR(20) CHECK (identity_type IN ('KTP', 'PASSPORT', 'SIM')),
    
    -- Corporate customer fields  
    company_name VARCHAR(200),
    company_registration_number VARCHAR(100),
    tax_identification_number VARCHAR(50),
    
    -- Common fields
    email VARCHAR(100),
    phone_number VARCHAR(20),
    address TEXT,
    city VARCHAR(100),
    postal_code VARCHAR(10),
    country VARCHAR(50) DEFAULT 'Indonesia',
    
    -- Audit fields
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    
    -- Constraints
    CONSTRAINT chk_personal_fields CHECK (
        (customer_type = 'PERSONAL' AND first_name IS NOT NULL AND last_name IS NOT NULL AND date_of_birth IS NOT NULL AND identity_number IS NOT NULL)
        OR customer_type = 'CORPORATE'
    ),
    CONSTRAINT chk_corporate_fields CHECK (
        (customer_type = 'CORPORATE' AND company_name IS NOT NULL AND company_registration_number IS NOT NULL)
        OR customer_type = 'PERSONAL'
    )
);

-- Create products table for banking product configurations
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_code VARCHAR(20) UNIQUE NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    product_type VARCHAR(20) NOT NULL CHECK (product_type IN ('SAVINGS', 'CHECKING', 'LOAN', 'CREDIT_CARD', 'DEPOSIT')),
    product_category VARCHAR(50) NOT NULL,
    description TEXT,
    
    -- Basic product settings
    is_active BOOLEAN DEFAULT true,
    is_default BOOLEAN DEFAULT false,
    currency VARCHAR(3) DEFAULT 'IDR',
    
    -- Balance and limit configurations
    minimum_opening_balance DECIMAL(20,2) DEFAULT 0.00,
    minimum_balance DECIMAL(20,2) DEFAULT 0.00,
    maximum_balance DECIMAL(20,2),
    daily_withdrawal_limit DECIMAL(20,2),
    monthly_transaction_limit INTEGER,
    overdraft_limit DECIMAL(20,2) DEFAULT 0.00,
    
    -- Interest configurations
    interest_rate DECIMAL(5,4) DEFAULT 0.0000,
    interest_calculation_type VARCHAR(20) DEFAULT 'DAILY' CHECK (interest_calculation_type IN ('DAILY', 'MONTHLY', 'ANNUAL')),
    interest_payment_frequency VARCHAR(20) DEFAULT 'MONTHLY' CHECK (interest_payment_frequency IN ('DAILY', 'MONTHLY', 'QUARTERLY', 'ANNUALLY')),
    
    -- Fee configurations
    monthly_maintenance_fee DECIMAL(15,2) DEFAULT 0.00,
    atm_withdrawal_fee DECIMAL(15,2) DEFAULT 0.00,
    inter_bank_transfer_fee DECIMAL(15,2) DEFAULT 0.00,
    below_minimum_balance_fee DECIMAL(15,2) DEFAULT 0.00,
    account_closure_fee DECIMAL(15,2) DEFAULT 0.00,
    
    -- Transaction configurations
    free_transactions_per_month INTEGER DEFAULT 0,
    excess_transaction_fee DECIMAL(15,2) DEFAULT 0.00,
    allow_overdraft BOOLEAN DEFAULT false,
    require_maintaining_balance BOOLEAN DEFAULT true,
    
    -- Customer eligibility
    min_customer_age INTEGER,
    max_customer_age INTEGER,
    allowed_customer_types VARCHAR(50) DEFAULT 'PERSONAL,CORPORATE',
    required_documents TEXT,
    
    -- Product lifecycle
    launch_date DATE,
    retirement_date DATE,
    
    -- Audit fields
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    
    -- Business rules
    CONSTRAINT chk_minimum_balances CHECK (minimum_opening_balance >= 0 AND minimum_balance >= 0),
    CONSTRAINT chk_interest_rate CHECK (interest_rate >= 0 AND interest_rate <= 1),
    CONSTRAINT chk_fees_positive CHECK (
        monthly_maintenance_fee >= 0 AND 
        atm_withdrawal_fee >= 0 AND 
        inter_bank_transfer_fee >= 0 AND 
        below_minimum_balance_fee >= 0 AND
        account_closure_fee >= 0 AND
        excess_transaction_fee >= 0
    ),
    CONSTRAINT chk_customer_age CHECK (min_customer_age IS NULL OR max_customer_age IS NULL OR min_customer_age <= max_customer_age),
    CONSTRAINT chk_launch_retirement_date CHECK (retirement_date IS NULL OR launch_date IS NULL OR launch_date <= retirement_date)
);

-- Create accounts table for savings and checking accounts
CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_customers UUID NOT NULL,
    id_products UUID NOT NULL,
    account_number VARCHAR(50) UNIQUE NOT NULL,
    account_name VARCHAR(200) NOT NULL,
    balance DECIMAL(20,2) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'CLOSED', 'FROZEN')),
    
    -- Audit fields
    opened_date DATE DEFAULT CURRENT_DATE,
    closed_date DATE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    
    -- Foreign key constraints
    CONSTRAINT fk_accounts_customers FOREIGN KEY (id_customers) REFERENCES customers(id),
    CONSTRAINT fk_accounts_products FOREIGN KEY (id_products) REFERENCES products(id),
    
    -- Business rules
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT chk_closed_date CHECK (closed_date IS NULL OR closed_date >= opened_date)
);

-- Create transactions table for all account transactions
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_accounts UUID NOT NULL,
    transaction_number VARCHAR(50) UNIQUE NOT NULL,
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT', 'INTEREST', 'FEE')),
    amount DECIMAL(20,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'IDR',
    balance_before DECIMAL(20,2) NOT NULL,
    balance_after DECIMAL(20,2) NOT NULL,
    
    -- Transaction details
    description TEXT,
    reference_number VARCHAR(100),
    channel VARCHAR(50) DEFAULT 'TELLER' CHECK (channel IN ('TELLER', 'ATM', 'ONLINE', 'MOBILE', 'TRANSFER')),
    
    -- Transfer related fields (for future use)
    id_accounts_destination UUID,
    
    -- Audit fields
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    
    -- Foreign key constraints
    CONSTRAINT fk_transactions_accounts FOREIGN KEY (id_accounts) REFERENCES accounts(id),
    CONSTRAINT fk_transactions_accounts_destination FOREIGN KEY (id_accounts_destination) REFERENCES accounts(id),
    
    -- Business rules
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_balance_calculation CHECK (
        (transaction_type IN ('DEPOSIT', 'TRANSFER_IN', 'INTEREST') AND balance_after = balance_before + amount)
        OR (transaction_type IN ('WITHDRAWAL', 'TRANSFER_OUT', 'FEE') AND balance_after = balance_before - amount)
    )
);

-- Create indexes for performance
-- Customer indexes
CREATE INDEX idx_customers_customer_number ON customers(customer_number);
CREATE INDEX idx_customers_customer_type ON customers(customer_type);
CREATE INDEX idx_customers_identity_number ON customers(identity_number) WHERE identity_number IS NOT NULL;
CREATE INDEX idx_customers_company_registration_number ON customers(company_registration_number) WHERE company_registration_number IS NOT NULL;
CREATE INDEX idx_customers_email ON customers(email) WHERE email IS NOT NULL;

-- Product indexes
CREATE INDEX idx_products_product_code ON products(product_code);
CREATE INDEX idx_products_product_type ON products(product_type);
CREATE INDEX idx_products_is_active ON products(is_active);
CREATE INDEX idx_products_product_category ON products(product_category);

-- Account indexes
CREATE INDEX idx_accounts_customer ON accounts(id_customers);
CREATE INDEX idx_accounts_product ON accounts(id_products);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_accounts_status ON accounts(status);

-- Transaction indexes
CREATE INDEX idx_transactions_account ON transactions(id_accounts);
CREATE INDEX idx_transactions_transaction_number ON transactions(transaction_number);
CREATE INDEX idx_transactions_transaction_type ON transactions(transaction_type);
CREATE INDEX idx_transactions_transaction_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_reference_number ON transactions(reference_number) WHERE reference_number IS NOT NULL;
CREATE INDEX idx_transactions_destination_account ON transactions(id_accounts_destination) WHERE id_accounts_destination IS NOT NULL;

-- Create sequence table for maintaining last sequence numbers
CREATE TABLE sequence_numbers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sequence_name VARCHAR(50) UNIQUE NOT NULL,
    last_number BIGINT NOT NULL DEFAULT 0,
    prefix VARCHAR(10),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for performance
CREATE INDEX idx_sequence_numbers_name ON sequence_numbers(sequence_name);