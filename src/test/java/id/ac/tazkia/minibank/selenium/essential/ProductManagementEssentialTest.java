package id.ac.tazkia.minibank.selenium.essential;

import id.ac.tazkia.minibank.config.BaseSeleniumTest;
import id.ac.tazkia.minibank.selenium.pages.DashboardPage;
import id.ac.tazkia.minibank.selenium.pages.LoginPage;
import id.ac.tazkia.minibank.selenium.pages.ProductManagementPage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Tag("essential")
@DisplayName("Product Management Essential Tests")
class ProductManagementEssentialTest extends BaseSeleniumTest {

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/selenium/essential/login-credentials-essential.csv", numLinesToSkip = 1)
    @DisplayName("Should load product management page successfully for all user roles")
    void shouldLoadProductManagementPageForAllRoles(String username, String password, String expectedRole, String roleDescription) {
        log.info("Essential Test: Product management page access for {}: {} with role {}", roleDescription, username, expectedRole);
        
        // Login first
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith(username, password);
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to product management
        ProductManagementPage productPage = new ProductManagementPage(driver);
        productPage.navigateToList(baseUrl);
        
        // Verify product management page loads successfully
        assertTrue(productPage.isProductListPageLoaded(), 
                "Product management page should load successfully for " + roleDescription);
        
        // Verify essential page elements are visible (if user has appropriate permissions)
        if (expectedRole.equals("BRANCH_MANAGER")) {
            assertTrue(productPage.isCreateProductButtonVisible(), 
                    "Create product button should be visible for " + roleDescription);
        }
        
        log.info("✅ Product management page loaded successfully for {}", roleDescription);
    }

    @Test
    @DisplayName("Should display product creation form correctly")
    void shouldDisplayProductCreationFormCorrectly() {
        log.info("Essential Test: Product creation form display");
        
        // Login as admin (has full access)
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to product creation form
        ProductManagementPage productPage = new ProductManagementPage(driver);
        productPage.navigateToCreate(baseUrl);
        
        // Verify product form page loads
        assertTrue(productPage.isProductFormPageLoaded(), 
                "Product creation form should load correctly");
        
        // Verify essential form elements are present
        assertTrue(driver.getPageSource().contains("Product Code"), 
                "Product Code field should be visible");
        assertTrue(driver.getPageSource().contains("Product Name"), 
                "Product Name field should be visible");
        assertTrue(driver.getPageSource().contains("Product Type"), 
                "Product Type field should be visible");
        assertTrue(driver.getPageSource().contains("Category"), 
                "Category field should be visible");
        
        log.info("✅ Product creation form displayed correctly");
    }

    @Test
    @DisplayName("Should navigate from product list to creation form successfully")
    void shouldNavigateFromListToCreationForm() {
        log.info("Essential Test: Navigation from product list to creation form");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to product list
        ProductManagementPage productPage = new ProductManagementPage(driver);
        productPage.navigateToList(baseUrl);
        
        assertTrue(productPage.isProductListPageLoaded(), "Product list page should be loaded");
        assertTrue(productPage.isCreateProductButtonVisible(), "Create product button should be visible");
        
        // Click create product button
        productPage.clickCreateProduct();
        
        // Verify navigation to creation form
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/product/create"), 
                "Should navigate to product creation form");
        
        log.info("✅ Navigation from list to creation form working");
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/selenium/essential/product-filter-essential.csv", numLinesToSkip = 1)
    @DisplayName("Should perform product search and filtering operations")
    void shouldPerformProductSearchAndFiltering(String username, String password, String role, String roleDescription, 
                                               String filterType, String filterValue) {
        log.info("Essential Test: Product search/filtering for {}: {} with filter type '{}'", roleDescription, role, filterType);
        
        // Login first
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith(username, password);
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to product management
        ProductManagementPage productPage = new ProductManagementPage(driver);
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
            
            // Give time for filter to process
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            log.info("Verifying product list display without filter");
        }
        
        // Verify page remains accessible after search/filter (basic functionality check)
        assertTrue(driver.getCurrentUrl().contains("/product/list"), 
                "Should remain on product list page after filter operations");
        assertTrue(productPage.isProductListPageLoaded(), 
                "Page should remain functional after operations");
        
