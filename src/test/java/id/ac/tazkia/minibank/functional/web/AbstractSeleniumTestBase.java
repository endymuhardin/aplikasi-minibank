package id.ac.tazkia.minibank.functional.web;

import org.junit.jupiter.api.AfterAll;
import org.openqa.selenium.WebDriver;
import org.springframework.boot.test.web.server.LocalServerPort;

import id.ac.tazkia.minibank.config.ParallelSeleniumManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSeleniumTestBase {

    protected WebDriver driver;
    protected String baseUrl;

    @LocalServerPort 
    protected Integer webappPort;

    protected void setupWebDriverOnce() throws Exception {
        // This method is now called per-thread, not per-class
        String testClass = this.getClass().getSimpleName();
        String threadName = Thread.currentThread().getName();
        
        log.info("🧪 PARALLEL TEST SETUP: {} requesting WebDriver setup with webapp port: {} [Thread: {}]", 
                testClass, webappPort, threadName);
        
        try {
            // Get thread-local WebDriver from parallel manager
            driver = ParallelSeleniumManager.getDriver();
            baseUrl = ParallelSeleniumManager.getBaseUrl(webappPort);
            
            log.info("✅ PARALLEL WEBDRIVER ASSIGNMENT: {} using thread-local WebDriver on thread: {} [BaseURL: {}]", 
                    testClass, threadName, baseUrl);
            
        } catch (Exception e) {
            log.error("❌ PARALLEL SETUP FAILED: {} WebDriver setup failed on thread: {}", testClass, threadName, e);
            throw e;
        }
    }

    @AfterAll
    static void stopWebDriver(){
        String threadName = Thread.currentThread().getName();
        log.info("🧹 PARALLEL CLEANUP: WebDriver cleanup for thread: {}", threadName);
        
        try {
            // Cleanup thread-local WebDriver
            ParallelSeleniumManager.cleanupCurrentThread();
            log.info("✅ PARALLEL CLEANUP COMPLETE: Thread-local WebDriver cleaned up for thread: {}", threadName);
        } catch (Exception e) {
            log.warn("⚠️ PARALLEL CLEANUP WARNING: Error during cleanup for thread {}: {}", threadName, e.getMessage());
        }
    }

    protected String getHostUrl(){
        return ParallelSeleniumManager.TESTCONTAINER_HOST_URL + ":" + webappPort;
    }

    protected String getTestName(){
        return this.getClass().getSimpleName() + "_" + Thread.currentThread().getName();
    }
}