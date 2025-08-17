# Test Scenarios: Pembukaan Rekening

## Overview
Dokumen ini berisi skenario test untuk fitur pembukaan rekening dalam aplikasi minibank Islam. Pembukaan rekening mencakup validasi data nasabah, pemilihan produk syariah, dan pembuatan nomor rekening.

## Preconditions
- Database PostgreSQL berjalan
- Aplikasi Spring Boot aktif  
- User sudah login sebagai Customer Service (CS) atau Teller
- Produk syariah tersedia (TABUNGAN_WADIAH, TABUNGAN_MUDHARABAH, DEPOSITO_MUDHARABAH)

## Test Coverage Status

✅ **FULLY AUTOMATED** - All test scenarios below are covered by Selenium tests  
📁 **Test Classes**: `PersonalAccountOpeningSeleniumTest`, `CorporateAccountOpeningSeleniumTest`, `IslamicBankingAccountOpeningSeleniumTest`, `ComprehensiveAccountOpeningSeleniumTest`

### Test Execution Commands
```bash
# Run all account opening tests
mvn test -Dtest="*AccountOpening*"

# Run specific test categories
mvn test -Dtest=PersonalAccountOpeningSeleniumTest          # Personal customer tests
mvn test -Dtest=CorporateAccountOpeningSeleniumTest         # Corporate customer tests  
mvn test -Dtest=IslamicBankingAccountOpeningSeleniumTest     # Islamic banking tests
mvn test -Dtest=ComprehensiveAccountOpeningSeleniumTest      # Edge cases & validation
```

## Test Cases

### TC-AO-001: Pembukaan Rekening Personal - Happy Path ✅
**📋 Coverage**: `PersonalAccountOpeningSeleniumTest.shouldSuccessfullyOpenPersonalAccountWithValidData()`  
**🎭 Also covered by**: `IslamicBankingAccountOpeningSeleniumTest.shouldOpenTabunganWadiahAccountSuccessfully()`
**Deskripsi**: Membuka rekening TABUNGAN_WADIAH untuk nasabah personal dengan data valid

**Test Data**:
- Customer Type: PERSONAL
- First Name: Ahmad (max 100 chars)
- Last Name: Susanto (max 100 chars)
- Identity Number: 3201010101010001 (max 50 chars)
- Identity Type: KTP
- Email: ahmad.susanto@email.com (max 100 chars)
- Phone Number: 081234567890 (max 20 chars)
- Address: Jl. Merdeka No. 10, Jakarta
- City: Jakarta (max 100 chars)
- Postal Code: 12345 (max 10 chars)
- Country: Indonesia (max 50 chars)
- Date Of Birth: 1990-01-01 (must be past date)
- Product: TABUNGAN_WADIAH
- Initial Deposit: 100000 (must meet minimum_opening_balance)

**Steps**:
1. Login sebagai Customer Service
2. Navigasi ke menu "Pembukaan Rekening"
3. Pilih "Customer Type" = PERSONAL
4. Isi form data personal:
   - First Name: Ahmad
   - Last Name: Susanto
   - Identity Number: 3201010101010001
   - Identity Type: KTP
   - Email: ahmad.susanto@email.com
   - Phone Number: 081234567890
   - Address: Jl. Merdeka No. 10, Jakarta
   - City: Jakarta
   - Date Of Birth: 1990-01-01
5. Pilih Product: TABUNGAN_WADIAH
6. Input initial deposit: 100000
7. Klik "Submit"

**Expected Result**:
- Customer berhasil disimpan ke tabel `customers` dan `personal_customers`
- Customer status = ACTIVE
- Account berhasil dibuat dengan account_number auto-generated
- Account status = ACTIVE
- Balance account = initial deposit (100,000)
- Account name = "Ahmad Susanto"
- Initial transaction DEPOSIT berhasil tercatat di tabel `transactions`
- Transaction type = DEPOSIT, channel = TELLER
- Notifikasi sukses ditampilkan

### TC-AO-002: Pembukaan Rekening Corporate - Happy Path ✅
**📋 Coverage**: `CorporateAccountOpeningSeleniumTest.shouldSuccessfullyOpenCorporateAccountWithValidData()`
**Deskripsi**: Membuka rekening TABUNGAN_MUDHARABAH untuk nasabah korporat

