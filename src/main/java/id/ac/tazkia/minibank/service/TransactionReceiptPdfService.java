package id.ac.tazkia.minibank.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class TransactionReceiptPdfService {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8);

    public byte[] generateTransactionReceiptPdf(Transaction transaction, Account account, BigDecimal balanceAfter) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Create document with smaller page size for receipt
            Rectangle pageSize = new Rectangle(226, 567); // 8cm x 20cm in points
            Document document = new Document(pageSize, 10, 10, 10, 10);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Bank Header
            addBankHeader(document);
            
            // Receipt Title
            addReceiptTitle(document, transaction);
            
            // Transaction Details
            addTransactionDetails(document, transaction, account, balanceAfter);
            
            // Footer
            addReceiptFooter(document);

            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating transaction receipt PDF for transaction: {}", transaction.getTransactionNumber(), e);
            throw new RuntimeException("Failed to generate transaction receipt PDF", e);
        }
    }

    private void addBankHeader(Document document) throws DocumentException {
        // Bank logo/name section
        Paragraph bankName = new Paragraph("MINI BANK SYARIAH", HEADER_FONT);
        bankName.setAlignment(Element.ALIGN_CENTER);
        document.add(bankName);

        Paragraph address = new Paragraph("Jl. Islamic Banking No. 123\nJakarta, Indonesia", SMALL_FONT);
        address.setAlignment(Element.ALIGN_CENTER);
        document.add(address);

        // Add separator line
        document.add(new Paragraph("=" + "=".repeat(25), SMALL_FONT));
        document.add(Chunk.NEWLINE);
    }

    private void addReceiptTitle(Document document, Transaction transaction) throws DocumentException {
        String title = getReceiptTitle(transaction.getTransactionType());
        Paragraph receiptTitle = new Paragraph(title, TITLE_FONT);
        receiptTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(receiptTitle);
        
        document.add(new Paragraph("RECEIPT / STRUK", NORMAL_FONT) {{
            setAlignment(Element.ALIGN_CENTER);
        }});
        document.add(Chunk.NEWLINE);
    }

    private void addTransactionDetails(Document document, Transaction transaction, Account account, BigDecimal balanceAfter) throws DocumentException {
        // Create details table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{40, 60});

        // Transaction Number
        addReceiptRow(table, "Receipt No:", transaction.getTransactionNumber());
        
        // Date & Time
        addReceiptRow(table, "Date & Time:", transaction.getTransactionDate().format(DATE_TIME_FORMAT));
        
        // Account Information
        addReceiptRow(table, "Account No:", account.getAccountNumber());
        addReceiptRow(table, "Account Name:", account.getAccountName());
        
        // Transaction Type
        addReceiptRow(table, "Transaction:", getTransactionTypeDisplay(transaction.getTransactionType()));
        
        // Channel
        addReceiptRow(table, "Channel:", transaction.getChannel().name());
        
        // Amount
        String amountText = "IDR " + CURRENCY_FORMAT.format(transaction.getAmount());
        addReceiptRow(table, "Amount:", amountText);
        
        // Balance after transaction
        if (balanceAfter != null) {
            String balanceText = "IDR " + CURRENCY_FORMAT.format(balanceAfter);
            addReceiptRow(table, "Balance:", balanceText);
        }
        
        // Description if available
        if (transaction.getDescription() != null && !transaction.getDescription().trim().isEmpty()) {
            addReceiptRow(table, "Description:", transaction.getDescription());
        }

        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addReceiptFooter(Document document) throws DocumentException {
        document.add(new Paragraph("=" + "=".repeat(25), SMALL_FONT));
        
        Paragraph footer = new Paragraph("Thank you for banking with us\nTerima kasih telah menggunakan layanan kami", SMALL_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
        
        document.add(Chunk.NEWLINE);
        
        Paragraph timestamp = new Paragraph("Printed: " + java.time.LocalDateTime.now().format(DATE_TIME_FORMAT), SMALL_FONT);
        timestamp.setAlignment(Element.ALIGN_CENTER);
        document.add(timestamp);
        
        // Customer service info
        Paragraph support = new Paragraph("Customer Service: 1500-123\nwww.minibank.co.id", SMALL_FONT);
        support.setAlignment(Element.ALIGN_CENTER);
        document.add(support);
    }

    private void addReceiptRow(PdfPTable table, String label, String value) {
        // Label cell
        PdfPCell labelCell = new PdfPCell(new Phrase(label, SMALL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(2f);
        table.addCell(labelCell);
        
        // Value cell
        PdfPCell valueCell = new PdfPCell(new Phrase(value, SMALL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(2f);
        table.addCell(valueCell);
    }

    private String getReceiptTitle(Transaction.TransactionType type) {
        return switch (type) {
            case DEPOSIT -> "CASH DEPOSIT";
            case WITHDRAWAL -> "CASH WITHDRAWAL";
            case TRANSFER_OUT -> "TRANSFER OUT";
            case TRANSFER_IN -> "TRANSFER IN";
            default -> "TRANSACTION";
        };
    }

    private String getTransactionTypeDisplay(Transaction.TransactionType type) {
        return switch (type) {
            case DEPOSIT -> "Cash Deposit";
            case WITHDRAWAL -> "Cash Withdrawal";
            case TRANSFER_OUT -> "Transfer Out";
            case TRANSFER_IN -> "Transfer In";
            default -> type.name();
        };
    }
}