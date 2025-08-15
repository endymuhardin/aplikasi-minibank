# Technical Practices Guide

This document contains comprehensive technical practices, patterns, conventions, and guidelines extracted from the Aplikasi Mini Bank project to ensure consistent development practices across similar projects.

## Technology Stack

### Backend Technologies
- **Java 21** (LTS version with modern features)
- **Spring Boot 3.5.3** (Latest stable release)
- **Spring Data JPA** (Data access layer)
- **Spring Security 6** (Authentication and authorization)
- **Spring Validation** (Bean validation with Jakarta Validation)
- **Thymeleaf** (Server-side templating engine)
- **Lombok** (Boilerplate code reduction)
- **PostgreSQL 17** (Primary database)
- **Flyway** (Database migration management)

### Frontend Technologies
- **Tailwind CSS 3.4.3** (Utility-first CSS framework)
- **PostCSS 8.4.38** (CSS processing)
- **Autoprefixer 10.4.19** (CSS vendor prefixing)
- **Node.js/NPM** (Frontend build tooling)

### Testing Technologies
- **JUnit 5** (Unit testing framework)
- **AssertJ** (Fluent assertion library)
- **Spring Boot Test** (Integration testing)
- **TestContainers** (Container-based testing)
- **Karate 1.4.1** (BDD API testing)
- **Selenium** (Web UI testing)
- **JaCoCo 0.8.12** (Code coverage)
- **OpenCSV 5.9** (CSV test data)
- **Apache Commons Lang3 3.12.0** (Utility functions)

### Build and Tooling
- **Maven 3.6+** (Build automation)
- **Docker Compose** (Development environment)
- **SonarCloud** (Code quality analysis)
- **Git** (Version control)

## Project Structure and Package Organization

### Source Code Structure
```
src/
├── main/
│   ├── java/id/ac/tazkia/minibank/
│   │   ├── AplikasiMinibankApplication.java    # Main application class
│   │   ├── config/                             # Configuration classes
│   │   │   └── SecurityConfig.java
│   │   ├── controller/                         # Controller layer
│   │   │   ├── rest/                          # REST API controllers
│   │   │   └── web/                           # Web MVC controllers
│   │   ├── dto/                               # Data Transfer Objects
│   │   ├── entity/                            # JPA entities
│   │   ├── repository/                        # Data access layer
│   │   └── service/                           # Business logic layer
│   ├── resources/
│   │   ├── application.properties             # Main configuration
│   │   ├── db/migration/                      # Flyway migrations
│   │   ├── static/                           # Static web assets
│   │   └── templates/                        # Thymeleaf templates
│   └── frontend/                             # Frontend build assets
│       ├── package.json
│       ├── tailwind.config.js
│       ├── postcss.config.js
│       └── src/input.css
└── test/
    ├── java/id/ac/tazkia/minibank/
    │   ├── integration/                       # Integration tests
    │   │   ├── controller/                   # Controller tests
    │   │   ├── repository/                   # Repository tests
    │   │   └── service/                      # Service tests
    │   ├── functional/                       # Functional tests
    │   │   ├── api/                         # API BDD tests
    │   │   └── web/                         # Selenium tests
    │   └── util/                            # Test utilities
    └── resources/
        ├── application-test.properties       # Test configuration
        ├── fixtures/                        # CSV test data
        ├── karate/                          # Karate feature files
        └── sql/                             # Test SQL scripts
```

### Package Naming Conventions
- **Base package**: `id.ac.tazkia.minibank`
- **Domain-driven organization**: Packages organized by layer (controller, service, repository, entity)
- **Clear separation**: REST vs Web controllers in separate packages
- **Test mirroring**: Test packages mirror main source structure

## Coding Conventions

### Entity Design Patterns

#### 1. UUID Primary Keys
```java
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
}
```

#### 2. Audit Fields Pattern
```java
// Standard audit fields in all entities
@CreationTimestamp
@Column(name = "created_date", updatable = false)
private LocalDateTime createdDate;

@Column(name = "created_by", length = 100)
private String createdBy;

@UpdateTimestamp
@Column(name = "updated_date")
private LocalDateTime updatedDate;

@Column(name = "updated_by", length = 100)
private String updatedBy;
```

#### 3. Joined Table Inheritance
```java
@Entity
@Table(name = "customers")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "customer_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Customer {
    // Base fields
    public abstract CustomerType getCustomerType();
    public abstract String getDisplayName();
}

@Entity
@Table(name = "personal_customers")
public class PersonalCustomer extends Customer {
    // Personal-specific fields
}
```

#### 4. Business Logic in Entities
```java
@Entity
public class Account {
    // Entity fields...
    
    // Business methods
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }
    
    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.balance = this.balance.subtract(amount);
    }
}
```

