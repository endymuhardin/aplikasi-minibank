package id.ac.tazkia.minibank.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import lombok.extern.slf4j.Slf4j;

/**
 * Base class for integration tests that require a database.
 * Uses TestContainers with @ServiceConnection for automatic Spring Boot configuration.
 */
@Slf4j
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseIntegrationTest {
    
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres 
        = new PostgreSQLContainer<>("postgres:17-alpine");
    
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    
    @Autowired
    protected DataSource dataSource;
}