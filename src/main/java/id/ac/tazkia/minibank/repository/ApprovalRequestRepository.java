package id.ac.tazkia.minibank.repository;

import id.ac.tazkia.minibank.entity.ApprovalRequest;
import id.ac.tazkia.minibank.entity.ApprovalRequest.ApprovalStatus;
import id.ac.tazkia.minibank.entity.ApprovalRequest.EntityType;
import id.ac.tazkia.minibank.entity.ApprovalRequest.RequestType;
import id.ac.tazkia.minibank.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ApprovalRequest entity with comprehensive query methods
 */
@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, UUID> {

    /**
     * Find all approval requests by status
     */
    List<ApprovalRequest> findByApprovalStatusOrderByRequestedDateDesc(ApprovalStatus status);

    /**
     * Find all approval requests by branch and status
     */
    @Query("SELECT ar FROM ApprovalRequest ar LEFT JOIN FETCH ar.branch " +
           "WHERE ar.branch = :branch AND ar.approvalStatus = :status " +
           "ORDER BY ar.requestedDate DESC")
    List<ApprovalRequest> findByBranchAndApprovalStatus(@Param("branch") Branch branch,
                                                          @Param("status") ApprovalStatus status);

    /**
     * Find all approval requests by status with eager loading
     */
    @Query("SELECT ar FROM ApprovalRequest ar LEFT JOIN FETCH ar.branch " +
           "WHERE ar.approvalStatus = :status " +
           "ORDER BY ar.requestedDate DESC")
    List<ApprovalRequest> findByApprovalStatusWithBranch(@Param("status") ApprovalStatus status);

    /**
     * Find approval request by entity type and ID
     */
    Optional<ApprovalRequest> findByEntityTypeAndEntityId(EntityType entityType, UUID entityId);

    /**
     * Find all approval requests by entity type and approval status
     */
    List<ApprovalRequest> findByEntityTypeAndApprovalStatusOrderByRequestedDateDesc(
        EntityType entityType, ApprovalStatus status);

    /**
     * Find all approval requests by request type
     */
    List<ApprovalRequest> findByRequestTypeOrderByRequestedDateDesc(RequestType requestType);

    /**
     * Find all approval requests by request type and status
     */
    List<ApprovalRequest> findByRequestTypeAndApprovalStatusOrderByRequestedDateDesc(
        RequestType requestType, ApprovalStatus status);

    /**
     * Find all approval requests requested by a specific user
     */
    List<ApprovalRequest> findByRequestedByOrderByRequestedDateDesc(String requestedBy);

    /**
     * Find all approval requests reviewed by a specific user
     */
    List<ApprovalRequest> findByReviewedByOrderByReviewedDateDesc(String reviewedBy);

    /**
     * Check if there's a pending approval for an entity
     */
    @Query("SELECT CASE WHEN COUNT(ar) > 0 THEN true ELSE false END FROM ApprovalRequest ar " +
           "WHERE ar.entityType = :entityType AND ar.entityId = :entityId " +
           "AND ar.approvalStatus = 'PENDING'")
    boolean hasPendingApproval(@Param("entityType") EntityType entityType,
                                @Param("entityId") UUID entityId);

    /**
     * Count pending approvals
     */
    long countByApprovalStatus(ApprovalStatus status);

    /**
     * Count pending approvals by branch
     */
    long countByBranchAndApprovalStatus(Branch branch, ApprovalStatus status);

    /**
     * Count pending approvals by request type
     */
    long countByRequestTypeAndApprovalStatus(RequestType requestType, ApprovalStatus status);

    /**
     * Find all pending approvals with entity details (for dashboard)
     */
    @Query("SELECT ar FROM ApprovalRequest ar LEFT JOIN FETCH ar.branch " +
           "WHERE ar.approvalStatus = 'PENDING' " +
           "ORDER BY ar.requestedDate DESC")
    List<ApprovalRequest> findAllPendingApprovals();

    /**
     * Find pending approvals by branch with entity details
     */
    @Query("SELECT ar FROM ApprovalRequest ar LEFT JOIN FETCH ar.branch " +
           "WHERE ar.branch = :branch AND ar.approvalStatus = 'PENDING' " +
           "ORDER BY ar.requestedDate DESC")
    List<ApprovalRequest> findPendingApprovalsByBranch(@Param("branch") Branch branch);
}
