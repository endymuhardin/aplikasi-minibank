package id.ac.tazkia.minibank.integration;

import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestContainersConfiguration.class)
@ActiveProfiles("test")
public abstract class BaseRepositoryTest {
}