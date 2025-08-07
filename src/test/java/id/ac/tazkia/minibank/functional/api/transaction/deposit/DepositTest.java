package id.ac.tazkia.minibank.functional.api.transaction.deposit;

import com.intuit.karate.junit5.Karate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "/sql/setup/setup-deposit-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/cleanup/cleanup-deposit-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DepositTest {

    @LocalServerPort
    private int port;

    @Karate.Test
    Karate testDeposit() {
        System.setProperty("karate.port", String.valueOf(port));
        return Karate.run("classpath:karate/features/deposit.feature");
    }
    
    @Karate.Test
    Karate testDepositValidation() {
        System.setProperty("karate.port", String.valueOf(port));
        return Karate.run("classpath:karate/features/deposit-validation.feature");
    }
}