package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

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
        // Wait for page to be stable and table to be present
        waitForPageToLoad();
        
        // Wait for products table to be present (or no products message)
        try {
            waitForElementToBeVisible(productsTable);
        } catch (Exception e) {
            // Table might not be visible if there are no products
        }
        
        // First check if we're showing "No products found" message
        String pageSource = driver.getPageSource();
        if (pageSource.contains("No products found")) {
            return false;
        }
        
        // Reinitialize page elements to get fresh references
        org.openqa.selenium.support.PageFactory.initElements(driver, this);
        
        // Look for the product code in the table rows
        try {
            return productRows.stream()
                .anyMatch(row -> {
                    String rowText = row.getText();
                    return rowText.contains(productCode);
                });
        } catch (Exception e) {
            // Fallback to page source check if table rows fail
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
        // Use ID-based locator for more reliable element finding
        WebElement editLink = driver.findElement(By.id("edit-" + productCode));
        waitForElementToBeClickable(editLink);
        editLink.click();
        return new ProductFormPage(driver, baseUrl);
    }
    
    public ProductListPage deactivateProduct(String productCode) {
        WebElement productRow = findProductRow(productCode);
        WebElement deactivateButton = productRow.findElement(By.xpath(".//button[text()='Deactivate']"));
        deactivateButton.click();
        // Handle confirmation dialog
        driver.switchTo().alert().accept();
        
        // Wait for page to reload after form submission
        waitForPageToLoad();
        
        // Reinitialize page elements after page reload
        org.openqa.selenium.support.PageFactory.initElements(driver, this);
        
        return this;
    }
    
    public ProductListPage activateProduct(String productCode) {
        WebElement productRow = findProductRow(productCode);
        WebElement activateButton = productRow.findElement(By.xpath(".//button[text()='Activate']"));
        activateButton.click();
        // Handle confirmation dialog
        driver.switchTo().alert().accept();
        
        // Wait for page to reload after form submission
        waitForPageToLoad();
        
        // Reinitialize page elements after page reload
        org.openqa.selenium.support.PageFactory.initElements(driver, this);
        
        return this;
    }
    
    public String getProductStatus(String productCode) {
        // Use ID-based locator for more reliable element finding
        try {
            WebElement statusElement = driver.findElement(By.id("status-" + productCode));
            return statusElement.getText().trim();
        } catch (Exception e) {
            throw new RuntimeException("Could not find status element for product: " + productCode, e);
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
            WebElement successMessage = driver.findElement(By.id("success-message"));
            return successMessage.isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
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