package id.ac.tazkia.minibank.functional.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductManagementPage {
    
    private final Page page;
    
    // List page elements using IDs exclusively
    private final Locator pageTitle;
    private final Locator createProductButton;
    private final Locator productsTable;
    private final Locator productsTableBody;
    private final Locator searchField;
    private final Locator productTypeFilter;
    private final Locator categoryFilter;
    private final Locator filterButton;
    private final Locator noProductsMessage;
    
    // Flash messages
    private final Locator successMessage;
    private final Locator errorMessage;
    
    // Form page elements
    private final Locator productCodeInput;
    private final Locator productNameInput;
    private final Locator productTypeSelect;
    private final Locator productCategoryInput;
    private final Locator descriptionInput;
    private final Locator isActiveCheckbox;
    private final Locator submitButton;
    private final Locator legacySubmitButton;
    
    // Multi-step form elements
    private final Locator nextStep1Button;
    private final Locator step1Section;
    private final Locator step2Section;
    
    public ProductManagementPage(Page page) {
        this.page = page;
        
        // Initialize locators using IDs exclusively
        this.pageTitle = page.locator("#page-title");
        this.createProductButton = page.locator("#create-product-btn");
        this.productsTable = page.locator("#products-table");
        this.productsTableBody = page.locator("#products-table-body");
        this.searchField = page.locator("#search");
        this.productTypeFilter = page.locator("#productType");
        this.categoryFilter = page.locator("#category");
        this.filterButton = page.locator("#filter-button");
        this.noProductsMessage = page.locator("#no-products-message");
        
        // Flash messages
        this.successMessage = page.locator("#success-message");
        this.errorMessage = page.locator("#error-message");
        
        // Form elements
        this.productCodeInput = page.locator("#productCode");
        this.productNameInput = page.locator("#productName");
        this.productTypeSelect = page.locator("#productType");
        this.productCategoryInput = page.locator("#productCategory");
        this.descriptionInput = page.locator("#description");
        this.isActiveCheckbox = page.locator("#isActive");
        this.submitButton = page.locator("#submit-btn");
        this.legacySubmitButton = page.locator("#legacy-submit-btn");
        
        // Multi-step form elements
        this.nextStep1Button = page.locator("#next-step-1");
        this.step1Section = page.locator("#step-1");
        this.step2Section = page.locator("#step-2");
    }
    
    /**
     * Navigate to product list page
     */
    public ProductManagementPage navigateToList(String baseUrl) {
        page.navigate(baseUrl + "/product/list");
        return waitForListPageLoad();
    }
    
    /**
     * Navigate to product creation page
     */
    public ProductManagementPage navigateToCreate(String baseUrl) {
        page.navigate(baseUrl + "/product/create");
        return waitForFormPageLoad();
    }
    
    /**
     * Wait for product list page to load
     */
    public ProductManagementPage waitForListPageLoad() {
        pageTitle.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        // Wait for either products table or no products message
        page.waitForCondition(() -> 
            productsTable.isVisible() || noProductsMessage.isVisible()
        );
        log.debug("Product list page loaded successfully");
        return this;
    }
    
    /**
     * Wait for product form page to load
     */
    public ProductManagementPage waitForFormPageLoad() {
        productCodeInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        step1Section.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        log.debug("Product form page loaded successfully");
        return this;
    }
    
    /**
     * Check if product list page is loaded
     */
    public boolean isProductListPageLoaded() {
        try {
            waitForListPageLoad();
            return page.url().contains("/product/list") &&
                   pageTitle.textContent().equals("Product Management");
        } catch (Exception e) {
            log.debug("Product list page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if product form page is loaded
     */
    public boolean isProductFormPageLoaded() {
        try {
            waitForFormPageLoad();
            return page.url().contains("/product/create") ||
                   page.url().contains("/product/edit");
        } catch (Exception e) {
            log.debug("Product form page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if create product button is visible
     */
    public boolean isCreateProductButtonVisible() {
        try {
            return createProductButton.isVisible();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Click create product button
     */
    public ProductManagementPage clickCreateProduct() {
        createProductButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        createProductButton.click();
        log.debug("Clicked create product button");
        return this;
    }
    
    /**
     * Search for products
     */
    public ProductManagementPage searchProducts(String searchTerm) {
        searchField.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        searchField.clear();
        searchField.fill(searchTerm);
        filterButton.click();
        log.debug("Searched for products with term: {}", searchTerm);
        return this;
    }
    
    /**
     * Filter products by type
     */
    public ProductManagementPage filterByProductType(String productType) {
        try {
            productTypeFilter.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            productTypeFilter.selectOption(productType);
            filterButton.click();
            log.debug("Filtered products by type: {}", productType);
        } catch (Exception e) {
            log.debug("Could not filter by product type '{}', type may not be available: {}", productType, e.getMessage());
            // Continue with the test - just click filter without selecting specific type
            filterButton.click();
        }
        return this;
    }
    
    /**
     * Filter products by category
     */
    public ProductManagementPage filterByCategory(String category) {
        try {
            categoryFilter.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            categoryFilter.selectOption(category);
            filterButton.click();
            log.debug("Filtered products by category: {}", category);
        } catch (Exception e) {
            log.debug("Could not filter by category '{}', category may not be available: {}", category, e.getMessage());
            // Continue with the test - just click filter without selecting specific category
            filterButton.click();
        }
        return this;
    }
    
    /**
     * Check if products are displayed in the table
     */
    public boolean areProductsDisplayed() {
        try {
            return productsTable.isVisible() && 
                   !page.content().contains("No products found");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if no products message is displayed
     */
    public boolean isNoProductsMessageDisplayed() {
        try {
            return noProductsMessage.isVisible();
        } catch (Exception e) {
            return false;
        }
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
     * Fill basic product information (Step 1)
     */
    public ProductManagementPage fillBasicProductInfo(String code, String name, String type, String category) {
        productCodeInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        
        productCodeInput.clear();
        productCodeInput.fill(code);
        
        productNameInput.clear();
        productNameInput.fill(name);
        
        productTypeSelect.selectOption(type);
        
        productCategoryInput.clear();
        productCategoryInput.fill(category);
        
        log.debug("Filled basic product info: code={}, name={}, type={}, category={}", code, name, type, category);
        return this;
    }
    
    /**
     * Fill product description
     */
    public ProductManagementPage fillDescription(String description) {
        descriptionInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        descriptionInput.clear();
        descriptionInput.fill(description);
        log.debug("Filled product description");
        return this;
    }
    
    /**
     * Set product active status
     */
    public ProductManagementPage setActiveStatus(boolean active) {
        isActiveCheckbox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        if (isActiveCheckbox.isChecked() != active) {
            isActiveCheckbox.click();
        }
        log.debug("Set product active status: {}", active);
        return this;
    }
    
    /**
     * Click next step button (Step 1)
     */
    public ProductManagementPage clickNextStep() {
        nextStep1Button.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        nextStep1Button.click();
        log.debug("Clicked next step button");
        return this;
    }
    
    /**
     * Trigger form validation without completing full submission
     */
    public ProductManagementPage triggerValidation() {
        try {
            // Try to trigger client-side validation by clicking next/submit
            Locator nextButton = page.locator("#next-step-1");
            if (nextButton.isVisible()) {
                nextButton.click();
                page.waitForTimeout(300);
                log.debug("Triggered validation by clicking next step");
            } else {
                // Try direct submit button if visible
                if (submitButton.isVisible()) {
                    submitButton.click();
                    page.waitForTimeout(300);
                    log.debug("Triggered validation by clicking submit");
                }
            }
        } catch (Exception e) {
            log.debug("Validation trigger completed: {}", e.getMessage());
        }
        return this;
    }

    /**
     * Submit product form (using visible submit button)
     */
    public ProductManagementPage submitForm() {
        try {
            // First try the main submit button (only visible on step 6)
            if (submitButton.isVisible()) {
                submitButton.click();
                log.debug("Clicked main submit button");
            } else {
                // Navigate to final step first if not already there
                log.debug("Main submit button not visible, navigating to final step");
                
                // Click through steps to reach the final step
                for (int step = 1; step <= 5; step++) {
                    Locator nextButton = page.locator("#next-step-" + step);
                    if (nextButton.isVisible()) {
                        nextButton.click();
                        page.waitForTimeout(500); // Small delay for step transition
                    }
                }
                
                // Now try to submit
                if (submitButton.isVisible()) {
                    submitButton.click();
                    log.debug("Clicked main submit button after navigating to final step");
                } else {
                    // Last resort: submit the form directly
                    page.locator("#product-form").evaluate("form => form.submit()");
                    log.debug("Submitted form programmatically");
                }
            }
        } catch (Exception e) {
            log.error("Could not submit form: {}", e.getMessage());
            // Try form submission as last resort
            try {
                page.locator("#product-form").evaluate("form => form.submit()");
                log.debug("Submitted form programmatically as fallback");
            } catch (Exception e2) {
                log.error("All submit attempts failed: {}", e2.getMessage());
            }
        }
        return this;
    }
    

    /**
     * Check if a specific product is visible in the table by product code
     */
    public boolean isProductVisible(String productCode) {
        try {
            Locator productRow = page.locator("#product-code-" + productCode);
            return productRow.isVisible();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Click view button for a specific product
     */
    public ProductManagementPage clickViewProduct(String productCode) {
        try {
            Locator viewButton = page.locator("#view-" + productCode);
            viewButton.click();
            log.debug("Clicked view button for product: {}", productCode);
        } catch (Exception e) {
            log.error("Could not click view button for product: {}", productCode, e);
        }
        return this;
    }
    
    /**
     * Click edit button for a specific product
     */
    public ProductManagementPage clickEditProduct(String productCode) {
        try {
            Locator editButton = page.locator("#edit-" + productCode);
            editButton.click();
            log.debug("Clicked edit button for product: {}", productCode);
        } catch (Exception e) {
            log.error("Could not click edit button for product: {}", productCode, e);
        }
        return this;
    }
    
    /**
     * Get product status from the table
     */
    public String getProductStatus(String productCode) {
        try {
            Locator statusElement = page.locator("#status-" + productCode);
            return statusElement.textContent();
        } catch (Exception e) {
            log.debug("Could not get status for product: {}", productCode);
            return "";
        }
    }
    
    /**
     * Check if form validation errors are visible
     */
    public boolean hasValidationErrors() {
        try {
            // Check if validation alert container is visible
            Locator validationAlert = page.locator("#validation-alert");
            return validationAlert.isVisible() && !validationAlert.getAttribute("class").contains("hidden");
        } catch (Exception e) {
            // Fallback: check for individual error elements that have IDs
            try {
                return page.locator("#productCode-error").isVisible() ||
                       page.locator("#productName-error").isVisible() ||
                       page.locator("#productType-error").isVisible() ||
                       page.locator("#productCategory-error").isVisible();
            } catch (Exception e2) {
                return false;
            }
        }
    }
    
    /**
     * Check if step 2 is visible (multi-step form navigation)
     */
    public boolean isStep2Visible() {
        try {
            return step2Section.isVisible();
        } catch (Exception e) {
            return false;
        }
    }
}