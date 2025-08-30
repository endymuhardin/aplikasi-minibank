# Testing Strategies

> **Note**: The comprehensive testing documentation has been consolidated into [TESTING.md](../TESTING.md). This document provides a technical overview of testing strategies from an architectural perspective.

## 🧪 **Test Architecture Overview**

### Simplified Testing Strategy

The application implements a straightforward testing strategy using TestContainers and Spring Boot's auto-configuration for reliable, maintainable tests.

#### **Test Infrastructure Architecture**

```
src/test/java/id/ac/tazkia/minibank/
├── config/                             # Test infrastructure configuration
│   ├── BaseIntegrationTest.java        # Base class for database tests
│   ├── BasePlaywrightTest.java         # Base class for functional tests
│   └── TestDataFactory.java            # Test data generation utilities
├── unit/                               # Pure unit tests
├── integration/                        # Database integration tests
├── service/                           # Service layer integration tests
├── functional/                         # End-to-end Playwright tests
│   ├── success/                       # Happy path scenarios
│   ├── alternate/                     # Error and edge case scenarios
│   ├── pages/                         # Page Object Model classes
│   └── config/                        # Playwright configuration
└── feature/                           # Karate BDD API tests
```

## 🏗️ **Core Test Infrastructure Components**

### BaseIntegrationTest
- **Purpose**: Foundation for all database-requiring tests
- **Features**: 
  - PostgreSQL TestContainer integration
  - Automatic Flyway migrations
  - Spring Boot auto-configuration
  - Singleton container pattern for performance

### BasePlaywrightTest
- **Purpose**: Foundation for functional web UI tests
- **Features**:
  - Extends BaseIntegrationTest for database access
  - Playwright browser automation setup
  - Cross-browser support (Chromium, Firefox, WebKit)
  - Headless/headed execution modes


## 🎯 **Testing Strategy Principles**

### 1. **Test Pyramid Architecture**
- **Unit Tests**: Fast, isolated business logic testing
- **Integration Tests**: Database and service layer testing
- **Functional Tests**: End-to-end user workflow testing
- **API Tests**: REST endpoint validation

### 2. **Test Isolation**
- **Database**: Single container with automatic cleanup
- **Browser**: Fresh browser context per test
- **Test Data**: Generated via TestDataFactory utilities

### 3. **Performance Optimization**
- **TestContainers Integration**: Automatic container management via @ServiceConnection
- **Spring Context Caching**: Minimize context restarts
- **Selective Test Execution**: Category-based test filtering

## 📊 **Test Categories and Execution**

### Sequential Execution
All tests execute sequentially for maximum reliability and debugging ease.

### Test Execution Patterns
```bash
# Development workflow - fast feedback
mvn test -Dtest=**/unit/**/*Test

# Integration verification
mvn test -Dtest=**/integration/**/*Test

# Full regression testing
mvn test

# Functional testing specific scenarios
mvn test -Dtest=**/functional/success/*Test
mvn test -Dtest=**/functional/alternate/*Test
```

## 🔧 **Technical Implementation Details**

### Database Testing
- **Technology**: TestContainers + PostgreSQL 17
- **Migration**: Automatic Flyway execution
- **Isolation**: Transaction rollback where appropriate
- **Configuration**: @ServiceConnection for automatic setup

### Functional Testing  
- **Technology**: Playwright + Java
- **Architecture**: Page Object Model
- **Browsers**: Chromium (default), Firefox, WebKit
- **Execution**: Headless (default) or headed for debugging

### API Testing
- **Technology**: Karate BDD framework
- **Format**: Gherkin feature files
- **Validation**: JSON schema and response validation
- **Integration**: REST endpoint testing

## ✅ **Best Practices Implementation**

1. **Reliable Infrastructure**: TestContainers ensures consistent database state
2. **Clear Test Organization**: Separate success/alternate scenario testing
3. **Maintainable Code**: Page Object Model for UI tests
4. **Fast Feedback**: Unit tests provide immediate validation
5. **Comprehensive Coverage**: Multi-layer testing strategy

For detailed testing procedures and examples, refer to [TESTING.md](../TESTING.md).