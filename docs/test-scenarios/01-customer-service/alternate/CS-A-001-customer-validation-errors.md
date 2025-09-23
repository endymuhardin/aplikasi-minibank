# Test Scenario: CS-A-001 - Customer Validation Errors

## Scenario ID: CS-A-001
**Role**: Customer Service
**Type**: Alternate/Negative
**Module**: Customer Registration
**Priority**: High

## Overview
Test validation errors and edge cases during customer registration including duplicate NIK, invalid data formats, missing required fields, and blacklist checking.

## Test Cases

### CS-A-001-01: Duplicate NIK (Personal Customer)
**Precondition**: Personal customer with NIK 3273241708900001 already exists
**Steps**:
1. Login as Customer Service
2. Navigate to customer registration
3. Select "Personal Customer"
4. Enter existing NIK: 3273241708900001
5. Fill other required fields
6. Submit form

**Expected Result**:
- Validation error: "NIK already registered"
- Form not submitted
- Existing customer info displayed
- Option to view existing customer

### CS-A-001-02: Duplicate NPWP (Corporate Customer)
**Precondition**: Corporate customer with NPWP 01.234.567.8-901.000 exists
**Steps**:
1. Login as Customer Service
2. Navigate to customer registration
3. Select "Corporate Customer"
4. Enter existing NPWP: 01.234.567.8-901.000
5. Fill other required fields
6. Submit form

**Expected Result**:
- Validation error: "NPWP already registered"
- Form not submitted
- Existing corporate customer displayed

### CS-A-001-03: Invalid Email Format
**Test Data**:
- Invalid emails: "notanemail", "@invalid.com", "user@", "user@@domain.com"

**Steps**:
1. Enter invalid email format
2. Tab out of field or submit

**Expected Result**:
- Real-time validation error
- "Please enter valid email address"
- Field highlighted in red

### CS-A-001-04: Invalid Phone Number Format
**Test Data**:
- Invalid phones: "123", "abcd", "+6208", "081234567890123456"

**Steps**:
1. Enter invalid phone format
2. Submit form

**Expected Result**:
- Validation error for phone format
- Indonesian phone: 10-13 digits starting with 08/628

### CS-A-001-05: Missing Required Fields
**Required Fields**:
- Personal: NIK, Full Name, Date of Birth, Address
- Corporate: NPWP, Company Name, Business Type, Address

**Steps**:
1. Leave required fields empty
2. Submit form

**Expected Result**:
- Multiple validation errors shown
- All required fields highlighted
- Form not submitted

### CS-A-001-06: Age Validation (Under 17)
**Test Data**:
- Date of Birth: Current date - 16 years

**Steps**:
1. Enter DOB making customer under 17
2. Submit form

**Expected Result**:
- Validation error: "Customer must be at least 17 years old"
- Form not submitted

### CS-A-001-07: Blacklist Customer Check
**Test Data**:
- NIK on blacklist: 3273241708900099 (test blacklist entry)

**Steps**:
1. Enter blacklisted NIK
2. Complete form
3. Submit

**Expected Result**:
- Warning: "Customer is on blacklist"
- Reason displayed
- Manager approval required to proceed
- Audit log created

### CS-A-001-08: Special Characters in Name
**Test Data**:
- Names: "John@Doe", "Jane#Smith", "Robert<script>"

**Steps**:
1. Enter name with special characters
2. Submit form

**Expected Result**:
- Validation error: "Name can only contain letters, spaces, and apostrophes"
- XSS prevention active

## Playwright Test Mapping
- Test Class: `CustomerValidationAlternateTest`
- Test Methods map to scenario IDs
- Uses Page Object Model
- Data-driven with CSV fixtures