#### 5. Enum Usage Patterns
```java
@Enumerated(EnumType.STRING)
@Column(name = "status", length = 20)
private AccountStatus status = AccountStatus.ACTIVE;

public enum AccountStatus {
    ACTIVE, INACTIVE, CLOSED, FROZEN
}
```

### Validation Patterns

#### 1. Bean Validation Annotations
```java
@NotBlank(message = "Customer number is required")
@Size(max = 50, message = "Customer number must not exceed 50 characters")
@Column(name = "customer_number", unique = true, nullable = false, length = 50)
private String customerNumber;

@NotBlank(message = "Email is required")
@Email(message = "Email should be valid")
@Size(max = 100, message = "Email must not exceed 100 characters")
@Column(name = "email", length = 100)
private String email;
```

#### 2. Custom Validation Messages
```java
// Use descriptive, user-friendly validation messages
@Size(max = 100, message = "Company name must not exceed 100 characters")
@NotNull(message = "Date of birth is required")
@Past(message = "Date of birth must be in the past")
```

### Controller Patterns

#### 1. REST Controller Error Handling
```java
@PostMapping("/personal/register")
public ResponseEntity<Object> registerPersonalCustomer(
        @Valid @RequestBody PersonalCustomer customer, 
        BindingResult bindingResult) {
    
    if (bindingResult.hasErrors()) {
        Map<String, String> errors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }
    
    PersonalCustomer savedCustomer = personalCustomerRepository.save(customer);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
}
```

#### 2. Optional Pattern for Not Found
```java
@GetMapping("/personal/{id}")
public ResponseEntity<PersonalCustomer> getPersonalCustomer(@PathVariable UUID id) {
    return personalCustomerRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
```

#### 3. Search Parameter Handling
```java
@GetMapping("/personal")
public ResponseEntity<List<PersonalCustomer>> getAllPersonalCustomers(
        @RequestParam(required = false) String search) {
    List<PersonalCustomer> customers;
    if (search != null && !search.trim().isEmpty()) {
        customers = personalCustomerRepository.findPersonalCustomersWithSearchTerm(search.trim());
    } else {
        customers = personalCustomerRepository.findAll();
    }
    return ResponseEntity.ok(customers);
}
```

### Service Layer Patterns

#### 1. Transactional Service Methods
```java
@Service
@Transactional
public class SequenceNumberService {
    
    private final SequenceNumberRepository sequenceNumberRepository;
    
    public SequenceNumberService(SequenceNumberRepository sequenceNumberRepository) {
        this.sequenceNumberRepository = sequenceNumberRepository;
    }
    
    public String generateNextSequence(String sequenceName, String prefix) {
        SequenceNumber sequence = getOrCreateSequence(sequenceName, prefix);
        String result = sequence.generateNextSequence();
        sequenceNumberRepository.save(sequence);
        return result;
    }
}
```

#### 2. Constructor Injection
```java
// Always use constructor injection, not field injection
public CustomerRestController(PersonalCustomerRepository personalCustomerRepository,
                            CorporateCustomerRepository corporateCustomerRepository) {
    this.personalCustomerRepository = personalCustomerRepository;
    this.corporateCustomerRepository = corporateCustomerRepository;
}
```

### Financial Data Handling

#### 1. BigDecimal for Money
```java
// Always use BigDecimal for financial amounts
@Column(name = "balance", precision = 20, scale = 2)
private BigDecimal balance = BigDecimal.ZERO;

// Proper BigDecimal comparison
if (this.balance.compareTo(amount) < 0) {
    throw new IllegalArgumentException("Insufficient balance");
}
```

#### 2. Precision and Scale Standards
```java
// Standard precision and scale for financial fields
private BigDecimal amount;           // DECIMAL(20,2) - amounts
private BigDecimal profitRatio;      // DECIMAL(5,4) - ratios/percentages
private BigDecimal interestRate;     // DECIMAL(5,4) - rates
```

## Architectural Patterns

### 1. Layered Architecture
- **Presentation Layer**: Controllers (REST and Web)
- **Business Layer**: Services and rich domain entities
- **Data Access Layer**: Spring Data JPA repositories
- **Database Layer**: PostgreSQL with Flyway migrations

### 2. Repository Pattern
```java
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByCustomer(Customer customer);
    List<Account> findByCustomerId(UUID customerId);
    
    @Query("SELECT a FROM Account a WHERE a.status = :status")
    List<Account> findByStatus(@Param("status") Account.AccountStatus status);
    
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.customer.id = :customerId AND a.status = 'ACTIVE'")
    BigDecimal getTotalBalanceByCustomerId(@Param("customerId") UUID customerId);
}
```

