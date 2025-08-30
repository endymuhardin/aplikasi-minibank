# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Common Development Commands

### Build and Run
```bash
# Start database (required first)
docker compose up -d

# Build and run application
mvn spring-boot:run

# Build frontend assets (in separate terminal)
cd src/main/frontend
npm install
npm run watch

# Access application
# Web UI: http://localhost:8080/product/list
# REST API: http://localhost:8080/api/customers/*
```

### Testing
```bash
# Run all tests (sequential by default)
mvn test

# Run tests with coverage report
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=AccountRepositoryTest

# Run specific test method
mvn test -Dtest=AccountRepositoryTest#shouldFindByCustomerId

# Run specific feature test (Karate)
mvn test -Dtest=DepositTest

# Run single Karate test method
mvn test -Dtest=DepositTest#testDeposit

# Run functional tests (all scenarios - success and alternate)
mvn test -Dtest=**/functional/**/*Test

# Run functional success scenario tests only
mvn test -Dtest=**/functional/success/*Test

# Run functional alternate scenario tests only (negative cases)
mvn test -Dtest=**/functional/alternate/*Test

# Run specific functional test class
mvn test -Dtest=**/functional/**/ProductManagementSuccessTest

# Run functional tests with visible browser window (for debugging)
mvn test -Dtest=**/functional/**/*Test -Dplaywright.headless=false

# Run functional tests with Firefox instead of Chromium
mvn test -Dtest=**/functional/**/*Test -Dplaywright.browser=firefox

# Run functional tests with WebKit (Safari engine)
mvn test -Dtest=**/functional/**/*Test -Dplaywright.browser=webkit

# Combined options for functional debugging
mvn test -Dtest=**/functional/success/*Test -Dplaywright.headless=false -Dplaywright.browser=chromium

# Test Execution Profiles
# Sequential execution (default - single threaded, easier debugging)
mvn test

# Parallel execution (2 threads at class level, faster execution)
mvn test -Dtest.profile=parallel
```

## Test Configuration

### Test Execution Profiles

The application uses **2 simplified test profiles**:

| Profile | Default | Usage | Execution | Purpose |
|---------|---------|-------|-----------|---------|
| **sequential** | ✅ Yes | `mvn test` | Single-threaded | Debugging, stability |
| **parallel** | ❌ No | `mvn test -Dtest.profile=parallel` | 2 threads (classes) | Faster execution |

### Functional Test Configuration

| Setting | Default | Override Example |
|---------|---------|------------------|
| **Browser** | Chromium | `-Dplaywright.browser=firefox` |
| **Headless** | true | `-Dplaywright.headless=false` |
| **Test Pattern** | All | `-Dtest=**/functional/success/*Test` |

### Functional Test Types

| Type | Purpose | Command |
|------|---------|---------|
| **functional** | All scenarios (success + alternate) | `mvn test -Dtest=**/functional/**/*Test` |
| **functional-success** | Success scenarios (happy paths) | `mvn test -Dtest=**/functional/success/*Test` |
| **functional-alternate** | Alternate scenarios (edge cases, errors) | `mvn test -Dtest=**/functional/alternate/*Test` |

### Database Operations
```bash
# Start PostgreSQL container
docker compose up postgres

# Connect to database directly
docker exec -it aplikasi-minibank-postgres-1 psql -U minibank -d pgminibank

# Reset database (stops container and removes volume)
docker compose down -v
```

## Architecture Overview

This is a **Spring Boot minibank application** using layered architecture with:

- **Presentation Layer**: Thymeleaf web controllers + REST API endpoints
- **Business Layer**: Rich domain entities with business logic
- **Data Access Layer**: Spring Data JPA repositories  
- **Database Layer**: PostgreSQL with Flyway migrations

### Key Architectural Patterns
- **Repository Pattern**: All data access through Spring Data JPA
- **Entity-Based Domain Modeling**: Business logic in entity classes
- **Dependency Injection**: Spring IoC for all components

## Core Domain Model

### Entity Relationships
```
Customer (1) ←→ (N) Account (N) ←→ (1) Product
Customer ||--|| PersonalCustomer/CorporateCustomer (joined table inheritance)
Account (1) ←→ (N) Transaction
User (N) ←→ (N) Role (N) ←→ (N) Permission
User (1) ←→ (1) UserPassword
```

### Key Entities
- **Customer**: Base table with PERSONAL/CORPORATE types using joined table inheritance (PersonalCustomer/CorporateCustomer)
- **Product**: Islamic banking products (TABUNGAN_WADIAH, TABUNGAN_MUDHARABAH, DEPOSITO_MUDHARABAH, PEMBIAYAAN_*) with rich configuration and profit sharing
- **Account**: Links customers to products, contains business methods for deposit/withdrawal operations
- **Transaction**: Complete transaction recording with audit trail, supports multiple channels (TELLER, ATM, ONLINE, MOBILE, TRANSFER)
- **User**: System users with role-based access control (RBAC)
- **UserPassword**: Separate table for password storage with BCrypt hashing
- **Role/Permission**: Fine-grained permission system for different user types (CUSTOMER_SERVICE, TELLER, BRANCH_MANAGER)
- **SequenceNumber**: Thread-safe sequence generation for business keys (account numbers, transaction numbers)

### Business Logic Location
Entity classes contain business methods (e.g., `Account.deposit()`, `Account.withdraw()`). Always use these methods instead of direct field manipulation.

## Package Structure

