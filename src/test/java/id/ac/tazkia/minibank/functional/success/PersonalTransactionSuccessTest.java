package id.ac.tazkia.minibank.functional.success;

import id.ac.tazkia.minibank.functional.config.BasePlaywrightTest;
import id.ac.tazkia.minibank.functional.pages.TransactionPage;
import id.ac.tazkia.minibank.functional.pages.LoginPage;
import id.ac.tazkia.minibank.functional.pages.DashboardPage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Tag("playwright-success")
@DisplayName("Personal Customer Transaction Success Scenario Tests")
class PersonalTransactionSuccessTest extends BasePlaywrightTest {
    
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
        
        log.info("Personal transaction test setup complete - using personal accounts (TAB001, TAB002)");
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/functional/personal-transaction-deposit-success.csv", numLinesToSkip = 1)
    @DisplayName("Should successfully process cash deposits for personal customers")
    void shouldProcessPersonalCashDepositSuccessfully(
            String accountIdentifier, String amount, String description, 
            String channel, String expectedResult) {
        
        log.info("Personal Success Test: Processing {} deposit for account: {}", channel, accountIdentifier);
        
        // Navigate to deposit page
        transactionPage.navigateToDeposit(baseUrl);
        page.waitForLoadState();
        
        // Fill deposit form
        transactionPage.fillDepositForm(accountIdentifier, amount, description, channel);
        
        // Submit transaction
        transactionPage.clickSubmit();
        page.waitForLoadState();
        
        // Debug: Check current page state
        log.info("Current URL after submit: {}", page.url());
        log.info("Success message visible: {}", transactionPage.isSuccessMessageVisible());
        log.info("On view page: {}", transactionPage.isOnViewPage());
        
        // Check for any error messages on the page
        if (page.locator("#error-message").isVisible()) {
            String errorText = page.locator("#error-message").textContent();
            log.error("Error message found: {}", errorText);
        }
        if (page.locator("#validation-errors").isVisible()) {
            String validationText = page.locator("#validation-errors").textContent();
            log.error("Validation errors found: {}", validationText);
        }
        
        // Verify success
        assertTrue(transactionPage.isSuccessMessageVisible() || transactionPage.isOnViewPage(),
                "Should show success message or redirect to transaction receipt");
        
        // If on view page, verify transaction details
        if (transactionPage.isOnViewPage()) {
            String transactionNumber = transactionPage.getTransactionNumber();
            assertFalse(transactionNumber.isEmpty(), "Transaction number should be generated");
            assertTrue(transactionNumber.matches("TXN\\d+"), 
                    "Transaction number should follow pattern TXNxxxxxxx");
            
            String balanceAfter = transactionPage.getBalanceAfter();
            assertFalse(balanceAfter.isEmpty(), "Balance after should be displayed");
            
            String txDetails = transactionPage.getTransactionDetailsText();
            assertTrue(txDetails.contains(amount) || txDetails.contains(formatAmount(amount)),
                    "Transaction amount should be displayed");
            assertTrue(txDetails.contains(description) || txDetails.contains(channel),
                    "Transaction description or channel should be displayed");
            
            log.info("✅ Personal deposit processed successfully: {} via {}", transactionNumber, channel);
        }
    }
    
