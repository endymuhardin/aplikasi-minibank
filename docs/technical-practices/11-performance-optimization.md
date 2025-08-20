# Performance Optimization

### 1. Database Performance
```java
// Lazy loading for collections
@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
@JsonIgnore
private List<Account> accounts;

// Strategic indexing
CREATE INDEX idx_transactions_account_date ON transactions(id_accounts, transaction_date);
```

### 2. Repository Query Optimization
```java
// Custom queries for specific needs
@Query("SELECT a FROM Account a JOIN FETCH a.customer JOIN FETCH a.product WHERE a.accountNumber = :accountNumber")
Optional<Account> findByAccountNumberWithDetails(@Param("accountNumber") String accountNumber);
```
