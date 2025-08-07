package id.ac.tazkia.minibank.functional.api.account.opening;

import com.intuit.karate.junit5.Karate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "/sql/setup/setup-account-opening-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/cleanup/cleanup-account-opening-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AccountOpeningTest {

    @LocalServerPort
    private int port;

    @Karate.Test
    Karate testAccountOpening() {
        System.setProperty("karate.port", String.valueOf(port));
        return Karate.run("classpath:karate/features/account-opening.feature");
    }
    
    @Karate.Test
    Karate testAccountOpeningValidation() {
        System.setProperty("karate.port", String.valueOf(port));
        return Karate.run("classpath:karate/features/account-opening-validation.feature");
    }
}