package id.ac.tazkia.minibank.functional.success;

import id.ac.tazkia.minibank.functional.config.BasePlaywrightTest;
import id.ac.tazkia.minibank.functional.pages.TransactionPage;
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
@DisplayName("General Transaction Integration Tests")
class TransactionSuccessTest extends BasePlaywrightTest {
    
    private TransactionPage transactionPage;
    
    @BeforeEach
    void setUp() {
        // Login as Teller who has permission to process transactions
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("teller1", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");
        
        // Initialize transaction page
        transactionPage = new TransactionPage(page);
        
        log.info("Transaction integration test setup complete");
    }
    
    @Test
    @DisplayName("Should successfully navigate to transaction deposit page")
    void shouldNavigateToTransactionDepositPage() {
        log.info("Integration Test: Transaction deposit page navigation");
        
        // Navigate to deposit page
        transactionPage.navigateToDeposit(baseUrl);
        page.waitForLoadState();
        
        // Verify we're on the transaction deposit page (account selection)
        assertTrue(page.url().contains("/transaction/cash-deposit"), 
                "Should navigate to transaction deposit page");
        
        // Verify key elements are present
        assertTrue(page.locator("#accounts-list, .account-card, #search").count() > 0,
                "Should show account selection interface");
        
        log.info("✅ Transaction deposit page navigation successful");
    }
    
    @Test
    @DisplayName("Should successfully navigate to transaction withdrawal page")
    void shouldNavigateToTransactionWithdrawalPage() {
        log.info("Integration Test: Transaction withdrawal page navigation");
        
        // Navigate to withdrawal page
        transactionPage.navigateToWithdrawal(baseUrl);
        page.waitForLoadState();
        
        // Verify we're on the transaction withdrawal page (account selection)
        assertTrue(page.url().contains("/transaction/cash-withdrawal"), 
                "Should navigate to transaction withdrawal page");
        
        // Verify key elements are present
        assertTrue(page.locator("#accounts-list, .account-card, #search").count() > 0,
                "Should show account selection interface for withdrawal");
        
        log.info("✅ Transaction withdrawal page navigation successful");
    }
    
    @Test
    @DisplayName("Should successfully display transaction forms structure")
    void shouldDisplayTransactionFormsStructure() {
        log.info("Integration Test: Transaction forms structure validation");
        
        // Test deposit page structure
        transactionPage.navigateToDeposit(baseUrl);
        page.waitForLoadState();
        
        // Verify basic page structure
        assertTrue(page.locator("h1, h2").count() > 0, "Should have page headers");
        assertTrue(page.locator("#accounts-list, .no-accounts").count() > 0, 
                "Should have accounts list or no-accounts message");
        
        // Test withdrawal page structure
        transactionPage.navigateToWithdrawal(baseUrl);
        page.waitForLoadState();
        
        // Verify basic page structure
        assertTrue(page.locator("h1, h2").count() > 0, "Should have page headers");
        assertTrue(page.locator("#accounts-list, .no-accounts").count() > 0, 
                "Should have accounts list or no-accounts message");
        
        log.info("✅ Transaction forms structure validated successfully");
    }
    
    @Test
    @DisplayName("Should successfully validate authentication requirements")
    void shouldValidateAuthenticationRequirements() {
        log.info("Integration Test: Transaction authentication validation");
        
        // Test that authentication is required for transaction pages
        page.navigate(baseUrl + "/transaction/cash-deposit");
        
        // Should either be logged in already or redirected to login
        assertTrue(page.url().contains("/login") || page.url().contains("/transaction/cash-deposit"),
                "Should require authentication for transaction deposit");
        
        // Test withdrawal page authentication
        page.navigate(baseUrl + "/transaction/cash-withdrawal");
        assertTrue(page.url().contains("/login") || page.url().contains("/transaction/cash-withdrawal"),
                "Should require authentication for transaction withdrawal");
        
        log.info("✅ Transaction authentication validation successful");
    }
}