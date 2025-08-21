package id.ac.tazkia.minibank.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.transaction.annotation.Transactional;

import id.ac.tazkia.minibank.dto.TransferRequest;
import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.entity.Transaction;
import id.ac.tazkia.minibank.integration.BaseIntegrationTest;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.BranchRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.repository.TransactionRepository;
import id.ac.tazkia.minibank.service.TransferService;
import id.ac.tazkia.minibank.util.SimpleParallelTestDataFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Integration test for TransferService with real database operations
 * 
 * Tests the complete transfer workflow including:
 * - Account validation
 * - Transfer processing 
 * - Transaction creation
 * - Balance updates
 * - Error handling scenarios
 */
@Slf4j
@SqlGroup({
    @Sql(scripts = "/sql/setup-account-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(scripts = "/sql/cleanup-account-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class TransferServiceIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private TransferService transferService;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private BranchRepository branchRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    private Account sourceAccount;
    private Account destinationAccount;
    private Branch testBranch;
    private PersonalCustomer sourceCustomer;
    private PersonalCustomer destinationCustomer;
    private Product testProduct;
    
    @BeforeEach
    @Transactional
    void setUp() {
        // Create test branch
        testBranch = SimpleParallelTestDataFactory.createUniqueBranch();
        testBranch = branchRepository.save(testBranch);
        
        // Create test product
        testProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        testProduct = productRepository.save(testProduct);
        
        // Create source customer and account
        sourceCustomer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(testBranch);
        sourceCustomer = customerRepository.save(sourceCustomer);
        
        sourceAccount = SimpleParallelTestDataFactory.createUniqueAccount(sourceCustomer, testProduct, testBranch);
        sourceAccount.setBalance(new BigDecimal("100000.00")); // IDR 100,000
        sourceAccount = accountRepository.save(sourceAccount);
        
        // Create destination customer and account  
        destinationCustomer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(testBranch);
        destinationCustomer = customerRepository.save(destinationCustomer);
        
        destinationAccount = SimpleParallelTestDataFactory.createUniqueAccount(destinationCustomer, testProduct, testBranch);
        destinationAccount.setBalance(new BigDecimal("50000.00")); // IDR 50,000
        destinationAccount = accountRepository.save(destinationAccount);
        
        log.info("Test setup completed - Source: {}, Destination: {}", 
            sourceAccount.getAccountNumber(), destinationAccount.getAccountNumber());
    }
    
    @Test
    @Transactional
    void shouldValidateSuccessfulTransfer() {
        log.info("🧪 TEST START: shouldValidateSuccessfulTransfer");
        
        // Given - Valid transfer request
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(sourceAccount.getId());
        transferRequest.setToAccountNumber(destinationAccount.getAccountNumber());
        transferRequest.setAmount(new BigDecimal("25000.00"));
        transferRequest.setDescription("Test transfer validation");
        transferRequest.setCreatedBy("TEST_USER");
        
        // When - Validate transfer
        TransferRequest validatedRequest = transferService.validateTransfer(transferRequest);
        
        // Then - Validation successful
        assertThat(validatedRequest).isNotNull();
        assertThat(validatedRequest.getToAccountId()).isEqualTo(destinationAccount.getId());
        assertThat(validatedRequest.getDestinationAccountName()).isEqualTo(destinationAccount.getAccountName());
        assertThat(validatedRequest.getDestinationCustomerName()).isEqualTo(destinationCustomer.getDisplayName());
        
        log.info("✅ TEST PASS: shouldValidateSuccessfulTransfer");
    }
    
    @Test
    @Transactional
    void shouldThrowExceptionForInvalidSourceAccount() {
        log.info("🧪 TEST START: shouldThrowExceptionForInvalidSourceAccount");
        
        // Given - Transfer request with invalid source account
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(java.util.UUID.randomUUID()); // Non-existent account
        transferRequest.setToAccountNumber(destinationAccount.getAccountNumber());
        transferRequest.setAmount(new BigDecimal("25000.00"));
        
        // When & Then - Should throw exception
        assertThatThrownBy(() -> transferService.validateTransfer(transferRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Source account not found");
        
        log.info("✅ TEST PASS: shouldThrowExceptionForInvalidSourceAccount");
    }
    
    @Test
    @Transactional
    void shouldThrowExceptionForInvalidDestinationAccount() {
        log.info("🧪 TEST START: shouldThrowExceptionForInvalidDestinationAccount");
        
        // Given - Transfer request with invalid destination account
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(sourceAccount.getId());
        transferRequest.setToAccountNumber("INVALID_ACCOUNT_NUMBER");
        transferRequest.setAmount(new BigDecimal("25000.00"));
        
        // When & Then - Should throw exception
        assertThatThrownBy(() -> transferService.validateTransfer(transferRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Destination account not found: INVALID_ACCOUNT_NUMBER");
        
        log.info("✅ TEST PASS: shouldThrowExceptionForInvalidDestinationAccount");
    }
    
    @Test
    @Transactional
    void shouldThrowExceptionForSelfTransfer() {
        log.info("🧪 TEST START: shouldThrowExceptionForSelfTransfer");
        
        // Given - Transfer request to same account
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(sourceAccount.getId());
        transferRequest.setToAccountNumber(sourceAccount.getAccountNumber());
        transferRequest.setAmount(new BigDecimal("25000.00"));
        
        // When & Then - Should throw exception
        assertThatThrownBy(() -> transferService.validateTransfer(transferRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cannot transfer to the same account");
        
        log.info("✅ TEST PASS: shouldThrowExceptionForSelfTransfer");
    }
    
    @Test
    @Transactional
    void shouldThrowExceptionForInsufficientBalance() {
        log.info("🧪 TEST START: shouldThrowExceptionForInsufficientBalance");
        
        // Given - Transfer request exceeding source balance
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(sourceAccount.getId());
        transferRequest.setToAccountNumber(destinationAccount.getAccountNumber());
        transferRequest.setAmount(new BigDecimal("200000.00")); // Exceeds source balance
        
        // When & Then - Should throw exception
        assertThatThrownBy(() -> transferService.validateTransfer(transferRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Insufficient balance. Available: 100000.00");
        
        log.info("✅ TEST PASS: shouldThrowExceptionForInsufficientBalance");
    }
    
    @Test
    @Transactional
    void shouldProcessTransferSuccessfully() {
        log.info("🧪 TEST START: shouldProcessTransferSuccessfully");
        
        // Given - Valid transfer request
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(sourceAccount.getId());
        transferRequest.setToAccountId(destinationAccount.getId());
        transferRequest.setToAccountNumber(destinationAccount.getAccountNumber());
        transferRequest.setAmount(new BigDecimal("30000.00"));
        transferRequest.setDescription("Integration test transfer");
        transferRequest.setReferenceNumber("TEST_REF_001");
        transferRequest.setCreatedBy("INTEGRATION_TEST");
        transferRequest.setDestinationAccountName(destinationAccount.getAccountName());
        transferRequest.setDestinationCustomerName(destinationCustomer.getDisplayName());
        
        // Record initial balances
        BigDecimal initialSourceBalance = sourceAccount.getBalance();
        BigDecimal initialDestinationBalance = destinationAccount.getBalance();
        
        // When - Process transfer
        transferService.processTransfer(transferRequest);
        
        // Then - Verify account balances updated
        Account updatedSourceAccount = accountRepository.findById(sourceAccount.getId()).get();
        Account updatedDestinationAccount = accountRepository.findById(destinationAccount.getId()).get();
        
        assertThat(updatedSourceAccount.getBalance())
            .isEqualTo(initialSourceBalance.subtract(transferRequest.getAmount()));
        assertThat(updatedDestinationAccount.getBalance())
            .isEqualTo(initialDestinationBalance.add(transferRequest.getAmount()));
        
        // Verify transactions created
        List<Transaction> sourceTransactions = transactionRepository.findByAccount(updatedSourceAccount);
        List<Transaction> destinationTransactions = transactionRepository.findByAccount(updatedDestinationAccount);
        
        // Find the transfer transactions
        Optional<Transaction> transferOutTxn = sourceTransactions.stream()
            .filter(txn -> Transaction.TransactionType.TRANSFER_OUT.equals(txn.getTransactionType()))
            .findFirst();
        Optional<Transaction> transferInTxn = destinationTransactions.stream()
            .filter(txn -> Transaction.TransactionType.TRANSFER_IN.equals(txn.getTransactionType()))
            .findFirst();
        
        assertThat(transferOutTxn).isPresent();
        assertThat(transferInTxn).isPresent();
        
        // Verify transfer out transaction
        Transaction outTxn = transferOutTxn.get();
        assertThat(outTxn.getAmount()).isEqualTo(transferRequest.getAmount());
        assertThat(outTxn.getBalanceBefore()).isEqualTo(initialSourceBalance);
        assertThat(outTxn.getBalanceAfter()).isEqualTo(updatedSourceAccount.getBalance());
        assertThat(outTxn.getDestinationAccount()).isEqualTo(updatedDestinationAccount);
        assertThat(outTxn.getReferenceNumber()).isEqualTo("TEST_REF_001");
        assertThat(outTxn.getChannel()).isEqualTo(Transaction.TransactionChannel.TRANSFER);
        assertThat(outTxn.getCreatedBy()).isEqualTo("INTEGRATION_TEST");
        
        // Verify transfer in transaction
        Transaction inTxn = transferInTxn.get();
        assertThat(inTxn.getAmount()).isEqualTo(transferRequest.getAmount());
        assertThat(inTxn.getBalanceBefore()).isEqualTo(initialDestinationBalance);
        assertThat(inTxn.getBalanceAfter()).isEqualTo(updatedDestinationAccount.getBalance());
        assertThat(inTxn.getDestinationAccount()).isEqualTo(updatedSourceAccount);
        assertThat(inTxn.getReferenceNumber()).isEqualTo("TEST_REF_001");
        assertThat(inTxn.getChannel()).isEqualTo(Transaction.TransactionChannel.TRANSFER);
        assertThat(inTxn.getCreatedBy()).isEqualTo("INTEGRATION_TEST");
        
        log.info("✅ TEST PASS: shouldProcessTransferSuccessfully - Source: {} -> {}, Destination: {} -> {}", 
            initialSourceBalance, updatedSourceAccount.getBalance(),
            initialDestinationBalance, updatedDestinationAccount.getBalance());
    }
    
    @Test
    @Transactional
    void shouldThrowExceptionWhenProcessingInvalidTransfer() {
        log.info("🧪 TEST START: shouldThrowExceptionWhenProcessingInvalidTransfer");
        
        // Given - Invalid transfer request (destination account changed to invalid)
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(sourceAccount.getId());
        transferRequest.setToAccountId(java.util.UUID.randomUUID()); // Invalid destination
        transferRequest.setToAccountNumber("INVALID_ACCOUNT");
        transferRequest.setAmount(new BigDecimal("25000.00"));
        
        // When & Then - Should throw exception
        assertThatThrownBy(() -> transferService.processTransfer(transferRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Destination account not found: INVALID_ACCOUNT");
        
        log.info("✅ TEST PASS: shouldThrowExceptionWhenProcessingInvalidTransfer");
    }
    
    @Test
    @Transactional 
    void shouldHandleInactiveSourceAccount() {
        log.info("🧪 TEST START: shouldHandleInactiveSourceAccount");
        
        // Given - Inactive source account
        sourceAccount.setStatus(Account.AccountStatus.INACTIVE);
        accountRepository.save(sourceAccount);
        
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(sourceAccount.getId());
        transferRequest.setToAccountNumber(destinationAccount.getAccountNumber());
        transferRequest.setAmount(new BigDecimal("25000.00"));
        
        // When & Then - Should throw exception
        assertThatThrownBy(() -> transferService.validateTransfer(transferRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Source account is not active");
        
        log.info("✅ TEST PASS: shouldHandleInactiveSourceAccount");
    }
    
    @Test
    @Transactional
    void shouldHandleInactiveDestinationAccount() {
        log.info("🧪 TEST START: shouldHandleInactiveDestinationAccount");
        
        // Given - Inactive destination account
        destinationAccount.setStatus(Account.AccountStatus.INACTIVE);
        accountRepository.save(destinationAccount);
        
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(sourceAccount.getId());
        transferRequest.setToAccountNumber(destinationAccount.getAccountNumber());
        transferRequest.setAmount(new BigDecimal("25000.00"));
        
        // When & Then - Should throw exception
        assertThatThrownBy(() -> transferService.validateTransfer(transferRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Destination account is not active");
        
        log.info("✅ TEST PASS: shouldHandleInactiveDestinationAccount");
    }
    
    @Test
    @Transactional
    void shouldProcessMinimumAmountTransfer() {
        log.info("🧪 TEST START: shouldProcessMinimumAmountTransfer");
        
        // Given - Minimum transfer amount (0.01)
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(sourceAccount.getId());
        transferRequest.setToAccountId(destinationAccount.getId());
        transferRequest.setToAccountNumber(destinationAccount.getAccountNumber());
        transferRequest.setAmount(new BigDecimal("0.01"));
        transferRequest.setDescription("Minimum amount transfer test");
        transferRequest.setReferenceNumber("MIN_TRANSFER_001");
        transferRequest.setCreatedBy("TEST");
        
        BigDecimal initialSourceBalance = sourceAccount.getBalance();
        BigDecimal initialDestinationBalance = destinationAccount.getBalance();
        
        // When - Process transfer
        transferService.processTransfer(transferRequest);
        
        // Then - Verify balances updated correctly
        Account updatedSourceAccount = accountRepository.findById(sourceAccount.getId()).get();
        Account updatedDestinationAccount = accountRepository.findById(destinationAccount.getId()).get();
        
        assertThat(updatedSourceAccount.getBalance())
            .isEqualTo(initialSourceBalance.subtract(new BigDecimal("0.01")));
        assertThat(updatedDestinationAccount.getBalance())
            .isEqualTo(initialDestinationBalance.add(new BigDecimal("0.01")));
        
        log.info("✅ TEST PASS: shouldProcessMinimumAmountTransfer");
    }
    
    @Test
    @Transactional
    void shouldProcessMaximumBalanceTransfer() {
        log.info("🧪 TEST START: shouldProcessMaximumBalanceTransfer");
        
        // Given - Transfer entire source balance
        BigDecimal transferAmount = sourceAccount.getBalance();
        
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(sourceAccount.getId());
        transferRequest.setToAccountId(destinationAccount.getId());
        transferRequest.setToAccountNumber(destinationAccount.getAccountNumber());
        transferRequest.setAmount(transferAmount);
        transferRequest.setDescription("Maximum balance transfer test");
        transferRequest.setReferenceNumber("MAX_TRANSFER_001");
        transferRequest.setCreatedBy("TEST");
        
        BigDecimal initialDestinationBalance = destinationAccount.getBalance();
        
        // When - Process transfer
        transferService.processTransfer(transferRequest);
        
        // Then - Verify source account has zero balance
        Account updatedSourceAccount = accountRepository.findById(sourceAccount.getId()).get();
        Account updatedDestinationAccount = accountRepository.findById(destinationAccount.getId()).get();
        
        assertThat(updatedSourceAccount.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(updatedDestinationAccount.getBalance())
            .isEqualTo(initialDestinationBalance.add(transferAmount));
        
        log.info("✅ TEST PASS: shouldProcessMaximumBalanceTransfer - Transferred entire balance: {}", transferAmount);
    }
}