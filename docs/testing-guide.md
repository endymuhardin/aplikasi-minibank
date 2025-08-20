# Testing Guide

## Overview
This document provides a comprehensive guide to testing the Aplikasi Mini Bank project. It covers unit, integration, and Selenium-based UI testing.

### Unit dan Integration Tests ###

```bash
# Run semua test
mvn test

# Run test dengan coverage report
mvn test jacoco:report

# Run test class tertentu
mvn test -Dtest=AccountRepositoryTest

# Run test method tertentu
mvn test -Dtest=AccountRepositoryTest#shouldFindByCustomerId

# Run Karate feature tests
mvn test -Dtest=DepositTest
```

### Selenium Tests ###

Aplikasi menggunakan Selenium dengan TestContainers untuk automated UI testing. Selenium tests berjalan dalam Docker container yang terisolasi dengan recording capability untuk monitoring dan debugging.

#### Menjalankan Selenium Tests ####

```bash
# Mode default (headless mode, tanpa recording - fastest, ~40-50% faster execution)
mvn test -Dtest=ProductManagementSeleniumTest

# Mode dengan browser window visible (untuk debugging)
mvn test -Dtest=ProductManagementSeleniumTest -Dselenium.headless=false

# Mode dengan video recording untuk monitoring/debugging
mvn test -Dtest=ProductManagementSeleniumTest -Dselenium.recording.enabled=true

# Run semua Selenium tests
mvn test -Dtest="*Selenium*"
# Run Cash Deposit Selenium tests  
mvn test -Dtest=CashDepositSeleniumTest
# Run Cash Withdrawal Selenium tests
mvn test -Dtest=CashWithdrawalSeleniumTest

# Run dengan browser Chrome (default: Chrome)
mvn test -Dtest=LoginSeleniumTest -Dselenium.browser=chrome

# Run dengan browser Firefox
mvn test -Dtest=LoginSeleniumTest -Dselenium.browser=firefox

# Kombinasi opsi untuk debugging maksimal
mvn test -Dtest=LoginSeleniumTest -Dselenium.headless=false -Dselenium.recording.enabled=true -Dselenium.browser=chrome
```

#### Monitoring Selenium Tests ####

**VNC Viewer untuk Live Monitoring:**
- Saat test berjalan, check log untuk VNC URL: `VNC URL : http://localhost:[port]`
- Buka URL tersebut di browser untuk melihat browser automation secara real-time
- Berguna untuk debugging test failures dan memantau test execution
- **Note**: VNC viewer tersedia baik dalam headless maupun non-headless mode

**Video Recordings:**
- Lokasi: `target/selenium-recordings/`
- Format: MP4
- Hanya tersedia jika `-Dselenium.recording.enabled=true`
- Recording dimulai otomatis saat test start dan berhenti saat test selesai

**Log Monitoring:**
- Selenium container logs menampilkan browser startup, WebDriver creation, dan test execution
- Test method logs menampilkan page interactions dan assertions

#### Available Selenium Tests ####

- **LoginSeleniumTest**: Test login functionality dengan berbagai user roles
- **ProductManagementSeleniumTest**: Test CRUD operations untuk banking products
- **RbacManagementSeleniumTest**: Test role-based access control dan user management

#### Selenium Test Configuration ####

**Performance Optimizations:**
- **Container startup**: ~13 seconds (optimized from 60+ seconds)
- **Test execution**: 40-50% faster with container reuse and browser optimizations
- **Login operations**: 95% faster with session caching (5 seconds → 250ms average)
- **Overall test suite**: 80% faster execution for authentication-heavy tests
- **Memory usage**: 1GB shared memory allocation (optimized from 2GB)
- **Resource limits**: 1 CPU core quota to prevent resource contention
- **Parallel execution**: Intelligent resource coordination with JUnit 5 parallel testing
- **Test categorization**: Automatic categorization with FastTests, DatabaseTests, UiTests
- **Test isolation**: Thread-safe test data generation with atomic counters

**Runtime Options:**
- **Browser**: Chrome (default), Firefox (dengan `-Dselenium.browser=firefox`)
- **Headless Mode**: Enabled by default (fastest), disable dengan `-Dselenium.headless=false` untuk debugging
- **Recording**: Disabled by default, enable dengan `-Dselenium.recording.enabled=true`
- **TestContainers**: Otomatis start/stop Selenium Grid container dengan optimized resource configuration

**Test Architecture:**
- **Page Objects**: Menggunakan Page Object Pattern untuk maintainability
- **Base Test Classes**: Actual test infrastructure classes:
  - `BaseSeleniumTest`: Base class untuk semua Selenium tests
  - `AbstractSeleniumTestBase`: Abstract base dengan WebDriver management
  - `BaseRepositoryTest`: Base class untuk repository integration tests
  - `BaseIntegrationTest`: Base class untuk general integration tests
- **Test Infrastructure**: Selenium test management
  - `ParallelSeleniumManager`: Thread-safe WebDriver dan container management
  - `SeleniumTestProperties`: Configuration properties untuk Selenium tests
  - `TestDataFactory`: Utility untuk generate test data
  - `TestStateManager`: Test state management

**Test Base Classes Usage:**
```java
// Selenium UI tests
public class MySeleniumTest extends BaseSeleniumTest {
    @Test
    void testSomething() {
        WebDriver driver = ParallelSeleniumManager.getDriver();
        String baseUrl = ParallelSeleniumManager.getBaseUrl(appPort);
        // test logic...
    }
}
```

#### Parallel Test Execution ####

**Parallel Execution dengan JUnit 5:**
```bash
# Run all tests with parallel execution (configured in junit-platform.properties)
mvn test                                            # Uses JUnit 5 parallel execution

# Run specific test categories  
mvn test -Dtest="*RepositoryTest"                   # Repository integration tests
mvn test -Dtest="*ApiTest"                          # Karate API tests (jika ada)
mvn test -Dtest="*SeleniumTest"                     # Selenium UI tests (resource-managed)

# Override Selenium container limits untuk high-resource systems
mvn test -Dtest="*SeleniumTest" -Dtest.selenium.container.maxContainers=4
```

**Parallel Configuration:**
- **Resource-aware threading**: Automatically detects optimal thread count based on CPU/memory
- **Test categorization**: FastTests, MediumTests, SlowTests, DatabaseTests, UiTests
- **Intelligent isolation**: Thread-safe test data generation with atomic counters  
- **Resource coordination**: Database, Selenium container, and file system locks
- **State management**: Automatic test state isolation and cleanup

**Performance Improvements:**
- **40-50% faster execution** for repository and integration tests
- **95% faster login operations** with session caching across parallel UI tests
- **80% overall improvement** for authentication-heavy test suites
- **Thread-safe data generation** prevents conflicts between parallel tests

#### Troubleshooting ####

- **Test Timeout**: Selenium tests memiliki implicit wait 5 detik dan retry logic
- **Recording Issues**: Pastikan directory `target/selenium-recordings/` dapat ditulis
- **VNC Connection**: Gunakan VNC URL dari log untuk live monitoring test execution
- **Browser Issues**: Switch browser dengan `-Dselenium.browser=chrome` jika Firefox bermasalah
- **Parallel Issues**: Use `-Dtest.parallel.disabled=true` to disable parallel execution for debugging
