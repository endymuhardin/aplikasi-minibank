package id.ac.tazkia.minibank.functional.success;

import id.ac.tazkia.minibank.functional.config.BasePlaywrightTest;
import id.ac.tazkia.minibank.functional.pages.LoginPage;
import id.ac.tazkia.minibank.functional.pages.DashboardPage;

import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Tag("playwright-success")
@DisplayName("Passbook Direct Print Debug Tests")
class PassbookDirectPrintDebugTest extends BasePlaywrightTest {

    private String testAccountId;
    private String testAccountNumber;
    private List<String> consoleMessages;
    private List<String> pageErrors;

    @BeforeEach
    void setUp() {
        consoleMessages = new ArrayList<>();
        pageErrors = new ArrayList<>();

        // Set up console message listener
        page.onConsoleMessage(msg -> {
            String message = String.format("[%s] %s", msg.type(), msg.text());
            consoleMessages.add(message);
            log.info("Browser Console: {}", message);
        });

        // Set up page error listener
        page.onPageError(error -> {
            String errorMessage = error.toString();
            pageErrors.add(errorMessage);
            log.error("Browser Page Error: {}", errorMessage);
        });

        // Login as Teller
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("teller1", "minibank123");

        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");

        // Create test account with transactions
        createTestAccountWithTransactions();

        log.info("Debug test setup complete - Account: {}", testAccountNumber);
    }

    @Test
    @DisplayName("Debug: Verify JavaScript file loads on direct print page")
    void shouldLoadJavaScriptFile() {
        log.info("Test: Verify epson-plq20.js loads correctly");

        List<String> loadedScripts = new ArrayList<>();
        List<String> failedScripts = new ArrayList<>();

        // Track all responses
        page.onResponse(response -> {
            String url = response.url();
            if (url.endsWith(".js")) {
                if (response.ok()) {
                    loadedScripts.add(url);
                    log.info("✅ JavaScript loaded: {} (Status: {})", url, response.status());
                } else {
                    failedScripts.add(url + " (Status: " + response.status() + ")");
                    log.error("❌ JavaScript failed to load: {} (Status: {})", url, response.status());
                }
            }
        });

        // Navigate to direct print page
        String directPrintUrl = baseUrl + "/passbook/direct-print/" + testAccountId;
        log.info("Navigating to: {}", directPrintUrl);

        Response response = page.navigate(directPrintUrl);
        assertNotNull(response, "Navigation response should not be null");
        assertTrue(response.ok(), "Page should load successfully");

        // Wait for page to be fully loaded
        page.waitForLoadState();

        // Take screenshot of the page
        takeScreenshot("direct_print_page_loaded");

        // Check if epson-plq20.js was loaded
        boolean epsonJsLoaded = loadedScripts.stream()
            .anyMatch(url -> url.contains("epson-plq20.js"));

        log.info("Loaded scripts count: {}", loadedScripts.size());
        log.info("Failed scripts count: {}", failedScripts.size());

        if (!epsonJsLoaded) {
            log.error("❌ epson-plq20.js was NOT loaded!");
            log.error("Loaded scripts: {}", loadedScripts);
            log.error("Failed scripts: {}", failedScripts);
        }

        assertTrue(epsonJsLoaded, "epson-plq20.js should be loaded. Loaded scripts: " + loadedScripts);

        // Check for JavaScript errors in console
        List<String> errors = consoleMessages.stream()
            .filter(msg -> msg.contains("[error]"))
            .toList();

        if (!errors.isEmpty()) {
            log.error("JavaScript console errors found:");
            errors.forEach(error -> log.error("  - {}", error));
        }

        assertTrue(pageErrors.isEmpty(), "No page errors should occur. Errors: " + pageErrors);

        log.info("✅ JavaScript file loaded successfully");
    }

    @Test
    @DisplayName("Debug: Verify PassbookPrintManager is defined")
    void shouldDefinePassbookPrintManager() {
        log.info("Test: Verify PassbookPrintManager class is defined");

        // Navigate to direct print page
        page.navigate(baseUrl + "/passbook/direct-print/" + testAccountId);
        page.waitForLoadState();

        // Check if PassbookPrintManager is defined
        Object passbookManagerType = page.evaluate("typeof PassbookPrintManager");
        log.info("PassbookPrintManager type: {}", passbookManagerType);

        assertEquals("function", passbookManagerType,
            "PassbookPrintManager should be defined as a function/class");

        // Check if printManager instance is defined
        Object printManagerType = page.evaluate("typeof printManager");
        log.info("printManager instance type: {}", printManagerType);

        assertEquals("object", printManagerType,
            "printManager instance should be defined as an object");

        // Check if EpsonPLQ20 is defined
        Object epsonType = page.evaluate("typeof EpsonPLQ20");
        log.info("EpsonPLQ20 type: {}", epsonType);

        assertEquals("function", epsonType,
            "EpsonPLQ20 should be defined as a function/class");

        log.info("✅ All JavaScript classes are properly defined");
    }

