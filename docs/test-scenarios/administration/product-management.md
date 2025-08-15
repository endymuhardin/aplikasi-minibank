# Test Scenarios: Product Management

## Overview
Dokumen ini berisi skenario test untuk fitur product management dalam aplikasi minibank Islam. Product management mencakup CRUD operations untuk Islamic banking products, validation business rules, dan configuration management.

## Preconditions
- Database PostgreSQL berjalan
- Aplikasi Spring Boot aktif
- User sudah login sebagai Branch Manager atau System Administrator
- Islamic banking products tersedia sesuai seed data
- RBAC permissions configured: PRODUCT_VIEW, PRODUCT_CREATE, PRODUCT_UPDATE

## Existing Seed Data Reference

### Islamic Banking Products (V002__insert_initial_data.sql)
```sql
-- Available Products dari seed data:
TAB001: Tabungan Wadiah Basic (minimum: 50,000)
TAB002: Tabungan Mudharabah Premium (minimum: 1,000,000, nisbah: 70:30)
DEP001: Deposito Mudharabah (minimum: 100,000, nisbah: 70:30)
PEM001: Pembiayaan Murabahah (corporate only, minimum: 5,000,000)
PEM002: Pembiayaan Musharakah (minimum: 2,000,000, nisbah: 60:40)
```

### Test Data dari CSV Fixtures
```csv
# src/test/resources/fixtures/product/product-creation-data.csv
TAB001,Tabungan Wadiah Basic,TABUNGAN_WADIAH,Tabungan Syariah,Basic Islamic savings account,0.0125,true,false,100000,50000
TAB002,Tabungan Mudharabah Premium,TABUNGAN_MUDHARABAH,Tabungan Syariah,Premium savings with profit sharing,0.025,true,false,1000000,500000
CHK001,Basic Checking,CHECKING,Personal Banking,Basic checking account,0.0,true,true,50000,25000
DEP001,Deposito Mudharabah,DEPOSITO_MUDHARABAH,Deposito Syariah,Islamic time deposit,0.045,true,false,10000000,10000000
```

## Test Cases

### TC-PM-001: View Product List - Happy Path
**Deskripsi**: Menampilkan daftar semua Islamic banking products dengan paging

**Test Data**:
- User: Branch Manager (has PRODUCT_VIEW permission)
- Expected Products: 5 products dari seed data

**Steps** (sesuai ProductManagementSeleniumTest):
1. Login sebagai Branch Manager
2. Navigasi ke Product List page (/product/list)
3. Verify product list loaded dengan pagination
4. Check table headers: Code, Name, Type, Category, Status
5. Verify product data displayed correctly

**Expected Result**:
- Page title: "Product Management"
- Product table contains 5 rows minimum
- Products sorted by product_code
- Status indicators: Active/Inactive badges
- Pagination controls visible (if > 10 products)
- Each row shows: product_code, product_name, product_type, is_active
- Action buttons: View, Edit, Deactivate

### TC-PM-002: Search Products by Type
**Deskripsi**: Filter products berdasarkan product type

**Test Data**:
- Search Filter: "TABUNGAN_WADIAH"
- Expected Results: TAB001 only

**Steps**:
1. Login sebagai Branch Manager
2. Navigate to Product List page
3. Select product type filter: "TABUNGAN_WADIAH"
4. Click Apply Filter button
5. Verify filtered results

**Expected Result**:
- Filter applied successfully
- Only TABUNGAN_WADIAH products displayed
- TAB001 "Tabungan Wadiah Basic" shown
- Other product types hidden
- Clear filter button available

### TC-PM-003: Search Products by Category
**Deskripsi**: Filter products berdasarkan product category

**Test Data**:
- Search Filter: "Tabungan Syariah"
- Expected Results: TAB001, TAB002

**Steps**:
1. Login sebagai Branch Manager
2. Navigate to Product List page
3. Input search term: "Tabungan Syariah"
4. Press Enter or click Search button
5. Verify search results

**Expected Result**:
- Search executed successfully
- Products with category "Tabungan Syariah" displayed
- TAB001 and TAB002 both shown
- Pembiayaan products hidden
- Search term highlighted in results

