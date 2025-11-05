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
mvn test -Dtest=**/functional/**/CustomerManagementSuccessTest

# Run functional tests with visible browser window (for debugging)
mvn test -Dtest=**/functional/**/*Test -Dplaywright.headless=false

# Run functional tests with Firefox instead of Chromium
mvn test -Dtest=**/functional/**/*Test -Dplaywright.browser=firefox

# Run functional tests with WebKit (Safari engine)
mvn test -Dtest=**/functional/**/*Test -Dplaywright.browser=webkit

# Run functional tests with video recording enabled
mvn test -Dtest=**/functional/**/*Test -Dplaywright.record=true

# Run functional tests with custom recording directory
mvn test -Dtest=**/functional/**/*Test -Dplaywright.record=true -Dplaywright.record.dir=test-recordings

# Run approval workflow documentation tests with screenshots and videos (V2.0)
mvn test -Dtest=ApprovalWorkflowTutorialTest -Dplaywright.headless=false -Dplaywright.slowmo=2000 -Dplaywright.record=true

# Run approval workflow tests (includes success, rejection, queue management)
mvn test -Dtest=ApprovalWorkflowSuccessTest

# Run tests with both screenshots and videos enabled
mvn test -Dtest=**/functional/**/*Test -Dplaywright.record=true -Dplaywright.screenshot.dir=target/screenshots

# Generate approval workflow user manual (V2.0)
mvn exec:java -Dexec.mainClass="id.ac.tazkia.minibank.util.ApprovalWorkflowDocGenerator"

