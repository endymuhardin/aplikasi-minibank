package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ProductViewPage extends BasePage {
    
    @FindBy(id = "product-code")
    private WebElement productCodeElement;
    
    @FindBy(id = "product-name")
    private WebElement productNameElement;
    
    @FindBy(id = "product-status")
    private WebElement productStatusElement;
    
    @FindBy(id = "interest-rate")
    private WebElement interestRateElement;
    
    @FindBy(linkText = "Edit Product")
    private WebElement editProductButton;
    
    @FindBy(linkText = "Back to List")
    private WebElement backToListButton;
    
    public ProductViewPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    public String getProductCode() {
        waitForElementToBeVisible(productCodeElement);
        return productCodeElement.getText();
    }
    
    public String getProductName() {
        waitForElementToBeVisible(productNameElement);
        return productNameElement.getText();
    }
    
    public String getProductStatus() {
        waitForElementToBeVisible(productStatusElement);
        return productStatusElement.getText();
    }
    
    public String getInterestRate() {
        waitForElementToBeVisible(interestRateElement);
        return interestRateElement.getText();
    }
    
    public ProductFormPage clickEditProduct() {
        waitForElementToBeClickable(editProductButton);
        editProductButton.click();
        return new ProductFormPage(driver, baseUrl);
    }
    
    public ProductListPage clickBackToList() {
        waitForElementToBeClickable(backToListButton);
        backToListButton.click();
        return new ProductListPage(driver, baseUrl);
    }
    
    public boolean isProductDisplayed() {
        return isElementVisible(productCodeElement) && 
               isElementVisible(productNameElement) && 
               isElementVisible(productStatusElement);
    }
    
    public String getPageTitle() {
        return driver.getTitle();
    }
}