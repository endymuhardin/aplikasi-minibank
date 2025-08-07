package id.ac.tazkia.minibank.integration;

import id.ac.tazkia.minibank.config.TestContainersConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
}