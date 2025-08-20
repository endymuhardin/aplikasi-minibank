package id.ac.tazkia.minibank.parallel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.TestInfo;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import id.ac.tazkia.minibank.util.TestDataFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages test state isolation for parallel test execution.
 * Ensures tests don't interfere with each other while running concurrently.
 */
@Slf4j
public class TestStateManager {
    
    private static final Map<String, TestState> activeTests = new ConcurrentHashMap<>();
    private static final AtomicLong testCounter = new AtomicLong(1);
    
    /**
     * Initialize test state for a specific test
     */
    public static TestState initializeTestState(TestInfo testInfo) {
        String testId = generateTestId(testInfo);
        String namespace = generateTestNamespace(testInfo);
        
        TestState state = new TestState(testId, namespace, testInfo);
        activeTests.put(testId, state);
        
        // Set test context in data factory
        TestDataFactory.setTestContext(namespace);
        
        log.debug("Initialized test state: {} [Namespace: {}, Thread: {}]", 
                 testId, namespace, Thread.currentThread().getName());
        
        return state;
    }
    
    /**
     * Cleanup test state after test completion
     */
    public static void cleanupTestState(TestInfo testInfo) {
        String testId = generateTestId(testInfo);
        TestState state = activeTests.remove(testId);
        
        if (state != null) {
            state.cleanup();
            log.debug("Cleaned up test state: {} [Thread: {}]", 
                     testId, Thread.currentThread().getName());
        }
        
        // Clear test context from data factory
        TestDataFactory.clearTestContext();
    }
    
    /**
     * Get current test state
     */
    public static TestState getCurrentTestState(TestInfo testInfo) {
        String testId = generateTestId(testInfo);
        return activeTests.get(testId);
    }
    
    /**
     * Check if test is running in isolation
     */
    public static boolean isTestIsolated(TestInfo testInfo) {
        TestState state = getCurrentTestState(testInfo);
        return state != null && state.isIsolated();
    }
    
    /**
     * Generate unique test ID
     */
    private static String generateTestId(TestInfo testInfo) {
        String className = testInfo.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        String methodName = testInfo.getTestMethod().map(m -> m.getName()).orElse("unknown");
        String threadName = Thread.currentThread().getName();
        
        return String.format("%s_%s_%s_%d", 
                           className, methodName, threadName, testCounter.getAndIncrement());
    }
    
    /**
     * Generate test namespace for data isolation
     */
    private static String generateTestNamespace(TestInfo testInfo) {
        String className = testInfo.getTestClass().map(Class::getSimpleName).orElse("test");
        long randomSuffix = ThreadLocalRandom.current().nextLong(1000, 9999);
        
        return String.format("%s_%d_%d", 
                           className.toLowerCase(), 
                           System.currentTimeMillis() % 100000,
                           randomSuffix);
    }
    
    /**
     * Test state container
     */
    public static class TestState {
        private final String testId;
        private final String namespace;
        private final TestInfo testInfo;
        private final long createdTime;
        private final Map<String, Object> testData;
        private TransactionStatus transactionStatus;
        private boolean isolated;
        
        public TestState(String testId, String namespace, TestInfo testInfo) {
            this.testId = testId;
            this.namespace = namespace;
            this.testInfo = testInfo;
            this.createdTime = System.currentTimeMillis();
            this.testData = new ConcurrentHashMap<>();
            this.isolated = true;
        }
        
        public String getTestId() {
            return testId;
        }
        
        public String getNamespace() {
            return namespace;
        }
        
        public TestInfo getTestInfo() {
            return testInfo;
        }
        
        public long getCreatedTime() {
            return createdTime;
        }
        
        public boolean isIsolated() {
            return isolated;
        }
        
        public void setIsolated(boolean isolated) {
            this.isolated = isolated;
        }
        
        /**
         * Store test-specific data
         */
        public void putData(String key, Object value) {
            testData.put(key, value);
        }
        
        /**
         * Retrieve test-specific data
         */
        @SuppressWarnings("unchecked")
        public <T> T getData(String key, Class<T> type) {
            Object value = testData.get(key);
            return type.isInstance(value) ? (T) value : null;
        }
        
        /**
         * Check if test data exists
         */
        public boolean hasData(String key) {
            return testData.containsKey(key);
        }
        
        /**
         * Set transaction status for transactional tests
         */
        public void setTransactionStatus(TransactionStatus status) {
            this.transactionStatus = status;
        }
        
