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
        private String sandiCode;      // Transaction code for passbook (e.g., "52", "26")
        private String tellerName;     // Teller/operator name (Petugas)
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

        // Column positions (in characters from left) - matches physical passbook layout
        // Columns: Tanggal | Sandi | Mutasi Debit | Mutasi Kredit | Saldo | Petugas
        private Integer dateColumn = 0;          // DD/MM/YYYY (10 chars)
        private Integer sandiColumn = 11;        // Transaction code (13 chars)
        private Integer debitColumn = 24;        // Debit amount (14 chars)
        private Integer creditColumn = 38;       // Credit amount (14 chars)
        private Integer balanceColumn = 52;      // Balance (14 chars)
        private Integer tellerColumn = 66;       // Teller name (14 chars)

        // Column widths
        private Integer dateWidth = 10;
        private Integer sandiWidth = 12;
        private Integer amountWidth = 13;
        private Integer tellerWidth = 14;

        // Date format for passbook
        private String dateFormat = "dd/MM/yyyy";

        // Amount format
        private Integer decimalPlaces = 2;

        // Line spacing (1/6 inch = 1, 1/8 inch = 0)
        private Integer lineSpacing = 1;

        // Starting line position on passbook page
        private Integer startingLinePosition = 1;
    }
}