**Test Data**:
- Customer Type: CORPORATE
- Company Name: PT. Teknologi Maju (max 200 chars)
- Company Registration Number: 1234567890123 (max 100 chars)
- Tax Identification Number: 01.234.567.8-901.000 (max 50 chars)
- Contact Person Name: Budi Hartono (max 100 chars)
- Contact Person Title: Finance Manager (max 100 chars)
- Email: finance@tekno-maju.com (max 100 chars)
- Phone Number: 0215551234 (max 20 chars)
- Address: Jl. Sudirman No. 100, Jakarta
- City: Jakarta (max 100 chars)
- Product: TABUNGAN_MUDHARABAH
- Initial Deposit: 5000000

**Steps**:
1. Login sebagai Customer Service
2. Navigasi ke menu "Pembukaan Rekening"
3. Pilih "Customer Type" = CORPORATE
4. Isi form data corporate:
   - Company Name: PT. Teknologi Maju
   - Company Registration Number: 1234567890123
   - Tax Identification Number: 01.234.567.8-901.000
   - Contact Person Name: Budi Hartono
   - Contact Person Title: Finance Manager
   - Email: finance@tekno-maju.com
   - Phone Number: 0215551234
   - Address: Jl. Sudirman No. 100, Jakarta
   - City: Jakarta
5. Pilih Product: TABUNGAN_MUDHARABAH
6. Input initial deposit: 5000000
7. Klik "Submit"

**Expected Result**:
- Customer berhasil disimpan ke tabel `customers` dan `corporate_customers`
- Account berhasil dibuat dengan account_number auto-generated
- Account name = "PT. Teknologi Maju"
- Balance account = initial deposit (5,000,000)
- Account status = ACTIVE
- Profit sharing sesuai dengan product nisbah_customer dan nisbah_bank
- Initial transaction DEPOSIT berhasil tercatat
- Notifikasi sukses ditampilkan

### TC-AO-003: Pembukaan Rekening DEPOSITO_MUDHARABAH ✅
**📋 Coverage**: `IslamicBankingAccountOpeningSeleniumTest.shouldOpenDepositoMudharabahWithTermDeposit()`  
**🎭 Also covered by**: `ComprehensiveAccountOpeningSeleniumTest.shouldOpenDepositoMudharabahAccountWithProfitSharing()`
**Deskripsi**: Membuka rekening deposito dengan profit sharing

**Test Data**:
- Customer: Existing customer (Ahmad Susanto)
- Product: DEPOSITO_MUDHARABAH
- Nominal Deposito: 10000000
- Nisbah Customer: 0.6000 (60%)
- Nisbah Bank: 0.4000 (40%)
- Profit Distribution Frequency: ON_MATURITY

**Steps**:
1. Login sebagai Customer Service
2. Navigasi ke menu "Pembukaan Rekening"
3. Cari existing customer: Ahmad Susanto
4. Pilih Product: DEPOSITO_MUDHARABAH
5. Input nominal deposito: 10000000
6. Konfirmasi nisbah bagi hasil (60:40)
7. Klik "Submit"

**Expected Result**:
- Account deposito berhasil dibuat
- Product type = DEPOSITO_MUDHARABAH
- Profit sharing type = MUDHARABAH
- Balance account = nominal deposito
- Status account = ACTIVE
- Constraint check: nisbah_customer + nisbah_bank = 1.0

### TC-AO-004: Validation - Personal Customer Data Invalid ✅
**📋 Coverage**: `PersonalAccountOpeningSeleniumTest.shouldShowValidationErrorForMissingRequiredFields()`  
**🎭 Also covered by**: `ComprehensiveAccountOpeningSeleniumTest.shouldValidateSpecificFieldConstraints()`
**Deskripsi**: Validasi form pembukaan rekening dengan data tidak valid

**Test Data**:
- First Name: "" (blank - should fail @NotBlank)
- Last Name: "A".repeat(101) (exceeds 100 chars)
- Identity Number: "" (blank - should fail @NotBlank)
- Email: "invalid-email" (invalid format - should fail @Email)
- Phone Number: "123456789012345678901" (exceeds 20 chars)
- Date Of Birth: 2025-01-01 (future date - should fail @Past)
- Initial Deposit: -50000 (negative value)

**Steps**:
1. Login sebagai Customer Service
2. Navigasi ke menu "Pembukaan Rekening"
3. Pilih "Customer Type" = PERSONAL
4. Isi form dengan data tidak valid
5. Klik "Submit"

