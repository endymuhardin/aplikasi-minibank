# Test Scenarios: Account Lifecycle Management

## Overview
Dokumen ini berisi skenario test untuk complete account lifecycle management dalam aplikasi minibank Islam. Account lifecycle mencakup pembukaan, maintenance, status changes, dan penutupan rekening dengan business rules yang sesuai.

## Implementation Status
**✅ FULLY IMPLEMENTED**: Complete account lifecycle management
- **✅ Account Opening**: Web UI + REST API with comprehensive validation
- **✅ Account Status Management**: ACTIVE/INACTIVE/CLOSED/FROZEN status transitions
- **✅ Account Closure Workflow**: closeAccountForm() and closeAccount() methods in AccountController
- **✅ Business Logic**: Account.closeAccount() entity method with balance validation
- **✅ Test Coverage**: AccountClosureEssentialTest with comprehensive scenarios

## Preconditions
- Database PostgreSQL berjalan
- Aplikasi Spring Boot aktif
- User sudah login dengan appropriate role permissions
- Customer data tersedia (personal atau corporate)
- Islamic banking products configured
- Sequence number service functional

## Existing Implementation Reference

### Entity Classes
```java
// Account.java - Core account entity dengan business methods
public class Account {
    private AccountStatus status = AccountStatus.ACTIVE;
    public void deposit(BigDecimal amount) { ... }
    public void withdraw(BigDecimal amount) { ... }
    public boolean isActive() { return AccountStatus.ACTIVE.equals(this.status); }
}

// Account status enumeration
public enum AccountStatus {
    ACTIVE, INACTIVE, CLOSED, FROZEN
}
```

### REST API Integration
```java
// AccountRestController.java - Account opening API
@PostMapping("/api/accounts")
public ResponseEntity<Object> openAccount(@Valid @RequestBody AccountOpeningRequest request)
```

## Test Cases

### TC-AL-001: Complete Account Opening Workflow
**Deskripsi**: End-to-end account opening process dengan all validations

**Test Data**:
- Customer: Ahmad Suharto (existing customer C1000001)
- Product: TABUNGAN_WADIAH (TAB001)
- Initial Deposit: 100000.00
- Account Name: "Ahmad Suharto - Tabungan Utama"
- Opening Channel: CUSTOMER_SERVICE
- Created By: cs1

**Steps**:
1. Login sebagai Customer Service (cs1)
2. Navigate to Account Opening form
3. Search dan select existing customer: Ahmad Suharto
4. Select product: TABUNGAN_WADIAH (TAB001)
5. Input initial deposit: 100000.00
6. Input account name: "Ahmad Suharto - Tabungan Utama"
7. Review account opening summary
8. Submit account opening request

**Expected Result**:
- **Account Creation**:
  - Account number auto-generated: A2000xxx
  - Account status: ACTIVE
  - Balance: 100000.00
  - Opened date: current date
- **Initial Transaction**:
  - Transaction type: DEPOSIT
  - Amount: 100000.00
  - Description: "Initial deposit for account opening"
  - Balance after: 100000.00
- **Sequence Updates**:
  - Account number sequence incremented
  - Transaction number sequence incremented
- **Business Rules Validated**:
  - Initial deposit >= minimum opening balance
  - Customer eligibility for product
  - Product active and available
- **Audit Trail**:
  - Account created_by: cs1
  - Account created_date: current timestamp
  - Opening transaction logged

### TC-AL-002: Account Opening dengan Multiple Products
**Deskripsi**: Customer opening multiple accounts dengan different products

**Test Data**:
- Existing Customer: Siti Nurhaliza (C1000002)
- Account 1: TABUNGAN_WADIAH (initial: 150000.00)
- Account 2: DEPOSITO_MUDHARABAH (initial: 10000000.00)
- Same day opening

**Steps**:
1. Login sebagai Customer Service
2. Open first account: TABUNGAN_WADIAH
3. Complete first account opening
4. Immediately open second account: DEPOSITO_MUDHARABAH
5. Complete second account opening

**Expected Result**:
- **Two Active Accounts** untuk same customer:
  - Account 1: TABUNGAN_WADIAH dengan balance 150000.00
  - Account 2: DEPOSITO_MUDHARABAH dengan balance 10000000.00
