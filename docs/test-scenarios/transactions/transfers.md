# Test Scenarios: Transfer Antar Rekening

## Overview
Dokumen ini berisi skenario test untuk fitur transfer antar rekening dalam aplikasi minibank Islam. Transfer operations mencakup transfer internal antar rekening dalam bank yang sama, validasi saldo, limit checking, dan pencatatan transaksi ganda (debit dan credit).

## Implementation Status
**⚠️ Note**: Berdasarkan README.md, transfer functionality belum diimplementasi ("❓ Transfer: Belum diimplementasi"). Test scenarios ini disusun sebagai panduan untuk implementasi future dan testing comprehensive.

## Preconditions
- Database PostgreSQL berjalan
- Aplikasi Spring Boot aktif
- User sudah login sebagai Teller atau Customer Service
- Source dan destination accounts exist dan dalam status ACTIVE
- Source account memiliki saldo mencukupi
- Sequence number service berfungsi normal

## Planned Implementation Architecture

### Expected Entity Extensions
```java
// Transaction.java - Additional transaction types
public enum TransactionType {
    DEPOSIT, WITHDRAWAL, 
    TRANSFER_IN,     // Credit to destination account
    TRANSFER_OUT,    // Debit from source account
    INTEREST, FEE
}

// Transaction.java - Transfer reference
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "id_accounts_destination")
private Account destinationAccount;  // For transfer operations
```

### Expected REST API Endpoints
```java
@PostMapping("/api/transactions/transfer")
public ResponseEntity<Object> transfer(@Valid @RequestBody TransferRequest request)

@GetMapping("/api/accounts/{accountId}/transfers")
public ResponseEntity<List<TransferHistoryResponse>> getTransferHistory(@PathVariable UUID accountId)
```

## Test Cases

### TC-TR-001: Transfer Internal - Happy Path
**Deskripsi**: Transfer antar rekening dalam bank yang sama dengan validasi lengkap

**Test Data**:
- Source Account: ACC0000001 (Balance: 1000000.00)
- Destination Account: ACC0000002 (Balance: 500000.00)
- Transfer Amount: 250000.00
- Description: "Transfer pembayaran tagihan"
- Reference Number: TRF20240815001
- Channel: TELLER
- Created By: teller1

**Steps**:
1. Login sebagai Teller (teller1)
2. Navigasi ke menu "Transfer Antar Rekening"
3. Input source account: ACC0000001
4. Verify source account details dan balance
5. Input destination account: ACC0000002
6. Verify destination account details
7. Input transfer amount: 250000.00
8. Input description: "Transfer pembayaran tagihan"
9. Input reference number: TRF20240815001
10. Review transfer summary
11. Confirm transfer

**Expected Result**:
- **Source Account (ACC0000001)**:
  - Balance before: 1000000.00
  - Balance after: 750000.00
  - Transaction record: TRANSFER_OUT, amount: 250000.00
- **Destination Account (ACC0000002)**:
  - Balance before: 500000.00
  - Balance after: 750000.00
  - Transaction record: TRANSFER_IN, amount: 250000.00
- **Transaction Linking**:
  - Both transactions share same transfer reference
  - Source transaction points to destination account
  - Destination transaction points to source account
- **Audit Trail**:
  - Two transaction records dengan transaction numbers berbeda
  - Same reference number untuk linking
  - Same timestamp untuk both transactions
  - Created by: teller1
- **Business Rules Validated**:
  - Source balance sufficient
  - Both accounts active
  - Amount positive
  - Transfer limits compliance

### TC-TR-002: Transfer dengan Multiple Currency Support
**Deskripsi**: Transfer antar rekening dengan currency validation

**Test Data**:
- Source Account: ACC0000003 (Currency: IDR)
- Destination Account: ACC0000004 (Currency: IDR)
- Transfer Amount: 500000.00 IDR
- Cross-currency scenario: USD to IDR (future)

**Steps**:
1. Login sebagai Teller
2. Initiate transfer between IDR accounts
3. Verify currency compatibility
4. Process transfer

**Expected Result**:
- Currency validation passed (both IDR)
- Transfer processed successfully
- Currency field consistent dalam transaction records
- Exchange rate handling (if applicable for future USD/IDR)
- Currency mismatch error handling (for different currencies)

