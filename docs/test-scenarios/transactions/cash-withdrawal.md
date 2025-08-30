# Test Scenarios: Penarikan Tunai

## Overview
Dokumen ini berisi skenario test untuk fitur penarikan tunai dalam aplikasi minibank Islam. Penarikan tunai mencakup validasi saldo, limit checking, pencatatan transaksi, dan update saldo menggunakan entity business methods.

## Preconditions
- Database PostgreSQL berjalan
- Aplikasi Spring Boot aktif
- User sudah login sebagai Teller atau Customer Service
- Account nasabah sudah ada dan dalam status ACTIVE dengan saldo mencukupi
- Sequence number service berfungsi normal untuk transaction_number

## Existing Implementation Reference

### REST API Controller (TransactionRestController)
```java
@PostMapping("/withdrawal")
@Transactional
public ResponseEntity<Object> withdrawal(@Valid @RequestBody WithdrawalRequest request, BindingResult bindingResult)

// Business Method Integration:
account.withdraw(request.getAmount()); // Entity business method
```

### ✅ **NEW: Web Interface Controller (TransactionController)**
```java
@PostMapping("/cash-withdrawal")
public String processCashWithdrawal(@Valid @ModelAttribute WithdrawalRequest withdrawalRequest,
                                   BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes)

// Navigation endpoints:
@GetMapping("/cash-withdrawal") // Account selection
@GetMapping("/cash-withdrawal/{accountId}") // Withdrawal form

// Business Logic Integration:
account.withdraw(withdrawalRequest.getAmount()); // Entity business method
```

### ✅ **NEW: Web UI Templates**
- `templates/transaction/select-account.html` - Account selection with withdrawal support
- `templates/transaction/cash-withdrawal-form.html` - Withdrawal form with real-time validation
- `templates/transaction/list.html` - Transaction list with withdrawal button and display

### CSV Test Data Reference
```csv
# src/test/resources/fixtures/transaction/withdrawal-normal.csv
# src/test/resources/fixtures/transaction/withdrawal-validation.csv
```

## Test Cases

### TC-CW-001: Penarikan Tunai Normal - TABUNGAN_WADIAH
**Deskripsi**: Melakukan penarikan tunai dari rekening TABUNGAN_WADIAH dengan saldo mencukupi

**Test Data**:
- Account Number: ACC0000001 (TABUNGAN_WADIAH)
- Current Balance: 1000000.00 (DECIMAL 20,2)
- Withdrawal Amount: 250000.00 (must be positive)
- Transaction Type: WITHDRAWAL
- Transaction Channel: TELLER
- Created By: teller1 (max 100 chars)

**Steps**:
1. Login sebagai Teller (teller1)
2. **✅ NEW: Navigate via Web UI:** Dashboard → Transaction List → "- Penarikan Tunai" button
3. **✅ NEW: Account Selection Page:** Search and select account ACC0000001
4. **✅ NEW: Withdrawal Form Page:** System displays customer data, account info, and current balance
5. **✅ NEW: Real-time Validation:** Input withdrawal amount: 250000.00 (with live balance calculation)
6. Input description: "Penarikan tunai harian"
7. Input reference number (optional)
8. Input processed by: "teller1"
9. **✅ NEW: Client-side Validation:** System validates sufficient balance before submit
10. Click "Proses Penarikan Tunai" button

**Expected Result**:
- Account.withdraw(amount) method dipanggil
- Validation: amount > 0 ✓
- Validation: balance >= amount ✓
- **✅ NEW: Client-side Validation:** Real-time balance warning system functioning
- **✅ NEW: JavaScript Validation:** New balance calculated and displayed correctly
- Balance sebelum: 1000000.00
- Balance sesudah: 750000.00
- Transaction record tersimpan dengan:
  - transaction_number: auto-generated (VARCHAR 50, UNIQUE)
  - transaction_type: WITHDRAWAL
  - amount: 250000.00
  - currency: IDR
  - balance_before: 1000000.00
  - balance_after: 750000.00
  - channel: TELLER
  - description: "Penarikan tunai harian"
  - transaction_date: current timestamp
  - processed_date: current timestamp
  - created_by: teller1
