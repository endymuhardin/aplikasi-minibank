package id.ac.tazkia.minibank.controller.web;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.ApprovalRequest;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Web controller for approval workflow management.
 * Provides UI for branch managers to review and approve/reject customer and account requests.
 *
 * Security: All methods require APPROVAL_VIEW permission (Branch Manager only)
 */
@Slf4j
@Controller
@RequestMapping("/approval")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('APPROVAL_VIEW')")
public class ApprovalController {

    private static final String APPROVAL_QUEUE_VIEW = "approval/queue";
    private static final String APPROVAL_DETAIL_VIEW = "approval/detail";
    private static final String APPROVAL_QUEUE_REDIRECT = "redirect:/approval/queue";
    private static final String ERROR_MESSAGE_ATTR = "errorMessage";
    private static final String SUCCESS_MESSAGE_ATTR = "successMessage";
    private static final String APPROVAL_NOT_FOUND_MSG = "Approval request not found";

    private final ApprovalService approvalService;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    /**
     * Display approval queue with all pending approval requests
     */
    @GetMapping("/queue")
    public String queue(@RequestParam(required = false) String filterType, Model model) {
        log.info("Displaying approval queue");

        List<ApprovalRequest> pendingApprovals;

        if (filterType != null && !filterType.trim().isEmpty()) {
            // Filter by request type
            ApprovalRequest.RequestType requestType = ApprovalRequest.RequestType.valueOf(filterType);
            pendingApprovals = approvalService.getPendingApprovalsByType(requestType);
        } else {
            // Get all pending approvals
            pendingApprovals = approvalService.getAllPendingApprovals();
        }

        model.addAttribute("pendingApprovals", pendingApprovals);
        model.addAttribute("filterType", filterType);
        model.addAttribute("requestTypes", ApprovalRequest.RequestType.values());
        model.addAttribute("pendingCount", pendingApprovals.size());

        return APPROVAL_QUEUE_VIEW;
    }

    /**
     * Display approval request detail page with entity information
     */
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        log.info("Displaying approval request detail for ID: {}", id);

