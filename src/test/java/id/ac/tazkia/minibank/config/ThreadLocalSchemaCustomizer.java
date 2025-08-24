package id.ac.tazkia.minibank.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadLocalSchemaCustomizer {

    public static void customize(Connection connection) throws SQLException {
        String schemaName = TestSchemaInitializer.getCurrentSchema();
        if (schemaName != null && !schemaName.trim().isEmpty()) {
            try (Statement statement = connection.createStatement()) {
                // First create schema if it doesn't exist (defensive programming)
                statement.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
                
                // Set the search path to use this schema
                statement.execute("SET search_path TO " + schemaName);
                
                log.debug("ThreadLocalSchemaCustomizer: Set connection schema to {} for thread {}", 
                        schemaName, Thread.currentThread().getName());
            } catch (SQLException e) {
                log.error("ThreadLocalSchemaCustomizer: Failed to set schema {} for thread {}: {}", 
                        schemaName, Thread.currentThread().getName(), e.getMessage());
                throw e;
            }
        } else {
            log.warn("ThreadLocalSchemaCustomizer: No schema name found in ThreadLocal for thread {}", 
                    Thread.currentThread().getName());
        }
    }
}