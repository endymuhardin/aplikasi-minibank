package id.ac.tazkia.minibank.functional.success;

import id.ac.tazkia.minibank.functional.config.BasePlaywrightTest;
import id.ac.tazkia.minibank.functional.pages.PassbookInitializationPage;
import id.ac.tazkia.minibank.functional.pages.LoginPage;
import id.ac.tazkia.minibank.functional.pages.DashboardPage;

import com.microsoft.playwright.Page;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Tag("playwright-success")
@DisplayName("Passbook Initialization Success Scenario Tests")
class PassbookInitializationSuccessTest extends BasePlaywrightTest {

    private PassbookInitializationPage passbookPage;
    private String testAccountId;
    private String testAccountNumber;

    @BeforeEach
    void setUp() {
        // Login as Teller who has permission to initialize passbooks
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("teller1", "minibank123");

        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");

        // Initialize passbook page
        passbookPage = new PassbookInitializationPage(page);

        // Create test account with transactions for passbook initialization
        createTestAccountWithTransactions();

        log.info("Passbook initialization test setup complete - Account: {}", testAccountNumber);
    }

    @Test
    @DisplayName("Should successfully initialize passbook with page and line number")
    void shouldInitializePassbookWithPageAndLine() {
        log.info("Test: Initialize passbook with page=1, line=5");

        // Navigate to passbook initialization page
        passbookPage.navigateTo(baseUrl, testAccountId);

        // Verify page loaded
        assertTrue(passbookPage.isPageLoaded(), "Passbook initialization page should be loaded");

        // Verify account information is displayed
        assertEquals(testAccountNumber, passbookPage.getAccountNumber(),
            "Account number should be displayed");
        assertFalse(passbookPage.getAccountName().isEmpty(),
            "Account name should be displayed");
        assertFalse(passbookPage.getCustomerName().isEmpty(),
            "Customer name should be displayed");

        // Verify transaction list is available
        int transactionCount = passbookPage.getTransactionCount();
        log.info("Found {} transactions on the page", transactionCount);

        // If no transactions found via table, check via available dropdown options
        int availableTransactions = passbookPage.getAvailableTransactionCount();
        log.info("Found {} transactions in dropdown", availableTransactions);

        assertTrue(transactionCount >= 3 || availableTransactions >= 3,
            "Should have at least 3 transactions (found " + transactionCount + " in table, " + availableTransactions + " in dropdown)");

        // Fill initialization form
        passbookPage.fillInitializationForm(1, 5);

        // Submit form
        passbookPage.submitForm();

        // Wait for redirect
        page.waitForURL(url -> url.contains("/passbook/direct-print/"), new Page.WaitForURLOptions().setTimeout(5000));

        // Verify redirected to direct print page
        assertTrue(passbookPage.isOnDirectPrintPage(),
            "Should redirect to direct print page after successful initialization");

        // Verify success message is visible on direct print page
        assertTrue(page.locator("#success-message").isVisible(),
            "Should show success message");

        String successText = page.locator("#success-message").textContent();
        assertTrue(successText.contains("initialized successfully"),
            "Success message should confirm initialization");
        assertTrue(successText.contains("Current Page: 1"),
            "Success message should show current page");
        assertTrue(successText.contains("Last Line: 5"),
            "Success message should show last line");

        log.info("✅ Passbook initialized successfully with page=1, line=5");
    }

    @Test
    @DisplayName("Should successfully initialize passbook with last transaction selected")
    void shouldInitializePassbookWithLastTransaction() {
        log.info("Test: Initialize passbook with last transaction");

        // Navigate to passbook initialization page
        passbookPage.navigateTo(baseUrl, testAccountId);

        // Verify page loaded
        assertTrue(passbookPage.isPageLoaded(), "Passbook initialization page should be loaded");

        // Get available transactions
        int availableTransactions = passbookPage.getAvailableTransactionCount();
        assertTrue(availableTransactions >= 3,
            "Should have at least 3 transactions available");

        // Fill form with page, line, and select second transaction
        passbookPage.fillInitializationForm(1, 2);
        passbookPage.selectTransactionByIndex(2); // Select 2nd transaction (index 0 is "None")

        // Submit form
        passbookPage.submitForm();

        // Wait for redirect
        page.waitForURL(url -> url.contains("/passbook/direct-print/"), new Page.WaitForURLOptions().setTimeout(5000));

        // Verify success
        assertTrue(passbookPage.isOnDirectPrintPage(),
            "Should redirect to direct print page");
        assertTrue(page.locator("#success-message").isVisible(),
            "Should show success message");

        log.info("✅ Passbook initialized successfully with transaction selection");
    }

