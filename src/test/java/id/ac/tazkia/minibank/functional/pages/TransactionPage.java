package id.ac.tazkia.minibank.functional.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionPage {
    
    private final Page page;
    
    // Transaction list elements
    private final Locator transactionTable;
    private final Locator searchInput;
    private final Locator searchButton;
    private final Locator filterType;
    private final Locator filterDateFrom;
    private final Locator filterDateTo;
    private final Locator transactionRows;
    
    // Deposit form elements
    private final Locator depositButton;
    private final Locator accountNumberInput;
    private final Locator accountSearchButton;
    private final Locator depositAmountInput;
    private final Locator depositDescriptionTextarea;
    private final Locator depositChannelSelect;
    private final Locator createdByInput;
    private final Locator referenceNumberInput;
    
    // Withdrawal form elements
    private final Locator withdrawalButton;
    private final Locator withdrawalAmountInput;
    private final Locator withdrawalDescriptionTextarea;
    private final Locator withdrawalChannelSelect;
    
    // Transaction details elements
    private final Locator transactionNumber;
    private final Locator transactionType;
    private final Locator transactionAmount;
    private final Locator transactionDate;
    private final Locator transactionChannel;
    private final Locator balanceBefore;
    private final Locator balanceAfter;
    private final Locator accountDetails;
    
    // Form buttons
    private final Locator submitButton;
    private final Locator cancelButton;
    private final Locator printButton;
    private final Locator backButton;
    
    // Messages
    private final Locator successMessage;
    private final Locator errorMessage;
    private final Locator validationErrors;
    private final Locator balanceInfo;
    
    public TransactionPage(Page page) {
        this.page = page;
        
        // Initialize transaction list locators
        this.transactionTable = page.locator("#transaction-table");
        this.searchInput = page.locator("#search-input");
        this.searchButton = page.locator("#search-button");
        this.filterType = page.locator("#filter-type");
        this.filterDateFrom = page.locator("#filter-date-from");
        this.filterDateTo = page.locator("#filter-date-to");
        this.transactionRows = page.locator("tr[data-transaction-id]");
        
        // Initialize deposit form locators
        this.depositButton = page.locator("#deposit-button");
        this.accountNumberInput = page.locator("#account-number");
        this.accountSearchButton = page.locator("#account-search-button");
        this.depositAmountInput = page.locator("#amount");
        this.depositDescriptionTextarea = page.locator("#description");
        this.depositChannelSelect = page.locator("#transactionChannel");
        this.createdByInput = page.locator("#createdBy");
        this.referenceNumberInput = page.locator("#referenceNumber");
        
        // Initialize withdrawal form locators (withdrawal form uses same IDs as deposit form)
        this.withdrawalButton = page.locator("#withdrawal-button");
        this.withdrawalAmountInput = page.locator("#amount");
        this.withdrawalDescriptionTextarea = page.locator("#description");
        this.withdrawalChannelSelect = page.locator("#transactionChannel");
        
        // Transaction details locators
        this.transactionNumber = page.locator("#transaction-number");
        this.transactionType = page.locator("#transaction-type");
        this.transactionAmount = page.locator("#transaction-amount");
        this.transactionDate = page.locator("#transaction-date");
        this.transactionChannel = page.locator("#transaction-channel");
        this.balanceBefore = page.locator("#balance-before");
        this.balanceAfter = page.locator("#balance-after");
        this.accountDetails = page.locator("#account-details");
        
        // Form buttons
        this.submitButton = page.locator("#process-deposit-btn");
        this.cancelButton = page.locator("#cancel-button");
        this.printButton = page.locator("#print-button");
        this.backButton = page.locator("#back-to-cash-deposit-selection");
        
        // Messages
        this.successMessage = page.locator("#success-message");
        this.errorMessage = page.locator("#error-message");
        this.validationErrors = page.locator("#validation-errors");
        this.balanceInfo = page.locator("#current-balance");
    }
    
    /**
     * Navigate to transaction list page
     */
    public TransactionPage navigateToList(String baseUrl) {
        page.navigate(baseUrl + "/transaction/list");
        waitForListPageLoad();
        return this;
    }
    
    /**
     * Navigate to deposit page
     */
    public TransactionPage navigateToDeposit(String baseUrl) {
        page.navigate(baseUrl + "/transaction/cash-deposit");
        return this;
    }
    
    /**
     * Navigate to withdrawal page
     */
    public TransactionPage navigateToWithdrawal(String baseUrl) {
        page.navigate(baseUrl + "/transaction/cash-withdrawal");
        return this;
    }
    
    /**
     * Wait for transaction list page to load
     */
    public TransactionPage waitForListPageLoad() {
        page.waitForLoadState();
        if (transactionTable.isVisible()) {
            transactionTable.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        }
        log.debug("Transaction list page loaded");
        return this;
    }
    
    /**
     * Wait for transaction form to load
     */
    public TransactionPage waitForFormLoad() {
        page.waitForLoadState();
        if (submitButton.isVisible()) {
            submitButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        }
        log.debug("Transaction form loaded");
        return this;
    }
    
    /**
     * Click deposit button
     */
    public TransactionPage clickDepositButton() {
        depositButton.click();
        log.debug("Deposit button clicked");
        return this;
    }
    
    /**
     * Click withdrawal button
     */
    public TransactionPage clickWithdrawalButton() {
        withdrawalButton.click();
        log.debug("Withdrawal button clicked");
        return this;
    }
    
    /**
     * Enter account number for transaction
     */
    public TransactionPage enterAccountNumber(String accountNumber) {
        accountNumberInput.clear();
        accountNumberInput.fill(accountNumber);
        log.debug("Entered account number: {}", accountNumber);
        return this;
    }
    
    /**
     * Search and select account
     */
    public TransactionPage searchAndSelectAccount(String accountNumber) {
        log.info("Searching and selecting account: {}", accountNumber);
        
        // Monitor console logs for JavaScript errors
        page.onConsoleMessage(msg -> {
            if (msg.type().equals("error") || msg.type().equals("warning")) {
                log.error("BROWSER CONSOLE {}: {}", msg.type().toUpperCase(), msg.text());
            } else {
                log.debug("BROWSER CONSOLE {}: {}", msg.type().toUpperCase(), msg.text());
            }
        });
        
        // Monitor page errors
        page.onPageError(error -> {
            log.error("BROWSER PAGE ERROR: {}", error);
        });
        
        // Wait for accounts list to load
        page.waitForSelector("#accounts-list", new Page.WaitForSelectorOptions().setTimeout(10000));
        
        // Look for account cards  
        Locator accountCards = page.locator("#accounts-list .account-card");
        int cardCount = accountCards.count();
        
        // First, look through visible cards
        for (int i = 0; i < cardCount; i++) {
            Locator card = accountCards.nth(i);
            Locator accountNumberElement = card.locator(".account-number");
            
            if (accountNumberElement.isVisible()) {
                String cardAccountNumber = accountNumberElement.textContent().trim();
                
                if (accountNumber.equals(cardAccountNumber)) {
                    log.info("Found matching account: {}", accountNumber);
                    
                    // Use ID-based selector only (button ID follows pattern: select-button-{accountId})
                    Locator selectButton = card.locator("button[id*='select-button-']");
                    
                    if (selectButton.count() > 0) {
                        log.info("Found select button, analyzing button and attempting click");
                        
                        // Debug button styling and text visibility
                        String buttonText = selectButton.first().textContent();
                        String buttonClasses = selectButton.first().getAttribute("class");
                        String accountId = selectButton.first().getAttribute("data-account-id");
                        
                        log.info("Button text content: '{}'", buttonText);
                        log.info("Button classes: '{}'", buttonClasses);
                        log.info("Button data-account-id: {}", accountId);
                        
                        // Click the button normally to trigger onclick handler
                        log.info("Clicking button to trigger onclick handler");
                        selectButton.first().click();
                        page.waitForLoadState();
                        
                        // Wait for the deposit form to be loaded by checking for the amount field
                        page.waitForSelector("#amount", new Page.WaitForSelectorOptions().setTimeout(10000));
                        
                        // Check what happened after click
                        log.info("After button click - URL: {}", page.url());
                        log.info("After button click - Page title: {}", page.title());
                        
                        return this;
                    } else {
                        log.error("No ID-based select button found for account: {}", accountNumber);
                    }
                }
            }
        }
        
        // If not found, try searching first
        log.info("Account not found in visible cards, searching for: {}", accountNumber);
        Locator searchInput = page.locator("#search");
        if (searchInput.isVisible()) {
            searchInput.clear();
            searchInput.fill(accountNumber);
            page.locator("#search-button").click();
            page.waitForLoadState();
            
            // Look for the account again after search
            accountCards = page.locator("#accounts-list .account-card");
            for (int i = 0; i < accountCards.count(); i++) {
                Locator card = accountCards.nth(i);
                Locator accountNumberElement = card.locator(".account-number");
                
                if (accountNumberElement.isVisible()) {
                    String cardAccountNumber = accountNumberElement.textContent().trim();
                    
                    if (accountNumber.equals(cardAccountNumber)) {
                        log.info("Found account after search, clicking button");
                        Locator selectButton = card.locator("button[id*='select-button-']");
                        selectButton.first().click();
                        page.waitForLoadState();
                        
                        // Wait for the deposit form to be loaded by checking for the amount field
                        page.waitForSelector("#amount", new Page.WaitForSelectorOptions().setTimeout(10000));
                        
                        return this;
                    }
                }
            }
        }
        
        throw new RuntimeException("Account not found: " + accountNumber);
    }
    
    /**
     * Search and select account for withdrawal
     */
    public TransactionPage searchAndSelectAccountForWithdrawal(String accountNumber) {
        log.info("Searching and selecting account for withdrawal: {}", accountNumber);
        
        // Enable console logging for debugging
        page.onConsoleMessage(msg -> {
            if (msg.type().equals("error")) {
                log.error("BROWSER CONSOLE ERROR: {}", msg.text());
            } else {
                log.debug("BROWSER CONSOLE {}: {}", msg.type().toUpperCase(), msg.text());
            }
        });
        
        // Monitor page errors
        page.onPageError(error -> {
            log.error("BROWSER PAGE ERROR: {}", error);
        });
        
        // Wait for accounts list to load
        page.waitForSelector("#accounts-list", new Page.WaitForSelectorOptions().setTimeout(10000));
        
        // Look for account cards  
        Locator accountCards = page.locator("#accounts-list .account-card");
        int cardCount = accountCards.count();
        
        // First, look through visible cards
        for (int i = 0; i < cardCount; i++) {
            Locator card = accountCards.nth(i);
            Locator accountNumberElement = card.locator(".account-number");
            
            if (accountNumberElement.isVisible()) {
                String cardAccountNumber = accountNumberElement.textContent().trim();
                
                if (accountNumber.equals(cardAccountNumber)) {
                    log.info("Found matching account: {}", accountNumber);
                    
                    // Use ID-based selector only (button ID follows pattern: select-button-{accountId})
                    Locator selectButton = card.locator("button[id*='select-button-']");
                    
                    if (selectButton.count() > 0) {
                        log.info("Found select button, analyzing button and attempting click");
                        
                        // Debug button styling and text visibility
                        String buttonText = selectButton.first().textContent();
                        String buttonClasses = selectButton.first().getAttribute("class");
                        String accountId = selectButton.first().getAttribute("data-account-id");
                        
                        log.info("Button text content: '{}'", buttonText);
                        log.info("Button classes: '{}'", buttonClasses);
                        log.info("Button data-account-id: {}", accountId);
                        
                        // Click the button normally to trigger onclick handler
                        log.info("Clicking button to trigger onclick handler");
                        selectButton.first().click();
                        page.waitForLoadState();
                        
                        // Wait for the withdrawal form to be loaded by checking for the amount field
                        page.waitForSelector("#amount", new Page.WaitForSelectorOptions().setTimeout(10000));
                        
                        // Check what happened after click
                        log.info("After button click - URL: {}", page.url());
                        log.info("After button click - Page title: {}", page.title());
                        
                        return this;
                    } else {
                        log.error("No ID-based select button found for account: {}", accountNumber);
                    }
                }
            }
        }
        
        // If not found, try searching first
        log.info("Account not found in visible cards, searching for: {}", accountNumber);
        Locator searchInput = page.locator("#search");
        if (searchInput.isVisible()) {
            searchInput.clear();
            searchInput.fill(accountNumber);
            page.locator("#search-button").click();
            page.waitForLoadState();
            
            // Look for the account again after search
            accountCards = page.locator("#accounts-list .account-card");
            for (int i = 0; i < accountCards.count(); i++) {
                Locator card = accountCards.nth(i);
                Locator accountNumberElement = card.locator(".account-number");
                
                if (accountNumberElement.isVisible()) {
                    String cardAccountNumber = accountNumberElement.textContent().trim();
                    
                    if (accountNumber.equals(cardAccountNumber)) {
                        log.info("Found account after search, clicking button");
                        Locator selectButton = card.locator("button[id*='select-button-']");
                        selectButton.first().click();
                        page.waitForLoadState();
                        
                        // Wait for the withdrawal form to be loaded by checking for the amount field
                        page.waitForSelector("#amount", new Page.WaitForSelectorOptions().setTimeout(10000));
                        
                        return this;
                    }
                }
            }
        }
        
        throw new RuntimeException("Account not found: " + accountNumber);
    }
    
    /**
     * Enter deposit amount
     */
    public TransactionPage enterDepositAmount(String amount) {
        depositAmountInput.clear();
        depositAmountInput.fill(amount);
        log.debug("Entered deposit amount: {}", amount);
        return this;
    }
    
    /**
     * Enter withdrawal amount
     */
    public TransactionPage enterWithdrawalAmount(String amount) {
        withdrawalAmountInput.clear();
        withdrawalAmountInput.fill(amount);
        log.debug("Entered withdrawal amount: {}", amount);
        return this;
    }
    
    /**
     * Enter transaction description
     */
    public TransactionPage enterDescription(String description) {
        Locator descField = depositDescriptionTextarea.isVisible() ? 
            depositDescriptionTextarea : withdrawalDescriptionTextarea;
        descField.fill(description);
        log.debug("Entered description: {}", description);
        return this;
    }
    
    /**
     * Select transaction channel (if field exists)
     */
    public TransactionPage selectChannel(String channel) {
        try {
            // Check if either channel selector is available
            if (depositChannelSelect.isVisible()) {
                depositChannelSelect.selectOption(channel);
                log.debug("Selected deposit channel: {}", channel);
            } else if (withdrawalChannelSelect.isVisible()) {
                withdrawalChannelSelect.selectOption(channel);
                log.debug("Selected withdrawal channel: {}", channel);
            } else {
                // Channel selection not available in current form
                log.debug("Channel selection not available in form, channel {} will be set automatically by backend", channel);
            }
        } catch (Exception e) {
            // Channel field doesn't exist or is not available - this is expected for some forms
            log.debug("Channel selection not available ({}), backend will set channel automatically", e.getMessage());
        }
        return this;
    }
    
    /**
     * Fill deposit form
     */
    public TransactionPage fillDepositForm(String accountNumber, String amount, String description, String channel) {
        searchAndSelectAccount(accountNumber);
        enterDepositAmount(amount);
        if (description != null) {
            enterDescription(description);
        }
        if (channel != null) {
            selectChannel(channel);
        }
        // Always fill createdBy field as it's required
        enterCreatedBy("teller1");
        log.debug("Deposit form filled");
        return this;
    }
    
    /**
     * Enter created by field
     */
    public TransactionPage enterCreatedBy(String createdBy) {
        if (createdByInput.isVisible()) {
            createdByInput.fill(createdBy);
            log.debug("Entered created by: {}", createdBy);
        }
        return this;
    }
    
    /**
     * Fill withdrawal form
     */
    public TransactionPage fillWithdrawalForm(String accountNumber, String amount, String description, String channel) {
        searchAndSelectAccountForWithdrawal(accountNumber);
        enterWithdrawalAmount(amount);
        if (description != null) {
            enterDescription(description);
        }
        if (channel != null) {
            selectChannel(channel);
        }
        // Always fill createdBy field as it's required
        enterCreatedBy("teller1");
        log.debug("Withdrawal form filled");
        return this;
    }
    
    /**
     * Click submit button
     */
    public TransactionPage clickSubmit() {
        submitButton.click();
        log.debug("Submit button clicked");
        return this;
    }
    
    /**
     * Search for transaction
     */
    public TransactionPage searchTransaction(String searchTerm) {
        searchInput.fill(searchTerm);
        searchButton.click();
        page.waitForLoadState();
        log.debug("Searched for transaction: {}", searchTerm);
        return this;
    }
    
    /**
     * Filter transactions by type
     */
    public TransactionPage filterByType(String type) {
        filterType.selectOption(type);
        page.waitForLoadState();
        log.debug("Filtered by type: {}", type);
        return this;
    }
    
    /**
     * Get number of transaction rows in table
     */
    public int getTransactionRowCount() {
        return transactionRows.count();
    }
    
    /**
     * Click on transaction row by index
     */
    public TransactionPage clickTransactionRow(int index) {
        transactionRows.nth(index).click();
        log.debug("Clicked transaction row at index: {}", index);
        return this;
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
     * Get success message text
     */
    public String getSuccessMessage() {
        if (isSuccessMessageVisible()) {
            return successMessage.textContent();
        }
        return "";
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
     * Get error message text
     */
    public String getErrorMessage() {
        if (isErrorMessageVisible()) {
            return errorMessage.textContent();
        }
        return "";
    }
    
    /**
     * Check if validation errors are visible
     */
    public boolean hasValidationErrors() {
        return validationErrors.count() > 0;
    }
    
    /**
     * Get transaction number from view page
     */
    public String getTransactionNumber() {
        if (transactionNumber.isVisible()) {
            return transactionNumber.textContent();
        }
        // Try alternative selector
        Locator altTxNumber = page.locator("*:has-text('Transaction Number')").locator("..").locator("td, span, div").last();
        if (altTxNumber.isVisible()) {
            return altTxNumber.textContent();
        }
        return "";
    }
    
    /**
     * Get balance before transaction
     */
    public String getBalanceBefore() {
        if (balanceBefore.isVisible()) {
            return balanceBefore.textContent();
        }
        Locator altBalance = page.locator("*:has-text('Balance Before')").locator("..").locator("td, span, div").last();
        if (altBalance.isVisible()) {
            return altBalance.textContent();
        }
        return "";
    }
    
    /**
     * Get balance after transaction
     */
    public String getBalanceAfter() {
        if (balanceAfter.isVisible()) {
            return balanceAfter.textContent();
        }
        Locator altBalance = page.locator("*:has-text('Balance After')").locator("..").locator("td, span, div").last();
        if (altBalance.isVisible()) {
            return altBalance.textContent();
        }
        return "";
    }
    
    /**
     * Check if on transaction view page
     */
    public boolean isOnViewPage() {
        return page.url().contains("/transaction/view/") ||
               page.url().contains("/transaction/receipt/");
    }
    
    /**
     * Check if on transaction list page
     */
    public boolean isOnListPage() {
        return page.url().contains("/transaction/list");
    }
    
    /**
     * Get current account balance
     */
    public String getCurrentBalance() {
        if (balanceInfo.isVisible()) {
            return balanceInfo.textContent();
        }
        return "";
    }
    
    /**
     * Verify balance updated after transaction
     */
    public boolean isBalanceUpdated(String expectedBalance) {
        String currentBalance = getCurrentBalance();
        return currentBalance.contains(expectedBalance);
    }
    
    /**
     * Click print button
     */
    public TransactionPage clickPrintButton() {
        if (printButton.isVisible()) {
            printButton.click();
            log.debug("Print button clicked");
        }
        return this;
    }
    
    /**
     * Check if transaction exists in list
     */
    public boolean isTransactionInList(String transactionNumber) {
        return page.locator("tr:has-text('" + transactionNumber + "')").count() > 0;
    }
    
    /**
     * Get transaction details text
     */
    public String getTransactionDetailsText() {
        return page.locator(".container .bg-white").textContent();
    }
    
    /**
     * Validate minimum balance requirement
     */
    public boolean hasMinimumBalanceError() {
        String error = getErrorMessage();
        return error.contains("minimum balance") || error.contains("saldo minimum");
    }
    
    /**
     * Check if page is loaded
     */
    public boolean isLoaded() {
        try {
            return page.url().contains("/transaction");
        } catch (Exception e) {
            return false;
        }
    }
}