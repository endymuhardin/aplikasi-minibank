# Test Scenario: CS-S-001 - Customer Registration

## Scenario ID: CS-S-001
**Role**: Customer Service
**Type**: Success
**Module**: Customer Management
**Priority**: High

## Implementation Status
**Overall Status**: ✅ IMPLEMENTED (8/8 sub-scenarios)
**Test Class**: `CustomerManagementSuccessTest`
**Last Updated**: 2025-01-23

## Overview

This document outlines comprehensive test scenarios for the Customer Management module, covering both Personal and Corporate customer types. The scenarios are designed to work with existing seed data, test fixtures, and Playwright tests.

## Test Data Sources

### Seed Data (Migration V002)
Pre-loaded customers available for testing:

**Personal Customers:**
- C1000001: Ahmad Suharto (KTP: 3271081503850001)
- C1000002: Siti Nurhaliza (KTP: 3271082207900002) 
- C1000004: Budi Santoso (KTP: 3271081011880003)
- C1000006: Dewi Lestari (KTP: 3271081805920004)

**Corporate Customers:**
- C1000003: PT. Teknologi Maju (Reg: 1234567890123456)

### Test SQL Setup Data
Additional test customers for functional testing:

**Personal Test Customers:**
- EDIT001: John Doe (editable test customer)
- SEARCH001: Test Customer (search functionality test)
- STATUS001: Status Test (activate/deactivate test)
- VIEW001: View Test (detail view test)
- DUPLICATE001: Duplicate Test (duplicate validation test)

**Corporate Test Customers:**
- CORP_EDIT001: Corporate Edit Company (editable corporate customer)
- CORP_VIEW001: Corporate View Inc (corporate detail view test)

## Test Scenarios

### 1. Customer Registration

#### 1.1 Personal Customer Registration - Happy Path

**Test ID:** PC-REG-001  
**User Role:** Customer Service (cs1, cs2, cs3)  
**Data Source:** CSV fixtures `/fixtures/customer/personal-customer-creation-data.csv`

**Test Cases:**
1. **Basic Registration** - Register customer with all required fields
2. **KTP Identity** - Register with KTP identity document
3. **Passport Identity** - Register with Passport identity document  
4. **SIM Identity** - Register with SIM identity document

**Test Data Examples:**
```
customerNumber: PERS001
firstName: John
lastName: Doe
email: john.doe@example.com
phone: 081234567890
address: 123 Main Street
city: Jakarta
identityNumber: 1234567890123456
dateOfBirth: 1990-01-01
identityType: KTP
```

**Expected Results:**
- Customer successfully created with auto-generated customer number
- Database persistence verified
- Success message displayed
- Customer visible in customer list
- All audit fields populated (created_by, created_date)

#### 1.2 Corporate Customer Registration - Happy Path

**Test ID:** CC-REG-001  
**User Role:** Customer Service (cs1, cs2, cs3)  
**Data Source:** CSV fixtures `/fixtures/customer/corporate-customer-creation-data.csv`

**Test Cases:**
1. **Basic Corporate Registration** - Register company with all required fields
2. **Complete Corporate Profile** - Register with all optional fields filled

**Test Data Examples:**
```
customerNumber: C2000999
companyName: PT. Test Company
companyRegistrationNumber: 1234567890123456
taxIdentificationNumber: 01.234.567.8-901.000
contactPersonName: Contact Person
contactPersonTitle: Manager
email: info@testcompany.com
phone: 02123456789
address: Jl. Corporate No. 789
city: Jakarta
```

**Expected Results:**
- Corporate customer successfully created
- Database persistence in both customers and corporate_customers tables
- Success message displayed
- Customer visible in customer list with CORPORATE type
- All audit fields populated

### 2. Customer Validation

#### 2.1 Personal Customer Validation - Error Cases

**Test ID:** PC-VAL-001  
**User Role:** Customer Service  
**Data Source:** CSV fixtures `/fixtures/customer/personal-customer-validation-data.csv`

**Validation Test Cases:**

1. **Required Field Validation:**
   - Empty Customer Number → "Customer number is required"
   - Empty First Name → "First name is required"
   - Empty Last Name → "Last name is required"
   - Empty Email → "Email is required"
   - Empty Phone → "Phone is required"

2. **Format Validation:**
   - Invalid Email Format → "Invalid email format"
   - Invalid phone format → Phone validation error