    @Test
    @DisplayName("Should initialize passbook starting from page 2")
    void shouldInitializePassbookFromPage2() {
        log.info("Test: Initialize passbook from page 2");

        // Navigate to passbook initialization page
        passbookPage.navigateTo(baseUrl, testAccountId);

        // Verify page loaded
        assertTrue(passbookPage.isPageLoaded(), "Passbook initialization page should be loaded");

        // Fill form for page 2, line 10
        passbookPage.fillInitializationForm(2, 10);

        // Submit form
        passbookPage.submitForm();

        // Wait for redirect
        page.waitForURL(url -> url.contains("/passbook/direct-print/"), new Page.WaitForURLOptions().setTimeout(5000));

        // Verify success
        assertTrue(passbookPage.isOnDirectPrintPage(),
            "Should redirect to direct print page");

        String successText = page.locator("#success-message").textContent();
        assertTrue(successText.contains("Current Page: 2"),
            "Success message should show page 2");
        assertTrue(successText.contains("Last Line: 10"),
            "Success message should show line 10");

        log.info("✅ Passbook initialized successfully from page 2, line 10");
    }

    @Test
    @DisplayName("Should show validation error for invalid page number")
    void shouldShowErrorForInvalidPageNumber() {
        log.info("Test: Validation error for invalid page number");

        // Navigate to passbook initialization page
        passbookPage.navigateTo(baseUrl, testAccountId);

        // Verify page loaded
        assertTrue(passbookPage.isPageLoaded(), "Passbook initialization page should be loaded");

        // Fill form with invalid page number (0)
        passbookPage.fillInitializationForm(0, 5);

        // Try to submit form - should be prevented by HTML5 validation (min=1)
        // The form won't actually submit due to browser validation
        passbookPage.submitForm();

        // Should still be on initialization page
        assertFalse(passbookPage.isOnDirectPrintPage(),
            "Should not redirect when validation fails");

        log.info("✅ Validation prevents invalid page number submission");
    }

    @Test
    @DisplayName("Should show validation error for line number out of range")
    void shouldShowErrorForInvalidLineNumber() {
        log.info("Test: Validation error for invalid line number");

        // Navigate to passbook initialization page
        passbookPage.navigateTo(baseUrl, testAccountId);

        // Verify page loaded
        assertTrue(passbookPage.isPageLoaded(), "Passbook initialization page should be loaded");

        // Fill form with invalid line number (31, max is 30)
        passbookPage.fillInitializationForm(1, 31);

        // Try to submit form - should be prevented by HTML5 validation (max=30)
        passbookPage.submitForm();

        // Should still be on initialization page
        assertFalse(passbookPage.isOnDirectPrintPage(),
            "Should not redirect when validation fails");

        log.info("✅ Validation prevents invalid line number submission");
    }

    /**
     * Create a test account with transactions for passbook testing
     */
    private void createTestAccountWithTransactions() {
        try {
            // Get test customer
            String customerId = jdbcTemplate.queryForObject(
                "SELECT id FROM customers WHERE customer_type = 'PERSONAL' ORDER BY created_date DESC LIMIT 1",
                String.class);

            if (customerId == null) {
                throw new RuntimeException("No personal customer found for testing");
            }

            // Get Tabungan Wadiah product
            String productId = jdbcTemplate.queryForObject(
                "SELECT id FROM products WHERE product_type = 'TABUNGAN_WADIAH' LIMIT 1",
                String.class);

            // Get branch
            String branchId = jdbcTemplate.queryForObject(
                "SELECT id FROM branches LIMIT 1",
                String.class);

            // Create account
            UUID accountUuid = UUID.randomUUID();
            testAccountId = accountUuid.toString();
            testAccountNumber = "PBTEST" + System.currentTimeMillis();

            jdbcTemplate.update("""
                INSERT INTO accounts (id, id_customers, id_products, id_branches,
                    account_number, account_name, balance, status,
                    opened_date, created_by)
                VALUES (?::uuid, ?::uuid, ?::uuid, ?::uuid, ?, ?, ?, 'ACTIVE', CURRENT_DATE, 'TEST_SYSTEM')
                """, testAccountId, customerId, productId, branchId,
                testAccountNumber, "Passbook Test Account", 0);

            // Create 3 test transactions
            int previousBalance = 0;
            for (int i = 1; i <= 3; i++) {
                String txId = UUID.randomUUID().toString();
                String txNumber = "PBTX" + System.currentTimeMillis() + i;
                int amount = 100000 * i;
                int balanceBefore = previousBalance;
                int balanceAfter = previousBalance + amount;

                jdbcTemplate.update("""
                    INSERT INTO transactions (id, id_accounts, transaction_number,
                        transaction_type, transaction_date, amount, balance_before, balance_after,
                        description, channel, created_by)
                    VALUES (?::uuid, ?::uuid, ?, 'DEPOSIT', CURRENT_TIMESTAMP, ?, ?, ?, ?, 'TELLER', 'TEST_SYSTEM')
                    """, txId, testAccountId, txNumber, amount, balanceBefore, balanceAfter,
                    "Test Deposit " + i);

                // Update account balance
                jdbcTemplate.update(
                    "UPDATE accounts SET balance = ? WHERE id = ?::uuid",
                    balanceAfter, testAccountId);

                previousBalance = balanceAfter;
            }

            log.info("Created test account {} with 3 transactions", testAccountNumber);

        } catch (Exception e) {
            log.error("Error creating test account: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create test account", e);
        }
    }
}
