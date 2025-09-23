# Test Scenarios Documentation

## Overview
Comprehensive test scenarios for Islamic minibank application, organized by user role and scenario type for easy navigation and Playwright test mapping.

## Naming Convention

### Scenario ID Format
```
[ROLE]-[TYPE]-[NUMBER]-[description]

Where:
- ROLE: CS (Customer Service), TL (Teller), BM (Branch Manager), CM (Customer Mobile), SA (System Admin)
- TYPE: S (Success/Happy Path), A (Alternate/Negative)
- NUMBER: Sequential 3-digit number (001, 002, etc.)
- description: Kebab-case description
```

### Examples
- `CS-S-001-customer-registration.md` - Customer Service success scenario for registration
- `TL-A-001-insufficient-balance.md` - Teller alternate scenario for insufficient balance
- `BM-S-001-product-management.md` - Branch Manager success scenario for products

## Directory Structure

```
docs/test-scenarios/
├── README.md                              # Overview and navigation guide
├── 01-customer-service/                   # Customer Service role
│   ├── success/                          # Happy path scenarios
│   │   ├── CS-S-001-customer-registration.md
│   │   ├── CS-S-002-account-opening.md
│   │   └── CS-S-003-passbook-issuance.md
│   └── alternate/                        # Error cases & validations
│       ├── CS-A-001-customer-validation-errors.md
│       └── CS-A-002-account-opening-rejections.md
│
├── 02-teller/                            # Teller role
│   ├── success/
│   │   ├── TL-S-001-cash-deposit.md
│   │   ├── TL-S-002-cash-withdrawal.md
│   │   └── TL-S-003-transfers.md
│   └── alternate/
│       ├── TL-A-001-insufficient-balance.md
│       └── TL-A-002-transaction-limits.md
│
├── 03-branch-manager/                    # Branch Manager role
│   ├── success/
│   │   ├── BM-S-001-product-management.md
│   │   ├── BM-S-002-account-lifecycle.md
│   │   ├── BM-S-003-report-generation.md
│   │   └── BM-S-004-audit-compliance.md
│   └── alternate/
│       └── BM-A-001-product-validation-errors.md
│
├── 04-customer-mobile/                   # Future: Mobile banking
│   ├── success/
│   │   └── CM-S-001-balance-inquiry.md
│   └── alternate/
│       └── CM-A-001-authentication-failures.md
│
└── 05-system-admin/                      # System Administrator
    └── success/
        └── SA-S-001-rbac-management.md
```

## Test Coverage Matrix

### Customer Service (CS)
| Scenario ID | Description | Type | Priority |
|------------|-------------|------|----------|
| CS-S-001 | Customer Registration | Success | High |
| CS-S-002 | Account Opening | Success | High |
| CS-S-003 | Passbook Issuance | Success | Medium |
| CS-A-001 | Customer Validation Errors | Alternate | High |
| CS-A-002 | Account Opening Rejections | Alternate | High |

### Teller (TL)
| Scenario ID | Description | Type | Priority |
|------------|-------------|------|----------|
| TL-S-001 | Cash Deposit | Success | High |
| TL-S-002 | Cash Withdrawal | Success | High |
| TL-S-003 | Transfers | Success | High |
| TL-A-001 | Insufficient Balance | Alternate | High |
| TL-A-002 | Transaction Limits | Alternate | High |

### Branch Manager (BM)
| Scenario ID | Description | Type | Priority |
|------------|-------------|------|----------|
| BM-S-001 | Product Management | Success | High |
| BM-S-002 | Account Lifecycle | Success | Medium |
| BM-S-003 | Report Generation | Success | Medium |
| BM-S-004 | Audit Compliance | Success | Low |
| BM-A-001 | Product Validation Errors | Alternate | High |

### Customer Mobile (CM) - Future Development
| Scenario ID | Description | Type | Status |
|------------|-------------|------|--------|
| CM-S-001 | Balance Inquiry | Success | Future |
| CM-A-001 | Authentication Failures | Alternate | Future |

