# Database Design Patterns

### 1. Migration Versioning
```sql
-- V001__create_bank_schema.sql
-- V002__insert_initial_data.sql
-- V003__create_user_permission_schema.sql
-- V004__insert_roles_permissions_data.sql
```

### 2. UUID Primary Keys
```sql
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_type VARCHAR(20) NOT NULL CHECK (customer_type IN ('PERSONAL', 'CORPORATE')),
    customer_number VARCHAR(50) UNIQUE NOT NULL
);
```

### 3. Audit Trail Fields
```sql
-- Standard audit fields for all tables
created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
created_by VARCHAR(100),
updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_by VARCHAR(100)
```

### 4. Joined Table Inheritance
```sql
-- Base table
CREATE TABLE customers (...);

-- Specialized tables
CREATE TABLE personal_customers (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    CONSTRAINT fk_personal_customers_id FOREIGN KEY (id) REFERENCES customers(id) ON DELETE CASCADE
);
```

### 5. Performance Indexes
```sql
-- Performance indexes for common queries
CREATE INDEX idx_customers_customer_number ON customers(customer_number);
CREATE INDEX idx_customers_customer_type ON customers(customer_type);
CREATE INDEX idx_accounts_customer ON accounts(id_customers);
CREATE INDEX idx_transactions_account ON transactions(id_accounts);
CREATE INDEX idx_transactions_transaction_date ON transactions(transaction_date);
```
