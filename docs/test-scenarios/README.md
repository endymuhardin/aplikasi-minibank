# Test Scenarios Documentation

> **Note**: For comprehensive testing guide including infrastructure, configuration, and execution, see [TESTING.md](../TESTING.md). This document focuses specifically on detailed test scenarios.

## Overview
Dokumentasi ini berisi test scenarios yang komprehensif untuk aplikasi minibank Islam. Semua test scenarios telah disesuaikan dengan actual database schema, entity validation rules, existing seed data, CSV fixtures, dan Playwright test patterns yang ada di codebase.

## Test Scenarios Overview

**Test Coverage Areas:**
- **Customer Management** - Full CRUD, search, validation scenarios
- **Product Management** - Islamic banking products, CRUD operations
- **Account Opening** - Personal & Corporate, Islamic banking compliance
- **Cash Deposit Transactions** - Complete Web UI workflow scenarios
- **Cash Withdrawal Transactions** - Complete Web UI dengan real-time validation
- **Transfer Operations** - Transfer antar rekening scenarios
- **Passbook Printing** - Transaction history and printing scenarios
- **RBAC Management** - Users, roles, permissions management
- **Authentication & Authorization** - Multi-role login, security scenarios
- **Dashboard & Navigation** - UI navigation workflow scenarios
- **Islamic Financing Applications** - Murabahah, Mudharabah, Musharakah scenarios
- **Account Statement PDF** - PDF generation scenarios

## Folder Structure

```
docs/test-scenarios/
├── README.md                           # Overview dan panduan penggunaan
├── account-management/                 # Test scenarios untuk manajemen account
│   ├── account-opening.md             # Pembukaan rekening baru
│   └── account-lifecycle.md           # Complete account lifecycle management
├── transactions/                      # Test scenarios untuk transaksi
│   ├── cash-deposit.md               # Setoran tunai
│   ├── cash-withdrawal.md            # Penarikan tunai
│   └── transfers.md                  # Transfer antar rekening (future implementation)
├── reporting/                        # Test scenarios untuk reporting
│   └── account-statement-pdf.md      # Cetak rekening koran PDF
├── administration/                   # Test scenarios untuk admin functions
│   └── product-management.md         # Islamic banking product management
├── system-management/               # Test scenarios untuk system admin
│   └── rbac-data-management.md      # Role-based access control management
├── islamic-financing/               # Test scenarios untuk Islamic financing
│   └── islamic-financing-products.md # Murabahah, Mudharabah, Musharakah, Ijarah, Salam, Istisna
└── compliance/                      # Test scenarios untuk audit dan compliance
    └── audit-and-compliance.md      # Regulatory compliance, audit trails, AML, KYC
```

## Comprehensive Coverage

### 1. Core Banking Operations
- **Account Management** - Account opening, lifecycle management, status changes
- **Transaction Processing** - Cash deposits, withdrawals, transfers
- **Account Statements** - PDF generation dengan flexible printer support
- **Customer Management** - Personal & Corporate customers dengan Islamic banking

### 2. Islamic Banking Features
- **Islamic Financing Products** - Murabahah, Mudharabah, Musharakah applications
- **Asset-Based Financing** - Ijarah (leasing), Salam (forward sale), Istisna (manufacturing)
- **Profit-Loss Sharing** - Authentic Mudharabah and Musharakah partnerships (nisbah calculations)
- **Shariah Compliance** - Complete compliance validation and audit trails

### 3. Administrative Functions  
- **Product Management** - CRUD operations untuk Islamic banking products
- **RBAC Management** - User, role, dan permission management
- **Audit & Compliance** - Basic audit trails and regulatory reporting UI

### 4. System Integration & Compliance
- **Database Schema Compliance** - Semua test data sesuai migration files
- **Entity Validation** - Bean validation annotations (@NotBlank, @Email, @Size)
- **Functional Test Integration** - Page Object Model dan test data fixtures using Playwright
- **CSV Test Data** - Integration dengan existing fixtures
- **Seed Data Synchronization** - Test scenarios menggunakan actual seed data
- **Regulatory Compliance** - Indonesian banking regulations (BI, OJK, PPATK)

## Database Schema Compliance

Semua test scenarios telah divalidasi terhadap:

### Migration Files Validation
- **V001__create_bank_schema.sql** - Core banking entities
- **V002__insert_initial_data.sql** - Islamic banking products dan sample customers
- **V003__create_user_permission_schema.sql** - RBAC system
- **V004__insert_roles_permissions_data.sql** - Users, roles, permissions dengan seed data
- Field names, data types, constraints, dan business rules

