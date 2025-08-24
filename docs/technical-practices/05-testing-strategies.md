# Testing Strategies

## 🧪 **Test Architecture Overview**

### Schema-Per-Thread Integration Testing Strategy

The application implements a sophisticated schema-per-thread isolation strategy for parallel integration testing using TestContainers PostgreSQL and JUnit 5 parallel execution.

#### **Test Infrastructure Architecture**

```
src/test/java/id/ac/tazkia/minibank/
├── config/                             # Test infrastructure configuration
│   ├── BaseIntegrationTest.java        # Base class with @BeforeAll schema setup
│   ├── TestSchemaInitializer.java      # ApplicationContextInitializer for schema config
│   ├── TestSchemaManager.java          # Centralized schema management utility
│   ├── TestDataFactory.java            # Thread-safe test data generation
│   └── ThreadLocalSchemaCustomizer.java # HikariCP connection customization
├── integration/                        # Schema-per-thread integration tests
│   ├── SchemaPerThreadJdbcTemplateTest.java  # JDBC-level tests with lifecycle management
│   ├── SchemaPerThreadJpaTest.java           # JPA-level tests with entity relationships
│   └── service/                              # Service layer integration tests
└── resources/
    └── junit-platform.properties       # JUnit 5 parallel execution configuration
```

## 🏗️ **Core Test Infrastructure Components**

### BaseIntegrationTest - Schema Isolation Foundation

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {TestSchemaInitializer.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
public abstract class BaseIntegrationTest {

    @ServiceConnection
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withLogConsumer(new Slf4jLogConsumer(logger))
            .withStartupTimeout(Duration.ofMinutes(5));
    
    protected static String schemaName;
    protected JdbcTemplate jdbcTemplate;
    
    @BeforeAll
    static void setUpSchema() {
        // Thread-specific schema generation using TestSchemaManager
        schemaName = TestSchemaManager.generateSchemaName();
        
        // Create isolated schema with direct DataSource connection
        try (Connection connection = TestSchemaManager.createConnection()) {
            TestSchemaManager.createSchemaIfNotExists(connection, schemaName);
            
            // Run Flyway migrations in isolated schema
            Flyway flyway = TestSchemaManager.configureFlyway(connection, schemaName);
            flyway.migrate();
        }
    }
}
```

### TestSchemaManager - Centralized Schema Operations

```java
public class TestSchemaManager {
    private static final AtomicLong SCHEMA_COUNTER = new AtomicLong(0);
    
    /**
     * Generates unique schema name using thread name and counter
     * Format: test_threadname_hash8chars
     */
    public static String generateSchemaName() {
        String threadName = Thread.currentThread().getName().toLowerCase()
            .replace('-', '_').replace(' ', '_');
        String hash = Integer.toHexString((threadName + SCHEMA_COUNTER.incrementAndGet()).hashCode())
            .substring(0, 8);
        return "test_" + threadName + "_" + hash;
    }
    
    /**
     * Thread-safe schema creation with proper error handling
     */
    public static void createSchemaIfNotExists(Connection connection, String schemaName) {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA " + schemaName);
        } catch (SQLException e) {
            if (!e.getSQLState().equals("42P06")) { // Schema already exists
                throw new RuntimeException("Failed to create schema: " + schemaName, e);
            }
        }
    }
}
```

### TestDataFactory - Thread-Safe Data Generation

```java
public class TestDataFactory {
    private static final ThreadLocal<Faker> FAKER = ThreadLocal.withInitial(() -> 
        new Faker(new Locale("id", "ID")));
    private static final AtomicLong ACCOUNT_COUNTER = new AtomicLong(8000000000L);
    private static final AtomicLong BRANCH_COUNTER = new AtomicLong(1000);
    
    /**
     * Generates thread-safe unique account numbers
     * Format: 10-digit numbers starting from 8000000000
     */
    public static String generateAccountNumber() {
        return String.valueOf(ACCOUNT_COUNTER.incrementAndGet());
    }
    
    /**
     * Generates realistic Indonesian personal names using Faker
     */
    public static String generateIndonesianPersonName() {
        return FAKER.get().name().firstName() + " " + FAKER.get().name().lastName();
    }
    
    /**
     * Generates thread markers for precise test data cleanup
     * Format: TEST_THREADNAME
     */
    public static String generateThreadMarker() {
        return "TEST_" + Thread.currentThread().getName()
            .toUpperCase().replace('-', '_').replace(' ', '_');
    }
}
```

## ⚡ **JUnit 5 Parallel Execution Configuration**

### junit-platform.properties

```properties
# Enable JUnit 5 parallel execution
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent

