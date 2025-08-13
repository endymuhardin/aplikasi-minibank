package id.ac.tazkia.minibank.functional.web;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import id.ac.tazkia.minibank.functional.web.helper.LoginHelper;

/**
 * Base class for Selenium tests with full security enabled.
 * All tests extending this class will use production security configuration
 * and must authenticate before accessing protected resources.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({PostgresTestContainersConfiguration.class})
@ActiveProfiles("test")
public abstract class BaseSeleniumTest extends AbstractSeleniumTestBase {
    
    protected LoginHelper loginHelper;
    
    @Override
    void setupWebDriver() throws Exception {
        super.setupWebDriver();
        this.loginHelper = new LoginHelper(driver, baseUrl);
    }
}