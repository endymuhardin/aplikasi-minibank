package id.ac.tazkia.minibank.integration;

import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import id.ac.tazkia.minibank.util.ParallelTestDataContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import lombok.extern.slf4j.Slf4j;

/**
 * Parallel-safe base class for repository tests.
 * 
 * Features:
 * - Thread-safe test data generation
 * - Automatic test context initialization
 * - Parallel execution support
 * - Proper resource cleanup
 * - Database transaction management
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestContainersConfiguration.class)
@ActiveProfiles("test")
@Execution(ExecutionMode.CONCURRENT)
@Slf4j
public abstract class ParallelBaseRepositoryTest {
    
    @BeforeEach
    void initializeParallelTestContext(TestInfo testInfo) {
        String testClass = testInfo.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        String testMethod = testInfo.getTestMethod().map(m -> m.getName()).orElse("unknown");
        String threadName = Thread.currentThread().getName();
        
        // Initialize thread-local test context
        ParallelTestDataContext.initialize();
        
        log.debug("🧪 PARALLEL REPOSITORY TEST START: {}.{} [Thread: {}, Context: {}]",
                testClass, testMethod, threadName, ParallelTestDataContext.getUniquePrefix());
    }
    
    @AfterEach
    void cleanupParallelTestContext(TestInfo testInfo) {
        String testClass = testInfo.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        String testMethod = testInfo.getTestMethod().map(m -> m.getName()).orElse("unknown");
        String threadName = Thread.currentThread().getName();
        
        try {
            // Cleanup thread-local test context
            ParallelTestDataContext.cleanup();
            
            log.debug("✅ PARALLEL REPOSITORY TEST CLEANUP: {}.{} [Thread: {}]",
                    testClass, testMethod, threadName);
        } catch (Exception e) {
            log.warn("⚠️ PARALLEL REPOSITORY TEST CLEANUP ERROR: {}.{} [Thread: {}]: {}",
                    testClass, testMethod, threadName, e.getMessage());
        }
    }
    
    /**
     * Get unique test prefix for current thread
     */
    protected String getTestPrefix() {
        return ParallelTestDataContext.getUniquePrefix();
    }
    
    /**
     * Log test execution info for debugging
     */
    protected void logTestExecution(String operation) {
        log.debug("🔍 TEST OPERATION: {} [Thread: {}, Context: {}]", 
                operation, Thread.currentThread().getName(), 
                ParallelTestDataContext.getUniquePrefix());
    }
}