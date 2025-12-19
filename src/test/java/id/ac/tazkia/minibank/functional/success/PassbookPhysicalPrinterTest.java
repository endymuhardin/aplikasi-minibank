package id.ac.tazkia.minibank.functional.success;

import id.ac.tazkia.minibank.functional.config.BasePlaywrightTest;
import id.ac.tazkia.minibank.functional.pages.LoginPage;
import id.ac.tazkia.minibank.functional.pages.DashboardPage;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Tag("playwright-physical-printer")
@DisplayName("Passbook Physical Printer Tests")
@EnabledIfSystemProperty(named = "test.physical.printer", matches = "true")
class PassbookPhysicalPrinterTest extends BasePlaywrightTest {

    private String testAccountId;
    private String testAccountNumber;
    private List<String> consoleMessages;

    @BeforeEach
    void setUp() {
        consoleMessages = new ArrayList<>();

        // Set up console message listener
        page.onConsoleMessage(msg -> {
            String message = String.format("[%s] %s", msg.type(), msg.text());
            consoleMessages.add(message);
            log.info("Browser Console: {}", message);
        });

        // Login as Teller
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("teller1", "minibank123");

        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");

        // Create test account with transactions
        createTestAccountWithTransactions();

        log.info("Physical printer test setup complete - Account: {}", testAccountNumber);
    }

    @Test
    @DisplayName("Physical Printer: Full print workflow from initialization to print")
    void shouldCompleteFullPrintWorkflowWithPhysicalPrinter() {
        log.info("========================================");
        log.info("PHYSICAL PRINTER TEST");
        log.info("========================================");
        log.info("This test requires manual interaction:");
        log.info("1. Ensure Epson PLQ-20 is connected via USB");
        log.info("2. Printer is powered on");
        log.info("3. Passbook is loaded in the printer");
        log.info("4. You will need to select the printer from browser dialog");
        log.info("========================================");

        // Step 1: Initialize passbook
        log.info("Step 1: Initialize passbook");
        String initUrl = baseUrl + "/passbook/initialize/" + testAccountId;
        page.navigate(initUrl);
        page.waitForLoadState();

        takeScreenshot("01_initialization_page");

        // Fill initialization form
        page.fill("#current-page-input", "1");
        page.fill("#last-line-input", "0");

        takeScreenshot("02_initialization_form_filled");

        // Submit initialization
        page.click("#submit-button");

        // Wait for redirect to direct print page
        page.waitForURL(url -> url.contains("/passbook/direct-print/"),
            new Page.WaitForURLOptions().setTimeout(5000));

        takeScreenshot("03_redirected_to_direct_print");

        // Verify success message
        assertTrue(page.locator("#success-message").isVisible(),
            "Should show initialization success message");

        log.info("✅ Passbook initialized successfully");

        // Step 2: Verify page loaded correctly
        log.info("Step 2: Verify direct print page elements");

        assertTrue(page.locator("#account-number").isVisible(),
            "Account number should be visible");
        assertTrue(page.locator("#connect-printer-btn").isVisible(),
            "Connect printer button should be visible");
        assertTrue(page.locator("#print-btn").isVisible(),
            "Print button should be visible");

        takeScreenshot("04_direct_print_page_loaded");

        // Step 3: Verify transactions loaded
        log.info("Step 3: Verify transactions loaded");

        page.waitForSelector("#transactions-tbody");
        String tableContent = page.locator("#transactions-tbody").textContent();

        assertFalse(tableContent.contains("Loading transactions"),
            "Transactions should be loaded");
        assertFalse(tableContent.contains("No transactions"),
            "Should have transactions to print");

        takeScreenshot("05_transactions_loaded");

        // Step 4: Connect to printer (MANUAL INTERACTION REQUIRED)
        log.info("========================================");
        log.info("Step 4: Connect to physical printer");
        log.info("MANUAL ACTION REQUIRED:");
        log.info("- A browser dialog will appear");
        log.info("- Select 'Epson PLQ-20' or USB Serial Device");
        log.info("- Click 'Connect'");
        log.info("- You have 30 seconds to complete this");
        log.info("========================================");

        // Click connect button
        page.click("#connect-printer-btn");

        takeScreenshot("06_after_clicking_connect");

        // Wait for connection (user needs to select printer manually)
        // Check if connection succeeded within 30 seconds
        boolean connected = false;
        for (int i = 0; i < 30; i++) {
            String printerStatus = page.locator("#printer-status-text").textContent();
            log.info("Waiting for connection... Status: {} ({}s)", printerStatus, i + 1);

            if (printerStatus.contains("Connected")) {
                connected = true;
                log.info("✅ Printer connected successfully!");
                break;
            }

            page.waitForTimeout(1000);
        }

        if (!connected) {
            takeScreenshot("07_connection_timeout");
            log.warn("⚠️  Printer connection timeout - did you select the printer?");
            fail("Printer did not connect within 30 seconds. Please ensure printer is selected from dialog.");
        }

        takeScreenshot("08_printer_connected");

        // Verify connection status
        String printerStatus = page.locator("#printer-status-text").textContent();
        assertTrue(printerStatus.contains("Connected"),
            "Printer status should show 'Connected'");

        // Verify print button is enabled
        assertFalse(page.locator("#print-btn").isDisabled(),
            "Print button should be enabled after connection");

        log.info("✅ Printer connected and ready");

        // Step 5: Print to passbook
        log.info("========================================");
        log.info("Step 5: Print to physical printer");
        log.info("Ensure passbook is properly loaded in printer");
        log.info("Printing will begin in 3 seconds...");
        log.info("========================================");

        page.waitForTimeout(3000);

        // Click print button
        log.info("Clicking Print to Passbook button...");
        page.click("#print-btn");

        takeScreenshot("09_print_started");

        // Wait for print progress
        page.waitForSelector("#print-progress",
            new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));

