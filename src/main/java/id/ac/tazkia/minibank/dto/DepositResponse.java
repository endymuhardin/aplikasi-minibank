package id.ac.tazkia.minibank.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class DepositResponse {
    
    private UUID transactionId;
    private String transactionNumber;
    private UUID accountId;
    private String accountNumber;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String description;
    private String referenceNumber;
    private String currency;
    private String channel;
    private LocalDateTime transactionDate;
    private LocalDateTime processedDate;
    
    @Data
    @NoArgsConstructor
    public static class AccountInfo {
        private UUID id;
        private String accountNumber;
        private String accountName;
        private BigDecimal currentBalance;
    }
    
    private AccountInfo account;
}