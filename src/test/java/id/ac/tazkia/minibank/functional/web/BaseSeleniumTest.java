package id.ac.tazkia.minibank.functional.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import id.ac.tazkia.minibank.category.TestCategories;
import id.ac.tazkia.minibank.config.ParallelSeleniumManager;
import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import id.ac.tazkia.minibank.config.ApplicationReadinessService;
import id.ac.tazkia.minibank.functional.web.helper.LoginHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Optimized base class for parallel Selenium tests.
 * 
 * Features:
 * - Parallel execution with thread-isolated WebDriver instances
 * - Per-thread browser containers for optimal isolation
 * - Thread-safe login helper with session management
 * - Browser state reset between tests
 * - Resource-aware parallel execution
 * - Comprehensive logging and monitoring
 * 
 * All Selenium tests should extend this class for consistent parallel behavior.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({PostgresTestContainersConfiguration.class})
@ActiveProfiles("test")
@Execution(ExecutionMode.CONCURRENT)
@Slf4j
public abstract class BaseSeleniumTest extends AbstractSeleniumTestBase 
        implements TestCategories.UiTests, TestCategories.SlowTests {
    
    // Thread-local login helper for parallel execution
    protected static final ThreadLocal<LoginHelper> loginHelper = new ThreadLocal<>();
    
    // Thread-safe state management
    private static volatile int activeSeleniumTests = 0;
    private static final Object seleniumLock = new Object();
    
    // Application readiness service
    @Autowired
    private ApplicationReadinessService applicationReadinessService;
    
    @BeforeEach
    void setupSeleniumTest(TestInfo testInfo) throws Exception {
        synchronized (seleniumLock) {
            activeSeleniumTests++;
            log.info("Parallel Selenium test starting: {} [Active: {}, Thread: {}]", 
                    testInfo.getDisplayName(), activeSeleniumTests, Thread.currentThread().getName());
        }
        
        String testClass = this.getClass().getSimpleName();
        String threadName = Thread.currentThread().getName();
        
        // IMPORTANT: Must expose host ports before starting any containers
        log.info("🔗 EXPOSING HOST PORT: {} exposing webapp port {} [Thread: {}]", testClass, webappPort, threadName);
        org.testcontainers.Testcontainers.exposeHostPorts(webappPort);
        
        // Setup thread-local WebDriver
        setupWebDriverForCurrentThread();
        
        // Wait for Spring Boot application to be ready - no more Thread.sleep!
        if (!applicationReadinessService.waitForApplicationReady(webappPort)) {
            throw new RuntimeException("Application failed to become ready within timeout");
        }
        
        // Initialize thread-local login helper
        if (loginHelper.get() == null) {
            loginHelper.set(new LoginHelper(driver, baseUrl));
            log.info("✅ THREAD-LOCAL LOGIN HELPER: {} initialized for thread {}", testClass, threadName);
        }
        
        // Reset browser state for test isolation
        ParallelSeleniumManager.resetBrowserState();
        
        // Perform authentication
        log.info("🔑 PARALLEL AUTHENTICATION: {} performing authentication on thread {}", testClass, threadName);
        performInitialLogin();
    }
    
    @AfterEach
    protected void cleanupSeleniumTest(TestInfo testInfo) {
        String threadName = Thread.currentThread().getName();
        
        try {
            // Reset browser state for next test
            ParallelSeleniumManager.resetBrowserState();
            
            // Clear thread-local login helper
            loginHelper.remove();
            
            log.debug("Parallel Selenium test cleanup: {} [Thread: {}]", 
                     testInfo.getDisplayName(), threadName);
        } finally {
            synchronized (seleniumLock) {
                activeSeleniumTests--;
                log.info("Parallel Selenium test completed: {} [Active: {}, Thread: {}]", 
                        testInfo.getDisplayName(), activeSeleniumTests, threadName);
            }
        }
    }
    
    /**
     * Setup WebDriver for current thread
     */
    private void setupWebDriverForCurrentThread() {
        try {
            // Get thread-local WebDriver from parallel manager
            driver = ParallelSeleniumManager.getDriver();
            baseUrl = ParallelSeleniumManager.getBaseUrl(webappPort);
            
            log.debug("WebDriver setup completed for thread: {} [BaseURL: {}]", 
                     Thread.currentThread().getName(), baseUrl);
            
        } catch (Exception e) {
            log.error("Failed to setup WebDriver for thread: {}", Thread.currentThread().getName(), e);
            throw new RuntimeException("WebDriver setup failed", e);
        }
    }
    
    protected void performInitialLogin() {
        // Default implementation - subclasses can override for specific user types
        getLoginHelper().loginAsManager();
    }
    
    /**
     * Get the login helper for current thread
     */
    protected LoginHelper getLoginHelper() {
        LoginHelper helper = loginHelper.get();
        if (helper == null) {
            throw new IllegalStateException("LoginHelper not initialized for thread: " + Thread.currentThread().getName());
        }
        return helper;
    }
    
    /**
     * Switch to different user role within same test class
     */
    protected void switchToRole(String role) {
        LoginHelper helper = loginHelper.get();
        if (helper == null) {
            throw new IllegalStateException("LoginHelper not initialized for thread: " + Thread.currentThread().getName());
        }
        
        log.info("🔄 PARALLEL ROLE SWITCH: Switching to role {} on thread {}", role, Thread.currentThread().getName());
        switch (role.toUpperCase()) {
            case "CUSTOMER_SERVICE":
                helper.loginAsCustomerServiceUser();
                break;
            case "TELLER":
                helper.loginAsTeller();
                break;
            case "MANAGER":
                helper.loginAsManager();
                break;
            case "USER_MANAGER":
                helper.loginAsUserManager();
                break;
            default:
                throw new IllegalArgumentException("Unknown role: " + role);
        }
    }
    
    /**
     * Force fresh login (bypasses session optimization)
     */
    protected void forceFreshLogin() {
        LoginHelper helper = loginHelper.get();
        if (helper == null) {
            throw new IllegalStateException("LoginHelper not initialized for thread: " + Thread.currentThread().getName());
        }
        
        log.info("🔄 PARALLEL FORCED FRESH LOGIN: Invalidating session on thread {}", Thread.currentThread().getName());
        helper.logout();
        performInitialLogin();
    }
    
    
    /**
     * Utility method to log current parallel execution status
     */
    protected void logParallelExecutionStatus() {
        LoginHelper helper = loginHelper.get();
        String sessionInfo = helper != null ? helper.getCurrentSessionInfo() : "No active session";
        
        log.info("📊 PARALLEL LOGIN STATUS [Thread: {}]: {}", Thread.currentThread().getName(), sessionInfo);
        log.info("📊 CONTAINER STATISTICS: {}", ParallelSeleniumManager.getContainerStatistics());
        
        synchronized (seleniumLock) {
            log.info("📊 ACTIVE PARALLEL TESTS: {}", activeSeleniumTests);
        }
    }
}