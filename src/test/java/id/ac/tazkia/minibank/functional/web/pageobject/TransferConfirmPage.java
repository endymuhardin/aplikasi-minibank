package id.ac.tazkia.minibank.functional.web.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

@Slf4j
public class TransferConfirmPage extends BasePage {
    
    // Page elements
    private static final By PAGE_TITLE = By.id("page-title");
    private static final By CONFIRM_CHECKBOX = By.id("confirm");
    private static final By PROCESS_TRANSFER_BUTTON = By.id("process-transfer-btn");
    private static final By CANCEL_TRANSFER_BUTTON = By.xpath("//button[contains(text(), 'Batalkan Transfer')]");
    
    // Transfer details display elements
    private static final By TRANSFER_AMOUNT_DISPLAY = By.xpath("//span[contains(@th:text, 'transferRequest.amount')]");
    private static final By SOURCE_ACCOUNT_NUMBER = By.xpath("//div[contains(@class, 'bg-white')]//span[@class='font-mono text-gray-900']");
    private static final By DESTINATION_ACCOUNT_NUMBER = By.xpath("//div[contains(@class, 'bg-white')]//span[@class='font-mono text-gray-900']");
    
    public TransferConfirmPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    public void waitForPageLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(PAGE_TITLE));
        log.info("Transfer confirmation page loaded successfully");
    }
    
    public boolean isOnTransferConfirmPage() {
        return getCurrentUrl().contains("/transfer") && 
               isElementPresent(PAGE_TITLE) &&
               driver.getPageSource().contains("Konfirmasi Transfer Dana");
    }
    
    public void confirmTransfer() {
        log.info("Checking confirmation checkbox");
        WebElement confirmCheckbox = driver.findElement(CONFIRM_CHECKBOX);
        if (!confirmCheckbox.isSelected()) {
            scrollToElementAndClick(confirmCheckbox);
        }
    }
    
    public boolean isProcessButtonEnabled() {
        WebElement processButton = driver.findElement(PROCESS_TRANSFER_BUTTON);
        return processButton.isEnabled() && !processButton.getAttribute("disabled").equals("true");
    }
    
    public TransactionListPage processTransfer() {
        log.info("Clicking process transfer button");
        scrollToElementAndClick(PROCESS_TRANSFER_BUTTON);
        return new TransactionListPage(driver, baseUrl);
    }
    
    public void processTransferExpectingError() {
        log.info("Clicking process transfer button expecting error");
        scrollToElementAndClick(PROCESS_TRANSFER_BUTTON);
        // Stay on same page or handle error
    }
    
    public TransferFormPage cancelTransfer() {
        log.info("Clicking cancel transfer button");
        scrollToElementAndClick(CANCEL_TRANSFER_BUTTON);
        return new TransferFormPage(driver, baseUrl);
    }
    
    public String getTransferAmount() {
        // Try to find transfer amount from the page content
        // Since Thymeleaf renders server-side, we'll look for text patterns
        String pageSource = driver.getPageSource();
        // Extract amount from rendered content
        return "Amount displayed on page"; // Placeholder - would need actual implementation
    }
    
    public boolean isTransferDetailsDisplayed() {
        return driver.getPageSource().contains("Detail Transfer") &&
               driver.getPageSource().contains("Rekening Pengirim") &&
               driver.getPageSource().contains("Rekening Penerima");
    }
    
    public boolean isWarningMessageDisplayed() {
        return driver.getPageSource().contains("Periksa Detail Transfer") &&
               driver.getPageSource().contains("Transfer yang telah diproses tidak dapat dibatalkan");
    }
}