        try {
            ApprovalRequest approvalRequest = approvalService.getApprovalRequest(id);

            // Add approval request to model
            model.addAttribute("approvalRequest", approvalRequest);

            // Load and add entity details based on entity type
            if (approvalRequest.getEntityType() == ApprovalRequest.EntityType.CUSTOMER) {
                Optional<Customer> customerOpt = customerRepository.findById(approvalRequest.getEntityId());
                if (customerOpt.isPresent()) {
                    model.addAttribute("customer", customerOpt.get());
                }
            } else if (approvalRequest.getEntityType() == ApprovalRequest.EntityType.ACCOUNT) {
                Optional<Account> accountOpt = accountRepository.findById(approvalRequest.getEntityId());
                if (accountOpt.isPresent()) {
                    Account account = accountOpt.get();
                    model.addAttribute("account", account);
                    model.addAttribute("customer", account.getCustomer());
                    model.addAttribute("product", account.getProduct());
                }
            }

            return APPROVAL_DETAIL_VIEW;

        } catch (IllegalArgumentException e) {
            log.error("Approval request not found: {}", id);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, APPROVAL_NOT_FOUND_MSG);
            return APPROVAL_QUEUE_REDIRECT;
        }
    }

    /**
     * Approve a customer creation request
     * Requires CUSTOMER_APPROVE permission
     */
    @PostMapping("/approve/customer/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_APPROVE')")
    public String approveCustomer(@PathVariable UUID id,
                                  @RequestParam(required = false) String reviewNotes,
                                  RedirectAttributes redirectAttributes) {
        log.info("Approving customer request ID: {}", id);

        try {
            // TODO: Get actual username from security context when authentication is implemented
            String reviewedBy = "branch-manager";

            Customer customer = approvalService.approveCustomer(id, reviewedBy, reviewNotes);

            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR,
                "Customer " + customer.getCustomerNumber() + " has been approved successfully");
            return APPROVAL_QUEUE_REDIRECT;

        } catch (IllegalArgumentException e) {
            log.error("Failed to approve customer request: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, e.getMessage());
            return "redirect:/approval/detail/" + id;
        } catch (Exception e) {
            log.error("Failed to approve customer request", e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR,
                "Failed to approve request: " + e.getMessage());
            return "redirect:/approval/detail/" + id;
        }
    }

    /**
     * Reject a customer creation request
     * Requires CUSTOMER_APPROVE permission
     */
    @PostMapping("/reject/customer/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_APPROVE')")
    public String rejectCustomer(@PathVariable UUID id,
                                 @RequestParam String rejectionReason,
                                 @RequestParam(required = false) String reviewNotes,
                                 RedirectAttributes redirectAttributes) {
        log.info("Rejecting customer request ID: {}", id);

        // Validate rejection reason
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR,
                "Rejection reason is required");
            return "redirect:/approval/detail/" + id;
        }

        try {
            // TODO: Get actual username from security context when authentication is implemented
            String reviewedBy = "branch-manager";

            Customer customer = approvalService.rejectCustomer(id, reviewedBy,
                rejectionReason, reviewNotes);

            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR,
                "Customer " + customer.getCustomerNumber() + " request has been rejected");
            return APPROVAL_QUEUE_REDIRECT;

        } catch (IllegalArgumentException e) {
            log.error("Failed to reject customer request: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, e.getMessage());
            return "redirect:/approval/detail/" + id;
        } catch (Exception e) {
            log.error("Failed to reject customer request", e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR,
                "Failed to reject request: " + e.getMessage());
            return "redirect:/approval/detail/" + id;
        }
    }

    /**
     * Approve an account opening request
     * Requires ACCOUNT_APPROVE permission
     */
    @PostMapping("/approve/account/{id}")
    @PreAuthorize("hasAuthority('ACCOUNT_APPROVE')")
    public String approveAccount(@PathVariable UUID id,
                                @RequestParam(required = false) String reviewNotes,
                                RedirectAttributes redirectAttributes) {
        log.info("Approving account request ID: {}", id);

        try {
            // TODO: Get actual username from security context when authentication is implemented
            String reviewedBy = "branch-manager";

            Account account = approvalService.approveAccount(id, reviewedBy, reviewNotes);

            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR,
                "Account " + account.getAccountNumber() + " has been approved successfully");
            return APPROVAL_QUEUE_REDIRECT;

        } catch (IllegalArgumentException e) {
            log.error("Failed to approve account request: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, e.getMessage());
            return "redirect:/approval/detail/" + id;
        } catch (Exception e) {
            log.error("Failed to approve account request", e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR,
                "Failed to approve request: " + e.getMessage());
            return "redirect:/approval/detail/" + id;
        }
    }

    /**
     * Reject an account opening request
     * Requires ACCOUNT_APPROVE permission
     */
    @PostMapping("/reject/account/{id}")
    @PreAuthorize("hasAuthority('ACCOUNT_APPROVE')")
    public String rejectAccount(@PathVariable UUID id,
                               @RequestParam String rejectionReason,
                               @RequestParam(required = false) String reviewNotes,
                               RedirectAttributes redirectAttributes) {
        log.info("Rejecting account request ID: {}", id);

        // Validate rejection reason
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR,
                "Rejection reason is required");
            return "redirect:/approval/detail/" + id;
        }

        try {
            // TODO: Get actual username from security context when authentication is implemented
            String reviewedBy = "branch-manager";

            Account account = approvalService.rejectAccount(id, reviewedBy,
                rejectionReason, reviewNotes);

            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR,
                "Account " + account.getAccountNumber() + " request has been rejected");
            return APPROVAL_QUEUE_REDIRECT;

        } catch (IllegalArgumentException e) {
            log.error("Failed to reject account request: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, e.getMessage());
            return "redirect:/approval/detail/" + id;
        } catch (Exception e) {
            log.error("Failed to reject account request", e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR,
                "Failed to reject request: " + e.getMessage());
            return "redirect:/approval/detail/" + id;
        }
    }
}
