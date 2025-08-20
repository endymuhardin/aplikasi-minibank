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
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class AccountStatementPdfService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");

    public byte[] generateAccountStatementPdf(Account account, List<Transaction> transactions, 
                                            LocalDate startDate, LocalDate endDate) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Header
            addHeader(document, account, startDate, endDate);
            
            // Account Information
            addAccountInfo(document, account);
            
            // Transaction Table
            addTransactionTable(document, transactions);
            
            // Summary
            addSummary(document, transactions, account);

            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating PDF statement for account: {}", account.getAccountNumber(), e);
            throw new RuntimeException("Failed to generate PDF statement", e);
        }
    }

    private void addHeader(Document document, Account account, LocalDate startDate, LocalDate endDate) throws DocumentException {
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
        Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
        
        Paragraph title = new Paragraph("MINIBANK SYARIAH", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        Paragraph subtitle = new Paragraph("REKENING KORAN / ACCOUNT STATEMENT", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitle);
        
        Paragraph period = new Paragraph("Periode: " + startDate.format(DATE_FORMAT) + " s/d " + endDate.format(DATE_FORMAT), normalFont);
        period.setAlignment(Element.ALIGN_CENTER);
        document.add(period);
        
        document.add(new Paragraph(" "));
    }

    private void addAccountInfo(Document document, Account account) throws DocumentException {
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{30, 70});

        PdfPCell cell1 = new PdfPCell(new Phrase("Nomor Rekening:", normalFont));
        cell1.setBorder(Rectangle.NO_BORDER);
        infoTable.addCell(cell1);
        
        PdfPCell cell2 = new PdfPCell(new Phrase(account.getAccountNumber(), normalFont));
        cell2.setBorder(Rectangle.NO_BORDER);
        infoTable.addCell(cell2);

        cell1 = new PdfPCell(new Phrase("Nama Rekening:", normalFont));
        cell1.setBorder(Rectangle.NO_BORDER);
        infoTable.addCell(cell1);
        
        cell2 = new PdfPCell(new Phrase(account.getAccountName(), normalFont));
        cell2.setBorder(Rectangle.NO_BORDER);
        infoTable.addCell(cell2);

        cell1 = new PdfPCell(new Phrase("Nama Nasabah:", normalFont));
        cell1.setBorder(Rectangle.NO_BORDER);
        infoTable.addCell(cell1);
        
        cell2 = new PdfPCell(new Phrase(account.getCustomer().getDisplayName(), normalFont));
        cell2.setBorder(Rectangle.NO_BORDER);
        infoTable.addCell(cell2);

        cell1 = new PdfPCell(new Phrase("Jenis Produk:", normalFont));
        cell1.setBorder(Rectangle.NO_BORDER);
        infoTable.addCell(cell1);
        
        cell2 = new PdfPCell(new Phrase(account.getProduct().getProductName(), normalFont));
        cell2.setBorder(Rectangle.NO_BORDER);
        infoTable.addCell(cell2);

        cell1 = new PdfPCell(new Phrase("Saldo Saat Ini:", normalFont));
        cell1.setBorder(Rectangle.NO_BORDER);
        infoTable.addCell(cell1);
        
        cell2 = new PdfPCell(new Phrase("IDR " + CURRENCY_FORMAT.format(account.getBalance()), boldFont));
        cell2.setBorder(Rectangle.NO_BORDER);
        infoTable.addCell(cell2);

        document.add(infoTable);
        document.add(new Paragraph(" "));
    }

    private void addTransactionTable(Document document, List<Transaction> transactions) throws DocumentException {
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
        Font boldSmallFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
        
        Paragraph header = new Paragraph("MUTASI REKENING / TRANSACTION HISTORY", headerFont);
        document.add(header);

        if (transactions.isEmpty()) {
            Paragraph noTransactions = new Paragraph("Tidak ada transaksi dalam periode ini.", smallFont);
            noTransactions.setAlignment(Element.ALIGN_CENTER);
            document.add(noTransactions);
            return;
        }

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{15, 15, 30, 15, 15, 15});

        // Headers
        PdfPCell cell = new PdfPCell(new Phrase("Tanggal", boldSmallFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("No. Transaksi", boldSmallFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Keterangan", boldSmallFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Debet", boldSmallFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Kredit", boldSmallFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Saldo", boldSmallFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);

        // Transaction rows
        for (Transaction transaction : transactions) {
            table.addCell(new PdfPCell(new Phrase(transaction.getTransactionDate().format(DATE_TIME_FORMAT), smallFont)));
            table.addCell(new PdfPCell(new Phrase(transaction.getTransactionNumber(), smallFont)));
            
            String description = buildTransactionDescription(transaction);
            table.addCell(new PdfPCell(new Phrase(description, smallFont)));

            // Debit column
            if (transaction.isDebitTransaction()) {
                cell = new PdfPCell(new Phrase(CURRENCY_FORMAT.format(transaction.getAmount()), smallFont));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);
                
                cell = new PdfPCell(new Phrase("-", smallFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            } else {
                cell = new PdfPCell(new Phrase("-", smallFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
                
                cell = new PdfPCell(new Phrase(CURRENCY_FORMAT.format(transaction.getAmount()), smallFont));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);
            }

            cell = new PdfPCell(new Phrase(CURRENCY_FORMAT.format(transaction.getBalanceAfter()), smallFont));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);
        }

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private String buildTransactionDescription(Transaction transaction) {
        StringBuilder desc = new StringBuilder();
        desc.append(getTransactionTypeDescription(transaction.getTransactionType()));
        
        if (transaction.getDescription() != null && !transaction.getDescription().isEmpty()) {
            desc.append(" - ").append(transaction.getDescription());
        }
        
        if (transaction.getChannel() != null) {
            desc.append(" (").append(getChannelDescription(transaction.getChannel())).append(")");
        }
        
        return desc.toString();
    }

    private String getTransactionTypeDescription(Transaction.TransactionType type) {
        return switch (type) {
            case DEPOSIT -> "SETORAN TUNAI";
            case WITHDRAWAL -> "PENARIKAN TUNAI";
            case TRANSFER_IN -> "TRANSFER MASUK";
            case TRANSFER_OUT -> "TRANSFER KELUAR";
            case FEE -> "BIAYA ADMINISTRASI";
        };
    }

    private String getChannelDescription(Transaction.TransactionChannel channel) {
        return switch (channel) {
            case TELLER -> "TELLER";
            case ATM -> "ATM";
            case ONLINE -> "INTERNET BANKING";
            case MOBILE -> "MOBILE BANKING";
            case TRANSFER -> "TRANSFER";
        };
    }

    private void addSummary(Document document, List<Transaction> transactions, Account account) throws DocumentException {
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
        
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        
        for (Transaction transaction : transactions) {
            if (transaction.isDebitTransaction()) {
                totalDebit = totalDebit.add(transaction.getAmount());
            } else {
                totalCredit = totalCredit.add(transaction.getAmount());
            }
        }

        Paragraph summaryHeader = new Paragraph("RINGKASAN / SUMMARY", headerFont);
        document.add(summaryHeader);

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(50);
        summaryTable.setWidths(new float[]{50, 50});

        PdfPCell cell1 = new PdfPCell(new Phrase("Total Debet:", normalFont));
        cell1.setBorder(Rectangle.NO_BORDER);
        summaryTable.addCell(cell1);
        
        PdfPCell cell2 = new PdfPCell(new Phrase("IDR " + CURRENCY_FORMAT.format(totalDebit), normalFont));
        cell2.setBorder(Rectangle.NO_BORDER);
        cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summaryTable.addCell(cell2);

        cell1 = new PdfPCell(new Phrase("Total Kredit:", normalFont));
        cell1.setBorder(Rectangle.NO_BORDER);
        summaryTable.addCell(cell1);
        
        cell2 = new PdfPCell(new Phrase("IDR " + CURRENCY_FORMAT.format(totalCredit), normalFont));
        cell2.setBorder(Rectangle.NO_BORDER);
        cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summaryTable.addCell(cell2);

        cell1 = new PdfPCell(new Phrase("Jumlah Transaksi:", normalFont));
        cell1.setBorder(Rectangle.NO_BORDER);
        summaryTable.addCell(cell1);
        
        cell2 = new PdfPCell(new Phrase(String.valueOf(transactions.size()), normalFont));
        cell2.setBorder(Rectangle.NO_BORDER);
        cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summaryTable.addCell(cell2);

        document.add(summaryTable);
        
        document.add(new Paragraph(" "));
        
        Paragraph printDate = new Paragraph("Dicetak pada: " + LocalDate.now().format(DATE_FORMAT), smallFont);
        printDate.setAlignment(Element.ALIGN_RIGHT);
        document.add(printDate);
    }
}