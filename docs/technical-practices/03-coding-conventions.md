# Coding Conventions

### Entity Design Patterns

#### 1. UUID Primary Keys
```java
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
}
```

#### 2. Audit Fields Pattern
```java
// Standard audit fields in all entities
@CreationTimestamp
@Column(name = "created_date", updatable = false)
private LocalDateTime createdDate;

@Column(name = "created_by", length = 100)
private String createdBy;

@UpdateTimestamp
@Column(name = "updated_date")
private LocalDateTime updatedDate;

@Column(name = "updated_by", length = 100)
private String updatedBy;
```

#### 3. Joined Table Inheritance
```java
@Entity
@Table(name = "customers")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "customer_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Customer {
    // Base fields
    public abstract CustomerType getCustomerType();
    public abstract String getDisplayName();
}

@Entity
@Table(name = "personal_customers")
public class PersonalCustomer extends Customer {
    // Personal-specific fields
}
```

#### 4. Business Logic in Entities
```java
@Entity
public class Account {
    // Entity fields...
    
    // Business methods
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }
    
    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.balance = this.balance.subtract(amount);
    }
}
```

#### 5. Enum Usage Patterns
```java
@Enumerated(EnumType.STRING)
@Column(name = "status", length = 20)
private AccountStatus status = AccountStatus.ACTIVE;

public enum AccountStatus {
    ACTIVE, INACTIVE, CLOSED, FROZEN
}
```

### Validation Patterns

#### 1. Bean Validation Annotations
```java
@NotBlank(message = "Customer number is required")
@Size(max = 50, message = "Customer number must not exceed 50 characters")
@Column(name = "customer_number", unique = true, nullable = false, length = 50)
private String customerNumber;

@NotBlank(message = "Email is required")
@Email(message = "Email should be valid")
@Size(max = 100, message = "Email must not exceed 100 characters")
@Column(name = "email", length = 100)
private String email;
```

#### 2. Custom Validation Messages
```java
// Use descriptive, user-friendly validation messages
@Size(max = 100, message = "Company name must not exceed 100 characters")
@NotNull(message = "Date of birth is required")
@Past(message = "Date of birth must be in the past")
```

### Controller Patterns

#### 1. REST Controller Error Handling
```java
@PostMapping("/personal/register")
public ResponseEntity<Object> registerPersonalCustomer(
        @Valid @RequestBody PersonalCustomer customer, 
        BindingResult bindingResult) {
    
    if (bindingResult.hasErrors()) {
        Map<String, String> errors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }
    
    PersonalCustomer savedCustomer = personalCustomerRepository.save(customer);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
}
```

#### 2. Optional Pattern for Not Found
```java
@GetMapping("/personal/{id}")
public ResponseEntity<PersonalCustomer> getPersonalCustomer(@PathVariable UUID id) {
    return personalCustomerRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
```

#### 3. Search Parameter Handling
```java
@GetMapping("/personal")
public ResponseEntity<List<PersonalCustomer>> getAllPersonalCustomers(
        @RequestParam(required = false) String search) {
    List<PersonalCustomer> customers;
    if (search != null && !search.trim().isEmpty()) {
        customers = personalCustomerRepository.findPersonalCustomersWithSearchTerm(search.trim());
    } else {
        customers = personalCustomerRepository.findAll();
    }
    return ResponseEntity.ok(customers);
}
```

### Service Layer Patterns

#### 1. Transactional Service Methods
```java
@Service
@Transactional
public class SequenceNumberService {
    
    private final SequenceNumberRepository sequenceNumberRepository;
    
    public SequenceNumberService(SequenceNumberRepository sequenceNumberRepository) {
        this.sequenceNumberRepository = sequenceNumberRepository;
    }
    
    public String generateNextSequence(String sequenceName, String prefix) {
        SequenceNumber sequence = getOrCreateSequence(sequenceName, prefix);
        String result = sequence.generateNextSequence();
        sequenceNumberRepository.save(sequence);
        return result;
    }
}
```

#### 2. Constructor Injection
```java
// Always use constructor injection, not field injection
public CustomerRestController(PersonalCustomerRepository personalCustomerRepository,
                            CorporateCustomerRepository corporateCustomerRepository) {
    this.personalCustomerRepository = personalCustomerRepository;
    this.corporateCustomerRepository = corporateCustomerRepository;
}
```

### Financial Data Handling

#### 1. BigDecimal for Money
```java
// Always use BigDecimal for financial amounts
@Column(name = "balance", precision = 20, scale = 2)
private BigDecimal balance = BigDecimal.ZERO;

// Proper BigDecimal comparison
if (this.balance.compareTo(amount) < 0) {
    throw new IllegalArgumentException("Insufficient balance");
}
```

#### 2. Precision and Scale Standards
```java
// Standard precision and scale for financial fields
private BigDecimal amount;           // DECIMAL(20,2) - amounts
private BigDecimal profitRatio;      // DECIMAL(5,4) - ratios/percentages
private BigDecimal profitSharingRatio; // DECIMAL(5,4) - profit sharing ratios
```
