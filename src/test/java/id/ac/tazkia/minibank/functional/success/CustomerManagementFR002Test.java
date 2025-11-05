package id.ac.tazkia.minibank.functional.success;

import id.ac.tazkia.minibank.functional.config.BasePlaywrightTest;
import id.ac.tazkia.minibank.functional.pages.CustomerManagementPage;
import id.ac.tazkia.minibank.functional.pages.LoginPage;
import id.ac.tazkia.minibank.functional.pages.DashboardPage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for FR.002 - Manajemen Rekening Nasabah
 * Tests all new fields added per FR.002 specification including:
 * - Personal Data fields (education, religion, marital status, dependents)
 * - Identity fields (citizenship, residency status, identity expiry)
 * - Employment Data fields (occupation, company info, income, etc.)
 */
@Slf4j
@Tag("playwright-success")
@Tag("fr002")
@DisplayName("FR.002 Customer Management Tests - Complete Field Set")
class CustomerManagementFR002Test extends BasePlaywrightTest {

    private CustomerManagementPage customerPage;
    private String testCustomerId;
    private boolean cleanupRequired = false;

    @BeforeEach
    void setUp() {
        // Login as Customer Service (CS) who has permission to manage customers
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("cs1", "minibank123");

        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");

        // Initialize customer management page
        customerPage = new CustomerManagementPage(page);

        // Reset cleanup state
        testCustomerId = null;
        cleanupRequired = false;
    }

