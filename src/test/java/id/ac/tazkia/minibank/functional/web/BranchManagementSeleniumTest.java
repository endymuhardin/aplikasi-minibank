package id.ac.tazkia.minibank.functional.web;

import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.repository.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openqa.selenium.support.ui.ExpectedConditions.*;

@SqlGroup({
    @Sql(scripts = "/sql/setup-branch-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(scripts = "/sql/cleanup-branch-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
class BranchManagementSeleniumTest extends BaseSeleniumTest {

    @Autowired
    private BranchRepository branchRepository;

    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test
    void shouldDisplayBranchListPage() {
        // When
        driver.get(baseUrl + "/branch/list");

        // Then
        wait.until(titleIs("Branch Management - Minibank"));
        assertThat(driver.getTitle()).contains("Branch Management");
        
        WebElement pageHeader = wait.until(presenceOfElementLocated(By.tagName("h1")));
        assertThat(pageHeader.getText()).isEqualTo("Branch Management");
        
        // Verify branches are displayed
        WebElement branchesTable = wait.until(presenceOfElementLocated(By.id("branches-table")));
        assertThat(branchesTable.getText()).contains("HO001");
        assertThat(branchesTable.getText()).contains("Kantor Pusat Jakarta");
        assertThat(branchesTable.getText()).contains("JKT01");
        assertThat(branchesTable.getText()).contains("Cabang Jakarta Timur");
        
        // Verify main branch indicator
        assertThat(branchesTable.getText()).contains("Main");
    }

    @Test
    void shouldFilterBranchesByStatus() {
        // Given
        driver.get(baseUrl + "/branch/list");
        wait.until(presenceOfElementLocated(By.id("branches-table")));

        // When - Filter by ACTIVE status
        Select statusSelect = new Select(wait.until(presenceOfElementLocated(By.id("status"))));
        statusSelect.selectByValue("ACTIVE");
        
        WebElement filterButton = driver.findElement(By.id("filter-button"));
        filterButton.click();

        // Then
        wait.until(presenceOfElementLocated(By.id("branches-table")));
        WebElement branchesTable = driver.findElement(By.id("branches-table"));
        assertThat(branchesTable.getText()).contains("HO001");
        assertThat(branchesTable.getText()).contains("JKT01");
    }

    @Test
    void shouldSearchBranches() {
        // Given
        driver.get(baseUrl + "/branch/list");
        wait.until(presenceOfElementLocated(By.id("branches-table")));

        // When - Search for "Pusat"
        WebElement searchInput = driver.findElement(By.id("search"));
        searchInput.clear();
        searchInput.sendKeys("Pusat");
        
        WebElement filterButton = driver.findElement(By.id("filter-button"));
        filterButton.click();

        // Then
        wait.until(presenceOfElementLocated(By.id("branches-table")));
        WebElement branchesTable = driver.findElement(By.id("branches-table"));
        assertThat(branchesTable.getText()).contains("HO001");
        assertThat(branchesTable.getText()).contains("Kantor Pusat Jakarta");
        assertThat(branchesTable.getText()).doesNotContain("JKT01");
    }

    @Test
    void shouldCreateNewBranch() {
        // Given
        driver.get(baseUrl + "/branch/list");

        // When - Click create button
        WebElement createButton = wait.until(presenceOfElementLocated(By.id("create-branch-btn")));
        createButton.click();

        // Then - Should be on create form
        wait.until(titleContains("Create Branch"));
        assertThat(driver.getTitle()).contains("Create Branch");

        // Fill form
        driver.findElement(By.id("branchCode")).sendKeys("TEST01");
        driver.findElement(By.id("branchName")).sendKeys("Test Branch");
        driver.findElement(By.id("address")).sendKeys("Jl. Test No. 123");
        driver.findElement(By.id("city")).sendKeys("Test City");
        driver.findElement(By.id("postalCode")).sendKeys("12345");
        driver.findElement(By.id("phoneNumber")).sendKeys("021-1234567");
        driver.findElement(By.id("email")).sendKeys("test@bankbsi.co.id");
        driver.findElement(By.id("managerName")).sendKeys("Test Manager");
        
        Select statusSelect = new Select(driver.findElement(By.id("status")));
        statusSelect.selectByValue("ACTIVE");

        // Submit form
        WebElement submitButton = driver.findElement(By.id("submit-branch-form"));
        submitButton.click();

        // Then - Should redirect to list with success message
        wait.until(urlContains("/branch/list"));
        WebElement successMessage = wait.until(presenceOfElementLocated(By.id("success-message")));
        assertThat(successMessage.getText()).contains("Branch created successfully");

        // Verify branch appears in list
        WebElement branchesTable = driver.findElement(By.id("branches-table"));
        assertThat(branchesTable.getText()).contains("TEST01");
        assertThat(branchesTable.getText()).contains("Test Branch");

        // Verify in database
        Optional<Branch> savedBranch = branchRepository.findByBranchCode("TEST01");
        assertThat(savedBranch).isPresent();
        assertThat(savedBranch.get().getBranchName()).isEqualTo("Test Branch");
    }

    @Test
    void shouldValidateRequiredFields() {
        // Given
        driver.get(baseUrl + "/branch/create");
        wait.until(titleContains("Create Branch"));

        // When - Submit form without required fields
        WebElement submitButton = driver.findElement(By.id("submit-branch-form"));
        submitButton.click();

        // Then - Should stay on form and show validation errors
        wait.until(titleContains("Create Branch"));
        assertThat(driver.getCurrentUrl()).contains("/branch/create");
    }

    @Test
    void shouldEditExistingBranch() {
        // Given
        driver.get(baseUrl + "/branch/list");
        wait.until(presenceOfElementLocated(By.id("branches-table")));

        // When - Click edit button for HO001
        WebElement editButton = wait.until(presenceOfElementLocated(By.id("edit-HO001")));
        editButton.click();

        // Then - Should be on edit form
        wait.until(titleContains("Edit Branch"));
        assertThat(driver.getTitle()).contains("Edit Branch");

        // Verify form is pre-filled
        WebElement branchNameField = driver.findElement(By.id("branchName"));
        assertThat(branchNameField.getAttribute("value")).isEqualTo("Kantor Pusat Jakarta");

        // Update branch name
        branchNameField.clear();
        branchNameField.sendKeys("Updated Kantor Pusat Jakarta");

        // Update manager name
        WebElement managerField = driver.findElement(By.id("managerName"));
        managerField.clear();
        managerField.sendKeys("H. Ahmad Surya Updated");

        // Submit form
        WebElement submitButton = driver.findElement(By.id("submit-branch-form"));
        submitButton.click();

        // Then - Should redirect to list with success message
        wait.until(urlContains("/branch/list"));
        WebElement successMessage = wait.until(presenceOfElementLocated(By.id("success-message")));
        assertThat(successMessage.getText()).contains("Branch updated successfully");

        // Verify changes in table
        WebElement branchesTable = driver.findElement(By.id("branches-table"));
        assertThat(branchesTable.getText()).contains("Updated Kantor Pusat Jakarta");

        // Verify in database
        Branch updatedBranch = branchRepository.findByBranchCode("HO001").orElseThrow();
        assertThat(updatedBranch.getBranchName()).isEqualTo("Updated Kantor Pusat Jakarta");
        assertThat(updatedBranch.getManagerName()).isEqualTo("H. Ahmad Surya Updated");
    }

    @Test
    void shouldViewBranchDetails() {
        // Given
        driver.get(baseUrl + "/branch/list");
        wait.until(presenceOfElementLocated(By.id("branches-table")));

        // When - Click view button for HO001
        WebElement viewButton = wait.until(presenceOfElementLocated(By.id("view-HO001")));
        viewButton.click();

        // Then - Should be on view page
        wait.until(titleIs("Branch Details - Minibank"));
        assertThat(driver.getTitle()).isEqualTo("Branch Details - Minibank");

        // Verify branch details are displayed
        assertThat(driver.getPageSource()).contains("HO001");
        assertThat(driver.getPageSource()).contains("Kantor Pusat Jakarta");
        assertThat(driver.getPageSource()).contains("H. Ahmad Surya");
        assertThat(driver.getPageSource()).contains("Jakarta Pusat");
        assertThat(driver.getPageSource()).contains("Main Branch");
        assertThat(driver.getPageSource()).contains("Active");
    }

    @Test
    void shouldDeactivateRegularBranch() {
        // Given
        driver.get(baseUrl + "/branch/list");
        wait.until(presenceOfElementLocated(By.id("branches-table")));

        // When - Click deactivate button for JKT01 (not main branch)
        WebElement deactivateButton = wait.until(presenceOfElementLocated(By.id("deactivate-JKT01")));
        deactivateButton.click();

        // Handle confirmation dialog
        wait.until(alertIsPresent());
        driver.switchTo().alert().accept();

        // Then - Should redirect to list with success message
        wait.until(urlContains("/branch/list"));
        WebElement successMessage = wait.until(presenceOfElementLocated(By.id("success-message")));
        assertThat(successMessage.getText()).contains("Branch deactivated successfully");

        // Verify status change in table
        WebElement statusBadge = driver.findElement(By.id("status-JKT01"));
        assertThat(statusBadge.getText()).isEqualTo("Inactive");

        // Verify in database
        Branch deactivatedBranch = branchRepository.findByBranchCode("JKT01").orElseThrow();
        assertThat(deactivatedBranch.getStatus()).isEqualTo(Branch.BranchStatus.INACTIVE);
    }

    @Test
    void shouldNotDeactivateMainBranch() {
        // Given
        driver.get(baseUrl + "/branch/list");
        wait.until(presenceOfElementLocated(By.id("branches-table")));

        // When - Try to find deactivate button for HO001 (main branch)
        // Then - Should not have deactivate button for main branch
        assertThat(driver.findElements(By.id("deactivate-HO001"))).isEmpty();
    }

    @Test
    void shouldActivateInactiveBranch() {
        // Given - Make JKT01 inactive first
        Branch branch = branchRepository.findByBranchCode("JKT01").orElseThrow();
        branch.setStatus(Branch.BranchStatus.INACTIVE);
        branchRepository.save(branch);

        driver.get(baseUrl + "/branch/list");
        wait.until(presenceOfElementLocated(By.id("branches-table")));

        // When - Click activate button for JKT01
        WebElement activateButton = wait.until(presenceOfElementLocated(By.id("activate-JKT01")));
        activateButton.click();

        // Handle confirmation dialog
        wait.until(alertIsPresent());
        driver.switchTo().alert().accept();

        // Then - Should redirect to list with success message
        wait.until(urlContains("/branch/list"));
        WebElement successMessage = wait.until(presenceOfElementLocated(By.id("success-message")));
        assertThat(successMessage.getText()).contains("Branch activated successfully");

        // Verify status change in table
        WebElement statusBadge = driver.findElement(By.id("status-JKT01"));
        assertThat(statusBadge.getText()).isEqualTo("Active");

        // Verify in database
        Branch activatedBranch = branchRepository.findByBranchCode("JKT01").orElseThrow();
        assertThat(activatedBranch.getStatus()).isEqualTo(Branch.BranchStatus.ACTIVE);
    }

    @Test
    void shouldPreventDuplicateBranchCode() {
        // Given
        driver.get(baseUrl + "/branch/create");
        wait.until(titleContains("Create Branch"));

        // When - Try to create branch with existing code
        driver.findElement(By.id("branchCode")).sendKeys("HO001"); // Existing code
        driver.findElement(By.id("branchName")).sendKeys("Duplicate Branch");
        
        Select statusSelect = new Select(driver.findElement(By.id("status")));
        statusSelect.selectByValue("ACTIVE");

        WebElement submitButton = driver.findElement(By.id("submit-branch-form"));
        submitButton.click();

        // Then - Should stay on form with error
        wait.until(titleContains("Create Branch"));
        assertThat(driver.getPageSource()).contains("Branch code already exists");
    }

    @Test
    void shouldPreventMultipleMainBranches() {
        // Given
        driver.get(baseUrl + "/branch/create");
        wait.until(titleContains("Create Branch"));

        // When - Try to create another main branch
        driver.findElement(By.id("branchCode")).sendKeys("TEST01");
        driver.findElement(By.id("branchName")).sendKeys("Another Main Branch");
        
        Select statusSelect = new Select(driver.findElement(By.id("status")));
        statusSelect.selectByValue("ACTIVE");
        
        WebElement mainBranchCheckbox = driver.findElement(By.id("isMainBranch"));
        mainBranchCheckbox.click();

        WebElement submitButton = driver.findElement(By.id("submit-branch-form"));
        submitButton.click();

        // Then - Should stay on form with error
        wait.until(titleContains("Create Branch"));
        assertThat(driver.getPageSource()).contains("Only one main branch is allowed");
    }

}