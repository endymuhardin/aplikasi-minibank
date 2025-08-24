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
        // Generate schema name using centralized utility
        String schemaName = TestSchemaManager.generateSchemaName();
        
        currentSchema.set(schemaName);
        // Also store in thread-safe map for parallel execution
        threadSchemaMap.put(Thread.currentThread().getName(), schemaName);

        // Get PostgreSQL container details using centralized utility
        String jdbcUrl = TestSchemaManager.getJdbcUrl();
        String username = TestSchemaManager.getUsername();
        String password = TestSchemaManager.getPassword();
        
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