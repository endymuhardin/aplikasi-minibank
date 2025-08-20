package id.ac.tazkia.minibank.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Configuration properties for Selenium test execution.
 * These can be overridden via application-test.yml or system properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "test.selenium")
public class SeleniumTestProperties {

    /**
     * Container resource configuration
     */
    private Container container = new Container();
    
    /**
     * Timeout configuration
     */
    private Timeouts timeouts = new Timeouts();
    
    /**
     * Application readiness configuration
     */
    private ApplicationReadiness applicationReadiness = new ApplicationReadiness();

    @Data
    public static class Container {
        /**
         * Maximum number of parallel containers (default: calculated based on system resources)
         */
        private Integer maxContainers;
        
        /**
         * Memory limit per container in MB
         */
        private long memoryLimitMb = 1024;
        
        /**
         * CPU quota per container (100000 = 1 full CPU core)
         */
        private long cpuQuota = 100000L;
        
        /**
         * Shared memory size per container in bytes
         */
        private long sharedMemoryBytes = 1073741824L; // 1GB
        
        /**
         * Container startup timeout in seconds
         */
        private int startupTimeoutSeconds = 300; // 5 minutes
        
        /**
         * Maximum retry attempts for container startup
         */
        private int maxStartupRetries = 3;
        
        /**
         * Base delay between retry attempts in milliseconds
         */
        private int retryDelayMs = 5000;
    }

    @Data
    public static class Timeouts {
        /**
         * Page load timeout for WebDriver in seconds
         */
        private int pageLoadTimeoutSeconds = 30;
        
        /**
         * Script execution timeout for WebDriver in seconds
         */
        private int scriptTimeoutSeconds = 20;
        
        /**
         * Implicit wait timeout for element location in seconds
         */
        private int implicitWaitSeconds = 10;
        
        /**
         * Page object wait timeout in seconds
         */
        private int pageObjectWaitSeconds = 30;
    }

    @Data
    public static class ApplicationReadiness {
        /**
         * Maximum time to wait for application startup in seconds
         */
        private int maxWaitTimeSeconds = 60;
        
        /**
         * Polling interval for readiness checks in milliseconds
         */
        private int pollIntervalMs = 1000;
        
        /**
         * HTTP timeout for readiness checks in seconds
         */
        private int httpTimeoutSeconds = 3;
        
        /**
         * Enable application readiness checks
         */
        private boolean enabled = true;
    }

    /**
     * Calculate optimal number of containers based on system resources
     */
    public int getEffectiveMaxContainers() {
        if (container.maxContainers != null) {
            return container.maxContainers;
        }
        
        int processors = Runtime.getRuntime().availableProcessors();
        long maxMemoryMB = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        
        // Conservative approach: 1 container per 4 processors and 2GB RAM
        int maxByProcessors = Math.max(1, processors / 4);
        int maxByMemory = Math.max(1, (int) (maxMemoryMB / 2048));
        
        int calculatedMax = Math.min(maxByProcessors, maxByMemory);
        calculatedMax = Math.min(calculatedMax, 3); // Cap at 3 containers max
        calculatedMax = Math.max(calculatedMax, 1); // Ensure at least 1 container
        
        return calculatedMax;
    }
}