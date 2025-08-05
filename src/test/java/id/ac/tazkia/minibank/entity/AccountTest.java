package id.ac.tazkia.minibank.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private Account account;
    private Customer customer;
    private Product product;

    @BeforeEach
    void setUp() {
        // Setup test customer
        customer = new Customer();
        customer.setCustomerType(Customer.CustomerType.PERSONAL);
        customer.setCustomerNumber("C1000001");
        customer.setFirstName("John");
        customer.setLastName("Doe");

        // Setup test product
        product = new Product();
        product.setProductCode("SAV001");
        product.setProductName("Basic Savings");
        product.setProductType(Product.ProductType.SAVINGS);
        product.setMinimumBalance(new BigDecimal("10000"));

        // Setup test account
        account = new Account();
        account.setCustomer(customer);
        account.setProduct(product);
        account.setAccountNumber("A2000001");
        account.setAccountName("John Doe - Savings");
        account.setBalance(new BigDecimal("100000"));
        account.setStatus(Account.AccountStatus.ACTIVE);
        account.setOpenedDate(LocalDate.now());
        account.setCreatedBy("TEST");
    }

    @Test
    void shouldCreateAccountWithDefaultValues() {
        // Given
        Account newAccount = new Account();

        // Then
        assertThat(newAccount.getBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(newAccount.getStatus()).isEqualTo(Account.AccountStatus.ACTIVE);
        assertThat(newAccount.getOpenedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void shouldDepositPositiveAmount() {
        // Given
        BigDecimal initialBalance = account.getBalance();
        BigDecimal depositAmount = new BigDecimal("50000");

        // When
        account.deposit(depositAmount);

        // Then
        assertThat(account.getBalance()).isEqualTo(initialBalance.add(depositAmount));
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("150000"));
    }

    @Test
    void shouldThrowExceptionWhenDepositingZeroAmount() {
        // Given
        BigDecimal zeroAmount = BigDecimal.ZERO;

        // When & Then
        assertThatThrownBy(() -> account.deposit(zeroAmount))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Deposit amount must be positive");
    }

    @Test
    void shouldThrowExceptionWhenDepositingNegativeAmount() {
        // Given
        BigDecimal negativeAmount = new BigDecimal("-1000");

        // When & Then
        assertThatThrownBy(() -> account.deposit(negativeAmount))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Deposit amount must be positive");
    }

    @Test
    void shouldWithdrawValidAmount() {
        // Given
        BigDecimal initialBalance = account.getBalance();
        BigDecimal withdrawalAmount = new BigDecimal("30000");

        // When
        account.withdraw(withdrawalAmount);

        // Then
        assertThat(account.getBalance()).isEqualTo(initialBalance.subtract(withdrawalAmount));
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("70000"));
    }

    @Test
    void shouldThrowExceptionWhenWithdrawingZeroAmount() {
        // Given
        BigDecimal zeroAmount = BigDecimal.ZERO;

        // When & Then
        assertThatThrownBy(() -> account.withdraw(zeroAmount))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Withdrawal amount must be positive");
    }

    @Test
    void shouldThrowExceptionWhenWithdrawingNegativeAmount() {
        // Given
        BigDecimal negativeAmount = new BigDecimal("-1000");

        // When & Then
        assertThatThrownBy(() -> account.withdraw(negativeAmount))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Withdrawal amount must be positive");
    }

    @Test
    void shouldThrowExceptionWhenWithdrawingMoreThanBalance() {
        // Given
        BigDecimal excessiveAmount = new BigDecimal("200000"); // More than current balance of 100000

        // When & Then
        assertThatThrownBy(() -> account.withdraw(excessiveAmount))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Insufficient balance");
    }

    @Test
    void shouldAllowWithdrawalOfExactBalance() {
        // Given
        BigDecimal exactBalance = account.getBalance(); // 100000

        // When
        account.withdraw(exactBalance);

        // Then
        assertThat(account.getBalance()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnTrueWhenAccountIsActive() {
        // Given
        account.setStatus(Account.AccountStatus.ACTIVE);

        // When & Then
        assertThat(account.isActive()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenAccountIsNotActive() {
        // Given & When & Then
        account.setStatus(Account.AccountStatus.INACTIVE);
        assertThat(account.isActive()).isFalse();

        account.setStatus(Account.AccountStatus.CLOSED);
        assertThat(account.isActive()).isFalse();

        account.setStatus(Account.AccountStatus.FROZEN);
        assertThat(account.isActive()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenAccountIsClosed() {
        // Given
        account.setStatus(Account.AccountStatus.CLOSED);

        // When & Then
        assertThat(account.isClosed()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenAccountIsNotClosed() {
        // Given & When & Then
        account.setStatus(Account.AccountStatus.ACTIVE);
        assertThat(account.isClosed()).isFalse();

        account.setStatus(Account.AccountStatus.INACTIVE);
        assertThat(account.isClosed()).isFalse();

        account.setStatus(Account.AccountStatus.FROZEN);
        assertThat(account.isClosed()).isFalse();
    }

    @Test
    void shouldHandleMultipleDepositsAndWithdrawals() {
        // Given
        BigDecimal initialBalance = new BigDecimal("50000");
        account.setBalance(initialBalance);

        // When
        account.deposit(new BigDecimal("25000"));  // Balance: 75000
        account.withdraw(new BigDecimal("15000")); // Balance: 60000
        account.deposit(new BigDecimal("10000"));  // Balance: 70000
        account.withdraw(new BigDecimal("20000")); // Balance: 50000

        // Then
        assertThat(account.getBalance()).isEqualByComparingTo("50000");
    }

    @Test
    void shouldHandleDecimalAmounts() {
        // Given
        account.setBalance(new BigDecimal("1000.50"));

        // When
        account.deposit(new BigDecimal("250.25"));
        account.withdraw(new BigDecimal("100.75"));

        // Then
        assertThat(account.getBalance()).isEqualByComparingTo("1150.00");
    }

    @Test
    void shouldMaintainBalancePrecision() {
        // Given
        account.setBalance(new BigDecimal("999.99"));

        // When
        account.deposit(new BigDecimal("0.01"));

        // Then
        assertThat(account.getBalance()).isEqualByComparingTo("1000.00");
        assertThat(account.getBalance().scale()).isEqualTo(2);
    }

    @Test
    void shouldTestAccountStatusEnum() {
        // Test all enum values
        assertThat(Account.AccountStatus.valueOf("ACTIVE")).isEqualTo(Account.AccountStatus.ACTIVE);
        assertThat(Account.AccountStatus.valueOf("INACTIVE")).isEqualTo(Account.AccountStatus.INACTIVE);
        assertThat(Account.AccountStatus.valueOf("CLOSED")).isEqualTo(Account.AccountStatus.CLOSED);
        assertThat(Account.AccountStatus.valueOf("FROZEN")).isEqualTo(Account.AccountStatus.FROZEN);
        
        // Test enum values array
        Account.AccountStatus[] statuses = Account.AccountStatus.values();
        assertThat(statuses).hasSize(4);
        assertThat(statuses).containsExactly(
            Account.AccountStatus.ACTIVE,
            Account.AccountStatus.INACTIVE, 
            Account.AccountStatus.CLOSED,
            Account.AccountStatus.FROZEN
        );
    }
}