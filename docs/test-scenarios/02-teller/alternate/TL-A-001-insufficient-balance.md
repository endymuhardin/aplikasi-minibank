# Test Scenario: TL-A-001 - Insufficient Balance

## Scenario ID: TL-A-001
**Role**: Teller
**Type**: Alternate/Negative
**Module**: Cash Withdrawal & Transfers
**Priority**: High

## Overview
Test insufficient balance scenarios for withdrawals and transfers, including minimum balance maintenance, overdraft prevention, and frozen accounts.

## Test Cases

### TL-A-001-01: Withdrawal Below Minimum Balance
**Test Data**:
- Account Balance: 150,000
- Minimum Balance Required: 100,000
- Withdrawal Amount: 75,000
- Available for Withdrawal: 50,000

**Steps**:
1. Login as Teller
2. Input account number
3. Enter withdrawal: 75,000
4. Submit transaction

**Expected Result**:
- Error: "Insufficient balance. Available: 50,000"
- Minimum balance rule displayed
- Transaction rejected
- No balance change

### TL-A-001-02: Transfer Exceeding Available Balance
**Test Data**:
- Source Account Balance: 500,000
- Transfer Amount: 600,000

**Steps**:
1. Select transfer transaction
2. Enter source and destination accounts
3. Amount: 600,000
4. Submit transfer

**Expected Result**:
- Error: "Insufficient funds for transfer"
- Current balance shown
- Transfer cancelled
- Audit log created

### TL-A-001-03: Withdrawal from Zero Balance Account
**Test Data**:
- Account Balance: 0
- Withdrawal Attempt: 50,000

**Steps**:
1. Input account with zero balance
2. Attempt withdrawal

**Expected Result**:
- Error: "Account has zero balance"
- Suggest deposit transaction
- Transaction blocked

### TL-A-001-04: Multiple Transactions Causing Overdraft
**Test Scenario**: Race condition prevention
**Test Data**:
- Account Balance: 200,000
- Transaction 1: Withdraw 150,000
- Transaction 2: Transfer 100,000 (simultaneous)

**Steps**:
1. Two tellers process transactions simultaneously
2. Both transactions would cause negative balance

**Expected Result**:
- First transaction succeeds
- Second transaction fails
- Database locks prevent overdraft
- Balance never goes negative

### TL-A-001-05: Blocked Account Status
**Test Data**:
- Account Status: FROZEN
- Freeze Reason: "Court order"

**Steps**:
1. Attempt withdrawal from frozen account
2. Enter any amount

**Expected Result**:
- Error: "Account is frozen"
- Freeze reason displayed
- Manager authorization required
- Transaction blocked

## Playwright Test Mapping
- Test Class: `InsufficientBalanceAlternateTest`
- Parallel execution safe
- Uses database transactions for isolation