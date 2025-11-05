package id.ac.tazkia.minibank.functional.success;

import id.ac.tazkia.minibank.functional.config.BasePlaywrightTest;
import id.ac.tazkia.minibank.functional.pages.ApprovalQueuePage;
import id.ac.tazkia.minibank.functional.pages.CustomerManagementPage;
import id.ac.tazkia.minibank.functional.pages.LoginPage;
import id.ac.tazkia.minibank.functional.pages.DashboardPage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional tests for Approval Workflow success scenarios.
 * Tests the complete approval flow: CS creates entity -> Branch Manager approves -> Entity becomes active.
 */
@Slf4j
@Tag("playwright-success")
@DisplayName("Approval Workflow Success Scenario Tests")
class ApprovalWorkflowSuccessTest extends BasePlaywrightTest {

    @Test
    @DisplayName("[AW-S-001] Should successfully create and approve personal customer")
    void shouldCreateAndApprovePersonalCustomer() {
        log.info("Success Test: Complete customer approval workflow");

        // STEP 1: Login as Customer Service (CS) and create customer
        log.info("STEP 1: CS creates customer");
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("cs1", "minibank123");

        assertTrue(dashboardPage.isDashboardLoaded(), "CS should be logged in successfully");

        // Navigate to create customer
        CustomerManagementPage customerPage = new CustomerManagementPage(page);
        customerPage.navigateToAddCustomer(baseUrl);
        customerPage.selectCustomerType("PERSONAL");
        page.waitForLoadState();

        // Create unique customer data (FR.002 compliant)
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueSuffix = timestamp.substring(timestamp.length() - 6);
        String uniqueName = "Approval Testuser"; // No numbers in name (violates pattern)
        String uniqueIdentityNumber = "3201010101" + uniqueSuffix; // 16 digits
        String uniqueEmail = "approval" + uniqueSuffix + "@test.com";
        String uniquePhone = "0812345" + uniqueSuffix; // 13 digits

        // Use FR.002 compliant extended form method
        customerPage.fillPersonalCustomerFormExtended(
            // Basic Personal Information
            uniqueName, "Approval", uniqueIdentityNumber, "KTP",
            "1990-01-01", "Jakarta", "MALE", "Ibu Approval",
            // Personal Data (FR.002)
            "S1", "ISLAM", "KAWIN", "2",
            // Identity Information (FR.002)
            "WNI", "Domiciled", "2030-01-01",
            // Contact Information
            uniqueEmail, uniquePhone, "Jl. Approval Test No. 1", "Jakarta", "DKI Jakarta", "12345",
            // Employment Data (FR.002)
            "Test Engineer", "PT Test", "Jl. Test No. 1", "Technology",
            "10000000", "Salary", "Daily transactions", "20", "15000000"
        );

        customerPage.clickSave();
        page.waitForLoadState();

        // Debug: Check for validation errors if operation failed
        if (!customerPage.isOperationSuccessful()) {
            log.error("Form submission failed");
            log.error("Current URL: {}", page.url());

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

        // Verify success message indicates pending approval
        assertTrue(customerPage.isOperationSuccessful(), "Should show success message or redirect after creating customer");

        // If success message is visible, verify it mentions approval
        if (customerPage.isSuccessMessageVisible()) {
            String successMsg = customerPage.getSuccessMessage();
            assertTrue(successMsg.contains("submitted for approval") || successMsg.contains("approval"),
                "Success message should indicate approval workflow: " + successMsg);
        }

        log.info("✓ Customer created and submitted for approval");

        // STEP 2: Logout CS and login as Branch Manager
        log.info("STEP 2: Switch to Branch Manager");
        dashboardPage.logout();
        loginPage.navigateTo(baseUrl);
        dashboardPage = loginPage.loginWith("manager1", "minibank123");

        assertTrue(dashboardPage.isDashboardLoaded(), "Branch Manager should be logged in successfully");
        log.info("✓ Branch Manager logged in");

        // STEP 3: Navigate to approval queue
        log.info("STEP 3: View approval queue");
        ApprovalQueuePage approvalPage = new ApprovalQueuePage(page);
        approvalPage.navigateToQueue();

        // Verify there are pending approvals
        int pendingCount = approvalPage.getPendingCount();
        assertTrue(pendingCount > 0, "Should have at least one pending approval");
        log.info("✓ Found {} pending approval(s)", pendingCount);

        // STEP 4: View first approval detail (should be our customer)
        log.info("STEP 4: View approval detail");
        approvalPage.viewFirstApprovalDetail();

        // Verify it's a customer creation request
        assertEquals("CUSTOMER_CREATION", approvalPage.getRequestType(),
            "Should be customer creation request");
        assertEquals("CUSTOMER", approvalPage.getEntityType(),
            "Should be customer entity");
        assertEquals("customer-service", approvalPage.getRequestedBy(),
            "Should be requested by customer-service");

        assertTrue(approvalPage.isCustomerDetailsVisible(),
            "Customer details should be visible");
        assertTrue(approvalPage.isApprovalActionsVisible(),
            "Approval actions should be available");

        log.info("✓ Approval request details loaded");

        // STEP 5: Approve the request
        log.info("STEP 5: Approve customer");
        approvalPage.approveRequest("Approved for testing purposes");

        // Verify success message
        assertTrue(approvalPage.isSuccessMessageVisible(), "Should show approval success message");
        String approvalMsg = approvalPage.getSuccessMessage();
        assertTrue(approvalMsg.contains("approved") || approvalMsg.contains("successfully"),
            "Should show approval confirmation: " + approvalMsg);

        log.info("✓ Customer approved successfully");

        // STEP 6: Verify approval queue is updated
        log.info("STEP 6: Verify queue updated");
        int newPendingCount = approvalPage.getPendingCount();
        assertTrue(newPendingCount < pendingCount || newPendingCount == 0,
            "Pending count should decrease after approval");

        log.info("✓ Approval workflow completed successfully");
        log.info("Test Summary: Customer created by CS, approved by Branch Manager");
    }

    @Test
    @DisplayName("[AW-S-002] Should successfully reject customer creation request")
    void shouldRejectCustomerCreationRequest() {
        log.info("Success Test: Customer rejection workflow");

        // STEP 1: Login as CS and create customer
        log.info("STEP 1: CS creates customer");
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("cs1", "minibank123");

        assertTrue(dashboardPage.isDashboardLoaded(), "CS should be logged in");

        CustomerManagementPage customerPage = new CustomerManagementPage(page);
        customerPage.navigateToAddCustomer(baseUrl);
        customerPage.selectCustomerType("PERSONAL");
        page.waitForLoadState();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueSuffix = timestamp.substring(timestamp.length() - 6);
        String uniqueIdentityNumber = "3201010102" + uniqueSuffix; // 16 digits
        String uniqueName = "Reject Testuser"; // No numbers in name (violates pattern)
        String uniqueEmail = "reject" + uniqueSuffix + "@test.com";
        String uniquePhone = "0812346" + uniqueSuffix; // 13 digits

        // Use FR.002 compliant extended form method
        customerPage.fillPersonalCustomerFormExtended(
            // Basic Personal Information
            uniqueName, "Reject", uniqueIdentityNumber, "KTP",
            "1990-01-01", "Jakarta", "FEMALE", "Ibu Reject",
            // Personal Data (FR.002)
            "S1", "ISLAM", "BELUM_KAWIN", "0",
            // Identity Information (FR.002)
            "WNI", "Domiciled", "2030-01-01",
            // Contact Information
            uniqueEmail, uniquePhone, "Jl. Reject Test No. 1", "Jakarta", "DKI Jakarta", "12345",
            // Employment Data (FR.002)
            "Test Engineer", "PT Reject Test", "Jl. Test No. 1", "Technology",
            "10000000", "Salary", "Daily transactions", "20", "15000000"
        );

        customerPage.clickSave();
        page.waitForLoadState();

        // Debug: Check for validation errors if operation failed
        if (!customerPage.isOperationSuccessful()) {
            log.error("Form submission failed");
            log.error("Current URL: {}", page.url());

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

        assertTrue(customerPage.isOperationSuccessful(), "Should create customer for approval");

        // STEP 2: Switch to Branch Manager
        log.info("STEP 2: Switch to Branch Manager");
        dashboardPage.logout();
        loginPage.navigateTo(baseUrl);
        dashboardPage = loginPage.loginWith("manager1", "minibank123");
        assertTrue(dashboardPage.isDashboardLoaded(), "Manager should be logged in");

        // STEP 3: Navigate to approval queue and view first request
        log.info("STEP 3: Navigate to approval request");
        ApprovalQueuePage approvalPage = new ApprovalQueuePage(page);
        approvalPage.navigateToQueue();
        approvalPage.viewFirstApprovalDetail();

        assertTrue(approvalPage.isApprovalActionsVisible(), "Should see approval actions");

        // STEP 4: Reject the request
        log.info("STEP 4: Reject customer");
        approvalPage.rejectRequest("Incomplete documentation", "Missing required documents");

        // Verify rejection success
        assertTrue(approvalPage.isSuccessMessageVisible(), "Should show rejection success");
        String rejectionMsg = approvalPage.getSuccessMessage();
        assertTrue(rejectionMsg.contains("reject") || rejectionMsg.contains("successfully"),
            "Should confirm rejection: " + rejectionMsg);

        log.info("✓ Customer rejection workflow completed successfully");
    }

    @Test
    @DisplayName("[AW-S-003] Should filter approval queue by request type")
    void shouldFilterApprovalQueueByRequestType() {
        log.info("Success Test: Filter approval queue");

        // Login as Branch Manager
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("manager1", "minibank123");

        assertTrue(dashboardPage.isDashboardLoaded(), "Manager should be logged in");

        // Navigate to approval queue
        ApprovalQueuePage approvalPage = new ApprovalQueuePage(page);
        approvalPage.navigateToQueue();

        // Get initial count
        int totalPending = approvalPage.getPendingCount();
        log.info("Total pending approvals: {}", totalPending);

        // Filter by customer creation
        approvalPage.filterByRequestType("CUSTOMER_CREATION");

        // Verify filtering works (count might be same or less)
        int filteredCount = approvalPage.getPendingCount();
        assertTrue(filteredCount <= totalPending, "Filtered count should not exceed total");
        log.info("Filtered count (CUSTOMER_CREATION): {}", filteredCount);

        // Clear filter
        approvalPage.clearFilters();

        // Verify we're back to showing all
        int clearedCount = approvalPage.getPendingCount();
        assertEquals(totalPending, clearedCount, "Should show all pending after clearing filter");

        log.info("✓ Filter functionality verified");
    }
}
