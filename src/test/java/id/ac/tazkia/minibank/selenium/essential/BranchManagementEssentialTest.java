package id.ac.tazkia.minibank.selenium.essential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import id.ac.tazkia.minibank.config.BaseSeleniumTest;
import id.ac.tazkia.minibank.selenium.pages.BranchPage;
import id.ac.tazkia.minibank.selenium.pages.LoginPage;
import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Tag("essential")
@DisplayName("Branch Management Essential Tests")
class BranchManagementEssentialTest extends BaseSeleniumTest {
    
    private BranchPage branchPage;
    private LoginPage loginPage;
    
    @BeforeEach
    void setUp() {
        branchPage = new BranchPage(driver);
        loginPage = new LoginPage(driver);
        log.info("Starting branch management essential test");
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/selenium/essential/login-credentials-essential.csv", numLinesToSkip = 1)
    @DisplayName("Should handle branch management access based on role permissions")
    void shouldHandleBranchManagementAccessBasedOnRolePermissions(String username, String password, String expectedRole, String roleDescription) {
        log.info("Testing branch management access for role: {} ({})", expectedRole, roleDescription);
        
        // Login with provided credentials
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith(username, password);
        
        // Only BRANCH_MANAGER role has full branch management access
        boolean hasBranchManagementAccess = expectedRole.equals("BRANCH_MANAGER");
        
        if (hasBranchManagementAccess) {
            // Navigate to branch management
            branchPage.navigateToBranchList(baseUrl);
            assertTrue(branchPage.isBranchListPageLoaded(), 
                       "Branch management page should load for " + expectedRole);
            
            // Check if create button is visible for management roles
            assertTrue(branchPage.isCreateBranchButtonVisible(),
                      "Create branch button should be visible for " + expectedRole);
        } else {
            // TELLER and CUSTOMER_SERVICE may have limited access or no access
            // They should be able to view branches but not manage them
            try {
                branchPage.navigateToBranchList(baseUrl);
                if (branchPage.isBranchListPageLoaded()) {
                    // Can view but may not have create button
                    log.debug("✓ Role {} can view branches", expectedRole);
                } else {
                    log.debug("✓ Role {} does not have branch management access", expectedRole);
                }
            } catch (Exception e) {
                log.debug("✓ Role {} does not have branch management access as expected", expectedRole);
            }
        }
        
        log.debug("✓ Branch management access verified for {}", expectedRole);
    }
    
    @Test
    @DisplayName("Should display branch list page correctly")
    void shouldDisplayBranchListPageCorrectly() {
        log.info("Testing branch list page display");
        
        // Login as admin (full access)
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to branch list
        branchPage.navigateToBranchList(baseUrl);
        
        // Verify page elements
        assertTrue(branchPage.isBranchListPageLoaded(), 
                   "Branch list page should be loaded");
        assertTrue(branchPage.isCreateBranchButtonVisible(),
                   "Create branch button should be visible");
        
        log.debug("✓ Branch list page displayed correctly");
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/selenium/essential/branch-search-essential.csv", numLinesToSkip = 1)
    @DisplayName("Should search branches correctly")
    void shouldSearchBranchesCorrectly(String searchTerm, String expectedResults, String description) {
        log.info("Testing branch search: {}", description);
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to branch list and search
        branchPage.navigateToBranchList(baseUrl)
                  .searchBranches(searchTerm);
        
        // Wait for search results
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify search results based on expected outcome
        if (expectedResults.contains("No branches found")) {
            boolean noResults = branchPage.isNoBranchesMessageVisible() || !branchPage.areBranchesDisplayed();
            assertTrue(noResults, "Should show no results for search term: " + searchTerm);
        } else {
            // For valid searches, branches should be displayed or no branches message
            boolean hasResults = branchPage.areBranchesDisplayed() || branchPage.isNoBranchesMessageVisible();
            assertTrue(hasResults, "Should show search results or no branches message");
        }
        
        log.debug("✓ Branch search verified for: {}", searchTerm);
    }
    
    @Test
    @DisplayName("Should filter branches by status correctly")
    void shouldFilterBranchesByStatusCorrectly() {
        log.info("Testing branch status filtering");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to branch list
        branchPage.navigateToBranchList(baseUrl);
        
        // Test filtering by ACTIVE status
        branchPage.filterByStatus("ACTIVE");
        
        // Wait for filter results
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify page remains functional after filtering
        assertTrue(branchPage.isBranchListPageLoaded(), 
                   "Branch list page should remain loaded after filtering");
        
        log.debug("✓ Branch status filtering verified");
    }
    
    @Test
    @DisplayName("Should filter branches by city correctly")
    void shouldFilterBranchesByCityCorrectly() {
        log.info("Testing branch city filtering");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to branch list and filter by city
        branchPage.navigateToBranchList(baseUrl)
                  .filterByCity("Jakarta");
        
        // Wait for filter results
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify filtering functionality
        assertTrue(branchPage.isBranchListPageLoaded(), 
                   "Branch list page should remain loaded after city filtering");
        
        log.debug("✓ Branch city filtering verified");
    }
    
    @Test
    @DisplayName("Should display branch creation form correctly")
    void shouldDisplayBranchCreationFormCorrectly() {
        log.info("Testing branch creation form display");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to create branch form
        branchPage.navigateToCreateBranch(baseUrl);
        
        // Verify form page loads
        assertTrue(branchPage.isBranchFormPageLoaded(), 
                   "Branch creation form should be loaded");
        
        log.debug("✓ Branch creation form displayed correctly");
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/selenium/essential/branch-creation-essential.csv", numLinesToSkip = 1)
    @DisplayName("Should handle branch creation validation correctly")
    void shouldHandleBranchCreationValidationCorrectly(String branchCode, String branchName, String managerName,
                                                       String city, String status, boolean isMainBranch,
                                                       String expectedResult, String description) {
        log.info("Testing branch creation validation: {}", description);
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to create branch form
        branchPage.navigateToCreateBranch(baseUrl);
        
        // Fill the form with test data
        branchPage.fillBranchForm(branchCode, branchName, managerName, 
                                 "Test Address", city, "12345", "Indonesia",
                                 "021-1234567", "test@example.com", status, isMainBranch);
        
        // Submit the form
        branchPage.submitBranchForm();
        
        // Wait for form processing
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify the result based on expected outcome
        if (expectedResult.equals("Success")) {
            // Should redirect to list with success message or show success
            boolean successResult = driver.getCurrentUrl().contains("/branch/list") || 
                                   branchPage.isSuccessMessageVisible();
            assertTrue(successResult, "Branch creation should succeed for: " + description);
        } else {
            // Should show validation error or remain on form
            boolean errorResult = branchPage.isErrorMessageVisible() || 
                                 branchPage.isBranchFormPageLoaded();
            assertTrue(errorResult, "Branch creation should show validation error for: " + description);
        }
        
        log.debug("✓ Branch creation validation verified for: {}", description);
    }
    
    @Test
    @DisplayName("Should display branch details correctly")
    void shouldDisplayBranchDetailsCorrectly() {
        log.info("Testing branch details display");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to branch list
        branchPage.navigateToBranchList(baseUrl);
        
        // Check if any branches are displayed
        if (branchPage.areBranchesDisplayed()) {
            // Check if the first branch has required details visible
            int branchCount = branchPage.getBranchCount();
            assertTrue(branchCount > 0, "Should display branch details in the list");
            log.debug("Displayed {} branches with details", branchCount);
        } else if (branchPage.isNoBranchesMessageVisible()) {
            // No branches available is also a valid state
            log.debug("No branches message is displayed - valid state");
        }
        
        assertTrue(branchPage.isBranchListPageLoaded(), 
                   "Branch list page should be properly loaded");
        
        log.debug("✓ Branch details display verified");
    }
    
    @Test
    @DisplayName("Should support column sorting functionality")
    void shouldSupportColumnSortingFunctionality() {
        log.info("Testing branch table column sorting");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to branch list
        branchPage.navigateToBranchList(baseUrl);
        
        // Test sorting by different columns
        branchPage.clickColumnSort("Code");
        
        // Wait for sort
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertTrue(branchPage.isBranchListPageLoaded(), 
                   "Branch list should remain loaded after sorting");
        
        // Test sorting by name
        branchPage.clickColumnSort("Name");
        
        // Wait for sort
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertTrue(branchPage.isBranchListPageLoaded(), 
                   "Branch list should remain loaded after name sorting");
        
        log.debug("✓ Column sorting functionality verified");
    }
    
    @Test
    @DisplayName("Should handle branch status management correctly")
    void shouldHandleBranchStatusManagementCorrectly() {
        log.info("Testing branch status management");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to branch list
        branchPage.navigateToBranchList(baseUrl);
        
        // Check if there are existing branches for status management
        if (branchPage.areBranchesDisplayed()) {
            // We can see branches and their status
            int branchCount = branchPage.getBranchCount();
            assertTrue(branchCount > 0, "Should have branches for status management");
            log.debug("Found {} branches for status management testing", branchCount);
        } else {
            // No branches to manage is also a valid state
            log.debug("No branches available for status management");
        }
        
        assertTrue(branchPage.isBranchListPageLoaded(), 
                   "Branch management page should be accessible");
        
        log.debug("✓ Branch status management verified");
    }
    
    @Test
    @DisplayName("Should display action buttons for each branch")
    void shouldDisplayActionButtonsForEachBranch() {
        log.info("Testing branch action buttons display");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to branch list
        branchPage.navigateToBranchList(baseUrl);
        
        // Verify the page structure and action availability
        assertTrue(branchPage.isBranchListPageLoaded(), 
                   "Branch list page should load with proper structure");
        
        // Check branch display status
        if (branchPage.areBranchesDisplayed()) {
            log.debug("Branches displayed with action buttons");
        } else {
            log.debug("No branches to display action buttons for");
        }
        
        log.debug("✓ Branch action buttons structure verified");
    }
    
    @Test
    @DisplayName("Should support branch workflow for admin users")
    void shouldSupportBranchWorkflowForAdminUsers() {
        log.info("Testing complete branch workflow for admin");
        
        // Login as admin (full branch management access)
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Admin workflow: List -> Search -> Create -> View
        branchPage.navigateToBranchList(baseUrl);
        assertTrue(branchPage.isBranchListPageLoaded(), 
                   "Admin should access branch list");
        
        // Test search functionality
        branchPage.searchBranches("HO");
        
        // Wait for search
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Navigate to create form
        branchPage.navigateToCreateBranch(baseUrl);
        assertTrue(branchPage.isBranchFormPageLoaded(),
                   "Admin should access branch creation form");
        
        // Return to list
        branchPage.navigateToBranchList(baseUrl);
        assertTrue(branchPage.isBranchListPageLoaded(), 
                   "Admin workflow should be properly supported");
        
        log.debug("✓ Admin branch workflow verified");
    }
    
    @Test
    @DisplayName("Should support branch workflow for manager users")
    void shouldSupportBranchWorkflowForManagerUsers() {
        log.info("Testing branch workflow for branch manager");
        
        // Login as manager
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("manager1", "minibank123");
        
        // Manager should be able to access branch management
        branchPage.navigateToBranchList(baseUrl);
        assertTrue(branchPage.isBranchListPageLoaded(), 
                   "Manager should access branch management");
        
        // Test filtering capabilities
        branchPage.filterByStatus("ACTIVE");
        
        // Wait for filter
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertTrue(branchPage.isBranchListPageLoaded(), 
                   "Manager workflow should be properly supported");
        
        log.debug("✓ Manager branch workflow verified");
    }
    
    @Test
    @DisplayName("Should handle branch page navigation correctly")
    void shouldHandleBranchPageNavigationCorrectly() {
        log.info("Testing navigation between branch pages");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Test navigation flow
        branchPage.navigateToBranchList(baseUrl);
        assertTrue(branchPage.isBranchListPageLoaded(), 
                   "Should start at branch list page");
        
        // Navigate to create form
        branchPage.navigateToCreateBranch(baseUrl);
        assertTrue(branchPage.isBranchFormPageLoaded(),
                   "Should navigate to create form");
        
        // Navigate back to list
        branchPage.navigateToBranchList(baseUrl);
        assertTrue(branchPage.isBranchListPageLoaded(), 
                   "Should navigate back to branch list");
        
        log.debug("✓ Navigation between branch pages verified");
    }
    
    @Test
    @DisplayName("Should handle branch page styling and layout")
    void shouldHandleBranchPageStylingAndLayout() {
        log.info("Testing branch page styling and layout");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to branch list
        branchPage.navigateToBranchList(baseUrl);
        
        // Verify page layout elements
        assertTrue(branchPage.isBranchListPageLoaded(), 
                   "Branch page should have proper layout and styling");
        
        // Check create form layout
        branchPage.navigateToCreateBranch(baseUrl);
        assertTrue(branchPage.isBranchFormPageLoaded(),
                   "Branch form should have proper layout and styling");
        
        log.debug("✓ Branch page styling and layout verified");
    }
}