- **Unique Account Numbers**: Different auto-generated numbers
- **Independent Balances**: Each account maintains separate balance
- **Product-Specific Rules**: Each account follows its product configuration
- **Customer Relationship**: Both accounts linked to same customer
- **Transaction History**: Separate transaction histories per account

### TC-AL-003: Account Status Change - Activate/Deactivate
**Deskripsi**: Changing account status dengan business impact validation

**Test Data**:
- Account: ACC0000001 (current status: ACTIVE)
- New Status: INACTIVE
- Reason: "Customer request - temporary suspension"
- Authorized By: manager1

**Steps**:
1. Login sebagai Branch Manager (manager1)
2. Navigate to Account Management
3. Search account: ACC0000001
4. Click "Change Status" → "Deactivate"
5. Input reason: "Customer request - temporary suspension"
6. Confirm status change

**Expected Result**:
- **Account Status Updated**:
  - Status changed: ACTIVE → INACTIVE
  - Updated_by: manager1
  - Updated_date: current timestamp
- **Business Impact**:
  - No new transactions allowed (deposits/withdrawals blocked)
  - Existing balance preserved
  - Account still visible dalam customer account list
  - Status change logged dalam audit trail
- **Transaction Validation**:
  - Existing transactions remain valid
  - New transaction attempts return error: "Account is not active"
- **Reactivation Available**:
  - Account can be reactivated by authorized users
  - Status change reversible with proper authorization

### TC-AL-004: Account Freezing - Security Hold
**Deskripsi**: Freezing account due to security concerns atau compliance

**Test Data**:
- Account: ACC0000002 (status: ACTIVE, balance: 5000000.00)
- Freeze Reason: "Suspicious transaction pattern detected"
- Freeze Type: SECURITY_HOLD
- Authorized By: manager1
- Notification Required: Yes

**Steps**:
1. Login sebagai Branch Manager
2. Search account: ACC0000002
3. Click "Freeze Account"
4. Select freeze type: SECURITY_HOLD
5. Input reason: "Suspicious transaction pattern detected"
6. Enable customer notification
7. Confirm freeze action

**Expected Result**:
- **Account Status**: ACTIVE → FROZEN
- **Transaction Restrictions**:
  - All transactions blocked (deposits, withdrawals, transfers)
  - Balance inquiry allowed untuk customer
  - Account statements can still be generated
- **Security Logging**:
  - Freeze reason recorded
  - Manager authorization logged
  - Timestamp and audit trail maintained
- **Customer Notification**:
  - Email/SMS notification sent to customer
  - Clear explanation of freeze reason
  - Instructions for account unfreeze process
- **Compliance**:
  - Regulatory reporting triggered (if required)
  - Compliance team notified

### TC-AL-005: Account Closure - Normal Process
**Deskripsi**: Proper account closure dengan balance settlement

**Test Data**:
- Account: ACC0000003 (balance: 250000.00)
- Closure Reason: "Customer relocation"
- Closure Type: CUSTOMER_REQUEST
- Balance Settlement: Transfer to another account
- Destination Account: ACC0000004 (same customer)

**Steps**:
1. Login sebagai Customer Service
2. Initiate account closure untuk ACC0000003
3. Verify no pending transactions
4. Input closure reason: "Customer relocation"
5. Select balance settlement: Transfer to ACC0000004
6. Generate account closure documents
7. Complete closure process

**Expected Result**:
- **Account Status**: ACTIVE → CLOSED
- **Balance Settlement**:
  - Remaining balance (250000.00) transferred to ACC0000004
  - Settlement transaction recorded dengan proper references
  - Account balance becomes 0.00
- **Account Closure**:
  - Closed_date: current date
  - Account no longer available untuk new transactions
  - Account history preserved untuk audit/compliance
- **Documentation**:
  - Account closure certificate generated
  - Customer acknowledgment recorded
  - Closure reason documented
- **Regulatory Compliance**:
  - Account closure reported to relevant authorities
  - Tax implications handled (if applicable)
  - Audit trail complete

### TC-AL-006: Account Closure - Zero Balance
**Deskripsi**: Account closure dengan zero balance

**Test Data**:
- Account: ACC0000005 (balance: 0.00)
- Closure Reason: "Inactive account cleanup"
- Closure Type: BANK_INITIATED
- Last Transaction: 6 months ago

