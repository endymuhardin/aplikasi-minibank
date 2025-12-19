package id.ac.tazkia.minibank.controller.rest;

import id.ac.tazkia.minibank.dto.PassbookPrintDataResponse;
import id.ac.tazkia.minibank.dto.PassbookPrintResultRequest;
import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Passbook;
import id.ac.tazkia.minibank.entity.PassbookPrintHistory;
import id.ac.tazkia.minibank.entity.Transaction;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.TransactionRepository;
import id.ac.tazkia.minibank.service.PassbookPrintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/passbook")
@RequiredArgsConstructor
public class PassbookRestController {

    private final PassbookPrintService passbookPrintService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Get print data for passbook printing via Web Serial API
     */
    @GetMapping("/{accountId}/print-data")
    public ResponseEntity<Object> getPrintData(@PathVariable UUID accountId) {
        try {
            // Validate account
            Optional<Account> accountOpt = accountRepository.findById(accountId);
            if (accountOpt.isEmpty()) {
                return errorResponse("Account not found", HttpStatus.NOT_FOUND);
            }

            Account account = accountOpt.get();
            if (!account.isActive()) {
                return errorResponse("Account is not active", HttpStatus.BAD_REQUEST);
            }

            // Get or create passbook
            Passbook passbook = passbookPrintService.getOrCreatePassbook(accountId);

            // Get unprinted transactions
            List<Transaction> unprintedTransactions = passbookPrintService.getUnprintedTransactions(accountId);

            if (unprintedTransactions.isEmpty()) {
                return errorResponse("No new transactions to print", HttpStatus.OK);
            }

            // Limit to remaining lines on current page
            int remainingLines = passbook.getRemainingLines();
            List<Transaction> transactionsToPrint = unprintedTransactions.stream()
                    .limit(remainingLines)
                    .toList();

            // Build response
            PassbookPrintDataResponse response = buildPrintDataResponse(passbook, account, transactionsToPrint);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting print data for account: {}", accountId, e);
            return errorResponse("Failed to get print data: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get passbook status
     */
    @GetMapping("/{accountId}/status")
    public ResponseEntity<Object> getPassbookStatus(@PathVariable UUID accountId) {
        try {
            Optional<Account> accountOpt = accountRepository.findById(accountId);
            if (accountOpt.isEmpty()) {
                return errorResponse("Account not found", HttpStatus.NOT_FOUND);
            }

            Optional<Passbook> passbookOpt = passbookPrintService.getPassbookByAccountId(accountId);

            Map<String, Object> status = new HashMap<>();
            if (passbookOpt.isPresent()) {
                Passbook passbook = passbookOpt.get();
                status.put("hasPassbook", true);
                status.put("passbookNumber", passbook.getPassbookNumber());
                status.put("currentPage", passbook.getCurrentPage());
                status.put("lastPrintedLine", passbook.getLastPrintedLine());
                status.put("remainingLines", passbook.getRemainingLines());
                status.put("status", passbook.getStatus().name());
                status.put("lastPrintDate", passbook.getLastPrintDate());
            } else {
                status.put("hasPassbook", false);
            }

            // Count unprinted transactions
            List<Transaction> unprintedTransactions = passbookPrintService.getUnprintedTransactions(accountId);
            status.put("unprintedTransactionCount", unprintedTransactions.size());

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("Error getting passbook status for account: {}", accountId, e);
            return errorResponse("Failed to get passbook status", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Record print result from browser
     */
    @PostMapping("/print-result")
    public ResponseEntity<Object> recordPrintResult(@Valid @RequestBody PassbookPrintResultRequest request,
                                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            PassbookPrintHistory history;

            if (request.getStatus() == PassbookPrintResultRequest.PrintStatus.SUCCESS ||
                request.getStatus() == PassbookPrintResultRequest.PrintStatus.PARTIAL) {

                // Get printed transactions
                List<Transaction> printedTransactions = new ArrayList<>();
                if (request.getPrintedTransactionIds() != null) {
                    for (UUID txId : request.getPrintedTransactionIds()) {
                        transactionRepository.findById(txId).ifPresent(printedTransactions::add);
                    }
                }

                if (printedTransactions.isEmpty()) {
                    return errorResponse("No valid transaction IDs provided", HttpStatus.BAD_REQUEST);
                }

                // Sort by transaction date to ensure correct order
                printedTransactions.sort(Comparator.comparing(Transaction::getTransactionDate));

                history = passbookPrintService.recordPrintSuccess(
                        request.getAccountId(),
                        printedTransactions,
                        request.getPrinterName(),
                        request.getPrinterPort()
                );

            } else {
                history = passbookPrintService.recordPrintFailure(
                        request.getAccountId(),
                        request.getErrorMessage(),
                        request.getPrinterName(),
                        request.getPrinterPort()
                );
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("printHistoryId", history.getId());
            response.put("status", history.getStatus().name());
            response.put("transactionsPrinted", history.getTransactionsPrinted());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error recording print result", e);
            return errorResponse("Failed to record print result: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Advance to next page (manual page turn)
     */
    @PostMapping("/{accountId}/next-page")
    public ResponseEntity<Object> advanceToNextPage(@PathVariable UUID accountId) {
        try {
            Passbook passbook = passbookPrintService.advanceToNextPage(accountId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("currentPage", passbook.getCurrentPage());
            response.put("lastPrintedLine", passbook.getLastPrintedLine());
            response.put("remainingLines", passbook.getRemainingLines());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error advancing to next page for account: {}", accountId, e);
            return errorResponse("Failed to advance to next page", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get print history for account
     */
    @GetMapping("/{accountId}/history")
    public ResponseEntity<Object> getPrintHistory(@PathVariable UUID accountId) {
        try {
            List<PassbookPrintHistory> history = passbookPrintService.getPrintHistory(accountId);

            List<Map<String, Object>> historyList = history.stream().map(h -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", h.getId());
                item.put("printDate", h.getPrintDate());
                item.put("pageNumber", h.getPageNumber());
                item.put("startLine", h.getStartLine());
                item.put("endLine", h.getEndLine());
                item.put("transactionsPrinted", h.getTransactionsPrinted());
                item.put("status", h.getStatus().name());
                item.put("printerName", h.getPrinterName());
                return item;
            }).toList();

            return ResponseEntity.ok(historyList);

        } catch (Exception e) {
            log.error("Error getting print history for account: {}", accountId, e);
            return errorResponse("Failed to get print history", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper methods

    private PassbookPrintDataResponse buildPrintDataResponse(Passbook passbook, Account account,
                                                              List<Transaction> transactions) {
        PassbookPrintDataResponse response = new PassbookPrintDataResponse();

        // Passbook info
        PassbookPrintDataResponse.PassbookInfo pbInfo = new PassbookPrintDataResponse.PassbookInfo();
        pbInfo.setId(passbook.getId());
        pbInfo.setPassbookNumber(passbook.getPassbookNumber());
        pbInfo.setCurrentPage(passbook.getCurrentPage());
        pbInfo.setLastPrintedLine(passbook.getLastPrintedLine());
        pbInfo.setLinesPerPage(passbook.getLinesPerPage());
        pbInfo.setRemainingLines(passbook.getRemainingLines());
        pbInfo.setStatus(passbook.getStatus().name());
        pbInfo.setLastPrintDate(passbook.getLastPrintDate());
        response.setPassbook(pbInfo);

        // Account info
        PassbookPrintDataResponse.AccountInfo acctInfo = new PassbookPrintDataResponse.AccountInfo();
        acctInfo.setId(account.getId());
        acctInfo.setAccountNumber(account.getAccountNumber());
        acctInfo.setAccountName(account.getAccountName());
        acctInfo.setCurrentBalance(account.getBalance());
        acctInfo.setProductName(account.getProduct().getProductName());
        acctInfo.setOpenedDate(account.getOpenedDate());
        acctInfo.setStatus(account.getStatus().name());
        response.setAccount(acctInfo);

        // Customer info
        PassbookPrintDataResponse.CustomerInfo custInfo = new PassbookPrintDataResponse.CustomerInfo();
        custInfo.setId(account.getCustomer().getId());
        custInfo.setCustomerNumber(account.getCustomer().getCustomerNumber());
        custInfo.setCustomerName(account.getCustomer().getDisplayName());
        custInfo.setCustomerType(account.getCustomer().getCustomerType().name());
        response.setCustomer(custInfo);

        // Transaction lines
        List<PassbookPrintDataResponse.TransactionLine> txLines = new ArrayList<>();
        int lineNumber = passbook.getLastPrintedLine() + 1;
        for (Transaction tx : transactions) {
            PassbookPrintDataResponse.TransactionLine line = new PassbookPrintDataResponse.TransactionLine();
            line.setId(tx.getId());
            line.setTransactionNumber(tx.getTransactionNumber());
            line.setTransactionDate(tx.getTransactionDate());
            line.setDescription(truncateDescription(tx.getDescription(), 20));
            line.setTransactionType(tx.getTransactionType().name());

            if (tx.isDebitTransaction()) {
                line.setDebit(tx.getAmount());
                line.setCredit(null);
            } else {
                line.setDebit(null);
                line.setCredit(tx.getAmount());
            }

            line.setBalance(tx.getBalanceAfter());
            line.setLineNumber(lineNumber++);
            txLines.add(line);
        }
        response.setTransactions(txLines);

        // Print config (using defaults)
        response.setConfig(new PassbookPrintDataResponse.PrintConfig());

        return response;
    }

    private String truncateDescription(String description, int maxLength) {
        if (description == null) return "";
        if (description.length() <= maxLength) return description;
        return description.substring(0, maxLength - 3) + "...";
    }

    private ResponseEntity<Object> errorResponse(String message, HttpStatus status) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("success", false);
        return ResponseEntity.status(status).body(error);
    }
}