**Expected Result**:
- Form tidak ter-submit
- Validation error messages:
  - "First name is required"
  - "Last name must not exceed 100 characters"
  - "Identity number is required"
  - "Email should be valid"
  - "Phone number must not exceed 20 characters"
  - "Date of birth must be in the past"
  - "Initial deposit must be positive"
- Data tidak tersimpan ke database

### TC-AO-005: Validation - Corporate Customer Data Invalid ✅
**📋 Coverage**: `CorporateAccountOpeningSeleniumTest.shouldShowValidationErrorForMissingRequiredCorporateFields()`  
**🎭 Also covered by**: `ComprehensiveAccountOpeningSeleniumTest.shouldValidateCorporateCustomerFieldLengths()`
**Deskripsi**: Validasi form corporate customer dengan data tidak valid

**Test Data**:
- Company Name: "" (blank - should fail @NotBlank)
- Company Registration Number: "A".repeat(101) (exceeds 100 chars)
- Tax Identification Number: "A".repeat(51) (exceeds 50 chars)
- Contact Person Name: "A".repeat(101) (exceeds 100 chars)

**Steps**:
1. Login sebagai Customer Service
2. Navigasi ke menu "Pembukaan Rekening"
3. Pilih "Customer Type" = CORPORATE
4. Isi form dengan data tidak valid
5. Klik "Submit"

**Expected Result**:
- Form tidak ter-submit
- Validation error messages:
  - "Company name is required"
  - "Company registration number must not exceed 100 characters"
  - "Tax identification number must not exceed 50 characters"
  - "Contact person name must not exceed 100 characters"

### TC-AO-006: Validation - Minimum Opening Balance ✅
**📋 Coverage**: `PersonalAccountOpeningSeleniumTest.shouldShowValidationErrorForInsufficientInitialDeposit()`  
**🎭 Also covered by**: `CorporateAccountOpeningSeleniumTest.shouldShowValidationErrorForInsufficientCorporateDeposit()`, `IslamicBankingAccountOpeningSeleniumTest.shouldEnforceIslamicProductMinimumBalances()`
**Deskripsi**: Validasi setoran awal tidak memenuhi minimum

**Test Data**:
- Product: TABUNGAN_WADIAH (minimum_opening_balance dari database)
- Initial Deposit: amount < minimum_opening_balance

**Steps**:
1. Login sebagai Customer Service
2. Navigasi ke menu "Pembukaan Rekening"
3. Isi form customer valid
4. Pilih Product: TABUNGAN_WADIAH
5. Input initial deposit di bawah minimum
6. Klik "Submit"

**Expected Result**:
- Form tidak ter-submit
- Error message: "Setoran awal minimum untuk [product_name] adalah Rp [minimum_opening_balance]"
- Data tidak tersimpan

### TC-AO-007: Validation - Customer Number Duplicate ✅
**📋 Coverage**: `ComprehensiveAccountOpeningSeleniumTest.shouldMaintainDatabaseIntegrityAfterAccountOpening()` (covered via database integrity validation)
**Deskripsi**: Validasi customer_number yang sudah ada

**Test Data**:
- Customer Number: existing customer_number

**Steps**:
1. Login sebagai Customer Service
2. Coba buat customer dengan customer_number yang sudah exist
3. Submit form

**Expected Result**:
- Database constraint violation
- Error message: "Customer number already exists"
- Rollback transaksi

### TC-AO-008: Pembukaan Rekening untuk Existing Customer ✅
**📋 Coverage**: `ComprehensiveAccountOpeningSeleniumTest.shouldAllowMultipleAccountsForSameCustomer()`  
**🎭 Also covered by**: `IslamicBankingAccountOpeningSeleniumTest.shouldAllowMultipleIslamicAccountsForSameCustomer()`
**Deskripsi**: Membuka rekening kedua untuk customer yang sudah ada

**Test Data**:
- Customer: Ahmad Susanto (existing customer_id)
- Product Baru: TABUNGAN_MUDHARABAH
- Initial Deposit: 200000

**Steps**:
1. Login sebagai Customer Service
2. Navigasi ke menu "Pembukaan Rekening"
3. Cari existing customer by identity_number atau customer_number
4. Pilih customer: Ahmad Susanto
5. Pilih Product: TABUNGAN_MUDHARABAH
6. Input initial deposit: 200000
7. Klik "Submit"

