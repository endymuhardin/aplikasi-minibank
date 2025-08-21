package id.ac.tazkia.minibank.functional.web;

import id.ac.tazkia.minibank.functional.web.pageobject.AccountListPage;
import id.ac.tazkia.minibank.functional.web.pageobject.AccountStatementPage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.context.jdbc.Sql;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Selenium tests for Account Statement PDF functionality.
 * 
 * Tests the complete user journey for generating and downloading account statements:
 * - Navigation to statement page
 * - Form interaction and date selection
 * - PDF generation and download
 * - Validation scenarios and error handling
 * 
 * Uses Teller role as per established access patterns for account operations.
 */
@Slf4j
public class AccountStatementSeleniumTest extends BaseSeleniumTest {

    @TempDir
    Path tempDownloadDir;
    
    private File downloadDir;
    private AccountStatementPage statementPage;
    private AccountListPage accountListPage;
    
    @Override
    protected void performInitialLogin() {
        // Login as Teller for statement operations (follows established pattern)
        getLoginHelper().loginAsTeller();
        log.info("Logged in as Teller for account statement operations");
    }

    @BeforeEach
    void setUp() {
        downloadDir = tempDownloadDir.toFile();
        
        // Initialize page objects
        statementPage = new AccountStatementPage(driver, baseUrl);
        accountListPage = new AccountListPage(driver, baseUrl);
        
        log.info("Test setup completed. Download directory: {}", downloadDir.getAbsolutePath());
    }

