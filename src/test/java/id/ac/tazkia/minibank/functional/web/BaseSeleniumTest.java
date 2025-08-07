package id.ac.tazkia.minibank.functional.web;

import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import id.ac.tazkia.minibank.config.SeleniumTestContainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.BrowserWebDriverContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({PostgresTestContainersConfiguration.class, SeleniumTestContainersConfiguration.class})
@ActiveProfiles("test")
public abstract class BaseSeleniumTest {
    
    protected WebDriver driver;
    
    @LocalServerPort
    protected int port;
    
    protected String baseUrl;
    
    @Autowired
    private BrowserWebDriverContainer<?> browserContainer;
    
    @BeforeEach
    void setUp() {
        driver = browserContainer.getWebDriver();
        baseUrl = "http://host.testcontainers.internal:" + port;
    }
    
    @AfterEach
    void tearDown() {
        // Driver cleanup is handled by TestContainers
        // No need to quit the driver manually
    }
}