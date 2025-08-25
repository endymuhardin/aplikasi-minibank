package id.ac.tazkia.minibank.service;

import id.ac.tazkia.minibank.config.TestDataFactory;
import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.entity.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Transaction Receipt PDF Service Tests")
class TransactionReceiptPdfServiceTest {

    private TransactionReceiptPdfService receiptPdfService;
    private Transaction testTransaction;
    private Account testAccount;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        receiptPdfService = new TransactionReceiptPdfService();
        
        // Create test customer using TestDataFactory for parallel execution safety
        PersonalCustomer personalCustomer = new PersonalCustomer();
        personalCustomer.setId(UUID.randomUUID());
        personalCustomer.setCustomerNumber(TestDataFactory.generateCustomerCode());
        personalCustomer.setFirstName("John");
        personalCustomer.setLastName("Doe");
        testCustomer = personalCustomer;

        // Create test product using TestDataFactory
        Product testProduct = new Product();
        testProduct.setId(UUID.randomUUID());
        testProduct.setProductCode("TAB" + TestDataFactory.generateLifecycleCode());
        testProduct.setProductName("Tabungan Wadiah Basic");

        // Create test account using TestDataFactory
        testAccount = new Account();
        testAccount.setId(UUID.randomUUID());
        testAccount.setAccountNumber(TestDataFactory.generateAccountNumber());
        testAccount.setAccountName("John Doe Savings");
        testAccount.setBalance(new BigDecimal("1500.00"));
        testAccount.setCustomer(testCustomer);
        testAccount.setProduct(testProduct);

        // Create test transaction using TestDataFactory
        testTransaction = new Transaction();
        testTransaction.setId(UUID.randomUUID());
        testTransaction.setTransactionNumber(TestDataFactory.generateTransactionNumber());
        testTransaction.setTransactionType(Transaction.TransactionType.DEPOSIT);
        testTransaction.setAmount(new BigDecimal("500.00"));
        testTransaction.setBalanceAfter(new BigDecimal("1500.00"));
        testTransaction.setDescription("Cash Deposit");
        testTransaction.setChannel(Transaction.TransactionChannel.TELLER);
        testTransaction.setTransactionDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        testTransaction.setAccount(testAccount);
        testTransaction.setReferenceNumber("REF" + TestDataFactory.generateLifecycleCode());
    }

    @Test
    @DisplayName("Should generate PDF receipt for deposit transaction")
    void shouldGeneratePdfReceiptForDepositTransaction() {
        // Given
        BigDecimal balanceAfter = new BigDecimal("1500.00");

        // When
        byte[] pdfContent = receiptPdfService.generateTransactionReceiptPdf(testTransaction, testAccount, balanceAfter);

        // Then
        assertNotNull(pdfContent, "PDF content should not be null");
        assertTrue(pdfContent.length > 0, "PDF content should not be empty");
        
        // Verify PDF header (starts with %PDF)
        String pdfHeader = new String(pdfContent, 0, 4);
        assertEquals("%PDF", pdfHeader, "Generated content should be a valid PDF");
    }

    @Test
    @DisplayName("Should generate PDF receipt for withdrawal transaction")
    void shouldGeneratePdfReceiptForWithdrawalTransaction() {
        // Given
        testTransaction.setTransactionType(Transaction.TransactionType.WITHDRAWAL);
        testTransaction.setAmount(new BigDecimal("200.00"));
        testTransaction.setDescription("Cash Withdrawal");
        BigDecimal balanceAfter = new BigDecimal("800.00");

        // When
        byte[] pdfContent = receiptPdfService.generateTransactionReceiptPdf(testTransaction, testAccount, balanceAfter);

        // Then
        assertNotNull(pdfContent, "PDF content should not be null");
        assertTrue(pdfContent.length > 0, "PDF content should not be empty");
        
        // Verify PDF header
        String pdfHeader = new String(pdfContent, 0, 4);
        assertEquals("%PDF", pdfHeader, "Generated content should be a valid PDF");
    }

    @Test
    @DisplayName("Should generate PDF receipt for transfer transaction")
    void shouldGeneratePdfReceiptForTransferTransaction() {
        // Given
        testTransaction.setTransactionType(Transaction.TransactionType.TRANSFER_OUT);
        testTransaction.setAmount(new BigDecimal("300.00"));
        testTransaction.setDescription("Transfer to A2000002");
        testTransaction.setChannel(Transaction.TransactionChannel.ONLINE);
        BigDecimal balanceAfter = new BigDecimal("1200.00");

        // When
        byte[] pdfContent = receiptPdfService.generateTransactionReceiptPdf(testTransaction, testAccount, balanceAfter);

        // Then
        assertNotNull(pdfContent, "PDF content should not be null");
        assertTrue(pdfContent.length > 0, "PDF content should not be empty");
        
        // Verify PDF header
        String pdfHeader = new String(pdfContent, 0, 4);
        assertEquals("%PDF", pdfHeader, "Generated content should be a valid PDF");
    }

    @Test
    @DisplayName("Should handle null balance after transaction")
    void shouldHandleNullBalanceAfterTransaction() {
        // Given
        BigDecimal balanceAfter = null;

        // When
        byte[] pdfContent = receiptPdfService.generateTransactionReceiptPdf(testTransaction, testAccount, balanceAfter);

        // Then
        assertNotNull(pdfContent, "PDF content should not be null even with null balance");
        assertTrue(pdfContent.length > 0, "PDF content should not be empty");
        
        // Verify PDF header
        String pdfHeader = new String(pdfContent, 0, 4);
        assertEquals("%PDF", pdfHeader, "Generated content should be a valid PDF");
    }

    @Test
    @DisplayName("Should handle different transaction channels")
    void shouldHandleDifferentTransactionChannels() {
        // Given
        testTransaction.setChannel(Transaction.TransactionChannel.ATM);
        BigDecimal balanceAfter = new BigDecimal("1500.00");

        // When
        byte[] pdfContent = receiptPdfService.generateTransactionReceiptPdf(testTransaction, testAccount, balanceAfter);

        // Then
        assertNotNull(pdfContent, "PDF content should not be null");
        assertTrue(pdfContent.length > 0, "PDF content should not be empty");
        
        // Verify PDF header
        String pdfHeader = new String(pdfContent, 0, 4);
        assertEquals("%PDF", pdfHeader, "Generated content should be a valid PDF");
    }

    @Test
    @DisplayName("Should throw exception for invalid transaction data")
    void shouldThrowExceptionForInvalidTransactionData() {
        // Given
        Transaction invalidTransaction = null;
        BigDecimal balanceAfter = new BigDecimal("1500.00");

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            receiptPdfService.generateTransactionReceiptPdf(invalidTransaction, testAccount, balanceAfter);
        }, "Should throw exception for null transaction");
    }
}