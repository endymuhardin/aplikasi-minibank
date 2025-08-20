# Documentation Standards

### 1. Code Documentation
```java
/**
 * Service for managing sequence number generation.
 * Provides thread-safe sequence generation for business keys like account numbers.
 */
@Service
@Transactional
public class SequenceNumberService {
    
    /**
     * Generates the next sequence number with the specified prefix.
     * 
     * @param sequenceName the name of the sequence
     * @param prefix the prefix to prepend to the number
     * @return formatted sequence string (e.g., "TXN0000001")
     */
    public String generateNextSequence(String sequenceName, String prefix) {
        // Implementation
    }
}
```

### 2. API Documentation Patterns
```java
// RESTful endpoint documentation
/**
 * Register a new personal customer.
 * 
 * @param customer the customer data
 * @param bindingResult validation results
 * @return HTTP 201 with customer data or HTTP 400 with validation errors
 */
@PostMapping("/personal/register")
public ResponseEntity<Object> registerPersonalCustomer(
    @Valid @RequestBody PersonalCustomer customer, 
    BindingResult bindingResult) {
    // Implementation
}
```
