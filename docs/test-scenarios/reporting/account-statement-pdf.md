# Test Scenarios: Cetak Rekening Koran PDF

## Overview
Dokumen ini berisi skenario test untuk fitur cetak rekening koran dalam format PDF yang telah diimplementasi. Fitur ini menggunakan iText PDF library untuk menghasilkan dokumen profesional yang dapat dicetak ke printer apapun, membuatnya lebih fleksibel dan kompatibel dengan berbagai perangkat.

## Implementation Status
✅ **IMPLEMENTED** - Fitur Account Statement PDF telah diimplementasi dengan komponen berikut:
- `AccountStatementService`: Business logic untuk mengambil data account dan transaksi
- `AccountStatementPdfService`: PDF generation menggunakan iText 5.5.13.3
- REST API endpoints: `POST /api/accounts/statement/pdf` dan `GET /api/accounts/statement/pdf`
- Web interface: `/account/{accountId}/statement` untuk form input dan PDF download
- `AccountStatementRequest` DTO dengan validasi tanggal
- Branch-based access control terintegrasi

## Preconditions
- Database PostgreSQL berjalan
- Aplikasi Spring Boot aktif
- User sudah login sebagai Customer Service, Teller, atau Customer
- Account nasabah sudah ada dengan transaction history
- PDF generation library tersedia (iText, JasperReports, atau serupa)
- Data transactions tersedia dalam rentang waktu yang diminta

## Test Cases

### TC-AS-001: Cetak Rekening Koran Bulanan - Normal
**Deskripsi**: Mencetak rekening koran untuk periode 1 bulan dengan transactions normal

**Test Data**:
- Account Number: ACC0000001
- Period: 2024-01-01 to 2024-01-31
- Transaction Count: 15 transactions (mix DEPOSIT dan WITHDRAWAL)
- Starting Balance: 1000000.00
- Ending Balance: 1250000.00
- Customer: PersonalCustomer dengan firstName="Ahmad", lastName="Susanto"

**Steps**:
1. Login sebagai Customer Service
2. Navigasi ke menu "Cetak Rekening Koran"
3. Input account number: ACC0000001
4. Pilih start date: 2024-01-01
5. Pilih end date: 2024-01-31
6. Pilih format: PDF
7. Klik "Generate Rekening Koran"

**Expected Result**:
- PDF berhasil di-generate dalam <5 detik
- PDF content mencakup:
  - Header bank dengan logo dan alamat
  - Customer data (display name "Ahmad Susanto", account number, product name)
  - Periode laporan (01/01/2024 - 31/01/2024)
  - Starting balance periode
  - Detail semua transactions dengan proper formatting:
    - transaction_date (format: dd/MM/yyyy HH:mm)
    - description
    - amount with currency formatting (Rp 1.000.000,00)
    - balance_after for running balance
  - Ending balance periode
  - Total DEPOSIT dan total WITHDRAWAL dalam periode
  - Footer dengan timestamp generation dan created_by user
- PDF dapat dibuka tanpa error
- PDF dapat dicetak ke printer fisik
- File size reasonable (<2MB untuk 50 transactions)

### TC-AS-002: Cetak Rekening Koran Harian
**Deskripsi**: Mencetak rekening koran untuk periode 1 hari

**Test Data**:
- Account Number: ACC0000002
- Period: 2024-01-15 (single day)
- Transaction Count: 5 transactions dalam 1 hari
- Transaction types: DEPOSIT, WITHDRAWAL

**Steps**:
1. Login sebagai Teller
2. Navigasi ke menu "Cetak Rekening Koran"
3. Input account number: ACC0000002
4. Pilih start date: 2024-01-15
5. Pilih end date: 2024-01-15
6. Generate PDF

**Expected Result**:
- PDF berhasil di-generate
- Menampilkan hanya transactions dengan transaction_date between '2024-01-15 00:00:00' and '2024-01-15 23:59:59'
- Starting balance adalah balance sebelum hari tersebut
- Semua transactions hari itu tercantum lengkap dengan timestamp
- Running balance calculation accurate per transaction

### TC-AS-003: Cetak Rekening Koran Periode Kosong
**Deskripsi**: Mencetak rekening koran untuk periode tanpa transactions

