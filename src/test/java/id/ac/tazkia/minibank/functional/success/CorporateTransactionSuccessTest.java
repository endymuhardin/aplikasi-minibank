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
@DisplayName("Corporate Customer Transaction Success Scenario Tests")
class CorporateTransactionSuccessTest extends BasePlaywrightTest {
    
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
        
        log.info("Corporate transaction test setup complete - using corporate Giro account (GIR001)");
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/functional/corporate-transaction-deposit-success.csv", numLinesToSkip = 1)
    @DisplayName("Should successfully process cash deposits for corporate customers")
    void shouldProcessCorporateCashDepositSuccessfully(
            String accountIdentifier, String amount, String description, 
            String channel, String expectedResult) {
        
        log.info("Corporate Success Test: Processing {} deposit for account: {}", channel, accountIdentifier);
        
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
            assertTrue(txDetails.contains(description) || txDetails.contains("PT. Teknologi Maju"),
                    "Transaction description or corporate customer should be displayed");
            
            log.info("✅ Corporate deposit processed successfully: {} via {}", transactionNumber, channel);
        }
    }
    
    @Test
    @DisplayName("Should successfully process Giro Wadiah Corporate deposits")
    void shouldProcessGiroWadiahCorporateDepositSuccessfully() {
        log.info("Corporate Success Test: Giro Wadiah Corporate deposit");
        
        // Navigate to deposit page
        transactionPage.navigateToDeposit(baseUrl);
        page.waitForLoadState();
        
        // Process deposit to Giro Wadiah Corporate account
        transactionPage.fillDepositForm("A2000005", "2000000", "Corporate Business Deposit", "TELLER");
        
        // Submit transaction
        transactionPage.clickSubmit();
        page.waitForLoadState();
        
        // Verify success
        assertTrue(transactionPage.isSuccessMessageVisible() || transactionPage.isOnViewPage(),
                "Should successfully process Giro Wadiah Corporate deposit");
        
        if (transactionPage.isOnViewPage()) {
            String details = transactionPage.getTransactionDetailsText();
            assertTrue(details.contains("2000000") || details.contains("2.000.000"),
                    "Should show deposit amount");
            assertTrue(details.contains("PT. Teknologi Maju") || details.contains("A2000005"),
                    "Should show corporate account information");
        }
        
        log.info("✅ Giro Wadiah Corporate deposit processed successfully");
    }
    
    @Test
    @DisplayName("Should successfully handle high-value corporate deposits")
    void shouldHandleHighValueCorporateDepositsSuccessfully() {
        log.info("Corporate Success Test: High-value corporate deposit");
        
        // Navigate to deposit page
        transactionPage.navigateToDeposit(baseUrl);
        page.waitForLoadState();
        
        // Process high-value deposit typical for corporate accounts
        String highAmount = "50000000"; // 50 million
        transactionPage.fillDepositForm("A2000005", highAmount, "Large Corporate Deposit", "TELLER");
        
        // Submit transaction
        transactionPage.clickSubmit();
        page.waitForLoadState();
        
        // Verify success
        assertTrue(transactionPage.isSuccessMessageVisible() || transactionPage.isOnViewPage(),
                "Should successfully process high-value corporate deposit");
        
        if (transactionPage.isOnViewPage()) {
            String details = transactionPage.getTransactionDetailsText();
            assertTrue(details.contains("50000000") || details.contains("50.000.000"),
                    "Should show high-value deposit amount");
            
            String balanceAfter = transactionPage.getBalanceAfter();
            assertFalse(balanceAfter.isEmpty(), "Balance after should be displayed for high-value deposit");
        }
        
        log.info("✅ High-value corporate deposit processed successfully");
    }
    
    @Test
    @DisplayName("Should successfully validate corporate account balances after deposit")
    void shouldValidateCorporateAccountBalanceAfterDeposit() {
        log.info("Corporate Success Test: Balance validation after corporate deposit");
        
        // Navigate to deposit page
        transactionPage.navigateToDeposit(baseUrl);
        page.waitForLoadState();
        
        // Process a corporate deposit
        String depositAmount = "5000000";
        transactionPage.fillDepositForm("A2000005", depositAmount, "Corporate Balance Test", "TELLER");
        
        // Submit transaction
        transactionPage.clickSubmit();
        page.waitForLoadState();
        
        // Verify success and balance update
        assertTrue(transactionPage.isSuccessMessageVisible() || transactionPage.isOnViewPage(),
                "Should process corporate deposit successfully");
        
        if (transactionPage.isOnViewPage()) {
            String balanceAfter = transactionPage.getBalanceAfter();
            assertFalse(balanceAfter.isEmpty(), "Corporate balance after should be displayed");
            
            // The original balance for A2000005 is 5,000,000, so after 5,000,000 deposit = 10,000,000
            assertTrue(balanceAfter.contains("10000000") || balanceAfter.contains("10.000.000"),
                    "Corporate balance should reflect the deposit");
        }
        
        log.info("✅ Corporate account balance validated successfully after deposit");
    }
    
    @Test
    @DisplayName("Should successfully validate corporate Wadiah account characteristics")
    void shouldValidateCorporateWadiahAccountCharacteristics() {
        log.info("Corporate Success Test: Wadiah account characteristics validation");
        
        // Navigate to deposit page
        transactionPage.navigateToDeposit(baseUrl);
        page.waitForLoadState();
        
        // Process deposit to corporate Wadiah account
        transactionPage.fillDepositForm("A2000005", "1000000", "Wadiah Test Deposit", "TELLER");
        
        // Submit transaction
        transactionPage.clickSubmit();
        page.waitForLoadState();
        
        // Verify success
        assertTrue(transactionPage.isSuccessMessageVisible() || transactionPage.isOnViewPage(),
                "Should process Wadiah account deposit successfully");
        
        if (transactionPage.isOnViewPage()) {
            String details = transactionPage.getTransactionDetailsText();
            
            // Wadiah accounts are safe-keeping accounts, not profit-sharing
            // Verify corporate context is maintained
            assertTrue(details.contains("PT.") || details.contains("Corporate") || details.contains("Giro"),
                    "Should show corporate account context");
        }
        
        log.info("✅ Corporate Wadiah account characteristics validated successfully");
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