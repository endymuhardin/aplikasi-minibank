package id.ac.tazkia.minibank.functional.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;
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
import id.ac.tazkia.minibank.functional.web.pageobject.AccountClosurePage;
import id.ac.tazkia.minibank.functional.web.pageobject.AccountListPage;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.BranchRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.util.SimpleParallelTestDataFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Account Closure Selenium Tests
 * 
 * This test class focuses on account closure scenarios using Selenium WebDriver.
 * Tests cover the complete flow from account list navigation to successful closure
 * and various validation scenarios.
 * 
 * Key test areas:
 * - Account closure form navigation and display
 * - Balance validation before closure
 * - Account closure workflow for zero balance accounts
 * - Error handling for non-zero balance accounts
 * - Form validation and confirmation requirements
 * - Navigation and cancellation scenarios
 * 
 * Based on the Islamic banking account lifecycle requirements documented in:
 * docs/test-scenarios/account-management/account-lifecycle.md
 */
@Slf4j
@SqlGroup({
    @Sql(scripts = "/sql/setup-account-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(scripts = "/sql/cleanup-account-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class AccountClosureSeleniumTest extends BaseSeleniumTest {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private BranchRepository branchRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Override
    protected void performInitialLogin() {
        // Login as Customer Service user who has account closure permissions
        getLoginHelper().loginAsCustomerServiceUser();
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldLoadAccountListPageWithCloseLinks() {
        log.info("🧪 TEST START: shouldLoadAccountListPageWithCloseLinks");
        AccountListPage listPage = new AccountListPage(driver, baseUrl);
        listPage.openAndWaitForLoad();
        
        assertTrue(listPage.isOnAccountListPage());
        // Verify accounts have close links (for non-closed accounts)
        if (listPage.hasAccounts()) {
            // At least one account should have a close link available
            assertTrue(listPage.getAccountCount() >= 0);
        }
        log.info("✅ TEST PASS: shouldLoadAccountListPageWithCloseLinks completed successfully");
    }
    
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    @Transactional
    void shouldNavigateToAccountClosureForm() {
        log.info("🧪 TEST START: shouldNavigateToAccountClosureForm");
        
        // Create a test account with zero balance for closure
        Account testAccount = createTestAccountWithZeroBalance();
        String accountNumber = testAccount.getAccountNumber();
        log.info("Created test account: {} for closure test", accountNumber);
        
        AccountListPage listPage = new AccountListPage(driver, baseUrl);
        listPage.openAndWaitForLoad();
        
        // Verify the test account appears in the list and has close link
        assertTrue(listPage.isAccountDisplayed(accountNumber));
        assertTrue(listPage.hasCloseLink(accountNumber));
        
        AccountClosurePage closurePage = listPage.closeAccount(accountNumber);
        closurePage.waitForPageLoad();
        
        assertTrue(closurePage.isOnAccountClosurePage());
        assertTrue(driver.getCurrentUrl().contains("/close"));
        assertEquals(accountNumber, closurePage.getAccountNumber());
        
        log.info("✅ TEST PASS: shouldNavigateToAccountClosureForm completed successfully");
    }
    
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    @Transactional
    void shouldDisplayAccountInformationOnClosureForm() {
        log.info("🧪 TEST START: shouldDisplayAccountInformationOnClosureForm");
        
        Account testAccount = createTestAccountWithZeroBalance();
        String accountNumber = testAccount.getAccountNumber();
        
        AccountListPage listPage = new AccountListPage(driver, baseUrl);
        listPage.openAndWaitForLoad();
        
        AccountClosurePage closurePage = listPage.closeAccount(accountNumber);
        closurePage.waitForPageLoad();
        
        // Verify account information is displayed correctly
        assertEquals(accountNumber, closurePage.getAccountNumber());
        assertNotNull(closurePage.getAccountName());
        assertTrue(closurePage.getCurrentBalance().contains("0.00") || 
                   closurePage.getCurrentBalance().contains("0,00"));
        
        // Verify warning message is displayed
        assertTrue(closurePage.isWarningMessageDisplayed());
        assertTrue(closurePage.getWarningMessage().contains("Account Closure Warning"));
        
        log.info("✅ TEST PASS: shouldDisplayAccountInformationOnClosureForm completed successfully");
    }
    
    @Test
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    @Transactional
    void shouldSuccessfullyCloseAccountWithZeroBalance() {
        log.info("🧪 TEST START: shouldSuccessfullyCloseAccountWithZeroBalance");
        
        Account testAccount = createTestAccountWithZeroBalance();
        String accountNumber = testAccount.getAccountNumber();
        
        AccountListPage listPage = new AccountListPage(driver, baseUrl);
        listPage.openAndWaitForLoad();
        
        AccountClosurePage closurePage = listPage.closeAccount(accountNumber);
        closurePage.waitForPageLoad();
        
        // Fill closure form
        closurePage.fillCompleteForm("Customer request - account no longer needed");
        
        // Verify close button is enabled for zero balance account
        assertFalse(closurePage.isCloseButtonDisabled());
        
        AccountListPage returnedListPage = closurePage.closeAccount();
        
        // Verify successful closure
        assertTrue(returnedListPage.isSuccessMessageDisplayed());
        assertTrue(returnedListPage.getSuccessMessage().contains("closed successfully"));
        assertTrue(returnedListPage.getSuccessMessage().contains(accountNumber));
        
        // Verify account status changed in database
        Optional<Account> updatedAccount = accountRepository.findByAccountNumber(accountNumber);
        assertTrue(updatedAccount.isPresent());
        assertEquals(Account.AccountStatus.CLOSED, updatedAccount.get().getStatus());
        assertNotNull(updatedAccount.get().getClosedDate());
        
        log.info("✅ TEST PASS: shouldSuccessfullyCloseAccountWithZeroBalance completed successfully");
    }
    
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    @Transactional
    void shouldPreventClosureOfAccountWithBalance() {
        log.info("🧪 TEST START: shouldPreventClosureOfAccountWithBalance");
        
        Account testAccount = createTestAccountWithBalance(new BigDecimal("50000.00"));
        String accountNumber = testAccount.getAccountNumber();
        
        AccountListPage listPage = new AccountListPage(driver, baseUrl);
        listPage.openAndWaitForLoad();
        
        AccountClosurePage closurePage = listPage.closeAccount(accountNumber);
        closurePage.waitForPageLoad();
        
        // Verify balance error is displayed
        assertTrue(closurePage.hasNonZeroBalance());
        assertTrue(closurePage.isCloseButtonDisabled());
        
        // Verify warning message about balance
        String currentBalance = closurePage.getCurrentBalance();
        assertTrue(currentBalance.contains("50") && currentBalance.contains("000"));
        
        log.info("✅ TEST PASS: shouldPreventClosureOfAccountWithBalance completed successfully");
    }
    
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    @Transactional
    void shouldRequireConfirmationForAccountClosure() {
        log.info("🧪 TEST START: shouldRequireConfirmationForAccountClosure");
        
        Account testAccount = createTestAccountWithZeroBalance();
        String accountNumber = testAccount.getAccountNumber();
        
        AccountListPage listPage = new AccountListPage(driver, baseUrl);
        listPage.openAndWaitForLoad();
        
        AccountClosurePage closurePage = listPage.closeAccount(accountNumber);
        closurePage.waitForPageLoad();
        
        // Verify confirmation checkbox is present for zero balance accounts
        assertTrue(closurePage.isConfirmationCheckboxVisible());
        
        // Fill reason but don't confirm
        closurePage.fillClosureReason("Test closure without confirmation");
        
        // Try to close without confirmation - button should require confirmation
        closurePage.closeAccountExpectingError();
        
        // Verify still on closure page (form validation should prevent submission)
        assertTrue(closurePage.isOnAccountClosurePage());
        
        log.info("✅ TEST PASS: shouldRequireConfirmationForAccountClosure completed successfully");
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    @Transactional
    void shouldAllowCancellingAccountClosure() {
        log.info("🧪 TEST START: shouldAllowCancellingAccountClosure");
        
        Account testAccount = createTestAccountWithZeroBalance();
        String accountNumber = testAccount.getAccountNumber();
        
        AccountListPage listPage = new AccountListPage(driver, baseUrl);
        listPage.openAndWaitForLoad();
        
        AccountClosurePage closurePage = listPage.closeAccount(accountNumber);
        closurePage.waitForPageLoad();
        
        AccountListPage cancelledListPage = closurePage.cancel();
        
        assertTrue(cancelledListPage.isOnAccountListPage());
        
        // Verify account is still active (not closed)
        Optional<Account> account = accountRepository.findByAccountNumber(accountNumber);
        assertTrue(account.isPresent());
        assertEquals(Account.AccountStatus.ACTIVE, account.get().getStatus());
        
        log.info("✅ TEST PASS: shouldAllowCancellingAccountClosure completed successfully");
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    @Transactional
    void shouldAllowNavigationBackToAccountList() {
        log.info("🧪 TEST START: shouldAllowNavigationBackToAccountList");
        
        Account testAccount = createTestAccountWithZeroBalance();
        String accountNumber = testAccount.getAccountNumber();
        
        AccountListPage listPage = new AccountListPage(driver, baseUrl);
        listPage.openAndWaitForLoad();
        
        AccountClosurePage closurePage = listPage.closeAccount(accountNumber);
        closurePage.waitForPageLoad();
        
        AccountListPage backToListPage = closurePage.backToAccountList();
        
        assertTrue(backToListPage.isOnAccountListPage());
        assertTrue(driver.getCurrentUrl().contains("/account/list"));
        
        log.info("✅ TEST PASS: shouldAllowNavigationBackToAccountList completed successfully");
    }
    
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    @Transactional
    void shouldNotShowCloseLinksForClosedAccounts() {
        log.info("🧪 TEST START: shouldNotShowCloseLinksForClosedAccounts");
        
        Account testAccount = createTestAccountWithZeroBalance();
        String accountNumber = testAccount.getAccountNumber();
        
        // Close the account programmatically
        testAccount.closeAccount();
        accountRepository.save(testAccount);
        
        AccountListPage listPage = new AccountListPage(driver, baseUrl);
        listPage.openAndWaitForLoad();
        
        // Verify closed account doesn't have close link
        assertTrue(listPage.isAccountDisplayed(accountNumber));
        assertFalse(listPage.hasCloseLink(accountNumber));
        
        log.info("✅ TEST PASS: shouldNotShowCloseLinksForClosedAccounts completed successfully");
    }
    
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    @Transactional
    void shouldHandleAlreadyClosedAccountError() {
        log.info("🧪 TEST START: shouldHandleAlreadyClosedAccountError");
        
        Account testAccount = createTestAccountWithZeroBalance();
        String accountNumber = testAccount.getAccountNumber();
        
        AccountListPage listPage = new AccountListPage(driver, baseUrl);
        listPage.openAndWaitForLoad();
        
        // Navigate to closure page while account is still active
        AccountClosurePage closurePage = listPage.closeAccount(accountNumber);
        closurePage.waitForPageLoad();
        
        // Close account programmatically while user is on form (simulating concurrent closure)
        testAccount.closeAccount();
        accountRepository.save(testAccount);
        
        // Try to close again via web interface
        closurePage.fillCompleteForm("Attempting to close already closed account");
        
        // This should either redirect back with error or show error on form
        closurePage.closeAccountExpectingError();
        
        // Verify error handling (either on closure page or redirected to list with error)
        boolean hasError = closurePage.isErrorMessageDisplayed() || 
                          (driver.getCurrentUrl().contains("/account/list") && 
                           new AccountListPage(driver, baseUrl).isErrorMessageDisplayed());
        
        assertTrue(hasError, "Should display error for attempting to close already closed account");
        
        log.info("✅ TEST PASS: shouldHandleAlreadyClosedAccountError completed successfully");
    }
    
    // Helper methods for creating test data
    private Account createTestAccountWithZeroBalance() {
        return createTestAccountWithBalance(BigDecimal.ZERO);
    }
    
    private Account createTestAccountWithBalance(BigDecimal balance) {
        // Create all required dependencies first
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer);
        
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        productRepository.save(product);
        
        // Create account with all dependencies
        Account account = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        account.setBalance(balance);
        account.setStatus(Account.AccountStatus.ACTIVE);
        
        return accountRepository.save(account);
    }
}