**Test Data**:
- Account Number: ACC0000003
- Period: 2024-02-01 to 2024-02-28 (tidak ada transactions)
- Account Status: ACTIVE

**Steps**:
1. Login sebagai Customer Service
2. Input account number: ACC0000003
3. Pilih periode tanpa transactions
4. Generate PDF

**Expected Result**:
- PDF tetap berhasil di-generate
- Header dan footer tetap ada dengan customer information
- Content menampilkan:
  - "Tidak ada transaksi dalam periode ini"
  - Starting balance = ending balance (tidak berubah)
  - Total DEPOSIT = Rp 0
  - Total WITHDRAWAL = Rp 0
- PDF valid dan dapat dibuka

### TC-AS-004: Cetak Rekening Koran Volume Besar
**Deskripsi**: Mencetak rekening koran dengan volume transactions tinggi

**Test Data**:
- Account Number: ACC0000004
- Period: 2024-01-01 to 2024-12-31 (1 tahun)
- Transaction Count: 500+ transactions
- Mix transaction types dan amounts

**Steps**:
1. Login sebagai Customer Service
2. Input account number: ACC0000004
3. Pilih periode 1 tahun penuh
4. Generate PDF

**Expected Result**:
- PDF berhasil di-generate meskipun volume besar
- Response time <30 detik untuk 500 transactions
- PDF structure tetap rapi dengan proper pagination
- Memory usage tidak excessive
- File size reasonable (<10MB)
- Semua transactions tercantum lengkap dan sorted by transaction_date
- Page breaks pada tempat yang tepat (tidak memotong transaction record)

### TC-AS-005: Cetak Rekening Koran dengan Filter Transaction Type
**Deskripsi**: Mencetak rekening koran dengan filter jenis transaksi tertentu

**Test Data**:
- Account Number: ACC0000005
- Period: 2024-01-01 to 2024-01-31
- Filter: Hanya transaction_type = 'DEPOSIT'

**Steps**:
1. Login sebagai Customer Service
2. Input account number: ACC0000005
3. Pilih periode
4. Centang filter "Hanya Setoran (DEPOSIT)"
5. Generate PDF

**Expected Result**:
- PDF hanya menampilkan transactions dengan transaction_type = 'DEPOSIT'
- Kolom untuk WITHDRAWAL amounts kosong atau hidden
- Total hanya menghitung filtered transactions
- Running balance masih dihitung berdasarkan ALL transactions (bukan hanya yang ditampilkan)
- Header jelas mencantumkan filter yang diterapkan
- Note: "Laporan ini hanya menampilkan transaksi setoran"

### TC-AS-006: Validation - Account Not Found
**Deskripsi**: Validasi cetak rekening koran untuk account yang tidak exist

**Test Data**:
- Account Number: ACC9999999 (tidak exist)

**Steps**:
1. Login sebagai Customer Service
2. Input account number: ACC9999999
3. Pilih periode valid
4. Coba generate PDF

**Expected Result**:
- Error message: "Account number not found"
- PDF tidak di-generate
- Form kembali ke state awal
- No file yang ter-download
- Proper error logging

### TC-AS-007: Validation - Invalid Date Range
**Deskripsi**: Validasi input periode yang tidak valid

**Test Data**:
- Start Date: 2024-01-31
- End Date: 2024-01-01 (end < start)

**Steps**:
1. Login sebagai Customer Service
2. Input account number valid
3. Input end date lebih awal dari start date
4. Coba generate PDF

**Expected Result**:
- Client-side validation error: "End date must be greater than start date"
- Server-side validation backup
- PDF tidak di-generate
- Date fields di-highlight
- Focus ke field tanggal yang error

### TC-AS-008: Validation - Future Date Range
**Deskripsi**: Validasi periode yang mencakup tanggal future

**Test Data**:
- Start Date: 2024-01-01
- End Date: 2025-12-31 (future date)

**Steps**:
1. Login sebagai Customer Service
2. Input periode yang mencakup future dates
3. Coba generate PDF

**Expected Result**:
- Warning message: "Period includes future dates, only historical data will be shown"
- Atau restriction: "End date cannot be in the future"
- PDF generation proceeds dengan data yang ada
- Clear indication dalam report tentang date limitation

### TC-AS-009: Validation - Excessive Date Range
**Deskripsi**: Validasi periode yang terlalu panjang