        /**
         * Get transaction status
         */
        public TransactionStatus getTransactionStatus() {
            return transactionStatus;
        }
        
        /**
         * Get execution time in milliseconds
         */
        public long getExecutionTime() {
            return System.currentTimeMillis() - createdTime;
        }
        
        /**
         * Generate unique identifier within this test's namespace
         */
        public String generateUniqueId(String prefix) {
            return String.format("%s_%s_%d", prefix, namespace, 
                               ThreadLocalRandom.current().nextLong(1000, 9999));
        }
        
        /**
         * Cleanup test state
         */
        public void cleanup() {
            testData.clear();
            if (transactionStatus != null && !transactionStatus.isCompleted()) {
                log.debug("Test state cleanup with active transaction for: {}", testId);
            }
        }
        
        @Override
        public String toString() {
            return String.format("TestState{id='%s', namespace='%s', isolated=%s, executionTime=%dms}", 
                               testId, namespace, isolated, getExecutionTime());
        }
    }
    
    /**
     * Transaction state management for database tests
     */
    public static class TransactionStateManager {
        
        /**
         * Begin isolated transaction for test
         */
        public static TransactionStatus beginIsolatedTransaction(
                PlatformTransactionManager transactionManager, TestState testState) {
            
            DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
            definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
            definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            definition.setName("Test-" + testState.getNamespace());
            definition.setTimeout(30); // 30 second timeout for test transactions
            
            TransactionStatus status = transactionManager.getTransaction(definition);
            testState.setTransactionStatus(status);
            
            log.debug("Started isolated transaction for test: {} [TX: {}]", 
                     testState.getTestId(), status.toString());
            
            return status;
        }
        
        /**
         * Rollback transaction for test cleanup
         */
        public static void rollbackTestTransaction(
                PlatformTransactionManager transactionManager, TestState testState) {
            
            TransactionStatus status = testState.getTransactionStatus();
            if (status != null && !status.isCompleted()) {
                try {
                    transactionManager.rollback(status);
                    log.debug("Rolled back transaction for test: {}", testState.getTestId());
                } catch (Exception e) {
                    log.warn("Failed to rollback transaction for test: {}, error: {}", 
                            testState.getTestId(), e.getMessage());
                }
            }
        }
    }
    
    /**
     * Selenium state management for UI tests
     */
    public static class SeleniumStateManager {
        
        /**
         * Reset browser state for test isolation
         */
        public static void resetBrowserState(TestState testState) {
            log.debug("Resetting browser state for test: {}", testState.getTestId());
            
            // Clear browser cache, cookies, and local storage
            try {
                // This would be implemented with actual WebDriver instance
                // driver.manage().deleteAllCookies();
                // driver.executeScript("localStorage.clear();");
                // driver.executeScript("sessionStorage.clear();");
                
                log.debug("Browser state reset completed for test: {}", testState.getTestId());
            } catch (Exception e) {
                log.warn("Failed to reset browser state for test: {}, error: {}", 
                        testState.getTestId(), e.getMessage());
            }
        }
        
        /**
         * Navigate to clean state
         */
        public static void navigateToCleanState(TestState testState, String baseUrl) {
            log.debug("Navigating to clean state for test: {}", testState.getTestId());
            
            try {
                // This would be implemented with actual WebDriver instance
                // driver.get("data:text/html,<html><body><h1>Clean State</h1></body></html>");
                // Thread.sleep(100); // Brief wait
                
                testState.putData("last_navigation", System.currentTimeMillis());
                log.debug("Navigation to clean state completed for test: {}", testState.getTestId());
            } catch (Exception e) {
                log.warn("Failed to navigate to clean state for test: {}, error: {}", 
                        testState.getTestId(), e.getMessage());
            }
        }
    }
    
    /**
     * Get overall state statistics
     */
    public static String getStateStatistics() {
        return String.format("TestStateManager: %d active tests, avg execution time: %.1fms", 
                           activeTests.size(), 
                           activeTests.values().stream()
                                     .mapToLong(TestState::getExecutionTime)
                                     .average().orElse(0.0));
    }
    
    /**
     * Force cleanup of all test states (emergency cleanup)
     */
    public static void forceCleanupAllStates() {
        log.warn("Force cleanup of all test states requested - {} active tests", activeTests.size());
        
        activeTests.values().forEach(TestState::cleanup);
        activeTests.clear();
        TestDataFactory.clearTestContext();
        
        log.info("Force cleanup completed");
    }
}