### TC-TR-003: Transfer dengan Fee Calculation
**Deskripsi**: Transfer dengan perhitungan biaya transfer

**Test Data**:
- Source Account: ACC0000005 (Product: TABUNGAN_WADIAH)
- Destination Account: ACC0000006 (Different bank - future feature)
- Transfer Amount: 1000000.00
- Inter-bank Transfer Fee: 7500.00 (dari product configuration)

**Steps**:
1. Login sebagai Teller
2. Input transfer details
3. System calculate transfer fee based on product
4. Display fee to user
5. Confirm transfer including fee

**Expected Result**:
- Fee calculation: 7500.00 (inter_bank_transfer_fee dari products table)
- **Source Account Transactions**:
  - TRANSFER_OUT: 1000000.00 (transfer amount)
  - FEE: 7500.00 (transfer fee)
  - Total debit: 1007500.00
- **Destination Account Transaction**:
  - TRANSFER_IN: 1000000.00 (received amount)
- Fee transparency dalam receipt
- Fee recorded as separate transaction untuk audit

### TC-TR-004: Validation - Insufficient Balance
**Deskripsi**: Validasi transfer dengan saldo tidak mencukupi

**Test Data**:
- Source Account: ACC0000007 (Balance: 100000.00)
- Transfer Amount: 150000.00
- Transfer Fee: 5000.00
- Total Required: 155000.00

**Steps**:
1. Login sebagai Teller
2. Input transfer amount exceeding available balance
3. System calculate total cost (amount + fee)
4. Validate against source balance

**Expected Result**:
- Error message: "Insufficient balance. Available: Rp 100,000, Required: Rp 155,000 (including fee: Rp 5,000)"
- Transfer rejected before processing
- No transaction records created
- Clear breakdown of required amount
- No partial transfers processed

### TC-TR-005: Validation - Same Account Transfer
**Deskripsi**: Validasi transfer ke rekening yang sama

**Test Data**:
- Source Account: ACC0000008
- Destination Account: ACC0000008 (same account)

**Steps**:
1. Login sebagai Teller
2. Input source account: ACC0000008
3. Input destination account: ACC0000008 (same)
4. Attempt to process transfer

**Expected Result**:
- Error message: "Source and destination accounts cannot be the same"
- Form validation prevents submission
- No transaction processing
- Business rule enforcement
- User guidance untuk correct input

### TC-TR-006: Validation - Inactive Destination Account
**Deskripsi**: Validasi transfer ke rekening non-aktif

**Test Data**:
- Source Account: ACC0000009 (Status: ACTIVE)
- Destination Account: ACC0000010 (Status: INACTIVE)

**Steps**:
1. Login sebagai Teller
2. Input valid source account
3. Input inactive destination account
4. Attempt transfer

**Expected Result**:
- Error message: "Destination account is inactive and cannot receive transfers"
- Account status validation enforced
- Transfer rejected
- Status indicator displayed untuk both accounts
- Option untuk activate destination account (if authorized)

### TC-TR-007: Validation - Daily Transfer Limit
**Deskripsi**: Validasi transfer yang melebihi limit harian

**Test Data**:
- Source Account: ACC0000011
- Daily Transfer Limit: 5000000.00
- Previous Transfers Today: 4000000.00
- Current Transfer: 1500000.00 (total: 5500000.00)

**Steps**:
1. Login sebagai Teller
2. Input transfer amount that exceeds daily limit
3. System check daily transfer totals
4. Validate against limit

**Expected Result**:
- Error message: "Daily transfer limit exceeded. Limit: Rp 5,000,000, Used: Rp 4,000,000, Available: Rp 1,000,000"
- Transfer limit calculation accurate
- Daily tracking per account
- Remaining limit displayed
- Manager override option (if applicable)

### TC-TR-008: Transfer dengan Reference Number Tracking
**Deskripsi**: Transfer dengan reference number untuk tracking dan reconciliation

**Test Data**:
- Reference Number: TRF20240815002
- External Reference: PAYROLL-20240815
- Transfer Purpose: "Gaji karyawan"