### 3. Domain-Driven Design Elements
- Rich domain entities with business methods
- Value objects for complex types (enums, money)
- Repository pattern for data access abstraction
- Domain services for complex business logic

### 4. Security Architecture
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .build();
    }
}
```

## Testing Strategies

### 1. Test Hierarchy and Organization
```
src/test/java/
├── integration/                    # Integration tests
│   ├── repository/                # @DataJpaTest repository tests
│   ├── controller/                # @SpringBootTest controller tests
│   └── service/                   # Service integration tests
├── functional/                    # End-to-end functional tests
│   ├── api/                      # Karate BDD API tests
│   └── web/                      # Selenium UI tests
└── util/                         # Test utilities and helpers
```

### 2. Repository Testing with TestContainers
```java
@DataJpaTest
@Import(PostgresTestContainersConfiguration.class)
@ActiveProfiles("test")
class AccountRepositoryTest extends BaseRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Test
    void shouldSaveAndFindAccount() {
        // Given
        Account account = createTestAccount();
        
        // When
        Account savedAccount = accountRepository.save(account);
        entityManager.flush();
        
        // Then
        assertThat(savedAccount.getId()).isNotNull();
        assertThat(savedAccount.getAccountNumber()).isEqualTo("A2000001");
    }
}
```

### 3. Parameterized Testing with CSV Data
```java
@ParameterizedTest
@CsvFileSource(resources = "/fixtures/account/accounts.csv", numLinesToSkip = 1)
void shouldSaveAndFindAccountFromCsv(
        String customerNumber,
        String productCode,
        String accountNumber,
        String accountName,
        String balanceStr,
        String status,
        String openedDateStr) {
    
    // Test implementation using CSV data
}
```

### 4. Selenium Testing with Page Objects
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({PostgresTestContainersConfiguration.class})
@ActiveProfiles("test")
public abstract class BaseSeleniumTest extends AbstractSeleniumTestBase {
    
    protected static LoginHelper loginHelper;
    
    @BeforeEach
    void ensureAuthentication() throws Exception {
        setupWebDriverOnce();
        
        if (loginHelper == null) {
            loginHelper = new LoginHelper(driver, baseUrl);
        }
        
        performInitialLogin();
    }
    
    protected void performInitialLogin() {
        loginHelper.loginAsManager();
    }
}
```

### 5. Karate BDD API Testing
```gherkin
Feature: Customer Registration API

  Background:
    * url baseUrl

  Scenario: Register personal customer successfully
    Given path '/api/customers/personal/register'
    And request { firstName: 'John', lastName: 'Doe', email: 'john@example.com' }
    When method post
    Then status 201
    And match response.firstName == 'John'
```

## Database Design Patterns

### 1. Migration Versioning
```sql
-- V001__create_bank_schema.sql
-- V002__insert_initial_data.sql
-- V003__create_user_permission_schema.sql
-- V004__insert_roles_permissions_data.sql
```

### 2. UUID Primary Keys
```sql
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_type VARCHAR(20) NOT NULL CHECK (customer_type IN ('PERSONAL', 'CORPORATE')),
    customer_number VARCHAR(50) UNIQUE NOT NULL
);
```

### 3. Audit Trail Fields
```sql
-- Standard audit fields for all tables
created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
created_by VARCHAR(100),
updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_by VARCHAR(100)
```

### 4. Joined Table Inheritance
```sql
-- Base table
CREATE TABLE customers (...);

-- Specialized tables
CREATE TABLE personal_customers (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    CONSTRAINT fk_personal_customers_id FOREIGN KEY (id) REFERENCES customers(id) ON DELETE CASCADE
);
```

### 5. Performance Indexes
```sql
-- Performance indexes for common queries
CREATE INDEX idx_customers_customer_number ON customers(customer_number);
CREATE INDEX idx_customers_customer_type ON customers(customer_type);
CREATE INDEX idx_accounts_customer ON accounts(id_customers);
CREATE INDEX idx_transactions_account ON transactions(id_accounts);
CREATE INDEX idx_transactions_transaction_date ON transactions(transaction_date);
```

## Configuration and Environment Setup

### 1. Development Environment
```yaml
# compose.yaml
services:
  postgres:
    image: 'postgres:17'
    environment:
      - 'POSTGRES_DB=pgminibank'
      - 'POSTGRES_PASSWORD=minibank1234'
      - 'POSTGRES_USER=minibank'
    ports:
      - '2345:5432'
    volumes:
      - './db-minibank:/var/lib/postgresql/data'
```

### 2. Application Configuration
```properties
# application.properties
spring.application.name=aplikasi-minibank
spring.datasource.url=jdbc:postgresql://localhost:2345/pgminibank
spring.datasource.username=minibank
spring.datasource.password=${DB_PASSWORD:minibank1234}

# Selenium Test Configuration
selenium.recording.enabled=false
```