# Dynamic strategy with 75% factor for optimal resource utilization  
junit.jupiter.execution.parallel.config.strategy=dynamic
junit.jupiter.execution.parallel.config.dynamic.factor=0.75
```

This configuration enables:
- **Concurrent execution** by default for all test classes and methods
- **Dynamic thread allocation** based on available CPU cores (75% utilization)
- **Thread pool management** automatically handled by JUnit 5

## 🧪 **Test Implementation Patterns**

### JDBC-Level Integration Tests

**SchemaPerThreadJdbcTemplateTest** demonstrates schema isolation using direct JDBC operations:

```java
@Slf4j
@Execution(ExecutionMode.CONCURRENT)
class SchemaPerThreadJdbcTemplateTest extends BaseIntegrationTest {
    
    private String threadMarker;
    
    @BeforeEach
    void setUpTestData() {
        threadMarker = TestDataFactory.generateThreadMarker();
        
        // Insert lifecycle test data
        jdbcTemplate.update(
            "INSERT INTO branches (branch_code, branch_name, address, city, created_by) VALUES (?, ?, ?, ?, ?)",
            TestDataFactory.generateLifecycleCode(),
            TestDataFactory.generateBranchName(),
            TestDataFactory.generateIndonesianAddress(),
            TestDataFactory.generateIndonesianCity(),
            threadMarker
        );
    }
    
    @AfterEach
    void cleanUpTestData() {
        // Clean up only test data for this thread
        int deletedRows = jdbcTemplate.update("DELETE FROM branches WHERE created_by = ?", threadMarker);
        log.info("Cleaned up {} test data rows for thread marker {}", deletedRows, threadMarker);
    }
}
```

### JPA-Level Integration Tests

**SchemaPerThreadJpaTest** demonstrates schema isolation using JPA repositories with JdbcTemplate verification:

```java
@Slf4j
@Execution(ExecutionMode.CONCURRENT)
class SchemaPerThreadJpaTest extends BaseIntegrationTest {
    
    @Autowired private AccountRepository accountRepository;
    @Autowired private BranchRepository branchRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private JdbcTemplate jdbcTemplate;
    
    private Account lifecycleAccount;
    private String threadMarker;
    
    @BeforeEach
    void setUpTestData() {
        threadMarker = TestDataFactory.generateThreadMarker();
        
        // Load migration data for relationships
        Branch migrationBranch = branchRepository.findByBranchCode("HO001")
            .orElseThrow(() -> new IllegalStateException("Migration branch HO001 not found"));
        Customer migrationCustomer = customerRepository.findByCustomerNumber("C1000001")
            .orElseThrow(() -> new IllegalStateException("Migration customer C1000001 not found"));
        Product migrationProduct = productRepository.findByProductCode("TAB001")
            .orElseThrow(() -> new IllegalStateException("Migration product TAB001 not found"));
        
        // Create and persist lifecycle account via repository
        lifecycleAccount = new Account();
        lifecycleAccount.setBranch(migrationBranch);
        lifecycleAccount.setCustomer(migrationCustomer);
        lifecycleAccount.setProduct(migrationProduct);
        lifecycleAccount.setAccountNumber(TestDataFactory.generateAccountNumber());
        lifecycleAccount.setAccountName(TestDataFactory.generateIndonesianPersonName() + " - Account");
        lifecycleAccount.setBalance(TestDataFactory.generateAccountBalance());
        lifecycleAccount.setCreatedBy(threadMarker);
        
        lifecycleAccount = accountRepository.save(lifecycleAccount);
    }
    
