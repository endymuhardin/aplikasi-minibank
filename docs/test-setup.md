# Schema-Per-Thread Integration Test Architecture

## Overview

This document describes the implemented integration test infrastructure that provides complete schema isolation for parallel test execution using TestContainers PostgreSQL and JUnit 5.

## Architecture Components

### 1. Schema Isolation Strategy
- **PostgreSQL 17** container via TestContainers
- **Unique schema per test thread** using thread name + hash pattern
- **JUnit 5 parallel execution** with 75% dynamic factor
- **Flyway migration** runs in each isolated schema
- **Migration data available** in every test schema for relationships

### 2. Test Infrastructure Classes

#### BaseIntegrationTest (Abstract Base)
- `@BeforeAll` schema creation and Flyway migration
- TestContainers PostgreSQL 17 container management  
- ApplicationContextInitializer integration via TestSchemaInitializer
- JdbcTemplate with schema-specific configuration

#### TestSchemaManager (Utility)
- Thread-safe schema name generation: `test_threadname_hash8chars`
- Centralized schema creation with error handling
- Flyway configuration for schema-targeted migrations
- Connection management for direct database operations

#### TestDataFactory (Data Generation)
- **Thread-safe test data generation** using ThreadLocal Faker
- **Indonesian localization** for realistic banking data
- **AtomicLong counters** for unique business keys (accounts, branches)
- **Thread markers** for precise test data cleanup

#### TestSchemaInitializer (Spring Integration)
- ApplicationContextInitializer for schema configuration
- DataSource URL modification for schema targeting
- Integration with Spring Boot test context

#### ThreadLocalSchemaCustomizer (Connection Management)
- HikariCP connection customization
- Schema-specific connection configuration
- Thread-aware database connection management

### 3. Test Implementation Patterns

#### JDBC-Level Tests (SchemaPerThreadJdbcTemplateTest)
```java
// Features: 8 test methods
- CREATE: Direct SQL operations with JdbcTemplate
- READ: Migration data verification (branches from Flyway)
- UPDATE: Lifecycle data modification with @BeforeEach/@AfterEach
- DELETE: Data removal with proper cleanup
- Constraint validation and schema isolation verification
- Thread marker-based cleanup preserving migration data
```

#### JPA-Level Tests (SchemaPerThreadJpaTest) 
```java
// Features: 7 test methods  
- Repository-based CRUD operations
- Entity relationship testing (Account → Branch, Customer, Product)
- Migration data integration (HO001, C1000001, TAB001)
- JdbcTemplate verification of repository operations
- Business method testing (account.deposit(), account.withdraw())
- Foreign key relationship verification
```

## Configuration

### JUnit 5 Parallel Execution (junit-platform.properties)
```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
junit.jupiter.execution.parallel.config.strategy=dynamic
junit.jupiter.execution.parallel.config.dynamic.factor=0.75
```

### TestContainers Setup
```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
    .withDatabaseName("testdb")
    .withUsername("testuser")  
    .withPassword("testpass")
    .withStartupTimeout(Duration.ofMinutes(5));
```

### Schema Generation Pattern
```
Format: test_threadname_hash8chars
Example: test_forkjoinpool_1_worker_1_dcd693bf
```

## Test Data Management

### Migration Data Usage
Tests leverage Flyway seed data for entity relationships:
- **Branch**: HO001 (Kantor Pusat Jakarta)
- **Customer**: C1000001 (Ahmad Suharto) 
- **Product**: TAB001 (Tabungan Wadiah Basic)

### Test Data Generation
- **Account numbers**: 10-digit starting from 8000000000
- **Indonesian names**: Faker with id_ID locale
- **Thread markers**: TEST_THREADNAME format for cleanup
- **Business keys**: AtomicLong counters ensuring uniqueness

### Cleanup Strategy
- **Thread-marker based**: Only test data removed per thread
- **Migration data preserved**: Flyway seed data remains intact
- **Automatic cleanup**: @AfterEach removes test-specific data

## Test Execution

### Standard Commands
```bash
# Run all integration tests (parallel by default)
mvn test

# Run specific schema-per-thread tests
mvn test -Dtest=SchemaPerThreadJdbcTemplateTest  # 8 tests
mvn test -Dtest=SchemaPerThreadJpaTest          # 7 tests

# Run both test classes 
mvn test -Dtest=SchemaPerThread*

# With test coverage
mvn test jacoco:report
```

### Verification Results
- ✅ **15 total tests** (8 JDBC + 7 JPA) - all passing
- ✅ **Parallel execution** with complete schema isolation
- ✅ **Migration integration** - Flyway data available in all schemas
- ✅ **Thread safety** - No cross-thread data contamination
- ✅ **JPA-JDBC integration** - Repository operations verified via direct SQL

## Key Benefits

### Complete Isolation
1. Each test thread gets isolated PostgreSQL schema
2. Full Flyway migration runs in every schema
3. No data conflicts between parallel tests
4. Migration seed data available for relationships

### Performance Optimized
1. @BeforeAll schema setup - one-time overhead per test class
2. Dynamic thread allocation (75% CPU utilization)
3. Efficient cleanup preserving migration data
4. HikariCP connection pooling with schema awareness

### Production-Like Testing
1. Full database schema with relationships
2. Realistic Indonesian banking test data  
3. Entity business method testing
4. Complete CRUD operation coverage

## Architecture Success Metrics

| Metric | Result |
|--------|--------|
| **Test Isolation** | ✅ 100% - No cross-thread contamination |
| **Parallel Execution** | ✅ All tests pass concurrently |
| **Migration Integration** | ✅ Flyway data available in all schemas |
| **Performance** | ✅ 75% CPU utilization with dynamic threading |
| **Data Integrity** | ✅ Thread-marker cleanup preserves seed data |
| **Framework Integration** | ✅ JPA + JDBC + TestContainers working seamlessly |

---

*This architecture provides a robust foundation for parallel integration testing with complete data isolation, enabling confident parallel test execution without compromising test reliability or data integrity.*