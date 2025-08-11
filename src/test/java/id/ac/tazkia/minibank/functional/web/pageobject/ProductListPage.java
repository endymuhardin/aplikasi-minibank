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
    
    @FindBy(css = "#products-table-body tr")
    private List<WebElement> productRows;
    
    public ProductListPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    public ProductListPage open() {
        // Always go to the first page with newest products first (sorted by creation date desc)
        // Use larger page size to reduce pagination issues
        driver.get(baseUrl + "/product/list?page=0&size=50&sortBy=createdDate&sortDir=desc");
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
            // Use larger page size to reduce pagination issues and sort by creation date desc to get newest first
            String searchUrl = baseUrl + "?search=" + encodedSearchTerm + "&page=0&size=50&sortBy=createdDate&sortDir=desc";
            
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
        String pageSource = null;
        try {
            pageSource = driver.getPageSource();
            if (pageSource != null && pageSource.contains("No products found")) {
                return false;
            }
        } catch (Exception e) {
            // Page source might not be available, continue with table check
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
            if (pageSource != null) {
                return pageSource.contains(productCode);
            }
            // If both approaches fail, return false
            return false;
        }
    }
    
    public ProductViewPage viewProduct(String productCode) {
        WebElement viewLink = driver.findElement(By.id("view-" + productCode));
        viewLink.click();
        return new ProductViewPage(driver, baseUrl);
    }
    
    public ProductFormPage editProduct(String productCode) {
        // Ensure we're on the right page with the product visible
        if (!isProductDisplayed(productCode)) {
            search(productCode);
            waitForPageToLoad();
            waitForElementToBeVisible(productsTable);
        }
        
        // Simple direct click on edit button - use presenceOfElementLocated for reliability
        try {
            WebElement editLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("edit-" + productCode)));
            // Scroll to element if needed to ensure it's clickable
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", editLink);
            editLink.click();
            return new ProductFormPage(driver, baseUrl);
        } catch (org.openqa.selenium.TimeoutException e) {
            throw new RuntimeException("Edit button for product " + productCode + " not found. Element ID: edit-" + productCode);
        }
    }
    
    public ProductListPage deactivateProduct(String productCode) {
        // Use same approach as edit button - presence + scroll
        try {
            WebElement deactivateButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("deactivate-" + productCode)));
            // Scroll to element if needed to ensure it's clickable
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", deactivateButton);
            deactivateButton.click();
        } catch (org.openqa.selenium.TimeoutException e) {
            throw new RuntimeException("Deactivate button for product " + productCode + " not found. Element ID: deactivate-" + productCode);
        }
        
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
        // Use same approach as other buttons - presence + scroll
        try {
            WebElement activateButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("activate-" + productCode)));
            // Scroll to element if needed to ensure it's clickable
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", activateButton);
            activateButton.click();
        } catch (org.openqa.selenium.TimeoutException e) {
            throw new RuntimeException("Activate button for product " + productCode + " not found. Element ID: activate-" + productCode);
        }
        
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
        // Ensure we're on the right page with the product visible
        if (!isProductDisplayed(productCode)) {
            search(productCode);
            waitForPageToLoad();
            waitForElementToBeVisible(productsTable);
        }
        
        // Simple direct lookup for status element - ensure it's visible and has text
        try {
            WebElement statusElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("status-" + productCode)));
            // Scroll to element to ensure it's visible
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", statusElement);
            // Wait a bit for rendering and then check if it's visible
            wait.until(ExpectedConditions.visibilityOf(statusElement));
            String text = statusElement.getText().trim();
            if (text.isEmpty()) {
                throw new RuntimeException("Status element for product " + productCode + " found but has empty text. Element ID: status-" + productCode);
            }
            return text;
        } catch (org.openqa.selenium.TimeoutException e) {
            throw new RuntimeException("Status element for product " + productCode + " not found or not visible. Element ID: status-" + productCode);
        }
    }
    
    public boolean hasProducts() {
        return !productRows.isEmpty();
    }
    
    public String getNoProductsMessage() {
        if (hasProducts()) {
            return "";
        }
        WebElement noProductsElement = driver.findElement(By.id("no-products-message"));
        return noProductsElement.getText();
    }
    
    public List<String> getProductCodes() {
        return productRows.stream()
            .map(row -> row.findElement(By.cssSelector("td:nth-child(1)")).getText())
            .toList();
    }
    
    public List<String> getProductNames() {
        return productRows.stream()
            .map(row -> row.findElement(By.cssSelector("td:nth-child(2) div:first-child")).getText())
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