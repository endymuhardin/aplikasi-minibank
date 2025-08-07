package id.ac.tazkia.minibank.integration.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ProductFormPage extends BasePage {
    
    @FindBy(id = "productCode")
    private WebElement productCodeField;
    
    @FindBy(id = "productName")
    private WebElement productNameField;
    
    @FindBy(id = "productType")
    private WebElement productTypeDropdown;
    
    @FindBy(id = "productCategory")
    private WebElement productCategoryField;
    
    @FindBy(id = "currency")
    private WebElement currencyField;
    
    @FindBy(id = "description")
    private WebElement descriptionField;
    
    @FindBy(id = "isActive")
    private WebElement isActiveCheckbox;
    
    @FindBy(id = "isDefault")
    private WebElement isDefaultCheckbox;
    
    @FindBy(id = "allowOverdraft")
    private WebElement allowOverdraftCheckbox;
    
    @FindBy(id = "requireMaintainingBalance")
    private WebElement requireMaintainingBalanceCheckbox;
    
    @FindBy(id = "minimumOpeningBalance")
    private WebElement minimumOpeningBalanceField;
    
    @FindBy(id = "minimumBalance")
    private WebElement minimumBalanceField;
    
    @FindBy(id = "maximumBalance")
    private WebElement maximumBalanceField;
    
    @FindBy(id = "interestRate")
    private WebElement interestRateField;
    
    @FindBy(id = "interestCalculationType")
    private WebElement interestCalculationTypeDropdown;
    
    @FindBy(id = "interestPaymentFrequency")
    private WebElement interestPaymentFrequencyDropdown;
    
    @FindBy(id = "dailyWithdrawalLimit")
    private WebElement dailyWithdrawalLimitField;
    
    @FindBy(id = "monthlyTransactionLimit")
    private WebElement monthlyTransactionLimitField;
    
    @FindBy(id = "overdraftLimit")
    private WebElement overdraftLimitField;
    
    @FindBy(id = "monthlyMaintenanceFee")
    private WebElement monthlyMaintenanceFeeField;
    
    @FindBy(id = "atmWithdrawalFee")
    private WebElement atmWithdrawalFeeField;
    
    @FindBy(id = "interBankTransferFee")
    private WebElement interBankTransferFeeField;
    
    @FindBy(id = "freeTransactionsPerMonth")
    private WebElement freeTransactionsPerMonthField;
    
    @FindBy(id = "excessTransactionFee")
    private WebElement excessTransactionFeeField;
    
    @FindBy(id = "belowMinimumBalanceFee")
    private WebElement belowMinimumBalanceFeeField;
    
    @FindBy(id = "minCustomerAge")
    private WebElement minCustomerAgeField;
    
    @FindBy(id = "maxCustomerAge")
    private WebElement maxCustomerAgeField;
    
    @FindBy(id = "allowedCustomerTypes")
    private WebElement allowedCustomerTypesField;
    
    @FindBy(id = "requiredDocuments")
    private WebElement requiredDocumentsField;
    
    @FindBy(id = "launchDate")
    private WebElement launchDateField;
    
    @FindBy(id = "retirementDate")
    private WebElement retirementDateField;
    
    @FindBy(id = "submit-btn")
    private WebElement submitButton;
    
    @FindBy(id = "product-form")
    private WebElement productForm;
    
    public ProductFormPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    public ProductFormPage fillBasicInformation(String productCode, String productName, String productType, String category, String currency) {
        if (productCode != null) {
            clearAndType(productCodeField, productCode);
        }
        if (productName != null) {
            clearAndType(productNameField, productName);
        }
        if (productType != null) {
            if (productType.isEmpty()) {
                // For empty product type, select the empty option
                selectDropdownByValue(productTypeDropdown, "");
            } else {
                selectDropdownByText(productTypeDropdown, productType);
            }
        }
        if (category != null) clearAndType(productCategoryField, category);
        if (currency != null) clearAndType(currencyField, currency);
        return this;
    }
    
    public ProductFormPage fillDescription(String description) {
        if (description != null) clearAndType(descriptionField, description);
        return this;
    }
    
    public ProductFormPage setSettings(Boolean isActive, Boolean isDefault, Boolean allowOverdraft, Boolean requireMaintainingBalance) {
        if (isActive != null) setCheckbox(isActiveCheckbox, isActive);
        if (isDefault != null) setCheckbox(isDefaultCheckbox, isDefault);
        if (allowOverdraft != null) setCheckbox(allowOverdraftCheckbox, allowOverdraft);
        if (requireMaintainingBalance != null) setCheckbox(requireMaintainingBalanceCheckbox, requireMaintainingBalance);
        return this;
    }
    
    public ProductFormPage fillFinancialConfiguration(String minimumOpeningBalance, String minimumBalance, String maximumBalance,
                                                   String interestRate, String interestCalculationType, String interestPaymentFrequency) {
        if (minimumOpeningBalance != null) clearAndType(minimumOpeningBalanceField, minimumOpeningBalance);
        if (minimumBalance != null) clearAndType(minimumBalanceField, minimumBalance);
        if (maximumBalance != null) clearAndType(maximumBalanceField, maximumBalance);
        if (interestRate != null) clearAndType(interestRateField, interestRate);
        if (interestCalculationType != null) selectDropdownByText(interestCalculationTypeDropdown, interestCalculationType);
        if (interestPaymentFrequency != null) selectDropdownByText(interestPaymentFrequencyDropdown, interestPaymentFrequency);
        return this;
    }
    
    public ProductFormPage fillLimitsAndFees(String dailyWithdrawalLimit, String monthlyTransactionLimit, String overdraftLimit,
                                            String monthlyMaintenanceFee, String atmWithdrawalFee, String interBankTransferFee) {
        if (dailyWithdrawalLimit != null) clearAndType(dailyWithdrawalLimitField, dailyWithdrawalLimit);
        if (monthlyTransactionLimit != null) clearAndType(monthlyTransactionLimitField, monthlyTransactionLimit);
        if (overdraftLimit != null) clearAndType(overdraftLimitField, overdraftLimit);
        if (monthlyMaintenanceFee != null) clearAndType(monthlyMaintenanceFeeField, monthlyMaintenanceFee);
        if (atmWithdrawalFee != null) clearAndType(atmWithdrawalFeeField, atmWithdrawalFee);
        if (interBankTransferFee != null) clearAndType(interBankTransferFeeField, interBankTransferFee);
        return this;
    }
    
    public ProductFormPage fillTransactionSettings(String freeTransactionsPerMonth, String excessTransactionFee, String belowMinimumBalanceFee) {
        if (freeTransactionsPerMonth != null) clearAndType(freeTransactionsPerMonthField, freeTransactionsPerMonth);
        if (excessTransactionFee != null) clearAndType(excessTransactionFeeField, excessTransactionFee);
        if (belowMinimumBalanceFee != null) clearAndType(belowMinimumBalanceFeeField, belowMinimumBalanceFee);
        return this;
    }
    
    public ProductFormPage fillCustomerEligibility(String minAge, String maxAge, String allowedTypes, String requiredDocs) {
        if (minAge != null) clearAndType(minCustomerAgeField, minAge);
        if (maxAge != null) clearAndType(maxCustomerAgeField, maxAge);
        if (allowedTypes != null) clearAndType(allowedCustomerTypesField, allowedTypes);
        if (requiredDocs != null) clearAndType(requiredDocumentsField, requiredDocs);
        return this;
    }
    
    public ProductFormPage fillProductLifecycle(String launchDate, String retirementDate) {
        if (launchDate != null) clearAndType(launchDateField, launchDate);
        if (retirementDate != null) clearAndType(retirementDateField, retirementDate);
        return this;
    }
    
    public ProductListPage submitForm() {
        waitForElementToBeClickable(submitButton);
        submitButton.click();
        
        // Wait for redirect to list page
        waitForUrlToContain("/product/list");
        
        return new ProductListPage(driver, baseUrl);
    }
    
    public ProductFormPage submitFormExpectingError() {
        waitForElementToBeClickable(submitButton);
        submitButton.click();
        return this;
    }
    
    public boolean hasValidationError(String fieldId) {
        try {
            // Avoid stale element references by finding elements fresh each time
            // Look for validation error indicators without relying on color classes
            
            // Look for validation messages or error indicators
            boolean hasErrorMessage = false;
            
            try {
                // Look for required field validation message
                WebElement errorElement = driver.findElement(
                    org.openqa.selenium.By.xpath("//input[@id='" + fieldId + "']/following-sibling::*[contains(text(), 'required') or contains(text(), 'must') or contains(text(), 'blank')]"));
                hasErrorMessage = errorElement.isDisplayed();
            } catch (org.openqa.selenium.NoSuchElementException e) {
                // Try alternative selectors for error messages
                try {
                    WebElement errorElement = driver.findElement(
                        org.openqa.selenium.By.xpath("//input[@id='" + fieldId + "']/parent::*//*[contains(text(), 'required') or contains(text(), 'must') or contains(text(), 'blank')]"));
                    hasErrorMessage = errorElement.isDisplayed();
                } catch (org.openqa.selenium.NoSuchElementException e2) {
                    // Check for HTML5 validation state
                    try {
                        WebElement field = driver.findElement(org.openqa.selenium.By.id(fieldId));
                        String validationMessage = field.getAttribute("validationMessage");
                        hasErrorMessage = validationMessage != null && !validationMessage.isEmpty();
                    } catch (org.openqa.selenium.StaleElementReferenceException e3) {
                        // Element became stale, try one more time
                        try {
                            WebElement field = driver.findElement(org.openqa.selenium.By.id(fieldId));
                            String validationMessage = field.getAttribute("validationMessage");
                            hasErrorMessage = validationMessage != null && !validationMessage.isEmpty();
                        } catch (Exception e4) {
                            // If all approaches fail, assume no validation error
                            hasErrorMessage = false;
                        }
                    }
                }
            }
            
            return hasErrorMessage;
        } catch (Exception e) {
            // If any error occurs, assume no validation error
            return false;
        }
    }
    
    public String getValidationErrorMessage(String fieldId) {
        WebElement errorElement = driver.findElement(
            org.openqa.selenium.By.xpath("//input[@id='" + fieldId + "']/following-sibling::p[contains(@class, 'text-red-600')]"));
        return errorElement.getText();
    }
    
    public String getProductCode() {
        return productCodeField.getAttribute("value");
    }
    
    public String getProductName() {
        return productNameField.getAttribute("value");
    }
    
    public String getProductType() {
        return getSelectedDropdownText(productTypeDropdown);
    }
    
    public boolean isActiveChecked() {
        return isActiveCheckbox.isSelected();
    }
    
    public boolean isDefaultChecked() {
        return isDefaultCheckbox.isSelected();
    }
    
    private void setCheckbox(WebElement checkbox, boolean checked) {
        if (checkbox.isSelected() != checked) {
            checkbox.click();
        }
    }
    
    private String getSelectedDropdownText(WebElement dropdown) {
        org.openqa.selenium.support.ui.Select select = new org.openqa.selenium.support.ui.Select(dropdown);
        return select.getFirstSelectedOption().getText();
    }
    
    public ProductFormPage fillCompleteProduct(String productCode, String productName, String productType, 
                                             String category, String description, String interestRate) {
        fillBasicInformation(productCode, productName, productType, category, "IDR");
        fillDescription(description);
        setSettings(true, false, false, true);
        fillFinancialConfiguration("100000", "50000", null, interestRate, "DAILY", "MONTHLY");
        return this;
    }
}