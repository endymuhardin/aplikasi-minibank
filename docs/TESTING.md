# Testing Guide

This is the master testing documentation for the Aplikasi Minibank Islamic Banking application.

## Table of Contents
1. [Testing Architecture](#testing-architecture)
2. [Test Infrastructure](#test-infrastructure)
3. [Test Categories](#test-categories)
4. [Running Tests](#running-tests)
5. [Test Configuration](#test-configuration)
6. [Best Practices](#best-practices)

## Testing Architecture

The application employs a multi-layered testing strategy:

```
src/test/java/id/ac/tazkia/minibank/
├── unit/                     # Pure unit tests
├── integration/              # Database integration tests
├── service/                  # Service layer tests
├── feature/                  # BDD-style Karate API tests
└── functional/
    ├── success/              # Success scenario tests (happy paths)
    ├── alternate/            # Alternate scenario tests (edge cases, errors)
    ├── pages/                # Page Object Model classes
    └── config/               # Base test configuration
```

## Test Infrastructure

### Core Components

#### BaseIntegrationTest (Abstract Base)
- PostgreSQL TestContainer for database tests
- Automatic Flyway migrations
- Spring Boot test context configuration

#### BasePlaywrightTest (Functional Tests)
- Extends BaseIntegrationTest for database access
- Playwright browser automation setup
- Page Object Model support

### Database Testing
- **PostgreSQL 17** container via TestContainers
- **Singleton container** shared across all tests for performance
- **Automatic Flyway migration** on startup
- **Spring Boot auto-configuration** handles all setup

## Test Categories

### 1. Unit Tests (`src/test/java/.../unit/`)
Pure unit tests focusing on business logic without external dependencies.

**Examples:**
- Entity business method validation
- Utility class functionality
- Domain logic verification

### 2. Integration Tests (`src/test/java/.../integration/`)
Database integration tests using TestContainers.

**Examples:**
- Repository CRUD operations
- Database constraint validation
- Transaction behavior testing

### 3. Service Tests (`src/test/java/.../service/`)
Service layer integration tests.

**Examples:**
- Business service workflow testing
- Service integration validation
- PDF generation testing

### 4. Functional Tests (`src/test/java/.../functional/`)
End-to-end web UI testing using Playwright.

**Structure:**
- `success/` - Happy path scenarios
- `alternate/` - Error handling and edge cases  
- `pages/` - Page Object Model classes
- `config/` - Base Playwright configuration

**Browser Support:**
- Chromium (default)
- Firefox (`-Dplaywright.browser=firefox`)
- WebKit (`-Dplaywright.browser=webkit`)

**Execution Modes:**
- Headless (default): `mvn test -Dtest=**/functional/**/*Test`
- Headed: `mvn test -Dtest=**/functional/**/*Test -Dplaywright.headless=false`

### 5. Feature Tests (`src/test/java/.../feature/`)
API integration tests using Karate BDD framework.

**Examples:**
- REST API endpoint testing
- JSON payload validation
- API workflow testing

## Running Tests

### All Tests
```bash
mvn test
```

### Specific Test Categories
```bash
# Unit tests only
mvn test -Dtest=**/unit/**/*Test

# Integration tests only  
mvn test -Dtest=**/integration/**/*Test

# Service tests only
mvn test -Dtest=**/service/**/*Test

# Functional tests (Playwright)
mvn test -Dtest=**/functional/**/*Test

# Functional success scenarios only
mvn test -Dtest=**/functional/success/*Test

# Functional alternate scenarios only (negative tests)
mvn test -Dtest=**/functional/alternate/*Test

# Feature tests (Karate)
mvn test -Dtest=**/feature/**/*Test
```

### Specific Test Classes
```bash
# Run specific test class
mvn test -Dtest=AuthenticationSuccessTest

# Run specific test method
mvn test -Dtest=AuthenticationSuccessTest#shouldDisplayLoginPageWithRequiredElements
```

### Test Coverage
```bash
# Generate coverage report
mvn test jacoco:report

# View report: target/site/jacoco/index.html
```

## Test Configuration

### Application Configuration
Tests use `application-test.properties` with minimal configuration:
- Bank configuration (required by application)
- TestContainers handles database configuration automatically

### Browser Configuration (Playwright)
- **Default**: Headless Chromium
- **Browser**: `-Dplaywright.browser=firefox|webkit|chromium`
- **Headless**: `-Dplaywright.headless=false` for visible browser

## Best Practices

### Database Tests
1. **Use TestContainers**: Let Spring Boot handle all database configuration
2. **Extend BaseIntegrationTest**: Inherit proper database setup
3. **Use @Transactional**: Ensure test isolation when needed
4. **Test with real data**: Flyway migrations provide realistic test data

### Functional Tests
1. **Use Page Object Model**: Maintain page classes in `functional/pages/`
2. **ID-based selectors**: Always use element IDs for stability
3. **Proper waits**: Use Playwright's built-in wait conditions
4. **Separate success/alternate**: Organize tests by scenario type

### API Tests
1. **Karate for APIs**: Use feature files for API endpoint testing
2. **JSON validation**: Validate both structure and data
3. **Error scenarios**: Test error responses and status codes

### General
1. **Descriptive test names**: Use clear, business-focused test method names
2. **Single responsibility**: Each test should verify one specific behavior
3. **Independent tests**: Tests should not depend on each other
4. **Clean test data**: Use TestDataFactory for consistent test data generation

## Test Scenarios Documentation

Detailed test scenarios are documented in the `docs/test-scenarios/` directory, organized by functional area:

- **Account Management**: Account opening, lifecycle management
- **Customer Management**: Personal and corporate customer operations
- **Product Management**: Islamic banking product administration
- **Transactions**: Deposits, withdrawals, transfers
- **RBAC**: User, role, and permission management
- **Reporting**: Account statements and reports
- **Passbook**: Passbook printing operations

See [test-scenarios/README.md](test-scenarios/README.md) for complete scenario documentation.