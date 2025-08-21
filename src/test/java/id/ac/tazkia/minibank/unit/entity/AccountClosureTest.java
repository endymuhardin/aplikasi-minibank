package id.ac.tazkia.minibank.unit.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.entity.Product;

class AccountClosureTest {

    private Account account;
    private PersonalCustomer customer;
    private Product product;
    private Branch branch;

    @BeforeEach
    void setUp() {
        branch = new Branch();
        branch.setBranchCode("TST001");
        branch.setBranchName("Test Branch");

        customer = new PersonalCustomer();
        customer.setCustomerNumber("C1000001");
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setBranch(branch);

        product = new Product();
        product.setProductCode("TW001");
        product.setProductName("Tabungan Wadiah");
        product.setProductType(Product.ProductType.TABUNGAN_WADIAH);
        product.setMinimumOpeningBalance(new BigDecimal("100000"));

        account = new Account();
        account.setAccountNumber("ACC1000001");
        account.setAccountName("John Doe Savings");
        account.setCustomer(customer);
        account.setProduct(product);
        account.setBranch(branch);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(Account.AccountStatus.ACTIVE);
    }

    @Test
    void shouldCloseAccountWhenBalanceIsZero() {
        // Given - Account with zero balance
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(Account.AccountStatus.ACTIVE);

        // When
        account.closeAccount();

        // Then
        assertThat(account.getStatus()).isEqualTo(Account.AccountStatus.CLOSED);
        assertThat(account.getClosedDate()).isEqualTo(LocalDate.now());
        assertThat(account.isClosed()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenClosingAccountWithBalance() {
        // Given - Account with non-zero balance
        account.setBalance(new BigDecimal("100000"));
        account.setStatus(Account.AccountStatus.ACTIVE);

        // When & Then
        assertThatThrownBy(() -> account.closeAccount())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Account balance must be zero before closure");

        assertThat(account.getStatus()).isEqualTo(Account.AccountStatus.ACTIVE);
        assertThat(account.getClosedDate()).isNull();
    }

    @Test
    void shouldThrowExceptionWhenClosingAlreadyClosedAccount() {
        // Given - Already closed account
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(Account.AccountStatus.CLOSED);

        // When & Then
        assertThatThrownBy(() -> account.closeAccount())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Account is already closed");
    }

    @Test
    void shouldNotAllowDepositToClosedAccount() {
        // Given - Closed account
        account.setStatus(Account.AccountStatus.CLOSED);

        // When & Then
        assertThatThrownBy(() -> account.deposit(new BigDecimal("10000")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot deposit to a closed account");
    }

    @Test
    void shouldNotAllowWithdrawalFromClosedAccount() {
        // Given - Closed account with balance
        account.setBalance(new BigDecimal("50000"));
        account.setStatus(Account.AccountStatus.CLOSED);

        // When & Then
        assertThatThrownBy(() -> account.withdraw(new BigDecimal("10000")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot withdraw from a closed account");
    }

    @Test
    void shouldAllowClosingInactiveAccountWithZeroBalance() {
        // Given - Inactive account with zero balance
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(Account.AccountStatus.INACTIVE);

        // When
        account.closeAccount();

        // Then
        assertThat(account.getStatus()).isEqualTo(Account.AccountStatus.CLOSED);
        assertThat(account.getClosedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void shouldAllowClosingFrozenAccountWithZeroBalance() {
        // Given - Frozen account with zero balance
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(Account.AccountStatus.FROZEN);

        // When
        account.closeAccount();

        // Then
        assertThat(account.getStatus()).isEqualTo(Account.AccountStatus.CLOSED);
        assertThat(account.getClosedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void shouldNotAllowClosingFrozenAccountWithBalance() {
        // Given - Frozen account with balance
        account.setBalance(new BigDecimal("50000"));
        account.setStatus(Account.AccountStatus.FROZEN);

        // When & Then
        assertThatThrownBy(() -> account.closeAccount())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Account balance must be zero before closure");
    }

    @Test
    void shouldNotAllowClosingInactiveAccountWithBalance() {
        // Given - Inactive account with balance
        account.setBalance(new BigDecimal("25000"));
        account.setStatus(Account.AccountStatus.INACTIVE);

        // When & Then
        assertThatThrownBy(() -> account.closeAccount())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Account balance must be zero before closure");
    }
}