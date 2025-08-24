# Build and Deployment Practices

### 1. Maven Build Lifecycle
```bash
# Standard development commands
mvn clean compile                    # Compile source code
mvn test                            # Run all tests
mvn test jacoco:report              # Generate coverage report
mvn spring-boot:run                 # Run application

# Test execution patterns
mvn test -Dtest=AccountRepositoryTest                          # Single test class
mvn test -Dtest=AccountRepositoryTest#shouldFindByCustomerId   # Single test method
mvn test -Dtest="SchemaPerThread*"                            # Schema isolation tests
```

### 2. Frontend Build Process
```bash
# Frontend asset building
cd src/main/frontend
npm install                         # Install dependencies
npm run build                       # One-time build
npm run watch                       # Watch mode for development
```

### 3. Database Management
```bash
# Development database operations
docker compose up -d               # Start PostgreSQL
docker compose down -v             # Reset database (removes volume)
docker exec -it aplikasi-minibank-postgres-1 psql -U minibank -d pgminibank
```

### 4. Integration Testing
```bash
# Schema-per-thread integration tests
mvn test -Dtest=SchemaPerThreadJdbcTemplateTest    # JDBC-level database tests (8 tests)
mvn test -Dtest=SchemaPerThreadJpaTest            # JPA-level entity tests (7 tests)
mvn test -Dtest=SchemaPerThread*                  # Run both test classes

# Test with coverage
mvn test jacoco:report -Dtest=SchemaPerThread*    # Integration tests with coverage
```