- Constraint validation: amount > 0 ✓
- Constraint validation: balance_after = balance_before - amount ✓
- **✅ NEW: Web UI Success:** Redirect to transaction list with success message
- **✅ NEW: Transaction Display:** Transaction appears in list with negative amount (-IDR 250,000.00)
- **✅ NEW: Transaction Detail:** View transaction detail shows withdrawal information correctly

### TC-CW-002: Penarikan Tunai dengan Limit Harian - TABUNGAN_MUDHARABAH
**Deskripsi**: Melakukan penarikan tunai dengan validasi daily withdrawal limit

**Test Data**:
- Account Number: ACC0000002 (TABUNGAN_MUDHARABAH)
- Current Balance: 5000000.00
- Product Daily Withdrawal Limit: 2000000.00
- Withdrawal Amount: 1500000.00 (within limit)
- Previous Withdrawals Today: 0.00

**Steps**:
1. Login sebagai Teller (teller2)
2. Navigasi ke menu "Penarikan Tunai"
3. Input account number: ACC0000002
4. Sistem check daily withdrawal limit dari product configuration
5. Input withdrawal amount: 1500000.00
6. Input description: "Penarikan untuk kebutuhan usaha"
7. Konfirmasi transaksi

**Expected Result**:
- Daily limit validation passed (1500000 < 2000000)
- Transaction berhasil diproses
- Balance updated: 5000000.00 - 1500000.00 = 3500000.00
- Daily withdrawal counter updated untuk account
- Transaction record tersimpan dengan benar
- Remaining daily limit: 500000.00
- Islamic banking compliance maintained

### TC-CW-003: Penarikan Tunai dengan Reference Number
**Deskripsi**: Penarikan tunai dengan reference number untuk tracking

**Test Data**:
- Account Number: ACC0000003
- Withdrawal Amount: 500000.00
- Reference Number: WD20240815001 (max 100 chars)
- Description: "Penarikan untuk bayar supplier"

**Steps**:
1. Login sebagai Teller
2. Navigasi ke menu "Penarikan Tunai"
3. Input account number: ACC0000003
4. Input withdrawal amount: 500000.00
5. Input reference number: WD20240815001
6. Input description: "Penarikan untuk bayar supplier"
7. Konfirmasi dan proses penarikan

**Expected Result**:
- Transaction amount: 500000.00
- Reference number tersimpan: WD20240815001
- Description tersimpan dengan detail lengkap
- Balance account terupdate dengan benar
- Reference number dapat digunakan untuk search/lookup
- Audit trail lengkap dengan reference

### TC-CW-004: Validation - Account Not Found
**Deskripsi**: Validasi penarikan dari account yang tidak exist

**Test Data**:
- Account Number: ACC9999999 (tidak exist)

**Steps**:
1. Login sebagai Teller
2. Navigasi ke menu "Penarikan Tunai"
3. Input account number: ACC9999999
4. Tekan Enter atau klik "Cari"

**Expected Result**:
- Error message: "Account not found"
- Form tidak dapat dilanjutkan
- Tidak ada transaction yang tercatat
- Focus kembali ke account number field
- API response: 400 Bad Request dengan error detail

### TC-CW-005: Validation - Account Status INACTIVE
**Deskripsi**: Validasi penarikan dari account dengan status non-aktif

**Test Data**:
- Account Number: ACC0000004
- Account Status: INACTIVE

**Steps**:
1. Login sebagai Teller
2. Navigasi ke menu "Penarikan Tunai"  
3. Input account number: ACC0000004
4. Sistem menampilkan account status: INACTIVE
5. Coba input withdrawal amount: 100000.00

