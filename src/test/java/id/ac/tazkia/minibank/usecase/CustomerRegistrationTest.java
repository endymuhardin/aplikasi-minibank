package id.ac.tazkia.minibank.usecase;

import com.intuit.karate.junit5.Karate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "/sql/cleanup-customers.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CustomerRegistrationTest {

    @LocalServerPort
    private int port;

    @Karate.Test
    Karate testCustomerRegistration() {
        System.setProperty("karate.port", String.valueOf(port));
        return Karate.run("customer-registration").relativeTo(getClass());
    }
    
    @Karate.Test
    Karate testCustomerRegistrationValidation() {
        System.setProperty("karate.port", String.valueOf(port));
        return Karate.run("customer-registration-validation").relativeTo(getClass());
    }
    
    @Karate.Test
    Karate testCustomerRegistrationConstraintValidation() {
        System.setProperty("karate.port", String.valueOf(port));
        return Karate.run("customer-registration-constraint-validation").relativeTo(getClass());
    }
}