    @Test
    @DisplayName("Debug: Verify Web Serial API support")
    void shouldCheckWebSerialAPISupport() {
        log.info("Test: Verify Web Serial API is available");

        // Navigate to direct print page
        page.navigate(baseUrl + "/passbook/direct-print/" + testAccountId);
        page.waitForLoadState();

        // Check if Web Serial API is supported
        Object webSerialSupported = page.evaluate("'serial' in navigator");
        log.info("Web Serial API supported: {}", webSerialSupported);

        assertTrue((Boolean) webSerialSupported,
            "Web Serial API should be supported in Chromium-based browsers");

        // Check browser compatibility warning visibility
        boolean warningVisible = page.locator("#browser-warning").isVisible();
        log.info("Browser warning visible: {}", warningVisible);

        assertFalse(warningVisible,
            "Browser compatibility warning should NOT be visible for supported browsers");

        log.info("✅ Web Serial API is supported");
    }

    @Test
    @DisplayName("Debug: Verify Connect Printer button exists and is enabled")
    void shouldVerifyConnectPrinterButton() {
        log.info("Test: Verify Connect Printer button");

        // Navigate to direct print page
        page.navigate(baseUrl + "/passbook/direct-print/" + testAccountId);
        page.waitForLoadState();

        // Wait for button to be available
        page.waitForSelector("#connect-printer-btn", new Page.WaitForSelectorOptions().setTimeout(5000));

        // Check button exists
        assertTrue(page.locator("#connect-printer-btn").isVisible(),
            "Connect Printer button should be visible");

        // Check button text
        String buttonText = page.locator("#connect-printer-btn").textContent();
        log.info("Connect button text: '{}'", buttonText);

        assertTrue(buttonText.contains("Connect"),
            "Button should show 'Connect' text");

        // Check if button is enabled (not disabled)
        boolean isDisabled = page.locator("#connect-printer-btn").isDisabled();
        log.info("Connect button disabled: {}", isDisabled);

        assertFalse(isDisabled,
            "Connect Printer button should be enabled when Web Serial API is supported");

        // Take screenshot
        takeScreenshot("connect_printer_button_visible");

        log.info("✅ Connect Printer button is properly configured");
    }

    @Test
    @DisplayName("Debug: Verify page elements and data loading")
    void shouldVerifyPageElements() {
        log.info("Test: Verify all page elements are present");

        // Navigate to direct print page
        page.navigate(baseUrl + "/passbook/direct-print/" + testAccountId);
        page.waitForLoadState();

        // Verify account details
        assertTrue(page.locator("#account-number").isVisible(),
            "Account number should be displayed");

        String displayedAccountNumber = page.locator("#account-number").textContent();
        log.info("Displayed account number: {}", displayedAccountNumber);

        // Verify passbook status section
        assertTrue(page.locator("#passbook-number").isVisible(),
            "Passbook number field should be visible");
        assertTrue(page.locator("#current-page").isVisible(),
            "Current page field should be visible");
        assertTrue(page.locator("#last-printed-line").isVisible(),
            "Last printed line field should be visible");
        assertTrue(page.locator("#remaining-lines").isVisible(),
            "Remaining lines field should be visible");

        // Verify printer status section
        assertTrue(page.locator("#printer-status-text").isVisible(),
            "Printer status text should be visible");

        String printerStatus = page.locator("#printer-status-text").textContent();
        log.info("Printer status: {}", printerStatus);

        // Verify print controls
        assertTrue(page.locator("#print-btn").isVisible(),
            "Print button should be visible");
        assertTrue(page.locator("#next-page-btn").isVisible(),
            "Next page button should be visible");

        // Verify activity log
        assertTrue(page.locator("#status-log").isVisible(),
            "Activity log should be visible");

        // Take full page screenshot
        takeScreenshot("all_page_elements");

        log.info("✅ All page elements are present and visible");
    }

