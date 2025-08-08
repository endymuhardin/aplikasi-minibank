package id.ac.tazkia.minibank.integration;

import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import id.ac.tazkia.minibank.config.SeleniumTestContainersConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import({PostgresTestContainersConfiguration.class, SeleniumTestContainersConfiguration.class})
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
}