### 3. Frontend Build Configuration
```javascript
// tailwind.config.js
module.exports = {
  content: [
    "../resources/templates/**/*.html",
    "../resources/static/js/**/*.js"
  ],
  theme: {
    extend: {
      colors: {
        'bsi-primary': '#00a39d',
        'bsi-secondary': '#f15922',
        // Brand-specific color palette
      }
    },
  }
}
```

### 4. Maven Configuration
```xml
<properties>
    <java.version>21</java.version>
    <sonar.organization>tazkia-ac-id</sonar.organization>
    <sonar.host.url>https://sonarcloud.io</sonar.host.url>
    <karate.version>1.4.1</karate.version>
    <jacoco.version>0.8.12</jacoco.version>
</properties>
```

## Build and Deployment Practices

### 1. Maven Build Lifecycle
```bash
# Standard development commands
mvn clean compile                    # Compile source code
mvn test                            # Run all tests
mvn test jacoco:report              # Generate coverage report
mvn spring-boot:run                 # Run application

# Test execution patterns
mvn test -Dtest=AccountRepositoryTest                          # Single test class
mvn test -Dtest=AccountRepositoryTest#shouldFindByCustomerId   # Single test method
mvn test -Dtest="*Selenium*"                                  # Pattern matching
```

### 2. Frontend Build Process
```bash
# Frontend asset building
cd src/main/frontend
npm install                         # Install dependencies
npm run build                       # One-time build
npm run watch                       # Watch mode for development
```

### 3. Database Management
```bash
# Development database operations
docker compose up -d               # Start PostgreSQL
docker compose down -v             # Reset database (removes volume)
docker exec -it aplikasi-minibank-postgres-1 psql -U minibank -d pgminibank
```

### 4. Testing with Different Configurations
```bash
# Selenium testing with various options
mvn test -Dtest=ProductManagementSeleniumTest                                    # Default (headless)
mvn test -Dtest=ProductManagementSeleniumTest -Dselenium.headless=false         # Visible browser
mvn test -Dtest=ProductManagementSeleniumTest -Dselenium.recording.enabled=true # With recording
mvn test -Dtest=ProductManagementSeleniumTest -Dselenium.browser=firefox        # Different browser
```

## Code Quality and Standards

### 1. SonarCloud Configuration
```xml
<properties>
    <sonar.exclusions>
        src/main/resources/static/**,
        src/main/resources/public/**
    </sonar.exclusions>
    <sonar.inclusions>
        **/*.java,
        **/*.properties,
        **/*.xml,
        **/*.html
    </sonar.inclusions>
    <sonar.sources>src/main/java</sonar.sources>
    <sonar.tests>src/test/java</sonar.tests>
</properties>
```

### 2. JaCoCo Coverage Configuration
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>**/AplikasiMinibankApplication.class</exclude>
            <exclude>**/config/**</exclude>
            <exclude>**/dto/**</exclude>
        </excludes>
    </configuration>
</plugin>
```

### 3. Test Naming Conventions
```java
// Unit test naming: should[ExpectedBehavior]When[StateUnderTest]
void shouldReturnTrueWhenAccountIsActive()
void shouldThrowExceptionWhenAmountIsNegative()

// Integration test naming: should[ExpectedOutcome][Context]
void shouldSaveAndFindAccountFromCsv()
void shouldFindAccountsByCustomer()

// Selenium test naming: should[UserAction][Expected Result]
void shouldLoginSuccessfullyWithValidCredentials()
void shouldDisplayErrorMessageWithInvalidCredentials()
```

## Security Best Practices

### 1. Password Security
```java
// BCrypt password hashing
@Service
public class AuthenticationService {
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
    
    public boolean validatePassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}
```

### 2. SQL Injection Prevention
```java
// Use parameterized queries with @Query
@Query("SELECT a FROM Account a WHERE a.customer.id = :customerId AND a.status = 'ACTIVE'")
BigDecimal getTotalBalanceByCustomerId(@Param("customerId") UUID customerId);
```

### 3. Input Validation
```java
// Comprehensive validation at multiple layers
@NotBlank(message = "Email is required")
@Email(message = "Email should be valid")
@Size(max = 100, message = "Email must not exceed 100 characters")
private String email;
```

## Performance Optimization

### 1. Database Performance
```java
// Lazy loading for collections
@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
@JsonIgnore
private List<Account> accounts;

// Strategic indexing
CREATE INDEX idx_transactions_account_date ON transactions(id_accounts, transaction_date);
```

### 2. Repository Query Optimization
```java
// Custom queries for specific needs
@Query("SELECT a FROM Account a JOIN FETCH a.customer JOIN FETCH a.product WHERE a.accountNumber = :accountNumber")
Optional<Account> findByAccountNumberWithDetails(@Param("accountNumber") String accountNumber);
```

## Documentation Standards

### 1. Code Documentation
```java
/**
 * Service for managing sequence number generation.
 * Provides thread-safe sequence generation for business keys like account numbers.
 */
