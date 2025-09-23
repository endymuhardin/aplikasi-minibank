# Test Scenario: CS-A-002 - Account Opening Rejections

## Scenario ID: CS-A-002
**Role**: Customer Service
**Type**: Alternate/Negative
**Module**: Account Opening
**Priority**: High

## Overview
Test account opening rejection scenarios including insufficient initial deposit, inactive products, customer status issues, and document verification failures.

## Test Cases

### CS-A-002-01: Below Minimum Opening Balance
**Test Data**:
- Product: TAB002 (Tabungan Mudharabah Premium)
- Minimum Opening Balance: 1,000,000
- Initial Deposit Attempted: 500,000

**Steps**:
1. Login as Customer Service
2. Select customer for account opening
3. Choose TAB002 product
4. Enter initial deposit: 500,000
5. Submit form

**Expected Result**:
- Validation error: "Initial deposit below minimum opening balance (1,000,000)"
- Form not submitted
- Field highlighted with error

### CS-A-002-02: Inactive Product Selection
**Precondition**: Product TAB003 set to inactive
**Steps**:
1. Attempt to select inactive product
2. Product should not appear in dropdown

**Expected Result**:
- Inactive products filtered from selection
- Only active products available
- Cannot manually enter inactive product code

### CS-A-002-03: Customer Status - Blacklisted
**Test Data**:
- Customer status: BLACKLISTED
- Reason: "Previous fraud case"

**Steps**:
1. Select blacklisted customer
2. Attempt account opening

**Expected Result**:
- Warning displayed: "Customer is blacklisted"
- Account opening blocked
- Manager override required
- Audit trail created

### CS-A-002-04: Maximum Accounts Per Product Limit
**Precondition**: Customer has 3 accounts with same product type
**Business Rule**: Max 3 accounts per product type

**Steps**:
1. Select customer with 3 existing accounts
2. Try to open 4th account with same product

**Expected Result**:
- Error: "Maximum account limit reached for this product"
- List of existing accounts shown
- Suggest different product type

### CS-A-002-05: Expired Identity Document
**Test Data**:
- NIK expiry date: Past date
- Document status: EXPIRED

**Steps**:
1. Customer with expired NIK
2. Attempt account opening

**Expected Result**:
- Warning: "Customer identity document expired"
- Update KYC required first
- Link to customer update form

### CS-A-002-06: Duplicate Account Number Generation
**Test Scenario**: Concurrent account creation
**Steps**:
1. Two CS agents create accounts simultaneously
2. System generates account numbers

**Expected Result**:
- Unique account numbers generated
- No duplicate account numbers
- Sequence number properly incremented
- Thread-safe generation confirmed

## Playwright Test Mapping
- Test Class: `AccountOpeningAlternateTest`
- Methods: `test_CS_A_002_01_belowMinimumBalance()` etc.
- CSV Fixtures: `account-opening-rejections.csv`