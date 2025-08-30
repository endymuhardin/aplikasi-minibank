# Lessons Learned and Common Pitfalls

Based on commit history analysis, here are critical lessons learned and common pitfalls to avoid:

### UI Testing Lessons (Future Implementation)

#### 1. **AVOID Thread.sleep() in Functional Tests**
❌ **Wrong:**
```java
// Bad practice - leads to flaky tests
try {
    Thread.sleep(1000);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}
```

✅ **Correct:**
```java
// Use Playwright waitFor methods (in functional tests)
page.locator("#elementId").waitFor();
page.locator("#buttonId").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
```

**Lesson:** Thread.sleep() causes brittle tests that fail randomly. Always use Playwright's built-in wait mechanisms.

#### 2. **Use ID Attributes as Primary Locators**
❌ **Wrong:**
```java
// Fragile locators that break with UI changes
By.xpath("//div[@class='form-group'][3]/input")
By.cssSelector("form.customer-form > div:nth-child(2) > input")
```

✅ **Correct:**
```java
// Stable, maintainable locators
By.id("customerNumber")
By.id("submitButton")
By.id("errorMessage")
```

**Lesson:** ID attributes are the most reliable locators. Commit `fa23405` standardized all test locators to use ID attributes for improved reliability.

#### 3. **Handle JavaScript Alerts Properly**
❌ **Wrong:**
```java
// Missing alert handling causes UnhandledAlertException
driver.findElement(By.id("deleteButton")).click();
// Test fails with UnhandledAlertException
```

✅ **Correct:**
```java
// Proper alert handling
driver.findElement(By.id("deleteButton")).click();
Alert alert = driver.switchTo().alert();
alert.accept(); // or alert.dismiss()
```

**Lesson:** Commit `2a8920b` fixed UnhandledAlertException by properly handling JavaScript confirm() dialogs.

#### 4. **Maximize Browser Window for Responsive Design**
❌ **Wrong:**
```java
// Small browser window may hide responsive elements
Browser browser = playwright.chromium().launch();
Page page = browser.newPage();
// Elements may be hidden on mobile breakpoints
```

✅ **Correct:**
```java
// Ensure full desktop view
Browser browser = playwright.chromium().launch();
Page page = browser.newPage();
driver.manage().window().maximize();
```

**Lesson:** Commit `5fb61c7` fixed test failures by maximizing browser window to prevent responsive design issues.

#### 5. **Optimize Browser Initialization in Functional Tests**
❌ **Wrong:**
```java
// Initializing Browser for every test method
@BeforeEach
void setUp() {
    driver = new ChromeDriver(); // Very expensive operation
}
```

✅ **Correct:**
```java
// Shared Browser instance per test class
@BeforeAll
static void setUpClass() {
    setupBrowserOnce(); // Initialize once per class
}
```

**Lesson:** Commit `3e4acf5` optimized test performance by sharing Browser instances, reducing startup overhead.

#### 6. **Comprehensive Logging for Debugging**
❌ **Wrong:**
```java
// Silent test failures are hard to debug
@Test
void shouldCreateCustomer() {
    // No logging when things go wrong
}
```

✅ **Correct:**
```java
// Rich logging with visual indicators
@Test
void shouldCreateCustomer() {
    log.info("🧪 TEST START: shouldCreateCustomer");
    // Test implementation
    log.info("✅ TEST PASS: shouldCreateCustomer completed successfully");
}
```

**Lesson:** Commit `36456a3` added comprehensive logging with emojis for easy identification of test execution flow.

#### 7. **Validate All Required Fields in Forms**
❌ **Wrong:**
```java
// Partial form filling causes validation errors
public void fillForm(String name, String email) {
    // Missing required fields like phone, address
}
```

✅ **Correct:**
```java
// Complete form data for all required fields
public void fillForm(String customerNumber, String firstName, String lastName,
                    String dateOfBirth, String identityType, String idNumber,
                    String email, String phone, String address, String city) {
    // Fill ALL required fields to pass validation
}
```

**Lesson:** Commit `c62330a` fixed validation failures by ensuring all @NotBlank/@NotNull required fields are provided.

### Karate Testing Best Practices

#### 1. **Use HTTP Basic Authentication Instead of Session-Based Auth**
❌ **Wrong:**
```gherkin
# Complex session management
* call read('auth-helper.feature@Login')
* def cookies = responseCookies
# Fragile session handling
```

✅ **Correct:**
```gherkin
# Simple, reliable Basic Auth
Background:
  * def authString = 'teller1:minibank123'
  * def encodedAuth = java.util.Base64.getEncoder().encodeToString(authString.getBytes())
  * configure headers = { Authorization: 'Basic #(encodedAuth)' }
```

**Lesson:** Commit `b726bd5` fixed authentication failures by switching from complex session auth to simple Basic authentication.

#### 2. **Use Correct Permission Names in Security Configuration**
❌ **Wrong:**
```java
// Using non-existent permission codes
.requestMatchers("/api/customers/**").hasAuthority("CUSTOMER_READ")
```

✅ **Correct:**
```java
// Use actual permission codes from database
.requestMatchers("/api/customers/**").hasAuthority("CUSTOMER_VIEW")
```

**Lesson:** Security configuration must match actual permission codes in the database.

#### 3. **Handle JSON Syntax Properly with Variable Interpolation**
❌ **Wrong:**
```gherkin
# Syntax error in JSON with variables
And request { "accountId": #(accountId), "amount": #(amount) }
```

