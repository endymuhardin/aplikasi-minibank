package id.ac.tazkia.minibank.controller.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import id.ac.tazkia.minibank.dto.DepositRequest;
import id.ac.tazkia.minibank.dto.WithdrawalRequest;
import id.ac.tazkia.minibank.dto.TransferRequest;
import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Transaction;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.TransactionRepository;
import id.ac.tazkia.minibank.service.SequenceNumberService;
import id.ac.tazkia.minibank.service.TransferService;
import id.ac.tazkia.minibank.service.TransactionReceiptPdfService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;

@Slf4j
@Controller
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private static final String CASH_DEPOSIT_FORM_VIEW = "transaction/cash-deposit-form";
    private static final String CASH_WITHDRAWAL_FORM_VIEW = "transaction/cash-withdrawal-form";
    private static final String TRANSFER_FORM_VIEW = "transaction/transfer-form";
    private static final String TRANSFER_CONFIRM_VIEW = "transaction/transfer-confirm";
    private static final String TRANSACTION_LIST_REDIRECT = "redirect:/transaction/list";
    private static final String DEPOSIT_REQUEST_ATTR = "depositRequest";
    private static final String WITHDRAWAL_REQUEST_ATTR = "withdrawalRequest";
    private static final String TRANSFER_REQUEST_ATTR = "transferRequest";
    private static final String ERROR_MESSAGE_ATTR = "errorMessage";
    private static final String SUCCESS_MESSAGE_ATTR = "successMessage";
    private static final String ACCOUNT_NOT_FOUND_MSG = "Account not found";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final SequenceNumberService sequenceNumberService;
    private final TransferService transferService;
    private final TransactionReceiptPdfService receiptPdfService;
    private final AuditorAware<String> auditorAware;
    
    @GetMapping("/list")
    public String transactionList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String transactionType,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<Transaction> transactions;
        
        if (search != null && !search.trim().isEmpty()) {
            transactions = transactionRepository.findByTransactionNumberContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                search.trim(), search.trim(), pageable);
        } else if (transactionType != null && !transactionType.trim().isEmpty()) {
            transactions = transactionRepository.findByTransactionType(
                Transaction.TransactionType.valueOf(transactionType), pageable);
        } else {
            transactions = transactionRepository.findAll(pageable);
        }
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("search", search);
        model.addAttribute("transactionType", transactionType);
        model.addAttribute("transactionTypes", Transaction.TransactionType.values());
        
        return "transaction/list";
    }
    
    @GetMapping("/cash-deposit")
    public String selectAccountForDeposit(@RequestParam(required = false) UUID accountId,
                                         @RequestParam(required = false) String search,
                                         Model model) {
        if (accountId != null) {
            Optional<Account> accountOpt = accountRepository.findById(accountId);
            if (accountOpt.isPresent() && accountOpt.get().isActive()) {
                return "redirect:/transaction/cash-deposit/" + accountId;
            }
        }
        
        List<Account> accounts;
        if (search != null && !search.trim().isEmpty()) {
            accounts = accountRepository.findByAccountNumberContainingIgnoreCaseOrAccountNameContainingIgnoreCase(
                search.trim(), search.trim()).stream()
                .filter(Account::isActive)
                .toList();
        } else {
            accounts = accountRepository.findByStatus(Account.AccountStatus.ACTIVE);
        }
        
        model.addAttribute("accounts", accounts);
        model.addAttribute("search", search);
        model.addAttribute("transactionType", "deposit");
        return "transaction/select-account";
    }
    
    @GetMapping("/cash-deposit/{accountId}")
    public String cashDepositForm(@PathVariable UUID accountId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, ACCOUNT_NOT_FOUND_MSG);
            return "redirect:/transaction/cash-deposit";
        }
        
        Account account = accountOpt.get();
        if (!account.isActive()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Account is not active");
            return "redirect:/transaction/cash-deposit";
        }
        
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setAccountId(accountId);
        
        model.addAttribute(DEPOSIT_REQUEST_ATTR, depositRequest);
        model.addAttribute("account", account);
        
        return CASH_DEPOSIT_FORM_VIEW;
    }
    
    @PostMapping("/cash-deposit")
    public String processCashDeposit(@Valid @ModelAttribute DepositRequest depositRequest,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return prepareDepositFormWithErrors(depositRequest, model, bindingResult);
        }
        
        try {
            // Validate account exists and is active
            Optional<Account> accountOpt = accountRepository.findById(depositRequest.getAccountId());
            if (accountOpt.isEmpty()) {
                model.addAttribute(ERROR_MESSAGE_ATTR, ACCOUNT_NOT_FOUND_MSG);
                return prepareDepositFormWithErrors(depositRequest, model, null);
            }
            
            Account account = accountOpt.get();
            if (!account.isActive()) {
                model.addAttribute(ERROR_MESSAGE_ATTR, "Account is not active");
                return prepareDepositFormWithErrors(depositRequest, model, null);
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
            transaction.setAmount(depositRequest.getAmount());
            transaction.setBalanceBefore(balanceBefore);
            transaction.setDescription(depositRequest.getDescription() != null ?
                depositRequest.getDescription() : "Setoran Tunai");
            transaction.setReferenceNumber(depositRequest.getReferenceNumber());
            transaction.setChannel(Transaction.TransactionChannel.TELLER);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setProcessedDate(LocalDateTime.now());
            // createdBy will be set automatically by JPA auditing

            // Process deposit using business method
            account.deposit(depositRequest.getAmount());
            transaction.setBalanceAfter(account.getBalance());
            
            // Save transaction and account
            transactionRepository.save(transaction);
            accountRepository.save(account);
            
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, 
                String.format("Setoran tunai berhasil diproses. Nomor Transaksi: %s, Saldo Baru: %,.2f", 
                    transactionNumber, account.getBalance()));
            return TRANSACTION_LIST_REDIRECT;
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid deposit amount: {}", e.getMessage());
            model.addAttribute(ERROR_MESSAGE_ATTR, e.getMessage());
            return prepareDepositFormWithErrors(depositRequest, model, null);
        } catch (Exception e) {
            log.error("Failed to process cash deposit", e);
            model.addAttribute(ERROR_MESSAGE_ATTR, "Gagal memproses setoran tunai: " + e.getMessage());
            return prepareDepositFormWithErrors(depositRequest, model, null);
        }
    }
    
    @GetMapping("/view/{id}")
    public String viewTransaction(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(id);
        if (transactionOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Transaction not found");
            return TRANSACTION_LIST_REDIRECT;
        }
        
        model.addAttribute("transaction", transactionOpt.get());
        return "transaction/view";
    }
    
    @GetMapping("/cash-withdrawal")
    public String selectAccountForWithdrawal(@RequestParam(required = false) UUID accountId,
                                           @RequestParam(required = false) String search,
                                           Model model) {
        if (accountId != null) {
            Optional<Account> accountOpt = accountRepository.findById(accountId);
            if (accountOpt.isPresent() && accountOpt.get().isActive()) {
                return "redirect:/transaction/cash-withdrawal/" + accountId;
            }
        }
        
        List<Account> accounts;
        if (search != null && !search.trim().isEmpty()) {
            accounts = accountRepository.findByAccountNumberContainingIgnoreCaseOrAccountNameContainingIgnoreCase(
                search.trim(), search.trim()).stream()
                .filter(Account::isActive)
                .toList();
        } else {
            accounts = accountRepository.findByStatus(Account.AccountStatus.ACTIVE);
        }
        
        model.addAttribute("accounts", accounts);
        model.addAttribute("search", search);
        model.addAttribute("transactionType", "withdrawal");
        return "transaction/select-account";
    }
    
    @GetMapping("/cash-withdrawal/{accountId}")
    public String cashWithdrawalForm(@PathVariable UUID accountId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, ACCOUNT_NOT_FOUND_MSG);
            return "redirect:/transaction/cash-withdrawal";
        }
        
        Account account = accountOpt.get();
        if (!account.isActive()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Account is not active");
            return "redirect:/transaction/cash-withdrawal";
        }
        
        WithdrawalRequest withdrawalRequest = new WithdrawalRequest();
        withdrawalRequest.setAccountId(accountId);
        
        model.addAttribute(WITHDRAWAL_REQUEST_ATTR, withdrawalRequest);
        model.addAttribute("account", account);
        
        return CASH_WITHDRAWAL_FORM_VIEW;
    }
    
    @PostMapping("/cash-withdrawal")
    public String processCashWithdrawal(@Valid @ModelAttribute WithdrawalRequest withdrawalRequest,
                                       BindingResult bindingResult,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return prepareWithdrawalFormWithErrors(withdrawalRequest, model, bindingResult);
        }
        
        try {
            // Validate account exists and is active
            Optional<Account> accountOpt = accountRepository.findById(withdrawalRequest.getAccountId());
            if (accountOpt.isEmpty()) {
                model.addAttribute(ERROR_MESSAGE_ATTR, ACCOUNT_NOT_FOUND_MSG);
                return prepareWithdrawalFormWithErrors(withdrawalRequest, model, null);
            }
            
            Account account = accountOpt.get();
            if (!account.isActive()) {
                model.addAttribute(ERROR_MESSAGE_ATTR, "Account is not active");
                return prepareWithdrawalFormWithErrors(withdrawalRequest, model, null);
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
            transaction.setAmount(withdrawalRequest.getAmount());
            transaction.setBalanceBefore(balanceBefore);
            transaction.setDescription(withdrawalRequest.getDescription() != null ?
                withdrawalRequest.getDescription() : "Penarikan Tunai");
            transaction.setReferenceNumber(withdrawalRequest.getReferenceNumber());
            transaction.setChannel(Transaction.TransactionChannel.TELLER);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setProcessedDate(LocalDateTime.now());
            // createdBy will be set automatically by JPA auditing

            // Process withdrawal using business method
            account.withdraw(withdrawalRequest.getAmount());
            transaction.setBalanceAfter(account.getBalance());
            
            // Save transaction and account
            transactionRepository.save(transaction);
            accountRepository.save(account);
            
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, 
                String.format("Penarikan tunai berhasil diproses. Nomor Transaksi: %s, Saldo Baru: %,.2f", 
                    transactionNumber, account.getBalance()));
            return TRANSACTION_LIST_REDIRECT;
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid withdrawal amount: {}", e.getMessage());
            model.addAttribute(ERROR_MESSAGE_ATTR, e.getMessage());
            return prepareWithdrawalFormWithErrors(withdrawalRequest, model, null);
        } catch (Exception e) {
            log.error("Failed to process cash withdrawal", e);
            model.addAttribute(ERROR_MESSAGE_ATTR, "Gagal memproses penarikan tunai: " + e.getMessage());
            return prepareWithdrawalFormWithErrors(withdrawalRequest, model, null);
        }
    }
    
    private String prepareDepositFormWithErrors(DepositRequest depositRequest, Model model, BindingResult bindingResult) {
        Optional<Account> accountOpt = accountRepository.findById(depositRequest.getAccountId());
        if (accountOpt.isPresent()) {
            model.addAttribute(DEPOSIT_REQUEST_ATTR, depositRequest);
            model.addAttribute("account", accountOpt.get());
        }
        
        return CASH_DEPOSIT_FORM_VIEW;
    }
    
    private String prepareWithdrawalFormWithErrors(WithdrawalRequest withdrawalRequest, Model model, BindingResult bindingResult) {
        Optional<Account> accountOpt = accountRepository.findById(withdrawalRequest.getAccountId());
        if (accountOpt.isPresent()) {
            model.addAttribute(WITHDRAWAL_REQUEST_ATTR, withdrawalRequest);
            model.addAttribute("account", accountOpt.get());
        }
        
        return CASH_WITHDRAWAL_FORM_VIEW;
    }
    
    // Transfer endpoints
    @GetMapping("/transfer")
    public String selectSourceAccountForTransfer(@RequestParam(required = false) UUID accountId,
                                               @RequestParam(required = false) String search,
                                               Model model) {
        if (accountId != null) {
            Optional<Account> accountOpt = accountRepository.findById(accountId);
            if (accountOpt.isPresent() && accountOpt.get().isActive()) {
                return "redirect:/transaction/transfer/" + accountId;
            }
        }
        
        List<Account> accounts;
        if (search != null && !search.trim().isEmpty()) {
            accounts = accountRepository.findByAccountNumberContainingIgnoreCaseOrAccountNameContainingIgnoreCase(
                search.trim(), search.trim()).stream()
                .filter(Account::isActive)
                .toList();
        } else {
            accounts = accountRepository.findByStatus(Account.AccountStatus.ACTIVE);
        }
        
        model.addAttribute("accounts", accounts);
        model.addAttribute("search", search);
        model.addAttribute("transactionType", "transfer");
        return "transaction/select-account";
    }
    
    @GetMapping("/transfer/{accountId}")
    public String transferForm(@PathVariable UUID accountId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, ACCOUNT_NOT_FOUND_MSG);
            return "redirect:/transaction/transfer";
        }
        
        Account account = accountOpt.get();
        if (!account.isActive()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Account is not active");
            return "redirect:/transaction/transfer";
        }
        
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(accountId);
        
        model.addAttribute(TRANSFER_REQUEST_ATTR, transferRequest);
        model.addAttribute("account", account);
        
        return TRANSFER_FORM_VIEW;
    }
    
    @PostMapping("/transfer/validate")
    public String validateTransfer(@Valid @ModelAttribute TransferRequest transferRequest,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return prepareTransferFormWithErrors(transferRequest, model, bindingResult);
        }
        
        try {
            // Generate reference number if not provided
            if (transferRequest.getReferenceNumber() == null || transferRequest.getReferenceNumber().trim().isEmpty()) {
                transferRequest.setReferenceNumber("TRF" + System.currentTimeMillis());
            }
            
            // Validate transfer and populate destination account info
            TransferRequest validatedRequest = transferService.validateTransfer(transferRequest);
            
            // Get source account for display
            Account sourceAccount = transferService.getAccountById(validatedRequest.getFromAccountId());
            
            model.addAttribute(TRANSFER_REQUEST_ATTR, validatedRequest);
            model.addAttribute("sourceAccount", sourceAccount);
            
            return TRANSFER_CONFIRM_VIEW;
            
        } catch (IllegalArgumentException e) {
            log.warn("Transfer validation failed: {}", e.getMessage());
            model.addAttribute(ERROR_MESSAGE_ATTR, e.getMessage());
            return prepareTransferFormWithErrors(transferRequest, model, null);
        } catch (Exception e) {
            log.error("Failed to validate transfer", e);
            model.addAttribute(ERROR_MESSAGE_ATTR, "Failed to validate transfer: " + e.getMessage());
            return prepareTransferFormWithErrors(transferRequest, model, null);
        }
    }
    
    @PostMapping("/transfer/process")
    public String processTransfer(@ModelAttribute TransferRequest transferRequest,
                                 RedirectAttributes redirectAttributes) {
        
        try {
            // Process the transfer
            transferService.processTransfer(transferRequest);
            
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, 
                String.format("Transfer berhasil diproses. Referensi: %s, Jumlah: %,.2f dari %s ke %s", 
                    transferRequest.getReferenceNumber(), 
                    transferRequest.getAmount(),
                    transferService.getAccountById(transferRequest.getFromAccountId()).getAccountNumber(),
                    transferRequest.getToAccountNumber()));
            return TRANSACTION_LIST_REDIRECT;
            
        } catch (IllegalArgumentException e) {
            log.warn("Transfer processing failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, e.getMessage());
            return "redirect:/transaction/transfer/" + transferRequest.getFromAccountId();
        } catch (Exception e) {
            log.error("Failed to process transfer", e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Failed to process transfer: " + e.getMessage());
            return "redirect:/transaction/transfer/" + transferRequest.getFromAccountId();
        }
    }
    
    @PostMapping("/transfer/cancel")
    public String cancelTransfer(@RequestParam UUID fromAccountId, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("infoMessage", "Transfer dibatalkan");
        return "redirect:/transaction/transfer/" + fromAccountId;
    }
    
    private String prepareTransferFormWithErrors(TransferRequest transferRequest, Model model, BindingResult bindingResult) {
        try {
            Optional<Account> accountOpt = accountRepository.findById(transferRequest.getFromAccountId());
            if (accountOpt.isPresent()) {
                model.addAttribute(TRANSFER_REQUEST_ATTR, transferRequest);
                model.addAttribute("account", accountOpt.get());
            }
        } catch (Exception e) {
            log.error("Error preparing transfer form", e);
        }
        
        return TRANSFER_FORM_VIEW;
    }

    @GetMapping("/receipt/{transactionId}")
    public ResponseEntity<byte[]> downloadTransactionReceipt(@PathVariable UUID transactionId) {
        log.info("Generating PDF receipt for transaction: {}", transactionId);
        
        try {
            // Get transaction details
            Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
            if (transactionOpt.isEmpty()) {
                log.warn("Transaction not found: {}", transactionId);
                return ResponseEntity.notFound().build();
            }
            
            Transaction transaction = transactionOpt.get();
            Account account = transaction.getAccount();
            
            // Calculate balance after transaction based on transaction type
            BigDecimal balanceAfter = calculateBalanceAfter(transaction, account);
            
            // Generate PDF receipt
            byte[] pdfContent = receiptPdfService.generateTransactionReceiptPdf(transaction, account, balanceAfter);
            
            // Create filename with transaction number
            String filename = "receipt_" + transaction.getTransactionNumber() + ".pdf";
            
            log.info("Generated PDF receipt for transaction {} with {} bytes", 
                    transaction.getTransactionNumber(), pdfContent.length);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .body(pdfContent);
                    
        } catch (Exception e) {
            log.error("Error generating transaction receipt for transaction: {}", transactionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    private BigDecimal calculateBalanceAfter(Transaction transaction, Account account) {
        // For deposit transactions, balance after = current balance (already includes the deposit)
        // For withdrawal/transfer out, balance after = current balance (already deducted)
        // For transfer in, balance after = current balance (already added)
        // We'll use the current account balance as it represents the state after the transaction
        return account.getBalance();
    }
}