### TC-PM-004: Create New Islamic Banking Product - Tabungan Wadiah
**Deskripsi**: Membuat produk Tabungan Wadiah baru dengan konfigurasi lengkap

**Test Data**:
- Product Code: TAB003 (unique, max 20 chars)
- Product Name: Tabungan Wadiah Gold (max 100 chars)
- Product Type: TABUNGAN_WADIAH
- Product Category: Tabungan Syariah (max 50 chars)
- Description: Premium Wadiah savings account with additional benefits
- Is Active: true
- Is Default: false
- Currency: IDR
- Minimum Opening Balance: 250000.00
- Minimum Balance: 100000.00
- Daily Withdrawal Limit: 2000000.00
- Monthly Transaction Limit: 100
- Profit Sharing Type: WADIAH
- Profit Distribution Frequency: MONTHLY
- Is Shariah Compliant: true
- Monthly Maintenance Fee: 5000.00
- ATM Withdrawal Fee: 2500.00
- Free Transactions Per Month: 15
- Allowed Customer Types: PERSONAL,CORPORATE

**Steps**:
1. Login sebagai Branch Manager
2. Navigate to Product List page
3. Click "Add New Product" button
4. Fill product form dengan data valid:
   - Product Code: TAB003
   - Product Name: Tabungan Wadiah Gold
   - Product Type: TABUNGAN_WADIAH
   - Product Category: Tabungan Syariah
   - Basic settings (currency, status, etc.)
   - Balance configurations
   - Fee configurations
   - Customer eligibility settings
5. Click "Save Product" button

**Expected Result**:
- Product created successfully
- Redirect to product list page
- Success notification displayed
- TAB003 appears in product list
- Product stored with all field values correct
- Database constraints validated (minimum_opening_balance >= 0)
- Created_by field = current user
- Created_date = current timestamp

### TC-PM-005: Create New Mudharabah Product with Profit Sharing
**Deskripsi**: Membuat produk DEPOSITO_MUDHARABAH dengan nisbah configuration

**Test Data**:
- Product Code: DEP002
- Product Name: Deposito Mudharabah Premium
- Product Type: DEPOSITO_MUDHARABAH
- Profit Sharing Type: MUDHARABAH
- Nisbah Customer: 0.7500 (75%)
- Nisbah Bank: 0.2500 (25%)
- Profit Distribution Frequency: ON_MATURITY
- Minimum Opening Balance: 50000000.00
- Shariah Board Approval Number: DSN-001/2024
- Shariah Board Approval Date: 2024-01-01

**Steps**:
1. Login sebagai Branch Manager
2. Navigate to Product Creation form
3. Fill mandatory fields:
   - Product Code: DEP002
   - Product Name: Deposito Mudharabah Premium
   - Product Type: DEPOSITO_MUDHARABAH
4. Configure profit sharing:
   - Profit Sharing Type: MUDHARABAH
   - Nisbah Customer: 0.7500
   - Nisbah Bank: 0.2500
   - Distribution Frequency: ON_MATURITY
5. Set Shariah compliance fields
6. Save product

**Expected Result**:
- Product created successfully
- Nisbah validation passes: 0.7500 + 0.2500 = 1.0000
- Database constraint chk_nisbah_sum satisfied
- Profit sharing configuration saved correctly
- Shariah compliance fields populated
- Product available for DEPOSITO_MUDHARABAH account opening

### TC-PM-006: Validation - Duplicate Product Code
**Deskripsi**: Validasi product code yang sudah ada

**Test Data**:
- Product Code: TAB001 (already exists in seed data)

**Steps**:
1. Login sebagai Branch Manager
2. Navigate to Product Creation form
3. Input product code: TAB001
4. Fill other required fields
5. Submit form

**Expected Result**:
- Form validation error
- Error message: "Product code already exists"
- Form tidak ter-submit
- Product code field highlighted
- Database unique constraint prevents duplicate
- User redirected back to form with error

### TC-PM-007: Validation - Invalid Nisbah Ratio for Mudharabah
**Deskripsi**: Validasi business rule untuk profit sharing products

**Test Data**:
- Product Type: TABUNGAN_MUDHARABAH
- Profit Sharing Type: MUDHARABAH
- Nisbah Customer: 0.8000
- Nisbah Bank: 0.3000 (total = 1.1000, should fail)

