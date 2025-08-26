package id.ac.tazkia.minibank.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TestSchemaInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    private static final InheritableThreadLocal<String> currentSchema = new InheritableThreadLocal<>();
    private static final Map<String, String> threadSchemaMap = new ConcurrentHashMap<>();
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // Get container details from system properties (set by BaseIntegrationTest)
        String jdbcUrl = System.getProperty("test.postgres.jdbcUrl");
        String username = System.getProperty("test.postgres.username");
        String password = System.getProperty("test.postgres.password");
        String schemaName = System.getProperty("test.schema.name");
        
        // If not set via system properties, generate them (fallback)
        if (schemaName == null) {
            schemaName = generateUniqueSchemaName();
            System.setProperty("test.schema.name", schemaName);
        }
        
        currentSchema.set(schemaName);
        // Also store in thread-safe map for parallel execution
        threadSchemaMap.put(Thread.currentThread().getName(), schemaName);

        // Only configure if we have valid connection details
        if (jdbcUrl != null && username != null && password != null) {
            TestPropertyValues.of(
                "spring.datasource.url=" + jdbcUrl,
                "spring.datasource.username=" + username,
                "spring.datasource.password=" + password,
                "spring.datasource.hikari.connection-customizer-class-name=id.ac.tazkia.minibank.config.ThreadLocalSchemaCustomizer",
                "spring.flyway.enabled=true",
                "spring.flyway.schemas=" + schemaName,
                "spring.flyway.locations=classpath:db/migration",
                "test.schema.name=" + schemaName
            ).applyTo(applicationContext);
            
            log.info("Configured schema {} for thread {} with URL: {}?currentSchema={}", 
                    schemaName, Thread.currentThread().getName(), jdbcUrl, schemaName);
        } else {
            log.warn("TestSchemaInitializer: No container details found, using fallback configuration");
        }
    }
    
    private String generateUniqueSchemaName() {
        String threadName = Thread.currentThread().getName()
                .replaceAll("[^a-zA-Z0-9]", "_")
                .toLowerCase();
        String uniqueId = UUID.randomUUID().toString().replace("-", "_").substring(0, 8);
        String schemaName = "test_" + threadName + "_" + uniqueId;
        
        // Ensure schema name is not too long for PostgreSQL (max 63 chars)
        if (schemaName.length() > 60) {
            schemaName = "test_" + uniqueId + "_" + String.valueOf(Math.abs(threadName.hashCode()));
        }
        
        return schemaName;
    }
    
    public static String getCurrentSchema() {
        String schema = currentSchema.get();
        if (schema == null) {
            // Fallback to thread-safe map for parallel execution
            schema = threadSchemaMap.get(Thread.currentThread().getName());
        }
        return schema;
    }
    
}