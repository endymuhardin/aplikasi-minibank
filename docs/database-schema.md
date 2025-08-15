# Database Schema Documentation

This document provides comprehensive documentation for the Mini Bank application database schema, including all tables, relationships, indexes, and migration information.

## Schema Overview

The database consists of two main modules:
1. **Banking Core Module**: Customer, product, account, and transaction management
2. **User Authentication Module**: User management, roles, and permissions

## Banking Core Tables

### customers
Base table for all customer types using joined table inheritance.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique identifier |
| customer_type | VARCHAR(20) | NOT NULL, CHECK IN ('PERSONAL', 'CORPORATE') | Customer type discriminator |
| customer_number | VARCHAR(50) | UNIQUE NOT NULL | Business customer number |
| email | VARCHAR(100) | | Customer email address |
| phone_number | VARCHAR(20) | | Phone number |
| address | TEXT | | Street address |
| city | VARCHAR(100) | | City |
| postal_code | VARCHAR(10) | | Postal/ZIP code |
| country | VARCHAR(50) | DEFAULT 'Indonesia' | Country |
| status | VARCHAR(20) | DEFAULT 'ACTIVE', CHECK IN ('ACTIVE', 'INACTIVE', 'CLOSED', 'FROZEN') | Customer account status |
| created_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| created_by | VARCHAR(100) | | User who created the record |
| updated_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update timestamp |
| updated_by | VARCHAR(100) | | User who last updated the record |

**Indexes:**
- `idx_customers_customer_number` ON customer_number
- `idx_customers_customer_type` ON customer_type  
- `idx_customers_email` ON email (WHERE email IS NOT NULL)

### personal_customers
Extension table for personal customer specific fields.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, FK to customers(id) | Inherited from customers |
| first_name | VARCHAR(100) | NOT NULL | First name |
| last_name | VARCHAR(100) | NOT NULL | Last name |
| date_of_birth | DATE | NOT NULL | Date of birth |
| identity_number | VARCHAR(50) | NOT NULL | ID number (KTP/Passport/SIM) |
| identity_type | VARCHAR(20) | NOT NULL, CHECK IN ('KTP', 'PASSPORT', 'SIM') | Type of ID |

**Indexes:**
- `idx_personal_customers_identity_number` ON identity_number
- `idx_personal_customers_name` ON first_name, last_name

### corporate_customers
Extension table for corporate customer specific fields.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, FK to customers(id) | Inherited from customers |
| company_name | VARCHAR(200) | NOT NULL | Company name |
| company_registration_number | VARCHAR(100) | NOT NULL | Business registration number |
| tax_identification_number | VARCHAR(50) | | Tax ID number |
| contact_person_name | VARCHAR(100) | | Contact person name |
| contact_person_title | VARCHAR(100) | | Contact person title |

**Indexes:**
- `idx_corporate_customers_company_registration_number` ON company_registration_number
- `idx_corporate_customers_company_name` ON company_name
- `idx_corporate_customers_tax_id` ON tax_identification_number (WHERE tax_identification_number IS NOT NULL)