**Expected Result**:
- Warning message: "Account is not active, transaction cannot be processed"
- Submit button disabled
- Tidak ada transaction yang tercatat
- Account status indicator clearly displayed
- Option untuk activate account (if user has permission)

### TC-CW-006: Validation - Insufficient Balance
**Deskripsi**: Validasi penarikan dengan saldo tidak mencukupi

**Test Data**:
- Account Number: ACC0000005
- Current Balance: 100000.00
- Withdrawal Amount: 150000.00 (exceeds balance)

**Steps**:
1. Login sebagai Teller
2. Navigasi ke menu "Penarikan Tunai"
3. Input account number: ACC0000005
4. Sistem menampilkan balance: 100000.00
5. Input withdrawal amount: 150000.00
6. Coba submit form

**Expected Result**:
- Error message: "Insufficient balance. Available: Rp 100,000, Requested: Rp 150,000"
- Business logic validation: account.withdraw() throws IllegalArgumentException
- Form tidak dapat disubmit
- Balance information prominently displayed
- Tidak ada transaction yang tercatat
- Suggested maximum withdrawal amount shown

### TC-CW-007: Validation - Daily Withdrawal Limit Exceeded
**Deskripsi**: Validasi penarikan yang melebihi limit harian

**Test Data**:
- Account Number: ACC0000006
- Daily Withdrawal Limit: 1000000.00
- Previous Withdrawals Today: 800000.00
- Current Withdrawal Request: 300000.00 (total would be 1100000.00)

**Steps**:
1. Login sebagai Teller
2. Input account number: ACC0000006
3. Check daily withdrawal history
4. Input withdrawal amount: 300000.00
5. Submit form

**Expected Result**:
- Error message: "Daily withdrawal limit exceeded. Limit: Rp 1,000,000, Used: Rp 800,000, Available: Rp 200,000"
- Transaction rejected by business rules
- Daily limit calculation accurate
- Transaction tidak tersimpan
- Remaining limit clearly displayed
- Override option untuk manager approval (if applicable)

### TC-CW-008: Validation - Minimum Balance Requirement
**Deskripsi**: Validasi penarikan yang akan menyebabkan saldo di bawah minimum

**Test Data**:
- Account Number: ACC0000007
- Current Balance: 100000.00
- Product Minimum Balance: 50000.00
- Withdrawal Amount: 75000.00 (would leave 25000.00, below minimum)

**Steps**:
1. Login sebagai Teller
2. Input account number: ACC0000007
3. Input withdrawal amount: 75000.00
4. Sistem check minimum balance requirement
5. Submit form

**Expected Result**:
- Error message: "Withdrawal would result in balance below minimum. Current: Rp 100,000, Minimum Required: Rp 50,000, Maximum Withdrawal: Rp 50,000"
- Product minimum balance rule enforced
- Transaction rejected
- Clear explanation of balance constraints
- Suggested maximum withdrawal shown

### TC-CW-009: Validation - Negative or Zero Amount
**Deskripsi**: Validasi input withdrawal amount tidak valid

**Test Data**:
- Withdrawal Amount: -50000.00 (negative)
- Withdrawal Amount: 0.00 (zero)

**Steps**:
1. Login sebagai Teller
2. Navigasi ke menu "Penarikan Tunai"
3. Input valid account number: ACC0000001
4. Input withdrawal amount: -50000.00
5. Coba submit form
6. Ulangi dengan amount: 0.00

**Expected Result**:
- Error message: "Withdrawal amount must be positive"
- Bean validation enforced (@DecimalMin)
- Form validation prevents submit
- Field highlighted in red
- Focus returns to amount field
- Database constraint chk_amount_positive prevents negative values

### TC-CW-010: Multiple Withdrawals Same Day
**Deskripsi**: Melakukan beberapa penarikan tunai dalam satu hari dengan limit tracking

