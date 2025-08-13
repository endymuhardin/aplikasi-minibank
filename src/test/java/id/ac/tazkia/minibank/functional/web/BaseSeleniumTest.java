package id.ac.tazkia.minibank.functional.web;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import id.ac.tazkia.minibank.functional.web.helper.LoginHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for Selenium tests with full security enabled.
 * All tests extending this class will use production security configuration
 * and must authenticate before accessing protected resources.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({PostgresTestContainersConfiguration.class})
@ActiveProfiles("test")
@Slf4j
public abstract class BaseSeleniumTest extends AbstractSeleniumTestBase {
    
    protected static LoginHelper loginHelper;
    
    @BeforeEach
    void ensureAuthentication() throws Exception {
        String testClass = this.getClass().getSimpleName();
        
        // Setup WebDriver once per test class (this gives us the main optimization)
        setupWebDriverOnce();
        
        if (loginHelper == null) {
            loginHelper = new LoginHelper(driver, baseUrl);
            log.info("✅ LOGIN HELPER READY: {} initialized successfully", testClass);
        }
        
        // Perform authentication for each test to ensure clean state
        log.info("🔑 AUTHENTICATION: {} performing authentication", testClass);
        performInitialLogin();
    }
    
    protected void performInitialLogin() {
        // Default implementation - subclasses can override for specific user types
        loginHelper.loginAsManager();
    }
}