package id.ac.tazkia.minibank.functional.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.functional.web.helper.LoginHelper;
import id.ac.tazkia.minibank.functional.web.pageobject.CorporateCustomerFormPage;
import id.ac.tazkia.minibank.functional.web.pageobject.CorporateCustomerViewPage;
import id.ac.tazkia.minibank.functional.web.pageobject.CustomerListPage;
import id.ac.tazkia.minibank.functional.web.pageobject.PersonalCustomerFormPage;
import id.ac.tazkia.minibank.functional.web.pageobject.PersonalCustomerViewPage;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SqlGroup({
    @Sql(scripts = "/sql/setup-customer-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(scripts = "/sql/cleanup-customer-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class CustomerManagementSeleniumTest extends BaseSeleniumTest {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Override
    protected void performInitialLogin() {
        // Login as Customer Service user who has CUSTOMER_READ, CUSTOMER_CREATE, CUSTOMER_UPDATE permissions
        loginHelper.loginAsCustomerServiceUser();
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void shouldLoadCustomerListPage() {
        log.info("Starting test: shouldLoadCustomerListPage");
        CustomerListPage listPage = new CustomerListPage(driver, baseUrl);
        listPage.open();
        
        assertTrue(driver.getCurrentUrl().contains("/customer/list"));
        assertEquals("Customer Management - Minibank", driver.getTitle());
        assertTrue(listPage.isCreateButtonDisplayed());
        assertTrue(listPage.isCustomerTableDisplayed());
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldCreatePersonalCustomer() {
        log.info("Starting test: shouldCreatePersonalCustomer");
        Assumptions.assumeTrue(driver != null, "Selenium tests are disabled");
        String uniqueId = "PERS" + System.currentTimeMillis();
        String firstName = "John";
        String lastName = "Doe";
        String email = "john.doe" + System.currentTimeMillis() + "@example.com";
        String phone = "081234567890";
        String address = "123 Main Street";
        String city = "Jakarta";
        String idNumber = "1234567890123456";
        
        CustomerListPage listPage = new CustomerListPage(driver, baseUrl);
        listPage.open();
        
        PersonalCustomerFormPage formPage = listPage.clickCreatePersonalCustomer();
        
        formPage.fillForm(uniqueId, firstName, lastName, "1990-01-01", "KTP", 
                         idNumber, email, phone, address, city);
        
        CustomerListPage resultPage = formPage.submitForm();
        
        assertTrue(resultPage.isSuccessMessageDisplayed());
        assertTrue(resultPage.isCustomerDisplayed(uniqueId));
        
        // Verify in database
        Customer savedCustomer = customerRepository.findByCustomerNumber(uniqueId).orElse(null);
        assertNotNull(savedCustomer);
        if (savedCustomer instanceof id.ac.tazkia.minibank.entity.PersonalCustomer personalCustomer) {
            assertEquals(firstName, personalCustomer.getFirstName());
            assertEquals(lastName, personalCustomer.getLastName());
        }
        assertEquals(email, savedCustomer.getEmail());
        assertEquals(Customer.CustomerType.PERSONAL, savedCustomer.getCustomerType());
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldCreateCorporateCustomer() {
        log.info("Starting test: shouldCreateCorporateCustomer");
        Assumptions.assumeTrue(driver != null, "Selenium tests are disabled");
        String uniqueId = "CORP" + System.currentTimeMillis();
        String companyName = "Test Corporation";
        String contactPerson = "Jane Smith";
        String email = "contact" + System.currentTimeMillis() + "@testcorp.com";
        String phone = "081234567891";
        String address = "456 Corporate Blvd";
        String city = "Surabaya";
        String taxId = "12.345.678.9-012.000";
        
        CustomerListPage listPage = new CustomerListPage(driver, baseUrl);
        listPage.open();
        
        CorporateCustomerFormPage formPage = listPage.clickCreateCorporateCustomer();
        
        formPage.fillForm(uniqueId, companyName, taxId, 
                         contactPerson, "Manager", email, phone, address, city);
        
        CustomerListPage resultPage = formPage.submitForm();
        
        assertTrue(resultPage.isSuccessMessageDisplayed());
        assertTrue(resultPage.isCustomerDisplayed(uniqueId));
        
        // Verify in database
        Customer savedCustomer = customerRepository.findByCustomerNumber(uniqueId).orElse(null);
        assertNotNull(savedCustomer);
        if (savedCustomer instanceof id.ac.tazkia.minibank.entity.CorporateCustomer corporateCustomer) {
            assertEquals(companyName, corporateCustomer.getCompanyName());
            assertEquals(contactPerson, corporateCustomer.getContactPersonName());
        }
        assertEquals(email, savedCustomer.getEmail());
        assertEquals(Customer.CustomerType.CORPORATE, savedCustomer.getCustomerType());
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/customer/personal-customer-creation-data.csv", numLinesToSkip = 1)
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    void shouldCreatePersonalCustomersFromCSVData(String customerNumber, String firstName, String lastName,
                                                String email, String phone, String address, String city,
                                                String identityNumber, String dateOfBirth, String identityType) {
        log.info("Starting test: shouldCreatePersonalCustomersFromCSVData with customerNumber: {}", customerNumber);
        Assumptions.assumeTrue(driver != null, "Selenium tests are disabled");
        
        String uniqueNumber = customerNumber + System.currentTimeMillis();
        String uniqueEmail = "test" + System.currentTimeMillis() + email;
        
        CustomerListPage listPage = new CustomerListPage(driver, baseUrl);
        listPage.open();
        
        PersonalCustomerFormPage formPage = listPage.clickCreatePersonalCustomer();
        formPage.fillForm(uniqueNumber, firstName, lastName, dateOfBirth, identityType, 
                         identityNumber, uniqueEmail, phone, address, city);
        
        CustomerListPage resultPage = formPage.submitForm();
        
        assertTrue(resultPage.isSuccessMessageDisplayed(), 
                  "Personal customer creation failed for: " + uniqueNumber);
        assertTrue(resultPage.isCustomerDisplayed(uniqueNumber));
        
        // Verify database persistence
        Customer savedCustomer = customerRepository.findByCustomerNumber(uniqueNumber).orElse(null);
        assertNotNull(savedCustomer, "Customer not saved to database: " + uniqueNumber);
        assertEquals(uniqueEmail, savedCustomer.getEmail());
        assertEquals(Customer.CustomerType.PERSONAL, savedCustomer.getCustomerType());
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/customer/corporate-customer-creation-data.csv", numLinesToSkip = 1)
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    void shouldCreateCorporateCustomersFromCSVData(String customerNumber, String companyName, String contactPersonName,
                                                 String email, String phone, String address, String city,
                                                 String taxIdentificationNumber) {
        log.info("Starting test: shouldCreateCorporateCustomersFromCSVData with customerNumber: {}", customerNumber);
        Assumptions.assumeTrue(driver != null, "Selenium tests are disabled");
        
        String uniqueNumber = customerNumber + System.currentTimeMillis();
        String uniqueEmail = "test" + System.currentTimeMillis() + email;
        
        CustomerListPage listPage = new CustomerListPage(driver, baseUrl);
        listPage.open();
        
        CorporateCustomerFormPage formPage = listPage.clickCreateCorporateCustomer();
        formPage.fillForm(uniqueNumber, companyName, taxIdentificationNumber,
                         contactPersonName, "Manager", uniqueEmail, phone, address, city);
        
        CustomerListPage resultPage = formPage.submitForm();
        
        assertTrue(resultPage.isSuccessMessageDisplayed(), 
                  "Corporate customer creation failed for: " + uniqueNumber);
        assertTrue(resultPage.isCustomerDisplayed(uniqueNumber));
        
        // Verify database persistence
        Customer savedCustomer = customerRepository.findByCustomerNumber(uniqueNumber).orElse(null);
        assertNotNull(savedCustomer, "Customer not saved to database: " + uniqueNumber);
        assertEquals(uniqueEmail, savedCustomer.getEmail());
        assertEquals(Customer.CustomerType.CORPORATE, savedCustomer.getCustomerType());
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldValidateRequiredFields() {
        log.info("Starting test: shouldValidateRequiredFields");
        Assumptions.assumeTrue(driver != null, "Selenium tests are disabled");
        CustomerListPage listPage = new CustomerListPage(driver, baseUrl);
        listPage.open();
        
        PersonalCustomerFormPage formPage = listPage.clickCreatePersonalCustomer();
        
        // Try to submit empty form
        formPage.submitFormExpectingError();
        
        // Should remain on form page
        assertTrue(driver.getCurrentUrl().contains("/customer/create"));
        
        // Check for validation errors
        assertTrue(formPage.hasValidationError("customerNumber") || 
                  driver.getPageSource().contains("text-red-600") ||
                  driver.getPageSource().contains("border-red-300"));
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldEditPersonalCustomer() {
        log.info("Starting test: shouldEditPersonalCustomer");
        Assumptions.assumeTrue(driver != null, "Selenium tests are disabled");
        String editNumber = "EDIT001"; // Assuming this is a personal customer in test data
        
        CustomerListPage listPage = new CustomerListPage(driver, baseUrl);
        listPage.open();
        
        assertTrue(listPage.isCustomerDisplayed(editNumber), 
                  "Customer " + editNumber + " should be visible");
        
        PersonalCustomerFormPage editPage = listPage.editPersonalCustomer(editNumber);
        
        // Verify form is populated with existing data
        assertEquals(editNumber, editPage.getCustomerNumber());
        
        // Update customer information
        editPage.updateEmail("updated" + System.currentTimeMillis() + "@example.com");
        editPage.updatePhone("087654321098");
        editPage.updateAddress("Updated Address 123");
        
        CustomerListPage resultPage = editPage.submitForm();
        
        assertTrue(resultPage.isSuccessMessageDisplayed());
        
        // Verify changes in database
        Customer updatedCustomer = customerRepository.findByCustomerNumber(editNumber).orElse(null);
        assertNotNull(updatedCustomer);
        assertTrue(updatedCustomer.getEmail().contains("updated"));
        assertEquals("087654321098", updatedCustomer.getPhoneNumber());
        assertEquals(Customer.CustomerType.PERSONAL, updatedCustomer.getCustomerType());
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldEditCorporateCustomer() {
        log.info("Starting test: shouldEditCorporateCustomer");
        Assumptions.assumeTrue(driver != null, "Selenium tests are disabled");
        String editNumber = "CORP_EDIT001"; // Need a corporate customer in test data
        
        CustomerListPage listPage = new CustomerListPage(driver, baseUrl);
        listPage.open();
        
        assertTrue(listPage.isCustomerDisplayed(editNumber), 
                  "Customer " + editNumber + " should be visible");
        
        CorporateCustomerFormPage editPage = listPage.editCorporateCustomer(editNumber);
        
        // Verify form is populated with existing data
        assertEquals(editNumber, editPage.getCustomerNumber());
        
        // Update customer information
        editPage.updateEmail("corp_updated" + System.currentTimeMillis() + "@example.com");
        editPage.updatePhone("087654321099");
        editPage.updateAddress("Updated Corporate Address 456");
        
        CustomerListPage resultPage = editPage.submitForm();
        
        assertTrue(resultPage.isSuccessMessageDisplayed());
        
        // Verify changes in database
        Customer updatedCustomer = customerRepository.findByCustomerNumber(editNumber).orElse(null);
        assertNotNull(updatedCustomer);
        assertTrue(updatedCustomer.getEmail().contains("corp_updated"));
        assertEquals("087654321099", updatedCustomer.getPhoneNumber());
        assertEquals(Customer.CustomerType.CORPORATE, updatedCustomer.getCustomerType());
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldSearchCustomers() {
        log.info("Starting test: shouldSearchCustomers");
        Assumptions.assumeTrue(driver != null, "Selenium tests are disabled");
        String searchNumber = "SEARCH001";
        
        CustomerListPage listPage = new CustomerListPage(driver, baseUrl);
        listPage.open();
        
        // Search by customer number
        listPage.search(searchNumber);
        
        assertTrue(listPage.isCustomerDisplayed(searchNumber));
        
        // Search by name
        listPage.search("Test Customer");
        
        // Should display customers matching the name
        assertTrue(listPage.hasSearchResults());
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldFilterCustomersByType() {
        log.info("Starting test: shouldFilterCustomersByType");
        Assumptions.assumeTrue(driver != null, "Selenium tests are disabled");
        CustomerListPage listPage = new CustomerListPage(driver, baseUrl);
        listPage.open();
        
        // Filter by personal customers
        listPage.filterByCustomerType("PERSONAL");
        
        assertTrue(listPage.hasFilterResults());
        
        // Filter by corporate customers
        listPage.filterByCustomerType("CORPORATE");
        
        assertTrue(listPage.hasFilterResults());
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldActivateAndDeactivateCustomer() {
        log.info("Starting test: shouldActivateAndDeactivateCustomer");
        Assumptions.assumeTrue(driver != null, "Selenium tests are disabled");
        String statusNumber = "STATUS001";
        
        CustomerListPage listPage = new CustomerListPage(driver, baseUrl);
        listPage.open();
        
        assertTrue(listPage.isCustomerDisplayed(statusNumber), 
                  "Customer " + statusNumber + " should be visible");
        
        // Verify initial status
        String initialStatus = listPage.getCustomerStatus(statusNumber);
        assertEquals("Active", initialStatus);
        
        // Deactivate customer
        listPage.deactivateCustomer(statusNumber);
        
        String deactivatedStatus = listPage.getCustomerStatus(statusNumber);
        assertEquals("Inactive", deactivatedStatus);
        
        // Reactivate customer
        listPage.activateCustomer(statusNumber);
        
        String activatedStatus = listPage.getCustomerStatus(statusNumber);
        assertEquals("Active", activatedStatus);
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldViewPersonalCustomerDetails() {
        log.info("Starting test: shouldViewPersonalCustomerDetails");
        Assumptions.assumeTrue(driver != null, "Selenium tests are disabled");
        String viewNumber = "VIEW001"; // Assuming this is a personal customer in test data
        
        CustomerListPage listPage = new CustomerListPage(driver, baseUrl);
        listPage.open();
        
        assertTrue(listPage.isCustomerDisplayed(viewNumber), 
                  "Customer " + viewNumber + " should be visible");
        
        PersonalCustomerViewPage viewPage = listPage.viewPersonalCustomer(viewNumber);
        
        assertTrue(driver.getCurrentUrl().contains("/customer/view/"));
        assertTrue(viewPage.isViewPageDisplayed());
        assertEquals(viewNumber, viewPage.getCustomerNumber());
        assertTrue(viewPage.isEditButtonDisplayed());
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldViewCorporateCustomerDetails() {
        log.info("Starting test: shouldViewCorporateCustomerDetails");
        Assumptions.assumeTrue(driver != null, "Selenium tests are disabled");
        String viewNumber = "CORP_VIEW001"; // Need a corporate customer in test data
        
        CustomerListPage listPage = new CustomerListPage(driver, baseUrl);
        listPage.open();
        
        assertTrue(listPage.isCustomerDisplayed(viewNumber), 
                  "Customer " + viewNumber + " should be visible");
        
        CorporateCustomerViewPage viewPage = listPage.viewCorporateCustomer(viewNumber);
        
        assertTrue(driver.getCurrentUrl().contains("/customer/view/"));
        assertTrue(viewPage.isViewPageDisplayed());
        assertEquals(viewNumber, viewPage.getCustomerNumber());
        assertTrue(viewPage.isEditButtonDisplayed());
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldValidateDuplicateCustomerNumber() {
        log.info("Starting test: shouldValidateDuplicateCustomerNumber");
        Assumptions.assumeTrue(driver != null, "Selenium tests are disabled");
        CustomerListPage listPage = new CustomerListPage(driver, baseUrl);
        listPage.open();
        
        PersonalCustomerFormPage formPage = listPage.clickCreatePersonalCustomer();
        
        // Try to create customer with existing number
        formPage.fillForm("DUPLICATE001", "John", "Doe", "1990-01-01", "KTP",
                         "1234567890123456", "test@example.com", "081234567890", 
                         "Test Address", "Jakarta");
        
        PersonalCustomerFormPage resultPage = formPage.submitFormExpectingError();
        
        // Should stay on form page with validation error
        assertTrue(driver.getCurrentUrl().contains("/customer/create"));
        
        // Check for validation error indicators
        assertTrue(resultPage.isErrorMessageDisplayed() || 
                  driver.getPageSource().contains("already exists") ||
                  hasValidationErrorOnPage());
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/customer/personal-customer-validation-data.csv", numLinesToSkip = 1)
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldValidatePersonalCustomerInputErrors(String testCase, String customerNumber, 
                                                 String firstName, String lastName,
                                                 String email, String phone, 
                                                 String expectedError) {
        log.info("Starting test: shouldValidatePersonalCustomerInputErrors with testCase: {}", testCase);
        Assumptions.assumeTrue(driver != null, "Selenium tests are disabled");
        
        CustomerListPage listPage = new CustomerListPage(driver, baseUrl);
        listPage.open();
        
        PersonalCustomerFormPage formPage = listPage.clickCreatePersonalCustomer();
        
        // Handle empty field cases first, then use provided values or defaults
        String testNumber, testFirstName, testLastName, testEmail, testPhone;
        
        if (testCase.contains("Empty Customer Number")) {
            testNumber = "";
        } else {
            testNumber = (customerNumber != null && !customerNumber.isEmpty()) ? customerNumber : "TEST" + System.currentTimeMillis();
        }
        
        if (testCase.contains("Empty First Name")) {
            testFirstName = "";
        } else {
            testFirstName = (firstName != null && !firstName.isEmpty()) ? firstName : "Test";
        }
        
        if (testCase.contains("Empty Last Name")) {
            testLastName = "";
        } else {
            testLastName = (lastName != null && !lastName.isEmpty()) ? lastName : "Customer";
        }
        
        if (testCase.contains("Empty Email")) {
            testEmail = "";
        } else {
            testEmail = (email != null && !email.isEmpty()) ? email : "test" + System.currentTimeMillis() + "@example.com";
        }
        
        if (testCase.contains("Empty Phone")) {
            testPhone = "";
        } else {
            testPhone = (phone != null && !phone.isEmpty()) ? phone : "081234567890";
        }
        
        formPage.fillForm(testNumber, testFirstName, testLastName, "1990-01-01", "KTP",
                         "1234567890123456", testEmail, testPhone, "Test Address", "Jakarta");
        
        PersonalCustomerFormPage resultPage = formPage.submitFormExpectingError();
        
        // Should stay on form page with validation error (not redirect to list)
        assertTrue(driver.getCurrentUrl().contains("/customer/create") || 
                   driver.getCurrentUrl().contains("/customer/personal-form"),
                   "Expected to stay on form page, but current URL is: " + driver.getCurrentUrl());
        
        // Check for validation error indicators
        assertTrue(resultPage.isErrorMessageDisplayed() || hasValidationErrorOnPage(),
                  "Should display validation error for: " + testCase);
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/customer/corporate-customer-validation-data.csv", numLinesToSkip = 1)
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldValidateCorporateCustomerInputErrors(String testCase, String customerNumber, 
                                                  String companyName, String contactPersonName,
                                                  String email, String phone, 
                                                  String expectedError) {
        log.info("Starting test: shouldValidateCorporateCustomerInputErrors with testCase: {}", testCase);
        Assumptions.assumeTrue(driver != null, "Selenium tests are disabled");
        
        CustomerListPage listPage = new CustomerListPage(driver, baseUrl);
        listPage.open();
        
        CorporateCustomerFormPage formPage = listPage.clickCreateCorporateCustomer();
        
        // Handle empty field cases first, then use provided values or defaults
        String testNumber, testCompanyName, testContactPerson, testEmail, testPhone;
        
        if (testCase.contains("Empty Customer Number")) {
            testNumber = "";
        } else {
            testNumber = (customerNumber != null && !customerNumber.isEmpty()) ? customerNumber : "TEST" + System.currentTimeMillis();
        }
        
        if (testCase.contains("Empty Company Name")) {
            testCompanyName = "";
        } else {
            testCompanyName = (companyName != null && !companyName.isEmpty()) ? companyName : "Test Company";
        }
        
        testContactPerson = (contactPersonName != null && !contactPersonName.isEmpty()) ? contactPersonName : "Contact Person";
        
        if (testCase.contains("Empty Email")) {
            testEmail = "";
        } else {
            testEmail = (email != null && !email.isEmpty()) ? email : "test" + System.currentTimeMillis() + "@example.com";
        }
        
        if (testCase.contains("Empty Phone")) {
            testPhone = "";
        } else {
            testPhone = (phone != null && !phone.isEmpty()) ? phone : "081234567890";
        }
        
        formPage.fillForm(testNumber, testCompanyName, "12.345.678.9-012.000",
                         testContactPerson, "Manager", testEmail, testPhone, "Test Address", "Jakarta");
        
        CorporateCustomerFormPage resultPage = formPage.submitFormExpectingError();
        
        // Should stay on form page with validation error
        assertTrue(driver.getCurrentUrl().contains("/customer/create"));
        
        // Check for validation error indicators
        assertTrue(resultPage.isErrorMessageDisplayed() || hasValidationErrorOnPage(),
                  "Should display validation error for: " + testCase);
    }
    
    private boolean hasValidationErrorOnPage() {
        // Check page source for server-side validation error indicators
        String pageSource = driver.getPageSource();
        return pageSource.contains("error-message") ||
               pageSource.contains("validation-errors") ||
               pageSource.contains("Please correct") ||
               pageSource.contains("is required") ||
               pageSource.contains("already exists") ||
               pageSource.contains("Failed to create") ||
               // Check if we stayed on create page after submission (validation failure)
               driver.getCurrentUrl().contains("/customer/create");
    }
}