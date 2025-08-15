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
# Run all tests
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

# Run Selenium tests (default: headless mode, no recording)
mvn test -Dtest=ProductManagementSeleniumTest

# Run Selenium tests with visible browser window (for debugging)
mvn test -Dtest=ProductManagementSeleniumTest -Dselenium.headless=false

# Run Selenium tests with recording enabled
mvn test -Dtest=ProductManagementSeleniumTest -Dselenium.recording.enabled=true

# Run Selenium tests with Firefox instead of Chrome
mvn test -Dtest=ProductManagementSeleniumTest -Dselenium.browser=firefox

# Combined options for debugging
mvn test -Dtest=ProductManagementSeleniumTest -Dselenium.headless=false -Dselenium.recording.enabled=true
```

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

### Test Structure
```
src/test/java/id/ac/tazkia/minibank/
├── unit/                               # Pure unit tests
│   └── entity/                         # Entity business logic tests
├── integration/                        # Integration tests with database
│   ├── repository/                     # @DataJpaTest repository tests
│   ├── service/                       # Service layer integration tests
│   └── controller/                    # REST controller tests
└── feature/                           # Feature tests (BDD style)
    ├── customer/registration/         # Customer registration features
    ├── account/opening/               # Account opening features
    ├── transaction/deposit/           # Transaction features
    └── user/                          # User management features
```

### Testing Approaches
- **@DataJpaTest**: Repository integration tests with real database
- **@ParameterizedTest + @CsvFileSource**: Data-driven testing using CSV files in `src/test/resources/fixtures/`
- **Entity Unit Tests**: Pure unit tests for business logic in entity classes
- **Karate BDD Tests**: Feature-driven API integration testing in `src/test/resources/karate/`
- **Spring Boot Test**: Full application context testing with `@SpringBootTest`

### Test Data Management
- **CSV Fixtures**: Test data in `src/test/resources/fixtures/` organized by domain
- **SQL Setup/Cleanup**: Database setup/teardown scripts for integration tests
- **Karate Background**: Shared test data setup using Background sections

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