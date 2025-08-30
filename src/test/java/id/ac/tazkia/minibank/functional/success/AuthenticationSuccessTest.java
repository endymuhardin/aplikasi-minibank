package id.ac.tazkia.minibank.functional.success;

import id.ac.tazkia.minibank.functional.config.BasePlaywrightTest;
import id.ac.tazkia.minibank.functional.pages.DashboardPage;
import id.ac.tazkia.minibank.functional.pages.LoginPage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Tag("playwright-success")
@DisplayName("Authentication Success Scenario Tests")
class AuthenticationSuccessTest extends BasePlaywrightTest {

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/functional/login-credentials-essential.csv", numLinesToSkip = 1)
    @DisplayName("Should successfully login with valid credentials for all user roles")
    void shouldLoginSuccessfullyWithValidCredentials(String username, String password, String expectedRole, String roleDescription) {
        log.info("Success Test: Login for {}: {} with role {}", roleDescription, username, expectedRole);
        
        // Navigate to login page
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        
        assertTrue(loginPage.isLoginPageLoaded(), "Login page should be loaded");
        
        // Perform login
        DashboardPage dashboardPage = loginPage.loginWith(username, password);
        
        // Verify successful login by checking dashboard
        assertTrue(dashboardPage.isDashboardLoaded(), 
                "Should successfully login and redirect to dashboard for " + roleDescription);
        
        // Verify user is properly authenticated
        assertTrue(dashboardPage.isNavigationMenuVisible(), 
                "Navigation menu should be visible after successful login");
        
        // Verify logout functionality is available
        assertTrue(dashboardPage.isLogoutButtonVisible(), 
                "Logout button should be visible for authenticated user");
        
        log.info("✅ Login successful for {}", roleDescription);
    }

    @Test
    @DisplayName("Should successfully display login page with all required elements")
    void shouldDisplayLoginPageWithRequiredElements() {
        log.info("Success Test: Login page display");
        
        // Navigate to login page
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        
        // Verify login page is loaded with all elements
        assertTrue(loginPage.isLoginPageLoaded(), "Login page should be loaded");
        
        // Verify essential form elements are present
        String pageContent = page.content();
        assertTrue(pageContent.contains("Username") || pageContent.contains("username"), 
                "Username field should be visible");
        assertTrue(pageContent.contains("Password") || pageContent.contains("password"), 
                "Password field should be visible");
        
        // Verify login button is present and enabled
        assertTrue(loginPage.isLoginButtonEnabled(), "Login button should be enabled");
        
        log.info("✅ Login page displayed correctly with all required elements");
    }

    @Test
    @DisplayName("Should successfully handle direct navigation to dashboard when not authenticated")
    void shouldRedirectToLoginWhenNotAuthenticated() {
        log.info("Success Test: Redirect to login when not authenticated");
        
        // Try to navigate directly to dashboard without logging in
        page.navigate(baseUrl + "/dashboard");
        
        // Should be redirected to login page
        page.waitForURL("**/login");
        assertTrue(page.url().contains("/login"), 
                "Should redirect to login page when accessing protected resource without authentication");
        
        // Verify login page is displayed
        LoginPage loginPage = new LoginPage(page);
        assertTrue(loginPage.isLoginPageLoaded(), "Login page should be displayed after redirect");
        
        log.info("✅ Successfully redirected to login page when accessing protected resource");
    }

    @Test
    @DisplayName("Should successfully logout and redirect to login page")
    void shouldLogoutSuccessfullyAndRedirectToLogin() {
        log.info("Success Test: Logout functionality");
        
        // First login
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");
        
        // Perform logout
        LoginPage logoutLoginPage = dashboardPage.logout();
        
        // Verify logout was successful - Spring Security redirects to /login?logout=true
        page.waitForURL("**/login?logout=true");
        assertTrue(page.url().contains("/login"), 
                "Should be redirected to login page after logout");
        assertTrue(logoutLoginPage.isLoginPageLoaded(), 
                "Login page should be displayed after logout");
        
        log.info("✅ Logout successful");
    }

    @Test
    @DisplayName("Should successfully maintain session across page navigation")
    void shouldMaintainSessionAcrossNavigation() {
        log.info("Success Test: Session maintenance across navigation");
        
        // Login first
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");
        
        // Navigate to different pages
        page.navigate(baseUrl + "/product/list");
        page.waitForLoadState();
        
        // Verify session is maintained (no redirect to login)
        assertFalse(page.url().contains("/login"), 
                "Should not be redirected to login page when session is valid");
        
        // Navigate back to dashboard
        dashboardPage.navigateTo(baseUrl);
        assertTrue(dashboardPage.isDashboardLoaded(), 
                "Should still have access to dashboard");
        
        log.info("✅ Session maintained successfully across navigation");
    }

    @Test
    @DisplayName("Should successfully clear form fields")
    void shouldClearLoginFormFields() {
        log.info("Success Test: Login form field clearing");
        
        // Navigate to login page
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        
        assertTrue(loginPage.isLoginPageLoaded(), "Login page should be loaded");
        
        // Fill form fields
        loginPage.fillUsername("testuser");
        loginPage.fillPassword("testpass");
        
        // Verify fields are filled
        assertEquals("testuser", page.locator("#username").inputValue(), 
                "Username field should be filled");
        assertEquals("testpass", page.locator("#password").inputValue(), 
                "Password field should be filled");
        
        // Clear form
        loginPage.clearForm();
        
        // Verify fields are cleared
        assertEquals("", page.locator("#username").inputValue(), 
                "Username field should be cleared");
        assertEquals("", page.locator("#password").inputValue(), 
                "Password field should be cleared");
        
        log.info("✅ Form fields cleared successfully");
    }

    @Test
    @DisplayName("Should successfully handle page refresh while maintaining login state")
    void shouldMaintainLoginStateAfterRefresh() {
        log.info("Success Test: Login state maintenance after page refresh");
        
        // Login first
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");
        
        // Refresh the page
        page.reload();
        
        // Verify login state is maintained
        dashboardPage.waitForPageLoad();
        assertTrue(dashboardPage.isDashboardLoaded(), 
                "Should maintain login state after page refresh");
        assertTrue(dashboardPage.isNavigationMenuVisible(), 
                "Navigation menu should remain visible after refresh");
        
        log.info("✅ Login state maintained successfully after page refresh");
    }

    @Test
    @DisplayName("Should successfully display welcome message after login")
    void shouldDisplayWelcomeMessageAfterLogin() {
        log.info("Success Test: Welcome message display");
        
        // Login
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");
        
        // Verify welcome message is displayed
        String welcomeMessage = dashboardPage.getWelcomeMessage();
        assertFalse(welcomeMessage.isEmpty(), "Welcome message should not be empty");
        
        // Verify user info is displayed
        String userInfo = dashboardPage.getUserInfo();
        assertFalse(userInfo.isEmpty(), "User info should not be empty");
        
        log.info("✅ Welcome message and user info displayed successfully");
    }

    @Test
    @DisplayName("Should successfully validate navigation menu access based on user permissions")
    void shouldValidateNavigationMenuAccess() {
        log.info("Success Test: Navigation menu access validation");
        
        // Login as admin (should have access to all features)
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");
        assertTrue(dashboardPage.isNavigationMenuVisible(), "Navigation menu should be visible");
        
        // Verify admin has access to key navigation items
        assertTrue(dashboardPage.isNavigationLinkVisible("product"), 
                "Admin should have access to product management");
        assertTrue(dashboardPage.isNavigationLinkVisible("user"), 
                "Admin should have access to user management");
        
        log.info("✅ Navigation menu access validated successfully");
    }
}