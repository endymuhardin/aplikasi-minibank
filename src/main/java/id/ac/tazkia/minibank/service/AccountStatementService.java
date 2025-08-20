package id.ac.tazkia.minibank.service;

import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Transaction;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class AccountStatementService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountStatementService(AccountRepository accountRepository, 
                                 TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public Optional<Account> findAccountById(UUID accountId) {
        return accountRepository.findById(accountId);
    }

    public Optional<Account> findAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    public List<Transaction> getTransactionsByAccountAndDateRange(UUID accountId, 
                                                                LocalDate startDate, 
                                                                LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        return transactionRepository.findByAccountIdAndTransactionDateBetween(
            accountId, startDateTime, endDateTime, 
            Sort.by(Sort.Direction.ASC, "transactionDate")
        );
    }

    public List<Transaction> getTransactionsByAccountNumber(String accountNumber, 
                                                          LocalDate startDate, 
                                                          LocalDate endDate) {
        Optional<Account> accountOpt = findAccountByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        
        return getTransactionsByAccountAndDateRange(accountOpt.get().getId(), startDate, endDate);
    }

    public List<Transaction> getRecentTransactions(UUID accountId, int limit) {
        return transactionRepository.findByAccountIdOrderByTransactionDateDesc(accountId)
                .stream()
                .limit(limit)
                .toList();
    }
}