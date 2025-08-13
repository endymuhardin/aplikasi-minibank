package id.ac.tazkia.minibank.functional.web;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.openqa.selenium.WebDriver;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.Testcontainers;
import org.testcontainers.lifecycle.TestDescription;

import id.ac.tazkia.minibank.config.SeleniumTestContainerSingleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSeleniumTestBase {

    protected WebDriver driver;
    protected String baseUrl;

    @LocalServerPort 
    protected Integer webappPort;

    @BeforeEach
    void setupWebDriver() throws Exception {
        String testClass = this.getClass().getSimpleName();
        log.info("🧪 TEST SETUP: {} requesting WebDriver setup with webapp port: {}", testClass, webappPort);
        Testcontainers.exposeHostPorts(webappPort);
        
        try {
            log.info("📞 CONTAINER REQUEST: {} calling SeleniumTestContainerSingleton.initialize()", testClass);
            SeleniumTestContainerSingleton.initialize();
            log.info("✅ CONTAINER RESPONSE: {} received container initialization response", testClass);
            
            driver = SeleniumTestContainerSingleton.driver;
            log.info("🔗 WEBDRIVER ASSIGNMENT: {} using singleton WebDriver: {}", 
                    testClass, driver != null ? "SUCCESS" : "NULL");
            
            if (SeleniumTestContainerSingleton.getContainer() != null) {
                log.info("🖥️  VNC URL for {}: {}", testClass, SeleniumTestContainerSingleton.getContainer().getVncAddress());
            } else {
                log.error("❌ CONTAINER ERROR: {} found null container after initialization", testClass);
            }
            
            baseUrl = getHostUrl();
            log.info("🌐 BASE URL: {} set to: {}", testClass, baseUrl);
        } catch (Exception e) {
            log.error("❌ SETUP FAILED: {} WebDriver setup failed", testClass, e);
            throw e;
        }
    }

    @AfterEach
    void stopWebDriver(){
        if (SeleniumTestContainerSingleton.getContainer() != null) {
            SeleniumTestContainerSingleton.getContainer().afterTest(
                    new TestDescription() {
                        @Override
                        public String getTestId() {
                            return getFilesystemFriendlyName();
                        }

                        @Override
                        public String getFilesystemFriendlyName() {
                            return getTestName();
                        }
                    },
                    Optional.empty()
                );
        } else {
            log.warn("Container is null, skipping afterTest cleanup");
        }
    }

    protected String getHostUrl(){
        return SeleniumTestContainerSingleton.TESTCONTAINER_HOST_URL + ":" + webappPort;
    }

    protected String getTestName(){
        return this.getClass().getSimpleName();
    }
}