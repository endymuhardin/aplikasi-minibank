package id.ac.tazkia.minibank.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an approval workflow request.
 * Tracks customer and account creation approvals with complete audit trail.
 */
@Entity
@Table(name = "approval_requests", indexes = {
    @Index(name = "idx_approval_requests_status", columnList = "approval_status"),
    @Index(name = "idx_approval_requests_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_approval_requests_branch", columnList = "branch_id"),
    @Index(name = "idx_approval_requests_requested_by", columnList = "requested_by"),
    @Index(name = "idx_approval_requests_request_type", columnList = "request_type"),
    @Index(name = "idx_approval_requests_branch_status", columnList = "branch_id, approval_status"),
    @Index(name = "idx_approval_requests_status_date", columnList = "approval_status, requested_date")
})
@Data
@NoArgsConstructor
public class ApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Request type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 50)
    private RequestType requestType;

    @NotNull(message = "Entity type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private EntityType entityType;

    @NotNull(message = "Entity ID is required")
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @NotNull(message = "Approval status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @NotBlank(message = "Requested by is required")
    @Column(name = "requested_by", nullable = false, length = 100)
    private String requestedBy;

    @Column(name = "request_notes", columnDefinition = "TEXT")
    private String requestNotes;

    @NotNull(message = "Requested date is required")
    @Column(name = "requested_date", nullable = false)
    private LocalDateTime requestedDate;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Column(name = "reviewed_date")
    private LocalDateTime reviewedDate;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    /**
     * Business method to approve the request
     */
    public void approve(String reviewedBy, String reviewNotes) {
        if (!isPending()) {
            throw new IllegalStateException("Only pending approval requests can be approved");
        }
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.reviewedBy = reviewedBy;
        this.reviewNotes = reviewNotes;
        this.reviewedDate = LocalDateTime.now();
    }

    /**
     * Business method to reject the request
     */
    public void reject(String reviewedBy, String rejectionReason, String reviewNotes) {
        if (!isPending()) {
            throw new IllegalStateException("Only pending approval requests can be rejected");
        }
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.reviewedBy = reviewedBy;
        this.rejectionReason = rejectionReason;
        this.reviewNotes = reviewNotes;
        this.reviewedDate = LocalDateTime.now();
    }

    /**
     * Check if request is pending
     */
    public boolean isPending() {
        return this.approvalStatus == ApprovalStatus.PENDING;
    }

    /**
     * Check if request is approved
     */
    public boolean isApproved() {
        return this.approvalStatus == ApprovalStatus.APPROVED;
    }

    /**
     * Check if request is rejected
     */
    public boolean isRejected() {
        return this.approvalStatus == ApprovalStatus.REJECTED;
    }

    /**
     * Type of approval request
     */
    public enum RequestType {
        CUSTOMER_CREATION,
        ACCOUNT_OPENING
    }

    /**
     * Entity being approved
     */
    public enum EntityType {
        CUSTOMER,
        ACCOUNT
    }

    /**
     * Status of approval
     */
    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    /**
     * Pre-persist hook to set requested date
     */
    @PrePersist
    protected void onCreate() {
        if (this.requestedDate == null) {
            this.requestedDate = LocalDateTime.now();
        }
    }
}
