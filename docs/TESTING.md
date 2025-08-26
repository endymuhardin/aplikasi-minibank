# Comprehensive Testing Guide

This is the master testing documentation for the Aplikasi Minibank Islamic Banking application, consolidating all testing strategies, infrastructure, and best practices.

## Table of Contents
1. [Testing Architecture](#testing-architecture)
2. [Test Infrastructure](#test-infrastructure)
3. [Test Categories](#test-categories)
4. [Running Tests](#running-tests)
5. [Test Configuration](#test-configuration)
6. [Essential Test Suite](#essential-test-suite)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)

## Testing Architecture

The application employs a multi-layered testing strategy with schema-per-thread isolation for parallel execution:

```
src/test/java/id/ac/tazkia/minibank/
├── unit/                     # Pure unit tests (no database)
├── integration/              # Database integration tests with TestContainers
├── feature/                  # BDD-style Karate API tests
└── selenium/
    └── essential/            # Critical E2E Selenium tests
```

### Schema-Per-Thread Isolation Strategy

**Architecture Components:**
- **PostgreSQL 17** container via TestContainers
- **Unique schema per test thread** using thread name + hash pattern
- **JUnit 5 parallel execution** with dynamic factor (75% CPU utilization)
- **Flyway migration** runs in each isolated schema
- **Migration data available** in every test schema for relationships

## Test Infrastructure

### Core Components

#### BaseIntegrationTest (Abstract Base)
- `@BeforeAll` schema creation and Flyway migration
- TestContainers PostgreSQL 17 container management  
- ApplicationContextInitializer integration via TestSchemaInitializer
- JdbcTemplate with schema-specific configuration

#### TestSchemaManager (Utility)
- Thread-safe schema name generation: `test_threadname_hash8chars`
- Centralized schema creation with error handling
- Flyway configuration for schema-targeted migrations
- Connection management for direct database operations

#### TestDataFactory (Data Generation)
- **Thread-safe test data generation** using ThreadLocal Faker
- **Indonesian localization** for realistic banking data
- **AtomicLong counters** for unique business keys (accounts, branches)
- **Thread markers** for precise test data cleanup

#### SeleniumContainerFactory (Selenium Testing)
- **Architecture-aware container selection** (ARM64 vs x86_64)
- **Seleniarm for M1/M2 Macs** (native ARM64 performance)
- **Standard Chrome for Intel/AMD64** systems
- **Recording and VNC capabilities** for debugging

### Configuration Files

#### JUnit 5 Parallel Execution (junit-platform.properties)
```properties
# Timeout configuration
junit.jupiter.execution.timeout.default=5m
junit.jupiter.execution.timeout.testable.method.default=2m

# Test instance lifecycle - per method for proper isolation
junit.jupiter.testinstance.lifecycle.default=per_method

# Display configuration
junit.jupiter.displayname.generator.default=org.junit.jupiter.api.DisplayNameGenerator$ReplaceUnderscores
```

#### TestContainers Setup
```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
    .withDatabaseName("testdb")
    .withUsername("testuser")
    .withPassword("testpass")
    .withStartupTimeout(Duration.ofMinutes(5));
```

#### Schema Generation Pattern
```
Format: test_threadname_hash8chars
Example: test_forkjoinpool_1_worker_1_dcd693bf
```

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

### 4. Essential Tests (`/selenium/essential`)
Selenium WebDriver E2E tests covering critical user journeys.

**Pattern:**
- Page Object Model (POM) for maintainability
- Data-driven testing with CSV fixtures
- Headless Chrome execution by default

## Running Tests

### Test Configuration Profiles

The application uses simplified Maven profiles for test execution:

#### Sequential Profile (Default)
- Single-threaded test execution
- More predictable and easier for debugging
- Better for environments with limited resources

```bash
# Sequential execution (default - no flags needed)
mvn test

# Explicit sequential execution
mvn test -Dtest.profile=sequential
```

#### Parallel Profile
- Multi-threaded execution at class level (2 threads)
- Faster execution for larger test suites
- Requires more memory and CPU resources

```bash
# Parallel execution
mvn test -Dtest.profile=parallel
```

### Quick Reference Commands

```bash
# All tests (sequential by default)
mvn test

# Specific test categories
mvn test -Dtest=*UnitTest               # Unit tests only
mvn test -Dtest=*IntegrationTest        # Integration tests
mvn test -Dtest=*EssentialTest          # Essential E2E tests

# Individual essential tests
mvn test -Dtest=CustomerManagementEssentialTest
mvn test -Dtest=ProductManagementEssentialTest
mvn test -Dtest=TransactionEssentialTest

# Selenium tests with debugging
mvn test -Dtest=*EssentialTest -Dselenium.headless=false
mvn test -Dtest=*EssentialTest -Dselenium.recording.enabled=true

# Coverage report
mvn test jacoco:report
# Report: target/site/jacoco/index.html
```

### Selenium Configuration

```properties
# Default Configuration
selenium.headless=true              # Run without browser window
selenium.recording.enabled=false    # No video recording
selenium.browser=chrome             # Browser choice

# Debug Mode
mvn test -Dselenium.headless=false -Dselenium.recording.enabled=true
```

## Test Configuration

### Maven Profiles
The build uses two simplified profiles:

| Profile | Mode | Threads | Use Case |
|---------|------|---------|----------|
| **sequential** (default) | Single-threaded | 1 | Debugging, predictable execution |
| **parallel** | Class-level parallel | 2 | Faster execution, CI/CD |

### Profile Configuration Details

#### Sequential Profile (Default)
```xml
<profile>
    <id>sequential</id>
    <activation>
        <activeByDefault>true</activeByDefault>
    </activation>
    <properties>
        <test.profile>sequential</test.profile>
        <selenium.headless>true</selenium.headless>
        <selenium.recording.enabled>false</selenium.recording.enabled>
    </properties>
</profile>
```

#### Parallel Profile
```xml
<profile>
    <id>parallel</id>
    <activation>
        <property>
            <name>test.profile</name>
            <value>parallel</value>
        </property>
    </activation>
    <properties>
        <test.profile>parallel</test.profile>
        <selenium.headless>true</selenium.headless>
        <selenium.recording.enabled>false</selenium.recording.enabled>
    </properties>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <parallel>classes</parallel>
                    <threadCount>2</threadCount>
                    <forkCount>1</forkCount>
                    <reuseForks>true</reuseForks>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

## Essential Test Suite

The Essential Test Suite comprises comprehensive E2E tests covering all core banking functionality:

### Test Coverage Matrix

| Controller | Essential Test | Coverage Type |
|------------|---------------|---------------|
| AccountController | AccountOpeningEssentialTest | Account opening, selection, form submission |
| AccountController | AccountClosureEssentialTest | Account closure workflow, validation |
| AccountController | StatementPdfEssentialTest | PDF statement generation, date ranges |
| AuthenticationController | AuthenticationEssentialTest | Login/logout flows, session management |
| BranchController | BranchManagementEssentialTest | Branch CRUD operations |
| CustomerController | CustomerManagementEssentialTest | Personal/corporate customer registration |
| DashboardController | DashboardEssentialTest | Dashboard metrics, navigation |
| PassbookController | PassbookEssentialTest | Passbook printing, transaction history |
| ProductController | ProductManagementEssentialTest | Islamic product configuration |
| RBACController | RBACEssentialTest | Roles, permissions, user access |
| TransactionController | TransactionEssentialTest | Deposits, withdrawals, transfers |
| UserController | UserManagementEssentialTest | User lifecycle, password management |

### Test Data Management

#### Migration Data Usage
Tests leverage Flyway seed data for entity relationships:
- **Branch**: HO001 (Kantor Pusat Jakarta)
- **Customer**: C1000001 (Ahmad Suharto) 
- **Product**: TAB001 (Tabungan Wadiah Basic)

#### Test Data Generation
- **Account numbers**: 10-digit starting from 8000000000
- **Indonesian names**: Faker with id_ID locale
- **Thread markers**: TEST_THREADNAME format for cleanup
- **Business keys**: AtomicLong counters ensuring uniqueness

#### Cleanup Strategy
- **Thread-marker based**: Only test data removed per thread
- **Migration data preserved**: Flyway seed data remains intact
- **Automatic cleanup**: @AfterEach removes test-specific data

### CSV Test Data Files

Data-driven tests use CSV fixtures:

```
src/test/resources/fixtures/selenium/essential/
├── login-credentials-essential.csv
├── customer-registration-essential.csv
├── product-configuration-essential.csv
├── account-opening-essential.csv
├── transaction-scenarios-essential.csv
├── user-creation-essential.csv
├── passbook-search-essential.csv
├── branch-creation-essential.csv
├── rbac-role-creation-essential.csv
├── rbac-permission-filter-essential.csv
├── account-closure-essential.csv
└── account-statement-pdf-essential.csv
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

### 5. Thread Safety
- **ThreadLocal Faker instances** for locale-specific test data
- **AtomicLong counters** for unique business key generation
- **Thread-specific schema names** preventing cross-thread contamination
- **Thread markers** for precise test data identification and cleanup

## Troubleshooting

### Common Issues and Solutions

#### 1. Test Execution Issues
- **Element Not Found**: Verify element has unique ID in template
- **Timing Issues**: Use proper ExpectedConditions, never Thread.sleep()
- **Container Startup**: Ensure Docker is running with adequate memory (2GB+)

#### 2. Schema Conflicts
- Each test gets unique schema automatically
- Check logs for schema creation/cleanup issues
- Verify PostgreSQL container is healthy

#### 3. Platform-Specific Issues
- Verify correct container image for architecture
- Use appropriate test profile (sequential vs parallel)
- Monitor container logs and resource usage

### Debug Options

```bash
# Enable debug logging
mvn test -Dlogging.level.id.ac.tazkia.minibank=DEBUG

# Run with visible browser
mvn test -Dselenium.headless=false -Dtest=LoginSeleniumTest

# Enable recording
mvn test -Dselenium.recording.enabled=true

# Combined debugging
mvn test -Dselenium.headless=false \
         -Dselenium.recording.enabled=true \
         -Dlogging.level.id.ac.tazkia.minibank=DEBUG

# Single thread execution
mvn test -Dtest.profile=sequential
```

### Test Metrics

#### Current Coverage Status
- **12 Essential Tests**: Complete E2E coverage including account closure and PDF statements
- **38 Templates**: Configured with consistent test IDs
- **15+ Integration Tests**: Repository and service layer
- **20+ Unit Tests**: Entity business logic
- **50+ Karate Scenarios**: API feature testing

#### Success Criteria
| Metric | Target | Current |
|--------|--------|---------|
| Controller Coverage | 100% | ✅ 100% |
| Template Test IDs | All critical | ✅ 38/46 |
| Test Execution Time | < 5 min | ✅ 3-4 min |
| Parallel Stability | No failures | ✅ Stable |
| Data Isolation | Complete | ✅ Schema-per-thread |

---

## Related Documentation

- **[Selenium Testing Documentation](selenium-testing-documentation.md)**: Comprehensive Selenium-specific guide
- **[Test Scenarios](test-scenarios/)**: Detailed test scenario documentation
- **[Remote Build Guide](remote-build-guide.md)**: Remote testing and build execution

---

*This master testing guide consolidates all testing strategies, infrastructure, and best practices for the Aplikasi Minibank Islamic Banking application. For Selenium-specific details, refer to the dedicated Selenium Testing Documentation.*