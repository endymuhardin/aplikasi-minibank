# Comprehensive Testing Guide - Aplikasi Minibank

## Table of Contents
1. [Testing Architecture Overview](#testing-architecture-overview)
2. [Test Categories](#test-categories)
3. [Essential Test Pattern](#essential-test-pattern)
4. [Selenium WebDriver Testing](#selenium-webdriver-testing)
5. [Integration Testing](#integration-testing)
6. [Test Data Management](#test-data-management)
7. [Running Tests](#running-tests)
8. [Best Practices](#best-practices)

## Testing Architecture Overview

The application employs a multi-layered testing strategy ensuring comprehensive coverage across all components:

```
src/test/java/id/ac/tazkia/minibank/
├── unit/                     # Pure unit tests (no database)
├── integration/              # Database integration tests
├── feature/                  # BDD-style feature tests
└── essential/               # Core workflow E2E tests
```

### Test Coverage Matrix

| Controller | Essential Test | Coverage Type |
|------------|---------------|---------------|
| AccountController | AccountOpeningEssentialTest | Account opening, selection, form submission |
| AuthenticationController | AuthenticationEssentialTest | Login/logout flows, session management |
| BranchController | BranchManagementEssentialTest | Branch CRUD operations |
| CustomerController | CustomerManagementEssentialTest | Personal/corporate customer registration |
| DashboardController | DashboardEssentialTest | Dashboard metrics, navigation |
| PassbookController | PassbookEssentialTest | Passbook printing, transaction history |
| ProductController | ProductManagementEssentialTest | Islamic product configuration |
| RBACController | RBACEssentialTest | Roles, permissions, user access |
| TransactionController | TransactionEssentialTest | Deposits, withdrawals, transfers |
| UserController | UserManagementEssentialTest | User lifecycle, password management |

## Test Categories

### 1. Unit Tests (`/unit`)
Pure Java tests without Spring context or database dependencies.

**Example: Entity Business Logic**
```java
@Test
void testAccountDeposit() {
    Account account = new Account();
    account.setBalance(BigDecimal.valueOf(1000));
    
    account.deposit(BigDecimal.valueOf(500));
    
    assertEquals(BigDecimal.valueOf(1500), account.getBalance());
}
```

### 2. Integration Tests (`/integration`)
Tests with real database using TestContainers PostgreSQL.

**Categories:**
- **Repository Tests**: `@DataJpaTest` with TestContainers
- **Service Tests**: Full Spring context with database
- **Controller Tests**: REST API endpoint testing

**Example: Repository Test**
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@TestPropertySource(properties = {
    "spring.test.database.replace=none",
    "spring.datasource.url=jdbc:tc:postgresql:17:///testdb"
})
class CustomerRepositoryTest {
    // Tests with real PostgreSQL
}
```

### 3. Feature Tests (`/feature`)
Karate-based BDD tests for API workflows.

**Structure:**
```
src/test/resources/karate/
├── customer/           # Customer registration features
├── account/           # Account management features
├── transaction/       # Transaction processing features
└── user/             # User management features
```

### 4. Essential Tests (`/essential`)
Selenium WebDriver E2E tests covering critical user journeys.

**Pattern:**
- Page Object Model (POM) for maintainability
- Data-driven testing with CSV fixtures
- Headless Chrome execution by default

## Essential Test Pattern

Essential tests provide comprehensive E2E coverage for all web controllers using Selenium WebDriver.

### Structure
Each essential test follows this pattern:

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:17:///testdb",
    "selenium.headless=true",
    "selenium.recording.enabled=false"
})
class ProductManagementEssentialTest extends BaseSeleniumTest {
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/selenium/essential/product-creation-essential.csv")
    void testProductCreation(String productCode, String productName, ...) {
        // Page Object Model interaction
        ProductPage productPage = new ProductPage(driver);
        productPage.navigateToCreateProduct();
        productPage.fillProductForm(productCode, productName, ...);
        productPage.submitForm();
        
        // Assertions
        assertTrue(productPage.isSuccessMessageDisplayed());
        assertEquals(productName, productPage.getCreatedProductName());
    }
}
```

### CSV Test Data
Located in `/src/test/resources/fixtures/selenium/essential/`:

```csv
productCode,productName,productType,expectedResult,testDescription
TAB_TEST_001,Test Savings Account,TABUNGAN_WADIAH,Success,Valid product creation
DEP_TEST_001,Test Deposit,DEPOSITO_MUDHARABAH,Success,Islamic deposit product
```

## Selenium WebDriver Testing

### Configuration
Selenium tests use configurable properties:

```properties
# Default Configuration (application-test.properties)
selenium.headless=true              # Run without browser window
selenium.recording.enabled=false    # No video recording
selenium.browser=chrome             # Browser choice

# Debug Mode
mvn test -Dselenium.headless=false -Dselenium.recording.enabled=true
```

### Page Object Model
All Selenium tests use POM for maintainability:

```java
public class CustomerPage extends BasePage {
    // Element locators
    @FindBy(id = "page-title")
    private WebElement pageTitle;
    
    @FindBy(id = "customerNumber")
    private WebElement customerNumberField;
    
    // Page methods
    public void fillCustomerForm(Customer customer) {
        enterText(customerNumberField, customer.getCustomerNumber());
        // ... more field entries
    }
    
    public boolean isOnCustomerPage() {
        return pageTitle.getText().contains("Customer");
    }
}
```

### Template Requirements
All Thymeleaf templates must include specific IDs for reliable testing:

```html
<!-- Required: Page title with ID -->
<h1 id="page-title" class="...">Customer Management</h1>

<!-- Required: Form elements with IDs -->
<input type="text" id="customerNumber" name="customerNumber" />
<button type="submit" id="submit-button">Submit</button>

<!-- Required: Message areas -->
<div id="success-message" th:if="${successMessage}">...</div>
<div id="error-message" th:if="${errorMessage}">...</div>
```

## Integration Testing

### Schema-Per-Thread Architecture
Parallel test execution with complete isolation:

```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:17:///testdb"
})
abstract class BaseIntegrationTest {
    // Each test thread gets unique schema
    // Format: test_threadname_hash8chars
    // Example: test_forkjoinpool_1_worker_1_dcd693bf
}
```

### TestContainers Configuration
```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
    .withDatabaseName("testdb")
    .withUsername("testuser")
    .withPassword("testpass")
    .withStartupTimeout(Duration.ofMinutes(5));
```

## Test Data Management

### 1. Migration Data (Flyway)
Pre-seeded reference data available in all tests:
- **Branches**: HO001 (Kantor Pusat)
- **Users**: admin, manager1-2, teller1-3, cs1-3
- **Products**: TAB001 (Tabungan Wadiah Basic)
- **Roles**: ADMIN, BRANCH_MANAGER, TELLER, CUSTOMER_SERVICE

### 2. Test Fixtures (CSV)
Data-driven test cases in `/src/test/resources/fixtures/`:

```
fixtures/
├── selenium/
│   └── essential/        # Essential test data
├── customer/            # Customer test data
├── account/             # Account test data
└── transaction/         # Transaction test data
```

### 3. TestDataFactory
Thread-safe test data generation:

```java
public class TestDataFactory {
    // Indonesian locale for realistic data
    private static final ThreadLocal<Faker> faker = 
        ThreadLocal.withInitial(() -> new Faker(new Locale("id", "ID")));
    
    // Unique counters per data type
    private static final AtomicLong accountCounter = 
        new AtomicLong(8000000000L);
    
    public Customer createTestCustomer() {
        // Generates realistic Indonesian customer data
    }
}
```

## Running Tests

### Quick Reference

```bash
# All tests (parallel execution)
mvn test

# Specific test categories
mvn test -Dtest=*UnitTest               # Unit tests only
mvn test -Dtest=*IntegrationTest        # Integration tests
mvn test -Dtest=*EssentialTest          # Essential E2E tests

# Individual essential tests
mvn test -Dtest=CustomerManagementEssentialTest
mvn test -Dtest=ProductManagementEssentialTest
mvn test -Dtest=TransactionEssentialTest

# Specific test method
mvn test -Dtest=AccountOpeningEssentialTest#testPersonalAccountOpening

# With debugging (visible browser)
mvn test -Dtest=*EssentialTest -Dselenium.headless=false

# With video recording
mvn test -Dtest=*EssentialTest -Dselenium.recording.enabled=true

# Coverage report
mvn test jacoco:report
# Report: target/site/jacoco/index.html
```

### Parallel Execution Configuration
```properties
# junit-platform.properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
junit.jupiter.execution.parallel.config.strategy=dynamic
junit.jupiter.execution.parallel.config.dynamic.factor=0.75
```

## Best Practices

### 1. Template Development
- **Always add `id="page-title"`** to main heading
- **Use semantic IDs** for form elements (not generated)
- **Include message divs** for success/error display
- **Avoid CSS-only selectors** in tests

### 2. Test Writing
- **Use Page Object Model** for all Selenium tests
- **Parameterize with CSV** for data-driven testing
- **Clean up test data** in @AfterEach methods
- **Use meaningful test descriptions** in CSV files

### 3. Test Stability
- **Explicit waits** over implicit waits
- **ID selectors** over CSS/XPath selectors
- **Data attributes** for test-specific elements
- **Retry mechanisms** for flaky network operations

### 4. Test Data
- **Unique business keys** using AtomicLong counters
- **Thread markers** for parallel test cleanup
- **Preserve migration data** - only clean test data
- **Indonesian localization** for realistic banking data

### 5. Debugging Failed Tests
```bash
# Run with visible browser
mvn test -Dtest=FailingTest -Dselenium.headless=false

# Enable video recording
mvn test -Dtest=FailingTest -Dselenium.recording.enabled=true

# Check recordings
ls target/test-recordings/

# Increase timeouts for slow environments
-Dselenium.timeout=30
```

### 6. CI/CD Considerations
- **Headless mode** enabled by default
- **TestContainers** requires Docker
- **Parallel execution** scales with CPU cores
- **Memory requirements**: Minimum 2GB for test suite

## Test Metrics

### Current Coverage Status
- **10 Essential Tests**: Complete E2E coverage for all web controllers
- **38 Templates**: Configured with consistent test IDs
- **15+ Integration Tests**: Repository and service layer
- **20+ Unit Tests**: Entity business logic
- **50+ Karate Scenarios**: API feature testing

### Success Criteria
| Metric | Target | Current |
|--------|--------|---------|
| Controller Coverage | 100% | ✅ 100% |
| Template Test IDs | All critical | ✅ 38/46 |
| Test Execution Time | < 5 min | ✅ 3-4 min |
| Parallel Stability | No failures | ✅ Stable |
| Data Isolation | Complete | ✅ Schema-per-thread |

---

*Last Updated: After implementing comprehensive essential test coverage and template standardization for robust Selenium testing.*