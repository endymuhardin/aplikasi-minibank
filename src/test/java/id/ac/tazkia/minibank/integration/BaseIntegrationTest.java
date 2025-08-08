package id.ac.tazkia.minibank.integration;

import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import({PostgresTestContainersConfiguration.class})
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
}