# Test Scenario: TL-A-002 - Transaction Limits

## Scenario ID: TL-A-002
**Role**: Teller
**Type**: Alternate/Negative
**Module**: Transaction Processing
**Priority**: High

## Overview
Test daily and monthly transaction limits, including withdrawal limits, transfer limits, and transaction count restrictions.

## Test Cases

### TL-A-002-01: Daily Withdrawal Limit Exceeded
**Test Data**:
- Daily Withdrawal Limit: 5,000,000
- Today's Withdrawals: 4,500,000
- New Withdrawal Attempt: 1,000,000

**Steps**:
1. Login as Teller
2. Customer already withdrew 4,500,000 today
3. Attempt withdrawal of 1,000,000
4. Submit transaction

**Expected Result**:
- Error: "Daily withdrawal limit exceeded"
- Show remaining limit: 500,000
- Today's total: 4,500,000
- Transaction rejected

### TL-A-002-02: Monthly Transaction Count Exceeded
**Test Data**:
- Monthly Transaction Limit: 100 transactions
- Current Month Count: 100
- Attempting: Transaction #101

**Steps**:
1. Account has reached 100 transactions this month
2. Attempt any new transaction

**Expected Result**:
- Error: "Monthly transaction limit reached (100)"
- Suggest account upgrade
- Transaction blocked
- Reset date shown

### TL-A-002-03: Single Transaction Amount Limit
**Test Data**:
- Maximum Single Transaction: 50,000,000
- Attempted Amount: 75,000,000

**Steps**:
1. Enter transfer amount: 75,000,000
2. Submit transaction

**Expected Result**:
- Error: "Transaction exceeds maximum limit (50,000,000)"
- Split transaction suggested
- Manager override option

### TL-A-002-04: ATM Daily Limit vs Teller Limit
**Test Data**:
- ATM Daily Limit: 2,500,000
- Teller Daily Limit: 10,000,000
- Channel: TELLER

**Steps**:
1. Process teller withdrawal: 5,000,000
2. Verify against teller limits, not ATM

**Expected Result**:
- Transaction approved (within teller limit)
- Channel-specific limits applied
- Audit shows TELLER channel

### TL-A-002-05: Free Transaction Quota Exceeded
**Test Data**:
- Free Transactions/Month: 15
- Used Free Transactions: 15
- Fee per Transaction: 2,500

**Steps**:
1. Account used all free transactions
2. Process new transaction

**Expected Result**:
- Warning: "Transaction fee will apply: 2,500"
- Confirmation required
- Fee deducted if confirmed
- Fee breakdown shown

## Playwright Test Mapping
- Test Class: `TransactionLimitsAlternateTest`
- Method naming: `test_TL_A_002_01_dailyWithdrawalLimit()`
- Test data from CSV fixtures