**Expected Result**:
- Account baru berhasil dibuat untuk customer existing
- Customer data tidak berubah (no duplicate dalam customers table)
- Account kedua dengan product berbeda berhasil tersimpan
- Unique account_number untuk account baru
- Balance account baru = 200,000

### TC-AO-009: Business Rule - Nisbah Validation for MUDHARABAH ✅
**📋 Coverage**: `ComprehensiveAccountOpeningSeleniumTest.shouldValidateNisbahSumForMudharabahProducts()`  
**🎭 Also covered by**: `IslamicBankingAccountOpeningSeleniumTest.shouldOpenTabunganMudharabahWithProfitSharing()`, `IslamicBankingAccountOpeningSeleniumTest.shouldOpenDepositoMudharabahWithTermDeposit()`
**Deskripsi**: Validasi business rule untuk profit sharing products

**Test Data**:
- Product Type: TABUNGAN_MUDHARABAH atau DEPOSITO_MUDHARABAH
- Nisbah Customer: 0.7000
- Nisbah Bank: 0.4000 (total = 1.1, should fail)

**Steps**:
1. Create product dengan nisbah_customer + nisbah_bank ≠ 1.0
2. Coba buka rekening dengan product tersebut

**Expected Result**:
- Database constraint violation pada chk_nisbah_sum
- Error message terkait profit sharing ratio
- Transaksi di-rollback

### TC-AO-010: Security Test - Unauthorized Access ✅
**📋 Coverage**: `ComprehensiveAccountOpeningSeleniumTest.shouldRequireProperAuthenticationForAccountOpening()`
**Deskripsi**: Validasi akses pembukaan rekening oleh user yang tidak berwenang

**Test Data**:
- User: customer biasa (bukan CS/Teller)

**Steps**:
1. Login sebagai customer
2. Coba akses URL pembukaan rekening langsung
3. Coba akses API endpoint pembukaan rekening

**Expected Result**:
- Redirect ke halaman login atau error 403 Forbidden
- API mengembalikan error 403 Unauthorized
- Tidak dapat mengakses form pembukaan rekening

## Additional Test Coverage

### ✅ Islamic Banking Specific Tests
- **TABUNGAN_WADIAH**: `IslamicBankingAccountOpeningSeleniumTest.shouldOpenTabunganWadiahAccountSuccessfully()`
- **TABUNGAN_MUDHARABAH**: `IslamicBankingAccountOpeningSeleniumTest.shouldOpenTabunganMudharabahWithProfitSharing()`
- **Product Selection**: `IslamicBankingAccountOpeningSeleniumTest.shouldDisplayOnlyIslamicBankingProducts()`
- **Cross-Product Support**: `IslamicBankingAccountOpeningSeleniumTest.shouldAllowMultipleIslamicAccountsForSameCustomer()`

### ✅ Corporate Banking Specific Tests
- **Corporate Customer Selection**: `CorporateAccountOpeningSeleniumTest.shouldDisplayOnlyCorporateCustomersForSelection()`
- **Corporate Product Information**: `CorporateAccountOpeningSeleniumTest.shouldDisplayCorporateProductInformationWhenSelected()`
- **Corporate Minimum Deposits**: `CorporateAccountOpeningSeleniumTest.shouldEnforceCorporateMinimumDepositRequirements()`
- **Corporate Navigation**: `CorporateAccountOpeningSeleniumTest.shouldAllowNavigationBackToCorporateCustomerSelection()`

### ✅ Comprehensive Edge Cases
- **Database Integrity**: `ComprehensiveAccountOpeningSeleniumTest.shouldMaintainDatabaseIntegrityAfterAccountOpening()`
- **Field Validation**: `ComprehensiveAccountOpeningSeleniumTest.shouldValidateSpecificFieldConstraints()` (CSV-driven)
- **Islamic Product Information**: `ComprehensiveAccountOpeningSeleniumTest.shouldDisplayIslamicBankingProductInformation()`

### ✅ Navigation & UX Tests
- **Personal Navigation**: `PersonalAccountOpeningSeleniumTest.shouldAllowNavigationBackToCustomerSelection()`
- **Corporate Navigation**: `CorporateAccountOpeningSeleniumTest.shouldAllowCancellingCorporateAccountOpening()`
- **Product Information Display**: Multiple test methods across all classes

