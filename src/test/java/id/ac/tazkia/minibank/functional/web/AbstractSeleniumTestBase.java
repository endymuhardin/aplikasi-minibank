package id.ac.tazkia.minibank.functional.web;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
        Testcontainers.exposeHostPorts(webappPort); 
        driver = SeleniumTestContainerSingleton.driver;
        log.info("Using singleton WebDriver");
        log.info("VNC URL : {}", SeleniumTestContainerSingleton.getContainer().getVncAddress());
        baseUrl = getHostUrl();
    }

    @AfterEach
    void stopWebDriver(){
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
    }

    protected String getHostUrl(){
        return SeleniumTestContainerSingleton.TESTCONTAINER_HOST_URL + ":" + webappPort;
    }

    protected String getTestName(){
        return this.getClass().getSimpleName();
    }
}