**Test Data**:
- Period: 2020-01-01 to 2024-12-31 (5 tahun)

**Steps**:
1. Login sebagai Customer Service
2. Input periode 5 tahun
3. Coba generate PDF

**Expected Result**:
- Warning message: "Period too long, may take significant time to process"
- Confirmation dialog: "Continue with large date range?"
- Option to limit results atau split into smaller periods
- Progress indicator jika dilanjutkan
- Timeout handling untuk long-running queries

### TC-AS-010: Customer Self-Service - Own Account
**Deskripsi**: Customer mencetak rekening koran sendiri

**Test Data**:
- User: customer (role = CUSTOMER)
- Account Number: ACC0000006 (owned by logged-in customer)

**Steps**:
1. Login sebagai customer
2. Navigasi ke menu "Rekening Koran"
3. Account list otomatis filtered untuk customer tersebut
4. Pilih periode
5. Generate PDF

**Expected Result**:
- Customer hanya dapat melihat accounts yang dimiliki
- Account selection dropdown otomatis filtered by customer ownership
- PDF berhasil di-generate dengan format standar
- Download otomatis dimulai
- Audit log mencatat customer self-service action

### TC-AS-011: Security Test - Unauthorized Account Access
**Deskripsi**: Validasi customer tidak dapat mencetak rekening orang lain

**Test Data**:
- User: customer1 (id = uuid1)
- Account Number: ACC0000007 (owned by customer2, id = uuid2)

**Steps**:
1. Login sebagai customer1
2. Coba akses URL langsung dengan parameter account customer2
3. Coba manipulasi request untuk account lain

**Expected Result**:
- Error 403 Forbidden
- Message: "You can only access your own accounts"
- Redirect ke account list customer sendiri
- API endpoint validates account ownership
- Security audit log untuk unauthorized access attempt

### TC-AS-012: Corporate Customer Account Statement
**Deskripsi**: Test format PDF untuk corporate customer

**Test Data**:
- Customer Type: CORPORATE
- Company Name: PT. Teknologi Maju
- Contact Person: Budi Hartono (Finance Manager)
- Account dengan high transaction volume

**Steps**:
1. Generate PDF untuk corporate account
2. Verify customer information display

**Expected Result**:
- PDF menampilkan corporate information:
  - Company name sebagai primary identifier
  - Contact person information
  - Company registration number (jika diperlukan)
- Format tetap professional dan branded
- Same transaction details format

### TC-AS-013: Account Statement with Different Transaction Channels
**Deskripsi**: Test display untuk transactions dari berbagai channels

**Test Data**:
- Transactions dengan channel: TELLER, ATM, ONLINE, MOBILE
- Mixed transaction types per channel

**Steps**:
1. Generate PDF untuk account dengan mixed channels
2. Verify channel information display

**Expected Result**:
- Transaction channel ditampilkan dalam description atau separate column
- Clear indication sumber transaction (TELLER vs ATM vs ONLINE)
- Consistent formatting across all channels
- Channel-specific additional information (jika ada)

### TC-AS-014: Concurrent PDF Generation
**Deskripsi**: Test multiple users generate PDF bersamaan

**Test Data**:
- 10 users berbeda generate PDF bersamaan
- Different accounts dan periods
- Mix of customer self-service dan staff-generated

**Steps**:
1. 10 users login bersamaan (mix customer & staff)
2. Generate PDF di waktu yang sama
3. Monitor server performance

**Expected Result**:
- Semua PDF berhasil di-generate
- No file corruption atau mixing of data
- Server tetap responsive
- Memory usage dalam batas normal
- No deadlock atau resource conflict
- Each PDF contains correct customer data (no data bleeding)

## Performance Test Cases

### TC-AS-P001: Load Test PDF Generation
**Deskripsi**: Test performa generation PDF dengan load tinggi

**Test Scenario**:
- 50 concurrent users
- Masing-masing generate 5 PDF/menit
- Durasi test: 10 menit
- Mix periode pendek dan panjang

**Expected Result**:
- Average response time < 10 detik
- 95% request < 15 detik
- Success rate > 99%
- Memory usage stabil
- No memory leak
- Database connection pool efficient

### TC-AS-P002: Large Dataset Performance
**Deskripsi**: Test performa dengan dataset transactions besar