    @Test
    @DisplayName("Debug: Check for JavaScript errors on button click")
    void shouldCheckButtonClickErrors() {
        log.info("Test: Check for errors when clicking Connect Printer button");

        // Navigate to direct print page
        page.navigate(baseUrl + "/passbook/direct-print/" + testAccountId);
        page.waitForLoadState();

        // Clear previous console messages
        consoleMessages.clear();
        pageErrors.clear();

        // Wait for button
        page.waitForSelector("#connect-printer-btn");

        // Take screenshot before click
        takeScreenshot("before_button_click");

        // Try to click the button
        log.info("Attempting to click Connect Printer button...");

        try {
            // We expect this to fail with "No available ports" since we don't have a real printer
            // But we want to verify that the JavaScript function is called
            page.locator("#connect-printer-btn").click();

            // Wait a moment for JavaScript to execute
            page.waitForTimeout(1000);

            // Take screenshot after click
            takeScreenshot("after_button_click");

            // Check console messages
            log.info("Console messages after click:");
            consoleMessages.forEach(msg -> log.info("  {}", msg));

            // Check if connectPrinter function was called
            boolean connectFunctionCalled = consoleMessages.stream()
                .anyMatch(msg -> msg.contains("Connecting to printer") ||
                                 msg.contains("requestPort") ||
                                 msg.contains("Failed to connect"));

            log.info("Connect function appears to have been called: {}", connectFunctionCalled);

            // We don't assert here because without a physical printer,
            // the dialog won't appear and the function will just log

        } catch (Exception e) {
            log.error("Error during button click: {}", e.getMessage());
            fail("Button click should not throw an exception: " + e.getMessage());
        }

        // Check for JavaScript errors
        if (!pageErrors.isEmpty()) {
            log.error("Page errors after button click:");
            pageErrors.forEach(error -> log.error("  {}", error));
            fail("No page errors should occur. Errors: " + pageErrors);
        }

        log.info("✅ Button click executed without JavaScript errors");
    }

    @Test
    @DisplayName("Debug: Verify fetchPrintData is called on page load")
    void shouldVerifyFetchPrintDataCalled() {
        log.info("Test: Verify fetchPrintData is called to load transactions");

        List<String> apiCalls = new ArrayList<>();

        // Track API requests
        page.onRequest(request -> {
            String url = request.url();
            if (url.contains("/api/passbook/")) {
                apiCalls.add(url);
                log.info("API Request: {} {}", request.method(), url);
            }
        });

        // Navigate to direct print page
        page.navigate(baseUrl + "/passbook/direct-print/" + testAccountId);
        page.waitForLoadState();

        // Wait for data to load
        page.waitForTimeout(2000);

        // Check if print-data API was called
        boolean printDataCalled = apiCalls.stream()
            .anyMatch(url -> url.contains("/print-data"));

        log.info("API calls made: {}", apiCalls);
        log.info("Print data API called: {}", printDataCalled);

        assertTrue(printDataCalled,
            "fetchPrintData API should be called on page load. API calls: " + apiCalls);

        // Check if transactions table has data
        page.waitForSelector("#transactions-tbody");
        String tableContent = page.locator("#transactions-tbody").textContent();

        log.info("Transactions table content: {}", tableContent);

        boolean hasTransactions = !tableContent.contains("Loading transactions");
        log.info("Transactions loaded: {}", hasTransactions);

        takeScreenshot("transactions_loaded");

        log.info("✅ Print data API is called correctly");
    }

    /**
     * Create a test account with transactions for testing
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
            testAccountNumber = "PBDEBUG" + System.currentTimeMillis();

            jdbcTemplate.update("""
                INSERT INTO accounts (id, id_customers, id_products, id_branches,
                    account_number, account_name, balance, status,
                    opened_date, created_by)
                VALUES (?::uuid, ?::uuid, ?::uuid, ?::uuid, ?, ?, ?, 'ACTIVE', CURRENT_DATE, 'TEST_SYSTEM')
                """, testAccountId, customerId, productId, branchId,
                testAccountNumber, "Debug Test Account", 0);

            // Create 3 test transactions
            int previousBalance = 0;
            for (int i = 1; i <= 3; i++) {
                String txId = UUID.randomUUID().toString();
                String txNumber = "DBTX" + System.currentTimeMillis() + i;
                int amount = 100000 * i;
                int balanceBefore = previousBalance;
                int balanceAfter = previousBalance + amount;

                jdbcTemplate.update("""
                    INSERT INTO transactions (id, id_accounts, transaction_number,
                        transaction_type, transaction_date, amount, balance_before, balance_after,
                        description, channel, created_by)
                    VALUES (?::uuid, ?::uuid, ?, 'DEPOSIT', CURRENT_TIMESTAMP, ?, ?, ?, ?, 'TELLER', 'TEST_SYSTEM')
                    """, txId, testAccountId, txNumber, amount, balanceBefore, balanceAfter,
                    "Debug Test Deposit " + i);

                // Update account balance
                jdbcTemplate.update(
                    "UPDATE accounts SET balance = ? WHERE id = ?::uuid",
                    balanceAfter, testAccountId);

                previousBalance = balanceAfter;
            }

            log.info("Created debug test account {} with 3 transactions", testAccountNumber);

        } catch (Exception e) {
            log.error("Error creating test account: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create test account", e);
        }
    }
}