### products
Banking product configurations and rules.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique identifier |
| product_code | VARCHAR(20) | UNIQUE NOT NULL | Business product code |
| product_name | VARCHAR(100) | NOT NULL | Product display name |
| product_type | VARCHAR(50) | NOT NULL, CHECK IN ('SAVINGS', 'CHECKING', 'DEPOSIT', 'TABUNGAN_WADIAH', 'TABUNGAN_MUDHARABAH', 'DEPOSITO_MUDHARABAH', 'PEMBIAYAAN_MURABAHAH', 'PEMBIAYAAN_MUDHARABAH', 'PEMBIAYAAN_MUSHARAKAH', 'PEMBIAYAAN_IJARAH', 'PEMBIAYAAN_SALAM', 'PEMBIAYAAN_ISTISNA') | Product category |
| product_category | VARCHAR(50) | NOT NULL | Product subcategory |
| description | TEXT | | Product description |
| is_active | BOOLEAN | DEFAULT true | Active status |
| is_default | BOOLEAN | DEFAULT false | Default product flag |
| currency | VARCHAR(3) | DEFAULT 'IDR' | Currency code |
| minimum_opening_balance | DECIMAL(20,2) | DEFAULT 0.00 | Minimum opening balance |
| minimum_balance | DECIMAL(20,2) | DEFAULT 0.00 | Minimum maintaining balance |
| maximum_balance | DECIMAL(20,2) | | Maximum balance limit |
| daily_withdrawal_limit | DECIMAL(20,2) | | Daily withdrawal limit |
| monthly_transaction_limit | INTEGER | | Monthly transaction limit |
| overdraft_limit | DECIMAL(20,2) | DEFAULT 0.00 | Overdraft limit |
| profit_sharing_ratio | DECIMAL(5,4) | DEFAULT 0.0000 | Profit sharing ratio (0-1) |
| profit_sharing_type | VARCHAR(20) | DEFAULT 'MUDHARABAH', CHECK IN ('MUDHARABAH', 'MUSHARAKAH', 'WADIAH', 'MURABAHAH', 'IJARAH', 'SALAM', 'ISTISNA') | Islamic banking profit sharing type |
| profit_distribution_frequency | VARCHAR(20) | DEFAULT 'MONTHLY', CHECK IN ('DAILY', 'MONTHLY', 'QUARTERLY', 'ANNUALLY', 'ON_MATURITY') | Profit distribution frequency |
| nisbah_customer | DECIMAL(5,4) | | Customer profit sharing percentage |
| nisbah_bank | DECIMAL(5,4) | | Bank profit sharing percentage |
| is_shariah_compliant | BOOLEAN | DEFAULT true | Shariah compliance flag |
| shariah_board_approval_number | VARCHAR(100) | | Shariah board approval reference |
| shariah_board_approval_date | DATE | | Date of shariah board approval |
| monthly_maintenance_fee | DECIMAL(15,2) | DEFAULT 0.00 | Monthly maintenance fee |
| atm_withdrawal_fee | DECIMAL(15,2) | DEFAULT 0.00 | ATM withdrawal fee |
| inter_bank_transfer_fee | DECIMAL(15,2) | DEFAULT 0.00 | Inter-bank transfer fee |
| below_minimum_balance_fee | DECIMAL(15,2) | DEFAULT 0.00 | Below minimum balance fee |
| account_closure_fee | DECIMAL(15,2) | DEFAULT 0.00 | Account closure fee |
| free_transactions_per_month | INTEGER | DEFAULT 0 | Free transaction count |
| excess_transaction_fee | DECIMAL(15,2) | DEFAULT 0.00 | Fee for excess transactions |
| allow_overdraft | BOOLEAN | DEFAULT false | Allow overdraft flag |
| require_maintaining_balance | BOOLEAN | DEFAULT true | Require maintaining balance |
| min_customer_age | INTEGER | | Minimum customer age |
| max_customer_age | INTEGER | | Maximum customer age |
| allowed_customer_types | VARCHAR(50) | DEFAULT 'PERSONAL,CORPORATE' | Allowed customer types |
| required_documents | TEXT | | Required documents list |
| launch_date | DATE | | Product launch date |
| retirement_date | DATE | | Product retirement date |
| created_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| created_by | VARCHAR(100) | | User who created the record |
| updated_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update timestamp |
| updated_by | VARCHAR(100) | | User who last updated the record |

**Indexes:**
- `idx_products_product_code` ON product_code
- `idx_products_product_type` ON product_type
- `idx_products_is_active` ON is_active
- `idx_products_product_category` ON product_category

### accounts
Customer accounts linked to products.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique identifier |
| id_customers | UUID | NOT NULL, FK to customers(id) | Customer reference |
| id_products | UUID | NOT NULL, FK to products(id) | Product reference |
| account_number | VARCHAR(50) | UNIQUE NOT NULL | Business account number |
| account_name | VARCHAR(200) | NOT NULL | Account display name |
| balance | DECIMAL(20,2) | DEFAULT 0.00, CHECK >= 0 | Current balance |
| status | VARCHAR(20) | DEFAULT 'ACTIVE', CHECK IN ('ACTIVE', 'INACTIVE', 'CLOSED', 'FROZEN') | Account status |
| opened_date | DATE | DEFAULT CURRENT_DATE | Account opening date |
| closed_date | DATE | CHECK >= opened_date | Account closure date |
| created_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| created_by | VARCHAR(100) | | User who created the record |
| updated_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update timestamp |
| updated_by | VARCHAR(100) | | User who last updated the record |

**Indexes:**
- `idx_accounts_customer` ON id_customers
- `idx_accounts_product` ON id_products
- `idx_accounts_account_number` ON account_number
- `idx_accounts_status` ON status