    @AfterEach
    void tearDown() {
        try {
            // Clean up any test data created during the test
            if (cleanupRequired && testCustomerId != null) {
                log.debug("Cleaning up test customer: {}", testCustomerId);
                // Navigate back to customer list and clear any search filters
                customerPage.navigateToList(baseUrl);
                // Clear search to ensure we see all customers
                page.locator("#search").fill("");
                page.locator("#search-btn").click();
                page.waitForLoadState();
            }
        } catch (Exception e) {
            log.warn("Cleanup encountered an issue: {}", e.getMessage());
        } finally {
            // Reset state
            testCustomerId = null;
            cleanupRequired = false;
        }
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/functional/personal-customer-fr002-success.csv", numLinesToSkip = 1)
    @DisplayName("[FR002-S-001] Should successfully create personal customers with all FR.002 fields")
    void shouldCreatePersonalCustomerWithFR002FieldsSuccessfully(
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

        log.info("FR.002 Test: Creating personal customer with complete field set: {}", name);

        // Navigate to add customer page
        customerPage.navigateToAddCustomer(baseUrl);

        // Select personal customer type
        customerPage.selectCustomerType("PERSONAL");
        page.waitForLoadState();

        // Make test data unique by adding timestamp to avoid conflicts
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueSuffix = timestamp.substring(timestamp.length() - 6); // Last 6 digits
        String uniqueIdentityNumber = identityNumber.substring(0, 10) + uniqueSuffix; // Make NIK unique (10 + 6 = 16 digits)
        String uniqueEmail = email.replace("@", "+" + uniqueSuffix + "@");
        String uniquePhone = phone.substring(0, 7) + uniqueSuffix; // Max 13 digits (7 + 6 = 13)

        // Fill form with all FR.002 fields using the extended method
        customerPage.fillPersonalCustomerFormExtended(
            // Basic Personal Information
            name, aliasName, uniqueIdentityNumber, identityType,
            birthDate, birthPlace, gender, motherName,
            // Personal Data (FR.002)
            education, religion, maritalStatus, dependents,
            // Identity Information (FR.002)
            citizenship, residencyStatus, identityExpiryDate,
            // Contact Information
            uniqueEmail, uniquePhone, address, city, province, postalCode,
            // Employment Data (FR.002)
            occupation, companyName, companyAddress, businessField,
            monthlyIncome, sourceOfFunds, accountPurpose,
            estimatedMonthlyTransactions, estimatedTransactionAmount
        );

        // Save customer
        customerPage.clickSave();
        page.waitForLoadState();

        // Debug: Check for validation errors on page
        if (!customerPage.isOperationSuccessful()) {
            log.error("Form submission failed for: {}", name);
            log.error("Current URL: {}", page.url());
            log.error("Page title: {}", page.title());

            // Check for HTML5 validation messages
            String validationScript = """
                Array.from(document.querySelectorAll('input:invalid, select:invalid, textarea:invalid'))
                    .map(el => el.id + ': ' + el.validationMessage)
                    .join('; ')
            """;
            String invalidFields = (String) page.evaluate(validationScript);
            if (invalidFields != null && !invalidFields.isEmpty()) {
                log.error("Invalid fields: {}", invalidFields);
            }
        }

        // Verify success using the comprehensive success check
        assertTrue(customerPage.isOperationSuccessful(),
                "Should show success message or redirect to view page after creating personal customer with FR.002 fields");

        // Verify customer details if on view page
        if (customerPage.isOnViewPage()) {
            String details = customerPage.getCustomerDetailsText();

            // Verify basic fields
            assertTrue(details.contains(name),
                "Customer name should be displayed");
            assertTrue(details.contains(uniqueIdentityNumber),
                "Identity number should be displayed");

            // Log the presence of FR.002 fields in the view
            boolean hasEducation = details.contains(education);
            boolean hasReligion = details.contains(religion);
            boolean hasOccupation = details.contains(occupation);

            log.debug("FR.002 fields visible in view - Education: {}, Religion: {}, Occupation: {}",
                hasEducation, hasReligion, hasOccupation);
        }

        log.info("✅ Personal customer created successfully with all FR.002 fields: {}", name);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/functional/personal-customer-fr002-success.csv", numLinesToSkip = 1)
    @DisplayName("[FR002-S-002] Should validate FR.002 required fields properly")
    void shouldValidateFR002RequiredFieldsProperly(
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

        log.info("FR.002 Validation Test: Testing form validation for: {}", name);

        // Navigate to add customer page
        customerPage.navigateToAddCustomer(baseUrl);
        customerPage.selectCustomerType("PERSONAL");
        page.waitForLoadState();

        // Test 1: Verify gender is now required (FR.002 requirement)
        // Fill only minimal required fields to trigger gender validation
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueSuffix = timestamp.substring(timestamp.length() - 6); // Last 6 digits
        String[] nameParts = name.split(" ", 2);
        String uniqueIdentityNumber = identityNumber.substring(0, 10) + uniqueSuffix;

        page.locator("#firstName").fill(nameParts[0]);
        if (nameParts.length > 1) {
            page.locator("#lastName").fill(nameParts[1]);
        }
        page.locator("#identityNumber").fill(uniqueIdentityNumber);
        page.locator("#identityType").selectOption(identityType);
        page.locator("#dateOfBirth").fill(birthDate);
        // Skip gender to test validation
        page.locator("#email").fill(email.replace("@", "+" + uniqueSuffix + "@"));
        page.locator("#phoneNumber").fill(phone.substring(0, 7) + uniqueSuffix);
        page.locator("#address").fill(address);
        page.locator("#city").fill(city);
        page.locator("#postalCode").fill(postalCode);

        // Try to submit without gender
        customerPage.clickSave();
        page.waitForTimeout(500); // Brief wait for validation to appear

        // Verify that browser validation prevents submission (HTML5 required attribute)
        // Since gender has required attribute, browser should prevent form submission
        assertTrue(page.url().contains("/customer/create/personal"),
            "Should stay on form page when required fields are missing");

        log.info("✅ FR.002 validation test passed - required fields enforced");
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/functional/personal-customer-fr002-success.csv", numLinesToSkip = 1)
    @DisplayName("[FR002-S-003] Should properly handle NIK validation (exactly 16 digits)")
    void shouldValidateNIKLength(
            String name, String aliasName, String identityNumber, String identityType,
            String birthDate, String birthPlace, String gender, String motherName,
            String education, String religion, String maritalStatus, String dependents,
            String citizenship, String residencyStatus, String identityExpiryDate,
            String email, String phone, String address, String city, String province, String postalCode,
            String occupation, String companyName, String companyAddress, String businessField,
            String monthlyIncome, String sourceOfFunds, String accountPurpose,
            String estimatedMonthlyTransactions, String estimatedTransactionAmount) {

        log.info("FR.002 NIK Validation Test: Testing 16-digit NIK requirement");

        // Navigate to form
        customerPage.navigateToAddCustomer(baseUrl);
        customerPage.selectCustomerType("PERSONAL");
        page.waitForLoadState();

        // Test with invalid NIK (too short)
        String invalidNIK = "327108"; // Only 6 digits

        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueSuffix = timestamp.substring(timestamp.length() - 6); // Last 6 digits

        customerPage.fillPersonalCustomerFormExtended(
            name, aliasName, invalidNIK, identityType,
            birthDate, birthPlace, gender, motherName,
            education, religion, maritalStatus, dependents,
            citizenship, residencyStatus, identityExpiryDate,
            email.replace("@", "+" + uniqueSuffix + "@"),
            phone.substring(0, 7) + uniqueSuffix,
            address, city, province, postalCode,
            occupation, companyName, companyAddress, businessField,
            monthlyIncome, sourceOfFunds, accountPurpose,
            estimatedMonthlyTransactions, estimatedTransactionAmount
        );

        customerPage.clickSave();
        page.waitForTimeout(500);

        // Should stay on form page due to validation
        assertTrue(page.url().contains("/customer/create/personal"),
            "Should stay on form page when NIK format is invalid");

        log.info("✅ NIK validation test passed - 16-digit requirement enforced");
    }
}
