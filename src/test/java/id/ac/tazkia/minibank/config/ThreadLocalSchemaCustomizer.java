package id.ac.tazkia.minibank.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadLocalSchemaCustomizer {

    public static void customize(Connection connection) throws SQLException {
        String schemaName = getCurrentTestSchema();
        
        if (schemaName != null && !schemaName.trim().isEmpty()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("SET search_path TO " + schemaName);
                
                log.debug("ThreadLocalSchemaCustomizer: Set connection schema to {} for thread {}", 
                        schemaName, Thread.currentThread().getName());
            } catch (SQLException e) {
                log.error("ThreadLocalSchemaCustomizer: Failed to set schema {} for thread {}: {}", 
                        schemaName, Thread.currentThread().getName(), e.getMessage());
                throw e;
            }
        } else {
            log.warn("ThreadLocalSchemaCustomizer: No schema name found for thread {}", 
                    Thread.currentThread().getName());
        }
    }
    
    private static String getCurrentTestSchema() {
        // Try to find schema from current test class system properties
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String className = element.getClassName();
            if (className.contains("Test") && !className.contains("ThreadLocalSchemaCustomizer")) {
                String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
                String schemaProperty = "test.schema.name." + simpleClassName;
                String schema = System.getProperty(schemaProperty);
                if (schema != null) {
                    return schema;
                }
            }
        }
        return null;
    }
}