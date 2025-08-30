package id.ac.tazkia.minibank.functional.success;

import id.ac.tazkia.minibank.functional.config.BasePlaywrightTest;
import id.ac.tazkia.minibank.functional.pages.DashboardPage;
import id.ac.tazkia.minibank.functional.pages.LoginPage;
import id.ac.tazkia.minibank.functional.pages.ProductManagementPage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Tag("playwright-success")
@DisplayName("Product Management Success Scenario Tests")
class ProductManagementSuccessTest extends BasePlaywrightTest {

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/functional/login-credentials-essential.csv", numLinesToSkip = 1)
    @DisplayName("Should successfully load product management page for all authorized user roles")
    void shouldLoadProductManagementPageForAllRoles(String username, String password, String expectedRole, String roleDescription) {
        log.info("Success Test: Product management page access for {}: {} with role {}", roleDescription, username, expectedRole);
        
        // Login first
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith(username, password);
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded successfully");
        
        // Navigate to product management
        ProductManagementPage productPage = new ProductManagementPage(page);
        productPage.navigateToList(baseUrl);
        
        // Verify product management page loads successfully
        assertTrue(productPage.isProductListPageLoaded(), 
                "Product management page should load successfully for " + roleDescription);
        
        // Verify appropriate UI elements are visible based on role
        if (expectedRole.equals("BRANCH_MANAGER")) {
            assertTrue(productPage.isCreateProductButtonVisible(), 
                    "Create product button should be visible for " + roleDescription);
        }
        
        log.info("✅ Product management page loaded successfully for {}", roleDescription);
    }

    @Test
    @DisplayName("Should successfully display product creation form with all required fields")
    void shouldDisplayProductCreationFormCorrectly() {
        log.info("Success Test: Product creation form display");
        
        // Login as admin (has full access)
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded successfully");
        
        // Navigate to product creation form
        ProductManagementPage productPage = new ProductManagementPage(page);
        productPage.navigateToCreate(baseUrl);
        
        // Verify product form page loads
        assertTrue(productPage.isProductFormPageLoaded(), 
                "Product creation form should load correctly");
        
        // Verify essential form elements are present
        assertTrue(page.content().contains("Product Code"), 
                "Product Code field should be visible");
        assertTrue(page.content().contains("Product Name"), 
                "Product Name field should be visible");
        assertTrue(page.content().contains("Product Type"), 
                "Product Type field should be visible");
        assertTrue(page.content().contains("Category"), 
                "Category field should be visible");
        
        log.info("✅ Product creation form displayed correctly");
    }

