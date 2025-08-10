package id.ac.tazkia.minibank.functional.web;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import id.ac.tazkia.minibank.config.SeleniumTestContainerSingleton;
import id.ac.tazkia.minibank.config.TestPasswordEncoderConfig;
import jakarta.annotation.PostConstruct;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"})
@Import({PostgresTestContainersConfiguration.class, TestPasswordEncoderConfig.class})
@ActiveProfiles("test")
public abstract class BaseSeleniumTest {

    protected RemoteWebDriver driver = SeleniumTestContainerSingleton.DRIVER;

    @LocalServerPort
    protected int port;
    
    protected String baseUrl;
    
    @PostConstruct
    void setUp() {
        baseUrl = "http://host.testcontainers.internal:" + port;
        System.out.println(">>> Selenium test baseUrl: " + baseUrl);
    }
}