@Service
@Transactional
public class SequenceNumberService {
    
    /**
     * Generates the next sequence number with the specified prefix.
     * 
     * @param sequenceName the name of the sequence
     * @param prefix the prefix to prepend to the number
     * @return formatted sequence string (e.g., "TXN0000001")
     */
    public String generateNextSequence(String sequenceName, String prefix) {
        // Implementation
    }
}
```

### 2. API Documentation Patterns
```java
// RESTful endpoint documentation
/**
 * Register a new personal customer.
 * 
 * @param customer the customer data
 * @param bindingResult validation results
 * @return HTTP 201 with customer data or HTTP 400 with validation errors
 */
@PostMapping("/personal/register")
public ResponseEntity<Object> registerPersonalCustomer(
    @Valid @RequestBody PersonalCustomer customer, 
    BindingResult bindingResult) {
    // Implementation
}
```

## Monitoring and Observability

### 1. Logging Patterns
```java
@Slf4j
public class CustomerService {
    
    public Customer createCustomer(Customer customer) {
        log.info("Creating customer with type: {}", customer.getCustomerType());
        
        try {
            Customer saved = customerRepository.save(customer);
            log.info("Successfully created customer with ID: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Failed to create customer: {}", e.getMessage(), e);
            throw e;
        }
    }
}
```

### 2. Test Execution Monitoring
```java
@Slf4j
public class BaseSeleniumTest {
    
    @BeforeEach
    void ensureAuthentication() {
        String testClass = this.getClass().getSimpleName();
        log.info("✅ LOGIN HELPER READY: {} initialized successfully", testClass);
        log.info("🔑 AUTHENTICATION: {} performing authentication", testClass);
    }
}
```

## Lessons Learned and Common Pitfalls

Based on commit history analysis, here are critical lessons learned and common pitfalls to avoid:

### Selenium Testing Lessons

#### 1. **AVOID Thread.sleep() in Selenium Tests**
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
// Use WebDriverWait with ExpectedConditions
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.presenceOfElementLocated(By.id("elementId")));
wait.until(ExpectedConditions.elementToBeClickable(By.id("buttonId")));
```

**Lesson:** Thread.sleep() causes brittle tests that fail randomly. Always use explicit waits with ExpectedConditions.

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
WebDriver driver = new ChromeDriver();
// Elements may be hidden on mobile breakpoints
```

✅ **Correct:**
```java
// Ensure full desktop view
WebDriver driver = new ChromeDriver();
driver.manage().window().maximize();
```

**Lesson:** Commit `5fb61c7` fixed test failures by maximizing browser window to prevent responsive design issues.

#### 5. **Optimize WebDriver Initialization**
❌ **Wrong:**
```java
// Initializing WebDriver for every test method
@BeforeEach
void setUp() {
    driver = new ChromeDriver(); // Very expensive operation
}
```

✅ **Correct:**
```java
// Shared WebDriver instance per test class
@BeforeAll
static void setUpClass() {
    setupWebDriverOnce(); // Initialize once per class
}
```

**Lesson:** Commit `3e4acf5` optimized test performance by sharing WebDriver instances, reducing container startup overhead.

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
    setupWebDriverOnce(); // Explicit dependency management
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
if (!"false".equals(System.getProperty("selenium.headless", "true"))) {
    options.addArguments("--headless");
}
```

**Lesson:** Use headless mode for CI performance, but allow GUI mode for debugging.

## Database Schema Design Conventions

Based on analysis of the Flyway migration files, here are the standardized database design conventions:

### Primary Key Standards

#### 1. **UUID Primary Keys (Mandatory)**
```sql
-- All tables use UUID primary keys with auto-generation
CREATE TABLE table_name (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- Other fields
);
```

**Why UUID:**
- Security: No sequential enumeration attacks
- Distributed system compatibility
- Merge conflicts reduced in multi-developer environments
- Future-proof for microservices architecture

#### 2. **Primary Key Naming**
- Always use `id` as the primary key column name
- Never use `table_name_id` for primary keys
- Use `UUID` data type with `gen_random_uuid()` default

### Foreign Key Conventions

#### 1. **Foreign Key Naming Pattern**
```sql
-- Pattern: id_{referenced_table_name} (singular)
id_customers UUID NOT NULL,  -- References customers.id
id_products UUID NOT NULL,   -- References products.id
id_accounts UUID NOT NULL,   -- References accounts.id

-- For junction tables or special cases:
id_accounts_destination UUID, -- References accounts.id (special purpose)
```

