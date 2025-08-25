package id.ac.tazkia.minibank.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = TestSchemaInitializer.class)
public abstract class BaseSeleniumTest extends BaseIntegrationTest {

    @LocalServerPort
    protected int serverPort;

    @Container
    protected static BrowserWebDriverContainer<?> seleniumContainer = 
        SeleniumContainerFactory.createSeleniumContainer();

    protected WebDriver driver;
    protected String baseUrl;

    @BeforeEach
    void setUpSelenium() {
        ChromeOptions options = new ChromeOptions();
        
        // Configure Chrome options based on system properties
        boolean headless = Boolean.parseBoolean(System.getProperty("selenium.headless", "true"));
        
        // Add Chrome-specific options
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        
        if (headless) {
            options.addArguments("--headless");
        }
        
        driver = new RemoteWebDriver(seleniumContainer.getSeleniumAddress(), options);
        org.testcontainers.Testcontainers.exposeHostPorts(serverPort);
        baseUrl = "http://host.testcontainers.internal:" + serverPort;
        
        // Log VNC information if running in non-headless mode
        if (!headless) {
            SeleniumContainerFactory.logVncInformation(seleniumContainer);
        }
        
        log.info("BaseSeleniumTest setUpSelenium: Selenium test starting on {} with schema {} | headless: {}", 
                baseUrl, schemaName, headless);
    }

    @AfterEach
    void tearDownSelenium() {
        if (driver != null) {
            try {
                driver.quit();
                log.debug("WebDriver quit successfully");
            } catch (Exception e) {
                log.warn("BaseSeleniumTest tearDownSelenium: Error closing driver: {}", e.getMessage());
            }
        }
        
        log.debug("BaseSeleniumTest tearDownSelenium: Cleanup completed for schema {}", schemaName);
    }
}