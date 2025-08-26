package id.ac.tazkia.minibank.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TestSchemaInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    // Thread-safe map to track schema assignments per thread
    private static final ConcurrentHashMap<String, String> threadSchemaMap = new ConcurrentHashMap<>();
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        String threadName = Thread.currentThread().getName();
        String schemaName = getSchemaForThread(threadName);
        
        log.info("Configured schema {} for thread {} with URL: {}?currentSchema={}", 
                schemaName, threadName, TestSchemaManager.getJdbcUrl(), schemaName);
        
        // Configure datasource with schema-specific properties
        TestPropertyValues.of(
                "spring.datasource.hikari.connection-init-sql=SET search_path TO " + schemaName,
                "spring.flyway.enabled=true",
                "spring.flyway.default-schema=" + schemaName,
                "spring.flyway.schemas=" + schemaName,
                "spring.flyway.baseline-on-migrate=true",
                "spring.flyway.validate-on-migrate=false"
        ).applyTo(applicationContext.getEnvironment());
    }
    
    /**
     * Gets or assigns schema for the current thread
     */
    private String getSchemaForThread(String threadName) {
        return threadSchemaMap.computeIfAbsent(threadName, key -> {
            // For parallel test threads, ensure each thread gets a unique schema identifier
            // but still use the shared approach from the working version
            String baseSchema = TestSchemaManager.generateSchemaName();
            
            // Extract just the unique ID part to reduce conflicts
            String[] parts = baseSchema.split("_");
            if (parts.length >= 3) {
                String uniqueId = parts[parts.length - 1]; // Get the UUID part
                return "test_forkjoinpool_1_worker_1_" + uniqueId;
            }
            
            return baseSchema;
        });
    }
    
    /**
     * Gets the schema assigned to current thread
     */
    public static String getCurrentThreadSchema() {
        String threadName = Thread.currentThread().getName();
        return threadSchemaMap.get(threadName);
    }
}