package id.ac.tazkia.minibank.integration;

import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import id.ac.tazkia.minibank.config.TestPasswordEncoderConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import({PostgresTestContainersConfiguration.class, TestPasswordEncoderConfig.class})
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
}