package id.ac.tazkia.minibank.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseIntegrationTest {
    
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    
    @Autowired
    protected DataSource dataSource;
    
    protected static String schemaName;
    
    @BeforeAll
    static void setUpSchema() throws Exception {
        // Generate unique schema name using centralized utility
        schemaName = TestSchemaManager.generateSchemaName();
        log.info("BaseIntegrationTest setUpSchema: Generated schema {} for thread {}", 
                schemaName, Thread.currentThread().getName());
        
        // Create datasource to create schema and run Flyway
        try {
            com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();
            config.setJdbcUrl(TestSchemaManager.getJdbcUrl());
            config.setUsername(TestSchemaManager.getUsername());
            config.setPassword(TestSchemaManager.getPassword());
            
            javax.sql.DataSource setupDataSource = new com.zaxxer.hikari.HikariDataSource(config);
            org.springframework.jdbc.core.JdbcTemplate setupJdbcTemplate = new org.springframework.jdbc.core.JdbcTemplate(setupDataSource);
            
            // Create the schema if it doesn't exist (thread-safe for parallel execution)
            try {
                setupJdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
                log.info("BaseIntegrationTest setUpSchema: Created schema {}", schemaName);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // Schema already exists, this is expected in parallel execution
                log.info("BaseIntegrationTest setUpSchema: Schema {} already exists, continuing", schemaName);
            }
            
            // Run Flyway migration
            Flyway schemaFlyway = Flyway.configure()
                    .dataSource(setupDataSource)
                    .schemas(schemaName)
                    .defaultSchema(schemaName)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .validateOnMigrate(false)
                    .load();
            
            schemaFlyway.migrate();
            log.info("BaseIntegrationTest setUpSchema: Flyway migration completed for schema {}", schemaName);
            
            // Close the setup datasource
            ((com.zaxxer.hikari.HikariDataSource) setupDataSource).close();
            
        } catch (Exception e) {
            log.error("BaseIntegrationTest setUpSchema: Schema setup failed for schema {}: {}", schemaName, e.getMessage());
            throw e;
        }
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