**Steps**:
1. Login sebagai Branch Manager
2. Review inactive accounts report
3. Select ACC0000005 untuk closure
4. Verify zero balance dan no recent activity
5. Process bank-initiated closure

**Expected Result**:
- **Closure Validation**:
  - Zero balance confirmed
  - No pending transactions
  - Minimum balance requirements waived untuk closure
- **Account Status**: ACTIVE → CLOSED
- **Audit Requirements**:
  - Bank-initiated closure properly documented
  - Customer notification sent (if required)
  - Regulatory reporting completed
- **Data Retention**:
  - Account history maintained
  - Transaction records preserved
  - Customer relationship data retained

### TC-AL-007: Account Reactivation Process
**Deskripsi**: Reactivating inactive account dengan validation

**Test Data**:
- Account: ACC0000006 (status: INACTIVE)
- Inactivation Date: 30 days ago
- Reactivation Reason: "Customer returned from overseas"
- Required Verification: KYC update

**Steps**:
1. Login sebagai Customer Service
2. Customer request reactivation untuk ACC0000006
3. Verify customer identity (KYC)
4. Check account history dan closure reason
5. Update customer information if needed
6. Process account reactivation

**Expected Result**:
- **Reactivation Validation**:
  - Customer identity verified
  - KYC information updated
  - No compliance issues
- **Account Status**: INACTIVE → ACTIVE
- **Business Rules**:
  - Previous balance restored (if any)
  - Transaction capabilities restored
  - Product terms current dan applicable
- **Documentation**:
  - Reactivation reason recorded
  - KYC update logged
  - Customer acknowledgment obtained

### TC-AL-008: Validation - Account Closure dengan Outstanding Balance
**Deskripsi**: Preventing closure of account dengan remaining balance

**Test Data**:
- Account: ACC0000007 (balance: 1500000.00)
- Closure Attempt: Without balance settlement
- Outstanding Transactions: Pending deposit

**Steps**:
1. Login sebagai Customer Service
2. Attempt to close account ACC0000007
3. System check untuk outstanding balance
4. System check untuk pending transactions

**Expected Result**:
- **Closure Prevented**:
  - Error message: "Account closure not allowed with outstanding balance: Rp 1,500,000"
  - Pending transactions warning displayed
- **Required Actions**:
  - Balance must be transferred atau withdrawn
  - Pending transactions must be completed
  - Clear instructions provided to user
- **Process Guidance**:
  - Step-by-step closure process explained
  - Balance settlement options displayed
  - Required documentation listed

### TC-AL-009: Validation - Islamic Banking Product Compliance
**Deskripsi**: Account lifecycle compliance dengan Islamic banking principles

**Test Data**:
- Account: ACC0000008 (DEPOSITO_MUDHARABAH)
- Maturity Date: Not yet reached
- Profit Sharing: Mudharabah contract active
- Early Closure Request: Customer initiated

**Steps**:
1. Customer request early closure of Deposito Mudharabah
2. System check maturity date dan contract terms
3. Calculate profit sharing implications
4. Apply Islamic banking rules untuk early closure

**Expected Result**:
- **Islamic Banking Compliance**:
  - Shariah board guidelines followed
  - Profit sharing calculated according to Mudharabah contract
  - Early closure penalties (if any) compliant dengan Islamic principles
- **Contract Validation**:
  - Maturity date impact assessed
  - Profit distribution calculated
  - Customer consent obtained untuk early closure terms
- **Documentation**:
  - Islamic banking compliance certificate
  - Shariah advisor approval (if required)
  - Profit sharing statement

### TC-AL-010: Account Maintenance - Information Updates
**Deskripsi**: Updating account information dan customer details

**Test Data**:
- Account: ACC0000009
- Update Type: Contact information change
- New Email: new.email@example.com
- New Phone: 081987654321
- New Address: Updated address

**Steps**:
1. Login sebagai Customer Service
2. Navigate to Account Maintenance
3. Select account: ACC0000009
4. Update customer contact information
5. Verify identity before applying changes
6. Save updates

**Expected Result**:
- **Information Updated**:
  - Customer email updated
  - Phone number updated
  - Address information updated
- **Audit Trail**:
  - Changes logged dengan before/after values
  - Updated_by: user who made changes
  - Update timestamp recorded
