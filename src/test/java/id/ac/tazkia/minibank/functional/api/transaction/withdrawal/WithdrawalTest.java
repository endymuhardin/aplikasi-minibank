package id.ac.tazkia.minibank.functional.api.transaction.withdrawal;

import com.intuit.karate.junit5.Karate;
import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({PostgresTestContainersConfiguration.class})
@ActiveProfiles("test")
@Sql(scripts = "/sql/setup/setup-withdrawal-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/cleanup/cleanup-withdrawal-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class WithdrawalTest {

    @LocalServerPort
    private int port;

    @Karate.Test
    Karate testWithdrawal() {
        System.setProperty("karate.port", String.valueOf(port));
        return Karate.run("classpath:karate/features/withdrawal.feature");
    }
    
    @Karate.Test
    Karate testWithdrawalValidation() {
        System.setProperty("karate.port", String.valueOf(port));
        return Karate.run("classpath:karate/features/withdrawal-validation.feature");
    }
}