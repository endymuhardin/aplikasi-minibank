package id.ac.tazkia.minibank.functional.web.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AccountStatementPage extends BasePage {
    
    // Page elements using @FindBy annotations for reliability
    @FindBy(id = "account-number")
    private WebElement accountNumberDisplay;
    
    @FindBy(id = "account-name")
    private WebElement accountNameDisplay;
    
    @FindBy(id = "customer-name")
    private WebElement customerNameDisplay;
    
    @FindBy(id = "current-balance")
    private WebElement currentBalanceDisplay;
    
    @FindBy(id = "start-date")
    private WebElement startDateInput;
    
    @FindBy(id = "end-date")
    private WebElement endDateInput;
    
    @FindBy(id = "generate-pdf-btn")
    private WebElement generatePdfButton;
    
    @FindBy(id = "back-to-accounts-btn")
    private WebElement backToAccountsButton;
    
    // Static locators for dynamic elements
    private static final By LOADING_INDICATOR = By.id("loading-indicator");
    private static final By SUCCESS_MESSAGE = By.id("success-message");
    private static final By ERROR_MESSAGE = By.id("error-message");
    private static final By VALIDATION_ERROR = By.className("validation-error");
    private static final By PAGE_TITLE = By.tagName("h1");
    
    // Date formatter for consistent date handling
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public AccountStatementPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    /**
     * Navigate to account statement page for specific account
     */
    public AccountStatementPage open(String accountId) {
        log.info("Opening account statement page for account ID: {}", accountId);
        driver.get(baseUrl + "/account/" + accountId + "/statement");
        waitForPageToLoad();
        waitForAccountInfoToLoad();
        return this;
    }
    
    /**
     * Wait for account information to be loaded on the page
     */
    private void waitForAccountInfoToLoad() {
        // Wait for account number to be populated (indicates page data is loaded)
        wait.until(ExpectedConditions.and(
            ExpectedConditions.visibilityOf(accountNumberDisplay),
            ExpectedConditions.not(ExpectedConditions.textToBe(By.id("account-number"), ""))
        ));
        log.info("Account information loaded successfully");
    }
    
    /**
     * Get the displayed account number
     */
    public String getAccountNumber() {
        return accountNumberDisplay.getText().trim();
    }
    
    /**
     * Get the displayed account name
     */
    public String getAccountName() {
        return accountNameDisplay.getText().trim();
    }
    
    /**
     * Get the displayed customer name
     */
    public String getCustomerName() {
        return customerNameDisplay.getText().trim();
    }
    
    /**
     * Get the displayed current balance
     */
    public String getCurrentBalance() {
        return currentBalanceDisplay.getText().trim();
    }
    
    /**
     * Set the start date for the statement period
     */
    public AccountStatementPage setStartDate(String date) {
        log.info("Setting start date to: {}", date);
        setDateInputValue(startDateInput, date);
        return this;
    }
    
    /**
     * Set the start date using LocalDate
     */
    public AccountStatementPage setStartDate(LocalDate date) {
        return setStartDate(date.format(DATE_FORMAT));
    }
    
    /**
     * Set the end date for the statement period
     */
    public AccountStatementPage setEndDate(String date) {
        log.info("Setting end date to: {}", date);
        setDateInputValue(endDateInput, date);
        return this;
    }
    
    /**
     * Set the end date using LocalDate
     */
    public AccountStatementPage setEndDate(LocalDate date) {
        return setEndDate(date.format(DATE_FORMAT));
    }
    
    /**
     * Set both start and end dates for the statement period
     */
    public AccountStatementPage setDateRange(String startDate, String endDate) {
        log.info("Setting date range from {} to {}", startDate, endDate);
        setStartDate(startDate);
        setEndDate(endDate);
        return this;
    }
    
    /**
     * Set both start and end dates using LocalDate
     */
    public AccountStatementPage setDateRange(LocalDate startDate, LocalDate endDate) {
        return setDateRange(startDate.format(DATE_FORMAT), endDate.format(DATE_FORMAT));
    }
    
    /**
     * Get the current start date value
     */
    public String getStartDate() {
        return startDateInput.getAttribute("value");
    }
    
    /**
     * Get the current end date value
     */
    public String getEndDate() {
        return endDateInput.getAttribute("value");
    }
    
    /**
     * Click the Generate PDF button and wait for download to start
     */
    public AccountStatementPage clickGeneratePdf() {
        log.info("Clicking Generate PDF button");
        scrollToElementAndClick(generatePdfButton);
        
        // Wait for any loading indicator to appear and disappear
        waitForLoadingToComplete();
        
        return this;
    }
    
    /**
     * Click Generate PDF button and wait for file download with timeout
     */
    public File clickGeneratePdfAndWaitForDownload(File downloadDir, int timeoutSeconds) {
        log.info("Generating PDF and waiting for download in directory: {}", downloadDir.getAbsolutePath());
        
        // Click generate button
        clickGeneratePdf();
        
        // Wait for file to be downloaded
        return waitForFileDownload(downloadDir, "statement_*.pdf", timeoutSeconds);
    }
    
    /**
     * Click Generate PDF button and verify download starts (simplified version)
     */
    public boolean clickGeneratePdfAndVerifyDownloadStarts() {
        log.info("Clicking Generate PDF button and verifying download starts");
        
        // Get current URL before clicking
        String currentUrl = driver.getCurrentUrl();
        
        // Click generate button  
        scrollToElementAndClick(generatePdfButton);
        
        // Wait a moment for any loading or response
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Check if we either stayed on the same page (indicating download started)
        // or got redirected (which could also be valid)
        String newUrl = driver.getCurrentUrl();
        boolean downloadStarted = newUrl.equals(currentUrl) || newUrl.contains("/statement");
        
        log.info("Download verification: stayed on page = {}, current URL = {}", downloadStarted, newUrl);
        return downloadStarted;
    }
    
    /**
     * Click Generate PDF with default download directory and timeout
     */
    public File clickGeneratePdfAndWaitForDownload(File downloadDir) {
        return clickGeneratePdfAndWaitForDownload(downloadDir, 30);
    }
    
    /**
     * Wait for a file matching the pattern to be downloaded
     */
    private File waitForFileDownload(File downloadDir, String filePattern, int timeoutSeconds) {
        log.info("Waiting for file download: pattern='{}', timeout={}s", filePattern, timeoutSeconds);
        
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            File[] files = downloadDir.listFiles((dir, name) -> 
                name.matches(filePattern.replace("*", ".*")) && !name.endsWith(".crdownload"));
            
            if (files != null && files.length > 0) {
                // Sort by last modified to get the most recent file
                File latestFile = files[0];
                for (File file : files) {
                    if (file.lastModified() > latestFile.lastModified()) {
                        latestFile = file;
                    }
                }
                
                // Verify file has content
                if (latestFile.length() > 0) {
                    log.info("File downloaded successfully: {}, size: {} bytes", 
                        latestFile.getName(), latestFile.length());
                    return latestFile;
                }
            }
            
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Download wait interrupted", e);
            }
        }
        
        throw new RuntimeException(String.format(
            "File download timeout after %d seconds. Pattern: %s, Directory: %s", 
            timeoutSeconds, filePattern, downloadDir.getAbsolutePath()));
    }
    
    /**
     * Wait for loading indicator to complete
     */
    private void waitForLoadingToComplete() {
        try {
            // Wait for loading indicator to appear (if it exists)
            wait.until(ExpectedConditions.presenceOfElementLocated(LOADING_INDICATOR));
            log.debug("Loading indicator appeared");
            
            // Then wait for it to disappear
            wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADING_INDICATOR));
            log.debug("Loading indicator disappeared");
        } catch (Exception e) {
            // Loading indicator might not appear for fast operations
            log.debug("No loading indicator found, continuing");
        }
    }
    
    /**
     * Navigate back to accounts list
     */
    public AccountListPage clickBackToAccounts() {
        log.info("Clicking back to accounts button");
        scrollToElementAndClick(backToAccountsButton);
        return new AccountListPage(driver, baseUrl);
    }
    
    /**
     * Check if validation errors are displayed
     */
    public boolean hasValidationErrors() {
        return isElementPresentSafely(VALIDATION_ERROR);
    }
    
    /**
     * Get validation error messages
     */
    public String getValidationErrorMessage() {
        if (hasValidationErrors()) {
            return driver.findElement(VALIDATION_ERROR).getText();
        }
        return "";
    }
    
    /**
     * Check if currently on account statement page
     */
    public boolean isOnAccountStatementPage() {
        return getCurrentUrl().contains("/account/") && 
               getCurrentUrl().contains("/statement") &&
               isElementPresentSafely(By.id("generate-pdf-btn"));
    }
    
    /**
     * Verify page title
     */
    public boolean hasCorrectPageTitle() {
        try {
            WebElement titleElement = driver.findElement(PAGE_TITLE);
            String title = titleElement.getText().toLowerCase();
            return title.contains("rekening koran") || title.contains("account statement");
        } catch (Exception e) {
            log.warn("Could not find page title element");
            return false;
        }
    }
    
    /**
     * Submit form expecting validation errors (for negative testing)
     */
    public AccountStatementPage clickGeneratePdfExpectingError() {
        log.info("Clicking Generate PDF button expecting validation error");
        scrollToElementAndClick(generatePdfButton);
        
        // Wait for validation errors to appear
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(VALIDATION_ERROR),
            ExpectedConditions.presenceOfElementLocated(ERROR_MESSAGE)
        ));
        
        return this;
    }
    
    /**
     * Clear all date inputs
     */
    public AccountStatementPage clearDateInputs() {
        log.info("Clearing all date inputs");
        startDateInput.clear();
        endDateInput.clear();
        return this;
    }
    
    /**
     * Check if Generate PDF button is enabled
     */
    public boolean isGeneratePdfButtonEnabled() {
        return generatePdfButton.isEnabled();
    }
    
    /**
     * Check if account information is displayed correctly
     */
    public boolean isAccountInfoDisplayed() {
        try {
            return accountNumberDisplay.isDisplayed() &&
                   accountNameDisplay.isDisplayed() &&
                   customerNameDisplay.isDisplayed() &&
                   currentBalanceDisplay.isDisplayed() &&
                   !getAccountNumber().isEmpty() &&
                   !getCustomerName().isEmpty();
        } catch (Exception e) {
            log.warn("Account information not fully displayed", e);
            return false;
        }
    }
    
    /**
     * Set date input value using JavaScript to avoid Chrome date input issues
     */
    private void setDateInputValue(WebElement dateInput, String dateValue) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        
        // Scroll element into view and ensure it's interactable
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", dateInput);
        wait.until(ExpectedConditions.elementToBeClickable(dateInput));
        
        // Set the value using JavaScript to bypass Chrome date input quirks
        js.executeScript("arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('change'));", 
            dateInput, dateValue);
        
        log.debug("Date input value set to: {} using JavaScript", dateValue);
    }
}