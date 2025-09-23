# Test Scenarios: Setoran Tunai

## Overview
Dokumen ini berisi skenario test untuk fitur setoran tunai dalam aplikasi minibank Islam. Setoran tunai mencakup validasi rekening, pencatatan transaksi, dan update saldo menggunakan entity business methods.

## Preconditions
- Database PostgreSQL berjalan
- Aplikasi Spring Boot aktif
- User sudah login sebagai Teller atau Customer Service
- Account nasabah sudah ada dan dalam status ACTIVE
- Sequence number service berfungsi normal untuk transaction_number

## Test Cases

### TC-CD-001: Setoran Tunai Normal - TABUNGAN_WADIAH
**Deskripsi**: Melakukan setoran tunai ke rekening TABUNGAN_WADIAH dengan nominal normal

**Test Data**:
- Account Number: ACC0000001 (TABUNGAN_WADIAH)
- Current Balance: 500000.00 (DECIMAL 20,2)
- Deposit Amount: 250000.00 (must be positive)
- Transaction Type: DEPOSIT
- Transaction Channel: TELLER
- Created By: teller1 (max 100 chars)

**Steps**:
1. Login sebagai Teller (teller1)
2. Navigasi ke menu "Setoran Tunai"
3. Input account number: ACC0000001
4. Sistem menampilkan customer data dan current balance
5. Input deposit amount: 250000.00
6. Input description: "Setoran tunai harian"
7. Konfirmasi transaksi
8. Klik "Proses Setoran"

**Expected Result**:
- Account.deposit(amount) method dipanggil
- Balance sebelum: 500000.00
- Balance sesudah: 750000.00
- Transaction record tersimpan dengan:
  - transaction_number: auto-generated (VARCHAR 50, UNIQUE)
  - transaction_type: DEPOSIT
  - amount: 250000.00
  - currency: IDR
  - balance_before: 500000.00
  - balance_after: 750000.00
  - channel: TELLER
  - description: "Setoran tunai harian"
  - transaction_date: current timestamp
  - processed_date: current timestamp
  - created_by: teller1
- Constraint validation: amount > 0 ✓
- Constraint validation: balance_after = balance_before + amount ✓
- Notifikasi sukses ditampilkan

### TC-CD-002: Setoran Tunai Besar - TABUNGAN_MUDHARABAH
**Deskripsi**: Melakukan setoran tunai dengan nominal besar ke rekening TABUNGAN_MUDHARABAH

**Test Data**:
- Account Number: ACC0000002 (TABUNGAN_MUDHARABAH)
- Current Balance: 1000000.00
- Deposit Amount: 50000000.00 (large amount)
- Transaction Channel: TELLER
- Created By: teller2

**Steps**:
1. Login sebagai Teller (teller2)
2. Navigasi ke menu "Setoran Tunai"
3. Input account number: ACC0000002
4. Sistem menampilkan customer data dan balance
5. Input deposit amount: 50000000.00
6. Input description: "Setoran hasil usaha"
7. Konfirmasi transaksi
8. Klik "Proses Setoran"

**Expected Result**:
- Transaction berhasil diproses
- Balance updated: 1000000.00 + 50000000.00 = 51000000.00
- Transaction record tersimpan dengan benar
- For large amounts, mungkin perlu additional approval workflow
- Monitoring flag untuk suspicious transactions (if > threshold)
- Profit sharing calculation may be triggered for MUDHARABAH account

### TC-CD-003: Setoran Tunai dengan Reference Number
**Deskripsi**: Setoran tunai dengan reference number untuk tracking

**Test Data**:
- Account Number: ACC0000003
- Deposit Amount: 175000.00
- Reference Number: REF20240115001 (max 100 chars)
- Description: Detail breakdown of denominations

**Steps**:
1. Login sebagai Teller
2. Navigasi ke menu "Setoran Tunai"
3. Input account number: ACC0000003
4. Input deposit amount: 175000.00
5. Input reference number: REF20240115001
6. Input description: "100rb:1, 50rb:1, 20rb:1, 5rb:1"
7. Konfirmasi dan proses setoran

**Expected Result**:
- Total amount: 175000.00
- Reference number tersimpan: REF20240115001
- Description tersimpan dengan detail denominations
- Balance account terupdate dengan benar
- Reference number dapat digunakan untuk search/lookup

