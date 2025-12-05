package id.ac.tazkia.minibank.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request to record passbook print result from browser
 */
@Data
@NoArgsConstructor
public class PassbookPrintResultRequest {

    @NotNull(message = "Account ID is required")
    private UUID accountId;

    @NotNull(message = "Print status is required")
    private PrintStatus status;

    // IDs of transactions that were printed
    private List<UUID> printedTransactionIds;

    // Printer info (from Web Serial API)
    private String printerName;
    private String printerPort;

    // Error info (if failed)
    private String errorMessage;

    public enum PrintStatus {
        SUCCESS,
        FAILED,
        PARTIAL
    }
}