✅ **Correct:**
```gherkin
# Proper JSON syntax with Karate variables
And request 
  """
  {
    "accountId": "#(accountId)",
    "amount": "#(amount)"
  }
  """
```

**Lesson:** Use proper JSON syntax and string interpolation with Karate variables.

### Security and Authentication Pitfalls

#### 1. **CSRF Token Support for Form Submissions**
❌ **Wrong:**
```html
<!-- Form without CSRF token fails in production -->
<form method="post" action="/customers">
    <!-- Missing CSRF token -->
</form>
```

✅ **Correct:**
```html
<!-- Include CSRF token for security -->
<form method="post" action="/customers">
    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
    <!-- Form fields -->
</form>
```

**Lesson:** Commit `6093658` fixed login failures by adding CSRF token support to prevent authentication errors.

#### 2. **Set HttpOnly Flag for Security Cookies**
❌ **Wrong:**
```java
// Missing security flags on cookies
http.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
);
```

✅ **Correct:**
```java
// Secure cookie configuration
http.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
    .sessionCookieHttpOnly(true)
);
```

**Lesson:** Commit `595fb52` fixed SonarCloud security issues by adding HttpOnly flag to prevent XSS attacks.

### Database and JPA Pitfalls

#### 1. **Satisfy All Entity Validation Constraints**
❌ **Wrong:**
```java
// Partial updates that miss required fields
Customer customer = new Customer();
customer.setEmail("new@email.com"); // Missing required fields
customerRepository.save(customer); // Validation error
```

✅ **Correct:**
```java
// Complete entity with all required fields
Customer customer = new Customer();
customer.setCustomerNumber("C1000001");
customer.setEmail("new@email.com");
customer.setPhoneNumber("081234567890");
// Set all @NotBlank/@NotNull fields
customerRepository.save(customer);
```

**Lesson:** Entity validation constraints must be satisfied even during partial updates.

#### 2. **Use Correct Column References in Native Queries**
❌ **Wrong:**
```java
// Using entity field names in native SQL
@Query(value = "SELECT * FROM users WHERE fullName = :name", nativeQuery = true)
```

✅ **Correct:**
```java
// Using actual database column names
@Query(value = "SELECT * FROM users WHERE full_name = :name", nativeQuery = true)
```

**Lesson:** Native queries must use database column names, not entity field names.

### Code Quality and SonarCloud Issues

#### 1. **Add Assertions to Test Methods**
❌ **Wrong:**
```java
// Test without assertions triggers SonarCloud blocker
@Test
void shouldGeneratePassword() {
    String password = passwordGenerator.generate();
    // Missing assertion - SonarCloud blocker
}
```

✅ **Correct:**
```java
// Test with proper assertions
@Test
void shouldGeneratePassword() {
    String password = passwordGenerator.generate();
    assertThat(password).isNotNull();
    assertThat(password).hasSize(10);
}
```

**Lesson:** All test methods must have assertions to satisfy SonarCloud requirements.

#### 2. **Proper Exception Handling with Logging**
❌ **Wrong:**
```java
// Silent exception handling
try {
    riskyOperation();
} catch (Exception e) {
    // Silent failure - SonarCloud issue
}
```

✅ **Correct:**
```java
// Proper exception handling with logging
try {
    riskyOperation();
} catch (Exception e) {
    log.error("Failed to perform operation: {}", e.getMessage(), e);
    throw new BusinessException("Operation failed", e);
}
```

**Lesson:** Commit `0690b5f` added proper error logging to all catch blocks for better debugging.

### Test Infrastructure Lessons

#### 1. **Use TestContainers for Consistent Test Environment**
❌ **Wrong:**
```java
// Tests depend on external database
@SpringBootTest
class CustomerRepositoryTest {
    // Depends on external PostgreSQL instance
}
```

✅ **Correct:**
```java
// Self-contained tests with TestContainers
@DataJpaTest
@Import(PostgresTestContainersConfiguration.class)
class CustomerRepositoryTest {
    // Isolated PostgreSQL container per test
}
```

**Lesson:** TestContainers provide consistent, isolated test environments.

#### 2. **Proper @BeforeEach Method Execution Order**
❌ **Wrong:**
```java
// Inheritance chain with overlapping @BeforeEach methods
@BeforeEach
void setUp() {
    // May not execute in correct order
}
```

✅ **Correct:**
```java
// Manual initialization with proper dependencies
@BeforeEach
void setUp() {
    setupBrowserOnce(); // Explicit dependency management
    ensureAuthentication();
}
```

**Lesson:** Commit `d58c36f` fixed @BeforeEach execution order problems with manual initialization.

### Performance Optimization Lessons

#### 1. **Configure Appropriate Container Timeouts**
❌ **Wrong:**
```java
// Short timeouts cause container initialization failures
container.withStartupTimeout(Duration.ofMinutes(2)); // Too short
```

✅ **Correct:**
```java
// Sufficient timeout for container startup
container.withStartupTimeout(Duration.ofMinutes(5)); // Adequate for CI
```

**Lesson:** Container initialization can be slow in CI environments; use appropriate timeouts.

#### 2. **Use Headless Mode for CI Performance**
❌ **Wrong:**
```java
// Always use GUI mode (slow in CI)
ChromeOptions options = new ChromeOptions();
// No headless mode
```

✅ **Correct:**
```java
// Conditional headless mode
ChromeOptions options = new ChromeOptions();
if (!"false".equals(System.getProperty("playwright.headless", "true"))) {
    options.addArguments("--headless");
}
```

**Lesson:** Use headless mode for CI performance, but allow GUI mode for debugging.