    @Test
    @DisplayName("Should successfully navigate from product list to creation form")
    void shouldNavigateFromListToCreationForm() {
        log.info("Success Test: Navigation from product list to creation form");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded successfully");
        
        // Navigate to product list
        ProductManagementPage productPage = new ProductManagementPage(page);
        productPage.navigateToList(baseUrl);
        
        assertTrue(productPage.isProductListPageLoaded(), "Product list page should be loaded");
        assertTrue(productPage.isCreateProductButtonVisible(), "Create product button should be visible");
        
        // Click create product button
        productPage.clickCreateProduct();
        
        // Wait for navigation and verify creation form
        page.waitForURL("**/product/create");
        assertTrue(page.url().contains("/product/create"), 
                "Should navigate to product creation form");
        
        log.info("✅ Navigation from list to creation form successful");
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/functional/product-filter-essential.csv", numLinesToSkip = 1)
    @DisplayName("Should successfully perform product search and filtering operations")
    void shouldPerformProductSearchAndFiltering(String username, String password, String role, String roleDescription, 
                                               String filterType, String filterValue) {
        log.info("Success Test: Product search/filtering for {}: {} with filter type '{}'", roleDescription, role, filterType);
        
        // Login first
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith(username, password);
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded successfully");
        
        // Navigate to product management
        ProductManagementPage productPage = new ProductManagementPage(page);
        productPage.navigateToList(baseUrl);
        
        assertTrue(productPage.isProductListPageLoaded(), "Product management page should be loaded");
        
        // Perform search/filter operation based on test data
        if (filterType != null && !filterType.isEmpty() && filterValue != null && !filterValue.isEmpty()) {
            switch (filterType) {
                case "search":
                    productPage.searchProducts(filterValue);
                    log.info("Performed product search with term: {}", filterValue);
                    break;
                case "type":
                    productPage.filterByProductType(filterValue);
                    log.info("Performed product type filter with: {}", filterValue);
                    break;
                case "category":
                    productPage.filterByCategory(filterValue);
                    log.info("Performed product category filter with: {}", filterValue);
                    break;
            }
            
            // Wait for filter results to load
            page.waitForTimeout(1000);
        } else {
            log.info("Verifying product list display without filter");
        }
        
        // Verify page remains accessible after search/filter (basic functionality check)
        assertTrue(page.url().contains("/product/list"), 
                "Should remain on product list page after filter operations");
        assertTrue(productPage.isProductListPageLoaded(), 
                "Page should remain functional after operations");
        
        log.info("✅ Product search/filtering operations successful for {}", roleDescription);
    }

    @Test
    @DisplayName("Should successfully display product list with all essential table elements")
    void shouldDisplayProductListWithEssentialElements() {
        log.info("Success Test: Product list essential elements display");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded successfully");
        
        // Navigate to product management
        ProductManagementPage productPage = new ProductManagementPage(page);
        productPage.navigateToList(baseUrl);
        
        assertTrue(productPage.isProductListPageLoaded(), "Product management page should be loaded");
        
        // Verify essential table elements are present
        String pageContent = page.content();
        assertTrue(pageContent.contains("Code"), 
                "Product table should have Code column");
        assertTrue(pageContent.contains("Name"), 
                "Product table should have Name column");
        assertTrue(pageContent.contains("Type"), 
                "Product table should have Type column");
        assertTrue(pageContent.contains("Category"), 
                "Product table should have Category column");
        assertTrue(pageContent.contains("Status"), 
                "Product table should have Status column");
        
        // Verify search and filter elements
        assertTrue(pageContent.contains("Search"), 
                "Search field should be visible");
        assertTrue(pageContent.contains("All Types"), 
                "Product type filter should be visible");
        
        log.info("✅ Product list essential elements displayed correctly");
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/functional/product-test-essential.csv", numLinesToSkip = 1)
    @DisplayName("Should successfully fill product form with basic information")
    void shouldFillProductFormWithBasicInfo(String productCode, String productName, String productType, String productCategory, String description) {
        log.info("Success Test: Product form basic information filling for product: {}", productCode);
        
        // Login as admin
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded successfully");
        
        // Navigate to product creation form
        ProductManagementPage productPage = new ProductManagementPage(page);
        productPage.navigateToCreate(baseUrl);
        
        assertTrue(productPage.isProductFormPageLoaded(), "Product form should be loaded");
        
        // Fill basic product information
        productPage.fillBasicProductInfo(productCode, productName, productType, productCategory);
        productPage.fillDescription(description);
        productPage.setActiveStatus(true);
        
        // Verify form was filled correctly (basic check)
        String productCodeValue = page.locator("#productCode").inputValue();
        assertEquals(productCode, productCodeValue, "Product code should be filled correctly in the form");
        
        log.info("✅ Product form filled successfully with basic information for {}", productCode);
    }

    @Test
    @DisplayName("Should successfully handle multi-step form navigation")
    void shouldHandleMultiStepFormNavigation() {
        log.info("Success Test: Multi-step form navigation");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded successfully");
        
        // Navigate to product creation form
        ProductManagementPage productPage = new ProductManagementPage(page);
        productPage.navigateToCreate(baseUrl);
        
        assertTrue(productPage.isProductFormPageLoaded(), "Product form should be loaded");
        
        // Fill required basic information
        productPage.fillBasicProductInfo("NAV001", "Navigation Test Product", "TABUNGAN_WADIAH", "Test Category");
        productPage.setActiveStatus(true);
        
        // Navigate to next step
        productPage.clickNextStep();
        
        // Wait for step transition
        page.waitForTimeout(500);
        
        // Verify step 2 is visible or accessible
        boolean step2Accessible = productPage.isStep2Visible() || 
                                page.content().contains("Islamic Banking") ||
                                page.content().contains("step-2");
        
        assertTrue(step2Accessible, "Should be able to navigate to step 2 of the form");
        
        log.info("✅ Multi-step form navigation successful");
    }

    @Test
    @DisplayName("Should successfully handle page refresh while maintaining functionality")
    void shouldHandleProductManagementPageRefreshCorrectly() {
        log.info("Success Test: Product management page refresh handling");
        
        // Login first
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded successfully");
        
        // Navigate to product management
        ProductManagementPage productPage = new ProductManagementPage(page);
        productPage.navigateToList(baseUrl);
        
        assertTrue(productPage.isProductListPageLoaded(), 
                "Product management page should be loaded initially");
        
        // Refresh the page
        page.reload();
        
        // Verify page still loads correctly after refresh
        productPage.waitForListPageLoad();
        assertTrue(productPage.isProductListPageLoaded(), 
                "Product management page should load correctly after refresh");
        assertTrue(productPage.isCreateProductButtonVisible(), 
                "Create product button should remain visible after refresh");
        
        log.info("✅ Product management page refresh handling successful");
    }

    @Test
    @DisplayName("Should successfully navigate between dashboard and product management")
    void shouldNavigateBetweenDashboardAndProductManagement() {
        log.info("Success Test: Navigation between dashboard and product management");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded successfully");
        
        // Navigate to product management
        ProductManagementPage productPage = new ProductManagementPage(page);
        productPage.navigateToList(baseUrl);
        
        assertTrue(productPage.isProductListPageLoaded(), 
                "Should be able to navigate to product management");
        
        // Navigate back to dashboard
        dashboardPage.navigateTo(baseUrl);
        
        assertTrue(dashboardPage.isDashboardLoaded(), 
                "Should be able to navigate back to dashboard");
        
        // Navigate to product management again
        productPage.navigateToList(baseUrl);
        assertTrue(productPage.isProductListPageLoaded(), 
                "Should be able to navigate to product management again");
        
        log.info("✅ Navigation between dashboard and product management successful");
    }

    @Test
    @DisplayName("Should successfully display search and filter interface with all controls")
    void shouldDisplaySearchAndFilterInterface() {
        log.info("Success Test: Search and filter interface display");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded successfully");
        
        // Navigate to product management
        ProductManagementPage productPage = new ProductManagementPage(page);
        productPage.navigateToList(baseUrl);
        
        assertTrue(productPage.isProductListPageLoaded(), "Product management page should be loaded");
        
        // Verify search and filter interface elements are present
        String pageContent = page.content();
        assertTrue(pageContent.contains("Product code, name, or description"), 
                "Search field placeholder should be visible");
        assertTrue(pageContent.contains("Filter"), 
                "Filter button should be visible");
        assertTrue(pageContent.contains("All Types"), 
                "Product type filter should have 'All Types' option");
        assertTrue(pageContent.contains("All Categories"), 
                "Category filter should have 'All Categories' option");
        
        log.info("✅ Search and filter interface displayed correctly");
    }
}