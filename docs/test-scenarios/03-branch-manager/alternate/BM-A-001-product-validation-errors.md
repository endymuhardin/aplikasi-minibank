# Test Scenario: BM-A-001 - Product Validation Errors

## Scenario ID: BM-A-001
**Role**: Branch Manager
**Type**: Alternate/Negative
**Module**: Product Management
**Priority**: High

## Overview
Test product creation and update validation errors including invalid nisbah ratios, duplicate product codes, and business rule violations.

## Test Cases

### BM-A-001-01: Invalid Nisbah Ratio (Not Sum to 1.0)
**Test Data**:
- Product Type: TABUNGAN_MUDHARABAH
- Nisbah Customer: 0.75
- Nisbah Bank: 0.30
- Total: 1.05 (Invalid)

**Steps**:
1. Login as Branch Manager
2. Create new Mudharabah product
3. Set nisbah customer: 0.75
4. Set nisbah bank: 0.30
5. Submit form

**Expected Result**:
- Error: "Nisbah ratios must sum to 1.0"
- Current sum shown: 1.05
- Form not submitted
- Fields highlighted

### BM-A-001-02: Duplicate Product Code
**Test Data**:
- Existing Product Code: TAB001
- Attempting to create: TAB001

**Steps**:
1. Navigate to product creation
2. Enter product code: TAB001
3. Fill other fields
4. Submit

**Expected Result**:
- Error: "Product code TAB001 already exists"
- Unique constraint violation
- Form returns with error
- Suggest alternative code

### BM-A-001-03: Negative Minimum Balance
**Test Data**:
- Minimum Balance: -100,000 (negative)
- Minimum Opening Balance: -50,000

**Steps**:
1. Enter negative values for balances
2. Submit product form

**Expected Result**:
- Validation errors for negative amounts
- "Minimum balance must be >= 0"
- Client-side validation prevents submission

### BM-A-001-04: Opening Balance Less Than Minimum
**Test Data**:
- Minimum Balance: 100,000
- Minimum Opening Balance: 50,000

**Steps**:
1. Set minimum balance: 100,000
2. Set opening balance: 50,000
3. Submit form

**Expected Result**:
- Error: "Opening balance cannot be less than minimum balance"
- Business rule validation
- Logical consistency enforced

### BM-A-001-05: Invalid Product Type for Customer Segment
**Test Data**:
- Product Type: PEMBIAYAAN_MURABAHAH
- Allowed Customer Types: PERSONAL (business requires CORPORATE)

**Steps**:
1. Create financing product
2. Set allowed types to PERSONAL only
3. Submit

**Expected Result**:
- Warning: "Pembiayaan products typically require CORPORATE customers"
- Confirmation required
- Business logic validation

### BM-A-001-06: Shariah Compliance Missing
**Test Data**:
- Product Type: DEPOSITO_MUDHARABAH
- Is Shariah Compliant: false
- Shariah Board Approval: empty

**Steps**:
1. Create Islamic product
2. Leave shariah fields empty
3. Submit

**Expected Result**:
- Error: "Islamic products require shariah compliance"
- Required fields:
  - Shariah board approval number
  - Approval date
  - Compliance certificate

## Playwright Test Mapping
- Test Class: `ProductValidationAlternateTest`
- Data-driven testing with error scenarios
- Validates both client and server-side