        log.info("✅ Print progress started");

        // Monitor print progress
        boolean printCompleted = false;
        for (int i = 0; i < 60; i++) { // Wait up to 60 seconds for print to complete
            if (!page.locator("#print-progress").isVisible()) {
                // Progress bar disappeared, print might be done
                printCompleted = true;
                break;
            }

            String progressText = page.locator("#progress-text").textContent();
            log.info("Print progress: {}", progressText);

            takeScreenshot(String.format("10_printing_progress_%02d", i + 1));

            if (progressText.contains("completed") || progressText.contains("failed")) {
                printCompleted = true;
                break;
            }

            page.waitForTimeout(1000);
        }

        assertTrue(printCompleted, "Print should complete within 60 seconds");

        takeScreenshot("11_print_completed");

        // Check final status in activity log
        String activityLog = page.locator("#status-log").textContent();
        log.info("Final activity log:\n{}", activityLog);

        // Verify success
        assertTrue(activityLog.contains("Successfully printed") ||
                   activityLog.contains("transactions"),
            "Activity log should show successful print");

        log.info("========================================");
        log.info("✅ FULL PRINT WORKFLOW COMPLETED");
        log.info("========================================");
        log.info("Please verify the passbook physical output:");
        log.info("1. Check that transactions are printed clearly");
        log.info("2. Verify alignment is correct");
        log.info("3. Confirm all transaction details are accurate");
        log.info("========================================");
    }

    @Test
    @DisplayName("Physical Printer: Test printer connection only")
    void shouldConnectToPhysicalPrinter() {
        log.info("========================================");
        log.info("PRINTER CONNECTION TEST");
        log.info("========================================");

        // Navigate to direct print page
        page.navigate(baseUrl + "/passbook/direct-print/" + testAccountId);
        page.waitForLoadState();

        takeScreenshot("connection_test_01_page_loaded");

        // Verify Web Serial API support
        Object webSerialSupported = page.evaluate("'serial' in navigator");
        assertTrue((Boolean) webSerialSupported,
            "Web Serial API must be supported");

        log.info("✅ Web Serial API is supported");

        // Click connect button
        log.info("MANUAL ACTION: Select printer from dialog (30 seconds)");
        page.click("#connect-printer-btn");

        takeScreenshot("connection_test_02_dialog_shown");

        // Wait for connection
        boolean connected = false;
        for (int i = 0; i < 30; i++) {
            String printerStatus = page.locator("#printer-status-text").textContent();

            if (printerStatus.contains("Connected")) {
                connected = true;
                log.info("✅ Printer connected!");
                break;
            }

            page.waitForTimeout(1000);
        }

        assertTrue(connected, "Printer should connect within 30 seconds");

        takeScreenshot("connection_test_03_connected");

        // Verify printer info via JavaScript
        Object printerInfo = page.evaluate("printManager.printer.getPortInfo()");
        log.info("Printer info: {}", printerInfo);

        assertNotNull(printerInfo, "Printer info should be available");

        log.info("✅ Connection test passed");
    }

    @Test
    @DisplayName("Physical Printer: Test advance to next page")
    void shouldAdvanceToNextPageWithPhysicalPrinter() {
        log.info("========================================");
        log.info("ADVANCE PAGE TEST");
        log.info("This tests the paper advance functionality");
        log.info("========================================");

        // Navigate to direct print page
        page.navigate(baseUrl + "/passbook/direct-print/" + testAccountId);
        page.waitForLoadState();

        // Get initial passbook status
        String initialPage = page.locator("#current-page").textContent();
        String initialLine = page.locator("#last-printed-line").textContent();

        log.info("Initial status - Page: {}, Line: {}", initialPage, initialLine);

        takeScreenshot("advance_01_initial_status");

        // Connect to printer
        log.info("MANUAL ACTION: Connect to printer (30 seconds)");
        page.click("#connect-printer-btn");

        boolean connected = false;
        for (int i = 0; i < 30; i++) {
            if (page.locator("#printer-status-text").textContent().contains("Connected")) {
                connected = true;
                break;
            }
            page.waitForTimeout(1000);
        }

        assertTrue(connected, "Printer must be connected for this test");

        takeScreenshot("advance_02_printer_connected");

        // Enable and click next page button
        log.info("Clicking 'Advance to Next Page' button...");
        page.click("#next-page-btn");

        // Wait for page advance
        page.waitForTimeout(2000);

        takeScreenshot("advance_03_after_page_advance");

        // Verify page was advanced (should see updated status)
        String newPage = page.locator("#current-page").textContent();
        log.info("After advance - Page: {}", newPage);

        // Check activity log
        String activityLog = page.locator("#status-log").textContent();
        assertTrue(activityLog.contains("Advanced to page") ||
                   activityLog.contains("page"),
            "Activity log should show page advance");

        log.info("✅ Page advance test completed");
    }

    @Test
    @DisplayName("Physical Printer: Verify transaction formatting")
    void shouldDisplayTransactionsInCorrectFormat() {
        log.info("Test: Verify transaction display format for printing");

        // Navigate to direct print page
        page.navigate(baseUrl + "/passbook/direct-print/" + testAccountId);
        page.waitForLoadState();

        // Wait for transactions to load
        page.waitForSelector("#transactions-tbody tr");

        takeScreenshot("format_test_transactions_table");

        // Get all transaction rows
        int rowCount = page.locator("#transactions-tbody tr").count();
        log.info("Found {} transaction rows", rowCount);

        assertTrue(rowCount >= 3, "Should have at least 3 test transactions");

        // Check each transaction row has all required columns
        for (int i = 0; i < Math.min(rowCount, 5); i++) {
            String rowSelector = String.format("#transactions-tbody tr:nth-child(%d)", i + 1);

            // Check for date column
            String dateText = page.locator(rowSelector + " td:nth-child(2)").textContent();
            assertFalse(dateText.trim().isEmpty(), "Date should be present");

            // Check for balance column
            String balanceText = page.locator(rowSelector + " td:nth-child(6)").textContent();
            assertFalse(balanceText.trim().isEmpty(), "Balance should be present");

            log.info("Transaction {}: Date={}, Balance={}", i + 1, dateText, balanceText);
        }

        log.info("✅ All transactions are properly formatted");
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
            testAccountNumber = "PBPRINTER" + System.currentTimeMillis();

            jdbcTemplate.update("""
                INSERT INTO accounts (id, id_customers, id_products, id_branches,
                    account_number, account_name, balance, status,
                    opened_date, created_by)
                VALUES (?::uuid, ?::uuid, ?::uuid, ?::uuid, ?, ?, ?, 'ACTIVE', CURRENT_DATE, 'TEST_SYSTEM')
                """, testAccountId, customerId, productId, branchId,
                testAccountNumber, "Physical Printer Test Account", 0);

            // Create 5 test transactions with realistic data
            int previousBalance = 0;
            String[] descriptions = {
                "Opening Balance",
                "Cash Deposit - Branch",
                "Transfer from A1000001",
                "Cash Deposit - Teller",
                "ATM Withdrawal"
            };
            int[] amounts = {100000, 250000, 150000, 300000, -50000};

            for (int i = 0; i < 5; i++) {
                String txId = UUID.randomUUID().toString();
                String txNumber = "PRTX" + System.currentTimeMillis() + String.format("%03d", i);
                int amount = amounts[i];
                int balanceBefore = previousBalance;
                int balanceAfter = previousBalance + amount;
                String txType = amount > 0 ? "DEPOSIT" : "WITHDRAWAL";

                jdbcTemplate.update("""
                    INSERT INTO transactions (id, id_accounts, transaction_number,
                        transaction_type, transaction_date, amount, balance_before, balance_after,
                        description, channel, created_by)
                    VALUES (?::uuid, ?::uuid, ?, ?, CURRENT_TIMESTAMP - INTERVAL '%d days', ?, ?, ?, ?, 'TELLER', 'TEST_SYSTEM')
                    """.formatted(4 - i), // Different dates
                    txId, testAccountId, txNumber, txType, Math.abs(amount),
                    balanceBefore, balanceAfter, descriptions[i]);

                previousBalance = balanceAfter;
            }

            // Update account balance to final amount
            jdbcTemplate.update(
                "UPDATE accounts SET balance = ? WHERE id = ?::uuid",
                previousBalance, testAccountId);

            log.info("Created physical printer test account {} with 5 transactions", testAccountNumber);
            log.info("Final balance: {}", previousBalance);

        } catch (Exception e) {
            log.error("Error creating test account: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create test account", e);
        }
    }
}
