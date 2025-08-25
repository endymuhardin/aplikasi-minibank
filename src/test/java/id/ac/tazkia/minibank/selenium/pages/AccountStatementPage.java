package id.ac.tazkia.minibank.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class AccountStatementPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Page Elements
    @FindBy(id = "page-title")
    private WebElement pageTitle;
    
    @FindBy(id = "back-to-accounts-btn")
    private WebElement backButton;
    
    // Account Information Elements
    @FindBy(id = "account-number")
    private WebElement accountNumber;
    
    @FindBy(id = "account-name")
    private WebElement accountName;
    
    @FindBy(id = "customer-name")
    private WebElement customerName;
    
    @FindBy(id = "current-balance")
    private WebElement currentBalance;
    
    // Form Elements
    @FindBy(id = "statement-form")
    private WebElement statementForm;
    
    @FindBy(id = "start-date")
    private WebElement startDateInput;
    
    @FindBy(id = "end-date")
    private WebElement endDateInput;
    
    @FindBy(id = "generate-pdf-btn")
    private WebElement generatePdfButton;
    
    // Messages and Indicators
    @FindBy(id = "success-message")
    private WebElement successMessage;
    
    @FindBy(id = "error-message")
    private WebElement errorMessage;
    
    @FindBy(id = "loading-indicator")
    private WebElement loadingIndicator;

    public AccountStatementPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15)); // Longer wait for PDF generation
        PageFactory.initElements(driver, this);
    }

    public void navigateToAccountStatement(String baseUrl, String accountId) {
        String statementUrl = baseUrl + "/account/" + accountId + "/statement";
        log.info("Navigating to account statement: {}", statementUrl);
        driver.get(statementUrl);
        waitForPageLoad();
    }

    public boolean isStatementPageLoaded() {
        try {
            wait.until(ExpectedConditions.visibilityOf(pageTitle));
            String titleText = pageTitle.getText();
            return titleText.contains("Account Statement") || titleText.contains("Rekening Koran");
        } catch (Exception e) {
            log.warn("Statement page not loaded properly: {}", e.getMessage());
            return false;
        }
    }

    public boolean isAccountInformationDisplayed() {
        try {
            return accountNumber.isDisplayed() && 
                   accountName.isDisplayed() && 
                   customerName.isDisplayed() && 
                   currentBalance.isDisplayed();
        } catch (Exception e) {
            log.warn("Account information not fully displayed: {}", e.getMessage());
            return false;
        }
    }

    public String getAccountNumber() {
        try {
            wait.until(ExpectedConditions.visibilityOf(accountNumber));
            return accountNumber.getText();
        } catch (Exception e) {
            log.warn("Could not get account number: {}", e.getMessage());
            return "";
        }
    }

    public String getAccountName() {
        try {
            return accountName.getText();
        } catch (Exception e) {
            log.warn("Could not get account name: {}", e.getMessage());
            return "";
        }
    }

    public String getCustomerName() {
        try {
            return customerName.getText();
        } catch (Exception e) {
            log.warn("Could not get customer name: {}", e.getMessage());
            return "";
        }
    }

    public String getCurrentBalance() {
        try {
            return currentBalance.getText();
        } catch (Exception e) {
            log.warn("Could not get current balance: {}", e.getMessage());
            return "";
        }
    }

    public boolean isStatementFormDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(statementForm));
            return statementForm.isDisplayed() && 
                   startDateInput.isDisplayed() && 
                   endDateInput.isDisplayed() && 
                   generatePdfButton.isDisplayed();
        } catch (Exception e) {
            log.warn("Statement form not fully displayed: {}", e.getMessage());
            return false;
        }
    }

    public void setDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Setting date range: {} to {}", startDate, endDate);
        
        try {
            // Clear and set start date
            wait.until(ExpectedConditions.elementToBeClickable(startDateInput));
            startDateInput.clear();
            startDateInput.sendKeys(startDate.format(DATE_FORMATTER));
            
            // Clear and set end date
            wait.until(ExpectedConditions.elementToBeClickable(endDateInput));
            endDateInput.clear();
            endDateInput.sendKeys(endDate.format(DATE_FORMATTER));
            
            log.info("Date range set successfully");
        } catch (Exception e) {
            log.error("Failed to set date range: {}", e.getMessage());
            throw new RuntimeException("Could not set date range");
        }
    }

    public void setStartDate(LocalDate startDate) {
        log.info("Setting start date: {}", startDate);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(startDateInput));
            startDateInput.clear();
            startDateInput.sendKeys(startDate.format(DATE_FORMATTER));
        } catch (Exception e) {
            log.error("Failed to set start date: {}", e.getMessage());
            throw new RuntimeException("Could not set start date");
        }
    }

    public void setEndDate(LocalDate endDate) {
        log.info("Setting end date: {}", endDate);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(endDateInput));
            endDateInput.clear();
            endDateInput.sendKeys(endDate.format(DATE_FORMATTER));
        } catch (Exception e) {
            log.error("Failed to set end date: {}", e.getMessage());
            throw new RuntimeException("Could not set end date");
        }
    }

    public String getStartDateValue() {
        try {
            return startDateInput.getAttribute("value");
        } catch (Exception e) {
            return "";
        }
    }

    public String getEndDateValue() {
        try {
            return endDateInput.getAttribute("value");
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isGeneratePdfButtonEnabled() {
        try {
            return generatePdfButton.isEnabled() && generatePdfButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickGeneratePdf() {
        log.info("Clicking generate PDF button");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(generatePdfButton));
            generatePdfButton.click();
            log.info("Generate PDF button clicked");
            
            // Wait for loading indicator to appear and disappear (PDF generation)
            try {
                wait.until(ExpectedConditions.visibilityOf(loadingIndicator));
                log.info("Loading indicator appeared");
                wait.until(ExpectedConditions.invisibilityOf(loadingIndicator));
                log.info("Loading indicator disappeared - PDF generation completed");
            } catch (Exception e) {
                log.info("Loading indicator behavior not as expected, continuing: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Failed to click generate PDF button: {}", e.getMessage());
            throw new RuntimeException("Could not click generate PDF button");
        }
    }

    public boolean isLoadingIndicatorDisplayed() {
        try {
            return loadingIndicator.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void waitForPdfGeneration() {
        log.info("Waiting for PDF generation to complete");
        try {
            // Wait for loading indicator to appear
            wait.until(ExpectedConditions.visibilityOf(loadingIndicator));
            
            // Wait for loading indicator to disappear (PDF generated)
            wait.until(ExpectedConditions.invisibilityOf(loadingIndicator));
            
            log.info("PDF generation completed");
        } catch (Exception e) {
            log.info("PDF generation wait not as expected: {}", e.getMessage());
        }
    }

    public boolean isSuccessMessageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(successMessage));
            return successMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getSuccessMessage() {
        try {
            wait.until(ExpectedConditions.visibilityOf(successMessage));
            return successMessage.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isErrorMessageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(errorMessage));
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        try {
            wait.until(ExpectedConditions.visibilityOf(errorMessage));
            return errorMessage.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public void clickBackToAccounts() {
        log.info("Clicking back to accounts button");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(backButton));
            backButton.click();
            log.info("Back to accounts button clicked");
        } catch (Exception e) {
            log.error("Failed to click back button: {}", e.getMessage());
            throw new RuntimeException("Could not click back button");
        }
    }

    private void waitForPageLoad() {
        try {
            wait.until(ExpectedConditions.visibilityOf(pageTitle));
        } catch (Exception e) {
            log.warn("Page title not loaded within timeout: {}", e.getMessage());
        }
    }
}