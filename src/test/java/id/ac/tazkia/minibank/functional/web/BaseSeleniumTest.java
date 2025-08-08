package id.ac.tazkia.minibank.functional.web;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.lifecycle.TestDescription;

import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import id.ac.tazkia.minibank.config.SeleniumTestContainersConfiguration;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({PostgresTestContainersConfiguration.class, SeleniumTestContainersConfiguration.class})
@ActiveProfiles("test")
public abstract class BaseSeleniumTest {
    
    @Autowired BrowserWebDriverContainer<?> browserContainer;

    protected WebDriver driver;
    
    @LocalServerPort
    protected int port;
    
    protected String baseUrl;
    
    @BeforeEach
    void setUp() {
        Testcontainers.exposeHostPorts(port);
        baseUrl = "http://host.testcontainers.internal:" + port;
        
        // Retry mechanism for WebDriver creation with shorter waits
        int maxRetries = 2; // Reduced from 3 to 2
        for (int i = 0; i < maxRetries; i++) {
            try {
                driver = new RemoteWebDriver(browserContainer.getSeleniumAddress(), new FirefoxOptions());
                break;
            } catch (Exception e) {
                if (i == maxRetries - 1) {
                    throw new RuntimeException("Failed to create WebDriver after " + maxRetries + " attempts", e);
                }
                try {
                    Thread.sleep(1000); // Wait 1 second before retrying (reduced from 2)
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting to retry WebDriver creation", ie);
                }
            }
        }
    }
    
    @AfterEach
    void tearDown() {
        try {
            browserContainer.afterTest(new TestDescription() {

                @Override
                public String getFilesystemFriendlyName() {
                    return getTestClassName();
                }

                @Override
                public String getTestId() {
                    return getFilesystemFriendlyName();
                }
                
            }, Optional.empty());
        } catch (Exception e) {
            // Log the exception but continue with cleanup
            System.err.println("Error in browserContainer.afterTest(): " + e.getMessage());
        }
        
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                // Log the exception but don't fail the test
                System.err.println("Error quitting driver: " + e.getMessage());
            }
        }
    }

    private String getTestClassName(){
        return this.getClass().getSimpleName();
    }
}