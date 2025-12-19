package id.ac.tazkia.minibank.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response containing all data needed for passbook printing via Web Serial API
 */
@Data
@NoArgsConstructor
public class PassbookPrintDataResponse {

    // Passbook state
    private PassbookInfo passbook;

    // Account info
    private AccountInfo account;

    // Customer info
    private CustomerInfo customer;

    // Transactions to print
    private List<TransactionLine> transactions;

    // Print configuration
    private PrintConfig config;

    @Data
    @NoArgsConstructor
    public static class PassbookInfo {
        private UUID id;
        private String passbookNumber;
        private Integer currentPage;
        private Integer lastPrintedLine;
        private Integer linesPerPage;
        private Integer remainingLines;
        private String status;
        private LocalDateTime lastPrintDate;
    }

    @Data
    @NoArgsConstructor
    public static class AccountInfo {
        private UUID id;
        private String accountNumber;
        private String accountName;
        private BigDecimal currentBalance;
        private String productName;
        private LocalDate openedDate;
        private String status;
    }

    @Data
    @NoArgsConstructor
    public static class CustomerInfo {
        private UUID id;
        private String customerNumber;
        private String customerName;
        private String customerType;
    }

    @Data
    @NoArgsConstructor
    public static class TransactionLine {
        private UUID id;
        private String transactionNumber;
        private LocalDateTime transactionDate;
        private String description;
        private String transactionType;
        private BigDecimal debit;
        private BigDecimal credit;
        private BigDecimal balance;
        private Integer lineNumber; // Line position on passbook page
    }

    @Data
    @NoArgsConstructor
    public static class PrintConfig {
        // Epson PLQ-20 specific settings
        private Integer charactersPerLine = 80;
        private Integer linesPerPage = 20;

        // Column positions (in characters from left)
        private Integer dateColumn = 0;
        private Integer descriptionColumn = 11;
        private Integer debitColumn = 35;
        private Integer creditColumn = 50;
        private Integer balanceColumn = 65;

        // Date format for passbook
        private String dateFormat = "dd/MM/yyyy";

        // Amount format
        private Integer amountWidth = 14;
        private Integer decimalPlaces = 2;

        // Line spacing (1/6 inch = 1, 1/8 inch = 0)
        private Integer lineSpacing = 1;

        // Starting line position on passbook page
        private Integer startingLinePosition = 1;
    }
}