### Entity Classes Validation
- **Customer.java** - Base customer dengan joined inheritance
- **PersonalCustomer.java/CorporateCustomer.java** - Customer type specific fields
- **Account.java** - Account entity dengan business methods
- **Transaction.java** - Transaction entity dengan enums dan channels
- **Product.java** - Islamic banking products dengan profit sharing
- **User.java/Role.java/Permission.java** - RBAC entities
- Bean Validation annotations dan business constraints

### Seed Data Integration
All test scenarios reference actual seed data:

#### Islamic Banking Products (dari V002)
```sql
TAB001: Tabungan Wadiah Basic (min: 50,000)
TAB002: Tabungan Mudharabah Premium (min: 1,000,000, nisbah: 70:30)  
DEP001: Deposito Mudharabah (min: 100,000, nisbah: 70:30)
PEM001: Pembiayaan Murabahah (corporate, min: 5,000,000)
PEM002: Pembiayaan Musharakah (min: 2,000,000, nisbah: 60:40)
```

#### Sample Customers (dari V002)
```sql
C1000001: Ahmad Suharto (Personal, KTP: 3271081503850001)
C1000002: Siti Nurhaliza (Personal, KTP: 3271082207900002)
C1000003: PT. Teknologi Maju (Corporate, NPWP: 01.234.567.8-901.000)
```

#### RBAC Users (dari V004)
```sql
admin: System Administrator (BRANCH_MANAGER)
manager1-2: Branch Managers (BRANCH_MANAGER)
teller1-3: Teller staff (TELLER)
cs1-3: Customer Service staff (CUSTOMER_SERVICE)
Password untuk semua: minibank123
```

#### Permissions (29 permissions dari CSV fixtures)
```sql
Categories: CUSTOMER, ACCOUNT, TRANSACTION, PRODUCT, USER, ROLE, REPORT, AUDIT, CONFIG
Actions: VIEW, CREATE, UPDATE, DELETE, APPROVE, ASSIGN, EXPORT, etc.
```

## CSV Fixtures Integration

Test scenarios menggunakan existing CSV fixtures untuk data-driven testing:

### Customer Data
- `personal-customer-creation-data.csv` - Valid personal customer data
- `corporate-customer-creation-data.csv` - Valid corporate customer data  
- `personal-customer-validation-data.csv` - Validation error scenarios
- `corporate-customer-validation-data.csv` - Corporate validation scenarios

### Account & Transaction Data
- `account-opening-normal.csv` - Successful account opening scenarios
- `account-opening-validation.csv` - Account opening validation errors
- `deposit-normal.csv` - Successful deposit transactions
- `withdrawal-normal.csv` - Successful withdrawal transactions
- `deposit-validation.csv` - Deposit validation scenarios

### Product Management Data
- `product-creation-data.csv` - Islamic banking product test data
- `product-validation-data.csv` - Product validation scenarios
- `product-search-data.csv` - Search and filter test data

### RBAC Data
- `users.csv` - User test data untuk different roles
- `roles.csv` - Role definitions dan descriptions
- `permissions.csv` - Complete permission definitions
- `user_roles.csv` - User-role assignments
- `role_permissions.csv` - Role-permission mappings
- `user_authentication_scenarios.csv` - Login test scenarios

### Functional Test Integration Data
- `login_test_data.csv` - Login scenarios untuk functional tests
- `dashboard_navigation_data.csv` - Navigation test data

## Functional Test Implementation Status

### ✅ **Phase 1 P0 Critical Success Scenarios - IMPLEMENTED**

**Test Classes Implemented** (31 test methods total):

1. **CustomerManagementSuccessTest** (10 test methods)
   - Customer list access and navigation
   - Customer search functionality
   - Customer detail view validation
   - Customer creation form accessibility
   - Page Object: `CustomerManagementPage` ✅

2. **AccountOpeningSuccessTest** (10 test methods)
   - Account list display and navigation  
   - Account creation form functionality
   - Islamic banking product integration
   - Account status validation
   - Page Object: `AccountManagementPage` ✅

3. **TransactionSuccessTest** (11 test methods)
   - Transaction form accessibility
   - Cash deposit and withdrawal navigation
   - Transaction list functionality
   - Multi-channel support validation
   - Page Object: `TransactionPage` ✅

### Base Test Infrastructure
```java
// Using enhanced BasePlaywrightTest infrastructure
public abstract class BasePlaywrightTest extends BaseIntegrationTest {
    // Enhanced with video recording, slow motion debugging
    // Cross-browser support (Chromium, Firefox, WebKit)
    // Localization-safe assertions using URL patterns and element visibility
}
```

### Page Object Model Implementation
- **CustomerManagementPage** - Customer management UI elements ✅
- **AccountManagementPage** - Account management UI elements ✅  
- **TransactionPage** - Transaction processing UI elements ✅
- **LoginPage** - Login form interactions ✅
- **DashboardPage** - Dashboard navigation ✅

