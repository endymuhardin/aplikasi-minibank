package id.ac.tazkia.minibank.integration;

import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestContainersConfiguration.class)
@ActiveProfiles("test")
@Slf4j
public abstract class BaseRepositoryTest {
    
    @Autowired
    private DataSource dataSource;
    
    @AfterEach
    void ensureConnectionCleanup() {
        try {
            // Force close any leaked connections by testing pool
            try (Connection connection = dataSource.getConnection()) {
                connection.isValid(1);
            }
        } catch (SQLException e) {
            log.warn("Connection cleanup check failed: {}", e.getMessage());
        }
    }
}