# Combined options for functional debugging
mvn test -Dtest=**/functional/success/*Test -Dplaywright.headless=false -Dplaywright.browser=chromium

# Combined options with recording for debugging
mvn test -Dtest=**/functional/success/*Test -Dplaywright.headless=false -Dplaywright.record=true

# Run tests with slow motion for better observation (500ms delay between actions)
mvn test -Dtest=**/functional/success/*Test -Dplaywright.headless=false -Dplaywright.slowmo=500

# Ultimate debugging setup: visible, slow, recorded
mvn test -Dtest=**/functional/success/*Test -Dplaywright.headless=false -Dplaywright.slowmo=1000 -Dplaywright.record=true

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
| **Slow Motion** | 0ms | `-Dplaywright.slowmo=500` |
| **Recording** | false | `-Dplaywright.record=true` |
| **Recording Directory** | target/playwright-recordings | `-Dplaywright.record.dir=test-recordings` |
| **Screenshot Directory** | target/playwright-screenshots | `-Dplaywright.screenshot.dir=target/screenshots` |
| **Test Pattern** | All | `-Dtest=**/functional/success/*Test` |

### Human-Readable File Naming ✅ NEW

The enhanced Playwright framework now generates human-readable file and folder names:

**Video Files:**
- Format: `YYYY-MM-DD_HH-mm-ss_TestClass_testMethodName.webm`
- Example: `2025-01-15_14-30-25_CustomerManagementSuccessTest_shouldCreatePersonalCustomerSuccessfully.webm`
- Location: `target/playwright-recordings/YYYY-MM-DD_HH-mm-ss_TestClass/videos/`

**Screenshot Files:**
- Format: `YYYY-MM-DD_HH-mm-ss-SSS_TestClass_testMethodName_description.png`
- Example: `2025-01-15_14-30-25-123_CustomerManagementSuccessTest_shouldCreatePersonalCustomerSuccessfully_customer_form_filled.png`
- Location: `target/playwright-screenshots/YYYY-MM-DD_TestClass/screenshots/`

**Directory Structure:**
```
target/
├── playwright-recordings/
│   └── 2025-01-15_14-30-25_CustomerManagementSuccessTest/
│       └── videos/
│           └── 2025-01-15_14-30-25_CustomerManagementSuccessTest_shouldCreatePersonalCustomerSuccessfully.webm
└── playwright-screenshots/
    └── 2025-01-15_CustomerManagementSuccessTest/
        └── screenshots/
            ├── 2025-01-15_14-30-25-123_CustomerManagementSuccessTest_step01_login_page_loaded.png
            ├── 2025-01-15_14-30-25-456_CustomerManagementSuccessTest_step02_customer_form_filled.png
            └── 2025-01-15_14-30-25-789_CustomerManagementSuccessTest_step03_customer_created_success.png
```

### Functional Test Types

| Type | Purpose | Command |
|------|---------|---------|
| **functional** | All scenarios (success + alternate) | `mvn test -Dtest=**/functional/**/*Test` |
| **functional-success** | Success scenarios (happy paths) | `mvn test -Dtest=**/functional/success/*Test` |
| **functional-alternate** | Alternate scenarios (edge cases, errors) | `mvn test -Dtest=**/functional/alternate/*Test` |

### Debugging Features

| Feature | Purpose | Usage |
|---------|---------|-------|
| **Visible Browser** | Watch test execution in real browser | `-Dplaywright.headless=false` |
| **Slow Motion** | Slow down actions for better observation | `-Dplaywright.slowmo=500` (500ms delay) |
| **Video Recording** | Record test execution for analysis | `-Dplaywright.record=true` |
| **Custom Recording Dir** | Specify recording output directory | `-Dplaywright.record.dir=my-recordings` |
| **Cross-Browser Testing** | Test on different browser engines | `-Dplaywright.browser=firefox` or `webkit` |

### Indonesian User Manual Generation ✅ NEW

The framework now includes automated Indonesian user manual generation:

**Features:**
- **Language**: Complete Indonesian language support for banking operations
- **Format**: Professional Markdown documentation with navigation
- **Content**: Step-by-step tutorials with screenshots and videos
- **Target**: Customer Service (CS) role for personal account opening
- **Screenshots**: Automatically embedded with human-readable descriptions
- **Videos**: Linked WebM files with descriptive titles

**One-Stop Script Usage:**
```bash
# Complete solution: runs tests + generates documentation
bash scripts/generate-user-manual.sh

# Options
bash scripts/generate-user-manual.sh --visible    # Shows browser execution (default)
bash scripts/generate-user-manual.sh --fast       # Headless mode for faster execution
bash scripts/generate-user-manual.sh --help       # Show usage help
```

**Manual Steps (if needed):**
```bash
# 1. Generate approval workflow screenshots and videos (V2.0)
mvn test -Dtest=ApprovalWorkflowTutorialTest \
  -Dplaywright.headless=false -Dplaywright.slowmo=2000 -Dplaywright.record=true

# 2. Generate approval workflow user manual from captured media
mvn exec:java -Dexec.mainClass="id.ac.tazkia.minibank.util.ApprovalWorkflowDocGenerator"
```

**Generated Files:**
- `docs/user-manual/README.md` - Index and overview (updated for V2.0)
- `docs/user-manual/panduan-approval-workflow.md` - Complete approval workflow tutorial

**Manual Contents (Version 2.0):**
1. Gambaran Umum (Overview) - Including approval workflow benefits
2. Prasyarat (Prerequisites) - For CS and Branch Manager
3. Alur Kerja Approval (Approval workflow diagram with Mermaid)
4. 11 Langkah Detail (Detailed Steps) with 34 screenshots
   - Bagian 1: Customer Service (Steps 1-5)
   - Bagian 2: Branch Manager (Steps 6-11)
5. Video Tutorial section
6. Status Approval reference tables
7. Tips dan Catatan Penting (Important Tips) for both roles
8. Pemecahan Masalah Umum (Common Troubleshooting)

**Sample Output Size:** ~50KB Markdown with comprehensive approval workflow coverage

### Database Operations
```bash
# Start PostgreSQL container
docker compose up postgres

# Connect to database directly
docker exec -it aplikasi-minibank-postgres-1 psql -U minibank -d pgminibank

# Reset database (stops container and removes volume)
docker compose down -v
```

### Documentation Generation (Version 2.0 - Approval Workflow)

```bash
# Manual steps for approval workflow documentation
# 1. Run approval workflow documentation test to generate screenshots/videos
mvn test -Dtest=ApprovalWorkflowTutorialTest \
  -Dplaywright.headless=false \
  -Dplaywright.slowmo=2000 \
  -Dplaywright.record=true

# 2. Generate approval workflow user manual from captured media
mvn exec:java -Dexec.mainClass="id.ac.tazkia.minibank.util.ApprovalWorkflowDocGenerator"

# View generated documentation
open docs/user-manual/panduan-approval-workflow.md
```

### GitHub Actions & Automated Documentation

The project includes automated documentation generation via GitHub Actions:

```yaml
# Workflow: .github/workflows/maven.yml
# Triggers: Push to main branch
# Output: GitHub Pages deployment with user documentation
```

**Automated Process (Version 2.0):**
1. **Build & Test**: Runs unit/integration tests with parallel execution
2. **Documentation Generation**:
   - Installs Playwright dependencies in CI environment
   - Runs `ApprovalWorkflowTutorialTest` in headless mode
   - Captures 34 screenshots of complete CS + Branch Manager workflow
   - Records videos of approval process
   - Generates comprehensive Indonesian approval workflow manual
3. **GitHub Pages Deployment**:
   - Creates web-friendly HTML versions of documentation
   - Deploys to GitHub Pages for public access
   - Auto-updates on every push to main branch

**Access Documentation:**
- **Public URL**: `https://<username>.github.io/<repository>/` (after first deployment)
- **Local Generated**: `docs/user-manual/panduan-approval-workflow.md`
- **Download Artifacts**: Available in GitHub Actions runs

**Features (Version 2.0):**
- ✅ Automated screenshot capture with human-readable Indonesian filenames
- ✅ Video tutorial generation (WebM format) for each approval step
- ✅ Complete approval workflow documentation (CS + Branch Manager)
- ✅ Indonesian banking documentation with proper terminology
- ✅ Professional HTML styling with Bootstrap CSS
- ✅ Mobile-responsive design for various devices
- ✅ Automatic timestamp updates on each deployment
- 🆕 **NEW**: Dual-role workflow documentation (Customer Service + Branch Manager)
- 🆕 **NEW**: Approval queue management screenshots
- 🆕 **NEW**: Approval/rejection flow with review notes

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