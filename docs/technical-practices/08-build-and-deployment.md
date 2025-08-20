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
mvn test -Dtest="*Selenium*"                                  # Pattern matching
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

### 4. Testing with Different Configurations
```bash
# Selenium testing with various options
mvn test -Dtest=ProductManagementSeleniumTest                                    # Default (headless)
mvn test -Dtest=ProductManagementSeleniumTest -Dselenium.headless=false         # Visible browser
mvn test -Dtest=ProductManagementSeleniumTest -Dselenium.recording.enabled=true # With recording
mvn test -Dtest=ProductManagementSeleniumTest -Dselenium.browser=firefox        # Different browser
```