    @Test
    void shouldUpdateAccountSuccessfully() {
        // Load account via repository
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(lifecycleAccount.getAccountNumber());
        assertTrue(accountOpt.isPresent());
        
        Account accountToUpdate = accountOpt.get();
        BigDecimal originalBalance = accountToUpdate.getBalance();
        
        // Modify and save via repository
        String newAccountName = TestDataFactory.generateIndonesianPersonName() + " - Updated Account";
        BigDecimal depositAmount = TestDataFactory.generateDepositAmount();
        
        accountToUpdate.setAccountName(newAccountName);
        accountToUpdate.deposit(depositAmount); // Use business method
        accountToUpdate.setUpdatedBy(threadMarker + "_UPDATED");
        
        Account savedAccount = accountRepository.save(accountToUpdate);
        
        // Verify via JdbcTemplate
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        Map<String, Object> result = jdbcTemplate.queryForMap(sql, lifecycleAccount.getAccountNumber());
        
        assertEquals(newAccountName, result.get("account_name"));
        assertTrue(originalBalance.add(depositAmount).compareTo((BigDecimal) result.get("balance")) == 0);
        assertEquals(threadMarker + "_UPDATED", result.get("updated_by"));
    }
}
```

## 📊 **Test Architecture Benefits**

### Schema Isolation Advantages

1. **Complete Data Isolation**: Each test thread operates in its own PostgreSQL schema
2. **Parallel Execution Safety**: No data conflicts between concurrent tests
3. **Migration Data Preservation**: Flyway seed data available in every schema
4. **Realistic Test Environment**: Full database schema with relationships

### Performance Optimization

1. **Dynamic Thread Allocation**: JUnit 5 manages optimal thread count (75% CPU utilization)
2. **@BeforeAll Schema Setup**: One-time schema creation per test class
3. **Efficient Cleanup**: Thread-marker based cleanup preserves migration data
4. **Connection Pool Management**: HikariCP with schema-specific connection customization

## 🛠️ **Test Execution Commands**

### Standard Test Execution

```bash
# Run all integration tests with parallel execution
mvn test

# Run specific test classes
mvn test -Dtest=SchemaPerThreadJdbcTemplateTest
mvn test -Dtest=SchemaPerThreadJpaTest

# Run both schema-per-thread tests
mvn test -Dtest=SchemaPerThreadJdbcTemplateTest,SchemaPerThreadJpaTest

# Run with Maven debugging for thread analysis
mvn test -X -Dtest=SchemaPerThread*
```

### Performance Analysis

```bash
# Run tests with JaCoCo coverage
mvn test jacoco:report

# Extended timeout for complex tests
mvn test -Dmaven.surefire.timeout=600

# Debug mode for troubleshooting
mvn test -Dmaven.surefire.debug
```

## ✅ **Current Test Implementation Status**

### Integration Test Coverage (Implemented)

| Test Class | Coverage Area | Status | Test Methods |
|------------|---------------|--------|--------------| 
| **BaseIntegrationTest** | Schema infrastructure foundation | ✅ Complete | Abstract base |
| **SchemaPerThreadJdbcTemplateTest** | JDBC operations with schema isolation | ✅ Complete | 8 tests |
| **SchemaPerThreadJpaTest** | JPA operations with relationship verification | ✅ Complete | 7 tests |

### Test Infrastructure Components (Implemented)

| Component | Purpose | Status |
|-----------|---------|--------|
| **TestSchemaManager** | Centralized schema operations | ✅ Complete |
| **TestDataFactory** | Thread-safe data generation | ✅ Complete |
| **TestSchemaInitializer** | ApplicationContext schema configuration | ✅ Complete |
| **ThreadLocalSchemaCustomizer** | HikariCP connection customization | ✅ Complete |

### Verification Results

- ✅ **Parallel Execution**: All tests pass concurrently with proper isolation
- ✅ **Schema Isolation**: Each thread operates in unique PostgreSQL schema  
- ✅ **Migration Integration**: Flyway seed data available in all test schemas
- ✅ **Data Cleanup**: Thread-marker based cleanup preserves migration data
- ✅ **JPA-JDBC Integration**: Repository operations verified via JdbcTemplate queries

## 🎯 **Testing Best Practices Implemented**

### Thread Safety

1. **ThreadLocal Faker instances** for locale-specific test data
2. **AtomicLong counters** for unique business key generation
3. **Thread-specific schema names** preventing cross-thread contamination
4. **Thread markers** for precise test data identification and cleanup

### Data Management

1. **Migration data reuse** - Tests leverage Flyway seed data (branches, customers, products)
2. **Realistic test data** - Indonesian banking data using Faker localization
3. **Proper entity relationships** - Foreign key integrity maintained in test data
4. **Business method usage** - Tests use entity business logic (account.deposit(), account.withdraw())

### Performance Optimization

1. **@BeforeAll schema setup** - One-time overhead per test class
2. **Efficient cleanup** - Only test data removed, migration data preserved
3. **Connection pooling** - HikariCP optimized for concurrent schema access
4. **Resource management** - TestContainers lifecycle properly managed

---

*This documentation reflects the actual implemented test architecture focusing on schema-per-thread isolation using TestContainers PostgreSQL, JUnit 5 parallel execution, and comprehensive integration testing patterns.*