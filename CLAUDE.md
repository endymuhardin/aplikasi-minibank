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
Account (1) ←→ (N) Transaction
User (N) ←→ (N) Role (N) ←→ (N) Permission
```

### Key Entities
- **Customer**: Supports PERSONAL/CORPORATE types with polymorphic inheritance (PersonalCustomer/CorporateCustomer)
- **Product**: Banking products (SAVINGS, CHECKING, LOAN, etc.) with rich configuration and interest calculation
- **Account**: Links customers to products, contains business methods for deposit/withdrawal operations
- **Transaction**: Complete transaction recording with audit trail, supports multiple channels (TELLER, ATM, etc.)
- **User**: System users with role-based access control (RBAC)
- **Role/Permission**: Fine-grained permission system for different user types (CS, Teller, Branch Manager)
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
- Customer entity has conditional validation based on `customerType` (PERSONAL vs CORPORATE)
- Account operations must validate account status (ACTIVE/INACTIVE) before processing
- Interest rates in Product entity are stored as decimals (0.015 = 1.5%) and must be between 0-1

### Technical Patterns
- All entities use UUID primary keys for security and distributed system compatibility
- Sequence numbers are generated through `SequenceNumberService` for consistent formatting (e.g., TXN0000001)
- Audit fields (created_date, updated_date, etc.) are automatically managed via JPA annotations
- Polymorphic inheritance used for Customer types (PersonalCustomer/CorporateCustomer)

### Validation & Security
- REST controllers use Bean Validation with comprehensive error handling and field-level error mapping
- Security is currently configured as permitAll for development - production requires proper authentication
- RBAC system exists but requires integration with authentication mechanism
- Transaction amounts use BigDecimal for precise financial calculations

### Application Features (from README)
The application supports Indonesian banking operations including:
- Account opening for savings and deposits (tabungan dan deposito)
- Cash transactions (setoran tunai) 
- Passbook printing (cetak buku tabungan)
- Islamic financing (pembiayaan syariah) - Murabahah and Mudharabah
- Role-based access: Customer Service, Teller, Branch Manager (Kepala Cabang)