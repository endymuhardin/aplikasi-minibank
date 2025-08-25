package id.ac.tazkia.minibank.selenium.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BranchPage {
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    // List page elements
    @FindBy(id = "page-title")
    private WebElement pageTitle;
    
    @FindBy(id = "create-branch-btn")
    private WebElement createBranchButton;
    
    @FindBy(id = "search")
    private WebElement searchInput;
    
    @FindBy(id = "status")
    private WebElement statusSelect;
    
    @FindBy(id = "city")
    private WebElement cityInput;
    
    @FindBy(id = "filter-button")
    private WebElement filterButton;
    
    @FindBy(id = "branches-table")
    private WebElement branchesTable;
    
    @FindBy(id = "branches-table-body")
    private WebElement branchesTableBody;
    
    @FindBy(id = "no-branches-message")
    private WebElement noBranchesMessage;
    
    @FindBy(id = "success-message")
    private WebElement successMessage;
    
    @FindBy(id = "error-message")
    private WebElement errorMessage;
    
    // Form page elements
    @FindBy(id = "branchCode")
    private WebElement branchCodeInput;
    
    @FindBy(id = "branchName")
    private WebElement branchNameInput;
    
    @FindBy(id = "managerName")
    private WebElement managerNameInput;
    
    @FindBy(id = "address")
    private WebElement addressTextarea;
    
    @FindBy(id = "phoneNumber")
    private WebElement phoneNumberInput;
    
    @FindBy(id = "email")
    private WebElement emailInput;
    
    @FindBy(id = "postalCode")
    private WebElement postalCodeInput;
    
    @FindBy(id = "country")
    private WebElement countryInput;
    
    @FindBy(id = "isMainBranch")
    private WebElement isMainBranchCheckbox;
    
    @FindBy(id = "submit-branch-form")
    private WebElement submitFormButton;
    
    public BranchPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    
    /**
     * Navigate to branch list page
     */
    public BranchPage navigateToBranchList(String baseUrl) {
        driver.get(baseUrl + "/branch/list");
        return waitForBranchListPageLoad();
    }
    
    /**
     * Navigate to branch create form
     */
    public BranchPage navigateToCreateBranch(String baseUrl) {
        driver.get(baseUrl + "/branch/create");
        return waitForBranchFormPageLoad();
    }
    
    /**
     * Navigate to branch edit form
     */
    public BranchPage navigateToEditBranch(String baseUrl, String branchId) {
        driver.get(baseUrl + "/branch/edit/" + branchId);
        return waitForBranchFormPageLoad();
    }
    
    /**
     * Wait for branch list page to load
     */
    public BranchPage waitForBranchListPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        wait.until(ExpectedConditions.textToBePresentInElement(pageTitle, "Branch Management"));
        wait.until(ExpectedConditions.visibilityOf(searchInput));
        log.debug("Branch list page loaded successfully");
        return this;
    }
    
    /**
     * Wait for branch form page to load
     */
    public BranchPage waitForBranchFormPageLoad() {
        wait.until(ExpectedConditions.or(
            ExpectedConditions.textToBePresentInElement(pageTitle, "Create Branch"),
            ExpectedConditions.textToBePresentInElement(pageTitle, "Edit Branch")
        ));
        wait.until(ExpectedConditions.visibilityOf(branchCodeInput));
        log.debug("Branch form page loaded successfully");
        return this;
    }
    
    /**
     * Check if branch list page is loaded
     */
    public boolean isBranchListPageLoaded() {
        try {
            waitForBranchListPageLoad();
            return driver.getCurrentUrl().contains("/branch/list") &&
                   pageTitle.getText().contains("Branch Management");
        } catch (Exception e) {
            log.debug("Branch list page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if branch form page is loaded
     */
    public boolean isBranchFormPageLoaded() {
        try {
            waitForBranchFormPageLoad();
            return driver.getCurrentUrl().contains("/branch/") &&
                   (pageTitle.getText().contains("Create Branch") || 
                    pageTitle.getText().contains("Edit Branch"));
        } catch (Exception e) {
            log.debug("Branch form page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if create branch button is visible
     */
    public boolean isCreateBranchButtonVisible() {
        try {
            return createBranchButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Click create branch button
     */
    public BranchPage clickCreateBranch() {
        wait.until(ExpectedConditions.elementToBeClickable(createBranchButton));
        createBranchButton.click();
        log.debug("Clicked create branch button");
        return this;
    }
    
    /**
     * Search branches
     */
    public BranchPage searchBranches(String searchTerm) {
        wait.until(ExpectedConditions.visibilityOf(searchInput));
        searchInput.clear();
        searchInput.sendKeys(searchTerm);
        filterButton.click();
        log.debug("Searched branches with term: {}", searchTerm);
        return this;
    }
    
    /**
     * Filter branches by status
     */
    public BranchPage filterByStatus(String status) {
        wait.until(ExpectedConditions.visibilityOf(statusSelect));
        Select statusDropdown = new Select(statusSelect);
        statusDropdown.selectByVisibleText(status);
        filterButton.click();
        log.debug("Filtered branches by status: {}", status);
        return this;
    }
    
    /**
     * Filter branches by city
     */
    public BranchPage filterByCity(String city) {
        wait.until(ExpectedConditions.visibilityOf(cityInput));
        cityInput.clear();
        cityInput.sendKeys(city);
        filterButton.click();
        log.debug("Filtered branches by city: {}", city);
        return this;
    }
    
    /**
     * Check if branches are displayed in table
     */
    public boolean areBranchesDisplayed() {
        try {
            return branchesTable.isDisplayed() && 
                   !isNoBranchesMessageVisible();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if no branches message is visible
     */
    public boolean isNoBranchesMessageVisible() {
        try {
            return noBranchesMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if a specific branch is visible by branch code
     */
    public boolean isBranchVisible(String branchCode) {
        try {
            WebElement statusElement = driver.findElement(
                org.openqa.selenium.By.id("status-" + branchCode)
            );
            return statusElement.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get branch status
     */
    public String getBranchStatus(String branchCode) {
        try {
            WebElement statusElement = driver.findElement(
                org.openqa.selenium.By.id("status-" + branchCode)
            );
            return statusElement.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Click view button for specific branch
     */
    public BranchPage clickViewBranch(String branchCode) {
        try {
            WebElement viewButton = driver.findElement(
                org.openqa.selenium.By.id("view-" + branchCode)
            );
            wait.until(ExpectedConditions.elementToBeClickable(viewButton));
            viewButton.click();
            log.debug("Clicked view button for branch: {}", branchCode);
        } catch (Exception e) {
            log.error("Could not click view button for branch: {}", branchCode, e);
        }
        return this;
    }
    
    /**
     * Click edit button for specific branch
     */
    public BranchPage clickEditBranch(String branchCode) {
        try {
            WebElement editButton = driver.findElement(
                org.openqa.selenium.By.id("edit-" + branchCode)
            );
            wait.until(ExpectedConditions.elementToBeClickable(editButton));
            editButton.click();
            log.debug("Clicked edit button for branch: {}", branchCode);
        } catch (Exception e) {
            log.error("Could not click edit button for branch: {}", branchCode, e);
        }
        return this;
    }
    
    /**
     * Click activate button for specific branch
     */
    public BranchPage clickActivateBranch(String branchCode) {
        try {
            WebElement activateButton = driver.findElement(
                org.openqa.selenium.By.id("activate-" + branchCode)
            );
            wait.until(ExpectedConditions.elementToBeClickable(activateButton));
            activateButton.click();
            log.debug("Clicked activate button for branch: {}", branchCode);
        } catch (Exception e) {
            log.error("Could not click activate button for branch: {}", branchCode, e);
        }
        return this;
    }
    
    /**
     * Click deactivate button for specific branch
     */
    public BranchPage clickDeactivateBranch(String branchCode) {
        try {
            WebElement deactivateButton = driver.findElement(
                org.openqa.selenium.By.id("deactivate-" + branchCode)
            );
            wait.until(ExpectedConditions.elementToBeClickable(deactivateButton));
            deactivateButton.click();
            log.debug("Clicked deactivate button for branch: {}", branchCode);
        } catch (Exception e) {
            log.error("Could not click deactivate button for branch: {}", branchCode, e);
        }
        return this;
    }
    
    /**
     * Fill branch form
     */
    public BranchPage fillBranchForm(String branchCode, String branchName, String managerName,
                                    String address, String city, String postalCode, String country,
                                    String phoneNumber, String email, String status, boolean isMainBranch) {
        
        wait.until(ExpectedConditions.visibilityOf(branchCodeInput));
        
        // Clear and fill basic information
        branchCodeInput.clear();
        if (branchCode != null && !branchCode.isEmpty()) {
            branchCodeInput.sendKeys(branchCode);
        }
        
        branchNameInput.clear();
        if (branchName != null && !branchName.isEmpty()) {
            branchNameInput.sendKeys(branchName);
        }
        
        if (managerName != null && !managerName.isEmpty()) {
            managerNameInput.clear();
            managerNameInput.sendKeys(managerName);
        }
        
        // Fill location information
        if (address != null && !address.isEmpty()) {
            addressTextarea.clear();
            addressTextarea.sendKeys(address);
        }
        
        if (city != null && !city.isEmpty()) {
            this.cityInput.clear();
            this.cityInput.sendKeys(city);
        }
        
        if (postalCode != null && !postalCode.isEmpty()) {
            postalCodeInput.clear();
            postalCodeInput.sendKeys(postalCode);
        }
        
        if (country != null && !country.isEmpty()) {
            countryInput.clear();
            countryInput.sendKeys(country);
        }
        
        // Fill contact information
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            phoneNumberInput.clear();
            phoneNumberInput.sendKeys(phoneNumber);
        }
        
        if (email != null && !email.isEmpty()) {
            emailInput.clear();
            emailInput.sendKeys(email);
        }
        
        // Set status
        if (status != null && !status.isEmpty()) {
            Select statusDropdown = new Select(statusSelect);
            statusDropdown.selectByVisibleText(status);
        }
        
        // Set main branch checkbox
        if (isMainBranch && !isMainBranchCheckbox.isSelected()) {
            isMainBranchCheckbox.click();
        } else if (!isMainBranch && isMainBranchCheckbox.isSelected()) {
            isMainBranchCheckbox.click();
        }
        
        log.debug("Filled branch form for: {}", branchCode);
        return this;
    }
    
    /**
     * Submit branch form
     */
    public BranchPage submitBranchForm() {
        wait.until(ExpectedConditions.elementToBeClickable(submitFormButton));
        submitFormButton.click();
        log.debug("Submitted branch form");
        return this;
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
     * Get number of branches displayed
     */
    public int getBranchCount() {
        try {
            if (!areBranchesDisplayed()) {
                return 0;
            }
            List<WebElement> branchRows = branchesTableBody.findElements(
                org.openqa.selenium.By.tagName("tr")
            );
            return branchRows.size();
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Check if sorting is working by clicking column headers
     */
    public BranchPage clickColumnSort(String columnName) {
        try {
            String sortId = "sort-" + columnName.toLowerCase();
            WebElement columnHeader = driver.findElement(
                org.openqa.selenium.By.id(sortId)
            );
            wait.until(ExpectedConditions.elementToBeClickable(columnHeader));
            columnHeader.click();
            log.debug("Clicked sort for column: {}", columnName);
        } catch (Exception e) {
            log.error("Could not click sort for column: {}", columnName, e);
        }
        return this;
    }
}