**Steps**:
1. Login sebagai Branch Manager
2. Create new MUDHARABAH product
3. Set nisbah_customer: 0.8000
4. Set nisbah_bank: 0.3000
5. Submit form

**Expected Result**:
- Database constraint violation: chk_nisbah_sum
- Error message: "Nisbah customer and bank must sum to 1.0 for Mudharabah products"
- Transaction rollback
- Form remains with validation errors
- User can correct nisbah values

### TC-PM-008: Validation - Field Length Limits
**Deskripsi**: Validasi batas panjang field sesuai database schema

**Test Data**:
- Product Code: "A".repeat(21) (exceeds 20 chars limit)
- Product Name: "A".repeat(101) (exceeds 100 chars limit)
- Product Category: "A".repeat(51) (exceeds 50 chars limit)

**Steps**:
1. Login sebagai Branch Manager
2. Navigate to Product Creation form
3. Input data yang melebihi batas panjang
4. Submit form

**Expected Result**:
- Client-side validation errors:
  - "Product code must not exceed 20 characters"
  - "Product name must not exceed 100 characters"
  - "Product category must not exceed 50 characters"
- Server-side validation backup
- Bean validation annotations enforced (@Size)
- Form tidak ter-submit

### TC-PM-009: Update Existing Product Configuration
**Deskripsi**: Update konfigurasi produk existing dengan business rules

**Test Data**:
- Product: TAB001 (existing from seed data)
- New Minimum Opening Balance: 75000.00 (increased from 50000)
- New Monthly Maintenance Fee: 3000.00 (increased from 2500)
- New Daily Withdrawal Limit: 7500000.00

**Steps**:
1. Login sebagai Branch Manager
2. Navigate to Product List page
3. Click "Edit" button untuk TAB001
4. Update minimum opening balance: 75000.00
5. Update monthly maintenance fee: 3000.00
6. Update daily withdrawal limit: 7500000.00
7. Click "Update Product"

**Expected Result**:
- Product updated successfully
- Changes reflected in database
- Updated_by field = current user
- Updated_date = current timestamp
- Existing accounts tidak terpengaruh (grandfather clause)
- Product audit trail maintained
- Success notification displayed

### TC-PM-010: Deactivate Product
**Deskripsi**: Menonaktifkan produk tanpa menghapus dari database

**Test Data**:
- Product: TAB002 (existing active product)
- New Status: is_active = false

**Steps**:
1. Login sebagai Branch Manager
2. Navigate to Product List page
3. Find TAB002 product row
4. Click "Deactivate" button
5. Confirm deactivation in modal dialog

**Expected Result**:
- Product status changed to inactive (is_active = false)
- Product still visible in list with "Inactive" badge
- Product no longer available for new account opening
- Existing accounts with this product remain active
- Deactivation logged in audit trail
- Option to reactivate product available

### TC-PM-011: View Product Details
**Deskripsi**: Menampilkan detail lengkap product configuration

**Test Data**:
- Product: DEP001 (Deposito Mudharabah from seed data)

**Steps**:
1. Login sebagai Branch Manager
2. Navigate to Product List page
3. Click "View" button untuk DEP001
4. Review product detail page

**Expected Result**:
- Product detail page displayed
- All product fields shown in readable format
- Profit sharing configuration clearly displayed
- Nisbah ratios shown as percentages (70% / 30%)
- Fee structure table formatted
- Shariah compliance information
- Product usage statistics (number of accounts)
- Back to list navigation

### TC-PM-012: Security Test - Product Management Access Control
**Deskripsi**: Validasi akses product management berdasarkan role permissions

**Test Data**:
- User 1: Teller (has PRODUCT_VIEW only)
- User 2: Customer Service (has PRODUCT_VIEW only)
- User 3: Customer (no PRODUCT permissions)

**Steps**:
1. Login sebagai Teller
2. Navigate to Product List page (should work)
3. Try to access Product Creation form (should fail)
4. Logout and login sebagai Customer Service
5. Repeat access test
6. Logout and login sebagai Customer
7. Try to access any product management page