### TC-CD-004: Validation - Account Not Found
**Deskripsi**: Validasi setoran ke account yang tidak exist

**Test Data**:
- Account Number: ACC9999999 (tidak exist)

**Steps**:
1. Login sebagai Teller
2. Navigasi ke menu "Setoran Tunai"
3. Input account number: ACC9999999
4. Tekan Enter atau klik "Cari"

**Expected Result**:
- Error message: "Account number not found"
- Form tidak dapat dilanjutkan
- Tidak ada transaction yang tercatat
- Focus kembali ke account number field

### TC-CD-005: Validation - Account Status INACTIVE
**Deskripsi**: Validasi setoran ke account dengan status non-aktif

**Test Data**:
- Account Number: ACC0000004
- Account Status: INACTIVE

**Steps**:
1. Login sebagai Teller
2. Navigasi ke menu "Setoran Tunai"  
3. Input account number: ACC0000004
4. Sistem menampilkan account status: INACTIVE
5. Coba input deposit amount: 100000.00

**Expected Result**:
- Warning message: "Account is inactive, transaction cannot be processed"
- Submit button disabled
- Tidak ada transaction yang tercatat
- Opsi untuk activate account (if user has permission)

### TC-CD-006: Validation - Account Status FROZEN
**Deskripsi**: Validasi setoran ke account yang di-freeze

**Test Data**:
- Account Number: ACC0000005
- Account Status: FROZEN

**Steps**:
1. Login sebagai Teller
2. Navigasi ke menu "Setoran Tunai"
3. Input account number: ACC0000005
4. Sistem menampilkan account status: FROZEN
5. Coba input deposit amount: 100000.00

**Expected Result**:
- Error message: "Account is frozen, transaction rejected"
- Form tidak dapat disubmit
- Tidak ada transaction yang tercatat
- Information about contact for unfreeze process

### TC-CD-007: Validation - Account Status CLOSED
**Deskripsi**: Validasi setoran ke account yang sudah ditutup

**Test Data**:
- Account Number: ACC0000006
- Account Status: CLOSED
- Closed Date: 2024-01-01

**Steps**:
1. Login sebagai Teller
2. Input account number: ACC0000006
3. Sistem check account status

**Expected Result**:
- Error message: "Account is closed, no transactions allowed"
- Display closed date
- Transaction rejected
- Tidak ada record transaction yang tercatat

### TC-CD-008: Validation - Negative or Zero Amount
**Deskripsi**: Validasi input deposit amount tidak valid

**Test Data**:
- Deposit Amount: -50000.00 (negative)
- Deposit Amount: 0.00 (zero)

**Steps**:
1. Login sebagai Teller
2. Navigasi ke menu "Setoran Tunai"
3. Input valid account number: ACC0000001
4. Input deposit amount: -50000.00
5. Coba submit form
6. Ulangi dengan amount: 0.00

**Expected Result**:
- Error message: "Deposit amount must be positive"
- Form validation prevents submit
- Field highlighted in red
- Focus returns to amount field
- Database constraint chk_amount_positive prevents negative values

### TC-CD-009: Validation - Amount Precision
**Deskripsi**: Validasi precision untuk DECIMAL(20,2)

**Test Data**:
- Deposit Amount: 123456789012345678.123 (exceeds precision)
- Deposit Amount: 100.999 (3 decimal places)

**Steps**:
1. Login sebagai Teller
2. Input valid account number
3. Input amount dengan precision berlebihan
4. Submit form

**Expected Result**:
- Amount di-round ke 2 decimal places
- 123456789012345678.123 → 123456789012345678.12
- 100.999 → 101.00
- Atau error jika exceeds total precision (20 digits)

### TC-CD-010: Multiple Deposits Same Day
**Deskripsi**: Melakukan beberapa setoran tunai dalam satu hari untuk account sama

**Test Data**:
- Account Number: ACC0000001
- Deposit 1: 100000.00 (08:00)
- Deposit 2: 150000.00 (11:00)
- Deposit 3: 200000.00 (15:00)

**Steps**:
1. Login sebagai Teller
2. Proses deposit pertama: 100000.00
3. Proses deposit kedua: 150000.00
4. Proses deposit ketiga: 200000.00