**Test Scenario**:
- Account dengan 1000+ transactions
- Periode 1 tahun
- Complex filtering criteria

**Expected Result**:
- Generation time < 60 detik
- PDF file size < 20MB
- Memory usage efficient
- No timeout errors
- Query optimization effective

## PDF Content Validation

### Template Content Requirements
1. **Header Section**:
   - Bank logo (ukuran dan posisi consistent)
   - Bank name dan alamat
   - Title "REKENING KORAN" / "ACCOUNT STATEMENT"
   - Report period (dd/MM/yyyy format)

2. **Customer Information Section**:
   - Customer display name (firstName + lastName untuk personal, companyName untuk corporate)
   - Account number
   - Product name (dari Product.productName)
   - Account status (ACTIVE/INACTIVE/etc)

3. **Transaction Table**:
   - Header columns: Tanggal | Keterangan | Debit | Kredit | Saldo
   - Date format: dd/MM/yyyy HH:mm
   - Amount format: Rp 1.000.000,00 (Indonesian currency format)
   - Text alignment: Date & Description left, Amounts right
   - Running balance calculation accurate

4. **Summary Section**:
   - Starting balance (saldo awal periode)
   - Total debit (sum of WITHDRAWAL, TRANSFER_OUT, FEE)
   - Total credit (sum of DEPOSIT, TRANSFER_IN)
   - Ending balance (saldo akhir periode)

5. **Footer Section**:
   - Generation timestamp
   - Generated by user information
   - Page numbering (Page X of Y)
   - Disclaimer text (jika diperlukan)

## API Test Examples

### REST API Calls (Actual Implementation)
```bash
# Generate Account Statement PDF (POST with JSON body)
curl -X POST http://localhost:8080/api/accounts/statement/pdf \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "ACC0000001",
    "startDate": "2024-01-01",
    "endDate": "2024-01-31"
  }' \
  --output statement.pdf

# Generate Account Statement PDF (GET with parameters)
curl "http://localhost:8080/api/accounts/statement/pdf?accountNumber=ACC0000001&startDate=2024-01-01&endDate=2024-01-31" \
  --output statement.pdf

# Generate using Account ID instead of Account Number
curl -X POST http://localhost:8080/api/accounts/statement/pdf \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "123e4567-e89b-12d3-a456-426614174000",
    "startDate": "2024-01-01", 
    "endDate": "2024-01-31"
  }' \
  --output statement.pdf

# Web Interface Access
# Navigate to: http://localhost:8080/account/{accountId}/statement
# Fill form with date range and submit to download PDF
```

## Database Queries for Statement Generation

### Main Statement Query
```sql
-- Get account information
SELECT 
  a.account_number,
  a.account_name,
  a.balance as current_balance,
  a.status,
  c.customer_type,
  CASE 
    WHEN c.customer_type = 'PERSONAL' THEN pc.first_name || ' ' || pc.last_name
    WHEN c.customer_type = 'CORPORATE' THEN cc.company_name
  END as customer_display_name,
  p.product_name,
  p.product_type
FROM accounts a
JOIN customers c ON a.id_customers = c.id
LEFT JOIN personal_customers pc ON c.id = pc.id
LEFT JOIN corporate_customers cc ON c.id = cc.id
JOIN products p ON a.id_products = p.id
WHERE a.account_number = ?;

-- Get transactions for period
SELECT 
  t.transaction_date,
  t.transaction_type,
  t.amount,
  t.balance_before,
  t.balance_after,
  t.description,
  t.reference_number,
  t.channel,
  t.created_by
FROM transactions t
WHERE t.id_accounts = ?
  AND t.transaction_date >= ?
  AND t.transaction_date <= ?
ORDER BY t.transaction_date ASC;

-- Get starting balance (last transaction before period)
SELECT balance_after as starting_balance
FROM transactions 
WHERE id_accounts = ? 
  AND transaction_date < ?
ORDER BY transaction_date DESC 
LIMIT 1;

-- Summary calculations
SELECT 
  SUM(CASE WHEN transaction_type IN ('DEPOSIT', 'TRANSFER_IN') THEN amount ELSE 0 END) as total_credit,
  SUM(CASE WHEN transaction_type IN ('WITHDRAWAL', 'TRANSFER_OUT', 'FEE') THEN amount ELSE 0 END) as total_debit,
  COUNT(*) as transaction_count
FROM transactions 
WHERE id_accounts = ?
  AND transaction_date >= ?
  AND transaction_date <= ?;
```