- **Validation**:
  - Email format validation
  - Phone number format validation
  - Required field validation
- **Notification**:
  - Customer notified of information changes
  - Security notification sent to old email (if email changed)

### TC-AL-011: Account Linking - Family Banking
**Deskripsi**: Linking multiple accounts untuk family banking relationships

**Test Data**:
- Primary Customer: Ahmad Suharto (C1000001)
- Secondary Customer: Dewi Lestari (C1000006) - spouse
- Account 1: ACC0000001 (Ahmad - Tabungan)
- Account 2: ACC0000010 (Dewi - Tabungan)
- Relationship Type: SPOUSE

**Steps**:
1. Login sebagai Customer Service
2. Navigate to Account Linking
3. Select primary account: ACC0000001
4. Add linked account: ACC0000010
5. Define relationship: SPOUSE
6. Set access permissions
7. Complete linking process

**Expected Result**:
- **Account Relationship**:
  - Accounts linked dengan defined relationship
  - Cross-account visibility (with permissions)
  - Joint account features enabled (if applicable)
- **Access Control**:
  - Permission levels defined per relationship
  - Security restrictions maintained
  - Audit trail untuk cross-account access
- **Business Benefits**:
  - Family banking package eligibility
  - Combined statement options
  - Relationship-based product offerings

### TC-AL-012: Account Performance Monitoring
**Deskripsi**: Monitoring account performance dan activity patterns

**Test Data**:
- Account: ACC0000011 (TABUNGAN_MUDHARABAH)
- Monitoring Period: Last 6 months
- Transaction Volume: High activity
- Profit Sharing: Monthly calculations
- Performance Metrics: Various KPIs

**Steps**:
1. Login sebagai Branch Manager
2. Access Account Performance Dashboard
3. Select account: ACC0000011
4. Review performance metrics
5. Analyze transaction patterns
6. Generate performance report

**Expected Result**:
- **Performance Metrics**:
  - Transaction volume trends
  - Average balance patterns
  - Profit sharing performance (untuk Islamic products)
  - Fee generation analysis
- **Risk Assessment**:
  - Transaction pattern analysis
  - Compliance monitoring
  - Unusual activity detection
- **Reporting**:
  - Performance dashboard visualization
  - Trend analysis charts
  - Export capabilities untuk further analysis
- **Business Intelligence**:
  - Customer behavior insights
  - Product performance analysis
  - Risk indicators monitoring

## Performance Test Cases

### TC-AL-P001: High Volume Account Opening
**Deskripsi**: Test account opening performance dengan high volume

**Test Scenario**:
- 100 concurrent account openings
- Various customer types dan products
- Peak hour simulation
- Database performance monitoring

**Expected Result**:
- Account opening time < 5 seconds per request
- Success rate > 99%
- No duplicate account numbers generated
- Database locking mechanisms effective
- Sequence number generation reliable

### TC-AL-P002: Account Status Change Performance
**Deskripsi**: Test performance untuk mass account status changes

**Test Scenario**:
- 1000 accounts status change simultaneously
- Various status transitions
- Batch processing capabilities

**Expected Result**:
- Batch processing efficient
- No data corruption
- Audit trail complete untuk all changes
- Transaction restrictions applied immediately
- Performance monitoring shows acceptable metrics

## Security Test Cases

### TC-AL-S001: Account Access Authorization
**Deskripsi**: Validate account access based on user roles

**Test Data**:
- Customer Service: Can open/manage accounts
- Teller: Limited account access
- Customer: Can only view own accounts
- Manager: Full account management access

**Steps**:
1. Test account access dengan different user roles
2. Verify permission enforcement
3. Test unauthorized access attempts

**Expected Result**:
- Role-based access control enforced
- Unauthorized access blocked
- Audit logs record access attempts
- API endpoints protected appropriately

### TC-AL-S002: Account Information Privacy
**Deskripsi**: Ensure customer account information privacy

**Steps**:
1. Login as different users
2. Attempt to access other customers' accounts
3. Verify data isolation

**Expected Result**:
- Customers can only access own accounts
- Staff access limited by role permissions
- Cross-customer data leakage prevented
- Privacy compliance maintained

## Integration Test Cases

### TC-AL-I001: Account Integration dengan Product Configuration
**Deskripsi**: Validate account behavior based on product settings

