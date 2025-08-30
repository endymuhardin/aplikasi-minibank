package id.ac.tazkia.minibank.functional.alternate;

import id.ac.tazkia.minibank.functional.config.BasePlaywrightTest;
import id.ac.tazkia.minibank.functional.pages.DashboardPage;
import id.ac.tazkia.minibank.functional.pages.LoginPage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Tag("playwright-alternate")
@DisplayName("Authentication Alternate Scenario Tests")
class AuthenticationAlternateTest extends BasePlaywrightTest {

    @ParameterizedTest
    @CsvSource({
        "'invalid_user', 'minibank123', 'Invalid username'",
        "'admin', 'wrong_password', 'Invalid password'",
        "'', 'minibank123', 'Empty username'",
        "'admin', '', 'Empty password'",
        "'', '', 'Empty credentials'",
        "'test@user', 'password123', 'Special character in username'",
        "'user123', '123', 'Short password'",
        "'nonexistent', 'randompass', 'Non-existent user'"
    })
    @DisplayName("Should reject login attempts with invalid credentials")
    void shouldRejectInvalidLoginCredentials(String username, String password, String testCase) {
        log.info("Alternate Test: Invalid login - {}", testCase);
        
        // Navigate to login page
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        
        assertTrue(loginPage.isLoginPageLoaded(), "Login page should be loaded");
        
        // Attempt login with invalid credentials
        loginPage.fillUsername(username);
        loginPage.fillPassword(password);
        loginPage.clickLogin();
        
        // Wait a moment for response
        page.waitForTimeout(1000);
        
        // Should remain on login page (not redirect to dashboard)
        assertTrue(page.url().contains("/login"), 
                "Should remain on login page after failed login attempt");
        
        // Should not be able to access dashboard
        page.navigate(baseUrl + "/dashboard");
        page.waitForTimeout(500);
        
        // Should be redirected back to login or show access denied
        assertTrue(page.url().contains("/login") || page.content().contains("Access Denied") || page.content().contains("Unauthorized"), 
                "Should not have access to protected resources with invalid credentials");
        
        log.info("✅ Invalid login correctly rejected for case: {}", testCase);
    }

    @Test
    @DisplayName("Should handle SQL injection attempts in login form")
    void shouldHandleSQLInjectionAttempts() {
        log.info("Alternate Test: SQL injection prevention");
        
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        
        assertTrue(loginPage.isLoginPageLoaded(), "Login page should be loaded");
        
        // Test common SQL injection patterns
        String[] sqlInjectionAttempts = {
            "admin'--",
            "admin' OR '1'='1",
            "'; DROP TABLE users; --",
            "admin' UNION SELECT * FROM users --",
            "1' OR 1=1#"
        };
        
        for (String injectionAttempt : sqlInjectionAttempts) {
            loginPage.fillUsername(injectionAttempt);
            loginPage.fillPassword("anypassword");
            loginPage.clickLogin();
            
            page.waitForTimeout(500);
            
            // Should not be successful
            assertTrue(page.url().contains("/login"), 
                    "SQL injection attempt should not succeed: " + injectionAttempt);
            
            // Clear form for next attempt
            loginPage.clearForm();
        }
        
        log.info("✅ SQL injection attempts properly handled");
    }

    @Test
    @DisplayName("Should handle XSS attempts in login form")
    void shouldHandleXSSAttempts() {
        log.info("Alternate Test: XSS prevention");
        
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        
        assertTrue(loginPage.isLoginPageLoaded(), "Login page should be loaded");
        
        // Test XSS patterns
        String[] xssAttempts = {
            "<script>alert('xss')</script>",
            "javascript:alert('xss')",
            "<img src=x onerror=alert('xss')>",
            "<svg onload=alert('xss')>",
            "';alert('xss');//"
        };
        
        for (String xssAttempt : xssAttempts) {
            loginPage.fillUsername(xssAttempt);
            loginPage.fillPassword("password");
            loginPage.clickLogin();
            
            page.waitForTimeout(500);
            
            // Should not execute JavaScript or redirect to dashboard
            assertTrue(page.url().contains("/login"), 
                    "XSS attempt should not succeed: " + xssAttempt);
            
            // Verify no alert dialogs were triggered
            assertFalse(isAlertPresent(), "No JavaScript alert should be triggered");
            
            loginPage.clearForm();
        }
        
        log.info("✅ XSS attempts properly handled");
    }

    @Test
    @DisplayName("Should handle extremely long input values")
    void shouldHandleLongInputValues() {
        log.info("Alternate Test: Long input handling");
        
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        
        assertTrue(loginPage.isLoginPageLoaded(), "Login page should be loaded");
        
        // Create very long strings
        String longUsername = "a".repeat(1000);
        String longPassword = "b".repeat(1000);
        
        // Fill form with long values
        loginPage.fillUsername(longUsername);
        loginPage.fillPassword(longPassword);
        loginPage.clickLogin();
        
        page.waitForTimeout(1000);
        
        // Should handle gracefully without errors
        assertTrue(page.url().contains("/login"), 
                "Should remain on login page with long inputs");
        
        // Page should still be functional
        assertTrue(loginPage.isLoginPageLoaded(), 
                "Login page should remain functional after long input");
        
        log.info("✅ Long input values handled properly");
    }

