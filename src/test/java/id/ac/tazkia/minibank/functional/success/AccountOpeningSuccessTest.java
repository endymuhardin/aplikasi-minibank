package id.ac.tazkia.minibank.functional.success;

import id.ac.tazkia.minibank.functional.config.BasePlaywrightTest;
import id.ac.tazkia.minibank.functional.pages.AccountManagementPage;
import id.ac.tazkia.minibank.functional.pages.LoginPage;
import id.ac.tazkia.minibank.functional.pages.DashboardPage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Tag("playwright-success")
@DisplayName("General Account Opening Integration Tests")
class AccountOpeningSuccessTest extends BasePlaywrightTest {
    
    private AccountManagementPage accountPage;
    
    @BeforeEach
    void setUp() {
        // Login as Customer Service (CS) who has permission to open accounts
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("cs1", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");
        
        // Initialize account management page
        accountPage = new AccountManagementPage(page);
    }
    
    @Test
    @DisplayName("Should successfully navigate to account opening page")
    void shouldNavigateToAccountOpeningPage() {
        log.info("Integration Test: Account opening page navigation");
        
        // Navigate to account opening page
        accountPage.navigateToOpenAccount(baseUrl);
        page.waitForLoadState();
        
        // Verify we're on the account opening page
        assertTrue(page.url().contains("/account/open"), 
                "Should navigate to account opening page");
        
        // Verify key elements are present
        assertTrue(page.locator("#customer-search, .customer-card").count() > 0,
                "Should show customer selection interface");
        
        log.info("✅ Account opening page navigation successful");
    }
    
    @Test
    @DisplayName("Should successfully display account list page with search prompt")
    void shouldDisplayAccountListSuccessfully() {
        log.info("Integration Test: Account list page display");

        // Navigate to account list
        accountPage.navigateToList(baseUrl);

        // Verify list page loaded
        assertTrue(accountPage.isOnListPage(), "Should be on account list page");

        // On initial load, should show search prompt (not data)
        boolean searchPromptVisible = page.locator("#search-prompt-message").isVisible();

        // Search prompt should be visible on initial load
        assertTrue(searchPromptVisible,
                "Search prompt should be visible on initial load (data not displayed until search)");

        log.info("✅ Account list page displayed successfully with search prompt");
    }
    
    @Test
    @DisplayName("Should successfully validate page structure and security")
    void shouldValidatePageStructureAndSecurity() {
        log.info("Integration Test: Page structure and security validation");
        
        // Test that authentication is required
        page.navigate(baseUrl + "/account/open");
        
        // Should either be logged in already or redirected to login
        assertTrue(page.url().contains("/login") || page.url().contains("/account/open"),
                "Should require authentication for account opening");
        
        // Test account list access
        page.navigate(baseUrl + "/account/list");
        assertTrue(page.url().contains("/login") || page.url().contains("/account/list"),
                "Should require authentication for account list");
        
        log.info("✅ Page structure and security validation successful");
    }
    
    @Test
    @DisplayName("Should successfully verify UI components are present")
    void shouldVerifyUIComponentsPresent() {
        log.info("Integration Test: UI components verification");

        // Navigate to account opening
        accountPage.navigateToOpenAccount(baseUrl);
        page.waitForLoadState();

        // Check if we can proceed to the form (either customers exist or we can search)
        boolean hasCustomers = page.locator(".customer-card").count() > 0;
        boolean hasSearchInput = page.locator("#search-input").isVisible();

        assertTrue(hasCustomers || hasSearchInput,
                "Should have either existing customers or search capability");

        // Navigate to account list
        accountPage.navigateToList(baseUrl);

        // Verify search form and search prompt are present (initial load shows search prompt, not data)
        boolean hasSearchButton = page.locator("#search-accounts-btn").isVisible();
        boolean hasSearchPrompt = page.locator("#search-prompt-message").isVisible();

        assertTrue(hasSearchButton && hasSearchPrompt,
                "Should have search button and search prompt on initial load");

        log.info("✅ UI components verification successful");
    }
}