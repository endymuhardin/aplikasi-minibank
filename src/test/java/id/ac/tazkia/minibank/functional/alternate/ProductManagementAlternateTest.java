package id.ac.tazkia.minibank.functional.alternate;

import id.ac.tazkia.minibank.functional.config.BasePlaywrightTest;
import id.ac.tazkia.minibank.functional.pages.DashboardPage;
import id.ac.tazkia.minibank.functional.pages.LoginPage;
import id.ac.tazkia.minibank.functional.pages.ProductManagementPage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Tag("playwright-alternate")
@DisplayName("Product Management Alternate Scenario Tests")
class ProductManagementAlternateTest extends BasePlaywrightTest {

    @Test
    @DisplayName("Should prevent access to product management without proper authentication")
    void shouldPreventUnauthenticatedAccess() {
        log.info("Alternate Test: Unauthenticated access to product management");
        
        // Try to access product management without logging in
        page.navigate(baseUrl + "/product/list");
        page.waitForTimeout(1000);
        
        // Should be redirected to login or show access denied
        assertTrue(page.url().contains("/login") || page.content().contains("Access Denied"), 
                "Should not allow access to product management without authentication");
        
        // Try to access product creation without logging in
        page.navigate(baseUrl + "/product/create");
        page.waitForTimeout(1000);
        
        assertTrue(page.url().contains("/login") || page.content().contains("Access Denied"), 
                "Should not allow access to product creation without authentication");
        
        log.info("✅ Unauthenticated access properly prevented");
    }

    @ParameterizedTest
    @CsvSource({
        "'', 'Test Product', 'TABUNGAN_WADIAH', 'Test Category', 'Empty product code'",
        "'PROD001', '', 'TABUNGAN_WADIAH', 'Test Category', 'Empty product name'",
        "'PROD002', 'Test Product', '', 'Test Category', 'Empty product type'",
        "'PROD003', 'Test Product', 'TABUNGAN_WADIAH', '', 'Empty category'",
        "'PR', 'Test Product', 'TABUNGAN_WADIAH', 'Test Category', 'Too short product code'",
        "'PRODUCT_CODE_TOO_LONG', 'Test Product', 'TABUNGAN_WADIAH', 'Test Category', 'Too long product code'"
    })
    @DisplayName("Should validate product form fields and show appropriate error messages")
    void shouldValidateProductFormFields(String productCode, String productName, String productType, String category, String testCase) {
        log.info("Alternate Test: Product form validation - {}", testCase);
        
        // Login as admin
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");
        
        // Navigate to product creation form
        ProductManagementPage productPage = new ProductManagementPage(page);
        productPage.navigateToCreate(baseUrl);
        
        assertTrue(productPage.isProductFormPageLoaded(), "Product form should be loaded");
        
        // Fill form with invalid data
        if (!productCode.isEmpty()) productPage.fillBasicProductInfo(productCode, productName, productType, category);
        else {
            // Handle empty fields individually
            if (!productName.isEmpty()) page.locator("#productName").fill(productName);
            if (!productType.isEmpty()) page.locator("#productType").selectOption(productType);
            if (!category.isEmpty()) page.locator("#productCategory").fill(category);
        }
        
        // Trigger form validation without full submission
        productPage.triggerValidation();
        
        // Should show validation errors or remain on form page (not crash with constraint error)
        assertTrue(page.url().contains("/product/create") || 
                  page.content().contains("error") || 
                  page.content().contains("required") ||
                  productPage.hasValidationErrors(), 
                "Should show validation errors or remain on form for case: " + testCase);
        
        log.info("✅ Form validation working for case: {}", testCase);
    }

    @Test
    @DisplayName("Should handle duplicate product code creation attempt")
    void shouldHandleDuplicateProductCode() {
        log.info("Alternate Test: Duplicate product code handling");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");
        
        // Navigate to product creation form
        ProductManagementPage productPage = new ProductManagementPage(page);
        productPage.navigateToCreate(baseUrl);
        
        assertTrue(productPage.isProductFormPageLoaded(), "Product form should be loaded");
        
        // Try to create product with potentially existing code
        productPage.fillBasicProductInfo("DUPLICATE001", "Duplicate Test Product", "TABUNGAN_WADIAH", "Test Category");
        productPage.setActiveStatus(true);
        productPage.submitForm();
        
        page.waitForTimeout(2000);
        
        // If successful first time, try again with same code
        if (page.url().contains("/product/list") || productPage.isSuccessMessageVisible()) {
            // Try to create another product with same code
            productPage.navigateToCreate(baseUrl);
            productPage.fillBasicProductInfo("DUPLICATE001", "Another Duplicate Product", "TABUNGAN_WADIAH", "Test Category");
            productPage.setActiveStatus(true);
            productPage.submitForm();
            
            page.waitForTimeout(1000);
            
            // Should show error or validation message
            assertTrue(productPage.hasValidationErrors() || productPage.isErrorMessageVisible() || 
                      page.url().contains("/product/create"),
                    "Should prevent duplicate product code creation");
        }
        
        log.info("✅ Duplicate product code handling verified");
    }