    @Test
    @DisplayName("Should handle multiple rapid login attempts")
    void shouldHandleRapidLoginAttempts() {
        log.info("Alternate Test: Rapid login attempts");
        
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        
        assertTrue(loginPage.isLoginPageLoaded(), "Login page should be loaded");
        
        // Perform multiple rapid login attempts
        for (int i = 0; i < 5; i++) {
            loginPage.fillUsername("invalid_user_" + i);
            loginPage.fillPassword("invalid_pass_" + i);
            loginPage.clickLogin();
            
            // Very short wait between attempts
            page.waitForTimeout(100);
            
            // Should remain on login page
            assertTrue(page.url().contains("/login"), 
                    "Should remain on login page for attempt " + (i + 1));
        }
        
        // Page should still be functional after rapid attempts
        assertTrue(loginPage.isLoginPageLoaded(), 
                "Login page should remain functional after rapid attempts");
        
        log.info("✅ Rapid login attempts handled properly");
    }

    @Test
    @DisplayName("Should handle session timeout appropriately")
    void shouldHandleSessionTimeout() {
        log.info("Alternate Test: Session timeout handling");
        
        // Login first
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");
        
        // Simulate session timeout by clearing cookies/storage
        page.context().clearCookies();
        page.evaluate("() => { sessionStorage.clear(); localStorage.clear(); }");
        
        // Try to access protected resource
        page.navigate(baseUrl + "/product/list");
        page.waitForTimeout(1000);
        
        // Should be redirected to login due to session timeout
        assertTrue(page.url().contains("/login") || page.content().contains("login"), 
                "Should be redirected to login after session timeout");
        
        log.info("✅ Session timeout handled appropriately");
    }

    @Test
    @DisplayName("Should prevent access to protected resources without authentication")
    void shouldPreventUnauthorizedAccess() {
        log.info("Alternate Test: Unauthorized access prevention");
        
        // List of protected endpoints to test
        String[] protectedEndpoints = {
            "/dashboard",
            "/product/list",
            "/product/create",
            "/customer/list",
            "/account/list",
            "/user/list"
        };
        
        for (String endpoint : protectedEndpoints) {
            page.navigate(baseUrl + endpoint);
            page.waitForTimeout(500);
            
            // Should be redirected to login or show unauthorized
            assertTrue(page.url().contains("/login") || 
                      page.content().contains("Unauthorized") || 
                      page.content().contains("Access Denied"),
                    "Endpoint " + endpoint + " should be protected");
        }
        
        log.info("✅ Unauthorized access properly prevented for all protected endpoints");
    }

    @Test
    @DisplayName("Should handle concurrent login attempts")
    void shouldHandleConcurrentLogins() {
        log.info("Alternate Test: Concurrent login handling");
        
        // This test simulates what happens when same user tries to login from multiple sessions
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "First login should be successful");
        
        // Open new browser context (simulate new session)
        var newContext = page.context().browser().newContext();
        var newPage = newContext.newPage();
        
        // Try to login with same credentials from new session
        LoginPage newLoginPage = new LoginPage(newPage);
        newLoginPage.navigateTo(baseUrl);
        DashboardPage newDashboard = newLoginPage.loginWith("admin", "minibank123");
        
        // Both sessions should handle this gracefully
        // The behavior depends on the application's session management policy
        assertTrue(newDashboard.isDashboardLoaded() || newPage.url().contains("/login"), 
                "Concurrent login should be handled appropriately");
        
        // Cleanup
        newContext.close();
        
        log.info("✅ Concurrent login attempts handled appropriately");
    }

    @Test
    @DisplayName("Should validate input field constraints")
    void shouldValidateInputFieldConstraints() {
        log.info("Alternate Test: Input field validation");
        
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        
        assertTrue(loginPage.isLoginPageLoaded(), "Login page should be loaded");
        
        // Test with whitespace-only values
        loginPage.fillUsername("   ");
        loginPage.fillPassword("   ");
        loginPage.clickLogin();
        
        page.waitForTimeout(500);
        
        // Should not accept whitespace-only credentials
        assertTrue(page.url().contains("/login"), 
                "Should reject whitespace-only credentials");
        
        // Test with special control characters (excluding null bytes which PostgreSQL can't handle)
        loginPage.clearForm();
        loginPage.fillUsername("admin\u007F"); // DEL character instead of null byte
        loginPage.fillPassword("password\n\r\t");
        loginPage.clickLogin();
        
        page.waitForTimeout(500);
        
        // Should handle control characters appropriately
        assertTrue(page.url().contains("/login"), 
                "Should handle control characters appropriately");
        
        log.info("✅ Input field constraints validated properly");
    }

    private boolean isAlertPresent() {
        try {
            // Check for JavaScript alerts/dialogs
            return page.locator("dialog").isVisible() || 
                   page.evaluate("() => window.alert !== undefined && window.alert.toString() !== 'function alert() { [native code] }'").toString().equals("true");
        } catch (Exception e) {
            return false;
        }
    }
}