**Expected Result**:
- Teller: Can view products, cannot create/edit
- Customer Service: Can view products, cannot create/edit
- Customer: Cannot access product management at all
- Proper 403 Forbidden responses for unauthorized actions
- Navigation menu items hidden based on permissions
- API endpoints protected by @PreAuthorize annotations

### TC-PM-013: Islamic Banking Product Types Validation
**Deskripsi**: Validasi semua jenis produk Islamic banking

**Test Data**:
- Product Types: TABUNGAN_WADIAH, TABUNGAN_MUDHARABAH, DEPOSITO_MUDHARABAH, PEMBIAYAAN_MURABAHAH, PEMBIAYAAN_MUDHARABAH, PEMBIAYAAN_MUSHARAKAH, PEMBIAYAAN_IJARAH, PEMBIAYAAN_SALAM, PEMBIAYAAN_ISTISNA

**Steps**:
1. Login sebagai Branch Manager
2. Create products untuk each Islamic banking type
3. Verify type-specific validations
4. Check profit sharing configurations per type

**Expected Result**:
- All Islamic banking product types supported
- Type-specific business rules enforced:
  - WADIAH: No profit sharing (nisbah = NULL)
  - MUDHARABAH/MUSHARAKAH: Nisbah required and sum = 1.0
  - MURABAHAH: Fixed margin, no profit sharing
  - IJARAH: Lease-based, different fee structure
- Product type enum validation working
- Shariah compliance flags properly set

### TC-PM-014: Product Search and Filtering
**Deskripsi**: Comprehensive search dan filtering functionality

**Test Data**:
- Filter by Status: Active/Inactive
- Filter by Customer Type: PERSONAL/CORPORATE
- Search by Name: partial text search
- Sort by: Product Code, Name, Type

**Steps**:
1. Login sebagai Branch Manager
2. Navigate to Product List page
3. Test various filter combinations:
   - Active products only
   - Products for PERSONAL customers
   - Search "Mudharabah"
   - Sort by product name A-Z
4. Verify filter persistence across pagination

**Expected Result**:
- All filters work independently and in combination
- Search supports partial text matching
- Sorting works on all filterable columns
- Filter state maintained during pagination
- Clear all filters option available
- Result count displayed
- No products found message when appropriate

### TC-PM-015: Product Configuration Export/Import
**Deskripsi**: Export product configurations untuk backup dan import

**Test Data**:
- Export format: CSV/Excel
- All active products

**Steps**:
1. Login sebagai Branch Manager
2. Navigate to Product List page
3. Click "Export Products" button
4. Select export format: CSV
5. Download and verify file content
6. Modify exported data
7. Import modified data

**Expected Result**:
- Export generates valid CSV file
- All product fields included in export
- Currency amounts properly formatted
- Date fields in ISO format
- Import validates data integrity
- Import preview shows changes
- Import logs all modifications
- Rollback option available if import fails

## Performance Test Cases

### TC-PM-P001: Product List Loading Performance
**Deskripsi**: Test performa loading product list dengan large dataset

**Test Scenario**:
- 1000+ products in database
- Various filters applied
- Pagination with different page sizes

**Expected Result**:
- Page load time < 3 seconds
- Pagination responsive
- Search results < 1 second
- Memory usage reasonable
- Database query optimization effective

### TC-PM-P002: Concurrent Product Updates
**Deskripsi**: Test concurrent editing of same product

**Test Scenario**:
- 2 Branch Managers edit same product simultaneously
- Different fields modified
- Submit within 1 second of each other

**Expected Result**:
- Optimistic locking prevents data corruption
- Last writer wins or conflict resolution
- User notification of concurrent modification
- Data integrity maintained
- Audit trail shows both attempts

## Database Validation

### Product Data Integrity Checks
```sql
-- Verify product constraints
SELECT product_code, nisbah_customer, nisbah_bank,
       (nisbah_customer + nisbah_bank) as total_nisbah
FROM products 
WHERE profit_sharing_type IN ('MUDHARABAH', 'MUSHARAKAH')
AND (nisbah_customer + nisbah_bank) != 1.0;

-- Check fee validations
SELECT product_code, monthly_maintenance_fee, atm_withdrawal_fee
FROM products 
WHERE monthly_maintenance_fee < 0 OR atm_withdrawal_fee < 0;

-- Validate balance constraints
SELECT product_code, minimum_opening_balance, minimum_balance
FROM products 
WHERE minimum_opening_balance < 0 OR minimum_balance < 0
OR minimum_opening_balance < minimum_balance;

-- Check Islamic banking compliance
SELECT product_code, product_type, is_shariah_compliant,
       shariah_board_approval_number
FROM products 
WHERE product_type LIKE '%MUDHARABAH%' 
AND (is_shariah_compliant != true OR shariah_board_approval_number IS NULL);
```

