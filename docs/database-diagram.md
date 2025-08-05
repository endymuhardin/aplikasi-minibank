# Database Schema Diagram

This diagram represents the database schema for the Mini Bank application based on the migration files.

```mermaid
erDiagram
    customers {
        UUID id PK
        VARCHAR customer_type
        VARCHAR customer_number UK
        VARCHAR first_name
        VARCHAR last_name
        DATE date_of_birth
        VARCHAR identity_number
        VARCHAR identity_type
        VARCHAR company_name
        VARCHAR company_registration_number
        VARCHAR tax_identification_number
        VARCHAR email
        VARCHAR phone_number
        TEXT address
        VARCHAR city
        VARCHAR postal_code
        VARCHAR country
        TIMESTAMP created_date
        VARCHAR created_by
        TIMESTAMP updated_date
        VARCHAR updated_by
    }

    products {
        UUID id PK
        VARCHAR product_code UK
        VARCHAR product_name
        VARCHAR product_type
        VARCHAR product_category
        TEXT description
        BOOLEAN is_active
        BOOLEAN is_default
        VARCHAR currency
        DECIMAL minimum_opening_balance
        DECIMAL minimum_balance
        DECIMAL maximum_balance
        DECIMAL daily_withdrawal_limit
        INTEGER monthly_transaction_limit
        DECIMAL overdraft_limit
        DECIMAL interest_rate
        VARCHAR interest_calculation_type
        VARCHAR interest_payment_frequency
        DECIMAL monthly_maintenance_fee
        DECIMAL atm_withdrawal_fee
        DECIMAL inter_bank_transfer_fee
        DECIMAL below_minimum_balance_fee
        DECIMAL account_closure_fee
        INTEGER free_transactions_per_month
        DECIMAL excess_transaction_fee
        BOOLEAN allow_overdraft
        BOOLEAN require_maintaining_balance
        INTEGER min_customer_age
        INTEGER max_customer_age
        VARCHAR allowed_customer_types
        TEXT required_documents
        DATE launch_date
        DATE retirement_date
        TIMESTAMP created_date
        VARCHAR created_by
        TIMESTAMP updated_date
        VARCHAR updated_by
    }

    accounts {
        UUID id PK
        UUID id_customers FK
        UUID id_products FK
        VARCHAR account_number UK
        VARCHAR account_name
        DECIMAL balance
        VARCHAR status
        DATE opened_date
        DATE closed_date
        TIMESTAMP created_date
        VARCHAR created_by
        TIMESTAMP updated_date
        VARCHAR updated_by
    }

    transactions {
        UUID id PK
        UUID id_accounts FK
        VARCHAR transaction_number UK
        VARCHAR transaction_type
        DECIMAL amount
        VARCHAR currency
        DECIMAL balance_before
        DECIMAL balance_after
        TEXT description
        VARCHAR reference_number
        VARCHAR channel
        UUID id_accounts_destination FK
        TIMESTAMP transaction_date
        TIMESTAMP processed_date
        VARCHAR created_by
    }

    sequence_numbers {
        UUID id PK
        VARCHAR sequence_name UK
        BIGINT last_number
        VARCHAR prefix
        TIMESTAMP created_date
        TIMESTAMP updated_date
    }

    customers ||--o{ accounts : "has"
    products ||--o{ accounts : "defines"
    accounts ||--o{ transactions : "records"
    accounts ||--o{ transactions : "destination"
```

## Table Relationships

### customers → accounts (One-to-Many)
- One customer can have multiple accounts
- Each account belongs to exactly one customer
- Foreign key: `accounts.id_customers` → `customers.id`

### products → accounts (One-to-Many)
- One product can be used for multiple accounts
- Each account is based on exactly one product
- Foreign key: `accounts.id_products` → `products.id`

### accounts → transactions (One-to-Many)
- One account can have multiple transactions
- Each transaction belongs to exactly one account
- Foreign key: `transactions.id_accounts` → `accounts.id`

### accounts → transactions (One-to-Many, for transfers)
- One account can be the destination for multiple transfer transactions
- Each transfer transaction can have one destination account (optional)
- Foreign key: `transactions.id_accounts_destination` → `accounts.id`

## Key Features

### Customer Types
- **PERSONAL**: Individual customers with personal information fields
- **CORPORATE**: Business customers with company information fields

### Product Types
- **SAVINGS**: Savings accounts with interest earnings
- **CHECKING**: Current accounts with overdraft facilities
- **LOAN**: Loan products (structure prepared)
- **CREDIT_CARD**: Credit card products (structure prepared)
- **DEPOSIT**: Term deposit products (structure prepared)

### Transaction Types
- **DEPOSIT**: Money deposited into account
- **WITHDRAWAL**: Money withdrawn from account
- **TRANSFER_IN**: Incoming transfer from another account
- **TRANSFER_OUT**: Outgoing transfer to another account
- **INTEREST**: Interest earned/charged
- **FEE**: Various fees charged

### Transaction Channels
- **TELLER**: Branch teller transactions
- **ATM**: ATM transactions
- **ONLINE**: Online banking
- **MOBILE**: Mobile app transactions
- **TRANSFER**: Inter-bank transfers

### Account Status
- **ACTIVE**: Normal operating account
- **INACTIVE**: Temporarily suspended account
- **CLOSED**: Permanently closed account
- **FROZEN**: Frozen due to compliance issues