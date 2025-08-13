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
    
    protected LoginHelper loginHelper;
    
    @BeforeEach
    void initializeLoginHelper() throws Exception {
        log.info("BaseSeleniumTest.setupLoginHelper() started");
        super.setupWebDriver();
        log.info("super.setupWebDriver() completed. Driver: {}, BaseUrl: {}", driver, baseUrl);
        
        if (driver != null && baseUrl != null) {
            this.loginHelper = new LoginHelper(driver, baseUrl);
            log.info("LoginHelper initialized successfully: {}", loginHelper);
        } else {
            log.error("Driver or baseUrl is null - selenium container failed to initialize");
            log.error("Driver: {}, BaseUrl: {}", driver, baseUrl);
            throw new RuntimeException("Selenium container failed to initialize - driver or baseUrl is null");
        }
    }
}