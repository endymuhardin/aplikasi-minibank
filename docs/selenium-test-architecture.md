# Selenium Test Architecture - Best Practices Documentation

This document provides a comprehensive guide to the Selenium test architecture implemented in the Minibank application, serving as a reference for creating future tests following established best practices.

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Core Design Patterns](#core-design-patterns)
3. [Infrastructure Components](#infrastructure-components)
4. [Test Implementation Guidelines](#test-implementation-guidelines)
5. [Best Practices](#best-practices)
6. [Examples and Templates](#examples-and-templates)

## Architecture Overview

### Layered Test Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Test Layer (JUnit 5)                     │
│                 LoginSeleniumTest, etc.                     │
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

1. **Separation of Concerns**: Each layer has a distinct responsibility
2. **Test Isolation**: Schema-per-thread database isolation
3. **Container-Based Testing**: Consistent test environment using Docker
4. **Platform Awareness**: Automatic detection and optimization for different architectures
5. **Parallel Execution**: Thread-safe design supporting concurrent test execution

## Core Design Patterns

### 1. Page Object Pattern

The Page Object pattern encapsulates web page interactions and provides a clean API for tests.

**Structure:**
```java
public class PageName {
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    // Element locators using @FindBy
    @FindBy(id = "element-id")
    private WebElement elementName;
    
    // Constructor with PageFactory initialization
    public PageName(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    
    // Action methods returning page objects for chaining
    public NextPage performAction() {
        wait.until(ExpectedConditions.elementToBeClickable(element));
        element.click();
        return new NextPage(driver);
    }
    
    // Verification methods
    public boolean isElementVisible() {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
```

**Key Principles:**
- **Single Responsibility**: Each page object represents one page/component
- **Method Chaining**: Methods return page objects for fluent interface
- **Explicit Waits**: Always use WebDriverWait for element interactions
- **Error Handling**: Graceful handling of element not found scenarios
- **ID-Only Locators**: Exclusively use element IDs for stability

### 2. Base Test Pattern

Centralized test setup and teardown logic with inheritance hierarchy.

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = TestSchemaInitializer.class)
public abstract class BaseSeleniumTest extends BaseIntegrationTest {
    
    @Container
    protected static BrowserWebDriverContainer<?> seleniumContainer = 
        SeleniumContainerFactory.createSeleniumContainer();
    
    protected WebDriver driver;
    protected String baseUrl;
    
    @BeforeEach
    void setUpSelenium() {
        // Initialize WebDriver with proper configuration
        // Set up base URL with TestContainers host access
        // Configure headless/non-headless mode
    }
    
    @AfterEach
    void tearDownSelenium() {
        // Clean up WebDriver
        // Allow recording finalization if enabled
    }
}
```

### 3. Factory Pattern for Container Creation

Platform-aware container creation with configuration management.

```java
@UtilityClass
public class SeleniumContainerFactory {
    
    public static BrowserWebDriverContainer<?> createSeleniumContainer() {
        // Detect architecture (ARM64/x86_64)
        // Select appropriate container image
        // Configure resources based on profile
        // Set up recording if enabled
        return configuredContainer;
    }
}
```

### 4. Data-Driven Testing Pattern

Using CSV files for test data with parameterized tests.

```java
@ParameterizedTest(name = "[{index}] {3}: {0}")
@CsvFileSource(resources = "/fixtures/selenium/login-credentials.csv", 
               numLinesToSkip = 1)
@DisplayName("Should login successfully with valid credentials")
void shouldLoginSuccessfullyWithValidCredentials(
    String username, String password, String role, String description) {
    // Data-driven test implementation
}
```

## Infrastructure Components

### 1. BaseSeleniumTest

**Responsibilities:**
- WebDriver lifecycle management
- TestContainers integration
- Schema-per-thread setup (inherited)
- Base URL configuration
- VNC logging for debugging

**Key Features:**
```java
- Random port allocation for Spring Boot
- Host port exposure for container access
- ChromeOptions configuration
- Headless/non-headless mode switching
- Recording support
```

### 2. SeleniumContainerFactory

**Responsibilities:**
- Architecture detection (ARM64/x86_64)
- Container image selection
- Resource allocation
- Recording configuration
- VNC setup

**Profiles Supported:**
- `local-m1`: ARM64 Mac with Seleniarm
- `local-default`: Standard Selenium Chrome
- `remote`: High-resource remote server configuration

### 3. TestSchemaInitializer

**Responsibilities:**
- Generate unique schema per test thread
- Configure datasource with schema
- Enable parallel test execution
- Prevent test data conflicts

### 4. MethodLevelRecordingManager

**Responsibilities:**
- Per-method recording management
- Test status tracking
- Recording file naming
- Future extensibility for TestContainers

## Test Implementation Guidelines

### 1. Creating a New Page Object

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
    
    // ALWAYS use ID selectors
    @FindBy(id = "customer-type")
    private WebElement customerTypeSelect;
    
    @FindBy(id = "first-name")
    private WebElement firstNameInput;
    
    @FindBy(id = "last-name")
    private WebElement lastNameInput;
    
    @FindBy(id = "submit-button")
    private WebElement submitButton;
    
    @FindBy(id = "success-message")
    private WebElement successMessage;
    
    public CustomerFormPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    
    // Navigation method
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
    
    // Form interaction with method chaining
    public CustomerFormPage selectCustomerType(String type) {
        wait.until(ExpectedConditions.elementToBeClickable(customerTypeSelect));
        // Select implementation
        log.debug("Selected customer type: {}", type);
        return this;
    }
    
    public CustomerFormPage fillPersonalInfo(String firstName, String lastName) {
        firstNameInput.clear();
        firstNameInput.sendKeys(firstName);
        lastNameInput.clear();
        lastNameInput.sendKeys(lastName);
        log.debug("Filled personal info: {} {}", firstName, lastName);
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
    
    public String getSuccessMessageText() {
        wait.until(ExpectedConditions.visibilityOf(successMessage));
        return successMessage.getText();
    }
}
```

### 2. Creating a New Test Class

```java
package id.ac.tazkia.minibank.selenium;

import id.ac.tazkia.minibank.config.BaseSeleniumTest;
import id.ac.tazkia.minibank.selenium.pages.LoginPage;
import id.ac.tazkia.minibank.selenium.pages.CustomerFormPage;
import id.ac.tazkia.minibank.selenium.pages.CustomerListPage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("Customer Management Selenium Tests")
class CustomerManagementSeleniumTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Should create personal customer successfully")
    void shouldCreatePersonalCustomerSuccessfully() {
        // Arrange - Login first
        LoginPage loginPage = new LoginPage(driver);
        var dashboard = loginPage.navigateTo(baseUrl)
                                 .loginWith("admin", "minibank123");
        
        assertTrue(dashboard.isDashboardLoaded(), 
                  "Should be redirected to dashboard after login");
        
        // Act - Navigate to customer form and create customer
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
        
        // Login
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl)
                 .loginWith("cs1", "minibank123");
        
        // Create customer
        CustomerFormPage form = new CustomerFormPage(driver);
        form.navigateTo(baseUrl)
            .selectCustomerType(customerType)
            .fillPersonalInfo(firstName, lastName);
        
        if ("SUCCESS".equals(expectedResult)) {
            var listPage = form.submitForm();
            assertTrue(listPage.isCustomerInList(firstName + " " + lastName),
                      "Customer should be created successfully");
        } else {
            form.submitForm();
            assertTrue(form.hasValidationError(),
                      "Should show validation error");
        }
    }
    
    @Test
    @DisplayName("Should verify role-based access for customer creation")
    void shouldVerifyRoleBasedAccess() {
        // Test that CS role can access customer creation
        testRoleAccess("cs1", "minibank123", true);
        
        // Test that Teller role cannot access customer creation
        testRoleAccess("teller1", "minibank123", false);
    }
    
    private void testRoleAccess(String username, String password, 
                                boolean shouldHaveAccess) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl)
                 .loginWith(username, password);
        
        CustomerFormPage form = new CustomerFormPage(driver);
        driver.get(baseUrl + "/customer/create");
        
        if (shouldHaveAccess) {
            assertTrue(form.isFormVisible(),
                      username + " should have access to customer creation");
        } else {
            assertTrue(form.isAccessDeniedMessageVisible(),
                      username + " should not have access to customer creation");
        }
    }
}
```

### 3. Test Data CSV Format

Create test data files in `src/test/resources/fixtures/selenium/`:

**customer-test-data.csv:**
```csv
customerType,firstName,lastName,expectedResult
PERSONAL,John,Smith,SUCCESS
PERSONAL,Jane,Doe,SUCCESS
CORPORATE,ABC,Company,SUCCESS
PERSONAL,,Smith,VALIDATION_ERROR
PERSONAL,John,,VALIDATION_ERROR
```

## Best Practices

### 1. Element Location Strategy

**MANDATORY: Use ID selectors exclusively**

```html
<!-- In your Thymeleaf templates -->
<input type="text" id="customer-name" name="customerName" />
<button type="submit" id="save-customer-button">Save</button>
<div id="error-message" class="alert alert-danger"></div>
```

```java
// In your Page Objects
@FindBy(id = "customer-name")
private WebElement customerNameInput;

@FindBy(id = "save-customer-button")
private WebElement saveButton;

@FindBy(id = "error-message")
private WebElement errorMessage;
```

**Why ID-only?**
- Maximum stability across UI changes
- Fastest element location performance
- Clear contract between test and application
- Easy to maintain and debug

### 2. Wait Strategies

**Always use explicit waits, never Thread.sleep()**

```java
// Good - Explicit wait
wait.until(ExpectedConditions.visibilityOf(element));
wait.until(ExpectedConditions.elementToBeClickable(button));
wait.until(ExpectedConditions.textToBePresentInElement(message, "Success"));

// Bad - Hard-coded sleep
Thread.sleep(2000); // NEVER do this
```

### 3. Test Data Management

**Use external data sources for test data**

```java
// Good - External CSV file
@CsvFileSource(resources = "/fixtures/selenium/test-data.csv")

// Good - Centralized test data constants
public class TestData {
    public static final String ADMIN_USER = "admin";
    public static final String ADMIN_PASSWORD = "minibank123";
}

// Bad - Hard-coded test data scattered in tests
void test() {
    login("admin", "password123"); // Avoid
}
```

### 4. Logging and Debugging

**Implement comprehensive logging**

```java
public class PageObject {
    
    public PageObject performAction() {
        log.debug("Performing action on element: {}", elementId);
        try {
            element.click();
            log.info("Successfully clicked element: {}", elementId);
        } catch (Exception e) {
            log.error("Failed to click element: {}", elementId, e);
            throw e;
        }
        return this;
    }
}
```

### 5. Test Organization

**Follow consistent naming and structure**

```
src/test/java/id/ac/tazkia/minibank/
├── selenium/                      # All Selenium tests
│   ├── LoginSeleniumTest.java
│   ├── CustomerSeleniumTest.java
│   └── pages/                     # Page Objects
│       ├── LoginPage.java
│       ├── DashboardPage.java
│       └── CustomerFormPage.java
├── config/                        # Test infrastructure
│   ├── BaseSeleniumTest.java
│   └── SeleniumContainerFactory.java
└── resources/
    └── fixtures/
        └── selenium/              # Test data
            ├── login-credentials.csv
            └── customer-data.csv
```

### 6. Parallel Execution

**Design tests to be thread-safe**

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

### 7. Platform-Specific Optimization

**Support multiple environments**

```bash
# Local development on M1 Mac
mvn test -Dtest=CustomerSeleniumTest

# Remote CI/CD server
mvn test -Dtest.profile=remote -Dtest=CustomerSeleniumTest

# Debugging with visible browser
mvn test -Dselenium.headless=false -Dtest=CustomerSeleniumTest

# With recording enabled
mvn test -Dselenium.recording.enabled=true -Dtest=CustomerSeleniumTest
```

### 8. Assertion Best Practices

**Write clear, specific assertions**

```java
// Good - Specific assertion with message
assertTrue(dashboard.isUserMenuVisible(), 
          "User menu should be visible after successful login");

assertEquals("Customer created successfully", 
            successMessage.getText(),
            "Success message should confirm customer creation");

// Good - Multiple related assertions
assertAll("Dashboard elements",
    () -> assertTrue(dashboard.isStatisticsSectionVisible()),
    () -> assertTrue(dashboard.isQuickActionsVisible()),
    () -> assertEquals("Dashboard", dashboard.getPageTitle())
);

// Bad - Generic assertion without context
assertTrue(result); // What is being tested?
```

### 9. Error Recovery

**Handle failures gracefully**

```java
public boolean isElementVisible() {
    try {
        return element.isDisplayed();
    } catch (NoSuchElementException e) {
        log.debug("Element not found: {}", e.getMessage());
        return false;
    } catch (StaleElementReferenceException e) {
        log.debug("Element is stale, retrying...");
        // Re-initialize and retry
        PageFactory.initElements(driver, this);
        return element.isDisplayed();
    }
}
```

### 10. Role-Based Testing

**Verify permissions systematically**

```java
@Test
void shouldVerifyRoleBasedElementVisibility() {
    Map<String, List<String>> roleExpectations = Map.of(
        "ADMIN", List.of("user-management", "system-config"),
        "MANAGER", List.of("product-management", "reports"),
        "TELLER", List.of("transaction-processing", "account-lookup"),
        "CUSTOMER_SERVICE", List.of("customer-registration", "account-opening")
    );
    
    roleExpectations.forEach((role, expectedElements) -> {
        loginAs(role);
        DashboardPage dashboard = new DashboardPage(driver);
        
        expectedElements.forEach(elementId -> 
            assertTrue(dashboard.isElementVisible(elementId),
                      String.format("%s role should see %s", role, elementId))
        );
    });
}
```

## Examples and Templates

### 1. Complete Test Template

```java
package id.ac.tazkia.minibank.selenium;

import id.ac.tazkia.minibank.config.BaseSeleniumTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("Feature Name Selenium Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FeatureNameSeleniumTest extends BaseSeleniumTest {
    
    @BeforeEach
    void setUp() {
        // Additional setup if needed
        log.info("Setting up test for feature X");
    }
    
    @Test
    @Order(1)
    @DisplayName("Should perform basic feature functionality")
    void shouldPerformBasicFunctionality() {
        // Arrange
        LoginPage loginPage = new LoginPage(driver);
        var dashboard = loginPage.navigateTo(baseUrl)
                                 .loginWith("admin", "minibank123");
        
        // Act
        FeaturePage featurePage = new FeaturePage(driver);
        var result = featurePage.navigateTo(baseUrl)
                                .performAction()
                                .getResult();
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("Expected", result, "Result should match expected value");
        
        // Verify side effects
        assertTrue(featurePage.isSuccessMessageDisplayed(),
                  "Success message should be displayed");
    }
    
    @ParameterizedTest(name = "[{index}] Test with {0}")
    @CsvFileSource(resources = "/fixtures/selenium/feature-test-data.csv",
                   numLinesToSkip = 1)
    @DisplayName("Should handle various input scenarios")
    void shouldHandleVariousScenarios(String input, String expected) {
        // Data-driven test implementation
    }
    
    @Test
    @DisplayName("Should handle error conditions gracefully")
    void shouldHandleErrors() {
        // Test error scenarios
    }
    
    @AfterEach
    void tearDown() {
        // Additional cleanup if needed
        log.info("Test completed");
    }
}
```

### 2. Page Object Template

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
public class FeaturePage {
    
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    // Page elements - ALWAYS use ID
    @FindBy(id = "page-title")
    private WebElement pageTitle;
    
    @FindBy(id = "action-button")
    private WebElement actionButton;
    
    @FindBy(id = "result-message")
    private WebElement resultMessage;
    
    // Constructor
    public FeaturePage(WebDriver driver) {
        this(driver, DEFAULT_TIMEOUT);
    }
    
    public FeaturePage(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
        PageFactory.initElements(driver, this);
    }
    
    // Navigation
    public FeaturePage navigateTo(String baseUrl) {
        String url = baseUrl + "/feature";
        driver.get(url);
        waitForPageLoad();
        log.debug("Navigated to: {}", url);
        return this;
    }
    
    // Wait methods
    private void waitForPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        log.debug("Page loaded successfully");
    }
    
    public FeaturePage waitForResult() {
        wait.until(ExpectedConditions.visibilityOf(resultMessage));
        return this;
    }
    
    // Action methods
    public FeaturePage performAction() {
        wait.until(ExpectedConditions.elementToBeClickable(actionButton));
        actionButton.click();
        log.debug("Action performed");
        return this;
    }
    
    // Getter methods
    public String getPageTitle() {
        return pageTitle.getText();
    }
    
    public String getResultMessage() {
        waitForResult();
        return resultMessage.getText();
    }
    
    // Verification methods
    public boolean isPageLoaded() {
        try {
            return pageTitle.isDisplayed() && 
                   driver.getCurrentUrl().contains("/feature");
        } catch (Exception e) {
            log.debug("Page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean isActionSuccessful() {
        try {
            wait.until(ExpectedConditions.textToBePresentInElement(
                resultMessage, "Success"));
            return true;
        } catch (Exception e) {
            log.debug("Action not successful: {}", e.getMessage());
            return false;
        }
    }
}
```

### 3. Running Tests

```bash
# Run all Selenium tests
mvn test -Dtest=*SeleniumTest

# Run specific test class
mvn test -Dtest=CustomerManagementSeleniumTest

# Run specific test method
mvn test -Dtest=CustomerManagementSeleniumTest#shouldCreatePersonalCustomerSuccessfully

# Debugging options
mvn test -Dtest=CustomerManagementSeleniumTest \
         -Dselenium.headless=false \
         -Dselenium.recording.enabled=true \
         -Dlogging.level.id.ac.tazkia.minibank=DEBUG

# Parallel execution control
mvn test -Djunit.jupiter.execution.parallel.enabled=true \
         -Djunit.jupiter.execution.parallel.config.dynamic.factor=0.75

# Profile-specific execution
mvn test -Dtest.profile=remote -Dtest=CustomerManagementSeleniumTest
```

## Troubleshooting Guide

### Common Issues and Solutions

1. **Element Not Found**
   - Verify element has unique ID in template
   - Check wait conditions
   - Enable debug logging to see page source
   - Use VNC viewer to watch test execution

2. **Timing Issues**
   - Increase wait timeout for slow operations
   - Use proper ExpectedConditions
   - Never use Thread.sleep()
   - Check for JavaScript-rendered content

3. **Parallel Execution Conflicts**
   - Ensure schema-per-thread is working
   - Check for shared state in tests
   - Verify container configuration
   - Monitor resource usage

4. **Platform-Specific Issues**
   - Verify correct container image for architecture
   - Check Docker resource allocation
   - Use appropriate test profile
   - Monitor container logs

## Summary

This architecture provides:

1. **Maintainability**: Clear separation of concerns and consistent patterns
2. **Scalability**: Parallel execution support with isolated test environments
3. **Reliability**: Container-based testing ensures consistency
4. **Debuggability**: Comprehensive logging, VNC access, and recording
5. **Flexibility**: Platform-aware configuration and multiple execution modes
6. **Best Practices**: Industry-standard patterns and clean code principles

Following these guidelines ensures high-quality, maintainable Selenium tests that provide reliable feedback on application functionality across different roles and scenarios.