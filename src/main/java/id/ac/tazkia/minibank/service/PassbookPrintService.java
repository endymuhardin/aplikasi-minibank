package id.ac.tazkia.minibank.service;

import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Passbook;
import id.ac.tazkia.minibank.entity.PassbookPrintHistory;
import id.ac.tazkia.minibank.entity.Transaction;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.PassbookPrintHistoryRepository;
import id.ac.tazkia.minibank.repository.PassbookRepository;
import id.ac.tazkia.minibank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PassbookPrintService {

    private final PassbookRepository passbookRepository;
    private final PassbookPrintHistoryRepository printHistoryRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final SequenceNumberService sequenceNumberService;

    /**
     * Create or get existing passbook for an account
     */
    public Passbook getOrCreatePassbook(UUID accountId) {
        Optional<Passbook> existingPassbook = passbookRepository.findActiveByAccountId(accountId);
        if (existingPassbook.isPresent()) {
            return existingPassbook.get();
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        Passbook passbook = new Passbook();
        passbook.setAccount(account);
        passbook.setPassbookNumber(sequenceNumberService.generateNextSequence("PASSBOOK", "PB"));
        passbook.setStatus(Passbook.PassbookStatus.ACTIVE);

        return passbookRepository.save(passbook);
    }

    /**
     * Get passbook by account ID
     */
    public Optional<Passbook> getPassbookByAccountId(UUID accountId) {
        return passbookRepository.findActiveByAccountId(accountId);
    }

    /**
     * Get passbook with full details
     */
    public Optional<Passbook> getPassbookWithDetails(UUID accountId) {
        return passbookRepository.findActiveByAccountIdWithDetails(accountId);
    }

    /**
     * Get unprinted transactions for a passbook
     */
    @Transactional(readOnly = true)
    public List<Transaction> getUnprintedTransactions(UUID accountId) {
        Optional<Passbook> passbookOpt = passbookRepository.findActiveByAccountId(accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        List<Transaction> allTransactions = transactionRepository.findByAccountOrderByTransactionDateAsc(account);

        if (passbookOpt.isEmpty() || passbookOpt.get().getLastPrintedTransaction() == null) {
            // No passbook or no previous print - return all transactions
            return allTransactions;
        }

        Passbook passbook = passbookOpt.get();
        Transaction lastPrinted = passbook.getLastPrintedTransaction();

        // Find transactions after the last printed one
        boolean foundLast = false;
        List<Transaction> unprinted = new java.util.ArrayList<>();
        for (Transaction tx : allTransactions) {
            if (foundLast) {
                unprinted.add(tx);
            } else if (tx.getId().equals(lastPrinted.getId())) {
                foundLast = true;
            }
        }

        return unprinted;
    }

    /**
     * Get unprinted transactions limited by remaining lines on current page
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsForCurrentPage(UUID accountId) {
        Passbook passbook = getOrCreatePassbook(accountId);
        List<Transaction> unprinted = getUnprintedTransactions(accountId);

        int remainingLines = passbook.getRemainingLines();
        if (remainingLines <= 0) {
            // Need to advance to next page first
            return Collections.emptyList();
        }

        // Return only transactions that fit on current page
        return unprinted.stream()
                .limit(remainingLines)
                .toList();
    }

    /**
     * Record successful print and update passbook state
     */
    public PassbookPrintHistory recordPrintSuccess(UUID accountId, List<Transaction> printedTransactions,
                                                   String printerName, String printerPort) {
        if (printedTransactions == null || printedTransactions.isEmpty()) {
            throw new IllegalArgumentException("No transactions to record");
        }

        Passbook passbook = getOrCreatePassbook(accountId);

        int startLine = passbook.getLastPrintedLine() + 1;
        int transactionCount = printedTransactions.size();
        int endLine = startLine + transactionCount - 1;

        // Create print history record
        PassbookPrintHistory history = new PassbookPrintHistory();
        history.setPassbook(passbook);
        history.setPrintDate(LocalDateTime.now());
        history.setPageNumber(passbook.getCurrentPage());
        history.setStartLine(startLine);
        history.setEndLine(endLine);
        history.setTransactionsPrinted(transactionCount);
        history.setFirstTransaction(printedTransactions.get(0));
        history.setLastTransaction(printedTransactions.get(transactionCount - 1));
        history.setPrinterName(printerName);
        history.setPrinterPort(printerPort);
        history.setStatus(PassbookPrintHistory.PrintStatus.SUCCESS);

        // Update passbook state
        passbook.updateAfterPrint(transactionCount, printedTransactions.get(transactionCount - 1));
        passbookRepository.save(passbook);

        return printHistoryRepository.save(history);
    }

    /**
     * Record failed print attempt
     */
    public PassbookPrintHistory recordPrintFailure(UUID accountId, String errorMessage,
                                                   String printerName, String printerPort) {
        Passbook passbook = getOrCreatePassbook(accountId);

        PassbookPrintHistory history = new PassbookPrintHistory();
        history.setPassbook(passbook);
        history.setPrintDate(LocalDateTime.now());
        history.setPageNumber(passbook.getCurrentPage());
        history.setStartLine(passbook.getLastPrintedLine() + 1);
        history.setEndLine(passbook.getLastPrintedLine());
        history.setTransactionsPrinted(0);
        history.setPrinterName(printerName);
        history.setPrinterPort(printerPort);
        history.setStatus(PassbookPrintHistory.PrintStatus.FAILED);
        history.setErrorMessage(errorMessage);

        return printHistoryRepository.save(history);
    }

    /**
     * Advance passbook to next page (for manual page turn)
     */
    public Passbook advanceToNextPage(UUID accountId) {
        Passbook passbook = getOrCreatePassbook(accountId);
        passbook.advanceToNextPage();
        return passbookRepository.save(passbook);
    }

    /**
     * Replace passbook with new one (for lost/full passbooks)
     */
    public Passbook replacePassbook(UUID accountId, Passbook.PassbookStatus oldStatus) {
        Optional<Passbook> oldPassbookOpt = passbookRepository.findActiveByAccountId(accountId);

        if (oldPassbookOpt.isPresent()) {
            Passbook oldPassbook = oldPassbookOpt.get();
            oldPassbook.setStatus(oldStatus);
            passbookRepository.save(oldPassbook);
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        // Create new passbook
        Passbook newPassbook = new Passbook();
        newPassbook.setAccount(account);
        newPassbook.setPassbookNumber(sequenceNumberService.generateNextSequence("PASSBOOK", "PB"));
        newPassbook.setStatus(Passbook.PassbookStatus.ACTIVE);

        return passbookRepository.save(newPassbook);
    }

    /**
     * Get print history for a passbook
     */
    @Transactional(readOnly = true)
    public List<PassbookPrintHistory> getPrintHistory(UUID accountId) {
        Optional<Passbook> passbookOpt = passbookRepository.findActiveByAccountId(accountId);
        if (passbookOpt.isEmpty()) {
            return Collections.emptyList();
        }
        return printHistoryRepository.findByPassbookId(passbookOpt.get().getId());
    }

    /**
     * Initialize passbook state for non-empty passbook
     */
    public Passbook initializePassbook(UUID accountId, Integer currentPage, Integer lastPrintedLine,
                                      Transaction lastPrintedTransaction) {
        Passbook passbook = getOrCreatePassbook(accountId);

        passbook.setCurrentPage(currentPage);
        passbook.setLastPrintedLine(lastPrintedLine);
        passbook.setLastPrintedTransaction(lastPrintedTransaction);

        if (lastPrintedTransaction != null) {
            passbook.setLastPrintDate(LocalDateTime.now());
        }

        return passbookRepository.save(passbook);
    }
}