### Enhanced Debugging Features
- **Video Recording**: `-Dplaywright.record=true`
- **Slow Motion**: `-Dplaywright.slowmo=500` 
- **Cross-browser Testing**: Firefox, WebKit support
- **Headed Mode**: `-Dplaywright.headless=false` for development

### SQL Test Data Setup
```java
@SqlGroup({
    @Sql(scripts = "/sql/setup-test-data.sql", executionPhase = BEFORE_TEST_METHOD),
    @Sql(scripts = "/sql/cleanup-test-data.sql", executionPhase = AFTER_TEST_METHOD)
})
```

### Parameterized Testing Integration
```java
@ParameterizedTest
@CsvFileSource(resources = "/fixtures/product/product-creation-data.csv", numLinesToSkip = 1)
void shouldCreateProductSuccessfully(String productCode, String productName, String productType) {
    // Test implementation using CSV data
}
```

## Business Logic Integration

### Islamic Banking Compliance
- **Nisbah Validation**: Customer + Bank ratio = 1.0 untuk MUDHARABAH/MUSHARAKAH
- **Shariah Compliance**: All Islamic products marked dengan is_shariah_compliant = true
- **Profit Sharing Types**: WADIAH (no sharing), MUDHARABAH/MUSHARAKAH (sharing required)
- **Product Categories**: Tabungan Syariah, Deposito Syariah, Pembiayaan Syariah

### Entity Business Methods
```java
// Test scenarios menggunakan actual entity methods
account.deposit(amount);        // Business logic untuk deposits
account.withdraw(amount);       // Business logic untuk withdrawals
customer.getDisplayName();      // Customer name formatting
product.isShariahCompliant();   // Islamic banking validation
```

### Authorization Integration
- **Role-Based Access**: CS (create accounts), Teller (transactions), Manager (all)
- **Permission Granularity**: Fine-grained permissions untuk each feature
- **Security Testing**: Unauthorized access prevention dan proper error handling

## API Testing Integration

### REST Endpoints Coverage
```bash
# Account Management
POST /api/accounts/open              # Account opening
GET  /api/accounts/{id}/balance      # Balance inquiry
PATCH /api/accounts/{id}/status      # Account status changes
POST /api/accounts/{id}/close        # Account closure

# Transaction Processing
POST /api/transactions/deposit       # Cash deposits
POST /api/transactions/withdrawal    # Cash withdrawals
POST /api/transactions/transfer      # Transfer operations (planned)

# Islamic Financing
POST /api/islamic-financing/murabahah     # Murabahah applications
POST /api/islamic-financing/mudharabah    # Mudharabah partnerships
POST /api/islamic-financing/profit-distribution  # Profit sharing

# Product Management  
GET  /api/products                   # Product listing
POST /api/products                   # Product creation
PUT  /api/products/{id}              # Product updates

# RBAC Management
GET  /api/users                      # User listing
POST /api/users                      # User creation
PUT  /api/users/{id}/roles           # Role assignments

# Reporting & Compliance
POST /api/accounts/{id}/statement    # PDF statement generation
GET  /api/audit/transactions/{id}    # Audit trail inquiry
POST /api/compliance/reports         # Regulatory reporting
GET  /api/aml/status/{customerId}    # AML compliance status
```

### Security Testing
- **Authorization Headers**: Bearer token validation
- **Role-Based Endpoints**: Different access levels per role
- **Data Isolation**: Users can only access own data (customers)
- **Audit Logging**: All administrative actions logged

## Performance Testing

### Load Testing Scenarios
- **100 concurrent account openings** dengan validation
- **10,000 deposit transactions** dalam 10 menit
- **500+ transaction PDF generation** dalam <30 detik
- **1000+ product search/filter operations**
- **Large user/role dataset management** (10,000+ users)

### Database Performance
- **Query Optimization**: Proper indexing untuk search operations
- **Concurrent Transactions**: Race condition prevention
- **Connection Pooling**: Efficient database resource usage
- **Bulk Operations**: Efficient batch processing

## Security & Compliance

### Data Privacy
- **Customer Data Isolation**: Customers access only own accounts
- **Role-Based Access**: Staff access based on job functions
- **Audit Trail**: Complete logging untuk compliance
- **Sensitive Data**: Proper handling untuk PII dan financial data

### Islamic Banking Compliance
- **Shariah Board Approval**: Tracking untuk product approvals
- **Profit Sharing**: Accurate nisbah calculations
- **Interest-Free Operations**: No interest-based transactions
- **Compliance Reporting**: Reports untuk regulatory requirements

## Usage Guidelines

