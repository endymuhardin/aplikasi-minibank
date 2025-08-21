package id.ac.tazkia.minibank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
public class TransferRequest {
    
    @NotNull(message = "Source account ID is required")
    private UUID fromAccountId;
    
    @NotNull(message = "Destination account is required")
    @Size(min = 1, max = 50, message = "Destination account number must be between 1 and 50 characters")
    private String toAccountNumber;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description = "Transfer Dana";
    
    @Size(max = 100, message = "Reference number cannot exceed 100 characters")
    private String referenceNumber;
    
    @Size(max = 100, message = "Created by cannot exceed 100 characters")
    private String createdBy;
    
    // Additional fields for validation and confirmation
    private String destinationAccountName;
    private String destinationCustomerName;
    private UUID toAccountId; // Set after validation
}