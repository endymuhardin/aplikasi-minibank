# Database Schema Design Conventions

Based on analysis of the Flyway migration files, here are the standardized database design conventions:

### Primary Key Standards

#### 1. **UUID Primary Keys (Mandatory)**
```sql
-- All tables use UUID primary keys with auto-generation
CREATE TABLE table_name (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- Other fields
);
```

**Why UUID:**
- Security: No sequential enumeration attacks
- Distributed system compatibility
- Merge conflicts reduced in multi-developer environments
- Future-proof for microservices architecture

#### 2. **Primary Key Naming**
- Always use `id` as the primary key column name
- Never use `table_name_id` for primary keys
- Use `UUID` data type with `gen_random_uuid()` default

### Foreign Key Conventions

#### 1. **Foreign Key Naming Pattern**
```sql
-- Pattern: id_{referenced_table_name} (singular)
id_customers UUID NOT NULL,  -- References customers.id
id_products UUID NOT NULL,   -- References products.id
id_accounts UUID NOT NULL,   -- References accounts.id

-- For junction tables or special cases:
id_accounts_destination UUID, -- References accounts.id (special purpose)
```

#### 2. **Foreign Key Constraint Naming**
```sql
-- Pattern: fk_{current_table}_{referenced_table}
CONSTRAINT fk_accounts_customers FOREIGN KEY (id_customers) REFERENCES customers(id),
CONSTRAINT fk_accounts_products FOREIGN KEY (id_products) REFERENCES products(id),
CONSTRAINT fk_user_roles_users FOREIGN KEY (id_users) REFERENCES users(id) ON DELETE CASCADE,
```

#### 3. **Cascade Rules**
```sql
-- Use CASCADE for dependent data (owned relationships)
ON DELETE CASCADE  -- Child records deleted when parent is deleted

-- Examples:
-- personal_customers -> customers (CASCADE - personal data owned by customer)
-- user_passwords -> users (CASCADE - passwords owned by user)
-- user_roles -> users (CASCADE - role assignments owned by user)
```

### Monetary and Financial Data Types

#### 1. **Money Amount Fields**
```sql
-- Large amounts (account balances, transaction amounts, limits)
balance DECIMAL(20,2) DEFAULT 0.00,
amount DECIMAL(20,2) NOT NULL,
minimum_opening_balance DECIMAL(20,2) DEFAULT 0.00,
daily_withdrawal_limit DECIMAL(20,2),
```

**Standard: DECIMAL(20,2)**
- Precision: 20 total digits
- Scale: 2 decimal places
- Range: Up to 999,999,999,999,999,999.99
- Suitable for: Account balances, transaction amounts, large limits

#### 2. **Fee and Small Amount Fields**
```sql
-- Fees and smaller amounts
monthly_maintenance_fee DECIMAL(15,2) DEFAULT 0.00,
atm_withdrawal_fee DECIMAL(15,2) DEFAULT 0.00,
inter_bank_transfer_fee DECIMAL(15,2) DEFAULT 0.00,
```

**Standard: DECIMAL(15,2)**
- Precision: 15 total digits
- Scale: 2 decimal places  
- Range: Up to 9,999,999,999,999.99
- Suitable for: Fees, small amounts, service charges

#### 3. **Ratio and Percentage Fields**
```sql
-- Profit sharing ratios, nisbah ratios, percentages
profit_sharing_ratio DECIMAL(5,4) DEFAULT 0.0000,
nisbah_customer DECIMAL(5,4),
nisbah_bank DECIMAL(5,4),
```

**Standard: DECIMAL(5,4)**
- Precision: 5 total digits
- Scale: 4 decimal places
- Range: 0.0000 to 1.0000 (0% to 100% with 4 decimal precision)
- Suitable for: Ratios, percentages, profit sharing ratios, nisbah values

#### 4. **Currency Standards**
```sql
-- Always include currency field for international compatibility
currency VARCHAR(3) DEFAULT 'IDR',
-- Use ISO 4217 currency codes (USD, EUR, IDR, etc.)
```

### Table and Column Naming Standards

#### 1. **Table Names**
- Use **plural nouns** for table names
- Use **snake_case** (lowercase with underscores)
- Examples: `customers`, `accounts`, `transactions`, `user_roles`, `role_permissions`

#### 2. **Column Names**
- Use **snake_case** (lowercase with underscores)
- Use descriptive names without abbreviations
- Boolean fields start with `is_` or use clear boolean meaning
- Date/timestamp fields end with appropriate suffix