3. **Length Validation:**
   - First Name > 100 chars → "First name too long"
   - Last Name > 100 chars → "Last name too long"
   - Email > 100 chars → "Email too long"

4. **Business Rule Validation:**
   - Duplicate Customer Number → "Customer number already exists"
   - Duplicate Identity Number → "Identity number already exists"

**Expected Results:**
- Form submission fails with validation errors
- User remains on form page
- Appropriate error messages displayed
- No database record created

#### 2.2 Corporate Customer Validation - Error Cases

**Test ID:** CC-VAL-001  
**User Role:** Customer Service  
**Data Source:** CSV fixtures `/fixtures/customer/corporate-customer-validation-data.csv`

**Validation Test Cases:**

1. **Required Field Validation:**
   - Empty Customer Number → "Customer number is required"
   - Empty Company Name → "Company name is required"
   - Empty Email → "Email is required"
   - Empty Phone → "Phone is required"

2. **Length Validation:**
   - Company Name > 200 chars → "Company name too long"
   - Email > 100 chars → "Email too long"

3. **Business Rule Validation:**
   - Duplicate Customer Number → "Customer number already exists"
   - Duplicate Company Registration Number → "Registration number already exists"

**Expected Results:**
- Form submission fails with validation errors
- User remains on form page
- Appropriate error messages displayed
- No database record created

### 3. Customer Search and Filtering

#### 3.1 Customer Search Functionality

**Test ID:** CUS-SEARCH-001  
**User Role:** Customer Service, Teller, Branch Manager  
**Test Data:** Seed data + SQL setup data

**Search Test Cases:**

1. **Search by Customer Number:**
   - Search "C1000001" → Returns Ahmad Suharto
   - Search "SEARCH001" → Returns Test Customer
   - Search partial number "C1000" → Returns multiple results

2. **Search by Name:**
   - Search "Ahmad" → Returns Ahmad Suharto
   - Search "Suharto" → Returns Ahmad Suharto
   - Search "Test Customer" → Returns search test customer

3. **Search by Email:**
   - Search "ahmad.suharto@email.com" → Returns Ahmad Suharto
   - Search partial email "@email.com" → Returns multiple results

4. **Search by Phone:**
   - Search "081234567890" → Returns matching customers

**Expected Results:**
- Accurate search results displayed
- Search results highlighted
- Case-insensitive search working
- Partial matches supported
- No results message when appropriate

#### 3.2 Customer Filtering

**Test ID:** CUS-FILTER-001  
**User Role:** Customer Service, Teller, Branch Manager

**Filter Test Cases:**

1. **Filter by Customer Type:**
   - PERSONAL filter → Shows only personal customers
   - CORPORATE filter → Shows only corporate customers
   - ALL → Shows all customers

2. **Filter by Status:**
   - ACTIVE → Shows only active customers
   - INACTIVE → Shows only inactive customers
   - ALL → Shows all statuses

3. **Combined Filters:**
   - PERSONAL + ACTIVE → Shows active personal customers only

**Expected Results:**
- Filtered results match criteria
- Filter indicators visible
- Clear filter option available
- Result count displayed correctly

### 4. Customer Details and Viewing

#### 4.1 Personal Customer Details

**Test ID:** PC-VIEW-001  
**User Role:** Customer Service, Teller, Branch Manager  
**Test Data:** VIEW001 customer from SQL setup

**View Test Cases:**

1. **Basic Information Display:**
   - Customer number: VIEW001
   - Full name: View Test
   - Email and phone correctly displayed
   - Address information complete

2. **Personal Specific Information:**
   - Identity type and number displayed
   - Date of birth formatted correctly
   - Age calculated accurately

3. **System Information:**
   - Customer status displayed
   - Created date and user shown
   - Last updated information visible

**Expected Results:**
- All customer information displayed accurately
- Personal customer specific fields visible
- Edit button available (for authorized users)
- Account summary section present
- Audit trail information displayed

#### 4.2 Corporate Customer Details

**Test ID:** CC-VIEW-001  
**User Role:** Customer Service, Teller, Branch Manager  
**Test Data:** CORP_VIEW001 customer from SQL setup

**View Test Cases:**

1. **Basic Company Information:**
   - Customer number: CORP_VIEW001
   - Company name: Corporate View Inc
   - Contact information complete

2. **Corporate Specific Information:**
   - Company registration number displayed
   - Tax ID shown
   - Contact person details visible

3. **Business Information:**
   - Company address complete
   - Business phone number
   - Corporate email address

