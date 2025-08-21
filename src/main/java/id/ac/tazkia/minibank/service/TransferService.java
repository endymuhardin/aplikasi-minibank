package id.ac.tazkia.minibank.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.tazkia.minibank.dto.TransferRequest;
import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Transaction;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final SequenceNumberService sequenceNumberService;
    
    /**
     * Validates transfer request and populates destination account information
     */
    public TransferRequest validateTransfer(TransferRequest transferRequest) {
        // Validate source account
        Optional<Account> fromAccountOpt = accountRepository.findById(transferRequest.getFromAccountId());
        if (fromAccountOpt.isEmpty()) {
            throw new IllegalArgumentException("Source account not found");
        }
        
        Account fromAccount = fromAccountOpt.get();
        if (!fromAccount.isActive()) {
            throw new IllegalArgumentException("Source account is not active");
        }
        
        // Validate destination account
        Optional<Account> toAccountOpt = accountRepository.findByAccountNumber(transferRequest.getToAccountNumber());
        if (toAccountOpt.isEmpty()) {
            throw new IllegalArgumentException("Destination account not found: " + transferRequest.getToAccountNumber());
        }
        
        Account toAccount = toAccountOpt.get();
        if (!toAccount.isActive()) {
            throw new IllegalArgumentException("Destination account is not active");
        }
        
        // Check for self-transfer
        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        
        // Validate amount
        if (transferRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }
        
        if (fromAccount.getBalance().compareTo(transferRequest.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance. Available: " + fromAccount.getBalance());
        }
        
        // Populate destination account information for confirmation
        transferRequest.setToAccountId(toAccount.getId());
        transferRequest.setDestinationAccountName(toAccount.getAccountName());
        transferRequest.setDestinationCustomerName(toAccount.getCustomer().getDisplayName());
        
        return transferRequest;
    }
    
    /**
     * Processes the transfer between accounts
     */
    @Transactional
    public void processTransfer(TransferRequest transferRequest) {
        // Re-validate accounts (in case status changed between validation and processing)
        validateTransfer(transferRequest);
        
        // Get accounts
        Account fromAccount = accountRepository.findById(transferRequest.getFromAccountId())
            .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        Account toAccount = accountRepository.findById(transferRequest.getToAccountId())
            .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));
        
        log.info("Processing transfer: {} from {} to {} amount: {}", 
            transferRequest.getReferenceNumber(), fromAccount.getAccountNumber(), 
            toAccount.getAccountNumber(), transferRequest.getAmount());
        
        // Record balances before transfer
        BigDecimal fromBalanceBefore = fromAccount.getBalance();
        BigDecimal toBalanceBefore = toAccount.getBalance();
        
        // Generate transaction numbers
        String transferOutTxnNumber = sequenceNumberService.generateNextSequence("TRANSACTION_NUMBER", "TXN");
        String transferInTxnNumber = sequenceNumberService.generateNextSequence("TRANSACTION_NUMBER", "TXN");
        
        // Process transfer using entity business methods
        fromAccount.transferOut(transferRequest.getAmount());
        toAccount.transferIn(transferRequest.getAmount());
        
        // Create transfer out transaction
        Transaction transferOutTransaction = new Transaction();
        transferOutTransaction.setAccount(fromAccount);
        transferOutTransaction.setDestinationAccount(toAccount);
        transferOutTransaction.setTransactionNumber(transferOutTxnNumber);
        transferOutTransaction.setTransactionType(Transaction.TransactionType.TRANSFER_OUT);
        transferOutTransaction.setAmount(transferRequest.getAmount());
        transferOutTransaction.setBalanceBefore(fromBalanceBefore);
        transferOutTransaction.setBalanceAfter(fromAccount.getBalance());
        transferOutTransaction.setDescription(String.format("Transfer to %s - %s", 
            toAccount.getAccountNumber(), transferRequest.getDescription()));
        transferOutTransaction.setReferenceNumber(transferRequest.getReferenceNumber());
        transferOutTransaction.setChannel(Transaction.TransactionChannel.TRANSFER);
        transferOutTransaction.setTransactionDate(LocalDateTime.now());
        transferOutTransaction.setProcessedDate(LocalDateTime.now());
        transferOutTransaction.setCreatedBy(transferRequest.getCreatedBy() != null ? 
            transferRequest.getCreatedBy() : "SYSTEM");
        
        // Create transfer in transaction
        Transaction transferInTransaction = new Transaction();
        transferInTransaction.setAccount(toAccount);
        transferInTransaction.setDestinationAccount(fromAccount);
        transferInTransaction.setTransactionNumber(transferInTxnNumber);
        transferInTransaction.setTransactionType(Transaction.TransactionType.TRANSFER_IN);
        transferInTransaction.setAmount(transferRequest.getAmount());
        transferInTransaction.setBalanceBefore(toBalanceBefore);
        transferInTransaction.setBalanceAfter(toAccount.getBalance());
        transferInTransaction.setDescription(String.format("Transfer from %s - %s", 
            fromAccount.getAccountNumber(), transferRequest.getDescription()));
        transferInTransaction.setReferenceNumber(transferRequest.getReferenceNumber());
        transferInTransaction.setChannel(Transaction.TransactionChannel.TRANSFER);
        transferInTransaction.setTransactionDate(LocalDateTime.now());
        transferInTransaction.setProcessedDate(LocalDateTime.now());
        transferInTransaction.setCreatedBy(transferRequest.getCreatedBy() != null ? 
            transferRequest.getCreatedBy() : "SYSTEM");
        
        // Save all entities
        transactionRepository.save(transferOutTransaction);
        transactionRepository.save(transferInTransaction);
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        
        log.info("Transfer completed successfully. TXN OUT: {}, TXN IN: {}", 
            transferOutTxnNumber, transferInTxnNumber);
    }
    
    /**
     * Gets account information for display
     */
    public Account getAccountById(UUID accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }
    
    /**
     * Gets account by account number for lookup
     */
    public Optional<Account> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }
}