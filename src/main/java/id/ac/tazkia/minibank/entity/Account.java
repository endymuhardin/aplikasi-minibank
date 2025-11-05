package id.ac.tazkia.minibank.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_customers", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_products", nullable = false)
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_branches", nullable = false)
    private Branch branch;
    
    @Column(name = "account_number", unique = true, nullable = false, length = 50)
    private String accountNumber;
    
    @Column(name = "account_name", nullable = false, length = 200)
    private String accountName;
    
    @Column(name = "balance", precision = 20, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20, nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;

    // Audit fields
    @Column(name = "opened_date")
    private LocalDate openedDate = LocalDate.now();
    
    @Column(name = "closed_date")
    private LocalDate closedDate;
    
    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    // Relationships
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Transaction> transactions;
    
    // Business methods
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        if (this.status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Cannot deposit to a closed account");
        }
        this.balance = this.balance.add(amount);
    }
    
    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (this.status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Cannot withdraw from a closed account");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.balance = this.balance.subtract(amount);
    }
    
    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(this.status);
    }
    
    public boolean isClosed() {
        return AccountStatus.CLOSED.equals(this.status);
    }
    
    public void closeAccount() {
        if (this.status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Account is already closed");
        }
        if (this.balance.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Account balance must be zero before closure");
        }
        this.status = AccountStatus.CLOSED;
        this.closedDate = LocalDate.now();
    }
    
    public void transferOut(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (this.status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Cannot transfer from a closed account");
        }
        if (!this.isActive()) {
            throw new IllegalStateException("Account must be active to transfer funds");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance for transfer");
        }
        this.balance = this.balance.subtract(amount);
    }
    
    public void transferIn(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (this.status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Cannot transfer to a closed account");
        }
        if (!this.isActive()) {
            throw new IllegalStateException("Account must be active to receive transfers");
        }
        this.balance = this.balance.add(amount);
    }
    
    // Enums
    public enum AccountStatus {
        ACTIVE, INACTIVE, CLOSED, FROZEN
    }

    public enum ApprovalStatus {
        PENDING_APPROVAL, APPROVED, REJECTED
    }

    // Helper methods for approval status
    public boolean isPendingApproval() {
        return this.approvalStatus == ApprovalStatus.PENDING_APPROVAL;
    }

    public boolean isApproved() {
        return this.approvalStatus == ApprovalStatus.APPROVED;
    }

    public boolean isRejected() {
        return this.approvalStatus == ApprovalStatus.REJECTED;
    }
}