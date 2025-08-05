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
    Karate testPersonalCustomerRegistration() {
        System.setProperty("karate.port", String.valueOf(port));
        return Karate.run("personal-customer-registration").relativeTo(getClass());
    }
    
    @Karate.Test
    Karate testPersonalCustomerRegistrationValidation() {
        System.setProperty("karate.port", String.valueOf(port));
        return Karate.run("personal-customer-registration-validation").relativeTo(getClass());
    }
    
    @Karate.Test
    Karate testCorporateCustomerRegistration() {
        System.setProperty("karate.port", String.valueOf(port));
        return Karate.run("corporate-customer-registration").relativeTo(getClass());
    }
    
    @Karate.Test
    Karate testCorporateCustomerRegistrationValidation() {
        System.setProperty("karate.port", String.valueOf(port));
        return Karate.run("corporate-customer-registration-validation").relativeTo(getClass());
    }
}