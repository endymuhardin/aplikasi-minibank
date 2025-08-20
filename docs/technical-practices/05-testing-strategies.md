# Testing Strategies

## 🧪 **Test Architecture Overview**

### Comprehensive Testing Strategy

The application implements a multi-layered testing strategy with optimized Selenium test execution using TestContainers for UI automation.

#### **Test Types & Infrastructure**

```
src/test/java/id/ac/tazkia/minibank/
├── unit/                              # Pure unit tests
│   └── entity/                        # Entity business logic tests
├── integration/                       # Integration tests with database
│   ├── repository/                    # @DataJpaTest repository tests
│   ├── service/                       # Service layer integration tests
│   └── controller/                    # REST controller tests
├── functional/                        # Feature tests (BDD style)
│   ├── api/                          # API integration tests (Karate)
│   └── web/                          # UI automation tests (Selenium)
├── config/                           # Test configuration infrastructure
│   ├── ParallelSeleniumManager.java  # Selenium WebDriver management
│   └── SeleniumTestProperties.java   # Configuration properties
└── resources/
    ├── fixtures/                     # Test data (CSV files)
    ├── karate/                       # Karate BDD tests
    └── sql/                         # SQL setup/cleanup scripts
```

## 🚀 **Advanced Selenium Test Management**

### Thread-Safe Selenium WebDriver Infrastructure

The application implements sophisticated Selenium test management using TestContainers with resource-aware container allocation and thread-safe WebDriver management.

#### **Key Features**

- **Thread-Safe WebDriver Management**: Isolated WebDriver instances per thread via ThreadLocal
- **Resource-Aware Container Allocation**: Dynamic limits based on system CPU/memory
- **Container Pooling**: Efficient lifecycle management with startup throttling
- **Recording Support**: Optional MP4 test recording with configurable output
- **Multi-Browser Support**: Optimized Chrome and Firefox configurations
- **Memory Optimization**: Conservative resource allocation for parallel stability

#### **ParallelSeleniumManager Architecture**

```java
@Component
public class ParallelSeleniumManager {
    // Thread-local storage for WebDriver instances
    private static final ThreadLocal<WebDriverInstance> threadLocalDriver = new ThreadLocal<>();
    
    // Container pool management
    private static final Map<String, BrowserWebDriverContainer<?>> containerPool = new ConcurrentHashMap<>();
    private static final AtomicInteger containerCounter = new AtomicInteger(0);
    
    // Container startup throttling
    private static volatile Semaphore containerStartupSemaphore;
    
    // System property configuration
    private static final String BROWSER_TYPE = System.getProperty("selenium.browser", "chrome");
    private static final boolean RECORDING_ENABLED = Boolean.parseBoolean(
        System.getProperty("selenium.recording.enabled", "false"));
    private static final boolean HEADLESS_ENABLED = Boolean.parseBoolean(
        System.getProperty("selenium.headless", "true"));
}
```

#### **Resource Management Strategy**

```java
/**
 * Conservative Container Allocation (Current Implementation)
 * --------------------------------------------------------
 * Max containers = min(processors/4, memory_GB/2, 3)
 * 
 * Example for 8-core, 16GB system:
 * - maxByProcessors = 8/4 = 2
 * - maxByMemory = 16384/2048 = 8  
 * - finalMax = min(2, 8, 3) = 2 containers
 */
public int getEffectiveMaxContainers() {
    if (container.maxContainers != null) {
        return container.maxContainers;
    }
    
    int processors = Runtime.getRuntime().availableProcessors();
    long maxMemoryMB = Runtime.getRuntime().maxMemory() / (1024 * 1024);
    
    int maxByProcessors = Math.max(1, processors / 4);        // Very conservative
    int maxByMemory = Math.max(1, (int) (maxMemoryMB / 2048)); // 2GB per container
    
    int calculatedMax = Math.min(maxByProcessors, maxByMemory);
    calculatedMax = Math.min(calculatedMax, 3);  // Hard cap at 3 containers
    calculatedMax = Math.max(calculatedMax, 1);  // Ensure at least 1 container
    
    return calculatedMax;
}
```

#### **Browser Optimization Configurations**

