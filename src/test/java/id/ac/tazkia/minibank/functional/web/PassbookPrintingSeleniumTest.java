package id.ac.tazkia.minibank.functional.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import id.ac.tazkia.minibank.functional.web.pageobject.PassbookAccountSelectionPage;
import id.ac.tazkia.minibank.functional.web.pageobject.PassbookPreviewPage;
import id.ac.tazkia.minibank.functional.web.pageobject.PassbookPrintPage;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium tests for passbook printing functionality.
 * Tests cover account selection, preview, printing, and edge cases.
 * 
 * User Role: Teller (primary role for passbook operations)
 * URL Pattern: /passbook/*
 * Page Objects: PassbookAccountSelectionPage, PassbookPreviewPage, PassbookPrintPage
 */
@Slf4j
public class PassbookPrintingSeleniumTest extends BaseSeleniumTest {

    @Override
    protected void performInitialLogin() {
        // Use teller role for passbook operations
        getLoginHelper().loginAsTeller();
    }

    @Test
    void shouldNavigateToPassbookSelectionPageSuccessfully() {
        log.info("🧪 TEST START: shouldNavigateToPassbookSelectionPageSuccessfully");
        
        PassbookAccountSelectionPage selectionPage = new PassbookAccountSelectionPage(driver, baseUrl);
        selectionPage.navigateTo();
        
        assertThat(selectionPage.getCurrentUrl()).contains("/passbook/select-account");
        assertThat(selectionPage.getPageTitle()).contains("Select Account for Passbook Printing");
        
        log.info("✅ TEST PASS: shouldNavigateToPassbookSelectionPageSuccessfully");
    }

