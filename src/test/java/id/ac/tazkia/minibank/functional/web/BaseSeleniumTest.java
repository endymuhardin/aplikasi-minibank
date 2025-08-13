package id.ac.tazkia.minibank.functional.web;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import id.ac.tazkia.minibank.config.SeleniumTestContainerSingleton;
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
    
    protected LoginHelper loginHelper;
    
    @BeforeEach
    void initializeLoginHelper() throws Exception {
        String testClass = this.getClass().getSimpleName();
        log.info("🔐 LOGIN HELPER SETUP: {} starting setupLoginHelper()", testClass);
        
        // Log container status before calling super.setupWebDriver()
        SeleniumTestContainerSingleton.logCurrentStatus();
        
        super.setupWebDriver();
        log.info("✅ PARENT SETUP COMPLETE: {} super.setupWebDriver() completed. Driver: {}, BaseUrl: {}", 
                testClass, driver != null ? "AVAILABLE" : "NULL", baseUrl != null ? baseUrl : "NULL");
        
        // Log container status after setupWebDriver
        SeleniumTestContainerSingleton.logCurrentStatus();
        
        if (driver != null && baseUrl != null) {
            this.loginHelper = new LoginHelper(driver, baseUrl);
            log.info("✅ LOGIN HELPER READY: {} initialized successfully: {}", testClass, loginHelper);
        } else {
            log.error("❌ LOGIN HELPER FAILED: {} - selenium container failed to initialize", testClass);
            log.error("Details - Driver: {}, BaseUrl: {}", driver, baseUrl);
            throw new RuntimeException("Selenium container failed to initialize - driver or baseUrl is null");
        }
    }
}