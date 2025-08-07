package id.ac.tazkia.minibank.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank(message = "Product code is required")
    @Size(max = 20, message = "Product code must not exceed 20 characters")
    @Column(name = "product_code", unique = true, nullable = false, length = 20)
    private String productCode;
    
    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name must not exceed 100 characters")
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;
    
    @NotNull(message = "Product type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ProductType productType;
    
    @NotBlank(message = "Product category is required")
    @Size(max = 50, message = "Product category must not exceed 50 characters")
    @Column(name = "product_category", nullable = false, length = 50)
    private String productCategory;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    // Basic product settings
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    @Column(name = "currency", length = 3)
    private String currency = "IDR";
    
    // Balance and limit configurations
    @Column(name = "minimum_opening_balance", precision = 20, scale = 2)
    private BigDecimal minimumOpeningBalance = BigDecimal.ZERO;
    
    @Column(name = "minimum_balance", precision = 20, scale = 2)
    private BigDecimal minimumBalance = BigDecimal.ZERO;
    
    @Column(name = "maximum_balance", precision = 20, scale = 2)
    private BigDecimal maximumBalance;
    
    @Column(name = "daily_withdrawal_limit", precision = 20, scale = 2)
    private BigDecimal dailyWithdrawalLimit;
    
    @Column(name = "monthly_transaction_limit")
    private Integer monthlyTransactionLimit;
    
    @Column(name = "overdraft_limit", precision = 20, scale = 2)
    private BigDecimal overdraftLimit = BigDecimal.ZERO;
    
    // Interest configurations
    @Column(name = "interest_rate", precision = 5, scale = 4)
    @DecimalMin("0.0") @DecimalMax("1.0")
    private BigDecimal interestRate = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "interest_calculation_type", length = 20)
    private InterestCalculationType interestCalculationType = InterestCalculationType.DAILY;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "interest_payment_frequency", length = 20)
    private InterestPaymentFrequency interestPaymentFrequency = InterestPaymentFrequency.MONTHLY;
    
    // Fee configurations
    @Column(name = "monthly_maintenance_fee", precision = 15, scale = 2)
    private BigDecimal monthlyMaintenanceFee = BigDecimal.ZERO;
    
    @Column(name = "atm_withdrawal_fee", precision = 15, scale = 2)
    private BigDecimal atmWithdrawalFee = BigDecimal.ZERO;
    
    @Column(name = "inter_bank_transfer_fee", precision = 15, scale = 2)
    private BigDecimal interBankTransferFee = BigDecimal.ZERO;
    
    @Column(name = "below_minimum_balance_fee", precision = 15, scale = 2)
    private BigDecimal belowMinimumBalanceFee = BigDecimal.ZERO;
    
    @Column(name = "account_closure_fee", precision = 15, scale = 2)
    private BigDecimal accountClosureFee = BigDecimal.ZERO;
    
    // Transaction configurations
    @Column(name = "free_transactions_per_month")
    private Integer freeTransactionsPerMonth = 0;
    
    @Column(name = "excess_transaction_fee", precision = 15, scale = 2)
    private BigDecimal excessTransactionFee = BigDecimal.ZERO;
    
    @Column(name = "allow_overdraft")
    private Boolean allowOverdraft = false;
    
    @Column(name = "require_maintaining_balance")
    private Boolean requireMaintainingBalance = true;
    
    // Customer eligibility
    @Column(name = "min_customer_age")
    private Integer minCustomerAge;
    
    @Column(name = "max_customer_age")
    private Integer maxCustomerAge;
    
    @Column(name = "allowed_customer_types", length = 50)
    private String allowedCustomerTypes = "PERSONAL,CORPORATE";
    
    @Column(name = "required_documents", columnDefinition = "TEXT")
    private String requiredDocuments;
    
    // Product lifecycle
    @Column(name = "launch_date")
    private LocalDate launchDate;
    
    @Column(name = "retirement_date")
    private LocalDate retirementDate;
    
    // Audit fields
    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    // Relationships
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Account> accounts;
    
    // Enums
    public enum ProductType {
        SAVINGS, CHECKING, LOAN, CREDIT_CARD, DEPOSIT
    }
    
    public enum InterestCalculationType {
        DAILY, MONTHLY, ANNUAL
    }
    
    public enum InterestPaymentFrequency {
        DAILY, MONTHLY, QUARTERLY, ANNUALLY
    }
}