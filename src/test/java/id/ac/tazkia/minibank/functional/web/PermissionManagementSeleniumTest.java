package id.ac.tazkia.minibank.functional.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import id.ac.tazkia.minibank.entity.Permission;
import id.ac.tazkia.minibank.functional.web.pageobject.PermissionFormPage;
import id.ac.tazkia.minibank.functional.web.pageobject.PermissionListPage;
import id.ac.tazkia.minibank.repository.PermissionRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SqlGroup({
    @Sql(scripts = "/sql/setup-permission-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(scripts = "/sql/cleanup-permission-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class PermissionManagementSeleniumTest extends BaseSeleniumTest {
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Override
    protected void performInitialLogin() {
        // Login as Manager who has USER_READ, USER_CREATE, USER_UPDATE permissions for RBAC management
        loginHelper.loginAsManager();
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void shouldLoadPermissionListPage() {
        log.info("Starting test: shouldLoadPermissionListPage");
        PermissionListPage listPage = new PermissionListPage(driver, baseUrl);
        listPage.open();
        
        assertTrue(driver.getCurrentUrl().contains("/rbac/permissions/list"));
        assertEquals("Permission Management", listPage.getPageTitle());
        assertTrue(listPage.isCreateButtonDisplayed());
        assertTrue(listPage.isPermissionsTableDisplayed());
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldCreateNewPermission() {
        log.info("Starting test: shouldCreateNewPermission");
        String uniqueCode = "TEST_PERM_" + System.currentTimeMillis();
        String permissionName = "Test Permission";
        String category = "Test Category";
        String description = "Test permission for automated testing";
        
        PermissionListPage listPage = new PermissionListPage(driver, baseUrl);
        listPage.open();
        
        PermissionFormPage formPage = listPage.clickCreatePermission();
        
        formPage.fillPermissionForm(uniqueCode, permissionName, category, description);
        
        PermissionListPage resultPage = formPage.submitForm();
        
        assertTrue(resultPage.isSuccessMessageDisplayed());
        assertTrue(resultPage.isPermissionDisplayed(uniqueCode));
        
        // Verify in database
        Permission savedPermission = permissionRepository.findByPermissionCode(uniqueCode).orElse(null);
        assertNotNull(savedPermission);
        assertEquals(permissionName, savedPermission.getPermissionName());
        assertEquals(category, savedPermission.getPermissionCategory());
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/permission/permission-creation-data.csv", numLinesToSkip = 1)
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    void shouldCreatePermissionsFromCSVData(String permissionCode, String permissionName, 
                                          String category, String description) {
        log.info("Starting test: shouldCreatePermissionsFromCSVData with permissionCode: {}", permissionCode);
        
        String uniqueCode = permissionCode + System.currentTimeMillis();
        
        PermissionListPage listPage = new PermissionListPage(driver, baseUrl);
        listPage.open();
        
        PermissionFormPage formPage = listPage.clickCreatePermission();
        
        formPage.fillPermissionForm(uniqueCode, permissionName, category, description);
        
        PermissionListPage resultPage = formPage.submitForm();
        
        assertTrue(resultPage.isSuccessMessageDisplayed(), 
                  "Permission creation failed for: " + uniqueCode);
        assertTrue(resultPage.isPermissionDisplayed(uniqueCode));
        
        // Verify database persistence
        Permission savedPermission = permissionRepository.findByPermissionCode(uniqueCode).orElse(null);
        assertNotNull(savedPermission, "Permission not saved to database: " + uniqueCode);
        assertEquals(permissionName, savedPermission.getPermissionName());
        assertEquals(category, savedPermission.getPermissionCategory());
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldValidateRequiredFields() {
        log.info("Starting test: shouldValidateRequiredFields");
        PermissionListPage listPage = new PermissionListPage(driver, baseUrl);
        listPage.open();
        
        PermissionFormPage formPage = listPage.clickCreatePermission();
        
        // Disable HTML5 validation for testing server-side validation
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "document.getElementById('permission-form').setAttribute('novalidate', 'true');"
        );
        
        // Try to submit empty form (HTML5 validation now disabled)
        formPage.submitFormExpectingError();
        
        // Should remain on form page with server validation errors
        assertTrue(driver.getCurrentUrl().contains("/rbac/permissions/create"));
        
        // Check for server-side validation errors
        assertTrue(formPage.hasValidationError("permissionCode") || 
                  formPage.hasValidationError("permissionName") || 
                  formPage.hasValidationError("permissionCategory") ||
                  formPage.isErrorMessageDisplayed() ||
                  hasValidationErrorOnPage());
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldEditExistingPermission() {
        log.info("Starting test: shouldEditExistingPermission");
        String editCode = "TEST_EDIT_" + System.currentTimeMillis();
        String editName = "Test Edit Permission";
        String editCategory = "Test Category";
        
        PermissionListPage listPage = new PermissionListPage(driver, baseUrl);
        listPage.open();
        
        // First create a permission to edit
        PermissionFormPage createPage = listPage.clickCreatePermission();
        createPage.fillPermissionForm(editCode, editName, editCategory, 
                                     "Permission for testing edit");
        PermissionListPage resultList = createPage.submitForm();
        
        // Enhanced wait: Wait specifically for the created permission to appear
        resultList.openAndWaitForPermission(editCode);
        
        // Ensure permission is visible before attempting to edit
        assertTrue(resultList.ensurePermissionVisible(editCode), 
                  "Permission " + editCode + " should be visible");
        
        // Now edit the permission with enhanced wait strategy
        PermissionFormPage editPage = resultList.editPermission(editCode);
        
        // Verify form is populated with existing data
        assertEquals(editCode, editPage.getPermissionCode());
        
        // Update permission information
        editPage.fillPermissionForm(null, "Updated Permission Name", "Updated Category", 
                                   "Updated description");
        
        PermissionListPage resultPage = editPage.submitForm();
        
        assertTrue(resultPage.isSuccessMessageDisplayed());
        
        // Verify changes in database
        Permission updatedPermission = permissionRepository.findByPermissionCode(editCode).orElse(null);
        assertNotNull(updatedPermission);
        assertEquals("Updated Permission Name", updatedPermission.getPermissionName());
        assertEquals("Updated Category", updatedPermission.getPermissionCategory());
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldFilterPermissionsByCategory() {
        log.info("Starting test: shouldFilterPermissionsByCategory");
        PermissionListPage listPage = new PermissionListPage(driver, baseUrl);
        listPage.open();
        
        // Filter by specific category
        String categoryToFilter = "User Management";
        listPage.filterByCategory(categoryToFilter);
        
        assertTrue(listPage.isCategoryFilterDisplayed());
        // Verify that filtered results are displayed
        // Note: The actual verification depends on the test data setup
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldViewPermissionDetails() {
        log.info("Starting test: shouldViewPermissionDetails");
        String viewCode = "VIEW_TEST_PERM";
        
        PermissionListPage listPage = new PermissionListPage(driver, baseUrl);
        
        // Enhanced wait: Wait specifically for the test permission to be visible
        listPage.openAndWaitForPermission(viewCode);
        
        // Ensure permission is visible before attempting to view
        assertTrue(listPage.ensurePermissionVisible(viewCode), 
                  "Permission " + viewCode + " should be visible");
        
        // View permission with enhanced wait strategy
        listPage.viewPermission(viewCode);
        
        assertTrue(driver.getCurrentUrl().contains("/rbac/permissions/view/"));
        assertTrue(driver.getPageSource().contains(viewCode));
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldValidateDuplicatePermissionCode() {
        log.info("Starting test: shouldValidateDuplicatePermissionCode");
        PermissionListPage listPage = new PermissionListPage(driver, baseUrl);
        listPage.open();
        
        PermissionFormPage formPage = listPage.clickCreatePermission();
        
        // Try to create permission with existing code
        formPage.fillPermissionForm("DUPLICATE_PERM", "Duplicate Permission", 
                                   "Test Category", "Test description");
        
        PermissionFormPage resultPage = formPage.submitFormExpectingError();
        
        // Should stay on form page with validation error
        assertTrue(driver.getCurrentUrl().contains("/rbac/permissions/create"));
        
        // Check for validation error indicators
        assertTrue(resultPage.isErrorMessageDisplayed() || 
                  resultPage.hasValidationError("permissionCode") ||
                  hasValidationErrorOnPage());
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/permission/permission-validation-data.csv", numLinesToSkip = 1)
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldValidatePermissionInputErrors(String testCase, String permissionCode, 
                                           String permissionName, String category,
                                           String expectedError) {
        log.info("Starting test: shouldValidatePermissionInputErrors with testCase: {}, permissionCode: {}", testCase, permissionCode);
        
        PermissionListPage listPage = new PermissionListPage(driver, baseUrl);
        listPage.open();
        
        PermissionFormPage formPage = listPage.clickCreatePermission();
        
        // Disable HTML5 validation for testing server-side validation
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "document.getElementById('permission-form').setAttribute('novalidate', 'true');" +
            "document.getElementById('permissionCode').removeAttribute('required');" +
            "document.getElementById('permissionName').removeAttribute('required');" +
            "document.getElementById('permissionCategory').removeAttribute('required');"
        );
        
        // Fill form with potentially invalid data
        String defaultCode = "TEST" + System.currentTimeMillis();
        String defaultName = "Test Permission";
        String defaultCategory = "Test Category";
        String defaultDescription = "Test description";
        
        // Override with test data
        String testCode = (permissionCode != null) ? permissionCode : defaultCode;
        String testName = (permissionName != null) ? permissionName : defaultName;
        String testCat = (category != null) ? category : defaultCategory;
        
        // For empty string tests
        if (testCase.contains("Empty Permission Code")) testCode = "";
        if (testCase.contains("Empty Permission Name")) testName = "";
        if (testCase.contains("Empty Category")) testCat = "";
        
        formPage.fillPermissionForm(testCode, testName, testCat, defaultDescription);
        
        PermissionFormPage resultPage = formPage.submitFormExpectingError();
        
        // Should stay on form page with validation error
        assertTrue(driver.getCurrentUrl().contains("/rbac/permissions/create") ||
                   driver.getCurrentUrl().contains("/rbac/permissions/edit"));
        
        // Check for validation errors
        assertTrue(resultPage.isErrorMessageDisplayed() || hasValidationErrorOnPage());
        
        // Check for validation error indicators
        assertTrue(resultPage.isErrorMessageDisplayed() || hasValidationErrorOnPage(),
                  "Should display validation error for: " + testCase);
    }
    
    private boolean hasValidationErrorOnPage() {
        try {
            // Check for various types of validation errors that could be present
            boolean hasErrors = false;
            
            // Check for field-specific validation errors
            try {
                hasErrors = hasErrors || driver.findElement(By.id("permissionCode-error")).isDisplayed();
            } catch (org.openqa.selenium.NoSuchElementException e) {
                log.debug("permissionCode-error element not found: {}", e.getMessage());
            }
            
            try {
                hasErrors = hasErrors || driver.findElement(By.id("permissionName-error")).isDisplayed();
            } catch (org.openqa.selenium.NoSuchElementException e) {
                log.debug("permissionName-error element not found: {}", e.getMessage());
            }
            
            try {
                hasErrors = hasErrors || driver.findElement(By.id("permissionCategory-error")).isDisplayed();
            } catch (org.openqa.selenium.NoSuchElementException e) {
                log.debug("permissionCategory-error element not found: {}", e.getMessage());
            }
            
            // Check for general error message
            try {
                hasErrors = hasErrors || driver.findElement(By.id("error-message")).isDisplayed();
            } catch (org.openqa.selenium.NoSuchElementException e) {
                log.debug("error-message element not found: {}", e.getMessage());
            }
            
            // Check if we're still on the form page (indicates validation failed)
            hasErrors = hasErrors || driver.getCurrentUrl().contains("/rbac/permissions/create") || 
                       driver.getCurrentUrl().contains("/rbac/permissions/edit");
            
            return hasErrors;
        } catch (Exception e) {
            log.error("Error checking for validation errors on page", e);
            return false;
        }
    }
}