## API Test Examples

### REST API Calls
```bash
# Get all products with pagination
curl -X GET "http://localhost:8080/api/products?page=0&size=10&sort=productCode" \
  -H "Authorization: Bearer <token>"

# Get products by type
curl -X GET "http://localhost:8080/api/products?type=TABUNGAN_MUDHARABAH" \
  -H "Authorization: Bearer <token>"

# Create new product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "productCode": "TAB003",
    "productName": "Tabungan Wadiah Gold",
    "productType": "TABUNGAN_WADIAH",
    "productCategory": "Tabungan Syariah",
    "description": "Premium Wadiah savings account",
    "isActive": true,
    "minimumOpeningBalance": 250000.00,
    "minimumBalance": 100000.00,
    "profitSharingType": "WADIAH",
    "isShariahCompliant": true
  }'

# Update product
curl -X PUT http://localhost:8080/api/products/TAB001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "minimumOpeningBalance": 75000.00,
    "monthlyMaintenanceFee": 3000.00
  }'

# Deactivate product
curl -X PATCH http://localhost:8080/api/products/TAB002/deactivate \
  -H "Authorization: Bearer <token>"

# Get product usage statistics
curl -X GET http://localhost:8080/api/products/TAB001/usage \
  -H "Authorization: Bearer <token>"
```

## Integration with Account Opening

### TC-PM-I001: Product Configuration Impact on Account Opening
**Deskripsi**: Test integration antara product configuration dan account opening

**Steps**:
1. Create new product dengan minimum opening balance 1,000,000
2. Try to open account dengan initial deposit 500,000
3. Verify validation error
4. Update product configuration
5. Retry account opening

**Expected Result**:
- Account opening validates against current product configuration
- Real-time product data used in validation
- Product changes immediately affect new account openings
- Existing accounts unaffected by product changes

## Cleanup Scripts

### Test Data Cleanup
```sql
-- Cleanup test products
DELETE FROM accounts WHERE id_products IN (
  SELECT id FROM products WHERE product_code LIKE 'TEST%'
);

DELETE FROM products WHERE product_code LIKE 'TEST%';

-- Reset product sequence
UPDATE sequence_numbers 
SET last_number = (
  SELECT COUNT(*) FROM products
) 
WHERE sequence_name = 'PRODUCT_NUMBER';

-- Verify data integrity after cleanup
SELECT p.product_code, COUNT(a.id) as account_count
FROM products p
LEFT JOIN accounts a ON p.id = a.id_products
GROUP BY p.id, p.product_code
ORDER BY p.product_code;
```

## Selenium Test Integration

### Page Object Model (sesuai ProductManagementSeleniumTest)
```java
// ProductListPage elements
@FindBy(id = "product-table")
WebElement productTable;

@FindBy(className = "add-product-btn")
WebElement addProductButton;

@FindBy(id = "product-type-filter")
WebElement productTypeFilter;

// ProductFormPage elements
@FindBy(id = "product-code")
WebElement productCodeInput;

@FindBy(id = "product-name")
WebElement productNameInput;

@FindBy(id = "product-type")
WebElement productTypeSelect;

@FindBy(id = "nisbah-customer")
WebElement nisbahCustomerInput;
```

### Test Data Integration
Tests should use data from:
- **Seed data**: TAB001, TAB002, DEP001, PEM001, PEM002
- **CSV fixtures**: product-creation-data.csv, product-validation-data.csv
- **Dynamic test data**: Generated product codes dengan "TEST" prefix

### Authorization Integration
- **Customer Service**: PRODUCT_VIEW only (read operations)
- **Branch Manager**: Full PRODUCT permissions
- **Teller**: PRODUCT_VIEW for transaction processing
- **Customer**: No PRODUCT access