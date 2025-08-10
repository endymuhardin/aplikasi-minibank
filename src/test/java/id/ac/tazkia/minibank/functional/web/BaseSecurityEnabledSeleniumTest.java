package id.ac.tazkia.minibank.functional.web;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.openqa.selenium.remote.RemoteWebDriver;

import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import id.ac.tazkia.minibank.config.SeleniumTestContainerSingleton;
import id.ac.tazkia.minibank.config.TestSecurityConfig;

/**
 * Base class for Selenium tests that require Spring Security to be enabled.
 * This is used for testing authentication, authorization, and permission-related features.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({PostgresTestContainersConfiguration.class, TestSecurityConfig.class})
@ActiveProfiles({"test", "test-security"})
public abstract class BaseSecurityEnabledSeleniumTest {

    protected RemoteWebDriver driver = SeleniumTestContainerSingleton.DRIVER;

    @LocalServerPort
    protected int port;
    
    protected String baseUrl;
    
    @PostConstruct
    void setUp() {
        baseUrl = "http://host.testcontainers.internal:" + port;
        System.out.println(">>> Selenium test with security baseUrl: " + baseUrl);
    }
}