**Steps**:
1. Login sebagai Teller
2. Input transfer details dengan reference
3. Process transfer
4. Verify reference tracking

**Expected Result**:
- Reference number tersimpan di both transactions
- External reference untuk business tracking
- Reference searchable dalam transaction history
- Transfer linking via reference number
- Audit trail untuk reconciliation

### TC-TR-009: Bulk Transfer Processing
**Deskripsi**: Pemrosesan multiple transfers dalam satu batch

**Test Data**:
- Source Account: ACC0000012 (payroll account)
- Multiple Destinations: 10 employee accounts
- Individual Amounts: Various amounts per employee
- Batch Reference: PAYROLL-BATCH-20240815

**Steps**:
1. Login sebagai Teller atau Manager
2. Upload atau input bulk transfer file
3. Validate all destination accounts
4. Verify total amount vs source balance
5. Process batch transfer

**Expected Result**:
- All transfers processed atomically (all succeed or all fail)
- Individual transaction records untuk each transfer
- Batch reference linking all transfers
- Source account debited once untuk total amount
- Each destination credited individually
- Batch summary report generated
- Rollback capability if any transfer fails

### TC-TR-010: Transfer History dan Inquiry
**Deskripsi**: Inquiry transfer history dengan filtering

**Test Data**:
- Account: ACC0000013
- Date Range: Last 30 days
- Transfer Direction: Both incoming dan outgoing
- Amount Range: 100,000 - 1,000,000

**Steps**:
1. Login sebagai Teller atau Customer
2. Access transfer history page
3. Apply filters: date range, amount range, direction
4. Export transfer history

**Expected Result**:
- Transfer history displayed dengan proper formatting
- Direction clearly indicated (IN/OUT)
- Counterpart account information shown
- Reference numbers dan descriptions included
- Filtering works accurately
- Export functionality available (CSV/PDF)
- Pagination untuk large datasets

### TC-TR-011: Transfer Reversal/Cancellation
**Deskripsi**: Pembatalan atau pembalikan transfer

**Test Data**:
- Original Transfer: TRF20240815003
- Transfer Amount: 300000.00
- Reversal Reason: "Transfer error - wrong destination"
- Time Limit: Within 24 hours

**Steps**:
1. Login sebagai Manager (reversal requires authorization)
2. Search original transfer
3. Initiate transfer reversal
4. Input reversal reason
5. Confirm reversal

**Expected Result**:
- Original transfer marked as REVERSED
- Reversal transactions created:
  - Source account: TRANSFER_IN (credit back)
  - Destination account: TRANSFER_OUT (debit reversal)
- Audit trail maintains link to original transfer
- Reversal reason recorded
- Manager authorization logged
- Time limit enforced untuk reversals
- Reversal cannot be reversed (one-time only)

### TC-TR-012: Security Test - Unauthorized Transfer Access
**Deskripsi**: Validasi akses transfer berdasarkan role permissions

**Test Data**:
- User 1: Customer (should only see own accounts)
- User 2: CS (limited transfer permissions)
- User 3: Teller (full transfer permissions)

**Steps**:
1. Login dengan different user roles
2. Attempt various transfer operations
3. Verify permission enforcement

**Expected Result**:
- **Customer**: Can only transfer between own accounts
- **CS**: Limited transfer permissions (may require approval)
- **Teller**: Full transfer permissions within limits
- **Manager**: All transfer permissions including reversals
- API endpoints protected dengan proper authorization
- Role-based UI restrictions enforced

### TC-TR-013: Transfer dengan Islamic Banking Compliance
**Deskripsi**: Validasi compliance Islamic banking untuk transfers

**Test Data**:
- Islamic Banking Accounts: TABUNGAN_MUDHARABAH
- Transfer Purpose: Halal business transaction
- Shariah Compliance Check

**Steps**:
1. Initiate transfer between Islamic accounts
2. Verify Shariah compliance
3. Check profit sharing implications
4. Validate Islamic banking rules

**Expected Result**:
- Transfer complies dengan Islamic banking principles
- No interest-based calculations
- Shariah-compliant transaction processing
- Proper documentation untuk Islamic compliance
- Audit trail untuk regulatory reporting