```java
// Chrome optimizations for parallel execution
private static ChromeOptions createChromeOptions() {
    ChromeOptions options = new ChromeOptions();
    
    options.addArguments(
        // Security and stability
        "--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu",
        "--disable-web-security", "--disable-features=VizDisplayCompositor",
        
        // Performance optimizations for parallel execution
        "--disable-extensions", "--disable-plugins", 
        "--disable-images",  // Faster page loads
        "--disable-default-apps", "--disable-background-timer-throttling",
        "--disable-renderer-backgrounding", "--disable-backgrounding-occluded-windows",
        "--disable-client-side-phishing-detection", "--disable-sync",
        
        // Memory optimization for parallel execution
        "--aggressive-cache-discard", "--memory-pressure-off",
        "--max-old-space-size=256"  // Limit memory usage to 256MB
    );
    
    if (HEADLESS_ENABLED) {
        options.addArguments("--headless");
    }
    
    return options;
}

// Firefox optimizations for parallel execution  
private static FirefoxOptions createFirefoxOptions() {
    FirefoxOptions options = new FirefoxOptions();
    
    options.addPreference("dom.webnotifications.enabled", false);
    options.addPreference("media.volume_scale", "0.0");
    options.addPreference("dom.push.enabled", false);
    options.addPreference("dom.webdriver.enabled", false);
    
    // Memory optimization
    options.addPreference("browser.cache.memory.capacity", 32768);  // 32MB cache
    options.addPreference("browser.sessionhistory.max_total_viewers", 2);
    
    return options;
}
```

#### **Configuration Properties**

**application-test.yml:**
```yaml
test:
  selenium:
    container:
      # Resource allocation - can be overridden via system properties
      maxContainers: 2  # Conservative default, system will calculate if not set
      memoryLimitMb: 1024
      cpuQuota: 100000  # 1 full CPU core
      sharedMemoryBytes: 1073741824  # 1GB
      startupTimeoutSeconds: 300  # 5 minutes
      maxStartupRetries: 1  # Fail fast - no retries
      retryDelayMs: 0  # No delay since we don't retry
    
    timeouts:
      # WebDriver timeouts in seconds
      pageLoadTimeoutSeconds: 30
      scriptTimeoutSeconds: 20
      implicitWaitSeconds: 10
      pageObjectWaitSeconds: 30
    
    applicationReadiness:
      # Application startup checking
      enabled: true
      maxWaitTimeSeconds: 60
      pollIntervalMs: 1000
      httpTimeoutSeconds: 3
```

#### **JUnit 5 Parallel Configuration**

**src/test/resources/junit-platform.properties:**
```properties
# Optimized JUnit 5 Configuration for Parallel Selenium Execution

# Enable parallel execution
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
junit.jupiter.execution.parallel.mode.classes.default=concurrent

# Optimized parallel configuration for Selenium tests
junit.jupiter.execution.parallel.config.strategy=dynamic
junit.jupiter.execution.parallel.config.dynamic.factor=1.0
junit.jupiter.execution.parallel.config.dynamic.max-pool-size-factor=2.0
junit.jupiter.execution.parallel.config.dynamic.core-pool-size-factor=0.5
junit.jupiter.execution.parallel.config.dynamic.keep-alive=30s

# Custom parallel configuration for different test types
# Selenium tests: Limited by container resources
junit.jupiter.execution.parallel.config.custom.class.selenium-tests=4
# Repository tests: Database transaction isolated, can run in parallel  
junit.jupiter.execution.parallel.config.custom.class.repository-tests=6
# Integration tests: Database connection limited
junit.jupiter.execution.parallel.config.custom.class.integration-tests=4
# Unit tests: CPU bound, can use more threads
junit.jupiter.execution.parallel.config.custom.class.unit-tests=dynamic

# Optimized timeouts for parallel execution
junit.jupiter.execution.timeout.default=15m
junit.jupiter.execution.timeout.testable.method.default=8m
junit.jupiter.execution.timeout.testable.method.selenium=10m
junit.jupiter.execution.timeout.test.method.default=5m

# Test instance lifecycle
junit.jupiter.testinstance.lifecycle.default=per_class
```

#### **Test Execution Patterns**

