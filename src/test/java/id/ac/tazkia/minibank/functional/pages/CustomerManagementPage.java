package id.ac.tazkia.minibank.functional.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomerManagementPage {
    
    private final Page page;
    
    // Customer list elements
    private final Locator customerTable;
    private final Locator searchInput;
    private final Locator searchButton;
    private final Locator addCustomerButton;
    private final Locator customerRows;
    
    // Customer form elements
    private final Locator customerTypeSelect;
    private final Locator customerNumberInput;
    private final Locator customerNameInput;
    private final Locator lastNameInput;
    private final Locator aliasNameInput;
    private final Locator branchSelect;
    private final Locator identityNumberInput;
    private final Locator identityTypeSelect;
    private final Locator birthDateInput;
    private final Locator birthPlaceInput;
    private final Locator genderSelect;
    private final Locator motherNameInput;
    private final Locator emailInput;
    private final Locator phoneNumberInput;
    private final Locator addressTextarea;
    private final Locator cityInput;
    private final Locator provinceInput;
    private final Locator postalCodeInput;
    private final Locator countryInput;

    // FR.002 Personal Data fields
    private final Locator educationSelect;
    private final Locator religionSelect;
    private final Locator maritalStatusSelect;
    private final Locator dependentsSelect;

    // FR.002 Identity fields
    private final Locator citizenshipSelect;
    private final Locator residencyStatusInput;
    private final Locator identityExpiryDateInput;

    // FR.002 Employment Data fields
    private final Locator occupationInput;
    private final Locator companyNamePersonalInput;
    private final Locator companyAddressInput;
    private final Locator businessFieldInput;
    private final Locator monthlyIncomeInput;
    private final Locator sourceOfFundsInput;
    private final Locator accountPurposeInput;
    private final Locator estimatedMonthlyTransactionsInput;
    private final Locator estimatedTransactionAmountInput;
    
    // Corporate customer specific fields
    private final Locator companyNameInput;
    private final Locator npwpInput;
    private final Locator sibInput;
    private final Locator contactPersonNameInput;
    private final Locator contactPersonTitleInput;
    
    // Form buttons
    private final Locator saveButton;
    private final Locator cancelButton;
    private final Locator backButton;
    
    // Messages
    private final Locator successMessage;
    private final Locator errorMessage;
    private final Locator validationErrors;
    
    public CustomerManagementPage(Page page) {
        this.page = page;
        
        // Initialize customer list locators
        this.customerTable = page.locator("#customer-table");
        this.searchInput = page.locator("input[name='search']");
        this.searchButton = page.locator("#search-btn");
        this.addCustomerButton = page.locator("#create-customer-btn[href='/customer/create']");
        this.customerRows = page.locator("table tr");
        
        // Initialize form locators (personal form)
        this.customerTypeSelect = page.locator("input[name='customerType']");
        this.customerNumberInput = page.locator("#customerNumber");
        this.customerNameInput = page.locator("#firstName"); // Use firstName as primary name field
        this.lastNameInput = page.locator("#lastName");
        this.aliasNameInput = page.locator("#aliasName");
        this.branchSelect = page.locator("#branch");
        this.identityNumberInput = page.locator("#identityNumber");
        this.identityTypeSelect = page.locator("#identityType");
        this.birthDateInput = page.locator("#dateOfBirth");
        this.birthPlaceInput = page.locator("#birthPlace");
        this.genderSelect = page.locator("#gender");
        this.motherNameInput = page.locator("#motherName");
        this.emailInput = page.locator("#email");
        this.phoneNumberInput = page.locator("#phoneNumber");
        this.addressTextarea = page.locator("#address");
        this.cityInput = page.locator("#city");
        this.provinceInput = page.locator("#province");
        this.postalCodeInput = page.locator("#postalCode");
        this.countryInput = page.locator("#country");

        // Initialize FR.002 Personal Data field locators
        this.educationSelect = page.locator("#education");
        this.religionSelect = page.locator("#religion");
        this.maritalStatusSelect = page.locator("#maritalStatus");
        this.dependentsSelect = page.locator("#dependents");

        // Initialize FR.002 Identity field locators
        this.citizenshipSelect = page.locator("#citizenship");
        this.residencyStatusInput = page.locator("#residencyStatus");
        this.identityExpiryDateInput = page.locator("#identityExpiryDate");

        // Initialize FR.002 Employment Data field locators
        this.occupationInput = page.locator("#occupation");
        this.companyNamePersonalInput = page.locator("#companyName");
        this.companyAddressInput = page.locator("#companyAddress");
        this.businessFieldInput = page.locator("#businessField");
        this.monthlyIncomeInput = page.locator("#monthlyIncome");
        this.sourceOfFundsInput = page.locator("#sourceOfFunds");
        this.accountPurposeInput = page.locator("#accountPurpose");
        this.estimatedMonthlyTransactionsInput = page.locator("#estimatedMonthlyTransactions");
        this.estimatedTransactionAmountInput = page.locator("#estimatedTransactionAmount");
        
        // Corporate specific fields
        this.companyNameInput = page.locator("#companyName");
        this.npwpInput = page.locator("#taxIdentificationNumber");
        this.sibInput = page.locator("#companyRegistrationNumber");
        this.contactPersonNameInput = page.locator("#contactPersonName");
        this.contactPersonTitleInput = page.locator("#contactPersonTitle");
        
        // Form buttons
        this.saveButton = page.locator("#submit-button");
        this.cancelButton = page.locator("#cancel-button");
        this.backButton = page.locator("#back-to-type-selection-link, #back-to-list-link");
        
        // Messages
        this.successMessage = page.locator("#success-message");
        this.errorMessage = page.locator("#error-message");
        this.validationErrors = page.locator("#validation-errors");
    }
    
    /**
     * Navigate to customer list page
     */
    public CustomerManagementPage navigateToList(String baseUrl) {
        page.navigate(baseUrl + "/customer/list");
        waitForListPageLoad();
        return this;
    }
    
    /**
     * Navigate to add customer page
     */
    public CustomerManagementPage navigateToAddCustomer(String baseUrl) {
        page.navigate(baseUrl + "/customer/create");
        return this;
    }
    
    /**
     * Wait for customer list page to load
     */
    public CustomerManagementPage waitForListPageLoad() {
        page.waitForLoadState();
        searchInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        log.debug("Customer list page loaded");
        return this;
    }
    
    /**
     * Wait for customer form to load
     */
    public CustomerManagementPage waitForFormLoad() {
        page.waitForLoadState();
        saveButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        log.debug("Customer form loaded");
        return this;
    }
    
    /**
     * Click add customer button
     */
    public CustomerManagementPage clickAddCustomer() {
        addCustomerButton.click();
        log.debug("Add customer button clicked");
        return this;
    }
    
    /**
     * Select customer type (PERSONAL or CORPORATE)
     */
    public CustomerManagementPage selectCustomerType(String type) {
        if (customerTypeSelect.isVisible()) {
            customerTypeSelect.selectOption(type);
            log.debug("Selected customer type: {}", type);
        } else {
            // On select-type page, click the appropriate button
            if ("PERSONAL".equals(type)) {
                page.locator("#create-personal-customer-link").click();
            } else {
                page.locator("#create-corporate-customer-link").click();
            }
        }
        return this;
    }
    
    /**
     * Fill personal customer form - Original method (backward compatibility)
     * For tests that don't need FR.002 fields
     */
    public CustomerManagementPage fillPersonalCustomerForm(
            String name, String identityNumber, String identityType,
            String birthDate, String birthPlace, String gender,
            String motherName, String email, String phone,
            String address, String city, String province, String postalCode) {

        // Call the extended version with null values for FR.002 fields
        return fillPersonalCustomerFormExtended(
            name, null, identityNumber, identityType, birthDate, birthPlace, gender,  motherName,
            null, null, null, null,
            null, null, null,
            email, phone, address, city, province, postalCode,
            null, null, null, null, null, null, null, null, null
        );
    }

    /**
     * Fill personal customer form - Extended version with FR.002 fields
     */
    public CustomerManagementPage fillPersonalCustomerFormExtended(
            // Basic Personal Information
            String name, String aliasName, String identityNumber, String identityType,
            String birthDate, String birthPlace, String gender, String motherName,
            // Personal Data (FR.002)
            String education, String religion, String maritalStatus, String dependents,
            // Identity Information (FR.002)
            String citizenship, String residencyStatus, String identityExpiryDate,
            // Contact Information
            String email, String phone, String address, String city, String province, String postalCode,
            // Employment Data (FR.002)
            String occupation, String companyName, String companyAddress, String businessField,
            String monthlyIncome, String sourceOfFunds, String accountPurpose,
            String estimatedMonthlyTransactions, String estimatedTransactionAmount) {

        // Basic Personal Information
        if (name != null) {
            String[] nameParts = name.split(" ", 2);
            if (customerNameInput.isVisible()) {
                customerNameInput.fill(nameParts[0]); // firstName field
            }
            if (lastNameInput.isVisible() && nameParts.length > 1) {
                lastNameInput.fill(nameParts[1]); // lastName field
            }
        }

        if (aliasName != null && aliasNameInput.isVisible()) aliasNameInput.fill(aliasName);
        if (birthDate != null && birthDateInput.isVisible()) birthDateInput.fill(birthDate);
        if (birthPlace != null && birthPlaceInput.isVisible()) birthPlaceInput.fill(birthPlace);
        if (gender != null && genderSelect.isVisible()) genderSelect.selectOption(gender);
        if (motherName != null && motherNameInput.isVisible()) motherNameInput.fill(motherName);

        // Personal Data (FR.002)
        if (education != null && educationSelect.isVisible()) educationSelect.selectOption(education);
        if (religion != null && religionSelect.isVisible()) religionSelect.selectOption(religion);
        if (maritalStatus != null && maritalStatusSelect.isVisible()) maritalStatusSelect.selectOption(maritalStatus);
        if (dependents != null && dependentsSelect.isVisible()) dependentsSelect.selectOption(dependents);

        // Identity Information
        if (identityNumber != null && identityNumberInput.isVisible()) identityNumberInput.fill(identityNumber);
        if (identityType != null && identityTypeSelect.isVisible()) identityTypeSelect.selectOption(identityType);
        if (citizenship != null && citizenshipSelect.isVisible()) citizenshipSelect.selectOption(citizenship);
        if (residencyStatus != null && residencyStatusInput.isVisible()) residencyStatusInput.fill(residencyStatus);
        if (identityExpiryDate != null && identityExpiryDateInput.isVisible()) identityExpiryDateInput.fill(identityExpiryDate);

        // Contact Information
        if (email != null && emailInput.isVisible()) emailInput.fill(email);
        if (phone != null && phoneNumberInput.isVisible()) phoneNumberInput.fill(phone);
        if (address != null && addressTextarea.isVisible()) addressTextarea.fill(address);
        if (city != null && cityInput.isVisible()) cityInput.fill(city);
        if (province != null && provinceInput.isVisible()) provinceInput.fill(province);
        if (postalCode != null && postalCodeInput.isVisible()) postalCodeInput.fill(postalCode);

        // Employment Data (FR.002)
        if (occupation != null && occupationInput.isVisible()) occupationInput.fill(occupation);
        if (companyName != null && companyNamePersonalInput.isVisible()) companyNamePersonalInput.fill(companyName);
        if (companyAddress != null && companyAddressInput.isVisible()) companyAddressInput.fill(companyAddress);
        if (businessField != null && businessFieldInput.isVisible()) businessFieldInput.fill(businessField);
        if (monthlyIncome != null && monthlyIncomeInput.isVisible()) monthlyIncomeInput.fill(monthlyIncome);
        if (sourceOfFunds != null && sourceOfFundsInput.isVisible()) sourceOfFundsInput.fill(sourceOfFunds);
        if (accountPurpose != null && accountPurposeInput.isVisible()) accountPurposeInput.fill(accountPurpose);
        if (estimatedMonthlyTransactions != null && estimatedMonthlyTransactionsInput.isVisible())
            estimatedMonthlyTransactionsInput.fill(estimatedMonthlyTransactions);
        if (estimatedTransactionAmount != null && estimatedTransactionAmountInput.isVisible())
            estimatedTransactionAmountInput.fill(estimatedTransactionAmount);

        log.debug("Personal customer form filled (extended) for: {}", name);
        return this;
    }
    
    /**
     * Fill corporate customer form - only fields that actually exist
     */
    public CustomerManagementPage fillCorporateCustomerForm(
            String companyName, String companyRegistrationNumber, String taxIdentificationNumber, 
            String contactPersonName, String contactPersonTitle, String email, 
            String phone, String address, String city, String postalCode, String country) {
        
        log.debug("fillCorporateCustomerForm called with:");
        log.debug("  companyName: {}", companyName);
        log.debug("  companyRegistrationNumber: {}", companyRegistrationNumber);
        log.debug("  taxIdentificationNumber: {}", taxIdentificationNumber);
        log.debug("  contactPersonName: {}", contactPersonName);
        log.debug("  contactPersonTitle: {}", contactPersonTitle);
        log.debug("  email: {}", email);
        log.debug("  phone: {}", phone);
        log.debug("  address: {}", address);
        log.debug("  city: {}", city);
        log.debug("  postalCode: {}", postalCode);
        log.debug("  country: {}", country);
        
        if (companyName != null && companyNameInput.isVisible()) {
            log.debug("Filling companyName field (#companyName) with: {}", companyName);
            companyNameInput.fill(companyName);
        }
        
        if (taxIdentificationNumber != null && npwpInput.isVisible()) {
            log.debug("Filling taxIdentificationNumber field (#taxIdentificationNumber) with: {}", taxIdentificationNumber);
            npwpInput.fill(taxIdentificationNumber);
        }
        if (companyRegistrationNumber != null && sibInput.isVisible()) {
            log.debug("Filling companyRegistrationNumber field (#companyRegistrationNumber) with: {}", companyRegistrationNumber);
            sibInput.fill(companyRegistrationNumber);
        }
        
        // Fill contact person fields from CSV data
        if (contactPersonName != null && contactPersonNameInput.isVisible()) {
            log.debug("Filling contactPersonName field (#contactPersonName) with: {}", contactPersonName);
            contactPersonNameInput.fill(contactPersonName);
        }
        if (contactPersonTitle != null && contactPersonTitleInput.isVisible()) {
            log.debug("Filling contactPersonTitle field (#contactPersonTitle) with: {}", contactPersonTitle);
            contactPersonTitleInput.fill(contactPersonTitle);
        }
        
        if (email != null && emailInput.isVisible()) {
            log.debug("Filling email field with: {}", email);
            emailInput.fill(email);
        }
        if (phone != null && phoneNumberInput.isVisible()) {
            log.debug("Filling phoneNumber field with: {}", phone);
            phoneNumberInput.fill(phone);
        }
        if (address != null && addressTextarea.isVisible()) {
            log.debug("Filling address field with: {}", address);
            addressTextarea.fill(address);
        }
        if (city != null && cityInput.isVisible()) {
            log.debug("Filling city field (#city) with: {}", city);
            cityInput.fill(city);
        }
        if (postalCode != null && postalCodeInput.isVisible()) {
            log.debug("Filling postalCode field (#postalCode) with: {}", postalCode);
            postalCodeInput.fill(postalCode);
        }
        if (country != null && countryInput.isVisible()) {
            log.debug("Filling country field with: {}", country);
            countryInput.fill(country);
        }
        
        log.debug("Corporate customer form filled for: {}", companyName);
        return this;
    }
    
    /**
     * Click save button
     */
    public CustomerManagementPage clickSave() {
        saveButton.click();
        log.debug("Save button clicked");
        return this;
    }
    
    /**
     * Search for customer
     */
    public CustomerManagementPage searchCustomer(String searchTerm) {
        searchInput.fill(searchTerm);
        searchButton.click();
        page.waitForLoadState();
        log.debug("Searched for customer: {}", searchTerm);
        return this;
    }
    
    /**
     * Get number of customer rows in table (excluding header and empty row)
     */
    public int getCustomerRowCount() {
        // Check if the "no customers found" row is present using its specific ID
        if (page.locator("#no-customers-row").count() > 0 && page.locator("#no-customers-row").isVisible()) {
            return 0;
        }
        // Count tbody rows, excluding the "no customers found" row
        int totalRows = page.locator("#search-results tr").count();
        // Subtract 1 if no-customers-row is present (though it should be invisible when there are customers)
        if (page.locator("#no-customers-row").count() > 0) {
            totalRows = totalRows - 1;
        }
        return Math.max(0, totalRows);
    }
    
    /**
     * Click on customer row by index
     */
    public CustomerManagementPage clickCustomerRow(int index) {
        customerRows.nth(index).click();
        log.debug("Clicked customer row at index: {}", index);
        return this;
    }
    
    /**
     * Click on customer by customer number (e.g., C1000001)
     */
    public CustomerManagementPage clickCustomerByNumber(String customerNumber) {
        // Require exact customer number format for predictable behavior
        if (!customerNumber.matches("C\\d+")) {
            throw new IllegalArgumentException("Customer identifier must be in format C1000001, got: " + customerNumber);
        }
        
        String viewLinkId = "#view-" + customerNumber;
        page.locator(viewLinkId).click();
        page.waitForLoadState();
        log.debug("Clicked View link for customer number: {}", customerNumber);
        return this;
    }
    
    /**
     * Click on customer by name - converts name to customer number first
     */
    public CustomerManagementPage clickCustomerByName(String customerName) {
        String customerNumber = getCustomerNumberByName(customerName);
        return clickCustomerByNumber(customerNumber);
    }
    
    /**
     * Check if success message is visible
     */
    public boolean isSuccessMessageVisible() {
        try {
            return successMessage.isVisible();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get success message text
     */
    public String getSuccessMessage() {
        if (isSuccessMessageVisible()) {
            return successMessage.textContent();
        }
        return "";
    }
    
    /**
     * Check if error message is visible
     */
    public boolean isErrorMessageVisible() {
        try {
            return errorMessage.isVisible();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get error message text
     */
    public String getErrorMessage() {
        if (isErrorMessageVisible()) {
            return errorMessage.textContent();
        }
        return "";
    }
    
    /**
     * Check if validation errors are visible
     */
    public boolean hasValidationErrors() {
        return validationErrors.count() > 0;
    }
    
    /**
     * Check if operation was successful by checking success message OR successful redirect
     */
    public boolean isOperationSuccessful() {
        // Wait for any redirects to complete
        page.waitForLoadState(LoadState.NETWORKIDLE);
        
        // Check for success message
        if (isSuccessMessageVisible()) {
            log.debug("Success message found: {}", getSuccessMessage());
            return true;
        }
        
        // Check if redirected to customer view page
        if (isOnViewPage()) {
            log.debug("Redirected to view page: {}", page.url());
            return true;
        }
        
        // Check if redirected to customer list page (also indicates success)
        if (page.url().contains("/customer/list")) {
            log.debug("Redirected to list page: {}", page.url());
            return true;
        }
        
        log.warn("Operation not successful. Current URL: {}, Has success message: {}, Has error message: {}", 
                page.url(), isSuccessMessageVisible(), isErrorMessageVisible());
        if (isErrorMessageVisible()) {
            log.warn("Error message: {}", getErrorMessage());
        }
        
        return false;
    }
    
    /**
     * Get the full HTML source of the current page for debugging
     */
    public String getPageSource() {
        return page.content();
    }
    
    /**
     * Get validation error messages
     */
    public String getValidationErrors() {
        if (hasValidationErrors()) {
            return validationErrors.allTextContents().toString();
        }
        return "";
    }
    
    /**
     * Check if customer exists in list by customer name or number
     */
    public boolean isCustomerInList(String searchTerm) {
        // Use the specific tbody ID to avoid ambiguity
        boolean foundByText = page.locator("#search-results tr").filter(new Locator.FilterOptions().setHasText(searchTerm)).count() > 0;
        return foundByText;
    }
    
    /**
     * Get customer number by display name (for migrated test data)
     */
    public String getCustomerNumberByName(String customerName) {
        // Known mappings from migration data
        switch (customerName) {
            case "Ahmad Suharto":
                return "C1000001";
            case "Siti Nurhaliza": 
                return "C1000002";
            case "PT. Teknologi Maju":
                return "C1000003";
            default:
                // Try to find dynamically by searching for the name and extracting customer number
                Locator row = page.locator("#search-results tr").filter(new Locator.FilterOptions().setHasText(customerName));
                if (row.count() > 0) {
                    return row.first().locator("td").first().textContent().trim();
                }
                throw new IllegalArgumentException("Customer not found with name: " + customerName);
        }
    }
    
    /**
     * Get customer details from view page
     */
    public String getCustomerDetailsText() {
        // Use the main content container from the customer view template
        return page.locator(".container .bg-white").textContent();
    }
    
    /**
     * Check if on customer view page
     */
    public boolean isOnViewPage() {
        return page.url().contains("/customer/view/") || 
               page.url().contains("/customer/personal-view/") ||
               page.url().contains("/customer/corporate-view/");
    }
    
    /**
     * Check if on customer list page
     */
    public boolean isOnListPage() {
        return page.url().contains("/customer/list");
    }
    
    /**
     * Click edit button on view page
     */
    public CustomerManagementPage clickEditButton() {
        page.locator("a:has-text('Edit')").click();
        log.debug("Edit button clicked");
        return this;
    }
    
    /**
     * Update customer name in edit form
     */
    public CustomerManagementPage updateCustomerName(String newName) {
        customerNameInput.clear();
        customerNameInput.fill(newName);
        log.debug("Updated customer name to: {}", newName);
        return this;
    }
    
    /**
     * Update email in edit form
     */
    public CustomerManagementPage updateEmail(String newEmail) {
        emailInput.clear();
        emailInput.fill(newEmail);
        log.debug("Updated email to: {}", newEmail);
        return this;
    }
    
    /**
     * Update phone number in edit form
     */
    public CustomerManagementPage updatePhoneNumber(String newPhone) {
        phoneNumberInput.clear();
        phoneNumberInput.fill(newPhone);
        log.debug("Updated phone to: {}", newPhone);
        return this;
    }
    
    /**
     * Click back button
     */
    public CustomerManagementPage clickBackButton() {
        backButton.click();
        log.debug("Back button clicked");
        return this;
    }
}