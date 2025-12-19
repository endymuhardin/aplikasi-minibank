package id.ac.tazkia.minibank.controller.web;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Passbook;
import id.ac.tazkia.minibank.entity.Transaction;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.TransactionRepository;
import id.ac.tazkia.minibank.service.PassbookPrintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/passbook")
@RequiredArgsConstructor
public class PassbookController {

    private static final String ERROR_MESSAGE_ATTR = "errorMessage";
    private static final String ACCOUNT_NOT_FOUND_MSG = "Account not found";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PassbookPrintService passbookPrintService;
    
    @Value("${minibank.logo.path:/images/bank-logo.png}")
    private String bankLogoPath;
    
    @Value("${minibank.bank.name:Minibank Islamic Banking}")
    private String bankName;
    
    @Value("${minibank.bank.address:Jl. Raya Jakarta No. 123, Jakarta 12345}")
    private String bankAddress;
    
    @GetMapping("/select-account")
    public String selectAccount(@RequestParam(required = false) String search,
                               Model model) {
        
        List<Account> activeAccounts;
        if (search != null && !search.trim().isEmpty()) {
            activeAccounts = accountRepository.findByAccountNumberContainingIgnoreCaseOrAccountNameContainingIgnoreCase(
                search.trim(), search.trim()).stream()
                .filter(account -> account.getStatus() == Account.AccountStatus.ACTIVE)
                .toList();
        } else {
            activeAccounts = accountRepository.findByStatus(Account.AccountStatus.ACTIVE);
        }
        
        model.addAttribute("accounts", activeAccounts);
        model.addAttribute("search", search);
        return "passbook/select-account";
    }
    
