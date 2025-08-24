package id.ac.tazkia.minibank.config;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.UUID;

@UtilityClass
@Slf4j
public class TestSchemaManager {
    
    /**
     * Generates a unique schema name for test execution based on thread name and UUID
     * @return unique schema name safe for PostgreSQL (max 63 chars)
     */
    public static String generateSchemaName() {
        String threadName = Thread.currentThread().getName()
                .replaceAll("[^a-zA-Z0-9]", "_")
                .toLowerCase();
        String uniqueId = UUID.randomUUID().toString().replace("-", "_").substring(0, 8);
        String schemaName = "test_" + threadName + "_" + uniqueId;
        
        // Ensure schema name is not too long for PostgreSQL (max 63 chars)
        if (schemaName.length() > 60) {
            schemaName = "test_" + uniqueId + "_" + String.valueOf(Math.abs(threadName.hashCode()));
        }
        
        log.debug("Generated schema name: {} for thread: {}", schemaName, Thread.currentThread().getName());
        return schemaName;
    }
    
    /**
     * Accesses the static PostgreSQL container from BaseIntegrationTest via reflection
     * @return PostgreSQL container instance
     * @throws RuntimeException if container access fails
     */
    public static PostgreSQLContainer<?> getPostgresContainer() {
        try {
            Class<?> baseTestClass = Class.forName("id.ac.tazkia.minibank.config.BaseIntegrationTest");
            return (PostgreSQLContainer<?>) baseTestClass.getDeclaredField("postgres").get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access PostgreSQL container", e);
        }
    }
    
    /**
     * Gets JDBC URL from the PostgreSQL container
     * @return JDBC URL string
     */
    public static String getJdbcUrl() {
        return getPostgresContainer().getJdbcUrl();
    }
    
    /**
     * Gets username from the PostgreSQL container
     * @return username string
     */
    public static String getUsername() {
        return getPostgresContainer().getUsername();
    }
    
    /**
     * Gets password from the PostgreSQL container
     * @return password string
     */
    public static String getPassword() {
        return getPostgresContainer().getPassword();
    }
}