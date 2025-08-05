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
```

### Key Entities
- **Customer**: Supports PERSONAL/CORPORATE types with polymorphic fields
- **Product**: Banking products (SAVINGS, CHECKING, LOAN, etc.) with rich configuration
- **Account**: Links customers to products, contains business methods for deposit/withdrawal
- **Transaction**: Complete transaction recording with audit trail
- **SequenceNumber**: Thread-safe sequence generation for business keys

### Business Logic Location
Entity classes contain business methods (e.g., `Account.deposit()`, `Account.withdraw()`). Always use these methods instead of direct field manipulation.

## Package Structure

```
id.ac.tazkia.minibank/
├── AplikasiMinibankApplication.java
├── controller/
│   ├── ProductController.java          # Web MVC
│   └── rest/CustomerRestController.java # REST API
├── entity/                             # Domain entities with JPA
└── repository/                         # Spring Data JPA repositories
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

## Testing Approach

- **@DataJpaTest**: Repository integration tests with real database
- **@ParameterizedTest + @CsvFileSource**: Data-driven testing using CSV files in `src/test/resources/csv/`
- **Entity Tests**: Unit tests for business logic in entity classes
- **TestEntityManager**: For JPA testing utilities

## Development Workflow

1. **Start database**: `docker compose up -d`
2. **Frontend build**: In `src/main/frontend/`, run `npm run watch` 
3. **Backend**: `mvn spring-boot:run`
4. **Database changes**: Add new migration file in `src/main/resources/db/migration/`

## Important Notes

- Always use entity business methods (e.g., `account.deposit(amount)`) rather than direct field access
- Customer entity has conditional validation based on `customerType` (PERSONAL vs CORPORATE)
- Sequence numbers are generated through `SequenceNumber` entity for consistent formatting
- All entities use UUID primary keys
- Audit fields (created_date, updated_date, etc.) are automatically managed
- REST controllers should include Bean Validation with proper error handling