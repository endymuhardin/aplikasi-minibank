package id.ac.tazkia.minibank.functional.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.transaction.annotation.Transactional;

import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.functional.web.pageobject.AccountSelectionPage;
import id.ac.tazkia.minibank.functional.web.pageobject.TransactionListPage;
import id.ac.tazkia.minibank.functional.web.pageobject.TransferConfirmPage;
import id.ac.tazkia.minibank.functional.web.pageobject.TransferFormPage;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.BranchRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.util.SimpleParallelTestDataFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Transfer Selenium Tests
 * 
 * This test class focuses on transfer scenarios using Selenium WebDriver.
 * Tests cover the complete transfer workflow from account selection to successful transfer
 * and various validation scenarios.
 * 
 * Key test areas:
 * - Transfer account selection workflow
 * - Transfer form validation and data entry
 * - Transfer confirmation process
 * - Successful transfer execution
 * - Error handling for various validation scenarios
 * - Navigation and cancellation scenarios
 */
@Slf4j
@SqlGroup({
    @Sql(scripts = "/sql/setup-account-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(scripts = "/sql/cleanup-account-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class TransferSeleniumTest extends BaseSeleniumTest {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private BranchRepository branchRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    private Account sourceAccount;
    private Account destinationAccount;
    
    @Override
    protected void performInitialLogin() {
        // Login as Teller user who has transfer permissions
        getLoginHelper().loginAsTeller();
    }
    
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    void shouldNavigateToTransferAccountSelection() {
        log.info("🧪 TEST START: shouldNavigateToTransferAccountSelection");
        
        TransactionListPage transactionListPage = new TransactionListPage(driver, baseUrl);
        transactionListPage.navigateToTransactionList();
        
        AccountSelectionPage accountSelectionPage = transactionListPage.clickTransferButton();
        
        assertTrue(accountSelectionPage.isOnAccountSelectionPage());
        assertTrue(driver.getCurrentUrl().contains("/transaction/transfer"));
        
        log.info("✅ TEST PASS: shouldNavigateToTransferAccountSelection");
    }
    
    @Test
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    void shouldDisplayTransferFormForSelectedAccount() {
        log.info("🧪 TEST START: shouldDisplayTransferFormForSelectedAccount");
        
        // Create test account
        Account testAccount = createTestAccountWithBalance(new BigDecimal("250000.00"));
        String accountNumber = testAccount.getAccountNumber();
        
        AccountSelectionPage accountSelectionPage = new AccountSelectionPage(driver, baseUrl);
        accountSelectionPage.openTransferAccountSelection();
        
        TransferFormPage transferFormPage = accountSelectionPage.selectAccountForTransfer(accountNumber);
        transferFormPage.waitForPageLoad();
        
        assertTrue(transferFormPage.isOnTransferFormPage());
        assertEquals(accountNumber, transferFormPage.getSourceAccountNumber());
        assertNotNull(transferFormPage.getSourceAccountName());
        
        log.info("✅ TEST PASS: shouldDisplayTransferFormForSelectedAccount");
    }
    
    @Test
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    void shouldValidateTransferAndShowConfirmation() {
        log.info("🧪 TEST START: shouldValidateTransferAndShowConfirmation");
        
        // Create test accounts
        Account sourceAccount = createTestAccountWithBalance(new BigDecimal("500000.00"));
        Account destinationAccount = createTestAccountWithBalance(new BigDecimal("100000.00"));
        
        // Navigate to transfer form
        AccountSelectionPage accountSelectionPage = new AccountSelectionPage(driver, baseUrl);
        accountSelectionPage.openTransferAccountSelection();
        
        TransferFormPage transferFormPage = accountSelectionPage.selectAccountForTransfer(sourceAccount.getAccountNumber());
        transferFormPage.waitForPageLoad();
        
        // Fill transfer form
        transferFormPage.fillCompleteTransferForm(
            destinationAccount.getAccountNumber(),
            "150000.00",
            "Selenium test transfer",
            "SELENIUM_USER"
        );
        
        // Validate transfer
        TransferConfirmPage confirmPage = transferFormPage.validateTransfer();
        confirmPage.waitForPageLoad();
        
        assertTrue(confirmPage.isOnTransferConfirmPage());
        assertTrue(confirmPage.isTransferDetailsDisplayed());
        assertTrue(confirmPage.isWarningMessageDisplayed());
        
        log.info("✅ TEST PASS: shouldValidateTransferAndShowConfirmation");
    }
    
    @Test
    @Timeout(value = 150, unit = TimeUnit.SECONDS)
    void shouldProcessTransferSuccessfully() {
        log.info("🧪 TEST START: shouldProcessTransferSuccessfully");
        
        // Create test accounts
        Account sourceAccount = createTestAccountWithBalance(new BigDecimal("750000.00"));
        Account destinationAccount = createTestAccountWithBalance(new BigDecimal("250000.00"));
        
        BigDecimal initialSourceBalance = sourceAccount.getBalance();
        BigDecimal initialDestinationBalance = destinationAccount.getBalance();
        BigDecimal transferAmount = new BigDecimal("200000.00");
        
        // Navigate through complete transfer workflow
        AccountSelectionPage accountSelectionPage = new AccountSelectionPage(driver, baseUrl);
        accountSelectionPage.openTransferAccountSelection();
        
        TransferFormPage transferFormPage = accountSelectionPage.selectAccountForTransfer(sourceAccount.getAccountNumber());
        transferFormPage.waitForPageLoad();
        
        // Fill and validate transfer
        transferFormPage.fillCompleteTransferFormWithReference(
            destinationAccount.getAccountNumber(),
            transferAmount.toString(),
            "End-to-end Selenium transfer test",
            "SELENIUM_E2E_001",
            "SELENIUM_TELLER"
        );
        
        TransferConfirmPage confirmPage = transferFormPage.validateTransfer();
        confirmPage.waitForPageLoad();
        
        // Confirm and process transfer
        confirmPage.confirmTransfer();
        TransactionListPage transactionListPage = confirmPage.processTransfer();
        
        // Verify success
        assertTrue(transactionListPage.isOnTransactionListPage());
        assertTrue(transactionListPage.isSuccessMessageDisplayed());
        assertTrue(transactionListPage.getSuccessMessage().contains("berhasil diproses"));
        assertTrue(transactionListPage.getSuccessMessage().contains("SELENIUM_E2E_001"));
        
        // Verify database changes
        Account updatedSourceAccount = accountRepository.findById(sourceAccount.getId()).get();
        Account updatedDestinationAccount = accountRepository.findById(destinationAccount.getId()).get();
        
        BigDecimal expectedSourceBalance = initialSourceBalance.subtract(transferAmount);
        BigDecimal expectedDestinationBalance = initialDestinationBalance.add(transferAmount);
        
        assertEquals(0, updatedSourceAccount.getBalance().compareTo(expectedSourceBalance));
        assertEquals(0, updatedDestinationAccount.getBalance().compareTo(expectedDestinationBalance));
        
        log.info("✅ TEST PASS: shouldProcessTransferSuccessfully - Transferred {} from {} to {}", 
            transferAmount, sourceAccount.getAccountNumber(), destinationAccount.getAccountNumber());
    }
    
    @Test
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    void shouldShowValidationErrorForInvalidDestinationAccount() {
        log.info("🧪 TEST START: shouldShowValidationErrorForInvalidDestinationAccount");
        
        Account sourceAccount = createTestAccountWithBalance(new BigDecimal("300000.00"));
        
        AccountSelectionPage accountSelectionPage = new AccountSelectionPage(driver, baseUrl);
        accountSelectionPage.openTransferAccountSelection();
        
        TransferFormPage transferFormPage = accountSelectionPage.selectAccountForTransfer(sourceAccount.getAccountNumber());
        transferFormPage.waitForPageLoad();
        
        // Fill form with invalid destination account
        transferFormPage.fillCompleteTransferForm(
            "INVALID_ACCOUNT_NUMBER",
            "50000.00",
            "Invalid destination test",
            "SELENIUM_USER"
        );
        
        // Try to validate - should show error
        transferFormPage.validateTransferExpectingError();
        
        assertTrue(transferFormPage.isErrorMessageDisplayed());
        assertTrue(transferFormPage.getErrorMessage().contains("not found"));
        
        log.info("✅ TEST PASS: shouldShowValidationErrorForInvalidDestinationAccount");
    }
    
    @Test
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    void shouldShowValidationErrorForInsufficientBalance() {
        log.info("🧪 TEST START: shouldShowValidationErrorForInsufficientBalance");
        
        Account sourceAccount = createTestAccountWithBalance(new BigDecimal("100000.00"));
        Account destinationAccount = createTestAccountWithBalance(new BigDecimal("50000.00"));
        
        AccountSelectionPage accountSelectionPage = new AccountSelectionPage(driver, baseUrl);
        accountSelectionPage.openTransferAccountSelection();
        
        TransferFormPage transferFormPage = accountSelectionPage.selectAccountForTransfer(sourceAccount.getAccountNumber());
        transferFormPage.waitForPageLoad();
        
        // Try to transfer more than available balance
        transferFormPage.fillCompleteTransferForm(
            destinationAccount.getAccountNumber(),
            "150000.00", // Exceeds source balance
            "Insufficient balance test",
            "SELENIUM_USER"
        );
        
        transferFormPage.validateTransferExpectingError();
        
        assertTrue(transferFormPage.isErrorMessageDisplayed());
        assertTrue(transferFormPage.getErrorMessage().contains("Insufficient balance"));
        
        log.info("✅ TEST PASS: shouldShowValidationErrorForInsufficientBalance");
    }
    
    @Test
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    void shouldShowValidationErrorForSelfTransfer() {
        log.info("🧪 TEST START: shouldShowValidationErrorForSelfTransfer");
        
        Account testAccount = createTestAccountWithBalance(new BigDecimal("200000.00"));
        
        AccountSelectionPage accountSelectionPage = new AccountSelectionPage(driver, baseUrl);
        accountSelectionPage.openTransferAccountSelection();
        
        TransferFormPage transferFormPage = accountSelectionPage.selectAccountForTransfer(testAccount.getAccountNumber());
        transferFormPage.waitForPageLoad();
        
        // Try to transfer to same account
        transferFormPage.fillCompleteTransferForm(
            testAccount.getAccountNumber(), // Same account
            "50000.00",
            "Self transfer test",
            "SELENIUM_USER"
        );
        
        transferFormPage.validateTransferExpectingError();
        
        assertTrue(transferFormPage.isErrorMessageDisplayed());
        assertTrue(transferFormPage.getErrorMessage().contains("same account"));
        
        log.info("✅ TEST PASS: shouldShowValidationErrorForSelfTransfer");
    }
    
    @Test
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    void shouldAllowCancellingTransferFromConfirmation() {
        log.info("🧪 TEST START: shouldAllowCancellingTransferFromConfirmation");
        
        Account sourceAccount = createTestAccountWithBalance(new BigDecimal("400000.00"));
        Account destinationAccount = createTestAccountWithBalance(new BigDecimal("100000.00"));
        
        // Navigate to confirmation page
        AccountSelectionPage accountSelectionPage = new AccountSelectionPage(driver, baseUrl);
        accountSelectionPage.openTransferAccountSelection();
        
        TransferFormPage transferFormPage = accountSelectionPage.selectAccountForTransfer(sourceAccount.getAccountNumber());
        transferFormPage.waitForPageLoad();
        
        transferFormPage.fillCompleteTransferForm(
            destinationAccount.getAccountNumber(),
            "75000.00",
            "Cancellation test",
            "SELENIUM_USER"
        );
        
        TransferConfirmPage confirmPage = transferFormPage.validateTransfer();
        confirmPage.waitForPageLoad();
        
        // Cancel the transfer
        TransferFormPage backToFormPage = confirmPage.cancelTransfer();
        
        assertTrue(backToFormPage.isOnTransferFormPage());
        
        // Verify no transfer occurred
        Account unchangedSourceAccount = accountRepository.findById(sourceAccount.getId()).get();
        assertEquals(0, unchangedSourceAccount.getBalance().compareTo(new BigDecimal("400000.00")));
        
        log.info("✅ TEST PASS: shouldAllowCancellingTransferFromConfirmation");
    }
    
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    void shouldHandleMinimumTransferAmount() {
        log.info("🧪 TEST START: shouldHandleMinimumTransferAmount");
        
        Account sourceAccount = createTestAccountWithBalance(new BigDecimal("1000.00"));
        Account destinationAccount = createTestAccountWithBalance(new BigDecimal("500.00"));
        
        AccountSelectionPage accountSelectionPage = new AccountSelectionPage(driver, baseUrl);
        accountSelectionPage.openTransferAccountSelection();
        
        TransferFormPage transferFormPage = accountSelectionPage.selectAccountForTransfer(sourceAccount.getAccountNumber());
        transferFormPage.waitForPageLoad();
        
        // Test minimum transfer amount
        transferFormPage.fillCompleteTransferForm(
            destinationAccount.getAccountNumber(),
            "0.01", // Minimum amount
            "Minimum amount transfer",
            "SELENIUM_USER"
        );
        
        TransferConfirmPage confirmPage = transferFormPage.validateTransfer();
        confirmPage.waitForPageLoad();
        
        assertTrue(confirmPage.isOnTransferConfirmPage());
        
        log.info("✅ TEST PASS: shouldHandleMinimumTransferAmount");
    }
    
    // Helper methods for creating test data
    private Account createTestAccountWithBalance(BigDecimal balance) {
        // Create all required dependencies
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branch = branchRepository.save(branch);
        
        PersonalCustomer customer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customer = customerRepository.save(customer);
        
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        product = productRepository.save(product);
        
        // Create account with all dependencies
        Account account = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        account.setBalance(balance);
        account.setStatus(Account.AccountStatus.ACTIVE);
        
        return accountRepository.save(account);
    }
}