package id.ac.tazkia.minibank.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_accounts", nullable = false)
    private Account account;
    
    @Column(name = "transaction_number", unique = true, nullable = false, length = 50)
    private String transactionNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;
    
    @Column(name = "amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "currency", length = 3)
    private String currency = "IDR";
    
    @Column(name = "balance_before", nullable = false, precision = 20, scale = 2)
    private BigDecimal balanceBefore;
    
    @Column(name = "balance_after", nullable = false, precision = 20, scale = 2)
    private BigDecimal balanceAfter;
    
    // Transaction details
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 50)
    private TransactionChannel channel = TransactionChannel.TELLER;
    
    // Transfer related fields (for future use)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_accounts_destination")
    private Account destinationAccount;
    
    // Audit fields
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate = LocalDateTime.now();
    
    @Column(name = "processed_date")
    private LocalDateTime processedDate = LocalDateTime.now();
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    // Business methods
    public boolean isDebitTransaction() {
        return TransactionType.WITHDRAWAL.equals(this.transactionType) || 
               TransactionType.TRANSFER_OUT.equals(this.transactionType) ||
               TransactionType.FEE.equals(this.transactionType);
    }
    
    public boolean isCreditTransaction() {
        return TransactionType.DEPOSIT.equals(this.transactionType) || 
               TransactionType.TRANSFER_IN.equals(this.transactionType);
    }
    
    // Enums
    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT, FEE
    }
    
    public enum TransactionChannel {
        TELLER, ATM, ONLINE, MOBILE, TRANSFER
    }
}