        log.info("✅ Product search/filtering operations working for {}", roleDescription);
    }

    @Test
    @DisplayName("Should display product list with essential elements")
    void shouldDisplayProductListWithEssentialElements() {
        log.info("Essential Test: Product list essential elements display");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to product management
        ProductManagementPage productPage = new ProductManagementPage(driver);
        productPage.navigateToList(baseUrl);
        
        assertTrue(productPage.isProductListPageLoaded(), "Product management page should be loaded");
        
        // Verify essential table elements are present
        assertTrue(driver.getPageSource().contains("Code"), 
                "Product table should have Code column");
        assertTrue(driver.getPageSource().contains("Name"), 
                "Product table should have Name column");
        assertTrue(driver.getPageSource().contains("Type"), 
                "Product table should have Type column");
        assertTrue(driver.getPageSource().contains("Category"), 
                "Product table should have Category column");
        assertTrue(driver.getPageSource().contains("Status"), 
                "Product table should have Status column");
        
        // Verify search and filter elements
        assertTrue(driver.getPageSource().contains("Search"), 
                "Search field should be visible");
        assertTrue(driver.getPageSource().contains("All Types"), 
                "Product type filter should be visible");
        
        log.info("✅ Product list essential elements displayed correctly");
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/selenium/essential/product-test-essential.csv", numLinesToSkip = 1)
    @DisplayName("Should fill product form with basic information successfully")
    void shouldFillProductFormWithBasicInfo(String productCode, String productName, String productType, String productCategory, String description) {
        log.info("Essential Test: Product form basic information filling for product: {}", productCode);
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to product creation form
        ProductManagementPage productPage = new ProductManagementPage(driver);
        productPage.navigateToCreate(baseUrl);
        
        assertTrue(productPage.isProductFormPageLoaded(), "Product form should be loaded");
        
        // Fill basic product information
        productPage.fillBasicProductInfo(productCode, productName, productType, productCategory);
        productPage.fillDescription(description);
        productPage.setActiveStatus(true);
        
        // Verify form was filled correctly (basic check)
        assertTrue(driver.getPageSource().contains(productCode) || 
                  driver.findElement(org.openqa.selenium.By.id("productCode")).getAttribute("value").equals(productCode),
                "Product code should be filled in the form");
        
        log.info("✅ Product form filled successfully with basic information for {}", productCode);
    }

    @Test
    @DisplayName("Should handle multi-step form navigation correctly")
    void shouldHandleMultiStepFormNavigation() {
        log.info("Essential Test: Multi-step form navigation");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to product creation form
        ProductManagementPage productPage = new ProductManagementPage(driver);
        productPage.navigateToCreate(baseUrl);
        
        assertTrue(productPage.isProductFormPageLoaded(), "Product form should be loaded");
        
        // Fill required basic information
        productPage.fillBasicProductInfo("NAV001", "Navigation Test Product", "TABUNGAN_WADIAH", "Test Category");
        productPage.setActiveStatus(true);
        
        // Navigate to next step
        productPage.clickNextStep();
        
        // Give time for step transition
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify step 2 is visible or accessible
        boolean step2Accessible = productPage.isStep2Visible() || 
                                driver.getPageSource().contains("Islamic Banking") ||
                                driver.getPageSource().contains("step-2");
        
        assertTrue(step2Accessible, "Should be able to navigate to step 2 of the form");
        
        log.info("✅ Multi-step form navigation working");
    }

    @Test
    @DisplayName("Should handle product management page refresh correctly")
    void shouldHandleProductManagementPageRefreshCorrectly() {
        log.info("Essential Test: Product management page refresh handling");
        
        // Login first
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to product management
        ProductManagementPage productPage = new ProductManagementPage(driver);
        productPage.navigateToList(baseUrl);
        
        assertTrue(productPage.isProductListPageLoaded(), 
                "Product management page should be loaded initially");
        
        // Refresh the page
        driver.navigate().refresh();
        
        // Verify page still loads correctly after refresh
        productPage.waitForListPageLoad();
        assertTrue(productPage.isProductListPageLoaded(), 
                "Product management page should load correctly after refresh");
        assertTrue(productPage.isCreateProductButtonVisible(), 
                "Create product button should remain visible after refresh");
        
        log.info("✅ Product management page refresh handling verified");
    }

    @Test
    @DisplayName("Should navigate between dashboard and product management successfully")
    void shouldNavigateBetweenDashboardAndProductManagement() {
        log.info("Essential Test: Navigation between dashboard and product management");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to product management
        ProductManagementPage productPage = new ProductManagementPage(driver);
        productPage.navigateToList(baseUrl);
        
        assertTrue(productPage.isProductListPageLoaded(), 
                "Should be able to navigate to product management");
        
        // Navigate back to dashboard
        driver.get(baseUrl + "/dashboard");
        dashboardPage.waitForPageLoad();
        
        assertTrue(dashboardPage.isDashboardLoaded(), 
                "Should be able to navigate back to dashboard");
        
        // Navigate to product management again
        productPage.navigateToList(baseUrl);
        assertTrue(productPage.isProductListPageLoaded(), 
                "Should be able to navigate to product management again");
        
        log.info("✅ Navigation between dashboard and product management verified");
    }

    @Test
    @DisplayName("Should display search and filter interface correctly")
    void shouldDisplaySearchAndFilterInterface() {
        log.info("Essential Test: Search and filter interface display");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to product management
        ProductManagementPage productPage = new ProductManagementPage(driver);
        productPage.navigateToList(baseUrl);
        
        assertTrue(productPage.isProductListPageLoaded(), "Product management page should be loaded");
        
        // Verify search and filter interface elements are present
        assertTrue(driver.getPageSource().contains("Product code, name, or description"), 
                "Search field placeholder should be visible");
        assertTrue(driver.getPageSource().contains("Filter"), 
                "Filter button should be visible");
        assertTrue(driver.getPageSource().contains("All Types"), 
                "Product type filter should have 'All Types' option");
        assertTrue(driver.getPageSource().contains("All Categories"), 
                "Category filter should have 'All Categories' option");
        
        log.info("✅ Search and filter interface displayed correctly");
    }
}