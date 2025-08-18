package id.ac.tazkia.minibank.functional.web;

import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.openqa.selenium.WebDriver;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.Testcontainers;
import org.testcontainers.lifecycle.TestDescription;

import id.ac.tazkia.minibank.config.SeleniumTestContainerSingleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSeleniumTestBase {

    protected static WebDriver driver;
    protected static String baseUrl;
    protected static String currentTestClassName;

    @LocalServerPort 
    protected Integer webappPort;

    protected void setupWebDriverOnce() throws Exception {
        // Only initialize WebDriver once per test class
        if (driver == null) {
            String testClass = this.getClass().getSimpleName();
            currentTestClassName = testClass; // Store for later use in cleanup
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
    }

    @AfterAll
    static void stopWebDriver(){
        if (SeleniumTestContainerSingleton.getContainer() != null) {
            String testClassName = currentTestClassName != null ? currentTestClassName : "AbstractSeleniumTestBase";
            SeleniumTestContainerSingleton.getContainer().afterTest(
                    new TestDescription() {
                        @Override
                        public String getTestId() {
                            return testClassName;
                        }

                        @Override
                        public String getFilesystemFriendlyName() {
                            return testClassName;
                        }
                    },
                    Optional.empty()
                );
        } else {
            log.warn("Container is null, skipping afterTest cleanup");
        }
        
        // Clear static variables
        driver = null;
        baseUrl = null;
        currentTestClassName = null;
    }

    protected String getHostUrl(){
        return SeleniumTestContainerSingleton.TESTCONTAINER_HOST_URL + ":" + webappPort;
    }

    protected String getTestName(){
        return this.getClass().getSimpleName();
    }
}