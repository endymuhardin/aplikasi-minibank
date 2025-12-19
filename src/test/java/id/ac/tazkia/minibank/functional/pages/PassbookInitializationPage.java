package id.ac.tazkia.minibank.functional.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PassbookInitializationPage {

    private final Page page;

    // Page elements
    private final Locator pageTitle;
    private final Locator backButton;

    // Account information elements
    private final Locator accountNumber;
    private final Locator accountName;
    private final Locator customerName;
    private final Locator accountBalance;

    // Current passbook status elements
    private final Locator currentPassbookNumber;
    private final Locator currentPage;
    private final Locator currentLastLine;

    // Form elements
    private final Locator currentPageInput;
    private final Locator lastLineInput;
    private final Locator lastTransactionSelect;
    private final Locator submitButton;
    private final Locator cancelButton;

    // Message elements
    private final Locator successMessage;
    private final Locator errorMessage;

    // Transaction table
    private final Locator transactionsTable;

    public PassbookInitializationPage(Page page) {
        this.page = page;

        // Initialize page elements
        this.pageTitle = page.locator("#page-title");
        this.backButton = page.locator("#back-button");

        // Account information
        this.accountNumber = page.locator("#account-number");
        this.accountName = page.locator("#account-name");
        this.customerName = page.locator("#customer-name");
        this.accountBalance = page.locator("#account-balance");

        // Current passbook status
        this.currentPassbookNumber = page.locator("#current-passbook-number");
        this.currentPage = page.locator("#current-page");
        this.currentLastLine = page.locator("#current-last-line");

        // Form elements
        this.currentPageInput = page.locator("#current-page-input");
        this.lastLineInput = page.locator("#last-line-input");
        this.lastTransactionSelect = page.locator("#last-transaction-select");
        this.submitButton = page.locator("#submit-button");
        this.cancelButton = page.locator("#cancel-button");

        // Messages
        this.successMessage = page.locator("#success-message");
        this.errorMessage = page.locator("#error-message");

        // Transaction table
        this.transactionsTable = page.locator("#transactions-table");
    }

    /**
     * Navigate to passbook initialization page for a specific account
     */
    public void navigateTo(String baseUrl, String accountId) {
        String url = baseUrl + "/passbook/initialize/" + accountId;
        log.info("Navigating to passbook initialization page: {}", url);
        page.navigate(url);
        page.waitForLoadState();
    }

    /**
     * Check if page is loaded
     */
    public boolean isPageLoaded() {
        try {
            pageTitle.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            return pageTitle.isVisible();
        } catch (Exception e) {
            log.warn("Page title not found: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get account number displayed on page
     */
    public String getAccountNumber() {
        return accountNumber.textContent();
    }

    /**
     * Get account name displayed on page
     */
    public String getAccountName() {
        return accountName.textContent();
    }

    /**
     * Get customer name displayed on page
     */
    public String getCustomerName() {
        return customerName.textContent();
    }

    /**
     * Check if current passbook status is visible
     */
    public boolean hasExistingPassbook() {
        try {
            return currentPassbookNumber.isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get current passbook number if exists
     */
    public String getCurrentPassbookNumber() {
        if (hasExistingPassbook()) {
            return currentPassbookNumber.textContent();
        }
        return null;
    }

    /**
     * Fill initialization form
     */
    public void fillInitializationForm(int pageNumber, int lineNumber, String transactionId) {
        log.info("Filling initialization form - Page: {}, Line: {}, Transaction: {}",
            pageNumber, lineNumber, transactionId);

        // Clear and fill current page
        currentPageInput.clear();
        currentPageInput.fill(String.valueOf(pageNumber));

        // Clear and fill last line
        lastLineInput.clear();
        lastLineInput.fill(String.valueOf(lineNumber));

        // Select transaction if provided
        if (transactionId != null && !transactionId.isEmpty()) {
            lastTransactionSelect.selectOption(transactionId);
        }
    }

    /**
     * Fill initialization form without transaction selection
     */
    public void fillInitializationForm(int pageNumber, int lineNumber) {
        fillInitializationForm(pageNumber, lineNumber, null);
    }

    /**
     * Submit the form
     */
    public void submitForm() {
        log.info("Submitting passbook initialization form");
        submitButton.click();
        page.waitForLoadState();
    }

    /**
     * Click cancel button
     */
    public void clickCancel() {
        cancelButton.click();
        page.waitForLoadState();
    }

    /**
     * Check if success message is visible
     */
    public boolean isSuccessMessageVisible() {
        try {
            return successMessage.isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if error message is visible
     */
    public boolean isErrorMessageVisible() {
        try {
            return errorMessage.isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get success message text
     */
    public String getSuccessMessage() {
        if (isSuccessMessageVisible()) {
            return successMessage.textContent();
        }
        return null;
    }

    /**
     * Get error message text
     */
    public String getErrorMessage() {
        if (isErrorMessageVisible()) {
            return errorMessage.textContent();
        }
        return null;
    }

    /**
     * Check if on direct print page (after successful initialization)
     */
    public boolean isOnDirectPrintPage() {
        return page.url().contains("/passbook/direct-print/");
    }

    /**
     * Get number of transactions in the table
     */
    public int getTransactionCount() {
        try {
            return transactionsTable.locator("tbody tr").count();
        } catch (Exception e) {
            log.warn("Could not count transactions: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Get all transaction IDs from the dropdown
     */
    public int getAvailableTransactionCount() {
        try {
            // Count options minus 1 for the "None" option
            return lastTransactionSelect.locator("option").count() - 1;
        } catch (Exception e) {
            log.warn("Could not count transaction options: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Select transaction by index (1-based, 0 = None)
     */
    public void selectTransactionByIndex(int index) {
        try {
            // Get all options and select by index
            int optionCount = lastTransactionSelect.locator("option").count();
            if (index >= 0 && index < optionCount) {
                String value = lastTransactionSelect.locator("option").nth(index).getAttribute("value");
                if (value != null && !value.isEmpty()) {
                    lastTransactionSelect.selectOption(value);
                }
            }
        } catch (Exception e) {
            log.warn("Could not select transaction by index {}: {}", index, e.getMessage());
        }
    }
}