## Performance Test Cases

### TC-TR-P001: High Volume Transfer Processing
**Deskripsi**: Test performa dengan volume transfer tinggi

**Test Scenario**:
- 1000 concurrent transfers
- Various amounts dan account pairs
- Stress test database locking
- Monitor transaction performance

**Expected Result**:
- Response time < 3 detik per transfer
- Success rate > 99%
- No deadlocks or race conditions
- Accurate balance calculations under load
- Database performance maintained

### TC-TR-P002: Bulk Transfer Performance
**Deskripsi**: Test performa bulk transfer processing

**Test Scenario**:
- Bulk transfer dengan 500 destinations
- Large payroll processing simulation
- Memory usage monitoring

**Expected Result**:
- Bulk processing time < 2 minutes
- Memory usage reasonable
- Atomic transaction handling
- Progress tracking available
- Error recovery mechanisms

## Database Schema Requirements

### Expected Database Changes
```sql
-- Transfer tracking table (optional untuk advanced features)
CREATE TABLE transfer_batches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_reference VARCHAR(100) UNIQUE NOT NULL,
    total_amount DECIMAL(20,2) NOT NULL,
    transfer_count INTEGER NOT NULL,
    status VARCHAR(20) DEFAULT 'COMPLETED',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

-- Daily transfer limits tracking
CREATE TABLE daily_transfer_limits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    transfer_date DATE NOT NULL,
    total_amount DECIMAL(20,2) DEFAULT 0,
    transaction_count INTEGER DEFAULT 0,
    CONSTRAINT fk_daily_limits_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT uk_daily_limits UNIQUE (account_id, transfer_date)
);

-- Transfer fee configuration per product
ALTER TABLE products ADD COLUMN transfer_fee DECIMAL(15,2) DEFAULT 0.00;
ALTER TABLE products ADD COLUMN daily_transfer_limit DECIMAL(20,2);
ALTER TABLE products ADD COLUMN monthly_transfer_limit INTEGER;
```

## API Test Examples

### REST API Calls
```bash
# Process Internal Transfer
curl -X POST http://localhost:8080/api/transactions/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "sourceAccountId": "uuid-source-account",
    "destinationAccountId": "uuid-destination-account",
    "amount": 250000.00,
    "description": "Transfer pembayaran tagihan",
    "referenceNumber": "TRF20240815001",
    "createdBy": "teller1"
  }'

# Get Transfer History
curl -X GET "http://localhost:8080/api/accounts/{accountId}/transfers?startDate=2024-08-01&endDate=2024-08-31" \
  -H "Authorization: Bearer <token>"

# Check Daily Transfer Limits
curl -X GET "http://localhost:8080/api/accounts/{accountId}/limits/transfer/daily" \
  -H "Authorization: Bearer <token>"

# Process Bulk Transfer
curl -X POST http://localhost:8080/api/transactions/transfer/bulk \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "sourceAccountId": "uuid-source-account",
    "transfers": [
      {"destinationAccountId": "uuid-dest-1", "amount": 100000.00, "description": "Salary Employee 1"},
      {"destinationAccountId": "uuid-dest-2", "amount": 150000.00, "description": "Salary Employee 2"}
    ],
    "batchReference": "PAYROLL-20240815",
    "createdBy": "manager1"
  }'

# Transfer Reversal
curl -X POST http://localhost:8080/api/transactions/transfer/{transferId}/reverse \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "reason": "Transfer error - wrong destination",
    "authorizedBy": "manager1"
  }'
```

## Error Handling Test Cases

### TC-TR-E001: Network Failure During Transfer
**Deskripsi**: Test handling network failure during transfer processing

**Steps**:
1. Initiate transfer
2. Simulate network failure after debit but before credit
3. Test recovery mechanisms

**Expected Result**:
- Transaction rollback atau compensation
- No partial transfers
- Data integrity maintained
- Error recovery process documented

### TC-TR-E002: Concurrent Transfer Conflicts
**Deskripsi**: Test handling concurrent transfers affecting same accounts

**Steps**:
1. Initiate multiple transfers involving same accounts
2. Test database locking mechanisms
3. Verify balance consistency

