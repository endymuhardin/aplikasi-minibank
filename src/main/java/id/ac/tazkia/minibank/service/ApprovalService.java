package id.ac.tazkia.minibank.service;

import id.ac.tazkia.minibank.entity.*;
import id.ac.tazkia.minibank.entity.ApprovalRequest.ApprovalStatus;
import id.ac.tazkia.minibank.entity.ApprovalRequest.EntityType;
import id.ac.tazkia.minibank.entity.ApprovalRequest.RequestType;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.ApprovalRequestRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing approval workflow business logic
 */
@Slf4j
@Service
@Transactional
public class ApprovalService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    public ApprovalService(ApprovalRequestRepository approvalRequestRepository,
                          CustomerRepository customerRepository,
                          AccountRepository accountRepository) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Create approval request for customer creation
     */
    public ApprovalRequest createCustomerApprovalRequest(Customer customer, String requestedBy, String notes) {
        log.info("Creating approval request for customer: {} by {}", customer.getCustomerNumber(), requestedBy);

        ApprovalRequest request = new ApprovalRequest();
        request.setRequestType(RequestType.CUSTOMER_CREATION);
        request.setEntityType(EntityType.CUSTOMER);
        request.setEntityId(customer.getId());
        request.setApprovalStatus(ApprovalStatus.PENDING);
        request.setRequestedBy(requestedBy);
        request.setRequestNotes(notes);
        request.setBranch(customer.getBranch());

        return approvalRequestRepository.save(request);
    }

    /**
     * Create approval request for account opening
     */
    public ApprovalRequest createAccountApprovalRequest(Account account, String requestedBy, String notes) {
        log.info("Creating approval request for account: {} by {}", account.getAccountNumber(), requestedBy);

        ApprovalRequest request = new ApprovalRequest();
        request.setRequestType(RequestType.ACCOUNT_OPENING);
        request.setEntityType(EntityType.ACCOUNT);
        request.setEntityId(account.getId());
        request.setApprovalStatus(ApprovalStatus.PENDING);
        request.setRequestedBy(requestedBy);
        request.setRequestNotes(notes);
        request.setBranch(account.getBranch());

        return approvalRequestRepository.save(request);
    }

    /**
     * Approve customer creation
     */
    public Customer approveCustomer(UUID approvalRequestId, String reviewedBy, String reviewNotes) {
        log.info("Approving customer with approval request ID: {} by {}", approvalRequestId, reviewedBy);

        ApprovalRequest approvalRequest = getApprovalRequest(approvalRequestId);
        validateEntityType(approvalRequest, EntityType.CUSTOMER);

        // Get customer
        Customer customer = customerRepository.findById(approvalRequest.getEntityId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + approvalRequest.getEntityId()));

        // Approve request
        approvalRequest.approve(reviewedBy, reviewNotes);
        approvalRequestRepository.save(approvalRequest);

        // Update customer status
        customer.setApprovalStatus(Customer.ApprovalStatus.APPROVED);
        customer.setStatus(Customer.CustomerStatus.ACTIVE);

        return customerRepository.save(customer);
    }

    /**
     * Reject customer creation
     */
    public Customer rejectCustomer(UUID approvalRequestId, String reviewedBy,
                                   String rejectionReason, String reviewNotes) {
        log.info("Rejecting customer with approval request ID: {} by {}", approvalRequestId, reviewedBy);

        ApprovalRequest approvalRequest = getApprovalRequest(approvalRequestId);
        validateEntityType(approvalRequest, EntityType.CUSTOMER);

        // Get customer
        Customer customer = customerRepository.findById(approvalRequest.getEntityId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + approvalRequest.getEntityId()));

        // Reject request
        approvalRequest.reject(reviewedBy, rejectionReason, reviewNotes);
        approvalRequestRepository.save(approvalRequest);

        // Update customer status
        customer.setApprovalStatus(Customer.ApprovalStatus.REJECTED);
        customer.setStatus(Customer.CustomerStatus.INACTIVE);

        return customerRepository.save(customer);
    }

    /**
     * Approve account opening
     */
    public Account approveAccount(UUID approvalRequestId, String reviewedBy, String reviewNotes) {
        log.info("Approving account with approval request ID: {} by {}", approvalRequestId, reviewedBy);

        ApprovalRequest approvalRequest = getApprovalRequest(approvalRequestId);
        validateEntityType(approvalRequest, EntityType.ACCOUNT);

        // Get account
        Account account = accountRepository.findById(approvalRequest.getEntityId())
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + approvalRequest.getEntityId()));

        // Approve request
        approvalRequest.approve(reviewedBy, reviewNotes);
        approvalRequestRepository.save(approvalRequest);

        // Update account status
        account.setApprovalStatus(Account.ApprovalStatus.APPROVED);
        account.setStatus(Account.AccountStatus.ACTIVE);

        return accountRepository.save(account);
    }

    /**
     * Reject account opening
     */
    public Account rejectAccount(UUID approvalRequestId, String reviewedBy,
                                String rejectionReason, String reviewNotes) {
        log.info("Rejecting account with approval request ID: {} by {}", approvalRequestId, reviewedBy);

        ApprovalRequest approvalRequest = getApprovalRequest(approvalRequestId);
        validateEntityType(approvalRequest, EntityType.ACCOUNT);

        // Get account
        Account account = accountRepository.findById(approvalRequest.getEntityId())
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + approvalRequest.getEntityId()));

        // Reject request
        approvalRequest.reject(reviewedBy, rejectionReason, reviewNotes);
        approvalRequestRepository.save(approvalRequest);

        // Update account status
        account.setApprovalStatus(Account.ApprovalStatus.REJECTED);
        account.setStatus(Account.AccountStatus.CLOSED);

        return accountRepository.save(account);
    }

    /**
     * Get all pending approval requests
     */
    @Transactional(readOnly = true)
    public List<ApprovalRequest> getAllPendingApprovals() {
        return approvalRequestRepository.findAllPendingApprovals();
    }

    /**
     * Get pending approvals by branch
     */
    @Transactional(readOnly = true)
    public List<ApprovalRequest> getPendingApprovalsByBranch(Branch branch) {
        return approvalRequestRepository.findPendingApprovalsByBranch(branch);
    }

    /**
     * Get approval request by ID
     */
    @Transactional(readOnly = true)
    public ApprovalRequest getApprovalRequest(UUID id) {
        return approvalRequestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Approval request not found: " + id));
    }

    /**
     * Get pending approvals by request type
     */
    @Transactional(readOnly = true)
    public List<ApprovalRequest> getPendingApprovalsByType(RequestType requestType) {
        return approvalRequestRepository.findByRequestTypeAndApprovalStatusOrderByRequestedDateDesc(
            requestType, ApprovalStatus.PENDING);
    }

    /**
     * Count pending approvals
     */
    @Transactional(readOnly = true)
    public long countPendingApprovals() {
        return approvalRequestRepository.countByApprovalStatus(ApprovalStatus.PENDING);
    }

    /**
     * Count pending approvals by branch
     */
    @Transactional(readOnly = true)
    public long countPendingApprovalsByBranch(Branch branch) {
        return approvalRequestRepository.countByBranchAndApprovalStatus(branch, ApprovalStatus.PENDING);
    }

    /**
     * Check if entity has pending approval
     */
    @Transactional(readOnly = true)
    public boolean hasPendingApproval(EntityType entityType, UUID entityId) {
        return approvalRequestRepository.hasPendingApproval(entityType, entityId);
    }

    /**
     * Validate entity type matches request
     */
    private void validateEntityType(ApprovalRequest request, EntityType expectedType) {
        if (request.getEntityType() != expectedType) {
            throw new IllegalArgumentException(
                String.format("Invalid entity type. Expected %s but got %s",
                    expectedType, request.getEntityType()));
        }
    }
}