**Test Data**:
- Account Number: ACC0000001
- Daily Limit: 5000000.00
- Withdrawal 1: 1000000.00 (08:00)
- Withdrawal 2: 1500000.00 (11:00)
- Withdrawal 3: 2000000.00 (15:00) - total 4500000.00

**Steps**:
1. Login sebagai Teller
2. Proses withdrawal pertama: 1000000.00
3. Proses withdrawal kedua: 1500000.00
4. Proses withdrawal ketiga: 2000000.00

**Expected Result**:
- Semua transactions berhasil diproses (total within daily limit)
- Daily withdrawal counter accurate per transaction
- Balance terakumulasi dengan benar (decremented)
- Setiap transaction punya transaction_number unik
- Transaction history tercatat lengkap dengan timestamp
- Remaining daily limit: 500000.00 after all transactions

### TC-CW-011: ATM vs TELLER Channel Differences
**Deskripsi**: Test withdrawal dengan berbagai transaction channels

**Test Data**:
- Channel: TELLER (default)
- Channel: ATM (different limits/fees)
- Account with different fee structures per channel

**Steps**:
1. Process withdrawals dengan channel berbeda
2. Verify channel-specific business rules
3. Check fee calculations per channel

**Expected Result**:
- Transaction channel tersimpan sesuai input
- Enum validation: channel IN ('TELLER', 'ATM', 'ONLINE', 'MOBILE', 'TRANSFER')
- Different fee structures applied per channel
- ATM withdrawals may have different limits
- Channel-specific audit requirements met

### TC-CW-012: Withdrawal with Fee Calculation
**Deskripsi**: Penarikan tunai dengan perhitungan fee sesuai product configuration

**Test Data**:
- Account: TABUNGAN_WADIAH (ATM withdrawal fee: 5000.00)
- Withdrawal Amount: 200000.00
- Channel: ATM
- Free Transactions This Month: 8 (limit: 10)

**Steps**:
1. Login sebagai Teller
2. Process ATM withdrawal
3. Verify fee calculation
4. Check free transaction counter

**Expected Result**:
- No fee charged (within free transaction limit)
- Free transaction counter incremented: 9/10
- Total amount debited: 200000.00 (no fee)
- Fee calculation logic documented in transaction
- Monthly fee tracking accurate

### TC-CW-013: Withdrawal Exceeding Free Transaction Limit
**Deskripsi**: Penarikan yang melebihi transaksi gratis bulanan

**Test Data**:
- Account: TABUNGAN_WADIAH (free: 10/month, excess fee: 2500.00)
- Free Transactions Used: 10/10
- Withdrawal Amount: 300000.00
- Channel: ATM

**Steps**:
1. Login sebagai Teller
2. Process withdrawal setelah free limit exceeded
3. Verify fee calculation and deduction

**Expected Result**:
- Fee charged: 2500.00 (excess transaction fee)
- Total amount debited: 302500.00 (withdrawal + fee)
- Two transaction records:
  - WITHDRAWAL: 300000.00
  - FEE: 2500.00
- Balance calculation accurate including fee
- Fee transparency dalam receipt

### TC-CW-014: Security Test - Unauthorized Access
**Deskripsi**: Validasi akses withdrawal oleh user tidak berwenang

**Test Data**:
- User: customer biasa
- User: admin (non-teller/non-CS)

**Steps**:
1. Login sebagai customer
2. Coba akses menu penarikan tunai
3. Ulangi dengan user admin non-teller

**Expected Result**:
- Customer: tidak dapat akses menu penarikan tunai
- Admin non-teller: error 403 Forbidden
- API endpoint menolak request dengan proper authorization
- Role-based access control enforced
- RBAC permissions validated: TRANSACTION_WITHDRAWAL required

### TC-CW-015: Concurrent Withdrawals Same Account
**Deskripsi**: Test race condition dengan withdrawals bersamaan dari account sama

