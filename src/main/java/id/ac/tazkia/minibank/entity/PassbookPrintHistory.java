package id.ac.tazkia.minibank.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "passbook_print_history")
@Data
@NoArgsConstructor
public class PassbookPrintHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_passbooks", nullable = false)
    private Passbook passbook;

    // Print session details
    @Column(name = "print_date")
    private LocalDateTime printDate = LocalDateTime.now();

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "start_line", nullable = false)
    private Integer startLine;

    @Column(name = "end_line", nullable = false)
    private Integer endLine;

    @Column(name = "transactions_printed", nullable = false)
    private Integer transactionsPrinted = 0;

    // First and last transaction in this print session
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_first_transaction")
    private Transaction firstTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_last_transaction")
    private Transaction lastTransaction;

    // Printer info
    @Column(name = "printer_name", length = 100)
    private String printerName;

    @Column(name = "printer_port", length = 50)
    private String printerPort;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private PrintStatus status = PrintStatus.SUCCESS;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // Audit
    @Column(name = "printed_by", length = 100)
    private String printedBy;

    // Enums
    public enum PrintStatus {
        SUCCESS,    // All transactions printed successfully
        FAILED,     // Print failed completely
        PARTIAL     // Some transactions printed
    }
}