### For Developers
1. **Follow Existing Patterns**: Use established Page Object Model dan test infrastructure
2. **Reference Seed Data**: Use actual product codes (TAB001, TAB002) dan user accounts
3. **Validate Against Schema**: Ensure test data matches database constraints
4. **Use CSV Fixtures**: Leverage existing data files untuk consistency
5. **Implement Business Logic**: Use entity methods rather than direct field manipulation

### For QA Engineers
1. **Execute Test Suites**: Run tests in order specified dalam documentation
2. **Validate Database State**: Check data integrity after each test
3. **Use Cleanup Scripts**: Maintain test environment cleanliness
4. **Monitor Performance**: Track response times dan resource usage
5. **Security Testing**: Verify access controls dan authorization

### For Business Analysts
1. **Islamic Banking Rules**: Understand Shariah compliance requirements
2. **User Workflows**: Validate end-to-end business processes
3. **Role Definitions**: Ensure proper separation of duties
4. **Audit Requirements**: Verify compliance reporting capabilities

## Maintenance & Updates

### Regular Synchronization
- **Schema Changes**: Update test scenarios ketika database schema changes
- **Seed Data Updates**: Maintain alignment dengan migration files
- **CSV Fixtures**: Keep test data current dengan business requirements
- **Functional Test Elements**: Update page objects ketika UI changes

### Test Data Management
- **Isolation**: Each test suite uses independent test data
- **Cleanup**: Automated cleanup scripts untuk each test category
- **Fixtures**: Centralized CSV files untuk consistent test data
- **Seed Data**: Production-like data untuk realistic testing

### Performance Monitoring
- **Baseline Metrics**: Track performance benchmarks over time
- **Resource Usage**: Monitor memory, CPU, dan database load
- **Response Times**: Ensure SLA compliance untuk all operations
- **Scalability**: Test with increasing data volumes

## Integration Points

### Cross-Module Dependencies
- **Account Opening** → **Product Configuration**: Account opening validates against product rules
- **Transactions** → **Account Status**: Deposits require active account status
- **PDF Generation** → **Transaction History**: Statement generation queries transaction data
- **User Management** → **Audit Logging**: All admin actions create audit records
- **Role Changes** → **Session Management**: Permission changes affect active sessions

### External Integrations
- **PDF Generation**: Integration dengan PDF libraries
- **Email Notifications**: User management email triggers
- **Audit Reporting**: Compliance report generation
- **Backup Systems**: Test data backup dan recovery

---

**Last Updated**: 2025-08-30
**Schema Version**: V004 (current migration)  
**Test Coverage**: 8 major functional areas dengan comprehensive test scenarios
**Integration**: CSV fixtures dan seed data alignment  
**Compliance**: Islamic banking requirements, Indonesian banking regulations, audit trails
**Functional Tests**: Phase 1 P0 critical success scenarios implemented (31 test methods across 3 test classes)  

## Test Scenario Categories

### 1. Account Management Test Scenarios
   - Account opening (Personal & Corporate customers)
   - Islamic banking product selection and validation
   - Account list, search, filter functionality
   - Multi-account support per customer
   - Comprehensive field validation and business rules

### 2. Customer Management Test Scenarios
   - Personal customer CRUD operations
   - Corporate customer management dengan legal validation
   - Customer search, filter, pagination
   - Customer data validation dan field constraints

### 3. Product Management Test Scenarios
   - Islamic banking product CRUD operations
   - Profit sharing ratio management (nisbah validation)
   - Product search, filter, activation/deactivation
   - Shariah compliance settings

### 4. Transaction Processing Test Scenarios
   - Cash deposits workflow scenarios
   - Cash withdrawals dengan real-time validation
   - Transaction list, search, filter by type
   - Transaction detail view dengan balance calculations
   - Transfer operations scenarios

### 5. RBAC Management Test Scenarios
   - User creation, editing, activation/deactivation
   - Role management dan permission assignments
   - Multi-role user assignments
   - Permission management dan granular access control
   - Security testing dan authorization validation

### 6. Authentication & Navigation Test Scenarios
   - Multi-role login testing (Admin, Manager, Teller, CS)
   - Dashboard navigation workflows
   - Security testing dan unauthorized access prevention
   - Session management dan logout functionality

### 7. PDF Reporting Test Scenarios
   - Account statement PDF generation
   - Passbook printing functionality
   - Transaction receipt printing

### 8. Islamic Financing Applications Test Scenarios
   - Murabahah application forms
   - Mudharabah partnership applications
   - Profit distribution workflows
   - Islamic financing document generation

### 9. Advanced Compliance Test Scenarios
   - AML monitoring dashboards
   - KYC workflow forms
   - Regulatory reporting interfaces
   - Compliance audit report generation