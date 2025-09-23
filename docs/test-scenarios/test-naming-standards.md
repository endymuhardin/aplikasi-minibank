# Test Naming Standards and @DisplayName Conventions

## Overview
This document defines the standard format for test method names and @DisplayName annotations to ensure consistent bidirectional tracking between test scenarios and Playwright test implementations.

## @DisplayName Format

### Standard Pattern
```java
@DisplayName("[SCENARIO-ID] Brief description of what the test does")
```

### Format Components
1. **Scenario ID**: Enclosed in square brackets `[CS-S-001-01]`
2. **Space**: Single space after closing bracket
3. **Description**: Brief, descriptive text starting with capital letter

### Examples

#### ✅ Correct Format
```java
@DisplayName("[CS-S-001-01] Should successfully create personal customers")
@DisplayName("[TL-S-001-05] Should process high-value corporate deposits")
@DisplayName("[BM-A-001-02] Should reject duplicate product codes")
@DisplayName("[SA-S-001-03] Should assign multiple roles to user")
```

#### ❌ Incorrect Format
```java
@DisplayName("CS-S-001-01: Should create personal customers")    // Wrong: colon instead of brackets
@DisplayName("[CS-S-001-01]Should create customers")            // Wrong: missing space
@DisplayName("[cs-s-001-01] should create customers")           // Wrong: lowercase
@DisplayName("Should successfully create personal customers")    // Wrong: missing scenario ID
```

## Scenario ID Components

### Role Prefixes
| Role | Prefix | Description |
|------|---------|-------------|
| Customer Service | `CS` | Account opening, customer management |
| Teller | `TL` | Transactions, cash operations |
| Branch Manager | `BM` | Product management, reports, approvals |
| Customer Mobile | `CM` | Mobile banking (future) |
| System Admin | `SA` | User management, system configuration |

### Scenario Types
| Type | Code | Description |
|------|------|-------------|
| Success | `S` | Happy path scenarios |
| Alternate | `A` | Error cases, validation failures |

### Number Format
- **Major Number**: 001, 002, 003 (group of related scenarios)
- **Minor Number**: 01, 02, 03 (specific test cases within a group)

### Complete Format: `[ROLE-TYPE-MAJOR-MINOR]`
- `CS-S-001-01`: Customer Service, Success, Group 1, Test 1
- `TL-A-002-05`: Teller, Alternate, Group 2, Test 5
- `BM-S-001-03`: Branch Manager, Success, Group 1, Test 3

## Mapping to Documentation

### File Path Convention
Scenario IDs map to specific documentation files:

```
[CS-S-001-XX] → docs/test-scenarios/01-customer-service/success/CS-S-001-customer-registration.md
[TL-S-001-XX] → docs/test-scenarios/02-teller/success/TL-S-001-cash-deposit.md
[BM-A-001-XX] → docs/test-scenarios/03-branch-manager/alternate/BM-A-001-product-validation-errors.md
```

### Cross-Reference Format
Documentation files should include implementation status:

```markdown
## Implementation Status
**Overall Status**: ✅ IMPLEMENTED (8/8 sub-scenarios)
**Test Class**: `CustomerManagementSuccessTest`
**Test Methods**:
- `[CS-S-001-01]` shouldCreatePersonalCustomerSuccessfully()
- `[CS-S-001-02]` shouldCreateCorporateCustomerSuccessfully()
- `[CS-S-001-03]` shouldSearchCustomerSuccessfully()
```

## Test Class Naming

### Naming Convention
```java
[Module][Role][Type]Test.java

Examples:
CustomerManagementSuccessTest.java
ProductManagementAlternateTest.java
TransactionSuccessTest.java
AuthenticationAlternateTest.java
```

### Class-Level @DisplayName
```java
@DisplayName("Customer Service Success Scenarios - Customer Registration [CS-S-001]")
@DisplayName("Branch Manager Alternate Scenarios - Product Validation [BM-A-001]")
@DisplayName("Teller Success Scenarios - Cash Deposits [TL-S-001]")
```

## Benefits of This Approach