**Running Parallel Tests:**
```bash
# Run all tests with parallel execution (default with junit-platform.properties)
mvn test

# Run specific test categories with parallel execution
mvn test -Dtest="*RepositoryTest"              # Repository tests (parallel-safe)
mvn test -Dtest="*ApiTest"                     # API integration tests  
mvn test -Dtest="*SeleniumTest"                # UI tests (resource-limited)

# Selenium-specific execution options
mvn test -Dtest=ProductManagementSeleniumTest                           # Headless mode (default)
mvn test -Dtest=ProductManagementSeleniumTest -Dselenium.headless=false # Visible browser
mvn test -Dtest=ProductManagementSeleniumTest -Dselenium.recording.enabled=true # With recording
mvn test -Dtest=ProductManagementSeleniumTest -Dselenium.browser=firefox        # Firefox browser

# Override container limits for high-resource systems
mvn test -Dtest=*SeleniumTest -Dtest.selenium.container.maxContainers=4
```

**Key Configuration Options:**
```bash
# System Properties for Test Execution
-Dselenium.headless=true|false           # Browser visibility
-Dselenium.recording.enabled=true|false  # Video recording
-Dselenium.browser=chrome|firefox        # Browser type
-Dtest.selenium.container.maxContainers=N # Override container limit
```

## 📊 **Maven Surefire Configuration**

**pom.xml:**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.0.0-M9</version>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
            <include>**/*Runner.java</include>
        </includes>
        <!-- Parallel execution handled by JUnit 5 platform configuration -->
    </configuration>
</plugin>
```

## 🏗️ **Actual Test Architecture Patterns**

### Repository Tests (@DataJpaTest)
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepositoryTest {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Test
    void shouldSavePersonalCustomer() {
        PersonalCustomer customer = new PersonalCustomer();
        customer.setFirstName("Test");
        customer.setLastName("Customer");
        customer.setEmail("test@example.com");
        // ... set required fields
        
        Customer saved = customerRepository.save(customer);
        assertThat(saved.getId()).isNotNull();
    }
}
```

### Karate API Integration Tests  
```java
@Karate.Test
class CustomerRegistrationTest {
    // Tests defined in customer-registration.feature
    // Supports parallel execution through JUnit 5
}
```

### Selenium UI Tests
```java
public class CustomerManagementSeleniumTest extends AbstractSeleniumTestBase {
    
    @Test
    void shouldCreatePersonalCustomer() {
        WebDriver driver = ParallelSeleniumManager.getDriver();
        String baseUrl = ParallelSeleniumManager.getBaseUrl(appPort);
        
        // Test implementation with container-managed WebDriver
        driver.get(baseUrl + "/customer/personal/form");
        // ... test logic
    }
}
```

## 📈 **Performance Benefits & Monitoring**

### Resource Optimization Results
- **Container Resource Management**: Dynamic allocation based on system capabilities
- **Memory Efficiency**: 1GB RAM + 1GB shared memory per Selenium container
- **Browser Optimization**: Disabled images, extensions, and background processes
- **Startup Throttling**: Semaphore-based container startup prevents resource exhaustion

### Container Statistics
```java
// Available through ParallelSeleniumManager
String stats = ParallelSeleniumManager.getContainerStatistics();
// Output: "Active containers: 2/3, Browser: chrome, Headless: true"
```

## 🚀 **Key Takeaways**

### Parallel Test Execution Status
- **JUnit 5 Parallel Execution**: ✅ Enabled with dynamic strategy
- **Selenium Container Management**: ✅ Thread-safe with resource pooling  
- **Conservative Resource Allocation**: ✅ Max 3 containers, 2GB RAM per container
- **Browser Optimization**: ✅ Chrome/Firefox with performance tuning
- **Recording Support**: ✅ Optional MP4 recording for debugging

### Recommended Test Execution
```bash
# Standard parallel test execution
mvn test

# Selenium tests with visible browser (for debugging)
mvn test -Dtest="*SeleniumTest" -Dselenium.headless=false

# High-performance execution with recording
mvn test -Dselenium.recording.enabled=true -Dtest.selenium.container.maxContainers=4
```

---

*This documentation reflects the actual implementation as of the current codebase state, focusing on the sophisticated Selenium test management infrastructure with TestContainers and JUnit 5 parallel execution capabilities.*
