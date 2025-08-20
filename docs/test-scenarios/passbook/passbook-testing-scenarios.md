# Passbook Printing Test Scenarios

## Repository Test Scenarios

### Basic Query Tests
- **Scenario**: Find transactions by account with pagination
- **Given**: Account with multiple transactions
- **When**: Query with pagination parameters
- **Then**: Returns correct page of transactions

### Date Range Tests
- **Scenario**: Find transactions within date range
- **Given**: Account with transactions across multiple months
- **When**: Query with start and end dates
- **Then**: Returns only transactions within range

### Edge Case Scenarios

#### Empty Account Testing
- **Scenario**: Handle account with no transactions
- **Given**: New account with zero transactions
- **When**: Request passbook printing
- **Then**: Display appropriate empty state message

#### Large Dataset Performance
- **Scenario**: Handle account with 500+ transactions
- **Given**: Account with extensive transaction history
- **When**: Query for passbook data
- **Then**: Response time < 10 seconds, proper pagination

#### Date Boundary Testing
- **Scenario**: Transactions spanning year boundaries
- **Given**: Transactions from December to January
- **When**: Query across year boundary
- **Then**: Correct date filtering and ordering

## Controller Test Scenarios

### Account Selection Tests
- **Scenario**: Display account selection page
- **Given**: User with ACCOUNT_VIEW permission
- **When**: Access /passbook/select-account
- **Then**: Show active accounts only

### Preview Functionality Tests
- **Scenario**: Display passbook preview
- **Given**: Valid account ID
- **When**: Access /passbook/preview/{accountId}
- **Then**: Show recent 10 transactions with bank details

### Print Functionality Tests
- **Scenario**: Generate print-ready document
- **Given**: Account with transactions
- **When**: Access /passbook/print/{accountId}
- **Then**: Display print-optimized format with all transactions

### Security Test Scenarios
- **Scenario**: Enforce permission requirements
- **Given**: User without ACCOUNT_VIEW permission
- **When**: Attempt to access passbook features
- **Then**: Access denied with appropriate error

## Performance Test Scenarios

### Small Dataset (1-50 transactions)
- **Expected Performance**: < 1 second
- **Memory Usage**: Minimal
- **Pagination**: Not required

### Medium Dataset (51-500 transactions)
- **Expected Performance**: < 5 seconds
- **Memory Usage**: Moderate
- **Pagination**: Recommended

### Large Dataset (500+ transactions)
- **Expected Performance**: < 10 seconds
- **Memory Usage**: Controlled via pagination
- **Pagination**: Required

## Data Validation Scenarios

### Balance Integrity
- **Scenario**: Verify running balance calculations
- **Given**: Sequence of transactions
- **When**: Display in passbook
- **Then**: Each balance equals previous + transaction amount

### Transaction Ordering
- **Scenario**: Verify chronological order
- **Given**: Mixed transaction dates
- **When**: Query for passbook
- **Then**: Transactions ordered by date ascending

## User Role Test Scenarios

| Role | Account Selection | Preview | Print | Expected Result |
|------|------------------|---------|-------|-----------------|
| Teller | ✅ | ✅ | ✅ | Full Access |
| Customer Service | ✅ | ✅ | ✅ | Full Access |
| Branch Manager | ✅ | ✅ | ✅ | Full Access |
| Unauthorized User | ❌ | ❌ | ❌ | Access Denied |

## Error Handling Scenarios

### Invalid Account ID
- **Scenario**: Non-existent account
- **Given**: Invalid account identifier
- **When**: Request passbook data
- **Then**: Return 404 with error message

### Inactive Account
- **Scenario**: Closed or frozen account
- **Given**: Account with INACTIVE status
- **When**: Attempt passbook printing
- **Then**: Block operation with status message

### Invalid Date Format
- **Scenario**: Malformed date parameters
- **Given**: Invalid date string
- **When**: Apply date filter
- **Then**: Validation error with format guidance

## Browser Compatibility Scenarios

### Chrome Print Test
- **Scenario**: Print functionality in Chrome
- **Given**: Passbook print page
- **When**: Use browser print function
- **Then**: Correct formatting and layout

### Firefox Print Test
- **Scenario**: Print functionality in Firefox
- **Given**: Passbook print page
- **When**: Use browser print function
- **Then**: Correct formatting and layout

### Print Settings Validation
- **Required Settings**: A4 paper, 0.5" margins, background graphics enabled
- **Optional Settings**: Headers/footers disabled

## Test Data Requirements

### Sample Transaction Types
- Account opening deposits
- Regular cash deposits
- ATM withdrawals
- Bill payments
- Transfer transactions

### Date Coverage
- Same day multiple transactions
- Weekly patterns
- Monthly patterns
- Year-end boundary transactions

### Amount Variations
- Small amounts (< 100,000 IDR)
- Medium amounts (100,000 - 1,000,000 IDR)
- Large amounts (> 1,000,000 IDR)

## Automated Test Execution

### Repository Tests
```bash
mvn test -Dtest="TransactionRepositoryTest"
mvn test -Dtest="PassbookRepositoryEdgeCaseTest"
```

### Controller Tests
```bash
mvn test -Dtest="PassbookControllerTest"
```

### Performance Tests
```bash
mvn test -Dtest="PassbookRepositoryEdgeCaseTest#shouldHandleLargeTransactionVolumeEfficiently"
```

## Monitoring and Metrics

### Coverage Targets
- Repository methods: >95%
- Controller endpoints: >90%
- Edge cases: 100%
- Security scenarios: 100%

### Performance Benchmarks
- Database query optimization
- Memory usage monitoring
- Response time tracking
- Concurrent access testing