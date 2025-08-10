package id.ac.tazkia.minibank.functional.web;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import id.ac.tazkia.minibank.config.TestPasswordEncoderConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"})
@Import({PostgresTestContainersConfiguration.class, TestPasswordEncoderConfig.class})
@ActiveProfiles("test")
public abstract class BaseSeleniumTest extends AbstractSeleniumTestBase {

}