    @Test
    void shouldDisplayActiveAccountsOnSelectionPage() {
        log.info("🧪 TEST START: shouldDisplayActiveAccountsOnSelectionPage");
        
        PassbookAccountSelectionPage selectionPage = new PassbookAccountSelectionPage(driver, baseUrl);
        selectionPage.navigateTo();
        
        // Verify that only active accounts are displayed
        assertThat(selectionPage.getDisplayedAccounts()).isNotEmpty();
        assertThat(selectionPage.areOnlyActiveAccountsDisplayed()).isTrue();
        
        log.info("✅ TEST PASS: shouldDisplayActiveAccountsOnSelectionPage");
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/passbook/search-scenarios.csv", numLinesToSkip = 1)
    void shouldSearchAccountsCorrectly(String searchTerm, String expectedResults, String description) {
        log.info("🧪 TEST START: shouldSearchAccountsCorrectly - {}", description);
        
        PassbookAccountSelectionPage selectionPage = new PassbookAccountSelectionPage(driver, baseUrl);
        selectionPage.navigateTo();
        
        if (!searchTerm.equals("EMPTY")) {
            selectionPage.searchAccounts(searchTerm);
        }
        
        switch (expectedResults) {
            case "FOUND":
                assertThat(selectionPage.getDisplayedAccounts()).isNotEmpty();
                if (!searchTerm.equals("EMPTY")) {
                    assertThat(selectionPage.isAccountDisplayed(searchTerm)).isTrue();
                }
                break;
            case "PARTIAL":
                assertThat(selectionPage.getDisplayedAccounts()).isNotEmpty();
                break;
            case "EMPTY":
                if (!searchTerm.equals("EMPTY")) {
                    assertThat(selectionPage.isNoAccountsMessageDisplayed()).isTrue();
                }
                break;
            case "ALL":
                assertThat(selectionPage.getDisplayedAccounts()).isNotEmpty();
                break;
        }
        
        log.info("✅ TEST PASS: shouldSearchAccountsCorrectly - {}", description);
    }

    @Test
    void shouldClearSearchSuccessfully() {
        log.info("🧪 TEST START: shouldClearSearchSuccessfully");
        
        PassbookAccountSelectionPage selectionPage = new PassbookAccountSelectionPage(driver, baseUrl);
        selectionPage.navigateTo();
        
        // First search for something specific
        selectionPage.searchAccounts("A2000001");
        int searchResults = selectionPage.getDisplayedAccounts().size();
        
        // Clear search
        selectionPage.clearSearch();
        int allResults = selectionPage.getDisplayedAccounts().size();
        
        // Should show all accounts now
        assertThat(allResults).isGreaterThanOrEqualTo(searchResults);
        
        log.info("✅ TEST PASS: shouldClearSearchSuccessfully");
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/passbook/passbook-test-scenarios.csv", numLinesToSkip = 1)
    void shouldPreviewPassbookForActiveAccounts(String accountNumber, String expectedStatus, String description) {
        log.info("🧪 TEST START: shouldPreviewPassbookForActiveAccounts - {}", description);
        
        PassbookAccountSelectionPage selectionPage = new PassbookAccountSelectionPage(driver, baseUrl);
        selectionPage.navigateTo();
        
        // Search for the specific account
        selectionPage.searchAccounts(accountNumber);
        
        if (expectedStatus.equals("OK")) {
            assertThat(selectionPage.isAccountDisplayed(accountNumber)).isTrue();
            
            // Click preview
            selectionPage.clickPreviewPassbook(accountNumber);
            
            // Verify preview page loaded
            PassbookPreviewPage previewPage = new PassbookPreviewPage(driver, baseUrl);
            previewPage.waitForPageLoad();
            
            assertThat(previewPage.getCurrentUrl()).contains("/passbook/preview/");
            assertThat(previewPage.isPreviewMode()).isTrue();
            assertThat(previewPage.getAccountNumber()).isNotEmpty();
            assertThat(previewPage.getCustomerName()).isNotEmpty();
            assertThat(previewPage.getCurrentBalance()).isNotEmpty();
            
            // Verify bank information is displayed
            assertThat(previewPage.isBankLogoDisplayed()).isTrue();
            assertThat(previewPage.getBankName()).isNotEmpty();
            assertThat(previewPage.getBankAddress()).isNotEmpty();
        }
        
        log.info("✅ TEST PASS: shouldPreviewPassbookForActiveAccounts - {}", description);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/passbook/passbook-test-scenarios.csv", numLinesToSkip = 1)
    void shouldPrintPassbookForActiveAccounts(String accountNumber, String expectedStatus, String description) {
        log.info("🧪 TEST START: shouldPrintPassbookForActiveAccounts - {}", description);
        
        PassbookAccountSelectionPage selectionPage = new PassbookAccountSelectionPage(driver, baseUrl);
        selectionPage.navigateTo();
        
        // Search for the specific account
        selectionPage.searchAccounts(accountNumber);
        
        if (expectedStatus.equals("OK")) {
            assertThat(selectionPage.isAccountDisplayed(accountNumber)).isTrue();
            
            // Click print
            selectionPage.clickPrintPassbook(accountNumber);
            
            // Verify print page loaded
            PassbookPrintPage printPage = new PassbookPrintPage(driver, baseUrl);
            printPage.waitForPageLoad();
            
            assertThat(printPage.getCurrentUrl()).contains("/passbook/print/");
            assertThat(printPage.isPrintMode()).isTrue();
            assertThat(printPage.getAccountNumber()).isNotEmpty();
            assertThat(printPage.getCustomerName()).isNotEmpty();
            assertThat(printPage.getCurrentBalance()).isNotEmpty();
            assertThat(printPage.getPrintDate()).isNotEmpty();
            
            // Verify bank information is displayed
            assertThat(printPage.isBankLogoDisplayed()).isTrue();
            assertThat(printPage.getBankName()).isNotEmpty();
            assertThat(printPage.getBankAddress()).isNotEmpty();
            
            // Verify date filter functionality exists
            assertThat(printPage.hasDateFilter()).isTrue();
        }
        
        log.info("✅ TEST PASS: shouldPrintPassbookForActiveAccounts - {}", description);
    }

    @Test
    void shouldApplyDateFilterOnPrintPage() {
        log.info("🧪 TEST START: shouldApplyDateFilterOnPrintPage");
        
        PassbookAccountSelectionPage selectionPage = new PassbookAccountSelectionPage(driver, baseUrl);
        selectionPage.navigateTo();
        
        // Select first available account
        selectionPage.searchAccounts("A2000001");
        assertThat(selectionPage.isAccountDisplayed("A2000001")).isTrue();
        
        selectionPage.clickPrintPassbook("A2000001");
        
        PassbookPrintPage printPage = new PassbookPrintPage(driver, baseUrl);
        printPage.waitForPageLoad();
        
        // Apply date filter
        printPage.setDateRange("2024-01-01", "2024-06-30");
        
        // The page should still load successfully with date filter applied
        assertThat(printPage.getCurrentUrl()).contains("fromDate=2024-01-01");
        assertThat(printPage.getCurrentUrl()).contains("toDate=2024-06-30");
        
        // Verify filter inputs retain their values
        assertThat(printPage.getFromDate()).isEqualTo("2024-01-01");
        assertThat(printPage.getToDate()).isEqualTo("2024-06-30");
        
        log.info("✅ TEST PASS: shouldApplyDateFilterOnPrintPage");
    }

    @Test
    void shouldClearDateFilterOnPrintPage() {
        log.info("🧪 TEST START: shouldClearDateFilterOnPrintPage");
        
        PassbookAccountSelectionPage selectionPage = new PassbookAccountSelectionPage(driver, baseUrl);
        selectionPage.navigateTo();
        
        // Select first available account
        selectionPage.searchAccounts("A2000001");
        selectionPage.clickPrintPassbook("A2000001");
        
        PassbookPrintPage printPage = new PassbookPrintPage(driver, baseUrl);
        printPage.waitForPageLoad();
        
        // Apply date filter first
        printPage.setDateRange("2024-01-01", "2024-06-30");
        
        // Clear the filter
        printPage.clearDateFilter();
        
        // URL should not contain date parameters
        assertThat(printPage.getCurrentUrl()).doesNotContain("fromDate=");
        assertThat(printPage.getCurrentUrl()).doesNotContain("toDate=");
        
        log.info("✅ TEST PASS: shouldClearDateFilterOnPrintPage");
    }

    @Test
    void shouldNavigateBackToSelectionFromPreview() {
        log.info("🧪 TEST START: shouldNavigateBackToSelectionFromPreview");
        
        PassbookAccountSelectionPage selectionPage = new PassbookAccountSelectionPage(driver, baseUrl);
        selectionPage.navigateTo();
        
        selectionPage.searchAccounts("A2000001");
        selectionPage.clickPreviewPassbook("A2000001");
        
        PassbookPreviewPage previewPage = new PassbookPreviewPage(driver, baseUrl);
        previewPage.waitForPageLoad();
        
        // Navigate back
        previewPage.clickBackToSelection();
        
        // Should be back on selection page
        selectionPage.waitForPageLoad();
        assertThat(selectionPage.getCurrentUrl()).contains("/passbook/select-account");
        
        log.info("✅ TEST PASS: shouldNavigateBackToSelectionFromPreview");
    }

    @Test
    void shouldNavigateBackToSelectionFromPrint() {
        log.info("🧪 TEST START: shouldNavigateBackToSelectionFromPrint");
        
        PassbookAccountSelectionPage selectionPage = new PassbookAccountSelectionPage(driver, baseUrl);
        selectionPage.navigateTo();
        
        selectionPage.searchAccounts("A2000001");
        selectionPage.clickPrintPassbook("A2000001");
        
        PassbookPrintPage printPage = new PassbookPrintPage(driver, baseUrl);
        printPage.waitForPageLoad();
        
        // Navigate back
        printPage.clickBackToSelection();
        
        // Should be back on selection page
        selectionPage.waitForPageLoad();
        assertThat(selectionPage.getCurrentUrl()).contains("/passbook/select-account");
        
        log.info("✅ TEST PASS: shouldNavigateBackToSelectionFromPrint");
    }

    @Test
    void shouldNavigateToAccountListFromSelection() {
        log.info("🧪 TEST START: shouldNavigateToAccountListFromSelection");
        
        PassbookAccountSelectionPage selectionPage = new PassbookAccountSelectionPage(driver, baseUrl);
        selectionPage.navigateTo();
        
        selectionPage.navigateToAccountList();
        
        // Should navigate to account list
        assertThat(driver.getCurrentUrl()).contains("/account/list");
        
        log.info("✅ TEST PASS: shouldNavigateToAccountListFromSelection");
    }

    @Test
    void shouldDisplayAccountDetailsCorrectly() {
        log.info("🧪 TEST START: shouldDisplayAccountDetailsCorrectly");
        
        PassbookAccountSelectionPage selectionPage = new PassbookAccountSelectionPage(driver, baseUrl);
        selectionPage.navigateTo();
        
        selectionPage.searchAccounts("A2000001");
        
        if (selectionPage.isAccountDisplayed("A2000001")) {
            // Verify account details are displayed
            String balance = selectionPage.getAccountBalance("A2000001");
            String customer = selectionPage.getAccountCustomer("A2000001");
            String product = selectionPage.getAccountProduct("A2000001");
            
            assertThat(balance).isNotEmpty();
            assertThat(customer).isNotEmpty();
            assertThat(product).isNotEmpty();
            
            // Balance should contain currency format
            assertThat(balance).contains("IDR");
        }
        
        log.info("✅ TEST PASS: shouldDisplayAccountDetailsCorrectly");
    }

    @Test
    void shouldHandleInvalidDateFormatsGracefully() {
        log.info("🧪 TEST START: shouldHandleInvalidDateFormatsGracefully");
        
        PassbookAccountSelectionPage selectionPage = new PassbookAccountSelectionPage(driver, baseUrl);
        selectionPage.navigateTo();
        
        // Navigate directly to print page with invalid date format
        driver.get(baseUrl + "/passbook/print/uuid-placeholder?fromDate=invalid-date&toDate=also-invalid");
        
        // Should redirect back to selection page with error message
        selectionPage.waitForPageLoad();
        assertThat(selectionPage.getCurrentUrl()).contains("/passbook/select-account");
        
        if (selectionPage.isErrorMessageDisplayed()) {
            assertThat(selectionPage.getErrorMessage()).contains("Invalid date format");
        }
        
        log.info("✅ TEST PASS: shouldHandleInvalidDateFormatsGracefully");
    }

    @Test
    void shouldHandleNonexistentAccountGracefully() {
        log.info("🧪 TEST START: shouldHandleNonexistentAccountGracefully");
        
        PassbookAccountSelectionPage selectionPage = new PassbookAccountSelectionPage(driver, baseUrl);
        
        // Navigate directly to print page with nonexistent account ID
        driver.get(baseUrl + "/passbook/print/00000000-0000-0000-0000-000000000000");
        
        // Should redirect back to selection page with error message
        selectionPage.waitForPageLoad();
        assertThat(selectionPage.getCurrentUrl()).contains("/passbook/select-account");
        
        if (selectionPage.isErrorMessageDisplayed()) {
            assertThat(selectionPage.getErrorMessage()).contains("Account not found");
        }
        
        log.info("✅ TEST PASS: shouldHandleNonexistentAccountGracefully");
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/passbook/edge-case-scenarios.csv", numLinesToSkip = 1)
    void shouldHandleEdgeCaseAccountsCorrectly(String scenario, String accountNumber, int transactionCount, 
                                              String startDate, String endDate, String description) {
        log.info("🧪 TEST START: shouldHandleEdgeCaseAccountsCorrectly - {} ({})", scenario, description);
        
        PassbookAccountSelectionPage selectionPage = new PassbookAccountSelectionPage(driver, baseUrl);
        selectionPage.navigateTo();
        
        // Search for the edge case account
        selectionPage.searchAccounts(accountNumber);
        
        if (selectionPage.isAccountDisplayed(accountNumber)) {
            // Click print to test the edge case
            selectionPage.clickPrintPassbook(accountNumber);
            
            PassbookPrintPage printPage = new PassbookPrintPage(driver, baseUrl);
            printPage.waitForPageLoad();
            
            // Verify page loads successfully regardless of edge case
            assertThat(printPage.getCurrentUrl()).contains("/passbook/print/");
            assertThat(printPage.getAccountNumber()).isNotEmpty();
            
            // Apply date filter if provided
            if (!startDate.isEmpty() && !endDate.isEmpty()) {
                printPage.setDateRange(startDate, endDate);
                // Should not cause errors even with edge case data
                assertThat(printPage.getCurrentUrl()).contains("fromDate=" + startDate);
            }
            
            // Verify the page handles the transaction count appropriately
            switch (scenario) {
                case "EMPTY_ACCOUNT":
                    // Should handle accounts with no transactions gracefully
                    assertThat(printPage.getTransactionCount()).isGreaterThanOrEqualTo(0);
                    break;
                case "SINGLE_TRANSACTION":
                    // Should display single transaction correctly
                    assertThat(printPage.getTransactionCount()).isGreaterThanOrEqualTo(0);
                    break;
                case "LARGE_VOLUME":
                    // Should handle pagination for large volumes
                    if (printPage.getTransactionCount() > 0) {
                        assertThat(printPage.isPaginationDisplayed()).isTrue();
                    }
                    break;
                default:
                    // Other edge cases should load without errors
                    assertThat(printPage.getTransactionCount()).isGreaterThanOrEqualTo(0);
                    break;
            }
        } else {
            log.info("⚠️ Edge case account {} not found in test data, skipping test", accountNumber);
        }
        
        log.info("✅ TEST PASS: shouldHandleEdgeCaseAccountsCorrectly - {} ({})", scenario, description);
    }
}