**Test Data**:
- Account Number: ACC0000001
- Current Balance: 1000000.00
- Teller 1: withdrawal 300000.00
- Teller 2: withdrawal 400000.00 (bersamaan)

**Steps**:
1. Login sebagai Teller1 di terminal 1
2. Login sebagai Teller2 di terminal 2
3. Kedua teller input account sama: ACC0000001
4. Teller1 input amount: 300000.00
5. Teller2 input amount: 400000.00
6. Submit bersamaan dalam waktu < 1 detik

**Expected Result**:
- Both transactions processed successfully (database locking handles concurrency)
- Final balance = 1000000 - 300000 - 400000 = 300000.00
- Kedua transactions tercatat dengan transaction_number berbeda
- Timestamp berbeda meskipun hampir bersamaan
- Tidak ada data corruption atau lost updates
- Optimistic locking prevents balance inconsistency

### TC-CW-016: Large Amount Withdrawal with Approval
**Deskripsi**: Penarikan nominal besar yang memerlukan approval manager

**Test Data**:
- Account Number: ACC0000008
- Withdrawal Amount: 25000000.00 (large amount)
- Approval Threshold: 10000000.00
- Manager: manager1

**Steps**:
1. Login sebagai Teller
2. Input large withdrawal amount
3. System triggers approval workflow
4. Manager approval required
5. Complete approval process

**Expected Result**:
- Transaction flagged for approval (amount > threshold)
- Approval workflow initiated
- Transaction status: PENDING_APPROVAL
- Manager notification sent
- Transaction processed only after approval
- Approval audit trail maintained

## Performance Test Cases

### TC-CW-P001: Load Test Withdrawal Transactions
**Deskripsi**: Test performa withdrawal dengan beban tinggi

**Test Scenario**:
- 50 concurrent tellers
- Masing-masing proses 15 withdrawals/menit
- Durasi test: 10 menit
- Total: 7,500 transactions

**Expected Result**:
- Response time < 2 detik per transaction
- Success rate > 99.9%
- Tidak ada deadlock di database
- Transaction number sequence tidak duplikat
- Daily limit calculations accurate under load
- Memory usage stabil

### TC-CW-P002: Daily Limit Calculation Performance
**Deskripsi**: Test performa perhitungan daily withdrawal limits

**Test Scenario**:
- Account dengan 100+ withdrawals per day
- Multiple concurrent limit checks
- Complex fee calculations

**Expected Result**:
- Daily limit calculation < 500ms
- Accurate limit tracking under concurrent load
- No limit calculation errors
- Efficient database queries untuk daily totals

## Database Validation

### Post-Transaction Validation
```sql
-- Check balance consistency after withdrawals
SELECT a.account_number, a.balance, 
       COALESCE(SUM(CASE 
         WHEN t.transaction_type = 'DEPOSIT' THEN t.amount 
         WHEN t.transaction_type = 'WITHDRAWAL' THEN -t.amount 
         ELSE 0 END), 0) as calculated_balance
FROM accounts a
LEFT JOIN transactions t ON a.id = t.id_accounts
WHERE a.account_number = 'ACC0000001'
GROUP BY a.id, a.account_number, a.balance;

-- Check withdrawal constraints
SELECT * FROM transactions 
WHERE transaction_type = 'WITHDRAWAL'
AND (amount <= 0  -- Should be empty (violates chk_amount_positive)
OR balance_after != balance_before - amount);  -- Should be empty for WITHDRAWAL

-- Daily withdrawal limit validation
SELECT 
  a.account_number,
  DATE(t.transaction_date) as transaction_date,
  SUM(t.amount) as daily_total,
  p.daily_withdrawal_limit
FROM transactions t
JOIN accounts a ON t.id_accounts = a.id
JOIN products p ON a.id_products = p.id
WHERE t.transaction_type = 'WITHDRAWAL'
AND DATE(t.transaction_date) = CURRENT_DATE
GROUP BY a.account_number, DATE(t.transaction_date), p.daily_withdrawal_limit
HAVING SUM(t.amount) > p.daily_withdrawal_limit;

-- Minimum balance compliance check
SELECT 
  a.account_number,
  a.balance,
  p.minimum_balance,
  CASE WHEN a.balance < p.minimum_balance THEN 'VIOLATION' ELSE 'OK' END as status
FROM accounts a
JOIN products p ON a.id_products = p.id
WHERE a.balance < p.minimum_balance;
```

