package id.ac.tazkia.minibank.integration.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseSeleniumTest {
    
    protected WebDriver driver;
    
    @LocalServerPort
    protected int port;
    
    protected String baseUrl;
    
    @BeforeEach
    void setUp() {
        // Configure ChromeOptions
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run in headless mode for CI
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-web-security");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");
        
        // Use Selenium Manager (automatic driver management since Selenium 4.6+)
        // No need for manual driver setup - Selenium Manager handles it automatically
        ChromeDriverService service = new ChromeDriverService.Builder()
            .build();
        
        driver = new ChromeDriver(service, options);
        baseUrl = "http://localhost:" + port;
    }
    
    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}