**Expected Result**:
- Proper locking prevents conflicts
- Balance calculations accurate
- No race conditions
- Transaction ordering maintained

## Integration Test Cases

### TC-TR-I001: End-to-End Transfer Workflow
**Deskripsi**: Complete transfer workflow dari initiation sampai completion

**Steps**:
1. Customer request transfer
2. Teller input transfer details
3. System validation dan processing
4. Receipt generation
5. Audit logging

**Expected Result**:
- Complete workflow functioning
- All validation points working
- Proper error handling
- Receipt accuracy
- Audit compliance

### TC-TR-I002: Transfer Integration dengan Reporting
**Deskripsi**: Transfer integration dengan reporting systems

**Steps**:
1. Process various transfers
2. Generate transfer reports
3. Verify data accuracy dalam reports
4. Test export functionality

**Expected Result**:
- Transfer data accurately reflected dalam reports
- Real-time reporting capabilities
- Export functionality working
- Report formatting correct

## Business Rules Validation

### Transfer Business Rules:
1. **Source Account Validation**:
   - Must be ACTIVE status
   - Sufficient balance (amount + fees)
   - Daily/monthly limits compliance
   - Account ownership verification

2. **Destination Account Validation**:
   - Must exist dalam system
   - Must be ACTIVE status
   - Can receive transfers (not blocked)
   - Currency compatibility

3. **Amount Validation**:
   - Must be positive
   - Within transfer limits
   - Precision validation (DECIMAL 20,2)
   - Fee calculation accuracy

4. **Transfer Processing**:
   - Atomic transactions (both succeed or both fail)
   - Proper transaction linking
   - Audit trail completeness
   - Reference number uniqueness

5. **Islamic Banking Compliance**:
   - Shariah-compliant transfer purposes
   - No interest-based calculations
   - Proper documentation
   - Regulatory compliance

## Cleanup Scripts

### Test Data Cleanup
```sql
-- Cleanup test transfer transactions
DELETE FROM transactions 
WHERE transaction_type IN ('TRANSFER_IN', 'TRANSFER_OUT')
AND (description LIKE '%test%' 
OR description LIKE '%Test%'
OR reference_number LIKE 'TEST%');

-- Cleanup transfer batches
DELETE FROM transfer_batches 
WHERE batch_reference LIKE 'TEST%';

-- Reset daily transfer limits
DELETE FROM daily_transfer_limits 
WHERE account_id IN (
  SELECT id FROM accounts WHERE account_number LIKE 'TEST%'
);

-- Reset account balances
UPDATE accounts 
SET balance = (
  SELECT COALESCE(SUM(CASE 
    WHEN t.transaction_type IN ('DEPOSIT', 'TRANSFER_IN') THEN t.amount 
    WHEN t.transaction_type IN ('WITHDRAWAL', 'TRANSFER_OUT', 'FEE') THEN -t.amount 
    ELSE 0 END), 0)
  FROM transactions t 
  WHERE t.id_accounts = accounts.id
)
WHERE account_number LIKE 'TEST%';
```

## Future Enhancement Considerations

### Advanced Transfer Features:
1. **Scheduled Transfers**: Recurring transfers dengan cron scheduling
2. **International Transfers**: Cross-border transfer dengan SWIFT integration
3. **Real-time Notifications**: SMS/email notifications untuk transfers
4. **Mobile Transfer**: Integration dengan mobile banking
5. **QR Code Transfers**: QR-based transfer initiation
6. **Transfer Templates**: Saved transfer templates untuk frequent recipients
7. **Transfer Approvals**: Multi-level approval workflows untuk large amounts
8. **Exchange Rate Integration**: Real-time exchange rates untuk currency conversion

### Regulatory Compliance:
1. **AML (Anti-Money Laundering)**: Transaction monitoring dan reporting
2. **KYC Integration**: Enhanced customer verification untuk large transfers
3. **Regulatory Reporting**: Automated reporting untuk central bank
4. **Audit Trail**: Enhanced audit capabilities untuk compliance

This comprehensive test scenario covers all aspects of transfer functionality that would need to be implemented and tested untuk complete banking system.