## API Test Examples

### REST API Calls
```bash
# Process Cash Withdrawal
curl -X POST http://localhost:8080/api/transactions/withdrawal \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "accountId": "uuid-account-id",
    "amount": 250000.00,
    "description": "Penarikan tunai harian",
    "referenceNumber": "WD20240815001",
    "createdBy": "teller1"
  }'

# Check Account Balance Before Withdrawal
curl -X GET http://localhost:8080/api/accounts/{accountId}/balance \
  -H "Authorization: Bearer <token>"

# Get Daily Withdrawal Summary
curl -X GET "http://localhost:8080/api/accounts/{accountId}/withdrawals/daily?date=2024-08-15" \
  -H "Authorization: Bearer <token>"

# Get Transaction History with Type Filter
curl -X GET "http://localhost:8080/api/accounts/{accountId}/transactions?type=WITHDRAWAL&limit=10" \
  -H "Authorization: Bearer <token>"

# Check Daily Withdrawal Limits
curl -X GET "http://localhost:8080/api/accounts/{accountId}/limits/daily" \
  -H "Authorization: Bearer <token>"
```

## Error Handling Test Cases

### TC-CW-E001: Database Connection Lost During Transaction
**Deskripsi**: Test handling ketika koneksi database terputus

**Steps**:
1. Mulai proses withdrawal
2. Matikan database connection saat submit
3. Restart database
4. Retry transaction

**Expected Result**:
- Graceful error handling dengan informative message
- Transaction tidak double processed (idempotency)
- Complete rollback jika transaction gagal
- Connection retry mechanism berfungsi
- Data integrity maintained (no partial withdrawals)

### TC-CW-E002: Concurrent Balance Updates
**Deskripsi**: Test handling concurrent balance modifications

**Steps**:
1. Process withdrawal dan deposit simultaneously
2. Verify balance calculation accuracy
3. Check transaction ordering

**Expected Result**:
- Pessimistic locking prevents race conditions
- Balance calculations accurate
- Transaction sequence maintained
- No lost updates

## Integration Test Cases

### TC-CW-I001: Withdrawal Integration with Fee Processing
**Deskripsi**: End-to-end test withdrawal dengan fee calculations

**Steps**:
1. Process withdrawal yang incurs fees
2. Verify fee calculation accuracy
3. Check multiple transaction records
4. Validate balance impact

**Expected Result**:
- Withdrawal dan fee recorded as separate transactions
- Balance reflects both withdrawal dan fee deduction
- Fee calculation follows product configuration
- Audit trail complete untuk both transactions

### TC-CW-I002: Integration dengan Islamic Banking Rules
**Deskripsi**: Validate Islamic banking compliance dalam withdrawals

**Steps**:
1. Process withdrawals dari Islamic banking products
2. Verify no interest-based calculations
3. Check profit sharing implications
4. Validate Shariah compliance

**Expected Result**:
- No interest charges applied
- Profit sharing calculations accurate
- Islamic banking principles maintained
- Shariah-compliant transaction processing

## Field Validation Reference

### WithdrawalRequest DTO Fields:
- `accountId` (UUID, NOT NULL)
- `amount` (DECIMAL 20,2, > 0)
- `description` (TEXT)
- `referenceNumber` (VARCHAR 100)
- `createdBy` (VARCHAR 100, NOT NULL)