#### 2. **Foreign Key Constraint Naming**
```sql
-- Pattern: fk_{current_table}_{referenced_table}
CONSTRAINT fk_accounts_customers FOREIGN KEY (id_customers) REFERENCES customers(id),
CONSTRAINT fk_accounts_products FOREIGN KEY (id_products) REFERENCES products(id),
CONSTRAINT fk_user_roles_users FOREIGN KEY (id_users) REFERENCES users(id) ON DELETE CASCADE,
```

#### 3. **Cascade Rules**
```sql
-- Use CASCADE for dependent data (owned relationships)
ON DELETE CASCADE  -- Child records deleted when parent is deleted

-- Examples:
-- personal_customers -> customers (CASCADE - personal data owned by customer)
-- user_passwords -> users (CASCADE - passwords owned by user)
-- user_roles -> users (CASCADE - role assignments owned by user)
```

### Monetary and Financial Data Types

#### 1. **Money Amount Fields**
```sql
-- Large amounts (account balances, transaction amounts, limits)
balance DECIMAL(20,2) DEFAULT 0.00,
amount DECIMAL(20,2) NOT NULL,
minimum_opening_balance DECIMAL(20,2) DEFAULT 0.00,
daily_withdrawal_limit DECIMAL(20,2),
```

**Standard: DECIMAL(20,2)**
- Precision: 20 total digits
- Scale: 2 decimal places
- Range: Up to 999,999,999,999,999,999.99
- Suitable for: Account balances, transaction amounts, large limits

#### 2. **Fee and Small Amount Fields**
```sql
-- Fees and smaller amounts
monthly_maintenance_fee DECIMAL(15,2) DEFAULT 0.00,
atm_withdrawal_fee DECIMAL(15,2) DEFAULT 0.00,
inter_bank_transfer_fee DECIMAL(15,2) DEFAULT 0.00,
```

**Standard: DECIMAL(15,2)**
- Precision: 15 total digits
- Scale: 2 decimal places  
- Range: Up to 9,999,999,999,999.99
- Suitable for: Fees, small amounts, service charges

#### 3. **Ratio and Percentage Fields**
```sql
-- Profit sharing ratios, interest rates, percentages
profit_sharing_ratio DECIMAL(5,4) DEFAULT 0.0000,
nisbah_customer DECIMAL(5,4),
nisbah_bank DECIMAL(5,4),
```

**Standard: DECIMAL(5,4)**
- Precision: 5 total digits
- Scale: 4 decimal places
- Range: 0.0000 to 1.0000 (0% to 100% with 4 decimal precision)
- Suitable for: Ratios, percentages, profit sharing rates

#### 4. **Currency Standards**
```sql
-- Always include currency field for international compatibility
currency VARCHAR(3) DEFAULT 'IDR',
-- Use ISO 4217 currency codes (USD, EUR, IDR, etc.)
```

### Table and Column Naming Standards

#### 1. **Table Names**
- Use **plural nouns** for table names
- Use **snake_case** (lowercase with underscores)
- Examples: `customers`, `accounts`, `transactions`, `user_roles`, `role_permissions`

#### 2. **Column Names**
- Use **snake_case** (lowercase with underscores)
- Use descriptive names without abbreviations
- Boolean fields start with `is_` or use clear boolean meaning
- Date/timestamp fields end with appropriate suffix

```sql
-- Good column naming examples
customer_number VARCHAR(50),           -- Clear, descriptive
phone_number VARCHAR(20),              -- No abbreviation
is_active BOOLEAN,                     -- Clear boolean prefix
is_locked BOOLEAN,                     -- Clear boolean prefix
created_date TIMESTAMP,                -- Clear timestamp suffix
last_login TIMESTAMP,                  -- Clear meaning
failed_login_attempts INTEGER,         -- Descriptive count field
company_registration_number VARCHAR(100), -- Full descriptive name
```

#### 3. **Business Key Fields**
```sql
-- Business identifiers follow consistent pattern
customer_number VARCHAR(50) UNIQUE NOT NULL,
account_number VARCHAR(50) UNIQUE NOT NULL,
transaction_number VARCHAR(50) UNIQUE NOT NULL,
product_code VARCHAR(20) UNIQUE NOT NULL,
username VARCHAR(50) UNIQUE NOT NULL,
role_code VARCHAR(50) UNIQUE NOT NULL,
permission_code VARCHAR(100) UNIQUE NOT NULL,
```

**Standards:**
- Use `_number` for sequential business identifiers
- Use `_code` for categorical/type identifiers
- Always add `UNIQUE NOT NULL` constraints
- Use appropriate varchar lengths based on business rules