    @GetMapping("/print/{accountId}")
    public String printPassbook(@PathVariable UUID accountId,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(required = false) String fromDate,
                               @RequestParam(required = false) String toDate,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, ACCOUNT_NOT_FOUND_MSG);
            return "redirect:/passbook/select-account";
        }
        
        Account account = accountOpt.get();
        
        // Check if account is active
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, 
                "Cannot print passbook for inactive account");
            return "redirect:/passbook/select-account";
        }
        
        // Build transaction query with optional date filters
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").ascending());
        Page<Transaction> transactions;
        
        LocalDate startDate = null;
        LocalDate endDate = null;
        
        try {
            if (fromDate != null && !fromDate.trim().isEmpty()) {
                startDate = LocalDate.parse(fromDate);
            }
            if (toDate != null && !toDate.trim().isEmpty()) {
                endDate = LocalDate.parse(toDate);
            }
        } catch (Exception e) {
            log.warn("Invalid date format provided: fromDate={}, toDate={}", fromDate, toDate);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, 
                "Invalid date format. Please use YYYY-MM-DD format.");
            return "redirect:/passbook/select-account";
        }
        
        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByAccountAndTransactionDateBetween(
                account, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay(), pageable);
        } else if (startDate != null) {
            transactions = transactionRepository.findByAccountAndTransactionDateGreaterThanEqual(
                account, startDate.atStartOfDay(), pageable);
        } else if (endDate != null) {
            transactions = transactionRepository.findByAccountAndTransactionDateLessThan(
                account, endDate.plusDays(1).atStartOfDay(), pageable);
        } else {
            transactions = transactionRepository.findByAccount(account, pageable);
        }
        
        // Calculate running balance for display
        // Note: In a real implementation, you might want to optimize this
        List<Transaction> allPreviousTransactions = transactionRepository.findByAccountOrderByTransactionDateAsc(account);
        
        model.addAttribute("account", account);
        model.addAttribute("transactions", transactions);
        model.addAttribute("allTransactions", allPreviousTransactions);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("printDate", LocalDate.now());
        model.addAttribute("bankLogoPath", bankLogoPath);
        model.addAttribute("bankName", bankName);
        model.addAttribute("bankAddress", bankAddress);
        
        return "passbook/print";
    }
    
    @GetMapping("/preview/{accountId}")
    public String previewPassbook(@PathVariable UUID accountId,
                                 @RequestParam(required = false) String fromDate,
                                 @RequestParam(required = false) String toDate,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, ACCOUNT_NOT_FOUND_MSG);
            return "redirect:/passbook/select-account";
        }
        
        Account account = accountOpt.get();
        
        // Get recent transactions for preview (last 10)
        Pageable recentTransactions = PageRequest.of(0, 10, Sort.by("transactionDate").descending());
        Page<Transaction> transactions = transactionRepository.findByAccount(account, recentTransactions);
        
        model.addAttribute("account", account);
        model.addAttribute("transactions", transactions);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("isPreview", true);
        model.addAttribute("bankLogoPath", bankLogoPath);
        model.addAttribute("bankName", bankName);
        model.addAttribute("bankAddress", bankAddress);

        return "passbook/preview";
    }

    @GetMapping("/direct-print/{accountId}")
    public String directPrintPassbook(@PathVariable UUID accountId,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {

        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, ACCOUNT_NOT_FOUND_MSG);
            return "redirect:/passbook/select-account";
        }

        Account account = accountOpt.get();

        // Check if account is active
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR,
                "Cannot print passbook for inactive account");
            return "redirect:/passbook/select-account";
        }

        // Get or create passbook for the account
        Passbook passbook = passbookPrintService.getOrCreatePassbook(accountId);

        // Get unprinted transactions count
        List<Transaction> unprintedTransactions = passbookPrintService.getUnprintedTransactions(accountId);

        model.addAttribute("account", account);
        model.addAttribute("passbook", passbook);
        model.addAttribute("unprintedCount", unprintedTransactions.size());
        model.addAttribute("bankLogoPath", bankLogoPath);
        model.addAttribute("bankName", bankName);
        model.addAttribute("bankAddress", bankAddress);

        return "passbook/direct-print";
    }

    @GetMapping("/initialize/{accountId}")
    public String initializePassbook(@PathVariable UUID accountId,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, ACCOUNT_NOT_FOUND_MSG);
            return "redirect:/passbook/select-account";
        }

        Account account = accountOpt.get();

        // Check if account is active
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR,
                "Cannot initialize passbook for inactive account");
            return "redirect:/passbook/select-account";
        }

        // Get all transactions for this account
        List<Transaction> allTransactions = transactionRepository.findByAccountOrderByTransactionDateAsc(account);

        // Get passbook if exists
        Optional<Passbook> passbookOpt = passbookPrintService.getPassbookByAccountId(accountId);

        model.addAttribute("account", account);
        model.addAttribute("transactions", allTransactions);
        model.addAttribute("passbook", passbookOpt.orElse(null));
        model.addAttribute("bankName", bankName);

        return "passbook/initialize";
    }

    @PostMapping("/initialize/{accountId}")
    public String submitInitializePassbook(@PathVariable UUID accountId,
                                          @RequestParam Integer currentPage,
                                          @RequestParam Integer lastPrintedLine,
                                          @RequestParam(required = false) UUID lastPrintedTransactionId,
                                          RedirectAttributes redirectAttributes) {

        try {
            // Validate inputs
            if (currentPage < 1) {
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Current page must be at least 1");
                return "redirect:/passbook/initialize/" + accountId;
            }

            if (lastPrintedLine < 0 || lastPrintedLine > 30) {
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Last printed line must be between 0 and 30");
                return "redirect:/passbook/initialize/" + accountId;
            }

            // Validate account
            Optional<Account> accountOpt = accountRepository.findById(accountId);
            if (accountOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, ACCOUNT_NOT_FOUND_MSG);
                return "redirect:/passbook/select-account";
            }

            // Validate transaction if provided
            Transaction lastTransaction = null;
            if (lastPrintedTransactionId != null) {
                Optional<Transaction> transactionOpt = transactionRepository.findById(lastPrintedTransactionId);
                if (transactionOpt.isEmpty()) {
                    redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Transaction not found");
                    return "redirect:/passbook/initialize/" + accountId;
                }
                lastTransaction = transactionOpt.get();

                // Verify transaction belongs to this account
                if (!lastTransaction.getAccount().getId().equals(accountId)) {
                    redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Transaction does not belong to this account");
                    return "redirect:/passbook/initialize/" + accountId;
                }
            }

            // Initialize passbook
            Passbook passbook = passbookPrintService.initializePassbook(
                accountId, currentPage, lastPrintedLine, lastTransaction);

            redirectAttributes.addFlashAttribute("successMessage",
                String.format("Passbook initialized successfully! Passbook Number: %s, Current Page: %d, Last Line: %d, Remaining Lines: %d",
                    passbook.getPassbookNumber(), passbook.getCurrentPage(),
                    passbook.getLastPrintedLine(), passbook.getRemainingLines()));

            return "redirect:/passbook/direct-print/" + accountId;

        } catch (Exception e) {
            log.error("Error initializing passbook for account: {}", accountId, e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Failed to initialize passbook: " + e.getMessage());
            return "redirect:/passbook/initialize/" + accountId;
        }
    }
}