### transactions
All account transaction records.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique identifier |
| id_accounts | UUID | NOT NULL, FK to accounts(id) | Source account |
| transaction_number | VARCHAR(50) | UNIQUE NOT NULL | Business transaction number |
| transaction_type | VARCHAR(20) | NOT NULL, CHECK IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT', 'INTEREST', 'FEE') | Transaction type |
| amount | DECIMAL(20,2) | NOT NULL, CHECK > 0 | Transaction amount |
| currency | VARCHAR(3) | DEFAULT 'IDR' | Currency code |
| balance_before | DECIMAL(20,2) | NOT NULL | Balance before transaction |
| balance_after | DECIMAL(20,2) | NOT NULL | Balance after transaction |
| description | TEXT | | Transaction description |
| reference_number | VARCHAR(100) | | External reference number |
| channel | VARCHAR(50) | DEFAULT 'TELLER', CHECK IN ('TELLER', 'ATM', 'ONLINE', 'MOBILE', 'TRANSFER') | Transaction channel |
| id_accounts_destination | UUID | FK to accounts(id) | Destination account (for transfers) |
| transaction_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Transaction timestamp |
| processed_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Processing timestamp |
| created_by | VARCHAR(100) | | User who created the record |

**Indexes:**
- `idx_transactions_account` ON id_accounts
- `idx_transactions_transaction_number` ON transaction_number
- `idx_transactions_transaction_type` ON transaction_type
- `idx_transactions_transaction_date` ON transaction_date
- `idx_transactions_reference_number` ON reference_number (WHERE reference_number IS NOT NULL)
- `idx_transactions_destination_account` ON id_accounts_destination (WHERE id_accounts_destination IS NOT NULL)

### sequence_numbers
Sequence number management for business keys.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique identifier |
| sequence_name | VARCHAR(50) | UNIQUE NOT NULL | Sequence identifier |
| last_number | BIGINT | NOT NULL DEFAULT 0 | Last used number |
| prefix | VARCHAR(10) | | Number prefix |
| created_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| updated_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

**Indexes:**
- `idx_sequence_numbers_name` ON sequence_name

## User Authentication Tables

### users
System user accounts.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique identifier |
| username | VARCHAR(50) | UNIQUE NOT NULL | Login username |
| email | VARCHAR(100) | UNIQUE | User email address |
| full_name | VARCHAR(100) | NOT NULL | Full display name |
| is_active | BOOLEAN | DEFAULT true | Active status |
| is_locked | BOOLEAN | DEFAULT false | Account locked status |
| last_login | TIMESTAMP | | Last login timestamp |
| failed_login_attempts | INTEGER | DEFAULT 0 | Failed login counter |
| locked_until | TIMESTAMP | | Lock expiration timestamp |
| created_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| created_by | VARCHAR(100) | | User who created the record |
| updated_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update timestamp |
| updated_by | VARCHAR(100) | | User who last updated the record |

**Indexes:**
- `idx_users_username` ON username
- `idx_users_email` ON email (WHERE email IS NOT NULL)
- `idx_users_is_active` ON is_active
- `idx_users_is_locked` ON is_locked
- `idx_users_last_login` ON last_login

### user_passwords
User password storage (separate table for CRUD operations).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique identifier |
| id_users | UUID | NOT NULL, FK to users(id) | User reference |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt password hash |
| password_expires_at | TIMESTAMP | | Password expiration date |
| is_active | BOOLEAN | DEFAULT true | Active password flag |
| created_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| created_by | VARCHAR(100) | | User who created the record |

**Indexes:**
- `idx_user_passwords_user` ON id_users
- `idx_user_passwords_is_active` ON is_active
- `idx_user_passwords_expires_at` ON password_expires_at

### roles
System roles definition.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique identifier |
| role_code | VARCHAR(50) | UNIQUE NOT NULL | Role identifier code |
| role_name | VARCHAR(100) | NOT NULL | Role display name |
| description | TEXT | | Role description |
| is_active | BOOLEAN | DEFAULT true | Active status |
| created_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| created_by | VARCHAR(100) | | User who created the record |
| updated_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update timestamp |
| updated_by | VARCHAR(100) | | User who last updated the record |

**Indexes:**
- `idx_roles_role_code` ON role_code
- `idx_roles_is_active` ON is_active

### permissions
System permission definitions.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique identifier |
| permission_code | VARCHAR(100) | UNIQUE NOT NULL | Permission identifier |
| permission_name | VARCHAR(100) | NOT NULL | Permission display name |
| permission_category | VARCHAR(50) | NOT NULL | Permission category |
| description | TEXT | | Permission description |
| resource | VARCHAR(100) | | Resource being protected |
| action | VARCHAR(50) | | Action being performed |
| created_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| created_by | VARCHAR(100) | | User who created the record |

**Indexes:**
- `idx_permissions_permission_code` ON permission_code
- `idx_permissions_category` ON permission_category
- `idx_permissions_resource_action` ON resource, action

