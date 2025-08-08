package id.ac.tazkia.minibank.functional.api.user;

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
@Sql(scripts = "/sql/cleanup/cleanup-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserManagementTest {

    @LocalServerPort
    private int port;

    @Karate.Test
    Karate testUserManagement() {
        System.setProperty("karate.port", String.valueOf(port));
        return Karate.run("classpath:karate/features/user-management.feature");
    }
    
    @Karate.Test
    Karate testUserManagementValidation() {
        System.setProperty("karate.port", String.valueOf(port));
        return Karate.run("classpath:karate/features/user-management-validation.feature");
    }
}