### Audit Fields Pattern

#### 1. **Standard Audit Fields (All Tables)**
```sql
-- Standard audit trail fields for all tables
created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
created_by VARCHAR(100),
updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_by VARCHAR(100)
```

#### 2. **Additional Audit Fields (When Needed)**
```sql
-- For user authentication and security
last_login TIMESTAMP,
failed_login_attempts INTEGER DEFAULT 0,
locked_until TIMESTAMP,

-- For business processes
assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
granted_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
opened_date DATE DEFAULT CURRENT_DATE,
closed_date DATE,
```

### Constraint Naming Conventions

#### 1. **Check Constraints**
```sql
-- Pattern: chk_{table_name}_{description} or chk_{field_description}
CONSTRAINT chk_minimum_balances CHECK (minimum_opening_balance >= 0 AND minimum_balance >= 0),
CONSTRAINT chk_profit_sharing_ratio CHECK (profit_sharing_ratio >= 0 AND profit_sharing_ratio <= 1),
CONSTRAINT chk_nisbah_customer CHECK (nisbah_customer IS NULL OR (nisbah_customer >= 0 AND nisbah_customer <= 1)),
CONSTRAINT chk_fees_positive CHECK (monthly_maintenance_fee >= 0 AND atm_withdrawal_fee >= 0),
```

#### 2. **Unique Constraints**
```sql
-- Pattern: uk_{table_name}_{field_names} or use UNIQUE on column
CONSTRAINT uk_user_roles UNIQUE (id_users, id_roles),
CONSTRAINT uk_role_permissions UNIQUE (id_roles, id_permissions),

-- Or inline unique constraints
customer_number VARCHAR(50) UNIQUE NOT NULL,
product_code VARCHAR(20) UNIQUE NOT NULL,
```

#### 3. **Enum-Style Check Constraints**
```sql
-- Use descriptive enum values in uppercase
CHECK (customer_type IN ('PERSONAL', 'CORPORATE')),
CHECK (status IN ('ACTIVE', 'INACTIVE', 'CLOSED', 'FROZEN')),
CHECK (identity_type IN ('KTP', 'PASSPORT', 'SIM')),
CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT', 'INTEREST', 'FEE')),
```

### Index Naming Conventions

#### 1. **Standard Index Naming Pattern**
```sql
-- Pattern: idx_{table_name}_{column_name(s)}
CREATE INDEX idx_customers_customer_number ON customers(customer_number);
CREATE INDEX idx_customers_customer_type ON customers(customer_type);
CREATE INDEX idx_accounts_customer ON accounts(id_customers);
CREATE INDEX idx_transactions_account ON transactions(id_accounts);
```

#### 2. **Composite Index Naming**
```sql
-- Pattern: idx_{table_name}_{column1}_{column2}
CREATE INDEX idx_personal_customers_name ON personal_customers(first_name, last_name);
CREATE INDEX idx_user_roles_user_role ON user_roles(id_users, id_roles);
```

#### 3. **Conditional Index Pattern**
```sql
-- Include WHERE clause for nullable fields or specific conditions
CREATE INDEX idx_customers_email ON customers(email) WHERE email IS NOT NULL;
CREATE INDEX idx_transactions_reference_number ON transactions(reference_number) WHERE reference_number IS NOT NULL;
CREATE INDEX idx_users_email ON users(email) WHERE email IS NOT NULL;
```

#### 4. **Common Index Patterns**
```sql
-- Foreign key indexes (mandatory for performance)
CREATE INDEX idx_accounts_customer ON accounts(id_customers);
CREATE INDEX idx_accounts_product ON accounts(id_products);
CREATE INDEX idx_transactions_account ON transactions(id_accounts);

-- Business key indexes (for frequent lookups)
CREATE INDEX idx_customers_customer_number ON customers(customer_number);
CREATE INDEX idx_products_product_code ON products(product_code);
CREATE INDEX idx_users_username ON users(username);

-- Status and type indexes (for filtering)
CREATE INDEX idx_accounts_status ON accounts(status);
CREATE INDEX idx_products_is_active ON products(is_active);
CREATE INDEX idx_users_is_active ON users(is_active);

-- Temporal indexes (for date-based queries)
CREATE INDEX idx_transactions_transaction_date ON transactions(transaction_date);
CREATE INDEX idx_users_last_login ON users(last_login);
```

### Data Type Standards

#### 1. **String Fields**
```sql
-- Short identifiers and codes
VARCHAR(20)  -- product_code, status fields, type fields
VARCHAR(50)  -- customer_number, account_number, username, role_code
VARCHAR(100) -- names, email, permission_code, description fields
VARCHAR(200) -- longer names like account_name, company_name
VARCHAR(255) -- password_hash, long identifiers
TEXT         -- descriptions, addresses, large text content
```

