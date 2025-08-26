package id.ac.tazkia.minibank.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.containers.BrowserWebDriverContainer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseSeleniumTest extends BaseIntegrationTest {

    @LocalServerPort
    protected int serverPort;

    protected BrowserWebDriverContainer<?> seleniumContainer;

    protected WebDriver driver;
    protected String baseUrl;

    @BeforeEach
    void setUpSelenium() {
        // Start Selenium container if needed (moved to @BeforeEach to ensure proper execution order)
        if (seleniumContainer == null || !seleniumContainer.isRunning()) {
            seleniumContainer = SeleniumContainerFactory.createSeleniumContainer();
            seleniumContainer.start();
            log.info("🚀 Started Selenium container for test class {}", this.getClass().getSimpleName());
        }
        
        ChromeOptions options = new ChromeOptions();
        
        // Configure Chrome options based on system properties
        boolean headless = Boolean.parseBoolean(System.getProperty("selenium.headless", "true"));
        
        // Add Chrome-specific options for better performance and stability
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-web-security");
        options.addArguments("--disable-features=VizDisplayCompositor");
        
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
        
        log.debug("🔧 Selenium Setup Complete - URL: {} | Schema: {} | Thread: {} | Headless: {}", 
                baseUrl, schemaName, Thread.currentThread().getName(), headless);
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
    
    @AfterAll
    void tearDownSeleniumContainer() {
        if (seleniumContainer != null && seleniumContainer.isRunning()) {
            try {
                seleniumContainer.stop();
                log.info("BaseSeleniumTest tearDownSeleniumContainer: Stopped Selenium container for schema {}", schemaName);
            } catch (Exception e) {
                log.warn("BaseSeleniumTest tearDownSeleniumContainer: Error stopping container: {}", e.getMessage());
            }
        }
    }
}