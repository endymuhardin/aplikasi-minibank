package id.ac.tazkia.minibank.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-safe test data context for parallel repository tests.
 * Generates unique identifiers to prevent data conflicts between parallel tests.
 */
@Slf4j
public class ParallelTestDataContext {
    
    private static final ThreadLocal<TestContext> threadContext = new ThreadLocal<>();
    private static final AtomicLong globalCounter = new AtomicLong(1);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");
    
    /**
     * Initialize test context for current thread
     */
    public static void initialize() {
        if (threadContext.get() != null) {
            log.debug("Test context already initialized for thread: {}", Thread.currentThread().getName());
            return;
        }
        
        String threadName = Thread.currentThread().getName()
                .replaceAll("[^a-zA-Z0-9]", "")
                .substring(0, Math.min(8, Thread.currentThread().getName().length()));
        
        String timeStamp = LocalDateTime.now().format(TIME_FORMATTER);
        long counter = globalCounter.getAndIncrement();
        int randomSuffix = ThreadLocalRandom.current().nextInt(100, 999);
        
        String prefix = String.format("T%s_%s_%03d_%d", threadName, timeStamp, counter, randomSuffix);
        
        TestContext context = new TestContext(prefix);
        threadContext.set(context);
        
        log.debug("Initialized test context for thread {}: prefix={}", 
                Thread.currentThread().getName(), prefix);
    }
    
    /**
     * Get current test context
     */
    public static TestContext getContext() {
        TestContext context = threadContext.get();
        if (context == null) {
            initialize();
            context = threadContext.get();
        }
        return context;
    }
    
    /**
     * Clean up test context for current thread
     */
    public static void cleanup() {
        TestContext context = threadContext.get();
        if (context != null) {
            log.debug("Cleaning up test context for thread {}: created {} entities", 
                    Thread.currentThread().getName(), context.getEntityCount());
            threadContext.remove();
        }
    }
    
    /**
     * Get unique prefix for current thread
     */
    public static String getUniquePrefix() {
        return getContext().getPrefix();
    }
    
    /**
     * Generate unique customer number
     */
    public static String generateCustomerNumber() {
        TestContext context = getContext();
        return String.format("C_%s_%03d", context.getPrefix(), context.nextCustomerSequence());
    }
    
    /**
     * Generate unique branch code (max 20 chars)
     */
    public static String generateBranchCode() {
        TestContext context = getContext();
        // Keep it short to fit 20 char limit
        String shortPrefix = context.getPrefix().substring(0, Math.min(8, context.getPrefix().length()));
        return String.format("BR_%s_%02d", shortPrefix, context.nextBranchSequence());
    }
    
    /**
     * Generate unique product code (max 20 chars)
     */
    public static String generateProductCode(String productType) {
        TestContext context = getContext();
        // Keep product code under 20 characters total
        String shortType = productType.length() > 4 ? productType.substring(0, 4) : productType;
        String shortPrefix = context.getPrefix().substring(0, Math.min(4, context.getPrefix().length()));
        return String.format("%s_%s_%03d", shortType, shortPrefix, context.nextProductSequence());
    }
    
    /**
     * Generate unique account number
     */
    public static String generateAccountNumber() {
        TestContext context = getContext();
        return String.format("A_%s_%06d", context.getPrefix(), context.nextAccountSequence());
    }
    
    /**
     * Generate unique username
     */
    public static String generateUsername(String baseUsername) {
        TestContext context = getContext();
        return String.format("%s_%s_%02d", baseUsername, context.getPrefix(), context.nextUserSequence());
    }
    
    /**
     * Generate unique email
     */
    public static String generateEmail(String baseName, String domain) {
        TestContext context = getContext();
        return String.format("%s_%s_%02d@%s", baseName, context.getPrefix(), 
                context.nextEmailSequence(), domain);
    }
    
    /**
     * Generate unique role code
     */
    public static String generateRoleCode(String baseRole) {
        TestContext context = getContext();
        return String.format("%s_%s_%02d", baseRole, context.getPrefix(), context.nextRoleSequence());
    }
    
    /**
     * Thread-local test context
     */
    public static class TestContext {
        private final String prefix;
        private final AtomicLong customerSequence = new AtomicLong(1);
        private final AtomicLong branchSequence = new AtomicLong(1);
        private final AtomicLong productSequence = new AtomicLong(1);
        private final AtomicLong accountSequence = new AtomicLong(1);
        private final AtomicLong userSequence = new AtomicLong(1);
        private final AtomicLong emailSequence = new AtomicLong(1);
        private final AtomicLong roleSequence = new AtomicLong(1);
        private final AtomicLong entityCount = new AtomicLong(0);
        
        public TestContext(String prefix) {
            this.prefix = prefix;
        }
        
        public String getPrefix() {
            return prefix;
        }
        
        public long nextCustomerSequence() {
            entityCount.incrementAndGet();
            return customerSequence.getAndIncrement();
        }
        
        public long nextBranchSequence() {
            entityCount.incrementAndGet();
            return branchSequence.getAndIncrement();
        }
        
        public long nextProductSequence() {
            entityCount.incrementAndGet();
            return productSequence.getAndIncrement();
        }
        
        public long nextAccountSequence() {
            entityCount.incrementAndGet();
            return accountSequence.getAndIncrement();
        }
        
        public long nextUserSequence() {
            entityCount.incrementAndGet();
            return userSequence.getAndIncrement();
        }
        
        public long nextEmailSequence() {
            entityCount.incrementAndGet();
            return emailSequence.getAndIncrement();
        }
        
        public long nextRoleSequence() {
            entityCount.incrementAndGet();
            return roleSequence.getAndIncrement();
        }
        
        public long getEntityCount() {
            return entityCount.get();
        }
        
        /**
         * Generate UUID-like string for entity IDs
         */
        public String generateEntityId() {
            long sequence = entityCount.incrementAndGet();
            return String.format("%s-0000-0000-0000-%012d", 
                    prefix.replace("_", "").substring(0, Math.min(8, prefix.length())), sequence);
        }
    }
}