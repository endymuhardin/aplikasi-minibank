# Test Failure Resolution Todo List

Based on Surefire test report analysis, here are the prioritized tasks to fix test failures, categorized by difficulty and complexity.

## 🔴 **HIGH COMPLEXITY** (Infrastructure & Core Issues)

### 1. Fix Docker/TestContainer setup for Selenium chrome containers (Status 500 errors)
- **Priority**: Critical
- **Impact**: Root cause of 85% of Selenium test failures
- **Description**: `ContainerLaunchException` for selenium/standalone-chrome:4.31.0 preventing all web UI tests
- **Skills Required**: Docker/container networking, TestContainer configuration
- **Files Affected**: All Selenium test classes

### 2. Implement proper test data cleanup to prevent duplicate key constraint violations
- **Priority**: High
- **Impact**: Database state conflicts between test runs
- **Description**: `PSQLException: duplicate key value violates unique constraint "customers_pkey"`
- **Skills Required**: Spring Test configuration, database transaction management
- **Files Affected**: CashDepositSeleniumTest, CashWithdrawalSeleniumTest

### 3. Fix transaction state management error (cannot start new transaction)
- **Priority**: High
- **Impact**: Core Spring transaction management issue
- **Description**: `IllegalStateException: Cannot start new transaction without ending existing transaction`
- **Skills Required**: Spring transaction boundaries, @Transactional configuration
- **Files Affected**: RolePermissionRepositoryTest

## 🟡 **MEDIUM COMPLEXITY** (Configuration & Timing)

### 4. Review and adjust Selenium timeout settings for long-running operations
- **Priority**: Medium
- **Impact**: Test stability and reliability
- **Description**: `TimeoutException` after 60 seconds in various UI operations
- **Skills Required**: Selenium WebDriver configuration, performance tuning
- **Files Affected**: Permission management, user management workflows

### 5. Improve authentication reliability in Selenium test login helpers
- **Priority**: Medium
- **Impact**: Prevents functional tests from proceeding past authentication
- **Description**: `RuntimeException: Optimized login failed`, `RuntimeException: Login failed - not redirected to dashboard`
- **Skills Required**: Selenium page object patterns, authentication flow debugging
- **Files Affected**: Multiple Selenium test classes

### 6. Fix timing issues in account opening workflow tests (customer selection page)
- **Priority**: Medium
- **Impact**: Account opening end-to-end workflows
- **Description**: Race conditions between page loading and element detection
- **Skills Required**: Selenium synchronization, page object model
- **Files Affected**: PersonalAccountOpeningSeleniumTest, CorporateAccountOpeningSeleniumTest

### 7. Resolve timeout issues in permission management validation tests
- **Priority**: Medium
- **Impact**: RBAC functionality testing
- **Description**: Long-running permission validation operations timing out
- **Skills Required**: UI interaction optimization, validation logic debugging
- **Files Affected**: PermissionManagementSeleniumTest

### 8. Fix missing role-based UI elements in dashboard Selenium tests
- **Priority**: Medium
- **Impact**: Role-based access control verification
- **Description**: Expected UI elements not found based on user roles
- **Skills Required**: RBAC testing, conditional element detection
- **Files Affected**: DashboardSeleniumTest

## 🟢 **LOW COMPLEXITY** (Element Detection & Minor Fixes)

### 9. Add proper explicit waits for UI elements (#no-customers-message, #amount, #page-title)
- **Priority**: Low
- **Impact**: Element detection reliability
- **Description**: `NoSuchElementException` for common UI elements
- **Skills Required**: WebDriver explicit waits, CSS selector knowledge
- **Files Affected**: Multiple Selenium tests

### 10. Fix element detection issues in cash deposit/withdrawal form tests
- **Priority**: Low
- **Impact**: Transaction workflow testing
- **Description**: Missing form elements during cash transaction tests
- **Skills Required**: CSS selector debugging, form interaction patterns
- **Files Affected**: CashWithdrawalSeleniumTest, CashDepositSeleniumTest

### 11. Fix API endpoint validation issues (missing productId/customerId parameters)
- **Priority**: Low
- **Impact**: API integration testing
- **Description**: `status code was: 400, expected: 201` due to missing required parameters
- **Skills Required**: API test data setup, request parameter validation
- **Files Affected**: AccountOpeningTest (Karate tests)

### 12. Fix Karate API test failures (account opening, insufficient balance scenarios)
- **Priority**: Low
- **Impact**: API behavior validation
- **Description**: Various API test scenario failures with unexpected status codes
- **Skills Required**: Karate test framework, API response validation
- **Files Affected**: account-opening.feature, account-opening-validation.feature

## Summary Statistics

- **Total Issues**: 12
- **Critical/High Priority**: 3 items
- **Medium Priority**: 5 items  
- **Low Priority**: 4 items

## Recommended Approach

1. **Phase 1**: Tackle HIGH COMPLEXITY items first (Docker container setup, database isolation)
2. **Phase 2**: Address MEDIUM COMPLEXITY timing and configuration issues
3. **Phase 3**: Clean up LOW COMPLEXITY element detection and minor API fixes

## Key Success Metrics

- Selenium container startup success rate: 100%
- Web UI test pass rate: >90%
- Database constraint violation elimination: 100%
- API test consistency: >95%

## Notes

- Most unit/integration tests are healthy (Repository, Service layer tests pass)
- Core application logic appears sound
- Issues are primarily infrastructure and test environment related
- Fixing Docker setup (#1) will likely resolve 70%+ of current failures