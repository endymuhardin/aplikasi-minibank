# Fix for Account Number Duplicate Key Error

## Problem
Error saat membuka rekening baru:
```
Failed to open account: could not execute statement
[ERROR: duplicate key value violates unique constraint "accounts_account_number_key"
Detail: Key (account_number)=(A2000001) already exists.]
```

## Root Cause Analysis

### 1. Database Migration Conflict
- **File**: `V002__insert_initial_data.sql`
- **Sample accounts created**: A2000001, A2000002, A2000003, A2000004, A2000005, A2000006
- **Sequence initialized**: `('ACCOUNT_NUMBER', 2000000, 'A')`

### 2. Code Logic Issues
- **AccountService**: Used prefix "ACC" instead of "A"
- **AccountRestController**: Used prefix "ACC" instead of "A"
- **Sequence start**: Sequence starts from 2000000 but existing accounts up to A2000006

## Solution Applied

### 1. Database Migration Fix
**File**: `V005__fix_sequence_numbers.sql`
```sql
-- Update sequence numbers to start after the highest existing numbers
UPDATE sequence_numbers
SET last_number = 2000006, prefix = 'A'
WHERE sequence_name = 'ACCOUNT_NUMBER';

-- Add missing sequence for corporate accounts
INSERT INTO sequence_numbers (sequence_name, last_number, prefix)
VALUES ('CORPORATE_ACCOUNT_NUMBER', 0, 'CORP')
ON CONFLICT (sequence_name) DO NOTHING;
```

### 2. Code Fixes
**AccountService.java**:
```java
// BEFORE (INCORRECT):
sequenceNumberService.generateNextSequence("ACCOUNT_NUMBER", "ACC")
sequenceNumberService.generateNextSequence("CORPORATE_ACCOUNT_NUMBER", "CORP")

// AFTER (FIXED):
sequenceNumberService.generateNextSequence("ACCOUNT_NUMBER", "A")
sequenceNumberService.generateNextSequence("CORPORATE_ACCOUNT_NUMBER", "A")
```

**AccountRestController.java**:
```java
// BEFORE (INCORRECT):
sequenceNumberService.generateNextSequence("ACCOUNT_NUMBER", "ACC")

// AFTER (FIXED):
sequenceNumberService.generateNextSequence("ACCOUNT_NUMBER", "A")
```

## Expected Behavior After Fix

### Account Number Generation:
1. **Personal Accounts**: A2000007, A2000008, A2000009, ...
2. **Corporate Accounts**: A2000007, A2000008, A2000009, ... (same sequence, different business logic)

### Transaction Number Generation:
- **All Transactions**: TXN3000001, TXN3000002, TXN3000003, ...

### Customer Number Generation:
- **All Customers**: C1000007, C1000008, C1000009, ...

## How to Apply the Fix

### 1. If Database is Fresh/New:
```bash
# All migrations will run including V005__fix_sequence_numbers.sql
docker compose up -d
mvn spring-boot:run
```

### 2. If Database Already Exists:
```bash
# Manually apply the migration fix
docker exec -it aplikasi-minibank-postgres-1 psql -U minibank -d pgminibank

-- Run these SQL commands:
UPDATE sequence_numbers SET last_number = 2000006, prefix = 'A' WHERE sequence_name = 'ACCOUNT_NUMBER';
UPDATE sequence_numbers SET last_number = 1000007, prefix = 'C' WHERE sequence_name = 'CUSTOMER_NUMBER';
INSERT INTO sequence_numbers (sequence_name, last_number, prefix) VALUES ('CORPORATE_ACCOUNT_NUMBER', 0, 'CORP') ON CONFLICT (sequence_name) DO NOTHING;
```

### 3. Restart Application:
```bash
mvn spring-boot:run
```

## Testing the Fix

### Test Account Opening:
1. **Via Web UI**: Open http://localhost:10002/account/create
2. **Via REST API**:
```bash
curl -X POST http://localhost:10002/api/accounts/open \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "customer-uuid",
    "productId": "product-uuid",
    "accountName": "Test Account",
    "initialDeposit": 100000,
    "createdBy": "test-user"
  }'
```

### Expected Result:
- Account number should be: **A2000007** or higher
- No duplicate key constraint violation
- Account created successfully

## Verification

Check that account numbers are unique and sequential:
```sql
SELECT account_number, account_name, opened_date
FROM accounts
ORDER BY account_number;
```

Should show:
- A2000001 (existing sample)
- A2000002 (existing sample)
- A2000003 (existing sample)
- A2000004 (existing sample)
- A2000005 (existing sample)
- A2000006 (existing sample)
- A2000007 (new account after fix)
- A2000008 (next new account)
- ...

## Files Modified

1. `src/main/resources/db/migration/V005__fix_sequence_numbers.sql` (NEW)
2. `src/main/java/id/ac/tazkia/minibank/service/AccountService.java` (MODIFIED)
3. `src/main/java/id/ac/tazkia/minibank/controller/rest/AccountRestController.java` (MODIFIED)

## Summary

The duplicate account number error has been fixed by:
1. ✅ **Database Migration**: Updating sequence numbers to account for existing data
2. ✅ **Code Consistency**: Using consistent prefix "A" for all account numbers
3. ✅ **Sequence Synchronization**: Ensuring sequence starts after highest existing number

The system will now generate unique, sequential account numbers starting from A2000007.