### 1. **Test Report Visibility**
Scenario IDs appear in:
- JUnit HTML reports
- Maven Surefire reports
- IDE test runners
- CI/CD pipeline outputs
- Console test execution logs

### 2. **Easy Navigation**
- From test failure → Find documentation quickly
- From scenario docs → Find implementing test method
- IDE search: Type `[CS-S-001-01]` to find test

### 3. **Progress Tracking**
- Clear identification of implemented vs missing scenarios
- Easy to generate test coverage reports
- Sprint planning with specific scenario IDs

### 4. **Maintenance**
- Refactoring tests while maintaining scenario links
- Adding new scenarios with predictable numbering
- Code reviews can verify scenario coverage

## Implementation Examples

### Customer Management Test
```java
@Test
@DisplayName("[CS-S-001-01] Should successfully create personal customers")
void shouldCreatePersonalCustomerSuccessfully() {
    // Maps to: docs/test-scenarios/01-customer-service/success/CS-S-001-customer-registration.md
    // Sub-scenario: CS-S-001-01 - Create Personal Customer
}

@Test
@DisplayName("[CS-S-001-03] Should successfully search customers")
void shouldSearchCustomerSuccessfully() {
    // Maps to: docs/test-scenarios/01-customer-service/success/CS-S-001-customer-registration.md
    // Sub-scenario: CS-S-001-03 - Search Customers
}
```

### Product Management Test
```java
@Test
@DisplayName("[BM-S-001-01] Should display product list with essential elements")
void shouldDisplayProductListWithEssentialElements() {
    // Maps to: docs/test-scenarios/03-branch-manager/success/BM-S-001-product-management.md
    // Sub-scenario: BM-S-001-01 - Display Product List
}

@Test
@DisplayName("[BM-A-001-02] Should reject duplicate product codes")
void shouldHandleDuplicateProductCode() {
    // Maps to: docs/test-scenarios/03-branch-manager/alternate/BM-A-001-product-validation-errors.md
    // Sub-scenario: BM-A-001-02 - Duplicate Product Code
}
```

## Migration Guide

### Updating Existing Tests
1. **Identify Scenario**: Determine which scenario the test implements
2. **Add Scenario ID**: Update @DisplayName with scenario ID
3. **Update Documentation**: Add implementation status to scenario docs
4. **Verify Mapping**: Ensure bidirectional links work

### Example Migration
```java
// Before
@DisplayName("Should successfully create customers")

// After
@DisplayName("[CS-S-001-01] Should successfully create personal customers")
```

## Validation Rules

### Required Elements
- ✅ Scenario ID in square brackets
- ✅ Space after closing bracket
- ✅ Descriptive text starting with capital
- ✅ Valid role prefix (CS, TL, BM, CM, SA)
- ✅ Valid type code (S, A)
- ✅ Proper number format (XXX-XX)

### Forbidden Elements
- ❌ Colons instead of brackets
- ❌ Missing spaces
- ❌ Lowercase scenario IDs
- ❌ Invalid role/type codes
- ❌ Wrong number formats

## Automation Support

### IDE Integration
Most IDEs can search by scenario ID:
1. IntelliJ IDEA: Ctrl+Shift+F → `[CS-S-001-01]`
2. VS Code: Ctrl+Shift+F → `[CS-S-001-01]`
3. Eclipse: Ctrl+H → `[CS-S-001-01]`

### Report Generation
Test reports can filter/group by scenario patterns:
- All Customer Service tests: `[CS-*`
- All Success scenarios: `*-S-*`
- Specific scenario group: `[CS-S-001-*`

### CI/CD Integration
Pipeline scripts can parse scenario IDs from test names for:
- Coverage reporting
- Failure analysis
- Progress tracking
- Release validation

## Future Enhancements

### Planned Features
1. **Automated Validation**: Script to verify scenario ID format compliance
2. **Coverage Reports**: Auto-generate scenario implementation status
3. **Documentation Sync**: Keep scenario docs and test implementations in sync
4. **Tagging Strategy**: Additional JUnit tags for filtering by scenario type

### Integration Points
- SonarQube test analysis
- GitHub Actions test reporting
- Slack notifications with scenario IDs
- JIRA ticket linking with scenario references