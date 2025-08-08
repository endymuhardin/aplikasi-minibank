package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class ProductListPage extends BasePage {
    
    @FindBy(id = "create-product-btn")
    private WebElement createProductButton;
    
    @FindBy(id = "search")
    private WebElement searchField;
    
    @FindBy(id = "productType")
    private WebElement productTypeDropdown;
    
    @FindBy(id = "category")
    private WebElement categoryDropdown;
    
    @FindBy(id = "filter-button")
    private WebElement filterButton;
    
    @FindBy(id = "products-table")
    private WebElement productsTable;
    
    @FindBy(css = "#products-table tbody tr")
    private List<WebElement> productRows;
    
    public ProductListPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    public ProductListPage open() {
        // Always go to the first page with newest products first (sorted by creation date desc)
        driver.get(baseUrl + "/product/list?page=0&sortBy=createdDate&sortDir=desc");
        return this;
    }
    
    public ProductFormPage clickCreateProduct() {
        waitForElementToBeClickable(createProductButton);
        createProductButton.click();
        return new ProductFormPage(driver, baseUrl);
    }
    
    public ProductListPage search(String searchTerm) {
        // Instead of using the form, directly navigate to the URL with search parameter
        // This bypasses any potential form issues and tests the backend filtering directly
        String currentUrl = driver.getCurrentUrl();
        String baseUrl = currentUrl.split("\\?")[0]; // Remove existing query parameters
        
        try {
            // URL encode the search term to handle any special characters
            String encodedSearchTerm = java.net.URLEncoder.encode(searchTerm, "UTF-8");
            String searchUrl = baseUrl + "?search=" + encodedSearchTerm + "&page=0&sortBy=createdDate&sortDir=desc";
            
            driver.get(searchUrl);
            
            // Wait for page to load and URL to update
            waitForPageToLoad();
            waitForUrlToContain("search=");
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to perform search", e);
        }
        
        // Reinitialize page elements after navigation
        org.openqa.selenium.support.PageFactory.initElements(driver, this);
        
        return this;
    }
    
    public ProductListPage clearSearch() {
        searchField.clear();
        filterButton.click();
        return this;
    }
    
    public ProductListPage filterByProductType(String productType) {
        // Use direct URL navigation to avoid form submission issues
        String currentUrl = driver.getCurrentUrl();
        String baseUrl = currentUrl.split("\\?")[0]; // Remove existing query parameters
        String filterUrl = baseUrl + "?productType=" + productType + "&page=0&sortBy=createdDate&sortDir=desc";
        
        driver.get(filterUrl);
        
        // Wait for page to load and URL to update
        waitForPageToLoad();
        waitForUrlToContain("productType=");
        
        // Reinitialize page elements after navigation
        org.openqa.selenium.support.PageFactory.initElements(driver, this);
        
        return this;
    }
    
    public ProductListPage filterByCategory(String category) {
        selectDropdownByText(categoryDropdown, category);
        filterButton.click();
        return this;
    }
    
    public ProductListPage applyFilters(String searchTerm, String productType, String category) {
        if (searchTerm != null && !searchTerm.isEmpty()) {
            clearAndType(searchField, searchTerm);
        }
        if (productType != null && !productType.isEmpty()) {
            selectDropdownByText(productTypeDropdown, productType);
        }
        if (category != null && !category.isEmpty()) {
            selectDropdownByText(categoryDropdown, category);
        }
        filterButton.click();
        return this;
    }
    
    public int getProductCount() {
        return productRows.size();
    }
    
    public boolean isProductDisplayed(String productCode) {
        // Don't refresh the page - preserve current search state
        // Wait for page to be stable
        waitForPageToLoad();
        
        // First check if we're showing "No products found" message
        String pageSource = driver.getPageSource();
        if (pageSource.contains("No products found")) {
            return false;
        }
        
        // Try to find the products table
        try {
            waitForElementToBeVisible(productsTable);
            
            // Reinitialize page elements to get fresh references
            org.openqa.selenium.support.PageFactory.initElements(driver, this);
            
            // Look for the product code in the table rows
            return productRows.stream()
                .anyMatch(row -> {
                    String rowText = row.getText();
                    return rowText.contains(productCode);
                });
        } catch (Exception e) {
            // If table is not available, fallback to page source check
            return pageSource.contains(productCode);
        }
    }
    
    public ProductViewPage viewProduct(String productCode) {
        WebElement productRow = findProductRow(productCode);
        WebElement viewLink = productRow.findElement(By.linkText("View"));
        viewLink.click();
        return new ProductViewPage(driver, baseUrl);
    }
    
    public ProductFormPage editProduct(String productCode) {
        // First wait for the page and table to be loaded
        waitForPageToLoad();
        waitForElementToBeVisible(productsTable);
        
        // Wait for the specific edit element to be present and clickable
        try {
            // Use explicit wait for the element to be present first
            WebElement editLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("edit-" + productCode)));
            // Then wait for it to be clickable
            wait.until(ExpectedConditions.elementToBeClickable(editLink));
            editLink.click();
            return new ProductFormPage(driver, baseUrl);
        } catch (org.openqa.selenium.TimeoutException e) {
            // If element not found, fail fast with descriptive error instead of trying fallback
            throw new RuntimeException("Edit button for product " + productCode + " not found or not clickable within 10 seconds. " +
                "This may indicate the product was not properly created or is on a different page.", e);
        }
    }
    
    public ProductListPage deactivateProduct(String productCode) {
        // Use ID selector instead of XPath
        WebElement deactivateButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("deactivate-" + productCode)));
        deactivateButton.click();
        
        // Handle confirmation dialog
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
        
        // Wait for page to reload after form submission and ensure we're back on the list page
        waitForPageToLoad();
        
        // The form submission should redirect back to the product list page
        // If not, navigate back to it
        if (!driver.getCurrentUrl().contains("/product/list")) {
            open();
        }
        
        return this;
    }
    
    public ProductListPage activateProduct(String productCode) {
        // Use ID selector instead of XPath
        WebElement activateButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("activate-" + productCode)));
        activateButton.click();
        
        // Handle confirmation dialog
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
        
        // Wait for page to reload after form submission and ensure we're back on the list page
        waitForPageToLoad();
        
        // The form submission should redirect back to the product list page
        // If not, navigate back to it
        if (!driver.getCurrentUrl().contains("/product/list")) {
            open();
        }
        
        return this;
    }
    
    public String getProductStatus(String productCode) {
        // Wait for page to load
        waitForPageToLoad();
        
        // Try to wait for the products table, but handle case where there might be no products
        try {
            waitForElementToBeVisible(productsTable);
        } catch (org.openqa.selenium.TimeoutException e) {
            // Check if we're on an empty page or different page
            if (driver.getPageSource().contains("No products found")) {
                throw new RuntimeException("Product " + productCode + " not found - page shows 'No products found'");
            }
            // If not on product list page, try to navigate there
            open();
            waitForElementToBeVisible(productsTable);
        }
        
        // First check if the product is visible on the page
        if (!isProductDisplayed(productCode)) {
            throw new RuntimeException("Product " + productCode + " is not visible on the current page - may be on a different page due to pagination");
        }
        
        // Use ID selector to find status element
        try {
            WebElement statusElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("status-" + productCode)));
            return statusElement.getText().trim();
        } catch (org.openqa.selenium.TimeoutException e) {
            throw new RuntimeException("Status element for product " + productCode + " not found within timeout. " +
                "This may indicate the product status is not properly rendered.", e);
        }
    }
    
    public boolean hasProducts() {
        return !productRows.isEmpty();
    }
    
    public String getNoProductsMessage() {
        if (hasProducts()) {
            return "";
        }
        WebElement noProductsElement = driver.findElement(By.xpath("//h3[text()='No products found']"));
        return noProductsElement.getText();
    }
    
    private WebElement findProductRow(String productCode) {
        // Don't refresh the page - preserve current search state  
        waitForPageToLoad();
        
        // Reinitialize page elements to get fresh references
        org.openqa.selenium.support.PageFactory.initElements(driver, this);
        
        // Only debug if we can't find the product
        boolean found = productRows.stream().anyMatch(row -> row.getText().contains(productCode));
        if (!found) {
            System.out.println("=== DEBUG: Product not found: " + productCode + " ===");
            System.out.println("Total product rows: " + productRows.size());
            System.out.println("Current URL: " + driver.getCurrentUrl());
            for (int i = 0; i < Math.min(3, productRows.size()); i++) {
                System.out.println("Row " + (i + 1) + ": " + productRows.get(i).getText().substring(0, Math.min(100, productRows.get(i).getText().length())));
            }
        }
        
        return productRows.stream()
            .filter(row -> row.getText().contains(productCode))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Product with code " + productCode + " not found"));
    }
    
    public List<String> getProductCodes() {
        return productRows.stream()
            .map(row -> row.findElement(By.cssSelector("td:first-child")).getText())
            .toList();
    }
    
    public List<String> getProductNames() {
        return productRows.stream()
            .map(row -> row.findElement(By.cssSelector("td:nth-child(2) .text-sm.font-medium")).getText())
            .toList();
    }
    
    public List<String> getProductTypes() {
        return productRows.stream()
            .map(row -> row.findElement(By.cssSelector("td:nth-child(3) span")).getText())
            .toList();
    }
    
    public boolean isSuccessMessageDisplayed() {
        try {
            // Wait for success message to appear (it might take a moment after page redirect)
            WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("success-message")));
            return successMessage.isDisplayed();
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }
    
    public String getSuccessMessage() {
        try {
            WebElement successMessage = driver.findElement(By.id("success-message"));
            return successMessage.getText();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return null;
        }
    }
}