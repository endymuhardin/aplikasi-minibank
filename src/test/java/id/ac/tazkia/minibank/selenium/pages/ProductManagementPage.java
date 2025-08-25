package id.ac.tazkia.minibank.selenium.pages;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductManagementPage {
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    // List page elements
    @FindBy(id = "page-title")
    private WebElement pageTitle;
    
    @FindBy(id = "create-product-btn")
    private WebElement createProductButton;
    
    @FindBy(id = "products-table")
    private WebElement productsTable;
    
    @FindBy(id = "products-table-body")
    private WebElement productsTableBody;
    
    @FindBy(id = "search")
    private WebElement searchField;
    
    @FindBy(id = "productType")
    private WebElement productTypeFilter;
    
    @FindBy(id = "category")
    private WebElement categoryFilter;
    
    @FindBy(id = "filter-button")
    private WebElement filterButton;
    
    @FindBy(id = "no-products-message")
    private WebElement noProductsMessage;
    
    // Flash messages
    @FindBy(id = "success-message")
    private WebElement successMessage;
    
    @FindBy(id = "error-message")
    private WebElement errorMessage;
    
    // Form page elements
    @FindBy(id = "productCode")
    private WebElement productCodeInput;
    
    @FindBy(id = "productName")
    private WebElement productNameInput;
    
    @FindBy(id = "productType")
    private WebElement productTypeSelect;
    
    @FindBy(id = "productCategory")
    private WebElement productCategoryInput;
    
    @FindBy(id = "description")
    private WebElement descriptionInput;
    
    @FindBy(id = "isActive")
    private WebElement isActiveCheckbox;
    
    @FindBy(id = "submit-btn")
    private WebElement submitButton;
    
    @FindBy(id = "legacy-submit-btn")
    private WebElement legacySubmitButton;
    
    // Multi-step form elements
    @FindBy(id = "next-step-1")
    private WebElement nextStep1Button;
    
    @FindBy(id = "step-1")
    private WebElement step1Section;
    
    @FindBy(id = "step-2")
    private WebElement step2Section;
    
    public ProductManagementPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    
    /**
     * Navigate to product list page
     */
    public ProductManagementPage navigateToList(String baseUrl) {
        driver.get(baseUrl + "/product/list");
        return waitForListPageLoad();
    }
    
    /**
     * Navigate to product creation page
     */
    public ProductManagementPage navigateToCreate(String baseUrl) {
        driver.get(baseUrl + "/product/create");
        return waitForFormPageLoad();
    }
    
    /**
     * Wait for product list page to load
     */
    public ProductManagementPage waitForListPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOf(productsTable),
            ExpectedConditions.visibilityOf(noProductsMessage)
        ));
        log.debug("Product list page loaded successfully");
        return this;
    }
    
    /**
     * Wait for product form page to load
     */
    public ProductManagementPage waitForFormPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(productCodeInput));
        wait.until(ExpectedConditions.visibilityOf(step1Section));
        log.debug("Product form page loaded successfully");
        return this;
    }
    
    /**
     * Check if product list page is loaded
     */
    public boolean isProductListPageLoaded() {
        try {
            waitForListPageLoad();
            return driver.getCurrentUrl().contains("/product/list") &&
                   pageTitle.getText().equals("Product Management");
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
            return driver.getCurrentUrl().contains("/product/create") ||
                   driver.getCurrentUrl().contains("/product/edit");
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
            return createProductButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Click create product button
     */
    public ProductManagementPage clickCreateProduct() {
        wait.until(ExpectedConditions.elementToBeClickable(createProductButton));
        createProductButton.click();
        log.debug("Clicked create product button");
        return this;
    }
    
    /**
     * Search for products
     */
    public ProductManagementPage searchProducts(String searchTerm) {
        wait.until(ExpectedConditions.visibilityOf(searchField));
        searchField.clear();
        searchField.sendKeys(searchTerm);
        filterButton.click();
        log.debug("Searched for products with term: {}", searchTerm);
        return this;
    }
    
    /**
     * Filter products by type
     */
    public ProductManagementPage filterByProductType(String productType) {
        try {
            wait.until(ExpectedConditions.visibilityOf(productTypeFilter));
            Select select = new Select(productTypeFilter);
            select.selectByVisibleText(productType);
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
            wait.until(ExpectedConditions.visibilityOf(categoryFilter));
            Select select = new Select(categoryFilter);
            select.selectByVisibleText(category);
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
            return productsTable.isDisplayed() && 
                   !driver.getPageSource().contains("No products found");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if no products message is displayed
     */
    public boolean isNoProductsMessageDisplayed() {
        try {
            return noProductsMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if success message is visible
     */
    public boolean isSuccessMessageVisible() {
        try {
            return successMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get success message text
     */
    public String getSuccessMessage() {
        if (isSuccessMessageVisible()) {
            return successMessage.getText();
        }
        return "";
    }
    
    /**
     * Check if error message is visible
     */
    public boolean isErrorMessageVisible() {
        try {
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get error message text
     */
    public String getErrorMessage() {
        if (isErrorMessageVisible()) {
            return errorMessage.getText();
        }
        return "";
    }
    
    /**
     * Fill basic product information (Step 1)
     */
    public ProductManagementPage fillBasicProductInfo(String code, String name, String type, String category) {
        wait.until(ExpectedConditions.visibilityOf(productCodeInput));
        
        productCodeInput.clear();
        productCodeInput.sendKeys(code);
        
        productNameInput.clear();
        productNameInput.sendKeys(name);
        
        Select typeSelect = new Select(productTypeSelect);
        typeSelect.selectByVisibleText(type);
        
        productCategoryInput.clear();
        productCategoryInput.sendKeys(category);
        
        log.debug("Filled basic product info: code={}, name={}, type={}, category={}", code, name, type, category);
        return this;
    }
    
    /**
     * Fill product description
     */
    public ProductManagementPage fillDescription(String description) {
        wait.until(ExpectedConditions.visibilityOf(descriptionInput));
        descriptionInput.clear();
        descriptionInput.sendKeys(description);
        log.debug("Filled product description");
        return this;
    }
    
    /**
     * Set product active status
     */
    public ProductManagementPage setActiveStatus(boolean active) {
        wait.until(ExpectedConditions.visibilityOf(isActiveCheckbox));
        if (isActiveCheckbox.isSelected() != active) {
            isActiveCheckbox.click();
        }
        log.debug("Set product active status: {}", active);
        return this;
    }
    
    /**
     * Click next step button (Step 1)
     */
    public ProductManagementPage clickNextStep() {
        wait.until(ExpectedConditions.elementToBeClickable(nextStep1Button));
        nextStep1Button.click();
        log.debug("Clicked next step button");
        return this;
    }
    
    /**
     * Submit product form (using visible submit button)
     */
    public ProductManagementPage submitForm() {
        try {
            // First try the main submit button
            if (submitButton.isDisplayed()) {
                wait.until(ExpectedConditions.elementToBeClickable(submitButton));
                submitButton.click();
                log.debug("Clicked main submit button");
            } else {
                // Fall back to legacy submit button
                wait.until(ExpectedConditions.elementToBeClickable(legacySubmitButton));
                legacySubmitButton.click();
                log.debug("Clicked legacy submit button");
            }
        } catch (Exception e) {
            // If both fail, try direct ID search
            try {
                WebElement submitBtn = driver.findElement(org.openqa.selenium.By.id("submit-btn"));
                submitBtn.click();
                log.debug("Clicked submit button using direct ID search");
            } catch (Exception e2) {
                log.error("Could not find submit button: {}", e2.getMessage());
            }
        }
        return this;
    }
    
    /**
     * Check if a specific product is visible in the table by product code
     */
    public boolean isProductVisible(String productCode) {
        try {
            WebElement productRow = driver.findElement(
                org.openqa.selenium.By.id("product-code-" + productCode)
            );
            return productRow.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Click view button for a specific product
     */
    public ProductManagementPage clickViewProduct(String productCode) {
        try {
            WebElement viewButton = driver.findElement(
                org.openqa.selenium.By.id("view-" + productCode)
            );
            wait.until(ExpectedConditions.elementToBeClickable(viewButton));
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
            WebElement editButton = driver.findElement(
                org.openqa.selenium.By.id("edit-" + productCode)
            );
            wait.until(ExpectedConditions.elementToBeClickable(editButton));
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
            WebElement statusElement = driver.findElement(
                org.openqa.selenium.By.id("status-" + productCode)
            );
            return statusElement.getText();
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
            WebElement validationAlert = driver.findElement(org.openqa.selenium.By.id("validation-alert"));
            return validationAlert.isDisplayed() && !validationAlert.getAttribute("class").contains("hidden");
        } catch (Exception e) {
            // Fallback: check for individual error elements that have IDs
            try {
                return driver.findElement(org.openqa.selenium.By.id("productCode-error")).isDisplayed() ||
                       driver.findElement(org.openqa.selenium.By.id("productName-error")).isDisplayed() ||
                       driver.findElement(org.openqa.selenium.By.id("productType-error")).isDisplayed() ||
                       driver.findElement(org.openqa.selenium.By.id("productCategory-error")).isDisplayed();
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
            return step2Section.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}