## Error Handling Test Cases

### TC-AS-E001: PDF Library Error
**Deskripsi**: Test handling ketika PDF library error

**Steps**:
1. Simulasi PDF library unavailable atau corrupted
2. Coba generate PDF

**Expected Result**:
- Graceful error handling dengan informative message
- "PDF generation service temporarily unavailable"
- No application crash
- Fallback mechanism (jika ada, misalnya HTML export)
- Error logging untuk troubleshooting

### TC-AS-E002: Disk Space Full
**Deskripsi**: Test handling ketika disk space penuh

**Steps**:
1. Simulasi disk space full di temp directory
2. Coba generate large PDF

**Expected Result**:
- Error detection sebelum/selama process
- Clear error message: "Insufficient disk space for PDF generation"
- No partial file creation
- Proper cleanup of temporary files
- Suggestion untuk retry later

### TC-AS-E003: Database Query Timeout
**Deskripsi**: Test handling untuk long-running queries

**Steps**:
1. Generate statement untuk account dengan massive transaction history
2. Simulate query timeout

**Expected Result**:
- Query timeout handled gracefully
- Option untuk split date range
- Progress indicator untuk user awareness
- Ability to cancel long-running operations

## Cleanup and Maintenance

### Temporary Files Cleanup
```bash
# Cleanup temporary PDF files (automated via scheduled job)
find /tmp -name "statement_*.pdf" -mtime +1 -delete

# Monitor disk usage for PDF generation
df -h /tmp
du -sh /app/temp/statements/*

# Cleanup old statement generation logs
find /var/log/statements -name "*.log" -mtime +30 -delete
```

### Database Maintenance
```sql
-- Performance monitoring for statement queries
EXPLAIN ANALYZE 
SELECT * FROM transactions 
WHERE id_accounts = ? 
  AND transaction_date BETWEEN ? AND ?
ORDER BY transaction_date;

-- Index usage verification
SELECT schemaname, tablename, indexname, idx_tup_read, idx_tup_fetch
FROM pg_stat_user_indexes 
WHERE tablename IN ('transactions', 'accounts', 'customers');

-- Transaction count per account (for capacity planning)
SELECT 
  a.account_number, 
  COUNT(t.id) as transaction_count,
  MIN(t.transaction_date) as first_transaction,
  MAX(t.transaction_date) as last_transaction
FROM accounts a
LEFT JOIN transactions t ON a.id = t.id_accounts
GROUP BY a.id, a.account_number
ORDER BY transaction_count DESC
LIMIT 10;
```

## Business Rules Validation

### Account Statement Business Rules:
1. **Date Range**: Maximum 2 years in single request
2. **Transaction Limit**: Maximum 10,000 transactions per statement
3. **File Size**: Maximum 50MB per PDF
4. **Access Control**: 
   - Customers can only access own accounts
   - Staff can access based on role permissions
   - Audit log for all statement generations
5. **Data Privacy**: 
   - Mask sensitive information for certain roles
   - Include only authorized transaction details

### Statement Content Requirements:
1. **Accuracy**: Running balance must match actual account balance
2. **Completeness**: All transactions in period must be included
3. **Audit Trail**: Generation timestamp and user must be recorded
4. **Formatting**: Consistent currency and date formatting
5. **Security**: PDF should not contain sensitive system information

## Integration Test Scenarios

### TC-AS-I001: End-to-End Account Statement Flow
**Steps**:
1. Create new personal customer
2. Open account dengan initial deposit
3. Process multiple transactions (deposits, withdrawals)
4. Generate statement untuk full account history
5. Verify PDF content accuracy

**Expected Result**:
- Complete customer journey works end-to-end
- All data properly integrated across modules
- PDF reflects exact transaction history
- Balance calculations accurate throughout

### TC-AS-I002: Multi-Account Customer Statement
**Steps**:
1. Customer dengan multiple accounts (different products)
2. Generate statements untuk each account
3. Verify account isolation dalam reports

**Expected Result**:
- Each statement contains only relevant account data
- No data bleeding between accounts
- Customer information consistent across statements
- Proper account identification in each PDF