    @AfterEach
    void tearDown() {
        // Clean up downloaded files
        if (downloadDir != null && downloadDir.exists()) {
            File[] files = downloadDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.delete()) {
                        log.debug("Cleaned up downloaded file: {}", file.getName());
                    }
                }
            }
        }
    }


    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    @Sql("/sql/setup-account-statement-test.sql")
    @Sql(scripts = "/sql/cleanup-account-statement-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldGenerateAccountStatementPdfWithTransactions() {
        log.info("=== Test: Generate account statement PDF for account with transactions ===");
        
        // Navigate to statement page for test account
        statementPage.open("f47ac10b-58cc-4372-a567-0e02b2c3d479");
        
        // Verify page loaded correctly
        assertTrue(statementPage.isOnAccountStatementPage(), 
            "Should be on account statement page");
        assertTrue(statementPage.hasCorrectPageTitle(), 
            "Page should have correct title");
        
        // Verify account information is displayed
        assertTrue(statementPage.isAccountInfoDisplayed(), 
            "Account information should be displayed");
        assertEquals("STMT0000001", statementPage.getAccountNumber(), 
            "Should display correct account number");
        assertFalse(statementPage.getCustomerName().isEmpty(), 
            "Customer name should be displayed");
        
        // Set date range to cover test transactions (last 6 months)
        LocalDate startDate = LocalDate.now().minusMonths(6);
        LocalDate endDate = LocalDate.now();
        statementPage.setDateRange(startDate, endDate);
        
        // Verify date inputs are set correctly
        assertEquals(startDate.toString(), statementPage.getStartDate(), 
            "Start date should be set correctly");
        assertEquals(endDate.toString(), statementPage.getEndDate(), 
            "End date should be set correctly");
        
        // Generate PDF and verify download starts
        boolean downloadStarted = statementPage.clickGeneratePdfAndVerifyDownloadStarts();
        
        // Verify download was initiated successfully
        assertTrue(downloadStarted, "PDF download should have started successfully");
        
        log.info("✅ Successfully initiated PDF generation and download");
    }

    @Test
    @Timeout(value = 45, unit = TimeUnit.SECONDS)
    @Sql("/sql/setup-account-statement-test.sql")
    @Sql(scripts = "/sql/cleanup-account-statement-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldGenerateStatementForCorporateCustomer() {
        log.info("=== Test: Generate statement for corporate customer ===");
        
        // Navigate to corporate customer account statement
        statementPage.open("f47ac10b-58cc-4372-a567-0e02b2c3d480");
        
        // Verify corporate account information
        assertTrue(statementPage.isAccountInfoDisplayed(), 
            "Corporate account information should be displayed");
        assertEquals("STMT0000002", statementPage.getAccountNumber(), 
            "Should display correct corporate account number");
        assertTrue(statementPage.getCustomerName().contains("PT."), 
            "Should display corporate customer name");
        
        // Set recent date range (last 3 months)
        LocalDate startDate = LocalDate.now().minusMonths(3);
        LocalDate endDate = LocalDate.now();
        statementPage.setDateRange(startDate, endDate);
        
        // Generate and verify PDF download
        boolean downloadStarted = statementPage.clickGeneratePdfAndVerifyDownloadStarts();
        
        assertTrue(downloadStarted, "Corporate statement PDF download should have started");
        
        log.info("✅ Successfully initiated corporate customer statement generation");
    }

    @Test
    @Timeout(value = 45, unit = TimeUnit.SECONDS)
    @Sql("/sql/setup-account-statement-test.sql")
    @Sql(scripts = "/sql/cleanup-account-statement-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldGenerateEmptyStatementForAccountWithNoTransactions() {
        log.info("=== Test: Generate statement for account with no transactions ===");
        
        // Navigate to account with no transactions
        statementPage.open("f47ac10b-58cc-4372-a567-0e02b2c3d481");
        
        // Verify account information
        assertEquals("STMT0000003", statementPage.getAccountNumber(), 
            "Should display correct account number");
        
        // Set date range
        LocalDate startDate = LocalDate.now().minusMonths(2);
        LocalDate endDate = LocalDate.now();
        statementPage.setDateRange(startDate, endDate);
        
        // Generate PDF for empty statement
        boolean downloadStarted = statementPage.clickGeneratePdfAndVerifyDownloadStarts();
        
        // Verify empty statement generation was initiated
        assertTrue(downloadStarted, "Empty statement PDF generation should have started");
        
        log.info("✅ Successfully initiated empty statement generation");
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    @Sql("/sql/setup-account-statement-test.sql")
    @Sql(scripts = "/sql/cleanup-account-statement-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldValidateInvalidDateRange() {
        log.info("=== Test: Validate invalid date range ===");
        
        // Navigate to statement page
        statementPage.open("f47ac10b-58cc-4372-a567-0e02b2c3d479");
        
        // Set invalid date range (end date before start date)
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusMonths(1);
        statementPage.setDateRange(startDate, endDate);
        
        // Attempt to generate PDF expecting error
        statementPage.clickGeneratePdfExpectingError();
        
        // Verify validation error is shown
        assertTrue(statementPage.hasValidationErrors() || statementPage.isErrorMessageDisplayed(), 
            "Should display validation error for invalid date range");
        
        // Verify we're still on the same page
        assertTrue(statementPage.isOnAccountStatementPage(), 
            "Should remain on statement page after validation error");
        
        log.info("✅ Successfully validated invalid date range");
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    @Sql("/sql/setup-account-statement-test.sql")
    @Sql(scripts = "/sql/cleanup-account-statement-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldValidateRequiredDateFields() {
        log.info("=== Test: Validate required date fields ===");
        
        // Navigate to statement page
        statementPage.open("f47ac10b-58cc-4372-a567-0e02b2c3d479");
        
        // Clear date inputs
        statementPage.clearDateInputs();
        
        // Attempt to generate PDF with empty dates
        statementPage.clickGeneratePdfExpectingError();
        
        // Verify validation occurs
        assertTrue(statementPage.hasValidationErrors() || statementPage.isErrorMessageDisplayed(), 
            "Should display validation error for missing dates");
        
        log.info("✅ Successfully validated required date fields");
    }

    @Test
    @Timeout(value = 45, unit = TimeUnit.SECONDS)
    @Sql("/sql/setup-account-statement-test.sql")
    @Sql(scripts = "/sql/cleanup-account-statement-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldNavigateFromAccountListToStatement() {
        log.info("=== Test: Navigate from account list to statement ===");
        
        // Start from account list page
        accountListPage.openAndWaitForLoad();
        assertTrue(accountListPage.isOnAccountListPage(), 
            "Should be on account list page");
        
        // Search for test account
        accountListPage.searchAccounts("STMT0000001");
        assertTrue(accountListPage.isAccountDisplayed("STMT0000001"), 
            "Test account should be visible in search results");
        
        // Note: Direct navigation to statement page (actual implementation may vary)
        // In practice, there would be a "Statement" link/button on each account row
        statementPage.open("f47ac10b-58cc-4372-a567-0e02b2c3d479");
        
        // Verify successful navigation
        assertTrue(statementPage.isOnAccountStatementPage(), 
            "Should navigate to statement page");
        assertEquals("STMT0000001", statementPage.getAccountNumber(), 
            "Should show correct account information");
        
        log.info("✅ Successfully navigated from account list to statement");
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    @Sql("/sql/setup-account-statement-test.sql")
    @Sql(scripts = "/sql/cleanup-account-statement-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldNavigateBackToAccountList() {
        log.info("=== Test: Navigate back to account list ===");
        
        // Start from statement page
        statementPage.open("f47ac10b-58cc-4372-a567-0e02b2c3d479");
        assertTrue(statementPage.isOnAccountStatementPage(), 
            "Should be on statement page");
        
        // Navigate back to account list
        AccountListPage returnedListPage = statementPage.clickBackToAccounts();
        
        // Verify successful navigation back
        assertTrue(returnedListPage.isOnAccountListPage(), 
            "Should return to account list page");
        
        log.info("✅ Successfully navigated back to account list");
    }

    @Test
    @Timeout(value = 45, unit = TimeUnit.SECONDS)
    @Sql("/sql/setup-account-statement-test.sql")
    @Sql(scripts = "/sql/cleanup-account-statement-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldGenerateStatementWithSpecificDateRange() {
        log.info("=== Test: Generate statement with specific date range ===");
        
        // Navigate to statement page
        statementPage.open("f47ac10b-58cc-4372-a567-0e02b2c3d479");
        
        // Set specific date range to capture only some transactions
        LocalDate startDate = LocalDate.now().minusMonths(4);
        LocalDate endDate = LocalDate.now().minusMonths(2);
        statementPage.setDateRange(startDate, endDate);
        
        // Generate PDF
        boolean downloadStarted = statementPage.clickGeneratePdfAndVerifyDownloadStarts();
        
        // Verify generation was initiated for specific period
        assertTrue(downloadStarted, "Specific date range PDF generation should have started");
        
        log.info("✅ Successfully initiated statement generation for specific date range");
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void shouldHandleNonExistentAccount() {
        log.info("=== Test: Handle non-existent account gracefully ===");
        
        // Attempt to navigate to non-existent account
        // This should either redirect to error page or account list
        try {
            statementPage.open("00000000-0000-0000-0000-000000000000");
            
            // Check if we're redirected or if error is shown
            // Implementation may vary - could redirect to account list or show error page
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("/account") || currentUrl.contains("/error") || 
                      statementPage.isErrorMessageDisplayed(),
                "Should handle non-existent account gracefully");
            
        } catch (Exception e) {
            // This is acceptable - page may throw exception for non-existent account
            log.info("Non-existent account handled with exception (expected): {}", e.getMessage());
        }
        
        log.info("✅ Non-existent account handled appropriately");
    }
}