### System Administrator (SA)
| Scenario ID | Description | Type | Priority |
|------------|-------------|------|----------|
| SA-S-001 | RBAC Management | Success | Medium |

## Playwright Test Mapping

### Test Class Naming Convention
```java
// Success scenarios
CustomerRegistrationSuccessTest    // Maps to CS-S-001
CashDepositSuccessTest             // Maps to TL-S-001
ProductManagementSuccessTest       // Maps to BM-S-001

// Alternate scenarios
CustomerValidationAlternateTest    // Maps to CS-A-001
InsufficientBalanceAlternateTest  // Maps to TL-A-001
ProductValidationAlternateTest     // Maps to BM-A-001
```

### Test Method Naming
```java
@Test
void test_CS_S_001_01_createPersonalCustomer() { }

@Test
void test_TL_A_001_02_transferExceedingBalance() { }
```

## Test Data Management

### CSV Fixtures Location
```
src/test/resources/fixtures/
├── customer/
│   ├── personal-customer-data.csv
│   └── corporate-customer-data.csv
├── account/
│   ├── account-opening-data.csv
│   └── account-validation-errors.csv
├── transaction/
│   ├── deposit-data.csv
│   ├── withdrawal-data.csv
│   └── transfer-data.csv
└── product/
    ├── product-creation-data.csv
    └── product-validation-errors.csv
```

## Islamic Banking Terminology

### Products
- **Tabungan Wadiah**: Safekeeping savings (no profit sharing)
- **Tabungan Mudharabah**: Profit-sharing savings
- **Deposito Mudharabah**: Profit-sharing time deposit

### Terms
- **Nisbah**: Profit sharing ratio (customer:bank)
- **Akad**: Islamic contract
- **Shariah Compliant**: Following Islamic banking principles

## Execution Guidelines

### Sequential Testing (Default)
```bash
mvn test -Dtest=**/success/*Test
```

### Parallel Testing
```bash
mvn test -Dtest.profile=parallel -Dtest=**/success/*Test
```

### Role-Specific Testing
```bash
# Customer Service tests only
mvn test -Dtest=**/CustomerRegistration*Test,**/AccountOpening*Test

# Teller tests only
mvn test -Dtest=**/CashDeposit*Test,**/CashWithdrawal*Test,**/Transfer*Test

# Branch Manager tests only
mvn test -Dtest=**/ProductManagement*Test,**/AccountLifecycle*Test
```

### Scenario Type Testing
```bash
# Success scenarios only
mvn test -Dtest=**/success/*Test

# Alternate scenarios only
mvn test -Dtest=**/alternate/*Test
```

## Test Reporting

### Coverage Goals
- Success Scenarios: 100% of happy paths
- Alternate Scenarios: All validation rules and error cases
- Integration: End-to-end workflows across roles

### Metrics Tracked
- Test execution time per scenario
- Pass/fail rates by role
- Defect density by module
- Test coverage percentage

## Maintenance

### Adding New Scenarios
1. Choose appropriate role folder (01-customer-service, 02-teller, etc.)
2. Determine scenario type (success or alternate)
3. Use next sequential number in naming
4. Follow the template structure
5. Update this README with new scenario

### Scenario Template
```markdown
# Test Scenario: [ID] - [Title]

## Scenario ID: [XX-X-XXX]
**Role**: [Role Name]
**Type**: [Success/Alternate]
**Module**: [Module Name]
**Priority**: [High/Medium/Low]

## Overview
[Brief description]

## Test Cases

### [ID]-01: [Test Case Name]
**Test Data**:
- Field1: Value1
- Field2: Value2

**Steps**:
1. Step 1
2. Step 2
3. Step 3

**Expected Result**:
- Result 1
- Result 2

## Playwright Test Mapping
- Test Class: `[TestClassName]`
- Test Methods: `test_[ID]_01_methodName()`
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