**Steps**:
1. Create accounts dengan different products
2. Test product-specific business rules
3. Verify fee calculations dan limits
4. Test Islamic banking compliance

**Expected Result**:
- Product configurations properly applied
- Business rules enforced per product
- Fee structures working correctly
- Islamic banking compliance maintained

### TC-AL-I002: Account Integration dengan Transaction Processing
**Deskripsi**: End-to-end integration testing

**Steps**:
1. Open account
2. Process various transactions
3. Change account status
4. Test transaction restrictions
5. Close account

**Expected Result**:
- Complete lifecycle working smoothly
- Transaction processing respects account status
- Status changes affect transaction capabilities
- Data consistency maintained throughout

## Database Validation

### Account Lifecycle Data Integrity
```sql
-- Check account status consistency
SELECT status, COUNT(*) as count
FROM accounts
GROUP BY status
ORDER BY count DESC;

-- Verify account-customer relationships
SELECT c.customer_type, COUNT(a.id) as account_count
FROM customers c
LEFT JOIN accounts a ON c.id = a.id_customers
GROUP BY c.customer_type;

-- Check account closure compliance
SELECT a.account_number, a.status, a.balance, a.closed_date
FROM accounts a
WHERE a.status = 'CLOSED'
AND (a.balance != 0 OR a.closed_date IS NULL);

-- Validate sequence number integrity
SELECT sequence_name, last_number, 
       (SELECT COUNT(*) FROM accounts) as actual_count
FROM sequence_numbers 
WHERE sequence_name = 'ACCOUNT_NUMBER';

-- Check Islamic banking product compliance
SELECT p.product_type, p.is_shariah_compliant, COUNT(a.id) as account_count
FROM products p
LEFT JOIN accounts a ON p.id = a.id_products
WHERE p.product_type LIKE '%MUDHARABAH%' OR p.product_type LIKE '%WADIAH%'
GROUP BY p.product_type, p.is_shariah_compliant;
```

## API Test Examples

### REST API Calls
```bash
# Open new account
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "customerId": "uuid-customer-id",
    "productId": "uuid-product-id",
    "initialDeposit": 100000.00,
    "accountName": "Ahmad Suharto - Tabungan Utama"
  }'

# Get account details
curl -X GET http://localhost:8080/api/accounts/{accountId} \
  -H "Authorization: Bearer <token>"

# Update account status
curl -X PATCH http://localhost:8080/api/accounts/{accountId}/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "status": "INACTIVE",
    "reason": "Customer request",
    "authorizedBy": "manager1"
  }'

# Close account
curl -X POST http://localhost:8080/api/accounts/{accountId}/close \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "reason": "Customer relocation",
    "balanceSettlement": {
      "type": "TRANSFER",
      "destinationAccountId": "uuid-destination-account"
    }
  }'

# Get customer accounts
curl -X GET http://localhost:8080/api/customers/{customerId}/accounts \
  -H "Authorization: Bearer <token>"

# Account performance metrics
curl -X GET "http://localhost:8080/api/accounts/{accountId}/performance?period=6months" \
  -H "Authorization: Bearer <token>"
```

## Cleanup Scripts

### Test Data Cleanup
```sql
-- Cleanup test accounts dan related data
DELETE FROM transactions WHERE id_accounts IN (
  SELECT id FROM accounts WHERE account_number LIKE 'TEST%'
);

DELETE FROM accounts WHERE account_number LIKE 'TEST%';

-- Reset account sequence
UPDATE sequence_numbers 
SET last_number = (
  SELECT COALESCE(MAX(CAST(SUBSTRING(account_number FROM '[0-9]+') AS INTEGER)), 2000000)
  FROM accounts
) 
WHERE sequence_name = 'ACCOUNT_NUMBER';

-- Cleanup account closure records
DELETE FROM account_closures WHERE account_id IN (
  SELECT id FROM accounts WHERE account_number LIKE 'TEST%'
);

-- Reset customer account counts
UPDATE customers 
SET account_count = (
  SELECT COUNT(*) FROM accounts WHERE id_customers = customers.id
);
```

This comprehensive account lifecycle management test scenario covers all major aspects of account management dalam Islamic banking system, dari opening hingga closure, dengan proper validation, security, dan compliance considerations.