```sql
-- Good column naming examples
customer_number VARCHAR(50),           -- Clear, descriptive
phone_number VARCHAR(20),              -- No abbreviation
is_active BOOLEAN,                     -- Clear boolean prefix
is_locked BOOLEAN,                     -- Clear boolean prefix
created_date TIMESTAMP,                -- Clear timestamp suffix
last_login TIMESTAMP,                  -- Clear meaning
failed_login_attempts INTEGER,         -- Descriptive count field
company_registration_number VARCHAR(100), -- Full descriptive name
```

#### 3. **Business Key Fields**
```sql
-- Business identifiers follow consistent pattern
customer_number VARCHAR(50) UNIQUE NOT NULL,
account_number VARCHAR(50) UNIQUE NOT NULL,
transaction_number VARCHAR(50) UNIQUE NOT NULL,
product_code VARCHAR(20) UNIQUE NOT NULL,
username VARCHAR(50) UNIQUE NOT NULL,
role_code VARCHAR(50) UNIQUE NOT NULL,
permission_code VARCHAR(100) UNIQUE NOT NULL,
```

**Standards:**
- Use `_number` for sequential business identifiers
- Use `_code` for categorical/type identifiers
- Always add `UNIQUE NOT NULL` constraints
- Use appropriate varchar lengths based on business rules

### Audit Fields Pattern

#### 1. **Standard Audit Fields (All Tables)**
```sql
-- Standard audit trail fields for all tables
created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
created_by VARCHAR(100),
updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_by VARCHAR(100)
```

#### 2. **Additional Audit Fields (When Needed)**
```sql
-- For user authentication and security
last_login TIMESTAMP,
failed_login_attempts INTEGER DEFAULT 0,
locked_until TIMESTAMP,

-- For business processes
assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
granted_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
opened_date DATE DEFAULT CURRENT_DATE,
closed_date DATE,
```

### Constraint Naming Conventions

#### 1. **Check Constraints**
```sql
-- Pattern: chk_{table_name}_{description} or chk_{field_description}
CONSTRAINT chk_minimum_balances CHECK (minimum_opening_balance >= 0 AND minimum_balance >= 0),
CONSTRAINT chk_profit_sharing_ratio CHECK (profit_sharing_ratio >= 0 AND profit_sharing_ratio <= 1),
CONSTRAINT chk_nisbah_customer CHECK (nisbah_customer IS NULL OR (nisbah_customer >= 0 AND nisbah_customer <= 1)),
CONSTRAINT chk_fees_positive CHECK (monthly_maintenance_fee >= 0 AND atm_withdrawal_fee >= 0),
```

#### 2. **Unique Constraints**
```sql
-- Pattern: uk_{table_name}_{field_names} or use UNIQUE on column
CONSTRAINT uk_user_roles UNIQUE (id_users, id_roles),
CONSTRAINT uk_role_permissions UNIQUE (id_roles, id_permissions),

-- Or inline unique constraints
customer_number VARCHAR(50) UNIQUE NOT NULL,
product_code VARCHAR(20) UNIQUE NOT NULL,
```

#### 3. **Enum-Style Check Constraints**
```sql
-- Use descriptive enum values in uppercase
CHECK (customer_type IN ('PERSONAL', 'CORPORATE')),
CHECK (status IN ('ACTIVE', 'INACTIVE', 'CLOSED', 'FROZEN')),
CHECK (identity_type IN ('KTP', 'PASSPORT', 'SIM')),
CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT', 'FEE')),
```

### Index Naming Conventions

#### 1. **Standard Index Naming Pattern**
```sql
-- Pattern: idx_{table_name}_{column_name(s)}
CREATE INDEX idx_customers_customer_number ON customers(customer_number);
CREATE INDEX idx_customers_customer_type ON customers(customer_type);
CREATE INDEX idx_accounts_customer ON accounts(id_customers);
CREATE INDEX idx_transactions_account ON transactions(id_accounts);
```

#### 2. **Composite Index Naming**
```sql
-- Pattern: idx_{table_name}_{column1}_{column2}
CREATE INDEX idx_personal_customers_name ON personal_customers(first_name, last_name);
CREATE INDEX idx_user_roles_user_role ON user_roles(id_users, id_roles);
```

#### 3. **Conditional Index Pattern**
```sql
```