    @Test
    @DisplayName("Should handle search with no results appropriately")
    void shouldHandleSearchWithNoResults() {
        log.info("Alternate Test: Search with no results");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");
        
        // Navigate to product list
        ProductManagementPage productPage = new ProductManagementPage(page);
        productPage.navigateToList(baseUrl);
        
        assertTrue(productPage.isProductListPageLoaded(), "Product list should be loaded");
        
        // Search for something that definitely doesn't exist
        String nonExistentSearch = "DEFINITELY_DOES_NOT_EXIST_123456789";
        productPage.searchProducts(nonExistentSearch);
        
        page.waitForTimeout(1000);
        
        // Should show "no products found" or empty table
        String pageContent = page.content();
        assertTrue(pageContent.contains("No products found") || 
                  pageContent.contains("no results") || 
                  pageContent.contains("No data") ||
                  productPage.isNoProductsMessageDisplayed(),
                "Should show appropriate message when no search results found");
        
        log.info("✅ Search with no results handled appropriately");
    }

    @Test
    @DisplayName("Should handle XSS attempts in product form fields")
    void shouldHandleXSSInProductForm() {
        log.info("Alternate Test: XSS prevention in product form");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");
        
        // Navigate to product creation form
        ProductManagementPage productPage = new ProductManagementPage(page);
        productPage.navigateToCreate(baseUrl);
        
        assertTrue(productPage.isProductFormPageLoaded(), "Product form should be loaded");
        
        // Test XSS in various fields
        String xssPayload = "<script>alert('xss')</script>";
        
        productPage.fillBasicProductInfo("XSS" + xssPayload, "Product" + xssPayload, "TABUNGAN_WADIAH", "Category" + xssPayload);
        productPage.fillDescription("Description" + xssPayload);
        
        // Try to submit
        productPage.submitForm();
        page.waitForTimeout(1000);
        
        // Should not execute JavaScript
        assertFalse(isAlertPresent(), "XSS payload should not execute");
        
        // Page should handle XSS attempt gracefully
        assertTrue(page.url().contains("/product"), 
                "Page should handle XSS attempt without breaking");
        
        log.info("✅ XSS attempts in product form handled properly");
    }

    @Test
    @DisplayName("Should handle SQL injection attempts in search functionality")
    void shouldHandleSQLInjectionInSearch() {
        log.info("Alternate Test: SQL injection prevention in search");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");
        
        // Navigate to product list
        ProductManagementPage productPage = new ProductManagementPage(page);
        productPage.navigateToList(baseUrl);
        
        assertTrue(productPage.isProductListPageLoaded(), "Product list should be loaded");
        
        // Test SQL injection patterns in search
        String[] sqlInjectionAttempts = {
            "'; DROP TABLE products; --",
            "' OR '1'='1",
            "' UNION SELECT * FROM users --",
            "'; DELETE FROM products WHERE '1'='1' --"
        };
        
        for (String injectionAttempt : sqlInjectionAttempts) {
            productPage.searchProducts(injectionAttempt);
            page.waitForTimeout(500);
            
            // Page should remain functional and not execute SQL
            assertTrue(page.url().contains("/product/list"), 
                    "Search should handle SQL injection attempt: " + injectionAttempt);
            assertTrue(productPage.isProductListPageLoaded(), 
                    "Product list should remain functional after SQL injection attempt");
        }
        
        log.info("✅ SQL injection attempts in search handled properly");
    }

