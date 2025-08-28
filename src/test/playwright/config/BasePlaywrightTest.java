package id.ac.tazkia.minibank.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.microsoft.playwright.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BasePlaywrightTest extends BaseIntegrationTest {

    @LocalServerPort
    protected int serverPort;

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext browserContext;
    protected Page page;
    protected String baseUrl;

    @BeforeEach
    void setUpPlaywright() {
        // Initialize Playwright
        playwright = Playwright.create();
        
        // Configure browser based on system properties
        boolean headless = Boolean.parseBoolean(System.getProperty("playwright.headless", "true"));
        String browserName = System.getProperty("playwright.browser", "chromium");
        
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
            .setHeadless(headless)
            .setSlowMo(0); // No artificial delays
            
        // Launch browser based on configuration
        switch (browserName.toLowerCase()) {
            case "firefox":
                browser = playwright.firefox().launch(launchOptions);
                break;
            case "webkit":
                browser = playwright.webkit().launch(launchOptions);
                break;
            default:
                browser = playwright.chromium().launch(launchOptions);
                break;
        }
        
        // Create browser context with reasonable defaults
        browserContext = browser.newContext(new Browser.NewContextOptions()
            .setViewportSize(1920, 1080)
            .setIgnoreHTTPSErrors(true));
        
        // Create page
        page = browserContext.newPage();
        
        // Set up base URL
        baseUrl = "http://localhost:" + serverPort;
        
        log.debug("🔧 Playwright Setup Complete - URL: {} | Schema: {} | Thread: {} | Browser: {} | Headless: {}", 
                baseUrl, schemaName, Thread.currentThread().getName(), browserName, headless);
    }

    @AfterEach
    void tearDownPlaywright() {
        if (page != null) {
            try {
                page.close();
                log.debug("Page closed successfully");
            } catch (Exception e) {
                log.warn("BasePlaywrightTest tearDownPlaywright: Error closing page: {}", e.getMessage());
            }
        }
        
        if (browserContext != null) {
            try {
                browserContext.close();
                log.debug("Browser context closed successfully");
            } catch (Exception e) {
                log.warn("BasePlaywrightTest tearDownPlaywright: Error closing browser context: {}", e.getMessage());
            }
        }
        
        if (browser != null) {
            try {
                browser.close();
                log.debug("Browser closed successfully");
            } catch (Exception e) {
                log.warn("BasePlaywrightTest tearDownPlaywright: Error closing browser: {}", e.getMessage());
            }
        }
        
        if (playwright != null) {
            try {
                playwright.close();
                log.debug("Playwright closed successfully");
            } catch (Exception e) {
                log.warn("BasePlaywrightTest tearDownPlaywright: Error closing Playwright: {}", e.getMessage());
            }
        }
        
        log.debug("BasePlaywrightTest tearDownPlaywright: Cleanup completed for schema {}", schemaName);
    }
}