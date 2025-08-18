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
 * Comprehensive Selenium tests for Cash Withdrawal (Penarikan Tunai) functionality.
 * 
 * This test class follows the technical practices and lessons learned from the 
 * Technical Practices Guide, specifically:
 * 
 * - Uses ID attributes as primary locators (stable and reliable)
 * - Implements explicit waits instead of Thread.sleep()
 * - Maximizes browser window to prevent responsive design issues
 * - Uses Page Object Model pattern for maintainability
 * - Includes comprehensive logging with emojis for easy identification
 * - Validates all required fields to satisfy entity constraints
 * - Handles JavaScript alerts and validations properly
 * - Tests client-side balance validation before server submission
 * - Covers insufficient balance, negative amount, and edge cases
 * 
 * Test Coverage:
 * - Navigation from dashboard to transaction list to cash withdrawal
 * - Account selection with search functionality
 * - Cash withdrawal form validation and submission
 * - Balance validation (insufficient balance, negative amounts)
 * - Transaction verification and detail view
 * - Error handling and edge cases
 * - Multi-role testing (Teller, Manager)
 * - Client-side JavaScript validation
 * - Real-time balance calculation
 * 
 * Follows lessons learned:
 * - AVOID Thread.sleep() - use WebDriverWait with ExpectedConditions
 * - USE ID attributes as primary locators for reliability
 * - HANDLE JavaScript alerts properly
 * - MAXIMIZE browser window for responsive design
 * - VALIDATE all required fields in forms
 * - ADD comprehensive logging for debugging
 * - TEST both positive and negative scenarios
 */