```
id.ac.tazkia.minibank/
├── AplikasiMinibankApplication.java
├── config/
│   └── SecurityConfig.java             # Spring Security configuration
├── controller/
│   ├── ProductController.java          # Thymeleaf web MVC
│   └── rest/                           # REST API endpoints
│       ├── AccountRestController.java  # Account operations
│       ├── CustomerRestController.java # Customer CRUD
│       ├── TransactionRestController.java # Financial transactions
│       └── UserRestController.java     # User management & RBAC
├── dto/                               # Data Transfer Objects
├── entity/                            # JPA entities with business logic
├── repository/                        # Spring Data JPA repositories
└── service/                          # Business services
    └── SequenceNumberService.java    # Thread-safe sequence generation
```

## Database Configuration

- **URL**: `jdbc:postgresql://localhost:2345/pgminibank`
- **Credentials**: minibank/minibank1234
- **Migrations**: Flyway-managed in `src/main/resources/db/migration/`
- **Test DB**: Uses same database as main application

## Technology Stack

- **Spring Boot 3.5.3** (Java 21)
- **PostgreSQL 17** (Docker)
- **Spring Data JPA** + Hibernate
- **Thymeleaf** + Tailwind CSS
- **Bean Validation** (jakarta.validation)
- **Flyway** for database migrations
- **JaCoCo** for test coverage
- **Karate** for API integration testing

## Testing Architecture

> **📖 For comprehensive testing documentation, see [docs/TESTING.md](docs/TESTING.md)**

### Test Structure
```
src/test/java/id/ac/tazkia/minibank/
├── unit/                               # Pure unit tests
│   └── entity/                         # Entity business logic tests
├── integration/                        # Integration tests with database
│   ├── repository/                     # @DataJpaTest repository tests
│   ├── service/                       # Service layer integration tests
│   └── controller/                    # REST controller tests
├── feature/                           # Feature tests (BDD style)
│   ├── customer/registration/         # Customer registration features
│   ├── account/opening/               # Account opening features
│   ├── transaction/deposit/           # Transaction features
│   └── user/                          # User management features
└── functional/                        # Functional E2E tests using Playwright (separate structure)
    ├── success/                       # Success scenario tests (happy paths)
    ├── alternate/                     # Alternate scenario tests (edge cases, errors)
    ├── pages/                         # Page Object Model classes
    └── config/                        # Base test configuration
```

### Testing Approaches
- **Schema-per-thread isolation**: Each test thread gets unique PostgreSQL schema
- **@DataJpaTest**: Repository integration tests with TestContainers
- **@ParameterizedTest + @CsvFileSource**: Data-driven testing using CSV files
- **Functional E2E Tests**: Modern browser automation using Playwright with Page Object Model
  - **Success Scenarios**: Happy path testing with positive flows
  - **Alternate Scenarios**: Edge cases, error handling, and security testing
  - **Cross-browser Support**: Chromium, Firefox, WebKit (Safari engine)
  - **ID-based Element Selection**: Uses element IDs exclusively for stability
  - **No Thread Sleep**: Uses proper wait conditions for reliable tests
- **Karate BDD Tests**: Feature-driven API integration testing
- **Spring Boot Test**: Full application context testing

## Development Workflow

1. **Start database**: `docker compose up -d`
2. **Frontend build**: In `src/main/frontend/`, run `npm run watch` 
3. **Backend**: `mvn spring-boot:run`
4. **Database changes**: Add new migration file in `src/main/resources/db/migration/`

## Important Notes

### Business Rules
- Always use entity business methods (e.g., `account.deposit(amount)`) rather than direct field access
- Customer entity uses joined table inheritance: base customers table + personal_customers/corporate_customers
- Account operations must validate account status (ACTIVE/INACTIVE/CLOSED/FROZEN) before processing
- Islamic banking products use profit sharing ratios and nisbah (customer/bank split) instead of interest rates
- Product constraints: nisbah_customer + nisbah_bank = 1.0 for MUDHARABAH/MUSHARAKAH products
- All amounts use DECIMAL(20,2) for precise financial calculations

### Technical Patterns
- All entities use UUID primary keys for security and distributed system compatibility
- Sequence numbers are generated through `SequenceNumberService` for consistent formatting (e.g., TXN0000001)
- Audit fields (created_date, updated_date, etc.) are automatically managed via JPA annotations
- Joined table inheritance used for Customer types (PersonalCustomer/CorporateCustomer)
- Password storage uses separate user_passwords table with BCrypt hashing

### Validation & Security
- REST controllers use Bean Validation with comprehensive error handling and field-level error mapping
- Security is currently configured as permitAll for development - production requires proper authentication
- Full RBAC system implemented with users, roles, permissions, and role_permissions tables
- Password security with BCrypt hashing, account locking, and failed login attempt tracking
- Sample users with password 'minibank123': admin, manager1-2, teller1-3, cs1-3
- Transaction amounts use BigDecimal for precise financial calculations

### Application Features (from README)
The application supports Indonesian Islamic banking operations including:
- Account opening for Islamic savings (Tabungan Wadiah, Tabungan Mudharabah)
- Islamic deposits (Deposito Mudharabah)
- Cash transactions (setoran tunai) with multiple channels
- Passbook printing (cetak buku tabungan)
- Islamic financing (pembiayaan syariah): Murabahah, Mudharabah, Musharakah, Ijarah, Salam, Istisna
- Role-based access: Customer Service (CS), Teller, Branch Manager (Kepala Cabang)
- Comprehensive RBAC with granular permissions