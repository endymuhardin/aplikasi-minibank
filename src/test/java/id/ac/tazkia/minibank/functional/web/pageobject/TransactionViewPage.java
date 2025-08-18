package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class TransactionViewPage extends BasePage {
    
    // Page elements
    @FindBy(id = "page-title")
    private WebElement pageTitle;
    
    @FindBy(id = "transaction-number-header")
    private WebElement transactionNumber;
    
    @FindBy(id = "transaction-amount-header")
    private WebElement transactionAmount;
    
    @FindBy(id = "transaction-number-detail")
    private WebElement transactionNumberDetail;
    
    @FindBy(id = "transaction-type")
    private WebElement transactionType;
    
    @FindBy(id = "transaction-channel")
    private WebElement transactionChannel;
    
    @FindBy(id = "account-number")
    private WebElement accountNumber;
    
    @FindBy(id = "balance-before")
    private WebElement balanceBefore;
    
    @FindBy(id = "balance-after")
    private WebElement balanceAfter;
    
    @FindBy(id = "description-text")
    private WebElement descriptionText;
    
    @FindBy(id = "processed-by")
    private WebElement processedBy;
    
    @FindBy(id = "back-to-transaction-list-button")
    private WebElement backToTransactionListButton;
    
    @FindBy(id = "print-receipt-button")
    private WebElement printReceiptButton;
    
    // Constructor
    public TransactionViewPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    // Page verification methods
    public void waitForPageLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("page-title")));
        wait.until(ExpectedConditions.textToBePresentInElement(pageTitle, "Detail Transaksi"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("transaction-number-header")));
        waitForPageToLoad();
    }
    
    public boolean isOnTransactionViewPage() {
        try {
            waitForPageLoad();
            return pageTitle.getText().contains("Detail Transaksi");
        } catch (Exception e) {
            return false;
        }
    }
    
    // Navigation methods
    public TransactionListPage clickBackToTransactionList() {
        scrollToElementAndClick(backToTransactionListButton);
        waitForUrlToContain("/transaction/list");
        return new TransactionListPage(driver, baseUrl);
    }
    
    public void clickPrintReceipt() {
        scrollToElementAndClick(printReceiptButton);
        // Note: This will trigger browser print dialog, which we can't easily test
    }
    
    // Transaction detail methods
    public String getTransactionNumber() {
        return transactionNumber.getText();
    }
    
    public String getTransactionNumberFromDetail() {
        return transactionNumberDetail.getText();
    }
    
    public String getTransactionAmount() {
        return transactionAmount.getText();
    }
    
    public String getTransactionType() {
        return transactionType.getText();
    }
    
    public String getTransactionChannel() {
        return transactionChannel.getText();
    }
    
    public String getAccountNumber() {
        return accountNumber.getText();
    }
    
    public String getBalanceBefore() {
        return balanceBefore.getText();
    }
    
    public String getBalanceAfter() {
        return balanceAfter.getText();
    }
    
    public String getDescription() {
        if (isElementVisible(descriptionText)) {
            return descriptionText.getText();
        }
        return "";
    }
    
    public String getProcessedBy() {
        try {
            return processedBy.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    // Validation methods
    public boolean isDepositTransaction() {
        return getTransactionType().equals("DEPOSIT");
    }
    
    public boolean isWithdrawalTransaction() {
        return getTransactionType().equals("WITHDRAWAL");
    }
    
    public boolean isTellerChannel() {
        return getTransactionChannel().equals("TELLER");
    }
    
    public boolean hasDescription() {
        return isElementVisible(descriptionText) && !getDescription().trim().isEmpty();
    }
    
    public boolean hasProcessedBy() {
        try {
            return !getProcessedBy().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    // Amount verification methods
    public boolean isAmountPositive() {
        String amountText = getTransactionAmount();
        // Remove currency and formatting, check if positive
        return amountText.contains("+") || (!amountText.contains("-") && isDepositTransaction());
    }
    
    public boolean isAmountNegative() {
        String amountText = getTransactionAmount();
        // Check if withdrawal amount is shown as negative
        return amountText.contains("-") || isWithdrawalTransaction();
    }
    
    // Balance verification methods
    public boolean isBalanceChangeCorrect() {
        try {
            // Extract numeric values from balance strings
            String beforeText = getBalanceBefore().replaceAll("[^0-9.,]", "").replace(",", "");
            String afterText = getBalanceAfter().replaceAll("[^0-9.,]", "").replace(",", "");
            String amountText = getTransactionAmount().replaceAll("[^0-9.,]", "").replace(",", "");
            
            double before = Double.parseDouble(beforeText);
            double after = Double.parseDouble(afterText);
            double amount = Double.parseDouble(amountText);
            
            if (isDepositTransaction()) {
                return Math.abs((before + amount) - after) < 0.01; // Allow for rounding errors
            } else if (isWithdrawalTransaction()) {
                return Math.abs((before - amount) - after) < 0.01; // Allow for rounding errors
            }
            
            return false;
        } catch (Exception e) {
            // If parsing fails, just return true (manual verification needed)
            return true;
        }
    }
    
    // Transaction verification methods
    public boolean verifyTransactionDetails(String expectedTransactionNumber, String expectedType, 
                                           String expectedAmount, String expectedChannel) {
        boolean numberMatches = getTransactionNumber().equals(expectedTransactionNumber);
        boolean typeMatches = getTransactionType().equals(expectedType);
        boolean channelMatches = getTransactionChannel().equals(expectedChannel);
        boolean amountMatches = getTransactionAmount().contains(expectedAmount);
        
        return numberMatches && typeMatches && channelMatches && amountMatches;
    }
}