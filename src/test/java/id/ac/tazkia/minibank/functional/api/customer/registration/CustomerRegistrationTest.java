package id.ac.tazkia.minibank.functional.api.customer.registration;

import com.intuit.karate.junit5.Karate;
import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestContainersConfiguration.class)
@ActiveProfiles("test")
@Sql(scripts = "/sql/cleanup/cleanup-customers.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CustomerRegistrationTest {

    @LocalServerPort
    private int port;

    @Karate.Test
    Karate testPersonalCustomerRegistration() {
        System.setProperty("karate.port", String.valueOf(port));
        return Karate.run("classpath:karate/features/personal-customer-registration.feature");
    }
    
    @Karate.Test
    Karate testPersonalCustomerRegistrationValidation() {
        System.setProperty("karate.port", String.valueOf(port));
        return Karate.run("classpath:karate/features/personal-customer-registration-validation.feature");
    }
    
    @Karate.Test
    Karate testCorporateCustomerRegistration() {
        System.setProperty("karate.port", String.valueOf(port));
        return Karate.run("classpath:karate/features/corporate-customer-registration.feature");
    }
    
    @Karate.Test
    Karate testCorporateCustomerRegistrationValidation() {
        System.setProperty("karate.port", String.valueOf(port));
        return Karate.run("classpath:karate/features/corporate-customer-registration-validation.feature");
    }
}