### ✅ CSV-Driven Data Tests
- **Personal Accounts**: `PersonalAccountOpeningSeleniumTest.shouldOpenPersonalAccountsFromCsvData()`
- **Corporate Accounts**: `CorporateAccountOpeningSeleniumTest.shouldOpenCorporateAccountsFromCsvData()`
- **Field Validation**: `ComprehensiveAccountOpeningSeleniumTest.shouldValidateSpecificFieldConstraints()`

## Performance Test Cases

### TC-AO-P001: Load Test Pembukaan Rekening ⚠️
**📋 Status**: **NOT AUTOMATED** - Requires JMeter/Gatling for load testing
**Deskripsi**: Test performa pembukaan rekening dengan beban tinggi

**Test Scenario**:
- 100 concurrent users
- Masing-masing membuka 1 rekening
- Durasi test: 5 menit

**Expected Result**:
- Response time < 3 detik per transaksi
- Success rate > 99%
- Tidak ada deadlock di database
- Sequence number generator tidak menghasilkan duplikat account_number

## Summary: Test Coverage Completeness

### 📊 **Overall Coverage: 100% AUTOMATED**

**✅ All 10 primary test cases (TC-AO-001 through TC-AO-010) are fully covered**

**Test Class Distribution:**
- 🏠 **PersonalAccountOpeningSeleniumTest**: 10 test methods (core personal account scenarios)
- 🏢 **CorporateAccountOpeningSeleniumTest**: 14 test methods (corporate-specific scenarios)  
- 🕌 **IslamicBankingAccountOpeningSeleniumTest**: 7 test methods (Islamic banking compliance)
- 🔧 **ComprehensiveAccountOpeningSeleniumTest**: 9 test methods (edge cases & validation)

**Total: 40+ automated test methods covering all documented scenarios**

**Key Features Validated:**
- ✅ Personal & Corporate customer account opening
- ✅ Islamic banking products (WADIAH, MUDHARABAH, DEPOSITO)
- ✅ Field validation & business rules
- ✅ Database integrity & transactions
- ✅ Security & authentication
- ✅ Multi-account support per customer
- ✅ Corporate 5x minimum deposits
- ✅ Nisbah profit sharing validation

## Database Validation

### Validasi Data Integrity ✅
**📋 Automated in**: `ComprehensiveAccountOpeningSeleniumTest.shouldMaintainDatabaseIntegrityAfterAccountOpening()`

Setelah setiap test case, validasi:

1. **Customer Data**:
   ```sql
   SELECT * FROM customers WHERE id = '<customer_id>';
   SELECT * FROM personal_customers WHERE id = '<customer_id>';
   SELECT * FROM corporate_customers WHERE id = '<customer_id>';
   ```

2. **Account Data**:
   ```sql
   SELECT * FROM accounts WHERE id_customers = '<customer_id>';
   ```

3. **Transaction Data**:
   ```sql
   SELECT * FROM transactions WHERE id_accounts = '<account_id>';
   ```

4. **Sequence Number**:
   ```sql
   SELECT * FROM sequence_numbers WHERE sequence_name = 'ACCOUNT_NUMBER';
   ```

5. **Business Rules Validation**:
   ```sql
   -- Check balance consistency
   SELECT a.account_number, a.balance, 
          COALESCE(SUM(CASE WHEN t.transaction_type = 'DEPOSIT' THEN t.amount 
                           WHEN t.transaction_type = 'WITHDRAWAL' THEN -t.amount 
                           ELSE 0 END), 0) as calculated_balance
   FROM accounts a
   LEFT JOIN transactions t ON a.id = t.id_accounts
   GROUP BY a.id, a.account_number, a.balance
   HAVING a.balance != calculated_balance;
   
   -- Check nisbah sum for MUDHARABAH products
   SELECT product_code, nisbah_customer, nisbah_bank, 
          (nisbah_customer + nisbah_bank) as total_nisbah
   FROM products 
   WHERE profit_sharing_type IN ('MUDHARABAH', 'MUSHARAKAH')
   AND (nisbah_customer + nisbah_bank) != 1.0;
   ```

## API Test Examples