### Transaction Entity Fields untuk Withdrawals:
- `transaction_type` = WITHDRAWAL
- `amount` (positive, validated by business logic)
- `balance_before`/`balance_after` (accurate calculation)
- `channel` (TELLER, ATM, ONLINE, MOBILE)

### Business Rules Validation:
- Amount must be positive
- Account must be ACTIVE
- Balance must be sufficient (balance >= amount)
- Daily withdrawal limit compliance
- Minimum balance after withdrawal
- Fee calculations per product configuration

## Cleanup Scripts

### Test Data Cleanup
```sql
-- Cleanup test withdrawal transactions
DELETE FROM transactions 
WHERE transaction_type = 'WITHDRAWAL'
AND (description LIKE '%test%' 
OR description LIKE '%Test%'
OR reference_number LIKE 'TEST%');

-- Reset account balances untuk test accounts
UPDATE accounts 
SET balance = (
  SELECT COALESCE(SUM(CASE 
    WHEN t.transaction_type = 'DEPOSIT' THEN t.amount 
    WHEN t.transaction_type = 'WITHDRAWAL' THEN -t.amount 
    ELSE 0 END), 0)
  FROM transactions t 
  WHERE t.id_accounts = accounts.id
)
WHERE account_number LIKE 'TEST%';

-- Reset daily withdrawal counters (if implemented)
DELETE FROM daily_withdrawal_tracking 
WHERE account_id IN (
  SELECT id FROM accounts WHERE account_number LIKE 'TEST%'
);

-- Reset transaction sequence if needed
UPDATE sequence_numbers 
SET last_number = (
  SELECT COALESCE(MAX(CAST(SUBSTRING(transaction_number FROM '[0-9]+') AS INTEGER)), 0)
  FROM transactions
) 
WHERE sequence_name = 'TRANSACTION_NUMBER';
```

## CSV Test Data Integration

### Using Existing CSV Fixtures
```java
@ParameterizedTest
@CsvFileSource(resources = "/fixtures/transaction/withdrawal-normal.csv", numLinesToSkip = 1)
void shouldProcessWithdrawalSuccessfully(String accountId, BigDecimal amount, String description, String expectedResult) {
    // Test implementation using CSV data
}

@ParameterizedTest
@CsvFileSource(resources = "/fixtures/transaction/withdrawal-validation.csv", numLinesToSkip = 1)
void shouldValidateWithdrawalErrors(String accountId, BigDecimal amount, String expectedError) {
    // Test validation errors using CSV data
}
```

### Test Data Samples
- **Normal Withdrawals**: Various amounts, different accounts, multiple channels
- **Validation Errors**: Insufficient balance, inactive accounts, limit exceeded
- **Fee Scenarios**: Excess transaction fees, channel-specific charges
- **Daily Limits**: Multiple withdrawals testing daily limits
- **Edge Cases**: Minimum balance constraints, large amounts

## ✅ **NEW: Comprehensive Playwright Testing**

### Web UI Automation Tests (CashWithdrawalPlaywrightTest)

The cash withdrawal functionality now includes comprehensive Playwright tests following technical practices and lessons learned:

#### Test Coverage (15+ Test Methods):
1. **Navigation Workflow Testing**
   - `shouldNavigateThroughCashWithdrawalWorkflow()` - Full UI navigation flow
   - Dashboard → Transaction List → Withdrawal Button → Account Selection → Withdrawal Form

2. **Successful Processing Tests**
   - `shouldProcessCashWithdrawalSuccessfully()` - Complete withdrawal processing
   - Real-time balance validation, transaction creation, success message verification

3. **Client-side Validation Tests**
   - `shouldRejectWithdrawalInsufficientBalance()` - JavaScript balance warnings
   - `shouldCalculateNewBalanceCorrectly()` - Real-time balance calculations
   - Button state management (disabled when insufficient balance)