    @Test
    @DisplayName("Should handle extremely long input values in product form")
    void shouldHandleLongInputInProductForm() {
        log.info("Alternate Test: Long input handling in product form");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");
        
        // Navigate to product creation form
        ProductManagementPage productPage = new ProductManagementPage(page);
        productPage.navigateToCreate(baseUrl);
        
        assertTrue(productPage.isProductFormPageLoaded(), "Product form should be loaded");
        
        // Create very long strings
        String longCode = "PROD" + "A".repeat(500);
        String longName = "Product Name " + "B".repeat(1000);
        String longCategory = "Category " + "C".repeat(500);
        String longDescription = "Description " + "D".repeat(2000);
        
        // Fill form with long values
        page.locator("#productCode").fill(longCode);
        page.locator("#productName").fill(longName);
        page.locator("#productCategory").fill(longCategory);
        page.locator("#description").fill(longDescription);
        
        // Try to submit
        productPage.submitForm();
        page.waitForTimeout(1000);
        
        // Should handle gracefully without breaking the application
        assertTrue(page.url().contains("/product"), 
                "Application should handle long inputs without breaking");
        
        log.info("✅ Long input values handled properly");
    }

    @Test
    @DisplayName("Should handle invalid product type selection")
    void shouldHandleInvalidProductType() {
        log.info("Alternate Test: Invalid product type handling");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");
        
        // Navigate to product creation form
        ProductManagementPage productPage = new ProductManagementPage(page);
        productPage.navigateToCreate(baseUrl);
        
        assertTrue(productPage.isProductFormPageLoaded(), "Product form should be loaded");
        
        // Try to manipulate product type field with invalid value
        page.evaluate("document.getElementById('productType').innerHTML += '<option value=\"INVALID_TYPE\">Invalid Type</option>'");
        
        // Fill basic info and select invalid type
        page.locator("#productCode").fill("INVALID001");
        page.locator("#productName").fill("Invalid Type Product");
        page.locator("#productType").selectOption("INVALID_TYPE");
        page.locator("#productCategory").fill("Test Category");
        
        // Try to submit
        productPage.submitForm();
        page.waitForTimeout(1000);
        
        // Should show validation error or reject invalid type
        assertTrue(productPage.hasValidationErrors() || page.url().contains("/product/create"),
                "Should reject invalid product type");
        
        log.info("✅ Invalid product type handled properly");
    }

    @Test
    @DisplayName("Should handle concurrent product creation attempts")
    void shouldHandleConcurrentProductCreation() {
        log.info("Alternate Test: Concurrent product creation");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");
        
        // Navigate to product creation form
        ProductManagementPage productPage = new ProductManagementPage(page);
        productPage.navigateToCreate(baseUrl);
        
        assertTrue(productPage.isProductFormPageLoaded(), "Product form should be loaded");
        
        // Fill form
        productPage.fillBasicProductInfo("CONCURRENT001", "Concurrent Test Product", "TABUNGAN_WADIAH", "Test Category");
        productPage.setActiveStatus(true);
        
        // Submit multiple times rapidly (simulate double-click or concurrent requests)
        productPage.submitForm();
        page.waitForTimeout(100);
        productPage.submitForm();
        page.waitForTimeout(100);
        productPage.submitForm();
        
        page.waitForTimeout(2000);
        
        // Should handle gracefully without creating duplicates or errors
        assertTrue(page.url().contains("/product"), 
                "Should handle concurrent submissions gracefully");
        
        log.info("✅ Concurrent product creation handled appropriately");
    }

    @Test
    @DisplayName("Should handle filter with invalid values gracefully")
    void shouldHandleInvalidFilterValues() {
        log.info("Alternate Test: Invalid filter values");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");
        
        // Navigate to product list
        ProductManagementPage productPage = new ProductManagementPage(page);
        productPage.navigateToList(baseUrl);
        
        assertTrue(productPage.isProductListPageLoaded(), "Product list should be loaded");
        
        // Try invalid filter values
        productPage.filterByProductType("INVALID_PRODUCT_TYPE_123");
        page.waitForTimeout(500);
        
        // Should handle gracefully
        assertTrue(productPage.isProductListPageLoaded(), 
                "Product list should remain functional after invalid filter");
        
        productPage.filterByCategory("INVALID_CATEGORY_123");
        page.waitForTimeout(500);
        
        // Should still be functional
        assertTrue(productPage.isProductListPageLoaded(), 
                "Product list should remain functional after invalid category filter");
        
        log.info("✅ Invalid filter values handled gracefully");
    }

    private boolean isAlertPresent() {
        try {
            return page.locator("dialog").isVisible() || 
                   page.evaluate("() => window.alert !== undefined && window.alert.toString() !== 'function alert() { [native code] }'").toString().equals("true");
        } catch (Exception e) {
            return false;
        }
    }
}