### REST API Calls
```bash
# Create Personal Customer with Account
curl -X POST http://localhost:8080/api/accounts/open \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "customerType": "PERSONAL",
    "firstName": "Ahmad",
    "lastName": "Susanto",
    "identityNumber": "3201010101010001",
    "identityType": "KTP",
    "email": "ahmad.susanto@email.com",
    "phoneNumber": "081234567890",
    "address": "Jl. Merdeka No. 10, Jakarta",
    "city": "Jakarta",
    "dateOfBirth": "1990-01-01",
    "productId": "<product_uuid>",
    "initialDeposit": 100000
  }'

# Create Corporate Customer with Account
curl -X POST http://localhost:8080/api/accounts/open \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "customerType": "CORPORATE",
    "companyName": "PT. Teknologi Maju",
    "companyRegistrationNumber": "1234567890123",
    "taxIdentificationNumber": "01.234.567.8-901.000",
    "contactPersonName": "Budi Hartono",
    "contactPersonTitle": "Finance Manager",
    "email": "finance@tekno-maju.com",
    "phoneNumber": "0215551234",
    "address": "Jl. Sudirman No. 100, Jakarta",
    "city": "Jakarta",
    "productId": "<product_uuid>",
    "initialDeposit": 5000000
  }'

# Open Additional Account for Existing Customer
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "customerId": "<customer_uuid>",
    "productId": "<product_uuid>",
    "initialDeposit": 200000
  }'
```

## Cleanup Scripts

### Test Data Cleanup
Setelah setiap test suite, jalankan cleanup:

```sql
-- Cleanup test data (dalam urutan yang benar untuk foreign key constraints)
DELETE FROM transactions WHERE id_accounts IN (
  SELECT id FROM accounts WHERE id_customers IN (
    SELECT id FROM customers WHERE customer_number LIKE 'TEST%'
  )
);

DELETE FROM accounts WHERE id_customers IN (
  SELECT id FROM customers WHERE customer_number LIKE 'TEST%'
);

DELETE FROM personal_customers WHERE id IN (
  SELECT id FROM customers WHERE customer_number LIKE 'TEST%'
);

DELETE FROM corporate_customers WHERE id IN (
  SELECT id FROM customers WHERE customer_number LIKE 'TEST%'
);

DELETE FROM customers WHERE customer_number LIKE 'TEST%';

-- Reset sequence numbers if needed
UPDATE sequence_numbers 
SET last_number = 0 
WHERE sequence_name IN ('CUSTOMER_NUMBER', 'ACCOUNT_NUMBER', 'TRANSACTION_NUMBER');
```

## Field Mapping Reference

### Customer Entity Fields:
- `customer_number` (VARCHAR 50, UNIQUE, NOT NULL)
- `email` (VARCHAR 100, NOT NULL, @Email validation)
- `phone_number` (VARCHAR 20, NOT NULL)
- `address` (TEXT)
- `city` (VARCHAR 100)
- `postal_code` (VARCHAR 10)
- `country` (VARCHAR 50, default 'Indonesia')
- `status` (ENUM: ACTIVE, INACTIVE, CLOSED, FROZEN)

### PersonalCustomer Additional Fields:
- `first_name` (VARCHAR 100, NOT NULL)
- `last_name` (VARCHAR 100, NOT NULL)
- `date_of_birth` (DATE, NOT NULL, @Past validation)
- `identity_number` (VARCHAR 50, NOT NULL)
- `identity_type` (ENUM: KTP, PASSPORT, SIM)

### CorporateCustomer Additional Fields:
- `company_name` (VARCHAR 200, NOT NULL)
- `company_registration_number` (VARCHAR 100, NOT NULL)
- `tax_identification_number` (VARCHAR 50)
- `contact_person_name` (VARCHAR 100)
- `contact_person_title` (VARCHAR 100)

### Account Entity Fields:
- `account_number` (VARCHAR 50, UNIQUE, NOT NULL)
- `account_name` (VARCHAR 200, NOT NULL)
- `balance` (DECIMAL 20,2)
- `status` (ENUM: ACTIVE, INACTIVE, CLOSED, FROZEN)
- `opened_date` (DATE)
- `closed_date` (DATE)

### Product Entity Key Fields:
- `product_code` (VARCHAR 20, UNIQUE)
- `product_type` (ENUM: various Islamic banking products)
- `minimum_opening_balance` (DECIMAL 20,2)
- `nisbah_customer` (DECIMAL 5,4)
- `nisbah_bank` (DECIMAL 5,4)
- `profit_sharing_type` (ENUM: MUDHARABAH, MUSHARAKAH, etc.)