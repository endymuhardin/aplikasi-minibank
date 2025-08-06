package id.ac.tazkia.minibank.controller.rest;

import id.ac.tazkia.minibank.dto.DepositRequest;
import id.ac.tazkia.minibank.dto.DepositResponse;
import id.ac.tazkia.minibank.dto.WithdrawalRequest;
import id.ac.tazkia.minibank.dto.WithdrawalResponse;
import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Transaction;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.TransactionRepository;
import id.ac.tazkia.minibank.service.SequenceNumberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
public class TransactionRestController {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private SequenceNumberService sequenceNumberService;
    
    @PostMapping("/deposit")
    @Transactional
    public ResponseEntity<?> deposit(@Valid @RequestBody DepositRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        
        try {
            // Validate account exists and is active
            Optional<Account> accountOpt = accountRepository.findById(request.getAccountId());
            if (!accountOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("accountId", "Account not found");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }
            
            Account account = accountOpt.get();
            if (!account.isActive()) {
                Map<String, String> error = new HashMap<>();
                error.put("accountId", "Account is not active");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }
            
            // Record balance before transaction
            BigDecimal balanceBefore = account.getBalance();
            
            // Generate transaction number
            String transactionNumber = sequenceNumberService.generateNextSequence("TRANSACTION_NUMBER", "TXN");
            
            // Create transaction record
            Transaction transaction = new Transaction();
            transaction.setAccount(account);
            transaction.setTransactionNumber(transactionNumber);
            transaction.setTransactionType(Transaction.TransactionType.DEPOSIT);
            transaction.setAmount(request.getAmount());
            transaction.setBalanceBefore(balanceBefore);
            transaction.setDescription(request.getDescription());
            transaction.setReferenceNumber(request.getReferenceNumber());
            transaction.setChannel(Transaction.TransactionChannel.TELLER);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setProcessedDate(LocalDateTime.now());
            transaction.setCreatedBy(request.getCreatedBy());
            
            // Process deposit using business method
            account.deposit(request.getAmount());
            transaction.setBalanceAfter(account.getBalance());
            
            // Save transaction and account
            Transaction savedTransaction = transactionRepository.save(transaction);
            Account savedAccount = accountRepository.save(account);
            
            // Build response
            DepositResponse response = new DepositResponse();
            response.setTransactionId(savedTransaction.getId());
            response.setTransactionNumber(savedTransaction.getTransactionNumber());
            response.setAccountId(savedAccount.getId());
            response.setAccountNumber(savedAccount.getAccountNumber());
            response.setAmount(savedTransaction.getAmount());
            response.setBalanceBefore(savedTransaction.getBalanceBefore());
            response.setBalanceAfter(savedTransaction.getBalanceAfter());
            response.setDescription(savedTransaction.getDescription());
            response.setReferenceNumber(savedTransaction.getReferenceNumber());
            response.setCurrency(savedTransaction.getCurrency());
            response.setChannel(savedTransaction.getChannel().name());
            response.setTransactionDate(savedTransaction.getTransactionDate());
            response.setProcessedDate(savedTransaction.getProcessedDate());
            
            // Account info
            DepositResponse.AccountInfo accountInfo = new DepositResponse.AccountInfo();
            accountInfo.setId(savedAccount.getId());
            accountInfo.setAccountNumber(savedAccount.getAccountNumber());
            accountInfo.setAccountName(savedAccount.getAccountName());
            accountInfo.setCurrentBalance(savedAccount.getBalance());
            response.setAccount(accountInfo);
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("amount", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to process deposit: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/withdrawal")
    @Transactional
    public ResponseEntity<?> withdrawal(@Valid @RequestBody WithdrawalRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        
        try {
            // Validate account exists and is active
            Optional<Account> accountOpt = accountRepository.findById(request.getAccountId());
            if (!accountOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("accountId", "Account not found");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }
            
            Account account = accountOpt.get();
            if (!account.isActive()) {
                Map<String, String> error = new HashMap<>();
                error.put("accountId", "Account is not active");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }
            
            // Record balance before transaction
            BigDecimal balanceBefore = account.getBalance();
            
            // Generate transaction number
            String transactionNumber = sequenceNumberService.generateNextSequence("TRANSACTION_NUMBER", "TXN");
            
            // Create transaction record
            Transaction transaction = new Transaction();
            transaction.setAccount(account);
            transaction.setTransactionNumber(transactionNumber);
            transaction.setTransactionType(Transaction.TransactionType.WITHDRAWAL);
            transaction.setAmount(request.getAmount());
            transaction.setBalanceBefore(balanceBefore);
            transaction.setDescription(request.getDescription());
            transaction.setReferenceNumber(request.getReferenceNumber());
            transaction.setChannel(Transaction.TransactionChannel.TELLER);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setProcessedDate(LocalDateTime.now());
            transaction.setCreatedBy(request.getCreatedBy());
            
            // Process withdrawal using business method
            account.withdraw(request.getAmount());
            transaction.setBalanceAfter(account.getBalance());
            
            // Save transaction and account
            Transaction savedTransaction = transactionRepository.save(transaction);
            Account savedAccount = accountRepository.save(account);
            
            // Build response
            WithdrawalResponse response = new WithdrawalResponse();
            response.setTransactionId(savedTransaction.getId());
            response.setTransactionNumber(savedTransaction.getTransactionNumber());
            response.setAccountId(savedAccount.getId());
            response.setAccountNumber(savedAccount.getAccountNumber());
            response.setAmount(savedTransaction.getAmount());
            response.setBalanceBefore(savedTransaction.getBalanceBefore());
            response.setBalanceAfter(savedTransaction.getBalanceAfter());
            response.setDescription(savedTransaction.getDescription());
            response.setReferenceNumber(savedTransaction.getReferenceNumber());
            response.setCurrency(savedTransaction.getCurrency());
            response.setChannel(savedTransaction.getChannel().name());
            response.setTransactionDate(savedTransaction.getTransactionDate());
            response.setProcessedDate(savedTransaction.getProcessedDate());
            
            // Account info
            WithdrawalResponse.AccountInfo accountInfo = new WithdrawalResponse.AccountInfo();
            accountInfo.setId(savedAccount.getId());
            accountInfo.setAccountNumber(savedAccount.getAccountNumber());
            accountInfo.setAccountName(savedAccount.getAccountName());
            accountInfo.setCurrentBalance(savedAccount.getBalance());
            response.setAccount(accountInfo);
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("amount", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to process withdrawal: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}