package id.ac.tazkia.minibank.functional.web;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import id.ac.tazkia.minibank.functional.web.pageobject.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Comprehensive Selenium tests for Cash Deposit (Setoran Tunai) functionality.
 * 
 * Test Coverage:
 * - Navigation from dashboard to transaction list to cash deposit
 * - Account selection with search functionality
 * - Cash deposit form validation and submission
 * - Transaction verification and detail view
 * - Error handling and edge cases
 * - Multi-role testing (Teller, Manager)
 * 
 * Follows the Page Object Model pattern for maintainable and reusable UI automation.
 */
@Slf4j
@SqlGroup({
    @Sql(scripts = "/sql/setup/setup-deposit-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(scripts = "/sql/cleanup/cleanup-deposit-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class CashDepositSeleniumTest extends BaseSeleniumTest {
    
    @Override
    protected void performInitialLogin() {
        // Use Teller role for transaction operations (primary use case)
        loginHelper.loginAsTeller();
    }
    
    @Test
    @DisplayName("Should navigate successfully through cash deposit workflow")
    void shouldNavigateThroughCashDepositWorkflow() {
        log.info("🧪 TEST START: shouldNavigateThroughCashDepositWorkflow");
        
        // Navigate to transaction list from dashboard
        DashboardPage dashboardPage = new DashboardPage(driver, baseUrl);
        assertTrue(dashboardPage.isOnDashboardPage(), "Should be on dashboard after login");
        
        TransactionListPage transactionListPage = dashboardPage.clickTransactionLink();
        assertTrue(transactionListPage.isOnTransactionListPage(), "Should navigate to transaction list");
        
        // Click cash deposit button
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashDepositButton();
        assertTrue(accountSelectionPage.isOnAccountSelectionPage(), "Should navigate to account selection");
        
        // Verify accounts are available
        assertTrue(accountSelectionPage.hasAccounts(), "Should have accounts available for selection");
        
        // Select first account
        CashDepositFormPage cashDepositFormPage = accountSelectionPage.selectFirstAccount();
        assertTrue(cashDepositFormPage.isOnCashDepositFormPage(), "Should navigate to cash deposit form");
        
        // Verify account information is displayed
        assertFalse(cashDepositFormPage.getAccountNumber().isEmpty(), "Account number should be displayed");
        assertFalse(cashDepositFormPage.getAccountName().isEmpty(), "Account name should be displayed");
        assertFalse(cashDepositFormPage.getCurrentBalance().isEmpty(), "Current balance should be displayed");
        
        log.info("✅ TEST PASS: shouldNavigateThroughCashDepositWorkflow completed successfully");
    }
    
    @Test
    @DisplayName("Should process cash deposit successfully with valid data")
    void shouldProcessCashDepositSuccessfully() {
        log.info("🧪 TEST START: shouldProcessCashDepositSuccessfully");
        
        // Navigate to cash deposit form
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashDepositButton();
        CashDepositFormPage cashDepositFormPage = accountSelectionPage.selectFirstAccount();
        
        // Store original balance for verification
        String originalBalance = cashDepositFormPage.getCurrentBalance();
        String accountNumber = cashDepositFormPage.getAccountNumber();
        
        // Fill and submit the form
        String depositAmount = "100000";
        String description = "Test setoran tunai via Selenium";
        String referenceNumber = "REF-TEST-001";
        String createdBy = "teller1";
        
        cashDepositFormPage.fillCompleteForm(depositAmount, description, referenceNumber, createdBy);
        
        // Verify new balance is calculated and displayed
        assertTrue(cashDepositFormPage.isNewBalanceDisplayed(), "New balance should be displayed after entering amount");
        
        // Submit the form
        TransactionListPage resultPage = cashDepositFormPage.submitForm();
        assertNotNull(resultPage, "Form submission should be successful");
        
        // Verify success message
        assertTrue(resultPage.hasSuccessMessage(), "Should display success message after deposit");
        String successMessage = resultPage.getSuccessMessageText();
        assertTrue(successMessage.contains("berhasil diproses"), "Success message should indicate successful processing");
        assertTrue(successMessage.contains("TXN"), "Success message should contain transaction number");
        
        // Verify the new transaction appears in the list
        assertTrue(resultPage.hasTransactions(), "Should have transactions in the list");
        assertEquals("DEPOSIT", resultPage.getFirstTransactionType(), "First transaction should be a deposit");
        assertTrue(resultPage.getFirstTransactionAmount().contains("100,000"), "Transaction amount should match deposit amount");
        
        log.info("✅ TEST PASS: shouldProcessCashDepositSuccessfully completed successfully");
    }
    
    @Test
    @DisplayName("Should validate required fields on cash deposit form")
    void shouldValidateRequiredFields() {
        log.info("🧪 TEST START: shouldValidateRequiredFields");
        
        // Navigate to cash deposit form
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashDepositButton();
        CashDepositFormPage cashDepositFormPage = accountSelectionPage.selectFirstAccount();
        
        // Verify required field attributes
        assertTrue(cashDepositFormPage.isAmountFieldRequired(), "Amount field should be required");
        assertTrue(cashDepositFormPage.isCreatedByFieldRequired(), "Created by field should be required");
        assertEquals("10000", cashDepositFormPage.getMinimumAmount(), "Minimum amount should be 10000");
        
        // Submit empty form
        cashDepositFormPage.submitFormExpectingError();
        
        // Verify validation errors are displayed
        assertTrue(cashDepositFormPage.hasValidationErrors() || cashDepositFormPage.hasErrorMessage(), 
                  "Should display validation errors for empty form");
        
        log.info("✅ TEST PASS: shouldValidateRequiredFields completed successfully");
    }
    
    @Test
    @DisplayName("Should reject deposit with amount below minimum")
    void shouldRejectDepositBelowMinimum() {
        log.info("🧪 TEST START: shouldRejectDepositBelowMinimum");
        
        // Navigate to cash deposit form
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashDepositButton();
        CashDepositFormPage cashDepositFormPage = accountSelectionPage.selectFirstAccount();
        
        // Fill form with amount below minimum (10,000)
        cashDepositFormPage.fillCompleteForm("5000", "Test below minimum", "", "teller1");
        
        // Submit the form expecting error
        cashDepositFormPage.submitFormExpectingError();
        
        // Verify error is displayed
        assertTrue(cashDepositFormPage.hasErrorMessage() || cashDepositFormPage.hasValidationErrors(), 
                  "Should display error for amount below minimum");
        
        log.info("✅ TEST PASS: shouldRejectDepositBelowMinimum completed successfully");
    }
    
    @Test
    @DisplayName("Should handle account search functionality")
    void shouldHandleAccountSearch() {
        log.info("🧪 TEST START: shouldHandleAccountSearch");
        
        // Navigate to account selection page
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashDepositButton();
        
        // Get initial account count
        int initialAccountCount = accountSelectionPage.getAccountCount();
        assertTrue(initialAccountCount > 0, "Should have accounts initially");
        
        // Search for a specific account number
        if (accountSelectionPage.hasAccounts()) {
            String firstAccountNumber = accountSelectionPage.getFirstAccountNumber();
            
            // Search for the first account number
            accountSelectionPage.searchAccounts(firstAccountNumber);
            
            // Verify search results
            assertTrue(accountSelectionPage.hasAccounts(), "Should still have accounts after search");
            assertTrue(accountSelectionPage.getFirstAccountNumber().contains(firstAccountNumber.substring(0, 4)), 
                      "Search results should match search criteria");
            
            // Reset search
            accountSelectionPage.resetSearch();
            assertEquals(initialAccountCount, accountSelectionPage.getAccountCount(), 
                        "Should return to original account count after reset");
        }
        
        log.info("✅ TEST PASS: shouldHandleAccountSearch completed successfully");
    }
    
    @Test
    @DisplayName("Should view transaction details after successful deposit")
    void shouldViewTransactionDetails() {
        log.info("🧪 TEST START: shouldViewTransactionDetails");
        
        // Process a cash deposit first
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashDepositButton();
        CashDepositFormPage cashDepositFormPage = accountSelectionPage.selectFirstAccount();
        
        String depositAmount = "50000";
        String description = "Test detail view transaction";
        String createdBy = "teller1";
        
        cashDepositFormPage.fillCompleteForm(depositAmount, description, "", createdBy);
        TransactionListPage resultPage = cashDepositFormPage.submitForm();
        
        // Verify transaction was created
        assertNotNull(resultPage, "Deposit should be successful");
        assertTrue(resultPage.hasTransactions(), "Should have transactions");
        
        // View the transaction details
        TransactionViewPage transactionViewPage = resultPage.clickViewDetailForFirstTransaction();
        assertTrue(transactionViewPage.isOnTransactionViewPage(), "Should navigate to transaction view page");
        
        // Verify transaction details
        assertTrue(transactionViewPage.getTransactionNumber().startsWith("TXN"), 
                  "Transaction number should start with TXN");
        assertTrue(transactionViewPage.isDepositTransaction(), "Should be a deposit transaction");
        assertTrue(transactionViewPage.isTellerChannel(), "Should use TELLER channel");
        assertTrue(transactionViewPage.getTransactionAmount().contains("50,000"), 
                  "Amount should match deposit amount");
        assertTrue(transactionViewPage.hasDescription(), "Should have description");
        assertTrue(transactionViewPage.getDescription().contains(description), 
                  "Description should match input");
        
        // Verify balance calculations
        assertTrue(transactionViewPage.isBalanceChangeCorrect(), 
                  "Balance before + amount should equal balance after");
        
        // Navigate back to transaction list
        TransactionListPage backToListPage = transactionViewPage.clickBackToTransactionList();
        assertTrue(backToListPage.isOnTransactionListPage(), "Should navigate back to transaction list");
        
        log.info("✅ TEST PASS: shouldViewTransactionDetails completed successfully");
    }
    
    @Test
    @DisplayName("Should handle form cancellation correctly")
    void shouldHandleFormCancellation() {
        log.info("🧪 TEST START: shouldHandleFormCancellation");
        
        // Navigate to cash deposit form
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashDepositButton();
        CashDepositFormPage cashDepositFormPage = accountSelectionPage.selectFirstAccount();
        
        // Fill some data in the form
        cashDepositFormPage.fillAmount("25000");
        cashDepositFormPage.fillDescription("Test cancellation");
        
        // Cancel the form
        AccountSelectionPage returnedPage = cashDepositFormPage.clickCancel();
        assertTrue(returnedPage.isOnAccountSelectionPage(), "Should return to account selection page");
        
        // Verify we can still proceed with a different transaction
        CashDepositFormPage newFormPage = returnedPage.selectFirstAccount();
        assertTrue(newFormPage.isOnCashDepositFormPage(), "Should be able to start new transaction");
        
        log.info("✅ TEST PASS: shouldHandleFormCancellation completed successfully");
    }
    
    @Test
    @DisplayName("Should filter transactions by type in transaction list")
    void shouldFilterTransactionsByType() {
        log.info("🧪 TEST START: shouldFilterTransactionsByType");
        
        // Navigate to transaction list
        TransactionListPage transactionListPage = navigateToTransactionList();
        
        // Filter by DEPOSIT type
        transactionListPage.filterByTransactionType("DEPOSIT");
        
        // Verify filter is applied (if there are transactions)
        if (transactionListPage.hasTransactions()) {
            assertEquals("DEPOSIT", transactionListPage.getFirstTransactionType(), 
                        "Filtered results should only show deposit transactions");
        }
        
        // Reset filters
        transactionListPage.resetFilters();
        
        log.info("✅ TEST PASS: shouldFilterTransactionsByType completed successfully");
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/transaction/valid-cash-deposits.csv", numLinesToSkip = 1)
    @DisplayName("Should process various valid cash deposit amounts")
    void shouldProcessValidCashDeposits(String amount, String description, String expectedResult) {
        log.info("🧪 PARAMETERIZED TEST: shouldProcessValidCashDeposits with amount: {}", amount);
        
        // Navigate to cash deposit form
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashDepositButton();
        CashDepositFormPage cashDepositFormPage = accountSelectionPage.selectFirstAccount();
        
        // Fill and submit the form
        cashDepositFormPage.fillCompleteForm(amount, description, "", "teller1");
        TransactionListPage resultPage = cashDepositFormPage.submitForm();
        
        if ("SUCCESS".equals(expectedResult)) {
            assertNotNull(resultPage, "Deposit should be successful for valid amount: " + amount);
            assertTrue(resultPage.hasSuccessMessage(), "Should show success message for valid amount: " + amount);
        } else {
            // For error cases, we should stay on the form page
            assertTrue(cashDepositFormPage.hasErrorMessage() || cashDepositFormPage.hasValidationErrors(), 
                      "Should show error for invalid amount: " + amount);
        }
        
        log.info("✅ PARAMETERIZED TEST PASS: shouldProcessValidCashDeposits completed for amount: {}", amount);
    }
    
    @Test
    @DisplayName("Should work with Manager role permissions")
    void shouldWorkWithManagerRole() {
        log.info("🧪 TEST START: shouldWorkWithManagerRole");
        
        // Login as Manager instead of Teller
        loginHelper.loginAsManager();
        
        // Navigate and process cash deposit
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashDepositButton();
        CashDepositFormPage cashDepositFormPage = accountSelectionPage.selectFirstAccount();
        
        // Process deposit as manager
        cashDepositFormPage.fillCompleteForm("75000", "Manager deposit test", "MGR-001", "admin");
        TransactionListPage resultPage = cashDepositFormPage.submitForm();
        
        assertNotNull(resultPage, "Manager should be able to process cash deposits");
        assertTrue(resultPage.hasSuccessMessage(), "Manager deposit should be successful");
        
        log.info("✅ TEST PASS: shouldWorkWithManagerRole completed successfully");
    }
    
    // Helper methods
    
    private TransactionListPage navigateToTransactionList() {
        DashboardPage dashboardPage = new DashboardPage(driver, baseUrl);
        return dashboardPage.clickTransactionLink();
    }
    
    private void assertTransactionIsCreated(TransactionListPage transactionListPage, String expectedAmount) {
        assertTrue(transactionListPage.hasTransactions(), "Should have transactions");
        assertEquals("DEPOSIT", transactionListPage.getFirstTransactionType(), "Should be deposit transaction");
        assertTrue(transactionListPage.getFirstTransactionAmount().contains(expectedAmount.replace("000", ",000")), 
                  "Amount should match expected");
    }
}