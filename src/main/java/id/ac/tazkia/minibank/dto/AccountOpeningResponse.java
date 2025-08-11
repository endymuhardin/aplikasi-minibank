package id.ac.tazkia.minibank.dto;

import id.ac.tazkia.minibank.entity.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class AccountOpeningResponse {
    
    private UUID accountId;
    private String accountNumber;
    private String accountName;
    private BigDecimal balance;
    private Account.AccountStatus status;
    private LocalDate openedDate;
    private LocalDateTime createdDate;
    
    private CustomerInfo customer;
    private ProductInfo product;
    
    @Data
    @NoArgsConstructor
    public static class CustomerInfo {
        private UUID id;
        private String customerNumber;
        private String displayName;
        private String customerType;
    }
    
    @Data
    @NoArgsConstructor
    public static class ProductInfo {
        private UUID id;
        private String productCode;
        private String productName;
        private String productType;
        private BigDecimal minimumOpeningBalance;
        private BigDecimal profitSharingRatio;
    }
}