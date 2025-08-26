package id.ac.tazkia.minibank.config;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@UtilityClass
@Slf4j
public class TestSchemaManager {
    
    private static final AtomicLong SCHEMA_COUNTER = new AtomicLong(System.currentTimeMillis() % 100000);
    private static final ConcurrentHashMap<String, String> THREAD_SCHEMA_MAP = new ConcurrentHashMap<>();
    
    /**
     * Gets or generates a unique schema name for the current thread.
     * Each thread gets exactly one schema name that is reused for all calls from that thread.
     * @return unique schema name safe for PostgreSQL (max 63 chars)
     */
    public static String generateSchemaName() {
        String threadName = Thread.currentThread().getName();
        
        // Return existing schema for this thread if already generated
        return THREAD_SCHEMA_MAP.computeIfAbsent(threadName, key -> {
            String normalizedThreadName = key.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
            long uniqueId = SCHEMA_COUNTER.incrementAndGet();
            String schemaName = "test_" + normalizedThreadName + "_" + uniqueId;
            
            // Ensure schema name is not too long for PostgreSQL (max 63 chars)
            if (schemaName.length() > 60) {
                schemaName = "test_" + uniqueId + "_" + String.valueOf(Math.abs(normalizedThreadName.hashCode()));
            }
            
            log.info("Generated schema name: {} for thread: {}", schemaName, key);
            return schemaName;
        });
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