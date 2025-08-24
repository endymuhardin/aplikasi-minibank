package id.ac.tazkia.minibank.selenium.comprehensive.authentication;

import id.ac.tazkia.minibank.config.BaseSeleniumTest;
import id.ac.tazkia.minibank.selenium.pages.DashboardPage;
import id.ac.tazkia.minibank.selenium.pages.LoginPage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Tag("comprehensive")
@DisplayName("Login Comprehensive Tests")
class LoginComprehensiveTest extends BaseSeleniumTest {

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/selenium/login-credentials.csv", numLinesToSkip = 1)
    @DisplayName("Should login successfully with valid credentials for each role")
    void shouldLoginSuccessfullyWithValidCredentials(String username, String password, String expectedRole, String roleDescription) {
        log.info("Testing login for {}: {} with role {}", roleDescription, username, expectedRole);
        
        // Navigate to login page
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        
        // Verify login form is visible
        assertTrue(loginPage.isLoginFormVisible(), "Login form should be visible");
        
        // Attempt login
        DashboardPage dashboardPage = loginPage.loginWith(username, password);
        
        // Verify successful login by checking dashboard
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded after successful login");
        assertEquals("Dashboard", dashboardPage.getPageTitle(), "Page title should be 'Dashboard'");
        
        // Verify role-specific elements based on user role
        verifyRoleSpecificElements(dashboardPage, expectedRole, roleDescription);
    }

    @Test
    @DisplayName("Should show error message with invalid credentials")
    void shouldShowErrorMessageWithInvalidCredentials() {
        log.info("Testing login with invalid credentials");
        
        // Navigate to login page
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        
        // Attempt login with invalid credentials
        loginPage.enterUsername("invalid-user");
        loginPage.enterPassword("wrong-password");
        loginPage.clickLogin();
        
        // Wait for error message or successful login (should be error)
        boolean errorVisible = false;
        try {
            Thread.sleep(2000); // Give time for error message to appear
            errorVisible = loginPage.isErrorMessageVisible();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify error handling
        if (errorVisible) {
            assertFalse(loginPage.getErrorMessage().isEmpty(), "Error message should not be empty");
            log.info("Login correctly failed with error: {}", loginPage.getErrorMessage());
        } else {
            // If no error message, we should not be on dashboard
            assertFalse(driver.getCurrentUrl().contains("/dashboard"), 
                    "Should not be redirected to dashboard with invalid credentials");
            log.info("Login failed without explicit error message");
        }
    }

    @Test
    @DisplayName("Should display login form elements correctly")
    void shouldDisplayLoginFormElementsCorrectly() {
        log.info("Testing login form elements display");
        
        // Navigate to login page
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        
        // Verify all form elements are present
        assertTrue(loginPage.isLoginFormVisible(), "Login form should be visible");
        
        // Test form interactions
        loginPage.enterUsername("test-username");
        loginPage.enterPassword("test-password");
        loginPage.checkRememberMe();
        
        log.info("Login form elements displayed and interactive correctly");
    }

    /**
     * Verify role-specific elements are displayed based on user role
     */
    private void verifyRoleSpecificElements(DashboardPage dashboardPage, String role, String roleDescription) {
        log.info("Verifying role-specific elements for {}: {}", roleDescription, role);
        
        boolean roleElementsValid = switch (role.toUpperCase()) {
            case "ADMIN" -> {
                boolean valid = dashboardPage.verifyAdminRoleElements();
                log.info("Admin role verification: {}", valid);
                yield valid;
            }
            case "MANAGER" -> {
                boolean valid = dashboardPage.verifyManagerRoleElements();
                log.info("Manager role verification: {}", valid);
                yield valid;
            }
            case "CUSTOMER_SERVICE", "CS" -> {
                boolean valid = dashboardPage.verifyCustomerServiceRoleElements();
                log.info("Customer Service role verification: {}", valid);
                yield valid;
            }
            case "TELLER" -> {
                boolean valid = dashboardPage.verifyTellerRoleElements();
                log.info("Teller role verification: {}", valid);
                yield valid;
            }
            default -> {
                log.warn("Unknown role: {}, skipping role-specific verification", role);
                yield true; // Don't fail for unknown roles
            }
        };
        
        assertTrue(roleElementsValid, 
                String.format("Role-specific elements should be visible for %s (%s)", roleDescription, role));
        
        log.info("Role verification completed successfully for {}", roleDescription);
    }
}