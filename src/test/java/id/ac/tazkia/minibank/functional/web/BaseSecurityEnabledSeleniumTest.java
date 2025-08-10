package id.ac.tazkia.minibank.functional.web;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import id.ac.tazkia.minibank.config.TestSecurityConfig;

/**
 * Base class for Selenium tests that require Spring Security to be enabled.
 * This is used for testing authentication, authorization, and permission-related features.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({PostgresTestContainersConfiguration.class, TestSecurityConfig.class})
@ActiveProfiles({"test", "test-security"})
public abstract class BaseSecurityEnabledSeleniumTest extends AbstractSeleniumTestBase {

}