### user_roles
User-role assignments (many-to-many).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique identifier |
| id_users | UUID | NOT NULL, FK to users(id) | User reference |
| id_roles | UUID | NOT NULL, FK to roles(id) | Role reference |
| assigned_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Assignment timestamp |
| assigned_by | VARCHAR(100) | | User who made the assignment |

**Constraints:**
- `uk_user_roles` UNIQUE (id_users, id_roles)

**Indexes:**
- `idx_user_roles_user` ON id_users
- `idx_user_roles_role` ON id_roles

### role_permissions
Role-permission grants (many-to-many).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique identifier |
| id_roles | UUID | NOT NULL, FK to roles(id) | Role reference |
| id_permissions | UUID | NOT NULL, FK to permissions(id) | Permission reference |
| granted_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Grant timestamp |
| granted_by | VARCHAR(100) | | User who granted the permission |

**Constraints:**
- `uk_role_permissions` UNIQUE (id_roles, id_permissions)

**Indexes:**
- `idx_role_permissions_role` ON id_roles
- `idx_role_permissions_permission` ON id_permissions

## Migration History

| Version | File | Description |
|---------|------|-------------|
| V001 | V001__create_bank_schema.sql | Initial banking core schema |
| V002 | V002__insert_initial_data.sql | Initial banking data (products, sequences) |
| V003 | V003__create_user_permission_schema.sql | User authentication and authorization schema |
| V004 | V004__insert_roles_permissions_data.sql | Initial roles, permissions, and admin user |

## Default Data

### Sequence Numbers (V002)
Initial sequence number configurations:
| Sequence Name | Last Number | Prefix | Purpose |
|---------------|-------------|--------|---------|
| CUSTOMER_NUMBER | 1000000 | C | Customer number generation (C1000001, C1000002, ...) |
| ACCOUNT_NUMBER | 2000000 | A | Account number generation (A2000001, A2000002, ...) |
| TRANSACTION_NUMBER | 3000000 | T | Transaction number generation (T3000001, T3000002, ...) |

### Islamic Banking Products (V002)
Pre-configured Islamic banking products:

| Product Code | Product Name | Type | Category | Min Opening | Min Balance | Nisbah Customer:Bank | Customer Types | Monthly Fee |
|--------------|--------------|------|----------|-------------|-------------|---------------------|----------------|-------------|
| **TAB001** | Tabungan Wadiah Basic | TABUNGAN_WADIAH | Tabungan Syariah | 50,000 | 10,000 | - | PERSONAL | 2,500 |
| **TAB002** | Tabungan Mudharabah Premium | TABUNGAN_MUDHARABAH | Tabungan Syariah | 1,000,000 | 500,000 | 70:30 | PERSONAL | 0 |
| **DEP001** | Deposito Mudharabah | DEPOSITO_MUDHARABAH | Deposito Syariah | 100,000 | 50,000 | 70:30 | PERSONAL | 5,000 |
| **PEM001** | Pembiayaan Murabahah | PEMBIAYAAN_MURABAHAH | Pembiayaan Syariah | 5,000,000 | 1,000,000 | - | CORPORATE | 15,000 |
| **PEM002** | Pembiayaan Musharakah | PEMBIAYAAN_MUSHARAKAH | Pembiayaan Syariah | 2,000,000 | 1,000,000 | 60:40 | PERSONAL | 0 |

### Sample Customers (V002)
Pre-loaded customer data for testing:

#### Personal Customers
| Customer Number | Name | Email | Identity Number | Phone | Address |
|-----------------|------|-------|-----------------|-------|---------|
| C1000001 | Ahmad Suharto | ahmad.suharto@email.com | 3271081503850001 (KTP) | 081234567890 | Jl. Sudirman No. 123, Jakarta 10220 |
| C1000002 | Siti Nurhaliza | siti.nurhaliza@email.com | 3271082207900002 (KTP) | 081234567891 | Jl. Thamrin No. 456, Jakarta 10230 |
| C1000004 | Budi Santoso | budi.santoso@email.com | 3271081011880003 (KTP) | 081234567892 | Jl. Gatot Subroto No. 321, Jakarta 12930 |
| C1000006 | Dewi Lestari | dewi.lestari@email.com | 3271081805920004 (KTP) | 081234567893 | Jl. MH Thamrin No. 654, Jakarta 10350 |

#### Corporate Customers
| Customer Number | Company Name | Email | Registration Number | Tax ID | Contact Person | Phone | Address |
|-----------------|--------------|-------|-------------------|--------|----------------|-------|---------|
| C1000003 | PT. Teknologi Maju | info@teknologimaju.com | 1234567890123456 | 01.234.567.8-901.000 | - | 02123456789 | Jl. HR Rasuna Said No. 789, Jakarta 12950 |