**Expected Results:**
- All corporate information displayed accurately
- Corporate customer specific fields visible
- Edit button available (for authorized users)
- Related accounts section present
- Audit trail information displayed

### 5. Customer Editing and Updates

#### 5.1 Personal Customer Edit

**Test ID:** PC-EDIT-001  
**User Role:** Customer Service (CUSTOMER_UPDATE permission)  
**Test Data:** EDIT001 customer from SQL setup

**Edit Test Cases:**

1. **Update Contact Information:**
   - Change email address
   - Update phone number
   - Modify address

2. **Update Personal Information:**
   - Change first name
   - Change last name
   - Update date of birth (if allowed)

3. **Validation During Edit:**
   - Required field validation
   - Email format validation
   - Duplicate email prevention

**Test Steps:**
1. Navigate to customer list
2. Find EDIT001 customer
3. Click edit button
4. Verify form pre-populated with existing data
5. Update email to "updated{timestamp}@example.com"
6. Update phone to "087654321098"
7. Update address to "Updated Address 123"
8. Submit form
9. Verify success message
10. Verify changes in database

**Expected Results:**
- Form pre-populated with current data
- Updates saved successfully
- Database reflects changes
- Audit trail updated (updated_by, updated_date)
- Success message displayed

#### 5.2 Corporate Customer Edit

**Test ID:** CC-EDIT-001  
**User Role:** Customer Service (CUSTOMER_UPDATE permission)  
**Test Data:** CORP_EDIT001 customer from SQL setup

**Edit Test Cases:**

1. **Update Company Information:**
   - Change company name
   - Update contact person
   - Modify business address

2. **Update Contact Information:**
   - Change business email
   - Update business phone
   - Modify contact person title

**Test Steps:**
1. Navigate to customer list
2. Find CORP_EDIT001 customer
3. Click edit button
4. Update email to "corp_updated{timestamp}@example.com"
5. Update phone to "087654321099"
6. Update address to "Updated Corporate Address 456"
7. Submit form
8. Verify changes persisted

**Expected Results:**
- Corporate information updated successfully
- Database reflects changes
- Audit trail maintained
- Success confirmation displayed

### 6. Customer Status Management

#### 6.1 Customer Activation/Deactivation

**Test ID:** CUS-STATUS-001  
**User Role:** Customer Service, Branch Manager  
**Test Data:** STATUS001 customer from SQL setup

**Status Test Cases:**

1. **Deactivate Active Customer:**
   - Verify initial status is ACTIVE
   - Click deactivate button
   - Confirm deactivation
   - Verify status changed to INACTIVE

2. **Reactivate Inactive Customer:**
   - Start with INACTIVE customer
   - Click activate button
   - Confirm activation
   - Verify status changed to ACTIVE

3. **Status Validation:**
   - Cannot open accounts for INACTIVE customers
   - Status change audit trail maintained

**Expected Results:**
- Status changes immediately visible
- Database status field updated
- Status change logged in audit trail
- Related business rules enforced
- Confirmation messages displayed

### 7. Role-Based Access Control

#### 7.1 Customer Service Access

**Test ID:** RBAC-CS-001  
**User Role:** Customer Service (cs1, cs2, cs3)  
**Permissions:** CUSTOMER_VIEW, CUSTOMER_CREATE, CUSTOMER_UPDATE

**Access Test Cases:**

1. **Allowed Operations:**
   - View customer list ✓
   - Create new customers ✓
   - Edit existing customers ✓
   - View customer details ✓

2. **Restricted Operations:**
   - Cannot delete customers ✗
   - Cannot change customer status (depends on business rules) ✗

#### 7.2 Teller Access

**Test ID:** RBAC-TELLER-001  
**User Role:** Teller (teller1, teller2, teller3)  
**Permissions:** CUSTOMER_VIEW, BALANCE_VIEW

**Access Test Cases:**

1. **Allowed Operations:**
   - View customer list ✓
   - View customer details ✓
   - View account balances ✓

2. **Restricted Operations:**
   - Cannot create customers ✗
   - Cannot edit customers ✗
   - Cannot change customer status ✗

#### 7.3 Branch Manager Access

**Test ID:** RBAC-BM-001  
**User Role:** Branch Manager (admin, manager1, manager2)  
**Permissions:** All permissions

**Access Test Cases:**

1. **Full Access:**
   - All customer operations ✓
   - User management ✓
   - System administration ✓

### 8. Data Integrity and Business Rules

