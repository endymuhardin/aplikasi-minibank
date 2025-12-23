package id.ac.tazkia.minibank.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "passbooks")
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Passbook {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_accounts", nullable = false, unique = true)
    private Account account;

    @Column(name = "passbook_number", unique = true, nullable = false, length = 50)
    private String passbookNumber;

    // Printing state tracking
    @Column(name = "current_page", nullable = false)
    private Integer currentPage = 1;

    @Column(name = "last_printed_line", nullable = false)
    private Integer lastPrintedLine = 0;

    @Column(name = "lines_per_page", nullable = false)
    private Integer linesPerPage = 30;

    // Last printed transaction tracking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_last_printed_transaction")
    private Transaction lastPrintedTransaction;

    @Column(name = "last_print_date")
    private LocalDateTime lastPrintDate;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private PassbookStatus status = PassbookStatus.ACTIVE;

    // Audit fields
    @Column(name = "issued_date")
    private LocalDate issuedDate = LocalDate.now();

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

    // Business methods

    /**
     * Check if passbook can accept more prints on current page
     */
    public boolean hasSpaceOnCurrentPage() {
        return lastPrintedLine < linesPerPage;
    }

    /**
     * Get remaining lines on current page
     */
    public int getRemainingLines() {
        return linesPerPage - lastPrintedLine;
    }

    /**
     * Advance to next page
     */
    public void advanceToNextPage() {
        this.currentPage++;
        this.lastPrintedLine = 0;
    }

    /**
     * Update after printing transactions
     */
    public void updateAfterPrint(int linesPrinted, Transaction lastTransaction) {
        this.lastPrintedLine += linesPrinted;
        this.lastPrintedTransaction = lastTransaction;
        this.lastPrintDate = LocalDateTime.now();

        // Auto-advance if page is full
        if (this.lastPrintedLine >= this.linesPerPage) {
            advanceToNextPage();
        }
    }

    /**
     * Mark passbook as full (no more pages available)
     */
    public void markAsFull() {
        this.status = PassbookStatus.FULL;
    }

    /**
     * Mark passbook as lost
     */
    public void markAsLost() {
        this.status = PassbookStatus.LOST;
    }

    /**
     * Check if passbook is active and can be printed
     */
    public boolean isActive() {
        return PassbookStatus.ACTIVE.equals(this.status);
    }

    // Enums
    public enum PassbookStatus {
        ACTIVE, // Can be used for printing
        FULL, // All pages used, needs replacement
        LOST, // Reported lost
        REPLACED, // Replaced with new passbook
        CLOSED // Account closed
    }
}
