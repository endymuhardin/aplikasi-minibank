# Selenium Testing Guide

This document provides guidance for running and extending the Selenium test framework for the Minibank application.

## Architecture Overview

The Selenium test framework is built with the following architectural principles:

- **PageObject Pattern**: Each web page is represented by a Page Object class
- **TestContainers**: Both PostgreSQL and Selenium containers for full isolation
- **Schema Per Thread**: Each test thread gets its own database schema
- **Parallel Execution**: JUnit 5 parallel execution with 75% dynamic factor
- **Profile-Based Configuration**: Different settings for M1 Mac, Intel Mac, and remote server

## Framework Components

### Base Classes

- `BaseSeleniumTest`: Extends `BaseIntegrationTest` with Selenium-specific setup
- `BaseIntegrationTest`: Provides PostgreSQL container and schema-per-thread isolation
- `SeleniumContainerFactory`: Creates appropriate Selenium containers based on platform

### Page Objects

- `LoginPage`: Handles login functionality and form interactions
- `DashboardPage`: Handles dashboard navigation and role-specific element verification

### Configuration

- `SeleniumContainerFactory`: Platform-aware container creation
- `TestSchemaManager`: Schema generation and management utilities

## Running Tests

### Local Development (Automatic Detection)
```bash
# Automatic architecture detection - uses Seleniarm on M1/M2, standard Chrome on Intel
mvn test -Dtest=LoginSeleniumTest

# Run specific test method
mvn test -Dtest=LoginSeleniumTest#shouldLoginSuccessfullyWithValidCredentials

# With recording enabled
mvn test -Dselenium.recording.enabled=true -Dtest=LoginSeleniumTest

# With visible browser for debugging
mvn test -Dselenium.headless=false -Dtest=LoginSeleniumTest
```

### Local Development (Manual Profile Override)
```bash
# Force M1 Seleniarm profile (useful for testing)
mvn test -Dtest.profile=local-m1 -Dtest=LoginSeleniumTest

# Force standard Chrome profile (useful for debugging on M1)
mvn test -Dtest.profile=local-default -Dtest=LoginSeleniumTest
```

### Remote Build Server
```bash
# Full-featured execution with recording enabled
mvn test -Dtest.profile=remote -Dtest=LoginSeleniumTest

# With additional debugging options
mvn test -Dtest.profile=remote -Dselenium.headless=false -Dtest=LoginSeleniumTest
```

## Test Profiles

### Automatic Detection (Default)
- **Detection**: Automatic based on system architecture
- **M1/M2 Mac**: Uses Seleniarm Chromium for native ARM64 performance
- **Intel/AMD64**: Uses standard Selenium Chrome
- **Resources**: Optimized per architecture
- **Parallelism**: 2 threads (local), 4 threads (remote)
- **Recording**: Configurable via `-Dselenium.recording.enabled=true`
- **Headless**: Configurable via `-Dselenium.headless=false`

### local-m1 Profile (Manual Override)
- **Platform**: ARM64 (M1/M2 Mac) - Forced Seleniarm
- **Container**: Seleniarm Chromium
- **Resources**: Minimal (512MB shared memory, 1GB JVM)
- **Parallelism**: 2 threads
- **Use Case**: Testing Seleniarm specifically or troubleshooting

### local-default Profile (Manual Override)
- **Platform**: Any - Forced standard Selenium
- **Container**: Standard Selenium Chrome (runs via Rosetta on M1)
- **Resources**: Medium (1GB shared memory, 1GB JVM)
- **Parallelism**: 2 threads  
- **Use Case**: Debugging issues on M1 with standard Chrome

### remote Profile
- **Platform**: x86_64 remote server
- **Container**: Standard Selenium Chrome
- **Resources**: High (2GB shared memory, 2GB JVM)
- **Parallelism**: 4 threads
- **Headless**: Configurable (default: disabled for debugging)
- **Recording**: Enabled by default
- **Use Case**: CI/CD and remote build servers

## Test Data

### Login Credentials
Test credentials are defined in `src/test/resources/fixtures/selenium/login-credentials.csv`:

| Username | Password | Role | Description |
|----------|----------|------|-------------|
| admin | minibank123 | ADMIN | System Administrator |
| manager1 | minibank123 | MANAGER | Branch Manager 1 |
| cs1 | minibank123 | CUSTOMER_SERVICE | Customer Service 1 |
| teller1 | minibank123 | TELLER | Bank Teller 1 |

## Writing New Tests

### 1. Create Page Object

```java
@Slf4j
public class NewPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    @FindBy(id = "element-id")
    private WebElement elementField;
    
    public NewPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    
    public NewPage performAction() {
        wait.until(ExpectedConditions.elementToBeClickable(elementField));
        elementField.click();
        return this;
    }
}
```

### 2. Create Test Class

```java
@Slf4j
@DisplayName("New Feature Selenium Tests")
class NewFeatureSeleniumTest extends BaseSeleniumTest {

    @Test
    @DisplayName("Should perform new feature functionality")
    void shouldPerformNewFeature() {
        // Test implementation
        LoginPage loginPage = new LoginPage(driver);
        DashboardPage dashboard = loginPage.navigateTo(baseUrl)
                                           .loginWith("admin", "minibank123");
        
        // Assertions
        assertTrue(dashboard.isDashboardLoaded());
    }
}
```