4. **Form Validation Tests**
   - `shouldValidateRequiredFields()` - Required field validation (amount, created by)
   - `shouldRejectNegativeAmount()` - Negative amount validation
   - `shouldRejectZeroAmount()` - Zero amount validation

5. **Account Management Tests**
   - `shouldHandleAccountSearch()` - Account search and filtering functionality
   - `shouldHandleFormCancellation()` - Form cancellation and navigation

6. **Transaction Verification Tests**
   - `shouldViewTransactionDetails()` - Transaction detail view validation
   - `shouldFilterTransactionsByWithdrawalType()` - Transaction filtering by WITHDRAWAL type

7. **Multi-role Permission Tests**
   - `shouldWorkWithManagerRole()` - Manager role withdrawal processing
   - Role-based access control validation

8. **Edge Case Testing**
   - `shouldHandleMinimumBalanceEdgeCase()` - Minimum balance scenarios
   - Withdrawal leaving exactly minimum balance

9. **Parameterized Testing**
   - `shouldProcessValidCashWithdrawals()` - CSV-driven test scenarios
   - Multiple withdrawal amounts based on percentage of current balance

#### Technical Practices Compliance:
- ✅ **ID-based Locators:** Primary use of ID attributes for stability
- ✅ **Explicit Waits:** WebDriverWait with ExpectedConditions (no Thread.sleep)
- ✅ **Page Object Model:** Clean abstractions with CashWithdrawalFormPage
- ✅ **Comprehensive Logging:** Emoji-based logging for easy test identification
- ✅ **JavaScript Testing:** Real-time validation, balance calculations, warning systems
- ✅ **Error Handling:** Client-side and server-side validation testing
- ✅ **Multi-role Testing:** Teller and Manager permission validation

#### Playwright Test Commands:
```bash
# Run Cash Withdrawal Playwright tests (headless mode)
mvn test -Dtest=CashWithdrawalPlaywrightTest

# Run with visible browser for debugging
mvn test -Dtest=CashWithdrawalPlaywrightTest -Dplaywright.headless=false

# Run with recording enabled for monitoring
mvn test -Dtest=CashWithdrawalPlaywrightTest -Dplaywright.recording.enabled=true

# Run specific test method
mvn test -Dtest=CashWithdrawalPlaywrightTest#shouldProcessCashWithdrawalSuccessfully

# Run with Firefox browser
mvn test -Dtest=CashWithdrawalPlaywrightTest -Dplaywright.browser=firefox

# Combined debugging options
mvn test -Dtest=CashWithdrawalPlaywrightTest -Dplaywright.headless=false -Dplaywright.recording.enabled=true
```

#### Test Data Integration:
- **CSV Parameterized Testing:** `/fixtures/transaction/valid-cash-withdrawals.csv`
- **SQL Setup/Cleanup:** Automated test data management
- **Account Test Data:** Multiple accounts with different balance scenarios
- **Percentage-based Amounts:** Dynamic withdrawal amounts based on account balance

#### Page Objects Created:
- **CashWithdrawalFormPage:** Complete withdrawal form automation
- **AccountSelectionPage:** Enhanced for withdrawal support
- **TransactionListPage:** Updated with withdrawal button functionality
- **TransactionViewPage:** Withdrawal transaction detail validation

#### JavaScript Validation Testing:
- **Real-time Balance Calculation:** New balance display during input
- **Insufficient Balance Warnings:** Client-side warning system
- **Button State Management:** Submit button disabled/enabled based on validation
- **Warning Text Validation:** Specific insufficient balance messages
- **Form Reset Testing:** JavaScript state reset functionality

#### Islamic Banking Compliance Testing:
- Uses existing `Account.withdraw()` business logic
- Validates Shariah-compliant transaction processing
- Tests Islamic banking product types (Wadiah, Mudharabah)
- Ensures no interest-based calculations in withdrawal processing