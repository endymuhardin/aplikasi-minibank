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
        log.info("Setting up WebDriver with webapp port: {}", webappPort);
        Testcontainers.exposeHostPorts(webappPort);
        
        try {
            log.info("About to initialize selenium container...");
            SeleniumTestContainerSingleton.initialize();
            log.info("Selenium container initialization completed");
            
            driver = SeleniumTestContainerSingleton.driver;
            log.info("Using singleton WebDriver: {}", driver != null ? "SUCCESS" : "NULL");
            
            if (SeleniumTestContainerSingleton.getContainer() != null) {
                log.info("VNC URL : {}", SeleniumTestContainerSingleton.getContainer().getVncAddress());
            } else {
                log.error("Container is null after initialization");
            }
            
            baseUrl = getHostUrl();
            log.info("Base URL set to: {}", baseUrl);
        } catch (Exception e) {
            log.error("Failed to setup WebDriver", e);
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