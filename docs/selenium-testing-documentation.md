# Selenium Testing Documentation - Minibank Application

This comprehensive guide consolidates all Selenium testing documentation for the Minibank application, serving as the single source of truth for writing, running, and maintaining Selenium tests.

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Infrastructure Components](#infrastructure-components)
3. [Running Tests](#running-tests)
4. [Page Object Pattern](#page-object-pattern)
5. [Writing New Tests](#writing-new-tests)
6. [Test Organization](#test-organization)
7. [Test Data Management](#test-data-management)
8. [Best Practices](#best-practices)
9. [Troubleshooting Guide](#troubleshooting-guide)
10. [CI/CD Integration](#cicd-integration)

## Architecture Overview

### Layered Test Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Test Layer (JUnit 5)                     │
│         LoginSeleniumTest, CustomerSeleniumTest, etc.       │
├─────────────────────────────────────────────────────────────┤
│                  Page Object Layer                          │
│            LoginPage, DashboardPage, etc.                   │
├─────────────────────────────────────────────────────────────┤
│              Base Test Infrastructure                       │
│     BaseSeleniumTest extends BaseIntegrationTest           │
├─────────────────────────────────────────────────────────────┤
│                Container Management                         │
│   SeleniumContainerFactory, TestSchemaInitializer          │
├─────────────────────────────────────────────────────────────┤
│                  TestContainers                            │
│        PostgreSQL + Selenium/Seleniarm                     │
└─────────────────────────────────────────────────────────────┘
```

### Key Architectural Principles
- **Separation of Concerns**: Each layer has distinct responsibilities
- **Test Isolation**: Schema-per-thread database isolation
- **Container-Based Testing**: Consistent test environment using Docker
- **Platform Awareness**: Automatic detection for ARM64/x86_64 architectures
- **Parallel Execution**: Thread-safe design supporting concurrent tests

## Infrastructure Components

### 1. BaseSeleniumTest
Extends `BaseIntegrationTest` with Selenium-specific setup:
- WebDriver lifecycle management
- TestContainers integration
- Schema-per-thread setup (inherited)
- Base URL configuration
- VNC logging for debugging

### 2. SeleniumContainerFactory
Platform-aware container creation:
- Architecture detection (ARM64/x86_64)
- Container image selection (Seleniarm for M1/M2, standard Chrome for Intel)
- Resource allocation based on profile
- Recording configuration
- VNC setup

### 3. TestSchemaInitializer
Database isolation:
- Generate unique schema per test thread
- Configure datasource with schema
- Enable parallel test execution
- Prevent test data conflicts

### 4. Supported Profiles

#### Automatic Detection (Default)
- **M1/M2 Mac**: Uses Seleniarm Chromium for native ARM64 performance
- **Intel/AMD64**: Uses standard Selenium Chrome
- **Resources**: Optimized per architecture
- **Parallelism**: 2 threads (local), 4 threads (remote)

#### Manual Profiles
- **local-m1**: Forced Seleniarm for ARM64 Macs
- **local-default**: Forced standard Selenium (runs via Rosetta on M1)
- **remote**: High-resource configuration for CI/CD servers

## Running Tests

### Quick Start Commands

```bash
# Run all Selenium tests (automatic architecture detection)
mvn test -Dtest=*SeleniumTest

# Run specific test class
mvn test -Dtest=LoginSeleniumTest

# Run specific test method
mvn test -Dtest=LoginSeleniumTest#shouldLoginSuccessfullyWithValidCredentials

# Run with visible browser for debugging
mvn test -Dselenium.headless=false -Dtest=LoginSeleniumTest

# Enable recording
mvn test -Dselenium.recording.enabled=true -Dtest=LoginSeleniumTest

# Combined debugging options
mvn test -Dselenium.headless=false \
         -Dselenium.recording.enabled=true \
         -Dlogging.level.id.ac.tazkia.minibank=DEBUG \
         -Dtest=LoginSeleniumTest
```

### Profile-Specific Execution

```bash
# Force M1 Seleniarm profile
mvn test -Dtest.profile=local-m1 -Dtest=LoginSeleniumTest

# Force standard Chrome profile
mvn test -Dtest.profile=local-default -Dtest=LoginSeleniumTest

# Remote server profile with high resources
mvn test -Dtest.profile=remote -Dtest=LoginSeleniumTest
```

### Parallel Execution Control

```bash
# Default: 75% CPU utilization
mvn test -Djunit.jupiter.execution.parallel.enabled=true \
         -Djunit.jupiter.execution.parallel.config.dynamic.factor=0.75

# Single thread execution for debugging
mvn test -Djunit.jupiter.execution.parallel.enabled=false
```

## Page Object Pattern

### Implementation Template

```java
package id.ac.tazkia.minibank.selenium.pages;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@Slf4j
public class CustomerFormPage {
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    // MANDATORY: Always use ID selectors
    @FindBy(id = "customer-type")
    private WebElement customerTypeSelect;
    
    @FindBy(id = "first-name")
    private WebElement firstNameInput;
    
    @FindBy(id = "submit-button")
    private WebElement submitButton;
    
    public CustomerFormPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    
    // Navigation
    public CustomerFormPage navigateTo(String baseUrl) {
        driver.get(baseUrl + "/customer/create");
        waitForPageLoad();
        log.debug("Navigated to customer creation form");
        return this;
    }
    
    // Wait for page load
    private void waitForPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(customerTypeSelect));
        log.debug("Customer form loaded");
    }
    
    // Form interactions with method chaining
    public CustomerFormPage selectCustomerType(String type) {
        wait.until(ExpectedConditions.elementToBeClickable(customerTypeSelect));
        // Select implementation
        log.debug("Selected customer type: {}", type);
        return this;
    }
    
    // Action that transitions to another page
    public CustomerListPage submitForm() {
        wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        submitButton.click();
        log.debug("Submitted customer form");
        return new CustomerListPage(driver);
    }
    
    // Verification methods
    public boolean isSuccessMessageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(successMessage));
            return true;
        } catch (Exception e) {
            log.debug("Success message not displayed: {}", e.getMessage());
            return false;
        }
    }
}
```

### Key Principles
- **ID-Only Locators**: Exclusively use element IDs for stability
- **Method Chaining**: Return page objects for fluent interface
- **Explicit Waits**: Always use WebDriverWait, never Thread.sleep()
- **Error Handling**: Graceful handling of element not found scenarios
- **Logging**: Comprehensive debug logging for troubleshooting

## Writing New Tests

### Test Class Template

```java
package id.ac.tazkia.minibank.selenium;

import id.ac.tazkia.minibank.config.BaseSeleniumTest;
import id.ac.tazkia.minibank.selenium.pages.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("Customer Management Selenium Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerManagementSeleniumTest extends BaseSeleniumTest {
    
    @Test
    @Order(1)
    @DisplayName("Should create personal customer successfully")
    void shouldCreatePersonalCustomerSuccessfully() {
        // Arrange - Login
        LoginPage loginPage = new LoginPage(driver);
        var dashboard = loginPage.navigateTo(baseUrl)
                                 .loginWith("admin", "minibank123");
        
        assertTrue(dashboard.isDashboardLoaded(), 
                  "Should be redirected to dashboard after login");
        
        // Act - Create customer
        CustomerFormPage customerForm = new CustomerFormPage(driver);
        var customerList = customerForm.navigateTo(baseUrl)
                                       .selectCustomerType("PERSONAL")
                                       .fillPersonalInfo("John", "Doe")
                                       .submitForm();
        
        // Assert
        assertTrue(customerList.isCustomerInList("John Doe"),
                  "New customer should appear in the list");
        
        log.info("Personal customer created successfully");
    }
    
    @ParameterizedTest(name = "[{index}] Create {0} customer")
    @CsvFileSource(resources = "/fixtures/selenium/customer-test-data.csv", 
                   numLinesToSkip = 1)
    @DisplayName("Should create customers with various data")
    void shouldCreateCustomersWithVariousData(
            String customerType, 
            String firstName, 
            String lastName,
            String expectedResult) {
        
        // Data-driven test implementation
    }
    
    @Test
    @DisplayName("Should verify role-based access")
    void shouldVerifyRoleBasedAccess() {
        // Test role permissions
    }
}
```

## Test Organization

### Test Suite Structure

```
src/test/java/id/ac/tazkia/minibank/selenium/
├── essential/                        # Priority 1: Core functionality tests
│   ├── AuthenticationEssentialTest.java
│   ├── CustomerManagementEssentialTest.java
│   ├── AccountOpeningEssentialTest.java
│   └── TransactionEssentialTest.java
├── comprehensive/                    # Priority 2: Full feature coverage
│   ├── authentication/
│   ├── customer/
│   ├── account/
│   ├── transaction/
│   └── workflow/
├── validation/                       # Priority 3: Edge cases & validation
└── pages/                           # Reusable Page Objects
    ├── common/
    ├── customer/
    ├── account/
    └── transaction/
```

### Test Data Structure

```
src/test/resources/fixtures/selenium/
├── essential/                        # Core test data
│   ├── login-credentials.csv
│   └── customer-essential-data.csv
├── comprehensive/                    # Detailed test scenarios
│   ├── authentication/
│   ├── customer/
│   └── transaction/
└── validation/                       # Edge case test data
```

### Execution Phases

#### Phase 1: Essential Tests (5-10 minutes)
```bash
mvn test -Dtest=id.ac.tazkia.minibank.selenium.essential.**
```
- Core functionality validation
- Happy path scenarios only
- Quick feedback cycle

#### Phase 2: Comprehensive Tests (30-60 minutes)
```bash
mvn test -Dtest=id.ac.tazkia.minibank.selenium.comprehensive.**
```
- Complete feature coverage
- Multiple data scenarios
- Role-based testing

#### Phase 3: Validation Tests (15-30 minutes)
```bash
mvn test -Dtest=id.ac.tazkia.minibank.selenium.validation.**
```
- Edge cases
- Error handling
- Business rule validation

## Test Data Management

### Login Credentials
Available in `/fixtures/selenium/login-credentials.csv`:

| Username | Password | Role | Description |
|----------|----------|------|-------------|
| admin | minibank123 | ADMIN | System Administrator |
| manager1 | minibank123 | MANAGER | Branch Manager |
| cs1 | minibank123 | CUSTOMER_SERVICE | Customer Service |
| teller1 | minibank123 | TELLER | Bank Teller |

### CSV File Format Example
```csv
customerType,firstName,lastName,expectedResult
PERSONAL,John,Smith,SUCCESS
PERSONAL,Jane,Doe,SUCCESS
CORPORATE,ABC,Company,SUCCESS
PERSONAL,,Smith,VALIDATION_ERROR
```

### Data-Driven Testing
```java
@ParameterizedTest(name = "[{index}] {3}: {0}")
@CsvFileSource(resources = "/fixtures/selenium/test-data.csv", 
               numLinesToSkip = 1)
void testWithCsvData(String param1, String param2, String expected) {
    // Test implementation
}
```

## Best Practices

### 1. Element Location Strategy
**MANDATORY: Use ID selectors exclusively**

```html
<!-- In Thymeleaf templates -->
<input type="text" id="customer-name" name="customerName" />
<button type="submit" id="save-button">Save</button>
<div id="error-message" class="alert alert-danger"></div>
```

### 2. Wait Strategies
```java
// Good - Explicit wait
wait.until(ExpectedConditions.visibilityOf(element));
wait.until(ExpectedConditions.elementToBeClickable(button));

// Bad - Never use Thread.sleep()
Thread.sleep(2000); // AVOID
```

### 3. Assertion Best Practices
```java
// Good - Specific assertion with context
assertTrue(dashboard.isUserMenuVisible(), 
          "User menu should be visible after successful login");

// Good - Multiple related assertions
assertAll("Dashboard elements",
    () -> assertTrue(dashboard.isStatisticsSectionVisible()),
    () -> assertTrue(dashboard.isQuickActionsVisible()),
    () -> assertEquals("Dashboard", dashboard.getPageTitle())
);

// Bad - Generic assertion without context
assertTrue(result); // What is being tested?
```

### 4. Test Design Principles
- Keep tests focused on single functionality
- Use meaningful test names and @DisplayName annotations
- Implement proper wait strategies
- Return page objects for method chaining
- Add comprehensive logging for debugging

### 5. Thread Safety
```java
// Good - Using schema-per-thread isolation
public class BaseSeleniumTest extends BaseIntegrationTest {
    // Each thread gets its own schema
}

// Good - Stateless page objects
public class LoginPage {
    private final WebDriver driver; // Thread-local driver
}

// Bad - Shared mutable state
public class BadExample {
    private static WebDriver sharedDriver; // Will cause issues
}
```

## Troubleshooting Guide

### Common Issues and Solutions

#### 1. Element Not Found
- Verify element has unique ID in template
- Check wait conditions and timeouts
- Enable debug logging: `-Dlogging.level.id.ac.tazkia.minibank=DEBUG`
- Use VNC viewer to watch test execution

#### 2. Container Startup Failures
- Ensure Docker is running
- Check available memory (minimum 2GB recommended)
- Verify network connectivity
- Review Docker resource allocation

#### 3. Timing Issues
- Increase wait timeout for slow operations
- Use proper ExpectedConditions
- Never use Thread.sleep()
- Check for JavaScript-rendered content

#### 4. Schema Conflicts
- Each test gets unique schema automatically
- Check logs for schema creation/cleanup issues
- Verify PostgreSQL container is healthy

#### 5. Platform-Specific Issues
- Verify correct container image for architecture
- Check Docker resource allocation
- Use appropriate test profile
- Monitor container logs

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
mvn test -Djunit.jupiter.execution.parallel.enabled=false
```

### VNC Viewer Access
When running with `-Dselenium.headless=false`:

```
═══════════════════════════════════════════════════════════
🖥️  VNC Viewer Information:
   Address: localhost:32768
   Password: secret (default VNC password)
   Connect using: vnc://localhost:32768 (macOS)
═══════════════════════════════════════════════════════════
```

**macOS**: Use built-in Screen Sharing - `Cmd+K` → `vnc://localhost:PORT`  
**Windows/Linux**: Use TigerVNC, RealVNC, or similar

### Test Recording
When enabled with `-Dselenium.recording.enabled=true`:
- Videos saved to `target/selenium-recordings/`
- All sessions recorded (FLV format)
- Viewable with VLC player
- Files appear after test completion (10-30 seconds)

## CI/CD Integration

### Local Development Integration
```bash
# Run with remote build system
./devops/start-remote-build.sh --test="LoginSeleniumTest"

# With specific profile
./devops/start-remote-build.sh --opts="-Dtest.profile=remote -Dtest=LoginSeleniumTest"
```

### Maven Profiles for CI/CD
```xml
<profiles>
    <!-- Essential Tests -->
    <profile>
        <id>essential</id>
        <properties>
            <test.groups>essential</test.groups>
            <junit.jupiter.execution.parallel.enabled>true</junit.jupiter.execution.parallel.enabled>
        </properties>
    </profile>
    
    <!-- Comprehensive Tests -->
    <profile>
        <id>comprehensive</id>
        <properties>
            <test.groups>comprehensive</test.groups>
            <junit.jupiter.execution.parallel.enabled>true</junit.jupiter.execution.parallel.enabled>
        </properties>
    </profile>
</profiles>
```

## Implementation Checklist

### For New Features
1. ✅ Add unique IDs to all interactive elements in templates
2. ✅ Create Page Object class for the feature
3. ✅ Write essential test (happy path)
4. ✅ Add comprehensive tests (multiple scenarios)
5. ✅ Create test data CSV files
6. ✅ Add validation tests (edge cases)
7. ✅ Verify parallel execution compatibility
8. ✅ Update documentation if needed

### For Maintenance
1. ✅ Update element IDs when UI changes
2. ✅ Keep test data files current
3. ✅ Review role-specific verifications
4. ✅ Monitor test execution times
5. ✅ Optimize slow-running tests
6. ✅ Remove obsolete tests

## Summary

This architecture provides:
- **Maintainability**: Clear separation of concerns and consistent patterns
- **Scalability**: Parallel execution with isolated test environments
- **Reliability**: Container-based testing ensures consistency
- **Debuggability**: Comprehensive logging, VNC access, and recording
- **Flexibility**: Platform-aware configuration and execution modes
- **Best Practices**: Industry-standard patterns and clean code

Following these guidelines ensures high-quality, maintainable Selenium tests that provide reliable feedback on application functionality across different roles and scenarios.