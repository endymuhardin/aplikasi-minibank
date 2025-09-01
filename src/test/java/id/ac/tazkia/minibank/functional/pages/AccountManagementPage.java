package id.ac.tazkia.minibank.functional.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccountManagementPage {
    
    private final Page page;
    
    // Account list elements
    private final Locator accountTable;
    private final Locator searchInput;
    private final Locator searchButton;
    private final Locator openAccountButton;
    private final Locator accountRows;
    
    // Account opening form elements
    private final Locator customerSelect;
    private final Locator customerSearchInput;
    private final Locator productSelect;
    private final Locator initialDepositInput;
    private final Locator accountPurposeTextarea;
    
    // Account details elements
    private final Locator accountNumber;
    private final Locator accountStatus;
    private final Locator currentBalance;
    private final Locator availableBalance;
    private final Locator customerName;
    private final Locator productName;
    
    // Form buttons
    private final Locator saveButton;
    private final Locator cancelButton;
    private final Locator backButton;
    private final Locator selectCustomerButton;
    
    // Messages
    private final Locator successMessage;
    private final Locator errorMessage;
    private final Locator validationErrors;
    
    public AccountManagementPage(Page page) {
        this.page = page;
        
        // Initialize account list locators
        this.accountTable = page.locator("#accounts-table");
        this.searchInput = page.locator("input[name='search']");
        this.searchButton = page.locator("#search-accounts-btn");
        this.openAccountButton = page.locator("#open-account-button");
        this.accountRows = page.locator("#accounts-table-body tr");
        
        // Initialize form locators
        this.customerSelect = page.locator("#customer-select");
        this.customerSearchInput = page.locator("#customer-search");
        this.productSelect = page.locator("#productId");
        this.initialDepositInput = page.locator("#initialDeposit");
        this.accountPurposeTextarea = page.locator("#accountName");
        
        // Account details locators
        this.accountNumber = page.locator("#account-number");
        this.accountStatus = page.locator("#account-status");
        this.currentBalance = page.locator("#current-balance");
        this.availableBalance = page.locator("#available-balance");
        this.customerName = page.locator("#customer-name");
        this.productName = page.locator("#product-name");
        
        // Form buttons
        this.saveButton = page.locator("#open-account-submit-btn");
        this.cancelButton = page.locator("#cancel-button");
        this.backButton = page.locator("#back-to-customer-selection");
        this.selectCustomerButton = page.locator("#select-customer-button");
        
        // Messages
        this.successMessage = page.locator("#success-message");
        this.errorMessage = page.locator("#error-message");
        this.validationErrors = page.locator("#validation-errors");
    }
    
    /**
     * Navigate to account list page
     */
    public AccountManagementPage navigateToList(String baseUrl) {
        page.navigate(baseUrl + "/account/list");
        waitForListPageLoad();
        return this;
    }
    
    /**
     * Navigate to open account page
     */
    public AccountManagementPage navigateToOpenAccount(String baseUrl) {
        page.navigate(baseUrl + "/account/open");
        return this;
    }
    
    /**
     * Wait for account list page to load
     */
    public AccountManagementPage waitForListPageLoad() {
        page.waitForLoadState();
        if (searchInput.isVisible()) {
            searchInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        }
        log.debug("Account list page loaded");
        return this;
    }
    
    /**
     * Wait for account form to load
     */
    public AccountManagementPage waitForFormLoad() {
        page.waitForLoadState();
        if (saveButton.isVisible()) {
            saveButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        }
        log.debug("Account form loaded");
        return this;
    }
    
    /**
     * Click open account button
     */
    public AccountManagementPage clickOpenAccount() {
        openAccountButton.click();
        log.debug("Open account button clicked");
        return this;
    }
    
    /**
     * Select customer for account opening
     */
    public AccountManagementPage selectCustomer(String customerIdentifier) {
        // Check if we're on the customer selection page (/account/open)
        if (page.url().contains("/account/open") && !page.url().contains("/account/open/")) {
            // We're on the customer selection page
            // First check if there are already customers visible without searching
            Locator existingButtons = page.locator("a:has-text('Open Account')");
            
            if (existingButtons.count() == 0) {
                // No customers visible, try searching
                log.info("No customers initially visible, searching for: {}", customerIdentifier);
                Locator searchInput = page.locator("#search-input");
                if (searchInput.isVisible() && customerIdentifier != null && !customerIdentifier.isEmpty()) {
                    searchInput.clear();
                    searchInput.fill(customerIdentifier);
                    page.locator("#search-button").click();
                    page.waitForLoadState();
                    // Wait for search results to appear
                    page.waitForSelector(".customer-card, #no-customers-message", new Page.WaitForSelectorOptions().setTimeout(3000));
                }
            } else {
                log.info("Found {} existing customers without search", existingButtons.count());
            }
            
            // Wait for customer cards or no-customers message to be visible
            page.waitForSelector(".customer-card, #no-customers-message", new Page.WaitForSelectorOptions().setTimeout(2000));
            
            // Debug: Check if the no-customers message is visible
            Locator noCustomersMessage = page.locator("#no-customers-message");
            if (noCustomersMessage.isVisible()) {
                log.warn("No customers message is visible on the page");
                
                // Take a screenshot for debugging if enabled
                try {
                    page.screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Paths.get("debug-no-customers.png")));
                    log.info("Debug screenshot saved to debug-no-customers.png");
                } catch (Exception e) {
                    log.debug("Could not save debug screenshot: {}", e.getMessage());
                }
            }
            
            // Check for customer cards first
            Locator customerCards = page.locator(".customer-card");
            log.debug("Found {} customer cards on page", customerCards.count());
            
            // Check if any customers are visible
            Locator openAccountButtons = page.locator("a:has-text('Open Account')");
            log.debug("Found {} open account buttons", openAccountButtons.count());
            
            if (openAccountButtons.count() > 0) {
                // If searching for specific customer, try to find the right one
                if (customerIdentifier != null && !customerIdentifier.isEmpty()) {
                    // Try to find customer card containing the identifier (name or customer number)
                    Locator specificCustomerButton = page.locator(
                        String.format(".customer-card:has-text('%s') a:has-text('Open Account')", customerIdentifier)
                    );
                    
                    if (specificCustomerButton.count() > 0) {
                        log.info("Found specific customer '{}', clicking Open Account button", customerIdentifier);
                        specificCustomerButton.first().click();
                        page.waitForLoadState();
                    } else {
                        log.warn("Specific customer '{}' not found, using first available customer", customerIdentifier);
                        openAccountButtons.first().click();
                        page.waitForLoadState();
                    }
                } else {
                    // Just click the first available Open Account button
                    log.info("No specific customer requested, clicking first Open Account button");
                    openAccountButtons.first().click();
                    page.waitForLoadState();
                }
            } else {
                // Log page content for debugging
                String pageContent = page.content();
                log.debug("Page HTML content length: {} chars", pageContent.length());
                
                // Check if we have search results or empty state
                if (noCustomersMessage.isVisible()) {
                    log.error("Page shows 'No customers found' message");
                } else {
                    log.error("No Open Account buttons found but no 'no customers' message either");
                }
                
                log.warn("No customers available for account opening. Customer identifier: {}", customerIdentifier);
                throw new RuntimeException("No customers available for account opening. Ensure customers exist in the system.");
            }
        } else if (customerSelect.isVisible()) {
            // Dropdown selection (if available on form page)
            customerSelect.selectOption(customerIdentifier);
        } else if (customerSearchInput.isVisible()) {
            // Search input on form page (if available)
            customerSearchInput.fill(customerIdentifier);
            // Wait for search results to appear
            page.waitForSelector("div.search-result:has-text('" + customerIdentifier + "')", new Page.WaitForSelectorOptions().setTimeout(1000));
            page.locator("div.search-result:has-text('" + customerIdentifier + "')").first().click();
        } else {
            // Navigate back to customer selection if needed
            Locator backToSelection = page.locator("#back-to-customer-selection");
            if (backToSelection.isVisible()) {
                backToSelection.click();
                page.waitForLoadState();
                // Retry selection on customer selection page
                return selectCustomer(customerIdentifier);
            }
        }
        log.debug("Selected customer: {}", customerIdentifier);
        return this;
    }
    
    /**
     * Select product for account
     */
    public AccountManagementPage selectProduct(String productCode) {
        if (productSelect.isVisible()) {
            log.debug("Selecting product: {}", productCode);
            
            // Wait for product dropdown to be enabled and have options
            productSelect.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(1000));
            
            // Wait for dropdown to populate with options
            try {
                page.waitForFunction("() => document.querySelector('#productId').options.length > 1", 
                                   new Page.WaitForFunctionOptions().setTimeout(5000));
                log.debug("Product dropdown populated successfully");
            } catch (Exception e) {
                log.debug("Product dropdown taking longer to populate, proceeding with selection");
            }
            
            // Try to select the product using the most common format: "Product Name (CODE)"
            Locator productOption = page.locator("option:has-text('(" + productCode + ")')");
            
            if (productOption.count() > 0) {
                String value = productOption.first().getAttribute("value");
                if (value != null && !value.isEmpty()) {
                    productSelect.selectOption(value);
                    log.info("✅ Successfully selected product: {}", productCode);
                    return this;
                }
            }
            
            // Fallback: try selecting by product code as text
            Locator fallbackOption = page.locator("option:has-text('" + productCode + "')");
            if (fallbackOption.count() > 0) {
                String value = fallbackOption.first().getAttribute("value");
                if (value != null && !value.isEmpty()) {
                    productSelect.selectOption(value);
                    log.info("✅ Successfully selected product (fallback): {}", productCode);
                    return this;
                }
            }
            
            // If product not found, log available options and throw error
            log.error("❌ Product not found: {}. Available options:", productCode);
            Locator allOptions = page.locator("#productId option");
            for (int i = 0; i < allOptions.count(); i++) {
                String optionText = allOptions.nth(i).textContent();
                String optionValue = allOptions.nth(i).getAttribute("value");
                log.error("  Option {}: '{}' (value: {})", i, optionText, optionValue);
            }
            
            throw new RuntimeException("Product not found in dropdown: " + productCode + 
                ". This may indicate a customer type mismatch or missing product data.");
        }
        
        throw new RuntimeException("Product dropdown is not visible");
    }
    
    /**
     * Enter initial deposit amount
     */
    public AccountManagementPage enterInitialDeposit(String amount) {
        initialDepositInput.clear();
        initialDepositInput.fill(amount);
        log.debug("Entered initial deposit: {}", amount);
        return this;
    }
    
    /**
     * Enter account name
     */
    public AccountManagementPage enterAccountName(String accountName) {
        if (accountPurposeTextarea.isVisible()) {
            accountPurposeTextarea.clear();
            accountPurposeTextarea.fill(accountName);
            log.debug("Entered account name: {}", accountName);
        }
        return this;
    }
    
    /**
     * Enter account purpose (legacy - now maps to account name)
     */
    public AccountManagementPage enterAccountPurpose(String purpose) {
        return enterAccountName(purpose);
    }
    
    /**
     * Fill account opening form
     */
    public AccountManagementPage fillAccountOpeningForm(String customer, String product, String initialDeposit, String purpose) {
        selectCustomer(customer);
        // Wait for form to update after customer selection
        page.waitForLoadState();
        selectProduct(product);
        enterInitialDeposit(initialDeposit);
        if (purpose != null) {
            enterAccountPurpose(purpose);
        }
        log.debug("Account opening form filled");
        return this;
    }
    
    /**
     * Click save button
     */
    public AccountManagementPage clickSave() {
        saveButton.click();
        log.debug("Save button clicked");
        return this;
    }
    
    /**
     * Search for account
     */
    public AccountManagementPage searchAccount(String searchTerm) {
        searchInput.fill(searchTerm);
        searchButton.click();
        page.waitForLoadState();
        log.debug("Searched for account: {}", searchTerm);
        return this;
    }
    
    /**
     * Get number of account rows in table
     */
    public int getAccountRowCount() {
        return accountRows.count();
    }
    
    /**
     * Click on account row by index
     */
    public AccountManagementPage clickAccountRow(int index) {
        accountRows.nth(index).click();
        log.debug("Clicked account row at index: {}", index);
        return this;
    }
    
    /**
     * Click on account by account number
     */
    public AccountManagementPage clickAccountByNumber(String accountNumber) {
        page.locator("tr:has-text('" + accountNumber + "')").click();
        log.debug("Clicked account with number: {}", accountNumber);
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
     * Get validation error messages
     */
    public String getValidationErrors() {
        if (hasValidationErrors()) {
            return validationErrors.allTextContents().toString();
        }
        return "";
    }
    
    /**
     * Check if account exists in list
     */
    public boolean isAccountInList(String accountNumber) {
        return page.locator("tr:has-text('" + accountNumber + "')").count() > 0;
    }
    
    /**
     * Get account details from view page
     */
    public String getAccountDetailsText() {
        return page.locator(".container .bg-white").textContent();
    }
    
    /**
     * Check if on account view page
     */
    public boolean isOnViewPage() {
        return page.url().contains("/account/view/");
    }
    
    /**
     * Check if on account list page
     */
    public boolean isOnListPage() {
        return page.url().contains("/account/list");
    }
    
    /**
     * Get account number from view page or success message
     */
    public String getAccountNumber() {
        // First check if there's an element with id="account-number"
        if (accountNumber.isVisible()) {
            return accountNumber.textContent().trim();
        }
        
        // Try to extract from success message
        if (successMessage.isVisible()) {
            String message = successMessage.textContent();
            // Extract account number from message like "Account opened successfully. Account Number: A2000001"
            if (message.contains("Account Number:")) {
                String[] parts = message.split("Account Number:");
                if (parts.length > 1) {
                    return parts[1].trim();
                }
            }
        }
        
        // Try alternative selector
        Locator altAccountNumber = page.locator("*:has-text('Account Number')").locator("..").locator("td, span, div").last();
        if (altAccountNumber.isVisible()) {
            return altAccountNumber.textContent().trim();
        }
        
        return "";
    }
    
    /**
     * Get account status
     */
    public String getAccountStatus() {
        if (accountStatus.isVisible()) {
            return accountStatus.textContent();
        }
        // Try alternative selector
        Locator altStatus = page.locator("*:has-text('Status')").locator("..").locator("td, span, div").last();
        if (altStatus.isVisible()) {
            return altStatus.textContent();
        }
        return "";
    }
    
    /**
     * Get current balance
     */
    public String getCurrentBalance() {
        if (currentBalance.isVisible()) {
            return currentBalance.textContent();
        }
        // Try alternative selector
        Locator altBalance = page.locator("*:has-text('Current Balance')").locator("..").locator("td, span, div").last();
        if (altBalance.isVisible()) {
            return altBalance.textContent();
        }
        return "";
    }
    
    /**
     * Check if account has expected balance
     */
    public boolean hasBalance(String expectedBalance) {
        String balance = getCurrentBalance();
        return balance.contains(expectedBalance);
    }
    
    /**
     * Click back button
     */
    public AccountManagementPage clickBackButton() {
        if (backButton.isVisible()) {
            backButton.click();
        } else {
            page.locator("a:has-text('Back')").click();
        }
        log.debug("Back button clicked");
        return this;
    }
    
    /**
     * Check if multiple accounts can be opened for same customer
     */
    public boolean canOpenMultipleAccounts() {
        // This would be determined by business rules
        return true;
    }
    
    /**
     * Get product details for account
     */
    public String getProductDetails() {
        if (productName.isVisible()) {
            return productName.textContent();
        }
        return page.locator("*:has-text('Product')").locator("..").locator("td, span, div").last().textContent();
    }
    
    /**
     * Verify nisbah ratio is displayed (for Islamic products)
     */
    public boolean isNisbahDisplayed() {
        return page.locator("*:has-text('Nisbah')").count() > 0 ||
               page.locator("*:has-text('Profit Sharing')").count() > 0;
    }
    
    /**
     * Get nisbah ratio text
     */
    public String getNisbahRatio() {
        Locator nisbah = page.locator("*:has-text('Nisbah')").locator("..").locator("td, span, div").last();
        if (nisbah.isVisible()) {
            return nisbah.textContent();
        }
        return "";
    }
    
    /**
     * Check if page is loaded
     */
    public boolean isLoaded() {
        try {
            return page.url().contains("/account");
        } catch (Exception e) {
            return false;
        }
    }
}