### Roles (V004)
Three main system roles with hierarchical permissions:
- **CUSTOMER_SERVICE**: Customer registration and account opening
- **TELLER**: Financial transaction processing  
- **BRANCH_MANAGER**: Full system access with monitoring and approvals

### Sample Users (V004)
All sample users have the password: `minibank123` (BCrypt hashed: `$2a$10$6tjICoD1DhK3r82bD4NiSuJ8A4xvf5osh96V7Q4BXFvIXZB3/s7da`)

| Username | Full Name | Email | Role | Purpose |
|----------|-----------|--------|------|---------|
| **admin** | System Administrator | admin@yopmail.com | BRANCH_MANAGER | System administration |
| **manager1** | Branch Manager Jakarta | manager1@yopmail.com | BRANCH_MANAGER | Jakarta branch management |
| **manager2** | Branch Manager Surabaya | manager2@yopmail.com | BRANCH_MANAGER | Surabaya branch management |
| **teller1** | Teller Counter 1 | teller1@yopmail.com | TELLER | Transaction processing |
| **teller2** | Teller Counter 2 | teller2@yopmail.com | TELLER | Transaction processing |
| **teller3** | Teller Counter 3 | teller3@yopmail.com | TELLER | Transaction processing |
| **cs1** | Customer Service Staff 1 | cs1@yopmail.com | CUSTOMER_SERVICE | Customer service |
| **cs2** | Customer Service Staff 2 | cs2@yopmail.com | CUSTOMER_SERVICE | Customer service |
| **cs3** | Customer Service Staff 3 | cs3@yopmail.com | CUSTOMER_SERVICE | Customer service |

### Permission System (V004)
Detailed permission breakdown by role:

#### Customer Service Permissions
| Permission Code | Permission Name | Category | Description |
|-----------------|-----------------|----------|-------------|
| CUSTOMER_VIEW | View Customer | CUSTOMER | View customer information |
| CUSTOMER_CREATE | Create Customer | CUSTOMER | Register new customers |
| CUSTOMER_UPDATE | Update Customer | CUSTOMER | Update customer information |
| ACCOUNT_VIEW | View Account | ACCOUNT | View account information |
| ACCOUNT_CREATE | Create Account | ACCOUNT | Open new accounts for customers |
| ACCOUNT_UPDATE | Update Account | ACCOUNT | Update account information |
| PRODUCT_VIEW | View Product | PRODUCT | View banking products |

#### Teller Permissions
| Permission Code | Permission Name | Category | Description |
|-----------------|-----------------|----------|-------------|
| CUSTOMER_VIEW | View Customer | CUSTOMER | View customer information |
| ACCOUNT_VIEW | View Account | ACCOUNT | View account information |
| BALANCE_VIEW | View Balance | ACCOUNT | View account balance |
| TRANSACTION_VIEW | View Transaction | TRANSACTION | View transaction history |
| TRANSACTION_DEPOSIT | Process Deposit | TRANSACTION | Process deposit transactions |
| TRANSACTION_WITHDRAWAL | Process Withdrawal | TRANSACTION | Process withdrawal transactions |
| TRANSACTION_TRANSFER | Process Transfer | TRANSACTION | Process transfer transactions |
| PRODUCT_VIEW | View Product | PRODUCT | View banking products |

#### Branch Manager Permissions (All permissions including)
| Permission Code | Permission Name | Category | Description |
|-----------------|-----------------|----------|-------------|
| REPORT_VIEW | View Reports | REPORT | View business reports and analytics |
| AUDIT_VIEW | View Audit Log | AUDIT | View system audit logs |
| TRANSACTION_APPROVE | Approve Transaction | TRANSACTION | Approve high-value transactions |
| ACCOUNT_APPROVE | Approve Account | ACCOUNT | Approve account opening/closing |
| USER_VIEW | View Users | USER | View system users |
| USER_CREATE | Create User | USER | Create new system users |
| USER_UPDATE | Update User | USER | Update user information |
| USER_DEACTIVATE | Deactivate User | USER | Deactivate system users |

**Note**: Branch Manager role inherits ALL permissions from Customer Service and Teller roles plus additional management permissions.

## Security Considerations

### Password Security
- BCrypt hashing with strength 10
- Separate password table for easier management
- Password expiration support
- Active password flag for password history

### Account Security
- Failed login attempt tracking
- Automatic account locking with timeout
- Session management with last login tracking
- Account status management (active/locked)

### Audit Trail
- All tables include created_by/updated_by fields
- Timestamp tracking for all operations
- User action attribution for compliance
- Role and permission assignment tracking