**Expected Result**:
- Semua transactions berhasil diproses
- Balance terakumulasi dengan benar
- Setiap transaction punya transaction_number unik
- Transaction history tercatat lengkap dengan timestamp
- Balance calculation: initial + 100000 + 150000 + 200000

### TC-CD-011: Concurrent Deposits to Same Account
**Deskripsi**: Test race condition dengan deposits bersamaan ke account sama

**Test Data**:
- Account Number: ACC0000001
- Teller 1: deposit 100000.00
- Teller 2: deposit 200000.00 (bersamaan)

**Steps**:
1. Login sebagai Teller1 di terminal 1
2. Login sebagai Teller2 di terminal 2
3. Kedua teller input account sama: ACC0000001
4. Teller1 input amount: 100000.00
5. Teller2 input amount: 200000.00
6. Submit bersamaan dalam waktu < 1 detik

**Expected Result**:
- Kedua transactions berhasil diproses (database locking handles concurrency)
- Final balance = initial + 100000 + 200000
- Kedua transactions tercatat dengan transaction_number berbeda
- Timestamp berbeda meskipun hampir bersamaan
- Tidak ada data corruption atau lost updates
- Entity method Account.deposit() handles concurrency correctly

### TC-CD-012: Transaction with Different Channels
**Deskripsi**: Test deposit dengan berbagai transaction channels

**Test Data**:
- Channel: TELLER (default)
- Channel: ATM
- Channel: ONLINE
- Channel: MOBILE

**Steps**:
1. Process deposits dengan channel berbeda
2. Verify channel tersimpan dengan benar

**Expected Result**:
- Transaction channel tersimpan sesuai input
- Enum validation: channel IN ('TELLER', 'ATM', 'ONLINE', 'MOBILE', 'TRANSFER')
- Different business rules per channel (if applicable)

### TC-CD-013: Security Test - Unauthorized Access
**Deskripsi**: Validasi akses deposit oleh user tidak berwenang

**Test Data**:
- User: customer biasa
- User: admin (non-teller/non-CS)

**Steps**:
1. Login sebagai customer
2. Coba akses menu setoran tunai
3. Ulangi dengan user admin non-teller

**Expected Result**:
- Customer: tidak dapat akses menu setoran tunai
- Admin non-teller: error 403 Forbidden
- API endpoint menolak request dengan proper authorization
- Role-based access control enforced

### TC-CD-014: Business Logic - Account Balance Consistency
**Deskripsi**: Validasi konsistensi balance calculation

**Test Data**:
- Account dengan existing transactions
- New deposit amount

**Steps**:
1. Get current balance dari Account entity
2. Process new deposit
3. Verify balance calculation

**Expected Result**:
- Balance calculation menggunakan Account.deposit() method
- Database constraint chk_balance_calculation enforced
- Balance consistency maintained across all transactions
- Audit trail complete

## Performance Test Cases

### TC-CD-P001: Load Test Deposit Transactions
**Deskripsi**: Test performa deposit dengan beban tinggi

**Test Scenario**:
- 50 concurrent tellers
- Masing-masing proses 20 deposits/menit
- Durasi test: 10 menit
- Total: 10,000 transactions

**Expected Result**:
- Response time < 2 detik per transaction
- Success rate > 99.9%
- Tidak ada deadlock di database
- Transaction number sequence tidak duplikat
- Memory usage stabil
- Database connection pool stable

### TC-CD-P002: Large Amount Deposits
**Deskripsi**: Test handling deposits dengan amount besar

**Test Scenario**:
- Deposits dengan amount mendekati DECIMAL(20,2) limit
- Multiple large deposits ke account sama

**Expected Result**:
- Precision maintained untuk large numbers
- No overflow errors
- Balance calculation accurate
- Performance tidak degraded

## Database Validation