#### 2. **Numeric Fields**
```sql
INTEGER      -- counts, limits, age, attempts
BIGINT       -- sequence numbers, large counts
DECIMAL(20,2) -- money amounts, balances
DECIMAL(15,2) -- fees, small amounts
DECIMAL(5,4)  -- ratios, percentages
```

#### 3. **Temporal Fields**
```sql
DATE         -- date_of_birth, opened_date, launch_date
TIMESTAMP    -- created_date, updated_date, transaction_date, last_login
```

#### 4. **Boolean Fields**
```sql
BOOLEAN DEFAULT true   -- is_active, is_default, is_locked
BOOLEAN DEFAULT false  -- is_locked, require_maintaining_balance
```

### Migration File Organization

#### 1. **File Naming Convention**
```
V001__create_bank_schema.sql           -- Core business schema
V002__insert_initial_data.sql          -- Reference/seed data
V003__create_user_permission_schema.sql -- Security/RBAC schema
V004__insert_roles_permissions_data.sql -- Security seed data
V005__add_new_feature_schema.sql       -- Feature additions
```

#### 2. **Migration Structure Pattern**
```sql
-- 1. Create tables with all constraints
CREATE TABLE table_name (...);

-- 2. Add foreign key constraints
ALTER TABLE child_table ADD CONSTRAINT fk_name ...;

-- 3. Create indexes for performance
CREATE INDEX idx_table_column ON table_name(column);

-- 4. Insert reference/seed data (if applicable)
INSERT INTO table_name VALUES (...);
```

### Business Logic Constraints

#### 1. **Financial Validation Rules**
```sql
-- Ensure balances are non-negative
CONSTRAINT chk_balance_non_negative CHECK (balance >= 0),

-- Ensure fees are positive
CONSTRAINT chk_fees_positive CHECK (
    monthly_maintenance_fee >= 0 AND 
    atm_withdrawal_fee >= 0 AND 
    inter_bank_transfer_fee >= 0
),

-- Ensure profit sharing ratios sum to 1.0 for Islamic banking
CONSTRAINT chk_nisbah_sum CHECK (
    (profit_sharing_type IN ('MUDHARABAH', 'MUSHARAKAH') 
     AND nisbah_customer IS NOT NULL 
     AND nisbah_bank IS NOT NULL 
     AND nisbah_customer + nisbah_bank = 1.0)
    OR (profit_sharing_type NOT IN ('MUDHARABAH', 'MUSHARAKAH'))
),
```

#### 2. **Date Validation Rules**
```sql
-- Ensure logical date relationships
CONSTRAINT chk_closed_date CHECK (closed_date IS NULL OR closed_date >= opened_date),
CONSTRAINT chk_launch_retirement_date CHECK (retirement_date IS NULL OR launch_date IS NULL OR launch_date <= retirement_date),
CONSTRAINT chk_customer_age CHECK (min_customer_age IS NULL OR max_customer_age IS NULL OR min_customer_age <= max_customer_age),
```

#### 3. **Transaction Balance Rules**
```sql
-- Ensure transaction balance calculations are correct
CONSTRAINT chk_balance_calculation CHECK (
    (transaction_type IN ('DEPOSIT', 'TRANSFER_IN', 'INTEREST') AND balance_after = balance_before + amount)
    OR (transaction_type IN ('WITHDRAWAL', 'TRANSFER_OUT', 'FEE') AND balance_after = balance_before - amount)
),
```

### Sequence Management Pattern

#### 1. **Sequence Numbers Table**
```sql
CREATE TABLE sequence_numbers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sequence_name VARCHAR(50) UNIQUE NOT NULL,
    last_number BIGINT NOT NULL DEFAULT 0,
    prefix VARCHAR(10),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Initialize sequences
INSERT INTO sequence_numbers (sequence_name, last_number, prefix) VALUES 
    ('CUSTOMER_NUMBER', 1000000, 'C'),
    ('ACCOUNT_NUMBER', 2000000, 'A'),
    ('TRANSACTION_NUMBER', 3000000, 'T');
```

#### 2. **Business Key Generation Pattern**
- Use sequence table for thread-safe number generation
- Include meaningful prefixes (C for Customer, A for Account, T for Transaction)
- Start with high numbers to avoid conflicts with test data
- Use service layer for sequence generation to ensure consistency

These database schema conventions ensure consistency, performance, security, and maintainability across all projects. They should be strictly followed to maintain standard practices and enable smooth team collaboration.

This technical practices guide should be used as a reference for maintaining consistency across projects and ensuring adherence to established patterns and conventions.