package id.ac.tazkia.minibank.functional.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;

/**
 * Page Object Model for Approval Queue and Approval Detail pages.
 * Provides methods to interact with approval workflow UI elements.
 */
@Slf4j
public class ApprovalQueuePage {

    private final Page page;

    // Queue page elements
    private final Locator queueTable;
    private final Locator filterTypeSelect;
    private final Locator filterButton;
    private final Locator clearFilterButton;
    private final Locator pendingCountBadge;
    private final Locator approvalRows;
    private final Locator noPendingMessage;

    // Detail page elements
    private final Locator requestType;
    private final Locator entityType;
    private final Locator requestedBy;
    private final Locator requestedDate;
    private final Locator requestNotes;
    private final Locator backToQueueButton;

    // Customer detail elements
    private final Locator customerNumber;
    private final Locator customerName;
    private final Locator customerEmail;
    private final Locator customerPhone;
    private final Locator customerAddress;

    // Account detail elements
    private final Locator accountNumber;
    private final Locator accountName;
    private final Locator productName;
    private final Locator accountBalance;
    private final Locator accountCustomerName;

    // Approval action elements
    private final Locator approveForm;
    private final Locator approveReviewNotes;
    private final Locator approveButton;
    private final Locator rejectForm;
    private final Locator rejectionReason;
    private final Locator rejectReviewNotes;
    private final Locator rejectButton;

    // Messages
    private final Locator successMessage;
    private final Locator errorMessage;
    private final Locator processedMessage;

    // Navigation
    private final Locator approvalQueueLink;

    public ApprovalQueuePage(Page page) {
        this.page = page;

        // Initialize queue page locators
        this.queueTable = page.locator("#approval-queue-table");
        this.filterTypeSelect = page.locator("#filterTypeSelect");
        this.filterButton = page.locator("#filter-btn");
        this.clearFilterButton = page.locator("#clear-filter-btn");
        this.pendingCountBadge = page.locator("#pending-count");
        this.approvalRows = page.locator("#approval-queue-results tr");
        this.noPendingMessage = page.locator("#no-pending-message");

        // Initialize detail page locators
        this.requestType = page.locator("#request-type");
        this.entityType = page.locator("#entity-type");
        this.requestedBy = page.locator("#requested-by");
        this.requestedDate = page.locator("#requested-date");
        this.requestNotes = page.locator("#request-notes");
        this.backToQueueButton = page.locator("#back-to-queue-btn");

        // Customer detail locators
        this.customerNumber = page.locator("#customer-number");
        this.customerName = page.locator("#customer-name");
        this.customerEmail = page.locator("#customer-email");
        this.customerPhone = page.locator("#customer-phone");
        this.customerAddress = page.locator("#customer-address");

        // Account detail locators
        this.accountNumber = page.locator("#account-number");
        this.accountName = page.locator("#account-name");
        this.productName = page.locator("#product-name");
        this.accountBalance = page.locator("#account-balance");
        this.accountCustomerName = page.locator("#account-customer-name");

        // Approval action locators
        this.approveForm = page.locator("#approve-form");
        this.approveReviewNotes = page.locator("#approve-review-notes");
        this.approveButton = page.locator("#approve-btn");
        this.rejectForm = page.locator("#reject-form");
        this.rejectionReason = page.locator("#rejection-reason");
        this.rejectReviewNotes = page.locator("#reject-review-notes");
        this.rejectButton = page.locator("#reject-btn");

        // Messages
        this.successMessage = page.locator("#success-message");
        this.errorMessage = page.locator("#error-message");
        this.processedMessage = page.locator("#processed-message");

        // Navigation
        this.approvalQueueLink = page.locator("#approval-queue-link");
    }

    /**
     * Navigate to approval queue page
     */
    public void navigateToQueue() {
        log.info("Navigating to approval queue");
        approvalQueueLink.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        queueTable.waitFor();
    }

    /**
     * Get count of pending approvals from badge
     */
    public int getPendingCount() {
        String badgeText = pendingCountBadge.textContent();
        // Extract number from "X Pending" text
        String countStr = badgeText.replaceAll("[^0-9]", "");
        return Integer.parseInt(countStr);
    }

    /**
     * Check if there are no pending approvals
     */
    public boolean hasNoPendingApprovals() {
        return noPendingMessage.isVisible();
    }

    /**
     * Filter approvals by request type
     */
    public void filterByRequestType(String requestType) {
        log.info("Filtering by request type: {}", requestType);
        filterTypeSelect.selectOption(requestType);
        filterButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Clear all filters
     */
    public void clearFilters() {
        log.info("Clearing filters");
        clearFilterButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Click view details for first pending approval
     */
    public void viewFirstApprovalDetail() {
        log.info("Viewing first approval detail");
        page.locator("#approval-queue-results tr").first().locator("a").first().click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Click view details for specific approval by ID
     */
    public void viewApprovalDetail(String approvalId) {
        log.info("Viewing approval detail for ID: {}", approvalId);
        page.locator("#view-detail-" + approvalId).click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Get request type from detail page
     */
    public String getRequestType() {
        return requestType.textContent();
    }

    /**
     * Get entity type from detail page
     */
    public String getEntityType() {
        return entityType.textContent();
    }

    /**
     * Get requested by from detail page
     */
    public String getRequestedBy() {
        return requestedBy.textContent();
    }

    /**
     * Check if customer details are visible
     */
    public boolean isCustomerDetailsVisible() {
        return customerNumber.isVisible();
    }

    /**
     * Check if account details are visible
     */
    public boolean isAccountDetailsVisible() {
        return accountNumber.isVisible();
    }

    /**
     * Get customer number from detail page
     */
    public String getCustomerNumber() {
        return customerNumber.textContent();
    }

    /**
     * Get account number from detail page
     */
    public String getAccountNumber() {
        return accountNumber.textContent();
    }

    /**
     * Approve request with optional review notes
     */
    public void approveRequest(String reviewNotes) {
        log.info("Approving request with notes: {}", reviewNotes);
        if (reviewNotes != null && !reviewNotes.isEmpty()) {
            approveReviewNotes.fill(reviewNotes);
        }
        approveButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Approve request without notes
     */
    public void approveRequest() {
        approveRequest(null);
    }

    /**
     * Reject request with rejection reason and optional review notes
     */
    public void rejectRequest(String reason, String reviewNotes) {
        log.info("Rejecting request with reason: {}", reason);
        rejectionReason.fill(reason);
        if (reviewNotes != null && !reviewNotes.isEmpty()) {
            rejectReviewNotes.fill(reviewNotes);
        }
        rejectButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Reject request with only reason
     */
    public void rejectRequest(String reason) {
        rejectRequest(reason, null);
    }

    /**
     * Check if approval actions are visible (pending status)
     */
    public boolean isApprovalActionsVisible() {
        return approveButton.isVisible() && rejectButton.isVisible();
    }

    /**
     * Check if processed message is visible (already approved/rejected)
     */
    public boolean isProcessedMessageVisible() {
        return processedMessage.isVisible();
    }

    /**
     * Back to queue from detail page
     */
    public void backToQueue() {
        log.info("Navigating back to queue");
        backToQueueButton.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Check if success message is visible
     */
    public boolean isSuccessMessageVisible() {
        return successMessage.isVisible();
    }

    /**
     * Check if error message is visible
     */
    public boolean isErrorMessageVisible() {
        return errorMessage.isVisible();
    }

    /**
     * Get success message text
     */
    public String getSuccessMessage() {
        return successMessage.textContent().trim();
    }

    /**
     * Get error message text
     */
    public String getErrorMessage() {
        return errorMessage.textContent().trim();
    }
}
