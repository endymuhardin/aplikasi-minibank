package id.ac.tazkia.minibank.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {
    
    protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    static {
        postgres.start();
    }
    
    protected String schemaName;
    
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    
    @Autowired  
    protected DataSource dataSource;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "false");
        
        // Add a unique property per test class to prevent Spring context caching conflicts
        String testClassName = getTestClassName();
        registry.add("test.class.identifier", () -> testClassName);
    }
    
    private static String getTestClassName() {
        // Get the actual test class name from stack trace
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String className = element.getClassName();
            if (className.contains("selenium.essential")) {
                return className.substring(className.lastIndexOf('.') + 1);
            }
        }
        return "unknown-" + System.currentTimeMillis();
    }
    
    @BeforeAll
    void setUpSchema() throws Exception {
        // Generate unique schema name for this test class
        schemaName = generateUniqueSchemaName();
        
        // Set schema as system property to ensure unique Spring context per test class
        System.setProperty("test.schema.name." + this.getClass().getSimpleName(), schemaName);
        
        // Create schema and run Flyway migration
        try {
            // Create the schema
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
            log.info("BaseIntegrationTest setUpSchema: Created schema {} for test class {} on thread {}", 
                    schemaName, this.getClass().getSimpleName(), Thread.currentThread().getName());
            
            // Run Flyway migration for this schema
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(schemaName)
                    .defaultSchema(schemaName)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .validateOnMigrate(false)
                    .load();
            
            flyway.migrate();
            log.info("BaseIntegrationTest setUpSchema: Flyway migration completed for schema {}", schemaName);
            
            // Set search path for this thread
            jdbcTemplate.execute("SET search_path TO " + schemaName);
            
        } catch (Exception e) {
            log.error("BaseIntegrationTest setUpSchema: Schema setup failed for schema {}: {}", schemaName, e.getMessage());
            throw e;
        }
    }
    
    private String generateUniqueSchemaName() {
        String className = this.getClass().getSimpleName().toLowerCase();
        String uniqueId = java.util.UUID.randomUUID().toString().replace("-", "_").substring(0, 8);
        String schemaName = "test_" + className + "_" + uniqueId;
        
        // Ensure schema name is not too long for PostgreSQL (max 63 chars)
        if (schemaName.length() > 60) {
            schemaName = "test_" + uniqueId + "_" + String.valueOf(Math.abs(className.hashCode()));
        }
        
        return schemaName;
    }
    
    @BeforeEach
    void setSchemaPath() {
        // Ensure JdbcTemplate uses the correct schema by setting search path
        // This is needed because not all connections go through ThreadLocalSchemaCustomizer
        try {
            jdbcTemplate.execute("SET search_path TO " + schemaName);
            log.debug("BaseIntegrationTest setSchemaPath: Set search path to schema {} on thread {}", 
                    schemaName, Thread.currentThread().getName());
        } catch (Exception e) {
            log.error("BaseIntegrationTest setSchemaPath: Failed to set search path for schema {}: {}", 
                    schemaName, e.getMessage());
            throw e;
        }
    }
}