#### 8.1 Customer Number Generation

**Test ID:** DATA-INT-001  
**Sequence:** CUSTOMER_NUMBER (starting from C1000001)

**Test Cases:**

1. **Auto-generated Numbers:**
   - New personal customer gets C1000007, C1000008, etc.
   - New corporate customer gets sequential numbers
   - No duplicate numbers generated

2. **Manual Numbers:**
   - Allow manual customer number entry
   - Validate uniqueness
   - Prevent conflicts with auto-generated numbers

#### 8.2 Identity Document Validation

**Test ID:** DATA-INT-002

**Test Cases:**

1. **Personal Customer Identity:**
   - KTP: 16 digits, Indonesian format
   - Passport: Alphanumeric, country-specific format
   - SIM: Numeric, driving license format

2. **Corporate Registration:**
   - Company registration: Business-specific format
   - Tax ID: Indonesian tax format (XX.XXX.XXX.X-XXX.XXX)

### 9. Integration Tests

#### 9.1 Customer-Account Integration

**Test ID:** INT-CUST-ACC-001

**Test Cases:**

1. **Customer with Accounts:**
   - View customer details shows related accounts
   - Account creation references customer
   - Customer status affects account operations

2. **Customer-Transaction Relationship:**
   - Customer history includes all account transactions
   - Customer reports aggregate account data

### 10. Performance and Load Testing

#### 10.1 Customer List Performance

**Test ID:** PERF-LIST-001

**Test Cases:**

1. **Large Dataset Performance:**
   - Load time with 1000+ customers
   - Search performance with large dataset
   - Pagination efficiency

2. **Concurrent User Access:**
   - Multiple users viewing customer list
   - Concurrent customer creation
   - Edit conflicts handling

## Test Execution Notes

### Functional Test Configuration

**Browser Support:**
- Chrome (default)
- Firefox (use `-Dplaywright.browser=firefox`)

**Execution Modes:**
- Headless (default, fastest)
- Visible mode (debugging: `-Dplaywright.headless=false`)
- Recording mode (debugging: `-Dplaywright.recording.enabled=true`)

**Commands:**
```bash
# Run all customer management tests
mvn test -Dtest=CustomerManagementFunctionalTest

# Run with visible browser for debugging
mvn test -Dtest=CustomerManagementFunctionalTest -Dplaywright.headless=false

# Run specific test method
mvn test -Dtest=CustomerManagementFunctionalTest#shouldCreatePersonalCustomer
```

### Database Setup

**Test Data Setup:**
- SQL scripts: `/sql/setup-customer-test.sql`
- Cleanup: `/sql/cleanup-customer-test.sql`
- Executed automatically with `@SqlGroup` annotations

**Test Data Management:**
- Pre-test: Clean tables and insert test data
- Post-test: Clean tables for next test
- Isolation: Each test runs with fresh data

### CSV Test Data Management

**File Locations:**
- Personal customer creation: `/fixtures/customer/personal-customer-creation-data.csv`
- Corporate customer creation: `/fixtures/customer/corporate-customer-creation-data.csv`
- Personal validation: `/fixtures/customer/personal-customer-validation-data.csv`
- Corporate validation: `/fixtures/customer/corporate-customer-validation-data.csv`

**Data Format:**
- Headers describe test fields
- Each row represents one test case
- Supports parameterized testing with `@CsvFileSource`

## Test Coverage Summary

| Feature | Unit Tests | Integration Tests | Functional Tests | API Tests |
|---------|------------|-------------------|----------------|-----------|
| Customer Creation | ✓ | ✓ | ✓ | ✓ |
| Customer Validation | ✓ | ✓ | ✓ | ✓ |
| Customer Search | ✓ | ✓ | ✓ | ✓ |
| Customer Edit | ✓ | ✓ | ✓ | ✓ |
| Customer View | ✓ | ✓ | ✓ | ✓ |
| Status Management | ✓ | ✓ | ✓ | ✓ |
| RBAC | ✓ | ✓ | ✓ | ✓ |
| Data Integrity | ✓ | ✓ | - | ✓ |

## Maintenance Notes

1. **Test Data Updates:** When adding new test customers, update both CSV fixtures and SQL setup files
2. **Schema Changes:** Update validation tests when database schema changes
3. **Permission Changes:** Update RBAC tests when role permissions change
4. **UI Changes:** Update functional test page objects when UI elements change
5. **Business Rules:** Update validation tests when business logic changes