## Element Location Strategy

**IMPORTANT**: Only use ID selectors for element location. This ensures:
- Maximum test stability
- Clear element identification
- Consistent locator strategy

### Required IDs in Templates

All interactive elements must have unique IDs:

```html
<!-- Login form elements -->
<input type="text" id="username" name="username" />
<input type="password" id="password" name="password" />
<button type="submit" id="login-button">Sign In</button>

<!-- Dashboard elements -->
<h1 id="page-title">Dashboard</h1>
<div id="dashboard-content">...</div>
<a id="product-management-link" href="/product/list">Product Management</a>
```

## Parallel Execution

The framework uses JUnit 5 parallel execution with the following configuration:

- **Strategy**: Dynamic with 75% processor factor
- **Mode**: Concurrent at class and method level
- **Schema Isolation**: Each thread gets unique database schema
- **Container Sharing**: Selenium container is shared per test class
- **Database Isolation**: PostgreSQL container is shared, schemas are isolated

## Troubleshooting

### Common Issues

1. **Container Startup Failures**
   - Ensure Docker is running
   - Check available memory (minimum 2GB recommended)
   - Verify network connectivity

2. **Element Not Found**
   - Verify element IDs exist in templates
   - Check wait conditions and timeouts
   - Enable debug logging: `-Dlogging.level.id.ac.tazkia.minibank=DEBUG`

3. **Schema Conflicts**
   - Each test gets unique schema automatically
   - Check logs for schema creation/cleanup issues
   - Verify PostgreSQL container is healthy

4. **Performance Issues**
   - Use appropriate test profile for your system
   - Reduce parallel thread count if needed
   - Monitor Docker resource usage

### Debug Options

```bash
# Enable debug logging
mvn test -Dlogging.level.id.ac.tazkia.minibank=DEBUG

# Run with visible browser (non-headless) + VNC viewing
mvn test -Dselenium.headless=false -Dtest=LoginSeleniumTest

# Enable recording
mvn test -Dselenium.recording.enabled=true -Dtest=LoginSeleniumTest

# Combined: visible browser + recording + debug
mvn test -Dselenium.headless=false -Dselenium.recording.enabled=true -Dlogging.level.id.ac.tazkia.minibank=DEBUG -Dtest=LoginSeleniumTest

# Single thread execution
mvn test -Djunit.jupiter.execution.parallel.enabled=false
```

### VNC Viewer Access

When running with `-Dselenium.headless=false`, the test framework will log VNC connection details:

```
═══════════════════════════════════════════════════════════
🖥️  VNC Viewer Information:
   Address: localhost:32768
   Password: secret (default VNC password)
   You can connect using any VNC client to view the browser
   Example: open vnc://localhost:32768 (macOS) or use TigerVNC/RealVNC
   Note: If 'secret' doesn't work, try no password or 'noVNC'
═══════════════════════════════════════════════════════════
```

**macOS**: Use built-in Screen Sharing - open Finder, press `Cmd+K`, enter `vnc://localhost:PORT`
**Windows/Linux**: Use TigerVNC, RealVNC, or similar VNC client

### Test Recording

When recording is enabled with `-Dselenium.recording.enabled=true`:
- Video files are saved to `target/selenium-recordings/` directory (auto-created)
- All test sessions are recorded (TestContainers RECORD_ALL mode)
- Recordings are in FLV format, viewable with VLC or similar players
- **Recording Finalization**: Tests may take extra time to complete as videos are processed
- **File Generation**: Videos appear in the directory after test completion (may take 10-30 seconds)
- Useful for debugging failed tests, demonstrations, or test documentation
- Directory and files are automatically managed - no manual cleanup needed

**Recording Behavior:**
- Videos are generated with timestamps in the filename
- Failed tests and successful tests are both recorded
- Recording process runs in background during test execution
- Final video files appear after TestContainers cleanup completes

**Troubleshooting Recordings:**
- If no files appear, check that Docker has sufficient resources
- Large test suites may take longer to finalize all recordings
- VNC must be available in the container (automatically handled by framework)

## Best Practices

### Test Design
1. Keep tests focused on single functionality
2. Use meaningful test names and descriptions
3. Implement proper wait strategies (avoid Thread.sleep)
4. Clean up test data when necessary

### Page Objects
1. Use descriptive method names
2. Return page objects for method chaining
3. Implement wait conditions in page methods
4. Add logging for debugging

### Maintenance
1. Update element IDs when UI changes
2. Keep test data files current
3. Review and update role-specific verifications
4. Monitor test execution times and optimize

## Integration with CI/CD

The framework integrates with the existing remote build system:

```bash
# Local development
./devops/start-remote-build.sh --test="LoginSeleniumTest"

# With specific profile
./devops/start-remote-build.sh --opts="-Dtest.profile=remote -Dtest=LoginSeleniumTest"
```

The remote build server automatically uses the `remote` profile for optimal resource utilization and comprehensive test execution.