### Post-Transaction Validation
```sql
-- Check balance consistency
SELECT a.account_number, a.balance, 
       COALESCE(SUM(CASE 
         WHEN t.transaction_type = 'DEPOSIT' THEN t.amount 
         WHEN t.transaction_type = 'WITHDRAWAL' THEN -t.amount 
         ELSE 0 END), 0) as calculated_balance
FROM accounts a
LEFT JOIN transactions t ON a.id = t.id_accounts
WHERE a.account_number = 'ACC0000001'
GROUP BY a.id, a.account_number, a.balance;

-- Check transaction constraints
SELECT * FROM transactions 
WHERE amount <= 0  -- Should be empty (violates chk_amount_positive)
OR balance_after != balance_before + amount  -- Should be empty for DEPOSIT

-- Check transaction sequence
SELECT transaction_number, transaction_date, amount, description
FROM transactions 
WHERE id_accounts = (SELECT id FROM accounts WHERE account_number = 'ACC0000001')
ORDER BY transaction_date DESC;

-- Check sequence number integrity
SELECT sequence_name, last_number, updated_date
FROM sequence_numbers 
WHERE sequence_name = 'TRANSACTION_NUMBER';
```

## API Test Examples

### REST API Calls
```bash
# Process Cash Deposit
curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "accountNumber": "ACC0000001",
    "amount": 250000.00,
    "description": "Setoran tunai harian",
    "channel": "TELLER",
    "referenceNumber": "REF20240115001"
  }'

# Check Account Balance
curl -X GET http://localhost:8080/api/accounts/ACC0000001/balance \
  -H "Authorization: Bearer <token>"

# Get Transaction History
curl -X GET "http://localhost:8080/api/accounts/ACC0000001/transactions?limit=10&type=DEPOSIT" \
  -H "Authorization: Bearer <token>"

# Get Account Details with Status
curl -X GET http://localhost:8080/api/accounts/ACC0000001 \
  -H "Authorization: Bearer <token>"
```

## Error Handling Test Cases

### TC-CD-E001: Database Connection Lost
**Deskripsi**: Test handling ketika koneksi database terputus

**Steps**:
1. Mulai proses deposit
2. Matikan database connection saat submit
3. Restart database
4. Retry transaction

**Expected Result**:
- Graceful error handling dengan informative message
- Transaction tidak double processed
- Automatic rollback jika transaction gagal
- Connection retry mechanism berfungsi
- Data integrity maintained

### TC-CD-E002: Transaction Rollback
**Deskripsi**: Test rollback ketika error terjadi

**Steps**:
1. Process deposit dengan constraint violation
2. Verify rollback behavior

**Expected Result**:
- Complete transaction rollback
- Account balance tidak berubah
- Tidak ada partial transaction records
- Proper error logging

## Field Validation Reference

### Transaction Entity Fields:
- `transaction_number` (VARCHAR 50, UNIQUE, NOT NULL)
- `transaction_type` (ENUM: DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT, FEE)
- `amount` (DECIMAL 20,2, NOT NULL, > 0)
- `currency` (VARCHAR 3, default 'IDR')
- `balance_before` (DECIMAL 20,2, NOT NULL)
- `balance_after` (DECIMAL 20,2, NOT NULL)
- `description` (TEXT)
- `reference_number` (VARCHAR 100)
- `channel` (ENUM: TELLER, ATM, ONLINE, MOBILE, TRANSFER)
- `transaction_date` (TIMESTAMP, default current)
- `processed_date` (TIMESTAMP, default current)
- `created_by` (VARCHAR 100)

### Account Entity Fields:
- `account_number` (VARCHAR 50, UNIQUE, NOT NULL)
- `balance` (DECIMAL 20,2, ≥ 0)
- `status` (ENUM: ACTIVE, INACTIVE, CLOSED, FROZEN)

### Business Rules:
- Amount must be positive (chk_amount_positive)
- Balance calculation must be correct (chk_balance_calculation)
- Account status must be ACTIVE for deposits
- Balance cannot be negative (chk_balance_non_negative)

## Cleanup Scripts

### Test Data Cleanup
```sql
-- Cleanup test transactions
DELETE FROM transactions 
WHERE description LIKE '%test%' 
OR description LIKE '%Test%'
OR reference_number LIKE 'TEST%';

-- Reset specific account balances untuk test accounts
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

-- Reset transaction sequence if needed
UPDATE sequence_numbers 
SET last_number = (
  SELECT COALESCE(MAX(CAST(SUBSTRING(transaction_number FROM '[0-9]+') AS INTEGER)), 0)
  FROM transactions
) 
WHERE sequence_name = 'TRANSACTION_NUMBER';
```