    @Test
    @DisplayName("Should successfully process Tabungan Wadiah deposits")
    void shouldProcessTabunganWadiahDepositSuccessfully() {
        log.info("Personal Success Test: Tabungan Wadiah deposit");
        
        // Navigate to deposit page
        transactionPage.navigateToDeposit(baseUrl);
        page.waitForLoadState();
        
        // Process deposit to Tabungan Wadiah account
        transactionPage.fillDepositForm("A2000001", "100000", "Wadiah Deposit Test", "TELLER");
        
        // Submit transaction
        transactionPage.clickSubmit();
        page.waitForLoadState();
        
        // Debug: Check current page state
        log.info("Current URL after submit: {}", page.url());
        log.info("Success message visible: {}", transactionPage.isSuccessMessageVisible());
        log.info("On view page: {}", transactionPage.isOnViewPage());
        
        // Check for any error messages on the page
        if (page.locator("#error-message").isVisible()) {
            String errorText = page.locator("#error-message").textContent();
            log.error("Error message found: {}", errorText);
        }
        if (page.locator("#validation-errors").isVisible()) {
            String validationText = page.locator("#validation-errors").textContent();
            log.error("Validation errors found: {}", validationText);
        }
        
        // Verify success
        assertTrue(transactionPage.isSuccessMessageVisible() || transactionPage.isOnViewPage(),
                "Should successfully process Tabungan Wadiah deposit");
        
        if (transactionPage.isOnViewPage()) {
            String details = transactionPage.getTransactionDetailsText();
            assertTrue(details.contains("100000") || details.contains("100.000"),
                    "Should show deposit amount");
            assertTrue(details.contains("Ahmad Suharto") || details.contains("A2000001"),
                    "Should show account information");
        }
        
        log.info("✅ Tabungan Wadiah deposit processed successfully");
    }
    
    @Test
    @DisplayName("Should successfully process Tabungan Mudharabah deposits")
    void shouldProcessTabunganMudharabahDepositSuccessfully() {
        log.info("Personal Success Test: Tabungan Mudharabah deposit");
        
        // Navigate to deposit page
        transactionPage.navigateToDeposit(baseUrl);
        page.waitForLoadState();
        
        // Process deposit to Tabungan Mudharabah account
        transactionPage.fillDepositForm("A2000002", "500000", "Mudharabah Deposit Test", "TELLER");
        
        // Submit transaction
        transactionPage.clickSubmit();
        page.waitForLoadState();
        
        // Verify success
        assertTrue(transactionPage.isSuccessMessageVisible() || transactionPage.isOnViewPage(),
                "Should successfully process Tabungan Mudharabah deposit");
        
        if (transactionPage.isOnViewPage()) {
            String details = transactionPage.getTransactionDetailsText();
            assertTrue(details.contains("500000") || details.contains("500.000"),
                    "Should show deposit amount");
            assertTrue(details.contains("Siti Nurhaliza") || details.contains("A2000002"),
                    "Should show account information");
        }
        
        log.info("✅ Tabungan Mudharabah deposit processed successfully");
    }
    
    @Test
    @DisplayName("Should successfully validate personal account balances after deposit")
    void shouldValidatePersonalAccountBalanceAfterDeposit() {
        log.info("Personal Success Test: Balance validation after deposit");
        
        // Navigate to deposit page
        transactionPage.navigateToDeposit(baseUrl);
        page.waitForLoadState();
        
        // Process a deposit
        String depositAmount = "300000";
        transactionPage.fillDepositForm("A2000003", depositAmount, "Balance Test", "TELLER");
        
        // Submit transaction
        transactionPage.clickSubmit();
        page.waitForLoadState();
        
        // Verify success and balance update
        assertTrue(transactionPage.isSuccessMessageVisible() || transactionPage.isOnViewPage(),
                "Should process deposit successfully");
        
        if (transactionPage.isOnViewPage()) {
            String balanceAfter = transactionPage.getBalanceAfter();
            assertFalse(balanceAfter.isEmpty(), "Balance after should be displayed");
            
            // The original balance for A2000003 is 750,000, so after 300,000 deposit = 1,050,000
            assertTrue(balanceAfter.contains("1050000") || balanceAfter.contains("1.050.000"),
                    "Balance should reflect the deposit");
        }
        
        log.info("✅ Personal account balance validated successfully after deposit");
    }
    
    /**
     * Helper method to format amount for display
     */
    private String formatAmount(String amount) {
        try {
            long value = Long.parseLong(amount);
            return String.format("%,d", value).replace(",", ".");
        } catch (NumberFormatException e) {
            return amount;
        }
    }
}