@Slf4j
@SqlGroup({
    @Sql(scripts = "/sql/setup/setup-withdrawal-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(scripts = "/sql/cleanup/cleanup-withdrawal-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class CashWithdrawalSeleniumTest extends BaseSeleniumTest {
    
    @Override
    protected void performInitialLogin() {
        // Use Teller role for transaction operations (primary use case)
        loginHelper.loginAsTeller();
    }
    
    @Test
    @DisplayName("Should navigate successfully through cash withdrawal workflow")
    void shouldNavigateThroughCashWithdrawalWorkflow() {
        log.info("🧪 TEST START: shouldNavigateThroughCashWithdrawalWorkflow");
        
        // Navigate to transaction list from dashboard
        DashboardPage dashboardPage = new DashboardPage(driver, baseUrl);
        assertTrue(dashboardPage.isOnDashboardPage(), "Should be on dashboard after login");
        
        TransactionListPage transactionListPage = dashboardPage.clickTransactionLink();
        assertTrue(transactionListPage.isOnTransactionListPage(), "Should navigate to transaction list");
        
        // Click cash withdrawal button
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashWithdrawalButton();
        assertTrue(accountSelectionPage.isOnAccountSelectionPage(), "Should navigate to account selection");
        assertTrue(accountSelectionPage.isWithdrawalType(), "Should be withdrawal type selection");
        
        // Verify accounts are available
        assertTrue(accountSelectionPage.hasAccounts(), "Should have accounts available for selection");
        
        // Select first account for withdrawal
        CashWithdrawalFormPage cashWithdrawalFormPage = accountSelectionPage.selectFirstAccountForWithdrawal();
        assertTrue(cashWithdrawalFormPage.isOnCashWithdrawalFormPage(), "Should navigate to cash withdrawal form");
        
        // Verify account information is displayed
        assertFalse(cashWithdrawalFormPage.getAccountNumber().isEmpty(), "Account number should be displayed");
        assertFalse(cashWithdrawalFormPage.getAccountName().isEmpty(), "Account name should be displayed");
        assertFalse(cashWithdrawalFormPage.getCurrentBalance().isEmpty(), "Current balance should be displayed");
        
        log.info("✅ TEST PASS: shouldNavigateThroughCashWithdrawalWorkflow completed successfully");
    }
    
    @Test
    @DisplayName("Should process cash withdrawal successfully with valid data")
    void shouldProcessCashWithdrawalSuccessfully() {
        log.info("🧪 TEST START: shouldProcessCashWithdrawalSuccessfully");
        
        // Navigate to cash withdrawal form
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashWithdrawalButton();
        CashWithdrawalFormPage cashWithdrawalFormPage = accountSelectionPage.selectFirstAccountForWithdrawal();
        
        // Store original balance for verification
        String originalBalance = cashWithdrawalFormPage.getCurrentBalance();
        String accountNumber = cashWithdrawalFormPage.getAccountNumber();
        double currentBalance = cashWithdrawalFormPage.getCurrentBalanceAsNumber();
        
        // Use withdrawal amount less than current balance
        String withdrawalAmount = String.valueOf((int)(currentBalance * 0.3)); // 30% of current balance
        String description = "Test penarikan tunai via Selenium";
        String referenceNumber = "REF-WD-001";
        String createdBy = "teller1";
        
        cashWithdrawalFormPage.fillCompleteForm(withdrawalAmount, description, referenceNumber, createdBy);
        
        // Verify new balance is calculated and displayed
        assertTrue(cashWithdrawalFormPage.isNewBalanceDisplayed(), "New balance should be displayed after entering amount");
        assertFalse(cashWithdrawalFormPage.hasBalanceWarning(), "Should not have balance warning for valid amount");
        assertTrue(cashWithdrawalFormPage.isWithdrawalAmountValid(), "Withdrawal amount should be valid");
        assertFalse(cashWithdrawalFormPage.isSubmitButtonDisabled(), "Submit button should be enabled for valid amount");
        
        // Submit the form
        TransactionListPage resultPage = cashWithdrawalFormPage.submitForm();
        assertNotNull(resultPage, "Form submission should be successful");
        
        // Verify success message
        assertTrue(resultPage.hasSuccessMessage(), "Should display success message after withdrawal");
        String successMessage = resultPage.getSuccessMessageText();
        assertTrue(successMessage.contains("berhasil diproses"), "Success message should indicate successful processing");
        assertTrue(successMessage.contains("TXN"), "Success message should contain transaction number");
        
        // Verify the new transaction appears in the list
        assertTrue(resultPage.hasTransactions(), "Should have transactions in the list");
        assertEquals("WITHDRAWAL", resultPage.getFirstTransactionType(), "First transaction should be a withdrawal");
        assertTrue(resultPage.getFirstTransactionAmount().contains(withdrawalAmount.replace("000", ",000")), 
                  "Transaction amount should match withdrawal amount");
        assertTrue(resultPage.getFirstTransactionAmount().startsWith("-"), "Withdrawal amount should be negative");
        
        log.info("✅ TEST PASS: shouldProcessCashWithdrawalSuccessfully completed successfully");
    }
    
    @Test
    @DisplayName("Should reject withdrawal with insufficient balance")
    void shouldRejectWithdrawalInsufficientBalance() {
        log.info("🧪 TEST START: shouldRejectWithdrawalInsufficientBalance");
        
        // Navigate to cash withdrawal form
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashWithdrawalButton();
        CashWithdrawalFormPage cashWithdrawalFormPage = accountSelectionPage.selectFirstAccountForWithdrawal();
        
        // Get current balance and try to withdraw more than available
        double currentBalance = cashWithdrawalFormPage.getCurrentBalanceAsNumber();
        String excessiveAmount = String.valueOf((int)(currentBalance + 100000)); // Exceed balance by 100k
        
        // Fill form with amount exceeding balance
        cashWithdrawalFormPage.fillCompleteForm(excessiveAmount, "Test insufficient balance", "", "teller1");
        
        // Verify client-side validation shows warning
        assertTrue(cashWithdrawalFormPage.hasBalanceWarning(), "Should display balance warning for excessive amount");
        assertTrue(cashWithdrawalFormPage.isInsufficientBalanceWarningDisplayed(), 
                  "Should show insufficient balance warning");
        assertTrue(cashWithdrawalFormPage.isSubmitButtonDisabled(), 
                  "Submit button should be disabled for insufficient balance");
        
        // Attempt to submit should be blocked by client-side validation
        cashWithdrawalFormPage.submitFormExpectingError();
        
        // Should still be on the form page
        assertTrue(cashWithdrawalFormPage.isOnCashWithdrawalFormPage(), 
                  "Should remain on withdrawal form after validation error");
        
        log.info("✅ TEST PASS: shouldRejectWithdrawalInsufficientBalance completed successfully");
    }
    
    @Test
    @DisplayName("Should validate required fields on cash withdrawal form")
    void shouldValidateRequiredFields() {
        log.info("🧪 TEST START: shouldValidateRequiredFields");
        
        // Navigate to cash withdrawal form
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashWithdrawalButton();
        CashWithdrawalFormPage cashWithdrawalFormPage = accountSelectionPage.selectFirstAccountForWithdrawal();
        
        // Verify required field attributes
        assertTrue(cashWithdrawalFormPage.isAmountFieldRequired(), "Amount field should be required");
        assertTrue(cashWithdrawalFormPage.isCreatedByFieldRequired(), "Created by field should be required");
        assertEquals("1", cashWithdrawalFormPage.getMinimumAmount(), "Minimum amount should be 1");
        
        // Submit empty form
        cashWithdrawalFormPage.submitFormExpectingError();
        
        // Verify validation errors are displayed
        assertTrue(cashWithdrawalFormPage.hasValidationErrors() || cashWithdrawalFormPage.hasErrorMessage(), 
                  "Should display validation errors for empty form");
        
        log.info("✅ TEST PASS: shouldValidateRequiredFields completed successfully");
    }
    
    @Test
    @DisplayName("Should reject withdrawal with negative amount")
    void shouldRejectNegativeAmount() {
        log.info("🧪 TEST START: shouldRejectNegativeAmount");
        
        // Navigate to cash withdrawal form
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashWithdrawalButton();
        CashWithdrawalFormPage cashWithdrawalFormPage = accountSelectionPage.selectFirstAccountForWithdrawal();
        
        // Fill form with negative amount
        cashWithdrawalFormPage.fillCompleteForm("-50000", "Test negative amount", "", "teller1");
        
        // Submit the form expecting error
        cashWithdrawalFormPage.submitFormExpectingError();
        
        // Verify error is displayed
        assertTrue(cashWithdrawalFormPage.hasErrorMessage() || cashWithdrawalFormPage.hasValidationErrors(), 
                  "Should display error for negative amount");
        
        log.info("✅ TEST PASS: shouldRejectNegativeAmount completed successfully");
    }
    
    @Test
    @DisplayName("Should reject withdrawal with zero amount")
    void shouldRejectZeroAmount() {
        log.info("🧪 TEST START: shouldRejectZeroAmount");
        
        // Navigate to cash withdrawal form
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashWithdrawalButton();
        CashWithdrawalFormPage cashWithdrawalFormPage = accountSelectionPage.selectFirstAccountForWithdrawal();
        
        // Fill form with zero amount
        cashWithdrawalFormPage.fillCompleteForm("0", "Test zero amount", "", "teller1");
        
        // Submit the form expecting error
        cashWithdrawalFormPage.submitFormExpectingError();
        
        // Verify error is displayed
        assertTrue(cashWithdrawalFormPage.hasErrorMessage() || cashWithdrawalFormPage.hasValidationErrors(), 
                  "Should display error for zero amount");
        
        log.info("✅ TEST PASS: shouldRejectZeroAmount completed successfully");
    }
    
    @Test
    @DisplayName("Should handle account search functionality")
    void shouldHandleAccountSearch() {
        log.info("🧪 TEST START: shouldHandleAccountSearch");
        
        // Navigate to account selection page
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashWithdrawalButton();
        
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
    @DisplayName("Should calculate new balance correctly in real-time")
    void shouldCalculateNewBalanceCorrectly() {
        log.info("🧪 TEST START: shouldCalculateNewBalanceCorrectly");
        
        // Navigate to cash withdrawal form
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashWithdrawalButton();
        CashWithdrawalFormPage cashWithdrawalFormPage = accountSelectionPage.selectFirstAccountForWithdrawal();
        
        double currentBalance = cashWithdrawalFormPage.getCurrentBalanceAsNumber();
        String withdrawalAmount = "75000";
        
        // Fill withdrawal amount
        cashWithdrawalFormPage.fillAmount(withdrawalAmount);
        
        // Verify new balance calculation
        assertTrue(cashWithdrawalFormPage.isNewBalanceDisplayed(), "New balance should be displayed");
        String newBalanceText = cashWithdrawalFormPage.getNewBalanceValue();
        assertFalse(newBalanceText.isEmpty(), "New balance value should not be empty");
        
        // Verify balance warning is not shown for valid amount
        assertFalse(cashWithdrawalFormPage.hasBalanceWarning(), "Should not show balance warning for valid amount");
        
        // Test with amount that exceeds balance
        String excessiveAmount = String.valueOf((int)(currentBalance + 50000));
        cashWithdrawalFormPage.fillAmount(excessiveAmount);
        
        // Should show warning for excessive amount
        assertTrue(cashWithdrawalFormPage.hasBalanceWarning(), "Should show balance warning for excessive amount");
        assertTrue(cashWithdrawalFormPage.isSubmitButtonDisabled(), "Submit button should be disabled");
        
        log.info("✅ TEST PASS: shouldCalculateNewBalanceCorrectly completed successfully");
    }
    
    @Test
    @DisplayName("Should view transaction details after successful withdrawal")
    void shouldViewTransactionDetails() {
        log.info("🧪 TEST START: shouldViewTransactionDetails");
        
        // Process a cash withdrawal first
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashWithdrawalButton();
        CashWithdrawalFormPage cashWithdrawalFormPage = accountSelectionPage.selectFirstAccountForWithdrawal();
        
        double currentBalance = cashWithdrawalFormPage.getCurrentBalanceAsNumber();
        String withdrawalAmount = String.valueOf((int)(currentBalance * 0.2)); // 20% of balance
        String description = "Test detail view transaction";
        String createdBy = "teller1";
        
        cashWithdrawalFormPage.fillCompleteForm(withdrawalAmount, description, "", createdBy);
        TransactionListPage resultPage = cashWithdrawalFormPage.submitForm();
        
        // Verify transaction was created
        assertNotNull(resultPage, "Withdrawal should be successful");
        assertTrue(resultPage.hasTransactions(), "Should have transactions");
        
        // View the transaction details
        TransactionViewPage transactionViewPage = resultPage.clickViewDetailForFirstTransaction();
        assertTrue(transactionViewPage.isOnTransactionViewPage(), "Should navigate to transaction view page");
        
        // Verify transaction details
        assertTrue(transactionViewPage.getTransactionNumber().startsWith("TXN"), 
                  "Transaction number should start with TXN");
        assertTrue(transactionViewPage.isWithdrawalTransaction(), "Should be a withdrawal transaction");
        assertTrue(transactionViewPage.isTellerChannel(), "Should use TELLER channel");
        assertTrue(transactionViewPage.getTransactionAmount().contains(withdrawalAmount.replace("000", ",000")), 
                  "Amount should match withdrawal amount");
        assertTrue(transactionViewPage.hasDescription(), "Should have description");
        assertTrue(transactionViewPage.getDescription().contains(description), 
                  "Description should match input");
        
        // Verify balance calculations (balance_after = balance_before - amount for withdrawal)
        assertTrue(transactionViewPage.isBalanceChangeCorrect(), 
                  "Balance before - amount should equal balance after");
        
        // Navigate back to transaction list
        TransactionListPage backToListPage = transactionViewPage.clickBackToTransactionList();
        assertTrue(backToListPage.isOnTransactionListPage(), "Should navigate back to transaction list");
        
        log.info("✅ TEST PASS: shouldViewTransactionDetails completed successfully");
    }
    
    @Test
    @DisplayName("Should handle form cancellation correctly")
    void shouldHandleFormCancellation() {
        log.info("🧪 TEST START: shouldHandleFormCancellation");
        
        // Navigate to cash withdrawal form
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashWithdrawalButton();
        CashWithdrawalFormPage cashWithdrawalFormPage = accountSelectionPage.selectFirstAccountForWithdrawal();
        
        // Fill some data in the form
        cashWithdrawalFormPage.fillAmount("25000");
        cashWithdrawalFormPage.fillDescription("Test cancellation");
        
        // Cancel the form
        AccountSelectionPage returnedPage = cashWithdrawalFormPage.clickCancel();
        assertTrue(returnedPage.isOnAccountSelectionPage(), "Should return to account selection page");
        
        // Verify we can still proceed with a different transaction
        CashWithdrawalFormPage newFormPage = returnedPage.selectFirstAccountForWithdrawal();
        assertTrue(newFormPage.isOnCashWithdrawalFormPage(), "Should be able to start new transaction");
        
        log.info("✅ TEST PASS: shouldHandleFormCancellation completed successfully");
    }
    
    @Test
    @DisplayName("Should filter transactions by WITHDRAWAL type in transaction list")
    void shouldFilterTransactionsByWithdrawalType() {
        log.info("🧪 TEST START: shouldFilterTransactionsByWithdrawalType");
        
        // Navigate to transaction list
        TransactionListPage transactionListPage = navigateToTransactionList();
        
        // Filter by WITHDRAWAL type
        transactionListPage.filterByTransactionType("WITHDRAWAL");
        
        // Verify filter is applied (if there are transactions)
        if (transactionListPage.hasTransactions()) {
            assertEquals("WITHDRAWAL", transactionListPage.getFirstTransactionType(), 
                        "Filtered results should only show withdrawal transactions");
        }
        
        // Reset filters
        transactionListPage.resetFilters();
        
        log.info("✅ TEST PASS: shouldFilterTransactionsByWithdrawalType completed successfully");
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/transaction/valid-cash-withdrawals.csv", numLinesToSkip = 1)
    @DisplayName("Should process various valid cash withdrawal amounts")
    void shouldProcessValidCashWithdrawals(String amountPercent, String description, String expectedResult) {
        log.info("🧪 PARAMETERIZED TEST: shouldProcessValidCashWithdrawals with amount percent: {}", amountPercent);
        
        // Navigate to cash withdrawal form
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashWithdrawalButton();
        CashWithdrawalFormPage cashWithdrawalFormPage = accountSelectionPage.selectFirstAccountForWithdrawal();
        
        // Calculate amount based on percentage of current balance
        double currentBalance = cashWithdrawalFormPage.getCurrentBalanceAsNumber();
        double percentage = Double.parseDouble(amountPercent) / 100.0;
        String amount = String.valueOf((int)(currentBalance * percentage));
        
        // Fill and submit the form
        cashWithdrawalFormPage.fillCompleteForm(amount, description, "", "teller1");
        
        if ("SUCCESS".equals(expectedResult)) {
            TransactionListPage resultPage = cashWithdrawalFormPage.submitForm();
            assertNotNull(resultPage, "Withdrawal should be successful for valid amount: " + amount);
            assertTrue(resultPage.hasSuccessMessage(), "Should show success message for valid amount: " + amount);
        } else {
            // For error cases, we should stay on the form page
            cashWithdrawalFormPage.submitFormExpectingError();
            assertTrue(cashWithdrawalFormPage.hasErrorMessage() || 
                      cashWithdrawalFormPage.hasValidationErrors() || 
                      cashWithdrawalFormPage.hasBalanceWarning(), 
                      "Should show error for invalid amount: " + amount);
        }
        
        log.info("✅ PARAMETERIZED TEST PASS: shouldProcessValidCashWithdrawals completed for amount: {}", amount);
    }
    
    @Test
    @DisplayName("Should work with Manager role permissions")
    void shouldWorkWithManagerRole() {
        log.info("🧪 TEST START: shouldWorkWithManagerRole");
        
        // Login as Manager instead of Teller
        loginHelper.loginAsManager();
        
        // Navigate and process cash withdrawal
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashWithdrawalButton();
        CashWithdrawalFormPage cashWithdrawalFormPage = accountSelectionPage.selectFirstAccountForWithdrawal();
        
        // Get current balance and calculate safe withdrawal amount
        double currentBalance = cashWithdrawalFormPage.getCurrentBalanceAsNumber();
        String withdrawalAmount = String.valueOf((int)(currentBalance * 0.4)); // 40% of balance
        
        // Process withdrawal as manager
        cashWithdrawalFormPage.fillCompleteForm(withdrawalAmount, "Manager withdrawal test", "MGR-WD-001", "admin");
        TransactionListPage resultPage = cashWithdrawalFormPage.submitForm();
        
        assertNotNull(resultPage, "Manager should be able to process cash withdrawals");
        assertTrue(resultPage.hasSuccessMessage(), "Manager withdrawal should be successful");
        
        log.info("✅ TEST PASS: shouldWorkWithManagerRole completed successfully");
    }
    
    @Test
    @DisplayName("Should handle edge case - withdrawal leaving exactly minimum balance")
    void shouldHandleMinimumBalanceEdgeCase() {
        log.info("🧪 TEST START: shouldHandleMinimumBalanceEdgeCase");
        
        // Navigate to cash withdrawal form
        TransactionListPage transactionListPage = navigateToTransactionList();
        AccountSelectionPage accountSelectionPage = transactionListPage.clickCashWithdrawalButton();
        CashWithdrawalFormPage cashWithdrawalFormPage = accountSelectionPage.selectFirstAccountForWithdrawal();
        
        // Get current balance
        double currentBalance = cashWithdrawalFormPage.getCurrentBalanceAsNumber();
        
        // Try to withdraw all but a small amount (edge case for minimum balance)
        String edgeAmount = String.valueOf((int)(currentBalance - 1000)); // Leave 1000
        
        cashWithdrawalFormPage.fillCompleteForm(edgeAmount, "Edge case minimum balance", "", "teller1");
        
        // This should be valid as it leaves some balance
        assertFalse(cashWithdrawalFormPage.hasBalanceWarning(), "Should not warn for amount leaving positive balance");
        
        TransactionListPage resultPage = cashWithdrawalFormPage.submitForm();
        
        // Should be successful if no minimum balance constraints
        if (resultPage != null) {
            assertTrue(resultPage.hasSuccessMessage(), "Edge case withdrawal should be successful");
        } else {
            // If failed, should show appropriate error
            assertTrue(cashWithdrawalFormPage.hasErrorMessage(), "Should show error message if minimum balance violated");
        }
        
        log.info("✅ TEST PASS: shouldHandleMinimumBalanceEdgeCase completed successfully");
    }
    
    // Helper methods
    
    private TransactionListPage navigateToTransactionList() {
        DashboardPage dashboardPage = new DashboardPage(driver, baseUrl);
        return dashboardPage.clickTransactionLink();
    }
    
    private void assertTransactionIsCreated(TransactionListPage transactionListPage, String expectedAmount) {
        assertTrue(transactionListPage.hasTransactions(), "Should have transactions");
        assertEquals("WITHDRAWAL", transactionListPage.getFirstTransactionType(), "Should be withdrawal transaction");
        assertTrue(transactionListPage.getFirstTransactionAmount().contains(expectedAmount.replace("000", ",000")), 
                  "Amount should match expected");
        assertTrue(transactionListPage.getFirstTransactionAmount().startsWith("-"), 
                  "Withdrawal amount should be negative");
    }
}